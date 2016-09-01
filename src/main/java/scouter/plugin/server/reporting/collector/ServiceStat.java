package scouter.plugin.server.reporting.collector;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.derby.drda.NetworkServerControl;
import org.apache.derby.tools.ij;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import scouter.lang.TextTypes;
import scouter.lang.ref.INT;
import scouter.lang.ref.LONG;
import scouter.plugin.server.reporting.ReportingPlugin;
import scouter.plugin.server.reporting.vo.AgentInfo;
import scouter.plugin.server.reporting.vo.IpAddress;
import scouter.plugin.server.reporting.vo.Service;
import scouter.plugin.server.reporting.vo.UserAgent;
import scouter.server.Logger;
import scouter.server.db.TextRD;
import scouter.util.DateUtil;
import scouter.util.MeteringUtil;
import scouter.util.MeteringUtil.Handler;
import scouter.util.ThreadUtil;

public class ServiceStat {
	
	public static final String REQUEST_CNT = "request_count";
	public static final String ERROR_CNT = "error_count";
	
	final static class Slot {
		int requestCount;
		int errorCount;
		long elapsed;
        long sqlCount;
        long sqlTime;
	}
	
	private int objHash;
	private int serviceHash;
	private String serviceName;
	private int maxElapsed;
	private int maxSqlCount;
	private int maxSqlTime;
	private int elapsedExceedCount;
	private int requestCount;
	private int errorCount;
	private Map<String, Map<String, Integer>> ipAddr = new HashMap<String, Map<String, Integer>>();
	private Map<Integer, Map<String, Integer>> userAgent = new HashMap<Integer, Map<String, Integer>>();
	private long startTime;
	private long lastAccessTime;
	
	private boolean isProcessing = false;
	private Slot lastSlot = new Slot();
	
	// 5분 슬롯 초기화
	private MeteringUtil<Slot> meter = new MeteringUtil<Slot>(1000, 302) {
		protected Slot create() {
			return new Slot();
		};

		protected void clear(Slot s) {
			s.requestCount = 0;
			s.errorCount = 0;
			s.elapsed = 0L;
			s.sqlCount = 0L;
			s.sqlTime = 0L;
		}
	};
	
	public ServiceStat(int objHash, int serviceHash) {
		this.objHash = objHash;
		this.serviceHash = serviceHash;
		this.startTime = System.currentTimeMillis() / DateUtil.MILLIS_PER_FIVE_MINUTE * DateUtil.MILLIS_PER_FIVE_MINUTE;
	}
	
	/**
	 * <pre>
	 * 10분 동안 사용되지 않으면 메모리에서 제거대상이 된다.
	 * </pre>
	 * 
	 * @return
	 */
	public boolean isPurge() {
		return (System.currentTimeMillis() - lastAccessTime) > (10 * 60 * 1000);
	}

	public void clear() {
		isProcessing = true;
		
		try {
			Slot s = meter.getCurrentBucket();
			lastSlot.requestCount = s.requestCount;
			lastSlot.errorCount = s.errorCount;
			lastSlot.elapsed = s.elapsed;
			lastSlot.sqlCount = s.sqlCount;
			lastSlot.sqlTime = s.sqlTime;
			
			this.maxElapsed = 0;
			this.maxSqlCount = 0;
			this.maxSqlTime = 0;
			this.elapsedExceedCount = 0;
			this.requestCount = 0;
			this.errorCount = 0;
			this.ipAddr = new HashMap<String, Map<String, Integer>>();
			this.userAgent = new HashMap<Integer, Map<String, Integer>>();
			
			// 현재 시간의 (0, 5, 10, 15, ... 50, 55분 단위로 변경)
			this.startTime = System.currentTimeMillis() / DateUtil.MILLIS_PER_FIVE_MINUTE * DateUtil.MILLIS_PER_FIVE_MINUTE;
		} finally {
			isProcessing = false;
		}
	}
	
