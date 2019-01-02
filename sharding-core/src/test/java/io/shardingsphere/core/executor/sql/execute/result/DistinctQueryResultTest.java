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

import io.shardingsphere.core.merger.QueryResult;
import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
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
            when(queryResult.getColumnLabel(i)).thenReturn("order_id");
            when(queryResult.getValue(i, Object.class)).thenReturn(10 * i);
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
        assertEquals(10, distinctQueryResult.getValue(1, Object.class));
    }
    
    @Test
    public void assertGetValueByColumnLabel() {
        distinctQueryResult.next();
        assertEquals(10, distinctQueryResult.getValue("order_id", Object.class));
    }
    
    @Test
    public void assertGetCalendarValue() {
    }
    
    @Test
    public void assertGetCalendarValue1() {
    }
    
    @Test
    public void assertGetInputStream() {
    }
    
    @Test
    public void assertGetInputStream1() {
    }
    
    @Test
    public void assertWasNull() {
    }
    
    @Test
    public void assertGetColumnCount() {
    }
    
    @Test
    public void assertGetColumnLabel() {
    }
    
    @Test
    public void assertGetColumnIndex() {
    }
    
    @Test
    public void assertGetColumnLabelAndIndexMap() {
    }
    
    @Test
    public void assertGetResultData() {
    }
    
    @Test
    public void assertGetCurrentRow() {
    }
}
