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

package org.apache.shardingsphere.sharding.checker;

import org.apache.shardingsphere.infra.config.checker.RuleConfigurationChecker;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ShardingStrategyConfiguration;
import org.apache.shardingsphere.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.spi.ordered.OrderedSPIRegistry;
import org.junit.Test;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ShardingRuleConfigurationCheckerTest {
    
    static {
        ShardingSphereServiceLoader.register(RuleConfigurationChecker.class);
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    public void assertValidCheck() {
        ShardingRuleConfiguration config = getValidConfiguration();
        RuleConfigurationChecker checker = OrderedSPIRegistry.getRegisteredServices(RuleConfigurationChecker.class, Collections.singleton(config)).get(config);
        assertThat(checker, instanceOf(ShardingRuleConfigurationChecker.class));
        checker.check("test", config);
    }
    
    private ShardingRuleConfiguration getValidConfiguration() {
        ShardingStrategyConfiguration strategyConfiguration = mock(ShardingStrategyConfiguration.class);
        ShardingTableRuleConfiguration ruleConfiguration = mock(ShardingTableRuleConfiguration.class);
        ShardingAutoTableRuleConfiguration autoTableRuleConfiguration = mock(ShardingAutoTableRuleConfiguration.class);
        ShardingRuleConfiguration result = mock(ShardingRuleConfiguration.class);
        when(result.getTables()).thenReturn(Collections.singleton(ruleConfiguration));
        when(result.getAutoTables()).thenReturn(Collections.singleton(autoTableRuleConfiguration));
        when(result.getDefaultTableShardingStrategy()).thenReturn(strategyConfiguration);
        return result;
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test(expected = IllegalStateException.class)
    public void assertInvalidCheck() {
        ShardingRuleConfiguration config = getInvalidConfiguration();
        RuleConfigurationChecker checker = OrderedSPIRegistry.getRegisteredServices(RuleConfigurationChecker.class, Collections.singleton(config)).get(config);
        assertThat(checker, instanceOf(ShardingRuleConfigurationChecker.class));
        checker.check("test", config);
    }
    
    private ShardingRuleConfiguration getInvalidConfiguration() {
        ShardingRuleConfiguration result = mock(ShardingRuleConfiguration.class);
        when(result.getTables()).thenReturn(Collections.emptyList());
        when(result.getAutoTables()).thenReturn(Collections.emptyList());
        return result;
    }
}
