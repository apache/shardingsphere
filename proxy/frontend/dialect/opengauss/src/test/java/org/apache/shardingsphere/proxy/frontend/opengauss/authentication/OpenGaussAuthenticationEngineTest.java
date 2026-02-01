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

package org.apache.shardingsphere.proxy.frontend.opengauss.authentication;

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
import org.apache.shardingsphere.database.protocol.opengauss.constant.OpenGaussAuthenticationMethod;
import org.apache.shardingsphere.database.protocol.opengauss.constant.OpenGaussProtocolVersion;
import org.apache.shardingsphere.database.protocol.opengauss.packet.authentication.OpenGaussAuthenticationHexData;
import org.apache.shardingsphere.database.protocol.payload.PacketPayload;
import org.apache.shardingsphere.database.protocol.postgresql.constant.PostgreSQLAuthenticationMethod;
import org.apache.shardingsphere.database.protocol.postgresql.packet.generic.PostgreSQLReadyForQueryPacket;
import org.apache.shardingsphere.database.protocol.postgresql.packet.handshake.PostgreSQLAuthenticationOKPacket;
import org.apache.shardingsphere.database.protocol.postgresql.packet.handshake.PostgreSQLSSLUnwillingPacket;
import org.apache.shardingsphere.database.protocol.postgresql.packet.handshake.PostgreSQLSSLWillingPacket;
import org.apache.shardingsphere.database.protocol.postgresql.packet.handshake.authentication.PostgreSQLMD5PasswordAuthenticationPacket;
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
import org.apache.shardingsphere.infra.util.string.HexStringUtils;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.frontend.ssl.ProxySSLContext;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.internal.configuration.plugins.Plugins;

import javax.crypto.Mac;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.SSLEngine;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings({ProxyContext.class, ProxySSLContext.class})
class OpenGaussAuthenticationEngineTest {
    
    private static final String USERNAME = "root";
    
    private static final String PASSWORD = "sharding";
    
    private static final String DATABASE_NAME = "sharding_db";
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ChannelHandlerContext channelHandlerContext;
    
    private OpenGaussAuthenticationEngine authenticationEngine;
    
    @SuppressWarnings("unchecked")
    @BeforeEach
    void setup() {
        authenticationEngine = new OpenGaussAuthenticationEngine();
        when(channelHandlerContext.channel().attr(CommonConstants.CHARSET_ATTRIBUTE_KEY)).thenReturn(mock(Attribute.class));
    }
    
    @Test
    void assertHandshakeGeneratesConnectionId() {
        int actual = authenticationEngine.handshake(channelHandlerContext);
        assertTrue(actual > 0);
    }
    
    @Test
    void assertSSLUnwilling() {
        ProxySSLContext proxySSLContext = mock(ProxySSLContext.class);
        when(proxySSLContext.isSSLEnabled()).thenReturn(false);
        when(ProxySSLContext.getInstance()).thenReturn(proxySSLContext);
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
        ProxySSLContext proxySSLContext = mock(ProxySSLContext.class);
        when(proxySSLContext.isSSLEnabled()).thenReturn(true);
        when(proxySSLContext.newSSLEngine(any())).thenReturn(mock(SSLEngine.class));
        when(ProxySSLContext.getInstance()).thenReturn(proxySSLContext);
        ByteBuf byteBuf = createByteBuf(8, 8);
        byteBuf.writeInt(8);
        byteBuf.writeInt(80877103);
        PacketPayload payload = new PostgreSQLPacketPayload(byteBuf, StandardCharsets.UTF_8);
        ChannelHandlerContext context = mock(ChannelHandlerContext.class, RETURNS_DEEP_STUBS);
        AuthenticationResult actual = authenticationEngine.authenticate(context, payload);
        verify(context).writeAndFlush(any(PostgreSQLSSLWillingPacket.class));
        verify(context.pipeline()).addFirst(eq(SslHandler.class.getSimpleName()), any(SslHandler.class));
        assertFalse(actual.isFinished());
    }
    
