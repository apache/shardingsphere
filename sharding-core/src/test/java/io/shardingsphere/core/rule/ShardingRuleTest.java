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
import io.shardingsphere.api.config.rule.ShardingRuleConfiguration;
import io.shardingsphere.api.config.rule.TableRuleConfiguration;
import io.shardingsphere.api.config.strategy.NoneShardingStrategyConfiguration;
import io.shardingsphere.api.config.strategy.ShardingStrategyConfiguration;
import io.shardingsphere.api.config.strategy.StandardShardingStrategyConfiguration;
import io.shardingsphere.core.exception.ShardingConfigurationException;
import io.shardingsphere.core.keygen.fixture.IncrementKeyGenerator;
import io.shardingsphere.core.parsing.parser.context.condition.Column;
import io.shardingsphere.core.routing.strategy.none.NoneShardingStrategy;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class ShardingRuleTest {
    
    @Test
    public void assertShardingRuleWithBroadcastTableRule() {
        ShardingRule actual = createShardingRule();
        assertThat(actual.getBroadcastTables().size(), is(1));
        assertThat(actual.getBroadcastTables().iterator().next(), is("BROADCAST_LOGIC_TABLE"));
    }
    
    @Test
    public void assertShardingRuleWithoutStrategy() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        TableRuleConfiguration tableRuleConfig = createTableRuleConfig();
        shardingRuleConfig.getTableRuleConfigs().add(tableRuleConfig);
        ShardingRule actual = new ShardingRule(shardingRuleConfig, createDataSourceNames());
        assertTrue(actual.getDefaultDatabaseShardingStrategy().getShardingColumns().isEmpty());
        assertTrue(actual.getDefaultTableShardingStrategy().getShardingColumns().isEmpty());
    }
    
    @Test
    public void assertShardingRuleWithBindingTableRuleWithoutStrategy() {
        ShardingRule actual = createShardingRule();
        assertTrue(actual.getDefaultDatabaseShardingStrategy().getShardingColumns().isEmpty());
        assertTrue(actual.getDefaultTableShardingStrategy().getShardingColumns().isEmpty());
    }
    
    @Test
    public void assertShardingRuleWithDatabaseStrategy() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        TableRuleConfiguration tableRuleConfig = createTableRuleConfig();
        shardingRuleConfig.getTableRuleConfigs().add(tableRuleConfig);
        shardingRuleConfig.setDefaultDatabaseShardingStrategyConfig(new NoneShardingStrategyConfiguration());
        ShardingRule actual = new ShardingRule(shardingRuleConfig, createDataSourceNames());
        assertTrue(actual.getDefaultDatabaseShardingStrategy().getShardingColumns().isEmpty());
        assertTrue(actual.getDefaultTableShardingStrategy().getShardingColumns().isEmpty());
    }
    
    @Test
    public void assertShardingRuleWithTableStrategy() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        TableRuleConfiguration tableRuleConfig = createTableRuleConfig();
        shardingRuleConfig.getTableRuleConfigs().add(tableRuleConfig);
        shardingRuleConfig.setDefaultTableShardingStrategyConfig(new NoneShardingStrategyConfiguration());
        ShardingRule actual = new ShardingRule(shardingRuleConfig, createDataSourceNames());
        assertTrue(actual.getDefaultDatabaseShardingStrategy().getShardingColumns().isEmpty());
        assertTrue(actual.getDefaultDatabaseShardingStrategy().getShardingColumns().isEmpty());
    }
    
    @Test
    public void assertShardingRuleWithoutBindingTableRule() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        TableRuleConfiguration tableRuleConfig = createTableRuleConfig();
        shardingRuleConfig.getTableRuleConfigs().add(tableRuleConfig);
        shardingRuleConfig.setDefaultDatabaseShardingStrategyConfig(new NoneShardingStrategyConfiguration());
        shardingRuleConfig.setDefaultTableShardingStrategyConfig(new NoneShardingStrategyConfiguration());
        ShardingRule actual = new ShardingRule(shardingRuleConfig, createDataSourceNames());
        assertTrue(actual.getDefaultDatabaseShardingStrategy().getShardingColumns().isEmpty());
        assertTrue(actual.getDefaultDatabaseShardingStrategy().getShardingColumns().isEmpty());
    }
    
    @Test
    public void assertFindTableRule() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        TableRuleConfiguration tableRuleConfig = createTableRuleConfig();
        shardingRuleConfig.getTableRuleConfigs().add(tableRuleConfig);
        shardingRuleConfig.setDefaultDatabaseShardingStrategyConfig(new NoneShardingStrategyConfiguration());
        shardingRuleConfig.setDefaultTableShardingStrategyConfig(new NoneShardingStrategyConfiguration());
        ShardingRule actual = new ShardingRule(shardingRuleConfig, createDataSourceNames());
        assertTrue(actual.findTableRuleByLogicTable("logic_Table").isPresent());
        assertFalse(actual.findTableRuleByLogicTable("null").isPresent());
    }
    
    @Test
    public void assertGetDatabaseShardingStrategyFromTableRule() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        TableRuleConfiguration tableRuleConfig = createTableRuleConfigWithDatabaseShardingStrategy(new NoneShardingStrategyConfiguration());
        shardingRuleConfig.getTableRuleConfigs().add(tableRuleConfig);
        ShardingRule actual = new ShardingRule(shardingRuleConfig, createDataSourceNames());
        assertThat(actual.getDatabaseShardingStrategy(actual.getTableRuleByLogicTableName("logic_Table")), instanceOf(NoneShardingStrategy.class));
    }
    
    @Test
    public void assertGetDatabaseShardingStrategyFromDefault() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        TableRuleConfiguration tableRuleConfig = createTableRuleConfig();
        shardingRuleConfig.getTableRuleConfigs().add(tableRuleConfig);
        shardingRuleConfig.setDefaultDatabaseShardingStrategyConfig(new NoneShardingStrategyConfiguration());
        ShardingRule actual = new ShardingRule(shardingRuleConfig, createDataSourceNames());
        assertThat(actual.getDatabaseShardingStrategy(actual.getTableRuleByLogicTableName("logic_Table")), instanceOf(NoneShardingStrategy.class));
    }
    
    @Test
    public void assertGetDatabaseShardingStrategyWithDefaultDataSource() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.setDefaultDataSourceName("ds0");
        TableRuleConfiguration tableRuleConfig = createTableRuleConfigWithDatabaseShardingStrategy(new NoneShardingStrategyConfiguration());
        shardingRuleConfig.getTableRuleConfigs().add(tableRuleConfig);
        ShardingRule actual = new ShardingRule(shardingRuleConfig, createDataSourceNames());
        assertThat(actual.getDatabaseShardingStrategy(actual.getTableRuleByLogicTableName("other_Table")), instanceOf(NoneShardingStrategy.class));
    }
    
    @Test(expected = ShardingConfigurationException.class)
    public void assertGetNoDatabaseShardingStrategy() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        TableRuleConfiguration tableRuleConfig = createTableRuleConfigWithDatabaseShardingStrategy(new NoneShardingStrategyConfiguration());
        shardingRuleConfig.getTableRuleConfigs().add(tableRuleConfig);
        ShardingRule actual = new ShardingRule(shardingRuleConfig, createDataSourceNames());
        actual.getDatabaseShardingStrategy(actual.getTableRuleByLogicTableName("other_Table"));
    }
    
    @Test
    public void assertGetDatabaseShardingStrategyForNullValue() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        TableRuleConfiguration tableRuleConfig = createTableRuleConfig();
        shardingRuleConfig.getTableRuleConfigs().add(tableRuleConfig);
        shardingRuleConfig.setDefaultDatabaseShardingStrategyConfig(null);
        ShardingRule actual = new ShardingRule(shardingRuleConfig, createDataSourceNames());
        assertNotNull(actual.getDatabaseShardingStrategy(actual.getTableRuleByLogicTableName("logic_Table")));
    }
    
    @Test
    public void assertGetTableShardingStrategyFromTableRule() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        TableRuleConfiguration tableRuleConfig = createTableRuleConfigWithTableShardingStrategy(new NoneShardingStrategyConfiguration());
        shardingRuleConfig.getTableRuleConfigs().add(tableRuleConfig);
        ShardingRule actual = new ShardingRule(shardingRuleConfig, createDataSourceNames());
        assertThat(actual.getDatabaseShardingStrategy(actual.getTableRuleByLogicTableName("logic_Table")), instanceOf(NoneShardingStrategy.class));
    }
    
    @Test
    public void assertGetTableShardingStrategyFromDefault() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        TableRuleConfiguration tableRuleConfig = createTableRuleConfig();
        shardingRuleConfig.getTableRuleConfigs().add(tableRuleConfig);
        shardingRuleConfig.setDefaultDatabaseShardingStrategyConfig(new NoneShardingStrategyConfiguration());
        ShardingRule actual = new ShardingRule(shardingRuleConfig, createDataSourceNames());
        assertThat(actual.getTableShardingStrategy(actual.getTableRuleByLogicTableName("logic_Table")), instanceOf(NoneShardingStrategy.class));
    }
    
    @Test
    public void assertGetTableShardingStrategyForNullValue() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        TableRuleConfiguration tableRuleConfig = createTableRuleConfig();
        shardingRuleConfig.getTableRuleConfigs().add(tableRuleConfig);
        shardingRuleConfig.setDefaultDatabaseShardingStrategyConfig(new NoneShardingStrategyConfiguration());
        ShardingRule actual = new ShardingRule(shardingRuleConfig, createDataSourceNames());
        assertNotNull(actual.getTableShardingStrategy(actual.getTableRuleByLogicTableName("logic_Table")));
    }
    
    @Test
    public void assertGetBindingTableRuleForNotConfig() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        TableRuleConfiguration tableRuleConfig = createTableRuleConfig();
        shardingRuleConfig.getTableRuleConfigs().add(tableRuleConfig);
        ShardingRule actual = new ShardingRule(shardingRuleConfig, createDataSourceNames());
        assertFalse(actual.findBindingTableRule("logic_Table").isPresent());
    }
    
    @Test
    public void assertGetBindingTableRuleForNotFound() {
        assertFalse(createShardingRule().findBindingTableRule("new_Table").isPresent());
    }
    
    @Test
    public void assertGetBindingTableRuleForFound() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        TableRuleConfiguration tableRuleConfig = createTableRuleConfig();
        TableRuleConfiguration subTableRuleConfig = createSubTableRuleConfig();
        shardingRuleConfig.getTableRuleConfigs().add(tableRuleConfig);
        shardingRuleConfig.getTableRuleConfigs().add(subTableRuleConfig);
        shardingRuleConfig.getBindingTableGroups().add(createTableRuleConfig().getLogicTable() + "," + createSubTableRuleConfig().getLogicTable());
        ShardingRule actual = new ShardingRule(shardingRuleConfig, createDataSourceNames());
        assertTrue(actual.findBindingTableRule("logic_Table").isPresent());
        assertThat(actual.findBindingTableRule("logic_Table").get().getTableRules().size(), is(2));
    }
    
    @Test
    public void assertIsAllBindingTableWhenLogicTablesIsEmpty() {
        assertFalse(createShardingRule().isAllBindingTables(Collections.<String>emptyList()));
    }
    
    @Test
    public void assertIsNotAllBindingTable() {
        assertFalse(createShardingRule().isAllBindingTables(Collections.singletonList("new_Table")));
        assertFalse(createShardingRule().isAllBindingTables(Arrays.asList("logic_Table", "new_Table")));
    }
    
    @Test
    public void assertIsAllBindingTable() {
        assertTrue(createShardingRule().isAllBindingTables(Collections.singletonList("logic_Table")));
        assertTrue(createShardingRule().isAllBindingTables(Collections.singletonList("logic_table")));
        assertTrue(createShardingRule().isAllBindingTables(Collections.singletonList("sub_Logic_Table")));
        assertTrue(createShardingRule().isAllBindingTables(Collections.singletonList("sub_logic_table")));
        assertTrue(createShardingRule().isAllBindingTables(Arrays.asList("logic_Table", "sub_Logic_Table")));
        assertTrue(createShardingRule().isAllBindingTables(Arrays.asList("logic_table", "sub_logic_Table")));
        assertFalse(createShardingRule().isAllBindingTables(Arrays.asList("logic_table", "sub_logic_Table", "new_table")));
        assertFalse(createShardingRule().isAllBindingTables(Collections.<String>emptyList()));
        assertFalse(createShardingRule().isAllBindingTables(Collections.singletonList("new_Table")));
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
        shardingRuleConfig.setDefaultDataSourceName("ds3");
        ShardingRule actual = new ShardingRule(shardingRuleConfig, createDataSourceNames());
        assertThat(actual.getShardingDataSourceNames().getDefaultDataSourceName(), is("ds3"));
    }
    
    @Test
    public void assertDataSourceNameFromDataSourceNames() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.setDefaultDataSourceName("ds3");
        assertThat(new ShardingRule(shardingRuleConfig, Collections.singletonList("ds0")).getShardingDataSourceNames().getDefaultDataSourceName(), is("ds0"));
    }
    
    @Test
    public void assertFindTableRuleByActualTable() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        TableRuleConfiguration tableRuleConfig = createTableRuleConfig();
        shardingRuleConfig.getTableRuleConfigs().add(tableRuleConfig);
        ShardingRule actual = new ShardingRule(shardingRuleConfig, createDataSourceNames());
        assertTrue(actual.findTableRuleByActualTable("table_0").isPresent());
        assertFalse(actual.findTableRuleByActualTable("table_3").isPresent());
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
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        TableRuleConfiguration tableRuleConfig = createTableRuleConfig();
        shardingRuleConfig.getTableRuleConfigs().add(tableRuleConfig);
        ShardingRule actual = new ShardingRule(shardingRuleConfig, createDataSourceNames());
        assertTrue(actual.isAllInDefaultDataSource(Collections.singletonList("table_0")));
        assertFalse(actual.isAllInDefaultDataSource(Collections.singletonList("logic_table")));
    }
    
    @Test
    public void assertGetGenerateKeyColumn() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        TableRuleConfiguration tableRuleConfig = createTableRuleConfig();
        shardingRuleConfig.getTableRuleConfigs().add(tableRuleConfig);
        ShardingRule actual = new ShardingRule(shardingRuleConfig, createDataSourceNames());
        assertFalse(actual.getGenerateKeyColumn("table_0").isPresent());
        assertFalse(actual.getGenerateKeyColumn("logic_table").isPresent());
    }
    
    @Test(expected = ShardingConfigurationException.class)
    public void assertGenerateKeyFailure() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        TableRuleConfiguration tableRuleConfig = createTableRuleConfig();
        shardingRuleConfig.getTableRuleConfigs().add(tableRuleConfig);
        new ShardingRule(shardingRuleConfig, createDataSourceNames()).generateKey("table_0");
    }
    
    @Test
    public void assertGenerateKeyWithDefaultKeyGenerator() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        TableRuleConfiguration tableRuleConfig = createTableRuleConfig();
        shardingRuleConfig.getTableRuleConfigs().add(tableRuleConfig);
        assertThat(new ShardingRule(shardingRuleConfig, createDataSourceNames()).generateKey("logic_table"), instanceOf(Long.class));
    }
     
    @Test
    public void assertGenerateKeyWithKeyGenerator() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        TableRuleConfiguration tableRuleConfig = createTableRuleConfig();
        tableRuleConfig.setKeyGenerator(new IncrementKeyGenerator());
        shardingRuleConfig.getTableRuleConfigs().add(tableRuleConfig);
        assertThat(new ShardingRule(shardingRuleConfig, createDataSourceNames()).generateKey("logic_table"), instanceOf(Integer.class));
    
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
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        TableRuleConfiguration tableRuleConfig = createTableRuleConfig();
        shardingRuleConfig.getTableRuleConfigs().add(tableRuleConfig);
        assertThat(new ShardingRule(shardingRuleConfig, createDataSourceNames()).findDataNode("logic_table").getDataSourceName(), is("ds0"));
    }
    
    @Test(expected = ShardingConfigurationException.class)
    public void assertFindDataNodeByLogicTableFailure() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.setDefaultDataSourceName("ds3");
        TableRuleConfiguration tableRuleConfig = createTableRuleConfig();
        shardingRuleConfig.getTableRuleConfigs().add(tableRuleConfig);
        new ShardingRule(shardingRuleConfig, createDataSourceNames()).findDataNode("logic_table_x");
    }
    
    @Test(expected = ShardingConfigurationException.class)
    public void assertFindDataNodeByLogicTableFailureWithDataSourceName() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        TableRuleConfiguration tableRuleConfig = createTableRuleConfig();
        shardingRuleConfig.getTableRuleConfigs().add(tableRuleConfig);
        assertThat(new ShardingRule(shardingRuleConfig, createDataSourceNames()).findDataNode("ds3", "logic_table").getDataSourceName(), is("ds0"));
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
        assertTrue(createShardingRule().contains("LOGIC_TABLE"));
    }
    
    @Test
    public void assertContainsWithBindingTableRule() {
        assertTrue(createShardingRule().contains("SUB_LOGIC_TABLE"));
    }
    
    @Test
    public void assertContainsWithBroadcastTableRule() {
        assertTrue(createShardingRule().contains("BROADCAST_LOGIC_TABLE"));
    }
    
    @Test
    public void assertNotContains() {
        assertFalse(createShardingRule().contains("NEW_TABLE"));
    }
    
    private ShardingRule createShardingRule() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        TableRuleConfiguration tableRuleConfig = createTableRuleConfig();
        TableRuleConfiguration subTableRuleConfig = createSubTableRuleConfig();
        shardingRuleConfig.getTableRuleConfigs().add(tableRuleConfig);
        shardingRuleConfig.getTableRuleConfigs().add(subTableRuleConfig);
        shardingRuleConfig.getBindingTableGroups().add(createTableRuleConfig().getLogicTable() + "," + createSubTableRuleConfig().getLogicTable());
        shardingRuleConfig.getBroadcastTables().add("BROADCAST_LOGIC_TABLE");
        return new ShardingRule(shardingRuleConfig, createDataSourceNames());
    }
    
    private Collection<String> createDataSourceNames() {
        return Arrays.asList("ds0", "ds1");
    }
    
    private TableRuleConfiguration createTableRuleConfig() {
        TableRuleConfiguration result = new TableRuleConfiguration();
        result.setLogicTable("LOGIC_TABLE");
        result.setActualDataNodes("ds${0..1}.table_${0..2}");
        return result;
    }
    
    private TableRuleConfiguration createSubTableRuleConfig() {
        TableRuleConfiguration result = new TableRuleConfiguration();
        result.setLogicTable("SUB_LOGIC_TABLE");
        result.setActualDataNodes("ds${0..1}.sub_table_${0..2}");
        return result;
    }
    
    private TableRuleConfiguration createTableRuleConfigWithLogicIndex() {
        TableRuleConfiguration result = new TableRuleConfiguration();
        result.setLogicTable("LOGIC_TABLE");
        result.setLogicIndex("INDEX_TABLE");
        result.setActualDataNodes("ds${0..1}.table_${0..2}");
        return result;
    }
    
    private TableRuleConfiguration createTableRuleConfigWithDatabaseShardingStrategy(final ShardingStrategyConfiguration strategyConfig) {
        TableRuleConfiguration result = new TableRuleConfiguration();
        result.setLogicTable("LOGIC_TABLE");
        result.setActualDataNodes("ds${0..1}.table_${0..2}");
        result.setDatabaseShardingStrategyConfig(strategyConfig);
        return result;
    }
    
    private TableRuleConfiguration createTableRuleConfigWithTableShardingStrategy(final ShardingStrategyConfiguration strategyConfig) {
        TableRuleConfiguration result = new TableRuleConfiguration();
        result.setLogicTable("LOGIC_TABLE");
        result.setActualDataNodes("ds${0..1}.table_${0..2}");
        result.setTableShardingStrategyConfig(strategyConfig);
        return result;
    }
    
    private TableRuleConfiguration createTableRuleConfigWithAllStrategies() {
        TableRuleConfiguration result = new TableRuleConfiguration();
        result.setLogicTable("LOGIC_TABLE");
        result.setActualDataNodes("ds${0..1}.table_${0..2}");
        result.setDatabaseShardingStrategyConfig(new StandardShardingStrategyConfiguration("column", new TestPreciseShardingAlgorithm()));
        result.setTableShardingStrategyConfig(new NoneShardingStrategyConfiguration());
        return result;
    }
    
    private TableRuleConfiguration createTableRuleConfigWithTableStrategies() {
        TableRuleConfiguration result = new TableRuleConfiguration();
        result.setLogicTable("LOGIC_TABLE");
        result.setActualDataNodes("ds${0..1}.table_${0..2}");
        result.setTableShardingStrategyConfig(new StandardShardingStrategyConfiguration("column", new TestPreciseShardingAlgorithm()));
        return result;
    }
}
