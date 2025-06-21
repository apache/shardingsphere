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

package org.apache.shardingsphere.sharding.merge.mysql;

import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.database.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResult;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataMergedResult;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sharding.merge.dal.DialectShardingDALResultMerger;
import org.apache.shardingsphere.sharding.merge.mysql.type.MySQLShardingLogicTablesMergedResult;
import org.apache.shardingsphere.sharding.merge.mysql.type.MySQLShardingShowCreateTableMergedResult;
import org.apache.shardingsphere.sharding.merge.mysql.type.MySQLShardingShowIndexMergedResult;
import org.apache.shardingsphere.sharding.merge.mysql.type.MySQLShardingShowTableStatusMergedResult;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dal.DALStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dal.ShowCreateTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dal.ShowDatabasesStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.show.index.MySQLShowIndexStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dal.ShowTableStatusStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dal.ShowTablesStatement;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MySQLShardingDALResultMergerTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "MySQL");
    
    private final DialectShardingDALResultMerger resultMerger = DatabaseTypedSPILoader.getService(DialectShardingDALResultMerger.class, databaseType);
    
    private final List<QueryResult> queryResults = Collections.singletonList(mock());
    
    @Test
    void assertMergeForShowDatabasesStatement() throws SQLException {
        SQLStatementContext sqlStatementContext = mockSQLStatementContext(mock(ShowDatabasesStatement.class));
        Optional<MergedResult> actual = resultMerger.merge("foo_db", mock(), sqlStatementContext, mock(), queryResults);
        assertTrue(actual.isPresent());
        assertThat(actual.get(), instanceOf(LocalDataMergedResult.class));
    }
    
    @Test
    void assertMergeForShowShowTablesStatement() throws SQLException {
        SQLStatementContext sqlStatementContext = mockSQLStatementContext(mock(ShowTablesStatement.class));
        Optional<MergedResult> actual = resultMerger.merge("foo_db", mock(), sqlStatementContext, mock(), queryResults);
        assertTrue(actual.isPresent());
        assertThat(actual.get(), instanceOf(MySQLShardingLogicTablesMergedResult.class));
    }
    
    @Test
    void assertMergeForShowTableStatusStatement() throws SQLException {
        SQLStatementContext sqlStatementContext = mockSQLStatementContext(mock(ShowTableStatusStatement.class));
        Optional<MergedResult> actual = resultMerger.merge("foo_db", mock(), sqlStatementContext, mock(), queryResults);
        assertTrue(actual.isPresent());
        assertThat(actual.get(), instanceOf(MySQLShardingShowTableStatusMergedResult.class));
    }
    
    @Test
    void assertMergeForShowIndexStatement() throws SQLException {
        SQLStatementContext sqlStatementContext = mockSQLStatementContext(mock(MySQLShowIndexStatement.class));
        Optional<MergedResult> actual = resultMerger.merge("foo_db", mock(), sqlStatementContext, mock(), queryResults);
        assertTrue(actual.isPresent());
        assertThat(actual.get(), instanceOf(MySQLShardingShowIndexMergedResult.class));
    }
    
    @Test
    void assertMergeForShowCreateTableStatement() throws SQLException {
        SQLStatementContext sqlStatementContext = mockSQLStatementContext(mock(ShowCreateTableStatement.class));
        Optional<MergedResult> actual = resultMerger.merge("foo_db", mock(), sqlStatementContext, mock(), queryResults);
        assertTrue(actual.isPresent());
        assertThat(actual.get(), instanceOf(MySQLShardingShowCreateTableMergedResult.class));
    }
    
    private SQLStatementContext mockSQLStatementContext(final DALStatement dalStatement) {
        SQLStatementContext result = mock(SQLStatementContext.class, RETURNS_DEEP_STUBS);
        when(result.getSqlStatement()).thenReturn(dalStatement);
        when(result.getTablesContext().getSchemaName()).thenReturn(Optional.empty());
        when(result.getDatabaseType()).thenReturn(databaseType);
        return result;
    }
}
