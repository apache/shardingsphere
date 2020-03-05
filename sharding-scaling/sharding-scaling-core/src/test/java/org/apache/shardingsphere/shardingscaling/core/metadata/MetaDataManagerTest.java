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

package org.apache.shardingsphere.shardingscaling.core.metadata;

import org.apache.shardingsphere.shardingscaling.core.metadata.column.ColumnMetaData;
import org.apache.shardingsphere.shardingscaling.core.metadata.table.TableMetaData;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.List;

import javax.sql.DataSource;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class MetaDataManagerTest {
    
    private static final String COLUMN_NAME = "COLUMN_NAME";
    
    private static final String DATA_TYPE = "DATA_TYPE";
    
    private static final String TYPE_NAME = "TYPE_NAME";
    
    private static final String TEST_TABLE = "test";
    
    @Mock
    private DataSource dataSource;
    
    @Mock
    private Connection connection;
    
    @Mock
    private DatabaseMetaData databaseMetaData;
    
    @Mock
    private ResultSet primaryKeyResultSet;
    
    @Mock
    private ResultSet columnMetaDataResultSet;
    
    @Before
    public void setUp() throws Exception {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.getCatalog()).thenReturn("");
        when(connection.getSchema()).thenReturn("");
        when(connection.getMetaData()).thenReturn(databaseMetaData);
        when(databaseMetaData.getColumns("", "", TEST_TABLE, "%")).thenReturn(columnMetaDataResultSet);
        when(databaseMetaData.getPrimaryKeys("", "", TEST_TABLE)).thenReturn(primaryKeyResultSet);
        when(primaryKeyResultSet.next()).thenReturn(true, false);
        when(primaryKeyResultSet.getString(COLUMN_NAME)).thenReturn("id");
        when(columnMetaDataResultSet.next()).thenReturn(true, true, true, false);
        when(columnMetaDataResultSet.getString(COLUMN_NAME)).thenReturn("id", "name", "age");
        when(columnMetaDataResultSet.getInt(DATA_TYPE)).thenReturn(Types.BIGINT, Types.VARCHAR, Types.INTEGER);
        when(columnMetaDataResultSet.getString(TYPE_NAME)).thenReturn("BIGINT", "VARCHAR", "INTEGER");
    }
    
    @Test
    public void assertGetTableMetaData() {
        MetaDataManager metaDataManager = new MetaDataManager(dataSource);
        assertColumnMetaData(metaDataManager.getTableMetaData(TEST_TABLE));
        assertPrimaryKeys(metaDataManager.getTableMetaData(TEST_TABLE).getPrimaryKeyColumns());
    }
    
    private void assertPrimaryKeys(final List<String> actual) {
        assertThat(actual.size(), is(1));
        assertThat(actual.get(0), is("id"));
    }
    
    private void assertColumnMetaData(final TableMetaData actual) {
        assertThat(actual.getColumnsSize(), is(3));
        assertColumnMetaData(actual.getColumnMetaData(0), "id", Types.BIGINT, "BIGINT");
        assertColumnMetaData(actual.getColumnMetaData(1), "name", Types.VARCHAR, "VARCHAR");
        assertColumnMetaData(actual.getColumnMetaData(2), "age", Types.INTEGER, "INTEGER");
    }
    
    private void assertColumnMetaData(final ColumnMetaData actual, final String expectedName, final int expectedType, final String expectedTypeName) {
        assertThat(actual.getColumnName(), is(expectedName));
        assertThat(actual.getColumnType(), is(expectedType));
        assertThat(actual.getColumnTypeName(), is(expectedTypeName));
    }
}
