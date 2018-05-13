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

package io.shardingjdbc.core.merger.iterator;

import io.shardingjdbc.core.merger.MergeEngine;
import io.shardingjdbc.core.merger.ResultSetMerger;
import io.shardingjdbc.core.parsing.parser.sql.dql.select.SelectStatement;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class IteratorStreamResultSetMergerTest {
    
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
    }
    
    @Test
    public void assertNextForResultSetsAllEmpty() throws SQLException {
        mergeEngine = new MergeEngine(resultSets, selectStatement);
        ResultSetMerger actual = mergeEngine.merge();
        assertFalse(actual.next());
    }
    
    @Test
    public void assertNextForResultSetsAllNotEmpty() throws SQLException {
        for (ResultSet each : resultSets) {
            when(each.next()).thenReturn(true, false);
        }
        mergeEngine = new MergeEngine(resultSets, selectStatement);
        ResultSetMerger actual = mergeEngine.merge();
        assertTrue(actual.next());
        assertTrue(actual.next());
        assertTrue(actual.next());
        assertFalse(actual.next());
    }
    
    @Test
    public void assertNextForFirstResultSetsNotEmptyOnly() throws SQLException {
        when(resultSets.get(0).next()).thenReturn(true, false);
        mergeEngine = new MergeEngine(resultSets, selectStatement);
        ResultSetMerger actual = mergeEngine.merge();
        assertTrue(actual.next());
        assertFalse(actual.next());
    }
    
    @Test
    public void assertNextForMiddleResultSetsNotEmpty() throws SQLException {
        when(resultSets.get(1).next()).thenReturn(true, false);
        mergeEngine = new MergeEngine(resultSets, selectStatement);
        ResultSetMerger actual = mergeEngine.merge();
        assertTrue(actual.next());
        assertFalse(actual.next());
    }
    
    @Test
    public void assertNextForLastResultSetsNotEmptyOnly() throws SQLException {
        when(resultSets.get(2).next()).thenReturn(true, false);
        mergeEngine = new MergeEngine(resultSets, selectStatement);
        ResultSetMerger actual = mergeEngine.merge();
        assertTrue(actual.next());
        assertFalse(actual.next());
    }
    
    @Test
    public void assertNextForMix() throws SQLException {
        resultSets.add(mock(ResultSet.class));
        resultSets.add(mock(ResultSet.class));
        resultSets.add(mock(ResultSet.class));
        when(resultSets.get(1).next()).thenReturn(true, false);
        when(resultSets.get(3).next()).thenReturn(true, false);
        when(resultSets.get(5).next()).thenReturn(true, false);
        mergeEngine = new MergeEngine(resultSets, selectStatement);
        ResultSetMerger actual = mergeEngine.merge();
        assertTrue(actual.next());
        assertTrue(actual.next());
        assertTrue(actual.next());
        assertFalse(actual.next());
    }
}
