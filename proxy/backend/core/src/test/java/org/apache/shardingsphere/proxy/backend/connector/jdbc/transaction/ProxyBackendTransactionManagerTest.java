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

package org.apache.shardingsphere.proxy.backend.connector.jdbc.transaction;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.apache.shardingsphere.infra.session.connection.transaction.TransactionConnectionContext;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.connector.ProxyDatabaseConnectionManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.session.transaction.TransactionStatus;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.apache.shardingsphere.transaction.ShardingSphereTransactionManagerEngine;
import org.apache.shardingsphere.transaction.api.TransactionType;
import org.apache.shardingsphere.transaction.rule.TransactionRule;
import org.apache.shardingsphere.transaction.spi.ShardingSphereDistributedTransactionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.internal.configuration.plugins.Plugins;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ProxyContext.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ProxyBackendTransactionManagerTest {
    
    @Mock
    private ConnectionSession connectionSession;
    
    @Mock
    private ProxyDatabaseConnectionManager databaseConnectionManager;
    
    @Mock
    private TransactionStatus transactionStatus;
    
    @Mock
    private LocalTransactionManager localTransactionManager;
    
    @Mock
    private ShardingSphereDistributedTransactionManager distributedTransactionManager;
    
    @Mock
    private Connection connection;
    
    private ProxyBackendTransactionManager transactionManager;
    
    @BeforeEach
    void setUp() {
        when(connectionSession.getTransactionStatus()).thenReturn(transactionStatus);
        when(databaseConnectionManager.getConnectionSession()).thenReturn(connectionSession);
        when(databaseConnectionManager.getCachedConnections()).thenReturn(mockCachedConnections());
        ConnectionContext connectionContext = mock(ConnectionContext.class);
        when(connectionSession.getConnectionContext()).thenReturn(connectionContext);
        TransactionConnectionContext context = new TransactionConnectionContext();
        when(connectionContext.getTransactionContext()).thenReturn(context);
    }
    
    private Multimap<String, Connection> mockCachedConnections() {
        Multimap<String, Connection> result = HashMultimap.create();
        result.putAll("ds1", Collections.singleton(connection));
        return result;
    }
    
    @Test
    void assertBeginForLocalTransaction() {
        ContextManager contextManager = mockContextManager(TransactionType.LOCAL);
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        newTransactionManager(TransactionType.LOCAL, false);
        transactionManager.begin();
        verify(transactionStatus).setInTransaction(true);
        verify(databaseConnectionManager).closeHandlers(true);
        verify(databaseConnectionManager).closeConnections(false);
        verify(localTransactionManager).begin();
    }
    
    @Test
    void assertBeginForDistributedTransaction() {
        ContextManager contextManager = mockContextManager(TransactionType.XA);
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        newTransactionManager(TransactionType.XA, true);
        transactionManager.begin();
        verify(transactionStatus, never()).setInTransaction(true);
        verify(databaseConnectionManager, times(1)).closeConnections(false);
        verify(distributedTransactionManager).begin();
    }
    
    @Test
    void assertCommitForLocalTransaction() throws SQLException {
        ContextManager contextManager = mockContextManager(TransactionType.LOCAL);
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        newTransactionManager(TransactionType.LOCAL, true);
        transactionManager.commit();
        verify(transactionStatus).setInTransaction(false);
        verify(localTransactionManager).commit();
    }
    
    @Test
    void assertCommitForDistributedTransaction() throws SQLException {
        ContextManager contextManager = mockContextManager(TransactionType.XA);
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        newTransactionManager(TransactionType.XA, true);
        transactionManager.commit();
        verify(transactionStatus).setInTransaction(false);
        verify(distributedTransactionManager).commit(false);
    }
    
    @Test
    void assertCommitWithoutTransaction() throws SQLException {
        ContextManager contextManager = mockContextManager(TransactionType.LOCAL);
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        newTransactionManager(TransactionType.LOCAL, false);
        transactionManager.commit();
        verify(transactionStatus, never()).setInTransaction(false);
        verify(localTransactionManager, never()).commit();
        verify(distributedTransactionManager, never()).commit(false);
    }
    
    @Test
    void assertRollbackForLocalTransaction() throws SQLException {
        ContextManager contextManager = mockContextManager(TransactionType.LOCAL);
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        newTransactionManager(TransactionType.LOCAL, true);
        transactionManager.rollback();
        verify(transactionStatus).setInTransaction(false);
        verify(localTransactionManager).rollback();
    }
    
    @Test
    void assertRollbackForDistributedTransaction() throws SQLException {
        ContextManager contextManager = mockContextManager(TransactionType.XA);
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        newTransactionManager(TransactionType.XA, true);
        transactionManager.rollback();
        verify(transactionStatus).setInTransaction(false);
        verify(distributedTransactionManager).rollback();
    }
    
    @Test
    void assertRollbackWithoutTransaction() throws SQLException {
        ContextManager contextManager = mockContextManager(TransactionType.LOCAL);
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        newTransactionManager(TransactionType.LOCAL, false);
        transactionManager.rollback();
        verify(transactionStatus, never()).setInTransaction(false);
        verify(localTransactionManager, never()).rollback();
        verify(distributedTransactionManager, never()).rollback();
    }
    
    private void newTransactionManager(final TransactionType transactionType, final boolean inTransaction) {
        when(ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getGlobalRuleMetaData().getSingleRule(TransactionRule.class).getDefaultType())
                .thenReturn(transactionType);
        when(transactionStatus.isInTransaction()).thenReturn(inTransaction);
        transactionManager = new ProxyBackendTransactionManager(databaseConnectionManager);
        setLocalTransactionManager();
        setTransactionHooks();
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private void setLocalTransactionManager() {
        Plugins.getMemberAccessor().set(ProxyBackendTransactionManager.class.getDeclaredField("localTransactionManager"), transactionManager, localTransactionManager);
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private void setTransactionHooks() {
        Plugins.getMemberAccessor().set(ProxyBackendTransactionManager.class.getDeclaredField("transactionHooks"), transactionManager, Collections.emptyMap());
    }
    
    private ContextManager mockContextManager(final TransactionType transactionType) {
        ContextManager result = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        RuleMetaData globalRuleMetaData = mockGlobalRuleMetaData(transactionType);
        when(result.getMetaDataContexts().getMetaData().getGlobalRuleMetaData()).thenReturn(globalRuleMetaData);
        return result;
    }
    
    private RuleMetaData mockGlobalRuleMetaData(final TransactionType transactionType) {
        ShardingSphereTransactionManagerEngine transactionManagerEngine = mock(ShardingSphereTransactionManagerEngine.class);
        when(transactionManagerEngine.getTransactionManager(TransactionType.XA)).thenReturn(distributedTransactionManager);
        TransactionRule transactionRule = mock(TransactionRule.class);
        when(transactionRule.getDefaultType()).thenReturn(transactionType);
        when(transactionRule.getResource()).thenReturn(transactionManagerEngine);
        return new RuleMetaData(Collections.singleton(transactionRule));
    }
}
