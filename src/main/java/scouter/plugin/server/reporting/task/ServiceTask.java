package scouter.plugin.server.reporting.task;

import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import scouter.plugin.server.reporting.ReportingPlugin;
import scouter.plugin.server.reporting.collector.ServiceStat;
import scouter.plugin.server.reporting.vo.Service;
import scouter.server.Logger;
import scouter.util.DateUtil;

public class ServiceTask implements Runnable {
	
	private SqlSessionFactory sqlSessionFactory;
	private Map<Integer, Map<Integer, ServiceStat>> serviceStatMap;
	
	public ServiceTask(SqlSessionFactory sqlSessionFactory, Map<Integer, Map<Integer, ServiceStat>> serviceStatMap) {
		this.sqlSessionFactory = sqlSessionFactory;
		this.serviceStatMap = serviceStatMap;
	}

	@Override
	public void run() {
		SqlSession session = sqlSessionFactory.openSession(true);
		
		try {
			long time = (System.currentTimeMillis() - 10000) / DateUtil.MILLIS_PER_FIVE_MINUTE * DateUtil.MILLIS_PER_FIVE_MINUTE;
			
			List<Integer> mainKeyList = new ArrayList<Integer>(serviceStatMap.keySet());
			
			List<Integer> subKeyList = null;
			Map<Integer, ServiceStat> serviceMap = null;
			ServiceStat serviceStat = null;
			Service service = null;
			
			// DB insert 시간이 길면 다음 스케쥴 시간에 해당하는 데이터도 함께 수집이 되므로, 현재 스케쥴 시간에 해당하는 데이터 목록만 구한다.
			List<Service> serviceList = new ArrayList<Service>();
			for (Integer mainKey : mainKeyList) {
				serviceMap = serviceStatMap.get(mainKey);
				
				subKeyList = new ArrayList<Integer>(serviceMap.keySet());
				for (Integer subKey : subKeyList) {
					serviceStat = serviceMap.get(subKey);
					
					if (serviceStat.isPurge()) {
						serviceMap.remove(subKey);
					} else {
						service = serviceStat.getServiceAndClear();
						
						if (service != null) {
							serviceList.add(service);
						}
					}
				}
			}
			
			for (Service s : serviceList) {
				// 서비스의 경우 데이터 처리에 많은 시간이 소요될 경우 다음 스케줄 시간에 해당하는 데이터도 함께 처리될 수 있으며,
				// 그런 경우 PK 충돌이 발생할 수 있다. 따라서 현재 스케쥴 시간에 해당하는 데이터만 처리한다.
				if (s.getLog_tm().equals(new Time(time))) {
					try {
						session.insert("Scouter.insertService", s);

						if (ReportingPlugin.conf.getBoolean("ext_plugin_reporting_logging_enabled", false)) {
							Logger.println("[" + s.getObject_hash() + "," + s.getService_hash() + "] service inserted.");
				        }
					} catch (Exception e) {
						//Logger.printStackTrace(e);
						Logger.println("[Duplicated] : " + s);
					}
					
					try {
						session.insert("Scouter.insertIpAddress", s);
					} catch (Exception e) {}
					
					try {
						session.insert("Scouter.insertUserAgent", s);
					} catch (Exception e) {}
				}
			}
		} catch (Exception e) {
			Logger.printStackTrace(e);
		} finally {
			if (session != null) {
				session.close();
			}
		}
	}
}