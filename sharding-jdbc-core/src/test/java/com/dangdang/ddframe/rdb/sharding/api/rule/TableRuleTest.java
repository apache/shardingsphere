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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class TableRuleTest {
    
    @Test
    public void assertTableRuleForDynamicWithoutAnyStrategies() {
        TableRule actual = new TableRule("logicTable", createDataSourceRule());
        assertDynamicTable(actual);
        assertNull(actual.getDatabaseShardingStrategy());
        assertNull(actual.getTableShardingStrategy());
    }
    
    @Test
    public void assertTableRuleForDynamicWithDatabaseStrategy() {
        TableRule actual = new TableRule("logicTable", createDataSourceRule(), new DatabaseShardingStrategy("", new NoneDatabaseShardingAlgorithm()));
        assertDynamicTable(actual);
        assertNotNull(actual.getDatabaseShardingStrategy());
        assertNull(actual.getTableShardingStrategy());
    }
    
    @Test
    public void assertTableRuleForDynamicWithTableStrategy() {
        TableRule actual = new TableRule("logicTable", createDataSourceRule(), new TableShardingStrategy("", new NoneTableShardingAlgorithm()));
        assertDynamicTable(actual);
        assertNull(actual.getDatabaseShardingStrategy());
        assertNotNull(actual.getTableShardingStrategy());
    }
    
    @Test
    public void assertTableRuleForDynamicWithAllStrategies() {
        TableRule actual = new TableRule("logicTable", createDataSourceRule(), 
                new DatabaseShardingStrategy("", new NoneDatabaseShardingAlgorithm()), new TableShardingStrategy("", new NoneTableShardingAlgorithm()));
        assertDynamicTable(actual);
        assertNotNull(actual.getDatabaseShardingStrategy());
        assertNotNull(actual.getTableShardingStrategy());
    }
    
    private void assertDynamicTable(final TableRule actual) {
        assertThat(actual.getActualTables().size(), is(2));
        assertTrue(actual.getActualTables().contains(new DynamicDataNode("ds0")));
        assertTrue(actual.getActualTables().contains(new DynamicDataNode("ds1")));
    }
    
    @Test
    public void assertTableRuleWithoutDataNode() {
        TableRule actual = new TableRule("logicTable", Arrays.asList("table_0", "table_1", "table_2"), createDataSourceRule());
        assertActualTable(actual);
        assertNull(actual.getDatabaseShardingStrategy());
        assertNull(actual.getTableShardingStrategy());
    }
    
    @Test
    public void assertTableRuleWithDatabaseShardingStrategyWithoutDataNode() {
        TableRule actual = new TableRule("logicTable", Arrays.asList("table_0", "table_1", "table_2"), createDataSourceRule(), new DatabaseShardingStrategy("", new NoneDatabaseShardingAlgorithm()));
        assertActualTable(actual);
        assertNotNull(actual.getDatabaseShardingStrategy());
        assertNull(actual.getTableShardingStrategy());
    }
    
    @Test
    public void assertTableRuleWithTableShardingStrategyWithoutDataNode() {
        TableRule actual = new TableRule("logicTable", Arrays.asList("table_0", "table_1", "table_2"), createDataSourceRule(), new TableShardingStrategy("", new NoneTableShardingAlgorithm()));
        assertActualTable(actual);
        assertNull(actual.getDatabaseShardingStrategy());
        assertNotNull(actual.getTableShardingStrategy());
    }
    
    @Test
    public void assertTableRuleWithDataNodeString() {
        TableRule actual = new TableRule("logicTable", Arrays.asList("ds0.table_0", "ds0.table_1", "ds0.table_2", "ds1.table_0", "ds1.table_1", "ds1.table_2"), createDataSourceRule());
        assertActualTable(actual);
        assertNull(actual.getDatabaseShardingStrategy());
        assertNull(actual.getTableShardingStrategy());
    }
    
    private void assertActualTable(final TableRule actual) {
        assertThat(actual.getActualTables().size(), is(6));
        assertTrue(actual.getActualTables().contains(new DataNode("ds0", "table_0")));
        assertTrue(actual.getActualTables().contains(new DataNode("ds0", "table_1")));
        assertTrue(actual.getActualTables().contains(new DataNode("ds0", "table_2")));
        assertTrue(actual.getActualTables().contains(new DataNode("ds1", "table_0")));
        assertTrue(actual.getActualTables().contains(new DataNode("ds1", "table_1")));
        assertTrue(actual.getActualTables().contains(new DataNode("ds1", "table_2")));
    }
    
    @Test
    public void assertGetActualDataNodes() {
        TableRule actual = new TableRule("logicTable", Arrays.asList("ds0.table_0", "ds0.table_1", "ds0.table_2", "ds1.table_0", "ds1.table_1", "ds1.table_2"), createDataSourceRule());
        assertThat(actual.getActualDataNodes(Collections.singletonList("ds1"), Arrays.asList("table_0", "table_1")), is(
                (Collection<DataNode>) Sets.newLinkedHashSet(Arrays.asList(new DataNode("ds1", "table_0"), new DataNode("ds1", "table_1")))));
    }
    
    @Test
    public void assertGetActualDatasourceNames() {
        TableRule actual = new TableRule("logicTable", Arrays.asList("ds0.table_0", "ds0.table_1", "ds0.table_2", "ds1.table_0", "ds1.table_1", "ds1.table_2"), createDataSourceRule());
        assertThat(actual.getActualDatasourceNames(), is((Collection<String>) Sets.newLinkedHashSet(Arrays.asList("ds0", "ds1"))));
    }
    
    @Test
    public void assertGetActualTableNames() {
        TableRule actual = new TableRule("logicTable", Arrays.asList("ds0.table_0", "ds0.table_1", "ds0.table_2", "ds1.table_0", "ds1.table_1", "ds1.table_2"), createDataSourceRule());
        assertThat(actual.getActualTableNames(Collections.singletonList("ds1")), is((Collection<String>) Sets.newLinkedHashSet(Arrays.asList("table_0", "table_1", "table_2"))));
    }
    
    @Test
    public void assertFindActualTableIndex() {
        TableRule actual = new TableRule("logicTable", Arrays.asList("ds0.table_0", "ds0.table_1", "ds0.table_2", "ds1.table_0", "ds1.table_1", "ds1.table_2"), createDataSourceRule());
        assertThat(actual.findActualTableIndex("ds1", "table_1"), is(4));
    }
    
    @Test
    public void assertFindActualTableIndexForNotFound() {
        TableRule actual = new TableRule("logicTable", Arrays.asList("ds0.table_0", "ds0.table_1", "ds0.table_2", "ds1.table_0", "ds1.table_1", "ds1.table_2"), createDataSourceRule());
        assertThat(actual.findActualTableIndex("ds2", "table_2"), is(-1));
    }
    
    @Test
    public void assertToString() {
        TableRule actual = new TableRule("logicTable", Arrays.asList("ds0.table_0", "ds0.table_1", "ds0.table_2", "ds1.table_0", "ds1.table_1", "ds1.table_2"), createDataSourceRule());
        assertThat(actual.toString(), is("TableRule(logicTable=logicTable, dynamic=false, actualTables=["
                + "DataNode(dataSourceName=ds0, tableName=table_0), "
                + "DataNode(dataSourceName=ds0, tableName=table_1), "
                + "DataNode(dataSourceName=ds0, tableName=table_2), "
                + "DataNode(dataSourceName=ds1, tableName=table_0), "
                + "DataNode(dataSourceName=ds1, tableName=table_1), "
                + "DataNode(dataSourceName=ds1, tableName=table_2)], "
                + "databaseShardingStrategy=null, tableShardingStrategy=null)"));
    }
    
    private DataSourceRule createDataSourceRule() {
        Map<String, DataSource> result = new HashMap<>(2);
        result.put("ds0", null);
        result.put("ds1", null);
        return new DataSourceRule(result);
    }
}
