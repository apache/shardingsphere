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

package org.apache.shardingsphere.proxy.backend.handler;

import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.DefaultDatabase;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeFactory;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.util.exception.external.sql.UnsupportedSQLOperationException;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.parser.config.SQLParserRuleConfiguration;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.proxy.backend.communication.DatabaseCommunicationEngine;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.JDBCBackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.handler.admin.DatabaseAdminQueryBackendHandler;
import org.apache.shardingsphere.proxy.backend.handler.admin.DatabaseAdminUpdateBackendHandler;
import org.apache.shardingsphere.proxy.backend.handler.data.impl.UnicastDatabaseBackendHandler;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.QueryableGlobalRuleRALBackendHandler;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.QueryableRALBackendHandler;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.hint.HintRALBackendHandler;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.updatable.SetVariableHandler;
import org.apache.shardingsphere.proxy.backend.handler.distsql.rql.RQLBackendHandler;
import org.apache.shardingsphere.proxy.backend.handler.distsql.rul.SQLRULBackendHandler;
import org.apache.shardingsphere.proxy.backend.handler.skip.SkipBackendHandler;
import org.apache.shardingsphere.proxy.backend.handler.transaction.TransactionBackendHandler;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.util.ProxyContextRestorer;
import org.apache.shardingsphere.sql.parser.api.CacheOption;
import org.apache.shardingsphere.sql.parser.exception.SQLParsingException;
import org.apache.shardingsphere.transaction.ShardingSphereTransactionManagerEngine;
import org.apache.shardingsphere.transaction.core.TransactionType;
import org.apache.shardingsphere.transaction.rule.TransactionRule;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.SQLException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class ProxyBackendHandlerFactoryTest extends ProxyContextRestorer {
    
    private final DatabaseType databaseType = DatabaseTypeFactory.getInstance("MySQL");
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ConnectionSession connectionSession;
    
    @Mock
    private JDBCBackendConnection backendConnection;
    
    @Before
    public void setUp() {
        when(connectionSession.getTransactionStatus().getTransactionType()).thenReturn(TransactionType.LOCAL);
        when(connectionSession.getDefaultDatabaseName()).thenReturn("db");
        when(connectionSession.getBackendConnection()).thenReturn(backendConnection);
        when(backendConnection.getConnectionSession()).thenReturn(connectionSession);
        MetaDataContexts metaDataContexts = mock(MetaDataContexts.class, RETURNS_DEEP_STUBS);
        mockGlobalRuleMetaData(metaDataContexts);
        ShardingSphereDatabase database = mockDatabase();
        when(metaDataContexts.getMetaData().getDatabase("db")).thenReturn(database);
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(contextManager.getMetaDataContexts()).thenReturn(metaDataContexts);
        when(metaDataContexts.getMetaData().getProps()).thenReturn(new ConfigurationProperties(new Properties()));
        CacheOption cacheOption = new CacheOption(1024, 1024);
        when(metaDataContexts.getMetaData().getGlobalRuleMetaData().getSingleRule(SQLParserRule.class)).thenReturn(new SQLParserRule(new SQLParserRuleConfiguration(true, cacheOption, cacheOption)));
        ProxyContext.init(contextManager);
    }
    
    private ShardingSphereDatabase mockDatabase() {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(result.getRuleMetaData().getRules()).thenReturn(Collections.emptyList());
        when(result.getSchema(DefaultDatabase.LOGIC_NAME).getAllColumnNames("t_order")).thenReturn(Collections.singletonList("order_id"));
        return result;
    }
    
    private void mockGlobalRuleMetaData(final MetaDataContexts metaDataContexts) {
        ShardingSphereRuleMetaData globalRuleMetaData = mock(ShardingSphereRuleMetaData.class);
        TransactionRule transactionRule = mock(TransactionRule.class);
        when(transactionRule.getResource()).thenReturn(new ShardingSphereTransactionManagerEngine());
        when(globalRuleMetaData.getSingleRule(TransactionRule.class)).thenReturn(transactionRule);
        when(metaDataContexts.getMetaData().getGlobalRuleMetaData()).thenReturn(globalRuleMetaData);
    }
    
    @Test
    public void assertNewInstanceWithDistSQL() throws SQLException {
        String sql = "set variable transaction_type='LOCAL'";
        ProxyBackendHandler actual = ProxyBackendHandlerFactory.newInstance(databaseType, sql, connectionSession);
        assertThat(actual, instanceOf(SetVariableHandler.class));
        sql = "show variable transaction_type";
        actual = ProxyBackendHandlerFactory.newInstance(databaseType, sql, connectionSession);
        assertThat(actual, instanceOf(QueryableRALBackendHandler.class));
        sql = "show all variables";
        actual = ProxyBackendHandlerFactory.newInstance(databaseType, sql, connectionSession);
        assertThat(actual, instanceOf(QueryableRALBackendHandler.class));
        sql = "set sharding hint database_value=1";
        actual = ProxyBackendHandlerFactory.newInstance(databaseType, sql, connectionSession);
        assertThat(actual, instanceOf(HintRALBackendHandler.class));
    }
    
    @Test
    public void assertNewInstanceWithBegin() throws SQLException {
        String sql = "BEGIN";
        ProxyBackendHandler actual = ProxyBackendHandlerFactory.newInstance(databaseType, sql, connectionSession);
        assertThat(actual, instanceOf(TransactionBackendHandler.class));
    }
    
    @Test
    public void assertNewInstanceWithStartTransaction() throws SQLException {
        String sql = "START TRANSACTION";
        ProxyBackendHandler actual = ProxyBackendHandlerFactory.newInstance(databaseType, sql, connectionSession);
        assertThat(actual, instanceOf(TransactionBackendHandler.class));
    }
    
    @Test
    public void assertNewInstanceWithSetAutoCommitToOff() throws SQLException {
        String sql = "SET AUTOCOMMIT=0";
        ProxyBackendHandler actual = ProxyBackendHandlerFactory.newInstance(databaseType, sql, connectionSession);
        assertThat(actual, instanceOf(TransactionBackendHandler.class));
    }
    
    @Test
    public void assertNewInstanceWithScopeSetAutoCommitToOff() throws SQLException {
        String sql = "SET @@SESSION.AUTOCOMMIT = OFF";
        ProxyBackendHandler actual = ProxyBackendHandlerFactory.newInstance(databaseType, sql, connectionSession);
        assertThat(actual, instanceOf(TransactionBackendHandler.class));
    }
    
    @Test
    public void assertNewInstanceWithSetAutoCommitToOn() throws SQLException {
        String sql = "SET AUTOCOMMIT=1";
        ProxyBackendHandler actual = ProxyBackendHandlerFactory.newInstance(databaseType, sql, connectionSession);
        assertThat(actual, instanceOf(TransactionBackendHandler.class));
    }
    
    @Test
    public void assertNewInstanceWithScopeSetAutoCommitToOnForInTransaction() throws SQLException {
        String sql = "SET @@SESSION.AUTOCOMMIT = ON";
        ProxyBackendHandler actual = ProxyBackendHandlerFactory.newInstance(databaseType, sql, connectionSession);
        assertThat(actual, instanceOf(TransactionBackendHandler.class));
    }
    
    @Test
    public void assertNewInstanceWithUse() throws SQLException {
        String sql = "use sharding_db";
        ProxyBackendHandler actual = ProxyBackendHandlerFactory.newInstance(databaseType, sql, connectionSession);
        assertThat(actual, instanceOf(DatabaseAdminUpdateBackendHandler.class));
    }
    
    @Test
    public void assertNewInstanceWithShowDatabase() throws SQLException {
        String sql = "show databases";
        ProxyBackendHandler actual = ProxyBackendHandlerFactory.newInstance(databaseType, sql, connectionSession);
        assertThat(actual, instanceOf(DatabaseAdminQueryBackendHandler.class));
    }
    
    @Test
    public void assertNewInstanceWithSet() throws SQLException {
        String sql = "set @num=1";
        ProxyBackendHandler actual = ProxyBackendHandlerFactory.newInstance(databaseType, sql, connectionSession);
        assertThat(actual, instanceOf(DatabaseAdminUpdateBackendHandler.class));
    }
    
    @Test
    public void assertNewInstanceWithShow() throws SQLException {
        String sql = "SHOW VARIABLES LIKE '%x%'";
        ProxyBackendHandler actual = ProxyBackendHandlerFactory.newInstance(databaseType, sql, connectionSession);
        assertThat(actual, instanceOf(UnicastDatabaseBackendHandler.class));
        sql = "SHOW VARIABLES WHERE Variable_name ='language'";
        actual = ProxyBackendHandlerFactory.newInstance(databaseType, sql, connectionSession);
        assertThat(actual, instanceOf(UnicastDatabaseBackendHandler.class));
        sql = "SHOW CHARACTER SET";
        actual = ProxyBackendHandlerFactory.newInstance(databaseType, sql, connectionSession);
        assertThat(actual, instanceOf(UnicastDatabaseBackendHandler.class));
        sql = "SHOW COLLATION";
        actual = ProxyBackendHandlerFactory.newInstance(databaseType, sql, connectionSession);
        assertThat(actual, instanceOf(UnicastDatabaseBackendHandler.class));
    }
    
    // TODO Fix me
    @Ignore
    @Test
    public void assertNewInstanceWithQuery() throws SQLException {
        String sql = "select * from t_order limit 1";
        ProxyContext proxyContext = ProxyContext.getInstance();
        when(proxyContext.getAllDatabaseNames()).thenReturn(new HashSet<>(Collections.singletonList("db")));
        when(proxyContext.getDatabase("db").containsDataSource()).thenReturn(true);
        ProxyBackendHandler actual = ProxyBackendHandlerFactory.newInstance(databaseType, sql, connectionSession);
        assertThat(actual, instanceOf(DatabaseCommunicationEngine.class));
        sql = "select * from information_schema.schemata limit 1";
        actual = ProxyBackendHandlerFactory.newInstance(databaseType, sql, connectionSession);
        assertThat(actual, instanceOf(DatabaseAdminQueryBackendHandler.class));
    }
    
    @Test
    public void assertNewInstanceWithEmptyString() throws SQLException {
        String sql = "";
        ProxyBackendHandler actual = ProxyBackendHandlerFactory.newInstance(databaseType, sql, connectionSession);
        assertThat(actual, instanceOf(SkipBackendHandler.class));
    }
    
    @Test(expected = SQLParsingException.class)
    public void assertNewInstanceWithErrorSQL() throws SQLException {
        String sql = "SELECT";
        ProxyBackendHandlerFactory.newInstance(databaseType, sql, connectionSession);
    }
    
    @Test(expected = SQLParsingException.class)
    public void assertNewInstanceWithErrorRDL() throws SQLException {
        String sql = "CREATE SHARDING";
        ProxyBackendHandlerFactory.newInstance(databaseType, sql, connectionSession);
    }
    
    @Test(expected = UnsupportedSQLOperationException.class)
    public void assertUnsupportedNonQueryDistSQLInTransaction() throws SQLException {
        when(connectionSession.getTransactionStatus().isInTransaction()).thenReturn(true);
        String sql = "CREATE SHARDING KEY GENERATOR snowflake_key_generator (TYPE(NAME='SNOWFLAKE', PROPERTIES('max-vibration-offset'='3')));";
        ProxyBackendHandlerFactory.newInstance(databaseType, sql, connectionSession);
    }
    
    @Test
    public void assertUnsupportedQueryableRALStatementInTransaction() throws SQLException {
        when(connectionSession.getTransactionStatus().isInTransaction()).thenReturn(true);
        String sql = "SHOW TRANSACTION RULE;";
        ProxyBackendHandler actual = ProxyBackendHandlerFactory.newInstance(databaseType, sql, connectionSession);
        assertThat(actual, instanceOf(QueryableGlobalRuleRALBackendHandler.class));
    }
    
    @Test
    public void assertUnsupportedRQLStatementInTransaction() throws SQLException {
        when(connectionSession.getTransactionStatus().isInTransaction()).thenReturn(true);
        String sql = "SHOW SINGLE TABLE RULES";
        ProxyBackendHandler actual = ProxyBackendHandlerFactory.newInstance(databaseType, sql, connectionSession);
        assertThat(actual, instanceOf(RQLBackendHandler.class));
    }
    
    @Test
    public void assertDistSQLRULStatementInTransaction() throws SQLException {
        when(connectionSession.getTransactionStatus().isInTransaction()).thenReturn(true);
        String sql = "PREVIEW INSERT INTO account VALUES(1, 1, 1)";
        ProxyBackendHandler actual = ProxyBackendHandlerFactory.newInstance(databaseType, sql, connectionSession);
        assertThat(actual, instanceOf(SQLRULBackendHandler.class));
    }
}
