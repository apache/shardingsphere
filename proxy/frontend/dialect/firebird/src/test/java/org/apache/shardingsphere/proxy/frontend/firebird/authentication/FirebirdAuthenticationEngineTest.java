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
import org.apache.shardingsphere.database.exception.core.exception.syntax.database.UnknownDatabaseException;
import org.apache.shardingsphere.database.protocol.constant.CommonConstants;
import org.apache.shardingsphere.database.protocol.firebird.constant.FirebirdAuthenticationMethod;
import org.apache.shardingsphere.database.protocol.firebird.constant.FirebirdConstant;
import org.apache.shardingsphere.database.protocol.firebird.constant.protocol.FirebirdProtocol;
import org.apache.shardingsphere.database.protocol.firebird.constant.protocol.FirebirdProtocolVersion;
import org.apache.shardingsphere.database.protocol.firebird.exception.FirebirdProtocolException;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.FirebirdCommandPacketType;
import org.apache.shardingsphere.database.protocol.firebird.packet.generic.FirebirdGenericResponsePacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.handshake.FirebirdAcceptPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.handshake.FirebirdAttachPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.handshake.FirebirdConnectPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.handshake.FirebirdSRPAuthenticationData;
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.user.Grantee;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUser;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.firebird.handler.admin.executor.variable.charset.FirebirdCharacterSets;
import org.apache.shardingsphere.proxy.frontend.connection.ConnectionIdGenerator;
import org.apache.shardingsphere.proxy.frontend.firebird.authentication.authenticator.FirebirdAuthenticator;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.blob.FirebirdBlobIdGenerator;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.blob.upload.FirebirdBlobUploadCache;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.statement.FirebirdStatementIdGenerator;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.statement.fetch.FirebirdFetchStatementCache;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.transaction.FirebirdTransactionIdGenerator;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.internal.configuration.plugins.Plugins;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.isA;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings({
        ConnectionIdGenerator.class, FirebirdTransactionIdGenerator.class, FirebirdStatementIdGenerator.class, FirebirdFetchStatementCache.class,
        FirebirdBlobIdGenerator.class, FirebirdBlobUploadCache.class, ProxyContext.class
})
class FirebirdAuthenticationEngineTest {
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ChannelHandlerContext context;
    
    @Mock
    private ConnectionIdGenerator idGenerator;
    
    @Mock
    private FirebirdTransactionIdGenerator transactionIdGenerator;
    
    @Mock
    private FirebirdStatementIdGenerator statementIdGenerator;
    
    @Mock
    private FirebirdFetchStatementCache fetchStatementCache;
    
    @Mock
    private FirebirdBlobIdGenerator blobIdGenerator;
    
    @Mock
    private FirebirdBlobUploadCache blobUploadCache;
    
    @Mock
    private ProxyContext proxyContext;
    
    private final FirebirdAuthenticationEngine authenticationEngine = new FirebirdAuthenticationEngine();
    
    @Test
    void assertHandshake() {
        when(ConnectionIdGenerator.getInstance()).thenReturn(idGenerator);
        when(idGenerator.nextId()).thenReturn(1);
        when(FirebirdTransactionIdGenerator.getInstance()).thenReturn(transactionIdGenerator);
        when(FirebirdStatementIdGenerator.getInstance()).thenReturn(statementIdGenerator);
        when(FirebirdFetchStatementCache.getInstance()).thenReturn(fetchStatementCache);
        when(FirebirdBlobIdGenerator.getInstance()).thenReturn(blobIdGenerator);
        when(FirebirdBlobUploadCache.getInstance()).thenReturn(blobUploadCache);
        assertThat(authenticationEngine.handshake(context), is(1));
        verify(transactionIdGenerator).registerConnection(1);
        verify(statementIdGenerator).registerConnection(1);
        verify(fetchStatementCache).registerConnection(1);
        verify(blobIdGenerator).registerConnection(1);
        verify(blobUploadCache).registerConnection(1);
    }
    
