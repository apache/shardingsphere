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

import org.apache.shardingsphere.authority.rule.AuthorityRule;
import org.apache.shardingsphere.authority.rule.builder.DefaultAuthorityRuleConfigurationBuilder;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.core.DefaultDatabase;
import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.state.cluster.ClusterState;
import org.apache.shardingsphere.infra.util.exception.external.sql.type.generic.UnsupportedSQLOperationException;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.parser.rule.builder.DefaultSQLParserRuleConfigurationBuilder;
import org.apache.shardingsphere.proxy.backend.connector.DatabaseConnector;
import org.apache.shardingsphere.proxy.backend.connector.ProxyDatabaseConnectionManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.handler.admin.DatabaseAdminQueryBackendHandler;
import org.apache.shardingsphere.proxy.backend.handler.data.impl.UnicastDatabaseBackendHandler;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.QueryableRALBackendHandler;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.UpdatableRALBackendHandler;
import org.apache.shardingsphere.proxy.backend.handler.distsql.rql.RQLBackendHandler;
import org.apache.shardingsphere.proxy.backend.handler.distsql.rul.SQLRULBackendHandler;
import org.apache.shardingsphere.proxy.backend.handler.skip.SkipBackendHandler;
import org.apache.shardingsphere.proxy.backend.handler.transaction.TransactionBackendHandler;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.sql.parser.exception.SQLParsingException;
import org.apache.shardingsphere.test.mock.AutoMockExtension;
import org.apache.shardingsphere.test.mock.StaticMockSettings;
import org.apache.shardingsphere.transaction.api.TransactionType;
import org.apache.shardingsphere.transaction.rule.TransactionRule;
import org.apache.shardingsphere.transaction.rule.builder.DefaultTransactionRuleConfigurationBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Properties;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ProxyContext.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ProxyBackendHandlerFactoryTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "MySQL");
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ConnectionSession connectionSession;
    
    @BeforeEach
    void setUp() {
        when(connectionSession.getTransactionStatus().getTransactionType()).thenReturn(TransactionType.LOCAL);
        when(connectionSession.getDefaultDatabaseName()).thenReturn("db");
        when(connectionSession.getDatabaseName()).thenReturn("db");
        ProxyDatabaseConnectionManager databaseConnectionManager = mock(ProxyDatabaseConnectionManager.class);
        when(databaseConnectionManager.getConnectionSession()).thenReturn(connectionSession);
        when(connectionSession.getDatabaseConnectionManager()).thenReturn(databaseConnectionManager);
        ContextManager contextManager = mockContextManager();
        when(contextManager.getClusterStateContext().getCurrentState()).thenReturn(ClusterState.OK);
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
    }
    
    private ContextManager mockContextManager() {
        MetaDataContexts metaDataContexts = mock(MetaDataContexts.class, RETURNS_DEEP_STUBS);
        ShardingSphereDatabase database = mockDatabase();
        when(metaDataContexts.getMetaData().getDatabase("db")).thenReturn(database);
        ContextManager result = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(result.getMetaDataContexts()).thenReturn(metaDataContexts);
        when(metaDataContexts.getMetaData().getProps()).thenReturn(new ConfigurationProperties(new Properties()));
        ShardingSphereRuleMetaData globalRuleMetaData = new ShardingSphereRuleMetaData(Arrays.asList(
                new AuthorityRule(new DefaultAuthorityRuleConfigurationBuilder().build(), Collections.emptyMap()),
                new SQLParserRule(new DefaultSQLParserRuleConfigurationBuilder().build()),
                new TransactionRule(new DefaultTransactionRuleConfigurationBuilder().build(), Collections.emptyMap())));
        when(metaDataContexts.getMetaData().getGlobalRuleMetaData()).thenReturn(globalRuleMetaData);
        return result;
    }
    
    private ShardingSphereDatabase mockDatabase() {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(result.getRuleMetaData().getRules()).thenReturn(Collections.emptyList());
        when(result.getSchema(DefaultDatabase.LOGIC_NAME).getAllColumnNames("t_order")).thenReturn(Collections.singletonList("order_id"));
        return result;
    }
    
    @Test
    void assertNewInstanceWithDistSQL() throws SQLException {
        String sql = "set dist variable sql_show='true'";
        ProxyBackendHandler actual = ProxyBackendHandlerFactory.newInstance(databaseType, sql, connectionSession, new HintValueContext());
        assertThat(actual, instanceOf(UpdatableRALBackendHandler.class));
        sql = "show dist variable where name = sql_show";
        actual = ProxyBackendHandlerFactory.newInstance(databaseType, sql, connectionSession, new HintValueContext());
        assertThat(actual, instanceOf(QueryableRALBackendHandler.class));
        sql = "show dist variables";
        actual = ProxyBackendHandlerFactory.newInstance(databaseType, sql, connectionSession, new HintValueContext());
        assertThat(actual, instanceOf(QueryableRALBackendHandler.class));
    }
    
    @ParameterizedTest(name = "{0}")
    @ArgumentsSource(TCLTestCaseArgumentsProvider.class)
    void assertNewInstanceWithTCL(final String sql) throws SQLException {
        ProxyBackendHandler actual = ProxyBackendHandlerFactory.newInstance(databaseType, sql, connectionSession, new HintValueContext());
        assertThat(actual, instanceOf(TransactionBackendHandler.class));
    }
    
    @Test
    void assertNewInstanceWithShow() throws SQLException {
        String sql = "SHOW VARIABLES LIKE '%x%'";
        ProxyBackendHandler actual = ProxyBackendHandlerFactory.newInstance(databaseType, sql, connectionSession, new HintValueContext());
        assertThat(actual, instanceOf(UnicastDatabaseBackendHandler.class));
        sql = "SHOW VARIABLES WHERE Variable_name ='language'";
        actual = ProxyBackendHandlerFactory.newInstance(databaseType, sql, connectionSession, new HintValueContext());
        assertThat(actual, instanceOf(UnicastDatabaseBackendHandler.class));
        sql = "SHOW CHARACTER SET";
        actual = ProxyBackendHandlerFactory.newInstance(databaseType, sql, connectionSession, new HintValueContext());
        assertThat(actual, instanceOf(UnicastDatabaseBackendHandler.class));
        sql = "SHOW COLLATION";
        actual = ProxyBackendHandlerFactory.newInstance(databaseType, sql, connectionSession, new HintValueContext());
        assertThat(actual, instanceOf(UnicastDatabaseBackendHandler.class));
    }
    
    // TODO
    @Disabled("FIXME")
    @Test
    void assertNewInstanceWithQuery() throws SQLException {
        String sql = "SELECT * FROM t_order limit 1";
        ProxyContext proxyContext = ProxyContext.getInstance();
        when(proxyContext.getAllDatabaseNames()).thenReturn(new HashSet<>(Collections.singletonList("db")));
        when(proxyContext.getDatabase("db").containsDataSource()).thenReturn(true);
        ProxyBackendHandler actual = ProxyBackendHandlerFactory.newInstance(databaseType, sql, connectionSession, new HintValueContext());
        assertThat(actual, instanceOf(DatabaseConnector.class));
        sql = "SELECT * FROM information_schema.schemata LIMIT 1";
        actual = ProxyBackendHandlerFactory.newInstance(databaseType, sql, connectionSession, new HintValueContext());
        assertThat(actual, instanceOf(DatabaseAdminQueryBackendHandler.class));
    }
    
    @Test
    void assertNewInstanceWithEmptyString() throws SQLException {
        String sql = "";
        ProxyBackendHandler actual = ProxyBackendHandlerFactory.newInstance(databaseType, sql, connectionSession, new HintValueContext());
        assertThat(actual, instanceOf(SkipBackendHandler.class));
    }
    
    @Test
    void assertNewInstanceWithErrorSQL() {
        String sql = "SELECT";
        assertThrows(SQLParsingException.class, () -> ProxyBackendHandlerFactory.newInstance(databaseType, sql, connectionSession, new HintValueContext()));
    }
    
    @Test
    void assertNewInstanceWithErrorRDL() {
        String sql = "CREATE SHARDING";
        assertThrows(SQLParsingException.class, () -> ProxyBackendHandlerFactory.newInstance(databaseType, sql, connectionSession, new HintValueContext()));
    }
    
    @Test
    void assertUnsupportedNonQueryDistSQLInTransaction() {
        when(connectionSession.getTransactionStatus().isInTransaction()).thenReturn(true);
        String sql = "CREATE SHARDING TABLE RULE t_order (STORAGE_UNITS(ms_group_0,ms_group_1), SHARDING_COLUMN=order_id, TYPE(NAME='hash_mod', PROPERTIES('sharding-count'='4')));";
        assertThrows(UnsupportedSQLOperationException.class, () -> ProxyBackendHandlerFactory.newInstance(databaseType, sql, connectionSession, new HintValueContext()));
    }
    
    @Test
    void assertUnsupportedQueryableRALStatementInTransaction() throws SQLException {
        when(connectionSession.getTransactionStatus().isInTransaction()).thenReturn(true);
        String sql = "SHOW TRANSACTION RULE;";
        ProxyBackendHandler actual = ProxyBackendHandlerFactory.newInstance(databaseType, sql, connectionSession, new HintValueContext());
        assertThat(actual, instanceOf(QueryableRALBackendHandler.class));
    }
    
    @Test
    void assertUnsupportedRQLStatementInTransaction() throws SQLException {
        when(connectionSession.getTransactionStatus().isInTransaction()).thenReturn(true);
        String sql = "SHOW DEFAULT SINGLE TABLE STORAGE UNIT";
        ProxyBackendHandler actual = ProxyBackendHandlerFactory.newInstance(databaseType, sql, connectionSession, new HintValueContext());
        assertThat(actual, instanceOf(RQLBackendHandler.class));
    }
    
    @Test
    void assertDistSQLRULStatementInTransaction() throws SQLException {
        when(connectionSession.getTransactionStatus().isInTransaction()).thenReturn(true);
        String sql = "PREVIEW INSERT INTO account VALUES(1, 1, 1)";
        ProxyBackendHandler actual = ProxyBackendHandlerFactory.newInstance(databaseType, sql, connectionSession, new HintValueContext());
        assertThat(actual, instanceOf(SQLRULBackendHandler.class));
    }
    
    private static class TCLTestCaseArgumentsProvider implements ArgumentsProvider {
        
        @Override
        public Stream<? extends Arguments> provideArguments(final ExtensionContext extensionContext) {
            return Stream.of(
                    Arguments.of("BEGIN"),
                    Arguments.of("START TRANSACTION"),
                    Arguments.of("SET AUTOCOMMIT=0"),
                    Arguments.of("SET @@SESSION.AUTOCOMMIT = OFF"),
                    Arguments.of("SET AUTOCOMMIT=1"),
                    Arguments.of("SET @@SESSION.AUTOCOMMIT = ON"));
        }
    }
}