    @Test
    void assertSSLRequestCodeMismatchFallsBackToStartup() {
        PostgreSQLPacketPayload payload = new PostgreSQLPacketPayload(createByteBuf(64, 256), StandardCharsets.UTF_8);
        payload.writeInt4(8);
        payload.writeInt4(1234);
        payload.writeStringNul("user");
        payload.writeStringNul(USERNAME);
        payload.writeStringNul("database");
        payload.writeStringNul(DATABASE_NAME);
        payload.writeStringNul("client_encoding");
        payload.writeStringNul("UTF8");
        Map<String, AlgorithmConfiguration> authenticators = Collections.singletonMap(PostgreSQLAuthenticationMethod.MD5.getMethodName(), new AlgorithmConfiguration("MD5", new Properties()));
        AuthorityRule authorityRule = createAuthorityRule(new UserConfiguration(USERNAME, PASSWORD, "", PostgreSQLAuthenticationMethod.MD5.getMethodName(), false),
                authenticators, PostgreSQLAuthenticationMethod.MD5.getMethodName(), new AlgorithmConfiguration("ALL_PERMITTED", new Properties()));
        MetaDataContexts metaDataContexts = createMetaDataContexts(authorityRule, true, DATABASE_NAME);
        mockProxyContext(metaDataContexts);
        assertFalse(authenticationEngine.authenticate(channelHandlerContext, payload).isFinished());
    }
    
    @Test
    void assertUserNotSet() {
        PostgreSQLPacketPayload payload = new PostgreSQLPacketPayload(createByteBuf(8, 512), StandardCharsets.UTF_8);
        payload.writeInt4(64);
        payload.writeInt4(196608);
        payload.writeStringNul("client_encoding");
        payload.writeStringNul("UTF8");
        MetaDataContexts metaDataContexts = createMetaDataContexts(createAuthorityRule(new UserConfiguration(USERNAME, PASSWORD, "", null, false),
                Collections.emptyMap(), null, new AlgorithmConfiguration("ALL_PERMITTED", new Properties())), false, null);
        mockProxyContext(metaDataContexts);
        assertThrows(EmptyUsernameException.class, () -> authenticationEngine.authenticate(channelHandlerContext, payload));
    }
    
    @Test
    void assertStartupRequestMd5Authenticator() {
        Map<String, AlgorithmConfiguration> authenticators = Collections.singletonMap(PostgreSQLAuthenticationMethod.MD5.getMethodName(), new AlgorithmConfiguration("MD5", new Properties()));
        AuthorityRule authorityRule = createAuthorityRule(new UserConfiguration(USERNAME, PASSWORD, "", PostgreSQLAuthenticationMethod.MD5.getMethodName(), false),
                authenticators, PostgreSQLAuthenticationMethod.MD5.getMethodName(), new AlgorithmConfiguration("ALL_PERMITTED", new Properties()));
        MetaDataContexts metaDataContexts = createMetaDataContexts(authorityRule, true, DATABASE_NAME);
        mockProxyContext(metaDataContexts);
        PostgreSQLPacketPayload payload = createStartupPayload(USERNAME, DATABASE_NAME, OpenGaussProtocolVersion.PROTOCOL_351.getVersion());
        AuthenticationResult actual = authenticationEngine.authenticate(channelHandlerContext, payload);
        verify(channelHandlerContext).writeAndFlush(any(PostgreSQLMD5PasswordAuthenticationPacket.class));
        assertFalse(actual.isFinished());
        assertThat(getMd5Salt(authenticationEngine).length, is(4));
    }
    
