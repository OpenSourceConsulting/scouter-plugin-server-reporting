package scouter.plugin.server.reporting.task;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;

import scouter.plugin.server.reporting.ReportingPlugin;
import scouter.plugin.server.reporting.collector.ServiceStat;
import scouter.plugin.server.reporting.vo.Service;
import scouter.server.Logger;

public class ServiceTask implements Runnable {
	
	private SqlSession session;
	private Map<Integer, Map<Integer, ServiceStat>> serviceStatMap;
	
	public ServiceTask(SqlSession session, Map<Integer, Map<Integer, ServiceStat>> serviceStatMap) {
		this.session = session;
		this.serviceStatMap = serviceStatMap;
	}

	@Override
	public void run() {
		try {
			List<Integer> mainKeyList = new ArrayList<Integer>(serviceStatMap.keySet());
			
			List<Integer> subKeyList = null;
			Map<Integer, ServiceStat> serviceMap = null;
			ServiceStat serviceStat = null;
			Service service = null;
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
//							long time = (System.currentTimeMillis() - 10000) / DateUtil.MILLIS_PER_FIVE_MINUTE * DateUtil.MILLIS_PER_FIVE_MINUTE;
//							
//							if (ReportingPlugin.conf.getBoolean("ext_plugin_reporting_logging_enabled", false)) {
//								Logger.println(new Date(time) + ":" + service);
//					        }
							
							session.insert("Scouter.insertService", service);
	
							if (ReportingPlugin.conf.getBoolean("ext_plugin_reporting_logging_enabled", false)) {
								Logger.println("[" + mainKey + "," + subKey + "] service inserted.");
					        }
							
							try {
								session.insert("Scouter.insertIpAddress", service);
							} catch (Exception e) {}
							
							try {
								session.insert("Scouter.insertUserAgent", service);
							} catch (Exception e) {}
						}
					}
				}
			}
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
	}
}