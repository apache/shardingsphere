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

package org.apache.shardingsphere.sharding.rule;

import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.config.exception.ShardingSphereConfigurationException;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.datanode.DataNodeInfo;
import org.apache.shardingsphere.sharding.algorithm.sharding.mod.ModShardingAlgorithm;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.keygen.KeyGenerateStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.NoneShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.factory.ShardingAlgorithmFactory;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class TableRuleTest {
    
    @Test
    public void assertCreateMinTableRule() {
        ShardingTableRuleConfiguration tableRuleConfig = new ShardingTableRuleConfiguration("LOGIC_TABLE");
        TableRule actual = new TableRule(tableRuleConfig, Arrays.asList("ds0", "ds1"), null);
        assertThat(actual.getLogicTable(), is("LOGIC_TABLE"));
        assertThat(actual.getActualDataNodes().size(), is(2));
        assertTrue(actual.getActualDataNodes().contains(new DataNode("ds0", "LOGIC_TABLE")));
        assertTrue(actual.getActualDataNodes().contains(new DataNode("ds1", "LOGIC_TABLE")));
    }
    
    @Test
    public void assertCreateFullTableRule() {
        ShardingTableRuleConfiguration tableRuleConfig = new ShardingTableRuleConfiguration("LOGIC_TABLE", "ds${0..1}.table_${0..2}");
        tableRuleConfig.setDatabaseShardingStrategy(new NoneShardingStrategyConfiguration());
        tableRuleConfig.setTableShardingStrategy(new NoneShardingStrategyConfiguration());
        tableRuleConfig.setKeyGenerateStrategy(new KeyGenerateStrategyConfiguration("col_1", "increment"));
        TableRule actual = new TableRule(tableRuleConfig, Arrays.asList("ds0", "ds1"), null);
        assertThat(actual.getLogicTable(), is("LOGIC_TABLE"));
        assertThat(actual.getActualDataNodes().size(), is(6));
        assertTrue(actual.getActualDataNodes().contains(new DataNode("ds0", "table_0")));
        assertTrue(actual.getActualDataNodes().contains(new DataNode("ds0", "table_1")));
        assertTrue(actual.getActualDataNodes().contains(new DataNode("ds0", "table_2")));
        assertTrue(actual.getActualDataNodes().contains(new DataNode("ds1", "table_0")));
        assertTrue(actual.getActualDataNodes().contains(new DataNode("ds1", "table_1")));
        assertTrue(actual.getActualDataNodes().contains(new DataNode("ds1", "table_2")));
        assertTrue(actual.getGenerateKeyColumn().isPresent());
        assertThat(actual.getGenerateKeyColumn().get(), is("col_1"));
        assertThat(actual.getKeyGeneratorName(), is("increment"));
    }
    
    @Test
    public void assertCreateAutoTableRuleWithModAlgorithm() {
        ShardingAutoTableRuleConfiguration tableRuleConfig = new ShardingAutoTableRuleConfiguration("LOGIC_TABLE", "ds0,ds1");
        tableRuleConfig.setShardingStrategy(new StandardShardingStrategyConfiguration("col_1", "MOD"));
        ModShardingAlgorithm shardingAlgorithm = (ModShardingAlgorithm) ShardingAlgorithmFactory.newInstance(new AlgorithmConfiguration("MOD", createProperties()));
        TableRule actual = new TableRule(tableRuleConfig, Arrays.asList("ds0", "ds1", "ds2"), shardingAlgorithm, null);
        assertThat(actual.getLogicTable(), is("LOGIC_TABLE"));
        assertThat(actual.getActualDataNodes().size(), is(4));
        assertTrue(actual.getActualDataNodes().contains(new DataNode("ds0", "logic_table_0")));
        assertTrue(actual.getActualDataNodes().contains(new DataNode("ds1", "logic_table_1")));
        assertTrue(actual.getActualDataNodes().contains(new DataNode("ds0", "logic_table_2")));
        assertTrue(actual.getActualDataNodes().contains(new DataNode("ds1", "logic_table_3")));
    }
    
    @Test
    public void assertCreateAutoTableRuleWithModAlgorithmWithoutActualDataSources() {
        ShardingAutoTableRuleConfiguration tableRuleConfig = new ShardingAutoTableRuleConfiguration("LOGIC_TABLE", null);
        tableRuleConfig.setShardingStrategy(new StandardShardingStrategyConfiguration("col_1", "MOD"));
        ModShardingAlgorithm shardingAlgorithm = (ModShardingAlgorithm) ShardingAlgorithmFactory.newInstance(new AlgorithmConfiguration("MOD", createProperties()));
        TableRule actual = new TableRule(tableRuleConfig, Arrays.asList("ds0", "ds1", "ds2"), shardingAlgorithm, null);
        assertThat(actual.getLogicTable(), is("LOGIC_TABLE"));
        assertThat(actual.getActualDataNodes().size(), is(4));
        assertTrue(actual.getActualDataNodes().contains(new DataNode("ds0", "logic_table_0")));
        assertTrue(actual.getActualDataNodes().contains(new DataNode("ds1", "logic_table_1")));
        assertTrue(actual.getActualDataNodes().contains(new DataNode("ds2", "logic_table_2")));
        assertTrue(actual.getActualDataNodes().contains(new DataNode("ds0", "logic_table_3")));
    }
    
    private Properties createProperties() {
        Properties result = new Properties();
        result.setProperty("sharding-count", "4");
        return result;
    }
    
    @Test
    public void assertGetActualDataSourceNames() {
        TableRule actual = new TableRule(new ShardingTableRuleConfiguration("LOGIC_TABLE", "ds${0..1}.table_${0..2}"), Arrays.asList("ds0", "ds1"), null);
        assertThat(actual.getActualDataSourceNames(), is(new LinkedHashSet<>(Arrays.asList("ds0", "ds1"))));
    }
    
    @Test
    public void assertGetActualTableNames() {
        TableRule actual = new TableRule(new ShardingTableRuleConfiguration("LOGIC_TABLE", "ds${0..1}.table_${0..2}"), Arrays.asList("ds0", "ds1"), null);
        assertThat(actual.getActualTableNames("ds0"), is(new LinkedHashSet<>(Arrays.asList("table_0", "table_1", "table_2"))));
        assertThat(actual.getActualTableNames("ds1"), is(new LinkedHashSet<>(Arrays.asList("table_0", "table_1", "table_2"))));
        assertThat(actual.getActualTableNames("ds2"), is(Collections.emptySet()));
    }
    
    @Test
    public void assertFindActualTableIndex() {
        TableRule actual = new TableRule(new ShardingTableRuleConfiguration("LOGIC_TABLE", "ds${0..1}.table_${0..2}"), Arrays.asList("ds0", "ds1"), null);
        assertThat(actual.findActualTableIndex("ds1", "table_1"), is(4));
    }
    
    @Test
    public void assertNotFindActualTableIndex() {
        TableRule actual = new TableRule(new ShardingTableRuleConfiguration("LOGIC_TABLE", "ds${0..1}.table_${0..2}"), Arrays.asList("ds0", "ds1"), null);
        assertThat(actual.findActualTableIndex("ds2", "table_2"), is(-1));
    }
    
    @Test
    public void assertActualTableNameExisted() {
        TableRule actual = new TableRule(new ShardingTableRuleConfiguration("LOGIC_TABLE", "ds${0..1}.table_${0..2}"), Arrays.asList("ds0", "ds1"), null);
        assertTrue(actual.isExisted("table_2"));
    }
    
    @Test
    public void assertActualTableNameNotExisted() {
        TableRule actual = new TableRule(new ShardingTableRuleConfiguration("LOGIC_TABLE", "ds${0..1}.table_${0..2}"), Arrays.asList("ds0", "ds1"), null);
        assertFalse(actual.isExisted("table_3"));
    }
    
    @Test(expected = ShardingSphereConfigurationException.class)
    public void assertActualDataNodesNotConfigured() {
        ShardingTableRuleConfiguration shardingTableRuleConfig = new ShardingTableRuleConfiguration("LOGIC_TABLE", "");
        shardingTableRuleConfig.setTableShardingStrategy(new StandardShardingStrategyConfiguration("shardingColumn", "INLINE"));
        new TableRule(shardingTableRuleConfig, Arrays.asList("ds0", "ds1"), null);
    }
    
    @Test
    public void assertDatNodeGroups() {
        Collection<String> dataSourceNames = new LinkedList<>();
        String logicTableName = "table_0";
        dataSourceNames.add("ds0");
        dataSourceNames.add("ds1");
        TableRule tableRule = new TableRule(dataSourceNames, logicTableName);
        Map<String, List<DataNode>> actual = tableRule.getDataNodeGroups();
        assertThat(actual.size(), is(2));
        assertTrue(actual.get("ds0").contains(new DataNode("ds0", "table_0")));
        assertTrue(actual.get("ds1").contains(new DataNode("ds1", "table_0")));
    }
    
    @Test
    public void assertCreateTableRuleWithDataSourceNames() {
        Collection<String> dataSourceNames = new LinkedList<>();
        String logicTableName = "table_0";
        dataSourceNames.add("ds0");
        dataSourceNames.add("ds1");
        TableRule actual = new TableRule(dataSourceNames, logicTableName);
        assertThat(actual.getActualDataNodes().size(), is(2));
        assertTrue(actual.getActualDataNodes().contains(new DataNode("ds0", "table_0")));
        assertTrue(actual.getActualDataNodes().contains(new DataNode("ds1", "table_0")));
    }
    
    @Test
    public void assertGetTableDataNode() {
        ShardingTableRuleConfiguration shardingTableRuleConfig = new ShardingTableRuleConfiguration("t_order", "ds_0.t_order_0_0,ds_0.t_order_0_1,ds_1.t_order_1_0,ds_1.t_order_1_1");
        TableRule tableRule = new TableRule(shardingTableRuleConfig, Arrays.asList("ds_0", "ds_1"), "order_id");
        DataNodeInfo actual = tableRule.getTableDataNode();
        assertThat(actual.getPrefix(), is("t_order_"));
        assertThat(actual.getPaddingChar(), is('0'));
        assertThat(actual.getSuffixMinLength(), is(3));
    }
    
    @Test
    public void assertGetTableDataNodeWhenLogicTableEndWithNumber() {
        ShardingTableRuleConfiguration shardingTableRuleConfig = new ShardingTableRuleConfiguration("t_order0", "ds_0.t_order0_0,ds_0.t_order0_1");
        TableRule tableRule = new TableRule(shardingTableRuleConfig, Arrays.asList("ds_0", "ds_1"), "order_id");
        DataNodeInfo actual = tableRule.getTableDataNode();
        assertThat(actual.getPrefix(), is("t_order0_"));
        assertThat(actual.getPaddingChar(), is('0'));
        assertThat(actual.getSuffixMinLength(), is(1));
    }
    
    @Test
    public void assertGetDataSourceDataNode() {
        ShardingTableRuleConfiguration shardingTableRuleConfig = new ShardingTableRuleConfiguration("t_order", "ds_0.t_order,ds_1.t_order");
        TableRule tableRule = new TableRule(shardingTableRuleConfig, Arrays.asList("ds_0", "ds_1"), "order_id");
        DataNodeInfo actual = tableRule.getDataSourceDataNode();
        assertThat(actual.getPrefix(), is("ds_"));
        assertThat(actual.getPaddingChar(), is('0'));
        assertThat(actual.getSuffixMinLength(), is(1));
    }
}
