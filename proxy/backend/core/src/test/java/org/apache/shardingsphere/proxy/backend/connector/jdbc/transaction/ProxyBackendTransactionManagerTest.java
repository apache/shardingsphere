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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyBoolean;
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
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("provideBeginScenarios")
    void assertBeginScenarios(final String name, final TransactionType defaultType, final boolean initialInTransaction, final boolean hasDistributedManager,
                              final boolean expectLocalBegin, final boolean expectDistributedBegin, final boolean expectCloseResources,
                              final boolean expectSetInTransaction, final boolean expectTransactionTypeSet, final boolean hasHook) {
        when(transactionStatus.isInTransaction()).thenReturn(initialInTransaction);
        TransactionHook transactionHook = hasHook ? mock(TransactionHook.class) : null;
        Map<ShardingSphereRule, TransactionHook> transactionHooks = hasHook ? Collections.singletonMap(mock(ShardingSphereRule.class), transactionHook) : Collections.emptyMap();
        ShardingSphereTransactionManagerEngine transactionManagerEngine = hasDistributedManager ? mock(ShardingSphereTransactionManagerEngine.class) : null;
        if (hasDistributedManager) {
            when(transactionManagerEngine.getTransactionManager(TransactionType.XA)).thenReturn(distributedTransactionManager);
        }
        mockProxyContext(defaultType, transactionManagerEngine, transactionHooks);
        ProxyBackendTransactionManager transactionManager = new ProxyBackendTransactionManager(databaseConnectionManager);
        setLocalTransactionManager(transactionManager);
        transactionManager.begin();
        if (expectCloseResources) {
            verify(databaseConnectionManager).closeHandlers(true);
            verify(databaseConnectionManager).closeConnections(false);
        } else {
            verify(databaseConnectionManager, never()).closeHandlers(true);
            verify(databaseConnectionManager, never()).closeConnections(false);
        }
        if (expectSetInTransaction) {
            verify(transactionStatus).setInTransaction(true);
        } else {
            verify(transactionStatus, never()).setInTransaction(true);
        }
        if (hasHook) {
            verify(transactionHook).beforeBegin(any(), any(DatabaseType.class), eq(transactionContext));
            verify(transactionHook).afterBegin(any(), any(DatabaseType.class), eq(transactionContext));
        }
        if (expectLocalBegin) {
            verify(localTransactionManager).begin();
        } else {
            verify(localTransactionManager, never()).begin();
        }
        if (expectDistributedBegin) {
            verify(distributedTransactionManager).begin();
        } else {
            verify(distributedTransactionManager, never()).begin();
        }
        if (expectTransactionTypeSet) {
            assertTrue(transactionContext.isTransactionStarted());
            assertThat(transactionContext.getTransactionType().get(), is(defaultType.name()));
        } else {
            assertFalse(transactionContext.isTransactionStarted());
        }
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
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("provideCommitScenarios")
    void assertCommitScenarios(final String name, final TransactionType transactionType, final boolean needLock, final boolean hasDistributedManager,
                               final boolean exceptionOccurred, final boolean hasHook, final boolean expectDistributedCommit) throws SQLException {
        when(transactionStatus.isInTransaction()).thenReturn(true);
        transactionContext.setExceptionOccur(exceptionOccurred);
        ConnectionSavepointManager savepointManager = mock(ConnectionSavepointManager.class);
        when(ConnectionSavepointManager.getInstance()).thenReturn(savepointManager);
        TransactionHook transactionHook = hasHook ? mock(TransactionHook.class) : null;
        if (hasHook) {
            when(transactionHook.isNeedLockWhenCommit(any())).thenReturn(needLock);
        }
        Map<ShardingSphereRule, TransactionHook> transactionHooks = hasHook ? Collections.singletonMap(mock(ShardingSphereRule.class), transactionHook) : Collections.emptyMap();
        ShardingSphereTransactionManagerEngine transactionManagerEngine = hasDistributedManager ? mock(ShardingSphereTransactionManagerEngine.class) : null;
        if (hasDistributedManager) {
            when(transactionManagerEngine.getTransactionManager(TransactionType.XA)).thenReturn(distributedTransactionManager);
        }
        if (needLock) {
            doAnswer(invocation -> {
                ExclusiveOperationVoidCallback callback = invocation.getArgument(2);
                callback.execute();
                return null;
            }).when(exclusiveOperatorEngine).operate(any(), anyLong(), any());
        }
        mockProxyContext(transactionType, transactionManagerEngine, transactionHooks);
        ProxyBackendTransactionManager transactionManager = new ProxyBackendTransactionManager(databaseConnectionManager);
        setLocalTransactionManager(transactionManager);
        transactionManager.commit();
        if (needLock) {
            verify(exclusiveOperatorEngine).operate(any(), anyLong(), any());
        } else {
            verify(exclusiveOperatorEngine, never()).operate(any(), anyLong(), any());
        }
        if (hasHook) {
            verify(transactionHook).beforeCommit(any(), any(DatabaseType.class), anyCollection(), eq(transactionContext));
        }
        if (expectDistributedCommit) {
            verify(distributedTransactionManager).commit(exceptionOccurred);
        } else {
            verify(localTransactionManager).commit();
            verify(distributedTransactionManager, never()).commit(anyBoolean());
        }
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
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("provideRollbackScenarios")
    void assertRollbackScenarios(final String name, final TransactionType transactionType, final boolean secondInTransaction,
                                 final boolean hasDistributedManager, final boolean expectDistributedRollback, final boolean hasHook,
                                 final boolean expectAfterHook, final boolean expectClear) throws SQLException {
        when(transactionStatus.isInTransaction()).thenReturn(true, secondInTransaction);
        ConnectionSavepointManager savepointManager = mock(ConnectionSavepointManager.class);
        when(ConnectionSavepointManager.getInstance()).thenReturn(savepointManager);
        TransactionHook transactionHook = hasHook ? mock(TransactionHook.class) : null;
        Map<ShardingSphereRule, TransactionHook> transactionHooks = hasHook ? Collections.singletonMap(mock(ShardingSphereRule.class), transactionHook) : Collections.emptyMap();
        ShardingSphereTransactionManagerEngine transactionManagerEngine = hasDistributedManager ? mock(ShardingSphereTransactionManagerEngine.class) : null;
        if (hasDistributedManager) {
            when(transactionManagerEngine.getTransactionManager(TransactionType.XA)).thenReturn(distributedTransactionManager);
        }
        mockProxyContext(transactionType, transactionManagerEngine, transactionHooks);
        ProxyBackendTransactionManager transactionManager = new ProxyBackendTransactionManager(databaseConnectionManager);
        setLocalTransactionManager(transactionManager);
        transactionManager.rollback();
        if (hasHook) {
            verify(transactionHook).beforeRollback(any(), any(DatabaseType.class), anyCollection(), eq(transactionContext));
        }
        if (expectDistributedRollback) {
            verify(distributedTransactionManager).rollback();
        } else if (expectClear) {
            verify(localTransactionManager).rollback();
            verify(distributedTransactionManager, never()).rollback();
        } else {
            verify(localTransactionManager, never()).rollback();
            verify(distributedTransactionManager, never()).rollback();
        }
        if (hasHook) {
            if (expectAfterHook) {
                verify(transactionHook).afterRollback(any(), any(DatabaseType.class), anyCollection(), eq(transactionContext));
            } else {
                verify(transactionHook, never()).afterRollback(any(), any(DatabaseType.class), anyCollection(), eq(transactionContext));
            }
        }
        if (expectClear) {
            verify(savepointManager).transactionFinished(connection);
            verify(transactionStatus).setInTransaction(false);
            verify(connectionContext).close();
        } else {
            verify(savepointManager, never()).transactionFinished(any());
            verify(transactionStatus, never()).setInTransaction(false);
            verify(connectionContext, never()).close();
        }
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
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("provideRollbackToScenarios")
    void assertRollbackToScenarios(final String name, final boolean initialExceptionFlag, final int exceptionCount, final boolean expectFlagCleared,
                                   final boolean expectThrows) throws SQLException {
        ConnectionSavepointManager savepointManager = mock(ConnectionSavepointManager.class);
        when(ConnectionSavepointManager.getInstance()).thenReturn(savepointManager);
        transactionContext.setExceptionOccur(initialExceptionFlag);
        if (exceptionCount == 2) {
            Connection anotherConnection = mock(Connection.class);
            when(databaseConnectionManager.getCachedConnections()).thenReturn(mockCachedConnections(connection, anotherConnection));
            doThrow(new SQLException("first")).when(savepointManager).rollbackToSavepoint(connection, "sp");
            doThrow(new SQLException("second")).when(savepointManager).rollbackToSavepoint(anotherConnection, "sp");
        } else {
            when(databaseConnectionManager.getCachedConnections()).thenReturn(mockCachedConnections(connection));
        }
        mockProxyContext(TransactionType.LOCAL, null, Collections.emptyMap());
        ProxyBackendTransactionManager transactionManager = new ProxyBackendTransactionManager(databaseConnectionManager);
        if (expectThrows) {
            SQLException actualException = assertThrows(SQLException.class, () -> transactionManager.rollbackTo("sp"));
            assertThat(actualException.getMessage(), is("first"));
            assertThat(actualException.getNextException().getMessage(), is("second"));
        } else {
            transactionManager.rollbackTo("sp");
            verify(savepointManager).rollbackToSavepoint(connection, "sp");
            if (exceptionCount == 0) {
                assertFalse(transactionContext.isExceptionOccur());
            }
            if (expectFlagCleared) {
                assertFalse(transactionContext.isExceptionOccur());
            }
        }
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
    
    private static Stream<Arguments> provideBeginScenarios() {
        return Stream.of(
                Arguments.of("LOCAL start initializes transaction context", TransactionType.LOCAL, false, false, true, false, true, true, true, true),
                Arguments.of("Distributed begin when already in transaction", TransactionType.XA, true, true, false, true, false, false, false, true),
                Arguments.of("XA default falls back to local when manager missing", TransactionType.XA, false, false, true, false, true, true, true, false)
        );
    }
    
    private static Stream<Arguments> provideCommitScenarios() {
        return Stream.of(
                Arguments.of("Local commit without lock", TransactionType.LOCAL, false, false, false, true, false),
                Arguments.of("Distributed commit with lock", TransactionType.XA, true, true, true, true, true),
                Arguments.of("XA fallback commit uses local manager", TransactionType.XA, false, false, false, false, false)
        );
    }
    
    private static Stream<Arguments> provideRollbackScenarios() {
        return Stream.of(
                Arguments.of("Local rollback with hooks", TransactionType.LOCAL, true, false, false, true, true, true),
                Arguments.of("Distributed rollback skipped after status cleared", TransactionType.XA, false, true, false, true, false, false),
                Arguments.of("XA rollback falls back to local without manager", TransactionType.XA, true, false, false, false, false, true),
                Arguments.of("Distributed rollback executes manager", TransactionType.XA, true, true, true, true, true, true)
        );
    }
    
    private static Stream<Arguments> provideRollbackToScenarios() {
        return Stream.of(
                Arguments.of("Rollback to clears exception flag after success", true, 0, true, false),
                Arguments.of("Rollback to keeps flag when no exception occurred", false, 0, false, false),
                Arguments.of("Rollback to aggregates SQLException across connections", false, 2, false, true)
        );
    }
}
