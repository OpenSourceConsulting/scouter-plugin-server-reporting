package scouter.plugin.server.reporting.vo;

public class Sql extends BaseObject {

	private int sql_hash;
	private int execute_count;
	private int error_count;
	private int elapsed;

	public int getSql_hash() {
		return sql_hash;
	}

	public void setSql_hash(int sql_hash) {
		this.sql_hash = sql_hash;
	}

	public int getExecute_count() {
		return execute_count;
	}

	public void setExecute_count(int execute_count) {
		this.execute_count = execute_count;
	}

	public int getError_count() {
		return error_count;
	}

	public void setError_count(int error_count) {
		this.error_count = error_count;
	}

	public int getElapsed() {
		return elapsed;
	}

	public void setElapsed(int elapsed) {
		this.elapsed = elapsed;
	}

	@Override
	public String toString() {
		return "Sql [sql_hash=" + sql_hash + ", execute_count=" + execute_count
				+ ", error_count=" + error_count + ", elapsed=" + elapsed + ", date=" + getDate() + ", object_hash="
				+ object_hash + ", log_dt=" + log_dt + ", log_tm=" + log_tm + "]";
	}
}