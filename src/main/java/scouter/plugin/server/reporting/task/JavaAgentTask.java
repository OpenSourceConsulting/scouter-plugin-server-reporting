package scouter.plugin.server.reporting.task;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;

import scouter.plugin.server.reporting.ReportingPlugin;
import scouter.plugin.server.reporting.collector.JavaAgentStat;
import scouter.plugin.server.reporting.vo.JavaAgent;
import scouter.server.Logger;
import scouter.util.DateUtil;

public class JavaAgentTask implements Runnable {
	
	private SqlSession session;
	private Map<Integer, JavaAgentStat> javaAgentStatMap;
	
	public JavaAgentTask(SqlSession session, Map<Integer, JavaAgentStat> javaAgentStatMap) {
		this.session = session;
		this.javaAgentStatMap = javaAgentStatMap;
	}

	@Override
	public void run() {
		List<Integer> keyList = new ArrayList<Integer>(javaAgentStatMap.keySet());
		
		JavaAgentStat javaAgentStat = null;
		JavaAgent javaAgent = null;
		
		for (Integer key : keyList) {
			javaAgentStat = javaAgentStatMap.get(key);
			
			if (javaAgentStat.isPurge()) {
				javaAgentStatMap.remove(key);
			} else {
				javaAgent = javaAgentStat.getJavaAgentAndClear();
				
				if (javaAgent != null) {
					long time = (System.currentTimeMillis() - 10000) / DateUtil.MILLIS_PER_FIVE_MINUTE * DateUtil.MILLIS_PER_FIVE_MINUTE;
					
					if (ReportingPlugin.conf.getBoolean("ext_plugin_reporting_logging_enabled", false)) {
						Logger.println(new Date(time) + ":" + javaAgent);
			        }
					
					session.insert("Scouter.insertJavaAgent", javaAgent);
				}
			}
		}
	}

}