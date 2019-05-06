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

package io.shardingsphere.core.merger.dql.pagination;

import com.google.common.collect.Lists;
import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.merger.MergedResult;
import io.shardingsphere.core.merger.QueryResult;
import io.shardingsphere.core.merger.dql.DQLMergeEngine;
import io.shardingsphere.core.merger.fixture.TestQueryResult;
import io.shardingsphere.core.parsing.parser.context.limit.Limit;
import io.shardingsphere.core.parsing.parser.context.limit.LimitValue;
import io.shardingsphere.core.parsing.parser.sql.dql.select.SelectStatement;
import org.junit.Before;
import org.junit.Test;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class TopAndRowNumberDecoratorMergedResultTest {
    
    private DQLMergeEngine mergeEngine;
    
    private List<QueryResult> queryResults;
    
    private SelectStatement selectStatement;
    
    @Before
    public void setUp() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        ResultSetMetaData resultSetMetaData = mock(ResultSetMetaData.class);
        when(resultSet.getMetaData()).thenReturn(resultSetMetaData);
        List<ResultSet> resultSets = Lists.newArrayList(resultSet, mock(ResultSet.class), mock(ResultSet.class), mock(ResultSet.class));
        for (ResultSet each : resultSets) {
            when(each.next()).thenReturn(true, true, false);
        }
        queryResults = new ArrayList<>(resultSets.size());
        for (ResultSet each : resultSets) {
            queryResults.add(new TestQueryResult(each));
        }
        selectStatement = new SelectStatement();
    }
    
    @Test
    public void assertNextForSkipAll() throws SQLException {
        Limit limit = new Limit();
        limit.setOffset(new LimitValue(Integer.MAX_VALUE, -1, true));
        selectStatement.setLimit(limit);
        mergeEngine = new DQLMergeEngine(DatabaseType.SQLServer, selectStatement, queryResults);
        MergedResult actual = mergeEngine.merge();
        assertFalse(actual.next());
    }
    
    @Test
    public void assertNextWithoutOffsetWithRowCount() throws SQLException {
        Limit limit = new Limit();
        limit.setRowCount(new LimitValue(5, -1, false));
        selectStatement.setLimit(limit);
        mergeEngine = new DQLMergeEngine(DatabaseType.SQLServer, selectStatement, queryResults);
        MergedResult actual = mergeEngine.merge();
        for (int i = 0; i < 5; i++) {
            assertTrue(actual.next());
        }
        assertFalse(actual.next());
    }
    
    @Test
    public void assertNextWithOffsetWithoutRowCount() throws SQLException {
        Limit limit = new Limit();
        limit.setOffset(new LimitValue(2, -1, true));
        selectStatement.setLimit(limit);
        mergeEngine = new DQLMergeEngine(DatabaseType.SQLServer, selectStatement, queryResults);
        MergedResult actual = mergeEngine.merge();
        for (int i = 0; i < 7; i++) {
            assertTrue(actual.next());
        }
        assertFalse(actual.next());
    }
    
    @Test
    public void assertNextWithOffsetBoundOpenedFalse() throws SQLException {
        Limit limit = new Limit();
        limit.setOffset(new LimitValue(2, -1, false));
        limit.setRowCount(new LimitValue(4, -1, false));
        selectStatement.setLimit(limit);
        mergeEngine = new DQLMergeEngine(DatabaseType.SQLServer, selectStatement, queryResults);
        MergedResult actual = mergeEngine.merge();
        assertTrue(actual.next());
        assertTrue(actual.next());
        assertFalse(actual.next());
    }

    @Test
    public void assertNextWithOffsetBoundOpenedTrue() throws SQLException {
        Limit limit = new Limit();
        limit.setOffset(new LimitValue(2, -1, true));
        limit.setRowCount(new LimitValue(4, -1, false));
        selectStatement.setLimit(limit);
        mergeEngine = new DQLMergeEngine(DatabaseType.SQLServer, selectStatement, queryResults);
        MergedResult actual = mergeEngine.merge();
        assertTrue(actual.next());
        assertTrue(actual.next());
        assertTrue(actual.next());
        assertFalse(actual.next());
    }
}
