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

package org.apache.shardingsphere.infra.executor.sql.execute.result.query.jdbc;

import org.apache.shardingsphere.infra.executor.sql.execute.result.query.jdbc.fixture.AbstractJDBCQueryResultSetFixture;
import org.junit.Before;
import org.junit.Test;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class AbstractJDBCQueryResultTest {
    
    private AbstractJDBCQueryResultSetFixture queryResult;
    
    @Before
    public void setUp() throws SQLException {
        queryResult = new AbstractJDBCQueryResultSetFixture(mockResultSetMetaData());
    }
    
    private ResultSetMetaData mockResultSetMetaData() throws SQLException {
        ResultSetMetaData result = mock(ResultSetMetaData.class);
        when(result.getColumnCount()).thenReturn(1);
        when(result.getColumnName(1)).thenReturn("order_id");
        when(result.getColumnLabel(1)).thenReturn("oid");
        when(result.getColumnTypeName(1)).thenReturn("INT");
        return result;
    }
    
    @Test
    public void assertGetColumnCount() throws SQLException {
        assertThat(queryResult.getColumnCount(), is(1));
    }
    
    @Test
    public void assertGetColumnName() throws SQLException {
        assertThat(queryResult.getColumnName(1), is("order_id"));
    }
    
    @Test
    public void assertGetColumnLabel() throws SQLException {
        assertThat(queryResult.getColumnLabel(1), is("oid"));
    }
    
    @Test
    public void assertGetColumnTypeName() throws SQLException {
        assertThat(queryResult.getColumnTypeName(1), is("INT"));
    }
}
