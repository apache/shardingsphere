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

package org.apache.shardingsphere.proxy.frontend.postgresql.auth;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.buffer.UnpooledHeapByteBuf;
import io.netty.channel.ChannelHandlerContext;
import lombok.SneakyThrows;
import org.apache.shardingsphere.db.protocol.payload.PacketPayload;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.binary.BinaryStatementRegistry;
import org.apache.shardingsphere.db.protocol.postgresql.packet.handshake.PostgreSQLAuthenticationMD5PasswordPacket;
import org.apache.shardingsphere.db.protocol.postgresql.payload.PostgreSQLPacketPayload;
import org.apache.shardingsphere.infra.auth.ShardingSphereUser;
import org.apache.shardingsphere.infra.auth.builtin.DefaultAuthentication;
import org.apache.shardingsphere.infra.context.metadata.impl.StandardMetaDataContexts;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.frontend.auth.AuthenticationResult;
import org.apache.shardingsphere.transaction.context.TransactionContexts;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public final class PostgreSQLAuthenticationEngineTest {
    
    private final String username = "root";
    
    private final String password = "sharding";
    
    @Test
    public void assertHandshake() {
        int connectionId = new PostgreSQLAuthenticationEngine().handshake(mock(ChannelHandlerContext.class));
        assertNotNull(BinaryStatementRegistry.getInstance().get(connectionId));
    }
    
    private ByteBuf createByteBuf(final int initialCapacity, final int maxCapacity) {
        return new UnpooledHeapByteBuf(UnpooledByteBufAllocator.DEFAULT, initialCapacity, maxCapacity);
    }
    
    @Test
    public void assertSSLNegative() {
        ByteBuf byteBuf = createByteBuf(8, 8);
        byteBuf.writeInt(8);
        byteBuf.writeInt(80877103);
        PacketPayload payload = new PostgreSQLPacketPayload(byteBuf);
        AuthenticationResult actual = new PostgreSQLAuthenticationEngine().auth(mock(ChannelHandlerContext.class), payload);
        assertThat(actual.isFinished(), is(false));
    }
    
    @Test
    public void assertDatabaseNotExist() {
        PostgreSQLPacketPayload payload = new PostgreSQLPacketPayload(createByteBuf(32, 512));
        payload.writeInt4(64);
        payload.writeInt4(196608);
        payload.writeStringNul("user");
        payload.writeStringNul(username);
        payload.writeStringNul("database");
        payload.writeStringNul("sharding_db");
        AuthenticationResult actual = new PostgreSQLAuthenticationEngine().auth(mock(ChannelHandlerContext.class), payload);
        assertThat(actual.isFinished(), is(false));
    }
    
    @Test
    public void assertUserNotSet() {
        PostgreSQLPacketPayload payload = new PostgreSQLPacketPayload(createByteBuf(8, 512));
        payload.writeInt4(64);
        payload.writeInt4(196608);
        AuthenticationResult actual = new PostgreSQLAuthenticationEngine().auth(mock(ChannelHandlerContext.class), payload);
        assertThat(actual.isFinished(), is(false));
    }
    
    @Test
    public void assertLoginSuccessful() {
        assertLogin(password);
    }
    
    @Test
    public void assertLoginFailed() {
        assertLogin("wrong" + password);
    }
    
    private void assertLogin(final String inputPassword) {
        PostgreSQLPacketPayload payload = new PostgreSQLPacketPayload(createByteBuf(16, 128));
        payload.writeInt4(64);
        payload.writeInt4(196608);
        payload.writeStringNul("user");
        payload.writeStringNul(username);
        ChannelHandlerContext channelHandlerContext = mock(ChannelHandlerContext.class);
        PostgreSQLAuthenticationEngine engine = new PostgreSQLAuthenticationEngine();
        AuthenticationResult actual = engine.auth(channelHandlerContext, payload);
        assertThat(actual.isFinished(), is(false));
        assertThat(actual.getUsername(), is(username));
        ArgumentCaptor<PostgreSQLAuthenticationMD5PasswordPacket> argumentCaptor = ArgumentCaptor.forClass(PostgreSQLAuthenticationMD5PasswordPacket.class);
        verify(channelHandlerContext).writeAndFlush(argumentCaptor.capture());
        PostgreSQLAuthenticationMD5PasswordPacket md5PasswordPacket = argumentCaptor.getValue();
        byte[] md5Salt = getMd5Salt(md5PasswordPacket);
        
        payload = new PostgreSQLPacketPayload(createByteBuf(16, 128));
        String md5Digest = md5Encode(username, inputPassword, md5Salt);
        payload.writeInt1('p');
        payload.writeInt4(4 + md5Digest.length() + 1);
        payload.writeStringNul(md5Digest);
        
        ProxyContext proxyContext = ProxyContext.getInstance();
        StandardMetaDataContexts standardMetaDataContexts = new StandardMetaDataContexts();
        ((DefaultAuthentication) standardMetaDataContexts.getAuthentication()).getUsers().put(username, new ShardingSphereUser(password, null));
        proxyContext.init(standardMetaDataContexts, mock(TransactionContexts.class));
        actual = engine.auth(channelHandlerContext, payload);
        assertThat(actual.isFinished(), is(password.equals(inputPassword)));
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private byte[] getMd5Salt(final PostgreSQLAuthenticationMD5PasswordPacket md5PasswordPacket) {
        Field field = PostgreSQLAuthenticationMD5PasswordPacket.class.getDeclaredField("md5Salt");
        field.setAccessible(true);
        return (byte[]) field.get(md5PasswordPacket);
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private String md5Encode(final String username, final String password, final byte[] md5Salt) {
        Method method = PostgreSQLAuthenticationHandler.class.getDeclaredMethod("md5Encode", String.class, String.class, byte[].class);
        method.setAccessible(true);
        return (String) method.invoke(PostgreSQLAuthenticationHandler.class, username, password, md5Salt);
    }
}
