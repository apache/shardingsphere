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
import lombok.SneakyThrows;
import org.apache.shardingsphere.authority.config.AuthorityRuleConfiguration;
import org.apache.shardingsphere.authority.rule.AuthorityRule;
import org.apache.shardingsphere.authority.rule.builder.AuthorityRuleBuilder;
import org.apache.shardingsphere.db.protocol.postgresql.constant.PostgreSQLAuthenticationMethod;
import org.apache.shardingsphere.db.protocol.postgresql.packet.handshake.PostgreSQLPasswordMessagePacket;
import org.apache.shardingsphere.db.protocol.postgresql.payload.PostgreSQLPacketPayload;
import org.apache.shardingsphere.dialect.postgresql.exception.authority.InvalidPasswordException;
import org.apache.shardingsphere.dialect.postgresql.exception.authority.UnknownUsernameException;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.DefaultDatabase;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ShardingSphereResource;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUser;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.frontend.postgresql.ProxyContextRestorer;
import org.apache.shardingsphere.proxy.frontend.postgresql.authentication.authenticator.PostgreSQLAuthenticator;
import org.apache.shardingsphere.proxy.frontend.postgresql.authentication.authenticator.PostgreSQLMD5PasswordAuthenticator;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class PostgreSQLAuthenticationHandlerTest extends ProxyContextRestorer {
    
    private static final String SCHEMA_PATTERN = "schema_%s";
    
    private final String username = "postgres";
    
    private final String password = "sharding";
    
    private final String database = "schema_0";
    
    private final String md5Salt = "md5test";
    
    private PostgreSQLPasswordMessagePacket passwordMessagePacket;
    
    @Before
    public void init() {
        PostgreSQLPacketPayload payload = new PostgreSQLPacketPayload(createByteBuf(16, 128), StandardCharsets.UTF_8);
        String md5Digest = md5Encode(username, password, md5Salt.getBytes(StandardCharsets.UTF_8));
        payload.writeInt4(4 + md5Digest.length() + 1);
        payload.writeStringNul(md5Digest);
        passwordMessagePacket = new PostgreSQLPasswordMessagePacket(payload);
    }
    
    @Test
    public void assertLoginSuccess() {
        initProxyContext(new ShardingSphereUser(username, password, "%"));
        new PostgreSQLAuthenticationHandler().login(username, database, md5Salt.getBytes(StandardCharsets.UTF_8), passwordMessagePacket);
    }
    
    @Test(expected = UnknownUsernameException.class)
    public void assertLoginWithAbsentUser() {
        initProxyContext(new ShardingSphereUser("username", password, "%"));
        new PostgreSQLAuthenticationHandler().login(username, database, md5Salt.getBytes(StandardCharsets.UTF_8), passwordMessagePacket);
    }
    
    @Test(expected = InvalidPasswordException.class)
    public void assertLoginWithIncorrectPassword() {
        initProxyContext(new ShardingSphereUser(username, "password", "%"));
        new PostgreSQLAuthenticationHandler().login(username, database, md5Salt.getBytes(StandardCharsets.UTF_8), passwordMessagePacket);
    }
    
    @Test(expected = InvalidPasswordException.class)
    public void assertLoginWithoutPassword() {
        initProxyContext(new ShardingSphereUser(username, null, "%"));
        new PostgreSQLAuthenticationHandler().login(username, database, md5Salt.getBytes(StandardCharsets.UTF_8), passwordMessagePacket);
    }
    
    @Test
    public void assertGetAuthenticator() {
        PostgreSQLAuthenticator authenticator = new PostgreSQLAuthenticationHandler().getAuthenticator(username, "");
        assertThat(authenticator, instanceOf(PostgreSQLMD5PasswordAuthenticator.class));
        assertThat(authenticator.getAuthenticationMethodName(), is(PostgreSQLAuthenticationMethod.MD5.getMethodName()));
    }
    
    private void initProxyContext(final ShardingSphereUser user) {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        MetaDataContexts metaDataContexts = getMetaDataContexts(user);
        when(contextManager.getMetaDataContexts()).thenReturn(metaDataContexts);
        ProxyContext.init(contextManager);
    }
    
    private MetaDataContexts getMetaDataContexts(final ShardingSphereUser user) {
        return new MetaDataContexts(mock(MetaDataPersistService.class),
                new ShardingSphereMetaData(getDatabases(), buildGlobalRuleMetaData(user), new ConfigurationProperties(new Properties())));
    }
    
    private ByteBuf createByteBuf(final int initialCapacity, final int maxCapacity) {
        return new UnpooledHeapByteBuf(UnpooledByteBufAllocator.DEFAULT, initialCapacity, maxCapacity);
    }
    
    private Map<String, ShardingSphereDatabase> getDatabases() {
        Map<String, ShardingSphereDatabase> result = new HashMap<>(10, 1);
        for (int i = 0; i < 10; i++) {
            ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
            ShardingSphereSchema schema = mock(ShardingSphereSchema.class);
            when(database.getResource()).thenReturn(new ShardingSphereResource(Collections.emptyMap()));
            when(database.getRuleMetaData()).thenReturn(new ShardingSphereRuleMetaData(Collections.emptyList()));
            when(database.getSchema(DefaultDatabase.LOGIC_NAME)).thenReturn(schema);
            when(schema.getTables()).thenReturn(Collections.emptyMap());
            result.put(String.format(SCHEMA_PATTERN, i), database);
        }
        return result;
    }
    
    private ShardingSphereRuleMetaData buildGlobalRuleMetaData(final ShardingSphereUser user) {
        AuthorityRuleConfiguration ruleConfig = new AuthorityRuleConfiguration(Collections.singletonList(user), new AlgorithmConfiguration("ALL_PERMITTED", new Properties()));
        AuthorityRule rule = new AuthorityRuleBuilder().build(ruleConfig, Collections.emptyMap(), mock(InstanceContext.class), mock(ConfigurationProperties.class));
        return new ShardingSphereRuleMetaData(Collections.singleton(rule));
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private String md5Encode(final String username, final String password, final byte[] md5Salt) {
        Method method = PostgreSQLMD5PasswordAuthenticator.class.getDeclaredMethod("md5Encode", String.class, String.class, byte[].class);
        method.setAccessible(true);
        return (String) method.invoke(new PostgreSQLMD5PasswordAuthenticator(), username, password, md5Salt);
    }
}