    @Test
    void assertStartupRequestScramWithProtocol350() {
        AuthorityRule authorityRule = createAuthorityRule(new UserConfiguration(USERNAME, PASSWORD, "", OpenGaussAuthenticationMethod.SCRAM_SHA256.getMethodName(), false),
                Collections.emptyMap(), null, new AlgorithmConfiguration("ALL_PERMITTED", new Properties()));
        MetaDataContexts metaDataContexts = createMetaDataContexts(authorityRule, true, DATABASE_NAME);
        mockProxyContext(metaDataContexts);
        PostgreSQLPacketPayload payload = createStartupPayload(USERNAME, DATABASE_NAME, OpenGaussProtocolVersion.PROTOCOL_350.getVersion());
        assertFalse(authenticationEngine.authenticate(channelHandlerContext, payload).isFinished());
        assertThat(getServerIteration(authenticationEngine), is(2048));
    }
    
    @Test
    void assertStartupRequestScramWithProtocol351WhenUserMissing() {
        AuthorityRule authorityRule = mock(AuthorityRule.class);
        when(authorityRule.findUser(any(Grantee.class))).thenReturn(Optional.empty());
        MetaDataContexts metaDataContexts = createMetaDataContexts(authorityRule, true, DATABASE_NAME);
        mockProxyContext(metaDataContexts);
        assertFalse(authenticationEngine.authenticate(channelHandlerContext, createStartupPayload(USERNAME, DATABASE_NAME, OpenGaussProtocolVersion.PROTOCOL_351.getVersion())).isFinished());
        assertThat(getServerIteration(authenticationEngine), is(10000));
    }
    
    @Test
    void assertAuthenticateWithNonPasswordMessage() {
        setAlreadyReceivedStartupMessage(authenticationEngine);
        PostgreSQLPacketPayload payload = new PostgreSQLPacketPayload(createByteBuf(8, 16), StandardCharsets.UTF_8);
        payload.writeInt1('F');
        payload.writeInt8(0L);
        MetaDataContexts metaDataContexts = createMetaDataContexts(createAuthorityRule(new UserConfiguration(USERNAME, PASSWORD, "", null, false),
                Collections.emptyMap(), null, new AlgorithmConfiguration("ALL_PERMITTED", new Properties())), true, DATABASE_NAME);
        mockProxyContext(metaDataContexts);
        assertThrows(ProtocolViolationException.class, () -> authenticationEngine.authenticate(channelHandlerContext, payload));
    }
    
    @Test
    void assertLoginWithUnknownDatabase() {
        Map<String, AlgorithmConfiguration> authenticators = Collections.singletonMap(PostgreSQLAuthenticationMethod.MD5.getMethodName(), new AlgorithmConfiguration("MD5", new Properties()));
        AuthorityRule authorityRule = createAuthorityRule(new UserConfiguration(USERNAME, PASSWORD, "", PostgreSQLAuthenticationMethod.MD5.getMethodName(), false),
                authenticators, PostgreSQLAuthenticationMethod.MD5.getMethodName(), new AlgorithmConfiguration("ALL_PERMITTED", new Properties()));
        MetaDataContexts metaDataContexts = createMetaDataContexts(authorityRule, false, null);
        mockProxyContext(metaDataContexts);
        authenticationEngine.authenticate(channelHandlerContext, createStartupPayload(USERNAME, DATABASE_NAME, OpenGaussProtocolVersion.PROTOCOL_351.getVersion()));
        byte[] md5Salt = getMd5Salt(authenticationEngine);
        PostgreSQLPacketPayload passwordPayload = createPasswordMessage(createMd5Digest(USERNAME, PASSWORD, md5Salt));
        assertThrows(UnknownDatabaseException.class, () -> authenticationEngine.authenticate(channelHandlerContext, passwordPayload));
    }
    
