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

package org.apache.shardingsphere.authority.it;

import org.apache.shardingsphere.authority.yaml.config.YamlAuthorityRuleConfiguration;
import org.apache.shardingsphere.authority.yaml.config.YamlUserConfiguration;
import org.apache.shardingsphere.infra.algorithm.core.yaml.YamlAlgorithmConfiguration;
import org.apache.shardingsphere.infra.yaml.config.pojo.YamlRootConfiguration;
import org.apache.shardingsphere.test.it.yaml.YamlRuleConfigurationIT;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthorityRuleConfigurationYamlIT extends YamlRuleConfigurationIT {
    
    AuthorityRuleConfigurationYamlIT() {
        super("yaml/authority-rule.yaml");
    }
    
    @Override
    protected void assertYamlRootConfiguration(final YamlRootConfiguration actual) {
        assertAuthorityRule((YamlAuthorityRuleConfiguration) actual.getRules().iterator().next());
    }
    
    private void assertAuthorityRule(final YamlAuthorityRuleConfiguration actual) {
        assertUsers(new ArrayList<>(actual.getUsers()));
        assertPrivilege(actual.getPrivilege());
        assertAuthenticators(actual.getAuthenticators());
        assertDefaultAuthenticator(actual.getDefaultAuthenticator());
    }
    
    private void assertUsers(final List<YamlUserConfiguration> actual) {
        assertThat(actual.size(), is(2));
        assertThat(actual.get(0).getUser(), is("root@%"));
        assertThat(actual.get(0).getPassword(), is("root"));
        assertTrue(actual.get(0).isAdmin());
        assertThat(actual.get(1).getUser(), is("sharding@"));
        assertThat(actual.get(1).getPassword(), is("sharding"));
        assertFalse(actual.get(1).isAdmin());
    }
    
    private void assertPrivilege(final YamlAlgorithmConfiguration actual) {
        assertThat(actual.getType(), is("ALL_PERMITTED"));
        assertTrue(actual.getProps().isEmpty());
    }
    
    private void assertAuthenticators(final Map<String, YamlAlgorithmConfiguration> actual) {
        assertThat(actual.size(), is(1));
        assertThat(actual.get("fixture").getType(), is("FIXTURE"));
        assertTrue(actual.get("fixture").getProps().isEmpty());
    }
    
    private void assertDefaultAuthenticator(final String actual) {
        assertThat(actual, is("fixture"));
    }
}
