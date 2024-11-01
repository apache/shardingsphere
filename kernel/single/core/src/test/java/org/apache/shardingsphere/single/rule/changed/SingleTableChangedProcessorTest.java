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

package org.apache.shardingsphere.single.rule.changed;

import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.event.dispatch.rule.alter.AlterNamedRuleItemEvent;
import org.apache.shardingsphere.mode.event.dispatch.rule.drop.DropRuleItemEvent;
import org.apache.shardingsphere.mode.spi.RuleItemConfigurationChangedProcessor;
import org.apache.shardingsphere.single.config.SingleRuleConfiguration;
import org.apache.shardingsphere.single.rule.SingleRule;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.LinkedList;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SingleTableChangedProcessorTest {
    
    @SuppressWarnings("unchecked")
    private final RuleItemConfigurationChangedProcessor<SingleRuleConfiguration, SingleRuleConfiguration> processor = TypedSPILoader.getService(
            RuleItemConfigurationChangedProcessor.class, "single.tables");
    
    @Test
    void assertSwapRuleItemConfiguration() {
        AlterNamedRuleItemEvent event = mock(AlterNamedRuleItemEvent.class);
        SingleRuleConfiguration actual = processor.swapRuleItemConfiguration(event, "- foo_tbl");
        assertThat(actual.getTables(), is(Collections.singleton("foo_tbl")));
    }
    
    @Test
    void assertFindRuleConfiguration() {
        SingleRuleConfiguration ruleConfig = mock(SingleRuleConfiguration.class);
        assertThat(processor.findRuleConfiguration(mockDatabase(ruleConfig)), is(ruleConfig));
    }
    
    private ShardingSphereDatabase mockDatabase(final SingleRuleConfiguration ruleConfig) {
        SingleRule rule = mock(SingleRule.class);
        when(rule.getConfiguration()).thenReturn(ruleConfig);
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class);
        when(result.getRuleMetaData()).thenReturn(new RuleMetaData(Collections.singleton(rule)));
        return result;
    }
    
    @Test
    void assertChangeRuleItemConfiguration() {
        AlterNamedRuleItemEvent event = mock(AlterNamedRuleItemEvent.class);
        SingleRuleConfiguration currentRuleConfig = new SingleRuleConfiguration(new LinkedList<>(Collections.singleton("foo_tbl")), null);
        SingleRuleConfiguration toBeChangedItemConfig = new SingleRuleConfiguration(new LinkedList<>(Collections.singleton("bar_tbl")), null);
        processor.changeRuleItemConfiguration(event, currentRuleConfig, toBeChangedItemConfig);
        assertThat(currentRuleConfig.getTables(), is(Collections.singletonList("bar_tbl")));
    }
    
    @Test
    void assertDropRuleItemConfiguration() {
        DropRuleItemEvent event = mock(DropRuleItemEvent.class);
        SingleRuleConfiguration currentRuleConfig = new SingleRuleConfiguration(new LinkedList<>(Collections.singleton("foo_tbl")), null);
        processor.dropRuleItemConfiguration(event, currentRuleConfig);
        assertTrue(currentRuleConfig.getTables().isEmpty());
    }
}
