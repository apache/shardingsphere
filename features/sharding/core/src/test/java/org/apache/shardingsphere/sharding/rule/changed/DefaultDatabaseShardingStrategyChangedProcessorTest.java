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
import org.apache.shardingsphere.mode.event.dispatch.rule.alter.AlterNamedRuleItemEvent;
import org.apache.shardingsphere.mode.event.dispatch.rule.drop.DropNamedRuleItemEvent;
import org.apache.shardingsphere.mode.spi.RuleItemConfigurationChangedProcessor;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.HintShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.NoneShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.yaml.config.strategy.sharding.YamlNoneShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.strategy.sharding.YamlShardingStrategyConfiguration;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.apache.shardingsphere.test.matcher.ShardingSphereAssertionMatchers.deepEqual;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DefaultDatabaseShardingStrategyChangedProcessorTest {
    
    @SuppressWarnings("unchecked")
    private final RuleItemConfigurationChangedProcessor<ShardingRuleConfiguration, ShardingStrategyConfiguration> processor = TypedSPILoader.getService(
            RuleItemConfigurationChangedProcessor.class, "sharding.default_database_strategy");
    
    @Test
    void assertSwapRuleItemConfiguration() {
        AlterNamedRuleItemEvent event = mock(AlterNamedRuleItemEvent.class);
        ShardingStrategyConfiguration actual = processor.swapRuleItemConfiguration(event, createYAMLContent());
        assertThat(actual, deepEqual(new NoneShardingStrategyConfiguration()));
    }
    
    private String createYAMLContent() {
        YamlShardingStrategyConfiguration yamlConfig = new YamlShardingStrategyConfiguration();
        yamlConfig.setNone(new YamlNoneShardingStrategyConfiguration());
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
        AlterNamedRuleItemEvent event = mock(AlterNamedRuleItemEvent.class);
        ShardingRuleConfiguration currentRuleConfig = createCurrentRuleConfiguration();
        ShardingStrategyConfiguration toBeChangedItemConfig = new HintShardingStrategyConfiguration("foo_algo");
        processor.changeRuleItemConfiguration(event, currentRuleConfig, toBeChangedItemConfig);
        assertThat(currentRuleConfig.getDefaultDatabaseShardingStrategy().getShardingAlgorithmName(), is("foo_algo"));
    }
    
    @Test
    void assertDropRuleItemConfiguration() {
        DropNamedRuleItemEvent event = mock(DropNamedRuleItemEvent.class);
        ShardingRuleConfiguration currentRuleConfig = createCurrentRuleConfiguration();
        processor.dropRuleItemConfiguration(event, currentRuleConfig);
        assertNull(currentRuleConfig.getDefaultDatabaseShardingStrategy());
    }
    
    private ShardingRuleConfiguration createCurrentRuleConfiguration() {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        result.setDefaultDatabaseShardingStrategy(new NoneShardingStrategyConfiguration());
        return result;
    }
}
