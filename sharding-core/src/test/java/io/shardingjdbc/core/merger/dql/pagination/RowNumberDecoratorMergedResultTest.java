package io.shardingjdbc.core.merger.dql.pagination;

import com.google.common.collect.Lists;
import io.shardingjdbc.core.constant.DatabaseType;
import io.shardingjdbc.core.merger.MergedResult;
import io.shardingjdbc.core.merger.QueryResult;
import io.shardingjdbc.core.merger.dql.DQLMergeEngine;
import io.shardingjdbc.core.merger.fixture.TestQueryResult;
import io.shardingjdbc.core.parsing.parser.context.limit.Limit;
import io.shardingjdbc.core.parsing.parser.context.limit.LimitValue;
import io.shardingjdbc.core.parsing.parser.sql.dql.select.SelectStatement;
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

public class RowNumberDecoratorMergedResultTest {
    
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
        Limit limit = new Limit(DatabaseType.Oracle);
        limit.setOffset(new LimitValue(Integer.MAX_VALUE, -1, true));
        selectStatement.setLimit(limit);
        mergeEngine = new DQLMergeEngine(queryResults, selectStatement);
        MergedResult actual = mergeEngine.merge();
        assertFalse(actual.next());
    }
    
    @Test
    public void assertNextWithoutOffsetWithoutRowCount() throws SQLException {
        Limit limit = new Limit(DatabaseType.Oracle);
        selectStatement.setLimit(limit);
        mergeEngine = new DQLMergeEngine(queryResults, selectStatement);
        MergedResult actual = mergeEngine.merge();
        for (int i = 0; i < 8; i++) {
            assertTrue(actual.next());
        }
        assertFalse(actual.next());
    }
    
    @Test
    public void assertNextForRowCountBoundOpendedFalse() throws SQLException {
        Limit limit = new Limit(DatabaseType.Oracle);
        limit.setOffset(new LimitValue(2, -1, true));
        limit.setRowCount(new LimitValue(4, -1, false));
        selectStatement.setLimit(limit);
        mergeEngine = new DQLMergeEngine(queryResults, selectStatement);
        MergedResult actual = mergeEngine.merge();
        assertTrue(actual.next());
        assertTrue(actual.next());
        assertFalse(actual.next());
    }
    
    @Test
    public void assertNextForRowCountBoundOpendedTrue() throws SQLException {
        Limit limit = new Limit(DatabaseType.Oracle);
        limit.setOffset(new LimitValue(2, -1, true));
        limit.setRowCount(new LimitValue(4, -1, true));
        selectStatement.setLimit(limit);
        mergeEngine = new DQLMergeEngine(queryResults, selectStatement);
        MergedResult actual = mergeEngine.merge();
        assertTrue(actual.next());
        assertTrue(actual.next());
        assertTrue(actual.next());
        assertFalse(actual.next());
    }
}
