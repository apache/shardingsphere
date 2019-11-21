/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.core.executor.sql.execute.result;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import io.shardingsphere.core.merger.QueryResult;
import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DistinctQueryResultTest {
    
    private DistinctQueryResult distinctQueryResult;
    
    @Before
    public void setUp() {
        Collection<QueryResult> queryResults = getQueryResults();
        List<String> distinctColumnLabels = Collections.singletonList("order_id");
        distinctQueryResult = new DistinctQueryResult(queryResults, distinctColumnLabels);
    }
    
    @SneakyThrows
    private Collection<QueryResult> getQueryResults() {
        Collection<QueryResult> result = new LinkedList<>();
        for (int i = 1; i <= 2; i++) {
            QueryResult queryResult = mock(QueryResult.class);
            when(queryResult.next()).thenReturn(true).thenReturn(false);
            when(queryResult.getColumnCount()).thenReturn(1);
            when(queryResult.getColumnLabel(1)).thenReturn("order_id");
            when(queryResult.getValue(1, Object.class)).thenReturn(10 * i);
            result.add(queryResult);
            result.add(queryResult);
        }
        return result;
    }
    
    @Test
    public void assertDivide() {
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
    @SneakyThrows
    public void assertGetInputStreamByColumnIndex() {
        distinctQueryResult.next();
        assertThat(distinctQueryResult.getInputStream(1, "Unicode").read(), is(getInputStream(10).read()));
    }
    
    @SneakyThrows
    private InputStream getInputStream(final Object value) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(value);
        objectOutputStream.flush();
        objectOutputStream.close();
        return new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
    }
    
    @Test
    @SneakyThrows
    public void assertGetInputStreamByColumnLabel() {
        distinctQueryResult.next();
        assertThat(distinctQueryResult.getInputStream("order_id", "Unicode").read(), is(getInputStream(10).read()));
    }
    
    @Test
    public void assertWasNull() {
        assertTrue(distinctQueryResult.wasNull());
    }
    
    @Test
    public void assertGetColumnCount() {
        assertThat(distinctQueryResult.getColumnCount(), is(1));
    }
    
    @Test
    @SneakyThrows
    public void assertGetColumnLabel() {
        assertThat(distinctQueryResult.getColumnLabel(1), is("order_id"));
    }
    
    @Test(expected = SQLException.class)
    @SneakyThrows
    public void assertGetColumnLabelWithException() {
        assertThat(distinctQueryResult.getColumnLabel(2), is("order_id"));
    }
    
    @Test
    public void assertGetColumnIndex() {
        assertThat(distinctQueryResult.getColumnIndex("order_id"), is(1));
    }
    
    @Test
    public void assertGetColumnLabelAndIndexMap() {
        Multimap<String, Integer> expected = HashMultimap.create();
        expected.put("order_id", 1);
        assertThat(distinctQueryResult.getColumnLabelAndIndexMap(), is(expected));
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
    @SneakyThrows
    public void assertGetColumnLabelAndIndexMapWithException() {
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
    @SneakyThrows
    public void assertGetResultDataWithException() {
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
