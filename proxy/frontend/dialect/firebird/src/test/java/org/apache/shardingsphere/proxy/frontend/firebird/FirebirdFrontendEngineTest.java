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

package org.apache.shardingsphere.proxy.frontend.firebird;

import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.exception.core.exception.transaction.InTransactionException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.BatchBlobContinuationBpbException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.BatchBpbTooBigException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.BatchSegmentExceedsBlobException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.BatchSegmentTooBigException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.BatchSmallDataException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.BlobFilterNotFoundException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.InvalidBpbVersionException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.ParameterConversionException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.StringTruncationException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.TransliterationFailedException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.UnknownBatchBlobIdException;
import org.apache.shardingsphere.database.protocol.firebird.codec.FirebirdPacketCodecEngine;
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.session.transaction.TransactionStatus;
import org.apache.shardingsphere.proxy.frontend.firebird.authentication.FirebirdAuthenticationEngine;
import org.apache.shardingsphere.proxy.frontend.firebird.command.FirebirdCommandExecuteEngine;
import org.apache.shardingsphere.proxy.frontend.firebird.resource.FirebirdConnectionResourceManager;
import org.apache.shardingsphere.proxy.frontend.spi.DatabaseProtocolFrontendEngine;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;

import java.util.Collections;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings({ProxyContext.class, FirebirdConnectionResourceManager.class})
class FirebirdFrontendEngineTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "Firebird");
    
    private final DatabaseProtocolFrontendEngine engine = DatabaseTypedSPILoader.getService(DatabaseProtocolFrontendEngine.class, databaseType);
    
    @Mock
    private ConnectionSession connectionSession;
    
    @Mock
    private FirebirdConnectionResourceManager connectionResourceManager;
    
    @Test
    void assertGetAuthenticationEngine() {
        assertThat(engine.getAuthenticationEngine(), isA(FirebirdAuthenticationEngine.class));
    }
    
    @Test
    void assertGetCommandExecuteEngine() {
        assertThat(engine.getCommandExecuteEngine(), isA(FirebirdCommandExecuteEngine.class));
    }
    
    @Test
    void assertGetCodecEngine() {
        assertThat(engine.getCodecEngine(), isA(FirebirdPacketCodecEngine.class));
    }
    
    @Test
    void assertRelease() {
        int connectionId = 1;
        when(connectionSession.getConnectionId()).thenReturn(connectionId);
        when(FirebirdConnectionResourceManager.getInstance()).thenReturn(connectionResourceManager);
        engine.release(connectionSession);
        verify(connectionResourceManager).unregisterConnection(connectionId);
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("provideHandleExceptionParameters")
    void assertHandleException(final String testName, final boolean exceptionOccur, final Exception exception) {
        ConnectionContext connectionContext = mockConnectionSession(true, exceptionOccur);
        engine.handleException(connectionSession, exception);
        assertTrue(connectionContext.getTransactionContext().isExceptionOccur());
    }
    
    private ConnectionContext mockConnectionSession(final boolean inTransaction, final boolean exceptionOccur) {
        TransactionStatus transactionStatus = new TransactionStatus();
        transactionStatus.setInTransaction(inTransaction);
        ConnectionContext result = new ConnectionContext(Collections::emptyList);
        result.getTransactionContext().setExceptionOccur(exceptionOccur);
        when(connectionSession.getTransactionStatus()).thenReturn(transactionStatus);
        if (inTransaction) {
            when(connectionSession.getConnectionContext()).thenReturn(result);
        }
        return result;
    }
    
    private static Stream<Arguments> provideHandleExceptionParameters() {
        return Stream.of(Arguments.of("mark when in transaction", false, new Exception("error")), Arguments.of("keep marked when already occurred", true, new Exception("error")));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("provideIgnoredExceptionParameters")
    void assertHandleIgnoredException(final String testName, final boolean inTransaction, final Exception exception) {
        ConnectionContext connectionContext = mockConnectionSession(inTransaction, false);
        engine.handleException(connectionSession, exception);
        assertFalse(connectionContext.getTransactionContext().isExceptionOccur());
    }
    
    private static Stream<Arguments> provideIgnoredExceptionParameters() {
        return Stream.of(
                Arguments.of("skip when in transaction exception", true, new InTransactionException()),
                Arguments.of("skip when batch blob continuation bpb exception", true, new BatchBlobContinuationBpbException(4L)),
                Arguments.of("skip when batch bpb too big exception", true, new BatchBpbTooBigException(8L, 4L)),
                Arguments.of("skip when batch segment exceeds blob exception", true, new BatchSegmentExceedsBlobException(50, 10)),
                Arguments.of("skip when batch segment too big exception", true, new BatchSegmentTooBigException(50, 10)),
                Arguments.of("skip when batch small data exception", true, new BatchSmallDataException("BLOB")),
                Arguments.of("skip when blob filter not found exception", true, new BlobFilterNotFoundException(0, 2)),
                Arguments.of("skip when invalid bpb version exception", true, new InvalidBpbVersionException(2, 1)),
                Arguments.of("skip when parameter conversion exception", true, new ParameterConversionException(new StringTruncationException(20, 32))),
                Arguments.of("skip when transliteration failed exception", true, new TransliterationFailedException()),
                Arguments.of("skip when unknown batch blob id exception", true, new UnknownBatchBlobIdException(1L)),
                Arguments.of("skip when not in transaction", false, new Exception("error")));
    }
}
