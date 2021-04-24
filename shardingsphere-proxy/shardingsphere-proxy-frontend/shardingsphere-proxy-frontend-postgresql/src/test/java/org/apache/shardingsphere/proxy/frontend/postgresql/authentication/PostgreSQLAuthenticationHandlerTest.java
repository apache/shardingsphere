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
import org.apache.shardingsphere.db.protocol.postgresql.constant.PostgreSQLErrorCode;
import org.apache.shardingsphere.db.protocol.postgresql.packet.handshake.PostgreSQLPasswordMessagePacket;
import org.apache.shardingsphere.db.protocol.postgresql.payload.PostgreSQLPacketPayload;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.context.metadata.MetaDataContexts;
import org.apache.shardingsphere.infra.context.metadata.impl.StandardMetaDataContexts;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.executor.kernel.ExecutorEngine;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.resource.ShardingSphereResource;
import org.apache.shardingsphere.infra.metadata.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUser;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUsers;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class PostgreSQLAuthenticationHandlerTest {
    
    private static final String SCHEMA_PATTERN = "schema_%s";
    
    private final String username = "postgres";
    
    private final String password = "sharding";
    
    private final String database = "schema_0";
    
    private final String md5Salt = "md5test";
    
    private PostgreSQLPacketPayload payload;
    
    private PostgreSQLPasswordMessagePacket passwordMessagePacket;
    
    @Before
    public void init() {
        payload = new PostgreSQLPacketPayload(createByteBuf(16, 128));
        String md5Digest = md5Encode(username, password, md5Salt.getBytes(StandardCharsets.UTF_8));
        payload.writeInt4(4 + md5Digest.length() + 1);
        payload.writeStringNul(md5Digest);
        passwordMessagePacket = new PostgreSQLPasswordMessagePacket(payload);
    }
    
    @Test
    public void assertLoginWithPassword() {
        initProxyContext(new ShardingSphereUser(username, password, "%"));
        PostgreSQLLoginResult postgreSQLLoginResult = PostgreSQLAuthenticationHandler.loginWithMd5Password(username, database, md5Salt.getBytes(StandardCharsets.UTF_8), passwordMessagePacket);
        assertThat(postgreSQLLoginResult.getErrorCode(), is(PostgreSQLErrorCode.SUCCESSFUL_COMPLETION));
    }
    
    @Test
    public void assertLoginWithAbsentUser() {
        initProxyContext(new ShardingSphereUser("username", password, "%"));
        PostgreSQLLoginResult postgreSQLLoginResult = PostgreSQLAuthenticationHandler.loginWithMd5Password(username, database, md5Salt.getBytes(StandardCharsets.UTF_8), passwordMessagePacket);
        assertThat(postgreSQLLoginResult.getErrorCode(), is(PostgreSQLErrorCode.INVALID_AUTHORIZATION_SPECIFICATION));
    }
    
    @Test
    public void assertLoginWithIncorrectPassword() {
        initProxyContext(new ShardingSphereUser(username, "password", "%"));
        PostgreSQLLoginResult postgreSQLLoginResult = PostgreSQLAuthenticationHandler.loginWithMd5Password(username, database, md5Salt.getBytes(StandardCharsets.UTF_8), passwordMessagePacket);
        assertThat(postgreSQLLoginResult.getErrorCode(), is(PostgreSQLErrorCode.INVALID_PASSWORD));
    }
    
    @Test
    public void assertLoginWithoutPassword() {
        initProxyContext(new ShardingSphereUser(username, null, "%"));
        PostgreSQLLoginResult postgreSQLLoginResult = PostgreSQLAuthenticationHandler.loginWithMd5Password(username, database, md5Salt.getBytes(StandardCharsets.UTF_8), passwordMessagePacket);
        assertThat(postgreSQLLoginResult.getErrorCode(), is(PostgreSQLErrorCode.INVALID_PASSWORD));
    }
    
    @Test
    public void assertLoginWithNonExistDatabase() {
        initProxyContext(new ShardingSphereUser(username, password, "%"));
        String database = "non_exist_database";
        PostgreSQLLoginResult postgreSQLLoginResult = PostgreSQLAuthenticationHandler.loginWithMd5Password(username, database, md5Salt.getBytes(StandardCharsets.UTF_8), passwordMessagePacket);
        assertThat(postgreSQLLoginResult.getErrorCode(), is(PostgreSQLErrorCode.INVALID_CATALOG_NAME));
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private void initProxyContext(final ShardingSphereUser user) {
        Field field = ProxyContext.getInstance().getClass().getDeclaredField("metaDataContexts");
        field.setAccessible(true);
        field.set(ProxyContext.getInstance(), getMetaDataContexts(user));
    }
    
    private MetaDataContexts getMetaDataContexts(final ShardingSphereUser user) {
        return new StandardMetaDataContexts(getMetaDataMap(), 
                mock(ShardingSphereRuleMetaData.class), mock(ExecutorEngine.class), new ShardingSphereUsers(Collections.singleton(user)), new ConfigurationProperties(new Properties()));
    }
    
    private ByteBuf createByteBuf(final int initialCapacity, final int maxCapacity) {
        return new UnpooledHeapByteBuf(UnpooledByteBufAllocator.DEFAULT, initialCapacity, maxCapacity);
    }
    
    private Map<String, ShardingSphereMetaData> getMetaDataMap() {
        Map<String, ShardingSphereMetaData> result = new HashMap<>(10, 1);
        for (int i = 0; i < 10; i++) {
            ShardingSphereMetaData metaData = mock(ShardingSphereMetaData.class);
            when(metaData.getResource()).thenReturn(new ShardingSphereResource(Collections.emptyMap(), null, null, new MySQLDatabaseType()));
            when(metaData.getRuleMetaData()).thenReturn(new ShardingSphereRuleMetaData(Collections.emptyList(), Collections.emptyList()));
            result.put(String.format(SCHEMA_PATTERN, i), metaData);
        }
        return result;
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private String md5Encode(final String username, final String password, final byte[] md5Salt) {
        Method method = PostgreSQLAuthenticationHandler.class.getDeclaredMethod("md5Encode", String.class, String.class, byte[].class);
        method.setAccessible(true);
        return (String) method.invoke(PostgreSQLAuthenticationHandler.class, username, password, md5Salt);
    }
}