	public synchronized void add(String serviceName, int elapsed, boolean error, String ipAddress, int agentHash, int sqlCount, int sqlTime) {
		while (true) {
			if (isProcessing) {
				ThreadUtil.sleep(10);
			} else {
				break;
			}
		}
		
		if (serviceName != null) {
			this.serviceName = serviceName;
		}
		
		// 현재 초(second)에 해당하는 슬롯에 데이터 저장
		Slot s = meter.getCurrentBucket();
		s.elapsed += elapsed;
		s.sqlCount += sqlCount;
		s.sqlTime += sqlTime;
		
		// 요청 카운트 증가
		s.requestCount++;
		this.requestCount++;
		
		//System.out.println(serviceName + " [" + meter.getPosition() + ":" + s.requestCount + "] " + getRequestCount(true) + " : " + getRequestCount(false) + " : " + this.requestCount);
		
		// 에러 카운트 증가
		if (error) {
			s.errorCount++;
			this.errorCount = this.errorCount + 1;
		}
		
		// 5분 내 최대 값 갱신
		if (elapsed > maxElapsed) {
			maxElapsed = elapsed;
		}
		if (sqlCount > maxSqlCount) {
			maxSqlCount = sqlCount;
		}
		if (sqlTime > maxSqlTime) {
			maxSqlTime = sqlTime;
		}
		
		// 허용 시간 초과 카운트 증가
		if (elapsed >= ReportingPlugin.conf.getInt("ext_plugin_reporting_max_elapsed_time", 5000)) {
			this.elapsedExceedCount++;
		}
		
		// Remote IP Address 정보 추가
		putIpAddr(ipAddress, error);
		
		// UserAgent 정보 추가
		putUserAgent(agentHash, error);
		
		this.lastAccessTime = System.currentTimeMillis();
	}
	
	private synchronized void putIpAddr(String ipAddress, boolean isError) {
		if (ipAddress == null) {
			ipAddress = "N/A";
		}
		
		Map<String, Integer> map = this.ipAddr.get(ipAddress);
		
		if (map == null) {
			map = new HashMap<String, Integer>();
		}
		
		int requestCount = map.get(REQUEST_CNT) == null ? 0 : map.get(REQUEST_CNT);
		int errorCount = map.get(ERROR_CNT) == null ? 0 : map.get(ERROR_CNT);
		
		requestCount++;
		
		if (isError) {
			errorCount++;
		}
		
		map.put(REQUEST_CNT, requestCount);
		map.put(ERROR_CNT, errorCount);
		
		this.ipAddr.put(ipAddress, map);
	}
	
	private synchronized void putUserAgent(int agentHash, boolean isError) {		
		Map<String, Integer> map = this.userAgent.get(agentHash);
		
		if (map == null) {
			map = new HashMap<String, Integer>();
		}
		
		int requestCount = map.get(REQUEST_CNT) == null ? 0 : map.get(REQUEST_CNT);
		int errorCount = map.get(ERROR_CNT) == null ? 0 : map.get(ERROR_CNT);
		
		requestCount++;
		
		if (isError) {
			errorCount++;
		}
		
		map.put(REQUEST_CNT, requestCount);
		map.put(ERROR_CNT, errorCount);
		
		this.userAgent.put(agentHash, map);
	}
	
	public Service getService() {
		return getService(false);
	}
	
