package scouter.plugin.server.reporting;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Time;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.output.WriterOutputStream;
import org.apache.derby.drda.NetworkServerControl;
import org.apache.derby.tools.ij;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import scouter.lang.AlertLevel;
import scouter.lang.SummaryEnum;
import scouter.lang.TextTypes;
import scouter.lang.TimeTypeEnum;
import scouter.lang.counters.CounterConstants;
import scouter.lang.pack.AlertPack;
import scouter.lang.pack.ObjectPack;
import scouter.lang.pack.PerfCounterPack;
import scouter.lang.pack.SummaryPack;
import scouter.lang.pack.XLogPack;
import scouter.lang.plugin.PluginConstants;
import scouter.lang.plugin.annotation.ServerPlugin;
import scouter.lang.value.ListValue;
import scouter.lang.value.Value;
import scouter.plugin.server.reporting.collector.HostAgentStat;
import scouter.plugin.server.reporting.collector.JavaAgentStat;
import scouter.plugin.server.reporting.collector.ServiceStat;
import scouter.plugin.server.reporting.report.AbstractReport;
import scouter.plugin.server.reporting.report.AlertReport;
import scouter.plugin.server.reporting.report.HostReport;
import scouter.plugin.server.reporting.report.JavaReport;
import scouter.plugin.server.reporting.report.OperationReport;
import scouter.plugin.server.reporting.report.ServiceReport;
import scouter.plugin.server.reporting.task.HostAgentTask;
import scouter.plugin.server.reporting.task.JavaAgentTask;
import scouter.plugin.server.reporting.task.ServiceTask;
import scouter.plugin.server.reporting.vo.AgentInfo;
import scouter.plugin.server.reporting.vo.Alert;
import scouter.plugin.server.reporting.vo.Sql;
import scouter.plugin.server.reporting.vo.SqlInfo;
import scouter.server.Configure;
import scouter.server.CounterManager;
import scouter.server.Logger;
import scouter.server.core.AgentManager;
import scouter.server.db.TextRD;
import scouter.util.DateUtil;
import scouter.util.HashUtil;

public class ReportingPlugin {
    public static Configure conf = Configure.getInstance();
    
    private static AtomicInteger ai = new AtomicInteger(0);
    private static volatile NetworkServerControl server;
	private static SqlSession session;
	
	// main key is objHash, sub key is serviceHash (used in PluginConstants.PLUGIN_SERVER_XLOG)
	private static Map<Integer, Map<Integer, ServiceStat>> serviceStatMap = new ConcurrentHashMap<Integer, Map<Integer, ServiceStat>>();
	private static Map<Integer, JavaAgentStat> javaAgentStatMap = new ConcurrentHashMap<Integer, JavaAgentStat>();
	private static Map<Integer, HostAgentStat> hostAgentStatMap = new ConcurrentHashMap<Integer, HostAgentStat>();
    
