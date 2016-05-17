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

import com.dangdang.ddframe.rdb.sharding.api.strategy.database.DatabaseShardingStrategy;
import com.dangdang.ddframe.rdb.sharding.api.strategy.database.NoneDatabaseShardingAlgorithm;
import com.dangdang.ddframe.rdb.sharding.api.strategy.table.NoneTableShardingAlgorithm;
import com.dangdang.ddframe.rdb.sharding.api.strategy.table.TableShardingStrategy;
import com.google.common.collect.Sets;
import org.junit.Test;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class ShardingRuleTest {
    
    @Test
    public void assertShardingRuleWithoutStrategy() {
        ShardingRule actual = new ShardingRule(createDataSourceRule(), Collections.singletonList(createTableRule()));
        assertTrue(actual.getDatabaseShardingStrategy().getShardingColumns().isEmpty());
        assertTrue(actual.getTableShardingStrategy().getShardingColumns().isEmpty());
    }
    
    @Test
    public void assertShardingRuleWithBindingTableRuleWithoutStrategy() {
        ShardingRule actual = createShardingRule();
        assertTrue(actual.getDatabaseShardingStrategy().getShardingColumns().isEmpty());
        assertTrue(actual.getTableShardingStrategy().getShardingColumns().isEmpty());
    }
    
    @Test
    public void assertShardingRuleWithDatabaseStrategy() {
        ShardingRule actual = new ShardingRule(createDataSourceRule(), Collections.singletonList(createTableRule()), createDatabaseShardingStrategy());
        assertThat(actual.getDatabaseShardingStrategy().getShardingColumns().size(), is(1));
        assertTrue(actual.getTableShardingStrategy().getShardingColumns().isEmpty());
    }
    
    @Test
    public void assertShardingRuleWithTableStrategy() {
        ShardingRule actual = new ShardingRule(createDataSourceRule(), Collections.singletonList(createTableRule()), createTableShardingStrategy());
        assertThat(actual.getTableShardingStrategy().getShardingColumns().size(), is(1));
        assertTrue(actual.getDatabaseShardingStrategy().getShardingColumns().isEmpty());
    }
    
    @Test
    public void assertShardingRuleWithoutBindingTableRule() {
        ShardingRule actual = new ShardingRule(createDataSourceRule(), Collections.singletonList(createTableRule()), createDatabaseShardingStrategy(), createTableShardingStrategy());
        assertThat(actual.getDatabaseShardingStrategy().getShardingColumns().size(), is(1));
        assertThat(actual.getTableShardingStrategy().getShardingColumns().size(), is(1));
    }
    
    @Test
    public void assertFindTableRule() {
        ShardingRule actual = new ShardingRule(createDataSourceRule(), Collections.singletonList(createTableRule()), createDatabaseShardingStrategy(), createTableShardingStrategy());
        assertTrue(actual.findTableRule("logicTable").isPresent());
        assertFalse(actual.findTableRule("null").isPresent());
    }
    
    @Test
    public void assertGetDatabaseShardingStrategyFromTableRule() {
        DatabaseShardingStrategy strategy = createDatabaseShardingStrategy();
        TableRule tableRule = createTableRule(strategy);
        ShardingRule actual = new ShardingRule(createDataSourceRule(), Collections.singletonList(tableRule));
        assertThat(actual.getDatabaseShardingStrategy(tableRule), is(strategy));
    }
    
    @Test
    public void assertGetDatabaseShardingStrategyFromDefault() {
        DatabaseShardingStrategy strategy = createDatabaseShardingStrategy();
        TableRule tableRule = createTableRule();
        ShardingRule actual = new ShardingRule(createDataSourceRule(), Collections.singletonList(tableRule), strategy);
        assertThat(actual.getDatabaseShardingStrategy(tableRule), is(strategy));
    }
    
    @Test
    public void assertGetDatabaseShardingStrategyForNullValue() {
        DatabaseShardingStrategy strategy = null;
        TableRule tableRule = createTableRule();
        ShardingRule actual = new ShardingRule(createDataSourceRule(), Collections.singletonList(tableRule), strategy);
        assertNotNull(actual.getDatabaseShardingStrategy(tableRule));
    }
    
    @Test
    public void assertGetTableShardingStrategyFromTableRule() {
        TableShardingStrategy strategy = createTableShardingStrategy();
        TableRule tableRule = createTableRule(strategy);
        ShardingRule actual = new ShardingRule(createDataSourceRule(), Collections.singletonList(tableRule));
        assertThat(actual.getTableShardingStrategy(tableRule), is(strategy));
    }
    
    @Test
    public void assertGetTableShardingStrategyFromDefault() {
        TableShardingStrategy strategy = createTableShardingStrategy();
        TableRule tableRule = createTableRule();
        ShardingRule actual = new ShardingRule(createDataSourceRule(), Collections.singletonList(tableRule), strategy);
        assertThat(actual.getTableShardingStrategy(tableRule), is(strategy));
    }
    
    @Test
    public void assertGetTableShardingStrategyForNullValue() {
        TableShardingStrategy strategy = null;
        TableRule tableRule = createTableRule();
        ShardingRule actual = new ShardingRule(createDataSourceRule(), Collections.singletonList(tableRule), strategy);
        assertNotNull(actual.getTableShardingStrategy(tableRule));
    }
    
    @Test
    public void assertGetBindingTableRuleForNotConfig() {
        ShardingRule actual = new ShardingRule(createDataSourceRule(), Collections.singletonList(createTableRule()));
        assertFalse(actual.findBindingTableRule("logicTable").isPresent());
    }
    
    @Test
    public void assertGetBindingTableRuleForNotFound() {
        assertFalse(createShardingRule().findBindingTableRule("newTable").isPresent());
    }
    
    @Test
    public void assertGetBindingTableRuleForFound() {
        BindingTableRule bindingTableRule = createBindingTableRule();
        ShardingRule actual = new ShardingRule(createDataSourceRule(), Collections.singletonList(createTableRule()), Collections.singletonList(bindingTableRule));
        assertThat(actual.findBindingTableRule("logicTable").get(), is(bindingTableRule));
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
    public void assertGetAllShardingColumnsWithoutStrategy() {
        assertThat(createShardingRule().getAllShardingColumns(), is((Collection<String>) Collections.<String>emptySet()));
    }
    
    @Test
    public void assertGetAllShardingColumnsWithStrategy() {
        assertThat(createShardingRuleWithStrategy().getAllShardingColumns(), is((Collection<String>) Sets.newHashSet("column")));
    }
    
    private ShardingRule createShardingRule() {
        return new ShardingRule(createDataSourceRule(), Collections.singletonList(createTableRule()), Collections.singletonList(createBindingTableRule()));
    }
    
    private DataSourceRule createDataSourceRule() {
        Map<String, DataSource> result = new HashMap<>(2);
        result.put("ds0", null);
        result.put("ds1", null);
        return new DataSourceRule(result);
    }
    
    private TableRule createTableRule() {
        return new TableRule("logicTable", Arrays.asList("table_0", "table_1", "table_2"), createDataSourceRule());
    }
    
    private TableRule createTableRule(final DatabaseShardingStrategy strategy) {
        return new TableRule("logicTable", Arrays.asList("table_0", "table_1", "table_2"), createDataSourceRule(), strategy);
    }
    
    private TableRule createTableRule(final TableShardingStrategy strategy) {
        return new TableRule("logicTable", Arrays.asList("table_0", "table_1", "table_2"), createDataSourceRule(), strategy);
    }
    
    private BindingTableRule createBindingTableRule() {
        return new BindingTableRule(Arrays.asList(createTableRule(), createSubTableRule()));
    }
    
    private TableRule createSubTableRule() {
        return new TableRule("subLogicTable", Arrays.asList("sub_table_0", "sub_table_1", "sub_table_2"), createDataSourceRule());
    }
    
    private DatabaseShardingStrategy createDatabaseShardingStrategy() {
        return new DatabaseShardingStrategy(Collections.singletonList("column"), new NoneDatabaseShardingAlgorithm());
    }
    
    private TableShardingStrategy createTableShardingStrategy() {
        return new TableShardingStrategy(Collections.singletonList("column"), new NoneTableShardingAlgorithm());
    }
    
    private ShardingRule createShardingRuleWithStrategy() {
        return new ShardingRule(createDataSourceRule(), Collections.singletonList(createTableRuleWithAllStrategies()), Collections.singletonList(createBindingTableRule()));
    }
    
    private TableRule createTableRuleWithAllStrategies() {
        return new TableRule("logicTable", Arrays.asList("table_0", "table_1", "table_2"), createDataSourceRule(), createDatabaseShardingStrategy(), createTableShardingStrategy());
    }
}
