package org.apache.shardingsphere.transaction.xa.jta.connection.dialect;


import com.zaxxer.hikari.HikariDataSource;
import oracle.jdbc.internal.OracleConnection;
import oracle.jdbc.xa.client.OracleXAConnection;

import org.apache.shardingsphere.infra.database.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.transaction.xa.fixture.DataSourceUtils;
import org.apache.shardingsphere.transaction.xa.jta.connection.XAConnectionWrapper;
import org.apache.shardingsphere.transaction.xa.jta.datasource.properties.XADataSourceDefinition;
import org.apache.shardingsphere.transaction.xa.jta.datasource.swapper.DataSourceSwapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import javax.sql.DataSource;
import javax.sql.XAConnection;
import javax.sql.XADataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class OracleXAConnectionWrapperTest {

    private OracleXAConnectionWrapper xaConnectionWrapper;

    @BeforeEach
    void setUp() {
        xaConnectionWrapper = new OracleXAConnectionWrapper();
        xaConnectionWrapper.init(new Properties());
    }
    @Test
    void assertWrap() throws SQLException {
        XADataSource xaDataSource = mock(XADataSource.class);
        Connection connection = mockConnection();
        XAConnection actual = xaConnectionWrapper.wrap(xaDataSource, connection);
        assertThat(actual, instanceOf(XAConnection.class));
    }

    @Test
    void assertGetDatabaseType() {
        assertThat(xaConnectionWrapper.getDatabaseType(), is("Oracle"));
    }

    private Connection mockConnection() throws SQLException {
        Connection result = mock(Connection.class);
        when(result.unwrap(OracleConnection.class)).thenReturn(mock(OracleConnection.class, RETURNS_DEEP_STUBS));
        return result;
    }
}
