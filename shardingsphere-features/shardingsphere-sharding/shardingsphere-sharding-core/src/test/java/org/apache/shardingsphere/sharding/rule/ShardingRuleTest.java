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

import org.apache.shardingsphere.infra.binder.segment.table.TablesContext;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.UpdateStatementContext;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.config.exception.ShardingSphereConfigurationException;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.sharding.algorithm.keygen.SnowflakeKeyGenerateAlgorithm;
import org.apache.shardingsphere.sharding.algorithm.keygen.fixture.IncrementKeyGenerateAlgorithm;
import org.apache.shardingsphere.sharding.algorithm.sharding.inline.InlineShardingAlgorithm;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.keygen.KeyGenerateStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ComplexShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.NoneShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.JoinTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLSelectStatement;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.TreeSet;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ShardingRuleTest {
    
    private static final String EQUAL = "=";
    
    private static final String AND = "AND";
    
    @Test
    public void assertNewShardingRuleWithMaximumConfiguration() {
        ShardingRule actual = createMaximumShardingRule();
        assertThat(actual.getTableRules().size(), is(2));
        assertThat(actual.getBindingTableRules().size(), is(2));
        assertTrue(actual.getBindingTableRules().containsKey("logic_table"));
        assertTrue(actual.getBindingTableRules().containsKey("sub_logic_table"));
        assertThat(actual.getBindingTableRules().values().iterator().next().getTableRules().size(), is(2));
        assertThat(actual.getBroadcastTables(), is(new TreeSet<>(Collections.singletonList("BROADCAST_TABLE"))));
        assertThat(actual.getDefaultKeyGenerateAlgorithm(), instanceOf(IncrementKeyGenerateAlgorithm.class));
        assertThat(actual.getDefaultShardingColumn(), is("table_id"));
    }
    
    @Test
    public void assertNewShardingRuleWithMinimumConfiguration() {
        ShardingRule actual = createMinimumShardingRule();
        assertThat(actual.getTableRules().size(), is(1));
        assertTrue(actual.getBindingTableRules().isEmpty());
        assertTrue(actual.getBroadcastTables().isEmpty());
        assertThat(actual.getDefaultKeyGenerateAlgorithm(), instanceOf(SnowflakeKeyGenerateAlgorithm.class));
        assertNull(actual.getDefaultShardingColumn());
    }
    
    @Test
    public void assertFindTableRule() {
        assertTrue(createMaximumShardingRule().findTableRule("logic_Table").isPresent());
    }
    
    @Test
    public void assertNotFindTableRule() {
        assertFalse(createMaximumShardingRule().findTableRule("other_Table").isPresent());
    }
    
    @Test
    public void assertFindTableRuleByActualTable() {
        assertTrue(createMaximumShardingRule().findTableRuleByActualTable("table_0").isPresent());
    }
    
    @Test
    public void assertNotFindTableRuleByActualTable() {
        assertFalse(createMaximumShardingRule().findTableRuleByActualTable("table_3").isPresent());
    }
    
    @Test
    public void assertFindLogicTableByActualTable() {
        assertTrue(createMaximumShardingRule().findLogicTableByActualTable("table_0").isPresent());
    }
    
    @Test
    public void assertNotFindLogicTableByActualTable() {
        assertFalse(createMaximumShardingRule().findLogicTableByActualTable("table_3").isPresent());
    }
    
    @Test
    public void assertGetTableRuleWithShardingTable() {
        TableRule actual = createMaximumShardingRule().getTableRule("Logic_Table");
        assertThat(actual.getLogicTable(), is("LOGIC_TABLE"));
    }
    
    @Test
    public void assertGetTableRuleWithBroadcastTable() {
        TableRule actual = createMaximumShardingRule().getTableRule("Broadcast_Table");
        assertThat(actual.getLogicTable(), is("Broadcast_Table"));
    }
    
    @Test(expected = ShardingSphereConfigurationException.class)
    public void assertGetTableRuleFailure() {
        createMinimumShardingRule().getTableRule("New_Table");
    }
    
    @Test
    public void assertIsAllBindingTableWhenLogicTablesIsEmpty() {
        assertFalse(createMaximumShardingRule().isAllBindingTables(Collections.emptyList()));
    }
    
    @Test
    public void assertIsNotAllBindingTable() {
        assertFalse(createMaximumShardingRule().isAllBindingTables(Collections.singleton("new_Table")));
        assertFalse(createMaximumShardingRule().isAllBindingTables(Arrays.asList("logic_Table", "new_Table")));
    }
    
    @Test
    public void assertIsAllBindingTable() {
        assertTrue(createMaximumShardingRule().isAllBindingTables(Collections.singleton("logic_Table")));
        assertTrue(createMaximumShardingRule().isAllBindingTables(Collections.singleton("logic_table")));
        assertTrue(createMaximumShardingRule().isAllBindingTables(Collections.singleton("sub_Logic_Table")));
        assertTrue(createMaximumShardingRule().isAllBindingTables(Collections.singleton("sub_logic_table")));
        assertTrue(createMaximumShardingRule().isAllBindingTables(Arrays.asList("logic_Table", "sub_Logic_Table")));
        assertTrue(createMaximumShardingRule().isAllBindingTables(Arrays.asList("logic_table", "sub_logic_Table")));
        assertFalse(createMaximumShardingRule().isAllBindingTables(Arrays.asList("logic_table", "sub_logic_Table", "new_table")));
        assertFalse(createMaximumShardingRule().isAllBindingTables(Collections.emptyList()));
        assertFalse(createMaximumShardingRule().isAllBindingTables(Collections.singleton("new_Table")));
    }
    
    @Test
    public void assertGetBindingTableRuleForNotConfig() {
        assertFalse(createMinimumShardingRule().findBindingTableRule("logic_Table").isPresent());
    }
    
    @Test
    public void assertGetBindingTableRuleForNotFound() {
        assertFalse(createMaximumShardingRule().findBindingTableRule("new_Table").isPresent());
    }
    
    @Test
    public void assertGetBindingTableRuleForFound() {
        ShardingRule actual = createMaximumShardingRule();
        assertTrue(actual.findBindingTableRule("logic_Table").isPresent());
        assertThat(actual.findBindingTableRule("logic_Table").get().getTableRules().size(), is(2));
    }
    
    @Test
    public void assertIsAllBroadcastTableWhenLogicTablesIsEmpty() {
        assertFalse(createMaximumShardingRule().isAllBroadcastTables(Collections.emptyList()));
    }
    
    @Test
    public void assertIsAllBroadcastTable() {
        assertTrue(createMaximumShardingRule().isAllBroadcastTables(Collections.singleton("Broadcast_Table")));
    }
    
    @Test
    public void assertIsNotAllBroadcastTable() {
        assertFalse(createMaximumShardingRule().isAllBroadcastTables(Arrays.asList("broadcast_table", "other_table")));
    }
    
    @Test
    public void assertIsBroadcastTable() {
        assertTrue(createMaximumShardingRule().isBroadcastTable("Broadcast_Table"));
    }
    
    @Test
    public void assertIsNotBroadcastTable() {
        assertFalse(createMaximumShardingRule().isBroadcastTable("other_table"));
    }
    
    @Test
    public void assertIsShardingTable() {
        assertTrue(createMaximumShardingRule().isShardingTable("LOGIC_TABLE"));
    }
    
    @Test
    public void assertIsNotShardingTable() {
        assertFalse(createMaximumShardingRule().isShardingTable("other_table"));
    }
    
    @Test
    public void assertFindShardingColumnForDefaultDatabaseShardingStrategy() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.getTables().add(createTableRuleConfigWithAllStrategies());
        shardingRuleConfig.setDefaultDatabaseShardingStrategy(new StandardShardingStrategyConfiguration("column", "STANDARD_TEST"));
        shardingRuleConfig.getShardingAlgorithms().put("standard", new ShardingSphereAlgorithmConfiguration("STANDARD_TEST", new Properties()));
        Optional<String> actual = new ShardingRule(shardingRuleConfig, createDataSourceNames()).findShardingColumn("column", "LOGIC_TABLE");
        assertTrue(actual.isPresent());
        assertThat(actual.get(), is("column"));
    }
    
    @Test
    public void assertFindShardingColumnForDefaultTableShardingStrategy() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.getTables().add(createTableRuleConfigWithAllStrategies());
        shardingRuleConfig.setDefaultTableShardingStrategy(new StandardShardingStrategyConfiguration("column", "STANDARD_TEST"));
        shardingRuleConfig.getShardingAlgorithms().put("standard", new ShardingSphereAlgorithmConfiguration("STANDARD_TEST", new Properties()));
        Optional<String> actual = new ShardingRule(shardingRuleConfig, createDataSourceNames()).findShardingColumn("column", "LOGIC_TABLE");
        assertTrue(actual.isPresent());
        assertThat(actual.get(), is("column"));
    }
    
    @Test
    public void assertFindShardingColumnForDatabaseShardingStrategy() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.getTables().add(createTableRuleConfigWithAllStrategies());
        shardingRuleConfig.getShardingAlgorithms().put("standard", new ShardingSphereAlgorithmConfiguration("STANDARD_TEST", new Properties()));
        Optional<String> actual = new ShardingRule(shardingRuleConfig, createDataSourceNames()).findShardingColumn("column", "logic_Table");
        assertTrue(actual.isPresent());
        assertThat(actual.get(), is("column"));
    }
    
    @Test
    public void assertFindShardingColumnForTableShardingStrategy() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.getTables().add(createTableRuleConfigWithTableStrategies());
        shardingRuleConfig.getShardingAlgorithms().put("standard", new ShardingSphereAlgorithmConfiguration("STANDARD_TEST", new Properties()));
        Optional<String> actual = new ShardingRule(shardingRuleConfig, createDataSourceNames()).findShardingColumn("column", "logic_Table");
        assertTrue(actual.isPresent());
        assertThat(actual.get(), is("column"));
    }
    
    @Test
    public void assertIsNotShardingColumn() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.getTables().add(createTableRuleConfigWithAllStrategies());
        Optional<String> actual = new ShardingRule(shardingRuleConfig, createDataSourceNames()).findShardingColumn("column", "other_Table");
        assertFalse(actual.isPresent());
    }
    
    @Test
    public void assertFindGenerateKeyColumn() {
        assertTrue(createMaximumShardingRule().findGenerateKeyColumnName("logic_table").isPresent());
    }
    
    @Test
    public void assertNotFindGenerateKeyColumn() {
        assertFalse(createMinimumShardingRule().findGenerateKeyColumnName("sub_logic_table").isPresent());
    }
    
    @Test(expected = ShardingSphereConfigurationException.class)
    public void assertGenerateKeyFailure() {
        createMaximumShardingRule().generateKey("table_0");
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertCreateInconsistentActualDatasourceNamesFailure() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        ShardingTableRuleConfiguration shardingTableRuleConfig = createTableRuleConfiguration("LOGIC_TABLE", "ds_${0..2}.table_${0..2}");
        shardingTableRuleConfig.setKeyGenerateStrategy(new KeyGenerateStrategyConfiguration("id", "increment"));
        ShardingTableRuleConfiguration subTableRuleConfig = createTableRuleConfiguration("SUB_LOGIC_TABLE", "ds_${0..1}.sub_table_${0..2}");
        shardingRuleConfig.getTables().add(shardingTableRuleConfig);
        shardingRuleConfig.getTables().add(subTableRuleConfig);
        shardingRuleConfig.getBindingTableGroups().add(shardingTableRuleConfig.getLogicTable() + "," + subTableRuleConfig.getLogicTable());
        new ShardingRule(shardingRuleConfig, createDataSourceNames());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertCreateInconsistentActualTableNamesFailure() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        ShardingTableRuleConfiguration shardingTableRuleConfig = createTableRuleConfiguration("LOGIC_TABLE", "ds_${0..1}.table_${0..3}");
        shardingTableRuleConfig.setKeyGenerateStrategy(new KeyGenerateStrategyConfiguration("id", "increment"));
        ShardingTableRuleConfiguration subTableRuleConfig = createTableRuleConfiguration("SUB_LOGIC_TABLE", "ds_${0..1}.sub_table_${0..2}");
        shardingRuleConfig.getTables().add(shardingTableRuleConfig);
        shardingRuleConfig.getTables().add(subTableRuleConfig);
        shardingRuleConfig.getBindingTableGroups().add(shardingTableRuleConfig.getLogicTable() + "," + subTableRuleConfig.getLogicTable());
        new ShardingRule(shardingRuleConfig, createDataSourceNames());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertCreateInconsistentAlgorithmExpressionOnDatabaseShardingStrategyFailure() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        ShardingTableRuleConfiguration shardingTableRuleConfig = createTableRuleConfiguration("LOGIC_TABLE", "ds_${0..1}.table_${0..2}");
        ShardingTableRuleConfiguration subTableRuleConfig = createTableRuleConfiguration("SUB_LOGIC_TABLE", "ds_${0..1}.sub_table_${0..2}");
        shardingRuleConfig.getTables().add(shardingTableRuleConfig);
        shardingRuleConfig.getTables().add(subTableRuleConfig);
        shardingRuleConfig.getBindingTableGroups().add(shardingTableRuleConfig.getLogicTable() + "," + subTableRuleConfig.getLogicTable());
        InlineShardingAlgorithm shardingAlgorithmDB = new InlineShardingAlgorithm();
        Properties shardingProps = new Properties();
        shardingProps.setProperty("algorithm-expression", "ds_%{ds_id % 2}");
        shardingAlgorithmDB.setProps(shardingProps);
        shardingTableRuleConfig.setDatabaseShardingStrategy(new StandardShardingStrategyConfiguration("ds_id", "shardingAlgorithmDB"));
        shardingRuleConfig.getShardingAlgorithms().put("shardingAlgorithmDB", new ShardingSphereAlgorithmConfiguration(shardingAlgorithmDB.getType(), shardingProps));
        InlineShardingAlgorithm subAlgorithmDB = new InlineShardingAlgorithm();
        Properties subProps = new Properties();
        subProps.setProperty("algorithm-expression", "ds_%{ds_id % 3}");
        subAlgorithmDB.setProps(subProps);
        subTableRuleConfig.setDatabaseShardingStrategy(new StandardShardingStrategyConfiguration("ds_id", "subAlgorithmDB"));
        shardingRuleConfig.getShardingAlgorithms().put("subAlgorithmDB", new ShardingSphereAlgorithmConfiguration(shardingAlgorithmDB.getType(), subProps));
        new ShardingRule(shardingRuleConfig, createDataSourceNames());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertCreateInconsistentAlgorithmExpressionOnTableShardingStrategyFailure() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        ShardingTableRuleConfiguration shardingTableRuleConfig = createTableRuleConfiguration("LOGIC_TABLE", "ds_${0..1}.table_${0..2}");
        ShardingTableRuleConfiguration subTableRuleConfig = createTableRuleConfiguration("SUB_LOGIC_TABLE", "ds_${0..1}.sub_table_${0..2}");
        shardingRuleConfig.getTables().add(shardingTableRuleConfig);
        shardingRuleConfig.getTables().add(subTableRuleConfig);
        shardingRuleConfig.getBindingTableGroups().add(shardingTableRuleConfig.getLogicTable() + "," + subTableRuleConfig.getLogicTable());
        InlineShardingAlgorithm shardingAlgorithmTBL = new InlineShardingAlgorithm();
        Properties shardingProps = new Properties();
        shardingProps.setProperty("algorithm-expression", "table_%{table_id % 2}");
        shardingAlgorithmTBL.setProps(shardingProps);
        shardingTableRuleConfig.setTableShardingStrategy(new StandardShardingStrategyConfiguration("table_id", "shardingAlgorithmTBL"));
        shardingRuleConfig.getShardingAlgorithms().put("shardingAlgorithmTBL", new ShardingSphereAlgorithmConfiguration(shardingAlgorithmTBL.getType(), shardingProps));
        InlineShardingAlgorithm subAlgorithmTBL = new InlineShardingAlgorithm();
        Properties subProps = new Properties();
        subProps.setProperty("algorithm-expression", "table_%{table_id % 3}");
        subAlgorithmTBL.setProps(subProps);
        subTableRuleConfig.setTableShardingStrategy(new StandardShardingStrategyConfiguration("table_id", "subAlgorithmTBL"));
        shardingRuleConfig.getShardingAlgorithms().put("subAlgorithmTBL", new ShardingSphereAlgorithmConfiguration(subAlgorithmTBL.getType(), subProps));
        new ShardingRule(shardingRuleConfig, createDataSourceNames());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertCreateInconsistentAlgorithmExpressionWithDefaultAndSpecifiedTableShardingStrategyFailure() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        ShardingTableRuleConfiguration shardingTableRuleConfig = createTableRuleConfiguration("LOGIC_TABLE", "ds_${0..1}.table_${0..2}");
        ShardingTableRuleConfiguration subTableRuleConfig = createTableRuleConfiguration("SUB_LOGIC_TABLE", "ds_${0..1}.sub_table_${0..2}");
        shardingRuleConfig.getTables().add(shardingTableRuleConfig);
        shardingRuleConfig.getTables().add(subTableRuleConfig);
        shardingRuleConfig.getBindingTableGroups().add(shardingTableRuleConfig.getLogicTable() + "," + subTableRuleConfig.getLogicTable());
        InlineShardingAlgorithm shardingAlgorithmTBL = new InlineShardingAlgorithm();
        Properties shardingProps = new Properties();
        shardingProps.setProperty("algorithm-expression", "table_%{table_id % 2}");
        shardingAlgorithmTBL.setProps(shardingProps);
        shardingTableRuleConfig.setTableShardingStrategy(new StandardShardingStrategyConfiguration("table_id", "shardingAlgorithmTBL"));
        shardingRuleConfig.getShardingAlgorithms().put("shardingAlgorithmTBL", new ShardingSphereAlgorithmConfiguration(shardingAlgorithmTBL.getType(), shardingProps));
        InlineShardingAlgorithm subAlgorithmTBL = new InlineShardingAlgorithm();
        Properties subProps = new Properties();
        subProps.setProperty("algorithm-expression", "table_%{table_id % 3}");
        subAlgorithmTBL.setProps(subProps);
        shardingRuleConfig.setDefaultTableShardingStrategy(new StandardShardingStrategyConfiguration("table_id", "table_inline"));
        shardingRuleConfig.setDefaultShardingColumn("table_id");
        shardingRuleConfig.getShardingAlgorithms().put("table_inline", new ShardingSphereAlgorithmConfiguration("INLINE", subProps));
        new ShardingRule(shardingRuleConfig, createDataSourceNames());
    }
    
    @Test
    public void assertGenerateKeyWithDefaultKeyGenerator() {
        assertThat(createMinimumShardingRule().generateKey("logic_table"), instanceOf(Long.class));
    }
    
    @Test
    public void assertGenerateKeyWithKeyGenerator() {
        assertThat(createMaximumShardingRule().generateKey("logic_table"), instanceOf(Integer.class));
    }
    
    @Test
    public void assertGetDataNodeByLogicTable() {
        assertThat(createMaximumShardingRule().getDataNode("logic_table"), is(new DataNode("ds_0.table_0")));
    }
    
    @Test
    public void assertGetShardingLogicTableNames() {
        ShardingRule actual = createMaximumShardingRule();
        assertThat(actual.getShardingLogicTableNames(Arrays.asList("LOGIC_TABLE", "BROADCAST_TABLE")), is(Collections.singletonList("LOGIC_TABLE")));
    }
    
    @Test
    public void assertTableRuleExists() {
        assertTrue(createMaximumShardingRule().tableRuleExists(Collections.singleton("logic_table")));
    }
    
    @Test
    public void assertTableRuleExistsForMultipleTables() {
        assertTrue(createMaximumShardingRule().tableRuleExists(Arrays.asList("logic_table", "table_0")));
    }
    
    @Test
    public void assertTableRuleNotExists() {
        assertFalse(createMinimumShardingRule().tableRuleExists(Collections.singleton("table_0")));
    }
    
    @Test
    public void assertGetTables() {
        assertThat(createMaximumShardingRule().getTables(), is(new LinkedHashSet<>(Arrays.asList("LOGIC_TABLE", "SUB_LOGIC_TABLE", "BROADCAST_TABLE"))));
    }
    
    @Test
    public void assertGetDataSourceNamesWithShardingAutoTables() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        ShardingTableRuleConfiguration tableRuleConfig = new ShardingTableRuleConfiguration("logic_table", "ds_${0..1}.table_${0..2}");
        shardingRuleConfig.getTables().add(tableRuleConfig);
        ShardingAutoTableRuleConfiguration autoTableRuleConfig = new ShardingAutoTableRuleConfiguration("auto_table", "resource0, resource1");
        autoTableRuleConfig.setShardingStrategy(new StandardShardingStrategyConfiguration("order_id", "hash_mod"));
        shardingRuleConfig.getAutoTables().add(autoTableRuleConfig);
        Properties props = new Properties();
        props.put("sharding-count", 4);
        shardingRuleConfig.getShardingAlgorithms().put("hash_mod", new ShardingSphereAlgorithmConfiguration("hash_mod", props));
        ShardingRule shardingRule = new ShardingRule(shardingRuleConfig, createDataSourceNames());
        assertThat(shardingRule.getDataSourceNames(), is(new LinkedHashSet<>(Arrays.asList("ds_0", "ds_1", "resource0", "resource1"))));
    }
    
    @Test
    public void assertGetDataSourceNamesWithoutShardingAutoTables() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        ShardingTableRuleConfiguration shardingTableRuleConfig = new ShardingTableRuleConfiguration("LOGIC_TABLE", "ds_${0..1}.table_${0..2}");
        shardingRuleConfig.getTables().add(shardingTableRuleConfig);
        ShardingRule shardingRule = new ShardingRule(shardingRuleConfig, createDataSourceNames());
        assertThat(shardingRule.getDataSourceNames(), is(new LinkedHashSet<>(Arrays.asList("ds_0", "ds_1"))));
    }
    
    @Test
    public void assertGetDataSourceNamesWithShardingAutoTablesAndInlineExpression() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        ShardingAutoTableRuleConfiguration autoTableRuleConfig = new ShardingAutoTableRuleConfiguration("auto_table", "resource${0..1}");
        autoTableRuleConfig.setShardingStrategy(new StandardShardingStrategyConfiguration("order_id", "hash_mod"));
        shardingRuleConfig.getAutoTables().add(autoTableRuleConfig);
        Properties props = new Properties();
        props.put("sharding-count", 4);
        shardingRuleConfig.getShardingAlgorithms().put("hash_mod", new ShardingSphereAlgorithmConfiguration("hash_mod", props));
        ShardingRule shardingRule = new ShardingRule(shardingRuleConfig, createDataSourceNames());
        assertThat(shardingRule.getDataSourceNames(), is(new LinkedHashSet<>(Arrays.asList("resource0", "resource1"))));
    }
    
    @Test
    public void assertGetDataSourceNamesWithoutShardingTablesAndShardingAutoTables() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        ShardingRule shardingRule = new ShardingRule(shardingRuleConfig, createDataSourceNames());
        assertThat(shardingRule.getDataSourceNames(), is(Arrays.asList("ds_0", "ds_1", "resource0", "resource1")));
    }
    
    private ShardingRule createMaximumShardingRule() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        ShardingTableRuleConfiguration shardingTableRuleConfig = createTableRuleConfiguration("LOGIC_TABLE", "ds_${0..1}.table_${0..2}");
        shardingTableRuleConfig.setKeyGenerateStrategy(new KeyGenerateStrategyConfiguration("id", "increment"));
        ShardingTableRuleConfiguration subTableRuleConfig = createTableRuleConfiguration("SUB_LOGIC_TABLE", "ds_${0..1}.sub_table_${0..2}");
        shardingRuleConfig.getTables().add(shardingTableRuleConfig);
        shardingRuleConfig.getTables().add(subTableRuleConfig);
        shardingRuleConfig.getBindingTableGroups().add(shardingTableRuleConfig.getLogicTable() + "," + subTableRuleConfig.getLogicTable());
        shardingRuleConfig.getBroadcastTables().add("BROADCAST_TABLE");
        InlineShardingAlgorithm shardingAlgorithmDB = new InlineShardingAlgorithm();
        Properties props = new Properties();
        props.setProperty("algorithm-expression", "ds_%{ds_id % 2}");
        shardingAlgorithmDB.setProps(props);
        shardingRuleConfig.setDefaultDatabaseShardingStrategy(new StandardShardingStrategyConfiguration("ds_id", "standard"));
        InlineShardingAlgorithm shardingAlgorithmTBL = new InlineShardingAlgorithm();
        props = new Properties();
        props.setProperty("algorithm-expression", "table_%{table_id % 2}");
        shardingAlgorithmTBL.setProps(props);
        shardingRuleConfig.setDefaultTableShardingStrategy(new StandardShardingStrategyConfiguration("table_id", "standard"));
        shardingRuleConfig.setDefaultShardingColumn("table_id");
        shardingRuleConfig.setDefaultKeyGenerateStrategy(new KeyGenerateStrategyConfiguration("id", "default"));
        shardingRuleConfig.getShardingAlgorithms().put("standard", new ShardingSphereAlgorithmConfiguration("STANDARD_TEST", new Properties()));
        shardingRuleConfig.getKeyGenerators().put("increment", new ShardingSphereAlgorithmConfiguration("INCREMENT", new Properties()));
        shardingRuleConfig.getKeyGenerators().put("default", new ShardingSphereAlgorithmConfiguration("INCREMENT", new Properties()));
        return new ShardingRule(shardingRuleConfig, createDataSourceNames());
    }
    
    private ShardingRule createMinimumShardingRule() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        ShardingTableRuleConfiguration shardingTableRuleConfig = createTableRuleConfiguration("LOGIC_TABLE", "ds_${0..1}.table_${0..2}");
        shardingRuleConfig.getTables().add(shardingTableRuleConfig);
        return new ShardingRule(shardingRuleConfig, createDataSourceNames());
    }
    
    private ShardingTableRuleConfiguration createTableRuleConfiguration(final String logicTableName, final String actualDataNodes) {
        ShardingTableRuleConfiguration result = new ShardingTableRuleConfiguration(logicTableName, actualDataNodes);
        result.setDatabaseShardingStrategy(new StandardShardingStrategyConfiguration("user_id", "database_inline"));
        result.setTableShardingStrategy(new StandardShardingStrategyConfiguration("order_id", "table_inline"));
        return result;
    }
    
    private Collection<String> createDataSourceNames() {
        return Arrays.asList("ds_0", "ds_1", "resource0", "resource1");
    }
    
    private ShardingTableRuleConfiguration createTableRuleConfigWithAllStrategies() {
        ShardingTableRuleConfiguration result = new ShardingTableRuleConfiguration("LOGIC_TABLE", "ds_${0..1}.table_${0..2}");
        result.setDatabaseShardingStrategy(new StandardShardingStrategyConfiguration("column", "standard"));
        result.setTableShardingStrategy(new NoneShardingStrategyConfiguration());
        return result;
    }
    
    private ShardingTableRuleConfiguration createTableRuleConfigWithTableStrategies() {
        ShardingTableRuleConfiguration result = new ShardingTableRuleConfiguration("LOGIC_TABLE", "ds_${0..1}.table_${0..2}");
        result.setTableShardingStrategy(new StandardShardingStrategyConfiguration("column", "standard"));
        return result;
    }
    
    @Test
    public void assertFindShardingColumnForComplexShardingStrategy() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.getTables().add(createTableRuleConfigWithComplexStrategies());
        Optional<String> actual = new ShardingRule(shardingRuleConfig, createDataSourceNames()).findShardingColumn("column1", "LOGIC_TABLE");
        assertTrue(actual.isPresent());
        assertThat(actual.get(), is("COLUMN1"));
    }
    
    @Test
    public void assertGetRuleType() {
        ShardingRule shardingRule = createMinimumShardingRule();
        assertThat(shardingRule.getType(), is(ShardingRule.class.getSimpleName()));
    }
    
    private ShardingTableRuleConfiguration createTableRuleConfigWithComplexStrategies() {
        ShardingTableRuleConfiguration result = new ShardingTableRuleConfiguration("LOGIC_TABLE", "ds_${0..1}.table_${0..2}");
        result.setDatabaseShardingStrategy(new ComplexShardingStrategyConfiguration("COLUMN1,COLUMN2", "COMPLEX_TEST"));
        result.setTableShardingStrategy(new NoneShardingStrategyConfiguration());
        return result;
    }
    
    @Test
    public void assertIsAllBindingTableWithUpdateStatementContext() {
        SQLStatementContext<?> sqlStatementContext = mock(UpdateStatementContext.class);
        assertTrue(createMaximumShardingRule().isAllBindingTables(mock(ShardingSphereSchema.class), sqlStatementContext, Arrays.asList("logic_Table", "sub_Logic_Table")));
    }
    
    @Test
    public void assertIsAllBindingTableWithoutJoinQuery() {
        SelectStatementContext sqlStatementContext = mock(SelectStatementContext.class);
        when(sqlStatementContext.isContainsJoinQuery()).thenReturn(false);
        assertTrue(createMaximumShardingRule().isAllBindingTables(mock(ShardingSphereSchema.class), sqlStatementContext, Arrays.asList("logic_Table", "sub_Logic_Table")));
    }
    
    @Test
    public void assertIsAllBindingTableWithJoinQueryWithoutJoinCondition() {
        SelectStatementContext sqlStatementContext = mock(SelectStatementContext.class);
        when(sqlStatementContext.isContainsJoinQuery()).thenReturn(true);
        when(sqlStatementContext.getSqlStatement()).thenReturn(mock(MySQLSelectStatement.class));
        assertFalse(createMaximumShardingRule().isAllBindingTables(mock(ShardingSphereSchema.class), sqlStatementContext, Arrays.asList("logic_Table", "sub_Logic_Table")));
    }
    
    @Test
    public void assertIsAllBindingTableWithJoinQueryWithDatabaseJoinCondition() {
        ColumnSegment leftDatabaseJoin = createColumnSegment("user_id", "logic_Table");
        ColumnSegment rightDatabaseJoin = createColumnSegment("user_id", "sub_Logic_Table");
        BinaryOperationExpression condition = createBinaryOperationExpression(leftDatabaseJoin, rightDatabaseJoin, EQUAL);
        JoinTableSegment joinTable = mock(JoinTableSegment.class);
        when(joinTable.getCondition()).thenReturn(condition);
        MySQLSelectStatement selectStatement = mock(MySQLSelectStatement.class);
        when(selectStatement.getFrom()).thenReturn(joinTable);
        SelectStatementContext sqlStatementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.getSqlStatement()).thenReturn(selectStatement);
        when(sqlStatementContext.isContainsJoinQuery()).thenReturn(true);
        ShardingSphereSchema schema = mock(ShardingSphereSchema.class);
        when(sqlStatementContext.getTablesContext().findTableNamesByColumnSegment(Arrays.asList(leftDatabaseJoin, rightDatabaseJoin), schema)).thenReturn(createColumnTableNameMap());
        assertFalse(createMaximumShardingRule().isAllBindingTables(schema, sqlStatementContext, Arrays.asList("logic_Table", "sub_Logic_Table")));
    }
    
    @Test
    public void assertIsAllBindingTableWithJoinQueryWithDatabaseJoinConditionInUpperCaseAndNoOwner() {
        ColumnSegment leftDatabaseJoin = createColumnSegment("USER_ID", "LOGIC_TABLE");
        ColumnSegment rightDatabaseJoin = createColumnSegment("UID", null);
        BinaryOperationExpression condition = createBinaryOperationExpression(leftDatabaseJoin, rightDatabaseJoin, EQUAL);
        JoinTableSegment joinTable = mock(JoinTableSegment.class);
        when(joinTable.getCondition()).thenReturn(condition);
        MySQLSelectStatement selectStatement = mock(MySQLSelectStatement.class);
        when(selectStatement.getFrom()).thenReturn(joinTable);
        SelectStatementContext sqlStatementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.getSqlStatement()).thenReturn(selectStatement);
        when(sqlStatementContext.isContainsJoinQuery()).thenReturn(true);
        Collection<SimpleTableSegment> tableSegments = Arrays.asList(
                new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("LOGIC_TABLE"))),
                new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("SUB_LOGIC_TABLE")))
        );
        TablesContext tablesContext = new TablesContext(tableSegments, Collections.emptyMap(), DatabaseTypeRegistry.getDefaultDatabaseType());
        when(sqlStatementContext.getTablesContext()).thenReturn(tablesContext);
        ShardingSphereSchema schema = mock(ShardingSphereSchema.class);
        when(schema.getAllColumnNames("LOGIC_TABLE")).thenReturn(Arrays.asList("user_id", "order_id"));
        when(schema.getAllColumnNames("SUB_LOGIC_TABLE")).thenReturn(Arrays.asList("uid", "order_id"));
        assertFalse(createMaximumShardingRule().isAllBindingTables(schema, sqlStatementContext, Arrays.asList("LOGIC_TABLE", "SUB_LOGIC_TABLE")));
    }
    
    @Test
    public void assertIsAllBindingTableWithJoinQueryWithDatabaseTableJoinCondition() {
        ColumnSegment leftDatabaseJoin = createColumnSegment("user_id", "logic_Table");
        ColumnSegment rightDatabaseJoin = createColumnSegment("user_id", "sub_Logic_Table");
        BinaryOperationExpression databaseJoin = createBinaryOperationExpression(leftDatabaseJoin, rightDatabaseJoin, EQUAL);
        ColumnSegment leftTableJoin = createColumnSegment("order_id", "logic_Table");
        ColumnSegment rightTableJoin = createColumnSegment("order_id", "sub_Logic_Table");
        BinaryOperationExpression tableJoin = createBinaryOperationExpression(leftTableJoin, rightTableJoin, EQUAL);
        JoinTableSegment joinTable = mock(JoinTableSegment.class);
        BinaryOperationExpression condition = createBinaryOperationExpression(databaseJoin, tableJoin, AND);
        when(joinTable.getCondition()).thenReturn(condition);
        MySQLSelectStatement selectStatement = mock(MySQLSelectStatement.class);
        when(selectStatement.getFrom()).thenReturn(joinTable);
        SelectStatementContext sqlStatementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.getSqlStatement()).thenReturn(selectStatement);
        when(sqlStatementContext.isContainsJoinQuery()).thenReturn(true);
        ShardingSphereSchema schema = mock(ShardingSphereSchema.class);
        when(sqlStatementContext.getTablesContext().findTableNamesByColumnSegment(Arrays.asList(leftDatabaseJoin, rightDatabaseJoin), schema)).thenReturn(createColumnTableNameMap());
        when(sqlStatementContext.getTablesContext().findTableNamesByColumnSegment(Arrays.asList(leftTableJoin, rightTableJoin), schema)).thenReturn(createColumnTableNameMap());
        assertTrue(createMaximumShardingRule().isAllBindingTables(schema, sqlStatementContext, Arrays.asList("logic_Table", "sub_Logic_Table")));
    }
    
    @Test
    public void assertIsAllTablesInSameDataSource() {
        Collection<String> logicTableNames = new LinkedHashSet<>();
        logicTableNames.add("logic_Table");
        ShardingRuleConfiguration config = new ShardingRuleConfiguration();
        Collection<String> dataSourceNames = new LinkedHashSet<>();
        dataSourceNames.add("resource0");
        ShardingTableRuleConfiguration shardingTableRuleConfiguration = new ShardingTableRuleConfiguration("LOGIC_TABLE", "ds_${0}.table_${0..2}");
        config.getTables().add(shardingTableRuleConfiguration);
        ShardingRule shardingRule = new ShardingRule(config, dataSourceNames);
        assertTrue(shardingRule.isAllTablesInSameDataSource(logicTableNames));
    }
    
    private BinaryOperationExpression createBinaryOperationExpression(final ExpressionSegment left, final ExpressionSegment right, final String operator) {
        BinaryOperationExpression result = mock(BinaryOperationExpression.class);
        when(result.getLeft()).thenReturn(left);
        when(result.getRight()).thenReturn(right);
        when(result.getOperator()).thenReturn(operator);
        return result;
    }
    
    private ColumnSegment createColumnSegment(final String columnName, final String owner) {
        ColumnSegment result = new ColumnSegment(0, 0, new IdentifierValue(columnName));
        if (null != owner) {
            result.setOwner(new OwnerSegment(0, 0, new IdentifierValue(owner)));
        }
        return result;
    }
    
    private Map<String, String> createColumnTableNameMap() {
        Map<String, String> result = new HashMap<>();
        result.put("logic_Table.user_id", "logic_Table");
        result.put("sub_Logic_Table.user_id", "sub_Logic_Table");
        result.put("logic_Table.order_id", "logic_Table");
        result.put("sub_Logic_Table.order_id", "sub_Logic_Table");
        return result;
    }
    
    @Test
    public void assertGetLogicTablesByActualTable() {
        assertThat(createShardingRuleWithSameActualTablesButDifferentLogicTables().getLogicTablesByActualTable("table_0"),
                is(new LinkedHashSet<>(Arrays.asList("ID_STRATEGY_LOGIC_TABLE", "HINT_STRATEGY_LOGIC_TABLE"))));
    }
    
    private ShardingRule createShardingRuleWithSameActualTablesButDifferentLogicTables() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        ShardingTableRuleConfiguration idTableRuleConfig = createTableRuleConfiguration("ID_STRATEGY_LOGIC_TABLE", "ds_${0..1}.table_${0..2}");
        ShardingTableRuleConfiguration hintTableRuleConfig = createTableRuleConfiguration("HINT_STRATEGY_LOGIC_TABLE", "ds_${0..1}.table_${0..2}");
        shardingRuleConfig.getTables().add(idTableRuleConfig);
        shardingRuleConfig.getTables().add(hintTableRuleConfig);
        return new ShardingRule(shardingRuleConfig, createDataSourceNames());
    }
    
    @Test
    public void assertGetDataNodesByTableName() {
        ShardingRule shardingRule = createMinimumShardingRule();
        Collection<DataNode> actual = shardingRule.getDataNodesByTableName("logic_table");
        assertThat(actual.size(), is(6));
        Iterator<DataNode> iterator = actual.iterator();
        DataNode firstDataNode = iterator.next();
        assertThat(firstDataNode.getDataSourceName(), is("ds_0"));
        assertThat(firstDataNode.getTableName(), is("table_0"));
        DataNode secondDataNode = iterator.next();
        assertThat(secondDataNode.getDataSourceName(), is("ds_0"));
        assertThat(secondDataNode.getTableName(), is("table_1"));
        DataNode thirdDataNode = iterator.next();
        assertThat(thirdDataNode.getDataSourceName(), is("ds_0"));
        assertThat(thirdDataNode.getTableName(), is("table_2"));
        DataNode fourthDataNode = iterator.next();
        assertThat(fourthDataNode.getDataSourceName(), is("ds_1"));
        assertThat(fourthDataNode.getTableName(), is("table_0"));
        DataNode fifthDataNode = iterator.next();
        assertThat(fifthDataNode.getDataSourceName(), is("ds_1"));
        assertThat(fifthDataNode.getTableName(), is("table_1"));
        DataNode sixthDataNode = iterator.next();
        assertThat(sixthDataNode.getDataSourceName(), is("ds_1"));
        assertThat(sixthDataNode.getTableName(), is("table_2"));
    }
}
