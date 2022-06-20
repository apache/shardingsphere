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

package org.apache.shardingsphere.sharding.swapper.rule;

import org.apache.shardingsphere.infra.yaml.config.swapper.YamlConfigurationSwapper;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.keygen.KeyGenerateStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.sharding.ShardingAutoTableAlgorithm;
import org.apache.shardingsphere.sharding.spi.ShardingAlgorithm;
import org.apache.shardingsphere.sharding.yaml.config.rule.YamlShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.strategy.keygen.YamlKeyGenerateStrategyConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.strategy.sharding.YamlShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.yaml.swapper.rule.ShardingAutoTableRuleConfigurationYamlSwapper;
import org.apache.shardingsphere.sharding.yaml.swapper.strategy.KeyGenerateStrategyConfigurationYamlSwapper;
import org.apache.shardingsphere.sharding.yaml.swapper.strategy.ShardingStrategyConfigurationYamlSwapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

@RunWith(MockitoJUnitRunner.class)
public final class ShardingAutoTableRuleConfigurationYamlSwapperTest {
    
    @Mock
    private ShardingStrategyConfigurationYamlSwapper shardingStrategyYamlSwapper;
    
    @Mock
    private KeyGenerateStrategyConfigurationYamlSwapper keyGenerateStrategyYamlSwapper;
    
    private final ShardingAutoTableRuleConfigurationYamlSwapper tableYamlSwapper = new ShardingAutoTableRuleConfigurationYamlSwapper(mockAlgorithms(), Collections.emptyMap());
    
    @Before
    public void setUp() throws ReflectiveOperationException {
        setSwapper("shardingStrategyYamlSwapper", shardingStrategyYamlSwapper);
        when(shardingStrategyYamlSwapper.swapToYamlConfiguration(ArgumentMatchers.any())).thenReturn(mock(YamlShardingStrategyConfiguration.class));
        setSwapper("keyGenerateStrategyYamlSwapper", keyGenerateStrategyYamlSwapper);
        when(keyGenerateStrategyYamlSwapper.swapToYamlConfiguration(ArgumentMatchers.any())).thenReturn(mock(YamlKeyGenerateStrategyConfiguration.class));
    }
    
    private void setSwapper(final String swapperFieldName, final YamlConfigurationSwapper swapperFieldValue) throws ReflectiveOperationException {
        Field field = ShardingAutoTableRuleConfigurationYamlSwapper.class.getDeclaredField(swapperFieldName);
        field.setAccessible(true);
        field.set(tableYamlSwapper, swapperFieldValue);
    }
    
    @Test
    public void assertSwapToYamlWithMinProperties() {
        YamlShardingAutoTableRuleConfiguration actual = tableYamlSwapper.swapToYamlConfiguration(new ShardingAutoTableRuleConfiguration("tbl", "ds0,ds1"));
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
        YamlShardingAutoTableRuleConfiguration actual = tableYamlSwapper.swapToYamlConfiguration(shardingTableRuleConfig);
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
        when(strategyConfiguration.getShardingAlgorithmName()).thenReturn("mod_2");
        shardingTableRuleConfig.setShardingStrategy(strategyConfiguration);
        shardingTableRuleConfig.setKeyGenerateStrategy(mock(KeyGenerateStrategyConfiguration.class));
        YamlShardingAutoTableRuleConfiguration actual = tableYamlSwapper.swapToYamlConfiguration(shardingTableRuleConfig);
        assertThat(actual.getLogicTable(), is("tbl"));
        assertThat(actual.getActualDataSources(), is("ds0,ds1"));
        assertThat(actual.getActualTablePrefix(), is("tmp_"));
        assertThat(actual.getActualDataNodes(), is("ds0.tbl_0,ds1.tbl_1"));
        assertNotNull(actual.getShardingStrategy());
        assertNotNull(actual.getKeyGenerateStrategy());
    }
    
    private Map<String, ShardingAlgorithm> mockAlgorithms() {
        Map<String, ShardingAlgorithm> result = new LinkedHashMap<>();
        ShardingAlgorithm algorithm = mock(ShardingAlgorithm.class, withSettings().extraInterfaces(ShardingAutoTableAlgorithm.class));
        when(((ShardingAutoTableAlgorithm) algorithm).getAutoTablesAmount()).thenReturn(2);
        result.put("mod_2", algorithm);
        return result;
    }
    
    @Test(expected = NullPointerException.class)
    public void assertSwapToObjectWithoutLogicTable() {
        new ShardingAutoTableRuleConfigurationYamlSwapper(Collections.emptyMap(), Collections.emptyMap()).swapToObject(new YamlShardingAutoTableRuleConfiguration());
    }
}
