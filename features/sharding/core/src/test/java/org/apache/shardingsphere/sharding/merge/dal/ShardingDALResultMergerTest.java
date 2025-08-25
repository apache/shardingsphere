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

package org.apache.shardingsphere.sharding.merge.dal;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResult;
import org.apache.shardingsphere.infra.merge.result.impl.transparent.TransparentMergedResult;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dal.DALStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dal.ExplainStatement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShardingDALResultMergerTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    private final ShardingDALResultMerger resultMerger = new ShardingDALResultMerger("foo_db", mock());
    
    private final List<QueryResult> queryResults = Collections.singletonList(mock());
    
    @Test
    void assertMergeForExplainStatement() throws SQLException {
        SQLStatementContext sqlStatementContext = mockSQLStatementContext(mock(ExplainStatement.class));
        assertThat(resultMerger.merge(queryResults, sqlStatementContext, mock(), mock()), isA(TransparentMergedResult.class));
    }
    
    @Test
    void assertMergeWithNotTableAvailable() throws SQLException {
        SQLStatementContext sqlStatementContext = mock(SQLStatementContext.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.getTablesContext().getDatabaseNames()).thenReturn(Collections.emptyList());
        when(sqlStatementContext.getSqlStatement().getDatabaseType()).thenReturn(databaseType);
        assertThat(resultMerger.merge(queryResults, sqlStatementContext, mock(), mock()), isA(TransparentMergedResult.class));
    }
    
    private SQLStatementContext mockSQLStatementContext(final DALStatement dalStatement) {
        SQLStatementContext result = mock(SQLStatementContext.class, RETURNS_DEEP_STUBS);
        when(result.getSqlStatement()).thenReturn(dalStatement);
        when(result.getTablesContext().getSchemaName()).thenReturn(Optional.empty());
        when(result.getSqlStatement().getDatabaseType()).thenReturn(databaseType);
        return result;
    }
}
