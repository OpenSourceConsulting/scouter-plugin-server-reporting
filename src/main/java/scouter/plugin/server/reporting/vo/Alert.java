package scouter.plugin.server.reporting.vo;

public class Alert extends BaseObject {

	private String object_name;
	private String day;
	private String time;
	private String level;
	private String title;
	private String message;

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

	public String getLevel() {
		return level;
	}

	public void setLevel(String level) {
		this.level = level;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	@Override
	public String toString() {
		return "Alert [object_name=" + object_name + ", day=" + day + ", time=" + time + ", level=" + level + ", title="
				+ title + ", message=" + message + ", date=" + date + ", object_hash=" + object_hash + ", log_dt="
				+ log_dt + ", log_tm=" + log_tm + "]";
	}
}