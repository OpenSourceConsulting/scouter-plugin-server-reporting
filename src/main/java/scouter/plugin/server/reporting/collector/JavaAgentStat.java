package scouter.plugin.server.reporting.collector;

import java.sql.Time;
import java.util.Date;

import scouter.plugin.server.reporting.vo.JavaAgent;
import scouter.util.DateUtil;
import scouter.util.ThreadUtil;

public class JavaAgentStat {
	
	private int objHash;
	private int avgActiveService;
	private int maxActiveService;
	private float heapTotal;
	private float avgHeapUsed;
	private float maxHeapUsed;
	private int avgRecentUser;
	private int maxRecentUser;
	private int avgServiceCount;
	private int maxServiceCount;
	private float avgApiTps;
	private float maxApiTps;
	private float avgSqlTps;
	private float maxSqlTps;
	private float avgTps;
	private float maxTps;
	private long startTime;
	private long lastAccessTime;
	
	private boolean isProcessing = false;
	
	public JavaAgentStat(int objHash) {
		this.objHash = objHash;
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
			this.maxActiveService = 0;
			this.heapTotal = 0F;
			this.maxHeapUsed = 0F;
			this.maxRecentUser = 0;
			this.maxServiceCount = 0;
			this.maxSqlTps = 0F;
			this.maxTps = 0F;
			
			// 현재 시간의 (0, 5, 10, 15, ... 50, 55분 단위로 변경)
			this.startTime = System.currentTimeMillis() / DateUtil.MILLIS_PER_FIVE_MINUTE * DateUtil.MILLIS_PER_FIVE_MINUTE;
		} finally {
			isProcessing = false;
		}
	}
	
	public synchronized void addMax(int activeService, float heapTotal, float heapUsed, int recentUser, int serviceCount, float apiTps, float sqlTps, float tps) {
		while (true) {
			if (isProcessing) {
				ThreadUtil.sleep(10);
			} else {
				break;
			}
		}
		
		if (heapTotal > 0f && this.heapTotal < heapTotal) {
			this.heapTotal = heapTotal;
		}
		
		// 5분 내 최대 값 갱신
		if (activeService > maxActiveService) {
			maxActiveService = activeService;
		}
		if (heapUsed > maxHeapUsed) {
			maxHeapUsed = heapUsed;
		}
		if (recentUser > maxRecentUser) {
			maxRecentUser = recentUser;
		}
		if (serviceCount > maxServiceCount) {
			maxServiceCount = serviceCount;
		}
		if (apiTps > maxApiTps) {
			maxApiTps = apiTps;
		}
		if (sqlTps > maxSqlTps) {
			maxSqlTps = sqlTps;
		}
		if (tps > maxTps) {
			maxTps = tps;
		}
		
		this.lastAccessTime = System.currentTimeMillis();
	}
	
	public synchronized void addAvg(int activeService, float heapUsed, int recentUser, int serviceCount, float apiTps, float sqlTps, float tps) {
		this.avgActiveService = activeService;
		this.avgHeapUsed = heapUsed;
		this.avgRecentUser = recentUser;
		this.avgServiceCount = serviceCount;
		this.avgApiTps = apiTps;
		this.avgSqlTps = sqlTps;
		this.avgTps = tps;
		
		this.lastAccessTime = System.currentTimeMillis();
	}
	
	public JavaAgent getJavaAgent() {
		isProcessing = true;
		
		try {
			JavaAgent agent = new JavaAgent();
			agent.setDate(this.startTime);
			agent.setObject_hash(this.objHash);
			agent.setLog_dt(new java.sql.Date(this.startTime));
			agent.setLog_tm(new Time(this.startTime));
			agent.setActive_service_avg(this.avgActiveService);
			agent.setActive_service_max(this.maxActiveService);
			agent.setHeap_total(this.heapTotal);
			agent.setHeap_used_avg(this.avgHeapUsed);
			agent.setHeap_used_max(this.maxHeapUsed);
			agent.setRecent_user_avg(this.avgRecentUser);
			agent.setRecent_user_max(this.maxRecentUser);
			agent.setService_count_avg(this.avgServiceCount);
			agent.setService_count_max(this.maxServiceCount);
			agent.setApi_tps_avg(this.avgApiTps);
			agent.setApi_tps_max(this.maxApiTps);
			agent.setSql_tps_avg(this.avgSqlTps);
			agent.setSql_tps_max(this.maxSqlTps);
			agent.setTps_avg(this.avgTps);
			agent.setTps_max(this.maxTps);

			// java agent가 동작하지 않는 경우
			if (this.heapTotal == 0f) {
				return null;
			}

			return agent;
		} finally {
			isProcessing = false;
		}
	}

	public JavaAgent getJavaAgentAndClear() {
		JavaAgent agent = getJavaAgent();
		clear();
		
		return agent;
	}
	
	public static void main(String[] args) {
		final JavaAgentStat jas = new JavaAgentStat(12345);
		
		final long until = System.currentTimeMillis() + (60 * 60 * 1000);

		new Thread() {
			public void run() {
				while (System.currentTimeMillis() < until) {
					jas.addMax(5, 1024f, 80.5f, 10, 9, 8, 7, 6);
					jas.addAvg(5, 80.5f, 10, 9, 8, 7, 6);
					ThreadUtil.sleep(1000);
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
				
				System.err.println(new Date(time) + ":" + jas.getJavaAgentAndClear());
			}
			
			ThreadUtil.sleep(1000);
		}
	}
}