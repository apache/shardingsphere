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

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import lombok.SneakyThrows;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.apache.shardingsphere.infra.session.connection.transaction.TransactionConnectionContext;
import org.apache.shardingsphere.infra.spi.type.ordered.OrderedSPILoader;
import org.apache.shardingsphere.mode.exclusive.ExclusiveOperatorEngine;
import org.apache.shardingsphere.mode.exclusive.callback.ExclusiveOperationVoidCallback;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.connector.ProxyDatabaseConnectionManager;
import org.apache.shardingsphere.proxy.backend.connector.jdbc.connection.ConnectionPostProcessor;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.session.transaction.TransactionStatus;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.apache.shardingsphere.transaction.ShardingSphereTransactionManagerEngine;
import org.apache.shardingsphere.transaction.api.TransactionType;
import org.apache.shardingsphere.transaction.rule.TransactionRule;
import org.apache.shardingsphere.transaction.savepoint.ConnectionSavepointManager;
import org.apache.shardingsphere.transaction.spi.ShardingSphereDistributedTransactionManager;
import org.apache.shardingsphere.transaction.spi.TransactionHook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.internal.configuration.plugins.Plugins;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings({ProxyContext.class, OrderedSPILoader.class, ConnectionSavepointManager.class})
@MockitoSettings(strictness = Strictness.LENIENT)
class ProxyBackendTransactionManagerTest {
    
    private final TransactionConnectionContext transactionContext = new TransactionConnectionContext();
    
    private final Collection<ConnectionPostProcessor> connectionPostProcessors = new LinkedList<>();
    
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
    
    @Mock
    private ConnectionContext connectionContext;
    
    @Mock
    private ExclusiveOperatorEngine exclusiveOperatorEngine;
    
