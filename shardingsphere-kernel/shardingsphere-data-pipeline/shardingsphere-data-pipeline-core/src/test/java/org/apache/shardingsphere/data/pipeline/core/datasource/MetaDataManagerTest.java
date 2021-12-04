/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.data.pipeline.core.datasource;

import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.schema.model.ColumnMetaData;
import org.apache.shardingsphere.infra.metadata.schema.model.TableMetaData;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class MetaDataManagerTest {
    
    private static final String TEST_CATALOG = "catalog";
    
    private static final String COLUMN_NAME = "COLUMN_NAME";
    
    private static final String DATA_TYPE = "DATA_TYPE";
    
    private static final String TYPE_NAME = "TYPE_NAME";
    
    private static final String TABLE_NAME = "TABLE_NAME";
    
    private static final String TEST_TABLE = "test";
    
    @Mock(extraInterfaces = AutoCloseable.class)
    private DataSource dataSource;
    
    @Mock
    private Connection connection;
    
    @Mock
    private Statement statement;
    
    @Mock
    private DatabaseMetaData databaseMetaData;
    
    @Mock
    private ResultSet tableResultSet;
    
    @Mock
    private ResultSet primaryKeyResultSet;
    
    @Mock
    private ResultSet columnMetaDataResultSet;
    
    @Mock
    private ResultSet indexMetaDataResultSet;
    
    @Mock
    private ResultSet caseSensitivesResultSet;
    
    @Mock
    private ResultSetMetaData resultSetMetaData;
    
    @Before
    public void setUp() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.getCatalog()).thenReturn(TEST_CATALOG);
        when(connection.getMetaData()).thenReturn(databaseMetaData);
        when(connection.createStatement()).thenReturn(statement);
        when(databaseMetaData.getColumns(TEST_CATALOG, null, TEST_TABLE, "%")).thenReturn(columnMetaDataResultSet);
        when(databaseMetaData.getPrimaryKeys(TEST_CATALOG, null, TEST_TABLE)).thenReturn(primaryKeyResultSet);
        when(databaseMetaData.getTables(TEST_CATALOG, null, TEST_TABLE, null)).thenReturn(tableResultSet);
        when(databaseMetaData.getIndexInfo(TEST_CATALOG, null, TEST_TABLE, false, false)).thenReturn(indexMetaDataResultSet);
        when(tableResultSet.next()).thenReturn(true);
        when(primaryKeyResultSet.next()).thenReturn(true, false);
        when(primaryKeyResultSet.getString(COLUMN_NAME)).thenReturn("id");
        when(columnMetaDataResultSet.next()).thenReturn(true, true, true, false);
        when(columnMetaDataResultSet.getString(TABLE_NAME)).thenReturn(TEST_TABLE);
        when(columnMetaDataResultSet.getString(COLUMN_NAME)).thenReturn("id", "name", "age");
        when(columnMetaDataResultSet.getInt(DATA_TYPE)).thenReturn(Types.BIGINT, Types.VARCHAR, Types.INTEGER);
        when(statement.executeQuery(anyString())).thenReturn(caseSensitivesResultSet);
        when(caseSensitivesResultSet.getMetaData()).thenReturn(resultSetMetaData);
    }
    
    @Test
    public void assertGetTableMetaData() {
        MetaDataManager metaDataManager = new MetaDataManager(dataSource);
        DatabaseType databaseType = mock(DatabaseType.class, RETURNS_DEEP_STUBS);
        when(databaseType.formatTableNamePattern(TEST_TABLE)).thenReturn(TEST_TABLE);
        assertColumnMetaData(metaDataManager.getTableMetaData(TEST_TABLE, databaseType));
        assertPrimaryKeys(metaDataManager.getTableMetaData(TEST_TABLE, databaseType).getPrimaryKeyColumns());
    }
    
    private void assertPrimaryKeys(final List<String> actual) {
        assertThat(actual.size(), is(1));
        assertThat(actual.get(0), is("id"));
    }
    
    private void assertColumnMetaData(final TableMetaData actual) {
        assertThat(actual.getColumns().size(), is(3));
        assertColumnMetaData(actual.getColumnMetaData(0), "id", Types.BIGINT);
        assertColumnMetaData(actual.getColumnMetaData(1), "name", Types.VARCHAR);
        assertColumnMetaData(actual.getColumnMetaData(2), "age", Types.INTEGER);
    }
    
    private void assertColumnMetaData(final ColumnMetaData actual, final String expectedName, final int expectedType) {
        assertThat(actual.getName(), is(expectedName));
        assertThat(actual.getDataType(), is(expectedType));
    }
    
    @Test(expected = RuntimeException.class)
    public void assertGetTableMetaDataFailure() throws SQLException {
        when(dataSource.getConnection()).thenThrow(new SQLException(""));
        new MetaDataManager(dataSource).getTableMetaData(TEST_TABLE, mock(DatabaseType.class));
    }
}
