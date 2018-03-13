package io.shardingjdbc.dbtest.common;

import io.shardingjdbc.core.constant.DatabaseType;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ConfigRuntime {


	public static Set<String> getDbs() {
		Map<String, String> dbs = ConfigUtils.getDatas("database");
		Set<String> sets = new HashSet<>();
		for (Map.Entry<String, String> db : dbs.entrySet()) {
			String[] dbsts = db.getKey().split("\\.");
			sets.add(dbsts[1]);
		}
		return sets;
	}

	private static String getjdbcConfig(String key) {
		return ConfigUtils.getString("database." + key, "");
	}

	public static String getDriverClassName(DatabaseType type) {
		switch (type) {
		case H2:
			return getjdbcConfig("h2.driver");
		case MySQL:
			return getjdbcConfig("mysql.driver");
		case Oracle:
			return getjdbcConfig("oracle.driver");
		case SQLServer:
			return getjdbcConfig("sqlserver.driver");
		case PostgreSQL:
			return getjdbcConfig("postgresql.driver");
		default:
			return null;
		}
	}

	public static String getURL(DatabaseType type, String dbName) {
		switch (type) {
		case H2:
			return String.format(getjdbcConfig("h2.url"), dbName);
		case MySQL:
			return String.format(getjdbcConfig("mysql.url"), dbName);
		case Oracle:
			return String.format(getjdbcConfig("oracle.url"), dbName);
		case SQLServer:
			return String.format(getjdbcConfig("sqlserver.url"), dbName);
		case PostgreSQL:
			return String.format(getjdbcConfig("postgresql.url"), dbName);
		default:
			return null;
		}
	}

	public static String getUsername(DatabaseType type) {
		switch (type) {
		case H2:
			return getjdbcConfig("h2.username");
		case MySQL:
			return getjdbcConfig("mysql.username");
		case Oracle:
			return getjdbcConfig("oracle.username");
		case SQLServer:
			return getjdbcConfig("sqlserver.username");
		case PostgreSQL:
			return getjdbcConfig("postgresql.username");
		default:
			return null;
		}
	}

	public static String getPassword(DatabaseType type) {
		switch (type) {
		case H2:
			return getjdbcConfig("h2.password");
		case MySQL:
			return getjdbcConfig("mysql.password");
		case Oracle:
			return getjdbcConfig("oracle.password");
		case SQLServer:
			return getjdbcConfig("sqlserver.password");
		case PostgreSQL:
			return getjdbcConfig("postgresql.password");
		default:
			return null;
		}
	}

	public static String getDefualtdb(DatabaseType type) {
		switch (type) {
		case H2:
			return getjdbcConfig("h2.defualtdb");
		case MySQL:
			return getjdbcConfig("mysql.defualtdb");
		case Oracle:
			return getjdbcConfig("oracle.defualtdb");
		case SQLServer:
			return getjdbcConfig("sqlserver.defualtdb");
		case PostgreSQL:
			return getjdbcConfig("postgresql.defualtdb");
		default:
			return null;
		}
	}

	public static String getAssertPath() {
		return ConfigUtils.getString("assert.path", null);
	}

	public static boolean isInitialized() {
		return Boolean.valueOf(ConfigUtils.getString("initialized", "false"));
	}
}