    @Test
    void assertAuthenticateWithUnsupportedCommand() {
        AuthorityRule rule = mock(AuthorityRule.class);
        mockProxyContext(rule, true);
        FirebirdPacketPayload payload = mockFirebirdPayload(FirebirdCommandPacketType.CONT_AUTH);
        assertThrows(FirebirdProtocolException.class, () -> authenticationEngine.authenticate(context, payload));
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
        when(context.channel().attr(CommonConstants.CHARSET_ATTRIBUTE_KEY)).thenReturn(charsetAttr);
        when(context.channel().attr(FirebirdConstant.CONNECTION_PROTOCOL_VERSION)).thenReturn(protocolVersionAttr);
        FirebirdPacketPayload payload = mockFirebirdPayload(FirebirdCommandPacketType.CONNECT);
        FirebirdProtocol protocol = mock(FirebirdProtocol.class);
        when(protocol.getVersion()).thenReturn(FirebirdProtocolVersion.PROTOCOL_VERSION10);
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
                        when(mockConnect.getPlugin()).thenReturn(connectPlugin);
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
                when(mockConnect.getPlugin()).thenReturn(connectPlugin);
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
                AuthenticationResultBuilder.continued(currentUsername, "", currentDatabase));
        FirebirdSRPAuthenticationData authData = mock(FirebirdSRPAuthenticationData.class);
        Plugins.getMemberAccessor().set(FirebirdAuthenticationEngine.class.getDeclaredField("authData"), authenticationEngine, authData);
        FirebirdPacketPayload payload = mockFirebirdPayload(FirebirdCommandPacketType.ATTACH);
        FirebirdAuthenticator authenticator = mock(FirebirdAuthenticator.class);
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
    
    private FirebirdPacketPayload mockFirebirdPayload(final FirebirdCommandPacketType commandPacketType) {
        FirebirdPacketPayload result = mock(FirebirdPacketPayload.class, RETURNS_DEEP_STUBS);
        when(result.readInt4()).thenReturn(commandPacketType.getValue());
        return result;
    }
    
    private void mockProxyContext(final AuthorityRule rule, final boolean containsDatabase) {
        ContextManager contextManager = mock(ContextManager.class);
        MetaDataContexts metaDataContexts = mock(MetaDataContexts.class);
        ShardingSphereMetaData metaData = mock(ShardingSphereMetaData.class);
        when(metaData.getGlobalRuleMetaData()).thenReturn(new RuleMetaData(Collections.singleton(rule)));
        lenient().when(metaData.containsDatabase(anyString())).thenReturn(containsDatabase);
        when(metaDataContexts.getMetaData()).thenReturn(metaData);
        when(contextManager.getMetaDataContexts()).thenReturn(metaDataContexts);
        when(ProxyContext.getInstance()).thenReturn(proxyContext);
        when(proxyContext.getContextManager()).thenReturn(contextManager);
    }
    
    private static Stream<Arguments> connectArguments() {
        return Stream.of(
                Arguments.of("pluginMismatch", "", FirebirdAuthenticationMethod.SRP, FirebirdAuthenticationMethod.SRP256, true, false),
                Arguments.of("srpPlugin", "SRP512", FirebirdAuthenticationMethod.SRP512, FirebirdAuthenticationMethod.SRP512, true, true),
                Arguments.of("legacyPlugin", "LEGACY_AUTH", FirebirdAuthenticationMethod.LEGACY_AUTH, FirebirdAuthenticationMethod.LEGACY_AUTH, false, false));
    }
    
    private static Stream<Arguments> attachArguments() {
        return Stream.of(
                Arguments.of("attachWithUser", true, new ShardingSphereUser("root", "pwd", ""), "root", "db", "UTF8", false, true, true, "db", "root", "cipher_pwd", "client_auth"),
                Arguments.of("attachWithoutUser", true, null, "absent", "db", "NONE", false, false, true, "db", "absent", null, null),
                Arguments.of("attachUnknownDatabase", false, new ShardingSphereUser("root", "pwd", ""), "root", "missing_db", "UTF8", true, false, false, "", "root", null, null),
                Arguments.of("attachEmptyDatabase", false, null, "root", "", "NONE", false, false, true, "", "root", null, null));
    }
}
