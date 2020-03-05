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

package org.apache.shardingsphere.shardingscaling.mysql.binlog;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.util.concurrent.Promise;
import org.apache.shardingsphere.shardingscaling.mysql.binlog.packet.auth.ClientAuthenticationPacket;
import org.apache.shardingsphere.shardingscaling.mysql.binlog.packet.auth.HandshakeInitializationPacket;
import org.apache.shardingsphere.shardingscaling.mysql.binlog.packet.response.ErrorPacket;
import org.apache.shardingsphere.shardingscaling.mysql.binlog.packet.response.OkPacket;
import org.apache.shardingsphere.shardingscaling.utils.ReflectionUtil;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class MySQLNegotiateHandlerTest {
    
    private static final String SERVER_VERSION = "5.7.13-log";
    
    private static final String USER_NAME = "username";
    
    private static final String PASSWORD = "password";
    
    @Mock
    private Promise<Object> authResultCallback;
    
    @Mock
    private ChannelHandlerContext channelHandlerContext;
    
    @Mock
    private Channel channel;
    
    @Mock
    private ChannelPipeline pipeline;
    
    private MySQLNegotiateHandler mySQLNegotiateHandler;
    
    @Before
    public void setUp() {
        when(channelHandlerContext.channel()).thenReturn(channel);
        when(channel.pipeline()).thenReturn(pipeline);
        mySQLNegotiateHandler = new MySQLNegotiateHandler(USER_NAME, PASSWORD, authResultCallback);
    }
    
    @Test
    public void assertChannelReadHandshakeInitPacket() throws NoSuchFieldException, IllegalAccessException {
        HandshakeInitializationPacket handshakeInitializationPacket = new HandshakeInitializationPacket();
        handshakeInitializationPacket.setServerVersion(SERVER_VERSION);
        handshakeInitializationPacket.setAuthPluginName("");
        handshakeInitializationPacket.setServerCapabilities(1);
        handshakeInitializationPacket.setAuthPluginDataPart1(new byte[8]);
        handshakeInitializationPacket.setAuthPluginDataPart2(new byte[12]);
        mySQLNegotiateHandler.channelRead(channelHandlerContext, handshakeInitializationPacket);
        verify(channel).writeAndFlush(ArgumentMatchers.any(ClientAuthenticationPacket.class));
        ServerInfo serverInfo = ReflectionUtil.getFieldValueFromClass(mySQLNegotiateHandler, "serverInfo", ServerInfo.class);
        assertThat(serverInfo.getServerVersion().getMajor(), is(5));
        assertThat(serverInfo.getServerVersion().getMinor(), is(7));
        assertThat(serverInfo.getServerVersion().getSeries(), is(13));
    }
    
    @Test
    public void assertChannelReadOkPacket() throws NoSuchFieldException, IllegalAccessException {
        OkPacket okPacket = new OkPacket();
        ServerInfo serverInfo = new ServerInfo();
        ReflectionUtil.setFieldValueToClass(mySQLNegotiateHandler, "serverInfo", serverInfo);
        mySQLNegotiateHandler.channelRead(channelHandlerContext, okPacket);
        verify(pipeline).remove(mySQLNegotiateHandler);
        verify(authResultCallback).setSuccess(serverInfo);
    }
    
    @Test(expected = RuntimeException.class)
    public void assertChannelReadErrorPacket() {
        ErrorPacket errorPacket = new ErrorPacket();
        mySQLNegotiateHandler.channelRead(channelHandlerContext, errorPacket);
    }
}
