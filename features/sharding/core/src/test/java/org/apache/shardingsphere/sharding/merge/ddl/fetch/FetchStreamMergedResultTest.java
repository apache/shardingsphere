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
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.core.DefaultDatabase;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResult;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ShardingSphereResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.apache.shardingsphere.infra.session.connection.cursor.CursorConnectionContext;
import org.apache.shardingsphere.sharding.merge.ddl.ShardingDDLResultMerger;
import org.apache.shardingsphere.sql.parser.sql.common.enums.DirectionType;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.cursor.CursorNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.cursor.DirectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.FetchStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussCursorStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussFetchStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.dml.OpenGaussSelectStatement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FetchStreamMergedResultTest {
    
    private FetchStatementContext fetchCountStatementContext;
    
    private FetchStatementContext fetchAllStatementContext;
    
    private ShardingDDLResultMerger resultMerger;
    
    private ShardingSphereDatabase database;
    
    private ConnectionContext connectionContext;
    
    @BeforeEach
    void setUp() {
        fetchCountStatementContext = createFetchStatementContext(false);
        fetchAllStatementContext = createFetchStatementContext(true);
        resultMerger = new ShardingDDLResultMerger();
        database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getName()).thenReturn(DefaultDatabase.LOGIC_NAME);
        connectionContext = mock(ConnectionContext.class);
        when(connectionContext.getCursorContext()).thenReturn(new CursorConnectionContext());
    }
    
    private static FetchStatementContext createFetchStatementContext(final boolean containsAllDirectionType) {
        FetchStatementContext result = new FetchStatementContext(createFetchStatement(containsAllDirectionType));
        result.setUpCursorDefinition(createCursorStatementContext());
        return result;
    }
    
    private static FetchStatement createFetchStatement(final boolean containsAllDirectionType) {
        FetchStatement result = new OpenGaussFetchStatement();
        result.setCursorName(new CursorNameSegment(0, 0, new IdentifierValue("t_order_cursor")));
        if (containsAllDirectionType) {
            DirectionSegment direction = new DirectionSegment(0, 0);
            direction.setDirectionType(DirectionType.ALL);
            result.setDirection(direction);
        }
        return result;
    }
    
    private static CursorStatementContext createCursorStatementContext() {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getName()).thenReturn(DefaultDatabase.LOGIC_NAME);
        OpenGaussCursorStatement cursorStatement = new OpenGaussCursorStatement();
        cursorStatement.setSelect(createSelectStatement());
        return new CursorStatementContext(createShardingSphereMetaData(database), Collections.emptyList(), cursorStatement, DefaultDatabase.LOGIC_NAME);
    }
    
    private static ShardingSphereMetaData createShardingSphereMetaData(final ShardingSphereDatabase database) {
        return new ShardingSphereMetaData(Collections.singletonMap(DefaultDatabase.LOGIC_NAME, database), mock(ShardingSphereResourceMetaData.class),
                mock(ShardingSphereRuleMetaData.class), mock(ConfigurationProperties.class));
    }
    
    private static SelectStatement createSelectStatement() {
        SelectStatement result = new OpenGaussSelectStatement();
        result.setProjections(new ProjectionsSegment(0, 0));
        result.setFrom(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order"))));
        return result;
    }
    
    @Test
    void assertNextForResultSetsAllEmpty() throws SQLException {
        List<QueryResult> queryResults = Arrays.asList(mock(QueryResult.class, RETURNS_DEEP_STUBS), mock(QueryResult.class, RETURNS_DEEP_STUBS), mock(QueryResult.class, RETURNS_DEEP_STUBS));
        MergedResult actual = resultMerger.merge(queryResults, fetchCountStatementContext, database, connectionContext);
        assertFalse(actual.next());
    }
    
    @Test
    void assertNextForResultSetsAllEmptyWhenConfigAllDirectionType() throws SQLException {
        List<QueryResult> queryResults = Arrays.asList(mock(QueryResult.class, RETURNS_DEEP_STUBS), mock(QueryResult.class, RETURNS_DEEP_STUBS), mock(QueryResult.class, RETURNS_DEEP_STUBS));
        MergedResult actual = resultMerger.merge(queryResults, fetchAllStatementContext, database, connectionContext);
        assertFalse(actual.next());
    }
    
    @Test
    void assertNextForResultSetsAllNotEmpty() throws SQLException {
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
    void assertNextForResultSetsAllNotEmptyWhenConfigAllDirectionType() throws SQLException {
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
    
    @ParameterizedTest(name = "{0}")
    @ArgumentsSource(TestCaseArgumentsProvider.class)
    void assertNextForNotEmpty(final String name, final int index, final FetchStatementContext fetchStatementContext) throws SQLException {
        List<QueryResult> queryResults = Arrays.asList(mock(QueryResult.class, RETURNS_DEEP_STUBS), mock(QueryResult.class, RETURNS_DEEP_STUBS), mock(QueryResult.class, RETURNS_DEEP_STUBS));
        when(queryResults.get(index).next()).thenReturn(true, false);
        MergedResult actual = resultMerger.merge(queryResults, fetchStatementContext, database, connectionContext);
        assertTrue(actual.next());
        assertFalse(actual.next());
    }
    
    private static class TestCaseArgumentsProvider implements ArgumentsProvider {
        
        @Override
        public Stream<? extends Arguments> provideArguments(final ExtensionContext extensionContext) {
            return Stream.of(
                    Arguments.of("first", 0, createFetchStatementContext(false)),
                    Arguments.of("middle", 1, createFetchStatementContext(false)),
                    Arguments.of("last", 2, createFetchStatementContext(false)),
                    Arguments.of("firstWithAllDirection", 0, createFetchStatementContext(true)),
                    Arguments.of("middleWithAllDirection", 1, createFetchStatementContext(true)),
                    Arguments.of("lastWithAllDirection", 2, createFetchStatementContext(true)));
        }
    }
}
