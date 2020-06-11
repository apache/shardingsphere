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
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.ShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.YamlKeyGeneratorConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.YamlShardingRuleConfiguration;
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
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class ShardingRuleConfigurationYamlSwapperTest {
    
    @Mock
    private TableRuleConfigurationYamlSwapper tableRuleConfigurationYamlSwapper;
    
    @Mock
    private ShardingStrategyConfigurationYamlSwapper shardingStrategyConfigurationYamlSwapper;
    
    @Mock
    private KeyGeneratorConfigurationYamlSwapper keyGeneratorConfigurationYamlSwapper;
    
    private final ShardingRuleConfigurationYamlSwapper shardingRuleConfigurationYamlSwapper = new ShardingRuleConfigurationYamlSwapper();
    
    @Before
    public void setUp() throws ReflectiveOperationException {
        setSwapper("tableRuleConfigurationYamlSwapper", tableRuleConfigurationYamlSwapper);
        when(tableRuleConfigurationYamlSwapper.swap(ArgumentMatchers.<ShardingTableRuleConfiguration>any())).thenReturn(mock(YamlTableRuleConfiguration.class));
        when(tableRuleConfigurationYamlSwapper.swap(ArgumentMatchers.<YamlTableRuleConfiguration>any())).thenReturn(mock(ShardingTableRuleConfiguration.class));
        setSwapper("shardingStrategyConfigurationYamlSwapper", shardingStrategyConfigurationYamlSwapper);
        when(shardingStrategyConfigurationYamlSwapper.swap(ArgumentMatchers.<ShardingStrategyConfiguration>any())).thenReturn(mock(YamlShardingStrategyConfiguration.class));
        when(shardingStrategyConfigurationYamlSwapper.swap(ArgumentMatchers.<YamlShardingStrategyConfiguration>any())).thenReturn(mock(ShardingStrategyConfiguration.class));
        setSwapper("keyGeneratorConfigurationYamlSwapper", keyGeneratorConfigurationYamlSwapper);
        when(keyGeneratorConfigurationYamlSwapper.swap(ArgumentMatchers.<KeyGeneratorConfiguration>any())).thenReturn(mock(YamlKeyGeneratorConfiguration.class));
        when(keyGeneratorConfigurationYamlSwapper.swap(ArgumentMatchers.<YamlKeyGeneratorConfiguration>any())).thenReturn(mock(KeyGeneratorConfiguration.class));
    }
    
    private void setSwapper(final String swapperFieldName, final YamlSwapper swapperFieldValue) throws ReflectiveOperationException {
        Field field = ShardingRuleConfigurationYamlSwapper.class.getDeclaredField(swapperFieldName);
        field.setAccessible(true);
        field.set(shardingRuleConfigurationYamlSwapper, swapperFieldValue);
    }
    
    @Test
    public void assertSwapToYamlWithMinProperties() {
        ShardingRuleConfiguration shardingRuleConfiguration = new ShardingRuleConfiguration();
        shardingRuleConfiguration.getTables().add(mock(ShardingTableRuleConfiguration.class));
        YamlShardingRuleConfiguration actual = shardingRuleConfigurationYamlSwapper.swap(shardingRuleConfiguration);
        assertThat(actual.getTables().size(), is(1));
        assertTrue(actual.getBindingTables().isEmpty());
        assertTrue(actual.getBroadcastTables().isEmpty());
        assertNull(actual.getDefaultDatabaseStrategy());
        assertNull(actual.getDefaultTableStrategy());
        assertNull(actual.getDefaultKeyGenerator());
    }
    
    @Test
    public void assertSwapToYamlWithMaxProperties() {
        ShardingRuleConfiguration shardingRuleConfiguration = new ShardingRuleConfiguration();
        shardingRuleConfiguration.getTables().add(mock(ShardingTableRuleConfiguration.class));
        shardingRuleConfiguration.getBindingTableGroups().add("tbl, sub_tbl");
        shardingRuleConfiguration.getBroadcastTables().add("dict");
        shardingRuleConfiguration.setDefaultDatabaseShardingStrategy(mock(ShardingStrategyConfiguration.class));
        shardingRuleConfiguration.setDefaultTableShardingStrategy(mock(ShardingStrategyConfiguration.class));
        shardingRuleConfiguration.setDefaultKeyGeneratorConfig(mock(KeyGeneratorConfiguration.class));
        YamlShardingRuleConfiguration actual = shardingRuleConfigurationYamlSwapper.swap(shardingRuleConfiguration);
        assertThat(actual.getTables().size(), is(1));
        assertThat(actual.getBindingTables().size(), is(1));
        assertThat(actual.getBindingTables().iterator().next(), is("tbl, sub_tbl"));
        assertThat(actual.getBroadcastTables().size(), is(1));
        assertThat(actual.getBroadcastTables().iterator().next(), is("dict"));
        assertNotNull(actual.getDefaultDatabaseStrategy());
        assertNotNull(actual.getDefaultTableStrategy());
        assertNotNull(actual.getDefaultKeyGenerator());
    }
    
    @Test
    public void assertSwapToObjectWithMinProperties() {
        YamlShardingRuleConfiguration yamlConfiguration = new YamlShardingRuleConfiguration();
        yamlConfiguration.getTables().put("tbl", mock(YamlTableRuleConfiguration.class));
        ShardingRuleConfiguration actual = shardingRuleConfigurationYamlSwapper.swap(yamlConfiguration);
        assertThat(actual.getTables().size(), is(1));
        assertTrue(actual.getBindingTableGroups().isEmpty());
        assertTrue(actual.getBroadcastTables().isEmpty());
        assertNull(actual.getDefaultDatabaseShardingStrategy());
        assertNull(actual.getDefaultTableShardingStrategy());
        assertNull(actual.getDefaultKeyGeneratorConfig());
    }
    
    @Test
    public void assertSwapToObjectWithMaxProperties() {
        YamlShardingRuleConfiguration yamlConfiguration = new YamlShardingRuleConfiguration();
        yamlConfiguration.getTables().put("tbl", mock(YamlTableRuleConfiguration.class));
        yamlConfiguration.getBindingTables().add("tbl, sub_tbl");
        yamlConfiguration.getBroadcastTables().add("dict");
        yamlConfiguration.setDefaultDatabaseStrategy(mock(YamlShardingStrategyConfiguration.class));
        yamlConfiguration.setDefaultTableStrategy(mock(YamlShardingStrategyConfiguration.class));
        yamlConfiguration.setDefaultKeyGenerator(mock(YamlKeyGeneratorConfiguration.class));
        ShardingRuleConfiguration actual = shardingRuleConfigurationYamlSwapper.swap(yamlConfiguration);
        assertThat(actual.getTables().size(), is(1));
        assertThat(actual.getBindingTableGroups().size(), is(1));
        assertThat(actual.getBindingTableGroups().iterator().next(), is("tbl, sub_tbl"));
        assertThat(actual.getBroadcastTables().size(), is(1));
        assertThat(actual.getBroadcastTables().iterator().next(), is("dict"));
        assertNotNull(actual.getDefaultDatabaseShardingStrategy());
        assertNotNull(actual.getDefaultTableShardingStrategy());
        assertNotNull(actual.getDefaultKeyGeneratorConfig());
    }
}
