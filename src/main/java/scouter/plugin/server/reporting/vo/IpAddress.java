package scouter.plugin.server.reporting.vo;

public class IpAddress extends BaseObject {

	private int service_hash;
	private String ip_address;
	private int request_count;
	private int error_count;

	public int getService_hash() {
		return service_hash;
	}

	public void setService_hash(int service_hash) {
		this.service_hash = service_hash;
	}

	public String getIp_address() {
		return ip_address;
	}

	public void setIp_address(String ip_address) {
		this.ip_address = ip_address;
	}

	public int getRequest_count() {
		return request_count;
	}

	public void setRequest_count(int request_count) {
		this.request_count = request_count;
	}

	public int getError_count() {
		return error_count;
	}

	public void setError_count(int error_count) {
		this.error_count = error_count;
	}

	@Override
	public String toString() {
		return "IpAddress [service_hash=" + service_hash + ", ip_address=" + ip_address + ", request_count="
				+ request_count + ", error_count=" + error_count + ", date=" + getDate() + ", object_hash=" + object_hash
				+ ", log_dt=" + log_dt + ", log_tm=" + log_tm + "]";
	}
}