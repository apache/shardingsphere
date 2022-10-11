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

package org.apache.shardingsphere.proxy.backend.handler.admin.mysql.executor;

import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeEngine;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResultMetaData;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.data.ShardingSphereData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.user.Grantee;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.util.ProxyContextRestorer;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dal.FromSchemaSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dal.ShowFilterSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dal.ShowLikeSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.DatabaseSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowTablesStatement;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ShowTablesExecutorTest extends ProxyContextRestorer {
    
    private static final String DATABASE_PATTERN = "db_%s";
    
    @Before
    public void setUp() {
        Map<String, ShardingSphereDatabase> databases = getDatabases();
        MetaDataContexts metaDataContexts = new MetaDataContexts(mock(MetaDataPersistService.class),
                new ShardingSphereMetaData(databases, mock(ShardingSphereRuleMetaData.class), new ConfigurationProperties(new Properties())), new ShardingSphereData());
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(contextManager.getMetaDataContexts()).thenReturn(metaDataContexts);
        ProxyContext.init(contextManager);
    }
    
    private Map<String, ShardingSphereDatabase> getDatabases() {
        Map<String, ShardingSphereTable> tables = new HashMap<>(4, 1);
        tables.put("t_account", new ShardingSphereTable("t_account", Collections.emptyList(), Collections.emptyList(), Collections.emptyList()));
        tables.put("t_account_bak", new ShardingSphereTable("t_account_bak", Collections.emptyList(), Collections.emptyList(), Collections.emptyList()));
        tables.put("t_account_detail", new ShardingSphereTable("t_account_detail", Collections.emptyList(), Collections.emptyList(), Collections.emptyList()));
        tables.put("t_test", new ShardingSphereTable("T_TEST", Collections.emptyList(), Collections.emptyList(), Collections.emptyList()));
        ShardingSphereSchema schema = new ShardingSphereSchema(tables, Collections.emptyMap());
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getSchema(String.format(DATABASE_PATTERN, 0))).thenReturn(schema);
        when(database.isComplete()).thenReturn(true);
        when(database.getResources().getDatabaseType()).thenReturn(new MySQLDatabaseType());
        Map<String, ShardingSphereDatabase> result = new HashMap<>(2, 1);
        result.put(String.format(DATABASE_PATTERN, 0), database);
        ShardingSphereDatabase uncompletedDatabase = mock(ShardingSphereDatabase.class);
        when(uncompletedDatabase.isComplete()).thenReturn(false);
        result.put("uncompleted", uncompletedDatabase);
        return result;
    }
    
    @Test
    public void assertShowTablesExecutorWithoutFilter() throws SQLException {
        ShowTablesExecutor showTablesExecutor = new ShowTablesExecutor(new MySQLShowTablesStatement(), DatabaseTypeEngine.getDatabaseType("MySQL"));
        showTablesExecutor.execute(mockConnectionSession());
        assertThat(showTablesExecutor.getQueryResultMetaData().getColumnCount(), is(2));
        showTablesExecutor.getMergedResult().next();
        assertThat(showTablesExecutor.getMergedResult().getValue(1, Object.class), is("T_TEST"));
        showTablesExecutor.getMergedResult().next();
        assertThat(showTablesExecutor.getMergedResult().getValue(1, Object.class), is("t_account"));
        showTablesExecutor.getMergedResult().next();
        assertThat(showTablesExecutor.getMergedResult().getValue(1, Object.class), is("t_account_bak"));
        showTablesExecutor.getMergedResult().next();
        assertThat(showTablesExecutor.getMergedResult().getValue(1, Object.class), is("t_account_detail"));
        assertFalse(showTablesExecutor.getMergedResult().next());
    }
    
    @Test
    public void assertShowTablesExecutorWithLikeFilter() throws SQLException {
        MySQLShowTablesStatement showTablesStatement = new MySQLShowTablesStatement();
        ShowFilterSegment showFilterSegment = mock(ShowFilterSegment.class);
        when(showFilterSegment.getLike()).thenReturn(Optional.of(new ShowLikeSegment(0, 10, "t_account%")));
        showTablesStatement.setFilter(showFilterSegment);
        ShowTablesExecutor showTablesExecutor = new ShowTablesExecutor(showTablesStatement, new MySQLDatabaseType());
        showTablesExecutor.execute(mockConnectionSession());
        assertThat(showTablesExecutor.getQueryResultMetaData().getColumnCount(), is(2));
        showTablesExecutor.getMergedResult().next();
        assertThat(showTablesExecutor.getMergedResult().getValue(1, Object.class), is("t_account"));
        showTablesExecutor.getMergedResult().next();
        assertThat(showTablesExecutor.getMergedResult().getValue(1, Object.class), is("t_account_bak"));
        showTablesExecutor.getMergedResult().next();
        assertThat(showTablesExecutor.getMergedResult().getValue(1, Object.class), is("t_account_detail"));
        assertFalse(showTablesExecutor.getMergedResult().next());
    }
    
    @Test
    public void assertShowTablesExecutorWithSpecificTable() throws SQLException {
        MySQLShowTablesStatement showTablesStatement = new MySQLShowTablesStatement();
        ShowFilterSegment showFilterSegment = mock(ShowFilterSegment.class);
        when(showFilterSegment.getLike()).thenReturn(Optional.of(new ShowLikeSegment(0, 10, "t_account")));
        showTablesStatement.setFilter(showFilterSegment);
        ShowTablesExecutor showTablesExecutor = new ShowTablesExecutor(showTablesStatement, new MySQLDatabaseType());
        showTablesExecutor.execute(mockConnectionSession());
        assertThat(showTablesExecutor.getQueryResultMetaData().getColumnCount(), is(2));
        showTablesExecutor.getMergedResult().next();
        assertThat(showTablesExecutor.getMergedResult().getValue(1, Object.class), is("t_account"));
        assertFalse(showTablesExecutor.getMergedResult().next());
    }
    
    @Test
    public void assertShowTablesExecutorWithUpperCase() throws SQLException {
        MySQLShowTablesStatement showTablesStatement = new MySQLShowTablesStatement();
        ShowFilterSegment showFilterSegment = mock(ShowFilterSegment.class);
        when(showFilterSegment.getLike()).thenReturn(Optional.of(new ShowLikeSegment(0, 10, "T_TEST")));
        showTablesStatement.setFilter(showFilterSegment);
        ShowTablesExecutor showTablesExecutor = new ShowTablesExecutor(showTablesStatement, new MySQLDatabaseType());
        showTablesExecutor.execute(mockConnectionSession());
        assertThat(showTablesExecutor.getQueryResultMetaData().getColumnCount(), is(2));
        showTablesExecutor.getMergedResult().next();
        assertThat(showTablesExecutor.getMergedResult().getValue(1, Object.class), is("T_TEST"));
        assertFalse(showTablesExecutor.getMergedResult().next());
    }
    
    @Test
    public void assertShowTablesExecutorWithLowerCase() throws SQLException {
        MySQLShowTablesStatement showTablesStatement = new MySQLShowTablesStatement();
        ShowFilterSegment showFilterSegment = mock(ShowFilterSegment.class);
        when(showFilterSegment.getLike()).thenReturn(Optional.of(new ShowLikeSegment(0, 10, "t_test")));
        showTablesStatement.setFilter(showFilterSegment);
        ShowTablesExecutor showTablesExecutor = new ShowTablesExecutor(showTablesStatement, new MySQLDatabaseType());
        showTablesExecutor.execute(mockConnectionSession());
        assertThat(showTablesExecutor.getQueryResultMetaData().getColumnCount(), is(2));
        showTablesExecutor.getMergedResult().next();
        assertThat(showTablesExecutor.getMergedResult().getValue(1, Object.class), is("T_TEST"));
        assertFalse(showTablesExecutor.getMergedResult().next());
    }
    
    @Test
    public void assertShowTableFromUncompletedDatabase() throws SQLException {
        MySQLShowTablesStatement showTablesStatement = new MySQLShowTablesStatement();
        showTablesStatement.setFromSchema(new FromSchemaSegment(0, 0, new DatabaseSegment(0, 0, new IdentifierValue("uncompleted"))));
        ShowTablesExecutor showTablesExecutor = new ShowTablesExecutor(showTablesStatement, new MySQLDatabaseType());
        showTablesExecutor.execute(mockConnectionSession());
        QueryResultMetaData actualMetaData = showTablesExecutor.getQueryResultMetaData();
        assertThat(actualMetaData.getColumnCount(), is(2));
        assertThat(actualMetaData.getColumnName(1), is("Tables_in_uncompleted"));
        MergedResult actualResult = showTablesExecutor.getMergedResult();
        assertFalse(actualResult.next());
    }
    
    private ConnectionSession mockConnectionSession() {
        ConnectionSession result = mock(ConnectionSession.class);
        when(result.getGrantee()).thenReturn(new Grantee("root", ""));
        when(result.getDatabaseName()).thenReturn(String.format(DATABASE_PATTERN, 0));
        return result;
    }
}
