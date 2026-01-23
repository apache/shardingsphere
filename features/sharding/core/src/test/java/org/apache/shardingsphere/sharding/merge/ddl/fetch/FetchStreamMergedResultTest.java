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

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.binder.context.statement.type.ddl.CursorHeldSQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.ddl.CursorStatementContext;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResult;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.apache.shardingsphere.infra.session.connection.cursor.CursorConnectionContext;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sharding.merge.ddl.ShardingDDLResultMerger;
import org.apache.shardingsphere.sql.parser.statement.core.enums.DirectionType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.cursor.CursorNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.cursor.DirectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.CursorStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.FetchStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.support.ParameterDeclarations;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class FetchStreamMergedResultTest {
    
    private static final DatabaseType DATABASE_TYPE = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    private CursorHeldSQLStatementContext fetchCountStatementContext;
    
    private CursorHeldSQLStatementContext fetchAllStatementContext;
    
    private ShardingDDLResultMerger resultMerger;
    
    @Mock
    private ShardingSphereDatabase database;
    
    private ConnectionContext connectionContext;
    
    @BeforeEach
    void setUp() {
        fetchCountStatementContext = createCursorHeldSQLStatementContext(false);
        fetchAllStatementContext = createCursorHeldSQLStatementContext(true);
        resultMerger = new ShardingDDLResultMerger();
        connectionContext = mock(ConnectionContext.class);
        when(connectionContext.getCursorContext()).thenReturn(new CursorConnectionContext());
    }
    
    private static CursorHeldSQLStatementContext createCursorHeldSQLStatementContext(final boolean containsAllDirectionType) {
        CursorHeldSQLStatementContext result = new CursorHeldSQLStatementContext(createFetchStatement(containsAllDirectionType));
        result.setCursorStatementContext(mockCursorStatementContext());
        return result;
    }
    
    private static FetchStatement createFetchStatement(final boolean containsAllDirectionType) {
        return new FetchStatement(DATABASE_TYPE, new CursorNameSegment(0, 0, new IdentifierValue("foo_cursor")), containsAllDirectionType ? new DirectionSegment(0, 0, DirectionType.ALL) : null);
    }
    
    private static CursorStatementContext mockCursorStatementContext() {
        SelectStatement selectStatement = mockSelectStatement();
        CursorStatement cursorStatement = new CursorStatement(DATABASE_TYPE, null, selectStatement);
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getName()).thenReturn("foo_db");
        when(database.getProtocolType()).thenReturn(DATABASE_TYPE);
        return new CursorStatementContext(new ShardingSphereMetaData(Collections.singleton(database), mock(), mock(), mock()), cursorStatement, "foo_db");
    }
    
    private static SelectStatement mockSelectStatement() {
        SelectStatement result = mock(SelectStatement.class);
        when(result.getDatabaseType()).thenReturn(DATABASE_TYPE);
        when(result.getProjections()).thenReturn(new ProjectionsSegment(0, 0));
        when(result.getFrom()).thenReturn(Optional.of(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("foo_tbl")))));
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
    void assertNextForNotEmpty(final String name, final int index, final CursorHeldSQLStatementContext sqlStatementContext) throws SQLException {
        List<QueryResult> queryResults = Arrays.asList(mock(QueryResult.class, RETURNS_DEEP_STUBS), mock(QueryResult.class, RETURNS_DEEP_STUBS), mock(QueryResult.class, RETURNS_DEEP_STUBS));
        when(queryResults.get(index).next()).thenReturn(true, false);
        MergedResult actual = resultMerger.merge(queryResults, sqlStatementContext, database, connectionContext);
        assertTrue(actual.next());
        assertFalse(actual.next());
    }
    
    private static final class TestCaseArgumentsProvider implements ArgumentsProvider {
        
        @Override
        public Stream<? extends Arguments> provideArguments(final ParameterDeclarations parameters, final ExtensionContext context) {
            return Stream.of(
                    Arguments.of("first", 0, createCursorHeldSQLStatementContext(false)),
                    Arguments.of("middle", 1, createCursorHeldSQLStatementContext(false)),
                    Arguments.of("last", 2, createCursorHeldSQLStatementContext(false)),
                    Arguments.of("firstWithAllDirection", 0, createCursorHeldSQLStatementContext(true)),
                    Arguments.of("middleWithAllDirection", 1, createCursorHeldSQLStatementContext(true)),
                    Arguments.of("lastWithAllDirection", 2, createCursorHeldSQLStatementContext(true)));
        }
    }
}
