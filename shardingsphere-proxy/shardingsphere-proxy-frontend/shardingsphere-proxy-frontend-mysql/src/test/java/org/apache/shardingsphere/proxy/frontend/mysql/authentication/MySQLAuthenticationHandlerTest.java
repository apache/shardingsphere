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

package org.apache.shardingsphere.proxy.frontend.mysql.authentication;

import com.google.common.primitives.Bytes;
import lombok.SneakyThrows;
import org.apache.shardingsphere.authority.provider.natived.NativeAuthorityProviderAlgorithm;
import org.apache.shardingsphere.db.protocol.mysql.constant.MySQLServerErrorCode;
import org.apache.shardingsphere.db.protocol.mysql.packet.handshake.MySQLAuthPluginData;
import org.apache.shardingsphere.infra.executor.check.SQLChecker;
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
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.junit.Before;
import org.junit.Ignore;
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
import static org.mockito.Mockito.when;

public final class MySQLAuthenticationHandlerTest {
    
    private static final String SCHEMA_PATTERN = "db%s";
    
    private final MySQLAuthenticationHandler authenticationHandler = new MySQLAuthenticationHandler();
    
    private final byte[] part1 = {84, 85, 115, 77, 68, 116, 85, 78};
    
    private final byte[] part2 = {83, 121, 75, 81, 87, 56, 120, 112, 73, 109, 77, 69};
    
    static {
        ShardingSphereServiceLoader.register(SQLChecker.class);
    }
    
    @Before
    public void setUp() {
        initAuthPluginDataForAuthenticationHandler();
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private void initAuthPluginDataForAuthenticationHandler() {
        MySQLAuthPluginData authPluginData = new MySQLAuthPluginData(part1, part2);
        Field field = MySQLAuthenticationHandler.class.getDeclaredField("authPluginData");
        field.setAccessible(true);
        field.set(authenticationHandler, authPluginData);
    }
    
    @Test
    public void assertLoginWithPassword() {
        setAuthority(new ShardingSphereUser("root", "root", ""));
        byte[] authResponse = {-27, 89, -20, -27, 65, -120, -64, -101, 86, -100, -108, -100, 6, -125, -37, 117, 14, -43, 95, -113};
        assertFalse(authenticationHandler.login("root", "", authResponse, "db1").isPresent());
    }
    
    @Test
    public void assertLoginWithAbsentUser() {
        setAuthority(new ShardingSphereUser("root", "root", ""));
        byte[] authResponse = {-27, 89, -20, -27, 65, -120, -64, -101, 86, -100, -108, -100, 6, -125, -37, 117, 14, -43, 95, -113};
        assertThat(authenticationHandler.login("root1", "", authResponse, "db1").orElse(null), is(MySQLServerErrorCode.ER_ACCESS_DENIED_ERROR));
    }
    
    @Test
    public void assertLoginWithIncorrectPassword() {
        setAuthority(new ShardingSphereUser("root", "root", ""));
        byte[] authResponse = {0, 89, -20, -27, 65, -120, -64, -101, 86, -100, -108, -100, 6, -125, -37, 117, 14, -43, 95, -113};
        assertThat(authenticationHandler.login("root", "", authResponse, "db1").orElse(null), is(MySQLServerErrorCode.ER_ACCESS_DENIED_ERROR));
    }
    
    @Test
    public void assertLoginWithoutPassword() {
        setAuthority(new ShardingSphereUser("root", null, ""));
        byte[] authResponse = {};
        assertFalse(authenticationHandler.login("root", "", authResponse, "db1").isPresent());
    }
    
    @Test
    @Ignore
    // TODO mock return false for SQLCheckEngine
    public void assertLoginWithUnauthorizedSchema() {
        initProxyContext(new ShardingSphereUser("root", "root", ""));
        byte[] authResponse = {-27, 89, -20, -27, 65, -120, -64, -101, 86, -100, -108, -100, 6, -125, -37, 117, 14, -43, 95, -113};
        assertThat(authenticationHandler.login("root", "", authResponse, "db2").orElse(null), is(MySQLServerErrorCode.ER_DBACCESS_DENIED_ERROR));
    }
    
    @Test
    public void assertGetAuthPluginData() {
        assertThat(authenticationHandler.getAuthPluginData().getAuthenticationPluginData(), is(Bytes.concat(part1, part2)));
    }
    
    private void setAuthority(final ShardingSphereUser user) {
        NativeAuthorityProviderAlgorithm algorithm = new NativeAuthorityProviderAlgorithm();
        algorithm.init(Collections.emptyMap(), Collections.emptyList());
        initProxyContext(user);
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
}
