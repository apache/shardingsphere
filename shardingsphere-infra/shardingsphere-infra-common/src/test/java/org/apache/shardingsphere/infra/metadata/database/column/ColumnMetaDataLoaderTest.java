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

package org.apache.shardingsphere.infra.metadata.database.column;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Collection;
import java.util.Iterator;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class ColumnMetaDataLoaderTest {
    
    private static final String TEST_CATALOG = "catalog";
    
    private static final String TEST_TABLE = "table";
    
    @Mock
    private Connection connection;
    
    @Mock
    private DatabaseMetaData databaseMetaData;
    
    @Mock
    private ResultSet primaryResultSet;
    
    @Mock
    private ResultSet columnResultSet;
    
    @Mock
    private Statement statement;
    
    @Mock
    private ResultSet caseSensitivesResultSet;
    
    @Mock
    private ResultSetMetaData resultSetMetaData;
    
    @Before
    public void setUp() throws SQLException {
        when(connection.getCatalog()).thenReturn(TEST_CATALOG);
        when(connection.getMetaData()).thenReturn(databaseMetaData);
        when(databaseMetaData.getPrimaryKeys(TEST_CATALOG, null, TEST_TABLE)).thenReturn(primaryResultSet);
        when(primaryResultSet.next()).thenReturn(true, false);
        when(primaryResultSet.getString("COLUMN_NAME")).thenReturn("pk_col");
        when(databaseMetaData.getColumns(TEST_CATALOG, null, TEST_TABLE, "%")).thenReturn(columnResultSet);
        when(columnResultSet.next()).thenReturn(true, true, false);
        when(columnResultSet.getString("TABLE_NAME")).thenReturn(TEST_TABLE);
        when(columnResultSet.getString("COLUMN_NAME")).thenReturn("pk_col", "col");
        when(columnResultSet.getInt("DATA_TYPE")).thenReturn(Types.INTEGER, Types.VARCHAR);
        when(columnResultSet.getString("TYPE_NAME")).thenReturn("INT", "VARCHAR");
        when(connection.createStatement()).thenReturn(statement);
        when(statement.executeQuery(anyString())).thenReturn(caseSensitivesResultSet);
        when(caseSensitivesResultSet.findColumn("pk_col")).thenReturn(1);
        when(caseSensitivesResultSet.findColumn("col")).thenReturn(2);
        when(caseSensitivesResultSet.getMetaData()).thenReturn(resultSetMetaData);
        when(resultSetMetaData.isCaseSensitive(1)).thenReturn(true);
    }
    
    @Test
    public void assertLoad() throws SQLException {
        Collection<ColumnMetaData> actual = ColumnMetaDataLoader.load(connection, TEST_TABLE, "");
        assertThat(actual.size(), is(2));
        Iterator<ColumnMetaData> columnMetaDataIterator = actual.iterator();
        assertColumnMetaData(columnMetaDataIterator.next(), "pk_col", Types.INTEGER, "INT", true, true);
        assertColumnMetaData(columnMetaDataIterator.next(), "col", Types.VARCHAR, "VARCHAR", false, false);
    }
    
    private void assertColumnMetaData(final ColumnMetaData actual, final String name, final int dataType, final String typeName, final boolean primaryKey, final boolean caseSensitive) {
        assertThat(actual.getName(), is(name));
        assertThat(actual.getDataType(), is(dataType));
        assertThat(actual.getDataTypeName(), is(typeName));
        assertThat(actual.isPrimaryKey(), is(primaryKey));
        assertThat(actual.isCaseSensitive(), is(caseSensitive));
    }
}
