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

package org.apache.shardingsphere.readwritesplitting.rule.changed;

import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.mode.spi.rule.RuleChangedItemType;
import org.apache.shardingsphere.mode.spi.rule.RuleItemConfigurationChangedProcessor;
import org.apache.shardingsphere.readwritesplitting.config.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.config.rule.ReadwriteSplittingDataSourceGroupRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.rule.ReadwriteSplittingRule;
import org.apache.shardingsphere.readwritesplitting.transaction.TransactionalReadQueryStrategy;
import org.apache.shardingsphere.readwritesplitting.yaml.config.rule.YamlReadwriteSplittingDataSourceGroupRuleConfiguration;
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

class ReadwriteSplittingDataSourceChangedProcessorTest {
    
    @SuppressWarnings("unchecked")
    private final RuleItemConfigurationChangedProcessor<ReadwriteSplittingRuleConfiguration, ReadwriteSplittingDataSourceGroupRuleConfiguration> processor = TypedSPILoader.getService(
            RuleItemConfigurationChangedProcessor.class, new RuleChangedItemType("readwrite_splitting", "data_source_groups"));
    
    @Test
    void assertSwapRuleItemConfigurationWithoutTransactionalReadQueryStrategy() {
        ReadwriteSplittingDataSourceGroupRuleConfiguration actual = processor.swapRuleItemConfiguration("foo", createYAMLContent(null));
        assertThat(actual, deepEqual(new ReadwriteSplittingDataSourceGroupRuleConfiguration("foo", "write_ds", Collections.singletonList("read_ds"), "foo_balancer")));
    }
    
    @Test
    void assertSwapRuleItemConfigurationWithTransactionalReadQueryStrategy() {
        ReadwriteSplittingDataSourceGroupRuleConfiguration actual = processor.swapRuleItemConfiguration("foo", createYAMLContent(TransactionalReadQueryStrategy.PRIMARY));
        assertThat(actual, deepEqual(new ReadwriteSplittingDataSourceGroupRuleConfiguration(
                "foo", "write_ds", Collections.singletonList("read_ds"), TransactionalReadQueryStrategy.PRIMARY, "foo_balancer")));
    }
    
    private String createYAMLContent(final TransactionalReadQueryStrategy strategy) {
        YamlReadwriteSplittingDataSourceGroupRuleConfiguration yamlConfig = new YamlReadwriteSplittingDataSourceGroupRuleConfiguration();
        yamlConfig.setWriteDataSourceName("write_ds");
        yamlConfig.setReadDataSourceNames(Collections.singletonList("read_ds"));
        yamlConfig.setTransactionalReadQueryStrategy(null == strategy ? null : strategy.name());
        yamlConfig.setLoadBalancerName("foo_balancer");
        return YamlEngine.marshal(yamlConfig);
    }
    
    @Test
    void assertFindRuleConfiguration() {
        ReadwriteSplittingRuleConfiguration ruleConfig = mock(ReadwriteSplittingRuleConfiguration.class);
        assertThat(processor.findRuleConfiguration(mockDatabase(ruleConfig)), is(ruleConfig));
    }
    
    private ShardingSphereDatabase mockDatabase(final ReadwriteSplittingRuleConfiguration ruleConfig) {
        ReadwriteSplittingRule rule = mock(ReadwriteSplittingRule.class);
        when(rule.getConfiguration()).thenReturn(ruleConfig);
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class);
        when(result.getRuleMetaData()).thenReturn(new RuleMetaData(Collections.singleton(rule)));
        return result;
    }
    
    @Test
    void assertChangeRuleItemConfiguration() {
        ReadwriteSplittingRuleConfiguration currentRuleConfig = createCurrentRuleConfiguration();
        ReadwriteSplittingDataSourceGroupRuleConfiguration toBeChangedItemConfig = new ReadwriteSplittingDataSourceGroupRuleConfiguration(
                "foo", "write_ds", Collections.singletonList("read_ds"), TransactionalReadQueryStrategy.FIXED, "foo_balancer");
        processor.changeRuleItemConfiguration("foo", currentRuleConfig, toBeChangedItemConfig);
        assertThat(new ArrayList<>(currentRuleConfig.getDataSourceGroups()).get(0).getTransactionalReadQueryStrategy(), is(TransactionalReadQueryStrategy.FIXED));
    }
    
    @Test
    void assertDropRuleItemConfiguration() {
        ReadwriteSplittingRuleConfiguration currentRuleConfig = createCurrentRuleConfiguration();
        processor.dropRuleItemConfiguration("foo", currentRuleConfig);
        assertTrue(currentRuleConfig.getDataSourceGroups().isEmpty());
    }
    
    private static ReadwriteSplittingRuleConfiguration createCurrentRuleConfiguration() {
        return new ReadwriteSplittingRuleConfiguration(
                new LinkedList<>(Collections.singleton(new ReadwriteSplittingDataSourceGroupRuleConfiguration("foo", "write_ds", Collections.singletonList("read_ds"), "foo_balancer"))),
                Collections.emptyMap());
    }
}
