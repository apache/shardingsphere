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

package org.apache.shardingsphere.proxy.frontend.firebird.authentication;

import io.netty.channel.ChannelHandlerContext;
import io.netty.util.Attribute;
import lombok.SneakyThrows;
import org.apache.shardingsphere.authentication.AuthenticatorFactory;
import org.apache.shardingsphere.authentication.result.AuthenticationResult;
import org.apache.shardingsphere.authentication.result.AuthenticationResultBuilder;
import org.apache.shardingsphere.authority.rule.AuthorityRule;
import org.apache.shardingsphere.database.exception.core.exception.connection.AccessDeniedException;
import org.apache.shardingsphere.database.exception.core.exception.data.InvalidParameterValueException;
import org.apache.shardingsphere.database.exception.core.exception.protocol.DatabaseProtocolException;
import org.apache.shardingsphere.database.exception.core.exception.syntax.database.UnknownDatabaseException;
import org.apache.shardingsphere.database.protocol.constant.CommonConstants;
import org.apache.shardingsphere.database.protocol.firebird.constant.FirebirdAuthenticationMethod;
import org.apache.shardingsphere.database.protocol.firebird.constant.FirebirdConstant;
import org.apache.shardingsphere.database.protocol.firebird.constant.FirebirdUserDataType;
import org.apache.shardingsphere.database.protocol.firebird.constant.protocol.FirebirdProtocol;
import org.apache.shardingsphere.database.protocol.firebird.constant.protocol.FirebirdProtocolVersion;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.FirebirdCommandPacketType;
import org.apache.shardingsphere.database.protocol.firebird.packet.generic.FirebirdGenericResponsePacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.handshake.FirebirdAcceptPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.handshake.FirebirdAttachPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.handshake.FirebirdConnectPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.handshake.FirebirdContinueAuthPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.handshake.FirebirdSRPAuthenticationData;
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.user.Grantee;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUser;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.firebird.handler.admin.executor.variable.charset.FirebirdCharacterSets;
import org.apache.shardingsphere.proxy.frontend.connection.ConnectionIdGenerator;
import org.apache.shardingsphere.proxy.frontend.firebird.authentication.authenticator.FirebirdAuthenticator;
import org.apache.shardingsphere.proxy.frontend.firebird.resource.FirebirdConnectionResourceManager;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.firebirdsql.gds.ISCConstants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.internal.configuration.plugins.Plugins;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings({
        ConnectionIdGenerator.class, FirebirdConnectionResourceManager.class, ProxyContext.class
})
class FirebirdAuthenticationEngineTest {
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ChannelHandlerContext context;
    
    @Mock
    private ConnectionIdGenerator idGenerator;
    
    @Mock
    private FirebirdConnectionResourceManager connectionResourceManager;
    
    private final FirebirdAuthenticationEngine authenticationEngine = new FirebirdAuthenticationEngine();
    
    @Test
    void assertHandshake() {
        when(ConnectionIdGenerator.getInstance()).thenReturn(idGenerator);
        when(idGenerator.nextId()).thenReturn(1);
        when(FirebirdConnectionResourceManager.getInstance()).thenReturn(connectionResourceManager);
        assertThat(authenticationEngine.handshake(context), is(1));
        verify(connectionResourceManager).registerConnection(1);
    }
    
