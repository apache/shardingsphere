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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import lombok.SneakyThrows;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.LinkedList;
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
    @SneakyThrows
    public void assertGetPrimaryKeys() {
        MetaDataManager metaDataManager = new MetaDataManager(dataSource);
        List<String> actual = metaDataManager.getPrimaryKeys(TEST_TABLE);
        assertThat(actual.size(), is(1));
        assertThat(actual.get(0), is("id"));
    }
    
    @Test
    @SneakyThrows
    public void assertGetColumnNames() {
        MetaDataManager metaDataManager = new MetaDataManager(dataSource);
        List<ColumnMetaData> actual = metaDataManager.getColumnNames(TEST_TABLE);
        assertThat(actual.size(), is(3));
        assertColumnMetaData(actual.get(0), "id", Types.BIGINT, "BIGINT");
        assertColumnMetaData(actual.get(1), "name", Types.VARCHAR, "VARCHAR");
        assertColumnMetaData(actual.get(2), "age", Types.INTEGER, "INTEGER");
    }
    
    private void assertColumnMetaData(final ColumnMetaData actual, final String expectedName, final int expectedType, final String expectedTypeName) {
        assertThat(actual.getColumnName(), is(expectedName));
        assertThat(actual.getColumnType(), is(expectedType));
        assertThat(actual.getColumnTypeName(), is(expectedTypeName));
    }
    
    @Test
    public void assertFindColumnIndex() {
        List<ColumnMetaData> columnMetaDataList = new LinkedList<>();
        ColumnMetaData columnMetaData = new ColumnMetaData();
        columnMetaData.setColumnName("id");
        columnMetaDataList.add(columnMetaData);
        columnMetaData = new ColumnMetaData();
        columnMetaData.setColumnName("name");
        columnMetaDataList.add(columnMetaData);
        MetaDataManager metaDataManager = new MetaDataManager(dataSource);
        assertThat(metaDataManager.findColumnIndex(columnMetaDataList, "id"), is(0));
        assertThat(metaDataManager.findColumnIndex(columnMetaDataList, "name"), is(1));
        assertThat(metaDataManager.findColumnIndex(columnMetaDataList, "age"), is(-1));
    }
}
