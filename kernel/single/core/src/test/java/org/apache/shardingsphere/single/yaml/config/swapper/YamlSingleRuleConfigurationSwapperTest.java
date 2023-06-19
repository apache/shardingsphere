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

package org.apache.shardingsphere.single.yaml.config.swapper;

import org.apache.shardingsphere.single.api.config.SingleRuleConfiguration;
import org.apache.shardingsphere.single.yaml.config.pojo.YamlSingleRuleConfiguration;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class YamlSingleRuleConfigurationSwapperTest {
    
    @Test
    void assertSwapToObject() {
        YamlSingleRuleConfiguration yamlConfig = new YamlSingleRuleConfiguration();
        yamlConfig.setDefaultDataSource("ds_0");
        SingleRuleConfiguration ruleConfig = new YamlSingleRuleConfigurationSwapper().swapToObject(yamlConfig);
        assertTrue(ruleConfig.getDefaultDataSource().isPresent());
        assertThat(ruleConfig.getDefaultDataSource().get(), is("ds_0"));
    }
    
    @Test
    void assertSwapToObjectWithoutDataSource() {
        assertFalse(new YamlSingleRuleConfigurationSwapper().swapToObject(new YamlSingleRuleConfiguration()).getDefaultDataSource().isPresent());
    }
    
    @Test
    void assertSwapToYaml() {
        assertThat(new YamlSingleRuleConfigurationSwapper().swapToYamlConfiguration(new SingleRuleConfiguration(Collections.emptyList(), "ds_0")).getDefaultDataSource(), is("ds_0"));
    }
    
    @Test
    void assertSwapToYamlWithoutDataSource() {
        assertNull(new YamlSingleRuleConfigurationSwapper().swapToYamlConfiguration(new SingleRuleConfiguration()).getDefaultDataSource());
    }
}
