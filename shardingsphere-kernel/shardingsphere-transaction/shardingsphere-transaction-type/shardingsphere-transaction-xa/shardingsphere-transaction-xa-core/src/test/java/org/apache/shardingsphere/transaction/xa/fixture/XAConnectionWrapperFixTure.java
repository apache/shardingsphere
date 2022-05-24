package org.apache.shardingsphere.transaction.xa.fixture;

import lombok.SneakyThrows;
import org.apache.shardingsphere.transaction.xa.jta.connection.XAConnectionWrapper;

import javax.sql.XAConnection;
import javax.sql.XADataSource;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;

public class XAConnectionWrapperFixTure implements XAConnectionWrapper {

	private static volatile Class<Connection> jdbcConnectionClass;

	private static volatile Method xaConnectionCreatorMethod;

	private static volatile boolean initialized;

	@Override
	public XAConnection wrap(final XADataSource xaDataSource, final Connection connection) throws SQLException {
		if (!initialized) {
			loadReflection();
			initialized = true;
		}
		return createXAConnection(xaDataSource, connection.unwrap(jdbcConnectionClass));
	}

	private void loadReflection() {
		jdbcConnectionClass = getJDBCConnectionClass();
		xaConnectionCreatorMethod = getXAConnectionCreatorMethod();
	}

	@SuppressWarnings("unchecked")
	@SneakyThrows(ReflectiveOperationException.class)
	private Class<Connection> getJDBCConnectionClass() {
		try {
			return (Class<Connection>) Class.forName("com.mysql.jdbc.Connection");
		} catch (final ClassNotFoundException ignored) {
			return (Class<Connection>) Class.forName("com.mysql.cj.jdbc.JdbcConnection");
		}
	}

	@SneakyThrows(ReflectiveOperationException.class)
	private Method getXAConnectionCreatorMethod() {
		Method result = getXADataSourceClass().getDeclaredMethod("wrapConnection", Connection.class);
		result.setAccessible(true);
		return result;
	}

	@SuppressWarnings("unchecked")
	@SneakyThrows(ReflectiveOperationException.class)
	private Class<XADataSource> getXADataSourceClass() {
		try {
			return (Class<XADataSource>) Class.forName("com.mysql.jdbc.jdbc2.optional.MysqlXADataSource");
		} catch (final ClassNotFoundException ignored) {
			return (Class<XADataSource>) Class.forName("com.mysql.cj.jdbc.MysqlXADataSource");
		}
	}

	@SneakyThrows(ReflectiveOperationException.class)
	private XAConnection createXAConnection(final XADataSource xaDataSource, final Connection connection) {
		return (XAConnection) xaConnectionCreatorMethod.invoke(xaDataSource, connection);
	}


	@Override
	public String getType() {
		return "TEST";
	}
}
