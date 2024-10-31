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
import org.apache.shardingsphere.mode.event.dispatch.rule.alter.AlterNamedRuleItemEvent;
import org.apache.shardingsphere.mode.event.dispatch.rule.drop.DropNamedRuleItemEvent;
import org.apache.shardingsphere.mode.spi.RuleItemConfigurationChangedProcessor;
import org.apache.shardingsphere.readwritesplitting.config.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.config.rule.ReadwriteSplittingDataSourceGroupRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.rule.ReadwriteSplittingRule;
import org.apache.shardingsphere.readwritesplitting.transaction.TransactionalReadQueryStrategy;
import org.apache.shardingsphere.readwritesplitting.yaml.config.rule.YamlReadwriteSplittingDataSourceGroupRuleConfiguration;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;

import static org.apache.shardingsphere.test.matcher.ShardingSphereAssertionMatchers.deepEqual;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ReadwriteSplittingDataSourceChangedProcessorTest {
    
    @SuppressWarnings("unchecked")
    private final RuleItemConfigurationChangedProcessor<ReadwriteSplittingRuleConfiguration, ReadwriteSplittingDataSourceGroupRuleConfiguration> processor = TypedSPILoader.getService(
            RuleItemConfigurationChangedProcessor.class, "readwrite_splitting.data_source_groups");
    
    @Test
    void assertSwapRuleItemConfigurationWithoutTransactionalReadQueryStrategy() {
        ReadwriteSplittingDataSourceGroupRuleConfiguration actual = processor.swapRuleItemConfiguration(new AlterNamedRuleItemEvent("", "foo", "", "", ""), createYAMLContent(null));
        assertThat(actual, deepEqual(new ReadwriteSplittingDataSourceGroupRuleConfiguration("foo", "write_ds", Collections.singletonList("read_ds"), "FIXTURE_BALANCER")));
    }
    
    @Test
    void assertSwapRuleItemConfigurationWithTransactionalReadQueryStrategy() {
        ReadwriteSplittingDataSourceGroupRuleConfiguration actual = processor.swapRuleItemConfiguration(
                new AlterNamedRuleItemEvent("", "foo", "", "", ""), createYAMLContent(TransactionalReadQueryStrategy.PRIMARY));
        assertThat(actual, deepEqual(new ReadwriteSplittingDataSourceGroupRuleConfiguration(
                "foo", "write_ds", Collections.singletonList("read_ds"), TransactionalReadQueryStrategy.PRIMARY, "FIXTURE_BALANCER")));
    }
    
    private String createYAMLContent(final TransactionalReadQueryStrategy strategy) {
        YamlReadwriteSplittingDataSourceGroupRuleConfiguration yamlConfig = new YamlReadwriteSplittingDataSourceGroupRuleConfiguration();
        yamlConfig.setWriteDataSourceName("write_ds");
        yamlConfig.setReadDataSourceNames(Collections.singletonList("read_ds"));
        yamlConfig.setTransactionalReadQueryStrategy(null == strategy ? null : strategy.name());
        yamlConfig.setLoadBalancerName("FIXTURE_BALANCER");
        return YamlEngine.marshal(yamlConfig);
    }
    
    @Test
    void assertFindRuleConfiguration() {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class);
        ReadwriteSplittingRule rule = mock(ReadwriteSplittingRule.class);
        ReadwriteSplittingRuleConfiguration ruleConfig = mock(ReadwriteSplittingRuleConfiguration.class);
        when(rule.getConfiguration()).thenReturn(ruleConfig);
        when(database.getRuleMetaData()).thenReturn(new RuleMetaData(Collections.singleton(rule)));
        assertThat(processor.findRuleConfiguration(database), is(ruleConfig));
    }

    @Test
    void assertChangeRuleItemConfiguration() {
        ReadwriteSplittingRuleConfiguration currentRuleConfig = new ReadwriteSplittingRuleConfiguration(
                new LinkedList<>(Collections.singleton(new ReadwriteSplittingDataSourceGroupRuleConfiguration("foo", "write_ds", Collections.singletonList("read_ds"), "FIXTURE_BALANCER"))),
                Collections.emptyMap());
        ReadwriteSplittingDataSourceGroupRuleConfiguration toBeChangedItemConfig = new ReadwriteSplittingDataSourceGroupRuleConfiguration(
                "foo", "write_ds", Collections.singletonList("read_ds"), TransactionalReadQueryStrategy.FIXED, "FIXTURE_BALANCER");
        processor.changeRuleItemConfiguration(mock(AlterNamedRuleItemEvent.class), currentRuleConfig, toBeChangedItemConfig);
        assertThat(new ArrayList<>(currentRuleConfig.getDataSourceGroups()).get(0).getTransactionalReadQueryStrategy(), is(TransactionalReadQueryStrategy.FIXED));
    }

    @Test
    void assertDropRuleItemConfiguration() {
        ReadwriteSplittingRuleConfiguration currentRuleConfig = new ReadwriteSplittingRuleConfiguration(
                new LinkedList<>(Collections.singleton(new ReadwriteSplittingDataSourceGroupRuleConfiguration("foo", "write_ds", Collections.singletonList("read_ds"), "FIXTURE_BALANCER"))),
                Collections.emptyMap());
        processor.dropRuleItemConfiguration(new DropNamedRuleItemEvent("", "foo", ""), currentRuleConfig);
        assertTrue(currentRuleConfig.getDataSourceGroups().isEmpty());
    }
}
