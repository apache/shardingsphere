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
import io.netty.util.concurrent.Promise;
import org.apache.shardingsphere.data.pipeline.core.exception.job.BinlogSyncChannelAlreadyClosedException;
import org.apache.shardingsphere.db.protocol.mysql.packet.command.binlog.MySQLComBinlogDumpCommandPacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.command.binlog.MySQLComRegisterSlaveCommandPacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.command.query.text.query.MySQLComQueryPacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.generic.MySQLOKPacket;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.FieldReader;
import org.mockito.internal.util.reflection.InstanceField;
import org.mockito.junit.MockitoJUnitRunner;

import java.net.InetSocketAddress;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class MySQLClientTest {
    
    @Mock
    private Channel channel;
    
    @Mock
    private ChannelPipeline pipeline;
    
    @Mock
    private ChannelFuture channelFuture;
    
    private MySQLClient mysqlClient;
    
    @Before
    public void setUp() throws InterruptedException {
        mysqlClient = new MySQLClient(new ConnectInfo(1, "host", 3306, "username", "password"));
        when(channel.pipeline()).thenReturn(pipeline);
        when(channel.isOpen()).thenReturn(true);
        when(channel.close()).thenReturn(channelFuture);
        when(channelFuture.sync()).thenAnswer(invocation -> {
            when(channel.isOpen()).thenReturn(false);
            return null;
        });
        when(channel.localAddress()).thenReturn(new InetSocketAddress("host", 3306));
    }
    
    @Test
    public void assertConnect() throws NoSuchFieldException {
        ServerInfo expected = new ServerInfo();
        mockChannelResponse(expected);
        mysqlClient.connect();
        ServerInfo actual = (ServerInfo) new FieldReader(mysqlClient, MySQLClient.class.getDeclaredField("serverInfo")).read();
        assertThat(actual, is(expected));
    }
    
    @Test
    public void assertExecute() throws NoSuchFieldException {
        mockChannelResponse(new MySQLOKPacket(0, 0));
        new InstanceField(MySQLClient.class.getDeclaredField("channel"), mysqlClient).set(channel);
        new InstanceField(MySQLClient.class.getDeclaredField("eventLoopGroup"), mysqlClient).set(new NioEventLoopGroup(1));
        assertTrue(mysqlClient.execute(""));
        verify(channel).writeAndFlush(ArgumentMatchers.any(MySQLComQueryPacket.class));
    }
    
    @Test
    public void assertExecuteUpdate() throws NoSuchFieldException {
        MySQLOKPacket expected = new MySQLOKPacket(0, 10, 0, 0);
        new InstanceField(MySQLOKPacket.class.getDeclaredField("affectedRows"), expected).set(10L);
        mockChannelResponse(expected);
        new InstanceField(MySQLClient.class.getDeclaredField("channel"), mysqlClient).set(channel);
        new InstanceField(MySQLClient.class.getDeclaredField("eventLoopGroup"), mysqlClient).set(new NioEventLoopGroup(1));
        assertThat(mysqlClient.executeUpdate(""), is(10));
        verify(channel).writeAndFlush(ArgumentMatchers.any(MySQLComQueryPacket.class));
    }
    
    @Test
    public void assertExecuteQuery() throws NoSuchFieldException {
        InternalResultSet expected = new InternalResultSet(null);
        mockChannelResponse(expected);
        new InstanceField(MySQLClient.class.getDeclaredField("channel"), mysqlClient).set(channel);
        new InstanceField(MySQLClient.class.getDeclaredField("eventLoopGroup"), mysqlClient).set(new NioEventLoopGroup(1));
        assertThat(mysqlClient.executeQuery(""), is(expected));
        verify(channel).writeAndFlush(ArgumentMatchers.any(MySQLComQueryPacket.class));
    }
    
    @Test
    public void assertSubscribeBelow56Version() throws NoSuchFieldException {
        ServerInfo serverInfo = new ServerInfo();
        serverInfo.setServerVersion(new ServerVersion("5.5.0-log"));
        new InstanceField(MySQLClient.class.getDeclaredField("serverInfo"), mysqlClient).set(serverInfo);
        new InstanceField(MySQLClient.class.getDeclaredField("channel"), mysqlClient).set(channel);
        new InstanceField(MySQLClient.class.getDeclaredField("eventLoopGroup"), mysqlClient).set(new NioEventLoopGroup(1));
        mockChannelResponse(new MySQLOKPacket(0, 0));
        mysqlClient.subscribe("", 4L);
        verify(channel).writeAndFlush(ArgumentMatchers.any(MySQLComRegisterSlaveCommandPacket.class));
        verify(channel).writeAndFlush(ArgumentMatchers.any(MySQLComBinlogDumpCommandPacket.class));
    }
    
    @SuppressWarnings("unchecked")
    private void mockChannelResponse(final Object response) {
        new Thread(() -> {
            while (true) {
                Promise<Object> responseCallback;
                try {
                    responseCallback = (Promise<Object>) new FieldReader(mysqlClient, MySQLClient.class.getDeclaredField("responseCallback")).read();
                } catch (final NoSuchFieldException ex) {
                    throw new RuntimeException(ex);
                }
                if (null != responseCallback) {
                    responseCallback.setSuccess(response);
                    break;
                }
            }
        }).start();
    }
    
    @Test
    public void assertCloseChannel() throws NoSuchFieldException {
        new InstanceField(MySQLClient.class.getDeclaredField("channel"), mysqlClient).set(channel);
        mysqlClient.closeChannel();
        assertFalse(channel.isOpen());
    }
    
    @Test(expected = BinlogSyncChannelAlreadyClosedException.class)
    public void assertPollFailed() throws NoSuchFieldException {
        new InstanceField(MySQLClient.class.getDeclaredField("channel"), mysqlClient).set(channel);
        new InstanceField(MySQLClient.class.getDeclaredField("running"), mysqlClient).set(false);
        mysqlClient.poll();
    }
}
