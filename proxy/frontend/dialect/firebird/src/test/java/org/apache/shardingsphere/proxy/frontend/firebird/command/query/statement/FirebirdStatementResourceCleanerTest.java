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

package org.apache.shardingsphere.proxy.frontend.firebird.command.query.statement;

import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.connector.ProxyDatabaseConnectionManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.exception.BackendConnectionException;
import org.apache.shardingsphere.proxy.backend.handler.ProxyBackendHandler;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.statement.fetch.FirebirdFetchStatementCache;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ProxyContext.class)
class FirebirdStatementResourceCleanerTest {
    
    private static final int CONNECTION_ID = 1;
    
    private static final int STATEMENT_ID = 2;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ConnectionSession connectionSession;
    
    @Mock
    private ProxyDatabaseConnectionManager connectionManager;
    
    @Mock
    private ConnectionContext connectionContext;
    
    @Mock
    private ProxyBackendHandler proxyBackendHandler;
    
    @AfterEach
    void tearDown() {
        FirebirdFetchStatementCache.getInstance().unregisterStatement(CONNECTION_ID, STATEMENT_ID);
        FirebirdFetchStatementCache.getInstance().unregisterConnection(CONNECTION_ID);
    }
    
    @Test
    void assertCreatePreparedStatementCacheKey() {
        assertThat(FirebirdStatementResourceCleaner.createPreparedStatementCacheKey(STATEMENT_ID).getToken(), is("firebird:2"));
    }
    
    @Test
    void assertCleanWithPreparedStatementCacheInvalidation() throws SQLException {
        FirebirdFetchStatementCache.getInstance().registerConnection(CONNECTION_ID);
        FirebirdFetchStatementCache.getInstance().registerStatement(CONNECTION_ID, STATEMENT_ID, proxyBackendHandler);
        when(connectionSession.getConnectionId()).thenReturn(CONNECTION_ID);
        when(connectionSession.getDatabaseConnectionManager()).thenReturn(connectionManager);
        when(connectionSession.getConnectionContext()).thenReturn(connectionContext);
        FirebirdStatementResourceCleaner.clean(connectionSession, STATEMENT_ID, true);
        InOrder inOrder = inOrder(connectionManager, connectionContext, proxyBackendHandler, connectionSession);
        inOrder.verify(connectionManager).removeResource(proxyBackendHandler);
        inOrder.verify(connectionContext).clearCursorContext();
        inOrder.verify(proxyBackendHandler).close();
        inOrder.verify(connectionSession).invalidatePreparedStatementCache(FirebirdStatementResourceCleaner.createPreparedStatementCacheKey(STATEMENT_ID));
        assertNull(FirebirdFetchStatementCache.getInstance().getFetchBackendHandler(CONNECTION_ID, STATEMENT_ID));
    }
    
    @Test
    void assertCleanWithPreparedStatementCacheInvalidationWithoutFetchHandler() throws SQLException {
        when(connectionSession.getConnectionId()).thenReturn(CONNECTION_ID);
        when(connectionSession.getConnectionContext()).thenReturn(connectionContext);
        FirebirdStatementResourceCleaner.clean(connectionSession, STATEMENT_ID, true);
        verify(connectionContext).clearCursorContext();
        verify(connectionSession).invalidatePreparedStatementCache(FirebirdStatementResourceCleaner.createPreparedStatementCacheKey(STATEMENT_ID));
        verifyNoInteractions(connectionManager, proxyBackendHandler);
    }
    
    @Test
    void assertCleanWithoutPreparedStatementCacheInvalidation() throws SQLException {
        FirebirdFetchStatementCache.getInstance().registerConnection(CONNECTION_ID);
        FirebirdFetchStatementCache.getInstance().registerStatement(CONNECTION_ID, STATEMENT_ID, proxyBackendHandler);
        when(connectionSession.getConnectionId()).thenReturn(CONNECTION_ID);
        when(connectionSession.getDatabaseConnectionManager()).thenReturn(connectionManager);
        when(connectionSession.getConnectionContext()).thenReturn(connectionContext);
        FirebirdStatementResourceCleaner.clean(connectionSession, STATEMENT_ID, false);
        verify(connectionManager).removeResource(proxyBackendHandler);
        verify(proxyBackendHandler).close();
        verify(connectionContext).clearCursorContext();
        verify(connectionSession, never()).invalidatePreparedStatementCache(any());
        assertNull(FirebirdFetchStatementCache.getInstance().getFetchBackendHandler(CONNECTION_ID, STATEMENT_ID));
    }
    
    @Test
    void assertCleanBeforeCloseExecutionResources() throws SQLException, BackendConnectionException {
        ConnectionSession actualConnectionSession = mock(ConnectionSession.class, Answers.RETURNS_DEEP_STUBS);
        ConnectionContext actualConnectionContext = mock(ConnectionContext.class, Answers.RETURNS_DEEP_STUBS);
        ContextManager contextManager = mock(ContextManager.class, Answers.RETURNS_DEEP_STUBS);
        ProxyBackendHandler actualProxyBackendHandler = mock(ProxyBackendHandler.class);
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        when(contextManager.getMetaDataContexts().getMetaData().getGlobalRuleMetaData().getRules()).thenReturn(Collections.emptyList());
        when(actualConnectionSession.getConnectionId()).thenReturn(CONNECTION_ID);
        when(actualConnectionSession.getConnectionContext()).thenReturn(actualConnectionContext);
        when(actualConnectionContext.getTransactionContext().getTransactionType()).thenReturn(Optional.of("XA"));
        when(actualConnectionSession.getTransactionStatus().isInConnectionHeldTransaction(any())).thenReturn(true);
        ProxyDatabaseConnectionManager actualConnectionManager = new ProxyDatabaseConnectionManager(actualConnectionSession);
        when(actualConnectionSession.getDatabaseConnectionManager()).thenReturn(actualConnectionManager);
        FirebirdFetchStatementCache.getInstance().registerConnection(CONNECTION_ID);
        FirebirdFetchStatementCache.getInstance().registerStatement(CONNECTION_ID, STATEMENT_ID, actualProxyBackendHandler);
        actualConnectionManager.add(actualProxyBackendHandler);
        actualConnectionManager.markResourceInUse(actualProxyBackendHandler);
        final AtomicBoolean closed = new AtomicBoolean(false);
        org.mockito.Mockito.doAnswer(invocation -> {
            if (closed.getAndSet(true)) {
                throw new SQLException("close twice");
            }
            return null;
        }).when(actualProxyBackendHandler).close();
        FirebirdStatementResourceCleaner.clean(actualConnectionSession, STATEMENT_ID, true);
        actualConnectionManager.closeExecutionResources();
        verify(actualProxyBackendHandler, times(1)).close();
        verify(actualConnectionSession).invalidatePreparedStatementCache(FirebirdStatementResourceCleaner.createPreparedStatementCacheKey(STATEMENT_ID));
        assertNull(FirebirdFetchStatementCache.getInstance().getFetchBackendHandler(CONNECTION_ID, STATEMENT_ID));
    }
}