    @Test
    void assertLoginWithUnknownUsername() {
        AuthorityRule authorityRule = mock(AuthorityRule.class);
        when(authorityRule.findUser(any(Grantee.class))).thenReturn(Optional.empty());
        MetaDataContexts metaDataContexts = createMetaDataContexts(authorityRule, true, DATABASE_NAME);
        mockProxyContext(metaDataContexts);
        authenticationEngine.authenticate(channelHandlerContext, createStartupPayload(USERNAME, DATABASE_NAME, OpenGaussProtocolVersion.PROTOCOL_351.getVersion()));
        PostgreSQLPacketPayload passwordPayload = createPasswordMessage("any");
        assertThrows(UnknownUsernameException.class, () -> authenticationEngine.authenticate(channelHandlerContext, passwordPayload));
    }
    
    @Test
    void assertLoginWithInvalidPassword() {
        Map<String, AlgorithmConfiguration> authenticators = Collections.singletonMap(PostgreSQLAuthenticationMethod.MD5.getMethodName(), new AlgorithmConfiguration("MD5", new Properties()));
        AuthorityRule authorityRule = createAuthorityRule(new UserConfiguration(USERNAME, PASSWORD, "", PostgreSQLAuthenticationMethod.MD5.getMethodName(), false),
                authenticators, PostgreSQLAuthenticationMethod.MD5.getMethodName(), new AlgorithmConfiguration("ALL_PERMITTED", new Properties()));
        MetaDataContexts metaDataContexts = createMetaDataContexts(authorityRule, true, DATABASE_NAME);
        mockProxyContext(metaDataContexts);
        authenticationEngine.authenticate(channelHandlerContext, createStartupPayload(USERNAME, DATABASE_NAME, OpenGaussProtocolVersion.PROTOCOL_351.getVersion()));
        byte[] md5Salt = getMd5Salt(authenticationEngine);
        PostgreSQLPacketPayload passwordPayload = createPasswordMessage(createMd5Digest(USERNAME, "wrong" + PASSWORD, md5Salt));
        assertThrows(InvalidPasswordException.class, () -> authenticationEngine.authenticate(channelHandlerContext, passwordPayload));
    }
    
    @Test
    void assertLoginWithoutPrivilege() {
        AuthorityRule authorityRule = mock(AuthorityRule.class);
        ShardingSphereUser user = new ShardingSphereUser(USERNAME, PASSWORD, "");
        ShardingSpherePrivileges privileges = mock(ShardingSpherePrivileges.class);
        when(privileges.hasPrivileges(DATABASE_NAME)).thenReturn(false);
        when(authorityRule.findUser(any(Grantee.class))).thenReturn(Optional.of(user));
        when(authorityRule.findPrivileges(any(Grantee.class))).thenReturn(Optional.of(privileges));
        when(authorityRule.getAuthenticatorType(user)).thenReturn(PostgreSQLAuthenticationMethod.MD5.getMethodName());
        MetaDataContexts metaDataContexts = createMetaDataContexts(authorityRule, true, DATABASE_NAME);
        mockProxyContext(metaDataContexts);
        authenticationEngine.authenticate(channelHandlerContext, createStartupPayload(USERNAME, DATABASE_NAME, OpenGaussProtocolVersion.PROTOCOL_351.getVersion()));
        byte[] md5Salt = getMd5Salt(authenticationEngine);
        PostgreSQLPacketPayload passwordPayload = createPasswordMessage(createMd5Digest(USERNAME, PASSWORD, md5Salt));
        assertThrows(PrivilegeNotGrantedException.class, () -> authenticationEngine.authenticate(channelHandlerContext, passwordPayload));
    }
    
