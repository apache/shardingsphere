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

package org.apache.shardingsphere.sharding.yaml.swapper.rule;

import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.keygen.KeyGenerateStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.sharding.ShardingAutoTableAlgorithm;
import org.apache.shardingsphere.sharding.yaml.config.rule.YamlShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.strategy.keygen.YamlKeyGenerateStrategyConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.strategy.sharding.YamlShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.strategy.sharding.YamlStandardShardingStrategyConfiguration;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class YamlShardingAutoTableRuleConfigurationSwapperTest {
    
    private YamlShardingAutoTableRuleConfigurationSwapper swapper;
    
    @Before
    public void setUp() {
        ShardingAutoTableAlgorithm shardingAlgorithm = mock(ShardingAutoTableAlgorithm.class);
        when(shardingAlgorithm.getAutoTablesAmount()).thenReturn(2);
        swapper = new YamlShardingAutoTableRuleConfigurationSwapper();
    }
    
    @Test
    public void assertSwapToYamlConfigurationWithMinProperties() {
        YamlShardingAutoTableRuleConfiguration actual = swapper.swapToYamlConfiguration(new ShardingAutoTableRuleConfiguration("tbl", "ds0,ds1"));
        assertThat(actual.getLogicTable(), is("tbl"));
        assertThat(actual.getActualDataSources(), is("ds0,ds1"));
        assertNull(actual.getShardingStrategy());
        assertNull(actual.getKeyGenerateStrategy());
    }
    
    @Test
    public void assertSwapToYamlConfigurationWithMaxProperties() {
        ShardingAutoTableRuleConfiguration shardingTableRuleConfig = new ShardingAutoTableRuleConfiguration("tbl", "ds0,ds1");
        shardingTableRuleConfig.setShardingStrategy(mock(StandardShardingStrategyConfiguration.class));
        shardingTableRuleConfig.setKeyGenerateStrategy(mock(KeyGenerateStrategyConfiguration.class));
        YamlShardingAutoTableRuleConfiguration actual = swapper.swapToYamlConfiguration(shardingTableRuleConfig);
        assertThat(actual.getLogicTable(), is("tbl"));
        assertThat(actual.getActualDataSources(), is("ds0,ds1"));
        assertNotNull(actual.getShardingStrategy());
        assertNotNull(actual.getKeyGenerateStrategy());
    }
    
    @Test(expected = NullPointerException.class)
    public void assertSwapToObjectWithoutLogicTable() {
        new YamlShardingAutoTableRuleConfigurationSwapper().swapToObject(new YamlShardingAutoTableRuleConfiguration());
    }
    
    @Test
    public void assertSwapToObject() {
        YamlShardingStrategyConfiguration yamlShardingStrategyConfig = new YamlShardingStrategyConfiguration();
        YamlStandardShardingStrategyConfiguration yamlStandardShardingStrategyConfig = new YamlStandardShardingStrategyConfiguration();
        yamlStandardShardingStrategyConfig.setShardingColumn("col");
        yamlStandardShardingStrategyConfig.setShardingAlgorithmName("foo_algorithm");
        yamlShardingStrategyConfig.setStandard(yamlStandardShardingStrategyConfig);
        YamlShardingAutoTableRuleConfiguration yamlShardingAutoTableRuleConfig = swapper.swapToYamlConfiguration(new ShardingAutoTableRuleConfiguration("tbl", "ds0,ds1"));
        yamlShardingAutoTableRuleConfig.setShardingStrategy(yamlShardingStrategyConfig);
        YamlKeyGenerateStrategyConfiguration keyGenerateStrategy = new YamlKeyGenerateStrategyConfiguration();
        keyGenerateStrategy.setColumn("col");
        yamlShardingAutoTableRuleConfig.setKeyGenerateStrategy(keyGenerateStrategy);
        ShardingAutoTableRuleConfiguration actual = new YamlShardingAutoTableRuleConfigurationSwapper().swapToObject(yamlShardingAutoTableRuleConfig);
        assertThat(actual.getLogicTable(), is("tbl"));
        assertThat(actual.getActualDataSources(), is("ds0,ds1"));
        assertNotNull(actual.getShardingStrategy());
        assertNotNull(actual.getKeyGenerateStrategy());
    }
}
