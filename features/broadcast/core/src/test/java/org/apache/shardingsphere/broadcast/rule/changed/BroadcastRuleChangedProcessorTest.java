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

package org.apache.shardingsphere.broadcast.rule.changed;

import org.apache.shardingsphere.broadcast.config.BroadcastRuleConfiguration;
import org.apache.shardingsphere.broadcast.rule.BroadcastRule;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.spi.rule.RuleChangedItemType;
import org.apache.shardingsphere.mode.spi.rule.RuleItemConfigurationChangedProcessor;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.LinkedList;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BroadcastRuleChangedProcessorTest {
    
    private final BroadcastRuleChangedProcessor processor = (BroadcastRuleChangedProcessor) TypedSPILoader.getService(
            RuleItemConfigurationChangedProcessor.class, new RuleChangedItemType("broadcast", null));
    
    @Test
    void assertSwapRuleItemConfiguration() {
        assertThat(processor.swapRuleItemConfiguration("unused", "- foo_tbl").getTables(), is(Collections.singleton("foo_tbl")));
    }
    
    @Test
    void assertFindRuleConfigurationWhenRuleExists() {
        BroadcastRuleConfiguration ruleConfig = mock(BroadcastRuleConfiguration.class);
        assertThat(processor.findRuleConfiguration(mockDatabaseWithRule(ruleConfig)), is(ruleConfig));
    }
    
    private ShardingSphereDatabase mockDatabaseWithRule(final BroadcastRuleConfiguration ruleConfig) {
        BroadcastRule rule = mock(BroadcastRule.class);
        when(rule.getConfiguration()).thenReturn(ruleConfig);
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class);
        when(result.getRuleMetaData()).thenReturn(new RuleMetaData(Collections.singleton(rule)));
        return result;
    }
    
    @Test
    void assertFindRuleConfigurationWhenRuleAbsent() {
        assertTrue(processor.findRuleConfiguration(mockDatabaseWithoutRule()).getTables().isEmpty());
    }
    
    private ShardingSphereDatabase mockDatabaseWithoutRule() {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class);
        when(result.getRuleMetaData()).thenReturn(new RuleMetaData(Collections.emptyList()));
        return result;
    }
    
    @Test
    void assertChangeRuleItemConfiguration() {
        BroadcastRuleConfiguration currentRuleConfig = new BroadcastRuleConfiguration(new LinkedList<>(Collections.singleton("foo_tbl")));
        BroadcastRuleConfiguration toBeChangedItemConfig = new BroadcastRuleConfiguration(new LinkedList<>(Collections.singleton("bar_tbl")));
        processor.changeRuleItemConfiguration("unused", currentRuleConfig, toBeChangedItemConfig);
        assertThat(currentRuleConfig.getTables(), is(Collections.singletonList("bar_tbl")));
    }
    
    @Test
    void assertDropRuleItemConfiguration() {
        BroadcastRuleConfiguration currentRuleConfig = new BroadcastRuleConfiguration(new LinkedList<>(Collections.singleton("foo_tbl")));
        processor.dropRuleItemConfiguration("unused", currentRuleConfig);
        assertTrue(currentRuleConfig.getTables().isEmpty());
    }
}
