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

import org.apache.shardingsphere.core.optimize.sharding.segment.select.item.AggregationDistinctSelectItem;
import org.apache.shardingsphere.core.parse.core.constant.AggregationType;
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
import java.util.LinkedList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class AggregationDistinctQueryResultTest {
    
    private AggregationDistinctQueryResult aggregationDistinctQueryResult;
    
    @Before
    public void setUp() throws SQLException {
        aggregationDistinctQueryResult = new AggregationDistinctQueryResult(getQueryResults(), getAggregationDistinctSelectItems());
    }
    
    private Collection<QueryResult> getQueryResults() throws SQLException {
        Collection<QueryResult> result = new LinkedList<>();
        for (int i = 1; i <= 2; i++) {
            QueryResult queryResult = mock(QueryResult.class);
            when(queryResult.next()).thenReturn(true).thenReturn(false);
            when(queryResult.getColumnCount()).thenReturn(5);
            when(queryResult.getColumnLabel(1)).thenReturn("order_id");
            when(queryResult.getColumnLabel(2)).thenReturn("c");
            when(queryResult.getColumnLabel(3)).thenReturn("a");
            when(queryResult.getColumnLabel(4)).thenReturn("AVG_DERIVED_COUNT_0");
            when(queryResult.getColumnLabel(5)).thenReturn("AVG_DERIVED_SUM_0");
            when(queryResult.getValue(1, Object.class)).thenReturn(10 * i);
            when(queryResult.getValue(2, Object.class)).thenReturn(10 * i);
            when(queryResult.getValue(3, Object.class)).thenReturn(10 * i);
            when(queryResult.getValue(4, Object.class)).thenReturn(10 * i);
            when(queryResult.getValue(5, Object.class)).thenReturn(10 * i);
            doReturn(getQueryResultMetaData()).when(queryResult).getQueryResultMetaData();
            result.add(queryResult);
            result.add(queryResult);
        }
        return result;
    }

    private QueryResultMetaData getQueryResultMetaData() throws SQLException {
        QueryResultMetaData result = mock(QueryResultMetaData.class);
        when(result.getColumnCount()).thenReturn(5);
        when(result.getColumnLabel(1)).thenReturn("order_id");
        when(result.getColumnLabel(2)).thenReturn("c");
        when(result.getColumnLabel(3)).thenReturn("a");
        when(result.getColumnLabel(4)).thenReturn("AVG_DERIVED_COUNT_0");
        when(result.getColumnLabel(5)).thenReturn("AVG_DERIVED_SUM_0");
        when(result.getColumnIndex("order_id")).thenReturn(1);
        when(result.getColumnIndex("c")).thenReturn(2);
        when(result.getColumnIndex("a")).thenReturn(3);
        when(result.getColumnIndex("AVG_DERIVED_COUNT_0")).thenReturn(4);
        when(result.getColumnIndex("AVG_DERIVED_SUM_0")).thenReturn(5);
        return result;
    }
    
    private List<AggregationDistinctSelectItem> getAggregationDistinctSelectItems() {
        List<AggregationDistinctSelectItem> result = new LinkedList<>();
        AggregationDistinctSelectItem distinctCountSelectItem = new AggregationDistinctSelectItem(0, 0, AggregationType.COUNT, "(DISTINCT order_id)", "c", "order_id");
        AggregationDistinctSelectItem distinctAvgSelectItem = new AggregationDistinctSelectItem(0, 0, AggregationType.AVG, "(DISTINCT order_id)", "a", "order_id");
        distinctAvgSelectItem.getDerivedAggregationItems().add(new AggregationDistinctSelectItem(0, 0, AggregationType.COUNT, "(DISTINCT order_id)", "AVG_DERIVED_COUNT_0", "order_id"));
        distinctAvgSelectItem.getDerivedAggregationItems().add(new AggregationDistinctSelectItem(0, 0, AggregationType.SUM, "(DISTINCT order_id)", "AVG_DERIVED_SUM_0", "order_id"));
        result.add(distinctCountSelectItem);
        result.add(distinctAvgSelectItem);
        return result;
    }
    
    @Test
    public void assertDivide() throws SQLException {
        List<DistinctQueryResult> actual = aggregationDistinctQueryResult.divide();
        assertThat(actual.size(), is(2));
        assertThat(actual.iterator().next().getColumnCount(), is((Object) 5));
    }
    
    @Test
    public void assertGetValueByColumnIndex() {
        aggregationDistinctQueryResult.next();
        assertThat(aggregationDistinctQueryResult.getValue(1, Object.class), is((Object) 10));
        assertThat(aggregationDistinctQueryResult.getValue(2, Object.class), is((Object) 1));
        assertThat(aggregationDistinctQueryResult.getValue(3, Object.class), is((Object) 10));
        assertThat(aggregationDistinctQueryResult.getValue(4, Object.class), is((Object) 1));
        assertThat(aggregationDistinctQueryResult.getValue(5, Object.class), is((Object) 10));
    }
    
    @Test
    public void assertGetValueByColumnLabel() {
        aggregationDistinctQueryResult.next();
        assertThat(aggregationDistinctQueryResult.getValue("order_id", Object.class), is((Object) 10));
        assertThat(aggregationDistinctQueryResult.getValue("a", Object.class), is((Object) 10));
    }
    
    @Test
    public void assertGetCalendarValueByColumnIndex() {
        aggregationDistinctQueryResult.next();
        assertThat(aggregationDistinctQueryResult.getCalendarValue(1, Object.class, Calendar.getInstance()), is((Object) 10));
    }
    
    @Test
    public void assertGetCalendarValueByColumnLabel() {
        aggregationDistinctQueryResult.next();
        assertThat(aggregationDistinctQueryResult.getCalendarValue("order_id", Object.class, Calendar.getInstance()), is((Object) 10));
    }
    
    @Test
    public void assertGetInputStreamByColumnIndex() throws IOException {
        aggregationDistinctQueryResult.next();
        assertThat(aggregationDistinctQueryResult.getInputStream(1, "Unicode").read(), is(getInputStream(10).read()));
    }
    
    @Test
    public void assertGetInputStreamByColumnLabel() throws IOException {
        aggregationDistinctQueryResult.next();
        assertThat(aggregationDistinctQueryResult.getInputStream("order_id", "Unicode").read(), is(getInputStream(10).read()));
    }
    
    @Test
    public void assertWasNull() {
        assertTrue(aggregationDistinctQueryResult.wasNull());
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
    public void assertGetColumnCount() throws SQLException {
        assertThat(aggregationDistinctQueryResult.getColumnCount(), is(5));
    }
    
    @Test
    public void assertGetColumnLabel() throws SQLException {
        assertThat(aggregationDistinctQueryResult.getColumnLabel(3), is("a"));
        assertThat(aggregationDistinctQueryResult.getColumnLabel(1), is("order_id"));
    }
    
    @Test(expected = SQLException.class)
    public void assertGetColumnLabelWithException() throws SQLException {
        assertThat(aggregationDistinctQueryResult.getColumnLabel(6), is("order_id"));
    }
    
    @Test
    public void assertGetColumnIndex() {
        assertThat(aggregationDistinctQueryResult.getColumnIndex("c"), is(2));
    }
}
