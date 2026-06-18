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

package org.apache.shardingsphere.sharding.rule.changed;

import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.mode.spi.rule.RuleChangedItemType;
import org.apache.shardingsphere.mode.spi.rule.RuleItemConfigurationChangedProcessor;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.infra.config.keygen.impl.ColumnKeyGenerateStrategiesRuleConfiguration;
import org.apache.shardingsphere.infra.config.keygen.KeyGenerateStrategiesConfiguration;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.yaml.config.strategy.keygen.YamlKeyGenerateStrategyRuleConfiguration;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class KeyGenerateStrategyRuleChangedProcessorTest {
    
    @SuppressWarnings("unchecked")
    private final RuleItemConfigurationChangedProcessor<ShardingRuleConfiguration, KeyGenerateStrategiesConfiguration> processor = TypedSPILoader.getService(
            RuleItemConfigurationChangedProcessor.class, new RuleChangedItemType("sharding", "key_generate_strategies"));
    
    @Test
    void assertSwapRuleItemConfiguration() {
        KeyGenerateStrategiesConfiguration actual = processor.swapRuleItemConfiguration(null, createYAMLContent());
        assertThat(actual, CoreMatchers.isA(KeyGenerateStrategiesConfiguration.class));
        assertThat(actual.getKeyGeneratorName(), is("foo_algo"));
        assertThat(((ColumnKeyGenerateStrategiesRuleConfiguration) actual).getLogicTable(), is("foo_tbl"));
        assertThat(((ColumnKeyGenerateStrategiesRuleConfiguration) actual).getKeyGenerateColumn(), is("foo_col"));
    }
    
    private String createYAMLContent() {
        YamlKeyGenerateStrategyRuleConfiguration yamlConfig = new YamlKeyGenerateStrategyRuleConfiguration();
        yamlConfig.setKeyGenerateType("column");
        yamlConfig.setKeyGeneratorName("foo_algo");
        yamlConfig.setLogicTable("foo_tbl");
        yamlConfig.setKeyGenerateColumn("foo_col");
        return YamlEngine.marshal(yamlConfig);
    }
    
    @Test
    void assertFindRuleConfiguration() {
        ShardingRuleConfiguration ruleConfig = mock(ShardingRuleConfiguration.class);
        assertThat(processor.findRuleConfiguration(mockDatabase(ruleConfig)), is(ruleConfig));
    }
    
    private ShardingSphereDatabase mockDatabase(final ShardingRuleConfiguration ruleConfig) {
        ShardingRule rule = mock(ShardingRule.class);
        when(rule.getConfiguration()).thenReturn(ruleConfig);
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class);
        when(result.getRuleMetaData()).thenReturn(new RuleMetaData(Collections.singleton(rule)));
        return result;
    }
    
    @Test
    void assertChangeRuleItemConfiguration() {
        ColumnKeyGenerateStrategiesRuleConfiguration toBeChangedItemConfig = new ColumnKeyGenerateStrategiesRuleConfiguration("foo_algo", "foo_tbl", "foo_col");
        ShardingRuleConfiguration currentRuleConfig = new ShardingRuleConfiguration();
        processor.changeRuleItemConfiguration("foo_strategy", currentRuleConfig, toBeChangedItemConfig);
        assertThat(currentRuleConfig.getKeyGenerateStrategies().size(), is(1));
        assertThat(((ColumnKeyGenerateStrategiesRuleConfiguration) currentRuleConfig.getKeyGenerateStrategies().get("foo_strategy")).getLogicTable(), is("foo_tbl"));
    }
    
    @Test
    void assertDropRuleItemConfiguration() {
        ShardingRuleConfiguration currentRuleConfig = new ShardingRuleConfiguration();
        currentRuleConfig.getKeyGenerateStrategies().put("foo_strategy", new ColumnKeyGenerateStrategiesRuleConfiguration("foo_algo", "foo_tbl", "foo_col"));
        processor.dropRuleItemConfiguration("foo_strategy", currentRuleConfig);
        assertTrue(currentRuleConfig.getKeyGenerateStrategies().isEmpty());
    }
}
