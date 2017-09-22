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

package io.shardingjdbc.core.rule;

import io.shardingjdbc.core.api.config.TableRuleConfiguration;
import io.shardingjdbc.core.api.config.strategy.NoneShardingStrategyConfiguration;
import io.shardingjdbc.core.keygen.fixture.IncrementKeyGenerator;
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
    public void assertTableRuleWithoutDataNode() {
        TableRuleConfiguration tableRuleConfig = new TableRuleConfiguration();
        tableRuleConfig.setLogicTable("logicTable");
        tableRuleConfig.setActualDataNodes("ds${0..1}.table_${0..2}");
        TableRule actual = tableRuleConfig.build(createDataSourceMap());
        assertActualTable(actual);
        assertNull(actual.getDatabaseShardingStrategy());
        assertNull(actual.getTableShardingStrategy());
    }
    
    @Test
    public void assertTableRuleWithDatabaseShardingStrategyWithoutDataNode() {
        TableRuleConfiguration tableRuleConfig = new TableRuleConfiguration();
        tableRuleConfig.setLogicTable("logicTable");
        tableRuleConfig.setActualDataNodes("ds${0..1}.table_${0..2}");
        tableRuleConfig.setDatabaseShardingStrategyConfig(new NoneShardingStrategyConfiguration());
        TableRule actual = tableRuleConfig.build(createDataSourceMap());
        assertActualTable(actual);
        assertNotNull(actual.getDatabaseShardingStrategy());
        assertNull(actual.getTableShardingStrategy());
    }
    
    @Test
    public void assertTableRuleWithTableShardingStrategyWithoutDataNode() {
        TableRuleConfiguration tableRuleConfig = new TableRuleConfiguration();
        tableRuleConfig.setLogicTable("logicTable");
        tableRuleConfig.setActualDataNodes("ds${0..1}.table_${0..2}");
        tableRuleConfig.setTableShardingStrategyConfig(new NoneShardingStrategyConfiguration());
        TableRule actual = tableRuleConfig.build(createDataSourceMap());
        assertActualTable(actual);
        assertNull(actual.getDatabaseShardingStrategy());
        assertNotNull(actual.getTableShardingStrategy());
    }
    
    @Test
    public void assertTableRuleWithDataNodeString() {
        TableRuleConfiguration tableRuleConfig = new TableRuleConfiguration();
        tableRuleConfig.setLogicTable("logicTable");
        tableRuleConfig.setActualDataNodes("ds${0..1}.table_${0..2}");
        TableRule actual = tableRuleConfig.build(createDataSourceMap());
        assertActualTable(actual);
        assertNull(actual.getDatabaseShardingStrategy());
        assertNull(actual.getTableShardingStrategy());
    }
    
    @Test
    public void assertTableRuleWithDataSourceNames() {
        TableRuleConfiguration tableRuleConfig = new TableRuleConfiguration();
        tableRuleConfig.setLogicTable("logicTable");
        tableRuleConfig.setActualDataNodes("ds${0..1}.table_${0..2}");
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
    public void assertGetActualDatasourceNames() {
        TableRuleConfiguration tableRuleConfig = new TableRuleConfiguration();
        tableRuleConfig.setLogicTable("logicTable");
        tableRuleConfig.setActualDataNodes("ds${0..1}.table_${0..2}");
        TableRule actual = tableRuleConfig.build(createDataSourceMap());
        assertThat(actual.getActualDatasourceNames(), is((Collection<String>) Sets.newLinkedHashSet(Arrays.asList("ds0", "ds1"))));
    }
    
    @Test
    public void assertGetActualTableNames() {
        TableRuleConfiguration tableRuleConfig = new TableRuleConfiguration();
        tableRuleConfig.setLogicTable("logicTable");
        tableRuleConfig.setActualDataNodes("ds${0..1}.table_${0..2}");
        TableRule actual = tableRuleConfig.build(createDataSourceMap());
        assertThat(actual.getActualTableNames("ds1"), is((Collection<String>) Sets.newLinkedHashSet(Arrays.asList("table_0", "table_1", "table_2"))));
    }
    
    @Test
    public void assertFindActualTableIndex() {
        TableRuleConfiguration tableRuleConfig = new TableRuleConfiguration();
        tableRuleConfig.setLogicTable("logicTable");
        tableRuleConfig.setActualDataNodes("ds${0..1}.table_${0..2}");
        TableRule actual = tableRuleConfig.build(createDataSourceMap());
        assertThat(actual.findActualTableIndex("ds1", "table_1"), is(4));
    }
    
    @Test
    public void assertFindActualTableIndexForNotFound() {
        TableRuleConfiguration tableRuleConfig = new TableRuleConfiguration();
        tableRuleConfig.setLogicTable("logicTable");
        tableRuleConfig.setActualDataNodes("ds${0..1}.table_${0..2}");
        TableRule actual = tableRuleConfig.build(createDataSourceMap());
        assertThat(actual.findActualTableIndex("ds2", "table_2"), is(-1));
    }
    
    @Test
    public void assertGenerateKeyColumn() {
        TableRuleConfiguration tableRuleConfig = new TableRuleConfiguration();
        tableRuleConfig.setLogicTable("logicTable");
        tableRuleConfig.setKeyGeneratorColumnName("col_1");
        tableRuleConfig.setKeyGeneratorClass(IncrementKeyGenerator.class.getName());
        TableRule actual = tableRuleConfig.build(createDataSourceMap());
        assertThat(actual.getGenerateKeyColumn(), is("col_1"));
        assertThat(actual.getKeyGenerator(), instanceOf(IncrementKeyGenerator.class));
    }
    
    private Map<String, DataSource> createDataSourceMap() {
        Map<String, DataSource> result = new HashMap<>(2, 1);
        result.put("ds0", null);
        result.put("ds1", null);
        return result;
    }
}
