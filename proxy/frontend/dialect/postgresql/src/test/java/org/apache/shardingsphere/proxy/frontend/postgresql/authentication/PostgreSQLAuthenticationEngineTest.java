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
import io.netty.handler.ssl.SslHandler;
import io.netty.util.Attribute;
import lombok.SneakyThrows;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.shardingsphere.authentication.result.AuthenticationResult;
import org.apache.shardingsphere.authority.config.AuthorityRuleConfiguration;
import org.apache.shardingsphere.authority.config.UserConfiguration;
import org.apache.shardingsphere.authority.model.ShardingSpherePrivileges;
import org.apache.shardingsphere.authority.rule.AuthorityRule;
import org.apache.shardingsphere.authority.rule.builder.AuthorityRuleBuilder;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.exception.core.exception.syntax.database.UnknownDatabaseException;
import org.apache.shardingsphere.database.exception.postgresql.exception.authority.EmptyUsernameException;
import org.apache.shardingsphere.database.exception.postgresql.exception.authority.InvalidPasswordException;
import org.apache.shardingsphere.database.exception.postgresql.exception.authority.PrivilegeNotGrantedException;
import org.apache.shardingsphere.database.exception.postgresql.exception.authority.UnknownUsernameException;
import org.apache.shardingsphere.database.exception.postgresql.exception.protocol.ProtocolViolationException;
import org.apache.shardingsphere.database.protocol.constant.CommonConstants;
import org.apache.shardingsphere.database.protocol.payload.PacketPayload;
import org.apache.shardingsphere.database.protocol.postgresql.constant.PostgreSQLAuthenticationMethod;
import org.apache.shardingsphere.database.protocol.postgresql.packet.handshake.PostgreSQLAuthenticationOKPacket;
import org.apache.shardingsphere.database.protocol.postgresql.packet.generic.PostgreSQLReadyForQueryPacket;
import org.apache.shardingsphere.database.protocol.postgresql.packet.handshake.PostgreSQLSSLUnwillingPacket;
import org.apache.shardingsphere.database.protocol.postgresql.packet.handshake.PostgreSQLSSLWillingPacket;
import org.apache.shardingsphere.database.protocol.postgresql.packet.handshake.authentication.PostgreSQLPasswordAuthenticationPacket;
import org.apache.shardingsphere.database.protocol.postgresql.payload.PostgreSQLPacketPayload;
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereStatistics;
import org.apache.shardingsphere.infra.metadata.user.Grantee;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUser;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.frontend.connection.ConnectionIdGenerator;
import org.apache.shardingsphere.proxy.frontend.ssl.ProxySSLContext;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.internal.configuration.plugins.Plugins;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings({ProxyContext.class, ProxySSLContext.class})
class PostgreSQLAuthenticationEngineTest {
    
    private static final String USERNAME = "root";
    
    private static final String PASSWORD = "sharding";
    
    private static final String DATABASE_NAME = "sharding_db";
    
    private PostgreSQLAuthenticationEngine authenticationEngine;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ChannelHandlerContext channelHandlerContext;
    