    public ReportingPlugin() {
    	PrintWriter writer = new PrintWriter(System.out);
    	OutputStream os = System.out;
    	
    	if (conf.getBoolean("ext_plugin_reporting_logging_enabled", false)) {
        	writer = Logger.pw();
        	os = new WriterOutputStream(writer, "UTF-8");
        }
    	
    	if (ai.incrementAndGet() == 1) {
	    	try {
	    		// ======================================================================
	    		// TODO 두 개 이상의 Collector Server에 대한 고려. (데이터 수집은 한군데에서 할 경우)
	    		// ======================================================================
	    		
	    		// Derby running as client/server mode
	    		server = new NetworkServerControl(InetAddress.getByName("0.0.0.0"), 1527);
				server.start(writer);
				
				Logger.println("[SCOUTER-X] 1. Derby server launched.");
				
				// Create a SqlSession
				SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(ReportingPlugin.class.getResourceAsStream("/mybatis-config.xml"));
			    session = sqlSessionFactory.openSession(true);
				
				Logger.println("[SCOUTER-X] 2. SqlSession was opened.");

	        	String date = null;
		        if (conf.getBoolean("ext_plugin_reporting_drop_table", true)) {
		    		for (int i = 1; i <= 31; i++) {
		    			if (i < 10) {
		    				date = "0" + i;
		    			} else {
		    				date = i + "";
		    			}
		    			
	    				try {
		    				session.update("Scouter.dropIpAddress", date);
		    			} catch (Exception e) {
		    				Logger.println("[SCOUTER-X] [IP_ADDRESS_5M_" + date + "] Drop failed.");
			    		}
		    			
	    				try {
		    				session.update("Scouter.dropUserAgent", date);
		    			} catch (Exception e) {
		    				Logger.println("[SCOUTER-X] [USER_AGENT_5M_" + date + "] Drop failed.");
			    		}
		    			
		    			try {
		    				session.update("Scouter.dropHostAgent", date);
		    			} catch (Exception e) {
		    				Logger.println("[SCOUTER-X] [HOST_AGENT_5M_" + date + "] Drop failed.");
		    			}
		    			
		    			try {
		    				session.update("Scouter.dropJavaAgent", date);
		    			} catch (Exception e) {
		    				Logger.println("[SCOUTER-X] [JAVA_AGENT_5M_" + date + "] Drop failed.");
			    		}
		    			
	    				try {
		    				session.update("Scouter.dropService", date);
		    			} catch (Exception e) {
		    				Logger.println("[SCOUTER-X] [SERVICE_5M_" + date + "] Drop failed.");
			    		}
		    			
	    				try {
		    				session.update("Scouter.dropSql", date);
		    			} catch (Exception e) {
		    				Logger.println("[SCOUTER-X] [SQL_5M_" + date + "] Drop failed.");
			    		}
		    			
	    				try {
		    				session.update("Scouter.dropAlert", date);
		    			} catch (Exception e) {
		    				Logger.println("[SCOUTER-X] [ALERT_" + date + "] Drop failed.");
			    		}
		    		}
		    		
		    		try {
		    			session.update("Scouter.dropSqlInfo");
		    		} catch (Exception e) {
	    				Logger.println("[SCOUTER-X] [SQL_INFO_TBL] Drop failed.");
		    		}
		    		
		    		try {
		    			session.update("Scouter.dropAgentInfo");
		    		} catch (Exception e) {
	    				Logger.println("[SCOUTER-X] [AGENT_INFO_TBL] Drop failed.");
		    		}

		    		try {
		    			session.getConnection().prepareStatement("DROP TABLE TIME_TBL").execute();
		    		} catch (Exception e) {
	    				Logger.println("[SCOUTER-X] [TIME_TBL] Drop failed.");
		    		}
					
					Logger.println("[SCOUTER-X] 3. Drop all tables.");
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
				
				Logger.println("[SCOUTER-X] 4. Check & Create TIME_TBL.");
		        
		        // 2. AGENT_INFO_TBL
		        rs = dbmd.getTables(null, "APP", "AGENT_INFO_TBL", null);
		        
		        if (!rs.next()) {
		        	// create the AGENT_INFO_TBL
					try {
						session.update("Scouter.createAgentInfo");
					} catch (Exception e) {
	    				println("[SCOUTER-X] [AGENT_INFO_TBL] Already exists.");
		    		}
		        }
				
				Logger.println("[SCOUTER-X] 5. Check & Create AGENT_INFO_TBL.");
		        
		        // 3. SQL_INFO_TBL
		        rs = dbmd.getTables(null, "APP", "SQL_INFO_TBL", null);
		        
		        if (!rs.next()) {
		        	// create the SQL_INFO_TBL
					try {
						session.update("Scouter.createSqlInfo");
					} catch (Exception e) {
	    				println("[SCOUTER-X] [SQL_INFO_TBL] Already exists.");
		    		}
		        }
				
				Logger.println("[SCOUTER-X] 6. Check & Create SQL_INFO_TBL.");
		        
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
		    				println("[SCOUTER-X] [HOST_AGENT_5M_" + date + "] Already exists.");
			    		}
					}
		        }
				
				Logger.println("[SCOUTER-X] 7. Check & Create HOST_AGENT_5M.");
		        
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
		    				println("[SCOUTER-X] [JAVA_AGENT_5M_" + date + "] Already exists.");
			    		}
					}
		        }
				
				Logger.println("[SCOUTER-X] 8. Check & Create JAVA_AGENT_5M.");
		        
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
		    				println("[SCOUTER-X] [SERVICE_5M_" + date + "] Already exists.");
			    		}
					}
		        }
				
				Logger.println("[SCOUTER-X] 9. Check & Create SERVICE_5M.");
		        
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
		    				println("[SCOUTER-X] [IP_ADDRESS_5M_" + date + "] Already exists.");
			    		}
					}
		        }
				
				Logger.println("[SCOUTER-X] 10. Check & Create IP_ADDRESS_5M.");
		        
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
		    				println("[SCOUTER-X] [USER_AGENT_5M_" + date + "] Already exists.");
			    		}
					}
		        }
				
				Logger.println("[SCOUTER-X] 11. Check & Create USER_AGENT_5M.");
		        
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
		    				println("[SCOUTER-X] [SQL_5M_" + date + "] Already exists.");
			    		}
					}
		        }
				
				Logger.println("[SCOUTER-X] 12. Check & Create SQL_5M.");
		        
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
		    				println("[SCOUTER-X] [ALERT_" + date + "] Already exists.");
			    		}
					}
		        }
				
				Logger.println("[SCOUTER-X] 13. Check & Create ALERT.");
			} catch (Exception e) {
				Logger.printStackTrace(e);
			}
	    	
	    	ScheduledExecutorService serviceTaskExecutor = Executors.newScheduledThreadPool(3);
	    	
	    	long initDelay = ((System.currentTimeMillis() / DateUtil.MILLIS_PER_FIVE_MINUTE) + 1) * DateUtil.MILLIS_PER_FIVE_MINUTE - System.currentTimeMillis();
	    	
	    	// intiDelay 후 5분마다 각 Task 실행
	    	serviceTaskExecutor.scheduleAtFixedRate(new HostAgentTask(session, hostAgentStatMap), initDelay + 50, DateUtil.MILLIS_PER_FIVE_MINUTE, TimeUnit.MILLISECONDS);
	    	serviceTaskExecutor.scheduleAtFixedRate(new JavaAgentTask(session, javaAgentStatMap), initDelay + 50, DateUtil.MILLIS_PER_FIVE_MINUTE, TimeUnit.MILLISECONDS);
	    	serviceTaskExecutor.scheduleAtFixedRate(new ServiceTask(session, serviceStatMap), initDelay + 10, DateUtil.MILLIS_PER_FIVE_MINUTE, TimeUnit.MILLISECONDS);
	    	
	    	// SQL 정보는 5분 단위 SummaryPack으로부터 데이터가 수신될때 DB에 직접 Insert한다.
	    	// Alert 정보는 AlertPack으로부터 데이터가 수신될때 DB에 직접 Insert한다.
	    	
	    	// reportingTask는 poi를 사용한 Excel 파일 생성 시 메모리 부하를 고려하여 한 시점에 하나의 스레드만 동작
	    	ScheduledExecutorService reportingTaskExecutor = Executors.newScheduledThreadPool(1);
	    	
	    	// today    
	    	Calendar date = new GregorianCalendar();
	    	
	    	// reset hour, minutes, seconds and millis
	    	date.set(Calendar.HOUR_OF_DAY, 0);
	    	date.set(Calendar.MINUTE, 0);
	    	date.set(Calendar.SECOND, 0);
	    	date.set(Calendar.MILLISECOND, 0);

	    	// next day
	    	date.add(Calendar.DAY_OF_MONTH, 1);
	    	
	    	final boolean forceMonthlyReport = false;
	    	long initialDelay = date.getTimeInMillis() - System.currentTimeMillis();
	    	
	    	// Alert Reporting
	    	reportingTaskExecutor.scheduleAtFixedRate(new Runnable() {
				@Override
				public void run() {
					Calendar cal = Calendar.getInstance();
					cal.add(Calendar.DAY_OF_MONTH, -1);
					
					AbstractReport report = new AlertReport();
					
					// Alert Reporting (Dayily)
					try {
						Logger.println("[SCOUTER-X] Start Daily Alert Report.");
						report.createExcel(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH));
					} catch (Exception e) {
						Logger.printStackTrace(e);
					}
					
					// Alert Reporting (Monthly)
					cal = Calendar.getInstance();
					if (cal.get(Calendar.DAY_OF_MONTH) == 1 || forceMonthlyReport) {
						try {
							cal.add(Calendar.MONTH, -1);
					    	Logger.println("[SCOUTER-X] Start Monthly Alert Report.");
							report.createExcel(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1);
						} catch (Exception e) {
							Logger.printStackTrace(e);
						}
					}
				}
	    	}, 
	    	initialDelay + 5000, DateUtil.MILLIS_PER_DAY, TimeUnit.MILLISECONDS);
	    	
	    	// Host Reporting
	    	reportingTaskExecutor.scheduleAtFixedRate(new Runnable() {
				@Override
				public void run() {
					Calendar cal = Calendar.getInstance();
					cal.add(Calendar.DAY_OF_MONTH, -1);
					
					AbstractReport report = new HostReport();
					
					// Host Reporting (Dayily)
					try {
				    	Logger.println("[SCOUTER-X] Start Daily Host Report.");
						report.createExcel(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH));
					} catch (Exception e) {
						Logger.printStackTrace(e);
					}
					
					// Host Reporting (Monthly)
					cal = Calendar.getInstance();
					if (cal.get(Calendar.DAY_OF_MONTH) == 1 || forceMonthlyReport) {
						try {
							cal.add(Calendar.MONTH, -1);
					    	Logger.println("[SCOUTER-X] Start Monthly Host Report.");
							report.createExcel(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1);
						} catch (Exception e) {
							Logger.printStackTrace(e);
						}
			    	}
				}
	    	}, 
	    	initialDelay + 5000, DateUtil.MILLIS_PER_DAY, TimeUnit.MILLISECONDS);
	    	
	    	// Java Reporting
	    	reportingTaskExecutor.scheduleAtFixedRate(new Runnable() {
				@Override
				public void run() {
					Calendar cal = Calendar.getInstance();
					cal.add(Calendar.DAY_OF_MONTH, -1);
					
					AbstractReport report = new JavaReport();
					
					// Java Reporting (Dayily)
					try {
				    	Logger.println("[SCOUTER-X] Start Daily Java Report.");
						report.createExcel(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH));
					} catch (Exception e) {
						Logger.printStackTrace(e);
					}
					
					// Java Reporting (Monthly)
					cal = Calendar.getInstance();
					if (cal.get(Calendar.DAY_OF_MONTH) == 1 || forceMonthlyReport) {
						try {
							cal.add(Calendar.MONTH, -1);
					    	Logger.println("[SCOUTER-X] Start Monthly Java Report.");
							report.createExcel(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1);
						} catch (Exception e) {
							Logger.printStackTrace(e);
						}
			    	}
				}
	    	}, 
	    	initialDelay + 5000, DateUtil.MILLIS_PER_DAY, TimeUnit.MILLISECONDS);
	    	
	    	// Service Reporting
	    	reportingTaskExecutor.scheduleAtFixedRate(new Runnable() {
				@Override
				public void run() {
					Calendar cal = Calendar.getInstance();
					cal.add(Calendar.DAY_OF_MONTH, -1);
					
					AbstractReport report = new ServiceReport();
					
					// Service Reporting (Dayily)
					try {
				    	Logger.println("[SCOUTER-X] Start Daily Service Report.");
						report.createExcel(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH));
					} catch (Exception e) {
						Logger.printStackTrace(e);
					}					
					
					// Service Reporting (Monthly)
					cal = Calendar.getInstance();
					if (cal.get(Calendar.DAY_OF_MONTH) == 1 || forceMonthlyReport) {
						try {
							cal.add(Calendar.MONTH, -1);
					    	Logger.println("[SCOUTER-X] Start Monthly Service Report.");
							report.createExcel(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1);
						} catch (Exception e) {
							Logger.printStackTrace(e);
						}
			    	}
				}
	    	}, 
	    	initialDelay + 5000, DateUtil.MILLIS_PER_DAY, TimeUnit.MILLISECONDS);
	    	
	    	// Application Operation Reporting
	    	reportingTaskExecutor.scheduleAtFixedRate(new Runnable() {
				@Override
				public void run() {
					Calendar cal = Calendar.getInstance();
					cal.add(Calendar.MONTH, -1);
					
					OperationReport report = new OperationReport();
					
					// Application Operation Reporting (Monthly)
					if (cal.get(Calendar.DAY_OF_MONTH) == 1 || forceMonthlyReport) {
						try {
					    	Logger.println("[SCOUTER-X] Start Monthly Application Operation Report.");
							report.createExcel(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1);
						} catch (Exception e) {
							Logger.printStackTrace(e);
						}
			    	}
				}
	    	}, 
	    	initialDelay + 5000, DateUtil.MILLIS_PER_DAY, TimeUnit.MILLISECONDS);
    	} else {
    		while (true) {
    			if (server != null && session != null) {
    				break;
    			}
    			
    			try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// ignore
				}
    		}
    	}
	}
    
    /**
     * AlertPack 발생 시 처리
     * @param pack
     */
    @ServerPlugin(PluginConstants.PLUGIN_SERVER_ALERT)
    public void alert(AlertPack pack) {
    	try {
			Alert alert = new Alert();

			alert.setDate(pack.time);
			alert.setObject_hash(pack.objHash);
			alert.setLog_dt(new java.sql.Date(pack.time));
			alert.setLog_tm(new Time(pack.time));
			alert.setLevel(AlertLevel.getName(pack.level));
			alert.setTitle(pack.title);
			alert.setMessage(pack.message);
			
			session.insert("Scouter.insertAlert", alert);
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
    	
    	if (pack.title.equals("INACTIVE_OBJECT")) {
    		AgentInfo agentInfo = selectAgentInfo(pack.objHash);
    		agentInfo.setLast_down_time(new Date(pack.time));
    		
    		updateAgentInfo(agentInfo);
    	}
    }

    /**
     * PerfCounterPack 발생 시 처리
     * @param pack
     */
    @ServerPlugin(PluginConstants.PLUGIN_SERVER_COUNTER)
    public void counter(PerfCounterPack pack) {
        String objName = pack.objName;
        int objHash = HashUtil.hash(objName);
        String objType = null;
        String objFamily = null;

        if (AgentManager.getAgent(objHash) != null) {
        	objType = AgentManager.getAgent(objHash).objType;
        }
        
        if (objType != null) {
        	objFamily = CounterManager.getInstance().getCounterEngine().getObjectType(objType).getFamily().getName();
        }
        
        // objFamily가 host인 경우
        if (CounterConstants.FAMILY_HOST.equals(objFamily)) {
        	if (hostAgentStatMap.get(objHash) == null) {
        		hostAgentStatMap.put(objHash, new HostAgentStat(objHash));
        	}
        	
        	if (pack.timetype == TimeTypeEnum.REALTIME) {
            	hostAgentStatMap.get(objHash).addMax(pack.data.getFloat(CounterConstants.HOST_CPU), 
            			pack.data.getInt(CounterConstants.HOST_MEM_TOTAL), 
            			pack.data.getFloat(CounterConstants.HOST_MEM), 
            			pack.data.getInt(CounterConstants.HOST_MEM_USED), 
            			pack.data.getInt(CounterConstants.HOST_NET_TX_BYTES), 
            			pack.data.getInt(CounterConstants.HOST_NET_RX_BYTES), 
            			pack.data.getInt(CounterConstants.HOST_DISK_READ_BYTES), 
            			pack.data.getInt(CounterConstants.HOST_DISK_WRITE_BYTES));
        	} else if (pack.timetype == TimeTypeEnum.FIVE_MIN) {
        		// NET_TX, NET_RX, DISK_READ, DISK_WRITE 정보는 FIVE_MIN에 포함되지 않음.
            	hostAgentStatMap.get(objHash).addAvg(pack.data.getFloat(CounterConstants.HOST_CPU), 
            			pack.data.getFloat(CounterConstants.HOST_MEM), 
            			pack.data.getInt(CounterConstants.HOST_MEM_USED));
            }
        }
        
        // objFamily가 javaee인 경우
        if (CounterConstants.FAMILY_JAVAEE.equals(objFamily)) {
        	if (javaAgentStatMap.get(objHash) == null) {
        		javaAgentStatMap.put(objHash, new JavaAgentStat(objHash));
        	}
        	
        	if (pack.timetype == TimeTypeEnum.REALTIME) {
        		// JAVA_HEAP_TOT_USAGE 정보가 없는 PerfCounterPack은 host agent가 동작중에 PROC_CPU 정보를 보내주는 경우와, FIVE_MIN 밖에 없음.
        		// PROC_CPU 정보는 수집 대상이 아님.
            	ListValue lv = pack.data.getList(CounterConstants.JAVA_HEAP_TOT_USAGE);
            	
            	if (lv != null && lv.size() > 0) {
                    javaAgentStatMap.get(objHash).addMax(pack.data.getInt(CounterConstants.WAS_ACTIVE_SERVICE), 
                    		lv.getFloat(0), 
                    		pack.data.getFloat(CounterConstants.JAVA_HEAP_USED), 
                    		pack.data.getInt(CounterConstants.WAS_RECENT_USER), 
                    		pack.data.getInt(CounterConstants.WAS_SERVICE_COUNT), 
                    		pack.data.getFloat(CounterConstants.WAS_APICALL_TPS), 
                    		pack.data.getFloat(CounterConstants.WAS_SQL_TPS), 
                    		pack.data.getFloat(CounterConstants.WAS_TPS));
            	}
        	} else if (pack.timetype == TimeTypeEnum.FIVE_MIN) {
        		if (pack.data.toMap().get(CounterConstants.PROC_CPU) == null) {
	                javaAgentStatMap.get(objHash).addAvg(pack.data.getInt(CounterConstants.WAS_ACTIVE_SERVICE), 
	                		pack.data.getFloat(CounterConstants.JAVA_HEAP_USED), 
	                		pack.data.getInt(CounterConstants.WAS_RECENT_USER), 
	                		pack.data.getInt(CounterConstants.WAS_SERVICE_COUNT), 
                    		pack.data.getFloat(CounterConstants.WAS_APICALL_TPS), 
	                		pack.data.getFloat(CounterConstants.WAS_SQL_TPS), 
	                		pack.data.getFloat(CounterConstants.WAS_TPS));
        		}
        	}
    	}
    }

    /**
     * ObjectPack 발생 시 처리
     * @param pack
     */
    @ServerPlugin(PluginConstants.PLUGIN_SERVER_OBJECT)
    public void object(ObjectPack pack) {
        if (!pack.objType.equals(CounterConstants.REQUESTPROCESS)) {
	        AgentInfo agentInfo = null;
			ObjectPack op = AgentManager.getAgent(pack.objHash);
			boolean isExist = true;
			boolean isDownState = false;
	        
			// Plugin의 loading이 채 끝나기 전에 agent로부터 heartbeat 메시지가 수신되는 경우
			// 해당 Agent의 구동 정보가 누락될 수 있기 때문에 매번 agent의 상태를 조회한다.
			
			agentInfo = selectAgentInfo(pack.objHash);
			if (agentInfo == null) {
				agentInfo = new AgentInfo();
				isExist = false;
			} else {
				Date lastDownTime = agentInfo.getLast_down_time();
				Date lastUpTime = agentInfo.getLast_up_time();
				
				if (lastDownTime != null && lastUpTime != null) {
					if (lastDownTime.getTime() - lastUpTime.getTime() > 0) {
						isDownState = true;
					}
				}
			}
			
			if ((op == null && pack.wakeup == 0L) || op.alive == false || !isExist || isDownState) {
	            println("[AgentInfo] : " + agentInfo);
				
				agentInfo.setObject_hash(pack.objHash);
				agentInfo.setObject_name(pack.objName);
				agentInfo.setObject_type(pack.objType);
				
				if (pack.objType != null) {
	            	String object_family = CounterManager.getInstance().getCounterEngine().getObjectType(pack.objType).getFamily().getName();
	            	agentInfo.setObject_family(object_family);
	            }
				
				agentInfo.setIp_address(pack.address);
				agentInfo.setLast_up_time(new Date(System.currentTimeMillis()));
				
				if (isExist) {
					updateAgentInfo(agentInfo);
				} else {
					insertAgentInfo(agentInfo);
				}
	    	}
        }
    }

    /**
     * SummaryPack 발생 시 처리
     * @param pack
     */
    @ServerPlugin(PluginConstants.PLUGIN_SERVER_SUMMARY)
    public void summary(SummaryPack pack) {
    	if (pack.stype == SummaryEnum.SQL) {
    		ListValue idList = pack.table.getList("id");
    		ListValue countList = pack.table.getList("count");
    		ListValue errorList = pack.table.getList("error");
    		ListValue elapsedList = pack.table.getList("elapsed");
    		
    		SqlInfo sqlInfo = null;
    		Sql sql = null;
    		int sqlHash = 0;
    		String sqlStr = null;
    		
    		Iterator<Value> iter = idList.iterator();
    		Value id, count, error, elapsed = null;
    		int idx = 0;
    		while (iter.hasNext()) {
    			id = iter.next();
    			count = countList.get(idx);
    			error = errorList.get(idx);
    			elapsed = elapsedList.get(idx++);
    			
    			try {
    				sqlHash = ((Number) id.toJavaObject()).intValue();
    				sqlStr = TextRD.getString(DateUtil.yyyymmdd(pack.time), TextTypes.SQL, sqlHash);
    				
    				if (sqlStr != null && sqlStr.length() > 32000) {
    					Logger.println("SQL String is too long to insert SQL_INFO_TBL.");
    					sqlStr = sqlStr.substring(0, 32000) + "...";
    				}
    						
    				sqlInfo = session.selectOne("Scouter.selectSqlInfo", ((Number) id.toJavaObject()).intValue());
    				if (sqlInfo == null) {
    					sqlInfo = new SqlInfo();
    					sqlInfo.setSql_hash(sqlHash);
    					sqlInfo.setSql_str(sqlStr);
    					session.insert("Scouter.insertSqlInfo", sqlInfo);
    				} else if (sqlInfo.getSql_str().equals(sqlStr)) {
    					sqlInfo.setSql_str(sqlStr);
    					session.update("Scouter.updateSqlInfo", sqlInfo);
    				}
    				
					sql = new Sql();
					sql.setDate(pack.time);
					sql.setObject_hash(pack.objHash);
					sql.setSql_hash(((Number) id.toJavaObject()).intValue());
					sql.setLog_dt(new java.sql.Date(pack.time));
					sql.setLog_tm(new Time(pack.time));
					sql.setExecute_count(((Number) count.toJavaObject()).intValue());
					sql.setError_count(((Number) error.toJavaObject()).intValue());
					sql.setElapsed(((Number) elapsed.toJavaObject()).intValue());
					
					session.insert("Scouter.insertSql", sql);
				} catch (Exception e) {
					Logger.printStackTrace(e);
				}
    		}
    	}
    }
    
    /**
     * XLogPack 발생 시 처리
     * @param pack
     */
    @ServerPlugin(PluginConstants.PLUGIN_SERVER_XLOG)
    public void xlog(XLogPack pack) {
    	try {
	    	if (serviceStatMap.get(pack.objHash) == null) {
	    		serviceStatMap.put(pack.objHash, new ConcurrentHashMap<Integer, ServiceStat>());
	    	}
	    	
	    	if (serviceStatMap.get(pack.objHash).get(pack.service) == null) {
	    		serviceStatMap.get(pack.objHash).put(pack.service, new ServiceStat(pack.objHash, pack.service));
	    	}

	    	String serviceName = TextRD.getString(DateUtil.yyyymmdd(pack.endTime), TextTypes.SERVICE, pack.service);
    		InetAddress inetAddr = InetAddress.getByAddress(pack.ipaddr);
    		serviceStatMap.get(pack.objHash).get(pack.service).add(serviceName, pack.elapsed, pack.error != 0, inetAddr == null ? null : inetAddr.getHostAddress(), pack.userAgent, pack.sqlCount, pack.sqlTime);
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
    }
    
    private void println(Object o) {
        if (conf.getBoolean("ext_plugin_reporting_logging_enabled", false)) {
        	Logger.println(o);
        }
    }
    
    private static AgentInfo selectAgentInfo(int objHash) {
		 return session.selectOne("Scouter.selectAgentInfo", objHash);
    }
    
    private synchronized static void insertAgentInfo(AgentInfo agentInfo) {
		 session.insert("Scouter.insertAgentInfo", agentInfo);
    }
    
    private synchronized static void updateAgentInfo(AgentInfo agentInfo) {
		 session.update("Scouter.updateAgentInfo", agentInfo);
    }
    
    public static void main(String[] args) throws Exception {
    	Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DAY_OF_MONTH, -1);

		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH) + 1;
		int date = cal.get(Calendar.DAY_OF_MONTH);
		
    	boolean isMonthly = false;
    	
    	args = new String[] {"201610"};

    	if (args.length == 1) {
    		if (args[0].length() == 6) {
    			isMonthly = true;
    			
    			year = Integer.parseInt(args[0].substring(0, 4));
    			month = Integer.parseInt(args[0].substring(4));
    		} else if (args[0].length() == 8) {
    			year = Integer.parseInt(args[0].substring(0, 4));
    			month = Integer.parseInt(args[0].substring(4, 6));
    			date = Integer.parseInt(args[0].substring(6));
    		} 
    	}
		
		AbstractReport report = null;
		
		// Alert Reporting
		try {
			report = new AlertReport();
			
			if (!isMonthly) {
				System.out.println("[SCOUTER-X] Start Daily Alert Report.");
				report.createExcel(year, month, date);
			} else {
				System.out.println("[SCOUTER-X] Start Monthly Alert Report.");
				report.createExcel(year, month);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// Host Reporting
		try {
			report = new HostReport();
			
			if (!isMonthly) {
				System.out.println("[SCOUTER-X] Start Daily Host Report.");
				report.createExcel(year, month, date);
			} else {
				System.out.println("[SCOUTER-X] Start Monthly Host Report.");
				report.createExcel(year, month);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// Java Reporting
		try {
			report = new JavaReport();
			
			if (!isMonthly) {
				System.out.println("[SCOUTER-X] Start Daily Java Report.");
				report.createExcel(year, month, date);
			} else {
				System.out.println("[SCOUTER-X] Start Monthly Java Report.");
				report.createExcel(year, month);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// Service Reporting
		try {
			report = new ServiceReport();
			
			if (!isMonthly) {
				System.out.println("[SCOUTER-X] Start Daily Service Report.");
				report.createExcel(year, month, date);
			} else {
				System.out.println("[SCOUTER-X] Start Monthly Service Report.");
				report.createExcel(year, month);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
    	
	}
}