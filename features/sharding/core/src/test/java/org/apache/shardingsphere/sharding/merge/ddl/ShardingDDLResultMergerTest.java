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

package org.apache.shardingsphere.sharding.merge.ddl;

import org.apache.shardingsphere.infra.binder.context.statement.ddl.CursorStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.ddl.FetchStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResult;
import org.apache.shardingsphere.infra.merge.result.impl.transparent.TransparentMergedResult;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.apache.shardingsphere.infra.session.connection.cursor.CursorConnectionContext;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sharding.merge.common.IteratorStreamMergedResult;
import org.apache.shardingsphere.sharding.merge.ddl.fetch.FetchStreamMergedResult;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.cursor.CursorNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.ddl.FetchStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShardingDDLResultMergerTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    private final ShardingDDLResultMerger merger = new ShardingDDLResultMerger();
    
    @Test
    void assertMergeWithIteratorStreamMergedResult() throws SQLException {
        assertThat(merger.merge(Collections.singletonList(createQueryResult()), mock(FetchStatementContext.class), mock(), mock()), instanceOf(IteratorStreamMergedResult.class));
    }
    
    @Test
    void assertMergeWithTransparentMergedResult() throws SQLException {
        assertThat(merger.merge(createQueryResults(), mock(SelectStatementContext.class), mock(), mock()), instanceOf(TransparentMergedResult.class));
    }
    
    @Test
    void assertMergeWithFetchStreamMergedResult() throws SQLException {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getName()).thenReturn("foo_db");
        ConnectionContext connectionContext = mock(ConnectionContext.class);
        when(connectionContext.getCursorContext()).thenReturn(new CursorConnectionContext());
        assertThat(merger.merge(createQueryResults(), createFetchStatementContext(database), mock(), connectionContext), instanceOf(FetchStreamMergedResult.class));
    }
    
    private FetchStatementContext createFetchStatementContext(final ShardingSphereDatabase database) {
        FetchStatementContext result = new FetchStatementContext(mockFetchStatement(), "foo_db");
        result.setCursorStatementContext(createCursorStatementContext(database));
        return result;
    }
    
    private CursorStatementContext createCursorStatementContext(final ShardingSphereDatabase database) {
        CursorStatementContext result = mock(CursorStatementContext.class, RETURNS_DEEP_STUBS);
        SelectStatement selectStatement = createSelectStatement();
        selectStatement.setProjections(new ProjectionsSegment(0, 0));
        SelectStatementContext selectStatementContext = new SelectStatementContext(
                new ShardingSphereMetaData(Collections.singleton(database), mock(), mock(), mock()), Collections.emptyList(), selectStatement, "foo_db", Collections.emptyList());
        when(result.getSelectStatementContext()).thenReturn(selectStatementContext);
        when(result.getSqlStatement().getSelect()).thenReturn(selectStatement);
        return result;
    }
    
    private SelectStatement createSelectStatement() {
        SelectStatement result = mock(SelectStatement.class, RETURNS_DEEP_STUBS);
        when(result.getFrom()).thenReturn(Optional.of(new SimpleTableSegment(new TableNameSegment(10, 13, new IdentifierValue("tbl")))));
        when(result.getProjections()).thenReturn(new ProjectionsSegment(0, 0));
        when(result.getDatabaseType()).thenReturn(databaseType);
        return result;
    }
    
    private List<QueryResult> createQueryResults() throws SQLException {
        List<QueryResult> result = new LinkedList<>();
        result.add(createQueryResult());
        result.add(mock(QueryResult.class, RETURNS_DEEP_STUBS));
        result.add(mock(QueryResult.class, RETURNS_DEEP_STUBS));
        result.add(mock(QueryResult.class, RETURNS_DEEP_STUBS));
        return result;
    }
    
    private QueryResult createQueryResult() throws SQLException {
        QueryResult result = mock(QueryResult.class, RETURNS_DEEP_STUBS);
        when(result.getMetaData().getColumnCount()).thenReturn(1);
        when(result.getMetaData().getColumnLabel(1)).thenReturn("count(*)");
        when(result.getValue(1, Object.class)).thenReturn(0);
        return result;
    }
    
    private FetchStatement mockFetchStatement() {
        FetchStatement result = mock(FetchStatement.class);
        when(result.getCursorName()).thenReturn(new CursorNameSegment(0, 0, new IdentifierValue("foo_cursor")));
        when(result.getDirection()).thenReturn(Optional.empty());
        when(result.getDatabaseType()).thenReturn(databaseType);
        return result;
    }
}
