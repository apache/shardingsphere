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

package org.apache.shardingsphere.sharding.merge.ddl.fetch;

import org.apache.shardingsphere.infra.binder.statement.ddl.CursorStatementContext;
import org.apache.shardingsphere.infra.binder.statement.ddl.FetchStatementContext;
import org.apache.shardingsphere.infra.context.ConnectionContext;
import org.apache.shardingsphere.infra.context.cursor.CursorConnectionContext;
import org.apache.shardingsphere.infra.database.DefaultDatabase;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResult;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.sharding.merge.ddl.ShardingDDLResultMerger;
import org.apache.shardingsphere.sql.parser.sql.common.constant.DirectionType;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.cursor.CursorNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.cursor.DirectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussCursorStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussFetchStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.dml.OpenGaussSelectStatement;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class FetchStreamMergedResultTest {
    
    private FetchStatementContext fetchCountStatementContext;
    
    private FetchStatementContext fetchAllStatementContext;
    
    private ShardingDDLResultMerger resultMerger;
    
    private ShardingSphereDatabase database;
    
    private ConnectionContext connectionContext;
    
    @Before
    public void setUp() {
        fetchCountStatementContext = new FetchStatementContext(createFetchStatement(false));
        fetchCountStatementContext.setUpCursorDefinition(createCursorStatementContext());
        fetchAllStatementContext = new FetchStatementContext(createFetchStatement(true));
        fetchAllStatementContext.setUpCursorDefinition(createCursorStatementContext());
        resultMerger = new ShardingDDLResultMerger();
        database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getName()).thenReturn(DefaultDatabase.LOGIC_NAME);
        connectionContext = mock(ConnectionContext.class);
        when(connectionContext.getCursorConnectionContext()).thenReturn(new CursorConnectionContext());
    }
    
    private OpenGaussFetchStatement createFetchStatement(final boolean containsAllDirectionType) {
        OpenGaussFetchStatement result = new OpenGaussFetchStatement();
        result.setCursorName(new CursorNameSegment(0, 0, new IdentifierValue("t_order_cursor")));
        if (containsAllDirectionType) {
            DirectionSegment direction = new DirectionSegment(0, 0);
            direction.setDirectionType(DirectionType.ALL);
            result.setDirection(direction);
        }
        return result;
    }
    
    private CursorStatementContext createCursorStatementContext() {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getName()).thenReturn(DefaultDatabase.LOGIC_NAME);
        OpenGaussCursorStatement cursorStatement = new OpenGaussCursorStatement();
        cursorStatement.setSelect(createSelectStatement());
        return new CursorStatementContext(Collections.singletonMap(DefaultDatabase.LOGIC_NAME, database), Collections.emptyList(), cursorStatement, DefaultDatabase.LOGIC_NAME);
    }
    
    private OpenGaussSelectStatement createSelectStatement() {
        OpenGaussSelectStatement result = new OpenGaussSelectStatement();
        result.setProjections(new ProjectionsSegment(0, 0));
        result.setFrom(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order"))));
        return result;
    }
    
    @Test
    public void assertNextForResultSetsAllEmpty() throws SQLException {
        List<QueryResult> queryResults = Arrays.asList(mock(QueryResult.class, RETURNS_DEEP_STUBS), mock(QueryResult.class, RETURNS_DEEP_STUBS), mock(QueryResult.class, RETURNS_DEEP_STUBS));
        MergedResult actual = resultMerger.merge(queryResults, fetchCountStatementContext, database, connectionContext);
        assertFalse(actual.next());
    }
    
    @Test
    public void assertNextForResultSetsAllEmptyWhenConfigAllDirectionType() throws SQLException {
        List<QueryResult> queryResults = Arrays.asList(mock(QueryResult.class, RETURNS_DEEP_STUBS), mock(QueryResult.class, RETURNS_DEEP_STUBS), mock(QueryResult.class, RETURNS_DEEP_STUBS));
        MergedResult actual = resultMerger.merge(queryResults, fetchAllStatementContext, database, connectionContext);
        assertFalse(actual.next());
    }
    
    @Test
    public void assertNextForResultSetsAllNotEmpty() throws SQLException {
        List<QueryResult> queryResults = Arrays.asList(mock(QueryResult.class, RETURNS_DEEP_STUBS), mock(QueryResult.class, RETURNS_DEEP_STUBS), mock(QueryResult.class, RETURNS_DEEP_STUBS));
        for (QueryResult each : queryResults) {
            when(each.next()).thenReturn(true, false);
        }
        MergedResult actual = resultMerger.merge(queryResults, fetchCountStatementContext, database, connectionContext);
        assertTrue(actual.next());
        assertFalse(actual.next());
        assertFalse(actual.next());
        assertFalse(actual.next());
    }
    
    @Test
    public void assertNextForResultSetsAllNotEmptyWhenConfigAllDirectionType() throws SQLException {
        List<QueryResult> queryResults = Arrays.asList(mock(QueryResult.class, RETURNS_DEEP_STUBS), mock(QueryResult.class, RETURNS_DEEP_STUBS), mock(QueryResult.class, RETURNS_DEEP_STUBS));
        for (QueryResult each : queryResults) {
            when(each.next()).thenReturn(true, false);
        }
        MergedResult actual = resultMerger.merge(queryResults, fetchAllStatementContext, database, connectionContext);
        assertTrue(actual.next());
        assertTrue(actual.next());
        assertTrue(actual.next());
        assertFalse(actual.next());
    }
    
    @Test
    public void assertNextForFirstResultSetsNotEmptyOnly() throws SQLException {
        List<QueryResult> queryResults = Arrays.asList(mock(QueryResult.class, RETURNS_DEEP_STUBS), mock(QueryResult.class, RETURNS_DEEP_STUBS), mock(QueryResult.class, RETURNS_DEEP_STUBS));
        when(queryResults.get(0).next()).thenReturn(true, false);
        MergedResult actual = resultMerger.merge(queryResults, fetchCountStatementContext, database, connectionContext);
        assertTrue(actual.next());
        assertFalse(actual.next());
    }
    
    @Test
    public void assertNextForFirstResultSetsNotEmptyOnlyWhenConfigAllDirectionType() throws SQLException {
        List<QueryResult> queryResults = Arrays.asList(mock(QueryResult.class, RETURNS_DEEP_STUBS), mock(QueryResult.class, RETURNS_DEEP_STUBS), mock(QueryResult.class, RETURNS_DEEP_STUBS));
        when(queryResults.get(0).next()).thenReturn(true, false);
        MergedResult actual = resultMerger.merge(queryResults, fetchAllStatementContext, database, connectionContext);
        assertTrue(actual.next());
        assertFalse(actual.next());
    }
    
    @Test
    public void assertNextForMiddleResultSetsNotEmpty() throws SQLException {
        List<QueryResult> queryResults = Arrays.asList(mock(QueryResult.class, RETURNS_DEEP_STUBS), mock(QueryResult.class, RETURNS_DEEP_STUBS), mock(QueryResult.class, RETURNS_DEEP_STUBS));
        when(queryResults.get(1).next()).thenReturn(true, false);
        MergedResult actual = resultMerger.merge(queryResults, fetchCountStatementContext, database, connectionContext);
        assertTrue(actual.next());
        assertFalse(actual.next());
    }
    
    @Test
    public void assertNextForMiddleResultSetsNotEmptyWhenConfigAllDirectionType() throws SQLException {
        List<QueryResult> queryResults = Arrays.asList(mock(QueryResult.class, RETURNS_DEEP_STUBS), mock(QueryResult.class, RETURNS_DEEP_STUBS), mock(QueryResult.class, RETURNS_DEEP_STUBS));
        when(queryResults.get(1).next()).thenReturn(true, false);
        MergedResult actual = resultMerger.merge(queryResults, fetchAllStatementContext, database, connectionContext);
        assertTrue(actual.next());
        assertFalse(actual.next());
    }
    
    @Test
    public void assertNextForLastResultSetsNotEmptyOnly() throws SQLException {
        List<QueryResult> queryResults = Arrays.asList(mock(QueryResult.class, RETURNS_DEEP_STUBS), mock(QueryResult.class, RETURNS_DEEP_STUBS), mock(QueryResult.class, RETURNS_DEEP_STUBS));
        when(queryResults.get(2).next()).thenReturn(true, false);
        MergedResult actual = resultMerger.merge(queryResults, fetchCountStatementContext, database, connectionContext);
        assertTrue(actual.next());
        assertFalse(actual.next());
    }
    
    @Test
    public void assertNextForLastResultSetsNotEmptyOnlyWhenConfigAllDirectionType() throws SQLException {
        List<QueryResult> queryResults = Arrays.asList(mock(QueryResult.class, RETURNS_DEEP_STUBS), mock(QueryResult.class, RETURNS_DEEP_STUBS), mock(QueryResult.class, RETURNS_DEEP_STUBS));
        when(queryResults.get(2).next()).thenReturn(true, false);
        MergedResult actual = resultMerger.merge(queryResults, fetchAllStatementContext, database, connectionContext);
        assertTrue(actual.next());
        assertFalse(actual.next());
    }
}
