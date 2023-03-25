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

package org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.driver.jdbc.metadata;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JDBCQueryResultMetaDataTest {
    
    private JDBCQueryResultMetaData queryResultMetaData;
    
    @BeforeEach
    void setUp() throws SQLException {
        queryResultMetaData = new JDBCQueryResultMetaData(mockResultSetMetaData());
    }
    
    private ResultSetMetaData mockResultSetMetaData() throws SQLException {
        ResultSetMetaData result = mock(ResultSetMetaData.class);
        when(result.getColumnCount()).thenReturn(1);
        when(result.getColumnName(1)).thenReturn("order_id");
        when(result.getColumnLabel(1)).thenReturn("oid");
        when(result.getColumnTypeName(1)).thenReturn("INT");
        when(result.getTableName(1)).thenReturn("order");
        when(result.getColumnType(1)).thenReturn(Types.INTEGER);
        when(result.getColumnDisplaySize(1)).thenReturn(10);
        when(result.getScale(1)).thenReturn(0);
        when(result.isSigned(1)).thenReturn(true);
        when(result.isNullable(1)).thenReturn(ResultSetMetaData.columnNoNulls);
        when(result.isAutoIncrement(1)).thenReturn(true);
        return result;
    }
    
    @Test
    void assertGetColumnCount() throws SQLException {
        assertThat(queryResultMetaData.getColumnCount(), is(1));
    }
    
    @Test
    void assertGetColumnName() throws SQLException {
        assertThat(queryResultMetaData.getColumnName(1), is("order_id"));
    }
    
    @Test
    void assertGetColumnLabel() throws SQLException {
        assertThat(queryResultMetaData.getColumnLabel(1), is("oid"));
    }
    
    @Test
    void assertGetColumnTypeName() throws SQLException {
        assertThat(queryResultMetaData.getColumnTypeName(1), is("INT"));
    }
    
    @Test
    void assertGetTableName() throws SQLException {
        assertThat(queryResultMetaData.getTableName(1), is("order"));
    }
    
    @Test
    void assertGetColumnType() throws SQLException {
        assertThat(queryResultMetaData.getColumnType(1), is(Types.INTEGER));
    }
    
    @Test
    void assertGetColumnLength() throws SQLException {
        assertThat(queryResultMetaData.getColumnLength(1), is(10));
    }
    
    @Test
    void assertGetDecimals() throws SQLException {
        assertThat(queryResultMetaData.getDecimals(1), is(0));
    }
    
    @Test
    void assertIsSigned() throws SQLException {
        assertTrue(queryResultMetaData.isSigned(1));
    }
    
    @Test
    void assertIsNotNull() throws SQLException {
        assertTrue(queryResultMetaData.isNotNull(1));
    }
    
    @Test
    void assertIsAutoIncrement() throws SQLException {
        assertTrue(queryResultMetaData.isAutoIncrement(1));
    }
}
