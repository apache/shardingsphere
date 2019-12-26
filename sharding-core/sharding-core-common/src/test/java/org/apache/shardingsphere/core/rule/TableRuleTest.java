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
import org.apache.shardingsphere.api.config.sharding.ShardingRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.TableRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.strategy.InlineShardingStrategyConfiguration;
import org.apache.shardingsphere.api.config.sharding.strategy.NoneShardingStrategyConfiguration;
import org.apache.shardingsphere.underlying.common.config.exception.ShardingSphereConfigurationException;
import org.apache.shardingsphere.core.strategy.keygen.fixture.IncrementShardingKeyGenerator;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
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
    
    @Test
    public void assertCreateMinTableRule() {
        TableRuleConfiguration tableRuleConfig = new TableRuleConfiguration("LOGIC_TABLE");
        TableRule actual = new TableRule(tableRuleConfig, createShardingDataSourceNames(), null);
        assertThat(actual.getLogicTable(), is("logic_table"));
        assertThat(actual.getActualDataNodes().size(), is(2));
        assertTrue(actual.getActualDataNodes().contains(new DataNode("ds0", "LOGIC_TABLE")));
        assertTrue(actual.getActualDataNodes().contains(new DataNode("ds1", "LOGIC_TABLE")));
        assertNull(actual.getDatabaseShardingStrategy());
        assertNull(actual.getTableShardingStrategy());
    }
    
    @Test
    public void assertCreateFullTableRule() {
        TableRuleConfiguration tableRuleConfig = new TableRuleConfiguration("LOGIC_TABLE", "ds${0..1}.table_${0..2}");
        tableRuleConfig.setDatabaseShardingStrategyConfig(new NoneShardingStrategyConfiguration());
        tableRuleConfig.setTableShardingStrategyConfig(new NoneShardingStrategyConfiguration());
        tableRuleConfig.setKeyGeneratorConfig(new KeyGeneratorConfiguration("INCREMENT", "col_1", new Properties()));
        TableRule actual = new TableRule(tableRuleConfig, createShardingDataSourceNames(), null);
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
        assertThat(actual.getGenerateKeyColumn(), is("col_1"));
        assertThat(actual.getShardingKeyGenerator(), instanceOf(IncrementShardingKeyGenerator.class));
    }
    
    @Test
    public void assertGetActualDatasourceNames() {
        TableRule actual = new TableRule(new TableRuleConfiguration("LOGIC_TABLE", "ds${0..1}.table_${0..2}"), createShardingDataSourceNames(), null);
        assertThat(actual.getActualDatasourceNames(), is((Collection<String>) Sets.newLinkedHashSet(Arrays.asList("ds0", "ds1"))));
    }
    
    @Test
    public void assertGetActualTableNames() {
        TableRule actual = new TableRule(new TableRuleConfiguration("LOGIC_TABLE", "ds${0..1}.table_${0..2}"), createShardingDataSourceNames(), null);
        assertThat(actual.getActualTableNames("ds0"), is((Collection<String>) Sets.newLinkedHashSet(Arrays.asList("table_0", "table_1", "table_2"))));
        assertThat(actual.getActualTableNames("ds1"), is((Collection<String>) Sets.newLinkedHashSet(Arrays.asList("table_0", "table_1", "table_2"))));
        assertThat(actual.getActualTableNames("ds2"), is((Collection<String>) Collections.<String>emptySet()));
    }
    
    @Test
    public void assertFindActualTableIndex() {
        TableRule actual = new TableRule(new TableRuleConfiguration("LOGIC_TABLE", "ds${0..1}.table_${0..2}"), createShardingDataSourceNames(), null);
        assertThat(actual.findActualTableIndex("ds1", "table_1"), is(4));
    }
    
    @Test
    public void assertNotFindActualTableIndex() {
        TableRule actual = new TableRule(new TableRuleConfiguration("LOGIC_TABLE", "ds${0..1}.table_${0..2}"), createShardingDataSourceNames(), null);
        assertThat(actual.findActualTableIndex("ds2", "table_2"), is(-1));
    }
    
    @Test
    public void assertActualTableNameExisted() {
        TableRule actual = new TableRule(new TableRuleConfiguration("LOGIC_TABLE", "ds${0..1}.table_${0..2}"), createShardingDataSourceNames(), null);
        assertTrue(actual.isExisted("table_2"));
    }
    
    @Test
    public void assertActualTableNameNotExisted() {
        TableRule actual = new TableRule(new TableRuleConfiguration("LOGIC_TABLE", "ds${0..1}.table_${0..2}"), createShardingDataSourceNames(), null);
        assertFalse(actual.isExisted("table_3"));
    }
    
    @Test(expected = ShardingSphereConfigurationException.class)
    public void assertActualDataNodesNotConfigured() {
        TableRuleConfiguration tableRuleConfiguration = new TableRuleConfiguration("LOGIC_TABLE", "");
        tableRuleConfiguration.setTableShardingStrategyConfig(new InlineShardingStrategyConfiguration("shardingColumn", "table_${shardingColumn % 3}"));
        TableRule actual = new TableRule(tableRuleConfiguration, createShardingDataSourceNames(), null);
    }
    
    @Test
    public void assertToString() {
        TableRule actual = new TableRule(new TableRuleConfiguration("LOGIC_TABLE", "ds${0..1}.table_${0..2}"), createShardingDataSourceNames(), null);
        String actualString = "TableRule(logicTable=logic_table, actualDataNodes=[DataNode(dataSourceName=ds0, tableName=table_0), DataNode(dataSourceName=ds0, tableName=table_1), "
                + "DataNode(dataSourceName=ds0, tableName=table_2), DataNode(dataSourceName=ds1, tableName=table_0), DataNode(dataSourceName=ds1, tableName=table_1), "
                + "DataNode(dataSourceName=ds1, tableName=table_2)], databaseShardingStrategy=null, tableShardingStrategy=null, generateKeyColumn=null, shardingKeyGenerator=null)";
        assertThat(actual.toString(), is(actualString));
    }
    
    private ShardingDataSourceNames createShardingDataSourceNames() {
        return new ShardingDataSourceNames(new ShardingRuleConfiguration(), Arrays.asList("ds0", "ds1"));
    }
}
