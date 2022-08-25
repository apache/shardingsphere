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
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.yaml.config.pojo.algorithm.YamlAlgorithmConfiguration;
import org.junit.Test;

import java.util.Collections;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class YamlAuthorityRuleConfigurationSwapperTest {
    
    private final YamlAuthorityRuleConfigurationSwapper swapper = new YamlAuthorityRuleConfigurationSwapper();
    
    @Test
    public void assertSwapToYamlConfiguration() {
        AuthorityRuleConfiguration authorityRuleConfig = new AuthorityRuleConfiguration(Collections.emptyList(), new AlgorithmConfiguration("type", new Properties()));
        YamlAuthorityRuleConfiguration actual = swapper.swapToYamlConfiguration(authorityRuleConfig);
        assertTrue(actual.getUsers().isEmpty());
        assertNotNull(actual.getProvider());
    }
    
    @Test
    public void assertSwapToObject() {
        YamlAuthorityRuleConfiguration authorityRuleConfig = new YamlAuthorityRuleConfiguration();
        authorityRuleConfig.setUsers(Collections.singletonList("root@localhost:pass"));
        authorityRuleConfig.setProvider(new YamlAlgorithmConfiguration("type", new Properties()));
        AuthorityRuleConfiguration actual = swapper.swapToObject(authorityRuleConfig);
        assertThat(actual.getUsers().size(), is(1));
        assertNotNull(actual.getProvider());
    }
    
    @Test
    public void assertSwapToObjectWithDefaultProvider() {
        YamlAuthorityRuleConfiguration authorityRuleConfig = new YamlAuthorityRuleConfiguration();
        authorityRuleConfig.setUsers(Collections.singletonList("root@localhost:pass"));
        AuthorityRuleConfiguration actual = swapper.swapToObject(authorityRuleConfig);
        assertThat(actual.getUsers().size(), is(1));
        assertThat(actual.getProvider().getType(), is("ALL_PERMITTED"));
    }
}
