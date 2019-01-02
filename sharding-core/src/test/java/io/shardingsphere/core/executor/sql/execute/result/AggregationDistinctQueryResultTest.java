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
import java.util.LinkedList;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AggregationDistinctQueryResultTest {
    
    private AggregationDistinctQueryResult aggregationDistinctQueryResult;
    
    @Before
    public void setUp() {
        
    }
    
    @SneakyThrows
    private Collection<QueryResult> getQueryResults() {
        Collection<QueryResult> result = new LinkedList<>();
        for (int i = 1; i <= 2; i++) {
            QueryResult queryResult = mock(QueryResult.class);
            when(queryResult.next()).thenReturn(true).thenReturn(false);
            when(queryResult.getColumnCount()).thenReturn(3);
            when(queryResult.getColumnLabel(1)).thenReturn("order_id");
            when(queryResult.getColumnLabel(2)).thenReturn("order_id");
            when(queryResult.getColumnLabel(3)).thenReturn("order_id");
            when(queryResult.getValue(1, Object.class)).thenReturn(10 * i);
            when(queryResult.getValue(2, Object.class)).thenReturn(10 * i);
            when(queryResult.getValue(3, Object.class)).thenReturn(10 * i);
            result.add(queryResult);
            result.add(queryResult);
        }
        return result;
    }
    
    @Test
    public void testDivide() {
    }
    
    @Test
    public void testGetValue() {
    }
    
    @Test
    public void testGetValue1() {
    }
    
    @Test
    public void testGetCalendarValue() {
    }
    
    @Test
    public void testGetCalendarValue1() {
    }
    
    @Test
    public void testGetInputStream() {
    }
    
    @Test
    public void testGetInputStream1() {
    }
    
    @Test
    public void testWasNull() {
    }
    
    @Test
    public void testGetColumnCount() {
    }
    
    @Test
    public void testGetColumnLabel() {
    }
    
    @Test
    public void testGetColumnIndex() {
    }
}