    @BeforeEach
    void setUp() {
        ConnectionSession connectionSession = mock(ConnectionSession.class);
        when(connectionSession.getTransactionStatus()).thenReturn(transactionStatus);
        when(databaseConnectionManager.getConnectionSession()).thenReturn(connectionSession);
        when(connectionSession.getConnectionContext()).thenReturn(connectionContext);
        when(connectionContext.getTransactionContext()).thenReturn(transactionContext);
        when(databaseConnectionManager.getConnectionPostProcessors()).thenReturn(connectionPostProcessors);
        when(databaseConnectionManager.getCachedConnections()).thenReturn(mockCachedConnections(connection));
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    @Test
    void assertConstructorUseTransactionManagerFromContext() {
        transactionContext.beginTransaction(TransactionType.XA.name(), distributedTransactionManager);
        mockProxyContext(TransactionType.XA, null, Collections.emptyMap());
        ProxyBackendTransactionManager transactionManager = new ProxyBackendTransactionManager(databaseConnectionManager);
        assertThat(Plugins.getMemberAccessor().get(ProxyBackendTransactionManager.class.getDeclaredField("distributedTransactionManager"), transactionManager), is(distributedTransactionManager));
    }
    
    @Test
    void assertBeginLocalWhenTransactionNotStarted() {
        TransactionHook transactionHook = mock(TransactionHook.class);
        mockProxyContext(TransactionType.LOCAL, null, Collections.singletonMap(mock(ShardingSphereRule.class), transactionHook));
        ProxyBackendTransactionManager transactionManager = new ProxyBackendTransactionManager(databaseConnectionManager);
        setLocalTransactionManager(transactionManager);
        transactionManager.begin();
        verify(databaseConnectionManager).closeHandlers(true);
        verify(databaseConnectionManager).closeConnections(false);
        verify(transactionStatus).setInTransaction(true);
        verify(transactionHook).beforeBegin(any(), any(DatabaseType.class), eq(transactionContext));
        verify(localTransactionManager).begin();
        verify(transactionHook).afterBegin(any(), any(DatabaseType.class), eq(transactionContext));
        assertTrue(transactionContext.isTransactionStarted());
        assertThat(transactionContext.getTransactionType().get(), is(TransactionType.LOCAL.name()));
    }
    
    @Test
    void assertBeginDistributedWhenAlreadyInTransaction() {
        when(transactionStatus.isInTransaction()).thenReturn(true);
        ShardingSphereTransactionManagerEngine transactionManagerEngine = mock(ShardingSphereTransactionManagerEngine.class);
        when(transactionManagerEngine.getTransactionManager(TransactionType.XA)).thenReturn(distributedTransactionManager);
        TransactionHook transactionHook = mock(TransactionHook.class);
        mockProxyContext(TransactionType.XA, transactionManagerEngine, Collections.singletonMap(mock(ShardingSphereRule.class), transactionHook));
        ProxyBackendTransactionManager transactionManager = new ProxyBackendTransactionManager(databaseConnectionManager);
        setLocalTransactionManager(transactionManager);
        transactionManager.begin();
        verify(databaseConnectionManager, never()).closeHandlers(true);
        verify(databaseConnectionManager, never()).closeConnections(false);
        verify(transactionStatus, never()).setInTransaction(true);
        verify(transactionHook).beforeBegin(any(), any(DatabaseType.class), eq(transactionContext));
        verify(distributedTransactionManager).begin();
        verify(transactionHook).afterBegin(any(), any(DatabaseType.class), eq(transactionContext));
    }
    
    @Test
    void assertCommitWithoutTransaction() throws SQLException {
        mockProxyContext(TransactionType.LOCAL, null, Collections.emptyMap());
        ProxyBackendTransactionManager transactionManager = new ProxyBackendTransactionManager(databaseConnectionManager);
        setLocalTransactionManager(transactionManager);
        transactionManager.commit();
        verify(localTransactionManager, never()).commit();
        verify(transactionStatus, never()).setInTransaction(false);
    }
    
    @Test
    void assertCommitLocalWithoutLock() throws SQLException {
        when(transactionStatus.isInTransaction()).thenReturn(true);
        ConnectionSavepointManager savepointManager = mock(ConnectionSavepointManager.class);
        when(ConnectionSavepointManager.getInstance()).thenReturn(savepointManager);
        TransactionHook transactionHook = mock(TransactionHook.class);
        mockProxyContext(TransactionType.LOCAL, null, Collections.singletonMap(mock(ShardingSphereRule.class), transactionHook));
        ProxyBackendTransactionManager transactionManager = new ProxyBackendTransactionManager(databaseConnectionManager);
        setLocalTransactionManager(transactionManager);
        transactionManager.commit();
        verify(transactionHook).beforeCommit(any(), any(DatabaseType.class), anyCollection(), eq(transactionContext));
        verify(localTransactionManager).commit();
        verify(savepointManager).transactionFinished(connection);
        verify(transactionStatus).setInTransaction(false);
        verify(connectionContext).close();
    }
    
    @Test
    void assertCommitWithLockAndDistributedManager() throws SQLException {
        when(transactionStatus.isInTransaction()).thenReturn(true);
        transactionContext.setExceptionOccur(true);
        ConnectionSavepointManager savepointManager = mock(ConnectionSavepointManager.class);
        when(ConnectionSavepointManager.getInstance()).thenReturn(savepointManager);
        ShardingSphereTransactionManagerEngine transactionManagerEngine = mock(ShardingSphereTransactionManagerEngine.class);
        when(transactionManagerEngine.getTransactionManager(TransactionType.XA)).thenReturn(distributedTransactionManager);
        doAnswer(invocation -> {
            ExclusiveOperationVoidCallback callback = invocation.getArgument(2);
            callback.execute();
            return null;
        }).when(exclusiveOperatorEngine).operate(any(), anyLong(), any());
        TransactionHook transactionHook = mock(TransactionHook.class);
        when(transactionHook.isNeedLockWhenCommit(any())).thenReturn(true);
        mockProxyContext(TransactionType.XA, transactionManagerEngine, Collections.singletonMap(mock(ShardingSphereRule.class), transactionHook));
        ProxyBackendTransactionManager transactionManager = new ProxyBackendTransactionManager(databaseConnectionManager);
        setLocalTransactionManager(transactionManager);
        transactionManager.commit();
        verify(exclusiveOperatorEngine).operate(any(), anyLong(), any());
        verify(transactionHook).beforeCommit(any(), any(DatabaseType.class), anyCollection(), eq(transactionContext));
        verify(distributedTransactionManager).commit(true);
        verify(savepointManager).transactionFinished(connection);
        verify(transactionStatus).setInTransaction(false);
        verify(connectionContext).close();
    }
    
    @Test
    void assertRollbackWithoutTransaction() throws SQLException {
        mockProxyContext(TransactionType.LOCAL, null, Collections.emptyMap());
        ProxyBackendTransactionManager transactionManager = new ProxyBackendTransactionManager(databaseConnectionManager);
        setLocalTransactionManager(transactionManager);
        transactionManager.rollback();
        verify(localTransactionManager, never()).rollback();
        verify(transactionStatus, never()).setInTransaction(false);
    }
    
    @Test
    void assertRollbackLocalWithHooks() throws SQLException {
        when(transactionStatus.isInTransaction()).thenReturn(true);
        ConnectionSavepointManager savepointManager = mock(ConnectionSavepointManager.class);
        when(ConnectionSavepointManager.getInstance()).thenReturn(savepointManager);
        TransactionHook transactionHook = mock(TransactionHook.class);
        mockProxyContext(TransactionType.LOCAL, null, Collections.singletonMap(mock(ShardingSphereRule.class), transactionHook));
        ProxyBackendTransactionManager transactionManager = new ProxyBackendTransactionManager(databaseConnectionManager);
        setLocalTransactionManager(transactionManager);
        transactionManager.rollback();
        verify(transactionHook).beforeRollback(any(), any(DatabaseType.class), anyCollection(), eq(transactionContext));
        verify(localTransactionManager).rollback();
        verify(transactionHook).afterRollback(any(), any(DatabaseType.class), anyCollection(), eq(transactionContext));
        verify(savepointManager).transactionFinished(connection);
        verify(transactionStatus).setInTransaction(false);
        verify(connectionContext).close();
    }
    
    @Test
    void assertRollbackDistributedSkipsWhenStatusCleared() throws SQLException {
        when(transactionStatus.isInTransaction()).thenReturn(true, false);
        ShardingSphereTransactionManagerEngine transactionManagerEngine = mock(ShardingSphereTransactionManagerEngine.class);
        when(transactionManagerEngine.getTransactionManager(TransactionType.XA)).thenReturn(distributedTransactionManager);
        TransactionHook transactionHook = mock(TransactionHook.class);
        mockProxyContext(TransactionType.XA, transactionManagerEngine, Collections.singletonMap(mock(ShardingSphereRule.class), transactionHook));
        ProxyBackendTransactionManager transactionManager = new ProxyBackendTransactionManager(databaseConnectionManager);
        setLocalTransactionManager(transactionManager);
        transactionManager.rollback();
        verify(transactionHook).beforeRollback(any(), any(DatabaseType.class), anyCollection(), eq(transactionContext));
        verify(distributedTransactionManager, never()).rollback();
        verify(transactionHook, never()).afterRollback(any(), any(DatabaseType.class), anyCollection(), eq(transactionContext));
        verify(connectionContext, never()).close();
    }
    
    @Test
    void assertRollbackDistributedTransaction() throws SQLException {
        when(transactionStatus.isInTransaction()).thenReturn(true);
        ConnectionSavepointManager savepointManager = mock(ConnectionSavepointManager.class);
        when(ConnectionSavepointManager.getInstance()).thenReturn(savepointManager);
        ShardingSphereTransactionManagerEngine transactionManagerEngine = mock(ShardingSphereTransactionManagerEngine.class);
        when(transactionManagerEngine.getTransactionManager(TransactionType.XA)).thenReturn(distributedTransactionManager);
        TransactionHook transactionHook = mock(TransactionHook.class);
        mockProxyContext(TransactionType.XA, transactionManagerEngine, Collections.singletonMap(mock(ShardingSphereRule.class), transactionHook));
        ProxyBackendTransactionManager transactionManager = new ProxyBackendTransactionManager(databaseConnectionManager);
        setLocalTransactionManager(transactionManager);
        transactionManager.rollback();
        verify(transactionHook).beforeRollback(any(), any(DatabaseType.class), anyCollection(), eq(transactionContext));
        verify(distributedTransactionManager).rollback();
        verify(transactionHook).afterRollback(any(), any(DatabaseType.class), anyCollection(), eq(transactionContext));
        verify(savepointManager).transactionFinished(connection);
        verify(transactionStatus).setInTransaction(false);
        verify(connectionContext).close();
    }
    
    @Test
    void assertSetSavepointAddsPostProcessor() throws SQLException {
        ConnectionSavepointManager savepointManager = mock(ConnectionSavepointManager.class);
        when(ConnectionSavepointManager.getInstance()).thenReturn(savepointManager);
        mockProxyContext(TransactionType.LOCAL, null, Collections.emptyMap());
        ProxyBackendTransactionManager transactionManager = new ProxyBackendTransactionManager(databaseConnectionManager);
        transactionManager.setSavepoint("sp");
        verify(savepointManager).setSavepoint(connection, "sp");
        Connection targetConnection = mock(Connection.class);
        for (ConnectionPostProcessor each : connectionPostProcessors) {
            each.process(targetConnection);
        }
        verify(savepointManager).setSavepoint(targetConnection, "sp");
    }
    
    @Test
    void assertRollbackToClearsExceptionFlagWhenNoErrors() throws SQLException {
        ConnectionSavepointManager savepointManager = mock(ConnectionSavepointManager.class);
        when(ConnectionSavepointManager.getInstance()).thenReturn(savepointManager);
        transactionContext.setExceptionOccur(true);
        mockProxyContext(TransactionType.LOCAL, null, Collections.emptyMap());
        ProxyBackendTransactionManager transactionManager = new ProxyBackendTransactionManager(databaseConnectionManager);
        transactionManager.rollbackTo("sp");
        verify(savepointManager).rollbackToSavepoint(connection, "sp");
        assertFalse(transactionContext.isExceptionOccur());
    }
    
    @Test
    void assertRollbackToThrowsCombinedSQLException() throws SQLException {
        Connection anotherConnection = mock(Connection.class);
        when(databaseConnectionManager.getCachedConnections()).thenReturn(mockCachedConnections(connection, anotherConnection));
        ConnectionSavepointManager savepointManager = mock(ConnectionSavepointManager.class);
        when(ConnectionSavepointManager.getInstance()).thenReturn(savepointManager);
        doThrow(new SQLException("first")).when(savepointManager).rollbackToSavepoint(connection, "sp");
        doThrow(new SQLException("second")).when(savepointManager).rollbackToSavepoint(anotherConnection, "sp");
        mockProxyContext(TransactionType.LOCAL, null, Collections.emptyMap());
        SQLException actualException = assertThrows(SQLException.class, () -> new ProxyBackendTransactionManager(databaseConnectionManager).rollbackTo("sp"));
        assertThat(actualException.getMessage(), is("first"));
        assertThat(actualException.getNextException().getMessage(), is("second"));
    }
    
    @Test
    void assertReleaseSavepointThrowsCombinedSQLException() throws SQLException {
        Connection anotherConnection = mock(Connection.class);
        when(databaseConnectionManager.getCachedConnections()).thenReturn(mockCachedConnections(connection, anotherConnection));
        ConnectionSavepointManager savepointManager = mock(ConnectionSavepointManager.class);
        when(ConnectionSavepointManager.getInstance()).thenReturn(savepointManager);
        doThrow(new SQLException("first")).when(savepointManager).releaseSavepoint(connection, "sp");
        doThrow(new SQLException("second")).when(savepointManager).releaseSavepoint(anotherConnection, "sp");
        mockProxyContext(TransactionType.LOCAL, null, Collections.emptyMap());
        SQLException actualException = assertThrows(SQLException.class, () -> new ProxyBackendTransactionManager(databaseConnectionManager).releaseSavepoint("sp"));
        assertThat(actualException.getMessage(), is("first"));
        assertThat(actualException.getNextException().getMessage(), is("second"));
    }
    
    @Test
    void assertReleaseSavepointWithoutErrors() throws SQLException {
        ConnectionSavepointManager savepointManager = mock(ConnectionSavepointManager.class);
        when(ConnectionSavepointManager.getInstance()).thenReturn(savepointManager);
        mockProxyContext(TransactionType.LOCAL, null, Collections.emptyMap());
        new ProxyBackendTransactionManager(databaseConnectionManager).releaseSavepoint("sp");
        verify(savepointManager).releaseSavepoint(connection, "sp");
    }
    
    private Multimap<String, Connection> mockCachedConnections(Connection... connections) {
        Multimap<String, Connection> result = LinkedHashMultimap.create();
        for (Connection each : connections) {
            result.put("ds1", each);
        }
        return result;
    }
    
    private void mockProxyContext(TransactionType defaultType, ShardingSphereTransactionManagerEngine engine, Map<ShardingSphereRule, TransactionHook> transactionHooks) {
        TransactionRule transactionRule = mock(TransactionRule.class);
        when(transactionRule.getDefaultType()).thenReturn(defaultType);
        when(transactionRule.getResource()).thenReturn(engine);
        ContextManager contextManager = mock(ContextManager.class, Answers.RETURNS_DEEP_STUBS);
        when(contextManager.getMetaDataContexts().getMetaData().getGlobalRuleMetaData()).thenReturn(new RuleMetaData(Collections.singleton(transactionRule)));
        when(contextManager.getExclusiveOperatorEngine()).thenReturn(exclusiveOperatorEngine);
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        when(OrderedSPILoader.getServices(eq(TransactionHook.class), ArgumentMatchers.<Collection<ShardingSphereRule>>any())).thenReturn(transactionHooks);
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private void setLocalTransactionManager(final ProxyBackendTransactionManager transactionManager) {
        Plugins.getMemberAccessor().set(ProxyBackendTransactionManager.class.getDeclaredField("localTransactionManager"), transactionManager, localTransactionManager);
    }
}
