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

package org.apache.shardingsphere.shardingproxy.frontend.mysql;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import lombok.SneakyThrows;
import org.apache.shardingsphere.core.rule.Authentication;
import org.apache.shardingsphere.core.rule.ProxyUser;
import org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.shardingproxy.context.ShardingProxyContext;
import org.apache.shardingsphere.shardingproxy.frontend.ConnectionIdGenerator;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.generic.MySQLErrPacket;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.generic.MySQLOKPacket;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.handshake.MySQLHandshakePacket;
import org.apache.shardingsphere.shardingproxy.transport.mysql.payload.MySQLPacketPayload;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Collections;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class MySQLFrontendEngineTest {
    
    private MySQLProtocolFrontendEngine mysqlFrontendEngine;
    
    @Mock
    private ChannelHandlerContext context;
    
    @Mock
    private MySQLPacketPayload payload;

    @Mock
    private Channel channel;
    
    @Before
    @SneakyThrows
    public void resetConnectionIdGenerator() {
        Field field = ConnectionIdGenerator.class.getDeclaredField("currentId");
        field.setAccessible(true);
        field.set(ConnectionIdGenerator.getInstance(), 0);
        mysqlFrontendEngine = new MySQLProtocolFrontendEngine();
    }
    
    @Test
    public void assertHandshake() {
        mysqlFrontendEngine.getAuthEngine().handshake(context, mock(BackendConnection.class));
        verify(context).writeAndFlush(isA(MySQLHandshakePacket.class));
    }
    
    @Test
    public void assertAuthWhenLoginSuccess() {
        ProxyUser proxyUser = new ProxyUser("", Collections.singleton("db1"));
        setAuthentication(proxyUser);
        when(payload.readStringNul()).thenReturn("root");
        assertTrue(mysqlFrontendEngine.getAuthEngine().auth(context, payload, mock(BackendConnection.class)));
        verify(context).writeAndFlush(isA(MySQLOKPacket.class));
    }
    
    @Test
    public void assertAuthWhenLoginFailure() {
        ProxyUser proxyUser = new ProxyUser("error", Collections.singleton("db1"));
        setAuthentication(proxyUser);
        when(payload.readStringNul()).thenReturn("root");
        when(payload.readStringNulByBytes()).thenReturn("root".getBytes());
        when(context.channel()).thenReturn(channel);
        when(channel.remoteAddress()).thenReturn(new InetSocketAddress("localhost", 3307));
        assertTrue(mysqlFrontendEngine.getAuthEngine().auth(context, payload, mock(BackendConnection.class)));
        verify(context).writeAndFlush(isA(MySQLErrPacket.class));
    }

    @Test
    @SneakyThrows
    public void assertErrorMsgWhenLoginFailure() {
        ProxyUser proxyUser = new ProxyUser("error", Collections.singleton("db1"));
        setAuthentication(proxyUser);
        when(payload.readStringNul()).thenReturn("root");
        when(payload.readStringNulByBytes()).thenReturn("root".getBytes());
        when(context.channel()).thenReturn(channel);
        when(channel.remoteAddress()).thenReturn(new InetSocketAddress(InetAddress.getByAddress(new byte[] {(byte) 192, (byte) 168, (byte) 0, (byte) 102}), 3307));
        assertTrue(mysqlFrontendEngine.getAuthEngine().auth(context, payload, mock(BackendConnection.class)));
        verify(context).writeAndFlush(Mockito.argThat(new ArgumentMatcher<MySQLErrPacket>() {
            
            @Override
            public boolean matches(final MySQLErrPacket argument) {
                return argument.getErrorMessage().equals("Access denied for user 'root'@'192.168.0.102' (using password: YES)");
            }
        }));
    }
    
    @SneakyThrows
    private void setAuthentication(final ProxyUser proxyUser) {
        Authentication authentication = new Authentication();
        authentication.getUsers().put("root", proxyUser);
        Field field = ShardingProxyContext.class.getDeclaredField("authentication");
        field.setAccessible(true);
        field.set(ShardingProxyContext.getInstance(), authentication);
    }
}