    @Test
    void assertLoginWithNullDatabaseSucceeds() {
        Map<String, AlgorithmConfiguration> authenticators = Collections.singletonMap(PostgreSQLAuthenticationMethod.MD5.getMethodName(), new AlgorithmConfiguration("MD5", new Properties()));
        AuthorityRule authorityRule = createAuthorityRule(new UserConfiguration(USERNAME, PASSWORD, "", PostgreSQLAuthenticationMethod.MD5.getMethodName(), false),
                authenticators, PostgreSQLAuthenticationMethod.MD5.getMethodName(), new AlgorithmConfiguration("ALL_PERMITTED", new Properties()));
        MetaDataContexts metaDataContexts = createMetaDataContexts(authorityRule, false, null);
        mockProxyContext(metaDataContexts);
        authenticationEngine.authenticate(channelHandlerContext, createStartupPayload(USERNAME, null, OpenGaussProtocolVersion.PROTOCOL_351.getVersion()));
        byte[] md5Salt = getMd5Salt(authenticationEngine);
        PostgreSQLPacketPayload passwordPayload = createPasswordMessage(createMd5Digest(USERNAME, PASSWORD, md5Salt));
        AuthenticationResult actual = authenticationEngine.authenticate(channelHandlerContext, passwordPayload);
        verify(channelHandlerContext).write(any(PostgreSQLAuthenticationOKPacket.class));
        verify(channelHandlerContext).writeAndFlush(PostgreSQLReadyForQueryPacket.NOT_IN_TRANSACTION);
        assertTrue(actual.isFinished());
        assertThat(actual.getUsername(), is(USERNAME));
        assertNull(actual.getDatabase());
    }
    
    @Test
    void assertLoginWithScramSucceeds() {
        AuthorityRule authorityRule = createAuthorityRule(new UserConfiguration(USERNAME, PASSWORD, "", OpenGaussAuthenticationMethod.SCRAM_SHA256.getMethodName(), false),
                Collections.emptyMap(), null, new AlgorithmConfiguration("ALL_PERMITTED", new Properties()));
        MetaDataContexts metaDataContexts = createMetaDataContexts(authorityRule, true, DATABASE_NAME);
        mockProxyContext(metaDataContexts);
        authenticationEngine.authenticate(channelHandlerContext, createStartupPayload(USERNAME, DATABASE_NAME, OpenGaussProtocolVersion.PROTOCOL_351.getVersion()));
        OpenGaussAuthenticationHexData authHexData = getAuthHexData(authenticationEngine);
        int serverIteration = getServerIteration(authenticationEngine);
        PostgreSQLPacketPayload passwordPayload = createPasswordMessage(createScramDigest(PASSWORD, authHexData, serverIteration));
        AuthenticationResult actual = authenticationEngine.authenticate(channelHandlerContext, passwordPayload);
        verify(channelHandlerContext).write(any(PostgreSQLAuthenticationOKPacket.class));
        verify(channelHandlerContext).writeAndFlush(PostgreSQLReadyForQueryPacket.NOT_IN_TRANSACTION);
        assertTrue(actual.isFinished());
        assertThat(actual.getUsername(), is(USERNAME));
        assertThat(actual.getDatabase(), is(DATABASE_NAME));
    }
    
    private ByteBuf createByteBuf(final int initialCapacity, final int maxCapacity) {
        return new UnpooledHeapByteBuf(UnpooledByteBufAllocator.DEFAULT, initialCapacity, maxCapacity);
    }
    
    private PostgreSQLPacketPayload createStartupPayload(final String username, final String databaseName, final int version) {
        PostgreSQLPacketPayload payload = new PostgreSQLPacketPayload(createByteBuf(32, 256), StandardCharsets.UTF_8);
        payload.writeInt4(64);
        payload.writeInt4(version);
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
    
    private MetaDataContexts createMetaDataContexts(final AuthorityRule authorityRule, final boolean containsDatabase, final String databaseName) {
        RuleMetaData ruleMetaData = new RuleMetaData(Collections.singleton(authorityRule));
        Collection<ShardingSphereDatabase> databases = containsDatabase && null != databaseName ? Collections.singleton(createDatabase(databaseName)) : Collections.emptyList();
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(databases, mock(ResourceMetaData.class), ruleMetaData, new ConfigurationProperties(new Properties()));
        return new MetaDataContexts(metaData, new ShardingSphereStatistics());
    }
    
    private ShardingSphereDatabase createDatabase(final String databaseName) {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getName()).thenReturn(databaseName);
        when(database.getProtocolType()).thenReturn(TypedSPILoader.getService(DatabaseType.class, "openGauss"));
        when(database.getRuleMetaData()).thenReturn(new RuleMetaData(Collections.emptyList()));
        return database;
    }
    
