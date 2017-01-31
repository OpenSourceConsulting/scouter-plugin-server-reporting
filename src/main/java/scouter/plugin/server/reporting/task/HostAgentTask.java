package scouter.plugin.server.reporting.task;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import scouter.plugin.server.reporting.ReportingPlugin;
import scouter.plugin.server.reporting.collector.HostAgentStat;
import scouter.plugin.server.reporting.vo.HostAgent;
import scouter.server.Logger;

public class HostAgentTask implements Runnable {

	private SqlSessionFactory sqlSessionFactory;
	private Map<Integer, HostAgentStat> hostAgentStatMap;
	
	public HostAgentTask(SqlSessionFactory sqlSessionFactory, Map<Integer, HostAgentStat> hostAgentStatMap) {
		this.sqlSessionFactory = sqlSessionFactory;
		this.hostAgentStatMap = hostAgentStatMap;
	}

	@Override
	public void run() {
		SqlSession session = sqlSessionFactory.openSession(true);
		
		try {
			List<Integer> keyList = new ArrayList<Integer>(hostAgentStatMap.keySet());
			
			HostAgentStat hostAgentStat = null;
			HostAgent hostAgent = null;
	
			if (ReportingPlugin.conf.getBoolean("ext_plugin_reporting_logging_enabled", false)) {
				Logger.println("hostAgentStatMap's size : " + keyList.size());
	        }
			
			for (Integer key : keyList) {
				hostAgentStat = hostAgentStatMap.get(key);
				
				if (hostAgentStat.isPurge()) {
					hostAgentStatMap.remove(key);
				} else {
					hostAgent = hostAgentStat.getHostAgentAndClear();
					
					if (hostAgent != null) {
//						long time = (System.currentTimeMillis() - 10000) / DateUtil.MILLIS_PER_FIVE_MINUTE * DateUtil.MILLIS_PER_FIVE_MINUTE;
//						
//						if (ReportingPlugin.conf.getBoolean("ext_plugin_reporting_logging_enabled", false)) {
//							Logger.println(new Date(time) + ":" + hostAgent);
//				        }
						
						try { 
							session.insert("Scouter.insertHostAgent", hostAgent);
							
							if (ReportingPlugin.conf.getBoolean("ext_plugin_reporting_logging_enabled", false)) {
								Logger.println("[" + key + "] hostAgent inserted.");
					        }
						} catch (Exception e) {
							Logger.printStackTrace(e);
						}
					}
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