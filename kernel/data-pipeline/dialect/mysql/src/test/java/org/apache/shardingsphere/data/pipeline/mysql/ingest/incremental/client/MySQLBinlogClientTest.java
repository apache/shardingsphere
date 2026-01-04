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

package org.apache.shardingsphere.data.pipeline.mysql.ingest.incremental.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.util.Attribute;
import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.Promise;
import lombok.SneakyThrows;
import org.apache.shardingsphere.data.pipeline.core.exception.PipelineInternalException;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.incremental.binlog.MySQLBinlogContext;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.incremental.binlog.event.MySQLBaseBinlogEvent;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.incremental.binlog.event.PlaceholderBinlogEvent;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.incremental.client.netty.MySQLBinlogEventPacketDecoder;
import org.apache.shardingsphere.database.protocol.mysql.constant.MySQLConstants;
import org.apache.shardingsphere.database.protocol.mysql.packet.command.binlog.MySQLComBinlogDumpCommandPacket;
import org.apache.shardingsphere.database.protocol.mysql.packet.command.binlog.MySQLComRegisterSlaveCommandPacket;
import org.apache.shardingsphere.database.protocol.mysql.packet.command.query.text.MySQLTextResultSetRowPacket;
import org.apache.shardingsphere.database.protocol.mysql.packet.command.query.text.query.MySQLComQueryPacket;
import org.apache.shardingsphere.database.protocol.mysql.packet.generic.MySQLErrPacket;
import org.apache.shardingsphere.database.protocol.mysql.packet.generic.MySQLOKPacket;
import org.apache.shardingsphere.infra.exception.generic.UnsupportedSQLOperationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.internal.configuration.plugins.Plugins;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.net.InetSocketAddress;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

