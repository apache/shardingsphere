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

import com.google.common.collect.Sets;
import org.apache.shardingsphere.infra.config.exception.ShardingSphereConfigurationException;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.sharding.algorithm.sharding.mod.ModShardingAlgorithm;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.keygen.KeyGenerateStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.NoneShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.spi.KeyGenerateAlgorithm;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class TableRuleTest {
    
    @BeforeClass
    public static void beforeClass() {
        ShardingSphereServiceLoader.register(KeyGenerateAlgorithm.class);
    }
    
    @Test
    public void assertCreateMinTableRule() {
        ShardingTableRuleConfiguration tableRuleConfig = new ShardingTableRuleConfiguration("LOGIC_TABLE");
        TableRule actual = new TableRule(tableRuleConfig, Arrays.asList("ds0", "ds1"), null);
        assertThat(actual.getLogicTable(), is("logic_table"));
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
        assertThat(actual.getLogicTable(), is("logic_table"));
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
        ShardingSphereServiceLoader.register(KeyGenerateAlgorithm.class);
        ShardingAutoTableRuleConfiguration tableRuleConfig = new ShardingAutoTableRuleConfiguration("LOGIC_TABLE", "ds0,ds1");
        tableRuleConfig.setShardingStrategy(new StandardShardingStrategyConfiguration("col_1", "MOD"));
        ModShardingAlgorithm shardingAlgorithm = new ModShardingAlgorithm();
        shardingAlgorithm.getProps().setProperty("sharding-count", "4");
        shardingAlgorithm.init();
        TableRule actual = new TableRule(tableRuleConfig, Arrays.asList("ds0", "ds1", "ds2"), shardingAlgorithm, null);
        assertThat(actual.getLogicTable(), is("logic_table"));
        assertThat(actual.getActualDataNodes().size(), is(4));
        assertTrue(actual.getActualDataNodes().contains(new DataNode("ds0", "logic_table_0")));
        assertTrue(actual.getActualDataNodes().contains(new DataNode("ds1", "logic_table_1")));
        assertTrue(actual.getActualDataNodes().contains(new DataNode("ds0", "logic_table_2")));
        assertTrue(actual.getActualDataNodes().contains(new DataNode("ds1", "logic_table_3")));
    }
    
    @Test
    public void assertCreateAutoTableRuleWithModAlgorithmWithoutActualDataSources() {
        ShardingSphereServiceLoader.register(KeyGenerateAlgorithm.class);
        ShardingAutoTableRuleConfiguration tableRuleConfig = new ShardingAutoTableRuleConfiguration("LOGIC_TABLE", null);
        tableRuleConfig.setShardingStrategy(new StandardShardingStrategyConfiguration("col_1", "MOD"));
        ModShardingAlgorithm shardingAlgorithm = new ModShardingAlgorithm();
        shardingAlgorithm.getProps().setProperty("sharding-count", "4");
        shardingAlgorithm.init();
        TableRule actual = new TableRule(tableRuleConfig, Arrays.asList("ds0", "ds1", "ds2"), shardingAlgorithm, null);
        assertThat(actual.getLogicTable(), is("logic_table"));
        assertThat(actual.getActualDataNodes().size(), is(4));
        assertTrue(actual.getActualDataNodes().contains(new DataNode("ds0", "logic_table_0")));
        assertTrue(actual.getActualDataNodes().contains(new DataNode("ds1", "logic_table_1")));
        assertTrue(actual.getActualDataNodes().contains(new DataNode("ds2", "logic_table_2")));
        assertTrue(actual.getActualDataNodes().contains(new DataNode("ds0", "logic_table_3")));
    }
    
    @Test
    public void assertGetActualDatasourceNames() {
        TableRule actual = new TableRule(new ShardingTableRuleConfiguration("LOGIC_TABLE", "ds${0..1}.table_${0..2}"), Arrays.asList("ds0", "ds1"), null);
        assertThat(actual.getActualDatasourceNames(), is(Sets.newLinkedHashSet(Arrays.asList("ds0", "ds1"))));
    }
    
    @Test
    public void assertGetActualTableNames() {
        TableRule actual = new TableRule(new ShardingTableRuleConfiguration("LOGIC_TABLE", "ds${0..1}.table_${0..2}"), Arrays.asList("ds0", "ds1"), null);
        assertThat(actual.getActualTableNames("ds0"), is(Sets.newLinkedHashSet(Arrays.asList("table_0", "table_1", "table_2"))));
        assertThat(actual.getActualTableNames("ds1"), is(Sets.newLinkedHashSet(Arrays.asList("table_0", "table_1", "table_2"))));
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
}
