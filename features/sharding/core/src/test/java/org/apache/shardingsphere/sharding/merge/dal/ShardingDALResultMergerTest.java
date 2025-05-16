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

import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.type.TableAvailable;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResult;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataMergedResult;
import org.apache.shardingsphere.infra.merge.result.impl.transparent.TransparentMergedResult;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sharding.merge.dal.show.LogicTablesMergedResult;
import org.apache.shardingsphere.sharding.merge.dal.show.ShowCreateTableMergedResult;
import org.apache.shardingsphere.sharding.merge.dal.show.ShowIndexMergedResult;
import org.apache.shardingsphere.sharding.merge.dal.show.ShowTableStatusMergedResult;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.DALStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.ExplainStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.ShowCreateTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.ShowDatabasesStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.ShowIndexStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.ShowTableStatusStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.ShowTablesStatement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

@ExtendWith(MockitoExtension.class)
class ShardingDALResultMergerTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    private final ShardingDALResultMerger resultMerger = new ShardingDALResultMerger("foo_db", mock());
    
    private final List<QueryResult> queryResults = Collections.singletonList(mock());
    
    @Test
    void assertMergeForShowDatabasesStatement() throws SQLException {
        SQLStatementContext sqlStatementContext = mockSQLStatementContext(mock(ShowDatabasesStatement.class));
        assertThat(resultMerger.merge(queryResults, sqlStatementContext, mock(), mock()), instanceOf(LocalDataMergedResult.class));
    }
    
    @Test
    void assertMergeForShowShowTablesStatement() throws SQLException {
        SQLStatementContext sqlStatementContext = mockSQLStatementContext(mock(ShowTablesStatement.class));
        assertThat(resultMerger.merge(queryResults, sqlStatementContext, mock(), mock()), instanceOf(LogicTablesMergedResult.class));
    }
    
    @Test
    void assertMergeForShowTableStatusStatement() throws SQLException {
        SQLStatementContext sqlStatementContext = mockSQLStatementContext(mock(ShowTableStatusStatement.class));
        assertThat(resultMerger.merge(queryResults, sqlStatementContext, mock(), mock()), instanceOf(ShowTableStatusMergedResult.class));
    }
    
    @Test
    void assertMergeForShowIndexStatement() throws SQLException {
        SQLStatementContext sqlStatementContext = mockSQLStatementContext(mock(ShowIndexStatement.class));
        assertThat(resultMerger.merge(queryResults, sqlStatementContext, mock(), mock()), instanceOf(ShowIndexMergedResult.class));
    }
    
    @Test
    void assertMergeForShowCreateTableStatement() throws SQLException {
        SQLStatementContext sqlStatementContext = mockSQLStatementContext(mock(ShowCreateTableStatement.class));
        assertThat(resultMerger.merge(queryResults, sqlStatementContext, mock(), mock()), instanceOf(ShowCreateTableMergedResult.class));
    }
    
    @Test
    void assertMergeForExplainStatement() throws SQLException {
        SQLStatementContext sqlStatementContext = mockSQLStatementContext(mock(ExplainStatement.class));
        assertThat(resultMerger.merge(queryResults, sqlStatementContext, mock(), mock()), instanceOf(TransparentMergedResult.class));
    }
    
    @Test
    void assertMergeWithNotTableAvailable() throws SQLException {
        SQLStatementContext sqlStatementContext = mock(SQLStatementContext.class);
        when(sqlStatementContext.getDatabaseType()).thenReturn(databaseType);
        assertThat(resultMerger.merge(queryResults, sqlStatementContext, mock(), mock()), instanceOf(TransparentMergedResult.class));
    }
    
    private SQLStatementContext mockSQLStatementContext(final DALStatement dalStatement) {
        SQLStatementContext result = mock(SQLStatementContext.class, withSettings().extraInterfaces(TableAvailable.class).defaultAnswer(RETURNS_DEEP_STUBS));
        when(result.getSqlStatement()).thenReturn(dalStatement);
        when(((TableAvailable) result).getTablesContext().getSchemaName()).thenReturn(Optional.empty());
        when(result.getDatabaseType()).thenReturn(databaseType);
        return result;
    }
}
