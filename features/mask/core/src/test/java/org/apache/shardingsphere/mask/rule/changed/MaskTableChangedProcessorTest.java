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

package org.apache.shardingsphere.mask.rule.changed;

import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.mask.config.MaskRuleConfiguration;
import org.apache.shardingsphere.mask.config.rule.MaskColumnRuleConfiguration;
import org.apache.shardingsphere.mask.config.rule.MaskTableRuleConfiguration;
import org.apache.shardingsphere.mask.rule.MaskRule;
import org.apache.shardingsphere.mask.yaml.config.rule.YamlMaskColumnRuleConfiguration;
import org.apache.shardingsphere.mask.yaml.config.rule.YamlMaskTableRuleConfiguration;
import org.apache.shardingsphere.mode.spi.rule.RuleChangedItemType;
import org.apache.shardingsphere.mode.spi.rule.RuleItemConfigurationChangedProcessor;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;

import static org.apache.shardingsphere.test.infra.framework.matcher.ShardingSphereAssertionMatchers.deepEqual;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MaskTableChangedProcessorTest {
    
    @SuppressWarnings("unchecked")
    private final RuleItemConfigurationChangedProcessor<MaskRuleConfiguration, MaskTableRuleConfiguration> processor = TypedSPILoader.getService(
            RuleItemConfigurationChangedProcessor.class, new RuleChangedItemType("mask", "tables"));
    
    @Test
    void assertSwapRuleItemConfiguration() {
        MaskTableRuleConfiguration actual = processor.swapRuleItemConfiguration(null, createYAMLContent());
        assertThat(actual, deepEqual(new MaskTableRuleConfiguration("foo_tbl", Collections.singletonList(new MaskColumnRuleConfiguration("foo_col", "foo_algo")))));
    }
    
    private String createYAMLContent() {
        YamlMaskTableRuleConfiguration yamlConfig = new YamlMaskTableRuleConfiguration();
        yamlConfig.setName("foo_tbl");
        YamlMaskColumnRuleConfiguration yamlColumnRuleConfig = new YamlMaskColumnRuleConfiguration();
        yamlColumnRuleConfig.setLogicColumn("foo_col");
        yamlColumnRuleConfig.setMaskAlgorithm("foo_algo");
        yamlConfig.setColumns(Collections.singletonMap("foo_col", yamlColumnRuleConfig));
        return YamlEngine.marshal(yamlConfig);
    }
    
    @Test
    void assertFindRuleConfiguration() {
        MaskRuleConfiguration ruleConfig = mock(MaskRuleConfiguration.class);
        assertThat(processor.findRuleConfiguration(mockDatabase(ruleConfig)), is(ruleConfig));
    }
    
    private ShardingSphereDatabase mockDatabase(final MaskRuleConfiguration ruleConfig) {
        MaskRule rule = mock(MaskRule.class);
        when(rule.getConfiguration()).thenReturn(ruleConfig);
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class);
        when(result.getRuleMetaData()).thenReturn(new RuleMetaData(Collections.singleton(rule)));
        return result;
    }
    
    @Test
    void assertChangeRuleItemConfiguration() {
        MaskRuleConfiguration currentRuleConfig = createCurrentRuleConfiguration();
        MaskTableRuleConfiguration toBeChangedItemConfig = new MaskTableRuleConfiguration("foo_tbl", Collections.singleton(mock(MaskColumnRuleConfiguration.class)));
        processor.changeRuleItemConfiguration("foo_tbl", currentRuleConfig, toBeChangedItemConfig);
        assertThat(currentRuleConfig.getTables().size(), is(1));
        assertThat(new ArrayList<>(currentRuleConfig.getTables()).get(0).getColumns().size(), is(1));
    }
    
    @Test
    void assertDropRuleItemConfiguration() {
        MaskRuleConfiguration currentRuleConfig = createCurrentRuleConfiguration();
        processor.dropRuleItemConfiguration("foo_tbl", currentRuleConfig);
        assertTrue(currentRuleConfig.getTables().isEmpty());
    }
    
    private MaskRuleConfiguration createCurrentRuleConfiguration() {
        return new MaskRuleConfiguration(new LinkedList<>(Collections.singleton(new MaskTableRuleConfiguration("foo_tbl", Collections.emptyList()))), Collections.emptyMap());
    }
}
