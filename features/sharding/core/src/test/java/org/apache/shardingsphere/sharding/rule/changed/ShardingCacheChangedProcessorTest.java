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
import org.apache.shardingsphere.sharding.api.config.cache.ShardingCacheConfiguration;
import org.apache.shardingsphere.sharding.api.config.cache.ShardingCacheOptionsConfiguration;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.yaml.config.cache.YamlShardingCacheConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.cache.YamlShardingCacheOptionsConfiguration;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.apache.shardingsphere.test.infra.framework.matcher.ShardingSphereAssertionMatchers.deepEqual;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShardingCacheChangedProcessorTest {
    
    @SuppressWarnings("unchecked")
    private final RuleItemConfigurationChangedProcessor<ShardingRuleConfiguration, ShardingCacheConfiguration> processor = TypedSPILoader.getService(
            RuleItemConfigurationChangedProcessor.class, new RuleChangedItemType("sharding", "sharding_cache"));
    
    @Test
    void assertSwapRuleItemConfiguration() {
        ShardingCacheConfiguration actual = processor.swapRuleItemConfiguration(null, createYAMLContent());
        assertThat(actual, deepEqual(new ShardingCacheConfiguration(1, new ShardingCacheOptionsConfiguration(true, 128, 1024))));
    }
    
    private String createYAMLContent() {
        YamlShardingCacheConfiguration yamlConfig = new YamlShardingCacheConfiguration();
        yamlConfig.setAllowedMaxSqlLength(1);
        YamlShardingCacheOptionsConfiguration yamlCacheOptionsConfig = new YamlShardingCacheOptionsConfiguration();
        yamlCacheOptionsConfig.setSoftValues(true);
        yamlCacheOptionsConfig.setInitialCapacity(128);
        yamlCacheOptionsConfig.setMaximumSize(1024);
        yamlConfig.setRouteCache(yamlCacheOptionsConfig);
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
        ShardingRuleConfiguration currentRuleConfig = createCurrentRuleConfiguration();
        ShardingCacheConfiguration toBeChangedItemConfig = new ShardingCacheConfiguration(2, new ShardingCacheOptionsConfiguration(false, 1280, 10240));
        processor.changeRuleItemConfiguration(null, currentRuleConfig, toBeChangedItemConfig);
        assertThat(currentRuleConfig.getShardingCache().getAllowedMaxSqlLength(), is(2));
        assertFalse(currentRuleConfig.getShardingCache().getRouteCache().isSoftValues());
        assertThat(currentRuleConfig.getShardingCache().getRouteCache().getInitialCapacity(), is(1280));
        assertThat(currentRuleConfig.getShardingCache().getRouteCache().getMaximumSize(), is(10240));
    }
    
    @Test
    void assertDropRuleItemConfiguration() {
        ShardingRuleConfiguration currentRuleConfig = createCurrentRuleConfiguration();
        processor.dropRuleItemConfiguration(null, currentRuleConfig);
        assertNull(currentRuleConfig.getShardingCache());
    }
    
    private ShardingRuleConfiguration createCurrentRuleConfiguration() {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        result.setShardingCache(new ShardingCacheConfiguration(1, new ShardingCacheOptionsConfiguration(true, 128, 1024)));
        return result;
    }
}
