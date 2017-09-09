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

package com.dangdang.ddframe.rdb.sharding.api.rule;

import com.dangdang.ddframe.rdb.sharding.api.config.ShardingRuleConfig;
import com.dangdang.ddframe.rdb.sharding.api.config.TableRuleConfig;
import com.dangdang.ddframe.rdb.sharding.api.config.strategy.NoneShardingStrategyConfig;
import com.dangdang.ddframe.rdb.sharding.api.config.strategy.ShardingStrategyConfig;
import com.dangdang.ddframe.rdb.sharding.api.config.strategy.StandardShardingStrategyConfig;
import com.dangdang.ddframe.rdb.sharding.api.strategy.fixture.TestPreciseShardingAlgorithm;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.condition.Column;
import com.dangdang.ddframe.rdb.sharding.routing.strategy.none.NoneShardingStrategy;
import org.junit.Test;

import javax.sql.DataSource;
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
    public void assertShardingRuleWithoutStrategy() {
        ShardingRuleConfig shardingRuleConfig = new ShardingRuleConfig();
        shardingRuleConfig.setDataSources(createDataSourceMap());
        TableRuleConfig tableRuleConfig = createTableRuleConfig();
        shardingRuleConfig.getTableRuleConfigs().add(tableRuleConfig);
        shardingRuleConfig.setDataSources(createDataSourceMap());
        ShardingRule actual = shardingRuleConfig.build();
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
        ShardingRuleConfig shardingRuleConfig = new ShardingRuleConfig();
        shardingRuleConfig.setDataSources(createDataSourceMap());
        TableRuleConfig tableRuleConfig = createTableRuleConfig();
        shardingRuleConfig.getTableRuleConfigs().add(tableRuleConfig);
        shardingRuleConfig.setDataSources(createDataSourceMap());
        shardingRuleConfig.setDefaultDatabaseShardingStrategyConfig(new NoneShardingStrategyConfig());
        ShardingRule actual = shardingRuleConfig.build();
        assertTrue(actual.getDefaultDatabaseShardingStrategy().getShardingColumns().isEmpty());
        assertTrue(actual.getDefaultTableShardingStrategy().getShardingColumns().isEmpty());
    }
    
    @Test
    public void assertShardingRuleWithTableStrategy() {
        ShardingRuleConfig shardingRuleConfig = new ShardingRuleConfig();
        shardingRuleConfig.setDataSources(createDataSourceMap());
        TableRuleConfig tableRuleConfig = createTableRuleConfig();
        shardingRuleConfig.getTableRuleConfigs().add(tableRuleConfig);
        shardingRuleConfig.setDataSources(createDataSourceMap());
        shardingRuleConfig.setDefaultTableShardingStrategyConfig(new NoneShardingStrategyConfig());
        ShardingRule actual = shardingRuleConfig.build();
        assertTrue(actual.getDefaultDatabaseShardingStrategy().getShardingColumns().isEmpty());
        assertTrue(actual.getDefaultDatabaseShardingStrategy().getShardingColumns().isEmpty());
    }
    
    @Test
    public void assertShardingRuleWithoutBindingTableRule() {
        ShardingRuleConfig shardingRuleConfig = new ShardingRuleConfig();
        shardingRuleConfig.setDataSources(createDataSourceMap());
        TableRuleConfig tableRuleConfig = createTableRuleConfig();
        shardingRuleConfig.getTableRuleConfigs().add(tableRuleConfig);
        shardingRuleConfig.setDataSources(createDataSourceMap());
        shardingRuleConfig.setDefaultDatabaseShardingStrategyConfig(new NoneShardingStrategyConfig());
        shardingRuleConfig.setDefaultTableShardingStrategyConfig(new NoneShardingStrategyConfig());
        ShardingRule actual = shardingRuleConfig.build();
        assertTrue(actual.getDefaultDatabaseShardingStrategy().getShardingColumns().isEmpty());
        assertTrue(actual.getDefaultDatabaseShardingStrategy().getShardingColumns().isEmpty());
    }
    
    @Test
    public void assertFindTableRule() {
        ShardingRuleConfig shardingRuleConfig = new ShardingRuleConfig();
        shardingRuleConfig.setDataSources(createDataSourceMap());
        TableRuleConfig tableRuleConfig = createTableRuleConfig();
        shardingRuleConfig.getTableRuleConfigs().add(tableRuleConfig);
        shardingRuleConfig.setDataSources(createDataSourceMap());
        shardingRuleConfig.setDefaultDatabaseShardingStrategyConfig(new NoneShardingStrategyConfig());
        shardingRuleConfig.setDefaultTableShardingStrategyConfig(new NoneShardingStrategyConfig());
        ShardingRule actual = shardingRuleConfig.build();
        assertTrue(actual.tryFindTableRule("logicTable").isPresent());
        assertFalse(actual.tryFindTableRule("null").isPresent());
    }
    
    @Test
    public void assertGetDatabaseShardingStrategyFromTableRule() {
        ShardingRuleConfig shardingRuleConfig = new ShardingRuleConfig();
        shardingRuleConfig.setDataSources(createDataSourceMap());
        TableRuleConfig tableRuleConfig = createTableRuleConfigWithDatabaseShardingStrategy(new NoneShardingStrategyConfig());
        shardingRuleConfig.getTableRuleConfigs().add(tableRuleConfig);
        shardingRuleConfig.setDataSources(createDataSourceMap());
        ShardingRule actual = shardingRuleConfig.build();
        assertThat(actual.getDatabaseShardingStrategy(actual.getTableRule("logicTable")), instanceOf(NoneShardingStrategy.class));
    }
    
    @Test
    public void assertGetDatabaseShardingStrategyFromDefault() {
        ShardingRuleConfig shardingRuleConfig = new ShardingRuleConfig();
        shardingRuleConfig.setDataSources(createDataSourceMap());
        TableRuleConfig tableRuleConfig = createTableRuleConfig();
        shardingRuleConfig.getTableRuleConfigs().add(tableRuleConfig);
        shardingRuleConfig.setDataSources(createDataSourceMap());
        shardingRuleConfig.setDefaultDatabaseShardingStrategyConfig(new NoneShardingStrategyConfig());
        ShardingRule actual = shardingRuleConfig.build();
        assertThat(actual.getDatabaseShardingStrategy(actual.getTableRule("logicTable")), instanceOf(NoneShardingStrategy.class));
    }
    
    @Test
    public void assertGetDatabaseShardingStrategyForNullValue() {
        ShardingRuleConfig shardingRuleConfig = new ShardingRuleConfig();
        shardingRuleConfig.setDataSources(createDataSourceMap());
        TableRuleConfig tableRuleConfig = createTableRuleConfig();
        shardingRuleConfig.getTableRuleConfigs().add(tableRuleConfig);
        shardingRuleConfig.setDataSources(createDataSourceMap());
        shardingRuleConfig.setDefaultDatabaseShardingStrategyConfig(null);
        ShardingRule actual = shardingRuleConfig.build();
        assertNotNull(actual.getDatabaseShardingStrategy(actual.getTableRule("logicTable")));
    }
    
    @Test
    public void assertGetTableShardingStrategyFromTableRule() {
        ShardingRuleConfig shardingRuleConfig = new ShardingRuleConfig();
        shardingRuleConfig.setDataSources(createDataSourceMap());
        TableRuleConfig tableRuleConfig = createTableRuleConfigWithTableShardingStrategy(new NoneShardingStrategyConfig());
        shardingRuleConfig.getTableRuleConfigs().add(tableRuleConfig);
        shardingRuleConfig.setDataSources(createDataSourceMap());
        ShardingRule actual = shardingRuleConfig.build();
        assertThat(actual.getDatabaseShardingStrategy(actual.getTableRule("logicTable")), instanceOf(NoneShardingStrategy.class));
    }
    
    @Test
    public void assertGetTableShardingStrategyFromDefault() {
        ShardingRuleConfig shardingRuleConfig = new ShardingRuleConfig();
        shardingRuleConfig.setDataSources(createDataSourceMap());
        TableRuleConfig tableRuleConfig = createTableRuleConfig();
        shardingRuleConfig.getTableRuleConfigs().add(tableRuleConfig);
        shardingRuleConfig.setDataSources(createDataSourceMap());
        shardingRuleConfig.setDefaultDatabaseShardingStrategyConfig(new NoneShardingStrategyConfig());
        ShardingRule actual = shardingRuleConfig.build();
        assertThat(actual.getTableShardingStrategy(actual.getTableRule("logicTable")), instanceOf(NoneShardingStrategy.class));
    }
    
    @Test
    public void assertGetTableShardingStrategyForNullValue() {
        ShardingRuleConfig shardingRuleConfig = new ShardingRuleConfig();
        shardingRuleConfig.setDataSources(createDataSourceMap());
        TableRuleConfig tableRuleConfig = createTableRuleConfig();
        shardingRuleConfig.getTableRuleConfigs().add(tableRuleConfig);
        shardingRuleConfig.setDataSources(createDataSourceMap());
        shardingRuleConfig.setDefaultDatabaseShardingStrategyConfig(new NoneShardingStrategyConfig());
        ShardingRule actual = shardingRuleConfig.build();
        assertNotNull(actual.getTableShardingStrategy(actual.getTableRule("logicTable")));
    }
    
    @Test
    public void assertGetBindingTableRuleForNotConfig() {
        ShardingRuleConfig shardingRuleConfig = new ShardingRuleConfig();
        shardingRuleConfig.setDataSources(createDataSourceMap());
        TableRuleConfig tableRuleConfig = createTableRuleConfig();
        shardingRuleConfig.getTableRuleConfigs().add(tableRuleConfig);
        shardingRuleConfig.setDataSources(createDataSourceMap());
        ShardingRule actual = shardingRuleConfig.build();
        assertFalse(actual.findBindingTableRule("logicTable").isPresent());
    }
    
    @Test
    public void assertGetBindingTableRuleForNotFound() {
        assertFalse(createShardingRule().findBindingTableRule("newTable").isPresent());
    }
    
    @Test
    public void assertGetBindingTableRuleForFound() {
        ShardingRuleConfig shardingRuleConfig = new ShardingRuleConfig();
        shardingRuleConfig.setDataSources(createDataSourceMap());
        TableRuleConfig tableRuleConfig = createTableRuleConfig();
        TableRuleConfig subTableRuleConfig = createSubTableRuleConfig();
        shardingRuleConfig.getTableRuleConfigs().add(tableRuleConfig);
        shardingRuleConfig.getTableRuleConfigs().add(subTableRuleConfig);
        shardingRuleConfig.setDataSources(createDataSourceMap());
        shardingRuleConfig.getBindingTableGroups().add(createTableRuleConfig().getLogicTable() + "," + createSubTableRuleConfig().getLogicTable());
        ShardingRule actual = shardingRuleConfig.build();
        assertThat(actual.findBindingTableRule("logicTable").get().getTableRules().size(), is(2));
    }
    
    @Test
    public void assertFilterAllBindingTablesWhenLogicTablesIsEmpty() {
        assertThat(createShardingRule().filterAllBindingTables(Collections.<String>emptyList()), is((Collection<String>) Collections.<String>emptyList()));
    }
    
    @Test
    public void assertFilterAllBindingTablesWhenBindingTableRuleIsNotFound() {
        assertThat(createShardingRule().filterAllBindingTables(Collections.singletonList("newTable")), is((Collection<String>) Collections.<String>emptyList()));
    }
    
    @Test
    public void assertFilterAllBindingTables() {
        assertThat(createShardingRule().filterAllBindingTables(Collections.singletonList("logicTable")), is((Collection<String>) Collections.singletonList("logicTable")));
        assertThat(createShardingRule().filterAllBindingTables(Collections.singletonList("subLogicTable")), is((Collection<String>) Collections.singletonList("subLogicTable")));
        assertThat(createShardingRule().filterAllBindingTables(Arrays.asList("logicTable", "subLogicTable")), is((Collection<String>) Arrays.asList("logicTable", "subLogicTable")));
        assertThat(createShardingRule().filterAllBindingTables(Arrays.asList("logicTable", "newTable", "subLogicTable")), is((Collection<String>) Arrays.asList("logicTable", "subLogicTable")));
    }
    
    @Test
    public void assertIsAllBindingTableWhenLogicTablesIsEmpty() {
        assertFalse(createShardingRule().isAllBindingTables(Collections.<String>emptyList()));
    }
    
    @Test
    public void assertIsNotAllBindingTable() {
        assertFalse(createShardingRule().isAllBindingTables(Collections.singletonList("newTable")));
        assertFalse(createShardingRule().isAllBindingTables(Arrays.asList("logicTable", "newTable")));
    }
    
    @Test
    public void assertIsAllBindingTable() {
        assertTrue(createShardingRule().isAllBindingTables(Collections.singletonList("logicTable")));
        assertTrue(createShardingRule().isAllBindingTables(Collections.singletonList("subLogicTable")));
        assertTrue(createShardingRule().isAllBindingTables(Arrays.asList("logicTable", "subLogicTable")));
    }
    
    @Test
    public void assertIsShardingColumnForDefaultDatabaseShardingStrategy() {
        ShardingRuleConfig shardingRuleConfig = new ShardingRuleConfig();
        shardingRuleConfig.setDataSources(createDataSourceMap());
        shardingRuleConfig.getTableRuleConfigs().add(createTableRuleConfigWithAllStrategies());
        StandardShardingStrategyConfig databaseShardingStrategyConfig = new StandardShardingStrategyConfig();
        databaseShardingStrategyConfig.setShardingColumn("column");
        databaseShardingStrategyConfig.setPreciseAlgorithmClassName(TestPreciseShardingAlgorithm.class.getName());
        shardingRuleConfig.setDefaultDatabaseShardingStrategyConfig(databaseShardingStrategyConfig);
        assertTrue(shardingRuleConfig.build().isShardingColumn(new Column("column", "")));
    }
    
    @Test
    public void assertIsShardingColumnForDefaultTableShardingStrategy() {
        ShardingRuleConfig shardingRuleConfig = new ShardingRuleConfig();
        shardingRuleConfig.setDataSources(createDataSourceMap());
        shardingRuleConfig.getTableRuleConfigs().add(createTableRuleConfigWithAllStrategies());
        StandardShardingStrategyConfig tableShardingStrategyConfig = new StandardShardingStrategyConfig();
        tableShardingStrategyConfig.setShardingColumn("column");
        tableShardingStrategyConfig.setPreciseAlgorithmClassName(TestPreciseShardingAlgorithm.class.getName());
        shardingRuleConfig.setDefaultTableShardingStrategyConfig(tableShardingStrategyConfig);
        assertTrue(shardingRuleConfig.build().isShardingColumn(new Column("column", "")));
    }
    
    @Test
    public void assertIsShardingColumnForDatabaseShardingStrategy() {
        ShardingRuleConfig shardingRuleConfig = new ShardingRuleConfig();
        shardingRuleConfig.setDataSources(createDataSourceMap());
        shardingRuleConfig.getTableRuleConfigs().add(createTableRuleConfigWithAllStrategies());
        assertTrue(shardingRuleConfig.build().isShardingColumn(new Column("column", "logicTable")));
    }
    
    @Test
    public void assertIsShardingColumnForTableShardingStrategy() {
        ShardingRuleConfig shardingRuleConfig = new ShardingRuleConfig();
        shardingRuleConfig.setDataSources(createDataSourceMap());
        shardingRuleConfig.getTableRuleConfigs().add(createTableRuleConfigWithTableStrategies());
        assertTrue(shardingRuleConfig.build().isShardingColumn(new Column("column", "logicTable")));
    }
    
    @Test
    public void assertIsNotShardingColumn() {
        ShardingRuleConfig shardingRuleConfig = new ShardingRuleConfig();
        shardingRuleConfig.setDataSources(createDataSourceMap());
        shardingRuleConfig.getTableRuleConfigs().add(createTableRuleConfigWithAllStrategies());
        assertFalse(shardingRuleConfig.build().isShardingColumn(new Column("column", "otherTable")));
    }
    
    private ShardingRule createShardingRule() {
        ShardingRuleConfig shardingRuleConfig = new ShardingRuleConfig();
        shardingRuleConfig.setDataSources(createDataSourceMap());
        TableRuleConfig tableRuleConfig = createTableRuleConfig();
        TableRuleConfig subTableRuleConfig = createSubTableRuleConfig();
        shardingRuleConfig.getTableRuleConfigs().add(tableRuleConfig);
        shardingRuleConfig.getTableRuleConfigs().add(subTableRuleConfig);
        shardingRuleConfig.setDataSources(createDataSourceMap());
        shardingRuleConfig.getBindingTableGroups().add(createTableRuleConfig().getLogicTable() + "," + createSubTableRuleConfig().getLogicTable());
        return shardingRuleConfig.build();
    }
    
    private Map<String, DataSource> createDataSourceMap() {
        Map<String, DataSource> result = new HashMap<>(2);
        result.put("ds0", null);
        result.put("ds1", null);
        return result;
    }
    
    private TableRuleConfig createTableRuleConfig() {
        TableRuleConfig result = new TableRuleConfig();
        result.setLogicTable("logicTable");
        result.setActualTables("table_0, table_1, table_2");
        return result;
    }
    
    private TableRuleConfig createTableRuleConfigWithDatabaseShardingStrategy(final ShardingStrategyConfig strategyConfig) {
        TableRuleConfig result = new TableRuleConfig();
        result.setLogicTable("logicTable");
        result.setActualTables("table_0, table_1, table_2");
        result.setDatabaseShardingStrategyConfig(strategyConfig);
        return result;
    }
    
    private TableRuleConfig createTableRuleConfigWithTableShardingStrategy(final ShardingStrategyConfig strategyConfig) {
        TableRuleConfig result = new TableRuleConfig();
        result.setLogicTable("logicTable");
        result.setActualTables("table_0, table_1, table_2");
        result.setTableShardingStrategyConfig(strategyConfig);
        return result;
    }
    
    private TableRuleConfig createSubTableRuleConfig() {
        TableRuleConfig result = new TableRuleConfig();
        result.setLogicTable("subLogicTable");
        result.setActualTables("sub_table_0, sub_table_1, sub_table_2");
        return result;
    }
    
    private TableRuleConfig createTableRuleConfigWithAllStrategies() {
        TableRuleConfig result = new TableRuleConfig();
        result.setLogicTable("logicTable");
        result.setActualTables("table_0, table_1, table_2");
        StandardShardingStrategyConfig databaseShardingStrategyConfig = new StandardShardingStrategyConfig();
        databaseShardingStrategyConfig.setShardingColumn("column");
        databaseShardingStrategyConfig.setPreciseAlgorithmClassName(TestPreciseShardingAlgorithm.class.getName());
        result.setDatabaseShardingStrategyConfig(databaseShardingStrategyConfig);
        result.setTableShardingStrategyConfig(new NoneShardingStrategyConfig());
        return result;
    }
    
    private TableRuleConfig createTableRuleConfigWithTableStrategies() {
        TableRuleConfig result = new TableRuleConfig();
        result.setLogicTable("logicTable");
        result.setActualTables("table_0, table_1, table_2");
        StandardShardingStrategyConfig tableShardingStrategyConfig = new StandardShardingStrategyConfig();
        tableShardingStrategyConfig.setShardingColumn("column");
        tableShardingStrategyConfig.setPreciseAlgorithmClassName(TestPreciseShardingAlgorithm.class.getName());
        result.setTableShardingStrategyConfig(tableShardingStrategyConfig);
        return result;
    }
}
