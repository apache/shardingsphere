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

package org.apache.shardingsphere.data.pipeline.mysql.ingest.client;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.Attribute;
import io.netty.util.concurrent.Promise;
import org.apache.shardingsphere.db.protocol.mysql.constant.MySQLConstants;
import org.apache.shardingsphere.db.protocol.mysql.packet.command.binlog.MySQLComBinlogDumpCommandPacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.command.binlog.MySQLComRegisterSlaveCommandPacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.command.query.text.query.MySQLComQueryPacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.generic.MySQLOKPacket;
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
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MySQLClientTest {
    
    @Mock
    private Channel channel;
    
    @Mock
    private ChannelPipeline pipeline;
    
    @Mock
    private ChannelFuture channelFuture;
    
    private MySQLClient mysqlClient;
    
    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() throws InterruptedException {
        mysqlClient = new MySQLClient(new ConnectInfo(1, "host", 3306, "username", "password"), false);
        when(channel.pipeline()).thenReturn(pipeline);
        when(channel.isOpen()).thenReturn(true);
        when(channel.close()).thenReturn(channelFuture);
        when(channelFuture.sync()).thenAnswer(invocation -> {
            when(channel.isOpen()).thenReturn(false);
            return null;
        });
        when(channel.localAddress()).thenReturn(new InetSocketAddress("host", 3306));
        when(channel.attr(MySQLConstants.MYSQL_SEQUENCE_ID)).thenReturn(mock(Attribute.class));
        when(channel.attr(MySQLConstants.MYSQL_SEQUENCE_ID).get()).thenReturn(new AtomicInteger());
    }
    
    @Test
    void assertConnect() throws ReflectiveOperationException {
        ServerInfo expected = new ServerInfo(new ServerVersion("5.5.0-log"));
        mockChannelResponse(expected);
        mysqlClient.connect();
        ServerInfo actual = (ServerInfo) Plugins.getMemberAccessor().get(MySQLClient.class.getDeclaredField("serverInfo"), mysqlClient);
        assertThat(actual, is(expected));
    }
    
    @Test
    void assertExecute() throws ReflectiveOperationException {
        mockChannelResponse(new MySQLOKPacket(0));
        Plugins.getMemberAccessor().set(MySQLClient.class.getDeclaredField("channel"), mysqlClient, channel);
        Plugins.getMemberAccessor().set(MySQLClient.class.getDeclaredField("eventLoopGroup"), mysqlClient, new NioEventLoopGroup(1));
        assertTrue(mysqlClient.execute(""));
        verify(channel).writeAndFlush(ArgumentMatchers.any(MySQLComQueryPacket.class));
    }
    
    @Test
    void assertExecuteUpdate() throws ReflectiveOperationException {
        MySQLOKPacket expected = new MySQLOKPacket(10, 0, 0);
        Plugins.getMemberAccessor().set(MySQLOKPacket.class.getDeclaredField("affectedRows"), expected, 10L);
        mockChannelResponse(expected);
        Plugins.getMemberAccessor().set(MySQLClient.class.getDeclaredField("channel"), mysqlClient, channel);
        Plugins.getMemberAccessor().set(MySQLClient.class.getDeclaredField("eventLoopGroup"), mysqlClient, new NioEventLoopGroup(1));
        assertThat(mysqlClient.executeUpdate(""), is(10));
        verify(channel).writeAndFlush(ArgumentMatchers.any(MySQLComQueryPacket.class));
    }
    
    @Test
    void assertExecuteQuery() throws ReflectiveOperationException {
        InternalResultSet expected = new InternalResultSet(null);
        mockChannelResponse(expected);
        Plugins.getMemberAccessor().set(MySQLClient.class.getDeclaredField("channel"), mysqlClient, channel);
        Plugins.getMemberAccessor().set(MySQLClient.class.getDeclaredField("eventLoopGroup"), mysqlClient, new NioEventLoopGroup(1));
        assertThat(mysqlClient.executeQuery(""), is(expected));
        verify(channel).writeAndFlush(ArgumentMatchers.any(MySQLComQueryPacket.class));
    }
    
    @Test
    void assertSubscribeBelow56Version() throws ReflectiveOperationException {
        ServerInfo serverInfo = new ServerInfo(new ServerVersion("5.5.0-log"));
        Plugins.getMemberAccessor().set(MySQLClient.class.getDeclaredField("serverInfo"), mysqlClient, serverInfo);
        Plugins.getMemberAccessor().set(MySQLClient.class.getDeclaredField("channel"), mysqlClient, channel);
        Plugins.getMemberAccessor().set(MySQLClient.class.getDeclaredField("eventLoopGroup"), mysqlClient, new NioEventLoopGroup(1));
        mockChannelResponse(new MySQLOKPacket(0));
        mysqlClient.subscribe("", 4L);
        verify(channel).writeAndFlush(ArgumentMatchers.any(MySQLComRegisterSlaveCommandPacket.class));
        verify(channel).writeAndFlush(ArgumentMatchers.any(MySQLComBinlogDumpCommandPacket.class));
    }
    
    private void mockChannelResponse(final Object response) {
        new Thread(() -> mockChannelResponseInThread(response)).start();
    }
    
    @SuppressWarnings("unchecked")
    private void mockChannelResponseInThread(final Object response) {
        while (true) {
            Promise<Object> responseCallback;
            try {
                responseCallback = (Promise<Object>) Plugins.getMemberAccessor().get(MySQLClient.class.getDeclaredField("responseCallback"), mysqlClient);
            } catch (final ReflectiveOperationException ex) {
                throw new RuntimeException(ex);
            }
            if (null != responseCallback) {
                responseCallback.setSuccess(response);
                break;
            }
        }
    }
    
    @Test
    void assertPollFailed() throws ReflectiveOperationException {
        Plugins.getMemberAccessor().set(MySQLClient.class.getDeclaredField("channel"), mysqlClient, channel);
        Plugins.getMemberAccessor().set(MySQLClient.class.getDeclaredField("running"), mysqlClient, false);
        assertThat(mysqlClient.poll(), is(Collections.emptyList()));
    }
}
