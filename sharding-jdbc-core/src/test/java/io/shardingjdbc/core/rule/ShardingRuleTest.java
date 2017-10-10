/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingjdbc.core.rule;

import io.shardingjdbc.core.api.config.ShardingRuleConfiguration;
import io.shardingjdbc.core.api.config.TableRuleConfiguration;
import io.shardingjdbc.core.api.config.strategy.NoneShardingStrategyConfiguration;
import io.shardingjdbc.core.api.config.strategy.ShardingStrategyConfiguration;
import io.shardingjdbc.core.api.config.strategy.StandardShardingStrategyConfiguration;
import io.shardingjdbc.core.api.algorithm.fixture.TestPreciseShardingAlgorithm;
import io.shardingjdbc.core.parsing.parser.context.condition.Column;
import io.shardingjdbc.core.routing.strategy.none.NoneShardingStrategy;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class ShardingRuleTest {
    
    @Test
    public void assertShardingRuleWithoutStrategy() throws SQLException {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        TableRuleConfiguration tableRuleConfig = createTableRuleConfig();
        shardingRuleConfig.getTableRuleConfigs().add(tableRuleConfig);
        ShardingRule actual = shardingRuleConfig.build(createDataSourceMap());
        assertTrue(actual.getDefaultDatabaseShardingStrategy().getShardingColumns().isEmpty());
        assertTrue(actual.getDefaultTableShardingStrategy().getShardingColumns().isEmpty());
    }
    
    @Test
    public void assertShardingRuleWithBindingTableRuleWithoutStrategy() throws SQLException {
        ShardingRule actual = createShardingRule();
        assertTrue(actual.getDefaultDatabaseShardingStrategy().getShardingColumns().isEmpty());
        assertTrue(actual.getDefaultTableShardingStrategy().getShardingColumns().isEmpty());
    }
    
    @Test
    public void assertShardingRuleWithDatabaseStrategy() throws SQLException {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        TableRuleConfiguration tableRuleConfig = createTableRuleConfig();
        shardingRuleConfig.getTableRuleConfigs().add(tableRuleConfig);
        shardingRuleConfig.setDefaultDatabaseShardingStrategyConfig(new NoneShardingStrategyConfiguration());
        ShardingRule actual = shardingRuleConfig.build(createDataSourceMap());
        assertTrue(actual.getDefaultDatabaseShardingStrategy().getShardingColumns().isEmpty());
        assertTrue(actual.getDefaultTableShardingStrategy().getShardingColumns().isEmpty());
    }
    
    @Test
    public void assertShardingRuleWithTableStrategy() throws SQLException {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        TableRuleConfiguration tableRuleConfig = createTableRuleConfig();
        shardingRuleConfig.getTableRuleConfigs().add(tableRuleConfig);
        shardingRuleConfig.setDefaultTableShardingStrategyConfig(new NoneShardingStrategyConfiguration());
        ShardingRule actual = shardingRuleConfig.build(createDataSourceMap());
        assertTrue(actual.getDefaultDatabaseShardingStrategy().getShardingColumns().isEmpty());
        assertTrue(actual.getDefaultDatabaseShardingStrategy().getShardingColumns().isEmpty());
    }
    
    @Test
    public void assertShardingRuleWithoutBindingTableRule() throws SQLException {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        TableRuleConfiguration tableRuleConfig = createTableRuleConfig();
        shardingRuleConfig.getTableRuleConfigs().add(tableRuleConfig);
        shardingRuleConfig.setDefaultDatabaseShardingStrategyConfig(new NoneShardingStrategyConfiguration());
        shardingRuleConfig.setDefaultTableShardingStrategyConfig(new NoneShardingStrategyConfiguration());
        ShardingRule actual = shardingRuleConfig.build(createDataSourceMap());
        assertTrue(actual.getDefaultDatabaseShardingStrategy().getShardingColumns().isEmpty());
        assertTrue(actual.getDefaultDatabaseShardingStrategy().getShardingColumns().isEmpty());
    }
    
    @Test
    public void assertFindTableRule() throws SQLException {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        TableRuleConfiguration tableRuleConfig = createTableRuleConfig();
        shardingRuleConfig.getTableRuleConfigs().add(tableRuleConfig);
        shardingRuleConfig.setDefaultDatabaseShardingStrategyConfig(new NoneShardingStrategyConfiguration());
        shardingRuleConfig.setDefaultTableShardingStrategyConfig(new NoneShardingStrategyConfiguration());
        ShardingRule actual = shardingRuleConfig.build(createDataSourceMap());
        assertTrue(actual.tryFindTableRule("logicTable").isPresent());
        assertFalse(actual.tryFindTableRule("null").isPresent());
    }
    
    @Test
    public void assertGetDatabaseShardingStrategyFromTableRule() throws SQLException {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        TableRuleConfiguration tableRuleConfig = createTableRuleConfigWithDatabaseShardingStrategy(new NoneShardingStrategyConfiguration());
        shardingRuleConfig.getTableRuleConfigs().add(tableRuleConfig);
        ShardingRule actual = shardingRuleConfig.build(createDataSourceMap());
        assertThat(actual.getDatabaseShardingStrategy(actual.getTableRule("logicTable")), instanceOf(NoneShardingStrategy.class));
    }
    
    @Test
    public void assertGetDatabaseShardingStrategyFromDefault() throws SQLException {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        TableRuleConfiguration tableRuleConfig = createTableRuleConfig();
        shardingRuleConfig.getTableRuleConfigs().add(tableRuleConfig);
        shardingRuleConfig.setDefaultDatabaseShardingStrategyConfig(new NoneShardingStrategyConfiguration());
        ShardingRule actual = shardingRuleConfig.build(createDataSourceMap());
        assertThat(actual.getDatabaseShardingStrategy(actual.getTableRule("logicTable")), instanceOf(NoneShardingStrategy.class));
    }
    
    @Test
    public void assertGetDatabaseShardingStrategyForNullValue() throws SQLException {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        TableRuleConfiguration tableRuleConfig = createTableRuleConfig();
        shardingRuleConfig.getTableRuleConfigs().add(tableRuleConfig);
        shardingRuleConfig.setDefaultDatabaseShardingStrategyConfig(null);
        ShardingRule actual = shardingRuleConfig.build(createDataSourceMap());
        assertNotNull(actual.getDatabaseShardingStrategy(actual.getTableRule("logicTable")));
    }
    
    @Test
    public void assertGetTableShardingStrategyFromTableRule() throws SQLException {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        TableRuleConfiguration tableRuleConfig = createTableRuleConfigWithTableShardingStrategy(new NoneShardingStrategyConfiguration());
        shardingRuleConfig.getTableRuleConfigs().add(tableRuleConfig);
        ShardingRule actual = shardingRuleConfig.build(createDataSourceMap());
        assertThat(actual.getDatabaseShardingStrategy(actual.getTableRule("logicTable")), instanceOf(NoneShardingStrategy.class));
    }
    
    @Test
    public void assertGetTableShardingStrategyFromDefault() throws SQLException {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        TableRuleConfiguration tableRuleConfig = createTableRuleConfig();
        shardingRuleConfig.getTableRuleConfigs().add(tableRuleConfig);
        shardingRuleConfig.setDefaultDatabaseShardingStrategyConfig(new NoneShardingStrategyConfiguration());
        ShardingRule actual = shardingRuleConfig.build(createDataSourceMap());
        assertThat(actual.getTableShardingStrategy(actual.getTableRule("logicTable")), instanceOf(NoneShardingStrategy.class));
    }
    
    @Test
    public void assertGetTableShardingStrategyForNullValue() throws SQLException {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        TableRuleConfiguration tableRuleConfig = createTableRuleConfig();
        shardingRuleConfig.getTableRuleConfigs().add(tableRuleConfig);
        shardingRuleConfig.setDefaultDatabaseShardingStrategyConfig(new NoneShardingStrategyConfiguration());
        ShardingRule actual = shardingRuleConfig.build(createDataSourceMap());
        assertNotNull(actual.getTableShardingStrategy(actual.getTableRule("logicTable")));
    }
    
    @Test
    public void assertGetBindingTableRuleForNotConfig() throws SQLException {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        TableRuleConfiguration tableRuleConfig = createTableRuleConfig();
        shardingRuleConfig.getTableRuleConfigs().add(tableRuleConfig);
        ShardingRule actual = shardingRuleConfig.build(createDataSourceMap());
        assertFalse(actual.findBindingTableRule("logicTable").isPresent());
    }
    
    @Test
    public void assertGetBindingTableRuleForNotFound() throws SQLException {
        assertFalse(createShardingRule().findBindingTableRule("newTable").isPresent());
    }
    
    @Test
    public void assertGetBindingTableRuleForFound() throws SQLException {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        TableRuleConfiguration tableRuleConfig = createTableRuleConfig();
        TableRuleConfiguration subTableRuleConfig = createSubTableRuleConfig();
        shardingRuleConfig.getTableRuleConfigs().add(tableRuleConfig);
        shardingRuleConfig.getTableRuleConfigs().add(subTableRuleConfig);
        shardingRuleConfig.getBindingTableGroups().add(createTableRuleConfig().getLogicTable() + "," + createSubTableRuleConfig().getLogicTable());
        ShardingRule actual = shardingRuleConfig.build(createDataSourceMap());
        assertThat(actual.findBindingTableRule("logicTable").get().getTableRules().size(), is(2));
    }
    
    @Test
    public void assertFilterAllBindingTablesWhenLogicTablesIsEmpty() throws SQLException {
        assertThat(createShardingRule().filterAllBindingTables(Collections.<String>emptyList()), is((Collection<String>) Collections.<String>emptyList()));
    }
    
    @Test
    public void assertFilterAllBindingTablesWhenBindingTableRuleIsNotFound() throws SQLException {
        assertThat(createShardingRule().filterAllBindingTables(Collections.singletonList("newTable")), is((Collection<String>) Collections.<String>emptyList()));
    }
    
    @Test
    public void assertFilterAllBindingTables() throws SQLException {
        assertThat(createShardingRule().filterAllBindingTables(Collections.singletonList("logicTable")), is((Collection<String>) Collections.singletonList("logicTable")));
        assertThat(createShardingRule().filterAllBindingTables(Collections.singletonList("subLogicTable")), is((Collection<String>) Collections.singletonList("subLogicTable")));
        assertThat(createShardingRule().filterAllBindingTables(Arrays.asList("logicTable", "subLogicTable")), is((Collection<String>) Arrays.asList("logicTable", "subLogicTable")));
        assertThat(createShardingRule().filterAllBindingTables(Arrays.asList("logicTable", "newTable", "subLogicTable")), is((Collection<String>) Arrays.asList("logicTable", "subLogicTable")));
    }
    
    @Test
    public void assertIsAllBindingTableWhenLogicTablesIsEmpty() throws SQLException {
        assertFalse(createShardingRule().isAllBindingTables(Collections.<String>emptyList()));
    }
    
    @Test
    public void assertIsNotAllBindingTable() throws SQLException {
        assertFalse(createShardingRule().isAllBindingTables(Collections.singletonList("newTable")));
        assertFalse(createShardingRule().isAllBindingTables(Arrays.asList("logicTable", "newTable")));
    }
    
    @Test
    public void assertIsAllBindingTable() throws SQLException {
        assertTrue(createShardingRule().isAllBindingTables(Collections.singletonList("logicTable")));
        assertTrue(createShardingRule().isAllBindingTables(Collections.singletonList("subLogicTable")));
        assertTrue(createShardingRule().isAllBindingTables(Arrays.asList("logicTable", "subLogicTable")));
    }
    
    @Test
    public void assertIsShardingColumnForDefaultDatabaseShardingStrategy() throws SQLException {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.getTableRuleConfigs().add(createTableRuleConfigWithAllStrategies());
        shardingRuleConfig.setDefaultDatabaseShardingStrategyConfig(new StandardShardingStrategyConfiguration("column", TestPreciseShardingAlgorithm.class.getName()));
        assertTrue(shardingRuleConfig.build(createDataSourceMap()).isShardingColumn(new Column("column", "")));
    }
    
    @Test
    public void assertIsShardingColumnForDefaultTableShardingStrategy() throws SQLException {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.getTableRuleConfigs().add(createTableRuleConfigWithAllStrategies());
        shardingRuleConfig.setDefaultTableShardingStrategyConfig(new StandardShardingStrategyConfiguration("column", TestPreciseShardingAlgorithm.class.getName()));
        assertTrue(shardingRuleConfig.build(createDataSourceMap()).isShardingColumn(new Column("column", "")));
    }
    
    @Test
    public void assertIsShardingColumnForDatabaseShardingStrategy() throws SQLException {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.getTableRuleConfigs().add(createTableRuleConfigWithAllStrategies());
        assertTrue(shardingRuleConfig.build(createDataSourceMap()).isShardingColumn(new Column("column", "logicTable")));
    }
    
    @Test
    public void assertIsShardingColumnForTableShardingStrategy() throws SQLException {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.getTableRuleConfigs().add(createTableRuleConfigWithTableStrategies());
        assertTrue(shardingRuleConfig.build(createDataSourceMap()).isShardingColumn(new Column("column", "logicTable")));
    }
    
    @Test
    public void assertIsNotShardingColumn() throws SQLException {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.getTableRuleConfigs().add(createTableRuleConfigWithAllStrategies());
        assertFalse(shardingRuleConfig.build(createDataSourceMap()).isShardingColumn(new Column("column", "otherTable")));
    }
    
    private ShardingRule createShardingRule() throws SQLException {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        TableRuleConfiguration tableRuleConfig = createTableRuleConfig();
        TableRuleConfiguration subTableRuleConfig = createSubTableRuleConfig();
        shardingRuleConfig.getTableRuleConfigs().add(tableRuleConfig);
        shardingRuleConfig.getTableRuleConfigs().add(subTableRuleConfig);
        shardingRuleConfig.getBindingTableGroups().add(createTableRuleConfig().getLogicTable() + "," + createSubTableRuleConfig().getLogicTable());
        return shardingRuleConfig.build(createDataSourceMap());
    }
    
    private Map<String, DataSource> createDataSourceMap() {
        Map<String, DataSource> result = new HashMap<>(2, 1);
        result.put("ds0", null);
        result.put("ds1", null);
        return result;
    }
    
    private TableRuleConfiguration createTableRuleConfig() {
        TableRuleConfiguration result = new TableRuleConfiguration();
        result.setLogicTable("logicTable");
        result.setActualDataNodes("ds${0..1}.table_${0..2}");
        return result;
    }
    
    private TableRuleConfiguration createTableRuleConfigWithDatabaseShardingStrategy(final ShardingStrategyConfiguration strategyConfig) {
        TableRuleConfiguration result = new TableRuleConfiguration();
        result.setLogicTable("logicTable");
        result.setActualDataNodes("ds${0..1}.table_${0..2}");
        result.setDatabaseShardingStrategyConfig(strategyConfig);
        return result;
    }
    
    private TableRuleConfiguration createTableRuleConfigWithTableShardingStrategy(final ShardingStrategyConfiguration strategyConfig) {
        TableRuleConfiguration result = new TableRuleConfiguration();
        result.setLogicTable("logicTable");
        result.setActualDataNodes("ds${0..1}.table_${0..2}");
        result.setTableShardingStrategyConfig(strategyConfig);
        return result;
    }
    
    private TableRuleConfiguration createSubTableRuleConfig() {
        TableRuleConfiguration result = new TableRuleConfiguration();
        result.setLogicTable("subLogicTable");
        result.setActualDataNodes("ds${0..1}.sub_table_${0..2}");
        return result;
    }
    
    private TableRuleConfiguration createTableRuleConfigWithAllStrategies() {
        TableRuleConfiguration result = new TableRuleConfiguration();
        result.setLogicTable("logicTable");
        result.setActualDataNodes("ds${0..1}.table_${0..2}");
        result.setDatabaseShardingStrategyConfig(new StandardShardingStrategyConfiguration("column", TestPreciseShardingAlgorithm.class.getName()));
        result.setTableShardingStrategyConfig(new NoneShardingStrategyConfiguration());
        return result;
    }
    
    private TableRuleConfiguration createTableRuleConfigWithTableStrategies() {
        TableRuleConfiguration result = new TableRuleConfiguration();
        result.setLogicTable("logicTable");
        result.setActualDataNodes("ds${0..1}.table_${0..2}");
        result.setTableShardingStrategyConfig(new StandardShardingStrategyConfiguration("column", TestPreciseShardingAlgorithm.class.getName()));
        return result;
    }
}
