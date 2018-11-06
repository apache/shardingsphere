/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.shardingproxy.frontend.mysql;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;
import io.netty.channel.EventLoopGroup;
import io.shardingsphere.core.constant.properties.ShardingProperties;
import io.shardingsphere.core.constant.properties.ShardingPropertiesConstant;
import io.shardingsphere.core.constant.transaction.TransactionType;
import io.shardingsphere.core.rule.Authentication;
import io.shardingsphere.shardingproxy.runtime.GlobalRegistry;
import io.shardingsphere.shardingproxy.transport.mysql.packet.generic.ErrPacket;
import io.shardingsphere.shardingproxy.transport.mysql.packet.generic.OKPacket;
import io.shardingsphere.shardingproxy.transport.mysql.packet.handshake.ConnectionIdGenerator;
import io.shardingsphere.shardingproxy.transport.mysql.packet.handshake.HandshakePacket;
import lombok.SneakyThrows;
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
    private EventLoopGroup eventLoopGroup;
    
    @Mock
    private ChannelHandlerContext context;
    
    @Before
    @SneakyThrows
    public void resetConnectionIdGenerator() {
        Field field = ConnectionIdGenerator.class.getDeclaredField("currentId");
        field.setAccessible(true);
        field.set(ConnectionIdGenerator.getInstance(), 0);
        mysqlFrontendHandler = new MySQLFrontendHandler(eventLoopGroup);
    }
    
    @Test
    public void assertHandshake() {
        Channel channel = mock(Channel.class);
        ChannelId channelId = mock(ChannelId.class);
        when(channelId.asShortText()).thenReturn("1");
        when(channel.id()).thenReturn(channelId);
        when(context.channel()).thenReturn(channel);
        mysqlFrontendHandler.handshake(context);
        verify(context).writeAndFlush(isA(HandshakePacket.class));
    }
    
    @Test
    public void assertAuthWhenLoginSuccess() throws ReflectiveOperationException {
        Authentication authentication = new Authentication();
        authentication.setUsername("");
        authentication.setPassword("");
        setAuthentication(authentication);
        mysqlFrontendHandler.auth(context, mock(ByteBuf.class));
        verify(context).writeAndFlush(isA(OKPacket.class));
    }
    
    @Test
    public void assertAuthWhenLoginFailure() throws ReflectiveOperationException {
        Authentication authentication = new Authentication();
        authentication.setUsername("root");
        authentication.setPassword("root");
        setAuthentication(authentication);
        mysqlFrontendHandler.auth(context, mock(ByteBuf.class));
        verify(context).writeAndFlush(isA(ErrPacket.class));
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
        field.set(GlobalRegistry.getInstance(), getShardingProperties(TransactionType.LOCAL));
    }
    
    private ShardingProperties getShardingProperties(final TransactionType transactionType) {
        Properties props = new Properties();
        props.setProperty(ShardingPropertiesConstant.PROXY_TRANSACTION_ENABLED.getKey(), String.valueOf(transactionType == TransactionType.XA));
        return new ShardingProperties(props);
    }
}
