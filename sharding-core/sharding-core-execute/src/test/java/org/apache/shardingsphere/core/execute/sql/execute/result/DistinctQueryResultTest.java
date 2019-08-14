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

package org.apache.shardingsphere.core.execute.sql.execute.result;

import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class DistinctQueryResultTest {
    
    private DistinctQueryResult distinctQueryResult;
    
    private QueryResultMetaData queryResultMetaData;
    
    @Before
    public void setUp() throws SQLException {
        queryResultMetaData = getQueryResultMetaData();
        Collection<QueryResult> queryResults = getQueryResults();
        List<String> distinctColumnLabels = Collections.singletonList("order_id");
        distinctQueryResult = new DistinctQueryResult(queryResults, distinctColumnLabels);
    }
    
    private Collection<QueryResult> getQueryResults() throws SQLException {
        Collection<QueryResult> result = new LinkedList<>();
        for (int i = 1; i <= 2; i++) {
            QueryResult queryResult = mock(QueryResult.class);
            when(queryResult.next()).thenReturn(true).thenReturn(false);
            when(queryResult.getColumnCount()).thenReturn(1);
            when(queryResult.getColumnLabel(1)).thenReturn("order_id");
            when(queryResult.getValue(1, Object.class)).thenReturn(10 * i);
            when(queryResult.isCaseSensitive(1)).thenReturn(true);
            doReturn(queryResultMetaData).when(queryResult).getQueryResultMetaData();
            result.add(queryResult);
            result.add(queryResult);
        }
        return result;
    }
    
    private QueryResultMetaData getQueryResultMetaData() throws SQLException {
        QueryResultMetaData result = mock(QueryResultMetaData.class);
        when(result.getColumnCount()).thenReturn(1);
        when(result.getColumnLabel(1)).thenReturn("order_id");
        when(result.getColumnIndex("order_id")).thenReturn(1);
        return result;
    }
    
    @Test
    public void assertDivide() throws SQLException {
        List<DistinctQueryResult> actual = distinctQueryResult.divide();
        assertThat(actual.size(), is(2));
        assertThat(actual.iterator().next().getColumnCount(), is((Object) 1));
    }
    
    @Test
    public void assertNext() {
        assertTrue(distinctQueryResult.next());
        assertTrue(distinctQueryResult.next());
        assertFalse(distinctQueryResult.next());
    }
    
    @Test
    public void assertGetValueByColumnIndex() {
        distinctQueryResult.next();
        assertThat(distinctQueryResult.getValue(1, Object.class), is((Object) 10));
    }
    
    @Test
    public void assertGetValueByColumnLabel() {
        distinctQueryResult.next();
        assertThat(distinctQueryResult.getValue("order_id", Object.class), is((Object) 10));
    }
    
    @Test
    public void assertGetCalendarValueByColumnIndex() {
        distinctQueryResult.next();
        assertThat(distinctQueryResult.getCalendarValue(1, Object.class, Calendar.getInstance()), is((Object) 10));
    }
    
    @Test
    public void assertGetCalendarValueByColumnLabel() {
        distinctQueryResult.next();
        assertThat(distinctQueryResult.getCalendarValue("order_id", Object.class, Calendar.getInstance()), is((Object) 10));
    }
    
    @Test
    public void assertGetInputStreamByColumnIndex() throws IOException {
        distinctQueryResult.next();
        assertThat(distinctQueryResult.getInputStream(1, "Unicode").read(), is(getInputStream(10).read()));
    }
    
    private InputStream getInputStream(final Object value) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(value);
        objectOutputStream.flush();
        objectOutputStream.close();
        return new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
    }
    
    @Test
    public void assertGetInputStreamByColumnLabel() throws IOException {
        distinctQueryResult.next();
        assertThat(distinctQueryResult.getInputStream("order_id", "Unicode").read(), is(getInputStream(10).read()));
    }
    
    @Test
    public void assertWasNull() {
        assertTrue(distinctQueryResult.wasNull());
    }
    
    @Test
    public void assertIsCaseSensitive() throws SQLException {
        when(queryResultMetaData.isCaseSensitive(1)).thenReturn(true);
        assertTrue(distinctQueryResult.isCaseSensitive(1));
    }
    
    @Test
    public void assertGetColumnCount() throws SQLException {
        assertThat(distinctQueryResult.getColumnCount(), is(1));
    }
    
    @Test
    public void assertGetColumnLabel() throws SQLException {
        assertThat(distinctQueryResult.getColumnLabel(1), is("order_id"));
    }
    
    @Test(expected = SQLException.class)
    public void assertGetColumnLabelWithException() throws SQLException {
        assertThat(distinctQueryResult.getColumnLabel(2), is("order_id"));
    }
    
    @Test
    public void assertGetColumnIndex() {
        assertThat(distinctQueryResult.getColumnIndex("order_id"), is(1));
    }
    
    @Test
    public void assertGetQueryResultMetaData() {
        assertThat(distinctQueryResult.getQueryResultMetaData(), is(queryResultMetaData));
    }
    
    @Test
    public void assertGetResultData() {
        assertThat(distinctQueryResult.getResultData().next().getColumnValue(1), is((Object) 10));
    }
    
    @Test
    public void assertGetCurrentRow() {
        distinctQueryResult.next();
        assertThat(distinctQueryResult.getCurrentRow().getColumnValue(1), is((Object) 10));
    }
    
    @Test(expected = SQLException.class)
    public void assertGetColumnLabelAndIndexMapWithException() throws SQLException {
        QueryResult queryResult = mock(QueryResult.class);
        when(queryResult.next()).thenReturn(true).thenReturn(false);
        when(queryResult.getColumnCount()).thenThrow(SQLException.class);
        when(queryResult.getColumnLabel(1)).thenReturn("order_id");
        when(queryResult.getValue(1, Object.class)).thenReturn(10);
        Collection<QueryResult> queryResults = new LinkedList<>();
        queryResults.add(queryResult);
        List<String> distinctColumnLabels = Collections.singletonList("order_id");
        distinctQueryResult = new DistinctQueryResult(queryResults, distinctColumnLabels);
    }
    
    @Test(expected = SQLException.class)
    public void assertGetResultDataWithException() throws SQLException {
        QueryResult queryResult = mock(QueryResult.class);
        when(queryResult.next()).thenThrow(SQLException.class);
        when(queryResult.getColumnCount()).thenReturn(1);
        when(queryResult.getColumnLabel(1)).thenReturn("order_id");
        when(queryResult.getValue(1, Object.class)).thenReturn(10);
        Collection<QueryResult> queryResults = new LinkedList<>();
        queryResults.add(queryResult);
        List<String> distinctColumnLabels = Collections.singletonList("order_id");
        distinctQueryResult = new DistinctQueryResult(queryResults, distinctColumnLabels);
    }
}
