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
import lombok.SneakyThrows;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.shardingsphere.authority.config.AuthorityRuleConfiguration;
import org.apache.shardingsphere.authority.rule.AuthorityRule;
import org.apache.shardingsphere.authority.rule.builder.AuthorityRuleBuilder;
import org.apache.shardingsphere.db.protocol.postgresql.constant.PostgreSQLErrorCode;
import org.apache.shardingsphere.db.protocol.postgresql.packet.handshake.PostgreSQLPasswordMessagePacket;
import org.apache.shardingsphere.db.protocol.postgresql.payload.PostgreSQLPacketPayload;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.executor.kernel.ExecutorEngine;
import org.apache.shardingsphere.infra.federation.optimizer.context.OptimizerContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.resource.ShardingSphereResource;
import org.apache.shardingsphere.infra.metadata.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUser;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.frontend.postgresql.authentication.PostgreSQLLoginResult;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class OpenGaussAuthenticationHandlerTest {
    
    private static final String SCHEMA_PATTERN = "schema_%s";
    
    private final String username = "gaussdb";
    
    private final String password = "P@ssw0rd";
    
    private final String database = "schema_0";
    
    private final String random64Code = RandomStringUtils.randomAlphanumeric(64);
    
    private final String token = RandomStringUtils.randomAlphanumeric(8);
    
    private final int serverIteration = 2048;
    
    private PostgreSQLPacketPayload payload;
    
    private PostgreSQLPasswordMessagePacket passwordMessagePacket;
    
    @Before
    public void init() {
        payload = new PostgreSQLPacketPayload(createByteBuf(16, 128), StandardCharsets.UTF_8);
        String digest = encodeDigest(password, random64Code, token, serverIteration);
        payload.writeInt4(4 + digest.length() + 1);
        payload.writeStringNul(digest);
        passwordMessagePacket = new PostgreSQLPasswordMessagePacket(payload);
    }
    
    @Test
    public void assertLoginWithPassword() {
        initProxyContext(new ShardingSphereUser(username, password, "%"));
        PostgreSQLLoginResult postgreSQLLoginResult = OpenGaussAuthenticationHandler.loginWithSha256Password(username, database, random64Code, token, serverIteration, passwordMessagePacket);
        assertThat(postgreSQLLoginResult.getErrorCode(), is(PostgreSQLErrorCode.SUCCESSFUL_COMPLETION));
    }
    
    @Test
    public void assertLoginWithAbsentUser() {
        initProxyContext(new ShardingSphereUser("username", password, "%"));
        PostgreSQLLoginResult postgreSQLLoginResult = OpenGaussAuthenticationHandler.loginWithSha256Password(username, database, random64Code, token, serverIteration, passwordMessagePacket);
        assertThat(postgreSQLLoginResult.getErrorCode(), is(PostgreSQLErrorCode.INVALID_AUTHORIZATION_SPECIFICATION));
    }
    
    @Test
    public void assertLoginWithIncorrectPassword() {
        initProxyContext(new ShardingSphereUser(username, "password", "%"));
        PostgreSQLLoginResult postgreSQLLoginResult = OpenGaussAuthenticationHandler.loginWithSha256Password(username, database, random64Code, token, serverIteration, passwordMessagePacket);
        assertThat(postgreSQLLoginResult.getErrorCode(), is(PostgreSQLErrorCode.INVALID_PASSWORD));
    }
    
    @Test
    public void assertLoginWithNonExistDatabase() {
        initProxyContext(new ShardingSphereUser(username, password, "%"));
        String database = "non_exist_database";
        PostgreSQLLoginResult postgreSQLLoginResult = OpenGaussAuthenticationHandler.loginWithSha256Password(username, database, random64Code, token, serverIteration, passwordMessagePacket);
        assertThat(postgreSQLLoginResult.getErrorCode(), is(PostgreSQLErrorCode.INVALID_CATALOG_NAME));
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private void initProxyContext(final ShardingSphereUser user) {
        Field contextManagerField = ProxyContext.getInstance().getClass().getDeclaredField("contextManager");
        contextManagerField.setAccessible(true);
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        MetaDataContexts metaDataContexts = getMetaDataContexts(user);
        when(contextManager.getMetaDataContexts()).thenReturn(metaDataContexts);
        contextManagerField.set(ProxyContext.getInstance(), contextManager);
    }
    
    private MetaDataContexts getMetaDataContexts(final ShardingSphereUser user) {
        return new MetaDataContexts(mock(MetaDataPersistService.class), getMetaDataMap(),
                buildGlobalRuleMetaData(user), mock(ExecutorEngine.class), new ConfigurationProperties(new Properties()), mock(OptimizerContext.class));
    }
    
    private ByteBuf createByteBuf(final int initialCapacity, final int maxCapacity) {
        return new UnpooledHeapByteBuf(UnpooledByteBufAllocator.DEFAULT, initialCapacity, maxCapacity);
    }
    
    private Map<String, ShardingSphereMetaData> getMetaDataMap() {
        Map<String, ShardingSphereMetaData> result = new HashMap<>(10, 1);
        for (int i = 0; i < 10; i++) {
            ShardingSphereMetaData metaData = mock(ShardingSphereMetaData.class);
            ShardingSphereSchema schema = mock(ShardingSphereSchema.class);
            when(metaData.getResource()).thenReturn(new ShardingSphereResource(Collections.emptyMap(), null, null, new MySQLDatabaseType()));
            when(metaData.getRuleMetaData()).thenReturn(new ShardingSphereRuleMetaData(Collections.emptyList(), Collections.emptyList()));
            when(metaData.getSchema()).thenReturn(schema);
            when(schema.getTables()).thenReturn(Collections.emptyMap());
            result.put(String.format(SCHEMA_PATTERN, i), metaData);
        }
        return result;
    }
    
    private ShardingSphereRuleMetaData buildGlobalRuleMetaData(final ShardingSphereUser user) {
        AuthorityRuleConfiguration authorityRuleConfiguration = new AuthorityRuleConfiguration(Collections.singletonList(user), new ShardingSphereAlgorithmConfiguration("NATIVE", new Properties()));
        AuthorityRule rule = new AuthorityRuleBuilder().build(authorityRuleConfiguration, Collections.emptyMap());
        return new ShardingSphereRuleMetaData(Collections.singletonList(authorityRuleConfiguration), Collections.singleton(rule));
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private String encodeDigest(final String password, final String random64code, final String token, final int serverIteration) {
        Method method = OpenGaussAuthenticationHandler.class.getDeclaredMethod("doRFC5802Algorithm", String.class, String.class, String.class, int.class);
        method.setAccessible(true);
        return new String((byte[]) method.invoke(OpenGaussAuthenticationHandler.class, password, random64code, token, serverIteration));
    }
}
