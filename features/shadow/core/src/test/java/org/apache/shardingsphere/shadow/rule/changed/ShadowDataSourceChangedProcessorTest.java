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

package org.apache.shardingsphere.shadow.rule.changed;

import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.mode.spi.rule.RuleChangedItemType;
import org.apache.shardingsphere.mode.spi.rule.RuleItemConfigurationChangedProcessor;
import org.apache.shardingsphere.shadow.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.config.datasource.ShadowDataSourceConfiguration;
import org.apache.shardingsphere.shadow.rule.ShadowRule;
import org.apache.shardingsphere.shadow.yaml.config.datasource.YamlShadowDataSourceConfiguration;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;

import static org.apache.shardingsphere.test.infra.framework.matcher.ShardingSphereAssertionMatchers.deepEqual;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShadowDataSourceChangedProcessorTest {
    
    @SuppressWarnings("unchecked")
    private final RuleItemConfigurationChangedProcessor<ShadowRuleConfiguration, ShadowDataSourceConfiguration> processor = TypedSPILoader.getService(
            RuleItemConfigurationChangedProcessor.class, new RuleChangedItemType("shadow", "data_sources"));
    
    @Test
    void assertSwapRuleItemConfiguration() {
        ShadowDataSourceConfiguration actual = processor.swapRuleItemConfiguration("foo_ds", createYAMLContent());
        assertThat(actual, deepEqual(new ShadowDataSourceConfiguration("foo_ds", "prod_ds", "shadow_ds")));
    }
    
    private String createYAMLContent() {
        YamlShadowDataSourceConfiguration yamlConfig = new YamlShadowDataSourceConfiguration();
        yamlConfig.setProductionDataSourceName("prod_ds");
        yamlConfig.setShadowDataSourceName("shadow_ds");
        return YamlEngine.marshal(yamlConfig);
    }
    
    @Test
    void assertFindRuleConfiguration() {
        ShadowRuleConfiguration ruleConfig = mock(ShadowRuleConfiguration.class);
        assertThat(processor.findRuleConfiguration(mockDatabase(ruleConfig)), is(ruleConfig));
    }
    
    private ShardingSphereDatabase mockDatabase(final ShadowRuleConfiguration ruleConfig) {
        ShadowRule rule = mock(ShadowRule.class);
        when(rule.getConfiguration()).thenReturn(ruleConfig);
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class);
        when(result.getRuleMetaData()).thenReturn(new RuleMetaData(Collections.singleton(rule)));
        return result;
    }
    
    @Test
    void assertChangeRuleItemConfiguration() {
        ShadowRuleConfiguration currentRuleConfig = createCurrentRuleConfiguration();
        ShadowDataSourceConfiguration toBeChangedItemConfig = new ShadowDataSourceConfiguration("foo_ds", "new_prod_ds", "new_shadow_ds");
        processor.changeRuleItemConfiguration(null, currentRuleConfig, toBeChangedItemConfig);
        assertThat(currentRuleConfig.getDataSources().size(), is(1));
        assertThat(new ArrayList<>(currentRuleConfig.getDataSources()).get(0).getProductionDataSourceName(), is("new_prod_ds"));
        assertThat(new ArrayList<>(currentRuleConfig.getDataSources()).get(0).getShadowDataSourceName(), is("new_shadow_ds"));
    }
    
    @Test
    void assertDropRuleItemConfiguration() {
        ShadowRuleConfiguration currentRuleConfig = createCurrentRuleConfiguration();
        processor.dropRuleItemConfiguration("foo_tbl", currentRuleConfig);
        assertTrue(currentRuleConfig.getTables().isEmpty());
    }
    
    private ShadowRuleConfiguration createCurrentRuleConfiguration() {
        ShadowRuleConfiguration result = new ShadowRuleConfiguration();
        result.getDataSources().add(new ShadowDataSourceConfiguration("foo_ds", "prod_ds", "shadow_ds"));
        return result;
    }
}
