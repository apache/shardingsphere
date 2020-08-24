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

import org.apache.shardingsphere.infra.yaml.swapper.YamlSwapper;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.keygen.KeyGenerateStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.rule.YamlShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.strategy.keygen.YamlKeyGenerateStrategyConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.strategy.sharding.YamlShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.yaml.swapper.strategy.KeyGenerateStrategyConfigurationYamlSwapper;
import org.apache.shardingsphere.sharding.yaml.swapper.strategy.ShardingStrategyConfigurationYamlSwapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Field;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class ShardingAutoTableRuleConfigurationYamlSwapperTest {
    
    @Mock
    private ShardingStrategyConfigurationYamlSwapper shardingStrategyYamlSwapper;
    
    @Mock
    private KeyGenerateStrategyConfigurationYamlSwapper keyGenerateStrategyYamlSwapper;
    
    private final ShardingAutoTableRuleConfigurationYamlSwapper tableYamlSwapper = new ShardingAutoTableRuleConfigurationYamlSwapper();
    
    @Before
    public void setUp() throws ReflectiveOperationException {
        setSwapper("shardingStrategyYamlSwapper", shardingStrategyYamlSwapper);
        when(shardingStrategyYamlSwapper.swapToYamlConfiguration(ArgumentMatchers.any())).thenReturn(mock(YamlShardingStrategyConfiguration.class));
        setSwapper("keyGenerateStrategyYamlSwapper", keyGenerateStrategyYamlSwapper);
        when(keyGenerateStrategyYamlSwapper.swapToYamlConfiguration(ArgumentMatchers.any())).thenReturn(mock(YamlKeyGenerateStrategyConfiguration.class));
    }
    
    private void setSwapper(final String swapperFieldName, final YamlSwapper swapperFieldValue) throws ReflectiveOperationException {
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
        ShardingAutoTableRuleConfiguration shardingTableRuleConfiguration = new ShardingAutoTableRuleConfiguration("tbl", "ds0,ds1");
        shardingTableRuleConfiguration.setShardingStrategy(mock(StandardShardingStrategyConfiguration.class));
        shardingTableRuleConfiguration.setKeyGenerateStrategy(mock(KeyGenerateStrategyConfiguration.class));
        YamlShardingAutoTableRuleConfiguration actual = tableYamlSwapper.swapToYamlConfiguration(shardingTableRuleConfiguration);
        assertThat(actual.getLogicTable(), is("tbl"));
        assertThat(actual.getActualDataSources(), is("ds0,ds1"));
        assertNotNull(actual.getShardingStrategy());
        assertNotNull(actual.getKeyGenerateStrategy());
    }
    
    @Test(expected = NullPointerException.class)
    public void assertSwapToObjectWithoutLogicTable() {
        new ShardingAutoTableRuleConfigurationYamlSwapper().swapToObject(new YamlShardingAutoTableRuleConfiguration());
    }
}
