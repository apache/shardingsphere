package org.apache.shardingsphere.sql.parser.binder.metadata.table;

import org.apache.shardingsphere.sql.parser.binder.metadata.schema.SchemaMetaDataLoader;
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

    @Mock
    private DataSource dataSource;

    @Mock
    private Connection connection;

    @Mock
    private DatabaseMetaData databaseMetaData;

    @Mock
    private ResultSet tableExistResultSet;

    @Test
    public void assertloadAllTableNamesForOracle() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.getCatalog()).thenReturn(TEST_CATALOG);
        when(connection.getMetaData()).thenReturn(databaseMetaData);
        when(databaseMetaData.getURL()).thenReturn("jdbc:oracle:thin:@127.0.0.1:1521/orcl");
        when(databaseMetaData.getUserName()).thenReturn(null);
        when(databaseMetaData.getTables(TEST_CATALOG, null, null, new String[]{TABLE_TYPE})).thenReturn(tableExistResultSet);
        SchemaMetaDataLoader.load(dataSource, 5, "Oracle");
    }

}
