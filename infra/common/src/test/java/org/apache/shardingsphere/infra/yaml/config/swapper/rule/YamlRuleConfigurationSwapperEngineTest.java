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

package org.apache.shardingsphere.infra.yaml.config.swapper.rule;

import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.fixture.FixtureRuleConfiguration;
import org.apache.shardingsphere.infra.yaml.config.pojo.rule.YamlRuleConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.fixture.YamlRuleConfigurationFixture;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class YamlRuleConfigurationSwapperEngineTest {
    
    @Test
    void assertSwapToYamlConfigurations() {
        Collection<YamlRuleConfiguration> actual = new YamlRuleConfigurationSwapperEngine().swapToYamlRuleConfigurations(Collections.singleton(new FixtureRuleConfiguration("test")));
        assertThat(actual.size(), is(1));
        assertThat(((YamlRuleConfigurationFixture) actual.iterator().next()).getName(), is("test"));
    }
    
    @Test
    void assertSwapToRuleConfigurations() {
        YamlRuleConfigurationFixture yamlRuleConfig = new YamlRuleConfigurationFixture();
        yamlRuleConfig.setName("test");
        Collection<RuleConfiguration> actual = new YamlRuleConfigurationSwapperEngine().swapToRuleConfigurations(Collections.singleton(yamlRuleConfig));
        assertThat(actual.size(), is(1));
        assertThat(((FixtureRuleConfiguration) actual.iterator().next()).getName(), is("test"));
    }
}
