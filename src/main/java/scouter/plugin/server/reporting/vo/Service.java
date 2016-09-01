package scouter.plugin.server.reporting.vo;

import java.util.ArrayList;
import java.util.List;

public class Service extends BaseObject {

	private String day;
	private String time;
	private String app_id;
	private Integer service_hash;
	private String service_name;
	private Integer elapsed_avg;
	private Integer elapsed_max;
	private Integer sql_count_avg;
	private Integer sql_count_max;
	private Integer sql_time_avg;
	private Integer sql_time_max;
	private Integer request_count;
	private Integer error_count;
	private Integer elapsed_exceed_count;
	private Integer ip_count;
	private Integer ua_count;
	
	private List<IpAddress> ipAddressList;
	private List<UserAgent> userAgentList;

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

	public String getApp_id() {
		return app_id;
	}

	public void setApp_id(String app_id) {
		this.app_id = app_id;
	}

	public Integer getService_hash() {
		return service_hash;
	}

	public void setService_hash(Integer service_hash) {
		this.service_hash = service_hash;
	}

	public String getService_name() {
		return service_name;
	}

	public void setService_name(String service_name) {
		this.service_name = service_name;
	}

	public Integer getElapsed_avg() {
		return elapsed_avg;
	}

	public void setElapsed_avg(Integer elapsed_avg) {
		this.elapsed_avg = elapsed_avg;
	}

	public Integer getElapsed_max() {
		return elapsed_max;
	}

	public void setElapsed_max(Integer elapsed_max) {
		this.elapsed_max = elapsed_max;
	}

	public Integer getSql_count_avg() {
		return sql_count_avg;
	}

	public void setSql_count_avg(Integer sql_count_avg) {
		this.sql_count_avg = sql_count_avg;
	}

	public Integer getSql_count_max() {
		return sql_count_max;
	}

	public void setSql_count_max(Integer sql_count_max) {
		this.sql_count_max = sql_count_max;
	}

	public Integer getSql_time_avg() {
		return sql_time_avg;
	}

	public void setSql_time_avg(Integer sql_time_avg) {
		this.sql_time_avg = sql_time_avg;
	}

	public Integer getSql_time_max() {
		return sql_time_max;
	}

	public void setSql_time_max(Integer sql_time_max) {
		this.sql_time_max = sql_time_max;
	}

	public Integer getRequest_count() {
		return request_count;
	}

	public void setRequest_count(Integer request_count) {
		this.request_count = request_count;
	}

	public Integer getError_count() {
		return error_count;
	}

	public void setError_count(Integer error_count) {
		this.error_count = error_count;
	}

	public Integer getElapsed_exceed_count() {
		return elapsed_exceed_count;
	}

	public void setElapsed_exceed_count(Integer elapsed_exceed_count) {
		this.elapsed_exceed_count = elapsed_exceed_count;
	}

	public Integer getIp_count() {
		return ip_count;
	}

	public void setIp_count(Integer ip_count) {
		this.ip_count = ip_count;
	}

	public Integer getUa_count() {
		return ua_count;
	}

	public void setUa_count(Integer ua_count) {
		this.ua_count = ua_count;
	}

	public List<IpAddress> getIpAddressList() {
		if (ipAddressList == null) {
			ipAddressList = new ArrayList<IpAddress>();
		}
		
		return ipAddressList;
	}

	public void addIpAddress(IpAddress ipAddress) {
		getIpAddressList().add(ipAddress);
	}

	public List<UserAgent> getUserAgentList() {
		if (userAgentList == null) {
			userAgentList = new ArrayList<UserAgent>();
		}
		
		return userAgentList;
	}

	public void addUserAgent(UserAgent userAgent) {
		getUserAgentList().add(userAgent);
	}

	@Override
	public String toString() {
		return "Service [day=" + day + ", time=" + time + ", app_id=" + app_id + ", service_hash=" + service_hash + ", service_name="
				+ service_name + ", elapsed_avg=" + elapsed_avg + ", elapsed_max=" + elapsed_max + ", sql_count_avg="
				+ sql_count_avg + ", sql_count_max=" + sql_count_max + ", sql_time_avg=" + sql_time_avg
				+ ", sql_time_max=" + sql_time_max + ", request_count=" + request_count + ", error_count=" + error_count
				+ ", elapsed_exceed_count=" + elapsed_exceed_count + ", ip_count=" + ip_count + ", ua_count=" + ua_count
				+ ", ipAddressList=" + ipAddressList + ", userAgentList=" + userAgentList + ", date=" + date
				+ ", object_hash=" + object_hash + ", log_dt=" + log_dt + ", log_tm=" + log_tm + "]";
	}
}