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

import org.apache.shardingsphere.infra.binder.segment.table.TablesContext;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.apache.shardingsphere.infra.database.core.DefaultDatabase;
import org.apache.shardingsphere.infra.database.mysql.MySQLDatabaseType;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResult;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataMergedResult;
import org.apache.shardingsphere.infra.merge.result.impl.transparent.TransparentMergedResult;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.sharding.merge.dal.show.LogicTablesMergedResult;
import org.apache.shardingsphere.sharding.merge.dal.show.ShowCreateTableMergedResult;
import org.apache.shardingsphere.sharding.merge.dal.show.ShowIndexMergedResult;
import org.apache.shardingsphere.sharding.merge.dal.show.ShowTableStatusMergedResult;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dal.DALStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLExplainStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowCreateTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowDatabasesStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowOtherStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowTableStatusStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowTablesStatement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShardingDALResultMergerTest {
    
    private final List<QueryResult> queryResults = new LinkedList<>();
    
    @BeforeEach
    void setUp() {
        queryResults.add(mock(QueryResult.class));
    }
    
    @Test
    void assertMergeForShowDatabasesStatement() throws SQLException {
        DALStatement dalStatement = new MySQLShowDatabasesStatement();
        SQLStatementContext sqlStatementContext = mockSQLStatementContext(dalStatement);
        ShardingDALResultMerger resultMerger = new ShardingDALResultMerger(DefaultDatabase.LOGIC_NAME, null);
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getName()).thenReturn(DefaultDatabase.LOGIC_NAME);
        assertThat(resultMerger.merge(queryResults, sqlStatementContext, database, mock(ConnectionContext.class)), instanceOf(LocalDataMergedResult.class));
    }
    
    @Test
    void assertMergeForShowShowTablesStatement() throws SQLException {
        DALStatement dalStatement = new MySQLShowTablesStatement();
        SQLStatementContext sqlStatementContext = mockSQLStatementContext(dalStatement);
        ShardingDALResultMerger resultMerger = new ShardingDALResultMerger(DefaultDatabase.LOGIC_NAME, null);
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getName()).thenReturn(DefaultDatabase.LOGIC_NAME);
        assertThat(resultMerger.merge(queryResults, sqlStatementContext, database, mock(ConnectionContext.class)), instanceOf(LogicTablesMergedResult.class));
    }
    
    @Test
    void assertMergeForShowCreateTableStatement() throws SQLException {
        DALStatement dalStatement = new MySQLShowCreateTableStatement();
        SQLStatementContext sqlStatementContext = mockSQLStatementContext(dalStatement);
        ShardingDALResultMerger resultMerger = new ShardingDALResultMerger(DefaultDatabase.LOGIC_NAME, null);
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getName()).thenReturn(DefaultDatabase.LOGIC_NAME);
        assertThat(resultMerger.merge(queryResults, sqlStatementContext, database, mock(ConnectionContext.class)), instanceOf(ShowCreateTableMergedResult.class));
    }
    
    @Test
    void assertMergeForShowOtherStatement() throws SQLException {
        DALStatement dalStatement = new MySQLShowOtherStatement();
        SQLStatementContext sqlStatementContext = mockSQLStatementContext(dalStatement);
        ShardingDALResultMerger resultMerger = new ShardingDALResultMerger(DefaultDatabase.LOGIC_NAME, null);
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getName()).thenReturn(DefaultDatabase.LOGIC_NAME);
        assertThat(resultMerger.merge(queryResults, sqlStatementContext, database, mock(ConnectionContext.class)), instanceOf(TransparentMergedResult.class));
    }
    
    @Test
    void assertMergeForDescribeStatement() throws SQLException {
        DALStatement dalStatement = new MySQLExplainStatement();
        SQLStatementContext sqlStatementContext = mockSQLStatementContext(dalStatement);
        ShardingDALResultMerger resultMerger = new ShardingDALResultMerger(DefaultDatabase.LOGIC_NAME, mock(ShardingRule.class));
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getName()).thenReturn(DefaultDatabase.LOGIC_NAME);
        assertThat(resultMerger.merge(queryResults, sqlStatementContext, database, mock(ConnectionContext.class)), instanceOf(TransparentMergedResult.class));
    }
    
    @Test
    void assertMergeForShowIndexStatement() throws SQLException {
        DALStatement dalStatement = new MySQLShowIndexStatement();
        SQLStatementContext sqlStatementContext = mockSQLStatementContext(dalStatement);
        ShardingDALResultMerger resultMerger = new ShardingDALResultMerger(DefaultDatabase.LOGIC_NAME, mock(ShardingRule.class));
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getName()).thenReturn(DefaultDatabase.LOGIC_NAME);
        assertThat(resultMerger.merge(queryResults, sqlStatementContext, database, mock(ConnectionContext.class)), instanceOf(ShowIndexMergedResult.class));
    }
    
    @Test
    void assertMergeForShowTableStatusStatement() throws SQLException {
        DALStatement dalStatement = new MySQLShowTableStatusStatement();
        SQLStatementContext sqlStatementContext = mockSQLStatementContext(dalStatement);
        ShardingDALResultMerger resultMerger = new ShardingDALResultMerger(DefaultDatabase.LOGIC_NAME, mock(ShardingRule.class));
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getName()).thenReturn(DefaultDatabase.LOGIC_NAME);
        assertThat(resultMerger.merge(queryResults, sqlStatementContext, database, mock(ConnectionContext.class)), instanceOf(ShowTableStatusMergedResult.class));
    }
    
    private SQLStatementContext mockSQLStatementContext(final DALStatement dalStatement) {
        SQLStatementContext result = mock(SQLStatementContext.class);
        when(result.getSqlStatement()).thenReturn(dalStatement);
        TablesContext tablesContext = mock(TablesContext.class);
        when(result.getTablesContext()).thenReturn(tablesContext);
        when(tablesContext.getSchemaName()).thenReturn(Optional.empty());
        when(result.getDatabaseType()).thenReturn(new MySQLDatabaseType());
        return result;
    }
}
