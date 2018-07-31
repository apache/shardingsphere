/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.proxy.transport.mysql.packet.handshake;

import com.google.common.base.Preconditions;
import com.google.common.primitives.Bytes;
import io.shardingsphere.jdbc.orchestration.internal.OrchestrationProxyConfiguration;
import io.shardingsphere.proxy.config.RuleRegistry;
import org.junit.Before;
import org.junit.Test;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class AuthorityHandlerTest {
    
    private final RuleRegistry RULE_REGISTRY = RuleRegistry.getInstance();
    
    private final AuthorityHandler authorityHandler = new AuthorityHandler();
    
    private final byte[] part1 = {84, 85, 115, 77, 68, 116, 85, 78};
    private final byte[] part2 = {83, 121, 75, 81, 87, 56, 120, 112, 73, 109, 77, 69};
    
    @Before
    public void setUp() throws IOException, NoSuchFieldException, IllegalAccessException  {
        OrchestrationProxyConfiguration config = loadLocalConfiguration(new File(AuthorityHandlerTest.class.getResource("/conf/config.yaml").getFile()));
        RULE_REGISTRY.init(config);
        reviseAuthorityHandler();
    }
    
    private void reviseAuthorityHandler() throws NoSuchFieldException, IllegalAccessException {
        AuthPluginData authPluginData = new AuthPluginData(part1, part2);
        Field field = authorityHandler.getClass().getDeclaredField("authPluginData");
        field.setAccessible(true);
        field.set(authorityHandler, authPluginData);
    }
    
    private OrchestrationProxyConfiguration loadLocalConfiguration(final File yamlFile) throws IOException {
        try (FileInputStream fileInputStream = new FileInputStream(yamlFile); InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, "UTF-8")) {
            OrchestrationProxyConfiguration result = new Yaml(new Constructor(OrchestrationProxyConfiguration.class)).loadAs(inputStreamReader, OrchestrationProxyConfiguration.class);
            Preconditions.checkNotNull(result, String.format("Configuration file `%s` is invalid.", yamlFile.getName()));
            Preconditions.checkState(!result.getDataSources().isEmpty(), "Data sources configuration can not be empty.");
            Preconditions.checkState(null != result.getShardingRule() || null != result.getMasterSlaveRule() || null != result.getOrchestration(), "Configuration invalid, sharding rule, local and orchestration configuration can not be both null.");
            Preconditions.checkNotNull(result.getProxyAuthority().getUsername(), "Authority configuration is invalid.");
            return result;
        }
    
    }
    
    @Test
    public void assertLogin() {
        byte[] authResponse = {-27, 89, -20, -27, 65, -120, -64, -101, 86, -100, -108, -100, 6, -125, -37, 117, 14, -43, 95, -113};
        assertTrue(authorityHandler.login("root", authResponse));
    }
    
    @Test
    public void assertLoginWithoutPassword() {
        RULE_REGISTRY.getProxyAuthority().setPassword("");
        byte[] authResponse = {-27, 89, -20, -27, 65, -120, -64, -101, 86, -100, -108, -100, 6, -125, -37, 117, 14, -43, 95, -113};
        assertTrue(authorityHandler.login("root", authResponse));
    }
    
    @Test
    public void assertGetAuthPluginData() {
        assertThat(authorityHandler.getAuthPluginData().getAuthPluginData(), is(Bytes.concat(part1, part2)));
    }
}
