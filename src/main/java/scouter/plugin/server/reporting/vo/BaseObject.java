package scouter.plugin.server.reporting.vo;

import java.sql.Date;
import java.sql.Time;
import java.util.Calendar;

public abstract class BaseObject {

	protected String date;
	protected int object_hash;
	protected Date log_dt;
	protected Time log_tm;

	public String getDate() {
		if (date == null) {
			int d = Calendar.getInstance().get(Calendar.DATE);
			
			if (d < 10) {
				date = "0" + d;
			} else {
				date = d + "";
			}
		}
		
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public void setDate(long timestamp) {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(timestamp);
		
		int d = c.get(Calendar.DATE);
		
		if (d < 10) {
			date = "0" + d;
		} else {
			date = d + "";
		}
	}

	public int getObject_hash() {
		return object_hash;
	}

	public void setObject_hash(int object_hash) {
		this.object_hash = object_hash;
	}

	public Date getLog_dt() {
		return log_dt;
	}

	public void setLog_dt(Date log_dt) {
		this.log_dt = log_dt;
	}

	public Time getLog_tm() {
		return log_tm;
	}

	public void setLog_tm(Time log_tm) {
		this.log_tm = log_tm;
	}
}
