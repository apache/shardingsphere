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
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.shardingsphere.authority.config.AuthorityRuleConfiguration;
import org.apache.shardingsphere.authority.rule.AuthorityRule;
import org.apache.shardingsphere.authority.rule.builder.AuthorityRuleBuilder;
import org.apache.shardingsphere.db.protocol.postgresql.packet.handshake.PostgreSQLPasswordMessagePacket;
import org.apache.shardingsphere.db.protocol.postgresql.payload.PostgreSQLPacketPayload;
import org.apache.shardingsphere.dialect.exception.syntax.database.UnknownDatabaseException;
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
import org.apache.shardingsphere.proxy.frontend.opengauss.ProxyContextRestorer;
import org.apache.shardingsphere.proxy.frontend.opengauss.authentication.fixture.OpenGaussAuthenticationAlgorithm;
import org.junit.Before;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class OpenGaussAuthenticationHandlerTest extends ProxyContextRestorer {
    
    private static final String SCHEMA_PATTERN = "schema_%s";
    
    private final String username = "gaussdb";
    
    private final String password = "P@ssw0rd";
    
    private final String database = "schema_0";
    
    private final String random64Code = RandomStringUtils.randomAlphanumeric(64);
    
    private final String token = RandomStringUtils.randomAlphanumeric(8);
    
    private final int serverIteration = 2048;
    
    private PostgreSQLPasswordMessagePacket passwordMessagePacket;
    
    @Before
    public void init() {
        PostgreSQLPacketPayload payload = new PostgreSQLPacketPayload(createByteBuf(16, 128), StandardCharsets.UTF_8);
        String digest = encodeDigest(password, random64Code, token, serverIteration);
        payload.writeInt4(4 + digest.length() + 1);
        payload.writeStringNul(digest);
        passwordMessagePacket = new PostgreSQLPasswordMessagePacket(payload);
    }
    
    @Test
    public void assertLoginSuccess() {
        initProxyContext(new ShardingSphereUser(username, password, "%"));
        OpenGaussAuthenticationHandler.loginWithSCRAMSha256Password(username, database, random64Code, token, serverIteration, passwordMessagePacket);
    }
    
    @Test(expected = UnknownUsernameException.class)
    public void assertLoginWithAbsentUser() {
        initProxyContext(new ShardingSphereUser("username", password, "%"));
        OpenGaussAuthenticationHandler.loginWithSCRAMSha256Password(username, database, random64Code, token, serverIteration, passwordMessagePacket);
    }
    
    @Test(expected = InvalidPasswordException.class)
    public void assertLoginWithIncorrectPassword() {
        initProxyContext(new ShardingSphereUser(username, "password", "%"));
        OpenGaussAuthenticationHandler.loginWithSCRAMSha256Password(username, database, random64Code, token, serverIteration, passwordMessagePacket);
    }
    
    @Test(expected = UnknownDatabaseException.class)
    public void assertLoginWithNonExistDatabase() {
        initProxyContext(new ShardingSphereUser(username, password, "%"));
        String database = "non_exist_database";
        OpenGaussAuthenticationHandler.loginWithSCRAMSha256Password(username, database, random64Code, token, serverIteration, passwordMessagePacket);
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
    
    private String encodeDigest(final String password, final String random64code, final String token, final int serverIteration) {
        return new String(OpenGaussAuthenticationAlgorithm.doRFC5802Algorithm(password, random64code, token, serverIteration));
    }
}
