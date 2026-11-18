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

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.embedded.EmbeddedChannel;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.exception.core.exception.transaction.InTransactionException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.InvalidBatchHandleException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.InvalidBatchStatementTypeException;
import org.apache.shardingsphere.database.protocol.codec.PacketCodec;
import org.apache.shardingsphere.database.protocol.constant.CommonConstants;
import org.apache.shardingsphere.database.protocol.firebird.codec.FirebirdPacketCodecEngine;
import org.apache.shardingsphere.database.protocol.firebird.constant.FirebirdConstant;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.FirebirdCommandPacketType;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.transaction.FirebirdCommitTransactionPacket;
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.session.transaction.TransactionStatus;
import org.apache.shardingsphere.proxy.frontend.event.WriteCompleteEvent;
import org.apache.shardingsphere.proxy.frontend.firebird.authentication.FirebirdAuthenticationEngine;
import org.apache.shardingsphere.proxy.frontend.firebird.command.FirebirdCommandExecuteEngine;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.transaction.FirebirdCommitTransactionCommandExecutor;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.transaction.FirebirdTransactionIdGenerator;
import org.apache.shardingsphere.proxy.frontend.firebird.resource.FirebirdConnectionResourceManager;
import org.apache.shardingsphere.proxy.frontend.netty.ProxyFlowControlHandler;
import org.apache.shardingsphere.proxy.frontend.spi.DatabaseProtocolFrontendEngine;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.firebirdsql.gds.ISCConstants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;

import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
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
    
    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12})
    void assertCommitBeforeRejectedPing(final int firstReadLength) throws SQLException {
        int connectionId = 7091;
        FirebirdTransactionIdGenerator transactions = FirebirdTransactionIdGenerator.getInstance();
        transactions.registerConnection(connectionId);
        AtomicBoolean delayFlush = new AtomicBoolean();
        EmbeddedChannel channel = new EmbeddedChannel(new ChannelOutboundHandlerAdapter() {
            
            @Override
            public void flush(final ChannelHandlerContext context) {
                if (!delayFlush.get()) {
                    context.flush();
                }
            }
        }, new PacketCodec(engine.getCodecEngine()));
        try {
            channel.attr(CommonConstants.CHARSET_ATTRIBUTE_KEY).set(StandardCharsets.UTF_8);
            channel.attr(FirebirdConstant.CURRENT_CONNECTION).set(connectionId);
            channel.pipeline().addLast(ProxyFlowControlHandler.class.getSimpleName(), new ProxyFlowControlHandler());
            engine.initChannel(channel);
            channel.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                
                @Override
                public void channelRead(final ChannelHandlerContext context, final Object message) {
                    context.channel().config().setAutoRead(false);
                    context.fireChannelRead(message);
                }
            });
            ByteBuf commands = Unpooled.buffer().writeInt(FirebirdCommandPacketType.COMMIT.getValue()).writeInt(transactions.nextTransactionId(connectionId));
            commands.writeInt(FirebirdCommandPacketType.PING.getValue());
            if (firstReadLength < commands.readableBytes()) {
                channel.writeInbound(commands.readRetainedSlice(firstReadLength));
            }
            channel.writeInbound(commands);
            assertTrue(channel.isOpen());
            assertNull(channel.readOutbound());
            ByteBuf actualFrame = channel.readInbound();
            try {
                assertThat(actualFrame.getInt(0), is(FirebirdCommandPacketType.COMMIT.getValue()));
                FirebirdCommitTransactionPacket packet = new FirebirdCommitTransactionPacket(new FirebirdPacketPayload(actualFrame, StandardCharsets.UTF_8));
                when(connectionSession.getConnectionId()).thenReturn(connectionId);
                when(connectionSession.isAutoCommit()).thenReturn(true);
                channel.writeOutbound(new FirebirdCommitTransactionCommandExecutor(packet, connectionSession).execute().iterator().next());
            } finally {
                actualFrame.release();
            }
            ByteBuf actualCommitResponse = channel.readOutbound();
            assertThat(actualCommitResponse.readInt(), is(FirebirdCommandPacketType.RESPONSE.getValue()));
            assertThat(actualCommitResponse.getInt(20), is(ISCConstants.isc_arg_end));
            actualCommitResponse.release();
            assertNull(channel.readOutbound());
            assertTrue(channel.isOpen());
            delayFlush.set(true);
            channel.pipeline().fireUserEventTriggered(new WriteCompleteEvent());
            channel.runPendingTasks();
            assertTrue(channel.isOpen());
            assertNull(channel.readOutbound());
            delayFlush.set(false);
            channel.flushOutbound();
            channel.runPendingTasks();
            ByteBuf actualError = channel.readOutbound();
            assertThat(actualError.readInt(), is(FirebirdCommandPacketType.RESPONSE.getValue()));
            assertThat(actualError.toString(StandardCharsets.UTF_8), containsString("Operation PING is not implemented."));
            actualError.release();
            assertFalse(channel.isOpen());
        } finally {
            channel.finishAndReleaseAll();
            transactions.unregisterConnection(connectionId);
        }
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
    void assertHandleException(final String testName, final boolean inTransaction, final boolean exceptionOccur, final Exception exception, final boolean expectedExceptionOccur) {
        ConnectionContext connectionContext = mockConnectionSession(inTransaction, exceptionOccur);
        engine.handleException(connectionSession, exception);
        assertThat(connectionContext.getTransactionContext().isExceptionOccur(), is(expectedExceptionOccur));
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
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("recoverableExceptionArguments")
    void assertHandleExceptionWithRecoverableError(final Exception exception) {
        ConnectionContext actual = mockConnectionSession(true, false);
        engine.handleException(connectionSession, exception);
        assertFalse(actual.getTransactionContext().isExceptionOccur());
    }
    
    private static Stream<Arguments> provideHandleExceptionParameters() {
        return Stream.of(
                Arguments.of("mark when in transaction", true, false, new Exception("error"), true),
                Arguments.of("skip when not in transaction", false, false, new Exception("error"), false),
                Arguments.of("keep marked when already occurred", true, true, new Exception("error"), true));
    }
    
    private static Stream<Arguments> recoverableExceptionArguments() {
        return Stream.of(Arguments.of(new InTransactionException()), Arguments.of(new InvalidBatchStatementTypeException()), Arguments.of(new InvalidBatchHandleException(1)));
    }
}