    @SuppressWarnings("unchecked")
    @BeforeEach
    void setup() {
        authenticationEngine = new PostgreSQLAuthenticationEngine();
        when(channelHandlerContext.channel().attr(CommonConstants.CHARSET_ATTRIBUTE_KEY)).thenReturn(mock(Attribute.class));
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    @Test
    void assertHandshakeAssignsNextConnectionId() {
        Plugins.getMemberAccessor().set(ConnectionIdGenerator.class.getDeclaredField("currentId"), ConnectionIdGenerator.getInstance(), 0);
        assertThat(authenticationEngine.handshake(channelHandlerContext), is(1));
    }
    
    @Test
    void assertSSLUnwilling() {
        ByteBuf byteBuf = createByteBuf(8, 8);
        byteBuf.writeInt(8);
        byteBuf.writeInt(80877103);
        PacketPayload payload = new PostgreSQLPacketPayload(byteBuf, StandardCharsets.UTF_8);
        ChannelHandlerContext context = mock(ChannelHandlerContext.class);
        AuthenticationResult actual = authenticationEngine.authenticate(context, payload);
        verify(context).writeAndFlush(any(PostgreSQLSSLUnwillingPacket.class));
        assertFalse(actual.isFinished());
    }
    
    @Test
    void assertSSLWilling() {
        ByteBuf byteBuf = createByteBuf(8, 8);
        byteBuf.writeInt(8);
        byteBuf.writeInt(80877103);
        PacketPayload payload = new PostgreSQLPacketPayload(byteBuf, StandardCharsets.UTF_8);
        ChannelHandlerContext context = mock(ChannelHandlerContext.class, RETURNS_DEEP_STUBS);
        when(ProxySSLContext.getInstance().isSSLEnabled()).thenReturn(true);
        AuthenticationResult actual = authenticationEngine.authenticate(context, payload);
        verify(context).writeAndFlush(any(PostgreSQLSSLWillingPacket.class));
        verify(context.pipeline()).addFirst(eq(SslHandler.class.getSimpleName()), any(SslHandler.class));
        assertFalse(actual.isFinished());
    }
    
    @Test
    void assertSSLRequestCodeMismatchFallsBackToStartup() {
        ByteBuf byteBuf = createByteBuf(8, 8);
        byteBuf.writeInt(8);
        byteBuf.writeInt(80877104);
        PacketPayload payload = new PostgreSQLPacketPayload(byteBuf, StandardCharsets.UTF_8);
        ProxyContext proxyContext = mock(ProxyContext.class);
        ContextManager contextManager = mock(ContextManager.class);
        when(contextManager.getMetaDataContexts()).thenReturn(createMetaDataContexts(mock(AuthorityRule.class), false, null));
        when(ProxyContext.getInstance()).thenReturn(proxyContext);
        when(proxyContext.getContextManager()).thenReturn(contextManager);
        assertThrows(EmptyUsernameException.class, () -> authenticationEngine.authenticate(channelHandlerContext, payload));
    }
    
    @Test
    void assertUserNotSet() {
        PostgreSQLPacketPayload payload = new PostgreSQLPacketPayload(createByteBuf(8, 512), StandardCharsets.UTF_8);
        payload.writeInt4(64);
        payload.writeInt4(196608);
        payload.writeStringNul("client_encoding");
        payload.writeStringNul("UTF8");
        ContextManager contextManager = mockContextManager(createMetaDataContexts(mock(AuthorityRule.class), false, null));
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        assertThrows(EmptyUsernameException.class, () -> authenticationEngine.authenticate(channelHandlerContext, payload));
    }
    
    @Test
    void assertStartupUsesPasswordAuthenticator() {
        UserConfiguration userConfig = new UserConfiguration(USERNAME, PASSWORD, "", PostgreSQLAuthenticationMethod.PASSWORD.getMethodName(), false);
        HashMap<String, AlgorithmConfiguration> authenticators = new HashMap<>(1);
        authenticators.put(PostgreSQLAuthenticationMethod.PASSWORD.getMethodName(), new AlgorithmConfiguration(PostgreSQLAuthenticationMethod.PASSWORD.getMethodName(), new Properties()));
        AuthorityRule authorityRule = createAuthorityRule(userConfig, authenticators, PostgreSQLAuthenticationMethod.PASSWORD.getMethodName());
        MetaDataContexts metaDataContexts = createMetaDataContexts(authorityRule, false, null);
        ContextManager contextManager = mockContextManager(metaDataContexts);
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        PostgreSQLPacketPayload payload = createStartupPayload(USERNAME, DATABASE_NAME);
        AuthenticationResult actual = authenticationEngine.authenticate(channelHandlerContext, payload);
        ArgumentCaptor<Object> argumentCaptor = ArgumentCaptor.forClass(Object.class);
        verify(channelHandlerContext).writeAndFlush(argumentCaptor.capture());
        assertThat(argumentCaptor.getValue().getClass(), is((Object) PostgreSQLPasswordAuthenticationPacket.class));
        assertFalse(actual.isFinished());
    }
    
    @Test
    void assertAuthenticateWithNonPasswordMessage() {
        setAlreadyReceivedStartupMessage(authenticationEngine);
        PostgreSQLPacketPayload payload = new PostgreSQLPacketPayload(createByteBuf(8, 16), StandardCharsets.UTF_8);
        payload.writeInt1('F');
        payload.writeInt8(0L);
        ContextManager contextManager = mockContextManager(createMetaDataContexts(mock(AuthorityRule.class), false, null));
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        assertThrows(ProtocolViolationException.class, () -> authenticationEngine.authenticate(channelHandlerContext, payload));
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private void setAlreadyReceivedStartupMessage(final PostgreSQLAuthenticationEngine target) {
        Plugins.getMemberAccessor().set(PostgreSQLAuthenticationEngine.class.getDeclaredField("startupMessageReceived"), target, true);
    }
    
    @Test
    void assertLoginFailed() {
        PostgreSQLPacketPayload payload = createStartupPayload(USERNAME, DATABASE_NAME);
        AuthorityRule authorityRule = createAuthorityRule(new UserConfiguration(USERNAME, PASSWORD, "", null, false), Collections.emptyMap(), null);
        MetaDataContexts metaDataContexts = createMetaDataContexts(authorityRule, true, DATABASE_NAME);
        ContextManager contextManager = mockContextManager(metaDataContexts);
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        authenticationEngine.authenticate(channelHandlerContext, payload);
        byte[] md5Salt = getMd5Salt(authenticationEngine);
        PostgreSQLPacketPayload passwordPayload = createPasswordMessage(createMd5Digest(USERNAME, "wrong" + PASSWORD, md5Salt));
        assertThrows(InvalidPasswordException.class, () -> authenticationEngine.authenticate(channelHandlerContext, passwordPayload));
    }
    
    @Test
    void assertLoginWithUnknownDatabase() {
        PostgreSQLPacketPayload payload = createStartupPayload(USERNAME, "missing_db");
        AuthorityRule authorityRule = createAuthorityRule(new UserConfiguration(USERNAME, PASSWORD, "", null, false), Collections.emptyMap(), null);
        MetaDataContexts metaDataContexts = createMetaDataContexts(authorityRule, false, null);
        ContextManager contextManager = mockContextManager(metaDataContexts);
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        authenticationEngine.authenticate(channelHandlerContext, payload);
        byte[] md5Salt = getMd5Salt(authenticationEngine);
        PostgreSQLPacketPayload passwordPayload = createPasswordMessage(createMd5Digest(USERNAME, PASSWORD, md5Salt));
        assertThrows(UnknownDatabaseException.class, () -> authenticationEngine.authenticate(channelHandlerContext, passwordPayload));
    }
    
    @Test
    void assertLoginWithUnknownUsername() {
        PostgreSQLPacketPayload payload = createStartupPayload(USERNAME, null);
        AuthorityRule authorityRule = mock(AuthorityRule.class);
        when(authorityRule.findUser(any(Grantee.class))).thenReturn(Optional.empty());
        MetaDataContexts metaDataContexts = createMetaDataContexts(authorityRule, true, DATABASE_NAME);
        ContextManager contextManager = mockContextManager(metaDataContexts);
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        authenticationEngine.authenticate(channelHandlerContext, payload);
        byte[] md5Salt = getMd5Salt(authenticationEngine);
        PostgreSQLPacketPayload passwordPayload = createPasswordMessage(createMd5Digest(USERNAME, PASSWORD, md5Salt));
        assertThrows(UnknownUsernameException.class, () -> authenticationEngine.authenticate(channelHandlerContext, passwordPayload));
    }
    
    @Test
    void assertLoginWithoutPrivilege() {
        PostgreSQLPacketPayload payload = createStartupPayload(USERNAME, DATABASE_NAME);
        AuthorityRule authorityRule = mock(AuthorityRule.class);
        ShardingSphereUser user = new ShardingSphereUser(USERNAME, PASSWORD, "");
        ShardingSpherePrivileges privileges = mock(ShardingSpherePrivileges.class);
        when(privileges.hasPrivileges(DATABASE_NAME)).thenReturn(false);
        when(authorityRule.findUser(any(Grantee.class))).thenReturn(Optional.of(user));
        when(authorityRule.findPrivileges(any(Grantee.class))).thenReturn(Optional.of(privileges));
        when(authorityRule.getAuthenticatorType(user)).thenReturn(PostgreSQLAuthenticationMethod.MD5.getMethodName());
        MetaDataContexts metaDataContexts = createMetaDataContexts(authorityRule, true, DATABASE_NAME);
        ContextManager contextManager = mockContextManager(metaDataContexts);
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        authenticationEngine.authenticate(channelHandlerContext, payload);
        byte[] md5Salt = getMd5Salt(authenticationEngine);
        PostgreSQLPacketPayload passwordPayload = createPasswordMessage(createMd5Digest(USERNAME, PASSWORD, md5Salt));
        assertThrows(PrivilegeNotGrantedException.class, () -> authenticationEngine.authenticate(channelHandlerContext, passwordPayload));
    }
    
    @Test
    void assertLoginWithNullDatabase() {
        PostgreSQLPacketPayload payload = createStartupPayload(USERNAME, null);
        AuthorityRule authorityRule = createAuthorityRule(new UserConfiguration(USERNAME, PASSWORD, "", null, false), Collections.emptyMap(), null);
        MetaDataContexts metaDataContexts = createMetaDataContexts(authorityRule, false, null);
        ContextManager contextManager = mockContextManager(metaDataContexts);
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        authenticationEngine.authenticate(channelHandlerContext, payload);
        byte[] md5Salt = getMd5Salt(authenticationEngine);
        PostgreSQLPacketPayload passwordPayload = createPasswordMessage(createMd5Digest(USERNAME, PASSWORD, md5Salt));
        AuthenticationResult actual = authenticationEngine.authenticate(channelHandlerContext, passwordPayload);
        verify(channelHandlerContext).write(any(PostgreSQLAuthenticationOKPacket.class));
        verify(channelHandlerContext).writeAndFlush(PostgreSQLReadyForQueryPacket.NOT_IN_TRANSACTION);
        assertThat(actual.isFinished(), is(true));
    }
    
    private ByteBuf createByteBuf(final int initialCapacity, final int maxCapacity) {
        return new UnpooledHeapByteBuf(UnpooledByteBufAllocator.DEFAULT, initialCapacity, maxCapacity);
    }
    
    private PostgreSQLPacketPayload createStartupPayload(final String username, final String databaseName) {
        PostgreSQLPacketPayload payload = new PostgreSQLPacketPayload(createByteBuf(32, 256), StandardCharsets.UTF_8);
        payload.writeInt4(64);
        payload.writeInt4(196608);
        payload.writeStringNul("user");
        payload.writeStringNul(username);
        if (null != databaseName) {
            payload.writeStringNul("database");
            payload.writeStringNul(databaseName);
        }
        payload.writeStringNul("client_encoding");
        payload.writeStringNul("UTF8");
        return payload;
    }
    
    private PostgreSQLPacketPayload createPasswordMessage(final String digest) {
        PostgreSQLPacketPayload payload = new PostgreSQLPacketPayload(createByteBuf(16, 128), StandardCharsets.UTF_8);
        payload.writeInt1('p');
        payload.writeInt4(4 + digest.length() + 1);
        payload.writeStringNul(digest);
        return payload;
    }
    
    private ContextManager mockContextManager(final MetaDataContexts metaDataContexts) {
        ContextManager result = mock(ContextManager.class);
        when(result.getMetaDataContexts()).thenReturn(metaDataContexts);
        return result;
    }
    
    private MetaDataContexts createMetaDataContexts(final AuthorityRule authorityRule, final boolean containsDatabase, final String databaseName) {
        RuleMetaData ruleMetaData = new RuleMetaData(Collections.singleton(authorityRule));
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(containsDatabase && null != databaseName ? Collections.singleton(createDatabase(databaseName)) : Collections.emptyList(),
                mock(ResourceMetaData.class), ruleMetaData, new ConfigurationProperties(new Properties()));
        return new MetaDataContexts(metaData, new ShardingSphereStatistics());
    }
    
    private ShardingSphereDatabase createDatabase(final String databaseName) {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getName()).thenReturn(databaseName);
        when(database.getProtocolType()).thenReturn(TypedSPILoader.getService(DatabaseType.class, "PostgreSQL"));
        when(database.getRuleMetaData()).thenReturn(new RuleMetaData(Collections.emptyList()));
        return database;
    }
    
    private AuthorityRule createAuthorityRule(final UserConfiguration userConfig, final Map<String, AlgorithmConfiguration> authenticators, final String defaultAuthenticator) {
        AuthorityRuleConfiguration ruleConfig = new AuthorityRuleConfiguration(
                Collections.singleton(userConfig), new AlgorithmConfiguration("ALL_PERMITTED", new Properties()), authenticators, defaultAuthenticator);
        return new AuthorityRuleBuilder().build(ruleConfig, Collections.emptyList(), mock(ConfigurationProperties.class));
    }
    
    private String createMd5Digest(final String username, final String password, final byte[] md5Salt) {
        String passwordHash = new String(Hex.encodeHex(DigestUtils.md5(password + username), true));
        MessageDigest messageDigest = DigestUtils.getMd5Digest();
        messageDigest.update(passwordHash.getBytes());
        messageDigest.update(md5Salt);
        return "md5" + new String(Hex.encodeHex(messageDigest.digest(), true));
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private byte[] getMd5Salt(final PostgreSQLAuthenticationEngine target) {
        return (byte[]) Plugins.getMemberAccessor().get(PostgreSQLAuthenticationEngine.class.getDeclaredField("md5Salt"), target);
    }
}
