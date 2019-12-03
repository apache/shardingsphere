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

package org.apache.shardingsphere.core.merge.dal.show;

import org.apache.shardingsphere.core.constant.ShardingConstant;
import org.apache.shardingsphere.core.execute.sql.execute.result.QueryResult;
import org.apache.shardingsphere.core.merge.fixture.ResultSetBasedQueryResultFixture;
import org.junit.Before;
import org.junit.Test;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ShowDatabasesMergedResultTest {
    
    private ShowDatabasesMergedResult showDatabasesMergedResult;
    
    @Before
    public void setUp() {
        showDatabasesMergedResult = new ShowDatabasesMergedResult();
    }
    
    @Test
    public void assertNext() {
        assertTrue(showDatabasesMergedResult.next());
        assertFalse(showDatabasesMergedResult.next());
    }
    
    @Test
    public void assertGetValueWithColumnIndex() throws SQLException {
        assertTrue(showDatabasesMergedResult.next());
        assertThat(showDatabasesMergedResult.getValue(1, Object.class).toString(), is(ShardingConstant.LOGIC_SCHEMA_NAME));
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetCalendarValueWithColumnIndex() throws SQLException {
        showDatabasesMergedResult.getCalendarValue(1, Object.class, Calendar.getInstance());
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetInputStreamWithColumnIndex() throws SQLException {
        showDatabasesMergedResult.getInputStream(1, "Ascii");
    }
    
    @Test
    public void assertWasNull() {
        assertFalse(showDatabasesMergedResult.wasNull());
    }

    @Test
    public void assertMergeNext() throws SQLException {
        ShowDatabasesMergedResult showDatabasesMergedResult = buildMergedShowDatabasesMergedResult();
        assertTrue(showDatabasesMergedResult.next());
        assertTrue(showDatabasesMergedResult.next());
        assertTrue(showDatabasesMergedResult.next());
        assertTrue(showDatabasesMergedResult.next());
        assertTrue(showDatabasesMergedResult.next());
        assertFalse(showDatabasesMergedResult.next());
    }

    @Test
    public void assertSchemes() throws SQLException {
        ShowDatabasesMergedResult showDatabasesMergedResult = buildMergedShowDatabasesMergedResult();
        showDatabasesMergedResult.next();
        assertThat(showDatabasesMergedResult.getValue(1, String.class).toString(), is("A"));
        showDatabasesMergedResult.next();
        assertThat(showDatabasesMergedResult.getValue(1, String.class).toString(), is("B"));
        showDatabasesMergedResult.next();
        assertThat(showDatabasesMergedResult.getValue(1, String.class).toString(), is("C"));
        showDatabasesMergedResult.next();
        assertThat(showDatabasesMergedResult.getValue(1, String.class).toString(), is("D"));
        showDatabasesMergedResult.next();
        assertThat(showDatabasesMergedResult.getValue(1, String.class).toString(), is("E"));
    }

    private ShowDatabasesMergedResult buildMergedShowDatabasesMergedResult() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.next()).thenReturn(true, true, true, false);
        when(resultSet.getString(anyInt())).thenReturn("A", "B", "C");
        ResultSetMetaData resultSetMetaData = mock(ResultSetMetaData.class);
        when(resultSet.getMetaData()).thenReturn(resultSetMetaData);
        when(resultSetMetaData.getColumnCount()).thenReturn(1);
        when(resultSetMetaData.getColumnLabel(1)).thenReturn("SCHEMA_NAME");

        ResultSet resultSet2 = mock(ResultSet.class);
        when(resultSet2.next()).thenReturn(true, true, false);
        when(resultSet2.getString(anyInt())).thenReturn("D", "E");
        ResultSetMetaData resultSetMetaData2 = mock(ResultSetMetaData.class);
        when(resultSet2.getMetaData()).thenReturn(resultSetMetaData);
        when(resultSetMetaData2.getColumnCount()).thenReturn(1);
        when(resultSetMetaData2.getColumnLabel(1)).thenReturn("SCHEMA_NAME");

        List<QueryResult> queryResults = new ArrayList<>();
        queryResults.add(new ResultSetBasedQueryResultFixture(resultSet));
        queryResults.add(new ResultSetBasedQueryResultFixture(resultSet2));
        return new ShowDatabasesMergedResult(null, queryResults);
    }
}
