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

package org.apache.shardingsphere.proxy.frontend.firebird.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.util.ReferenceCountUtil;
import org.apache.shardingsphere.database.protocol.codec.PacketCodec;
import org.apache.shardingsphere.database.protocol.constant.CommonConstants;
import org.apache.shardingsphere.database.protocol.firebird.constant.FirebirdConstant;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.FirebirdCommandPacketType;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.FirebirdBinaryColumnType;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.batch.FirebirdBatchColumnDescriptor;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.batch.FirebirdBatchRegistry;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.batch.FirebirdBatchStatement;
import org.apache.shardingsphere.proxy.backend.connector.ProxyDatabaseConnectionManager;
import org.apache.shardingsphere.proxy.frontend.event.WriteCompleteEvent;
import org.apache.shardingsphere.proxy.frontend.firebird.FirebirdFrontendEngine;
import org.apache.shardingsphere.proxy.frontend.firebird.command.admin.FirebirdCancellationRegistry;
import org.apache.shardingsphere.proxy.frontend.firebird.command.admin.FirebirdCancellationRegistry.Scope;
import org.apache.shardingsphere.proxy.frontend.netty.ProxyFlowControlHandler;
import org.firebirdsql.gds.BlrConstants;
import org.firebirdsql.gds.ISCConstants;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class FirebirdCancelInboundHandlerTest {
    
    private static final int CONNECTION_ID = 100002;
    
    @AfterEach
    void tearDown() {
        FirebirdCancellationRegistry.getInstance().unregister(CONNECTION_ID);
        FirebirdBatchRegistry.getInstance().unregisterConnection(CONNECTION_ID);
    }
    
    @Test
    void assertCancellationReachesActiveCommand() {
        List<Integer> commands = new ArrayList<>();
        EmbeddedChannel channel = createChannel(commands);
        channel.writeInbound(Unpooled.buffer().writeInt(FirebirdCommandPacketType.PING.getValue()));
        channel.writeInbound(Unpooled.buffer().writeInt(FirebirdCommandPacketType.CANCEL.getValue()).writeInt(ISCConstants.fb_cancel_raise));
        SQLException actual = assertThrows(SQLException.class, () -> FirebirdCancellationRegistry.getInstance().enter(CONNECTION_ID, mock(ProxyDatabaseConnectionManager.class)));
        assertThat(actual.getErrorCode(), is(ISCConstants.isc_cancelled));
        assertThat(commands, is(Arrays.asList(FirebirdCommandPacketType.PING.getValue())));
        channel.finishAndReleaseAll();
    }
    
    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3})
    void assertFragmentedHeaderWaitsForActiveCommand(final int split) throws SQLException {
        List<Integer> commands = new ArrayList<>();
        EmbeddedChannel channel = createChannel(commands);
        channel.writeInbound(Unpooled.buffer().writeInt(FirebirdCommandPacketType.PING.getValue()));
        ByteBuf packet = Unpooled.buffer().writeInt(FirebirdCommandPacketType.CANCEL.getValue()).writeInt(ISCConstants.fb_cancel_raise);
        channel.writeInbound(packet.readRetainedSlice(split));
        channel.writeInbound(packet.readRetainedSlice(packet.readableBytes()));
        packet.release();
        try (Scope ignored = FirebirdCancellationRegistry.getInstance().enter(CONNECTION_ID, mock(ProxyDatabaseConnectionManager.class))) {
            assertThat(commands, is(Arrays.asList(FirebirdCommandPacketType.PING.getValue())));
        }
        channel.pipeline().fireUserEventTriggered(new WriteCompleteEvent());
        channel.runPendingTasks();
        assertThat(commands, is(Arrays.asList(FirebirdCommandPacketType.PING.getValue(), FirebirdCommandPacketType.CANCEL.getValue())));
        assertTrue(channel.isActive());
        channel.finishAndReleaseAll();
    }
    
    @ParameterizedTest
    @ValueSource(ints = {4, 5, 6, 7})
    void assertFragmentedCancelBodyClosesAfterActiveResponse(final int split) {
        EmbeddedChannel channel = createChannel(new ArrayList<>());
        channel.writeInbound(Unpooled.buffer().writeInt(FirebirdCommandPacketType.PING.getValue()));
        ByteBuf packet = Unpooled.buffer().writeInt(FirebirdCommandPacketType.CANCEL.getValue()).writeInt(ISCConstants.fb_cancel_raise);
        ByteBuf first = packet.readRetainedSlice(split);
        channel.writeInbound(first);
        channel.writeInbound(packet.readRetainedSlice(packet.readableBytes()));
        packet.release();
        assertTrue(channel.isActive());
        channel.pipeline().fireUserEventTriggered(new WriteCompleteEvent());
        channel.runPendingTasks();
        assertFalse(channel.isActive());
        assertThat(first.refCnt(), is(0));
        channel.finishAndReleaseAll();
    }
    
    @Test
    void assertIncompleteCancelBodyDoesNotCloseBeforeRemainingBytes() {
        EmbeddedChannel channel = createChannel(new ArrayList<>());
        channel.writeInbound(Unpooled.buffer().writeInt(FirebirdCommandPacketType.PING.getValue()));
        ByteBuf packet = Unpooled.buffer().writeInt(FirebirdCommandPacketType.CANCEL.getValue()).writeInt(ISCConstants.fb_cancel_raise);
        ByteBuf first = packet.readRetainedSlice(5);
        channel.writeInbound(first);
        channel.pipeline().fireUserEventTriggered(new WriteCompleteEvent());
        channel.runPendingTasks();
        assertTrue(channel.isActive());
        channel.writeInbound(packet.readRetainedSlice(packet.readableBytes()));
        packet.release();
        assertFalse(channel.isActive());
        assertThat(first.refCnt(), is(0));
        channel.finishAndReleaseAll();
    }
    
    @Test
    void assertFragmentOfAnotherPacketIsNotCancellation() {
        List<Integer> commands = new ArrayList<>();
        EmbeddedChannel channel = createChannel(commands);
        channel.writeInbound(Unpooled.buffer().writeInt(FirebirdCommandPacketType.PING.getValue()));
        ByteBuf packet = Unpooled.buffer().writeInt(FirebirdCommandPacketType.TRANSACTION.getValue()).writeInt(0).writeInt(4).writeInt(FirebirdCommandPacketType.CANCEL.getValue());
        channel.writeInbound(packet.readRetainedSlice(12));
        channel.writeInbound(packet.readRetainedSlice(packet.readableBytes()));
        packet.release();
        channel.pipeline().fireUserEventTriggered(new WriteCompleteEvent());
        channel.runPendingTasks();
        assertThat(commands, is(Arrays.asList(FirebirdCommandPacketType.PING.getValue(), FirebirdCommandPacketType.TRANSACTION.getValue())));
        assertTrue(channel.isActive());
        channel.finishAndReleaseAll();
    }
    
    @Test
    void assertAbortClosesConnection() {
        EmbeddedChannel channel = createChannel(new ArrayList<>());
        channel.writeInbound(Unpooled.buffer().writeInt(FirebirdCommandPacketType.CANCEL.getValue()).writeInt(ISCConstants.fb_cancel_abort));
        assertFalse(channel.isActive());
        channel.finishAndReleaseAll();
    }
    
    @Test
    void assertAbortWaitsForActiveCommandResponse() {
        EmbeddedChannel channel = createChannel(new ArrayList<>());
        channel.writeInbound(Unpooled.buffer().writeInt(FirebirdCommandPacketType.PING.getValue()));
        channel.writeInbound(Unpooled.buffer().writeInt(FirebirdCommandPacketType.CANCEL.getValue()).writeInt(ISCConstants.fb_cancel_abort));
        assertTrue(channel.isActive());
        channel.pipeline().fireUserEventTriggered(new WriteCompleteEvent());
        channel.runPendingTasks();
        assertFalse(channel.isActive());
        channel.finishAndReleaseAll();
    }
    
    @Test
    void assertQueuedCommandsKeepOrder() {
        List<Integer> commands = new ArrayList<>();
        EmbeddedChannel channel = createChannel(commands);
        channel.writeInbound(Unpooled.buffer().writeInt(FirebirdCommandPacketType.PING.getValue()));
        channel.writeInbound(Unpooled.buffer().writeInt(FirebirdCommandPacketType.PING.getValue()));
        channel.writeInbound(Unpooled.buffer().writeInt(FirebirdCommandPacketType.CANCEL.getValue()).writeInt(ISCConstants.fb_cancel_disable));
        channel.pipeline().fireUserEventTriggered(new WriteCompleteEvent());
        channel.runPendingTasks();
        channel.pipeline().fireUserEventTriggered(new WriteCompleteEvent());
        channel.runPendingTasks();
        assertThat(commands, is(Arrays.asList(FirebirdCommandPacketType.PING.getValue(), FirebirdCommandPacketType.PING.getValue(), FirebirdCommandPacketType.CANCEL.getValue())));
        channel.finishAndReleaseAll();
    }
    
    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 20})
    void assertBatchMessageWaitsForBatchCreate(final int split) {
        List<Integer> commands = new ArrayList<>();
        EmbeddedChannel channel = createChannel(commands);
        channel.writeInbound(createBatchCreate());
        ByteBuf message = Unpooled.buffer().writeInt(FirebirdCommandPacketType.BATCH_MSG.getValue()).writeInt(7).writeInt(1).writeByte(0).writeZero(3).writeInt(42);
        channel.writeInbound(message.readRetainedSlice(split));
        if (message.isReadable()) {
            channel.writeInbound(message.readRetainedSlice(message.readableBytes()));
        }
        message.release();
        assertTrue(channel.isActive());
        assertThat(commands, is(Collections.singletonList(FirebirdCommandPacketType.BATCH_CREATE.getValue())));
        FirebirdBatchRegistry.getInstance().registerConnection(CONNECTION_ID);
        FirebirdBatchRegistry.getInstance().registerBatchStatement(CONNECTION_ID, 7, new FirebirdBatchStatement(7,
                Collections.singletonList(new FirebirdBatchColumnDescriptor(FirebirdBinaryColumnType.LONG, Integer.BYTES, 0, 0)), 256L * 1024 * 1024));
        channel.pipeline().fireUserEventTriggered(new WriteCompleteEvent());
        channel.runPendingTasks();
        assertTrue(channel.isActive());
        assertThat(commands, is(Arrays.asList(FirebirdCommandPacketType.BATCH_CREATE.getValue(), FirebirdCommandPacketType.BATCH_MSG.getValue())));
        channel.finishAndReleaseAll();
    }
    
    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4, 5, 6, 7, 8, 12})
    void assertContinuationOfCoalescedBatchMessageIsNotCancellation(final int split) {
        List<Integer> commands = new ArrayList<>();
        EmbeddedChannel channel = createChannel(commands);
        int statementId = FirebirdCommandPacketType.CANCEL.getValue();
        ByteBuf message = Unpooled.buffer().writeInt(FirebirdCommandPacketType.BATCH_MSG.getValue()).writeInt(statementId).writeInt(1).writeByte(0).writeZero(3).writeInt(42);
        channel.writeInbound(Unpooled.wrappedBuffer(createBatchCreate(statementId), message.readRetainedSlice(split)));
        if (4 == split) {
            channel.writeInbound(message.readRetainedSlice(4));
        }
        if (message.isReadable()) {
            channel.writeInbound(message.readRetainedSlice(message.readableBytes()));
        }
        message.release();
        FirebirdBatchRegistry.getInstance().registerConnection(CONNECTION_ID);
        FirebirdBatchRegistry.getInstance().registerBatchStatement(CONNECTION_ID, statementId, new FirebirdBatchStatement(statementId,
                Collections.singletonList(new FirebirdBatchColumnDescriptor(FirebirdBinaryColumnType.LONG, Integer.BYTES, 0, 0)), 256L * 1024 * 1024));
        channel.pipeline().fireUserEventTriggered(new WriteCompleteEvent());
        channel.runPendingTasks();
        assertTrue(channel.isActive());
        assertThat(commands, is(Arrays.asList(FirebirdCommandPacketType.BATCH_CREATE.getValue(), FirebirdCommandPacketType.BATCH_MSG.getValue())));
        channel.finishAndReleaseAll();
    }
    
    private ByteBuf createBatchCreate() {
        return createBatchCreate(7);
    }
    
    private ByteBuf createBatchCreate(final int statementId) {
        ByteBuf blr = Unpooled.buffer().writeByte(BlrConstants.blr_version5).writeByte(BlrConstants.blr_begin).writeByte(BlrConstants.blr_message).writeByte(0)
                .writeShortLE(2).writeByte(BlrConstants.blr_long).writeByte(0).writeByte(BlrConstants.blr_short).writeByte(0)
                .writeByte(BlrConstants.blr_end).writeByte(BlrConstants.blr_eoc);
        int length = blr.readableBytes();
        ByteBuf result = Unpooled.buffer().writeInt(FirebirdCommandPacketType.BATCH_CREATE.getValue()).writeInt(statementId)
                .writeInt(length).writeBytes(blr).writeZero((4 - length % 4) % 4).writeInt(6).writeInt(0);
        blr.release();
        return result;
    }
    
    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4})
    void assertContinuationAfterAllocateStatementIsNotCancellation(final int split) {
        List<Integer> commands = new ArrayList<>();
        EmbeddedChannel channel = createChannel(commands);
        ByteBuf next = Unpooled.buffer().writeInt(FirebirdCommandPacketType.TRANSACTION.getValue()).writeInt(FirebirdCommandPacketType.CANCEL.getValue())
                .writeInt(4).writeInt(FirebirdCommandPacketType.CANCEL.getValue());
        ByteBuf first = Unpooled.buffer().writeInt(FirebirdCommandPacketType.ALLOCATE_STATEMENT.getValue()).writeInt(0);
        channel.writeInbound(Unpooled.wrappedBuffer(first, next.readRetainedSlice(split)));
        if (4 == split) {
            channel.writeInbound(next.readRetainedSlice(4));
        }
        if (next.isReadable()) {
            channel.writeInbound(next.readRetainedSlice(next.readableBytes()));
        }
        next.release();
        channel.pipeline().fireUserEventTriggered(new WriteCompleteEvent());
        channel.runPendingTasks();
        assertTrue(channel.isActive());
        assertThat(commands, is(Arrays.asList(FirebirdCommandPacketType.ALLOCATE_STATEMENT.getValue(), FirebirdCommandPacketType.TRANSACTION.getValue())));
        channel.finishAndReleaseAll();
    }
    
    @Test
    void assertVoidAfterActiveCommandDoesNotPreventCancellation() {
        List<Integer> commands = new ArrayList<>();
        EmbeddedChannel channel = createChannel(commands);
        channel.writeInbound(Unpooled.buffer().writeInt(FirebirdCommandPacketType.PING.getValue()).writeInt(FirebirdCommandPacketType.VOID.getValue()));
        channel.writeInbound(Unpooled.buffer().writeInt(FirebirdCommandPacketType.CANCEL.getValue()).writeInt(ISCConstants.fb_cancel_raise));
        SQLException actual = assertThrows(SQLException.class, () -> FirebirdCancellationRegistry.getInstance().enter(CONNECTION_ID, mock(ProxyDatabaseConnectionManager.class)));
        assertThat(actual.getErrorCode(), is(ISCConstants.isc_cancelled));
        channel.finishAndReleaseAll();
    }
    
    @Test
    void assertVoidLeadingAReadDoesNotPreventLaterCancellation() {
        List<Integer> commands = new ArrayList<>();
        EmbeddedChannel channel = createChannel(commands);
        channel.writeInbound(Unpooled.buffer().writeInt(FirebirdCommandPacketType.VOID.getValue()).writeInt(FirebirdCommandPacketType.PING.getValue()));
        channel.pipeline().fireUserEventTriggered(new WriteCompleteEvent());
        channel.runPendingTasks();
        channel.writeInbound(Unpooled.buffer().writeInt(FirebirdCommandPacketType.PING.getValue()));
        channel.writeInbound(Unpooled.buffer().writeInt(FirebirdCommandPacketType.CANCEL.getValue()).writeInt(ISCConstants.fb_cancel_raise));
        SQLException actual = assertThrows(SQLException.class, () -> FirebirdCancellationRegistry.getInstance().enter(CONNECTION_ID, mock(ProxyDatabaseConnectionManager.class)));
        assertThat(actual.getErrorCode(), is(ISCConstants.isc_cancelled));
        channel.finishAndReleaseAll();
    }
    
    @Test
    void assertPendingForwardedFragmentIsReleasedOnClose() {
        EmbeddedChannel channel = createChannel(new ArrayList<>());
        ByteBuf input = Unpooled.buffer().writeInt(FirebirdCommandPacketType.PING.getValue()).writeByte(0);
        channel.writeInbound(input);
        channel.close();
        assertThat(input.refCnt(), is(0));
        channel.finishAndReleaseAll();
    }
    
    @ParameterizedTest
    @ValueSource(ints = {1, 2, 4})
    void assertDeferredInputReleasedOnClose(final int length) {
        EmbeddedChannel channel = createChannel(new ArrayList<>());
        channel.writeInbound(Unpooled.buffer().writeInt(FirebirdCommandPacketType.PING.getValue()));
        ByteBuf deferred = Unpooled.buffer().writeInt(FirebirdCommandPacketType.PING.getValue()).writerIndex(length);
        channel.writeInbound(deferred);
        assertThat(deferred.refCnt(), is(1));
        channel.close();
        assertThat(deferred.refCnt(), is(0));
        channel.finishAndReleaseAll();
    }
    
    @Test
    void assertCompleteCancellationBeforeDeferredCommand() {
        List<Integer> commands = new ArrayList<>();
        EmbeddedChannel channel = createChannel(commands);
        channel.writeInbound(Unpooled.buffer().writeInt(FirebirdCommandPacketType.PING.getValue()));
        channel.writeInbound(Unpooled.buffer().writeInt(FirebirdCommandPacketType.CANCEL.getValue()).writeInt(ISCConstants.fb_cancel_disable)
                .writeInt(FirebirdCommandPacketType.CANCEL.getValue()).writeInt(ISCConstants.fb_cancel_enable)
                .writeInt(FirebirdCommandPacketType.CANCEL.getValue()).writeInt(ISCConstants.fb_cancel_raise).writeInt(FirebirdCommandPacketType.PING.getValue()));
        SQLException actual = assertThrows(SQLException.class, () -> FirebirdCancellationRegistry.getInstance().enter(CONNECTION_ID, mock(ProxyDatabaseConnectionManager.class)));
        assertThat(actual.getErrorCode(), is(ISCConstants.isc_cancelled));
        assertThat(commands, is(Collections.singletonList(FirebirdCommandPacketType.PING.getValue())));
        channel.pipeline().fireUserEventTriggered(new WriteCompleteEvent());
        channel.runPendingTasks();
        assertThat(commands, is(Arrays.asList(FirebirdCommandPacketType.PING.getValue(), FirebirdCommandPacketType.PING.getValue())));
        channel.finishAndReleaseAll();
    }
    
    private EmbeddedChannel createChannel(final List<Integer> commands) {
        FirebirdFrontendEngine frontendEngine = new FirebirdFrontendEngine();
        EmbeddedChannel result = new EmbeddedChannel(new PacketCodec(frontendEngine.getCodecEngine()));
        result.pipeline().addLast(ProxyFlowControlHandler.class.getSimpleName(), new ProxyFlowControlHandler());
        frontendEngine.initChannel(result);
        result.pipeline().addLast(new ChannelInboundHandlerAdapter() {
            
            @Override
            public void channelRead(final ChannelHandlerContext context, final Object message) {
                ByteBuf buffer = (ByteBuf) message;
                commands.add(buffer.getInt(buffer.readerIndex()));
                context.channel().config().setAutoRead(false);
                ReferenceCountUtil.release(message);
            }
        });
        result.attr(FirebirdConstant.CURRENT_CONNECTION).set(CONNECTION_ID);
        result.attr(CommonConstants.CHARSET_ATTRIBUTE_KEY).set(StandardCharsets.UTF_8);
        return result;
    }
}
