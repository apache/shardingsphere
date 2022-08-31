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

package org.apache.shardingsphere.proxy.frontend.postgresql.authentication;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.buffer.UnpooledHeapByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.Attribute;
import lombok.SneakyThrows;
import org.apache.shardingsphere.authority.config.AuthorityRuleConfiguration;
import org.apache.shardingsphere.authority.rule.AuthorityRule;
import org.apache.shardingsphere.authority.rule.builder.AuthorityRuleBuilder;
import org.apache.shardingsphere.db.protocol.CommonConstants;
import org.apache.shardingsphere.db.protocol.payload.PacketPayload;
import org.apache.shardingsphere.db.protocol.postgresql.packet.handshake.authentication.PostgreSQLMD5PasswordAuthenticationPacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.identifier.PostgreSQLIdentifierPacket;
import org.apache.shardingsphere.db.protocol.postgresql.payload.PostgreSQLPacketPayload;
import org.apache.shardingsphere.dialect.postgresql.exception.EmptyUsernameException;
import org.apache.shardingsphere.dialect.postgresql.exception.InvalidPasswordException;
import org.apache.shardingsphere.dialect.postgresql.exception.PostgreSQLProtocolViolationException;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUser;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.frontend.authentication.AuthenticationResult;
import org.apache.shardingsphere.proxy.frontend.postgresql.ProxyContextRestorer;
import org.apache.shardingsphere.proxy.frontend.postgresql.authentication.authenticator.PostgreSQLMD5PasswordAuthenticator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class PostgreSQLAuthenticationEngineTest extends ProxyContextRestorer {
    
    private final String username = "root";
    
    private final String password = "sharding";
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ChannelHandlerContext channelHandlerContext;
    
    @SuppressWarnings("unchecked")
    @Before
    public void setup() {
        when(channelHandlerContext.channel().attr(CommonConstants.CHARSET_ATTRIBUTE_KEY)).thenReturn(mock(Attribute.class));
    }
    
    @Test
    public void assertSSLNegative() {
        ByteBuf byteBuf = createByteBuf(8, 8);
        byteBuf.writeInt(8);
        byteBuf.writeInt(80877103);
        PacketPayload payload = new PostgreSQLPacketPayload(byteBuf, StandardCharsets.UTF_8);
        AuthenticationResult actual = new PostgreSQLAuthenticationEngine().authenticate(mock(ChannelHandlerContext.class), payload);
        assertFalse(actual.isFinished());
    }
    
    @Test(expected = EmptyUsernameException.class)
    public void assertUserNotSet() {
        PostgreSQLPacketPayload payload = new PostgreSQLPacketPayload(createByteBuf(8, 512), StandardCharsets.UTF_8);
        payload.writeInt4(64);
        payload.writeInt4(196608);
        payload.writeStringNul("client_encoding");
        payload.writeStringNul("UTF8");
        new PostgreSQLAuthenticationEngine().authenticate(channelHandlerContext, payload);
    }
    
    @Test(expected = PostgreSQLProtocolViolationException.class)
    public void assertAuthenticateWithNonPasswordMessage() {
        PostgreSQLAuthenticationEngine authenticationEngine = new PostgreSQLAuthenticationEngine();
        setAlreadyReceivedStartupMessage(authenticationEngine);
        PostgreSQLPacketPayload payload = new PostgreSQLPacketPayload(createByteBuf(8, 16), StandardCharsets.UTF_8);
        payload.writeInt1('F');
        payload.writeInt8(0);
        authenticationEngine.authenticate(channelHandlerContext, payload);
    }
    
    @SneakyThrows
    private void setAlreadyReceivedStartupMessage(final PostgreSQLAuthenticationEngine target) {
        Field field = PostgreSQLAuthenticationEngine.class.getDeclaredField("startupMessageReceived");
        field.setAccessible(true);
        field.set(target, true);
        field.setAccessible(false);
    }
    
    @Test
    public void assertLoginSuccessful() {
        assertLogin(password);
    }
    
    @Test(expected = InvalidPasswordException.class)
    public void assertLoginFailed() {
        assertLogin("wrong" + password);
    }
    
    @Test
    @SneakyThrows(ReflectiveOperationException.class)
    public void assertGetIdentifierPacket() {
        Method method = PostgreSQLAuthenticationEngine.class.getDeclaredMethod("getIdentifierPacket", String.class);
        method.setAccessible(true);
        PostgreSQLIdentifierPacket packet = (PostgreSQLIdentifierPacket) method.invoke(new PostgreSQLAuthenticationEngine(), username);
        assertThat(packet, instanceOf(PostgreSQLMD5PasswordAuthenticationPacket.class));
    }
    
    private void assertLogin(final String inputPassword) {
        PostgreSQLPacketPayload payload = new PostgreSQLPacketPayload(createByteBuf(16, 128), StandardCharsets.UTF_8);
        payload.writeInt4(64);
        payload.writeInt4(196608);
        payload.writeStringNul("user");
        payload.writeStringNul(username);
        payload.writeStringNul("client_encoding");
        payload.writeStringNul("UTF8");
        PostgreSQLAuthenticationEngine engine = new PostgreSQLAuthenticationEngine();
        AuthenticationResult actual = engine.authenticate(channelHandlerContext, payload);
        assertFalse(actual.isFinished());
        assertThat(actual.getUsername(), is(username));
        ArgumentCaptor<PostgreSQLMD5PasswordAuthenticationPacket> argumentCaptor = ArgumentCaptor.forClass(PostgreSQLMD5PasswordAuthenticationPacket.class);
        verify(channelHandlerContext).writeAndFlush(argumentCaptor.capture());
        PostgreSQLMD5PasswordAuthenticationPacket md5PasswordPacket = argumentCaptor.getValue();
        byte[] md5Salt = getMd5Salt(md5PasswordPacket);
        payload = new PostgreSQLPacketPayload(createByteBuf(16, 128), StandardCharsets.UTF_8);
        String md5Digest = md5Encode(username, inputPassword, md5Salt);
        payload.writeInt1('p');
        payload.writeInt4(4 + md5Digest.length() + 1);
        payload.writeStringNul(md5Digest);
        MetaDataContexts metaDataContexts = getMetaDataContexts(new ShardingSphereUser(username, password, ""));
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(contextManager.getMetaDataContexts()).thenReturn(metaDataContexts);
        ProxyContext.init(contextManager);
        actual = engine.authenticate(channelHandlerContext, payload);
        assertThat(actual.isFinished(), is(password.equals(inputPassword)));
    }
    
    private ByteBuf createByteBuf(final int initialCapacity, final int maxCapacity) {
        return new UnpooledHeapByteBuf(UnpooledByteBufAllocator.DEFAULT, initialCapacity, maxCapacity);
    }
    
    private MetaDataContexts getMetaDataContexts(final ShardingSphereUser user) {
        return new MetaDataContexts(mock(MetaDataPersistService.class),
                new ShardingSphereMetaData(new LinkedHashMap<>(), buildGlobalRuleMetaData(user), new ConfigurationProperties(new Properties())));
    }
    
    private ShardingSphereRuleMetaData buildGlobalRuleMetaData(final ShardingSphereUser user) {
        AuthorityRuleConfiguration ruleConfig = new AuthorityRuleConfiguration(Collections.singletonList(user), new AlgorithmConfiguration("ALL_PERMITTED", new Properties()));
        AuthorityRule rule = new AuthorityRuleBuilder().build(ruleConfig, Collections.emptyMap(), mock(InstanceContext.class));
        return new ShardingSphereRuleMetaData(Collections.singletonList(rule));
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private byte[] getMd5Salt(final PostgreSQLMD5PasswordAuthenticationPacket md5PasswordPacket) {
        Field field = PostgreSQLMD5PasswordAuthenticationPacket.class.getDeclaredField("md5Salt");
        field.setAccessible(true);
        return (byte[]) field.get(md5PasswordPacket);
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private String md5Encode(final String username, final String password, final byte[] md5Salt) {
        Method method = PostgreSQLMD5PasswordAuthenticator.class.getDeclaredMethod("md5Encode", String.class, String.class, byte[].class);
        method.setAccessible(true);
        return (String) method.invoke(new PostgreSQLMD5PasswordAuthenticator(), username, password, md5Salt);
    }
}
