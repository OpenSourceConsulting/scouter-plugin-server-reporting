package scouter.plugin.server.reporting.vo;

public class HostAgent extends BaseObject {

	private String object_name;
	private String day;
	private String time;
	private Float cpu_avg;
	private Float cpu_max;
	private Integer mem_total;
	private Float mem_avg;
	private Float mem_max;
	private Integer mem_u_avg;
	private Integer mem_u_max;
	private Integer net_tx_avg;
	private Integer net_tx_max;
	private Integer net_rx_avg;
	private Integer net_rx_max;
	private Integer disk_r_avg;
	private Integer disk_r_max;
	private Integer disk_w_avg;
	private Integer disk_w_max;

	public String getObject_name() {
		return object_name;
	}

	public void setObject_name(String object_name) {
		this.object_name = object_name;
	}

	public String getDay() {
		return day;
	}

	public void setDay(String day) {
		this.day = day;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public Float getCpu_avg() {
		return cpu_avg;
	}

	public void setCpu_avg(Float cpu_avg) {
		this.cpu_avg = cpu_avg;
	}

	public Float getCpu_max() {
		return cpu_max;
	}

	public void setCpu_max(Float cpu_max) {
		this.cpu_max = cpu_max;
	}

	public Integer getMem_total() {
		return mem_total;
	}

	public void setMem_total(Integer mem_total) {
		this.mem_total = mem_total;
	}

	public Float getMem_avg() {
		return mem_avg;
	}

	public void setMem_avg(Float mem_avg) {
		this.mem_avg = mem_avg;
	}

	public Float getMem_max() {
		return mem_max;
	}

	public void setMem_max(Float mem_max) {
		this.mem_max = mem_max;
	}

	public Integer getMem_u_avg() {
		return mem_u_avg;
	}

	public void setMem_u_avg(Integer mem_u_avg) {
		this.mem_u_avg = mem_u_avg;
	}

	public Integer getMem_u_max() {
		return mem_u_max;
	}

	public void setMem_u_max(Integer mem_u_max) {
		this.mem_u_max = mem_u_max;
	}

	public Integer getNet_tx_avg() {
		return net_tx_avg;
	}

	public void setNet_tx_avg(Integer net_tx_avg) {
		this.net_tx_avg = net_tx_avg;
	}

	public Integer getNet_tx_max() {
		return net_tx_max;
	}

	public void setNet_tx_max(Integer net_tx_max) {
		this.net_tx_max = net_tx_max;
	}

	public Integer getNet_rx_avg() {
		return net_rx_avg;
	}

	public void setNet_rx_avg(Integer net_rx_avg) {
		this.net_rx_avg = net_rx_avg;
	}

	public Integer getNet_rx_max() {
		return net_rx_max;
	}

	public void setNet_rx_max(Integer net_rx_max) {
		this.net_rx_max = net_rx_max;
	}

	public Integer getDisk_r_avg() {
		return disk_r_avg;
	}

	public void setDisk_r_avg(Integer disk_r_avg) {
		this.disk_r_avg = disk_r_avg;
	}

	public Integer getDisk_r_max() {
		return disk_r_max;
	}

	public void setDisk_r_max(Integer disk_r_max) {
		this.disk_r_max = disk_r_max;
	}

	public Integer getDisk_w_avg() {
		return disk_w_avg;
	}

	public void setDisk_w_avg(Integer disk_w_avg) {
		this.disk_w_avg = disk_w_avg;
	}

	public Integer getDisk_w_max() {
		return disk_w_max;
	}

	public void setDisk_w_max(Integer disk_w_max) {
		this.disk_w_max = disk_w_max;
	}

	@Override
	public String toString() {
		return "HostAgent [object_name=" + object_name + ", day=" + day + ", time=" + time + ", cpu_avg=" + cpu_avg
				+ ", cpu_max=" + cpu_max + ", mem_total=" + mem_total + ", mem_avg=" + mem_avg + ", mem_max=" + mem_max
				+ ", mem_u_avg=" + mem_u_avg + ", mem_u_max=" + mem_u_max + ", net_tx_avg=" + net_tx_avg
				+ ", net_tx_max=" + net_tx_max + ", net_rx_avg=" + net_rx_avg + ", net_rx_max=" + net_rx_max
				+ ", disk_r_avg=" + disk_r_avg + ", disk_r_max=" + disk_r_max + ", disk_w_avg=" + disk_w_avg
				+ ", disk_w_max=" + disk_w_max + ", date=" + date + ", object_hash=" + object_hash + ", log_dt="
				+ log_dt + ", log_tm=" + log_tm + "]";
	}
}