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

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;
import lombok.SneakyThrows;
import org.apache.shardingsphere.core.constant.properties.ShardingProperties;
import org.apache.shardingsphere.core.rule.Authentication;
import org.apache.shardingsphere.shardingproxy.runtime.GlobalRegistry;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.generic.MySQLErrPacket;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.generic.MySQLOKPacket;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.handshake.MySQLConnectionIdGenerator;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.handshake.MySQLHandshakePacket;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Field;
import java.util.Properties;

import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class MySQLFrontendHandlerTest {
    
    private MySQLFrontendHandler mysqlFrontendHandler;
    
    @Mock
    private ChannelHandlerContext context;
    
    @Before
    @SneakyThrows
    public void resetConnectionIdGenerator() {
        Field field = MySQLConnectionIdGenerator.class.getDeclaredField("currentId");
        field.setAccessible(true);
        field.set(MySQLConnectionIdGenerator.getInstance(), 0);
        mysqlFrontendHandler = new MySQLFrontendHandler();
    }
    
    @Test
    public void assertHandshake() {
        mysqlFrontendHandler.handshake(context);
        verify(context).writeAndFlush(isA(MySQLHandshakePacket.class));
    }
    
    @Test
    public void assertAuthWhenLoginSuccess() throws ReflectiveOperationException {
        Authentication authentication = new Authentication("", "");
        setAuthentication(authentication);
        mysqlFrontendHandler.auth(context, mock(ByteBuf.class));
        verify(context).writeAndFlush(isA(MySQLOKPacket.class));
    }
    
    @Test
    public void assertAuthWhenLoginFailure() throws ReflectiveOperationException {
        Authentication authentication = new Authentication("root", "root");
        setAuthentication(authentication);
        mysqlFrontendHandler.auth(context, mock(ByteBuf.class));
        verify(context).writeAndFlush(isA(MySQLErrPacket.class));
    }
    
    @Test
    public void assertExecuteCommand() throws ReflectiveOperationException {
        Channel channel = mock(Channel.class);
        ChannelId channelId = mock(ChannelId.class);
        when(channel.id()).thenReturn(channelId);
        when(context.channel()).thenReturn(channel);
        setTransactionType();
        mysqlFrontendHandler.executeCommand(context, mock(ByteBuf.class));
    }
    
    private void setAuthentication(final Object value) throws ReflectiveOperationException {
        Field field = GlobalRegistry.class.getDeclaredField("authentication");
        field.setAccessible(true);
        field.set(GlobalRegistry.getInstance(), value);
    }
    
    private void setTransactionType() throws ReflectiveOperationException {
        Field field = GlobalRegistry.getInstance().getClass().getDeclaredField("shardingProperties");
        field.setAccessible(true);
        field.set(GlobalRegistry.getInstance(), getShardingProperties());
    }
    
    private ShardingProperties getShardingProperties() {
        Properties props = new Properties();
        return new ShardingProperties(props);
    }
}
