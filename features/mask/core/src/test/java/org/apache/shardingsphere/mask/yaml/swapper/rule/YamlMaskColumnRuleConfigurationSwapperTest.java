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

package org.apache.shardingsphere.mask.yaml.swapper.rule;

import org.apache.shardingsphere.mask.api.config.rule.MaskColumnRuleConfiguration;
import org.apache.shardingsphere.mask.yaml.config.rule.YamlMaskColumnRuleConfiguration;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class YamlMaskColumnRuleConfigurationSwapperTest {
    
    @Test
    void assertSwapToYamlConfiguration() {
        MaskColumnRuleConfiguration encryptColumnRuleConfig = new MaskColumnRuleConfiguration("logicColumn", "md5_mask");
        YamlMaskColumnRuleConfiguration actual = new YamlMaskColumnRuleConfigurationSwapper().swapToYamlConfiguration(encryptColumnRuleConfig);
        assertThat(actual.getLogicColumn(), is("logicColumn"));
        assertThat(actual.getMaskAlgorithm(), is("md5_mask"));
    }
    
    @Test
    void assertSwapToObject() {
        YamlMaskColumnRuleConfiguration yamlMaskColumnRuleConfig = new YamlMaskColumnRuleConfiguration();
        yamlMaskColumnRuleConfig.setLogicColumn("logicColumn");
        yamlMaskColumnRuleConfig.setMaskAlgorithm("md5_mask");
        MaskColumnRuleConfiguration actual = new YamlMaskColumnRuleConfigurationSwapper().swapToObject(yamlMaskColumnRuleConfig);
        assertThat(actual.getLogicColumn(), is("logicColumn"));
        assertThat(actual.getMaskAlgorithm(), is("md5_mask"));
    }
}
