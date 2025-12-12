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

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.Attribute;
import io.netty.util.concurrent.Promise;
import lombok.SneakyThrows;
import org.apache.shardingsphere.database.protocol.mysql.constant.MySQLConstants;
import org.apache.shardingsphere.database.protocol.mysql.packet.command.binlog.MySQLComBinlogDumpCommandPacket;
import org.apache.shardingsphere.database.protocol.mysql.packet.command.binlog.MySQLComRegisterSlaveCommandPacket;
import org.apache.shardingsphere.database.protocol.mysql.packet.command.query.text.query.MySQLComQueryPacket;
import org.apache.shardingsphere.database.protocol.mysql.packet.generic.MySQLOKPacket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.internal.configuration.plugins.Plugins;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MySQLBinlogClientTest {
    
    @Mock
    private Channel channel;
    
    @Mock
    private ChannelPipeline pipeline;
    
    @Mock
    private ChannelFuture channelFuture;
    
    private MySQLBinlogClient client;
    
    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        client = new MySQLBinlogClient(new ConnectInfo(1, "host", 3306, "username", "password"), false);
        when(channel.pipeline()).thenReturn(pipeline);
        when(channel.isOpen()).thenReturn(true);
        when(channel.close()).thenReturn(channelFuture);
        when(channel.localAddress()).thenReturn(new InetSocketAddress("host", 3306));
        when(channel.attr(MySQLConstants.SEQUENCE_ID_ATTRIBUTE_KEY)).thenReturn(mock(Attribute.class));
        when(channel.attr(MySQLConstants.SEQUENCE_ID_ATTRIBUTE_KEY).get()).thenReturn(new AtomicInteger());
    }
    
    @Test
    void assertConnect() throws ReflectiveOperationException {
        MySQLServerVersion expected = new MySQLServerVersion("5.5.0-log");
        mockChannelResponse(expected);
        client.connect();
        MySQLServerVersion actual = (MySQLServerVersion) Plugins.getMemberAccessor().get(MySQLBinlogClient.class.getDeclaredField("serverVersion"), client);
        assertThat(actual, is(expected));
    }
    
    @Test
    void assertExecute() throws ReflectiveOperationException {
        mockChannelResponse(new MySQLOKPacket(0));
        Plugins.getMemberAccessor().set(MySQLBinlogClient.class.getDeclaredField("channel"), client, channel);
        Plugins.getMemberAccessor().set(MySQLBinlogClient.class.getDeclaredField("eventLoopGroup"), client, new NioEventLoopGroup(1));
        assertTrue(client.execute(""));
        verify(channel).writeAndFlush(ArgumentMatchers.any(MySQLComQueryPacket.class));
    }
    
    @Test
    void assertExecuteUpdate() throws ReflectiveOperationException {
        MySQLOKPacket expected = new MySQLOKPacket(10L, 0L, 0);
        Plugins.getMemberAccessor().set(MySQLOKPacket.class.getDeclaredField("affectedRows"), expected, 10L);
        mockChannelResponse(expected);
        Plugins.getMemberAccessor().set(MySQLBinlogClient.class.getDeclaredField("channel"), client, channel);
        Plugins.getMemberAccessor().set(MySQLBinlogClient.class.getDeclaredField("eventLoopGroup"), client, new NioEventLoopGroup(1));
        assertThat(client.executeUpdate(""), is(10));
        verify(channel).writeAndFlush(ArgumentMatchers.any(MySQLComQueryPacket.class));
    }
    
    @Test
    void assertExecuteQuery() throws ReflectiveOperationException {
        InternalResultSet expected = new InternalResultSet(null);
        mockChannelResponse(expected);
        Plugins.getMemberAccessor().set(MySQLBinlogClient.class.getDeclaredField("channel"), client, channel);
        Plugins.getMemberAccessor().set(MySQLBinlogClient.class.getDeclaredField("eventLoopGroup"), client, new NioEventLoopGroup(1));
        assertThat(client.executeQuery(""), is(expected));
        verify(channel).writeAndFlush(ArgumentMatchers.any(MySQLComQueryPacket.class));
    }
    
    @Test
    void assertSubscribeBelow56Version() throws ReflectiveOperationException {
        MySQLServerVersion serverInfo = new MySQLServerVersion("5.5.0-log");
        Plugins.getMemberAccessor().set(MySQLBinlogClient.class.getDeclaredField("serverVersion"), client, serverInfo);
        Plugins.getMemberAccessor().set(MySQLBinlogClient.class.getDeclaredField("channel"), client, channel);
        Plugins.getMemberAccessor().set(MySQLBinlogClient.class.getDeclaredField("eventLoopGroup"), client, new NioEventLoopGroup(1));
        mockChannelResponse(new MySQLOKPacket(0));
        client.subscribe("", 4L);
        verify(channel).writeAndFlush(ArgumentMatchers.any(MySQLComRegisterSlaveCommandPacket.class));
        verify(channel).writeAndFlush(ArgumentMatchers.any(MySQLComBinlogDumpCommandPacket.class));
    }
    
    private void mockChannelResponse(final Object response) {
        new Thread(() -> mockChannelResponseInThread(response)).start();
    }
    
    @SneakyThrows(InterruptedException.class)
    @SuppressWarnings("unchecked")
    private void mockChannelResponseInThread(final Object response) {
        long t1 = System.currentTimeMillis();
        do {
            Promise<Object> responseCallback;
            try {
                responseCallback = (Promise<Object>) Plugins.getMemberAccessor().get(MySQLBinlogClient.class.getDeclaredField("responseCallback"), client);
            } catch (final ReflectiveOperationException ex) {
                throw new RuntimeException(ex);
            }
            if (null != responseCallback && !responseCallback.isDone()) {
                responseCallback.setSuccess(response);
            }
            TimeUnit.SECONDS.sleep(1L);
        } while (System.currentTimeMillis() - t1 <= TimeUnit.SECONDS.toMillis(20L));
    }
    
    @Test
    void assertPollOnNotRunning() throws ReflectiveOperationException {
        Plugins.getMemberAccessor().set(MySQLBinlogClient.class.getDeclaredField("channel"), client, channel);
        Plugins.getMemberAccessor().set(MySQLBinlogClient.class.getDeclaredField("running"), client, false);
        assertThat(client.poll(), is(Collections.emptyList()));
    }
}