@SuppressWarnings("ProhibitedExceptionDeclared")
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MySQLBinlogClientTest {
    
    @Mock
    private Channel channel;
    
    @Mock
    private ChannelPipeline pipeline;
    
    @Mock
    private ChannelFuture channelFuture;
    
    private final ConnectInfo connectInfo = new ConnectInfo(1, "host", 3306, "username", "password");
    
    private MySQLBinlogClient client;
    
    private final NioEventLoopGroup eventLoopGroup = new NioEventLoopGroup(1);
    
    @BeforeEach
    void setUp() {
        client = new MySQLBinlogClient(connectInfo, false);
        when(channel.pipeline()).thenReturn(pipeline);
        when(channel.isOpen()).thenReturn(true);
        when(channel.close()).thenReturn(channelFuture);
        when(channel.localAddress()).thenReturn(new InetSocketAddress("host", 3306));
        when(channel.attr(MySQLConstants.SEQUENCE_ID_ATTRIBUTE_KEY)).thenReturn(mock(Attribute.class));
        when(channel.attr(MySQLConstants.SEQUENCE_ID_ATTRIBUTE_KEY).get()).thenReturn(new AtomicInteger());
        when(pipeline.addLast(any(ChannelHandler.class))).thenReturn(pipeline);
        when(pipeline.fireChannelRegistered()).thenReturn(pipeline);
    }
    
    @AfterEach
    void tearDown() {
        eventLoopGroup.shutdownGracefully();
        Thread.interrupted();
    }
    
    @Test
    void assertConnect() throws Exception {
        MySQLServerVersion expected = new MySQLServerVersion("5.5.0-log");
        AtomicReference<ChannelInitializer<SocketChannel>> initializer = new AtomicReference<>();
        mockChannelResponse(expected);
        try (MockedConstruction<Bootstrap> ignored = mockConstruction(Bootstrap.class, (mock, context) -> {
            when(mock.group(any())).thenReturn(mock);
            when(mock.channel(any())).thenReturn(mock);
            when(mock.option(any(), any())).thenReturn(mock);
            when(mock.handler(any())).thenAnswer(invocation -> {
                initializer.set(invocation.getArgument(0));
                return mock;
            });
            when(mock.connect(anyString(), anyInt())).thenReturn(channelFuture);
        })) {
            client.connect();
        }
        ChannelHandlerContext context = mock(ChannelHandlerContext.class, RETURNS_DEEP_STUBS);
        when(context.executor()).thenReturn(eventLoopGroup.next());
        when(context.pipeline()).thenReturn(pipeline);
        initializer.get().channelRegistered(context);
        MySQLServerVersion actual = (MySQLServerVersion) Plugins.getMemberAccessor().get(MySQLBinlogClient.class.getDeclaredField("serverVersion"), client);
        assertThat(actual, is(expected));
        client.closeChannel(true);
    }
    
    @Test
    void assertExecuteSuccess() {
        prepareClientChannel();
        mockChannelResponse(new MySQLOKPacket(0));
        assertTrue(client.execute("sql"));
        verify(channel).writeAndFlush(any(MySQLComQueryPacket.class));
    }
    
    @Test
    void assertNullResponseHandling() {
        prepareClientChannel();
        mockChannelResponse(null);
        assertFalse(client.execute("sql"));
        PipelineInternalException updateException = assertThrows(PipelineInternalException.class, () -> client.executeUpdate("update"));
        assertThat(updateException.getMessage(), is("Could not get MySQL OK packet"));
        PipelineInternalException queryException = assertThrows(PipelineInternalException.class, () -> client.executeQuery("query"));
        assertThat(queryException.getMessage(), is("Could not get MySQL FieldCount/ColumnDefinition/TextResultSetRow packet"));
    }
    
    @Test
    void assertExecuteThrowsWhenErrorResponse() {
        prepareClientChannel();
        mockChannelResponse(new MySQLErrPacket(new SQLException("err", "state", 1)));
        PipelineInternalException ex = assertThrows(PipelineInternalException.class, () -> client.execute("sql"));
        assertThat(ex.getMessage(), is("err"));
    }
    
    @Test
    void assertExecuteQueryThrowsWhenUnexpectedResponse() {
        prepareClientChannel();
        mockChannelResponse(new Object());
        PipelineInternalException ex = assertThrows(PipelineInternalException.class, () -> client.executeQuery("query"));
        assertThat(ex.getMessage(), is("unexpected response type"));
    }
    
    @Test
    void assertExecuteUpdateReturnsAffectedRows() {
        prepareClientChannel();
        doAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            Promise<Object> callback = (Promise<Object>) Plugins.getMemberAccessor().get(MySQLBinlogClient.class.getDeclaredField("responseCallback"), client);
            callback.setSuccess(new MySQLOKPacket(5L, 0L, 0));
            return null;
        }).when(channel).writeAndFlush(any(MySQLComQueryPacket.class));
        assertThat(client.executeUpdate("update"), is(5));
    }
    
    @Test
    void assertExecuteQueryThrowsWhenInterrupted() throws Exception {
        prepareClientChannel();
        Promise<Object> interruptedPromise = mock(Promise.class);
        when(interruptedPromise.get(5L, TimeUnit.SECONDS)).thenThrow(new InterruptedException());
        doAnswer(invocation -> {
            setResponseCallback(interruptedPromise);
            return null;
        }).when(channel).writeAndFlush(any(MySQLComQueryPacket.class));
        assertThrows(PipelineInternalException.class, () -> client.executeQuery("query"));
        assertTrue(Thread.currentThread().isInterrupted());
    }
    
    @Test
    void assertExecuteThrowsWhenTimeout() throws Exception {
        prepareClientChannel();
        Promise<Object> timeoutPromise = mock(Promise.class);
        when(timeoutPromise.get(5L, TimeUnit.SECONDS)).thenThrow(new TimeoutException());
        doAnswer(invocation -> {
            setResponseCallback(timeoutPromise);
            return null;
        }).when(channel).writeAndFlush(any(MySQLComQueryPacket.class));
        PipelineInternalException actual = assertThrows(PipelineInternalException.class, () -> client.execute("sql"));
        assertThat(actual.getCause(), isA(TimeoutException.class));
    }
    
    @Test
    void assertExecuteQueryReturnsResultSet() {
        prepareClientChannel();
        InternalResultSet expected = new InternalResultSet(null);
        doAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            Promise<Object> callback = (Promise<Object>) Plugins.getMemberAccessor().get(MySQLBinlogClient.class.getDeclaredField("responseCallback"), client);
            callback.setSuccess(expected);
            return null;
        }).when(channel).writeAndFlush(any(MySQLComQueryPacket.class));
        assertThat(client.executeQuery("select"), is(expected));
    }
    
    @Test
    void assertSubscribeChecksumLengthBranches() {
        client = createClientMock();
        prepareClientChannel();
        setServerVersion("5.6.0");
        doReturn(true).when(client).execute(anyString());
        doReturn(createResultSet("NONE")).doReturn(createResultSet("CRC32")).when(client).executeQuery(anyString());
        when(channel.writeAndFlush(any(MySQLComRegisterSlaveCommandPacket.class))).thenAnswer(invocation -> {
            Promise<Object> callback = new DefaultPromise<>(eventLoopGroup.next());
            callback.setSuccess(new MySQLOKPacket(0));
            setResponseCallback(callback);
            return null;
        });
        doAnswer(invocation -> null).when(channel).writeAndFlush(any(MySQLComBinlogDumpCommandPacket.class));
        client.subscribe("binlog-000001", 4L);
        client.subscribe("binlog-000002", 8L);
        ArgumentCaptor<ChannelHandler> captor = ArgumentCaptor.forClass(ChannelHandler.class);
        verify(pipeline, times(4)).addLast(captor.capture());
        List<Integer> checksumLengths = captor.getAllValues().stream()
                .filter(each -> each instanceof MySQLBinlogEventPacketDecoder)
                .map(each -> getChecksumLength((MySQLBinlogEventPacketDecoder) each))
                .collect(Collectors.toList());
        assertThat(checksumLengths, is(Arrays.asList(0, 4)));
    }
    
    @Test
    void assertSubscribeUnsupportedChecksumThrows() {
        client = createClientMock();
        prepareClientChannel();
        setServerVersion("5.6.0");
        doReturn(true).when(client).execute(anyString());
        doReturn(createResultSet("SHA1")).when(client).executeQuery(anyString());
        when(channel.writeAndFlush(any(MySQLComRegisterSlaveCommandPacket.class))).thenAnswer(invocation -> {
            Promise<Object> callback = new DefaultPromise<>(eventLoopGroup.next());
            callback.setSuccess(new MySQLOKPacket(0));
            setResponseCallback(callback);
            return null;
        });
        assertThrows(UnsupportedSQLOperationException.class, () -> client.subscribe("binlog", 4L));
    }
    
    @Test
    void assertSubscribeBelow56UsesZeroChecksum() {
        client = createClientMock();
        prepareClientChannel();
        setServerVersion("5.5.0");
        doReturn(true).when(client).execute(anyString());
        when(channel.writeAndFlush(any(MySQLComRegisterSlaveCommandPacket.class))).thenAnswer(invocation -> {
            Promise<Object> callback = new DefaultPromise<>(eventLoopGroup.next());
            callback.setSuccess(new MySQLOKPacket(0));
            setResponseCallback(callback);
            return null;
        });
        doAnswer(invocation -> null).when(channel).writeAndFlush(any(MySQLComBinlogDumpCommandPacket.class));
        client.subscribe("binlog-000004", 16L);
        ArgumentCaptor<ChannelHandler> captor = ArgumentCaptor.forClass(ChannelHandler.class);
        verify(pipeline, times(2)).addLast(captor.capture());
        MySQLBinlogEventPacketDecoder decoder = captor.getAllValues().stream()
                .filter(each -> each instanceof MySQLBinlogEventPacketDecoder)
                .map(MySQLBinlogEventPacketDecoder.class::cast).findFirst().orElseThrow(IllegalStateException::new);
        assertThat(getChecksumLength(decoder), is(0));
    }
    
    @Test
    void assertPollOnNotRunning() throws ReflectiveOperationException {
        Plugins.getMemberAccessor().set(MySQLBinlogClient.class.getDeclaredField("channel"), client, channel);
        setRunning(false);
        assertThrows(RuntimeException.class, () -> client.poll());
    }
    
    @Test
    void assertPollOnNotReady() throws ReflectiveOperationException {
        Plugins.getMemberAccessor().set(MySQLBinlogClient.class.getDeclaredField("channel"), client, channel);
        setRunning(true);
        setReady(false);
        assertThat(client.poll(), is(Collections.emptyList()));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    void assertPollBranches() throws InterruptedException, ReflectiveOperationException {
        setRunning(true);
        setReady(false);
        assertThat(client.poll(), is(Collections.emptyList()));
        setReady(true);
        assertThat(client.poll(), is(Collections.emptyList()));
        List<MySQLBaseBinlogEvent> events = Collections.singletonList(new PlaceholderBinlogEvent("binlog", 4L, 1L));
        ((ArrayBlockingQueue<List<MySQLBaseBinlogEvent>>) Plugins.getMemberAccessor().get(MySQLBinlogClient.class.getDeclaredField("blockingEventQueue"), client)).put(events);
        assertThat(client.poll(), is(events));
        setReady(true);
        Thread.currentThread().interrupt();
        assertThat(client.poll(), is(Collections.emptyList()));
    }
    
    @Test
    void assertCloseChannelWhenChannelUnavailable() {
        assertFalse(client.closeChannel(true).isPresent());
    }
    
    @Test
    void assertCloseChannelWithoutEventLoopGroup() throws Exception {
        Plugins.getMemberAccessor().set(MySQLBinlogClient.class.getDeclaredField("channel"), client, channel);
        Optional<ChannelFuture> actual = client.closeChannel(true);
        assertTrue(actual.isPresent());
        verify(channel).close();
    }
    
    @Test
    void assertMySQLCommandResponseHandlerBranches() throws Exception {
        AtomicReference<ChannelInitializer<SocketChannel>> initializer = new AtomicReference<>();
        SocketChannel socketChannel = mock(SocketChannel.class, RETURNS_DEEP_STUBS);
        when(socketChannel.pipeline()).thenReturn(pipeline);
        when(channelFuture.channel()).thenReturn(channel);
        mockChannelResponse(new MySQLServerVersion("5.5.0-log"));
        try (MockedConstruction<Bootstrap> ignored = mockConstruction(Bootstrap.class, (mock, context) -> {
            when(mock.group(any())).thenReturn(mock);
            when(mock.channel(any())).thenReturn(mock);
            when(mock.option(any(), any())).thenReturn(mock);
            when(mock.handler(any())).thenAnswer(invocation -> {
                initializer.set(invocation.getArgument(0));
                return mock;
            });
            when(mock.connect(anyString(), anyInt())).thenReturn(channelFuture);
        })) {
            client.connect();
        }
        ChannelHandlerContext context = mock(ChannelHandlerContext.class);
        when(context.executor()).thenReturn(eventLoopGroup.next());
        when(context.channel()).thenReturn(socketChannel);
        when(context.pipeline()).thenReturn(pipeline);
        initializer.get().channelRegistered(context);
        ArgumentCaptor<ChannelHandler> captor = ArgumentCaptor.forClass(ChannelHandler.class);
        verify(pipeline, times(7)).addLast(captor.capture());
        ChannelInboundHandlerAdapter handler = captor.getAllValues().stream()
                .filter(each -> "MySQLCommandResponseHandler".equals(each.getClass().getSimpleName()))
                .map(each -> (ChannelInboundHandlerAdapter) each).findFirst().orElseThrow(IllegalStateException::new);
        Promise<Object> callback = new DefaultPromise<>(eventLoopGroup.next());
        setResponseCallback(callback);
        handler.channelRead(mock(ChannelHandlerContext.class), new Object());
        assertTrue(callback.isSuccess());
        callback = new DefaultPromise<>(eventLoopGroup.next());
        setResponseCallback(callback);
        handler.exceptionCaught(mock(ChannelHandlerContext.class), new RuntimeException("ex"));
        assertTrue(callback.isDone());
        setResponseCallback(null);
        handler.channelRead(mock(ChannelHandlerContext.class), new Object());
        handler.exceptionCaught(mock(ChannelHandlerContext.class), new RuntimeException("ex"));
        client.closeChannel(true);
    }
    
    private InternalResultSet createResultSet(final String checksum) {
        InternalResultSet result = new InternalResultSet(null);
        result.getFieldValues().add(new MySQLTextResultSetRowPacket(Collections.singletonList(checksum)));
        return result;
    }
    
    private MySQLBinlogClient createClientMock() {
        return mock(MySQLBinlogClient.class, withSettings().useConstructor(connectInfo, false).defaultAnswer(CALLS_REAL_METHODS));
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private void prepareClientChannel() {
        Plugins.getMemberAccessor().set(MySQLBinlogClient.class.getDeclaredField("channel"), client, channel);
        Plugins.getMemberAccessor().set(MySQLBinlogClient.class.getDeclaredField("eventLoopGroup"), client, eventLoopGroup);
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private void setServerVersion(final String version) {
        Plugins.getMemberAccessor().set(MySQLBinlogClient.class.getDeclaredField("serverVersion"), client, new MySQLServerVersion(version));
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private void setResponseCallback(final Promise<Object> promise) {
        Plugins.getMemberAccessor().set(MySQLBinlogClient.class.getDeclaredField("responseCallback"), client, promise);
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private void setRunning(final boolean value) {
        Plugins.getMemberAccessor().set(MySQLBinlogClient.class.getDeclaredField("running"), client, value);
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private void setReady(final boolean value) {
        Plugins.getMemberAccessor().set(MySQLBinlogClient.class.getDeclaredField("ready"), client, value);
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private int getChecksumLength(final MySQLBinlogEventPacketDecoder decoder) {
        MySQLBinlogContext binlogContext = (MySQLBinlogContext) Plugins.getMemberAccessor().get(MySQLBinlogEventPacketDecoder.class.getDeclaredField("binlogContext"), decoder);
        return binlogContext.getChecksumLength();
    }
    
    private void mockChannelResponse(final Object response) {
        new Thread(() -> mockChannelResponseInThread(response)).start();
    }
    
    @SneakyThrows(InterruptedException.class)
    @SuppressWarnings("unchecked")
    private void mockChannelResponseInThread(final Object response) {
        long startMillis = System.currentTimeMillis();
        do {
            Promise<Object> callback;
            try {
                callback = (Promise<Object>) Plugins.getMemberAccessor().get(MySQLBinlogClient.class.getDeclaredField("responseCallback"), client);
            } catch (final ReflectiveOperationException ex) {
                throw new RuntimeException(ex);
            }
            if (null != callback && !callback.isDone()) {
                callback.setSuccess(response);
            }
            TimeUnit.MILLISECONDS.sleep(100L);
        } while (System.currentTimeMillis() - startMillis <= TimeUnit.SECONDS.toMillis(20L));
    }
}
