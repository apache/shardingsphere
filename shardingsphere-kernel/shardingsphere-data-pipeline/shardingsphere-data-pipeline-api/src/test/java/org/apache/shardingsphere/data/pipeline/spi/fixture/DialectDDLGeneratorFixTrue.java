package org.apache.shardingsphere.data.pipeline.spi.fixture;

import org.apache.shardingsphere.data.pipeline.spi.ddlgenerator.DialectDDLGenerator;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DialectDDLGeneratorFixTrue implements DialectDDLGenerator {
	private static final String SHOW_CREATE_SQL = "SHOW CREATE TABLE %s";

	private static final String COLUMN_LABEL = "create table";

	@Override
	public String generateDDLSQL(String tableName, String schemaName, DataSource dataSource) throws SQLException {
		try (
				Statement statement = dataSource.getConnection().createStatement();
				ResultSet resultSet = statement.executeQuery(String.format(SHOW_CREATE_SQL, tableName))) {
			if (resultSet.next()) {
				return resultSet.getString(COLUMN_LABEL);
			}
		}
		throw new ShardingSphereException("Failed to get ddl sql for table %s", tableName);
	}
}
