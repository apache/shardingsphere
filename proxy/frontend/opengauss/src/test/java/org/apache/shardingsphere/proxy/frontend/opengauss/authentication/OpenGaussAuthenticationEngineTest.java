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
import io.netty.util.Attribute;
import lombok.SneakyThrows;
import org.apache.shardingsphere.authority.config.AuthorityRuleConfiguration;
import org.apache.shardingsphere.authority.rule.AuthorityRule;
import org.apache.shardingsphere.authority.rule.builder.AuthorityRuleBuilder;
import org.apache.shardingsphere.db.protocol.CommonConstants;
import org.apache.shardingsphere.db.protocol.opengauss.packet.authentication.OpenGaussAuthenticationSCRAMSha256Packet;
import org.apache.shardingsphere.db.protocol.payload.PacketPayload;
import org.apache.shardingsphere.db.protocol.postgresql.payload.PostgreSQLPacketPayload;
import org.apache.shardingsphere.dialect.postgresql.exception.authority.EmptyUsernameException;
import org.apache.shardingsphere.dialect.postgresql.exception.authority.InvalidPasswordException;
import org.apache.shardingsphere.dialect.postgresql.exception.protocol.ProtocolViolationException;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.data.ShardingSphereData;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUser;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.frontend.authentication.AuthenticationResult;
import org.apache.shardingsphere.proxy.frontend.opengauss.ProxyContextRestorer;
import org.apache.shardingsphere.proxy.frontend.opengauss.authentication.fixture.OpenGaussAuthenticationAlgorithm;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class OpenGaussAuthenticationEngineTest extends ProxyContextRestorer {
    
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
        AuthenticationResult actual = new OpenGaussAuthenticationEngine().authenticate(mock(ChannelHandlerContext.class), payload);
        assertFalse(actual.isFinished());
    }
    
    @Test(expected = EmptyUsernameException.class)
    public void assertUserNotSet() {
        PostgreSQLPacketPayload payload = new PostgreSQLPacketPayload(createByteBuf(8, 512), StandardCharsets.UTF_8);
        payload.writeInt4(64);
        payload.writeInt4(196608);
        payload.writeStringNul("client_encoding");
        payload.writeStringNul("UTF8");
        new OpenGaussAuthenticationEngine().authenticate(channelHandlerContext, payload);
    }
    
    @Test(expected = ProtocolViolationException.class)
    public void assertAuthenticateWithNonPasswordMessage() {
        OpenGaussAuthenticationEngine authenticationEngine = new OpenGaussAuthenticationEngine();
        setAlreadyReceivedStartupMessage(authenticationEngine);
        PostgreSQLPacketPayload payload = new PostgreSQLPacketPayload(createByteBuf(8, 16), StandardCharsets.UTF_8);
        payload.writeInt1('F');
        payload.writeInt8(0);
        authenticationEngine.authenticate(channelHandlerContext, payload);
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private void setAlreadyReceivedStartupMessage(final OpenGaussAuthenticationEngine target) {
        Field field = OpenGaussAuthenticationEngine.class.getDeclaredField("startupMessageReceived");
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
    
    private void assertLogin(final String inputPassword) {
        PostgreSQLPacketPayload payload = new PostgreSQLPacketPayload(createByteBuf(16, 128), StandardCharsets.UTF_8);
        payload.writeInt4(64);
        payload.writeInt4(196608);
        payload.writeStringNul("user");
        payload.writeStringNul(username);
        payload.writeStringNul("client_encoding");
        payload.writeStringNul("UTF8");
        OpenGaussAuthenticationEngine engine = new OpenGaussAuthenticationEngine();
        AuthenticationResult actual = engine.authenticate(channelHandlerContext, payload);
        assertFalse(actual.isFinished());
        assertThat(actual.getUsername(), is(username));
        ArgumentCaptor<OpenGaussAuthenticationSCRAMSha256Packet> argumentCaptor = ArgumentCaptor.forClass(OpenGaussAuthenticationSCRAMSha256Packet.class);
        verify(channelHandlerContext).writeAndFlush(argumentCaptor.capture());
        OpenGaussAuthenticationSCRAMSha256Packet sha256Packet = argumentCaptor.getValue();
        String random64Code = new String(getRandom64Code(sha256Packet));
        String token = new String(getToken(sha256Packet));
        int serverIteration = getServerIteration(sha256Packet);
        payload = new PostgreSQLPacketPayload(createByteBuf(16, 128), StandardCharsets.UTF_8);
        String clientDigest = encodeDigest(inputPassword, random64Code, token, serverIteration);
        payload.writeInt1('p');
        payload.writeInt4(4 + clientDigest.length() + 1);
        payload.writeStringNul(clientDigest);
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
                new ShardingSphereMetaData(new LinkedHashMap<>(), buildGlobalRuleMetaData(user), new ConfigurationProperties(new Properties())), new ShardingSphereData());
    }
    
    private ShardingSphereRuleMetaData buildGlobalRuleMetaData(final ShardingSphereUser user) {
        AuthorityRuleConfiguration ruleConfig = new AuthorityRuleConfiguration(Collections.singletonList(user), new AlgorithmConfiguration("ALL_PERMITTED", new Properties()));
        AuthorityRule rule = new AuthorityRuleBuilder().build(ruleConfig, Collections.emptyMap(), mock(InstanceContext.class), mock(ConfigurationProperties.class));
        return new ShardingSphereRuleMetaData(Collections.singletonList(rule));
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private byte[] getRandom64Code(final OpenGaussAuthenticationSCRAMSha256Packet packet) {
        Field field = OpenGaussAuthenticationSCRAMSha256Packet.class.getDeclaredField("random64Code");
        field.setAccessible(true);
        return (byte[]) field.get(packet);
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private byte[] getToken(final OpenGaussAuthenticationSCRAMSha256Packet packet) {
        Field field = OpenGaussAuthenticationSCRAMSha256Packet.class.getDeclaredField("token");
        field.setAccessible(true);
        return (byte[]) field.get(packet);
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private int getServerIteration(final OpenGaussAuthenticationSCRAMSha256Packet packet) {
        Field field = OpenGaussAuthenticationSCRAMSha256Packet.class.getDeclaredField("serverIteration");
        field.setAccessible(true);
        return (int) field.get(packet);
    }
    
    private String encodeDigest(final String password, final String random64code, final String token, final int serverIteration) {
        return new String(OpenGaussAuthenticationAlgorithm.doRFC5802Algorithm(password, random64code, token, serverIteration));
    }
}