	private Service getService(boolean isClear) {
		if (this.requestCount == 0) {
			return null;
		}
		
		isProcessing = true;
		
		try {
			if (this.serviceName == null) {
				this.serviceName = TextRD.getString(DateUtil.yyyymmdd(this.startTime), TextTypes.SERVICE, this.serviceHash);
			}
			
			if (this.serviceName == null) {
				return null;
			}
			
			Service service = new Service();
			service.setDate(this.startTime);
			service.setObject_hash(this.objHash);
			service.setService_hash(this.serviceHash);
			service.setLog_dt(new java.sql.Date(this.startTime));
			service.setLog_tm(new Time(this.startTime));
			service.setService_name(this.serviceName);
			service.setElapsed_avg(getElapsedAvg(isClear));
			service.setSql_count_avg(getSqlCountAvg(isClear));
			service.setSql_time_avg(getSqlTimeAvg(isClear));
			service.setElapsed_max(this.maxElapsed);
			service.setSql_count_max(this.maxSqlCount);
			service.setSql_time_max(this.maxSqlTime);
			service.setRequest_count(getRequestCount(isClear));
			service.setError_count(getErrorCount(isClear));
			
			List<?> keys = null;
			Map<String, Integer> value = null;
			IpAddress ia = null;
			UserAgent ua = null;
			
			keys = new ArrayList<String>(ipAddr.keySet());
			for (Object key : keys) {
				value = ipAddr.get(key);
				
				ia = new IpAddress();
				ia.setDate(this.startTime);
				ia.setService_hash(this.serviceHash);
				ia.setLog_dt(new java.sql.Date(this.startTime));
				ia.setLog_tm(new Time(this.startTime));
				ia.setIp_address((String) key);
				ia.setRequest_count(value.get(REQUEST_CNT));
				ia.setError_count(value.get(ERROR_CNT));
				
				service.addIpAddress(ia);
			}
			
			keys = new ArrayList<Integer>(userAgent.keySet());
			String agent = null;
			for (Object key : keys) {
				value = userAgent.get(key);
				
				ua = new UserAgent();
				ua.setDate(this.startTime);
				ua.setService_hash(this.serviceHash);
				ua.setLog_dt(new java.sql.Date(this.startTime));
				ua.setLog_tm(new Time(this.startTime));
				
				agent = TextRD.getString(DateUtil.yyyymmdd(this.startTime), TextTypes.USER_AGENT, (Integer) key);
				
				if (agent == null) {
					agent = "N/A";
				}
				
				ua.setUser_agent(agent);
				ua.setRequest_count(value.get(REQUEST_CNT));
				ua.setError_count(value.get(ERROR_CNT));
				
				service.addUserAgent(ua);
			}
			
			//System.out.println("Request Count : " + service.getRequest_count() + " : " + this.requestCount);
			//System.out.println("Error Count : " + service.getError_count() + " : " + this.errorCount);
			
			service.setElapsed_exceed_count(elapsedExceedCount);

			return service;
		} finally {
			isProcessing = false;
		}
	}
	
	public Service getServiceAndClear() {
		Service service = getService(true);
		clear();
		
		return service;
	}

	public int getElapsedAvg(boolean isClear) {
		final LONG sum = new LONG();
        final INT cnt = new INT();
        
        int period = isClear ? 301 : 300;
        
		meter.search(period, new Handler<Slot>() {
			public void process(Slot s) {
				sum.value += s.elapsed;
				cnt.value += s.requestCount;
			}
		});
		
		if (isClear) {
			sum.value -= lastSlot.elapsed;
			cnt.value -= lastSlot.requestCount;
		}
		
		return (int) ((cnt.value == 0) ? 0 : sum.value / cnt.value);
	}

	public int getSqlCountAvg(boolean isClear) {
		final LONG sum = new LONG();
        final INT cnt = new INT();
        
        int period = isClear ? 301 : 300;
        
		meter.search(period, new Handler<Slot>() {
			public void process(Slot s) {
				sum.value += s.sqlCount;
				cnt.value += s.requestCount;
			}
		});
		
		if (isClear) {
			sum.value -= lastSlot.sqlCount;
			cnt.value -= lastSlot.requestCount;
		}
		
		return (int) ((cnt.value == 0) ? 0 : sum.value / cnt.value);
	}

