package com.dangdang.ddframe.rdb.sharding.api.rule;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.junit.Test;

import com.dangdang.ddframe.rdb.sharding.api.strategy.database.DatabaseShardingStrategy;
import com.dangdang.ddframe.rdb.sharding.api.strategy.database.NoneDatabaseShardingAlgorithm;
import com.dangdang.ddframe.rdb.sharding.api.strategy.table.NoneTableShardingAlgorithm;
import com.dangdang.ddframe.rdb.sharding.api.strategy.table.TableShardingStrategy;

public final class ShardingRuleTest {
    
    @Test
    public void assertShardingRuleWithoutStrategy() {
        ShardingRule actual = new ShardingRule(createDataSourceRule(), Arrays.asList(createTableRule()));
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
        ShardingRule actual = new ShardingRule(createDataSourceRule(), Arrays.asList(createTableRule()), createDatabaseShardingStrategy());
        assertThat(actual.getDatabaseShardingStrategy().getShardingColumns().size(), is(1));
        assertTrue(actual.getTableShardingStrategy().getShardingColumns().isEmpty());
    }
    
    @Test
    public void assertShardingRuleWithTableStrategy() {
        ShardingRule actual = new ShardingRule(createDataSourceRule(), Arrays.asList(createTableRule()), createTableShardingStrategy());
        assertThat(actual.getTableShardingStrategy().getShardingColumns().size(), is(1));
        assertTrue(actual.getDatabaseShardingStrategy().getShardingColumns().isEmpty());
    }
    
    @Test
    public void assertShardingRuleWithoutBindingTableRule() {
        ShardingRule actual = new ShardingRule(createDataSourceRule(), Arrays.asList(createTableRule()), createDatabaseShardingStrategy(), createTableShardingStrategy());
        assertThat(actual.getDatabaseShardingStrategy().getShardingColumns().size(), is(1));
        assertThat(actual.getTableShardingStrategy().getShardingColumns().size(), is(1));
    }
    
    @Test
    public void assertFindTableRule() {
        ShardingRule actual = new ShardingRule(createDataSourceRule(), Arrays.asList(createTableRule()), createDatabaseShardingStrategy(), createTableShardingStrategy());
        assertTrue(actual.findTableRule("logicTable").isPresent());
        assertFalse(actual.findTableRule("null").isPresent());
    }
    
    @Test
    public void assertGetDatabaseShardingStrategyFromTableRule() {
        DatabaseShardingStrategy strategy = createDatabaseShardingStrategy();
        TableRule tableRule = createTableRule(strategy);
        ShardingRule actual = new ShardingRule(createDataSourceRule(), Arrays.asList(tableRule));
        assertThat(actual.getDatabaseShardingStrategy(tableRule), is(strategy));
    }
    
    @Test
    public void assertGetDatabaseShardingStrategyFromDefault() {
        DatabaseShardingStrategy strategy = createDatabaseShardingStrategy();
        TableRule tableRule = createTableRule();
        ShardingRule actual = new ShardingRule(createDataSourceRule(), Arrays.asList(tableRule), strategy);
        assertThat(actual.getDatabaseShardingStrategy(tableRule), is(strategy));
    }
    
    @Test(expected = NullPointerException.class)
    public void assertGetDatabaseShardingStrategyFailure() {
        DatabaseShardingStrategy strategy = null;
        TableRule tableRule = createTableRule();
        ShardingRule actual = new ShardingRule(createDataSourceRule(), Arrays.asList(tableRule), strategy);
        assertThat(actual.getDatabaseShardingStrategy(tableRule), is(strategy));
    }
    
    @Test
    public void assertGetTableShardingStrategyFromTableRule() {
        TableShardingStrategy strategy = createTableShardingStrategy();
        TableRule tableRule = createTableRule(strategy);
        ShardingRule actual = new ShardingRule(createDataSourceRule(), Arrays.asList(tableRule));
        assertThat(actual.getTableShardingStrategy(tableRule), is(strategy));
    }
    
