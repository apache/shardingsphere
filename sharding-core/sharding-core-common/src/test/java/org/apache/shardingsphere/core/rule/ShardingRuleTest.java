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

package org.apache.shardingsphere.core.rule;

import org.apache.shardingsphere.api.config.masterslave.MasterSlaveRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.KeyGeneratorConfiguration;
import org.apache.shardingsphere.api.config.sharding.ShardingRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.TableRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.strategy.InlineShardingStrategyConfiguration;
import org.apache.shardingsphere.api.config.sharding.strategy.NoneShardingStrategyConfiguration;
import org.apache.shardingsphere.api.config.sharding.strategy.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.core.config.ShardingConfigurationException;
import org.apache.shardingsphere.core.fixture.PreciseShardingAlgorithmFixture;
import org.apache.shardingsphere.core.strategy.keygen.SnowflakeShardingKeyGenerator;
import org.apache.shardingsphere.core.strategy.keygen.fixture.IncrementShardingKeyGenerator;
import org.apache.shardingsphere.core.strategy.route.inline.InlineShardingStrategy;
import org.apache.shardingsphere.core.strategy.route.none.NoneShardingStrategy;
import org.hamcrest.CoreMatchers;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ShardingRuleTest {
    
    @Test(expected = IllegalArgumentException.class)
    public void assertNewShardingRuleWithEmptyDataSourceNames() {
        new ShardingRule(new ShardingRuleConfiguration(), Collections.<String>emptyList());
    }
    
    @Test
    public void assertNewShardingRuleWithMaximumConfiguration() {
        ShardingRule actual = createMaximumShardingRule();
        assertThat(actual.getTableRules().size(), is(2));
        assertThat(actual.getBindingTableRules().size(), is(1));
        assertThat(actual.getBindingTableRules().iterator().next().getTableRules().size(), is(2));
        assertThat(actual.getBroadcastTables(), CoreMatchers.<Collection<String>>is(Collections.singletonList("BROADCAST_TABLE")));
        assertThat(actual.getDefaultDatabaseShardingStrategy(), instanceOf(InlineShardingStrategy.class));
        assertThat(actual.getDefaultTableShardingStrategy(), instanceOf(InlineShardingStrategy.class));
        assertThat(actual.getDefaultShardingKeyGenerator(), instanceOf(IncrementShardingKeyGenerator.class));
    }
    
    @Test
    public void assertNewShardingRuleWithMinimumConfiguration() {
        ShardingRule actual = createMinimumShardingRule();
        assertThat(actual.getTableRules().size(), is(1));
        assertTrue(actual.getBindingTableRules().isEmpty());
        assertTrue(actual.getBroadcastTables().isEmpty());
        assertThat(actual.getDefaultDatabaseShardingStrategy(), instanceOf(NoneShardingStrategy.class));
        assertThat(actual.getDefaultTableShardingStrategy(), instanceOf(NoneShardingStrategy.class));
        assertThat(actual.getDefaultShardingKeyGenerator(), instanceOf(SnowflakeShardingKeyGenerator.class));
    }
    
    @Test
    public void assertNewShardingRuleWithMasterSlaveConfiguration() {
        ShardingRule actual = createMasterSlaveShardingRule();
        assertThat(actual.getMasterSlaveRules().size(), is(2));
    }
    
    @Test
    public void assertFindTableRule() {
        assertTrue(createMaximumShardingRule().findTableRule("logic_Table").isPresent());
    }
    
    @Test
    public void assertNotFindTableRule() {
        assertFalse(createMaximumShardingRule().findTableRule("other_Table").isPresent());
    }
    
    @Test
    public void assertFindTableRuleByActualTable() {
        assertTrue(createMaximumShardingRule().findTableRuleByActualTable("table_0").isPresent());
    }
    
    @Test
    public void assertNotFindTableRuleByActualTable() {
        assertFalse(createMaximumShardingRule().findTableRuleByActualTable("table_3").isPresent());
    }
    
    @Test
    public void assertGetTableRuleWithShardingTable() {
        TableRule actual = createMaximumShardingRule().getTableRule("Logic_Table");
        assertThat(actual.getLogicTable(), is("logic_table"));
    }
    
    @Test
    public void assertGetTableRuleWithBroadcastTable() {
        TableRule actual = createMaximumShardingRule().getTableRule("Broadcast_Table");
        assertThat(actual.getLogicTable(), is("broadcast_table"));
    }
    
    @Test
    public void assertGetTableRuleWithDefaultDataSource() {
        ShardingRule shardingRule = createMaximumShardingRule();
        shardingRule.getBroadcastTables().clear();
        assertThat(shardingRule.getTableRule("Default_Table").getLogicTable(), is("default_table"));
    }
    
    @Test(expected = ShardingConfigurationException.class)
    public void assertGetTableRuleFailure() {
        createMinimumShardingRule().getTableRule("New_Table");
    }
    
    @Test
    public void assertGetDatabaseShardingStrategyFromTableRule() {
        TableRule tableRule = mock(TableRule.class);
        when(tableRule.getDatabaseShardingStrategy()).thenReturn(new NoneShardingStrategy());
        assertThat(createMaximumShardingRule().getDatabaseShardingStrategy(tableRule), instanceOf(NoneShardingStrategy.class));
    }
    
    @Test
    public void assertGetDatabaseShardingStrategyFromDefault() {
        assertThat(createMaximumShardingRule().getDatabaseShardingStrategy(mock(TableRule.class)), instanceOf(InlineShardingStrategy.class));
    }
    
    @Test
    public void assertGetTableShardingStrategyFromTableRule() {
        TableRule tableRule = mock(TableRule.class);
        when(tableRule.getTableShardingStrategy()).thenReturn(new NoneShardingStrategy());
        assertThat(createMaximumShardingRule().getTableShardingStrategy(tableRule), instanceOf(NoneShardingStrategy.class));
    }
    
    @Test
    public void assertGetTableShardingStrategyFromDefault() {
        assertThat(createMaximumShardingRule().getTableShardingStrategy(mock(TableRule.class)), instanceOf(InlineShardingStrategy.class));
    }
    
    @Test
    public void assertIsAllBindingTableWhenLogicTablesIsEmpty() {
        assertFalse(createMaximumShardingRule().isAllBindingTables(Collections.<String>emptyList()));
    }
    
    @Test
    public void assertIsNotAllBindingTable() {
        assertFalse(createMaximumShardingRule().isAllBindingTables(Collections.singletonList("new_Table")));
        assertFalse(createMaximumShardingRule().isAllBindingTables(Arrays.asList("logic_Table", "new_Table")));
    }
    
    @Test
    public void assertIsAllBindingTable() {
        assertTrue(createMaximumShardingRule().isAllBindingTables(Collections.singletonList("logic_Table")));
        assertTrue(createMaximumShardingRule().isAllBindingTables(Collections.singletonList("logic_table")));
        assertTrue(createMaximumShardingRule().isAllBindingTables(Collections.singletonList("sub_Logic_Table")));
        assertTrue(createMaximumShardingRule().isAllBindingTables(Collections.singletonList("sub_logic_table")));
        assertTrue(createMaximumShardingRule().isAllBindingTables(Arrays.asList("logic_Table", "sub_Logic_Table")));
        assertTrue(createMaximumShardingRule().isAllBindingTables(Arrays.asList("logic_table", "sub_logic_Table")));
        assertFalse(createMaximumShardingRule().isAllBindingTables(Arrays.asList("logic_table", "sub_logic_Table", "new_table")));
        assertFalse(createMaximumShardingRule().isAllBindingTables(Collections.<String>emptyList()));
        assertFalse(createMaximumShardingRule().isAllBindingTables(Collections.singletonList("new_Table")));
    }
    
    @Test
    public void assertGetBindingTableRuleForNotConfig() {
        assertFalse(createMinimumShardingRule().findBindingTableRule("logic_Table").isPresent());
    }
    
    @Test
    public void assertGetBindingTableRuleForNotFound() {
        assertFalse(createMaximumShardingRule().findBindingTableRule("new_Table").isPresent());
    }
    
    @Test
    public void assertGetBindingTableRuleForFound() {
        ShardingRule actual = createMaximumShardingRule();
        assertTrue(actual.findBindingTableRule("logic_Table").isPresent());
        assertThat(actual.findBindingTableRule("logic_Table").get().getTableRules().size(), is(2));
    }
    
    @Test
    public void assertIsAllBroadcastTableWhenLogicTablesIsEmpty() {
        assertFalse(createMaximumShardingRule().isAllBroadcastTables(Collections.<String>emptyList()));
    }
    
    @Test
    public void assertIsAllBroadcastTable() {
        assertTrue(createMaximumShardingRule().isAllBroadcastTables(Collections.singletonList("Broadcast_Table")));
    }
    
    @Test
    public void assertIsNotAllBroadcastTable() {
        assertFalse(createMaximumShardingRule().isAllBroadcastTables(Arrays.asList("broadcast_table", "other_table")));
    }
    
    @Test
    public void assertIsBroadcastTable() {
        assertTrue(createMaximumShardingRule().isBroadcastTable("Broadcast_Table"));
    }
    
    @Test
    public void assertIsNotBroadcastTable() {
        assertFalse(createMaximumShardingRule().isBroadcastTable("other_table"));
    }
    
    @Test
    public void assertIsAllInDefaultDataSource() {
        assertTrue(createMaximumShardingRule().isAllInDefaultDataSource(Collections.singletonList("table_0")));
    }
    
    @Test
    public void assertIsNotAllInDefaultDataSourceWithShardingTable() {
        assertFalse(createMaximumShardingRule().isAllInDefaultDataSource(Arrays.asList("table_0", "logic_table")));
    }
    
    @Test
    public void assertIsNotAllInDefaultDataSourceWithBroadcastTable() {
        assertFalse(createMaximumShardingRule().isAllInDefaultDataSource(Arrays.asList("table_0", "broadcast_table")));
    }
    
    @Test
    public void assertIsShardingColumnForDefaultDatabaseShardingStrategy() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.getTableRuleConfigs().add(createTableRuleConfigWithAllStrategies());
        shardingRuleConfig.setDefaultDatabaseShardingStrategyConfig(new StandardShardingStrategyConfiguration("column", new PreciseShardingAlgorithmFixture()));
        assertTrue(new ShardingRule(shardingRuleConfig, createDataSourceNames()).isShardingColumn("column", "LOGIC_TABLE"));
    }
    
    @Test
    public void assertIsShardingColumnForDefaultTableShardingStrategy() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.getTableRuleConfigs().add(createTableRuleConfigWithAllStrategies());
        shardingRuleConfig.setDefaultTableShardingStrategyConfig(new StandardShardingStrategyConfiguration("column", new PreciseShardingAlgorithmFixture()));
        assertTrue(new ShardingRule(shardingRuleConfig, createDataSourceNames()).isShardingColumn("column", "LOGIC_TABLE"));
    }
    
    @Test
    public void assertIsShardingColumnForDatabaseShardingStrategy() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.getTableRuleConfigs().add(createTableRuleConfigWithAllStrategies());
        assertTrue(new ShardingRule(shardingRuleConfig, createDataSourceNames()).isShardingColumn("column", "logic_Table"));
    }
    
    @Test
    public void assertIsShardingColumnForTableShardingStrategy() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.getTableRuleConfigs().add(createTableRuleConfigWithTableStrategies());
        assertTrue(new ShardingRule(shardingRuleConfig, createDataSourceNames()).isShardingColumn("column", "logic_Table"));
    }
    
    @Test
    public void assertIsNotShardingColumn() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.getTableRuleConfigs().add(createTableRuleConfigWithAllStrategies());
        assertFalse(new ShardingRule(shardingRuleConfig, createDataSourceNames()).isShardingColumn("column", "other_Table"));
    }
    
    @Test
    public void assertFindGenerateKeyColumn() {
        assertTrue(createMaximumShardingRule().findGenerateKeyColumnName("logic_table").isPresent());
    }
    
    @Test
    public void assertNotFindGenerateKeyColumn() {
        assertFalse(createMinimumShardingRule().findGenerateKeyColumnName("sub_logic_table").isPresent());
    }
    
    @Test(expected = ShardingConfigurationException.class)
    public void assertGenerateKeyFailure() {
        createMaximumShardingRule().generateKey("table_0");
    }
    
    @Test
    public void assertGenerateKeyWithDefaultKeyGenerator() {
        assertThat(createMinimumShardingRule().generateKey("logic_table"), instanceOf(Long.class));
    }
    
    @Test
    public void assertGenerateKeyWithKeyGenerator() {
        assertThat(createMaximumShardingRule().generateKey("logic_table"), instanceOf(Integer.class));
    }
    
    @Test
    public void assertDataSourceNameFromDefaultDataSourceName() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.setDefaultDataSourceName("ds_3");
        ShardingRule actual = new ShardingRule(shardingRuleConfig, createDataSourceNames());
        assertThat(actual.getShardingDataSourceNames().getDefaultDataSourceName(), is("ds_3"));
    }
    
    @Test
    public void assertDataSourceNameFromDataSourceNames() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.setDefaultDataSourceName("ds_3");
        assertThat(new ShardingRule(shardingRuleConfig, Collections.singletonList("ds_0")).getShardingDataSourceNames().getDefaultDataSourceName(), is("ds_0"));
    }
    
    @Test
    public void assertGetDataNodeByLogicTable() {
        assertThat(createMaximumShardingRule().getDataNode("logic_table"), is(new DataNode("ds_0.table_0")));
    }
    
    @Test
    public void assertGetDataNodeByDataSourceAndLogicTable() {
        assertThat(createMaximumShardingRule().getDataNode("ds_1", "logic_table"), is(new DataNode("ds_1.table_0")));
    }
    
    @Test(expected = ShardingConfigurationException.class)
    public void assertGetDataNodeByLogicTableFailureWithDataSourceName() {
        createMaximumShardingRule().getDataNode("ds_3", "logic_table");
    }
    
    @Test
    public void assertGetShardingLogicTableNames() {
        ShardingRule actual = createMaximumShardingRule();
        assertThat(actual.getShardingLogicTableNames(Arrays.asList("LOGIC_TABLE", "BROADCAST_TABLE")), CoreMatchers.<Collection<String>>is(Collections.singletonList("LOGIC_TABLE")));
    }

    @Test(expected = IllegalArgumentException.class)
    public void assertConstructShardingRuleWithNullShardingRuleConfiguration() {
        new ShardingRule(null, createDataSourceNames());
    }

    @Test(expected = IllegalArgumentException.class)
    public void assertConstructShardingRuleWithNullDataSourceNames(){
        ShardingRuleConfiguration shardingRuleConfiguration = new ShardingRuleConfiguration();
        TableRuleConfiguration tableRuleConfiguration = createTableRuleConfiguration("LOGIC_TABLE", "ms_ds_${0..1}.table_${0..2}");
        shardingRuleConfiguration.getTableRuleConfigs().add(tableRuleConfiguration);
        shardingRuleConfiguration.getMasterSlaveRuleConfigs().add(createMasterSlaveRuleConfiguration("ms_ds_0", "master_ds_0", "slave_ds_0"));
        shardingRuleConfiguration.getMasterSlaveRuleConfigs().add(createMasterSlaveRuleConfiguration("ms_ds_1", "master_ds_1", "slave_ds_1"));
        new ShardingRule(shardingRuleConfiguration, null);
    }
    
    private ShardingRule createMaximumShardingRule() {
        ShardingRuleConfiguration shardingRuleConfiguration = new ShardingRuleConfiguration();
        shardingRuleConfiguration.setDefaultDataSourceName("ds_0");
        TableRuleConfiguration tableRuleConfiguration = createTableRuleConfiguration("LOGIC_TABLE", "ds_${0..1}.table_${0..2}");
        tableRuleConfiguration.setKeyGeneratorConfig(new KeyGeneratorConfiguration("INCREMENT", "id", new Properties()));
        TableRuleConfiguration subTableRuleConfiguration = createTableRuleConfiguration("SUB_LOGIC_TABLE", "ds_${0..1}.sub_table_${0..2}");
        shardingRuleConfiguration.getTableRuleConfigs().add(tableRuleConfiguration);
        shardingRuleConfiguration.getTableRuleConfigs().add(subTableRuleConfiguration);
        shardingRuleConfiguration.getBindingTableGroups().add(tableRuleConfiguration.getLogicTable() + "," + subTableRuleConfiguration.getLogicTable());
        shardingRuleConfiguration.getBroadcastTables().add("BROADCAST_TABLE");
        shardingRuleConfiguration.setDefaultDatabaseShardingStrategyConfig(new InlineShardingStrategyConfiguration("id", "ds_%{id % 2}"));
        shardingRuleConfiguration.setDefaultTableShardingStrategyConfig(new InlineShardingStrategyConfiguration("id", "table_%{id % 2}"));
        shardingRuleConfiguration.setDefaultKeyGeneratorConfig(new KeyGeneratorConfiguration("INCREMENT", "id", new Properties()));
        return new ShardingRule(shardingRuleConfiguration, createDataSourceNames());
    }
    
    private ShardingRule createMinimumShardingRule() {
        ShardingRuleConfiguration shardingRuleConfiguration = new ShardingRuleConfiguration();
        TableRuleConfiguration tableRuleConfiguration = createTableRuleConfiguration("LOGIC_TABLE", "ds_${0..1}.table_${0..2}");
        shardingRuleConfiguration.getTableRuleConfigs().add(tableRuleConfiguration);
        return new ShardingRule(shardingRuleConfiguration, createDataSourceNames());
    }
    
    private TableRuleConfiguration createTableRuleConfiguration(final String logicTableName, final String actualDataNodes) {
        return new TableRuleConfiguration(logicTableName, actualDataNodes);
    }
    
    private Collection<String> createDataSourceNames() {
        return Arrays.asList("ds_0", "ds_1");
    }
    
    private ShardingRule createMasterSlaveShardingRule() {
        ShardingRuleConfiguration shardingRuleConfiguration = new ShardingRuleConfiguration();
        TableRuleConfiguration tableRuleConfiguration = createTableRuleConfiguration("LOGIC_TABLE", "ms_ds_${0..1}.table_${0..2}");
        shardingRuleConfiguration.getTableRuleConfigs().add(tableRuleConfiguration);
        shardingRuleConfiguration.getMasterSlaveRuleConfigs().add(createMasterSlaveRuleConfiguration("ms_ds_0", "master_ds_0", "slave_ds_0"));
        shardingRuleConfiguration.getMasterSlaveRuleConfigs().add(createMasterSlaveRuleConfiguration("ms_ds_1", "master_ds_1", "slave_ds_1"));
        return new ShardingRule(shardingRuleConfiguration, createMasterSlaveDataSourceNames());
    }
    
    private MasterSlaveRuleConfiguration createMasterSlaveRuleConfiguration(final String name, final String masterDataSourceName, final String slaveDataSourceName) {
        return new MasterSlaveRuleConfiguration(name, masterDataSourceName, Collections.singletonList(slaveDataSourceName));
    }
    
    private Collection<String> createMasterSlaveDataSourceNames() {
        return Arrays.asList("master_ds_0", "slave_ds_0", "master_ds_1", "slave_ds_1");
    }
    
    private TableRuleConfiguration createTableRuleConfigWithAllStrategies() {
        TableRuleConfiguration result = new TableRuleConfiguration("LOGIC_TABLE", "ds_${0..1}.table_${0..2}");
        result.setDatabaseShardingStrategyConfig(new StandardShardingStrategyConfiguration("column", new PreciseShardingAlgorithmFixture()));
        result.setTableShardingStrategyConfig(new NoneShardingStrategyConfiguration());
        return result;
    }
    
    private TableRuleConfiguration createTableRuleConfigWithTableStrategies() {
        TableRuleConfiguration result = new TableRuleConfiguration("LOGIC_TABLE", "ds_${0..1}.table_${0..2}");
        result.setTableShardingStrategyConfig(new StandardShardingStrategyConfiguration("column", new PreciseShardingAlgorithmFixture()));
        return result;
    }
}
