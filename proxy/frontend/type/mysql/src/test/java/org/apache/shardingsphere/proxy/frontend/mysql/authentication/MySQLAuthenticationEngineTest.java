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

package org.apache.shardingsphere.proxy.frontend.mysql.authentication;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.util.Attribute;
import lombok.SneakyThrows;
import org.apache.shardingsphere.authority.provider.simple.model.privilege.AllPrivilegesPermittedShardingSpherePrivileges;
import org.apache.shardingsphere.authority.rule.AuthorityRule;
import org.apache.shardingsphere.db.protocol.constant.CommonConstants;
import org.apache.shardingsphere.db.protocol.mysql.constant.MySQLCapabilityFlag;
import org.apache.shardingsphere.db.protocol.mysql.constant.MySQLConnectionPhase;
import org.apache.shardingsphere.db.protocol.mysql.constant.MySQLConstants;
import org.apache.shardingsphere.db.protocol.mysql.packet.generic.MySQLErrPacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.generic.MySQLOKPacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.handshake.MySQLHandshakePacket;
import org.apache.shardingsphere.db.protocol.mysql.payload.MySQLPacketPayload;
import org.apache.shardingsphere.infra.exception.dialect.exception.syntax.database.UnknownDatabaseException;
import org.apache.shardingsphere.infra.exception.mysql.exception.AccessDeniedException;
import org.apache.shardingsphere.infra.exception.mysql.exception.DatabaseAccessDeniedException;
import org.apache.shardingsphere.infra.exception.mysql.exception.HandshakeException;
import org.apache.shardingsphere.infra.exception.mysql.vendor.MySQLVendorError;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ShardingSphereResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.metadata.user.Grantee;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUser;
import org.apache.shardingsphere.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.frontend.authentication.AuthenticationResultBuilder;
import org.apache.shardingsphere.proxy.frontend.authentication.Authenticator;
import org.apache.shardingsphere.proxy.frontend.authentication.AuthenticatorFactory;
import org.apache.shardingsphere.proxy.frontend.mysql.ssl.MySQLSSLRequestHandler;
import org.apache.shardingsphere.proxy.frontend.ssl.ProxySSLContext;
import org.apache.shardingsphere.test.mock.AutoMockExtension;
import org.apache.shardingsphere.test.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedConstruction;
import org.mockito.MockedConstruction.Context;
import org.mockito.internal.configuration.plugins.Plugins;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings({ProxyContext.class, ProxySSLContext.class})
@MockitoSettings(strictness = Strictness.LENIENT)
class MySQLAuthenticationEngineTest {
    
    private final MySQLAuthenticationEngine authenticationEngine = new MySQLAuthenticationEngine();
    
    private final byte[] authResponse = {-27, 89, -20, -27, 65, -120, -64, -101, 86, -100, -108, -100, 6, -125, -37, 117, 14, -43, 95, -113};
    
    @Test
    void assertHandshakeWithSSLNotEnabled() {
        ChannelHandlerContext context = mockChannelHandlerContext();
        assertTrue(authenticationEngine.handshake(context) > 0);
        verify(context).writeAndFlush(any(MySQLHandshakePacket.class));
    }
    
    @Test
    void assertHandshakeWithSSLEnabled() {
        when(ProxySSLContext.getInstance().isSSLEnabled()).thenReturn(true);
        ChannelHandlerContext context = mockChannelHandlerContext();
        when(context.pipeline()).thenReturn(mock(ChannelPipeline.class));
        assertTrue(authenticationEngine.handshake(context) > 0);
        verify(context.pipeline()).addFirst(eq(MySQLSSLRequestHandler.class.getSimpleName()), any(MySQLSSLRequestHandler.class));
        verify(context).writeAndFlush(any(MySQLHandshakePacket.class));
    }
    
    @Test
    void assertBadHandshakeReceived() {
        AuthorityRule rule = mock(AuthorityRule.class);
        when(rule.getAuthenticatorType(any())).thenReturn("");
        ContextManager contextManager = mockContextManager(rule);
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        ChannelHandlerContext context = mockChannelHandlerContext();
        authenticationEngine.handshake(context);
        try (MockedConstruction<MySQLErrPacket> ignored = mockConstruction(MySQLErrPacket.class, this::assertBadHandshakeError)) {
            assertThrows(HandshakeException.class, () -> authenticationEngine.authenticate(context, new MySQLPacketPayload(Unpooled.wrappedBuffer(new byte[]{0x02, 0x03}), StandardCharsets.UTF_8)));
        }
    }
    
