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
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.keygen.KeyGenerateStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.rule.YamlTableRuleConfiguration;
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
public final class TableRuleConfigurationYamlSwapperTest {
    
    @Mock
    private ShardingStrategyConfigurationYamlSwapper shardingStrategyConfigurationYamlSwapper;
    
    @Mock
    private KeyGenerateStrategyConfigurationYamlSwapper keyGenerateStrategyConfigurationYamlSwapper;
    
    private final ShardingTableRuleConfigurationYamlSwapper tableRuleConfigurationYamlSwapper = new ShardingTableRuleConfigurationYamlSwapper();
    
    @Before
    public void setUp() throws ReflectiveOperationException {
        setSwapper("shardingStrategyConfigurationYamlSwapper", shardingStrategyConfigurationYamlSwapper);
        when(shardingStrategyConfigurationYamlSwapper.swap(ArgumentMatchers.<ShardingStrategyConfiguration>any())).thenReturn(mock(YamlShardingStrategyConfiguration.class));
        when(shardingStrategyConfigurationYamlSwapper.swap(ArgumentMatchers.<YamlShardingStrategyConfiguration>any())).thenReturn(mock(ShardingStrategyConfiguration.class));
        setSwapper("keyGenerateStrategyConfigurationYamlSwapper", keyGenerateStrategyConfigurationYamlSwapper);
        when(keyGenerateStrategyConfigurationYamlSwapper.swap(ArgumentMatchers.<KeyGenerateStrategyConfiguration>any())).thenReturn(mock(YamlKeyGenerateStrategyConfiguration.class));
        when(keyGenerateStrategyConfigurationYamlSwapper.swap(ArgumentMatchers.<YamlKeyGenerateStrategyConfiguration>any())).thenReturn(mock(KeyGenerateStrategyConfiguration.class));
    }
    
    private void setSwapper(final String swapperFieldName, final YamlSwapper swapperFieldValue) throws ReflectiveOperationException {
        Field field = ShardingTableRuleConfigurationYamlSwapper.class.getDeclaredField(swapperFieldName);
        field.setAccessible(true);
        field.set(tableRuleConfigurationYamlSwapper, swapperFieldValue);
    }
    
    @Test
    public void assertSwapToYamlWithMinProperties() {
        YamlTableRuleConfiguration actual = tableRuleConfigurationYamlSwapper.swap(new ShardingTableRuleConfiguration("tbl", "ds_$->{0..1}.tbl_$->{0..1}"));
        assertThat(actual.getLogicTable(), is("tbl"));
        assertThat(actual.getActualDataNodes(), is("ds_$->{0..1}.tbl_$->{0..1}"));
        assertNull(actual.getDatabaseStrategy());
        assertNull(actual.getTableStrategy());
        assertNull(actual.getKeyGenerateStrategy());
    }
    
    @Test
    public void assertSwapToYamlWithMaxProperties() {
        ShardingTableRuleConfiguration shardingTableRuleConfiguration = new ShardingTableRuleConfiguration("tbl", "ds_$->{0..1}.tbl_$->{0..1}");
        shardingTableRuleConfiguration.setDatabaseShardingStrategy(mock(StandardShardingStrategyConfiguration.class));
        shardingTableRuleConfiguration.setTableShardingStrategy(mock(StandardShardingStrategyConfiguration.class));
        shardingTableRuleConfiguration.setKeyGenerateStrategy(mock(KeyGenerateStrategyConfiguration.class));
        YamlTableRuleConfiguration actual = tableRuleConfigurationYamlSwapper.swap(shardingTableRuleConfiguration);
        assertThat(actual.getLogicTable(), is("tbl"));
        assertThat(actual.getActualDataNodes(), is("ds_$->{0..1}.tbl_$->{0..1}"));
        assertNotNull(actual.getDatabaseStrategy());
        assertNotNull(actual.getTableStrategy());
        assertNotNull(actual.getKeyGenerateStrategy());
    }
    
    @Test(expected = NullPointerException.class)
    public void assertSwapToObjectWithoutLogicTable() {
        new ShardingTableRuleConfigurationYamlSwapper().swap(new YamlTableRuleConfiguration());
    }
    
    @Test
    public void assertSwapToObjectWithMinProperties() {
        YamlTableRuleConfiguration yamlConfiguration = new YamlTableRuleConfiguration();
        yamlConfiguration.setLogicTable("tbl");
        yamlConfiguration.setActualDataNodes("ds_$->{0..1}.tbl_$->{0..1}");
        ShardingTableRuleConfiguration actual = tableRuleConfigurationYamlSwapper.swap(yamlConfiguration);
        assertThat(actual.getLogicTable(), is("tbl"));
        assertThat(actual.getActualDataNodes(), is("ds_$->{0..1}.tbl_$->{0..1}"));
        assertNull(actual.getDatabaseShardingStrategy());
        assertNull(actual.getTableShardingStrategy());
        assertNull(actual.getKeyGenerateStrategy());
    }
    
    @Test
    public void assertSwapToObjectWithMaxProperties() {
        YamlTableRuleConfiguration yamlConfiguration = new YamlTableRuleConfiguration();
        yamlConfiguration.setLogicTable("tbl");
        yamlConfiguration.setActualDataNodes("ds_$->{0..1}.tbl_$->{0..1}");
        yamlConfiguration.setDatabaseStrategy(mock(YamlShardingStrategyConfiguration.class));
        yamlConfiguration.setTableStrategy(mock(YamlShardingStrategyConfiguration.class));
        yamlConfiguration.setKeyGenerateStrategy(mock(YamlKeyGenerateStrategyConfiguration.class));
        ShardingTableRuleConfiguration actual = tableRuleConfigurationYamlSwapper.swap(yamlConfiguration);
        assertThat(actual.getLogicTable(), is("tbl"));
        assertThat(actual.getActualDataNodes(), is("ds_$->{0..1}.tbl_$->{0..1}"));
        assertNotNull(actual.getDatabaseShardingStrategy());
        assertNotNull(actual.getTableShardingStrategy());
        assertNotNull(actual.getKeyGenerateStrategy());
    }
}
