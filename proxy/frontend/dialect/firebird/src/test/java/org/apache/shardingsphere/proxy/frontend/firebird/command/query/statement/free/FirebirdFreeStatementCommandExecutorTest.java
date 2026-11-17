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

package org.apache.shardingsphere.proxy.frontend.firebird.command.query.statement.free;

import org.apache.shardingsphere.database.exception.core.exception.protocol.DatabaseProtocolException;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.batch.FirebirdBatchRegistry;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.batch.FirebirdBatchStatement;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.FirebirdFreeStatementPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.generic.FirebirdGenericResponsePacket;
import org.apache.shardingsphere.database.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.proxy.backend.handler.ProxyBackendHandler;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.session.ServerPreparedStatementRegistry;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.blob.generator.FirebirdBlobHandleGenerator;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.statement.FirebirdStatementIdGenerator;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.statement.FirebirdStatementResourceCleaner;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.statement.fetch.FirebirdFetchStatementCache;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.transaction.FirebirdTransactionIdGenerator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.sql.SQLException;
import java.util.Collection;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class FirebirdFreeStatementCommandExecutorTest {
    
    private static final int CONNECTION_ID = 1;
    
    private static final int STATEMENT_ID = 1;
    
    @Mock
    private FirebirdFreeStatementPacket packet;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ConnectionSession connectionSession;
    
    @Mock
    private ServerPreparedStatementRegistry registry;
    
    @Mock
    private ProxyBackendHandler proxyBackendHandler;
    
    @BeforeEach
    void setUp() {
        FirebirdTransactionIdGenerator.getInstance().registerConnection(CONNECTION_ID);
        FirebirdStatementIdGenerator.getInstance().registerConnection(CONNECTION_ID);
        FirebirdFetchStatementCache.getInstance().registerConnection(CONNECTION_ID);
        FirebirdFetchStatementCache.getInstance().registerStatement(CONNECTION_ID, STATEMENT_ID, proxyBackendHandler, connectionSession);
        FirebirdBatchRegistry.getInstance().registerConnection(CONNECTION_ID);
        FirebirdBatchRegistry.getInstance().registerBatchStatement(CONNECTION_ID, STATEMENT_ID, new FirebirdBatchStatement(STATEMENT_ID));
        when(packet.getStatementId()).thenReturn(STATEMENT_ID);
        when(connectionSession.getConnectionId()).thenReturn(CONNECTION_ID);
        when(connectionSession.getServerPreparedStatementRegistry()).thenReturn(registry);
    }
    
    @AfterEach
    void tearDown() {
        FirebirdTransactionIdGenerator.getInstance().unregisterConnection(CONNECTION_ID);
        FirebirdStatementIdGenerator.getInstance().unregisterConnection(CONNECTION_ID);
        FirebirdFetchStatementCache.getInstance().unregisterConnection(CONNECTION_ID);
        FirebirdBatchRegistry.getInstance().unregisterConnection(CONNECTION_ID);
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("preparedStatementFreeOptions")
    void assertExecuteWithPreparedStatementFreeOption(final String scenario, final int option) throws Exception {
        when(packet.getOption()).thenReturn(option);
        FirebirdFreeStatementCommandExecutor executor = new FirebirdFreeStatementCommandExecutor(packet, connectionSession);
        Collection<DatabasePacket> actual = executor.execute();
        assertThat(actual.iterator().next(), isA(FirebirdGenericResponsePacket.class));
        verify(registry).removePreparedStatement(STATEMENT_ID);
        verify(connectionSession).invalidatePreparedStatementCache(FirebirdStatementResourceCleaner.createPreparedStatementCacheKey(STATEMENT_ID));
        assertNull(FirebirdFetchStatementCache.getInstance().getFetchBackendHandler(CONNECTION_ID, STATEMENT_ID));
        assertNull(FirebirdBatchRegistry.getInstance().getBatchStatement(CONNECTION_ID, STATEMENT_ID));
    }
    
    @Test
    void assertExecuteWithClose() throws Exception {
        when(packet.getOption()).thenReturn(FirebirdFreeStatementPacket.CLOSE);
        new FirebirdFreeStatementCommandExecutor(packet, connectionSession).execute();
        verify(connectionSession, never()).invalidatePreparedStatementCache(any());
        verify(registry, never()).removePreparedStatement(STATEMENT_ID);
        assertNull(FirebirdFetchStatementCache.getInstance().getFetchBackendHandler(CONNECTION_ID, STATEMENT_ID));
        assertNull(FirebirdBatchRegistry.getInstance().getBatchStatement(CONNECTION_ID, STATEMENT_ID));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("preparedStatementFreeOptions")
    void assertExecuteWithPreparedStatementFreeOptionWithoutFetchHandler(final String scenario, final int option) throws Exception {
        FirebirdFetchStatementCache.getInstance().unregisterStatement(CONNECTION_ID, STATEMENT_ID);
        when(packet.getOption()).thenReturn(option);
        new FirebirdFreeStatementCommandExecutor(packet, connectionSession).execute();
        verify(registry).removePreparedStatement(STATEMENT_ID);
        verify(connectionSession).invalidatePreparedStatementCache(FirebirdStatementResourceCleaner.createPreparedStatementCacheKey(STATEMENT_ID));
    }
    
    @Test
    void assertFailedDropKeepsWireHandle() throws SQLException {
        int statementId = FirebirdStatementIdGenerator.getInstance().nextStatementId(CONNECTION_ID);
        when(packet.getStatementId()).thenReturn(statementId);
        when(packet.getOption()).thenReturn(FirebirdFreeStatementPacket.DROP);
        SQLException expected = new SQLException("foo_failure");
        doThrow(expected).when(proxyBackendHandler).close();
        FirebirdBlobHandleGenerator.getInstance().registerConnection(CONNECTION_ID);
        try {
            assertThat(assertThrows(SQLException.class, new FirebirdFreeStatementCommandExecutor(packet, connectionSession)::execute), sameInstance(expected));
            assertThat(FirebirdBlobHandleGenerator.getInstance().nextBlobHandle(CONNECTION_ID), is(2));
        } finally {
            FirebirdBlobHandleGenerator.getInstance().unregisterConnection(CONNECTION_ID);
        }
    }
    
    @Test
    void assertExecuteWithUnknownOption() {
        when(packet.getOption()).thenReturn(999);
        assertThrows(DatabaseProtocolException.class, new FirebirdFreeStatementCommandExecutor(packet, connectionSession)::execute);
    }
    
    private static Stream<Arguments> preparedStatementFreeOptions() {
        return Stream.of(
                Arguments.of("execute_dropCleansPreparedStatementAndFetchResources", FirebirdFreeStatementPacket.DROP),
                Arguments.of("execute_unprepareCleansPreparedStatementAndFetchResources", FirebirdFreeStatementPacket.UNPREPARE));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("freeHandleProvider")
    void assertExecuteResponseHandle(final String name, final int option, final int expectedHandle) throws SQLException {
        when(packet.getOption()).thenReturn(option);
        Collection<DatabasePacket> actual = new FirebirdFreeStatementCommandExecutor(packet, connectionSession).execute();
        assertThat(((FirebirdGenericResponsePacket) actual.iterator().next()).getHandle(), is(expectedHandle));
    }
    
    private static Stream<Arguments> freeHandleProvider() {
        return Stream.of(
                Arguments.of("drop", FirebirdFreeStatementPacket.DROP, 0xFFFF),
                Arguments.of("close", FirebirdFreeStatementPacket.CLOSE, STATEMENT_ID),
                Arguments.of("unprepare", FirebirdFreeStatementPacket.UNPREPARE, STATEMENT_ID));
    }
    
}
