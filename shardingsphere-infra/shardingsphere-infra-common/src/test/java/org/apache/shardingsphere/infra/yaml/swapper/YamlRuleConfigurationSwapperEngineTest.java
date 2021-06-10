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

package org.apache.shardingsphere.infra.yaml.swapper;

import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.yaml.config.YamlRuleConfiguration;
import org.apache.shardingsphere.infra.yaml.swapper.fixture.RuleConfigurationFixture;
import org.apache.shardingsphere.infra.yaml.swapper.fixture.YamlRuleConfigurationFixture;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class YamlRuleConfigurationSwapperEngineTest {
    
    @Test
    public void assertSwapToYamlConfigurations() {
        RuleConfigurationFixture ruleConfig = new RuleConfigurationFixture();
        ruleConfig.setName("test");
        Collection<YamlRuleConfiguration> actual = new YamlRuleConfigurationSwapperEngine().swapToYamlRuleConfigurations(Collections.singletonList(ruleConfig));
        assertThat(actual.size(), is(1));
        assertThat(((YamlRuleConfigurationFixture) actual.iterator().next()).getName(), is("test"));
    }
    
    @Test
    public void assertSwapToRuleConfigurations() {
        YamlRuleConfigurationFixture yamlRuleConfig = new YamlRuleConfigurationFixture();
        yamlRuleConfig.setName("test");
        Collection<RuleConfiguration> actual = new YamlRuleConfigurationSwapperEngine().swapToRuleConfigurations(Collections.singletonList(yamlRuleConfig));
        assertThat(actual.size(), is(1));
        assertThat(((RuleConfigurationFixture) actual.iterator().next()).getName(), is("test"));
    }
    
    @Test
    public void assertGetYamlShortcuts() {
        Map<String, Class<?>> actual = YamlRuleConfigurationSwapperEngine.getYamlShortcuts();
        assertThat(actual.size(), is(1));
        assertTrue(actual.containsKey("!FIXTURE"));
    }
}
