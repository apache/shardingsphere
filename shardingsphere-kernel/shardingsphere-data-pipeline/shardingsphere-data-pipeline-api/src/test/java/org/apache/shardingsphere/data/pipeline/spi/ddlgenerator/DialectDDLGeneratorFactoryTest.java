package org.apache.shardingsphere.data.pipeline.spi.ddlgenerator;

import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class DialectDDLGeneratorFactoryTest {

	private static final String CLIENT_USERNAME = "username";

	private static final String CLIENT_PASSWORD = "password";

	private static final int LOGIN_TIMEOUT = 15;

	@Mock(extraInterfaces = AutoCloseable.class)
	private DataSource dataSource;

	@Mock
	private Connection connection;

	@Mock
	private PrintWriter printWriter;

	@Mock
	private Logger parentLogger;

	private static final String DEFAULT_SCHEMA = "public";

	private static final String SHOW_CREATE_SQL = "SHOW CREATE TABLE %s";

	private static final String COLUMN_LABEL = "create table";


	@Before
	public void setUp() throws SQLException {

		when(dataSource.getConnection()).thenReturn(connection);
		when(dataSource.getConnection(CLIENT_USERNAME, CLIENT_PASSWORD)).thenReturn(connection);
		when(dataSource.getLogWriter()).thenReturn(printWriter);
		when(dataSource.getLoginTimeout()).thenReturn(LOGIN_TIMEOUT);
		when(dataSource.isWrapperFor(any())).thenReturn(Boolean.TRUE);
		when(dataSource.getParentLogger()).thenReturn(parentLogger);
	}

	@Test
	public void assertFindInstanceWithDialectDDLGenerator() throws SQLException {
		boolean thrown = false;

		if (DialectDDLSQLGeneratorFactory.findInstance(new MySQLDatabaseType()).isPresent()) {
			assertThat(DialectDDLSQLGeneratorFactory.findInstance(new MySQLDatabaseType()).get(), is(DialectDDLGenerator.class));

		}

		DatabaseType databaseType = new MySQLDatabaseType();
		DialectDDLGenerator dialectDDLGenerator = new DialectDDLGenerator() {
			@Override
			public String generateDDLSQL(String tableName, String schemaName, DataSource dataSource) throws SQLException {
				return null;
			}
		};

		try (
				Connection connection = dataSource.getConnection();
				Statement statement = connection.createStatement()) {
			if (DialectDDLSQLGeneratorFactory.findInstance(databaseType).isPresent()) {
				dialectDDLGenerator = DialectDDLSQLGeneratorFactory.findInstance(databaseType).get();
			}
			assertNewInstance(dialectDDLGenerator);
			String sql = DialectDDLSQLGeneratorFactory.findInstance(databaseType).orElseThrow(() -> new ShardingSphereException("Failed to get dialect ddl sql generator"))
					.generateDDLSQL("tableA", DEFAULT_SCHEMA, dataSource);
			statement.execute(sql);
		} catch (Exception ex) {
			thrown = true;
		}

		assertTrue(thrown);
	}

	private void assertNewInstance(final DialectDDLGenerator actual) {
		DialectDDLGenerator excepted = new DialectDDLGenerator() {
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
		};
		when(actual).thenReturn(excepted);
	}


}
