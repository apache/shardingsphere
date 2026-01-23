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

package org.apache.shardingsphere.proxy.backend.handler.tcl.local;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.apache.shardingsphere.infra.session.connection.transaction.TransactionConnectionContext;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.proxy.backend.connector.DatabaseProxyConnector;
import org.apache.shardingsphere.proxy.backend.connector.DatabaseProxyConnectorFactory;
import org.apache.shardingsphere.proxy.backend.connector.ProxyDatabaseConnectionManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.handler.ProxyBackendHandler;
import org.apache.shardingsphere.proxy.backend.handler.tcl.local.type.BeginTransactionProxyBackendHandler;
import org.apache.shardingsphere.proxy.backend.handler.tcl.local.type.CommitProxyBackendHandler;
import org.apache.shardingsphere.proxy.backend.handler.tcl.local.type.ReleaseSavepointProxyBackendHandler;
import org.apache.shardingsphere.proxy.backend.handler.tcl.local.type.RollbackProxyBackendHandler;
import org.apache.shardingsphere.proxy.backend.handler.tcl.local.type.RollbackSavepointProxyBackendHandler;
import org.apache.shardingsphere.proxy.backend.handler.tcl.local.type.SetAutoCommitProxyBackendHandler;
import org.apache.shardingsphere.proxy.backend.handler.tcl.local.type.SetSavepointProxyBackendHandler;
import org.apache.shardingsphere.proxy.backend.handler.tcl.local.type.SetTransactionProxyBackendHandler;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.session.transaction.TransactionStatus;
import org.apache.shardingsphere.sql.parser.statement.core.enums.OperationScope;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.BeginTransactionStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.CommitStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.ReleaseSavepointStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.RollbackStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.SavepointStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.SetAutoCommitStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.SetTransactionStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.StartTransactionStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.TCLStatement;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.apache.shardingsphere.transaction.rule.TransactionRule;
import org.apache.shardingsphere.transaction.rule.builder.DefaultTransactionRuleConfigurationBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Answers;
import org.mockito.Mock;

import java.util.Collections;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings({DatabaseProxyConnectorFactory.class, ProxyContext.class})
class LocalTCLProxyBackendHandlerFactoryTest {
    
    private static final DatabaseType DATABASE_TYPE = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ConnectionSession connectionSession;
    
    @Mock
    private ProxyDatabaseConnectionManager databaseConnectionManager;
    
    @BeforeEach
    void setUp() {
        when(connectionSession.getDatabaseConnectionManager()).thenReturn(databaseConnectionManager);
        lenient().when(databaseConnectionManager.getConnectionSession()).thenReturn(connectionSession);
        when(connectionSession.getProtocolType()).thenReturn(DATABASE_TYPE);
        when(connectionSession.getTransactionStatus()).thenReturn(new TransactionStatus());
        ConnectionContext connectionContext = mock(ConnectionContext.class);
        lenient().when(connectionContext.getTransactionContext()).thenReturn(new TransactionConnectionContext());
        when(connectionSession.getConnectionContext()).thenReturn(connectionContext);
        ContextManager contextManager = mockContextManager();
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
    }
    
    private ContextManager mockContextManager() {
        ContextManager result = mock(ContextManager.class);
        MetaDataContexts metaDataContexts = mock(MetaDataContexts.class, RETURNS_DEEP_STUBS);
        TransactionRule transactionRule = new TransactionRule(new DefaultTransactionRuleConfigurationBuilder().build(), Collections.emptyList());
        RuleMetaData globalRuleMetaData = new RuleMetaData(Collections.singleton(transactionRule));
        lenient().when(metaDataContexts.getMetaData().getGlobalRuleMetaData()).thenReturn(globalRuleMetaData);
        lenient().when(result.getMetaDataContexts()).thenReturn(metaDataContexts);
        return result;
    }
    
    @Test
    void assertNewInstanceReturnConnectorWhenNotMatched() {
        TCLStatement sqlStatement = new SetTransactionStatement(DATABASE_TYPE, OperationScope.GLOBAL, null, null);
        QueryContext queryContext = mockQueryContext(sqlStatement);
        DatabaseProxyConnector expected = mock(DatabaseProxyConnector.class);
        when(DatabaseProxyConnectorFactory.newInstance(queryContext, databaseConnectionManager, false)).thenReturn(expected);
        assertThat(LocalTCLProxyBackendHandlerFactory.newInstance(queryContext, connectionSession), is(expected));
    }
    
    @Test
    void assertNewInstanceReturnConnectorWhenUnknownStatement() {
        TCLStatement sqlStatement = new TCLStatement(DATABASE_TYPE) {
        };
        QueryContext queryContext = mockQueryContext(sqlStatement);
        DatabaseProxyConnector expected = mock(DatabaseProxyConnector.class);
        when(DatabaseProxyConnectorFactory.newInstance(queryContext, databaseConnectionManager, false)).thenReturn(expected);
        assertThat(LocalTCLProxyBackendHandlerFactory.newInstance(queryContext, connectionSession), is(expected));
    }
    
    @ParameterizedTest
    @MethodSource("assertNewInstanceArguments")
    void assertNewInstance(final TCLStatement sqlStatement, final Class<? extends ProxyBackendHandler> expectedClass) {
        assertThat(LocalTCLProxyBackendHandlerFactory.newInstance(mockQueryContext(sqlStatement), connectionSession), isA(expectedClass));
    }
    
    private QueryContext mockQueryContext(final TCLStatement sqlStatement) {
        QueryContext result = mock(QueryContext.class, RETURNS_DEEP_STUBS);
        when(result.getSqlStatementContext().getSqlStatement()).thenReturn(sqlStatement);
        return result;
    }
    
    private static Stream<Arguments> assertNewInstanceArguments() {
        return Stream.of(
                Arguments.of(new BeginTransactionStatement(DATABASE_TYPE), BeginTransactionProxyBackendHandler.class),
                Arguments.of(new StartTransactionStatement(DATABASE_TYPE), BeginTransactionProxyBackendHandler.class),
                Arguments.of(new SetAutoCommitStatement(DATABASE_TYPE, false), SetAutoCommitProxyBackendHandler.class),
                Arguments.of(new CommitStatement(DATABASE_TYPE), CommitProxyBackendHandler.class),
                Arguments.of(new RollbackStatement(DATABASE_TYPE, "sp"), RollbackSavepointProxyBackendHandler.class),
                Arguments.of(new RollbackStatement(DATABASE_TYPE), RollbackProxyBackendHandler.class),
                Arguments.of(new SavepointStatement(DATABASE_TYPE, "sp"), SetSavepointProxyBackendHandler.class),
                Arguments.of(new ReleaseSavepointStatement(DATABASE_TYPE, "sp"), ReleaseSavepointProxyBackendHandler.class),
                Arguments.of(new SetTransactionStatement(DATABASE_TYPE, OperationScope.SESSION, null, null), SetTransactionProxyBackendHandler.class));
    }
}
