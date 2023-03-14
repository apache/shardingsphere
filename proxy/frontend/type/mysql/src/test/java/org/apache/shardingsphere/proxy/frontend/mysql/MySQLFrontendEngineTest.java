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

package org.apache.shardingsphere.proxy.frontend.mysql;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import lombok.SneakyThrows;
import org.apache.shardingsphere.authority.config.AuthorityRuleConfiguration;
import org.apache.shardingsphere.authority.rule.AuthorityRule;
import org.apache.shardingsphere.authority.rule.builder.AuthorityRuleBuilder;
import org.apache.shardingsphere.db.protocol.mysql.constant.MySQLConnectionPhase;
import org.apache.shardingsphere.db.protocol.mysql.constant.MySQLConstants;
import org.apache.shardingsphere.db.protocol.mysql.packet.generic.MySQLErrPacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.generic.MySQLOKPacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.handshake.MySQLHandshakePacket;
import org.apache.shardingsphere.db.protocol.mysql.payload.MySQLPacketPayload;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUser;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.frontend.authentication.AuthenticationResult;
import org.apache.shardingsphere.proxy.frontend.connection.ConnectionIdGenerator;
import org.apache.shardingsphere.proxy.frontend.mysql.authentication.MySQLAuthenticationEngine;
import org.apache.shardingsphere.test.mock.AutoMockExtension;
import org.apache.shardingsphere.test.mock.StaticMockSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.internal.configuration.plugins.Plugins;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ProxyContext.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public final class MySQLFrontendEngineTest {
    
    private static final String SCHEMA_PATTERN = "schema_%s";
    
    private MySQLFrontendEngine engine;
    
    @Mock
    private ChannelHandlerContext context;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Channel channel;
    
    @Mock
    private MySQLPacketPayload payload;
    
    @BeforeEach
    public void setUp() throws ReflectiveOperationException {
        engine = new MySQLFrontendEngine();
        Plugins.getMemberAccessor().set(ConnectionIdGenerator.class.getDeclaredField("currentId"), ConnectionIdGenerator.getInstance(), 0);
        when(context.channel()).thenReturn(channel);
    }
    
    @Test
    public void assertInitChannel() {
        engine.initChannel(channel);
        verify(channel.attr(MySQLConstants.MYSQL_SEQUENCE_ID)).set(any(AtomicInteger.class));
    }
    
    @Test
    public void assertHandshake() {
        assertTrue(engine.getAuthenticationEngine().handshake(context) > 0);
        verify(context).writeAndFlush(isA(MySQLHandshakePacket.class));
    }
    
    @Test
    public void assertAuthWhenLoginSuccess() {
        setConnectionPhase(MySQLConnectionPhase.AUTH_PHASE_FAST_PATH);
        ContextManager contextManager = mockContextManager(new ShardingSphereUser("root", "", ""));
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        when(payload.readInt1()).thenReturn(1);
        when(payload.readStringNul()).thenReturn("root");
        when(payload.readStringNulByBytes()).thenReturn("root".getBytes());
        when(channel.remoteAddress()).thenReturn(new InetSocketAddress("localhost", 3307));
        AuthenticationResult actual = engine.getAuthenticationEngine().authenticate(context, payload);
        assertThat(actual.getUsername(), is("root"));
        assertNull(actual.getDatabase());
        assertTrue(actual.isFinished());
        verify(context).writeAndFlush(isA(MySQLOKPacket.class));
    }
    
    @Test
    public void assertAuthWhenLoginFailure() {
        setConnectionPhase(MySQLConnectionPhase.AUTH_PHASE_FAST_PATH);
        ContextManager contextManager = mockContextManager(new ShardingSphereUser("root", "error", ""));
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        when(payload.readInt1()).thenReturn(1);
        when(payload.readStringNul()).thenReturn("root");
        when(payload.readStringNulByBytes()).thenReturn("root".getBytes());
        when(channel.remoteAddress()).thenReturn(new InetSocketAddress("localhost", 3307));
        AuthenticationResult actual = engine.getAuthenticationEngine().authenticate(context, payload);
        assertNull(actual.getUsername());
        assertNull(actual.getDatabase());
        assertFalse(actual.isFinished());
        verify(context).writeAndFlush(isA(MySQLErrPacket.class));
    }
    
    @Test
    public void assertErrorMsgWhenLoginFailure() throws UnknownHostException {
        setConnectionPhase(MySQLConnectionPhase.AUTH_PHASE_FAST_PATH);
        ContextManager contextManager = mockContextManager(new ShardingSphereUser("root", "error", ""));
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        when(payload.readInt1()).thenReturn(1);
        when(payload.readStringNul()).thenReturn("root");
        when(payload.readStringNulByBytes()).thenReturn("root".getBytes());
        when(channel.remoteAddress()).thenReturn(new InetSocketAddress(InetAddress.getByAddress(new byte[]{(byte) 192, (byte) 168, (byte) 0, (byte) 102}), 3307));
        AuthenticationResult actual = engine.getAuthenticationEngine().authenticate(context, payload);
        assertNull(actual.getUsername());
        assertNull(actual.getDatabase());
        assertFalse(actual.isFinished());
        verify(context).writeAndFlush(argThat((ArgumentMatcher<MySQLErrPacket>) argument -> "Access denied for user 'root'@'192.168.0.102' (using password: YES)".equals(argument.getErrorMessage())));
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private void setConnectionPhase(final MySQLConnectionPhase connectionPhase) {
        Plugins.getMemberAccessor().set(MySQLAuthenticationEngine.class.getDeclaredField("connectionPhase"), engine.getAuthenticationEngine(), connectionPhase);
    }
    
    private ContextManager mockContextManager(final ShardingSphereUser user) {
        ContextManager result = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        MetaDataContexts metaDataContexts = new MetaDataContexts(
                mock(MetaDataPersistService.class), new ShardingSphereMetaData(createDatabases(), createGlobalRuleMetaData(user), new ConfigurationProperties(new Properties())));
        when(result.getMetaDataContexts()).thenReturn(metaDataContexts);
        return result;
    }
    
    private Map<String, ShardingSphereDatabase> createDatabases() {
        Map<String, ShardingSphereDatabase> result = new HashMap<>(10, 1);
        for (int i = 0; i < 10; i++) {
            ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
            when(database.getProtocolType()).thenReturn(new MySQLDatabaseType());
            result.put(String.format(SCHEMA_PATTERN, i), database);
        }
        return result;
    }
    
    private ShardingSphereRuleMetaData createGlobalRuleMetaData(final ShardingSphereUser user) {
        AuthorityRuleConfiguration ruleConfig = new AuthorityRuleConfiguration(Collections.singletonList(user), new AlgorithmConfiguration("ALL_PERMITTED", new Properties()), null);
        AuthorityRule rule = new AuthorityRuleBuilder().build(ruleConfig, Collections.emptyMap(), mock(ConfigurationProperties.class));
        return new ShardingSphereRuleMetaData(Collections.singleton(rule));
    }
}
