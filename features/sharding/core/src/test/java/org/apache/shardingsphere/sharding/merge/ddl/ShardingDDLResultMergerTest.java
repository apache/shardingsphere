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

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.binder.context.statement.type.ddl.CursorHeldSQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.ddl.CursorStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResult;
import org.apache.shardingsphere.infra.merge.result.impl.stream.IteratorStreamMergedResult;
import org.apache.shardingsphere.infra.merge.result.impl.transparent.TransparentMergedResult;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.apache.shardingsphere.infra.session.connection.cursor.CursorConnectionContext;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sharding.merge.ddl.fetch.FetchStreamMergedResult;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.cursor.CursorNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.FetchStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShardingDDLResultMergerTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    private final ShardingDDLResultMerger merger = new ShardingDDLResultMerger();
    
    @Test
    void assertMergeWithIteratorStreamMergedResult() throws SQLException {
        CursorHeldSQLStatementContext sqlStatement = mock(CursorHeldSQLStatementContext.class);
        when(sqlStatement.getSqlStatement()).thenReturn(mock(FetchStatement.class));
        assertThat(merger.merge(Collections.singletonList(createQueryResult()), sqlStatement, mock(), mock()), isA(IteratorStreamMergedResult.class));
    }
    
    @Test
    void assertMergeWithTransparentMergedResult() throws SQLException {
        assertThat(merger.merge(createQueryResults(), mock(SelectStatementContext.class), mock(), mock()), isA(TransparentMergedResult.class));
    }
    
    @Test
    void assertMergeWithFetchStreamMergedResult() throws SQLException {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getName()).thenReturn("foo_db");
        ConnectionContext connectionContext = mock(ConnectionContext.class);
        when(connectionContext.getCursorContext()).thenReturn(new CursorConnectionContext());
        assertThat(merger.merge(createQueryResults(), createCursorHeldSQLStatementContext(database), mock(), connectionContext), isA(FetchStreamMergedResult.class));
    }
    
    private CursorHeldSQLStatementContext createCursorHeldSQLStatementContext(final ShardingSphereDatabase database) {
        CursorHeldSQLStatementContext result = new CursorHeldSQLStatementContext(new FetchStatement(databaseType, new CursorNameSegment(0, 0, new IdentifierValue("foo_cursor")), null));
        result.setCursorStatementContext(createCursorStatementContext(database));
        return result;
    }
    
    private CursorStatementContext createCursorStatementContext(final ShardingSphereDatabase database) {
        CursorStatementContext result = mock(CursorStatementContext.class, RETURNS_DEEP_STUBS);
        SelectStatement selectStatement = createSelectStatement();
        SelectStatementContext selectStatementContext = new SelectStatementContext(
                selectStatement, new ShardingSphereMetaData(Collections.singleton(database), mock(), mock(), mock()), "foo_db", Collections.emptyList());
        when(result.getSelectStatementContext()).thenReturn(selectStatementContext);
        when(result.getSqlStatement().getSelect()).thenReturn(selectStatement);
        return result;
    }
    
    private SelectStatement createSelectStatement() {
        SelectStatement result = new SelectStatement(databaseType);
        result.setFrom(new SimpleTableSegment(new TableNameSegment(10, 13, new IdentifierValue("tbl"))));
        result.setProjections(new ProjectionsSegment(0, 0));
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
}
