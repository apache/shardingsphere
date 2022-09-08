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
import org.apache.shardingsphere.authority.config.AuthorityRuleConfiguration;
import org.apache.shardingsphere.authority.model.AuthorityRegistry;
import org.apache.shardingsphere.authority.rule.AuthorityRule;
import org.apache.shardingsphere.authority.rule.builder.AuthorityRuleBuilder;
import org.apache.shardingsphere.db.protocol.mysql.constant.MySQLAuthenticationMethod;
import org.apache.shardingsphere.db.protocol.mysql.packet.handshake.MySQLAuthPluginData;
import org.apache.shardingsphere.dialect.mysql.vendor.MySQLVendorError;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUser;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.frontend.mysql.ProxyContextRestorer;
import org.apache.shardingsphere.proxy.frontend.mysql.authentication.authenticator.MySQLAuthenticator;
import org.apache.shardingsphere.proxy.frontend.mysql.authentication.authenticator.MySQLNativePasswordAuthenticator;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class MySQLAuthenticationHandlerTest extends ProxyContextRestorer {
    
    private static final String SCHEMA_PATTERN = "db%s";
    
    private final MySQLAuthenticationHandler authenticationHandler = new MySQLAuthenticationHandler();
    
    private final byte[] part1 = {84, 85, 115, 77, 68, 116, 85, 78};
    
    private final byte[] part2 = {83, 121, 75, 81, 87, 56, 120, 112, 73, 109, 77, 69};
    
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
        initProxyContext(new ShardingSphereUser("root", "root", ""), true);
        byte[] authResponse = {-27, 89, -20, -27, 65, -120, -64, -101, 86, -100, -108, -100, 6, -125, -37, 117, 14, -43, 95, -113};
        assertFalse(authenticationHandler.login("root", "", authResponse, "db1").isPresent());
    }
    
    @Test
    public void assertLoginWithAbsentUser() {
        initProxyContext(new ShardingSphereUser("root", "root", ""), true);
        byte[] authResponse = {-27, 89, -20, -27, 65, -120, -64, -101, 86, -100, -108, -100, 6, -125, -37, 117, 14, -43, 95, -113};
        assertThat(authenticationHandler.login("root1", "", authResponse, "db1").orElse(null), is(MySQLVendorError.ER_ACCESS_DENIED_ERROR));
    }
    
    @Test
    public void assertLoginWithIncorrectPassword() {
        initProxyContext(new ShardingSphereUser("root", "root", ""), true);
        byte[] authResponse = {0, 89, -20, -27, 65, -120, -64, -101, 86, -100, -108, -100, 6, -125, -37, 117, 14, -43, 95, -113};
        assertThat(authenticationHandler.login("root", "", authResponse, "db1").orElse(null), is(MySQLVendorError.ER_ACCESS_DENIED_ERROR));
    }
    
    @Test
    public void assertLoginWithoutPassword() {
        initProxyContext(new ShardingSphereUser("root", null, ""), true);
        byte[] authResponse = {};
        assertFalse(authenticationHandler.login("root", "", authResponse, "db1").isPresent());
    }
    
    @Test
    public void assertLoginWithUnauthorizedSchema() {
        initProxyContext(new ShardingSphereUser("root", "root", ""), false);
        byte[] authResponse = {-27, 89, -20, -27, 65, -120, -64, -101, 86, -100, -108, -100, 6, -125, -37, 117, 14, -43, 95, -113};
        assertThat(authenticationHandler.login("root", "", authResponse, "db11").orElse(null), is(MySQLVendorError.ER_DBACCESS_DENIED_ERROR));
    }
    
    @Test
    public void assertGetAuthPluginData() {
        assertThat(authenticationHandler.getAuthPluginData().getAuthenticationPluginData(), is(Bytes.concat(part1, part2)));
    }
    
    @Test
    public void assertGetAuthenticator() {
        MySQLAuthenticator authenticator = authenticationHandler.getAuthenticator("root", "");
        assertThat(authenticator, instanceOf(MySQLNativePasswordAuthenticator.class));
        assertThat(authenticator.getAuthenticationMethodName(), is(MySQLAuthenticationMethod.SECURE_PASSWORD_AUTHENTICATION.getMethodName()));
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private void initProxyContext(final ShardingSphereUser user, final boolean isNeedSuper) {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        MetaDataContexts metaDataContexts = getMetaDataContexts(user, isNeedSuper);
        when(contextManager.getMetaDataContexts()).thenReturn(metaDataContexts);
        ProxyContext.init(contextManager);
    }
    
    private MetaDataContexts getMetaDataContexts(final ShardingSphereUser user, final boolean isNeedSuper) throws NoSuchFieldException, IllegalAccessException {
        return new MetaDataContexts(mock(MetaDataPersistService.class),
                new ShardingSphereMetaData(getDatabases(), buildGlobalRuleMetaData(user, isNeedSuper), new ConfigurationProperties(new Properties())));
    }
    
    private Map<String, ShardingSphereDatabase> getDatabases() {
        Map<String, ShardingSphereDatabase> result = new HashMap<>(10, 1);
        for (int i = 0; i < 10; i++) {
            ShardingSphereDatabase database = mock(ShardingSphereDatabase.class);
            when(database.getRuleMetaData()).thenReturn(new ShardingSphereRuleMetaData(Collections.emptyList()));
            result.put(String.format(SCHEMA_PATTERN, i), database);
        }
        return result;
    }
    
    private ShardingSphereRuleMetaData buildGlobalRuleMetaData(final ShardingSphereUser user, final boolean isNeedSuper) throws NoSuchFieldException, IllegalAccessException {
        AuthorityRuleConfiguration ruleConfig = new AuthorityRuleConfiguration(Collections.singletonList(user), new AlgorithmConfiguration("ALL_PERMITTED", new Properties()));
        AuthorityRule rule = new AuthorityRuleBuilder().build(ruleConfig, Collections.emptyMap(), mock(InstanceContext.class), mock(ConfigurationProperties.class));
        if (!isNeedSuper) {
            Field authorityRegistryField = AuthorityRule.class.getDeclaredField("authorityRegistry");
            AuthorityRegistry authorityRegistry = mock(AuthorityRegistry.class);
            when(authorityRegistry.findPrivileges(user.getGrantee())).thenReturn(Optional.empty());
            authorityRegistryField.setAccessible(true);
            authorityRegistryField.set(rule, authorityRegistry);
        }
        return new ShardingSphereRuleMetaData(Collections.singletonList(rule));
    }
}
