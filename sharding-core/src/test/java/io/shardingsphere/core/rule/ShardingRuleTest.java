/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.core.rule;

import io.shardingsphere.api.algorithm.fixture.TestPreciseShardingAlgorithm;
import io.shardingsphere.api.algorithm.masterslave.MasterSlaveLoadBalanceAlgorithmType;
import io.shardingsphere.api.config.KeyGeneratorConfiguration;
import io.shardingsphere.api.config.rule.MasterSlaveRuleConfiguration;
import io.shardingsphere.api.config.rule.ShardingRuleConfiguration;
import io.shardingsphere.api.config.rule.TableRuleConfiguration;
import io.shardingsphere.api.config.strategy.InlineShardingStrategyConfiguration;
import io.shardingsphere.api.config.strategy.NoneShardingStrategyConfiguration;
import io.shardingsphere.api.config.strategy.StandardShardingStrategyConfiguration;
import io.shardingsphere.core.exception.ShardingConfigurationException;
import io.shardingsphere.core.keygen.generator.KeyGenerator;
import io.shardingsphere.core.keygen.generator.SnowflakeKeyGenerator;
import io.shardingsphere.core.keygen.fixture.IncrementKeyGenerator;
import io.shardingsphere.core.parsing.parser.context.condition.Column;
import io.shardingsphere.core.routing.strategy.inline.InlineShardingStrategy;
import io.shardingsphere.core.routing.strategy.none.NoneShardingStrategy;
import org.hamcrest.CoreMatchers;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

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
        assertThat(actual.getDefaultKeyGenerator(), instanceOf(IncrementKeyGenerator.class));
    }
    
    @Test
    public void assertNewShardingRuleWithMinimumConfiguration() {
        ShardingRule actual = createMinimumShardingRule();
        assertThat(actual.getTableRules().size(), is(1));
        assertTrue(actual.getBindingTableRules().isEmpty());
        assertTrue(actual.getBroadcastTables().isEmpty());
        assertThat(actual.getDefaultDatabaseShardingStrategy(), instanceOf(NoneShardingStrategy.class));
        assertThat(actual.getDefaultTableShardingStrategy(), instanceOf(NoneShardingStrategy.class));
        assertThat(actual.getDefaultKeyGenerator(), instanceOf(SnowflakeKeyGenerator.class));
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
        assertThat(createMaximumShardingRule().getTableRule("Broadcast_Table").getLogicTable(), is("broadcast_table"));
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
    public void assertIsShardingColumnForDefaultDatabaseShardingStrategy() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.getTableRuleConfigs().add(createTableRuleConfigWithAllStrategies());
        shardingRuleConfig.setDefaultDatabaseShardingStrategyConfig(new StandardShardingStrategyConfiguration("column", new TestPreciseShardingAlgorithm()));
        assertTrue(new ShardingRule(shardingRuleConfig, createDataSourceNames()).isShardingColumn(new Column("column", "LOGIC_TABLE")));
    }
    
    @Test
    public void assertIsShardingColumnForDefaultTableShardingStrategy() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.getTableRuleConfigs().add(createTableRuleConfigWithAllStrategies());
        shardingRuleConfig.setDefaultTableShardingStrategyConfig(new StandardShardingStrategyConfiguration("column", new TestPreciseShardingAlgorithm()));
        assertTrue(new ShardingRule(shardingRuleConfig, createDataSourceNames()).isShardingColumn(new Column("column", "LOGIC_TABLE")));
    }
    
    @Test
    public void assertIsShardingColumnForDatabaseShardingStrategy() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.getTableRuleConfigs().add(createTableRuleConfigWithAllStrategies());
        assertTrue(new ShardingRule(shardingRuleConfig, createDataSourceNames()).isShardingColumn(new Column("column", "logic_Table")));
    }
    
    @Test
    public void assertIsShardingColumnForTableShardingStrategy() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.getTableRuleConfigs().add(createTableRuleConfigWithTableStrategies());
        assertTrue(new ShardingRule(shardingRuleConfig, createDataSourceNames()).isShardingColumn(new Column("column", "logic_Table")));
    }
    
    @Test
    public void assertIsNotShardingColumn() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.getTableRuleConfigs().add(createTableRuleConfigWithAllStrategies());
        assertFalse(new ShardingRule(shardingRuleConfig, createDataSourceNames()).isShardingColumn(new Column("column", "other_Table")));
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
    public void assertIsBroadcastTable() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.getBroadcastTables().add("table_0");
        ShardingRule actual = new ShardingRule(shardingRuleConfig, createDataSourceNames());
        assertTrue(actual.isBroadcastTable("table_0"));
        assertFalse(actual.isBroadcastTable("logic_table"));
    }
    
    @Test
    public void assertIsAllBroadcastTable() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.getBroadcastTables().add("table_0");
        shardingRuleConfig.getBroadcastTables().add("table_1");
        ShardingRule actual = new ShardingRule(shardingRuleConfig, createDataSourceNames());
        assertTrue(actual.isAllBroadcastTables(Arrays.asList("table_0", "table_1")));
        assertFalse(actual.isAllBroadcastTables(Arrays.asList("table_0", "table_2")));
        assertFalse(actual.isAllBroadcastTables(Arrays.asList("table_2", "table_3")));
    }
    
    @Test
    public void assertIsAllInDefaultDataSource() {
        ShardingRule actual = createMaximumShardingRule();
        assertTrue(actual.isAllInDefaultDataSource(Collections.singletonList("table_0")));
    }
    
    @Test
    public void assertIsNotAllInDefaultDataSourceWithShardingTable() {
        ShardingRule actual = createMaximumShardingRule();
        assertFalse(actual.isAllInDefaultDataSource(Arrays.asList("table_0", "logic_table")));
    }
    
    @Test
    public void assertIsNotAllInDefaultDataSourceWithBroadcastTable() {
        ShardingRule actual = createMaximumShardingRule();
        assertFalse(actual.isAllInDefaultDataSource(Arrays.asList("table_0", "broadcast_table")));
    }
    
    @Test
    public void assertGetGenerateKeyColumn() {
        ShardingRule actual = createMaximumShardingRule();
        assertFalse(actual.getGenerateKeyColumn("table_0").isPresent());
        assertFalse(actual.getGenerateKeyColumn("logic_table").isPresent());
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
    public void assertGetLogicTableNameSuccess() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        TableRuleConfiguration tableRuleConfig = createTableRuleConfigWithLogicIndex();
        shardingRuleConfig.getTableRuleConfigs().add(tableRuleConfig);
        assertThat(new ShardingRule(shardingRuleConfig, createDataSourceNames()).getLogicTableName("index_table"), is("logic_table"));
    }
    
    @Test(expected = ShardingConfigurationException.class)
    public void assertGetLogicTableNameFailure() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        TableRuleConfiguration tableRuleConfig = createTableRuleConfigWithLogicIndex();
        shardingRuleConfig.getTableRuleConfigs().add(tableRuleConfig);
        new ShardingRule(shardingRuleConfig, createDataSourceNames()).getLogicTableName("");
    }
    
    @Test
    public void assertFindDataNodeByLogicTableSuccess() {
        assertThat(createMaximumShardingRule().findDataNode("logic_table").getDataSourceName(), is("ds_0"));
    }
    
    @Test(expected = ShardingConfigurationException.class)
    public void assertFindDataNodeByLogicTableFailure() {
        createMinimumShardingRule().findDataNode("logic_table_x");
    }
    
    @Test(expected = ShardingConfigurationException.class)
    public void assertFindDataNodeByLogicTableFailureWithDataSourceName() {
        assertThat(createMaximumShardingRule().findDataNode("ds_3", "logic_table").getDataSourceName(), is("ds_0"));
    }
    
    @Test
    public void assertIsLogicIndex() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        TableRuleConfiguration tableRuleConfig = createTableRuleConfigWithLogicIndex();
        shardingRuleConfig.getTableRuleConfigs().add(tableRuleConfig);
        assertTrue(new ShardingRule(shardingRuleConfig, createDataSourceNames()).isLogicIndex("index_table", "logic_table"));
    }
    
    @Test
    public void assertContainsWithTableRule() {
        assertTrue(createMaximumShardingRule().contains("LOGIC_TABLE"));
    }
    
    @Test
    public void assertContainsWithBindingTableRule() {
        assertTrue(createMaximumShardingRule().contains("SUB_LOGIC_TABLE"));
    }
    
    @Test
    public void assertContainsWithBroadcastTableRule() {
        assertTrue(createMaximumShardingRule().contains("BROADCAST_TABLE"));
    }
    
    @Test
    public void assertNotContains() {
        assertFalse(createMaximumShardingRule().contains("NEW_TABLE"));
    }
    
    @Test
    public void assertGetShardingLogicTableNames() {
        ShardingRule actual = createMaximumShardingRule();
        assertThat(actual.getShardingLogicTableNames(Arrays.asList("LOGIC_TABLE", "BROADCAST_TABLE")), CoreMatchers.<Collection<String>>is(Collections.singletonList("LOGIC_TABLE")));
    }
    
    private ShardingRule createMaximumShardingRule() {
        ShardingRuleConfiguration shardingRuleConfiguration = new ShardingRuleConfiguration();
        shardingRuleConfiguration.setDefaultDataSourceName("ds_0");
        TableRuleConfiguration tableRuleConfiguration = createTableRuleConfiguration("LOGIC_TABLE", "ds_${0..1}.table_${0..2}");
        TableRuleConfiguration subTableRuleConfiguration = createTableRuleConfiguration("SUB_LOGIC_TABLE", "ds_${0..1}.sub_table_${0..2}");
        shardingRuleConfiguration.getTableRuleConfigs().add(tableRuleConfiguration);
        shardingRuleConfiguration.getTableRuleConfigs().add(subTableRuleConfiguration);
        shardingRuleConfiguration.getBindingTableGroups().add(tableRuleConfiguration.getLogicTable() + "," + subTableRuleConfiguration.getLogicTable());
        shardingRuleConfiguration.getBroadcastTables().add("BROADCAST_TABLE");
        shardingRuleConfiguration.setDefaultDatabaseShardingStrategyConfig(new InlineShardingStrategyConfiguration("id", "ds_%{id % 2}"));
        shardingRuleConfiguration.setDefaultTableShardingStrategyConfig(new InlineShardingStrategyConfiguration("id", "table_%{id % 2}"));
        shardingRuleConfiguration.setDefaultKeyGeneratorConfig(getKeyGeneratorConfiguration());
        return new ShardingRule(shardingRuleConfiguration, createDataSourceNames());
    }
    
    private KeyGeneratorConfiguration getKeyGeneratorConfiguration() {
        KeyGenerator keyGenerator = new IncrementKeyGenerator();
        KeyGeneratorConfiguration keyGeneratorConfiguration = new KeyGeneratorConfiguration();
        keyGeneratorConfiguration.setClassName(keyGenerator.getClass().getName());
        return keyGeneratorConfiguration;
    }
    
    private ShardingRule createMinimumShardingRule() {
        ShardingRuleConfiguration shardingRuleConfiguration = new ShardingRuleConfiguration();
        TableRuleConfiguration tableRuleConfiguration = createTableRuleConfiguration("LOGIC_TABLE", "ds_${0..1}.table_${0..2}");
        shardingRuleConfiguration.getTableRuleConfigs().add(tableRuleConfiguration);
        return new ShardingRule(shardingRuleConfiguration, createDataSourceNames());
    }
    
    private TableRuleConfiguration createTableRuleConfiguration(final String logicTableName, final String actualDataNodes) {
        TableRuleConfiguration result = new TableRuleConfiguration();
        result.setLogicTable(logicTableName);
        result.setActualDataNodes(actualDataNodes);
        return result;
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
        return new MasterSlaveRuleConfiguration(name, masterDataSourceName, Collections.singletonList(slaveDataSourceName), MasterSlaveLoadBalanceAlgorithmType.ROUND_ROBIN.getAlgorithm());
    }
    
    private Collection<String> createMasterSlaveDataSourceNames() {
        return Arrays.asList("master_ds_0", "slave_ds_0", "master_ds_1", "slave_ds_1");
    }
    
    private TableRuleConfiguration createTableRuleConfigWithLogicIndex() {
        TableRuleConfiguration result = new TableRuleConfiguration();
        result.setLogicTable("LOGIC_TABLE");
        result.setLogicIndex("INDEX_TABLE");
        result.setActualDataNodes("ds_${0..1}.table_${0..2}");
        return result;
    }
    
    private TableRuleConfiguration createTableRuleConfigWithAllStrategies() {
        TableRuleConfiguration result = new TableRuleConfiguration();
        result.setLogicTable("LOGIC_TABLE");
        result.setActualDataNodes("ds_${0..1}.table_${0..2}");
        result.setDatabaseShardingStrategyConfig(new StandardShardingStrategyConfiguration("column", new TestPreciseShardingAlgorithm()));
        result.setTableShardingStrategyConfig(new NoneShardingStrategyConfiguration());
        return result;
    }
    
    private TableRuleConfiguration createTableRuleConfigWithTableStrategies() {
        TableRuleConfiguration result = new TableRuleConfiguration();
        result.setLogicTable("LOGIC_TABLE");
        result.setActualDataNodes("ds_${0..1}.table_${0..2}");
        result.setTableShardingStrategyConfig(new StandardShardingStrategyConfiguration("column", new TestPreciseShardingAlgorithm()));
        return result;
    }
}
