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

package org.apache.shardingsphere.data.pipeline.mysql.ingest.client.netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.util.concurrent.Promise;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.client.ServerInfo;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.client.ServerVersion;
import org.apache.shardingsphere.db.protocol.mysql.constant.MySQLAuthenticationMethod;
import org.apache.shardingsphere.db.protocol.mysql.packet.generic.MySQLErrPacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.generic.MySQLOKPacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.handshake.MySQLAuthenticationPluginData;
import org.apache.shardingsphere.db.protocol.mysql.packet.handshake.MySQLHandshakePacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.handshake.MySQLHandshakeResponse41Packet;
import org.apache.shardingsphere.infra.exception.mysql.vendor.MySQLVendorError;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.internal.configuration.plugins.Plugins;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MySQLNegotiateHandlerTest {
    
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
    
    private MySQLNegotiateHandler mysqlNegotiateHandler;
    
    @BeforeEach
    void setUp() {
        when(channelHandlerContext.channel()).thenReturn(channel);
        when(channel.pipeline()).thenReturn(pipeline);
        mysqlNegotiateHandler = new MySQLNegotiateHandler(USER_NAME, PASSWORD, authResultCallback);
    }
    
    @Test
    void assertChannelReadHandshakeInitPacket() throws ReflectiveOperationException {
        MySQLHandshakePacket handshakePacket = new MySQLHandshakePacket(0, false, new MySQLAuthenticationPluginData(new byte[8], new byte[12]));
        handshakePacket.setAuthPluginName(MySQLAuthenticationMethod.NATIVE);
        mysqlNegotiateHandler.channelRead(channelHandlerContext, handshakePacket);
        verify(channel).writeAndFlush(ArgumentMatchers.any(MySQLHandshakeResponse41Packet.class));
        ServerInfo serverInfo = (ServerInfo) Plugins.getMemberAccessor().get(MySQLNegotiateHandler.class.getDeclaredField("serverInfo"), mysqlNegotiateHandler);
        assertNotNull(serverInfo);
        assertThat(serverInfo.getServerVersion().getMajor(), is(5));
        assertThat(serverInfo.getServerVersion().getMinor(), is(7));
        assertThat(serverInfo.getServerVersion().getSeries(), is(22));
    }
    
    @Test
    void assertChannelReadOkPacket() throws ReflectiveOperationException {
        MySQLOKPacket okPacket = new MySQLOKPacket(0);
        ServerInfo serverInfo = new ServerInfo(new ServerVersion("5.5.0-log"));
        Plugins.getMemberAccessor().set(MySQLNegotiateHandler.class.getDeclaredField("serverInfo"), mysqlNegotiateHandler, serverInfo);
        mysqlNegotiateHandler.channelRead(channelHandlerContext, okPacket);
        verify(pipeline).remove(mysqlNegotiateHandler);
        verify(authResultCallback).setSuccess(serverInfo);
    }
    
    @Test
    void assertChannelReadErrorPacket() {
        MySQLErrPacket errorPacket = new MySQLErrPacket(MySQLVendorError.ER_NO_DB_ERROR);
        assertThrows(RuntimeException.class, () -> mysqlNegotiateHandler.channelRead(channelHandlerContext, errorPacket));
    }
}
