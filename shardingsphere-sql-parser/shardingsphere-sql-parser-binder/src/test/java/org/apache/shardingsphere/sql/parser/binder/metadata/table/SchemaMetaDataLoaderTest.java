package org.apache.shardingsphere.sql.parser.binder.metadata.table;

import org.apache.shardingsphere.sql.parser.binder.metadata.schema.SchemaMetaDataLoader;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class SchemaMetaDataLoaderTest {
    private static final String TEST_CATALOG = "catalog";

    private static final String TABLE_TYPE = "TABLE";

    private static final int maxConnectionCount = 5;

    private static final String databaseType = "Oracle";

    @Mock
    private DataSource dataSource;

    @Mock
    private Connection connection;

    @Mock
    private DatabaseMetaData databaseMetaData;

    @Mock
    private ResultSet tableExistResultSet;

    @Before
    public void setUp() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.getCatalog()).thenReturn(TEST_CATALOG);
        when(connection.getMetaData()).thenReturn(databaseMetaData);
        when(databaseMetaData.getTables(TEST_CATALOG, null, null, new String[]{TABLE_TYPE})).thenReturn(tableExistResultSet);
    }

    @Test
    public void assertLoadAllTableNamesForOracle() throws SQLException {
        SchemaMetaDataLoader.load(dataSource, maxConnectionCount, databaseType);
    }

}