	public int getSqlTimeAvg(boolean isClear) {
		final LONG sum = new LONG();
        final INT cnt = new INT();
        
        int period = isClear ? 301 : 300;
        
		meter.search(period, new Handler<Slot>() {
			public void process(Slot s) {
				sum.value += s.sqlTime;
				cnt.value += s.requestCount;
			}
		});
		
		if (isClear) {
			sum.value -= lastSlot.sqlTime;
			cnt.value -= lastSlot.requestCount;
		}
		
		return (int) ((cnt.value == 0) ? 0 : sum.value / cnt.value);
	}

	public int getRequestCount(boolean isClear) {
		final INT sum = new INT();
        
        int period = isClear ? 301 : 300;
        
		meter.search(period, new Handler<Slot>() {
			public void process(Slot s) {
				sum.value += s.requestCount;
			}
		});
		
		if (isClear) {
			sum.value -= lastSlot.requestCount;
		}
		
		return sum.value;
	}

	public int getErrorCount(boolean isClear) {
		final INT sum = new INT();
        
        int period = isClear ? 301 : 300;
        
		meter.search(period, new Handler<Slot>() {
			public void process(Slot s) {
				sum.value += s.errorCount;
			}
		});
		
		if (isClear) {
			sum.value -= lastSlot.errorCount;
		}
		
		return sum.value;
	}
	
