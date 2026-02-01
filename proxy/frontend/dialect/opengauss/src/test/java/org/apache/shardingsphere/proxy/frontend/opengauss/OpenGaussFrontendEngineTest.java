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

package org.apache.shardingsphere.proxy.frontend.opengauss;

import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.exception.core.exception.transaction.InTransactionException;
import org.apache.shardingsphere.database.protocol.opengauss.codec.OpenGaussPacketCodecEngine;
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.session.transaction.TransactionStatus;
import org.apache.shardingsphere.proxy.frontend.opengauss.authentication.OpenGaussAuthenticationEngine;
import org.apache.shardingsphere.proxy.frontend.opengauss.command.OpenGaussCommandExecuteEngine;
import org.apache.shardingsphere.proxy.frontend.postgresql.PostgreSQLFrontendEngine;
import org.apache.shardingsphere.proxy.frontend.spi.DatabaseProtocolFrontendEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.internal.configuration.plugins.Plugins;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OpenGaussFrontendEngineTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "openGauss");
    
    private final DatabaseProtocolFrontendEngine databaseProtocolFrontendEngine = DatabaseTypedSPILoader.getService(DatabaseProtocolFrontendEngine.class, databaseType);
    
    @Mock
    private PostgreSQLFrontendEngine mockPostgreSQLFrontendEngine;
    
    @BeforeEach
    void setup() throws ReflectiveOperationException {
        Plugins.getMemberAccessor().set(OpenGaussFrontendEngine.class.getDeclaredField("postgresqlFrontendEngine"), databaseProtocolFrontendEngine, mockPostgreSQLFrontendEngine);
    }
    
    @Test
    void assertGetCommandExecuteEngine() {
        assertThat(databaseProtocolFrontendEngine.getCommandExecuteEngine(), isA(OpenGaussCommandExecuteEngine.class));
    }
    
    @Test
    void assertGetCodecEngine() {
        assertThat(databaseProtocolFrontendEngine.getCodecEngine(), isA(OpenGaussPacketCodecEngine.class));
    }
    
    @Test
    void assertGetAuthenticationEngine() {
        assertThat(databaseProtocolFrontendEngine.getAuthenticationEngine(), isA(OpenGaussAuthenticationEngine.class));
    }
    
    @Test
    void assertHandleExceptionWhenNeedMarkOccurred() {
        ConnectionContext connectionContext = mockConnectionContext(true, false);
        ConnectionSession connectionSession = mockConnectionSession(connectionContext);
        databaseProtocolFrontendEngine.handleException(connectionSession, new Exception("error"));
        assertTrue(connectionContext.getTransactionContext().isExceptionOccur());
    }
    
    @Test
    void assertHandleExceptionWhenNotInTransaction() {
        ConnectionContext connectionContext = mockConnectionContext(false, false);
        ConnectionSession connectionSession = mockConnectionSession(connectionContext);
        databaseProtocolFrontendEngine.handleException(connectionSession, new Exception("error"));
        assertFalse(connectionContext.getTransactionContext().isExceptionOccur());
    }
    
    @Test
    void assertHandleExceptionWhenAlreadyOccurred() {
        ConnectionContext connectionContext = mockConnectionContext(true, true);
        ConnectionSession connectionSession = mockConnectionSession(connectionContext);
        databaseProtocolFrontendEngine.handleException(connectionSession, new Exception("error"));
        assertTrue(connectionContext.getTransactionContext().isExceptionOccur());
    }
    
    @Test
    void assertHandleExceptionWithInTransactionException() {
        ConnectionContext connectionContext = mockConnectionContext(true, false);
        ConnectionSession connectionSession = mockConnectionSession(connectionContext);
        databaseProtocolFrontendEngine.handleException(connectionSession, new InTransactionException());
        assertFalse(connectionContext.getTransactionContext().isExceptionOccur());
    }
    
    @Test
    void assertRelease() {
        ConnectionSession connection = mock(ConnectionSession.class);
        databaseProtocolFrontendEngine.release(connection);
        verify(mockPostgreSQLFrontendEngine).release(connection);
    }
    
    private ConnectionSession mockConnectionSession(final ConnectionContext connectionContext) {
        TransactionStatus transactionStatus = new TransactionStatus();
        transactionStatus.setInTransaction(connectionContext.getTransactionContext().isTransactionStarted());
        ConnectionSession result = mock(ConnectionSession.class);
        when(result.getTransactionStatus()).thenReturn(transactionStatus);
        if (transactionStatus.isInTransaction()) {
            when(result.getConnectionContext()).thenReturn(connectionContext);
        }
        return result;
    }
    
    private ConnectionContext mockConnectionContext(final boolean inTransaction, final boolean exceptionOccur) {
        ConnectionContext result = new ConnectionContext(Collections::emptyList);
        if (inTransaction) {
            result.getTransactionContext().beginTransaction("LOCAL", null);
        }
        result.getTransactionContext().setExceptionOccur(exceptionOccur);
        return result;
    }
}
