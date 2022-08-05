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
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class YamlShardingAutoTableRuleConfigurationSwapperTest {
    
    private YamlShardingAutoTableRuleConfigurationSwapper swapper;
    
    @Before
    public void setUp() {
        ShardingAutoTableAlgorithm shardingAlgorithm = mock(ShardingAutoTableAlgorithm.class);
        when(shardingAlgorithm.getAutoTablesAmount()).thenReturn(2);
        swapper = new YamlShardingAutoTableRuleConfigurationSwapper(Collections.singletonMap("foo_algorithm", shardingAlgorithm), Collections.emptyMap());
    }
    
    @Test
    public void assertSwapToYamlWithMinProperties() {
        YamlShardingAutoTableRuleConfiguration actual = swapper.swapToYamlConfiguration(new ShardingAutoTableRuleConfiguration("tbl", "ds0,ds1"));
        assertThat(actual.getLogicTable(), is("tbl"));
        assertThat(actual.getActualDataSources(), is("ds0,ds1"));
        assertNull(actual.getShardingStrategy());
        assertNull(actual.getKeyGenerateStrategy());
    }
    
    @Test
    public void assertSwapToYamlWithMaxProperties() {
        ShardingAutoTableRuleConfiguration shardingTableRuleConfig = new ShardingAutoTableRuleConfiguration("tbl", "ds0,ds1");
        shardingTableRuleConfig.setActualTablePrefix("tmp_");
        shardingTableRuleConfig.setShardingStrategy(mock(StandardShardingStrategyConfiguration.class));
        shardingTableRuleConfig.setKeyGenerateStrategy(mock(KeyGenerateStrategyConfiguration.class));
        shardingTableRuleConfig.setActualDataNodes("ds0.tbl_0");
        YamlShardingAutoTableRuleConfiguration actual = swapper.swapToYamlConfiguration(shardingTableRuleConfig);
        assertThat(actual.getLogicTable(), is("tbl"));
        assertThat(actual.getActualDataSources(), is("ds0,ds1"));
        assertThat(actual.getActualTablePrefix(), is("tmp_"));
        assertThat(actual.getActualDataNodes(), is("ds0.tbl_0"));
        assertNotNull(actual.getShardingStrategy());
        assertNotNull(actual.getKeyGenerateStrategy());
    }
    
    @Test
    public void assertSwapToYamlAutoAddActualDataNodes() {
        ShardingAutoTableRuleConfiguration shardingTableRuleConfig = new ShardingAutoTableRuleConfiguration("tbl", "ds0,ds1");
        shardingTableRuleConfig.setActualTablePrefix("tmp_");
        StandardShardingStrategyConfiguration strategyConfiguration = mock(StandardShardingStrategyConfiguration.class);
        when(strategyConfiguration.getShardingAlgorithmName()).thenReturn("foo_algorithm");
        shardingTableRuleConfig.setShardingStrategy(strategyConfiguration);
        shardingTableRuleConfig.setKeyGenerateStrategy(mock(KeyGenerateStrategyConfiguration.class));
        YamlShardingAutoTableRuleConfiguration actual = swapper.swapToYamlConfiguration(shardingTableRuleConfig);
        assertThat(actual.getLogicTable(), is("tbl"));
        assertThat(actual.getActualDataSources(), is("ds0,ds1"));
        assertThat(actual.getActualTablePrefix(), is("tmp_"));
        assertThat(actual.getActualDataNodes(), is("ds0.tbl_0,ds1.tbl_1"));
        assertNotNull(actual.getShardingStrategy());
        assertNotNull(actual.getKeyGenerateStrategy());
    }
    
    @Test(expected = NullPointerException.class)
    public void assertSwapToObjectWithoutLogicTable() {
        new YamlShardingAutoTableRuleConfigurationSwapper(Collections.emptyMap(), Collections.emptyMap()).swapToObject(new YamlShardingAutoTableRuleConfiguration());
    }
}
