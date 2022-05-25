package org.apache.shardingsphere.data.pipeline.spi.ddlgenerator;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;
import javax.sql.DataSource;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeFactory;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.mockito.ArgumentMatchers.any;

import org.mockito.Mock;

import static org.mockito.Mockito.when;

import org.mockito.junit.MockitoJUnitRunner;

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

        DatabaseType databaseType = DatabaseTypeFactory.getInstance("MySQL");

        try {
            String sql = DialectDDLSQLGeneratorFactory.findInstance(databaseType).orElseThrow(() -> new ShardingSphereException("Failed to get dialect ddl sql generator"))
                    .generateDDLSQL("tableA", "", dataSource);
            assertEquals(sql, "SHOW CREATE TABLE tableA");
        } catch (SQLException ex) {
            thrown = true;
        }

        assertFalse(thrown);
    }
}
