package scouter.plugin.server.reporting.collector;

import java.sql.Time;
import java.util.Date;

import scouter.lang.ref.INT;
import scouter.lang.ref.LONG;
import scouter.plugin.server.reporting.vo.HostAgent;
import scouter.util.DateUtil;
import scouter.util.MeteringUtil;
import scouter.util.MeteringUtil.Handler;
import scouter.util.ThreadUtil;

public class HostAgentStat {
	
	final static class Slot {
		int netTx;
		int netRx;
		int diskRead;
		int diskWrite;
        int count;
	}
	
	private int objHash;
	private float avgCpu;
	private float maxCpu;
	private int memTotal;
	private float avgMem;
	private float maxMem;
	private int avgMemUsed;
	private int maxMemUsed;
	private int maxNetTx;
	private int maxNetRx;
	private int maxDiskRead;
	private int maxDiskWrite;
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
			s.netTx = 0;
			s.netRx = 0;
			s.diskRead = 0;
			s.diskWrite = 0;
			s.count = 0;
		}
	};
	
	public HostAgentStat(int objHash) {
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
			Slot s = meter.getCurrentBucket();
			lastSlot.netTx = s.netTx;
			lastSlot.netRx = s.netRx;
			lastSlot.diskRead = s.diskRead;
			lastSlot.diskWrite = s.diskWrite;
			lastSlot.count = s.count;

			this.maxCpu = 0F;
			this.memTotal = 0;
			this.maxMem = 0F;
			this.maxMemUsed = 0;
			this.maxNetTx = 0;
			this.maxNetRx = 0;
			this.maxDiskRead = 0;
			this.maxDiskWrite = 0;
			
			// 현재 시간의 (0, 5, 10, 15, ... 50, 55분 단위로 변경)
			this.startTime = System.currentTimeMillis() / DateUtil.MILLIS_PER_FIVE_MINUTE * DateUtil.MILLIS_PER_FIVE_MINUTE;
		} finally {
			isProcessing = false;
		}
	}
	
	public synchronized void addMax(float cpu, int memTotal, float mem, int memUsed, int netTx, int netRx, int diskRead, int diskWrite) {
		while (true) {
			if (isProcessing) {
				ThreadUtil.sleep(10);
			} else {
				break;
			}
		}
		
		// 현재 초(second)에 해당하는 슬롯에 데이터 저장
		Slot s = meter.getCurrentBucket();
		s.netTx += netTx;
		s.netRx += netRx;
		s.diskRead += diskRead;
		s.diskWrite += diskWrite;
		s.count++;
		
		this.memTotal = memTotal;
		
		// 5분 내 최대 값 갱신
		if (cpu > maxCpu) {
			maxCpu = cpu;
		}
		if (mem > maxMem) {
			maxMem = mem;
		}
		if (memUsed > maxMemUsed) {
			maxMemUsed = memUsed;
		}
		if (netTx > maxNetTx) {
			maxNetTx = netTx;
		}
		if (netRx > maxNetRx) {
			maxNetRx = netRx;
		}
		if (diskRead > maxDiskRead) {
			maxDiskRead = diskRead;
		}
		if (diskWrite > maxDiskWrite) {
			maxDiskWrite = diskWrite;
		}
		
		this.lastAccessTime = System.currentTimeMillis();
	}
	
	public synchronized void addAvg(float cpu, float mem, int memUsed) {
		this.avgCpu = cpu;
		this.avgMem = mem;
		this.avgMemUsed = memUsed;
		
		this.lastAccessTime = System.currentTimeMillis();
	}
	
	public HostAgent getHostAgent() {
		return getHostAgent(false);
	}
	
	private HostAgent getHostAgent(boolean isClear) {
		isProcessing = true;
		
		try {
			HostAgent agent = new HostAgent();
			agent.setDate(this.startTime);
			agent.setObject_hash(this.objHash);
			agent.setLog_dt(new java.sql.Date(this.startTime));
			agent.setLog_tm(new Time(this.startTime));
			agent.setCpu_avg(this.avgCpu);
			agent.setCpu_max(this.maxCpu);
			agent.setMem_total(this.memTotal);
			agent.setMem_avg(this.avgMem);
			agent.setMem_max(this.maxMem);
			agent.setMem_u_avg(this.avgMemUsed);
			agent.setMem_u_max(this.maxMemUsed);
			agent.setNet_tx_avg(getNetTxAvg(isClear));
			agent.setNet_tx_max(this.maxNetTx);
			agent.setNet_rx_avg(getNetRxAvg(isClear));
			agent.setNet_rx_max(this.maxNetRx);
			agent.setDisk_r_avg(getDiskReadAvg(isClear));
			agent.setDisk_r_max(this.maxDiskRead);
			agent.setDisk_w_avg(getDiskWriteAvg(isClear));
			agent.setDisk_w_max(this.maxDiskWrite);
			
			// host agent가 동작하지 않는 경우
			if (this.memTotal == 0) {
				return null;
			}

			return agent;
		} finally {
			isProcessing = false;
		}
	}

	public HostAgent getHostAgentAndClear() {
		HostAgent agent = getHostAgent();
		clear();
		
		return agent;
	}

	private int getNetTxAvg(boolean isClear) {
		final LONG sum = new LONG();
        final INT cnt = new INT();
        
        int period = isClear ? 301 : 300;
        
		meter.search(period, new Handler<Slot>() {
			public void process(Slot s) {
				sum.value += s.netTx;
				cnt.value += s.count;
			}
		});
		
		if (isClear) {
			sum.value -= lastSlot.netTx;
			cnt.value -= lastSlot.count;
		}
		
		return (int) ((cnt.value == 0) ? 0 : sum.value / cnt.value);
	}

	private int getNetRxAvg(boolean isClear) {
		final LONG sum = new LONG();
        final INT cnt = new INT();
        
        int period = isClear ? 301 : 300;
        
		meter.search(period, new Handler<Slot>() {
			public void process(Slot s) {
				sum.value += s.netRx;
				cnt.value += s.count;
			}
		});
		
		if (isClear) {
			sum.value -= lastSlot.netRx;
			cnt.value -= lastSlot.count;
		}
		
		return (int) ((cnt.value == 0) ? 0 : sum.value / cnt.value);
	}

	private int getDiskReadAvg(boolean isClear) {
		final LONG sum = new LONG();
        final INT cnt = new INT();
        
        int period = isClear ? 301 : 300;
        
		meter.search(period, new Handler<Slot>() {
			public void process(Slot s) {
				sum.value += s.diskRead;
				cnt.value += s.count;
			}
		});
		
		if (isClear) {
			sum.value -= lastSlot.diskRead;
			cnt.value -= lastSlot.count;
		}
		
		return (int) ((cnt.value == 0) ? 0 : sum.value / cnt.value);
	}

	private int getDiskWriteAvg(boolean isClear) {
		final LONG sum = new LONG();
        final INT cnt = new INT();
        
        int period = isClear ? 301 : 300;
        
		meter.search(period, new Handler<Slot>() {
			public void process(Slot s) {
				sum.value += s.diskWrite;
				cnt.value += s.count;
			}
		});
		
		if (isClear) {
			sum.value -= lastSlot.diskWrite;
			cnt.value -= lastSlot.count;
		}
		
		return (int) ((cnt.value == 0) ? 0 : sum.value / cnt.value);
	}
	
	public static void main(String[] args) {
		final HostAgentStat has = new HostAgentStat(12345);
		
		final long until = System.currentTimeMillis() + (60 * 60 * 1000);

		new Thread() {
			public void run() {
				while (System.currentTimeMillis() < until) {
					has.addMax(90.7f, 16 * 1024 * 1024, 52.5f, 7 * 1024 * 1024, 10, 20, 30, 40);
					has.addAvg(90.7f, 52.5f, 7 * 1024 * 1024);
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
				
				System.err.println(new Date(time) + ":" + has.getHostAgentAndClear());
			}
			
			ThreadUtil.sleep(1000);
		}
	}
}