    private AuthorityRule createAuthorityRule(final UserConfiguration userConfig, final Map<String, AlgorithmConfiguration> authenticators,
                                              final String defaultAuthenticator, final AlgorithmConfiguration privilegeProvider) {
        AuthorityRuleConfiguration ruleConfig = new AuthorityRuleConfiguration(Collections.singleton(userConfig), privilegeProvider, authenticators, defaultAuthenticator);
        return new AuthorityRuleBuilder().build(ruleConfig, Collections.emptyList(), mock(ConfigurationProperties.class));
    }
    
    private void mockProxyContext(final MetaDataContexts metaDataContexts) {
        ContextManager contextManager = mock(ContextManager.class);
        when(contextManager.getMetaDataContexts()).thenReturn(metaDataContexts);
        ProxyContext proxyContext = mock(ProxyContext.class);
        when(proxyContext.getContextManager()).thenReturn(contextManager);
        when(ProxyContext.getInstance()).thenReturn(proxyContext);
    }
    
    private String createMd5Digest(final String username, final String password, final byte[] md5Salt) {
        String passwordHash = new String(Hex.encodeHex(DigestUtils.md5(password + username), true));
        MessageDigest messageDigest = DigestUtils.getMd5Digest();
        messageDigest.update(passwordHash.getBytes());
        messageDigest.update(md5Salt);
        return "md5" + new String(Hex.encodeHex(messageDigest.digest(), true));
    }
    
    @SneakyThrows
    private String createScramDigest(final String password, final OpenGaussAuthenticationHexData authHexData, final int serverIteration) {
        PBEKeySpec keySpec = new PBEKeySpec(password.toCharArray(), Hex.decodeHex(authHexData.getSalt()), serverIteration, 32 * 8);
        byte[] secretKey = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1").generateSecret(keySpec).getEncoded();
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secretKey, "HmacSHA256"));
        byte[] clientKey = mac.doFinal("Client Key".getBytes(StandardCharsets.UTF_8));
        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        byte[] storedKey = sha256.digest(clientKey);
        Mac clientSignatureMac = Mac.getInstance("HmacSHA256");
        clientSignatureMac.init(new SecretKeySpec(storedKey, "HmacSHA256"));
        byte[] clientSignature = clientSignatureMac.doFinal(Hex.decodeHex(authHexData.getNonce()));
        byte[] clientProof = xor(clientKey, clientSignature);
        return HexStringUtils.toHexString(clientProof);
    }
    
    private byte[] xor(final byte[] left, final byte[] right) {
        byte[] result = new byte[left.length];
        for (int index = 0; index < left.length; index++) {
            result[index] = (byte) (left[index] ^ right[index]);
        }
        return result;
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private void setAlreadyReceivedStartupMessage(final OpenGaussAuthenticationEngine target) {
        Plugins.getMemberAccessor().set(OpenGaussAuthenticationEngine.class.getDeclaredField("startupMessageReceived"), target, true);
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private byte[] getMd5Salt(final OpenGaussAuthenticationEngine target) {
        return (byte[]) Plugins.getMemberAccessor().get(OpenGaussAuthenticationEngine.class.getDeclaredField("md5Salt"), target);
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private OpenGaussAuthenticationHexData getAuthHexData(final OpenGaussAuthenticationEngine target) {
        return (OpenGaussAuthenticationHexData) Plugins.getMemberAccessor().get(OpenGaussAuthenticationEngine.class.getDeclaredField("authHexData"), target);
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private int getServerIteration(final OpenGaussAuthenticationEngine target) {
        return (int) Plugins.getMemberAccessor().get(OpenGaussAuthenticationEngine.class.getDeclaredField("serverIteration"), target);
    }
}
