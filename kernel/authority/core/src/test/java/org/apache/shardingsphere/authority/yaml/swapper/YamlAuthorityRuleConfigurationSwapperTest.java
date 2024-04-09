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

package org.apache.shardingsphere.authority.yaml.swapper;

import org.apache.shardingsphere.authority.config.AuthorityRuleConfiguration;
import org.apache.shardingsphere.authority.yaml.config.YamlAuthorityRuleConfiguration;
import org.apache.shardingsphere.authority.yaml.config.YamlUserConfiguration;
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.algorithm.core.yaml.YamlAlgorithmConfiguration;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class YamlAuthorityRuleConfigurationSwapperTest {
    
    private final YamlAuthorityRuleConfigurationSwapper swapper = new YamlAuthorityRuleConfigurationSwapper();
    
    @Test
    void assertSwapToYamlConfiguration() {
        AuthorityRuleConfiguration authorityRuleConfig = new AuthorityRuleConfiguration(Collections.emptyList(), new AlgorithmConfiguration("ALL_PERMITTED", new Properties()),
                Collections.singletonMap("md5", createAlgorithmConfiguration()), "scram_sha256");
        YamlAuthorityRuleConfiguration actual = swapper.swapToYamlConfiguration(authorityRuleConfig);
        assertTrue(actual.getUsers().isEmpty());
        assertNotNull(actual.getPrivilege());
        assertThat(actual.getDefaultAuthenticator(), is("scram_sha256"));
        assertThat(actual.getAuthenticators().size(), is(1));
    }
    
    @Test
    void assertSwapToObject() {
        YamlAuthorityRuleConfiguration authorityRuleConfig = new YamlAuthorityRuleConfiguration();
        authorityRuleConfig.setUsers(Collections.singletonList(getYamlUser()));
        authorityRuleConfig.setPrivilege(createYamlAlgorithmConfiguration());
        authorityRuleConfig.setDefaultAuthenticator("scram_sha256");
        authorityRuleConfig.setAuthenticators(Collections.singletonMap("md5", createYamlAlgorithmConfiguration()));
        AuthorityRuleConfiguration actual = swapper.swapToObject(authorityRuleConfig);
        assertThat(actual.getUsers().size(), is(1));
        assertNotNull(actual.getPrivilegeProvider());
        assertThat(actual.getDefaultAuthenticator(), is("scram_sha256"));
        assertThat(actual.getAuthenticators().size(), is(1));
    }
    
    @Test
    void assertSwapToObjectWithDefaultProvider() {
        YamlAuthorityRuleConfiguration authorityRuleConfig = new YamlAuthorityRuleConfiguration();
        authorityRuleConfig.setUsers(Collections.singletonList(getYamlUser()));
        AuthorityRuleConfiguration actual = swapper.swapToObject(authorityRuleConfig);
        assertThat(actual.getUsers().size(), is(1));
        assertThat(actual.getPrivilegeProvider().getType(), is("ALL_PERMITTED"));
        assertThat(actual.getUsers().size(), is(1));
        assertNull(actual.getDefaultAuthenticator());
        assertTrue(actual.getAuthenticators().isEmpty());
    }
    
    private YamlUserConfiguration getYamlUser() {
        YamlUserConfiguration result = new YamlUserConfiguration();
        result.setUser("root@localhost");
        result.setPassword("password");
        return result;
    }
    
    private AlgorithmConfiguration createAlgorithmConfiguration() {
        return new AlgorithmConfiguration("MD5", new Properties());
    }
    
    private YamlAlgorithmConfiguration createYamlAlgorithmConfiguration() {
        YamlAlgorithmConfiguration result = new YamlAlgorithmConfiguration();
        result.setType("MD5");
        return result;
    }
}