	public <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
		List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(map.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
			public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
				return (o2.getValue()).compareTo(o1.getValue());
			}
		});

		Map<K, V> result = new LinkedHashMap<K, V>();
		for (Map.Entry<K, V> entry : list) {
			result.put(entry.getKey(), entry.getValue());
		}
		
		return result;
	}
	
	public static void main(String[] args) throws Exception {
		PrintWriter writer = new PrintWriter(System.out);
    	OutputStream os = System.out;
		
		// Derby running as client/server mode
		NetworkServerControl server = new NetworkServerControl(InetAddress.getByName("0.0.0.0"), 1527);
		server.start(writer);
		
		SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(ServiceStat.class.getResourceAsStream("/mybatis-config.xml"));
		SqlSession session = sqlSessionFactory.openSession(true);
		
		String date = null;
		boolean initTables = true;
        if (initTables) {
    		for (int i = 1; i <= 31; i++) {
    			if (i < 10) {
    				date = "0" + i;
    			} else {
    				date = i + "";
    			}
    			
				try {
    				session.update("Scouter.dropIpAddress", date);
    			} catch (Exception e) {
    				Logger.println("[IP_ADDRESS_5M_" + date + "] Drop failed.");
	    		}
    			
				try {
    				session.update("Scouter.dropUserAgent", date);
    			} catch (Exception e) {
    				Logger.println("[USER_AGENT_5M_" + date + "] Drop failed.");
	    		}
    			
    			try {
    				session.update("Scouter.dropHostAgent", date);
    			} catch (Exception e) {
    				Logger.println("[HOST_AGENT_5M_" + date + "] Drop failed.");
    			}
    			
    			try {
    				session.update("Scouter.dropJavaAgent", date);
    			} catch (Exception e) {
    				Logger.println("[JAVA_AGENT_5M_" + date + "] Drop failed.");
	    		}
    			
				try {
    				session.update("Scouter.dropService", date);
    			} catch (Exception e) {
    				Logger.println("[SERVICE_5M_" + date + "] Drop failed.");
	    		}
    			
				try {
    				session.update("Scouter.dropSql", date);
    			} catch (Exception e) {
    				Logger.println("[SQL_5M_" + date + "] Drop failed.");
	    		}
    			
				try {
    				session.update("Scouter.dropAlert", date);
    			} catch (Exception e) {
    				Logger.println("[ALERT_" + date + "] Drop failed.");
	    		}
    		}
    		
    		try {
    			session.update("Scouter.dropSqlInfo");
    		} catch (Exception e) {
				Logger.println("[SQL_INFO_TBL] Drop failed.");
    		}
    		
    		try {
    			session.update("Scouter.dropAgentInfo");
    		} catch (Exception e) {
				Logger.println("[AGENT_INFO_TBL] Drop failed.");
    		}

    		try {
    			session.getConnection().prepareStatement("DROP TABLE TIME_TBL").execute();
    		} catch (Exception e) {
				Logger.println("[TIME_TBL] Drop failed.");
    		}
        }
		
        DatabaseMetaData dbmd = session.getConnection().getMetaData();
        ResultSet rs = null;
        
	    /***********************************************
	     *                                             *
	     *  Check if required tables are exist or not. *
	     *                                             *
	     ***********************************************/
        
        // 1. TIME_TBL
        rs = dbmd.getTables(null, "APP", "TIME_TBL", null);
		
        if (!rs.next()) {
        	// create the TIME_TBL & insert rows
			ij.runScript(session.getConnection(), ReportingPlugin.class.getResourceAsStream("/init_time_table.sql"), "UTF-8", os, "UTF-8");
        }
        
        // 2. AGENT_INFO_TBL
        rs = dbmd.getTables(null, "APP", "AGENT_INFO_TBL", null);
        
        if (!rs.next()) {
        	// create the AGENT_INFO_TBL
			try {
				session.update("Scouter.createAgentInfo");
			} catch (Exception e) {
				System.err.println("[AGENT_INFO_TBL] Already exists.");
    		}
        }
        
        // 3. SQL_INFO_TBL
        rs = dbmd.getTables(null, "APP", "SQL_INFO_TBL", null);
        
        if (!rs.next()) {
        	// create the SQL_INFO_TBL
			try {
				session.update("Scouter.createSqlInfo");
			} catch (Exception e) {
				System.err.println("[SQL_INFO_TBL] Already exists.");
    		}
        }
        
        // 4. HOST_AGENT
        rs = dbmd.getTables(null, "APP", "HOST_AGENT_5M_01", null);		        
        
        if (!rs.next()) {
	        // create HOST_AGENT tables
			for (int i = 1; i <= 31; i++) {
				if (i < 10) {
					date = "0" + i;
				} else {
					date = i + "";
				}
				
				try {
					session.update("Scouter.createHostAgent", date);
				} catch (Exception e) {
					System.err.println("[HOST_AGENT_5M_" + date + "] Already exists.");
	    		}
			}
        }
        
        // 5. JAVA_AGENT
        rs = dbmd.getTables(null, "APP", "JAVA_AGENT_5M_01", null);		        
        
        if (!rs.next()) {
	        // create JAVA_AGENT tables
			for (int i = 1; i <= 31; i++) {
				if (i < 10) {
					date = "0" + i;
				} else {
					date = i + "";
				}
				
				try {
					session.update("Scouter.createJavaAgent", date);
				} catch (Exception e) {
					System.err.println("[JAVA_AGENT_5M_" + date + "] Already exists.");
	    		}
			}
        }
        
        // 6. SERVICE
        rs = dbmd.getTables(null, "APP", "SERVICE_5M_01", null);		        
        
        if (!rs.next()) {
	        // create SERVICE tables
			for (int i = 1; i <= 31; i++) {
				if (i < 10) {
					date = "0" + i;
				} else {
					date = i + "";
				}
				
				try {
					session.update("Scouter.createService", date);
				} catch (Exception e) {
					System.err.println("[SERVICE_5M_" + date + "] Already exists.");
	    		}
			}
        }
        
        // 7. IP_ADDRESS
        rs = dbmd.getTables(null, "APP", "IP_ADDRESS_5M_01", null);		        
        
        if (!rs.next()) {
	        // create IP_ADDRESS tables
			for (int i = 1; i <= 31; i++) {
				if (i < 10) {
					date = "0" + i;
				} else {
					date = i + "";
				}
				
				try {
					session.update("Scouter.createIpAddress", date);
					session.update("Scouter.alterIpAddress", date);
				} catch (Exception e) {
					System.err.println("[IP_ADDRESS_5M_" + date + "] Already exists.");
	    		}
			}
        }
        
        // 8. USER_AGENT
        rs = dbmd.getTables(null, "APP", "USER_AGENT_5M_01", null);		        
        
        if (!rs.next()) {
	        // create USER_AGENT tables
			for (int i = 1; i <= 31; i++) {
				if (i < 10) {
					date = "0" + i;
				} else {
					date = i + "";
				}
				
				try {
					session.update("Scouter.createUserAgent", date);
					session.update("Scouter.alterUserAgent", date);
				} catch (Exception e) {
					System.err.println("[USER_AGENT_5M_" + date + "] Already exists.");
	    		}
			}
        }
        
        // 9. SQL
        rs = dbmd.getTables(null, "APP", "SQL_5M_01", null);		        
        
        if (!rs.next()) {
	        // create SQL tables
			for (int i = 1; i <= 31; i++) {
				if (i < 10) {
					date = "0" + i;
				} else {
					date = i + "";
				}
				
				try {
					session.update("Scouter.createSql", date);
				} catch (Exception e) {
					System.err.println("[SQL_5M_" + date + "] Already exists.");
	    		}
			}
        }
        
        // 10. ALERT
        rs = dbmd.getTables(null, "APP", "ALERT_01", null);		        
        
        if (!rs.next()) {
	        // create ALERT tables
			for (int i = 1; i <= 31; i++) {
				if (i < 10) {
					date = "0" + i;
				} else {
					date = i + "";
				}
				
				try {
					session.update("Scouter.createAlert", date);
				} catch (Exception e) {
					System.err.println("[ALERT_" + date + "] Already exists.");
	    		}
			}
        }
		
		final ServiceStat[] ss = new ServiceStat[] { new ServiceStat(12345, 111), 
				new ServiceStat(12345, 222), 
				new ServiceStat(12345, 333), 
				new ServiceStat(12345, 444), 
				new ServiceStat(12345, 555) };
		
		AgentInfo agentInfo = new AgentInfo();
		agentInfo.setObject_hash(12345);
		agentInfo.setObject_name("test");
		agentInfo.setObject_type("java");
		agentInfo.setIp_address("127.0.0.1");
		session.insert("Scouter.insertAgentInfo", agentInfo);
		
		final long until = System.currentTimeMillis() + (60 * 60 * 1000);

		new Thread() {
			public void run() {
				while (System.currentTimeMillis() < until) {
					for (ServiceStat s : ss) {
						s.add("/" + s.serviceHash + ".jsp",
								9100 + new Random().nextInt(1000), 
								new Random().nextInt(10) < 1 ? true : false, 
								"192.168.0." + (new Random().nextInt(100) + 2), 
								new Random().nextInt(10),
								12345, 
								300 + new Random().nextInt(100));
						ThreadUtil.sleep(100);
					}
				}
			};
		}.start();
		
		long time = System.currentTimeMillis();
		long last_sent = time / DateUtil.MILLIS_PER_FIVE_MINUTE;
		
		while (System.currentTimeMillis() < until) {
			time = System.currentTimeMillis();
			long now = time / DateUtil.MILLIS_PER_FIVE_MINUTE;
	
			if (now != last_sent) {
				last_sent = now;
				
				time = (time - 10000) / DateUtil.MILLIS_PER_FIVE_MINUTE * DateUtil.MILLIS_PER_FIVE_MINUTE;

				Service service = null;
				for (ServiceStat s : ss) {
					service = s.getServiceAndClear();

					System.err.println(new Date(time) + ":" + service);
					session.insert("Scouter.insertService", service);
					
					System.out.println("IP_ADDRESS : " + service.getIpAddressList());
					session.insert("Scouter.insertIpAddress", service);
					
					System.out.println("USER_AGENT : " + service.getUserAgentList());
					session.insert("Scouter.insertUserAgent", service);
				}
			}
			
			ThreadUtil.sleep(1000);
		}
	}
}