    @Test
    void assertAuthenticateWithUnsupportedCommand() {
        AuthorityRule rule = mock(AuthorityRule.class);
        mockProxyContext(rule, true);
        FirebirdPacketPayload payload = mockFirebirdPayload(FirebirdCommandPacketType.CONT_AUTH);
        assertThrows(DatabaseProtocolException.class, () -> authenticationEngine.authenticate(context, payload));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("connectArguments")
    void assertAuthenticateConnect(final String name, final String rulePluginType, final FirebirdAuthenticationMethod connectPlugin,
                                   final FirebirdAuthenticationMethod expectedPlugin, final boolean expectAcceptData, final boolean mockAuthData) {
        AuthorityRule rule = mock(AuthorityRule.class);
        ShardingSphereUser user = new ShardingSphereUser("root", "pwd", "");
        when(rule.findUser(any(Grantee.class))).thenReturn(Optional.of(user));
        when(rule.getAuthenticatorType(user)).thenReturn(rulePluginType);
        mockProxyContext(rule, true);
        Attribute<Charset> charsetAttr = mock(Attribute.class);
        Attribute<FirebirdProtocolVersion> protocolVersionAttr = mock(Attribute.class);
        Attribute<Boolean> lazySendAttr = mock(Attribute.class);
        when(context.channel().attr(CommonConstants.CHARSET_ATTRIBUTE_KEY)).thenReturn(charsetAttr);
        when(context.channel().attr(FirebirdConstant.CONNECTION_PROTOCOL_VERSION)).thenReturn(protocolVersionAttr);
        when(context.channel().attr(FirebirdConstant.LAZY_SEND)).thenReturn(lazySendAttr);
        FirebirdPacketPayload payload = mockFirebirdPayload(FirebirdCommandPacketType.CONNECT);
        FirebirdProtocol protocol = mock(FirebirdProtocol.class);
        when(protocol.getVersion()).thenReturn(FirebirdProtocolVersion.PROTOCOL_VERSION10);
        when(protocol.getMaxType()).thenReturn(5);
        List<FirebirdProtocol> userProtocols = new ArrayList<>(Collections.singletonList(protocol));
        byte[] expectedSalt = new byte[]{1, 2};
        if (mockAuthData) {
            try (MockedConstruction<FirebirdSRPAuthenticationData> ignoredAuthDataConstruction = mockConstruction(FirebirdSRPAuthenticationData.class, (authData, construction) -> {
                when(authData.getSalt()).thenReturn(expectedSalt);
                when(authData.getPublicKeyHex()).thenReturn("publicKey");
            });
                    MockedConstruction<FirebirdConnectPacket> ignored = mockConstruction(FirebirdConnectPacket.class, (mockConnect, construction) -> {
                        when(mockConnect.getUserProtocols()).thenReturn(userProtocols);
                        when(mockConnect.getLogin()).thenReturn("root");
                        when(mockConnect.getHost()).thenReturn("host");
                        when(mockConnect.getDatabase()).thenReturn("db");
                        when(mockConnect.getAuthData()).thenReturn("client_key");
                        when(mockConnect.getPluginName()).thenReturn(connectPlugin.getMethodName());
                    })) {
                AuthenticationResult actual = authenticationEngine.authenticate(context, payload);
                assertFalse(actual.isFinished());
                assertThat(actual.getUsername(), is("root"));
                assertThat(actual.getDatabase(), is("db"));
            }
        } else {
            try (MockedConstruction<FirebirdConnectPacket> ignored = mockConstruction(FirebirdConnectPacket.class, (mockConnect, construction) -> {
                when(mockConnect.getUserProtocols()).thenReturn(userProtocols);
                when(mockConnect.getLogin()).thenReturn("root");
                when(mockConnect.getHost()).thenReturn("host");
                when(mockConnect.getDatabase()).thenReturn("db");
                when(mockConnect.getPluginName()).thenReturn(connectPlugin.getMethodName());
            })) {
                AuthenticationResult actual = authenticationEngine.authenticate(context, payload);
                assertFalse(actual.isFinished());
                assertThat(actual.getUsername(), is("root"));
                assertThat(actual.getDatabase(), is("db"));
            }
        }
        ArgumentCaptor<FirebirdAcceptPacket> acceptCaptor = ArgumentCaptor.forClass(FirebirdAcceptPacket.class);
        verify(context).writeAndFlush(acceptCaptor.capture());
        FirebirdAcceptPacket acceptPacket = acceptCaptor.getValue();
        verify(context.channel().attr(FirebirdConstant.LAZY_SEND)).set(true);
        if (expectAcceptData) {
            assertThat(acceptPacket.getAcceptDataPacket().getPlugin(), is(expectedPlugin));
            if (mockAuthData) {
                assertThat(acceptPacket.getAcceptDataPacket().getSalt(), is(expectedSalt));
            } else {
                assertThat(acceptPacket.getAcceptDataPacket().getSalt().length, is(0));
            }
        } else {
            assertNull(acceptPacket.getAcceptDataPacket());
        }
        verify(charsetAttr).set(FirebirdCharacterSets.findCharacterSet("NONE"));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("mismatchedPluginArguments")
    void assertAuthenticateConnectWithMismatchedPlugin(final String name, final String rulePluginType,
                                                       final FirebirdAuthenticationMethod connectPlugin, final FirebirdAuthenticationMethod expectedPlugin) {
        AuthorityRule rule = mock(AuthorityRule.class);
        ShardingSphereUser user = new ShardingSphereUser("root", "pwd", "");
        when(rule.findUser(any(Grantee.class))).thenReturn(Optional.of(user));
        when(rule.getAuthenticatorType(user)).thenReturn(rulePluginType);
        mockProxyContext(rule, true);
        FirebirdProtocol protocol = mock(FirebirdProtocol.class);
        when(protocol.getVersion()).thenReturn(FirebirdProtocolVersion.PROTOCOL_VERSION13);
        FirebirdPacketPayload payload = mockFirebirdPayload(FirebirdCommandPacketType.CONNECT);
        AuthenticationResult actual;
        try (MockedConstruction<FirebirdConnectPacket> ignored = mockConstruction(FirebirdConnectPacket.class, (connectPacket, construction) -> {
            when(connectPacket.getUserProtocols()).thenReturn(new ArrayList<>(Collections.singletonList(protocol)));
            when(connectPacket.getLogin()).thenReturn("root");
            when(connectPacket.getPlugin()).thenReturn(connectPlugin);
        })) {
            actual = authenticationEngine.authenticate(context, payload);
        }
        assertFalse(actual.isFinished());
        ArgumentCaptor<FirebirdAcceptPacket> acceptCaptor = ArgumentCaptor.forClass(FirebirdAcceptPacket.class);
        verify(context).writeAndFlush(acceptCaptor.capture());
        assertThat(acceptCaptor.getValue().getAcceptDataPacket().getPlugin(), is(expectedPlugin));
    }
    
    @SuppressWarnings("rawtypes")
    @SneakyThrows(ReflectiveOperationException.class)
    @ParameterizedTest(name = "{0}")
    @MethodSource("attachArguments")
    void assertAuthenticateAttach(final String name, final boolean containsDatabase, final ShardingSphereUser user, final String currentUsername,
                                  final String currentDatabase, final String encoding, final boolean expectException, final boolean expectAuthenticateCall,
                                  final boolean expectFinished, final String expectedDatabase, final String expectedUsername,
                                  final String encryptedPassword, final String attachAuthData) {
        AuthorityRule rule = mock(AuthorityRule.class);
        lenient().when(rule.findUser(any(Grantee.class))).thenReturn(Optional.ofNullable(user));
        mockProxyContext(rule, containsDatabase);
        Attribute<Charset> charsetAttr = mock(Attribute.class);
        when(context.channel().attr(CommonConstants.CHARSET_ATTRIBUTE_KEY)).thenReturn(charsetAttr);
        Plugins.getMemberAccessor().set(FirebirdAuthenticationEngine.class.getDeclaredField("currentAuthResult"), authenticationEngine,
                AuthenticationResultBuilder.continued(currentUsername, "", currentDatabase, Collections.emptyMap()));
        FirebirdSRPAuthenticationData authData = mock(FirebirdSRPAuthenticationData.class);
        Plugins.getMemberAccessor().set(FirebirdAuthenticationEngine.class.getDeclaredField("authData"), authenticationEngine, authData);
        FirebirdPacketPayload payload = mockFirebirdPayload(FirebirdCommandPacketType.ATTACH);
        FirebirdAuthenticator authenticator = mock(FirebirdAuthenticator.class);
        lenient().when(authenticator.authenticate(any(), any())).thenReturn(true);
        if (expectException) {
            try (MockedConstruction<FirebirdAttachPacket> ignored = mockConstruction(FirebirdAttachPacket.class, (attachPacket, construction) -> {
                when(attachPacket.getEncoding()).thenReturn(encoding);
                when(attachPacket.getEncPassword()).thenReturn(encryptedPassword);
                when(attachPacket.getAuthData()).thenReturn(attachAuthData);
            })) {
                assertThrows(UnknownDatabaseException.class, () -> authenticationEngine.authenticate(context, payload));
            }
            verify(charsetAttr).set(FirebirdCharacterSets.findCharacterSet(encoding));
            return;
        }
        try (MockedConstruction<FirebirdAttachPacket> ignored = mockConstruction(FirebirdAttachPacket.class, (attachPacket, construction) -> {
            when(attachPacket.getEncoding()).thenReturn(encoding);
            when(attachPacket.getEncPassword()).thenReturn(encryptedPassword);
            when(attachPacket.getAuthData()).thenReturn(attachAuthData);
        });
                MockedConstruction<AuthenticatorFactory> ignoredFactory = mockConstruction(AuthenticatorFactory.class,
                        (factory, construction) -> Optional.ofNullable(user).ifPresent(optional -> when(factory.newInstance(optional)).thenReturn(authenticator)))) {
            AuthenticationResult actual = authenticationEngine.authenticate(context, payload);
            assertThat(actual.isFinished(), is(expectFinished));
            assertThat(actual.getUsername(), is(expectedUsername));
            assertThat(actual.getDatabase(), is(expectedDatabase));
        }
        verify(context).writeAndFlush(isA(FirebirdGenericResponsePacket.class));
        verify(charsetAttr).set(FirebirdCharacterSets.findCharacterSet(encoding));
        if (expectAuthenticateCall && null != user) {
            ArgumentCaptor<Object[]> authInfoCaptor = ArgumentCaptor.forClass(Object[].class);
            verify(authenticator).authenticate(any(), authInfoCaptor.capture());
            Object[] actualAuthInfo = authInfoCaptor.getValue();
            assertThat(actualAuthInfo[0], is(encryptedPassword));
            assertThat(actualAuthInfo[1], is(authData));
            assertThat(actualAuthInfo[2], is(attachAuthData));
        }
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    @ParameterizedTest
    @CsvSource({"FOO_CHARSET", "OCTETS", "BINARY", "octets"})
    void assertAuthenticateAttachWithInvalidCharset(final String encoding) {
        AuthorityRule rule = mock(AuthorityRule.class);
        mockProxyContext(rule, true);
        Attribute<Charset> charsetAttr = mock(Attribute.class);
        when(context.channel().attr(CommonConstants.CHARSET_ATTRIBUTE_KEY)).thenReturn(charsetAttr);
        Plugins.getMemberAccessor().set(FirebirdAuthenticationEngine.class.getDeclaredField("currentAuthResult"), authenticationEngine,
                AuthenticationResultBuilder.continued("root", "", "db", Collections.emptyMap()));
        FirebirdPacketPayload payload = mockFirebirdPayload(FirebirdCommandPacketType.ATTACH);
        try (MockedConstruction<FirebirdAttachPacket> ignored = mockConstruction(FirebirdAttachPacket.class, (attachPacket, construction) -> when(attachPacket.getEncoding()).thenReturn(encoding))) {
            assertThrows(InvalidParameterValueException.class, () -> authenticationEngine.authenticate(context, payload));
        }
        verify(charsetAttr, never()).set(any());
    }
    
    @SuppressWarnings("rawtypes")
    @SneakyThrows(ReflectiveOperationException.class)
    @ParameterizedTest(name = "attachment charset {1}")
    @CsvSource(value = {"<default>,NONE", "UTF8,UTF8", "WIN1251,WIN1251", "UNICODE_FSS,UNICODE_FSS"}, nullValues = "<default>")
    void assertAuthenticateAttachWithCharset(final String encoding, final String expectedCharset) {
        AuthorityRule rule = mock(AuthorityRule.class);
        ShardingSphereUser user = new ShardingSphereUser("root", "pwd", "");
        when(rule.findUser(any(Grantee.class))).thenReturn(Optional.of(user));
        mockProxyContext(rule, true);
        Attribute<Charset> charsetAttr = mock(Attribute.class);
        when(context.channel().attr(CommonConstants.CHARSET_ATTRIBUTE_KEY)).thenReturn(charsetAttr);
        Plugins.getMemberAccessor().set(FirebirdAuthenticationEngine.class.getDeclaredField("currentAuthResult"), authenticationEngine,
                AuthenticationResultBuilder.continued("root", "", "db", Collections.emptyMap()));
        Plugins.getMemberAccessor().set(FirebirdAuthenticationEngine.class.getDeclaredField("selectedPlugin"), authenticationEngine, FirebirdAuthenticationMethod.LEGACY_AUTH);
        FirebirdPacketPayload payload = mockFirebirdPayload(FirebirdCommandPacketType.ATTACH);
        FirebirdAuthenticator authenticator = mock(FirebirdAuthenticator.class);
        when(authenticator.authenticate(any(), any())).thenReturn(true);
        try (
                MockedConstruction<FirebirdAttachPacket> ignored = mockConstruction(FirebirdAttachPacket.class, (attachPacket, construction) -> when(attachPacket.getEncoding()).thenReturn(encoding));
                MockedConstruction<AuthenticatorFactory> ignoredFactory = mockConstruction(AuthenticatorFactory.class,
                        (factory, construction) -> when(factory.newInstance(user)).thenReturn(authenticator))) {
            AuthenticationResult actual = authenticationEngine.authenticate(context, payload);
            assertTrue(actual.isFinished());
        }
        verify(charsetAttr).set(FirebirdCharacterSets.findCharacterSet(expectedCharset));
        verify(context.channel().attr(FirebirdConstant.ATTACHMENT_CHARSET)).set(expectedCharset);
    }
    
    @Test
    void assertAuthenticateConnectWithUnknownUser() {
        AuthorityRule rule = mock(AuthorityRule.class);
        when(rule.findUser(any(Grantee.class))).thenReturn(Optional.empty());
        mockProxyContext(rule, true);
        Attribute<Charset> charsetAttr = mock(Attribute.class);
        Attribute<FirebirdProtocolVersion> protocolVersionAttr = mock(Attribute.class);
        Attribute<Boolean> lazySendAttr = mock(Attribute.class);
        when(context.channel().attr(CommonConstants.CHARSET_ATTRIBUTE_KEY)).thenReturn(charsetAttr);
        when(context.channel().attr(FirebirdConstant.CONNECTION_PROTOCOL_VERSION)).thenReturn(protocolVersionAttr);
        when(context.channel().attr(FirebirdConstant.LAZY_SEND)).thenReturn(lazySendAttr);
        FirebirdProtocol protocol = mock(FirebirdProtocol.class);
        when(protocol.getVersion()).thenReturn(FirebirdProtocolVersion.PROTOCOL_VERSION10);
        List<FirebirdProtocol> userProtocols = new ArrayList<>(Collections.singletonList(protocol));
        FirebirdPacketPayload payload = mockFirebirdPayload(FirebirdCommandPacketType.CONNECT);
        try (MockedConstruction<FirebirdConnectPacket> ignored = mockConstruction(FirebirdConnectPacket.class, (mockConnect, construction) -> {
            when(mockConnect.getUserProtocols()).thenReturn(userProtocols);
            when(mockConnect.getLogin()).thenReturn("absent");
            when(mockConnect.getHost()).thenReturn("host");
        })) {
            assertThrows(AccessDeniedException.class, () -> authenticationEngine.authenticate(context, payload));
        }
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    @Test
    void assertAuthenticateAttachWithUnknownUser() {
        AuthorityRule rule = mock(AuthorityRule.class);
        when(rule.findUser(any(Grantee.class))).thenReturn(Optional.empty());
        mockProxyContext(rule, true);
        Attribute<Charset> charsetAttr = mock(Attribute.class);
        when(context.channel().attr(CommonConstants.CHARSET_ATTRIBUTE_KEY)).thenReturn(charsetAttr);
        Plugins.getMemberAccessor().set(FirebirdAuthenticationEngine.class.getDeclaredField("currentAuthResult"), authenticationEngine,
                AuthenticationResultBuilder.continued("absent", "", "db", Collections.emptyMap()));
        FirebirdPacketPayload payload = mockFirebirdPayload(FirebirdCommandPacketType.ATTACH);
        try (MockedConstruction<FirebirdAttachPacket> ignored = mockConstruction(FirebirdAttachPacket.class, (attachPacket, construction) -> when(attachPacket.getEncoding()).thenReturn("UTF8"))) {
            assertThrows(AccessDeniedException.class, () -> authenticationEngine.authenticate(context, payload));
        }
    }
    
    @SuppressWarnings("rawtypes")
    @SneakyThrows(ReflectiveOperationException.class)
    @Test
    void assertAuthenticateAttachWithWrongPassword() {
        AuthorityRule rule = mock(AuthorityRule.class);
        ShardingSphereUser user = new ShardingSphereUser("root", "pwd", "");
        when(rule.findUser(any(Grantee.class))).thenReturn(Optional.of(user));
        mockProxyContext(rule, true);
        Attribute<Charset> charsetAttr = mock(Attribute.class);
        when(context.channel().attr(CommonConstants.CHARSET_ATTRIBUTE_KEY)).thenReturn(charsetAttr);
        Plugins.getMemberAccessor().set(FirebirdAuthenticationEngine.class.getDeclaredField("currentAuthResult"), authenticationEngine,
                AuthenticationResultBuilder.continued("root", "", "db", Collections.emptyMap()));
        FirebirdSRPAuthenticationData authData = mock(FirebirdSRPAuthenticationData.class);
        Plugins.getMemberAccessor().set(FirebirdAuthenticationEngine.class.getDeclaredField("authData"), authenticationEngine, authData);
        FirebirdAuthenticator authenticator = mock(FirebirdAuthenticator.class);
        FirebirdPacketPayload payload = mockFirebirdPayload(FirebirdCommandPacketType.ATTACH);
        try (MockedConstruction<FirebirdAttachPacket> ignoredPacket = mockConstruction(FirebirdAttachPacket.class, (attachPacket, construction) -> {
            when(attachPacket.getEncoding()).thenReturn("UTF8");
            when(attachPacket.getEncPassword()).thenReturn("cipher_pwd");
            when(attachPacket.getAuthData()).thenReturn("client_auth");
        });
                MockedConstruction<AuthenticatorFactory> ignoredFactory = mockConstruction(AuthenticatorFactory.class,
                        (factory, construction) -> when(factory.newInstance(user)).thenReturn(authenticator))) {
            assertThrows(AccessDeniedException.class, () -> authenticationEngine.authenticate(context, payload));
        }
    }
    
    @SuppressWarnings("rawtypes")
    @Test
    void assertContinueAuthenticationAfterPluginSwitch() {
        AuthorityRule rule = mock(AuthorityRule.class);
        ShardingSphereUser user = new ShardingSphereUser("root", "pwd", "");
        when(rule.findUser(any(Grantee.class))).thenReturn(Optional.of(user));
        when(rule.getAuthenticatorType(user)).thenReturn("SRP256");
        mockProxyContext(rule, true);
        FirebirdProtocol protocol = mock(FirebirdProtocol.class);
        when(protocol.getVersion()).thenReturn(FirebirdProtocolVersion.PROTOCOL_VERSION13);
        when(protocol.getMaxType()).thenReturn(5);
        try (MockedConstruction<FirebirdConnectPacket> ignored = mockConstruction(FirebirdConnectPacket.class, (packet, construction) -> {
            when(packet.getUserProtocols()).thenReturn(new ArrayList<>(Collections.singletonList(protocol)));
            when(packet.getLogin()).thenReturn("root");
            when(packet.getHost()).thenReturn("host");
            when(packet.getDatabase()).thenReturn("db");
            when(packet.getPluginName()).thenReturn("Srp");
            when(packet.getUserInfoMap()).thenReturn(Collections.singletonMap(FirebirdUserDataType.CNCT_PLUGIN_LIST, "Srp,Srp256"));
        })) {
            assertFalse(authenticationEngine.authenticate(context, mockFirebirdPayload(FirebirdCommandPacketType.CONNECT)).isFinished());
        }
        byte[] salt = {1, 2};
        try (MockedConstruction<FirebirdAttachPacket> ignoredAttach = mockConstruction(FirebirdAttachPacket.class, (packet, construction) -> {
            when(packet.getEncoding()).thenReturn("UTF8");
            when(packet.getAuthData()).thenReturn("public-key");
        });
                MockedConstruction<FirebirdSRPAuthenticationData> ignoredData = mockConstruction(FirebirdSRPAuthenticationData.class, (data, construction) -> {
                    when(data.getSalt()).thenReturn(salt);
                    when(data.getPublicKeyHex()).thenReturn("ABCD");
                })) {
            assertFalse(authenticationEngine.authenticate(context, mockFirebirdPayload(FirebirdCommandPacketType.ATTACH)).isFinished());
        }
        FirebirdAuthenticator authenticator = mock(FirebirdAuthenticator.class);
        when(authenticator.authenticate(any(), any())).thenReturn(true);
        try (
                MockedConstruction<FirebirdContinueAuthPacket> ignoredPacket = mockConstruction(FirebirdContinueAuthPacket.class,
                        (packet, construction) -> when(packet.getData()).thenReturn("proof".getBytes(StandardCharsets.US_ASCII)));
                MockedConstruction<AuthenticatorFactory> ignoredFactory = mockConstruction(AuthenticatorFactory.class,
                        (factory, construction) -> when(factory.newInstance(user)).thenReturn(authenticator))) {
            assertTrue(authenticationEngine.authenticate(context, mockFirebirdPayload(FirebirdCommandPacketType.CONT_AUTH)).isFinished());
        }
        ArgumentCaptor<Object> messages = ArgumentCaptor.forClass(Object.class);
        verify(context, times(3)).writeAndFlush(messages.capture());
        FirebirdContinueAuthPacket actualChallenge = (FirebirdContinueAuthPacket) messages.getAllValues().get(1);
        byte[] expected = ByteBuffer.allocate(10).order(ByteOrder.LITTLE_ENDIAN).putShort((short) 2).put(salt).putShort((short) 4).put("ABCD".getBytes(StandardCharsets.US_ASCII)).array();
        assertThat(actualChallenge.getData(), is(expected));
        assertThat(actualChallenge.getPluginName(), is("Srp256"));
        ArgumentCaptor<Object[]> credentials = ArgumentCaptor.forClass(Object[].class);
        verify(authenticator).authenticate(any(), credentials.capture());
        assertThat(credentials.getValue()[2], is("proof"));
    }
    
    @Test
    void assertNoCommonAuthenticationPlugin() {
        AuthorityRule rule = mock(AuthorityRule.class);
        ShardingSphereUser user = new ShardingSphereUser("root", "pwd", "");
        when(rule.findUser(any(Grantee.class))).thenReturn(Optional.of(user));
        when(rule.getAuthenticatorType(user)).thenReturn("SRP256");
        mockProxyContext(rule, true);
        FirebirdProtocol protocol = mock(FirebirdProtocol.class);
        when(protocol.getVersion()).thenReturn(FirebirdProtocolVersion.PROTOCOL_VERSION13);
        when(protocol.getMaxType()).thenReturn(5);
        try (MockedConstruction<FirebirdConnectPacket> ignored = mockConstruction(FirebirdConnectPacket.class, (packet, construction) -> {
            when(packet.getUserProtocols()).thenReturn(new ArrayList<>(Collections.singletonList(protocol)));
            when(packet.getLogin()).thenReturn("root");
            when(packet.getHost()).thenReturn("host");
            when(packet.getPluginName()).thenReturn("Srp");
            when(packet.getUserInfoMap()).thenReturn(Collections.singletonMap(FirebirdUserDataType.CNCT_PLUGIN_LIST, "Srp"));
        })) {
            SQLException actual = assertThrows(SQLException.class, () -> authenticationEngine.authenticate(context, mockFirebirdPayload(FirebirdCommandPacketType.CONNECT)));
            assertThat(actual.getErrorCode(), is(ISCConstants.isc_login_error));
        }
    }
    
    private FirebirdPacketPayload mockFirebirdPayload(final FirebirdCommandPacketType commandPacketType) {
        FirebirdPacketPayload result = mock(FirebirdPacketPayload.class, RETURNS_DEEP_STUBS);
        when(result.readInt4()).thenReturn(commandPacketType.getValue());
        return result;
    }
    
    private void mockProxyContext(final AuthorityRule rule, final boolean containsDatabase) {
        ShardingSphereMetaData metaData = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData();
        when(metaData.getGlobalRuleMetaData()).thenReturn(new RuleMetaData(Collections.singleton(rule)));
        lenient().when(metaData.containsDatabase(anyString())).thenReturn(containsDatabase);
    }
    
    private static Stream<Arguments> connectArguments() {
        return Stream.of(
                Arguments.of("defaultSrp256Plugin", "", FirebirdAuthenticationMethod.SRP256, FirebirdAuthenticationMethod.SRP256, true, true),
                Arguments.of("srpPlugin", "SRP512", FirebirdAuthenticationMethod.SRP512, FirebirdAuthenticationMethod.SRP512, true, true),
                Arguments.of("legacyPluginSwitch", "LEGACY_AUTH", FirebirdAuthenticationMethod.SRP, FirebirdAuthenticationMethod.LEGACY_AUTH, true, false),
                Arguments.of("legacyPlugin", "LEGACY_AUTH", FirebirdAuthenticationMethod.LEGACY_AUTH, FirebirdAuthenticationMethod.LEGACY_AUTH, false, false));
    }
    
    private static Stream<Arguments> mismatchedPluginArguments() {
        return Stream.of(
                Arguments.of("defaultSrp256", "", FirebirdAuthenticationMethod.SRP, FirebirdAuthenticationMethod.SRP256),
                Arguments.of("configuredSrp256", "SRP256", FirebirdAuthenticationMethod.SRP, FirebirdAuthenticationMethod.SRP256),
                Arguments.of("configuredSrp512", "SRP512", FirebirdAuthenticationMethod.SRP256, FirebirdAuthenticationMethod.SRP512));
    }
    
    private static Stream<Arguments> attachArguments() {
        return Stream.of(
                Arguments.of("attachWithUser", true, new ShardingSphereUser("root", "pwd", ""), "root", "db", "UTF8", false, true, true, "db", "root", "cipher_pwd", "client_auth"),
                Arguments.of("attachUnknownDatabase", false, new ShardingSphereUser("root", "pwd", ""), "root", "missing_db", "UTF8", true, false, false, "", "root", null, null),
                Arguments.of("attachEmptyDatabase", false, new ShardingSphereUser("root", "pwd", ""), "root", "", "NONE", false, true, true, "", "root", "cipher_pwd", "client_auth"));
    }
}
