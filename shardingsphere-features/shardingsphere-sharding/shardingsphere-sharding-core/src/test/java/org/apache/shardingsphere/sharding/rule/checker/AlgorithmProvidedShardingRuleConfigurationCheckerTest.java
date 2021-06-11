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

package org.apache.shardingsphere.sharding.rule.checker;

import org.apache.shardingsphere.infra.rule.checker.RuleConfigurationChecker;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.ordered.OrderedSPIRegistry;
import org.apache.shardingsphere.sharding.algorithm.config.AlgorithmProvidedShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ShardingStrategyConfiguration;
import org.junit.Test;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class AlgorithmProvidedShardingRuleConfigurationCheckerTest {
    
    static {
        ShardingSphereServiceLoader.register(RuleConfigurationChecker.class);
    }
    
    @Test
    public void assertCheckPass() {
        ShardingStrategyConfiguration strategyConfiguration = mock(ShardingStrategyConfiguration.class);
        ShardingTableRuleConfiguration ruleConfiguration = mock(ShardingTableRuleConfiguration.class);
        ShardingAutoTableRuleConfiguration autoTableRuleConfiguration = mock(ShardingAutoTableRuleConfiguration.class);
        AlgorithmProvidedShardingRuleConfiguration ruleConfig = mock(AlgorithmProvidedShardingRuleConfiguration.class);
        when(ruleConfig.getTables()).thenReturn(Collections.singleton(ruleConfiguration));
        when(ruleConfig.getAutoTables()).thenReturn(Collections.singleton(autoTableRuleConfiguration));
        when(ruleConfig.getDefaultTableShardingStrategy()).thenReturn(strategyConfiguration);
        RuleConfigurationChecker checker = OrderedSPIRegistry.getRegisteredServices(Collections.singleton(ruleConfig), RuleConfigurationChecker.class).get(ruleConfig);
        assertNotNull(checker);
        assertThat(checker, instanceOf(AlgorithmProvidedShardingRuleConfigurationChecker.class));
        checker.check("test", ruleConfig);
    }
    
    @Test(expected = IllegalStateException.class)
    public void assertCheckNoPass() {
        AlgorithmProvidedShardingRuleConfiguration ruleConfig = mock(AlgorithmProvidedShardingRuleConfiguration.class);
        when(ruleConfig.getTables()).thenReturn(Collections.emptyList());
        when(ruleConfig.getAutoTables()).thenReturn(Collections.emptyList());
        when(ruleConfig.getDefaultTableShardingStrategy()).thenReturn(null);
        RuleConfigurationChecker checker = OrderedSPIRegistry.getRegisteredServices(Collections.singleton(ruleConfig), RuleConfigurationChecker.class).get(ruleConfig);
        assertNotNull(checker);
        assertThat(checker, instanceOf(AlgorithmProvidedShardingRuleConfigurationChecker.class));
        checker.check("test", ruleConfig);
    }
}
