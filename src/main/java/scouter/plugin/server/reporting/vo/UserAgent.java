package scouter.plugin.server.reporting.vo;

public class UserAgent extends BaseObject {

	private int service_hash;
	private String user_agent;
	private int request_count;
	private int error_count;

	public int getService_hash() {
		return service_hash;
	}

	public void setService_hash(int service_hash) {
		this.service_hash = service_hash;
	}

	public String getUser_agent() {
		return user_agent;
	}

	public void setUser_agent(String user_agent) {
		this.user_agent = user_agent;
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
		return "UserAgent [service_hash=" + service_hash + ", user_agent=" + user_agent + ", request_count="
				+ request_count + ", error_count=" + error_count + ", date=" + getDate() + ", object_hash=" + object_hash
				+ ", log_dt=" + log_dt + ", log_tm=" + log_tm + "]";
	}
}