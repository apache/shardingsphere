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

package org.apache.shardingsphere.database.connector.core.metadata.data.loader.type;

import org.apache.shardingsphere.database.connector.core.metadata.data.model.ColumnMetaData;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collection;
import java.util.Iterator;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ColumnMetaDataLoaderTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Connection connection;
    
    @Mock
    private ResultSet columnResultSet;
    
    @BeforeEach
    void setUp() throws SQLException {
        when(connection.getCatalog()).thenReturn("catalog");
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("COLUMN_NAME")).thenReturn("pk_col");
        when(connection.getMetaData().getPrimaryKeys("catalog", null, "tbl")).thenReturn(resultSet);
        when(connection.getMetaData().getColumns("catalog", null, "tbl", "%")).thenReturn(columnResultSet);
    }
    
    @Test
    void assertLoad() throws SQLException {
        when(columnResultSet.next()).thenReturn(true, true, true, false);
        when(columnResultSet.getString("TABLE_NAME")).thenReturn("other_tbl", "tbl", "tbl");
        when(columnResultSet.getString("COLUMN_NAME")).thenReturn("pk_col", "col");
        when(columnResultSet.getInt("DATA_TYPE")).thenReturn(Types.INTEGER, Types.VARCHAR);
        ResultSet caseSensitivesResultSet = mock(ResultSet.class);
        when(caseSensitivesResultSet.findColumn("pk_col")).thenReturn(1);
        when(caseSensitivesResultSet.findColumn("col")).thenReturn(2);
        ResultSetMetaData resultSetMetaData = mock(ResultSetMetaData.class);
        when(resultSetMetaData.isCaseSensitive(1)).thenReturn(true);
        when(caseSensitivesResultSet.getMetaData()).thenReturn(resultSetMetaData);
        when(connection.createStatement().executeQuery(anyString())).thenReturn(caseSensitivesResultSet);
        Collection<ColumnMetaData> actual = ColumnMetaDataLoader.load(connection, "tbl", databaseType);
        assertThat(actual.size(), is(2));
        Iterator<ColumnMetaData> columnMetaDataIterator = actual.iterator();
        assertColumnMetaData(columnMetaDataIterator.next(), "pk_col", Types.INTEGER, true, true);
        assertColumnMetaData(columnMetaDataIterator.next(), "col", Types.VARCHAR, false, false);
    }
    
    private void assertColumnMetaData(final ColumnMetaData actual, final String name, final int dataType, final boolean primaryKey, final boolean caseSensitive) {
        assertThat(actual.getName(), is(name));
        assertThat(actual.getDataType(), is(dataType));
        assertThat(actual.isPrimaryKey(), is(primaryKey));
        assertThat(actual.isCaseSensitive(), is(caseSensitive));
    }
    
    @Test
    void assertLoadWhenThrowsSQLException() throws SQLException {
        when(columnResultSet.next()).thenReturn(true, false);
        when(columnResultSet.getString("TABLE_NAME")).thenReturn("tbl");
        when(columnResultSet.getString("COLUMN_NAME")).thenReturn("pk_col");
        when(columnResultSet.getInt("DATA_TYPE")).thenReturn(Types.INTEGER);
        when(connection.createStatement().executeQuery(anyString())).thenThrow(SQLException.class);
        assertThrows(SQLException.class, () -> ColumnMetaDataLoader.load(connection, "tbl", databaseType));
    }
}
