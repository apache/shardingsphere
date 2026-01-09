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

package org.apache.shardingsphere.proxy.backend.config.yaml;

import org.apache.shardingsphere.infra.yaml.config.pojo.rule.YamlRuleConfiguration;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class YamlProxyServerConfigurationTest {
    
    @Test
    void assertSetRulesIgnoresNull() {
        YamlProxyServerConfiguration actual = new YamlProxyServerConfiguration();
        Collection<YamlRuleConfiguration> expectedRules = new LinkedList<>(Collections.singleton(mock(YamlRuleConfiguration.class)));
        actual.setRules(expectedRules);
        actual.setRules(null);
        assertThat(actual.getRules(), sameInstance(expectedRules));
    }
    
    @Test
    void assertSetRulesReplacesWhenNotNull() {
        YamlProxyServerConfiguration actual = new YamlProxyServerConfiguration();
        Collection<YamlRuleConfiguration> expectedRules = Collections.singletonList(mock(YamlRuleConfiguration.class));
        actual.setRules(expectedRules);
        assertThat(actual.getRules(), is(expectedRules));
    }
    
    @Test
    void assertSetPropsIgnoresNull() {
        YamlProxyServerConfiguration actual = new YamlProxyServerConfiguration();
        Properties expectedProps = new Properties();
        expectedProps.setProperty("key", "value");
        actual.setProps(expectedProps);
        actual.setProps(null);
        assertThat(actual.getProps(), sameInstance(expectedProps));
    }
    
    @Test
    void assertSetPropsReplacesWhenNotNull() {
        YamlProxyServerConfiguration actual = new YamlProxyServerConfiguration();
        Properties expectedProps = new Properties();
        expectedProps.setProperty("proxy-port", "3307");
        actual.setProps(expectedProps);
        assertTrue(actual.getProps().containsKey("proxy-port"));
        assertThat(actual.getProps(), is(expectedProps));
    }
}