    @Test
    public void assertGetTableShardingStrategyFromDefault() {
        TableShardingStrategy strategy = createTableShardingStrategy();
        TableRule tableRule = createTableRule();
        ShardingRule actual = new ShardingRule(createDataSourceRule(), Arrays.asList(tableRule), strategy);
        assertThat(actual.getTableShardingStrategy(tableRule), is(strategy));
    }
    
    @Test(expected = NullPointerException.class)
    public void assertGetTableShardingStrategyFailure() {
        TableShardingStrategy strategy = null;
        TableRule tableRule = createTableRule();
        ShardingRule actual = new ShardingRule(createDataSourceRule(), Arrays.asList(tableRule), strategy);
        assertThat(actual.getTableShardingStrategy(tableRule), is(strategy));
    }
    
    @Test
    public void assertGetBindingTableRuleForNotConfig() {
        ShardingRule actual = new ShardingRule(createDataSourceRule(), Arrays.asList(createTableRule()));
        assertFalse(actual.getBindingTableRule("logicTable").isPresent());
    }
    
    @Test
    public void assertGetBindingTableRuleForNotFound() {
        assertFalse(createShardingRule().getBindingTableRule("newTable").isPresent());
    }
    
    @Test
    public void assertGetBindingTableRuleForFound() {
        BindingTableRule bindingTableRule = createBindingTableRule();
        ShardingRule actual = new ShardingRule(createDataSourceRule(), Arrays.asList(createTableRule()), Arrays.asList(bindingTableRule));
        assertThat(actual.getBindingTableRule("logicTable").get(), is(bindingTableRule));
    }
    
    @Test
    public void assertFilterAllBindingTablesWhenLogicTablesIsEmpty() {
        assertThat(createShardingRule().filterAllBindingTables(Collections.<String>emptyList()), is((Collection<String>) Collections.<String>emptyList()));
    }
    
    @Test
    public void assertFilterAllBindingTablesWhenBindingTableRuleIsNotFound() {
        assertThat(createShardingRule().filterAllBindingTables(Arrays.asList("newTable")), is((Collection<String>) Collections.<String>emptyList()));
    }
    
    @Test
    public void assertFilterAllBindingTables() {
        assertThat(createShardingRule().filterAllBindingTables(Arrays.asList("logicTable")), is((Collection<String>) Arrays.asList("logicTable")));
        assertThat(createShardingRule().filterAllBindingTables(Arrays.asList("subLogicTable")), is((Collection<String>) Arrays.asList("subLogicTable")));
        assertThat(createShardingRule().filterAllBindingTables(Arrays.asList("logicTable", "subLogicTable")), is((Collection<String>) Arrays.asList("logicTable", "subLogicTable")));
        assertThat(createShardingRule().filterAllBindingTables(Arrays.asList("logicTable", "newTable", "subLogicTable")), is((Collection<String>) Arrays.asList("logicTable", "subLogicTable")));
    }
    
    @Test
    public void assertIsAllBindingTableWhenLogicTablesIsEmpty() {
        assertFalse(createShardingRule().isAllBindingTable(Collections.<String>emptyList()));
    }
    
    @Test
    public void assertIsNotAllBindingTable() {
        assertFalse(createShardingRule().isAllBindingTable(Arrays.asList("newTable")));
        assertFalse(createShardingRule().isAllBindingTable(Arrays.asList("logicTable", "newTable")));
    }
    
    @Test
    public void assertIsAllBindingTable() {
        assertTrue(createShardingRule().isAllBindingTable(Arrays.asList("logicTable")));
        assertTrue(createShardingRule().isAllBindingTable(Arrays.asList("subLogicTable")));
        assertTrue(createShardingRule().isAllBindingTable(Arrays.asList("logicTable", "subLogicTable")));
    }
    
    private ShardingRule createShardingRule() {
        return new ShardingRule(createDataSourceRule(), Arrays.asList(createTableRule()), Arrays.asList(createBindingTableRule()));
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
        return new DatabaseShardingStrategy(Arrays.asList("column"), new NoneDatabaseShardingAlgorithm());
    }
    
    private TableShardingStrategy createTableShardingStrategy() {
        return new TableShardingStrategy(Arrays.asList("column"), new NoneTableShardingAlgorithm());
    }
}
