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
import org.apache.shardingsphere.authority.config.AuthorityRuleConfiguration;
import org.apache.shardingsphere.authority.rule.AuthorityRule;
import org.apache.shardingsphere.authority.rule.builder.AuthorityRuleBuilder;
import org.apache.shardingsphere.db.protocol.postgresql.packet.handshake.PostgreSQLPasswordMessagePacket;
import org.apache.shardingsphere.db.protocol.postgresql.payload.PostgreSQLPacketPayload;
import org.apache.shardingsphere.dialect.postgresql.vendor.PostgreSQLVendorError;
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
import org.apache.shardingsphere.proxy.frontend.postgresql.authentication.PostgreSQLLoginResult;
import org.apache.shardingsphere.proxy.frontend.postgresql.authentication.authenticator.PostgreSQLMD5PasswordAuthenticator;
import org.junit.Test;

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

public final class OpenGaussAuthenticationHelperTest extends ProxyContextRestorer {
    
    private static final String SCHEMA_PATTERN = "schema_%s";
    
    private final String username = "gaussdb";
    
    private final String password = "P@ssw0rd";
    
    private final String database = "schema_0";
    
    private PostgreSQLPasswordMessagePacket passwordMessagePacket;
    
    private OpenGaussAuthenticationHelper helper;
    
    @Test
    public void testDefaultUserSuccess() {
        initProxyContext(new ShardingSphereUser(username, password, "%"));
        helper = new OpenGaussAuthenticationHelper(username);
        PostgreSQLPacketPayload payload = new PostgreSQLPacketPayload(createByteBuf(16, 128), StandardCharsets.UTF_8);
        String digest = encodeDigest(password, helper.getSaltHexString(), helper.getNonceHexString(), helper.getServerIteration());
        payload.writeInt4(4 + digest.length() + 1);
        payload.writeStringNul(digest);
        passwordMessagePacket = new PostgreSQLPasswordMessagePacket(payload);
        PostgreSQLLoginResult postgreSQLLoginResult = helper.processPassword(username, database, passwordMessagePacket);
        assertThat(postgreSQLLoginResult.getVendorError(), is(PostgreSQLVendorError.SUCCESSFUL_COMPLETION));
    }
    
    @Test
    public void testDefaultUserFalse() {
        initProxyContext(new ShardingSphereUser(username, password, "%"));
        helper = new OpenGaussAuthenticationHelper(username);
        PostgreSQLPacketPayload payload = new PostgreSQLPacketPayload(createByteBuf(16, 128), StandardCharsets.UTF_8);
        String digest = encodeDigest(password + "random", helper.getSaltHexString(), helper.getNonceHexString(), helper.getServerIteration());
        payload.writeInt4(4 + digest.length() + 1);
        payload.writeStringNul(digest);
        passwordMessagePacket = new PostgreSQLPasswordMessagePacket(payload);
        PostgreSQLLoginResult postgreSQLLoginResult = helper.processPassword(username, database, passwordMessagePacket);
        assertThat(postgreSQLLoginResult.getVendorError(), is(PostgreSQLVendorError.INVALID_PASSWORD));
    }
    
    @Test
    public void testSha256UserSuccess() {
        initProxyContext(new ShardingSphereUser(username, password, "%", "sha256"));
        helper = new OpenGaussAuthenticationHelper(username);
        PostgreSQLPacketPayload payload = new PostgreSQLPacketPayload(createByteBuf(16, 128), StandardCharsets.UTF_8);
        String digest = encodeDigest(password, helper.getSaltHexString(), helper.getNonceHexString(), helper.getServerIteration());
        payload.writeInt4(4 + digest.length() + 1);
        payload.writeStringNul(digest);
        passwordMessagePacket = new PostgreSQLPasswordMessagePacket(payload);
        PostgreSQLLoginResult postgreSQLLoginResult = helper.processPassword(username, database, passwordMessagePacket);
        assertThat(postgreSQLLoginResult.getVendorError(), is(PostgreSQLVendorError.SUCCESSFUL_COMPLETION));
    }
    
    @Test
    public void testSha256UserFalse() {
        initProxyContext(new ShardingSphereUser(username, password, "%", "sha256"));
        helper = new OpenGaussAuthenticationHelper(username);
        PostgreSQLPacketPayload payload = new PostgreSQLPacketPayload(createByteBuf(16, 128), StandardCharsets.UTF_8);
        String digest = encodeDigest(password + "random", helper.getSaltHexString(), helper.getNonceHexString(), helper.getServerIteration());
        payload.writeInt4(4 + digest.length() + 1);
        payload.writeStringNul(digest);
        passwordMessagePacket = new PostgreSQLPasswordMessagePacket(payload);
        PostgreSQLLoginResult postgreSQLLoginResult = helper.processPassword(username, database, passwordMessagePacket);
        assertThat(postgreSQLLoginResult.getVendorError(), is(PostgreSQLVendorError.INVALID_PASSWORD));
    }
    
    @Test
    public void testMD5UserSuccess() {
        initProxyContext(new ShardingSphereUser(username, password, "%", "md5"));
        helper = new OpenGaussAuthenticationHelper(username);
        helper.createIdentifierPacket();
        PostgreSQLPacketPayload payload = new PostgreSQLPacketPayload(createByteBuf(16, 128), StandardCharsets.UTF_8);
        String md5Digest = md5Encode(username, password, helper.getMd5Salt());
        payload.writeInt4(5);
        payload.writeStringNul(md5Digest);
        passwordMessagePacket = new PostgreSQLPasswordMessagePacket(payload);
        PostgreSQLLoginResult postgreSQLLoginResult = helper.processPassword(username, database, passwordMessagePacket);
        assertThat(postgreSQLLoginResult.getVendorError(), is(PostgreSQLVendorError.SUCCESSFUL_COMPLETION));
    }
    
    @Test
    public void testMD5UserFalse() {
        initProxyContext(new ShardingSphereUser(username, password, "%", "md5"));
        helper = new OpenGaussAuthenticationHelper(username);
        helper.createIdentifierPacket();
        PostgreSQLPacketPayload payload = new PostgreSQLPacketPayload(createByteBuf(16, 128), StandardCharsets.UTF_8);
        String md5Digest = md5Encode(username, password + "bbb", helper.getMd5Salt());
        payload.writeInt4(5);
        payload.writeStringNul(md5Digest);
        passwordMessagePacket = new PostgreSQLPasswordMessagePacket(payload);
        PostgreSQLLoginResult postgreSQLLoginResult = helper.processPassword(username, database, passwordMessagePacket);
        assertThat(postgreSQLLoginResult.getVendorError(), is(PostgreSQLVendorError.INVALID_PASSWORD));
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private String md5Encode(final String username, final String password, final byte[] md5Salt) {
        Method method = PostgreSQLMD5PasswordAuthenticator.class.getDeclaredMethod("md5Encode", String.class, String.class, byte[].class);
        method.setAccessible(true);
        return (String) method.invoke(new PostgreSQLMD5PasswordAuthenticator(), username, password, md5Salt);
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
        AuthorityRule rule = new AuthorityRuleBuilder().build(ruleConfig, Collections.emptyMap(), mock(InstanceContext.class));
        return new ShardingSphereRuleMetaData(Collections.singleton(rule));
    }
    
    private String encodeDigest(final String password, final String random64code, final String token, final int serverIteration) {
        return new String(OpenGaussAuthenticationAlgorithm.doRFC5802Algorithm(password, random64code, token, serverIteration));
    }
}
