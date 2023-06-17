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

package org.apache.shardingsphere.broadcast.yaml.swapper;

import org.apache.shardingsphere.broadcast.api.config.BroadcastRuleConfiguration;
import org.apache.shardingsphere.broadcast.yaml.config.YamlBroadcastRuleConfiguration;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class YamlBroadcastRuleConfigurationSwapperTest {
    
    @Test
    void assertSwapToYamlConfiguration() {
        BroadcastRuleConfiguration ruleConfig = new BroadcastRuleConfiguration(Collections.singleton("t_address"));
        YamlBroadcastRuleConfigurationSwapper swapper = new YamlBroadcastRuleConfigurationSwapper();
        YamlBroadcastRuleConfiguration yamlRuleConfig = swapper.swapToYamlConfiguration(ruleConfig);
        assertThat(yamlRuleConfig.getTables().size(), is(1));
        assertThat(yamlRuleConfig.getTables().iterator().next(), is("t_address"));
    }
    
    @Test
    void assertSwapToObject() {
        YamlBroadcastRuleConfiguration yamlRuleConfig = new YamlBroadcastRuleConfiguration();
        yamlRuleConfig.getTables().add("t_address");
        YamlBroadcastRuleConfigurationSwapper swapper = new YamlBroadcastRuleConfigurationSwapper();
        BroadcastRuleConfiguration ruleConfig = swapper.swapToObject(yamlRuleConfig);
        assertThat(ruleConfig.getTables().size(), is(1));
        assertThat(ruleConfig.getTables().iterator().next(), is("t_address"));
    }
}
