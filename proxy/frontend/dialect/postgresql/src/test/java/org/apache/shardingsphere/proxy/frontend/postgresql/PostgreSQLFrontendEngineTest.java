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

package org.apache.shardingsphere.proxy.frontend.postgresql;

import lombok.SneakyThrows;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.exception.core.exception.transaction.InTransactionException;
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.session.transaction.TransactionStatus;
import org.apache.shardingsphere.proxy.frontend.executor.ConnectionThreadExecutorGroup;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.PortalContext;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.PostgreSQLPortalContextRegistry;
import org.apache.shardingsphere.proxy.frontend.spi.DatabaseProtocolFrontendEngine;
import org.junit.jupiter.api.Test;
import org.mockito.internal.configuration.plugins.Plugins;

import java.util.Collections;
import java.util.concurrent.ConcurrentMap;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PostgreSQLFrontendEngineTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "PostgreSQL");
    
    private final DatabaseProtocolFrontendEngine engine = DatabaseTypedSPILoader.getService(DatabaseProtocolFrontendEngine.class, databaseType);
    
    @Test
    void assertRelease() {
        ConnectionSession connectionSession = mock(ConnectionSession.class);
        int connectionId = 1;
        when(connectionSession.getConnectionId()).thenReturn(connectionId);
        PostgreSQLPortalContextRegistry.getInstance().get(connectionId);
        ConnectionThreadExecutorGroup.getInstance().register(connectionId);
        ConnectionThreadExecutorGroup.getInstance().unregisterAndAwaitTermination(connectionId);
        engine.release(connectionSession);
        assertTrue(getPortalContexts().isEmpty());
    }
    
    @Test
    void assertHandleExceptionWhenNeedMarkOccurred() {
        ConnectionSession connectionSession = mock(ConnectionSession.class);
        ConnectionContext connectionContext = mockConnectionSession(connectionSession, true, false);
        engine.handleException(connectionSession, new Exception("error"));
        assertTrue(connectionContext.getTransactionContext().isExceptionOccur());
    }
    
    @Test
    void assertHandleExceptionWhenNotInTransaction() {
        ConnectionSession connectionSession = mock(ConnectionSession.class);
        ConnectionContext connectionContext = mockConnectionSession(connectionSession, false, false);
        engine.handleException(connectionSession, new Exception("error"));
        assertFalse(connectionContext.getTransactionContext().isExceptionOccur());
    }
    
    @Test
    void assertHandleExceptionWhenAlreadyOccurred() {
        ConnectionSession connectionSession = mock(ConnectionSession.class);
        ConnectionContext connectionContext = mockConnectionSession(connectionSession, true, true);
        engine.handleException(connectionSession, new Exception("error"));
        assertTrue(connectionContext.getTransactionContext().isExceptionOccur());
    }
    
    @Test
    void assertHandleExceptionWithInTransactionException() {
        ConnectionSession connectionSession = mock(ConnectionSession.class);
        ConnectionContext connectionContext = mockConnectionSession(connectionSession, true, false);
        engine.handleException(connectionSession, new InTransactionException());
        assertFalse(connectionContext.getTransactionContext().isExceptionOccur());
    }
    
    private ConnectionContext mockConnectionSession(final ConnectionSession connectionSession, final boolean inTransaction, final boolean exceptionOccur) {
        TransactionStatus transactionStatus = new TransactionStatus();
        transactionStatus.setInTransaction(inTransaction);
        ConnectionContext result = new ConnectionContext(Collections::emptyList);
        result.getTransactionContext().setExceptionOccur(exceptionOccur);
        when(connectionSession.getTransactionStatus()).thenReturn(transactionStatus);
        when(connectionSession.getConnectionContext()).thenReturn(result);
        return result;
    }
    
    @SuppressWarnings("unchecked")
    @SneakyThrows(ReflectiveOperationException.class)
    private ConcurrentMap<Integer, PortalContext> getPortalContexts() {
        return (ConcurrentMap<Integer, PortalContext>) Plugins.getMemberAccessor()
                .get(PostgreSQLPortalContextRegistry.class.getDeclaredField("portalContexts"), PostgreSQLPortalContextRegistry.getInstance());
    }
}
