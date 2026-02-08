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
import org.apache.shardingsphere.authority.rule.builder.DefaultUser;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.exception.generic.UnsupportedSQLOperationException;
import org.apache.shardingsphere.infra.executor.checker.SQLExecutionChecker;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.user.Grantee;
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.apache.shardingsphere.infra.session.connection.transaction.TransactionConnectionContext;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.state.ShardingSphereState;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.parser.rule.builder.DefaultSQLParserRuleConfigurationBuilder;
import org.apache.shardingsphere.proxy.backend.connector.ProxyDatabaseConnectionManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.handler.admin.DatabaseAdminProxyBackendHandlerFactory;
import org.apache.shardingsphere.proxy.backend.handler.data.DatabaseProxyBackendHandler;
import org.apache.shardingsphere.proxy.backend.handler.data.DatabaseProxyBackendHandlerFactory;
import org.apache.shardingsphere.proxy.backend.handler.data.type.UnicastDatabaseProxyBackendHandler;
import org.apache.shardingsphere.proxy.backend.handler.database.type.CreateDatabaseProxyBackendHandler;
import org.apache.shardingsphere.proxy.backend.handler.database.type.DropDatabaseProxyBackendHandler;
import org.apache.shardingsphere.proxy.backend.handler.distsql.DistSQLQueryProxyBackendHandler;
import org.apache.shardingsphere.proxy.backend.handler.distsql.DistSQLUpdateProxyBackendHandler;
import org.apache.shardingsphere.proxy.backend.handler.skip.SkipProxyBackendHandler;
import org.apache.shardingsphere.proxy.backend.handler.tcl.local.type.BeginTransactionProxyBackendHandler;
import org.apache.shardingsphere.proxy.backend.handler.tcl.local.type.CommitProxyBackendHandler;
import org.apache.shardingsphere.proxy.backend.handler.tcl.local.type.ReleaseSavepointProxyBackendHandler;
import org.apache.shardingsphere.proxy.backend.handler.tcl.local.type.RollbackProxyBackendHandler;
import org.apache.shardingsphere.proxy.backend.handler.tcl.local.type.RollbackSavepointProxyBackendHandler;
import org.apache.shardingsphere.proxy.backend.handler.tcl.local.type.SetAutoCommitProxyBackendHandler;
import org.apache.shardingsphere.proxy.backend.handler.tcl.local.type.SetSavepointProxyBackendHandler;
import org.apache.shardingsphere.proxy.backend.handler.tcl.local.type.SetTransactionProxyBackendHandler;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dal.EmptyStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.table.RenameTableStatement;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.apache.shardingsphere.transaction.rule.TransactionRule;
import org.apache.shardingsphere.transaction.rule.builder.DefaultTransactionRuleConfigurationBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.support.ParameterDeclarations;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings({ProxyContext.class, DatabaseAdminProxyBackendHandlerFactory.class})
@MockitoSettings(strictness = Strictness.LENIENT)
class ProxyBackendHandlerFactoryTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "MySQL");
    
    private final DatabaseType postgreSQLDatabaseType = TypedSPILoader.getService(DatabaseType.class, "PostgreSQL");
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ConnectionSession connectionSession;
    
    @BeforeEach
    void setUp() {
        when(DatabaseAdminProxyBackendHandlerFactory.newInstance(any(), any(), any(), any(), any())).thenCallRealMethod();
        ConnectionContext connectionContext = mockConnectionContext();
        when(connectionSession.getConnectionContext()).thenReturn(connectionContext);
        when(connectionSession.getCurrentDatabaseName()).thenReturn("db");
        when(connectionSession.getUsedDatabaseName()).thenReturn("db");
        ProxyDatabaseConnectionManager databaseConnectionManager = mock(ProxyDatabaseConnectionManager.class);
        when(databaseConnectionManager.getConnectionSession()).thenReturn(connectionSession);
        when(connectionSession.getDatabaseConnectionManager()).thenReturn(databaseConnectionManager);
        when(connectionSession.getProtocolType()).thenReturn(databaseType);
        ContextManager contextManager = mockContextManager();
        when(contextManager.getStateContext().getState()).thenReturn(ShardingSphereState.OK);
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
    }
    
    private ConnectionContext mockConnectionContext() {
        ConnectionContext result = mock(ConnectionContext.class);
        when(result.getCurrentDatabaseName()).thenReturn(Optional.of("db"));
        when(result.getGrantee()).thenReturn(new Grantee(DefaultUser.USERNAME, "%"));
        when(result.getTransactionContext()).thenReturn(mock(TransactionConnectionContext.class));
        return result;
    }
    
    private ContextManager mockContextManager() {
        MetaDataContexts metaDataContexts = mock(MetaDataContexts.class, RETURNS_DEEP_STUBS);
        when(metaDataContexts.getMetaData().getDatabase("db")).thenReturn(new ShardingSphereDatabase("db", mock(), mock(), new RuleMetaData(Collections.emptyList()), Collections.emptyList()));
        when(metaDataContexts.getMetaData().getDatabase("information_schema"))
                .thenReturn(new ShardingSphereDatabase("information_schema", mock(), mock(), new RuleMetaData(Collections.emptyList()), Collections.emptyList()));
        when(metaDataContexts.getMetaData().containsDatabase("db")).thenReturn(true);
        when(metaDataContexts.getMetaData().containsDatabase("information_schema")).thenReturn(true);
        ContextManager result = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(result.getMetaDataContexts()).thenReturn(metaDataContexts);
        when(result.getDatabase("db").getProtocolType()).thenReturn(databaseType);
        when(result.getDatabase("db").containsDataSource()).thenReturn(true);
        when(result.getDatabase("information_schema").getProtocolType()).thenReturn(databaseType);
        when(metaDataContexts.getMetaData().getProps()).thenReturn(new ConfigurationProperties(new Properties()));
        RuleMetaData globalRuleMetaData = new RuleMetaData(Arrays.asList(
                new AuthorityRule(new DefaultAuthorityRuleConfigurationBuilder().build()),
                new SQLParserRule(new DefaultSQLParserRuleConfigurationBuilder().build()),
                new TransactionRule(new DefaultTransactionRuleConfigurationBuilder().build(), Collections.emptyList())));
        when(metaDataContexts.getMetaData().getGlobalRuleMetaData()).thenReturn(globalRuleMetaData);
        return result;
    }
    
    @Test
    void assertNewInstanceWithDistSQL() throws SQLException {
        String sql = "set dist variable sql_show='true'";
        SQLStatement sqlStatement = ProxySQLComQueryParser.parse(sql, databaseType, connectionSession);
        ProxyBackendHandler actual = ProxyBackendHandlerFactory.newInstance(databaseType, sql, sqlStatement, connectionSession, new HintValueContext());
        assertThat(actual, isA(DistSQLUpdateProxyBackendHandler.class));
        sql = "show dist variable where name = sql_show";
        sqlStatement = ProxySQLComQueryParser.parse(sql, databaseType, connectionSession);
        actual = ProxyBackendHandlerFactory.newInstance(databaseType, sql, sqlStatement, connectionSession, new HintValueContext());
        assertThat(actual, isA(DistSQLQueryProxyBackendHandler.class));
        sql = "show dist variables";
        sqlStatement = ProxySQLComQueryParser.parse(sql, databaseType, connectionSession);
        actual = ProxyBackendHandlerFactory.newInstance(databaseType, sql, sqlStatement, connectionSession, new HintValueContext());
        assertThat(actual, isA(DistSQLQueryProxyBackendHandler.class));
    }
    
    @ParameterizedTest(name = "{0}")
    @ArgumentsSource(TCLTestCaseArgumentsProvider.class)
    void assertNewInstanceWithTCL(final String sql, final Class<? extends ProxyBackendHandler> proxyBackendHandlerClass) throws SQLException {
        SQLStatement sqlStatement = ProxySQLComQueryParser.parse(sql, databaseType, connectionSession);
        ProxyBackendHandler actual = ProxyBackendHandlerFactory.newInstance(databaseType, sql, sqlStatement, connectionSession, new HintValueContext());
        assertThat(actual, isA(proxyBackendHandlerClass));
    }
    
    @Test
    void assertNewInstanceWithShow() throws SQLException {
        String sql = "SHOW VARIABLES LIKE '%x%'";
        SQLStatement sqlStatement = ProxySQLComQueryParser.parse(sql, databaseType, connectionSession);
        ProxyBackendHandler actual = ProxyBackendHandlerFactory.newInstance(databaseType, sql, sqlStatement, connectionSession, new HintValueContext());
        assertThat(actual, isA(UnicastDatabaseProxyBackendHandler.class));
        sql = "SHOW VARIABLES WHERE Variable_name ='language'";
        sqlStatement = ProxySQLComQueryParser.parse(sql, databaseType, connectionSession);
        actual = ProxyBackendHandlerFactory.newInstance(databaseType, sql, sqlStatement, connectionSession, new HintValueContext());
        assertThat(actual, isA(UnicastDatabaseProxyBackendHandler.class));
        sql = "SHOW CHARACTER SET";
        sqlStatement = ProxySQLComQueryParser.parse(sql, databaseType, connectionSession);
        actual = ProxyBackendHandlerFactory.newInstance(databaseType, sql, sqlStatement, connectionSession, new HintValueContext());
        assertThat(actual, isA(UnicastDatabaseProxyBackendHandler.class));
        sql = "SHOW COLLATION";
        sqlStatement = ProxySQLComQueryParser.parse(sql, databaseType, connectionSession);
        actual = ProxyBackendHandlerFactory.newInstance(databaseType, sql, sqlStatement, connectionSession, new HintValueContext());
        assertThat(actual, isA(UnicastDatabaseProxyBackendHandler.class));
    }
    
    @Test
    void assertNewInstanceWithAdminQuery() throws SQLException {
        QueryContext queryContext = mock(QueryContext.class);
        SQLStatementContext sqlStatementContext = mock(SQLStatementContext.class);
        ProxyBackendHandler expected = mock(ProxyBackendHandler.class);
        when(queryContext.getSqlStatementContext()).thenReturn(sqlStatementContext);
        when(queryContext.getSql()).thenReturn("sql");
        when(queryContext.getParameters()).thenReturn(Collections.emptyList());
        when(sqlStatementContext.getSqlStatement()).thenReturn(mock(SQLStatement.class));
        when(DatabaseAdminProxyBackendHandlerFactory.newInstance(databaseType, sqlStatementContext, connectionSession, "sql", Collections.emptyList())).thenReturn(Optional.of(expected));
        ProxyBackendHandler actual = ProxyBackendHandlerFactory.newInstance(databaseType, queryContext, connectionSession, false);
        assertThat(actual, is(expected));
    }
    
    @Test
    void assertNewInstanceWithDatabaseNameFromTablesContext() throws SQLException {
        QueryContext queryContext = mock(QueryContext.class);
        SQLStatementContext sqlStatementContext = mock(SQLStatementContext.class, RETURNS_DEEP_STUBS);
        ShardingSphereMetaData metaData = mock(ShardingSphereMetaData.class);
        DatabaseProxyBackendHandler expected = mock(DatabaseProxyBackendHandler.class);
        when(queryContext.getSqlStatementContext()).thenReturn(sqlStatementContext);
        when(queryContext.getMetaData()).thenReturn(metaData);
        when(queryContext.getSql()).thenReturn("sql");
        when(queryContext.getParameters()).thenReturn(Collections.emptyList());
        when(sqlStatementContext.getSqlStatement()).thenReturn(mock(SQLStatement.class));
        when(sqlStatementContext.getTablesContext().getDatabaseName()).thenReturn(Optional.of("db"));
        when(metaData.getDatabase("db")).thenReturn(mock(ShardingSphereDatabase.class));
        try (MockedStatic<ShardingSphereServiceLoader> serviceLoader = mockStatic(ShardingSphereServiceLoader.class);
                MockedStatic<DatabaseProxyBackendHandlerFactory> databaseFactory = mockStatic(DatabaseProxyBackendHandlerFactory.class)) {
            serviceLoader.when(() -> ShardingSphereServiceLoader.getServiceInstances(SQLExecutionChecker.class)).thenReturn(Collections.emptyList());
            databaseFactory.when(() -> DatabaseProxyBackendHandlerFactory.newInstance(queryContext, connectionSession, false)).thenReturn(expected);
            ProxyBackendHandler actual = ProxyBackendHandlerFactory.newInstance(databaseType, queryContext, connectionSession, false);
            assertThat(actual, is(expected));
        }
    }
    
    @Test
    void assertNewInstanceWithNullDatabaseName() throws SQLException {
        QueryContext queryContext = mock(QueryContext.class);
        SQLStatementContext sqlStatementContext = mock(SQLStatementContext.class, RETURNS_DEEP_STUBS);
        DatabaseProxyBackendHandler expected = mock(DatabaseProxyBackendHandler.class);
        when(queryContext.getSqlStatementContext()).thenReturn(sqlStatementContext);
        when(queryContext.getSql()).thenReturn("sql");
        when(queryContext.getParameters()).thenReturn(Collections.emptyList());
        when(sqlStatementContext.getSqlStatement()).thenReturn(mock(SQLStatement.class));
        when(sqlStatementContext.getTablesContext().getDatabaseName()).thenReturn(Optional.empty());
        when(connectionSession.getUsedDatabaseName()).thenReturn(null);
        when(DatabaseAdminProxyBackendHandlerFactory.newInstance(databaseType, sqlStatementContext, connectionSession, "sql", Collections.emptyList())).thenReturn(Optional.empty());
        try (MockedStatic<DatabaseProxyBackendHandlerFactory> databaseFactory = mockStatic(DatabaseProxyBackendHandlerFactory.class)) {
            databaseFactory.when(() -> DatabaseProxyBackendHandlerFactory.newInstance(queryContext, connectionSession, false)).thenReturn(expected);
            ProxyBackendHandler actual = ProxyBackendHandlerFactory.newInstance(databaseType, queryContext, connectionSession, false);
            assertThat(actual, is(expected));
        }
    }
    
    @Test
    void assertNewInstanceWithCreateDatabaseStatement() throws SQLException {
        String sql = "CREATE DATABASE foo_db";
        SQLStatement sqlStatement = ProxySQLComQueryParser.parse(sql, databaseType, connectionSession);
        ProxyBackendHandler actual = ProxyBackendHandlerFactory.newInstance(databaseType, sql, sqlStatement, connectionSession, new HintValueContext());
        assertThat(actual, isA(CreateDatabaseProxyBackendHandler.class));
    }
    
    @Test
    void assertNewInstanceWithDropDatabaseStatement() throws SQLException {
        String sql = "DROP DATABASE foo_db";
        SQLStatement sqlStatement = ProxySQLComQueryParser.parse(sql, databaseType, connectionSession);
        ProxyBackendHandler actual = ProxyBackendHandlerFactory.newInstance(databaseType, sql, sqlStatement, connectionSession, new HintValueContext());
        assertThat(actual, isA(DropDatabaseProxyBackendHandler.class));
    }
    
    @Test
    void assertNewInstanceWithEmptyString() throws SQLException {
        String sql = "";
        ProxyBackendHandler actual = ProxyBackendHandlerFactory.newInstance(databaseType, sql, new EmptyStatement(databaseType), connectionSession, new HintValueContext());
        assertThat(actual, isA(SkipProxyBackendHandler.class));
    }
    
    @Test
    void assertNewInstanceWithEmptyStatementInQueryContext() throws SQLException {
        QueryContext queryContext = mock(QueryContext.class);
        SQLStatementContext sqlStatementContext = mock(SQLStatementContext.class);
        when(queryContext.getSqlStatementContext()).thenReturn(sqlStatementContext);
        when(sqlStatementContext.getSqlStatement()).thenReturn(new EmptyStatement(databaseType));
        ProxyBackendHandler actual = ProxyBackendHandlerFactory.newInstance(databaseType, queryContext, connectionSession, false);
        assertThat(actual, isA(SkipProxyBackendHandler.class));
    }
    
    @Test
    void assertNewInstanceWithUnsupportedStandardSQLStatement() {
        QueryContext queryContext = mock(QueryContext.class);
        SQLStatementContext sqlStatementContext = mock(SQLStatementContext.class);
        when(queryContext.getSqlStatementContext()).thenReturn(sqlStatementContext);
        when(sqlStatementContext.getSqlStatement()).thenReturn(new RenameTableStatement(databaseType, Collections.emptyList()));
        assertThrows(UnsupportedSQLOperationException.class, () -> ProxyBackendHandlerFactory.newInstance(databaseType, queryContext, connectionSession, false));
    }
    
    @Test
    void assertNewInstanceWhenTransactionFailedAndStatementIsNotCommitOrRollback() {
        when(connectionSession.getConnectionContext().getTransactionContext().isExceptionOccur()).thenReturn(true);
        String sql = "SELECT 1";
        SQLStatement sqlStatement = ProxySQLComQueryParser.parse(sql, postgreSQLDatabaseType, connectionSession);
        assertThrows(SQLFeatureNotSupportedException.class,
                () -> ProxyBackendHandlerFactory.newInstance(postgreSQLDatabaseType, sql, sqlStatement, connectionSession, new HintValueContext()));
    }
    
    @Test
    void assertNewInstanceWhenTransactionFailedAndStatementIsCommit() throws SQLException {
        when(connectionSession.getConnectionContext().getTransactionContext().isExceptionOccur()).thenReturn(true);
        String sql = "COMMIT";
        SQLStatement sqlStatement = ProxySQLComQueryParser.parse(sql, postgreSQLDatabaseType, connectionSession);
        ProxyBackendHandler actual = ProxyBackendHandlerFactory.newInstance(postgreSQLDatabaseType, sql, sqlStatement, connectionSession, new HintValueContext());
        assertThat(actual, isA(CommitProxyBackendHandler.class));
    }
    
    @Test
    void assertNewInstanceWhenTransactionFailedAndStatementIsRollback() throws SQLException {
        when(connectionSession.getConnectionContext().getTransactionContext().isExceptionOccur()).thenReturn(true);
        String sql = "ROLLBACK";
        SQLStatement sqlStatement = ProxySQLComQueryParser.parse(sql, postgreSQLDatabaseType, connectionSession);
        ProxyBackendHandler actual = ProxyBackendHandlerFactory.newInstance(postgreSQLDatabaseType, sql, sqlStatement, connectionSession, new HintValueContext());
        assertThat(actual, isA(RollbackProxyBackendHandler.class));
    }
    
    @Test
    void assertNewInstanceWhenTransactionFailedButDatabaseDoesNotRestrictStatement() throws SQLException {
        when(connectionSession.getConnectionContext().getTransactionContext().isExceptionOccur()).thenReturn(true);
        String sql = "COMMIT";
        SQLStatement sqlStatement = ProxySQLComQueryParser.parse(sql, databaseType, connectionSession);
        ProxyBackendHandler actual = ProxyBackendHandlerFactory.newInstance(databaseType, sql, sqlStatement, connectionSession, new HintValueContext());
        assertThat(actual, isA(CommitProxyBackendHandler.class));
    }
    
    @Test
    void assertNewInstanceWithReadOnlyClusterState() throws SQLException {
        when(ProxyContext.getInstance().getContextManager().getStateContext().getState()).thenReturn(ShardingSphereState.READ_ONLY);
        String sql = "SHOW VARIABLES";
        SQLStatement sqlStatement = ProxySQLComQueryParser.parse(sql, databaseType, connectionSession);
        ProxyBackendHandler actual = ProxyBackendHandlerFactory.newInstance(databaseType, sql, sqlStatement, connectionSession, new HintValueContext());
        assertThat(actual, isA(UnicastDatabaseProxyBackendHandler.class));
    }
    
    @Test
    void assertNewInstanceWithUnsupportedNonQueryDistSQLInTransaction() {
        when(connectionSession.getTransactionStatus().isInTransaction()).thenReturn(true);
        String sql = "CREATE SHARDING TABLE RULE t_order (STORAGE_UNITS(ms_group_0,ms_group_1), SHARDING_COLUMN=order_id, TYPE(NAME='hash_mod', PROPERTIES('sharding-count'='4')));";
        SQLStatement sqlStatement = ProxySQLComQueryParser.parse(sql, databaseType, connectionSession);
        assertThrows(UnsupportedSQLOperationException.class, () -> ProxyBackendHandlerFactory.newInstance(databaseType, sql, sqlStatement, connectionSession, new HintValueContext()));
    }
    
    @Test
    void assertNewInstanceWithQueryableRALStatementInTransaction() throws SQLException {
        when(connectionSession.getTransactionStatus().isInTransaction()).thenReturn(true);
        String sql = "SHOW DIST VARIABLES";
        SQLStatement sqlStatement = ProxySQLComQueryParser.parse(sql, databaseType, connectionSession);
        ProxyBackendHandler actual = ProxyBackendHandlerFactory.newInstance(databaseType, sql, sqlStatement, connectionSession, new HintValueContext());
        assertThat(actual, isA(DistSQLQueryProxyBackendHandler.class));
    }
    
    @Test
    void assertNewInstanceWithRQLStatementInTransaction() throws SQLException {
        when(connectionSession.getTransactionStatus().isInTransaction()).thenReturn(true);
        String sql = "SHOW DEFAULT SINGLE TABLE STORAGE UNIT";
        SQLStatement sqlStatement = ProxySQLComQueryParser.parse(sql, databaseType, connectionSession);
        ProxyBackendHandler actual = ProxyBackendHandlerFactory.newInstance(databaseType, sql, sqlStatement, connectionSession, new HintValueContext());
        assertThat(actual, isA(DistSQLQueryProxyBackendHandler.class));
    }
    
    @Test
    void assertNewInstanceWithRULStatementInTransaction() throws SQLException {
        when(connectionSession.getTransactionStatus().isInTransaction()).thenReturn(true);
        String sql = "PREVIEW INSERT INTO account VALUES(1, 1, 1)";
        SQLStatement sqlStatement = ProxySQLComQueryParser.parse(sql, databaseType, connectionSession);
        sqlStatement.buildAttributes();
        ProxyBackendHandler actual = ProxyBackendHandlerFactory.newInstance(databaseType, sql, sqlStatement, connectionSession, new HintValueContext());
        assertThat(actual, isA(DistSQLQueryProxyBackendHandler.class));
    }
    
    private static final class TCLTestCaseArgumentsProvider implements ArgumentsProvider {
        
        @Override
        public Stream<? extends Arguments> provideArguments(final ParameterDeclarations parameters, final ExtensionContext context) {
            return Stream.of(
                    Arguments.of("BEGIN", BeginTransactionProxyBackendHandler.class),
                    Arguments.of("START TRANSACTION", BeginTransactionProxyBackendHandler.class),
                    Arguments.of("SET AUTOCOMMIT=0", SetAutoCommitProxyBackendHandler.class),
                    Arguments.of("SET @@SESSION.AUTOCOMMIT = OFF", SetAutoCommitProxyBackendHandler.class),
                    Arguments.of("SET AUTOCOMMIT=1", SetAutoCommitProxyBackendHandler.class),
                    Arguments.of("SET @@SESSION.AUTOCOMMIT = ON", SetAutoCommitProxyBackendHandler.class),
                    Arguments.of("COMMIT", CommitProxyBackendHandler.class),
                    Arguments.of("ROLLBACK", RollbackProxyBackendHandler.class),
                    Arguments.of("SAVEPOINT foo_point", SetSavepointProxyBackendHandler.class),
                    Arguments.of("RELEASE SAVEPOINT foo_point", ReleaseSavepointProxyBackendHandler.class),
                    Arguments.of("ROLLBACK TO foo_point", RollbackSavepointProxyBackendHandler.class),
                    Arguments.of("SET TRANSACTION READ ONLY, ISOLATION LEVEL REPEATABLE READ", SetTransactionProxyBackendHandler.class));
        }
    }
}
