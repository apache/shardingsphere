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
import org.apache.shardingsphere.infra.database.DefaultDatabase;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResult;
import org.apache.shardingsphere.infra.merge.result.impl.transparent.TransparentMergedResult;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.sharding.merge.dal.common.SingleLocalDataMergedResult;
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
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ShardingDALResultMergerTest {
    
    private final List<QueryResult> queryResults = new LinkedList<>();
    
    @Before
    public void setUp() {
        queryResults.add(mock(QueryResult.class));
    }
    
    @Test
    public void assertMergeForShowDatabasesStatement() throws SQLException {
        DALStatement dalStatement = new MySQLShowDatabasesStatement();
        SQLStatementContext<?> sqlStatementContext = mockSQLStatementContext(dalStatement);
        ShardingDALResultMerger resultMerger = new ShardingDALResultMerger(DefaultDatabase.LOGIC_NAME, null);
        ShardingSphereMetaData metaData = mock(ShardingSphereMetaData.class);
        when(metaData.getDatabaseName()).thenReturn(DefaultDatabase.LOGIC_NAME);
        assertThat(resultMerger.merge(queryResults, sqlStatementContext, metaData), instanceOf(SingleLocalDataMergedResult.class));
    }
    
    @Test
    public void assertMergeForShowShowTablesStatement() throws SQLException {
        DALStatement dalStatement = new MySQLShowTablesStatement();
        SQLStatementContext<?> sqlStatementContext = mockSQLStatementContext(dalStatement);
        ShardingDALResultMerger resultMerger = new ShardingDALResultMerger(DefaultDatabase.LOGIC_NAME, null);
        ShardingSphereMetaData metaData = mock(ShardingSphereMetaData.class);
        when(metaData.getDatabaseName()).thenReturn(DefaultDatabase.LOGIC_NAME);
        assertThat(resultMerger.merge(queryResults, sqlStatementContext, metaData), instanceOf(LogicTablesMergedResult.class));
    }
    
    @Test
    public void assertMergeForShowCreateTableStatement() throws SQLException {
        DALStatement dalStatement = new MySQLShowCreateTableStatement();
        SQLStatementContext<?> sqlStatementContext = mockSQLStatementContext(dalStatement);
        ShardingDALResultMerger resultMerger = new ShardingDALResultMerger(DefaultDatabase.LOGIC_NAME, null);
        ShardingSphereMetaData metaData = mock(ShardingSphereMetaData.class);
        when(metaData.getDatabaseName()).thenReturn(DefaultDatabase.LOGIC_NAME);
        assertThat(resultMerger.merge(queryResults, sqlStatementContext, metaData), instanceOf(ShowCreateTableMergedResult.class));
    }
    
    @Test
    public void assertMergeForShowOtherStatement() throws SQLException {
        DALStatement dalStatement = new MySQLShowOtherStatement();
        SQLStatementContext<?> sqlStatementContext = mockSQLStatementContext(dalStatement);
        ShardingDALResultMerger resultMerger = new ShardingDALResultMerger(DefaultDatabase.LOGIC_NAME, null);
        ShardingSphereMetaData metaData = mock(ShardingSphereMetaData.class);
        when(metaData.getDatabaseName()).thenReturn(DefaultDatabase.LOGIC_NAME);
        assertThat(resultMerger.merge(queryResults, sqlStatementContext, metaData), instanceOf(TransparentMergedResult.class));
    }
    
    @Test
    public void assertMergeForDescribeStatement() throws SQLException {
        DALStatement dalStatement = new MySQLExplainStatement();
        SQLStatementContext<?> sqlStatementContext = mockSQLStatementContext(dalStatement);
        ShardingDALResultMerger resultMerger = new ShardingDALResultMerger(DefaultDatabase.LOGIC_NAME, mock(ShardingRule.class));
        ShardingSphereMetaData metaData = mock(ShardingSphereMetaData.class);
        when(metaData.getDatabaseName()).thenReturn(DefaultDatabase.LOGIC_NAME);
        assertThat(resultMerger.merge(queryResults, sqlStatementContext, metaData), instanceOf(TransparentMergedResult.class));
    }
    
    @Test
    public void assertMergeForShowIndexStatement() throws SQLException {
        DALStatement dalStatement = new MySQLShowIndexStatement();
        SQLStatementContext<?> sqlStatementContext = mockSQLStatementContext(dalStatement);
        ShardingDALResultMerger resultMerger = new ShardingDALResultMerger(DefaultDatabase.LOGIC_NAME, mock(ShardingRule.class));
        ShardingSphereMetaData metaData = mock(ShardingSphereMetaData.class);
        when(metaData.getDatabaseName()).thenReturn(DefaultDatabase.LOGIC_NAME);
        assertThat(resultMerger.merge(queryResults, sqlStatementContext, metaData), instanceOf(ShowIndexMergedResult.class));
    }
    
    @Test
    public void assertMergeForShowTableStatusStatement() throws SQLException {
        DALStatement dalStatement = new MySQLShowTableStatusStatement();
        SQLStatementContext<?> sqlStatementContext = mockSQLStatementContext(dalStatement);
        ShardingDALResultMerger resultMerger = new ShardingDALResultMerger(DefaultDatabase.LOGIC_NAME, mock(ShardingRule.class));
        ShardingSphereMetaData metaData = mock(ShardingSphereMetaData.class);
        when(metaData.getDatabaseName()).thenReturn(DefaultDatabase.LOGIC_NAME);
        assertThat(resultMerger.merge(queryResults, sqlStatementContext, metaData), instanceOf(ShowTableStatusMergedResult.class));
    }
    
    @SuppressWarnings("unchecked")
    private SQLStatementContext<DALStatement> mockSQLStatementContext(final DALStatement dalStatement) {
        SQLStatementContext<DALStatement> result = mock(SQLStatementContext.class);
        when(result.getSqlStatement()).thenReturn(dalStatement);
        TablesContext tablesContext = mock(TablesContext.class);
        when(result.getTablesContext()).thenReturn(tablesContext);
        when(tablesContext.getSchemaName()).thenReturn(Optional.empty());
        when(result.getDatabaseType()).thenReturn(new MySQLDatabaseType());
        return result;
    }
}
