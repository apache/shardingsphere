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

package org.apache.shardingsphere.sharding.yaml.swapper;

import org.apache.shardingsphere.sharding.api.config.rule.KeyGeneratorConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.ShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.YamlKeyGeneratorConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.YamlShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.YamlTableRuleConfiguration;
import org.apache.shardingsphere.infra.yaml.swapper.YamlSwapper;
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
    private KeyGeneratorConfigurationYamlSwapper keyGeneratorConfigurationYamlSwapper;
    
    private final TableRuleConfigurationYamlSwapper tableRuleConfigurationYamlSwapper = new TableRuleConfigurationYamlSwapper();
    
    @Before
    public void setUp() throws ReflectiveOperationException {
        setSwapper("shardingStrategyConfigurationYamlSwapper", shardingStrategyConfigurationYamlSwapper);
        when(shardingStrategyConfigurationYamlSwapper.swap(ArgumentMatchers.<ShardingStrategyConfiguration>any())).thenReturn(mock(YamlShardingStrategyConfiguration.class));
        when(shardingStrategyConfigurationYamlSwapper.swap(ArgumentMatchers.<YamlShardingStrategyConfiguration>any())).thenReturn(mock(ShardingStrategyConfiguration.class));
        setSwapper("keyGeneratorConfigurationYamlSwapper", keyGeneratorConfigurationYamlSwapper);
        when(keyGeneratorConfigurationYamlSwapper.swap(ArgumentMatchers.<KeyGeneratorConfiguration>any())).thenReturn(mock(YamlKeyGeneratorConfiguration.class));
        when(keyGeneratorConfigurationYamlSwapper.swap(ArgumentMatchers.<YamlKeyGeneratorConfiguration>any())).thenReturn(mock(KeyGeneratorConfiguration.class));
    }
    
    private void setSwapper(final String swapperFieldName, final YamlSwapper swapperFieldValue) throws ReflectiveOperationException {
        Field field = TableRuleConfigurationYamlSwapper.class.getDeclaredField(swapperFieldName);
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
        assertNull(actual.getKeyGenerator());
    }
    
    @Test
    public void assertSwapToYamlWithMaxProperties() {
        ShardingTableRuleConfiguration shardingTableRuleConfiguration = new ShardingTableRuleConfiguration("tbl", "ds_$->{0..1}.tbl_$->{0..1}");
        shardingTableRuleConfiguration.setDatabaseShardingStrategy(mock(StandardShardingStrategyConfiguration.class));
        shardingTableRuleConfiguration.setTableShardingStrategy(mock(StandardShardingStrategyConfiguration.class));
        shardingTableRuleConfiguration.setKeyGenerator(mock(KeyGeneratorConfiguration.class));
        YamlTableRuleConfiguration actual = tableRuleConfigurationYamlSwapper.swap(shardingTableRuleConfiguration);
        assertThat(actual.getLogicTable(), is("tbl"));
        assertThat(actual.getActualDataNodes(), is("ds_$->{0..1}.tbl_$->{0..1}"));
        assertNotNull(actual.getDatabaseStrategy());
        assertNotNull(actual.getTableStrategy());
        assertNotNull(actual.getKeyGenerator());
    }
    
    @Test(expected = NullPointerException.class)
    public void assertSwapToObjectWithoutLogicTable() {
        new TableRuleConfigurationYamlSwapper().swap(new YamlTableRuleConfiguration());
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
        assertNull(actual.getKeyGenerator());
    }
    
    @Test
    public void assertSwapToObjectWithMaxProperties() {
        YamlTableRuleConfiguration yamlConfiguration = new YamlTableRuleConfiguration();
        yamlConfiguration.setLogicTable("tbl");
        yamlConfiguration.setActualDataNodes("ds_$->{0..1}.tbl_$->{0..1}");
        yamlConfiguration.setDatabaseStrategy(mock(YamlShardingStrategyConfiguration.class));
        yamlConfiguration.setTableStrategy(mock(YamlShardingStrategyConfiguration.class));
        yamlConfiguration.setKeyGenerator(mock(YamlKeyGeneratorConfiguration.class));
        ShardingTableRuleConfiguration actual = tableRuleConfigurationYamlSwapper.swap(yamlConfiguration);
        assertThat(actual.getLogicTable(), is("tbl"));
        assertThat(actual.getActualDataNodes(), is("ds_$->{0..1}.tbl_$->{0..1}"));
        assertNotNull(actual.getDatabaseShardingStrategy());
        assertNotNull(actual.getTableShardingStrategy());
        assertNotNull(actual.getKeyGenerator());
    }
}
