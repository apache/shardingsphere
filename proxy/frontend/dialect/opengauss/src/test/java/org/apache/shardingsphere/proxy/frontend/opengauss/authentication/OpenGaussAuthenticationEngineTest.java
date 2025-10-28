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
import org.apache.shardingsphere.authentication.result.AuthenticationResult;
import org.apache.shardingsphere.authority.config.AuthorityRuleConfiguration;
import org.apache.shardingsphere.authority.config.UserConfiguration;
import org.apache.shardingsphere.authority.rule.builder.AuthorityRuleBuilder;
import org.apache.shardingsphere.database.exception.postgresql.exception.authority.EmptyUsernameException;
import org.apache.shardingsphere.database.exception.postgresql.exception.protocol.ProtocolViolationException;
import org.apache.shardingsphere.database.protocol.constant.CommonConstants;
import org.apache.shardingsphere.database.protocol.payload.PacketPayload;
import org.apache.shardingsphere.database.protocol.postgresql.packet.handshake.PostgreSQLSSLUnwillingPacket;
import org.apache.shardingsphere.database.protocol.postgresql.packet.handshake.PostgreSQLSSLWillingPacket;
import org.apache.shardingsphere.database.protocol.postgresql.payload.PostgreSQLPacketPayload;
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereStatistics;
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

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Properties;

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
class OpenGaussAuthenticationEngineTest {
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ChannelHandlerContext channelHandlerContext;
    
    @SuppressWarnings("unchecked")
    @BeforeEach
    void setup() {
        when(channelHandlerContext.channel().attr(CommonConstants.CHARSET_ATTRIBUTE_KEY)).thenReturn(mock(Attribute.class));
    }
    
    @Test
    void assertSSLUnwilling() {
        ByteBuf byteBuf = createByteBuf(8, 8);
        byteBuf.writeInt(8);
        byteBuf.writeInt(80877103);
        PacketPayload payload = new PostgreSQLPacketPayload(byteBuf, StandardCharsets.UTF_8);
        ChannelHandlerContext context = mock(ChannelHandlerContext.class);
        AuthenticationResult actual = new OpenGaussAuthenticationEngine().authenticate(context, payload);
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
        AuthenticationResult actual = new OpenGaussAuthenticationEngine().authenticate(context, payload);
        verify(context).writeAndFlush(any(PostgreSQLSSLWillingPacket.class));
        verify(context.pipeline()).addFirst(eq(SslHandler.class.getSimpleName()), any(SslHandler.class));
        assertFalse(actual.isFinished());
    }
    
    @Test
    void assertUserNotSet() {
        PostgreSQLPacketPayload payload = new PostgreSQLPacketPayload(createByteBuf(8, 512), StandardCharsets.UTF_8);
        payload.writeInt4(64);
        payload.writeInt4(196608);
        payload.writeStringNul("client_encoding");
        payload.writeStringNul("UTF8");
        ContextManager contextManager = mockContextManager();
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        assertThrows(EmptyUsernameException.class, () -> new OpenGaussAuthenticationEngine().authenticate(channelHandlerContext, payload));
    }
    
    @Test
    void assertAuthenticateWithNonPasswordMessage() {
        OpenGaussAuthenticationEngine authenticationEngine = new OpenGaussAuthenticationEngine();
        setAlreadyReceivedStartupMessage(authenticationEngine);
        PostgreSQLPacketPayload payload = new PostgreSQLPacketPayload(createByteBuf(8, 16), StandardCharsets.UTF_8);
        payload.writeInt1('F');
        payload.writeInt8(0L);
        ContextManager contextManager = mockContextManager();
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        assertThrows(ProtocolViolationException.class, () -> authenticationEngine.authenticate(channelHandlerContext, payload));
    }
    
    private ContextManager mockContextManager() {
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(Collections.emptyList(), mock(ResourceMetaData.class),
                buildGlobalRuleMetaData(new UserConfiguration("root", "sharding", "", null, false)), mock(ConfigurationProperties.class));
        MetaDataContexts metaDataContexts = new MetaDataContexts(metaData, new ShardingSphereStatistics());
        ContextManager result = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(result.getMetaDataContexts()).thenReturn(metaDataContexts);
        return result;
    }
    
    private RuleMetaData buildGlobalRuleMetaData(final UserConfiguration userConfig) {
        AuthorityRuleConfiguration ruleConfig = new AuthorityRuleConfiguration(
                Collections.singleton(userConfig), new AlgorithmConfiguration("ALL_PERMITTED", new Properties()), Collections.emptyMap(), null);
        return new RuleMetaData(Collections.singleton(new AuthorityRuleBuilder().build(ruleConfig, Collections.emptyList(), mock(ConfigurationProperties.class))));
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private void setAlreadyReceivedStartupMessage(final OpenGaussAuthenticationEngine target) {
        Plugins.getMemberAccessor().set(OpenGaussAuthenticationEngine.class.getDeclaredField("startupMessageReceived"), target, true);
    }
    
    private ByteBuf createByteBuf(final int initialCapacity, final int maxCapacity) {
        return new UnpooledHeapByteBuf(UnpooledByteBufAllocator.DEFAULT, initialCapacity, maxCapacity);
    }
}
