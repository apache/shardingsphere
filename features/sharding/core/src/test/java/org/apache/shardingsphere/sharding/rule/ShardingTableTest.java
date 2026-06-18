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

import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder.Property;
import org.apache.shardingsphere.sharding.algorithm.sharding.mod.ModShardingAlgorithm;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.keygen.KeyGenerateStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.NoneShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.spi.ShardingAlgorithm;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShardingTableTest {
    
    @Test
    void assertCreateFullShardingTable() {
        ShardingTable actual = createShardingTable();
        assertThat(actual.getLogicTable(), is("LOGIC_TABLE"));
        assertThat(actual.getActualDataNodes().size(), is(6));
        assertTrue(actual.getActualDataNodes().contains(new DataNode("ds0", (String) null, "table_0")));
        assertTrue(actual.getActualDataNodes().contains(new DataNode("ds0", (String) null, "table_1")));
        assertTrue(actual.getActualDataNodes().contains(new DataNode("ds0", (String) null, "table_2")));
        assertTrue(actual.getActualDataNodes().contains(new DataNode("ds1", (String) null, "table_0")));
        assertTrue(actual.getActualDataNodes().contains(new DataNode("ds1", (String) null, "table_1")));
        assertTrue(actual.getActualDataNodes().contains(new DataNode("ds1", (String) null, "table_2")));
        assertTrue(actual.getGenerateKeyColumn().isPresent());
        assertThat(actual.getGenerateKeyColumn().get(), is("col_1"));
        assertThat(actual.getKeyGeneratorName(), is("increment"));
    }
    
    private ShardingTable createShardingTable() {
        ShardingTableRuleConfiguration shardingTableRuleConfig = new ShardingTableRuleConfiguration("LOGIC_TABLE", "ds${0..1}.table_${0..2}");
        shardingTableRuleConfig.setDatabaseShardingStrategy(new NoneShardingStrategyConfiguration());
        shardingTableRuleConfig.setTableShardingStrategy(new NoneShardingStrategyConfiguration());
        return new ShardingTable(shardingTableRuleConfig, Arrays.asList("ds0", "ds1"), new KeyGenerateStrategyConfiguration("col_1", "increment"), null);
    }
    
    @Test
    void assertCreateAutoTableRuleWithModAlgorithm() {
        ShardingAutoTableRuleConfiguration shardingAutoTableRuleConfig = new ShardingAutoTableRuleConfiguration("LOGIC_TABLE", "ds0,ds1");
        shardingAutoTableRuleConfig.setShardingStrategy(new StandardShardingStrategyConfiguration("col_1", "MOD"));
        ModShardingAlgorithm shardingAlgorithm = (ModShardingAlgorithm) TypedSPILoader.getService(ShardingAlgorithm.class, "MOD", PropertiesBuilder.build(new Property("sharding-count", "4")));
        ShardingTable actual = new ShardingTable(shardingAutoTableRuleConfig, Arrays.asList("ds0", "ds1", "ds2"), shardingAlgorithm, null);
        assertThat(actual.getLogicTable(), is("LOGIC_TABLE"));
        assertThat(actual.getActualDataNodes().size(), is(4));
        assertTrue(actual.getActualDataNodes().contains(new DataNode("ds0", (String) null, "LOGIC_TABLE_0")));
        assertTrue(actual.getActualDataNodes().contains(new DataNode("ds1", (String) null, "LOGIC_TABLE_1")));
        assertTrue(actual.getActualDataNodes().contains(new DataNode("ds0", (String) null, "LOGIC_TABLE_2")));
        assertTrue(actual.getActualDataNodes().contains(new DataNode("ds1", (String) null, "LOGIC_TABLE_3")));
    }
    
    @Test
    void assertCreateAutoTableRuleWithModAlgorithmWithoutActualDataSources() {
        ShardingAutoTableRuleConfiguration shardingAutoTableRuleConfig = new ShardingAutoTableRuleConfiguration("LOGIC_TABLE", null);
        shardingAutoTableRuleConfig.setShardingStrategy(new StandardShardingStrategyConfiguration("col_1", "MOD"));
        ModShardingAlgorithm shardingAlgorithm = (ModShardingAlgorithm) TypedSPILoader.getService(ShardingAlgorithm.class, "MOD", PropertiesBuilder.build(new Property("sharding-count", "4")));
        ShardingTable actual = new ShardingTable(shardingAutoTableRuleConfig, Arrays.asList("ds0", "ds1", "ds2"), shardingAlgorithm, null);
        assertThat(actual.getLogicTable(), is("LOGIC_TABLE"));
        assertThat(actual.getActualDataNodes().size(), is(4));
        assertTrue(actual.getActualDataNodes().contains(new DataNode("ds0", (String) null, "LOGIC_TABLE_0")));
        assertTrue(actual.getActualDataNodes().contains(new DataNode("ds1", (String) null, "LOGIC_TABLE_1")));
        assertTrue(actual.getActualDataNodes().contains(new DataNode("ds2", (String) null, "LOGIC_TABLE_2")));
        assertTrue(actual.getActualDataNodes().contains(new DataNode("ds0", (String) null, "LOGIC_TABLE_3")));
    }
}
