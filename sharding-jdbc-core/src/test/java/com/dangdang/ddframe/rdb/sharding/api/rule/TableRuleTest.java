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
import com.dangdang.ddframe.rdb.sharding.keygen.fixture.IncrementKeyGenerator;
import com.google.common.collect.Sets;
import org.junit.Ignore;
import org.junit.Test;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class TableRuleTest {
    
    @Test
    public void assertTableRuleForDynamicWithoutAnyStrategies() {
        TableRule actual = TableRule.builder("logicTable").dynamic(true).dataSourceRule(createDataSourceRule()).build();
        assertDynamicTable(actual);
        assertNull(actual.getDatabaseShardingStrategy());
        assertNull(actual.getTableShardingStrategy());
    }
    
    @Test
    public void assertTableRuleForDynamicWithDatabaseStrategy() {
        TableRule actual = TableRule.builder("logicTable").dynamic(true).dataSourceRule(createDataSourceRule())
                .databaseShardingStrategy(new DatabaseShardingStrategy("", new NoneDatabaseShardingAlgorithm())).build();
        assertDynamicTable(actual);
        assertNotNull(actual.getDatabaseShardingStrategy());
        assertNull(actual.getTableShardingStrategy());
    }
    
    @Test
    public void assertTableRuleForDynamicWithTableStrategy() {
        TableRule actual = TableRule.builder("logicTable").dynamic(true).dataSourceRule(createDataSourceRule())
                .tableShardingStrategy(new TableShardingStrategy("", new NoneTableShardingAlgorithm())).build();
        assertDynamicTable(actual);
        assertNull(actual.getDatabaseShardingStrategy());
        assertNotNull(actual.getTableShardingStrategy());
    }
    
    @Test
    public void assertTableRuleForDynamicWithAllStrategies() {
        TableRule actual = TableRule.builder("logicTable").dynamic(true).dataSourceRule(createDataSourceRule())
                .databaseShardingStrategy(new DatabaseShardingStrategy("", new NoneDatabaseShardingAlgorithm()))
                .tableShardingStrategy(new TableShardingStrategy("", new NoneTableShardingAlgorithm())).build();
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
        TableRule actual = TableRule.builder("logicTable").actualTables(Arrays.asList("table_0", "table_1", "table_2")).dataSourceRule(createDataSourceRule()).build();
        assertActualTable(actual);
        assertNull(actual.getDatabaseShardingStrategy());
        assertNull(actual.getTableShardingStrategy());
    }
    
    @Test
    public void assertTableRuleWithDatabaseShardingStrategyWithoutDataNode() {
        TableRule actual = TableRule.builder("logicTable").actualTables(Arrays.asList("table_0", "table_1", "table_2")).dataSourceRule(createDataSourceRule())
                .databaseShardingStrategy(new DatabaseShardingStrategy("", new NoneDatabaseShardingAlgorithm())).build();
        assertActualTable(actual);
        assertNotNull(actual.getDatabaseShardingStrategy());
        assertNull(actual.getTableShardingStrategy());
    }
    
    @Test
    public void assertTableRuleWithTableShardingStrategyWithoutDataNode() {
        TableRule actual = TableRule.builder("logicTable").actualTables(Arrays.asList("table_0", "table_1", "table_2")).dataSourceRule(createDataSourceRule())
                .tableShardingStrategy(new TableShardingStrategy("", new NoneTableShardingAlgorithm())).build();
        assertActualTable(actual);
        assertNull(actual.getDatabaseShardingStrategy());
        assertNotNull(actual.getTableShardingStrategy());
    }
    
    @Test
    public void assertTableRuleWithDataNodeString() {
        TableRule actual = TableRule.builder("logicTable")
                .actualTables(Arrays.asList("ds0.table_0", "ds0.table_1", "ds0.table_2", "ds1.table_0", "ds1.table_1", "ds1.table_2")).build();
        assertActualTable(actual);
        assertNull(actual.getDatabaseShardingStrategy());
        assertNull(actual.getTableShardingStrategy());
    }
    
    @Test
    public void assertTableRuleWithDataSourceNames() {
        TableRule actual = TableRule.builder("logicTable").actualTables(Arrays.asList("table_0", "table_1", "table_2"))
                .dataSourceRule(createDataSourceRule()).dataSourceNames(Arrays.asList("ds0", "ds1")).build();
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
    
    @Test(expected = NullPointerException.class)
    public void assertTableRuleWithoutActualTablesAndDataSourceRule() {
        TableRule.builder("logicTable").build();
    }
    
    @Test
    public void assertGetActualDataNodesForStatic() {
        TableRule actual = TableRule.builder("logicTable")
                .actualTables(Arrays.asList("ds0.table_0", "ds0.table_1", "ds0.table_2", "ds1.table_0", "ds1.table_1", "ds1.table_2")).build();
        assertThat(actual.getActualDataNodes(Collections.singletonList("ds1"), Arrays.asList("table_0", "table_1")), is(
                (Collection<DataNode>) Sets.newLinkedHashSet(Arrays.asList(new DataNode("ds1", "table_0"), new DataNode("ds1", "table_1")))));
    }
    
    @Test
    public void assertGetActualDataNodesForDynamic() {
        TableRule actual = TableRule.builder("logicTable").dynamic(true).dataSourceRule(createDataSourceRule()).build();
        assertThat(actual.getActualDataNodes(Collections.singletonList("ds1"), Arrays.asList("table_0", "table_1")), is(
                (Collection<DataNode>) Sets.newLinkedHashSet(Arrays.asList(new DataNode("ds1", "table_0"), new DataNode("ds1", "table_1")))));
    }
    
    @Test
    public void assertGetActualDatasourceNames() {
        TableRule actual = TableRule.builder("logicTable")
                .actualTables(Arrays.asList("ds0.table_0", "ds0.table_1", "ds0.table_2", "ds1.table_0", "ds1.table_1", "ds1.table_2")).build();
        assertThat(actual.getActualDatasourceNames(), is((Collection<String>) Sets.newLinkedHashSet(Arrays.asList("ds0", "ds1"))));
    }
    
    @Test
    public void assertGetActualTableNames() {
        TableRule actual = TableRule.builder("logicTable")
                .actualTables(Arrays.asList("ds0.table_0", "ds0.table_1", "ds0.table_2", "ds1.table_0", "ds1.table_1", "ds1.table_2")).build();
        assertThat(actual.getActualTableNames(Collections.singletonList("ds1")), is((Collection<String>) Sets.newLinkedHashSet(Arrays.asList("table_0", "table_1", "table_2"))));
    }
    
    @Test
    public void assertFindActualTableIndex() {
        TableRule actual = TableRule.builder("logicTable")
                .actualTables(Arrays.asList("ds0.table_0", "ds0.table_1", "ds0.table_2", "ds1.table_0", "ds1.table_1", "ds1.table_2")).build();
        assertThat(actual.findActualTableIndex("ds1", "table_1"), is(4));
    }
    
    @Test
    public void assertFindActualTableIndexForNotFound() {
        TableRule actual = TableRule.builder("logicTable")
                .actualTables(Arrays.asList("ds0.table_0", "ds0.table_1", "ds0.table_2", "ds1.table_0", "ds1.table_1", "ds1.table_2")).build();
        assertThat(actual.findActualTableIndex("ds2", "table_2"), is(-1));
    }
    
    @Test
    @Ignore
    public void assertToString() {
        TableRule actual = TableRule.builder("logicTable")
                .actualTables(Arrays.asList("ds0.table_0", "ds0.table_1", "ds0.table_2", "ds1.table_0", "ds1.table_1", "ds1.table_2")).build();
        assertThat(actual.toString(), is("TableRule(logicTable=logicTable, dynamic=false, actualTables=["
                + "DataNode(dataSourceName=ds0, tableName=table_0), "
                + "DataNode(dataSourceName=ds0, tableName=table_1), "
                + "DataNode(dataSourceName=ds0, tableName=table_2), "
                + "DataNode(dataSourceName=ds1, tableName=table_0), "
                + "DataNode(dataSourceName=ds1, tableName=table_1), "
                + "DataNode(dataSourceName=ds1, tableName=table_2)], "
                + "databaseShardingStrategy=null, tableShardingStrategy=null, "
                + "generateKeyColumnsMap={})"));
    }
    
    @Test
    public void assertGenerateKeyColumn() {
        TableRule actual = TableRule.builder("logicTable").dataSourceRule(createDataSourceRule()).generateKeyColumn("col_1", IncrementKeyGenerator.class).build();
        assertThat(actual.getGenerateKeyColumn(), is("col_1"));
        assertThat(actual.getKeyGenerator(), instanceOf(IncrementKeyGenerator.class));
    }
    
    private DataSourceRule createDataSourceRule() {
        Map<String, DataSource> result = new HashMap<>(2);
        result.put("ds0", null);
        result.put("ds1", null);
        return new DataSourceRule(result);
    }
}
