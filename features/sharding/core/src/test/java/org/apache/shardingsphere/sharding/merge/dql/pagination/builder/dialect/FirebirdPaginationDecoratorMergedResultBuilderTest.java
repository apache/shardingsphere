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

package org.apache.shardingsphere.sharding.merge.dql.pagination.builder.dialect;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.binder.context.segment.select.pagination.PaginationContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResult;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.merge.result.impl.decorator.DecoratorMergedResult;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sharding.merge.dql.ShardingDQLResultMerger;
import org.apache.shardingsphere.sharding.merge.dql.pagination.LimitDecoratorMergedResult;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.pagination.limit.LimitSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.pagination.limit.NumberLiteralLimitValueSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.Reader;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FirebirdPaginationDecoratorMergedResultBuilderTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "Firebird");
    
    @Test
    void assertBuildWithLimitValueSegment() throws SQLException {
        FirebirdPaginationDecoratorMergedResultBuilder builder = new FirebirdPaginationDecoratorMergedResultBuilder();
        PaginationContext paginationContext = new PaginationContext(
                new NumberLiteralLimitValueSegment(0, 0, 10L), new NumberLiteralLimitValueSegment(0, 0, 10L), Collections.emptyList());
        DecoratorMergedResult actual = builder.build(new StubMergedResult(), paginationContext);
        assertThat(actual, isA(LimitDecoratorMergedResult.class));
    }
    
    @Test
    void assertDecorateFinalResultsAcrossShards() throws SQLException {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getName()).thenReturn("foo_db");
        SelectStatement selectStatement = SelectStatement.builder()
                .databaseType(databaseType)
                .projections(new ProjectionsSegment(0, 0))
                .limit(new LimitSegment(0, 0, new NumberLiteralLimitValueSegment(0, 0, 2L), new NumberLiteralLimitValueSegment(0, 0, 2L)))
                .build();
        SelectStatementContext selectStatementContext = new SelectStatementContext(
                selectStatement, new ShardingSphereMetaData(Collections.singleton(database), mock(), mock(), mock()), "foo_db", Collections.emptyList());
        ShardingDQLResultMerger resultMerger = new ShardingDQLResultMerger(databaseType);
        MergedResult actual = resultMerger.merge(
                Arrays.asList(mockQueryResult(), mockQueryResult(), mockQueryResult(), mockQueryResult()), selectStatementContext, database, mock(ConnectionContext.class));
        assertTrue(actual.next());
        assertTrue(actual.next());
        assertFalse(actual.next());
    }
    
    private QueryResult mockQueryResult() throws SQLException {
        QueryResult result = mock(QueryResult.class, RETURNS_DEEP_STUBS);
        when(result.next()).thenReturn(true, true, false);
        return result;
    }
    
    private static final class StubMergedResult implements MergedResult {
        
        @Override
        public boolean next() {
            return false;
        }
        
        @Override
        public Object getValue(final int columnIndex, final Class<?> type) {
            return null;
        }
        
        @Override
        public InputStream getInputStream(final int columnIndex, final String type) {
            return null;
        }
        
        @Override
        public Reader getCharacterStream(final int columnIndex) {
            return null;
        }
        
        @Override
        public Object getCalendarValue(final int columnIndex, final Class<?> type, final Calendar calendar) {
            return null;
        }
        
        @Override
        public boolean wasNull() {
            return false;
        }
    }
}
