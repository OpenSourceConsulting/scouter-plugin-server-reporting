package scouter.plugin.server.reporting.vo;

public class SqlInfo {

	private int sql_hash;
	private String sql_str;

	public int getSql_hash() {
		return sql_hash;
	}

	public void setSql_hash(int sql_hash) {
		this.sql_hash = sql_hash;
	}

	public String getSql_str() {
		return sql_str;
	}

	public void setSql_str(String sql_str) {
		this.sql_str = sql_str;
	}

	@Override
	public String toString() {
		return "SqlInfo [sql_hash=" + sql_hash + ", sql_str=" + sql_str + "]";
	}
}