    private void assertBadHandshakeError(final MySQLErrPacket mock, final Context mockContext) {
        List<?> arguments = mockContext.arguments();
        assertThat(arguments.get(0), is(MySQLVendorError.ER_HANDSHAKE_ERROR));
        assertThat(arguments.get(1), is(new Object[0]));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    void assertAuthenticationMethodMismatch() {
        AuthorityRule rule = mock(AuthorityRule.class);
        when(rule.getAuthenticatorType(any())).thenReturn("");
        setConnectionPhase(MySQLConnectionPhase.AUTH_PHASE_FAST_PATH);
        MySQLPacketPayload payload = mock(MySQLPacketPayload.class);
        ChannelHandlerContext channelHandlerContext = mock(ChannelHandlerContext.class);
        Channel channel = mock(Channel.class);
        when(payload.readStringNulByBytes()).thenReturn("root".getBytes());
        when(channel.remoteAddress()).thenReturn(new InetSocketAddress("localhost", 3307));
        when(channel.attr(CommonConstants.CHARSET_ATTRIBUTE_KEY)).thenReturn(mock(Attribute.class));
        when(channel.attr(MySQLConstants.MYSQL_CHARACTER_SET_ATTRIBUTE_KEY)).thenReturn(mock(Attribute.class));
        when(channelHandlerContext.channel()).thenReturn(channel);
        when(payload.readInt1()).thenReturn(1);
        when(payload.readInt4()).thenReturn(MySQLCapabilityFlag.CLIENT_PLUGIN_AUTH.getValue());
        when(payload.readStringNul()).thenReturn("root");
        ContextManager contextManager = mockContextManager(rule);
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        authenticationEngine.authenticate(channelHandlerContext, payload);
        assertThat(getConnectionPhase(), is(MySQLConnectionPhase.AUTHENTICATION_METHOD_MISMATCH));
    }
    
    @Test
    void assertAuthenticationSwitchResponse() {
        setConnectionPhase(MySQLConnectionPhase.AUTHENTICATION_METHOD_MISMATCH);
        MySQLPacketPayload payload = mock(MySQLPacketPayload.class);
        Channel channel = mock(Channel.class);
        ChannelHandlerContext channelHandlerContext = mock(ChannelHandlerContext.class);
        when(payload.readStringEOFByBytes()).thenReturn(authResponse);
        when(channel.remoteAddress()).thenReturn(new InetSocketAddress("localhost", 3307));
        when(channelHandlerContext.channel()).thenReturn(channel);
        setAuthenticationResult();
        AuthorityRule rule = mock(AuthorityRule.class);
        ShardingSphereUser user = new ShardingSphereUser("root", "", "127.0.0.1");
        when(rule.findUser(user.getGrantee())).thenReturn(Optional.of(user));
        when(rule.findPrivileges(user.getGrantee())).thenReturn(Optional.of(new AllPrivilegesPermittedShardingSpherePrivileges()));
        when(rule.getAuthenticatorType(any())).thenReturn("");
        ContextManager contextManager = mockContextManager(rule);
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        authenticationEngine.authenticate(channelHandlerContext, payload);
        assertThat(getAuthResponse(), is(authResponse));
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private void setAuthenticationResult() {
        Plugins.getMemberAccessor().set(MySQLAuthenticationEngine.class.getDeclaredField("currentAuthResult"), authenticationEngine, AuthenticationResultBuilder.continued("root", "", "foo_db"));
    }
    
    @Test
    void assertAuthenticateFailedWithAbsentUser() {
        setConnectionPhase(MySQLConnectionPhase.AUTH_PHASE_FAST_PATH);
        AuthorityRule rule = mock(AuthorityRule.class);
        when(rule.getAuthenticatorType(any())).thenReturn("");
        when(rule.findUser(new Grantee("root", "127.0.0.1"))).thenReturn(Optional.empty());
        ChannelHandlerContext context = mockChannelHandlerContext();
        ContextManager contextManager = mockContextManager(rule);
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        when(ProxyContext.getInstance().databaseExists("foo_db")).thenReturn(true);
        try (MockedConstruction<MySQLErrPacket> ignored = mockConstruction(MySQLErrPacket.class, (mock, mockContext) -> assertAuthenticationErrorPacket(mockContext.arguments()))) {
            assertThrows(AccessDeniedException.class, () -> authenticationEngine.authenticate(context, getPayload("root", "foo_db", authResponse)));
        }
    }
    
    @SuppressWarnings({"rawtypes", "unused"})
    @Test
    void assertAuthenticateFailedWithUnAuthenticatedUser() {
        setConnectionPhase(MySQLConnectionPhase.AUTH_PHASE_FAST_PATH);
        AuthorityRule rule = mock(AuthorityRule.class);
        when(rule.getAuthenticatorType(any())).thenReturn("");
        ShardingSphereUser user = new ShardingSphereUser("root", "", "127.0.0.1");
        when(rule.findUser(user.getGrantee())).thenReturn(Optional.of(user));
        ChannelHandlerContext context = mockChannelHandlerContext();
        ContextManager contextManager = mockContextManager(rule);
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        when(ProxyContext.getInstance().databaseExists("foo_db")).thenReturn(true);
        try (
                MockedConstruction<AuthenticatorFactory> mockedAuthenticatorFactory = mockConstruction(AuthenticatorFactory.class,
                        (mock, mockContext) -> when(mock.newInstance(user)).thenReturn(mock(Authenticator.class)));
                MockedConstruction<MySQLErrPacket> mockedErrPacket = mockConstruction(MySQLErrPacket.class, (mock, mockContext) -> assertAuthenticationErrorPacket(mockContext.arguments()))) {
            assertThrows(AccessDeniedException.class, () -> authenticationEngine.authenticate(context, getPayload("root", "foo_db", authResponse)));
        }
    }
    
    private void assertAuthenticationErrorPacket(final List<?> arguments) {
        assertThat(arguments.get(0), is(MySQLVendorError.ER_ACCESS_DENIED_ERROR));
        assertThat(arguments.get(1), is(new String[]{"root", "127.0.0.1", "YES"}));
    }
    
    @Test
    void assertAuthenticateFailedWithDatabaseAccessDenied() {
        setConnectionPhase(MySQLConnectionPhase.AUTH_PHASE_FAST_PATH);
        AuthorityRule rule = mock(AuthorityRule.class);
        when(rule.getAuthenticatorType(any())).thenReturn("");
        ShardingSphereUser user = new ShardingSphereUser("root", "", "127.0.0.1");
        when(rule.findUser(user.getGrantee())).thenReturn(Optional.of(user));
        ChannelHandlerContext context = mockChannelHandlerContext();
        ContextManager contextManager = mockContextManager(rule);
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        when(ProxyContext.getInstance().databaseExists("foo_db")).thenReturn(true);
        try (MockedConstruction<MySQLErrPacket> ignored = mockConstruction(MySQLErrPacket.class, (mock, mockContext) -> assertDatabaseAccessDeniedErrorPacket(mockContext.arguments()))) {
            assertThrows(DatabaseAccessDeniedException.class, () -> authenticationEngine.authenticate(context, getPayload("root", "foo_db", authResponse)));
        }
    }
    
    private void assertDatabaseAccessDeniedErrorPacket(final List<?> arguments) {
        assertThat(arguments.get(0), is(MySQLVendorError.ER_DBACCESS_DENIED_ERROR));
        assertThat(arguments.get(1), is(new String[]{"root", "127.0.0.1", "foo_db"}));
    }
    
    @Test
    void assertAuthenticateFailedWithInvalidDatabase() {
        AuthorityRule rule = mock(AuthorityRule.class);
        when(rule.getAuthenticatorType(any())).thenReturn("");
        setConnectionPhase(MySQLConnectionPhase.AUTH_PHASE_FAST_PATH);
        ChannelHandlerContext context = mockChannelHandlerContext();
        ContextManager contextManager = mockContextManager(rule);
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        try (MockedConstruction<MySQLErrPacket> ignored = mockConstruction(MySQLErrPacket.class, (mock, mockContext) -> assertInvalidDatabaseErrorPacket(mockContext.arguments()))) {
            assertThrows(UnknownDatabaseException.class, () -> authenticationEngine.authenticate(context, getPayload("root", "invalid_db", authResponse)));
        }
    }
    
    private void assertInvalidDatabaseErrorPacket(final List<?> arguments) {
        assertThat(arguments.get(0), is(MySQLVendorError.ER_BAD_DB_ERROR));
        assertThat(arguments.get(1), is(new String[]{"invalid_db"}));
    }
    
    @Test
    void assertAuthenticateSuccess() {
        setConnectionPhase(MySQLConnectionPhase.AUTH_PHASE_FAST_PATH);
        AuthorityRule rule = mock(AuthorityRule.class);
        when(rule.getAuthenticatorType(any())).thenReturn("");
        ShardingSphereUser user = new ShardingSphereUser("root", "", "127.0.0.1");
        when(rule.findUser(user.getGrantee())).thenReturn(Optional.of(user));
        ChannelHandlerContext context = mockChannelHandlerContext();
        ContextManager contextManager = mockContextManager(rule);
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        authenticationEngine.authenticate(context, getPayload("root", null, authResponse));
        verify(context).writeAndFlush(any(MySQLOKPacket.class));
    }
    
    private ContextManager mockContextManager(final AuthorityRule rule) {
        ContextManager result = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        Map<String, ShardingSphereDatabase> databases = Collections.singletonMap("foo_db", mock(ShardingSphereDatabase.class));
        MetaDataContexts metaDataContexts = new MetaDataContexts(mock(MetaDataPersistService.class), new ShardingSphereMetaData(databases,
                mock(ShardingSphereResourceMetaData.class), new ShardingSphereRuleMetaData(Collections.singleton(rule)), new ConfigurationProperties(new Properties())));
        when(result.getMetaDataContexts()).thenReturn(metaDataContexts);
        return result;
    }
    
    private MySQLPacketPayload getPayload(final String username, final String database, final byte[] authResponse) {
        MySQLPacketPayload result = mock(MySQLPacketPayload.class);
        when(result.readInt1()).thenReturn(1);
        when(result.readInt4()).thenReturn(MySQLCapabilityFlag.CLIENT_CONNECT_WITH_DB.getValue());
        when(result.readStringNul()).thenReturn(username).thenReturn(database);
        when(result.readStringNulByBytes()).thenReturn(authResponse);
        return result;
    }
    
    private ChannelHandlerContext mockChannelHandlerContext() {
        ChannelHandlerContext result = mock(ChannelHandlerContext.class);
        doReturn(getChannel()).when(result).channel();
        return result;
    }
    
    @SuppressWarnings("unchecked")
    private Channel getChannel() {
        Channel result = mock(Channel.class);
        doReturn(getRemoteAddress()).when(result).remoteAddress();
        when(result.attr(CommonConstants.CHARSET_ATTRIBUTE_KEY)).thenReturn(mock(Attribute.class));
        when(result.attr(MySQLConstants.MYSQL_CHARACTER_SET_ATTRIBUTE_KEY)).thenReturn(mock(Attribute.class));
        when(result.attr(MySQLConstants.MYSQL_SEQUENCE_ID)).thenReturn(mock(Attribute.class));
        return result;
    }
    
    private SocketAddress getRemoteAddress() {
        SocketAddress result = mock(SocketAddress.class);
        when(result.toString()).thenReturn("127.0.0.1");
        return result;
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private void setConnectionPhase(final MySQLConnectionPhase connectionPhase) {
        Plugins.getMemberAccessor().set(MySQLAuthenticationEngine.class.getDeclaredField("connectionPhase"), authenticationEngine, connectionPhase);
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private MySQLConnectionPhase getConnectionPhase() {
        return (MySQLConnectionPhase) Plugins.getMemberAccessor().get(MySQLAuthenticationEngine.class.getDeclaredField("connectionPhase"), authenticationEngine);
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private byte[] getAuthResponse() {
        return (byte[]) Plugins.getMemberAccessor().get(MySQLAuthenticationEngine.class.getDeclaredField("authResponse"), authenticationEngine);
    }
}
