/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingjdbc.core.merger.orderby;

import io.shardingjdbc.core.constant.OrderType;
import io.shardingjdbc.core.merger.MergeEngine;
import io.shardingjdbc.core.merger.ResultSetMerger;
import io.shardingjdbc.core.parsing.parser.context.OrderItem;
import io.shardingjdbc.core.parsing.parser.sql.dql.select.SelectStatement;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class OrderByStreamResultSetMergerTest {
    
    private MergeEngine mergeEngine;
    
    private List<ResultSet> resultSets;
    
    private SelectStatement selectStatement;
    
    @Before
    public void setUp() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        ResultSetMetaData resultSetMetaData = mock(ResultSetMetaData.class);
        when(resultSet.getMetaData()).thenReturn(resultSetMetaData);
        resultSets = Lists.newArrayList(resultSet, mock(ResultSet.class), mock(ResultSet.class));
        selectStatement = new SelectStatement();
        selectStatement.getOrderByItems().add(new OrderItem(1, OrderType.ASC, OrderType.ASC));
    }
    
    @Test
    public void assertNextForResultSetsAllEmpty() throws SQLException {
        mergeEngine = new MergeEngine(resultSets, selectStatement);
        ResultSetMerger actual = mergeEngine.merge();
        assertFalse(actual.next());
    }
    
    @Test
    public void assertNextForSomeResultSetsEmpty() throws SQLException {
        mergeEngine = new MergeEngine(resultSets, selectStatement);
        when(resultSets.get(0).next()).thenReturn(true, false);
        when(resultSets.get(0).getObject(1)).thenReturn("2");
        when(resultSets.get(2).next()).thenReturn(true, true, false);
        when(resultSets.get(2).getObject(1)).thenReturn("1", "1", "3", "3");
        ResultSetMerger actual = mergeEngine.merge();
        assertTrue(actual.next());
        assertThat(actual.getValue(1, Object.class).toString(), is("1"));
        assertTrue(actual.next());
        assertThat(actual.getValue(1, Object.class).toString(), is("2"));
        assertTrue(actual.next());
        assertThat(actual.getValue(1, Object.class).toString(), is("3"));
        assertFalse(actual.next());
    }
    
    @Test
    public void assertNextForMix() throws SQLException {
        mergeEngine = new MergeEngine(resultSets, selectStatement);
        when(resultSets.get(0).next()).thenReturn(true, false);
        when(resultSets.get(0).getObject(1)).thenReturn("2");
        when(resultSets.get(1).next()).thenReturn(true, true, true, false);
        when(resultSets.get(1).getObject(1)).thenReturn("2", "2", "3", "3", "4", "4");
        when(resultSets.get(2).next()).thenReturn(true, true, false);
        when(resultSets.get(2).getObject(1)).thenReturn("1", "1", "3", "3");
        ResultSetMerger actual = mergeEngine.merge();
        assertTrue(actual.next());
        assertThat(actual.getValue(1, Object.class).toString(), is("1"));
        assertTrue(actual.next());
        assertThat(actual.getValue(1, Object.class).toString(), is("2"));
        assertTrue(actual.next());
        assertThat(actual.getValue(1, Object.class).toString(), is("2"));
        assertTrue(actual.next());
        assertThat(actual.getValue(1, Object.class).toString(), is("3"));
        assertTrue(actual.next());
        assertThat(actual.getValue(1, Object.class).toString(), is("3"));
        assertTrue(actual.next());
        assertThat(actual.getValue(1, Object.class).toString(), is("4"));
        assertFalse(actual.next());
    }
}
