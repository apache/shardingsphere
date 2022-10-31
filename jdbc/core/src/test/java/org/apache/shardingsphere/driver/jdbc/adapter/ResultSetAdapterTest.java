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

package org.apache.shardingsphere.driver.jdbc.adapter;

import org.apache.shardingsphere.driver.jdbc.core.resultset.ShardingSphereResultSet;
import org.apache.shardingsphere.driver.jdbc.core.statement.ShardingSphereStatement;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionContext;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.junit.Test;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public final class ResultSetAdapterTest {
    
    @Test
    public void assertClose() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        ShardingSphereResultSet actual = mockShardingSphereResultSet(resultSet);
        actual.close();
        assertTrue(actual.isClosed());
        verify(resultSet).close();
    }
    
    @Test
    public void assertSetFetchDirection() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        ShardingSphereResultSet actual = mockShardingSphereResultSet(resultSet);
        actual.setFetchDirection(ResultSet.FETCH_REVERSE);
        verify(resultSet).setFetchDirection(ResultSet.FETCH_REVERSE);
    }
    
    @Test
    public void assertSetFetchSize() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        ShardingSphereResultSet actual = mockShardingSphereResultSet(resultSet);
        actual.setFetchSize(100);
        verify(resultSet).setFetchSize(100);
    }
    
    @Test
    public void assertGetType() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getType()).thenReturn(ResultSet.TYPE_FORWARD_ONLY);
        ShardingSphereResultSet actual = mockShardingSphereResultSet(resultSet);
        assertThat(actual.getType(), is(ResultSet.TYPE_FORWARD_ONLY));
    }
    
    @Test
    public void assertGetConcurrency() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getConcurrency()).thenReturn(ResultSet.CONCUR_READ_ONLY);
        ShardingSphereResultSet actual = mockShardingSphereResultSet(resultSet);
        assertThat(actual.getConcurrency(), is(ResultSet.CONCUR_READ_ONLY));
    }
    
    @Test
    public void assertGetStatement() throws SQLException {
        assertNotNull(mockShardingSphereResultSet(mock(ResultSet.class)).getStatement());
    }
    
    @Test
    public void assertClearWarnings() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        ShardingSphereResultSet actual = mockShardingSphereResultSet(resultSet);
        actual.clearWarnings();
        verify(resultSet).clearWarnings();
    }
    
    @Test
    public void assertGetMetaData() throws SQLException {
        assertThat(mockShardingSphereResultSet(mock(ResultSet.class)).getMetaData().getColumnLabel(1), is("col"));
    }
    
    @Test
    public void assertFindColumn() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.findColumn("col")).thenReturn(1);
        ShardingSphereResultSet actual = mockShardingSphereResultSet(resultSet);
        assertThat(actual.findColumn("col"), is(1));
    }
    
    private ShardingSphereResultSet mockShardingSphereResultSet(final ResultSet resultSet) throws SQLException {
        ResultSetMetaData resultSetMetaData = mock(ResultSetMetaData.class);
        when(resultSetMetaData.getColumnLabel(1)).thenReturn("col");
        when(resultSetMetaData.getColumnCount()).thenReturn(1);
        when(resultSet.getMetaData()).thenReturn(resultSetMetaData);
        return new ShardingSphereResultSet(Collections.singletonList(resultSet), mock(MergedResult.class), mock(ShardingSphereStatement.class, RETURNS_DEEP_STUBS), mock(ExecutionContext.class));
    }
}
