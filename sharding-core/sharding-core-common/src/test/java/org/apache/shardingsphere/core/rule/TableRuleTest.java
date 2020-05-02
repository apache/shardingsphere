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

package org.apache.shardingsphere.core.rule;

import com.google.common.collect.Sets;
import org.apache.shardingsphere.api.config.sharding.KeyGeneratorConfiguration;
import org.apache.shardingsphere.api.config.sharding.TableRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.strategy.NoneShardingStrategyConfiguration;
import org.apache.shardingsphere.api.config.sharding.strategy.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.core.strategy.algorithm.keygen.fixture.IncrementKeyGenerateAlgorithm;
import org.apache.shardingsphere.core.strategy.algorithm.sharding.inline.InlineShardingAlgorithm;
import org.apache.shardingsphere.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.spi.keygen.KeyGenerateAlgorithm;
import org.apache.shardingsphere.spi.type.TypedSPIRegistry;
import org.apache.shardingsphere.underlying.common.config.exception.ShardingSphereConfigurationException;
import org.apache.shardingsphere.underlying.common.rule.DataNode;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class TableRuleTest {
    
    @BeforeClass
    public static void beforeClass() {
        ShardingSphereServiceLoader.register(KeyGenerateAlgorithm.class);
    }
    
    @Test
    public void assertCreateMinTableRule() {
        TableRuleConfiguration tableRuleConfig = new TableRuleConfiguration("LOGIC_TABLE");
        TableRule actual = new TableRule(tableRuleConfig, Arrays.asList("ds0", "ds1"), null);
        assertThat(actual.getLogicTable(), is("logic_table"));
        assertThat(actual.getActualDataNodes().size(), is(2));
        assertTrue(actual.getActualDataNodes().contains(new DataNode("ds0", "LOGIC_TABLE")));
        assertTrue(actual.getActualDataNodes().contains(new DataNode("ds1", "LOGIC_TABLE")));
        assertNull(actual.getDatabaseShardingStrategy());
        assertNull(actual.getTableShardingStrategy());
    }
    
    @Test
    public void assertCreateFullTableRule() {
        ShardingSphereServiceLoader.register(KeyGenerateAlgorithm.class);
        TableRuleConfiguration tableRuleConfig = new TableRuleConfiguration("LOGIC_TABLE", "ds${0..1}.table_${0..2}");
        tableRuleConfig.setDatabaseShardingStrategyConfig(new NoneShardingStrategyConfiguration());
        tableRuleConfig.setTableShardingStrategyConfig(new NoneShardingStrategyConfiguration());
        KeyGenerateAlgorithm keyGenerateAlgorithm = TypedSPIRegistry.getRegisteredService(KeyGenerateAlgorithm.class, "INCREMENT", new Properties());
        tableRuleConfig.setKeyGeneratorConfig(new KeyGeneratorConfiguration("col_1", keyGenerateAlgorithm));
        TableRule actual = new TableRule(tableRuleConfig, Arrays.asList("ds0", "ds1"), null);
        assertThat(actual.getLogicTable(), is("logic_table"));
        assertThat(actual.getActualDataNodes().size(), is(6));
        assertTrue(actual.getActualDataNodes().contains(new DataNode("ds0", "table_0")));
        assertTrue(actual.getActualDataNodes().contains(new DataNode("ds0", "table_1")));
        assertTrue(actual.getActualDataNodes().contains(new DataNode("ds0", "table_2")));
        assertTrue(actual.getActualDataNodes().contains(new DataNode("ds1", "table_0")));
        assertTrue(actual.getActualDataNodes().contains(new DataNode("ds1", "table_1")));
        assertTrue(actual.getActualDataNodes().contains(new DataNode("ds1", "table_2")));
        assertNotNull(actual.getDatabaseShardingStrategy());
        assertNotNull(actual.getTableShardingStrategy());
        assertTrue(actual.getGenerateKeyColumn().isPresent());
        assertThat(actual.getGenerateKeyColumn().get(), is("col_1"));
        assertThat(actual.getKeyGenerateAlgorithm(), instanceOf(IncrementKeyGenerateAlgorithm.class));
    }
    
    @Test
    public void assertGetActualDatasourceNames() {
        TableRule actual = new TableRule(new TableRuleConfiguration("LOGIC_TABLE", "ds${0..1}.table_${0..2}"), Arrays.asList("ds0", "ds1"), null);
        assertThat(actual.getActualDatasourceNames(), is(Sets.newLinkedHashSet(Arrays.asList("ds0", "ds1"))));
    }
    
    @Test
    public void assertGetActualTableNames() {
        TableRule actual = new TableRule(new TableRuleConfiguration("LOGIC_TABLE", "ds${0..1}.table_${0..2}"), Arrays.asList("ds0", "ds1"), null);
        assertThat(actual.getActualTableNames("ds0"), is(Sets.newLinkedHashSet(Arrays.asList("table_0", "table_1", "table_2"))));
        assertThat(actual.getActualTableNames("ds1"), is(Sets.newLinkedHashSet(Arrays.asList("table_0", "table_1", "table_2"))));
        assertThat(actual.getActualTableNames("ds2"), is(Collections.emptySet()));
    }
    
    @Test
    public void assertFindActualTableIndex() {
        TableRule actual = new TableRule(new TableRuleConfiguration("LOGIC_TABLE", "ds${0..1}.table_${0..2}"), Arrays.asList("ds0", "ds1"), null);
        assertThat(actual.findActualTableIndex("ds1", "table_1"), is(4));
    }
    
    @Test
    public void assertNotFindActualTableIndex() {
        TableRule actual = new TableRule(new TableRuleConfiguration("LOGIC_TABLE", "ds${0..1}.table_${0..2}"), Arrays.asList("ds0", "ds1"), null);
        assertThat(actual.findActualTableIndex("ds2", "table_2"), is(-1));
    }
    
    @Test
    public void assertActualTableNameExisted() {
        TableRule actual = new TableRule(new TableRuleConfiguration("LOGIC_TABLE", "ds${0..1}.table_${0..2}"), Arrays.asList("ds0", "ds1"), null);
        assertTrue(actual.isExisted("table_2"));
    }
    
    @Test
    public void assertActualTableNameNotExisted() {
        TableRule actual = new TableRule(new TableRuleConfiguration("LOGIC_TABLE", "ds${0..1}.table_${0..2}"), Arrays.asList("ds0", "ds1"), null);
        assertFalse(actual.isExisted("table_3"));
    }
    
    @Test(expected = ShardingSphereConfigurationException.class)
    public void assertActualDataNodesNotConfigured() {
        TableRuleConfiguration tableRuleConfiguration = new TableRuleConfiguration("LOGIC_TABLE", "");
        InlineShardingAlgorithm shardingAlgorithm = new InlineShardingAlgorithm();
        shardingAlgorithm.getProperties().setProperty("algorithm.expression", "table_${shardingColumn % 3}");
        tableRuleConfiguration.setTableShardingStrategyConfig(new StandardShardingStrategyConfiguration("shardingColumn", shardingAlgorithm));
        new TableRule(tableRuleConfiguration, Arrays.asList("ds0", "ds1"), null);
    }
}
