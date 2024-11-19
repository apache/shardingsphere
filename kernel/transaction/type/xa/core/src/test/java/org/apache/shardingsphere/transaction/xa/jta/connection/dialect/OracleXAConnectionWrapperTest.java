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

import javax.sql.DataSource;
import javax.sql.XAConnection;
import javax.sql.XADataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class OracleXAConnectionWrapperTest {
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "OracleXA");

    @Test
    void assertWrap() throws Exception {
        XAConnection actual = DatabaseTypedSPILoader.getService(XAConnectionWrapper.class, databaseType).wrap(createXADataSource(), mockConnection());
        assertThat(actual.getClass(), is(OracleXAConnection.class));
    }

    private XADataSource createXADataSource() {
        DataSource dataSource = DataSourceUtils.build(HikariDataSource.class, databaseType,"foo_ds");
        return new DataSourceSwapper(DatabaseTypedSPILoader.getService(XADataSourceDefinition.class,databaseType)).swap(dataSource);
    }

    private Connection mockConnection() throws SQLException {
        Connection result = mock(Connection.class);
        when(result.unwrap(OracleConnection.class)).thenReturn(mock(OracleConnection.class, RETURNS_DEEP_STUBS));
        return result;
    }
}
