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

package org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor;

import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.core.type.DatabaseTypeFactory;
import org.apache.shardingsphere.infra.database.mysql.MySQLDatabaseType;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResultMetaData;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ShardingSphereResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dal.FromSchemaSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dal.ShowFilterSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dal.ShowLikeSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.DatabaseSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowTablesStatement;
import org.apache.shardingsphere.test.mock.AutoMockExtension;
import org.apache.shardingsphere.test.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ProxyContext.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ShowTablesExecutorTest {
    
    private static final String DATABASE_PATTERN = "db_%s";
    
    @Test
    void assertShowTablesExecutorWithoutFilter() throws SQLException {
        ShowTablesExecutor executor = new ShowTablesExecutor(new MySQLShowTablesStatement(), DatabaseTypeFactory.get("MySQL"));
        Map<String, ShardingSphereDatabase> databases = getDatabases();
        ContextManager contextManager = mockContextManager(databases);
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        when(ProxyContext.getInstance().getDatabase("db_0")).thenReturn(databases.get("db_0"));
        executor.execute(mockConnectionSession());
        assertThat(executor.getQueryResultMetaData().getColumnCount(), is(1));
        executor.getMergedResult().next();
        assertThat(executor.getMergedResult().getValue(1, Object.class), is("T_TEST"));
        executor.getMergedResult().next();
        assertThat(executor.getMergedResult().getValue(1, Object.class), is("t_account"));
        executor.getMergedResult().next();
        assertThat(executor.getMergedResult().getValue(1, Object.class), is("t_account_bak"));
        executor.getMergedResult().next();
        assertThat(executor.getMergedResult().getValue(1, Object.class), is("t_account_detail"));
        assertFalse(executor.getMergedResult().next());
    }
    
    @Test
    void assertShowTablesExecutorWithFull() throws SQLException {
        MySQLShowTablesStatement showTablesStatement = mock(MySQLShowTablesStatement.class);
        when(showTablesStatement.isContainsFull()).thenReturn(true);
        ShowTablesExecutor executor = new ShowTablesExecutor(showTablesStatement, DatabaseTypeFactory.get("MySQL"));
        Map<String, ShardingSphereDatabase> databases = getDatabases();
        ContextManager contextManager = mockContextManager(databases);
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        when(ProxyContext.getInstance().getDatabase("db_0")).thenReturn(databases.get("db_0"));
        executor.execute(mockConnectionSession());
        assertThat(executor.getQueryResultMetaData().getColumnCount(), is(2));
    }
    
    @Test
    void assertShowTablesExecutorWithLikeFilter() throws SQLException {
        MySQLShowTablesStatement showTablesStatement = new MySQLShowTablesStatement();
        ShowFilterSegment showFilterSegment = mock(ShowFilterSegment.class);
        when(showFilterSegment.getLike()).thenReturn(Optional.of(new ShowLikeSegment(0, 10, "t_account%")));
        showTablesStatement.setFilter(showFilterSegment);
        ShowTablesExecutor executor = new ShowTablesExecutor(showTablesStatement, new MySQLDatabaseType());
        Map<String, ShardingSphereDatabase> databases = getDatabases();
        ContextManager contextManager = mockContextManager(databases);
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        when(ProxyContext.getInstance().getDatabase("db_0")).thenReturn(databases.get("db_0"));
        executor.execute(mockConnectionSession());
        assertThat(executor.getQueryResultMetaData().getColumnCount(), is(1));
        executor.getMergedResult().next();
        assertThat(executor.getMergedResult().getValue(1, Object.class), is("t_account"));
        executor.getMergedResult().next();
        assertThat(executor.getMergedResult().getValue(1, Object.class), is("t_account_bak"));
        executor.getMergedResult().next();
        assertThat(executor.getMergedResult().getValue(1, Object.class), is("t_account_detail"));
        assertFalse(executor.getMergedResult().next());
    }
    
    @Test
    void assertShowTablesExecutorWithSpecificTable() throws SQLException {
        MySQLShowTablesStatement showTablesStatement = new MySQLShowTablesStatement();
        ShowFilterSegment showFilterSegment = mock(ShowFilterSegment.class);
        when(showFilterSegment.getLike()).thenReturn(Optional.of(new ShowLikeSegment(0, 10, "t_account")));
        showTablesStatement.setFilter(showFilterSegment);
        ShowTablesExecutor executor = new ShowTablesExecutor(showTablesStatement, new MySQLDatabaseType());
        Map<String, ShardingSphereDatabase> databases = getDatabases();
        ContextManager contextManager = mockContextManager(databases);
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        when(ProxyContext.getInstance().getDatabase("db_0")).thenReturn(databases.get("db_0"));
        executor.execute(mockConnectionSession());
        assertThat(executor.getQueryResultMetaData().getColumnCount(), is(1));
        executor.getMergedResult().next();
        assertThat(executor.getMergedResult().getValue(1, Object.class), is("t_account"));
        assertFalse(executor.getMergedResult().next());
    }
    
    @Test
    void assertShowTablesExecutorWithUpperCase() throws SQLException {
        MySQLShowTablesStatement showTablesStatement = new MySQLShowTablesStatement();
        ShowFilterSegment showFilterSegment = mock(ShowFilterSegment.class);
        when(showFilterSegment.getLike()).thenReturn(Optional.of(new ShowLikeSegment(0, 10, "T_TEST")));
        showTablesStatement.setFilter(showFilterSegment);
        ShowTablesExecutor executor = new ShowTablesExecutor(showTablesStatement, new MySQLDatabaseType());
        Map<String, ShardingSphereDatabase> databases = getDatabases();
        ContextManager contextManager = mockContextManager(databases);
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        when(ProxyContext.getInstance().getDatabase("db_0")).thenReturn(databases.get("db_0"));
        executor.execute(mockConnectionSession());
        assertThat(executor.getQueryResultMetaData().getColumnCount(), is(1));
        executor.getMergedResult().next();
        assertThat(executor.getMergedResult().getValue(1, Object.class), is("T_TEST"));
        assertFalse(executor.getMergedResult().next());
    }
    
    @Test
    void assertShowTablesExecutorWithLowerCase() throws SQLException {
        MySQLShowTablesStatement showTablesStatement = new MySQLShowTablesStatement();
        ShowFilterSegment showFilterSegment = mock(ShowFilterSegment.class);
        when(showFilterSegment.getLike()).thenReturn(Optional.of(new ShowLikeSegment(0, 10, "t_test")));
        showTablesStatement.setFilter(showFilterSegment);
        ShowTablesExecutor executor = new ShowTablesExecutor(showTablesStatement, new MySQLDatabaseType());
        Map<String, ShardingSphereDatabase> databases = getDatabases();
        ContextManager contextManager = mockContextManager(databases);
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        when(ProxyContext.getInstance().getDatabase("db_0")).thenReturn(databases.get("db_0"));
        executor.execute(mockConnectionSession());
        assertThat(executor.getQueryResultMetaData().getColumnCount(), is(1));
        executor.getMergedResult().next();
        assertThat(executor.getMergedResult().getValue(1, Object.class), is("T_TEST"));
        assertFalse(executor.getMergedResult().next());
    }
    
    @Test
    void assertShowTableFromUncompletedDatabase() throws SQLException {
        MySQLShowTablesStatement showTablesStatement = new MySQLShowTablesStatement();
        showTablesStatement.setFromSchema(new FromSchemaSegment(0, 0, new DatabaseSegment(0, 0, new IdentifierValue("uncompleted"))));
        ShowTablesExecutor executor = new ShowTablesExecutor(showTablesStatement, new MySQLDatabaseType());
        ContextManager contextManager = mockContextManager(getDatabases());
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        executor.execute(mockConnectionSession());
        QueryResultMetaData actualMetaData = executor.getQueryResultMetaData();
        assertThat(actualMetaData.getColumnCount(), is(1));
        assertThat(actualMetaData.getColumnName(1), is("Tables_in_uncompleted"));
        MergedResult actualResult = executor.getMergedResult();
        assertFalse(actualResult.next());
    }
    
    private ContextManager mockContextManager(final Map<String, ShardingSphereDatabase> databases) {
        MetaDataContexts metaDataContexts = new MetaDataContexts(mock(MetaDataPersistService.class),
                new ShardingSphereMetaData(databases, mock(ShardingSphereResourceMetaData.class), mock(ShardingSphereRuleMetaData.class), new ConfigurationProperties(new Properties())));
        ContextManager result = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(result.getMetaDataContexts()).thenReturn(metaDataContexts);
        return result;
    }
    
    private Map<String, ShardingSphereDatabase> getDatabases() {
        Map<String, ShardingSphereTable> tables = new HashMap<>(4, 1F);
        tables.put("t_account", new ShardingSphereTable("t_account", Collections.emptyList(), Collections.emptyList(), Collections.emptyList()));
        tables.put("t_account_bak", new ShardingSphereTable("t_account_bak", Collections.emptyList(), Collections.emptyList(), Collections.emptyList()));
        tables.put("t_account_detail", new ShardingSphereTable("t_account_detail", Collections.emptyList(), Collections.emptyList(), Collections.emptyList()));
        tables.put("t_test", new ShardingSphereTable("T_TEST", Collections.emptyList(), Collections.emptyList(), Collections.emptyList()));
        ShardingSphereSchema schema = new ShardingSphereSchema(tables, Collections.emptyMap());
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getSchema(String.format(DATABASE_PATTERN, 0))).thenReturn(schema);
        when(database.isComplete()).thenReturn(true);
        when(database.getProtocolType()).thenReturn(new MySQLDatabaseType());
        Map<String, ShardingSphereDatabase> result = new HashMap<>(2, 1F);
        result.put(String.format(DATABASE_PATTERN, 0), database);
        ShardingSphereDatabase uncompletedDatabase = mock(ShardingSphereDatabase.class);
        result.put("uncompleted", uncompletedDatabase);
        return result;
    }
    
    private ConnectionSession mockConnectionSession() {
        ConnectionSession result = mock(ConnectionSession.class);
        when(result.getDatabaseName()).thenReturn(String.format(DATABASE_PATTERN, 0));
        return result;
    }
}
