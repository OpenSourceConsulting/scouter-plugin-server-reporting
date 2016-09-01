package scouter.plugin.server.reporting.vo;

public class JavaAgent extends BaseObject {

	private String object_name;
	private String day;
	private String time;
	private Integer active_service_avg;
	private Integer active_service_max;
	private Float heap_total;
	private Float heap_used_avg;
	private Float heap_used_max;
	private Integer recent_user_avg;
	private Integer recent_user_max;
	private Integer service_count_avg;
	private Integer service_count_max;
	private Float api_tps_avg;
	private Float api_tps_max;
	private Float sql_tps_avg;
	private Float sql_tps_max;
	private Float tps_avg;
	private Float tps_max;

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

	public Integer getActive_service_avg() {
		return active_service_avg;
	}

	public void setActive_service_avg(Integer active_service_avg) {
		this.active_service_avg = active_service_avg;
	}

	public Integer getActive_service_max() {
		return active_service_max;
	}

	public void setActive_service_max(Integer active_service_max) {
		this.active_service_max = active_service_max;
	}

	public Float getHeap_total() {
		return heap_total;
	}

	public void setHeap_total(Float heap_total) {
		this.heap_total = heap_total;
	}

	public Float getHeap_used_avg() {
		return heap_used_avg;
	}

	public void setHeap_used_avg(Float heap_used_avg) {
		this.heap_used_avg = heap_used_avg;
	}

	public Float getHeap_used_max() {
		return heap_used_max;
	}

	public void setHeap_used_max(Float heap_used_max) {
		this.heap_used_max = heap_used_max;
	}

	public Integer getRecent_user_avg() {
		return recent_user_avg;
	}

	public void setRecent_user_avg(Integer recent_user_avg) {
		this.recent_user_avg = recent_user_avg;
	}

	public Integer getRecent_user_max() {
		return recent_user_max;
	}

	public void setRecent_user_max(Integer recent_user_max) {
		this.recent_user_max = recent_user_max;
	}

	public Integer getService_count_avg() {
		return service_count_avg;
	}

	public void setService_count_avg(Integer service_count_avg) {
		this.service_count_avg = service_count_avg;
	}

	public Integer getService_count_max() {
		return service_count_max;
	}

	public void setService_count_max(Integer service_count_max) {
		this.service_count_max = service_count_max;
	}

	public Float getApi_tps_avg() {
		return api_tps_avg;
	}

	public void setApi_tps_avg(Float api_tps_avg) {
		this.api_tps_avg = api_tps_avg;
	}

	public Float getApi_tps_max() {
		return api_tps_max;
	}

	public void setApi_tps_max(Float api_tps_max) {
		this.api_tps_max = api_tps_max;
	}

	public Float getSql_tps_avg() {
		return sql_tps_avg;
	}

	public void setSql_tps_avg(Float sql_tps_avg) {
		this.sql_tps_avg = sql_tps_avg;
	}

	public Float getSql_tps_max() {
		return sql_tps_max;
	}

	public void setSql_tps_max(Float sql_tps_max) {
		this.sql_tps_max = sql_tps_max;
	}

	public Float getTps_avg() {
		return tps_avg;
	}

	public void setTps_avg(Float tps_avg) {
		this.tps_avg = tps_avg;
	}

	public Float getTps_max() {
		return tps_max;
	}

	public void setTps_max(Float tps_max) {
		this.tps_max = tps_max;
	}

	@Override
	public String toString() {
		return "JavaAgent [object_name=" + object_name + ", day=" + day + ", time=" + time + ", active_service_avg="
				+ active_service_avg + ", active_service_max=" + active_service_max + ", heap_total=" + heap_total
				+ ", heap_used_avg=" + heap_used_avg + ", heap_used_max=" + heap_used_max + ", recent_user_avg="
				+ recent_user_avg + ", recent_user_max=" + recent_user_max + ", service_count_avg=" + service_count_avg
				+ ", service_count_max=" + service_count_max + ", api_tps_avg=" + api_tps_avg + ", api_tps_max="
				+ api_tps_max + ", sql_tps_avg=" + sql_tps_avg + ", sql_tps_max=" + sql_tps_max + ", tps_avg=" + tps_avg
				+ ", tps_max=" + tps_max + ", date=" + date + ", object_hash=" + object_hash + ", log_dt=" + log_dt
				+ ", log_tm=" + log_tm + "]";
	}
}