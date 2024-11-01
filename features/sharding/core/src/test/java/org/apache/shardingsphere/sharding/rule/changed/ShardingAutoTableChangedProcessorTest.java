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
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.yaml.config.rule.YamlShardingAutoTableRuleConfiguration;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;

import static org.apache.shardingsphere.test.matcher.ShardingSphereAssertionMatchers.deepEqual;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShardingAutoTableChangedProcessorTest {
    
    @SuppressWarnings("unchecked")
    private final RuleItemConfigurationChangedProcessor<ShardingRuleConfiguration, ShardingAutoTableRuleConfiguration> processor = TypedSPILoader.getService(
            RuleItemConfigurationChangedProcessor.class, "sharding.auto_tables");
    
    @Test
    void assertSwapRuleItemConfiguration() {
        AlterNamedRuleItemEvent event = mock(AlterNamedRuleItemEvent.class);
        ShardingAutoTableRuleConfiguration actual = processor.swapRuleItemConfiguration(event, createYAMLContent());
        assertThat(actual, deepEqual(new ShardingAutoTableRuleConfiguration("foo_tbl", "foo_ds")));
    }
    
    private String createYAMLContent() {
        YamlShardingAutoTableRuleConfiguration yamlConfig = new YamlShardingAutoTableRuleConfiguration();
        yamlConfig.setLogicTable("foo_tbl");
        yamlConfig.setActualDataSources("foo_ds");
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
        when(event.getItemName()).thenReturn("foo_tbl");
        ShardingRuleConfiguration currentRuleConfig = createCurrentRuleConfiguration();
        ShardingAutoTableRuleConfiguration toBeChangedItemConfig = new ShardingAutoTableRuleConfiguration("foo_tbl", "bar_ds");
        processor.changeRuleItemConfiguration(event, currentRuleConfig, toBeChangedItemConfig);
        assertThat(currentRuleConfig.getAutoTables().size(), is(1));
        assertThat(new ArrayList<>(currentRuleConfig.getAutoTables()).get(0).getActualDataSources(), is("bar_ds"));
    }
    
    @Test
    void assertDropRuleItemConfiguration() {
        DropNamedRuleItemEvent event = mock(DropNamedRuleItemEvent.class);
        when(event.getItemName()).thenReturn("foo_tbl");
        ShardingRuleConfiguration currentRuleConfig = createCurrentRuleConfiguration();
        processor.dropRuleItemConfiguration(event, currentRuleConfig);
        assertTrue(currentRuleConfig.getAutoTables().isEmpty());
    }
    
    private ShardingRuleConfiguration createCurrentRuleConfiguration() {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        result.getAutoTables().add(new ShardingAutoTableRuleConfiguration("foo_tbl", "foo_ds"));
        return result;
    }
}
