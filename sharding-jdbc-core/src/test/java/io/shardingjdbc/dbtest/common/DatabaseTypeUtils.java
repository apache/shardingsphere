package io.shardingjdbc.dbtest.common;

import io.shardingjdbc.core.constant.DatabaseType;

public class DatabaseTypeUtils {

	public static DatabaseType getDatabaseType(String type) {

		DatabaseType[] databaseTypes = DatabaseType.values();
		for (DatabaseType each : databaseTypes) {
			if (type.equalsIgnoreCase(each.name())) {
				return each;
			}
		}
		return DatabaseType.H2;
	}

}
