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

import com.dangdang.ddframe.rdb.sharding.api.config.TableRuleConfig;
import com.dangdang.ddframe.rdb.sharding.api.config.strategy.NoneShardingStrategyConfig;
import com.dangdang.ddframe.rdb.sharding.keygen.fixture.IncrementKeyGenerator;
import com.google.common.collect.Sets;
import org.junit.Test;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.Collection;
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
        TableRuleConfig tableRuleConfig = new TableRuleConfig();
        tableRuleConfig.setLogicTable("logicTable");
        tableRuleConfig.setDynamic(true);
        TableRule actual = tableRuleConfig.build(createDataSourceMap());
        assertDynamicTable(actual);
        assertNull(actual.getDatabaseShardingStrategy());
        assertNull(actual.getTableShardingStrategy());
    }
    
    @Test
    public void assertTableRuleForDynamicWithDatabaseStrategy() {
        TableRuleConfig tableRuleConfig = new TableRuleConfig();
        tableRuleConfig.setLogicTable("logicTable");
        tableRuleConfig.setDynamic(true);
        tableRuleConfig.setDatabaseShardingStrategyConfig(new NoneShardingStrategyConfig());
        TableRule actual = tableRuleConfig.build(createDataSourceMap());
        assertDynamicTable(actual);
        assertNotNull(actual.getDatabaseShardingStrategy());
        assertNull(actual.getTableShardingStrategy());
    }
    
    @Test
    public void assertTableRuleForDynamicWithTableStrategy() {
        TableRuleConfig tableRuleConfig = new TableRuleConfig();
        tableRuleConfig.setLogicTable("logicTable");
        tableRuleConfig.setDynamic(true);
        tableRuleConfig.setTableShardingStrategyConfig(new NoneShardingStrategyConfig());
        TableRule actual = tableRuleConfig.build(createDataSourceMap());
        assertDynamicTable(actual);
        assertNull(actual.getDatabaseShardingStrategy());
        assertNotNull(actual.getTableShardingStrategy());
    }
    
    @Test
    public void assertTableRuleForDynamicWithAllStrategies() {
        TableRuleConfig tableRuleConfig = new TableRuleConfig();
        tableRuleConfig.setLogicTable("logicTable");
        tableRuleConfig.setDynamic(true);
        tableRuleConfig.setDatabaseShardingStrategyConfig(new NoneShardingStrategyConfig());
        tableRuleConfig.setTableShardingStrategyConfig(new NoneShardingStrategyConfig());
        TableRule actual = tableRuleConfig.build(createDataSourceMap());
        assertDynamicTable(actual);
        assertNotNull(actual.getDatabaseShardingStrategy());
        assertNotNull(actual.getTableShardingStrategy());
    }
    
    private void assertDynamicTable(final TableRule actual) {
        assertThat(actual.getActualDataNodes().size(), is(2));
        assertTrue(actual.getActualDataNodes().contains(new DynamicDataNode("ds0")));
        assertTrue(actual.getActualDataNodes().contains(new DynamicDataNode("ds1")));
    }
    
    @Test
    public void assertTableRuleWithoutDataNode() {
        TableRuleConfig tableRuleConfig = new TableRuleConfig();
        tableRuleConfig.setLogicTable("logicTable");
        tableRuleConfig.setActualTables("table_0, table_1, table_2");
        TableRule actual = tableRuleConfig.build(createDataSourceMap());
        assertActualTable(actual);
        assertNull(actual.getDatabaseShardingStrategy());
        assertNull(actual.getTableShardingStrategy());
    }
    
    @Test
    public void assertTableRuleWithDatabaseShardingStrategyWithoutDataNode() {
        TableRuleConfig tableRuleConfig = new TableRuleConfig();
        tableRuleConfig.setLogicTable("logicTable");
        tableRuleConfig.setActualTables("table_0, table_1, table_2");
        tableRuleConfig.setDatabaseShardingStrategyConfig(new NoneShardingStrategyConfig());
        TableRule actual = tableRuleConfig.build(createDataSourceMap());
        assertActualTable(actual);
        assertNotNull(actual.getDatabaseShardingStrategy());
        assertNull(actual.getTableShardingStrategy());
    }
    
    @Test
    public void assertTableRuleWithTableShardingStrategyWithoutDataNode() {
        TableRuleConfig tableRuleConfig = new TableRuleConfig();
        tableRuleConfig.setLogicTable("logicTable");
        tableRuleConfig.setActualTables("table_0, table_1, table_2");
        tableRuleConfig.setTableShardingStrategyConfig(new NoneShardingStrategyConfig());
        TableRule actual = tableRuleConfig.build(createDataSourceMap());
        assertActualTable(actual);
        assertNull(actual.getDatabaseShardingStrategy());
        assertNotNull(actual.getTableShardingStrategy());
    }
    
    @Test
    public void assertTableRuleWithDataNodeString() {
        TableRuleConfig tableRuleConfig = new TableRuleConfig();
        tableRuleConfig.setLogicTable("logicTable");
        tableRuleConfig.setActualTables("ds0.table_0, ds0.table_1, ds0.table_2, ds1.table_0, ds1.table_1, ds1.table_2");
        TableRule actual = tableRuleConfig.build(null);
        assertActualTable(actual);
        assertNull(actual.getDatabaseShardingStrategy());
        assertNull(actual.getTableShardingStrategy());
    }
    
    @Test
    public void assertTableRuleWithDataSourceNames() {
        TableRuleConfig tableRuleConfig = new TableRuleConfig();
        tableRuleConfig.setLogicTable("logicTable");
        tableRuleConfig.setActualTables("table_0, table_1, table_2");
        tableRuleConfig.setDataSourceNames("ds0, ds1");
        TableRule actual = tableRuleConfig.build(createDataSourceMap());
        assertActualTable(actual);
        assertNull(actual.getDatabaseShardingStrategy());
        assertNull(actual.getTableShardingStrategy());
    }
    
    private void assertActualTable(final TableRule actual) {
        assertThat(actual.getActualDataNodes().size(), is(6));
        assertTrue(actual.getActualDataNodes().contains(new DataNode("ds0", "table_0")));
        assertTrue(actual.getActualDataNodes().contains(new DataNode("ds0", "table_1")));
        assertTrue(actual.getActualDataNodes().contains(new DataNode("ds0", "table_2")));
        assertTrue(actual.getActualDataNodes().contains(new DataNode("ds1", "table_0")));
        assertTrue(actual.getActualDataNodes().contains(new DataNode("ds1", "table_1")));
        assertTrue(actual.getActualDataNodes().contains(new DataNode("ds1", "table_2")));
    }
    
    @Test
    public void assertGetActualDataNodesForStatic() {
        TableRuleConfig tableRuleConfig = new TableRuleConfig();
        tableRuleConfig.setLogicTable("logicTable");
        tableRuleConfig.setActualTables("ds0.table_0, ds0.table_1, ds0.table_2, ds1.table_0, ds1.table_1, ds1.table_2");
        TableRule actual = tableRuleConfig.build(createDataSourceMap());
        assertThat(actual.getActualDataNodes("ds1", Arrays.asList("table_0", "table_1")), is(
                (Collection<DataNode>) Sets.newLinkedHashSet(Arrays.asList(new DataNode("ds1", "table_0"), new DataNode("ds1", "table_1")))));
    }
    
    @Test
    public void assertGetActualDataNodesForDynamic() {
        TableRuleConfig tableRuleConfig = new TableRuleConfig();
        tableRuleConfig.setLogicTable("logicTable");
        tableRuleConfig.setDynamic(true);
        TableRule actual = tableRuleConfig.build(createDataSourceMap());
        assertThat(actual.getActualDataNodes("ds1", Arrays.asList("table_0", "table_1")), is(
                (Collection<DataNode>) Sets.newLinkedHashSet(Arrays.asList(new DataNode("ds1", "table_0"), new DataNode("ds1", "table_1")))));
    }
    
    @Test
    public void assertGetActualDatasourceNames() {
        TableRuleConfig tableRuleConfig = new TableRuleConfig();
        tableRuleConfig.setLogicTable("logicTable");
        tableRuleConfig.setActualTables("ds0.table_0, ds0.table_1, ds0.table_2, ds1.table_0, ds1.table_1, ds1.table_2");
        TableRule actual = tableRuleConfig.build(null);
        assertThat(actual.getActualDatasourceNames(), is((Collection<String>) Sets.newLinkedHashSet(Arrays.asList("ds0", "ds1"))));
    }
    
    @Test
    public void assertGetActualTableNames() {
        TableRuleConfig tableRuleConfig = new TableRuleConfig();
        tableRuleConfig.setLogicTable("logicTable");
        tableRuleConfig.setActualTables("ds0.table_0, ds0.table_1, ds0.table_2, ds1.table_0, ds1.table_1, ds1.table_2");
        TableRule actual = tableRuleConfig.build(null);
        assertThat(actual.getActualTableNames("ds1"), is((Collection<String>) Sets.newLinkedHashSet(Arrays.asList("table_0", "table_1", "table_2"))));
    }
    
    @Test
    public void assertFindActualTableIndex() {
        TableRuleConfig tableRuleConfig = new TableRuleConfig();
        tableRuleConfig.setLogicTable("logicTable");
        tableRuleConfig.setActualTables("ds0.table_0, ds0.table_1, ds0.table_2, ds1.table_0, ds1.table_1, ds1.table_2");
        TableRule actual = tableRuleConfig.build(null);
        assertThat(actual.findActualTableIndex("ds1", "table_1"), is(4));
    }
    
    @Test
    public void assertFindActualTableIndexForNotFound() {
        TableRuleConfig tableRuleConfig = new TableRuleConfig();
        tableRuleConfig.setLogicTable("logicTable");
        tableRuleConfig.setActualTables("ds0.table_0, ds0.table_1, ds0.table_2, ds1.table_0, ds1.table_1, ds1.table_2");
        TableRule actual = tableRuleConfig.build(null);
        assertThat(actual.findActualTableIndex("ds2", "table_2"), is(-1));
    }
    
    @Test
    public void assertGenerateKeyColumn() {
        TableRuleConfig tableRuleConfig = new TableRuleConfig();
        tableRuleConfig.setLogicTable("logicTable");
        tableRuleConfig.setKeyGeneratorColumnName("col_1");
        tableRuleConfig.setKeyGeneratorClass(IncrementKeyGenerator.class.getName());
        TableRule actual = tableRuleConfig.build(createDataSourceMap());
        assertThat(actual.getGenerateKeyColumn(), is("col_1"));
        assertThat(actual.getKeyGenerator(), instanceOf(IncrementKeyGenerator.class));
    }
    
    private Map<String, DataSource> createDataSourceMap() {
        Map<String, DataSource> result = new HashMap<>(2);
        result.put("ds0", null);
        result.put("ds1", null);
        return result;
    }
}
