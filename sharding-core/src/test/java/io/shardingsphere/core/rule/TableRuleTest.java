/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.core.rule;

import com.google.common.collect.Sets;
import io.shardingsphere.api.config.rule.ShardingRuleConfiguration;
import io.shardingsphere.api.config.rule.TableRuleConfiguration;
import io.shardingsphere.api.config.strategy.NoneShardingStrategyConfiguration;
import io.shardingsphere.core.keygen.fixture.IncrementKeyGenerator;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

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
        TableRuleConfiguration tableRuleConfig = new TableRuleConfiguration();
        tableRuleConfig.setLogicTable("LOGIC_TABLE");
        TableRule actual = new TableRule(tableRuleConfig, createShardingDataSourceNames());
        assertThat(actual.getLogicTable(), is("logic_table"));
        assertThat(actual.getActualDataNodes().size(), is(2));
        assertTrue(actual.getActualDataNodes().contains(new DataNode("ds0", "LOGIC_TABLE")));
        assertTrue(actual.getActualDataNodes().contains(new DataNode("ds1", "LOGIC_TABLE")));
        assertNull(actual.getDatabaseShardingStrategy());
        assertNull(actual.getTableShardingStrategy());
        assertNull(actual.getLogicIndex());
    }
    
    @Test
    public void assertCreateFullTableRule() {
        TableRuleConfiguration tableRuleConfig = new TableRuleConfiguration();
        tableRuleConfig.setLogicTable("LOGIC_TABLE");
        tableRuleConfig.setActualDataNodes("ds${0..1}.table_${0..2}");
        tableRuleConfig.setDatabaseShardingStrategyConfig(new NoneShardingStrategyConfiguration());
        tableRuleConfig.setTableShardingStrategyConfig(new NoneShardingStrategyConfiguration());
        tableRuleConfig.setKeyGeneratorColumnName("col_1");
        tableRuleConfig.setKeyGenerator(new IncrementKeyGenerator());
        tableRuleConfig.setLogicIndex("LOGIC_INDEX");
        TableRule actual = new TableRule(tableRuleConfig, createShardingDataSourceNames());
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
        assertThat(actual.getKeyGenerator(), instanceOf(IncrementKeyGenerator.class));
        assertThat(actual.getLogicIndex(), is("logic_index"));
    }
    
    @Test
    public void assertGetActualDatasourceNames() {
        TableRuleConfiguration tableRuleConfig = new TableRuleConfiguration();
        tableRuleConfig.setLogicTable("LOGIC_TABLE");
        tableRuleConfig.setActualDataNodes("ds${0..1}.table_${0..2}");
        TableRule actual = new TableRule(tableRuleConfig, createShardingDataSourceNames());
        assertThat(actual.getActualDatasourceNames(), is((Collection<String>) Sets.newLinkedHashSet(Arrays.asList("ds0", "ds1"))));
    }
    
    @Test
    public void assertGetActualTableNames() {
        TableRuleConfiguration tableRuleConfig = new TableRuleConfiguration();
        tableRuleConfig.setLogicTable("LOGIC_TABLE");
        tableRuleConfig.setActualDataNodes("ds${0..1}.table_${0..2}");
        TableRule actual = new TableRule(tableRuleConfig, createShardingDataSourceNames());
        assertThat(actual.getActualTableNames("ds0"), is((Collection<String>) Sets.newLinkedHashSet(Arrays.asList("table_0", "table_1", "table_2"))));
        assertThat(actual.getActualTableNames("ds1"), is((Collection<String>) Sets.newLinkedHashSet(Arrays.asList("table_0", "table_1", "table_2"))));
        assertThat(actual.getActualTableNames("ds2"), is((Collection<String>) Collections.<String>emptySet()));
    }
    
    @Test
    public void assertFindActualTableIndex() {
        TableRuleConfiguration tableRuleConfig = new TableRuleConfiguration();
        tableRuleConfig.setLogicTable("LOGIC_TABLE");
        tableRuleConfig.setActualDataNodes("ds${0..1}.table_${0..2}");
        TableRule actual = new TableRule(tableRuleConfig, createShardingDataSourceNames());
        assertThat(actual.findActualTableIndex("ds1", "table_1"), is(4));
    }
    
    @Test
    public void assertNotFindActualTableIndex() {
        TableRuleConfiguration tableRuleConfig = new TableRuleConfiguration();
        tableRuleConfig.setLogicTable("LOGIC_TABLE");
        tableRuleConfig.setActualDataNodes("ds${0..1}.table_${0..2}");
        TableRule actual = new TableRule(tableRuleConfig, createShardingDataSourceNames());
        assertThat(actual.findActualTableIndex("ds2", "table_2"), is(-1));
    }
    
    @Test
    public void assertActualTableNameExisted() {
        TableRule actual = new TableRule(createTableRuleConfig(), createShardingDataSourceNames());
        assertTrue(actual.isExisted("table_2"));
    }
    
    @Test
    public void assertActualTableNameNotExisted() {
        TableRule actual = new TableRule(createTableRuleConfig(), createShardingDataSourceNames());
        assertFalse(actual.isExisted("table_3"));
    }
    
    @Test
    public void assertToString() {
        TableRule actual = new TableRule(createTableRuleConfig(), createShardingDataSourceNames());
        String actualString = "TableRule(logicTable=logic_table, actualDataNodes=[DataNode(dataSourceName=ds0, tableName=table_0), DataNode(dataSourceName=ds0, tableName=table_1), "
            + "DataNode(dataSourceName=ds0, tableName=table_2), DataNode(dataSourceName=ds1, tableName=table_0), DataNode(dataSourceName=ds1, tableName=table_1), "
            + "DataNode(dataSourceName=ds1, tableName=table_2)], databaseShardingStrategy=null, tableShardingStrategy=null, generateKeyColumn=null, keyGenerator=null, logicIndex=null)";
        assertThat(actual.toString(), is(actualString));
    }
    
    private TableRuleConfiguration createTableRuleConfig() {
        TableRuleConfiguration result = new TableRuleConfiguration();
        result.setLogicTable("LOGIC_TABLE");
        result.setActualDataNodes("ds${0..1}.table_${0..2}");
        return result;
    }
    
    private ShardingDataSourceNames createShardingDataSourceNames() {
        return new ShardingDataSourceNames(new ShardingRuleConfiguration(), Arrays.asList("ds0", "ds1"));
    }
}
