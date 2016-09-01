package scouter.plugin.server.reporting.vo;

import java.util.Date;

public class AgentInfo {

	private int object_hash;
	private String object_name;
	private String object_family;
	private String object_type;
	private String ip_address;
	private Date last_up_time;
	private Date last_down_time;

	public int getObject_hash() {
		return object_hash;
	}

	public void setObject_hash(int object_hash) {
		this.object_hash = object_hash;
	}

	public String getObject_name() {
		return object_name;
	}

	public void setObject_name(String object_name) {
		this.object_name = object_name;
	}

	public String getObject_family() {
		return object_family;
	}

	public void setObject_family(String object_family) {
		this.object_family = object_family;
	}

	public String getObject_type() {
		return object_type;
	}

	public void setObject_type(String object_type) {
		this.object_type = object_type;
	}

	public String getIp_address() {
		return ip_address;
	}

	public void setIp_address(String ip_address) {
		this.ip_address = ip_address;
	}

	public Date getLast_up_time() {
		return last_up_time;
	}

	public void setLast_up_time(Date last_up_time) {
		this.last_up_time = last_up_time;
	}

	public Date getLast_down_time() {
		return last_down_time;
	}

	public void setLast_down_time(Date last_down_time) {
		this.last_down_time = last_down_time;
	}

	@Override
	public String toString() {
		return "AgentInfo [object_hash=" + object_hash + ", object_name=" + object_name + ", object_family="
				+ object_family + ", object_type=" + object_type + ", ip_address=" + ip_address + ", last_up_time="
				+ last_up_time + ", last_down_time=" + last_down_time + "]";
	}
}