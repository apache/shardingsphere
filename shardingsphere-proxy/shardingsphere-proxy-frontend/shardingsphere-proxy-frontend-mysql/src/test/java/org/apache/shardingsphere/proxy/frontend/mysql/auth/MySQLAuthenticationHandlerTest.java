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

package org.apache.shardingsphere.proxy.frontend.mysql.auth;

import com.google.common.primitives.Bytes;
import lombok.SneakyThrows;
import org.apache.shardingsphere.db.protocol.mysql.constant.MySQLServerErrorCode;
import org.apache.shardingsphere.db.protocol.mysql.packet.handshake.MySQLAuthPluginData;
import org.apache.shardingsphere.infra.auth.Authentication;
import org.apache.shardingsphere.infra.auth.ProxyUser;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.kernel.context.SchemaContext;
import org.apache.shardingsphere.kernel.context.SchemaContexts;
import org.apache.shardingsphere.kernel.context.StandardSchemaContexts;
import org.apache.shardingsphere.kernel.context.runtime.RuntimeContext;
import org.apache.shardingsphere.kernel.context.schema.ShardingSphereSchema;
import org.apache.shardingsphere.proxy.backend.schema.ProxySchemaContexts;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public final class MySQLAuthenticationHandlerTest {
    
    private final MySQLAuthenticationHandler authenticationHandler = new MySQLAuthenticationHandler();
    
    private final byte[] part1 = {84, 85, 115, 77, 68, 116, 85, 78};
    
    private final byte[] part2 = {83, 121, 75, 81, 87, 56, 120, 112, 73, 109, 77, 69};
    
    @Before
    public void setUp() {
        initAuthPluginDataForAuthenticationHandler();
    }
    
    @SneakyThrows
    private void initAuthPluginDataForAuthenticationHandler() {
        MySQLAuthPluginData authPluginData = new MySQLAuthPluginData(part1, part2);
        Field field = MySQLAuthenticationHandler.class.getDeclaredField("authPluginData");
        field.setAccessible(true);
        field.set(authenticationHandler, authPluginData);
    }
    
    @Test
    public void assertLoginWithPassword() {
        setAuthentication(new ProxyUser("root", Collections.singleton("db1")));
        byte[] authResponse = {-27, 89, -20, -27, 65, -120, -64, -101, 86, -100, -108, -100, 6, -125, -37, 117, 14, -43, 95, -113};
        assertFalse(authenticationHandler.login("root", authResponse, "db1").isPresent());
    }
    
    @Test
    public void assertLoginWithAbsentUser() {
        setAuthentication(new ProxyUser("root", Collections.singleton("db1")));
        byte[] authResponse = {-27, 89, -20, -27, 65, -120, -64, -101, 86, -100, -108, -100, 6, -125, -37, 117, 14, -43, 95, -113};
        assertThat(authenticationHandler.login("root1", authResponse, "db1").orElse(null), is(MySQLServerErrorCode.ER_ACCESS_DENIED_ERROR));
    }
    
    @Test
    public void assertLoginWithIncorrectPassword() {
        setAuthentication(new ProxyUser("root", Collections.singleton("db1")));
        byte[] authResponse = {0, 89, -20, -27, 65, -120, -64, -101, 86, -100, -108, -100, 6, -125, -37, 117, 14, -43, 95, -113};
        assertThat(authenticationHandler.login("root", authResponse, "db1").orElse(null), is(MySQLServerErrorCode.ER_ACCESS_DENIED_ERROR));
    }
    
    @Test
    public void assertLoginWithoutPassword() {
        setAuthentication(new ProxyUser(null, null));
        byte[] authResponse = {-27, 89, -20, -27, 65, -120, -64, -101, 86, -100, -108, -100, 6, -125, -37, 117, 14, -43, 95, -113};
        assertFalse(authenticationHandler.login("root", authResponse, "db1").isPresent());
    }
    
    @Test
    public void assertLoginWithUnauthorizedSchema() {
        setAuthentication(new ProxyUser("root", Collections.singleton("db1")));
        byte[] authResponse = {-27, 89, -20, -27, 65, -120, -64, -101, 86, -100, -108, -100, 6, -125, -37, 117, 14, -43, 95, -113};
        assertThat(authenticationHandler.login("root", authResponse, "db2").orElse(null), is(MySQLServerErrorCode.ER_DBACCESS_DENIED_ERROR));
    }
    
    @Test
    public void assertGetAuthPluginData() {
        assertThat(authenticationHandler.getAuthPluginData().getAuthPluginData(), is(Bytes.concat(part1, part2)));
    }
    
    @SneakyThrows
    private void setAuthentication(final ProxyUser proxyUser) {
        Authentication authentication = new Authentication();
        authentication.getUsers().put("root", proxyUser);
        initProxySchemaContexts(authentication);
    }
    
    @SneakyThrows
    private void initProxySchemaContexts(final Authentication authentication) {
        Field field = ProxySchemaContexts.getInstance().getClass().getDeclaredField("schemaContexts");
        field.setAccessible(true);
        field.set(ProxySchemaContexts.getInstance(), getSchemaContexts(authentication));
    }
    
    private SchemaContexts getSchemaContexts(final Authentication authentication) {
        return new StandardSchemaContexts(getSchemaContextMap(), authentication, new ConfigurationProperties(new Properties()), new MySQLDatabaseType());
    }
    
    private Map<String, SchemaContext> getSchemaContextMap() {
        Map<String, SchemaContext> result = new HashMap<>(10);
        for (int i = 0; i < 10; i++) {
            String name = "schema_" + i;
            ShardingSphereSchema schema = mock(ShardingSphereSchema.class);
            RuntimeContext runtimeContext = mock(RuntimeContext.class);
            result.put(name, new SchemaContext(name, schema, runtimeContext));
        }
        return result;
    }
}
