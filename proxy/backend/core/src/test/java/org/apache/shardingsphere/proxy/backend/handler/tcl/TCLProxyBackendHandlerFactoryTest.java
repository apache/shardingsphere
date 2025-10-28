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

package org.apache.shardingsphere.proxy.backend.handler.tcl;

import lombok.SneakyThrows;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.session.connection.transaction.TransactionConnectionContext;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.connector.DatabaseProxyConnector;
import org.apache.shardingsphere.proxy.backend.connector.DatabaseProxyConnectorFactory;
import org.apache.shardingsphere.proxy.backend.connector.ProxyDatabaseConnectionManager;
import org.apache.shardingsphere.proxy.backend.connector.jdbc.transaction.ProxyBackendTransactionManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.handler.ProxyBackendHandler;
import org.apache.shardingsphere.proxy.backend.handler.tcl.local.type.CommitProxyBackendHandler;
import org.apache.shardingsphere.proxy.backend.handler.tcl.local.type.RollbackProxyBackendHandler;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.CommitStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.RollbackStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.TCLStatement;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.apache.shardingsphere.transaction.rule.TransactionRule;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.internal.configuration.plugins.Plugins;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings({ProxyContext.class, DatabaseProxyConnectorFactory.class})
class TCLProxyBackendHandlerFactoryTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @Test
    void assertTCLProxyBackendHandlerReturnedWhenTCLStatementInstanceOfCommitStatement() {
        ConnectionSession connectionSession = mock(ConnectionSession.class, Answers.RETURNS_DEEP_STUBS);
        ProxyDatabaseConnectionManager databaseConnectionManager = mock(ProxyDatabaseConnectionManager.class);
        when(connectionSession.getDatabaseConnectionManager()).thenReturn(databaseConnectionManager);
        when(connectionSession.getProtocolType()).thenReturn(databaseType);
        when(databaseConnectionManager.getConnectionSession()).thenReturn(connectionSession);
        when(databaseConnectionManager.getConnectionSession().getConnectionContext().getTransactionContext()).thenReturn(new TransactionConnectionContext());
        ContextManager contextManager = mockContextManager();
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        QueryContext queryContext = mock(QueryContext.class, RETURNS_DEEP_STUBS);
        when(queryContext.getSqlStatementContext().getSqlStatement()).thenReturn(new CommitStatement(databaseType));
        ProxyBackendHandler proxyBackendHandler = TCLProxyBackendHandlerFactory.newInstance(queryContext, connectionSession);
        assertThat(proxyBackendHandler, isA(CommitProxyBackendHandler.class));
        CommitProxyBackendHandler backendHandler = (CommitProxyBackendHandler) proxyBackendHandler;
        assertFieldOfInstance(getTransactionManager(backendHandler), "connection", is(databaseConnectionManager));
    }
    
    @Test
    void assertTCLProxyBackendHandlerReturnedWhenTCLStatementInstanceOfRollbackStatement() {
        ConnectionSession connectionSession = mock(ConnectionSession.class, Answers.RETURNS_DEEP_STUBS);
        ProxyDatabaseConnectionManager databaseConnectionManager = mock(ProxyDatabaseConnectionManager.class);
        when(connectionSession.getDatabaseConnectionManager()).thenReturn(databaseConnectionManager);
        when(connectionSession.getProtocolType()).thenReturn(databaseType);
        when(databaseConnectionManager.getConnectionSession()).thenReturn(connectionSession);
        when(databaseConnectionManager.getConnectionSession().getConnectionContext().getTransactionContext()).thenReturn(new TransactionConnectionContext());
        when(connectionSession.getDatabaseConnectionManager().getConnectionSession().getConnectionContext().getTransactionContext()).thenReturn(new TransactionConnectionContext());
        ContextManager contextManager = mockContextManager();
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        QueryContext queryContext = mock(QueryContext.class, RETURNS_DEEP_STUBS);
        when(queryContext.getSqlStatementContext().getSqlStatement()).thenReturn(new RollbackStatement(databaseType));
        ProxyBackendHandler proxyBackendHandler = TCLProxyBackendHandlerFactory.newInstance(queryContext, connectionSession);
        assertThat(proxyBackendHandler, isA(RollbackProxyBackendHandler.class));
        RollbackProxyBackendHandler backendHandler = (RollbackProxyBackendHandler) proxyBackendHandler;
        assertFieldOfInstance(getTransactionManager(backendHandler), "connection", is(databaseConnectionManager));
    }
    
    private ContextManager mockContextManager() {
        ContextManager result = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(result.getMetaDataContexts().getMetaData().getGlobalRuleMetaData()).thenReturn(new RuleMetaData(Collections.singleton(mock(TransactionRule.class))));
        return result;
    }
    
    @Test
    void assertBroadcastProxyBackendHandlerReturnedWhenTCLStatementNotHit() {
        SQLStatementContext sqlStatementContext = mock(SQLStatementContext.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.getTablesContext().getDatabaseNames()).thenReturn(Collections.emptyList());
        when(sqlStatementContext.getSqlStatement()).thenReturn(mock(TCLStatement.class));
        when(DatabaseProxyConnectorFactory.newInstance(any(QueryContext.class), nullable(ProxyDatabaseConnectionManager.class), anyBoolean())).thenReturn(mock(DatabaseProxyConnector.class));
        ShardingSphereMetaData metaData = mock(ShardingSphereMetaData.class);
        when(ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData()).thenReturn(metaData);
        QueryContext queryContext = mock(QueryContext.class);
        when(queryContext.getSqlStatementContext()).thenReturn(sqlStatementContext);
        assertThat(TCLProxyBackendHandlerFactory.newInstance(queryContext, mock(ConnectionSession.class)), isA(DatabaseProxyConnector.class));
    }
    
    @SuppressWarnings("unchecked")
    @SneakyThrows(ReflectiveOperationException.class)
    private <S, T> void assertFieldOfInstance(final S classInstance, final String fieldName, final Matcher<T> matcher) {
        T value = (T) Plugins.getMemberAccessor().get(classInstance.getClass().getDeclaredField(fieldName), classInstance);
        assertThat(value, matcher);
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private ProxyBackendTransactionManager getTransactionManager(final ProxyBackendHandler backendHandler) {
        return (ProxyBackendTransactionManager) Plugins.getMemberAccessor().get(backendHandler.getClass().getDeclaredField("transactionManager"), backendHandler);
    }
}
