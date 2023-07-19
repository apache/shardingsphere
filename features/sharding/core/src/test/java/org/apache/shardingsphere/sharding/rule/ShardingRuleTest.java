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
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.database.core.DefaultDatabase;
import org.apache.shardingsphere.infra.database.core.type.DatabaseTypeFactory;
import org.apache.shardingsphere.infra.database.mysql.MySQLDatabaseType;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.sharding.algorithm.audit.DMLShardingConditionsShardingAuditAlgorithm;
import org.apache.shardingsphere.sharding.algorithm.keygen.SnowflakeKeyGenerateAlgorithm;
import org.apache.shardingsphere.sharding.algorithm.keygen.UUIDKeyGenerateAlgorithm;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableReferenceRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.audit.ShardingAuditStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.keygen.KeyGenerateStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ComplexShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.NoneShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.exception.algorithm.keygen.GenerateKeyStrategyNotFoundException;
import org.apache.shardingsphere.sharding.exception.algorithm.sharding.ShardingAlgorithmInitializationException;
import org.apache.shardingsphere.sharding.exception.metadata.InvalidBindingTablesException;
import org.apache.shardingsphere.sharding.exception.metadata.ShardingTableRuleNotFoundException;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.JoinTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLSelectStatement;
import org.apache.shardingsphere.test.util.PropertiesBuilder;
import org.apache.shardingsphere.test.util.PropertiesBuilder.Property;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShardingRuleTest {
    
    private static final String EQUAL = "=";
    
    private static final String AND = "AND";
    
    @Test
    void assertNewShardingRuleWithMaximumConfiguration() {
        ShardingRule actual = createMaximumShardingRule();
        assertThat(actual.getTableRules().size(), is(2));
        assertThat(actual.getBindingTableRules().size(), is(2));
        assertTrue(actual.getBindingTableRules().containsKey("logic_table"));
        assertTrue(actual.getBindingTableRules().containsKey("sub_logic_table"));
        assertThat(actual.getBindingTableRules().values().iterator().next().getTableRules().size(), is(2));
        assertThat(actual.getDefaultKeyGenerateAlgorithm(), instanceOf(UUIDKeyGenerateAlgorithm.class));
        assertThat(actual.getAuditors().get("audit_algorithm"), instanceOf(DMLShardingConditionsShardingAuditAlgorithm.class));
        assertThat(actual.getDefaultShardingColumn(), is("table_id"));
    }
    
    @Test
    void assertNewShardingRuleWithMinimumConfiguration() {
        ShardingRule actual = createMinimumShardingRule();
        assertThat(actual.getTableRules().size(), is(1));
        assertTrue(actual.getBindingTableRules().isEmpty());
        assertThat(actual.getDefaultKeyGenerateAlgorithm(), instanceOf(SnowflakeKeyGenerateAlgorithm.class));
        assertNull(actual.getDefaultShardingColumn());
    }
    
    @Test
    void assertNewShardingRuleWithWrongShardingAlgorithmInDefaultDatabaseShardingStrategy() {
        ShardingRuleConfiguration ruleConfig = new ShardingRuleConfiguration();
        ruleConfig.setDefaultDatabaseShardingStrategy(new StandardShardingStrategyConfiguration("order_id", "MOD"));
        ruleConfig.getShardingAlgorithms().put("MOD", new AlgorithmConfiguration("MOD", PropertiesBuilder.build(new Property("sharding-count", "2"))));
        assertThrows(ShardingAlgorithmInitializationException.class, () -> new ShardingRule(ruleConfig, Collections.emptyList(), mock(InstanceContext.class)));
    }
    
    @Test
    void assertNewShardingRuleWithWrongShardingAlgorithmInDefaultTableShardingStrategy() {
        ShardingRuleConfiguration ruleConfig = new ShardingRuleConfiguration();
        ruleConfig.setDefaultTableShardingStrategy(new StandardShardingStrategyConfiguration("order_id", "MOD"));
        ruleConfig.getShardingAlgorithms().put("MOD", new AlgorithmConfiguration("MOD", PropertiesBuilder.build(new Property("sharding-count", "2"))));
        assertThrows(ShardingAlgorithmInitializationException.class, () -> new ShardingRule(ruleConfig, Collections.emptyList(), mock(InstanceContext.class)));
    }
    
    @Test
    void assertNewShardingRuleWithWrongShardingAlgorithmInDatabaseShardingStrategy() {
        ShardingRuleConfiguration ruleConfig = new ShardingRuleConfiguration();
        ShardingTableRuleConfiguration tableRuleConfig = new ShardingTableRuleConfiguration("t_order", "");
        tableRuleConfig.setDatabaseShardingStrategy(new StandardShardingStrategyConfiguration("order_id", "MOD"));
        ruleConfig.getTables().add(tableRuleConfig);
        ruleConfig.getShardingAlgorithms().put("MOD", new AlgorithmConfiguration("MOD", PropertiesBuilder.build(new Property("sharding-count", "2"))));
        assertThrows(ShardingAlgorithmInitializationException.class, () -> new ShardingRule(ruleConfig, Collections.emptyList(), mock(InstanceContext.class)));
    }
    
    @Test
    void assertNewShardingRuleWithWrongShardingAlgorithmInTableShardingStrategy() {
        ShardingRuleConfiguration ruleConfig = new ShardingRuleConfiguration();
        ShardingTableRuleConfiguration tableRuleConfig = new ShardingTableRuleConfiguration("t_order", "");
        tableRuleConfig.setTableShardingStrategy(new StandardShardingStrategyConfiguration("order_id", "MOD"));
        ruleConfig.getTables().add(tableRuleConfig);
        ruleConfig.getShardingAlgorithms().put("MOD", new AlgorithmConfiguration("MOD", PropertiesBuilder.build(new Property("sharding-count", "2"))));
        assertThrows(ShardingAlgorithmInitializationException.class, () -> new ShardingRule(ruleConfig, Collections.emptyList(), mock(InstanceContext.class)));
    }
    
    @Test
    void assertNewShardingRuleWithWrongShardingAlgorithmInAutoTableShardingStrategy() {
        ShardingRuleConfiguration ruleConfig = new ShardingRuleConfiguration();
        ShardingAutoTableRuleConfiguration autoTableRuleConfig = new ShardingAutoTableRuleConfiguration("t_order", "ds_0,ds_1");
        autoTableRuleConfig.setShardingStrategy(new StandardShardingStrategyConfiguration("order_id", "INLINE"));
        ruleConfig.getAutoTables().add(autoTableRuleConfig);
        ruleConfig.getShardingAlgorithms().put("INLINE", new AlgorithmConfiguration("INLINE", PropertiesBuilder.build(new Property("algorithm-expression", "t_order_%{order_id % 2}"))));
        assertThrows(ShardingAlgorithmInitializationException.class, () -> new ShardingRule(ruleConfig, Collections.emptyList(), mock(InstanceContext.class)));
    }
    
    @Test
    void assertFindTableRule() {
        assertTrue(createMaximumShardingRule().findTableRule("logic_Table").isPresent());
    }
    
    @Test
    void assertNotFindTableRule() {
        assertFalse(createMaximumShardingRule().findTableRule("other_Table").isPresent());
    }
    
    @Test
    void assertNotFindTableRuleWhenTableNameIsNull() {
        assertFalse(createMaximumShardingRule().findTableRule(null).isPresent());
    }
    
    @Test
    void assertFindTableRuleByActualTable() {
        assertTrue(createMaximumShardingRule().findTableRuleByActualTable("table_0").isPresent());
    }
    
    @Test
    void assertNotFindTableRuleByActualTable() {
        assertFalse(createMaximumShardingRule().findTableRuleByActualTable("table_3").isPresent());
    }
    
    @Test
    void assertFindLogicTableByActualTable() {
        assertTrue(createMaximumShardingRule().findLogicTableByActualTable("table_0").isPresent());
    }
    
    @Test
    void assertNotFindLogicTableByActualTable() {
        assertFalse(createMaximumShardingRule().findLogicTableByActualTable("table_3").isPresent());
    }
    
    @Test
    void assertGetTableRuleWithShardingTable() {
        TableRule actual = createMaximumShardingRule().getTableRule("Logic_Table");
        assertThat(actual.getLogicTable(), is("LOGIC_TABLE"));
    }
    
    @Test
    void assertGetTableRuleFailure() {
        assertThrows(ShardingTableRuleNotFoundException.class, () -> createMinimumShardingRule().getTableRule("New_Table"));
    }
    
    @Test
    void assertIsAllBindingTableWhenLogicTablesIsEmpty() {
        assertFalse(createMaximumShardingRule().isAllBindingTables(Collections.emptyList()));
    }
    
    @Test
    void assertIsNotAllBindingTable() {
        assertFalse(createMaximumShardingRule().isAllBindingTables(Collections.singleton("new_Table")));
        assertFalse(createMaximumShardingRule().isAllBindingTables(Arrays.asList("logic_Table", "new_Table")));
    }
    
    @Test
    void assertIsAllBindingTable() {
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
    void assertGetBindingTableRuleForNotConfig() {
        assertFalse(createMinimumShardingRule().findBindingTableRule("logic_Table").isPresent());
    }
    
    @Test
    void assertGetBindingTableRuleForNotFound() {
        assertFalse(createMaximumShardingRule().findBindingTableRule("new_Table").isPresent());
    }
    
    @Test
    void assertGetBindingTableRuleForFound() {
        ShardingRule actual = createMaximumShardingRule();
        assertTrue(actual.findBindingTableRule("logic_Table").isPresent());
        assertThat(actual.findBindingTableRule("logic_Table").get().getTableRules().size(), is(2));
    }
    
    @Test
    void assertIsShardingTable() {
        assertTrue(createMaximumShardingRule().isShardingTable("LOGIC_TABLE"));
    }
    
    @Test
    void assertIsNotShardingTable() {
        assertFalse(createMaximumShardingRule().isShardingTable("other_table"));
    }
    
    @Test
    void assertFindShardingColumnForDefaultDatabaseShardingStrategy() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.getTables().add(createTableRuleConfigWithAllStrategies());
        shardingRuleConfig.setDefaultDatabaseShardingStrategy(new StandardShardingStrategyConfiguration("column", "CORE.STANDARD.FIXTURE"));
        shardingRuleConfig.getShardingAlgorithms().put("core_standard_fixture", new AlgorithmConfiguration("CORE.STANDARD.FIXTURE", new Properties()));
        Optional<String> actual = new ShardingRule(shardingRuleConfig, createDataSourceNames(), mock(InstanceContext.class)).findShardingColumn("column", "LOGIC_TABLE");
        assertTrue(actual.isPresent());
        assertThat(actual.get(), is("column"));
    }
    
    @Test
    void assertFindShardingColumnForDefaultTableShardingStrategy() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.getTables().add(createTableRuleConfigWithAllStrategies());
        shardingRuleConfig.setDefaultTableShardingStrategy(new StandardShardingStrategyConfiguration("column", "core_standard_fixture"));
        shardingRuleConfig.getShardingAlgorithms().put("core_standard_fixture", new AlgorithmConfiguration("CORE.STANDARD.FIXTURE", new Properties()));
        Optional<String> actual = new ShardingRule(shardingRuleConfig, createDataSourceNames(), mock(InstanceContext.class)).findShardingColumn("column", "LOGIC_TABLE");
        assertTrue(actual.isPresent());
        assertThat(actual.get(), is("column"));
    }
    
    @Test
    void assertFindShardingColumnForDatabaseShardingStrategy() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.getTables().add(createTableRuleConfigWithAllStrategies());
        shardingRuleConfig.getShardingAlgorithms().put("core_standard_fixture", new AlgorithmConfiguration("CORE.STANDARD.FIXTURE", new Properties()));
        Optional<String> actual = new ShardingRule(shardingRuleConfig, createDataSourceNames(), mock(InstanceContext.class)).findShardingColumn("column", "logic_Table");
        assertTrue(actual.isPresent());
        assertThat(actual.get(), is("column"));
    }
    
    @Test
    void assertFindShardingColumnForTableShardingStrategy() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.getTables().add(createTableRuleConfigWithTableStrategies());
        shardingRuleConfig.getShardingAlgorithms().put("core_standard_fixture", new AlgorithmConfiguration("CORE.STANDARD.FIXTURE", new Properties()));
        Optional<String> actual = new ShardingRule(shardingRuleConfig, createDataSourceNames(), mock(InstanceContext.class)).findShardingColumn("column", "logic_Table");
        assertTrue(actual.isPresent());
        assertThat(actual.get(), is("column"));
    }
    
    @Test
    void assertIsNotShardingColumn() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.getTables().add(createTableRuleConfigWithAllStrategies());
        Optional<String> actual = new ShardingRule(shardingRuleConfig, createDataSourceNames(), mock(InstanceContext.class)).findShardingColumn("column", "other_Table");
        assertFalse(actual.isPresent());
    }
    
    @Test
    void assertFindGenerateKeyColumn() {
        assertTrue(createMaximumShardingRule().findGenerateKeyColumnName("logic_table").isPresent());
    }
    
    @Test
    void assertNotFindGenerateKeyColumn() {
        assertFalse(createMinimumShardingRule().findGenerateKeyColumnName("sub_logic_table").isPresent());
    }
    
    @Test
    void assertGenerateKeyFailure() {
        assertThrows(GenerateKeyStrategyNotFoundException.class, () -> createMaximumShardingRule().generateKey("table_0"));
    }
    
    @Test
    void assertCreateInconsistentActualDataSourceNamesFailure() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        ShardingTableRuleConfiguration shardingTableRuleConfig = createTableRuleConfiguration("LOGIC_TABLE", "ds_${0..2}.table_${0..2}");
        shardingTableRuleConfig.setKeyGenerateStrategy(new KeyGenerateStrategyConfiguration("id", "uuid"));
        ShardingTableRuleConfiguration subTableRuleConfig = createTableRuleConfiguration("SUB_LOGIC_TABLE", "ds_${0..1}.sub_table_${0..2}");
        shardingRuleConfig.getTables().add(shardingTableRuleConfig);
        shardingRuleConfig.getTables().add(subTableRuleConfig);
        shardingRuleConfig.getBindingTableGroups().add(new ShardingTableReferenceRuleConfiguration("foo", shardingTableRuleConfig.getLogicTable() + "," + subTableRuleConfig.getLogicTable()));
        assertThrows(InvalidBindingTablesException.class, () -> new ShardingRule(shardingRuleConfig, createDataSourceNames(), mock(InstanceContext.class)));
    }
    
    @Test
    void assertCreateInconsistentActualTableNamesFailure() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        ShardingTableRuleConfiguration shardingTableRuleConfig = createTableRuleConfiguration("LOGIC_TABLE", "ds_${0..1}.table_${0..3}");
        shardingTableRuleConfig.setKeyGenerateStrategy(new KeyGenerateStrategyConfiguration("id", "uuid"));
        ShardingTableRuleConfiguration subTableRuleConfig = createTableRuleConfiguration("SUB_LOGIC_TABLE", "ds_${0..1}.sub_table_${0..2}");
        shardingRuleConfig.getTables().add(shardingTableRuleConfig);
        shardingRuleConfig.getTables().add(subTableRuleConfig);
        shardingRuleConfig.getBindingTableGroups().add(new ShardingTableReferenceRuleConfiguration("foo", shardingTableRuleConfig.getLogicTable() + "," + subTableRuleConfig.getLogicTable()));
        assertThrows(InvalidBindingTablesException.class, () -> new ShardingRule(shardingRuleConfig, createDataSourceNames(), mock(InstanceContext.class)));
    }
    
    @Test
    void assertCreateInconsistentAlgorithmExpressionOnDatabaseShardingStrategyFailure() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        ShardingTableRuleConfiguration shardingTableRuleConfig = createTableRuleConfiguration("LOGIC_TABLE", "ds_${0..1}.table_${0..2}");
        ShardingTableRuleConfiguration subTableRuleConfig = createTableRuleConfiguration("SUB_LOGIC_TABLE", "ds_${0..1}.sub_table_${0..2}");
        shardingRuleConfig.getTables().add(shardingTableRuleConfig);
        shardingRuleConfig.getTables().add(subTableRuleConfig);
        shardingRuleConfig.getBindingTableGroups().add(new ShardingTableReferenceRuleConfiguration("foo", shardingTableRuleConfig.getLogicTable() + "," + subTableRuleConfig.getLogicTable()));
        shardingTableRuleConfig.setDatabaseShardingStrategy(new StandardShardingStrategyConfiguration("ds_id", "shardingAlgorithmDB"));
        shardingRuleConfig.getShardingAlgorithms().put("shardingAlgorithmDB", new AlgorithmConfiguration("INLINE", PropertiesBuilder.build(new Property("algorithm-expression", "ds_%{ds_id % 2}"))));
        subTableRuleConfig.setDatabaseShardingStrategy(new StandardShardingStrategyConfiguration("ds_id", "subAlgorithmDB"));
        shardingRuleConfig.getShardingAlgorithms().put("subAlgorithmDB", new AlgorithmConfiguration("INLINE", PropertiesBuilder.build(new Property("algorithm-expression", "ds_%{ds_id % 3}"))));
        assertThrows(InvalidBindingTablesException.class, () -> new ShardingRule(shardingRuleConfig, createDataSourceNames(), mock(InstanceContext.class)));
    }
    
    @Test
    void assertCreateInconsistentAlgorithmExpressionOnTableShardingStrategyFailure() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        ShardingTableRuleConfiguration shardingTableRuleConfig = createTableRuleConfiguration("LOGIC_TABLE", "ds_${0..1}.table_${0..2}");
        ShardingTableRuleConfiguration subTableRuleConfig = createTableRuleConfiguration("SUB_LOGIC_TABLE", "ds_${0..1}.sub_table_${0..2}");
        shardingRuleConfig.getTables().add(shardingTableRuleConfig);
        shardingRuleConfig.getTables().add(subTableRuleConfig);
        shardingRuleConfig.getBindingTableGroups().add(new ShardingTableReferenceRuleConfiguration("foo", shardingTableRuleConfig.getLogicTable() + "," + subTableRuleConfig.getLogicTable()));
        shardingTableRuleConfig.setTableShardingStrategy(new StandardShardingStrategyConfiguration("table_id", "shardingAlgorithmTBL"));
        shardingRuleConfig.getShardingAlgorithms().put(
                "shardingAlgorithmTBL", new AlgorithmConfiguration("INLINE", PropertiesBuilder.build(new Property("algorithm-expression", "table_%{table_id % 2}"))));
        subTableRuleConfig.setTableShardingStrategy(new StandardShardingStrategyConfiguration("table_id", "subAlgorithmTBL"));
        shardingRuleConfig.getShardingAlgorithms().put(
                "subAlgorithmTBL", new AlgorithmConfiguration("INLINE", PropertiesBuilder.build(new Property("algorithm-expression", "table_%{table_id % 3}"))));
        assertThrows(InvalidBindingTablesException.class, () -> new ShardingRule(shardingRuleConfig, createDataSourceNames(), mock(InstanceContext.class)));
    }
    
    @Test
    void assertCreateInconsistentAlgorithmExpressionWithDefaultAndSpecifiedTableShardingStrategyFailure() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        ShardingTableRuleConfiguration shardingTableRuleConfig = createTableRuleConfiguration("LOGIC_TABLE", "ds_${0..1}.table_${0..2}");
        ShardingTableRuleConfiguration subTableRuleConfig = createTableRuleConfiguration("SUB_LOGIC_TABLE", "ds_${0..1}.sub_table_${0..2}");
        shardingRuleConfig.getTables().add(shardingTableRuleConfig);
        shardingRuleConfig.getTables().add(subTableRuleConfig);
        shardingRuleConfig.getBindingTableGroups().add(new ShardingTableReferenceRuleConfiguration("foo", shardingTableRuleConfig.getLogicTable() + "," + subTableRuleConfig.getLogicTable()));
        shardingTableRuleConfig.setTableShardingStrategy(new StandardShardingStrategyConfiguration("table_id", "shardingAlgorithmTBL"));
        shardingRuleConfig.getShardingAlgorithms().put(
                "shardingAlgorithmTBL", new AlgorithmConfiguration("INLINE", PropertiesBuilder.build(new Property("algorithm-expression", "table_%{table_id % 2}"))));
        shardingRuleConfig.setDefaultTableShardingStrategy(new StandardShardingStrategyConfiguration("table_id", "table_inline"));
        shardingRuleConfig.setDefaultShardingColumn("table_id");
        shardingRuleConfig.getShardingAlgorithms().put("table_inline", new AlgorithmConfiguration("INLINE", PropertiesBuilder.build(new Property("algorithm-expression", "table_%{table_id % 3}"))));
        assertThrows(InvalidBindingTablesException.class, () -> new ShardingRule(shardingRuleConfig, createDataSourceNames(), mock(InstanceContext.class)));
    }
    
    @Test
    void assertGenerateKeyWithDefaultKeyGenerator() {
        assertThat(createMinimumShardingRule().generateKey("logic_table"), instanceOf(Long.class));
    }
    
    @Test
    void assertGenerateKeyWithKeyGenerator() {
        assertThat(createMaximumShardingRule().generateKey("logic_table"), instanceOf(String.class));
    }
    
    @Test
    void assertGetDataNodeByLogicTable() {
        assertThat(createMaximumShardingRule().getDataNode("logic_table"), is(new DataNode("ds_0.table_0")));
    }
    
    @Test
    void assertGetShardingLogicTableNames() {
        ShardingRule actual = createMaximumShardingRule();
        assertThat(actual.getShardingLogicTableNames(Arrays.asList("LOGIC_TABLE", "BROADCAST_TABLE")), is(Collections.singletonList("LOGIC_TABLE")));
    }
    
    @Test
    void assertContainsShardingTable() {
        assertTrue(createMaximumShardingRule().containsShardingTable(Collections.singleton("logic_table")));
    }
    
    @Test
    void assertContainsShardingTableForMultipleTables() {
        assertTrue(createMaximumShardingRule().containsShardingTable(Arrays.asList("logic_table", "table_0")));
    }
    
    @Test
    void assertNotContainsShardingTable() {
        assertFalse(createMinimumShardingRule().containsShardingTable(Collections.singleton("table_0")));
    }
    
    @Test
    void assertGetTables() {
        assertThat(new LinkedList<>(createMaximumShardingRule().getLogicTableMapper().getTableNames()), is(Arrays.asList("LOGIC_TABLE", "SUB_LOGIC_TABLE")));
    }
    
    @Test
    void assertGetDataSourceNamesWithShardingAutoTables() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        ShardingTableRuleConfiguration tableRuleConfig = new ShardingTableRuleConfiguration("logic_table", "ds_${0..1}.table_${0..2}");
        shardingRuleConfig.getTables().add(tableRuleConfig);
        ShardingAutoTableRuleConfiguration autoTableRuleConfig = new ShardingAutoTableRuleConfiguration("auto_table", "resource0, resource1");
        autoTableRuleConfig.setShardingStrategy(new StandardShardingStrategyConfiguration("order_id", "hash_mod"));
        shardingRuleConfig.getAutoTables().add(autoTableRuleConfig);
        shardingRuleConfig.getShardingAlgorithms().put("hash_mod", new AlgorithmConfiguration("hash_mod", PropertiesBuilder.build(new Property("sharding-count", "4"))));
        ShardingRule shardingRule = new ShardingRule(shardingRuleConfig, createDataSourceNames(), mock(InstanceContext.class));
        assertThat(shardingRule.getDataSourceNames(), is(new LinkedHashSet<>(Arrays.asList("ds_0", "ds_1", "resource0", "resource1"))));
    }
    
    @Test
    void assertGetDataSourceNamesWithoutShardingAutoTables() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        ShardingTableRuleConfiguration shardingTableRuleConfig = new ShardingTableRuleConfiguration("LOGIC_TABLE", "ds_${0..1}.table_${0..2}");
        shardingRuleConfig.getTables().add(shardingTableRuleConfig);
        ShardingRule shardingRule = new ShardingRule(shardingRuleConfig, createDataSourceNames(), mock(InstanceContext.class));
        assertThat(shardingRule.getDataSourceNames(), is(new LinkedHashSet<>(Arrays.asList("ds_0", "ds_1"))));
    }
    
    @Test
    void assertGetDataSourceNamesWithShardingAutoTablesAndInlineExpression() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        ShardingAutoTableRuleConfiguration autoTableRuleConfig = new ShardingAutoTableRuleConfiguration("auto_table", "resource${0..1}");
        autoTableRuleConfig.setShardingStrategy(new StandardShardingStrategyConfiguration("order_id", "hash_mod"));
        shardingRuleConfig.getAutoTables().add(autoTableRuleConfig);
        shardingRuleConfig.getShardingAlgorithms().put("hash_mod", new AlgorithmConfiguration("hash_mod", PropertiesBuilder.build(new Property("sharding-count", "4"))));
        ShardingRule shardingRule = new ShardingRule(shardingRuleConfig, createDataSourceNames(), mock(InstanceContext.class));
        assertThat(shardingRule.getDataSourceNames(), is(new LinkedHashSet<>(Arrays.asList("resource0", "resource1"))));
    }
    
    @Test
    void assertGetDataSourceNamesWithoutShardingTablesAndShardingAutoTables() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        ShardingRule shardingRule = new ShardingRule(shardingRuleConfig, createDataSourceNames(), mock(InstanceContext.class));
        assertThat(shardingRule.getDataSourceNames(), is(Arrays.asList("ds_0", "ds_1", "resource0", "resource1")));
    }
    
    private ShardingRule createMaximumShardingRule() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        ShardingTableRuleConfiguration shardingTableRuleConfig = createTableRuleConfiguration("LOGIC_TABLE", "ds_${0..1}.table_${0..2}");
        shardingTableRuleConfig.setKeyGenerateStrategy(new KeyGenerateStrategyConfiguration("id", "uuid"));
        ShardingTableRuleConfiguration subTableRuleConfig = createTableRuleConfiguration("SUB_LOGIC_TABLE", "ds_${0..1}.sub_table_${0..2}");
        subTableRuleConfig.setKeyGenerateStrategy(new KeyGenerateStrategyConfiguration("id", "auto_increment"));
        shardingRuleConfig.getTables().add(shardingTableRuleConfig);
        shardingRuleConfig.getTables().add(subTableRuleConfig);
        shardingRuleConfig.getBindingTableGroups().add(new ShardingTableReferenceRuleConfiguration("foo", shardingTableRuleConfig.getLogicTable() + "," + subTableRuleConfig.getLogicTable()));
        shardingRuleConfig.setDefaultDatabaseShardingStrategy(new StandardShardingStrategyConfiguration("ds_id", "standard"));
        shardingRuleConfig.setDefaultTableShardingStrategy(new StandardShardingStrategyConfiguration("table_id", "standard"));
        shardingRuleConfig.setDefaultShardingColumn("table_id");
        shardingRuleConfig.setDefaultKeyGenerateStrategy(new KeyGenerateStrategyConfiguration("id", "default"));
        shardingRuleConfig.setDefaultAuditStrategy(new ShardingAuditStrategyConfiguration(Collections.singletonList("audit_algorithm"), false));
        shardingRuleConfig.getShardingAlgorithms().put("core_standard_fixture", new AlgorithmConfiguration("CORE.STANDARD.FIXTURE", new Properties()));
        shardingRuleConfig.getKeyGenerators().put("uuid", new AlgorithmConfiguration("UUID", new Properties()));
        shardingRuleConfig.getKeyGenerators().put("default", new AlgorithmConfiguration("UUID", new Properties()));
        shardingRuleConfig.getKeyGenerators().put("auto_increment", new AlgorithmConfiguration("AUTO_INCREMENT.FIXTURE", new Properties()));
        shardingRuleConfig.getAuditors().put("audit_algorithm", new AlgorithmConfiguration("DML_SHARDING_CONDITIONS", new Properties()));
        return new ShardingRule(shardingRuleConfig, createDataSourceNames(), mock(InstanceContext.class));
    }
    
    private ShardingRule createMinimumShardingRule() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        ShardingTableRuleConfiguration shardingTableRuleConfig = createTableRuleConfiguration("LOGIC_TABLE", "ds_${0..1}.table_${0..2}");
        shardingRuleConfig.getTables().add(shardingTableRuleConfig);
        return new ShardingRule(shardingRuleConfig, createDataSourceNames(), mock(InstanceContext.class));
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
    void assertFindShardingColumnForComplexShardingStrategy() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.getTables().add(createTableRuleConfigWithComplexStrategies());
        Optional<String> actual = new ShardingRule(shardingRuleConfig, createDataSourceNames(), mock(InstanceContext.class)).findShardingColumn("column1", "LOGIC_TABLE");
        assertTrue(actual.isPresent());
        assertThat(actual.get(), is("COLUMN1"));
    }
    
    private ShardingTableRuleConfiguration createTableRuleConfigWithComplexStrategies() {
        ShardingTableRuleConfiguration result = new ShardingTableRuleConfiguration("LOGIC_TABLE", "ds_${0..1}.table_${0..2}");
        result.setDatabaseShardingStrategy(new ComplexShardingStrategyConfiguration("COLUMN1,COLUMN2", "CORE.COMPLEX.FIXTURE"));
        result.setTableShardingStrategy(new NoneShardingStrategyConfiguration());
        return result;
    }
    
    @Test
    void assertIsAllBindingTableWithUpdateStatementContext() {
        SQLStatementContext sqlStatementContext = mock(UpdateStatementContext.class);
        assertTrue(
                createMaximumShardingRule().isAllBindingTables(mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS), sqlStatementContext, Arrays.asList("logic_Table", "sub_Logic_Table")));
    }
    
    @Test
    void assertIsAllBindingTableWithoutJoinQuery() {
        SelectStatementContext sqlStatementContext = mock(SelectStatementContext.class);
        when(sqlStatementContext.isContainsJoinQuery()).thenReturn(false);
        assertTrue(
                createMaximumShardingRule().isAllBindingTables(mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS), sqlStatementContext, Arrays.asList("logic_Table", "sub_Logic_Table")));
    }
    
    @Test
    void assertIsAllBindingTableWithJoinQueryWithoutJoinCondition() {
        SelectStatementContext sqlStatementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.isContainsJoinQuery()).thenReturn(true);
        when(sqlStatementContext.getSqlStatement()).thenReturn(mock(MySQLSelectStatement.class));
        when(sqlStatementContext.getDatabaseType()).thenReturn(new MySQLDatabaseType());
        when(sqlStatementContext.getTablesContext().getSchemaName()).thenReturn(Optional.empty());
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getName()).thenReturn("db_schema");
        assertFalse(
                createMaximumShardingRule().isAllBindingTables(database, sqlStatementContext, Arrays.asList("logic_Table", "sub_Logic_Table")));
    }
    
    @Test
    void assertIsAllBindingTableWithJoinQueryWithDatabaseJoinCondition() {
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
        when(sqlStatementContext.getDatabaseType()).thenReturn(new MySQLDatabaseType());
        when(sqlStatementContext.getTablesContext().getSchemaName()).thenReturn(Optional.empty());
        ShardingSphereSchema schema = mock(ShardingSphereSchema.class);
        when(sqlStatementContext.getTablesContext().findTableNamesByColumnSegment(Arrays.asList(leftDatabaseJoin, rightDatabaseJoin), schema)).thenReturn(createColumnTableNameMap());
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getName()).thenReturn(DefaultDatabase.LOGIC_NAME);
        when(database.getSchema(DefaultDatabase.LOGIC_NAME)).thenReturn(schema);
        assertFalse(createMaximumShardingRule().isAllBindingTables(database, sqlStatementContext, Arrays.asList("logic_Table", "sub_Logic_Table")));
    }
    
    @Test
    void assertIsAllBindingTableWithJoinQueryWithDatabaseJoinConditionInUpperCaseAndNoOwner() {
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
                new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("SUB_LOGIC_TABLE"))));
        TablesContext tablesContext = new TablesContext(tableSegments, Collections.emptyMap(), DatabaseTypeFactory.get("MySQL"));
        when(sqlStatementContext.getTablesContext()).thenReturn(tablesContext);
        when(sqlStatementContext.getDatabaseType()).thenReturn(new MySQLDatabaseType());
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getName()).thenReturn(DefaultDatabase.LOGIC_NAME);
        when(database.getSchema(DefaultDatabase.LOGIC_NAME).getAllColumnNames("LOGIC_TABLE")).thenReturn(Arrays.asList("user_id", "order_id"));
        when(database.getSchema(DefaultDatabase.LOGIC_NAME).getAllColumnNames("SUB_LOGIC_TABLE")).thenReturn(Arrays.asList("uid", "order_id"));
        assertFalse(createMaximumShardingRule().isAllBindingTables(database, sqlStatementContext, Arrays.asList("LOGIC_TABLE", "SUB_LOGIC_TABLE")));
    }
    
    @Test
    void assertIsAllBindingTableWithJoinQueryWithDatabaseTableJoinCondition() {
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
        when(sqlStatementContext.getDatabaseType()).thenReturn(new MySQLDatabaseType());
        when(sqlStatementContext.getTablesContext().getSchemaName()).thenReturn(Optional.empty());
        when(sqlStatementContext.getWhereSegments()).thenReturn(Collections.singletonList(new WhereSegment(0, 0, condition)));
        ShardingSphereSchema schema = mock(ShardingSphereSchema.class);
        when(sqlStatementContext.getTablesContext().findTableNamesByColumnSegment(Arrays.asList(leftDatabaseJoin, rightDatabaseJoin), schema)).thenReturn(createColumnTableNameMap());
        when(sqlStatementContext.getTablesContext().findTableNamesByColumnSegment(Arrays.asList(leftTableJoin, rightTableJoin), schema)).thenReturn(createColumnTableNameMap());
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getName()).thenReturn(DefaultDatabase.LOGIC_NAME);
        when(database.getSchema(DefaultDatabase.LOGIC_NAME)).thenReturn(schema);
        assertTrue(createMaximumShardingRule().isAllBindingTables(database, sqlStatementContext, Arrays.asList("logic_Table", "sub_Logic_Table")));
    }
    
    @Test
    void assertIsAllTablesInSameDataSource() {
        Collection<String> logicTableNames = new LinkedHashSet<>();
        logicTableNames.add("logic_Table");
        ShardingRuleConfiguration ruleConfig = new ShardingRuleConfiguration();
        Collection<String> dataSourceNames = new LinkedHashSet<>();
        dataSourceNames.add("resource0");
        ruleConfig.getTables().add(new ShardingTableRuleConfiguration("LOGIC_TABLE", "ds_${0}.table_${0..2}"));
        ShardingRule shardingRule = new ShardingRule(ruleConfig, dataSourceNames, mock(InstanceContext.class));
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
    void assertGetDataNodesByTableName() {
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
    
    @Test
    void assertGetDatabaseShardingStrategyConfiguration() {
        ShardingRule actual = createMaximumShardingRule();
        TableRule logicTable = actual.getTableRule("Logic_Table");
        ShardingStrategyConfiguration databaseShardingStrategyConfig = actual.getDatabaseShardingStrategyConfiguration(logicTable);
        assertThat(databaseShardingStrategyConfig.getShardingAlgorithmName(), is("database_inline"));
    }
    
    @Test
    void assertGetTableShardingStrategyConfiguration() {
        ShardingRule actual = createMaximumShardingRule();
        TableRule logicTable = actual.getTableRule("Logic_Table");
        ShardingStrategyConfiguration tableShardingStrategyConfig = actual.getTableShardingStrategyConfiguration(logicTable);
        assertThat(tableShardingStrategyConfig.getShardingAlgorithmName(), is("table_inline"));
    }
    
    @Test
    void assertIsGenerateKeyColumn() {
        ShardingRule actual = createMaximumShardingRule();
        assertTrue(actual.isGenerateKeyColumn("id", "logic_table"));
    }
    
    @Test
    void assertGetShardingRuleTableNames() {
        ShardingRule actual = createMaximumShardingRule();
        Collection<String> shardingRuleTableNames = actual.getShardingRuleTableNames(Collections.singleton("Logic_Table"));
        assertTrue(shardingRuleTableNames.contains("Logic_Table"));
    }
    
    @Test
    void assertGetLogicAndActualTablesFromBindingTable() {
        ShardingRule actual = createMaximumShardingRule();
        Map<String, String> logicAndActualTablesFromBindingTable = actual.getLogicAndActualTablesFromBindingTable("ds_0", "LOGIC_TABLE", "table_0", Arrays.asList("logic_table", "sub_logic_table"));
        assertThat(logicAndActualTablesFromBindingTable.get("sub_logic_table"), is("sub_table_0"));
    }
    
    @Test
    void assertGetAllDataNodes() {
        ShardingRule actual = createMaximumShardingRule();
        Map<String, Collection<DataNode>> allDataNodes = actual.getAllDataNodes();
        assertTrue(allDataNodes.containsKey("logic_table"));
        assertTrue(allDataNodes.containsKey("sub_logic_table"));
        Collection<DataNode> logicTableDataNodes = allDataNodes.get("logic_table");
        assertGetDataNodes(logicTableDataNodes, "table_");
        Collection<DataNode> subLogicTableDataNodes = allDataNodes.get("sub_logic_table");
        assertGetDataNodes(subLogicTableDataNodes, "sub_table_");
    }
    
    private void assertGetDataNodes(final Collection<DataNode> dataNodes, final String tableNamePrefix) {
        int dataSourceNameSuffix = 0;
        int tableNameSuffix = 0;
        for (final DataNode each : dataNodes) {
            assertThat(each.getDataSourceName(), is("ds_" + dataSourceNameSuffix));
            assertThat(each.getTableName(), is(tableNamePrefix + tableNameSuffix));
            if (++tableNameSuffix == (dataNodes.size() / 2)) {
                tableNameSuffix = 0;
                dataSourceNameSuffix++;
            }
        }
    }
    
    @Test
    void assertFindFirstActualTable() {
        ShardingRule actual = createMaximumShardingRule();
        Optional<String> logicTable = actual.findFirstActualTable("logic_table");
        assertThat(logicTable.orElse(""), is("table_0"));
    }
    
    @Test
    void assertFindActualTableByCatalog() {
        ShardingRule actual = createMaximumShardingRule();
        Optional<String> actualTableByCatalog = actual.findActualTableByCatalog("ds_0", "logic_table");
        assertThat(actualTableByCatalog.orElse(""), is("table_0"));
    }
    
    @Test
    void assertIsSupportAutoIncrement() {
        assertFalse(createMaximumShardingRule().isSupportAutoIncrement("logic_table"));
        assertTrue(createMaximumShardingRule().isSupportAutoIncrement("sub_logic_table"));
    }
}
