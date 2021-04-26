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

package org.apache.shardingsphere.sharding.route.engine.validator.ddl;

import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.ddl.AlterTableStatementContext;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.sharding.route.engine.validator.ddl.impl.ShardingAlterTableStatementValidator;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.rule.TableRule;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.constraint.ConstraintDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.AlterTableStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLAlterTableStatement;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class ShardingAlterTableStatementValidatorTest {
    
    @Mock
    private ShardingRule shardingRule;
    
    @Mock
    private ShardingSphereSchema schema;
    
    @Test
    public void assertValidateAlterTableWithSingleShardingTableForPostgreSQL() {
        PostgreSQLAlterTableStatement sqlStatement = new PostgreSQLAlterTableStatement();
        sqlStatement.setTable(new SimpleTableSegment(0, 0, new IdentifierValue("t_order")));
        SQLStatementContext<AlterTableStatement> sqlStatementContext = new AlterTableStatementContext(sqlStatement);
        when(shardingRule.getShardingLogicTableNames(Collections.singletonList("t_order"))).thenReturn(Collections.singletonList("t_order"));
        new ShardingAlterTableStatementValidator().preValidate(shardingRule, sqlStatementContext, Collections.emptyList(), schema);
    }
    
    @Test
    public void assertValidateAlterTableWithAllSingleDataNodeWithSameDatasourceForPostgreSQL() {
        PostgreSQLAlterTableStatement sqlStatement = new PostgreSQLAlterTableStatement();
        sqlStatement.setTable(new SimpleTableSegment(0, 0, new IdentifierValue("t_order")));
        ConstraintDefinitionSegment constraintDefinitionSegment = new ConstraintDefinitionSegment(0, 0);
        constraintDefinitionSegment.setReferencedTable(new SimpleTableSegment(0, 0, new IdentifierValue("t_order_item")));
        sqlStatement.getAddConstraintDefinitions().add(constraintDefinitionSegment);
        when(shardingRule.getShardingLogicTableNames(Arrays.asList("t_order", "t_order_item"))).thenReturn(Arrays.asList("t_order", "t_order_item"));
        when(shardingRule.isAllSingleDataNodeTables(Arrays.asList("t_order", "t_order_item"))).thenReturn(true);
        when(shardingRule.getTableRule("t_order")).thenReturn(new TableRule(Collections.singletonList("ds_0"), "t_order"));
        when(shardingRule.getTableRule("t_order_item")).thenReturn(new TableRule(Collections.singletonList("ds_0"), "t_order_item"));
        SQLStatementContext<AlterTableStatement> sqlStatementContext = new AlterTableStatementContext(sqlStatement);
        new ShardingAlterTableStatementValidator().preValidate(shardingRule, sqlStatementContext, Collections.emptyList(), schema);
    }
    
    @Test(expected = ShardingSphereException.class)
    public void assertValidateAlterTableWithAllSingleDataNodeWithoutSameDatasourceForPostgreSQL() {
        PostgreSQLAlterTableStatement sqlStatement = new PostgreSQLAlterTableStatement();
        sqlStatement.setTable(new SimpleTableSegment(0, 0, new IdentifierValue("t_order")));
        ConstraintDefinitionSegment constraintDefinitionSegment = new ConstraintDefinitionSegment(0, 0);
        constraintDefinitionSegment.setReferencedTable(new SimpleTableSegment(0, 0, new IdentifierValue("t_order_item")));
        sqlStatement.getAddConstraintDefinitions().add(constraintDefinitionSegment);
        when(shardingRule.getShardingLogicTableNames(Arrays.asList("t_order", "t_order_item"))).thenReturn(Arrays.asList("t_order", "t_order_item"));
        when(shardingRule.isAllSingleDataNodeTables(Arrays.asList("t_order", "t_order_item"))).thenReturn(true);
        when(shardingRule.getTableRule("t_order")).thenReturn(new TableRule(Collections.singletonList("ds_0"), "t_order"));
        when(shardingRule.getTableRule("t_order_item")).thenReturn(new TableRule(Collections.singletonList("ds_1"), "t_order_item"));
        SQLStatementContext<AlterTableStatement> sqlStatementContext = new AlterTableStatementContext(sqlStatement);
        new ShardingAlterTableStatementValidator().preValidate(shardingRule, sqlStatementContext, Collections.emptyList(), schema);
    }
    
    @Test
    public void assertValidateAlterTableWithoutAllSingleDataNodeWithBindingTableForPostgreSQL() {
        PostgreSQLAlterTableStatement sqlStatement = new PostgreSQLAlterTableStatement();
        sqlStatement.setTable(new SimpleTableSegment(0, 0, new IdentifierValue("t_order")));
        ConstraintDefinitionSegment constraintDefinitionSegment = new ConstraintDefinitionSegment(0, 0);
        constraintDefinitionSegment.setReferencedTable(new SimpleTableSegment(0, 0, new IdentifierValue("t_order_item")));
        sqlStatement.getAddConstraintDefinitions().add(constraintDefinitionSegment);
        when(shardingRule.getShardingLogicTableNames(Arrays.asList("t_order", "t_order_item"))).thenReturn(Arrays.asList("t_order", "t_order_item"));
        when(shardingRule.isAllSingleDataNodeTables(Arrays.asList("t_order", "t_order_item"))).thenReturn(false);
        when(shardingRule.isAllBindingTables(Arrays.asList("t_order", "t_order_item"))).thenReturn(true);
        SQLStatementContext<AlterTableStatement> sqlStatementContext = new AlterTableStatementContext(sqlStatement);
        new ShardingAlterTableStatementValidator().preValidate(shardingRule, sqlStatementContext, Collections.emptyList(), schema);
    }
    
    @Test(expected = ShardingSphereException.class)
    public void assertValidateAlterTableWithoutAllSingleDataNodeWithoutBindingTableForPostgreSQL() {
        PostgreSQLAlterTableStatement sqlStatement = new PostgreSQLAlterTableStatement();
        sqlStatement.setTable(new SimpleTableSegment(0, 0, new IdentifierValue("t_order")));
        ConstraintDefinitionSegment constraintDefinitionSegment = new ConstraintDefinitionSegment(0, 0);
        constraintDefinitionSegment.setReferencedTable(new SimpleTableSegment(0, 0, new IdentifierValue("t_order_item")));
        sqlStatement.getAddConstraintDefinitions().add(constraintDefinitionSegment);
        when(shardingRule.getShardingLogicTableNames(Arrays.asList("t_order", "t_order_item"))).thenReturn(Arrays.asList("t_order", "t_order_item"));
        when(shardingRule.isAllSingleDataNodeTables(Arrays.asList("t_order", "t_order_item"))).thenReturn(false);
        when(shardingRule.isAllBindingTables(Arrays.asList("t_order", "t_order_item"))).thenReturn(false);
        SQLStatementContext<AlterTableStatement> sqlStatementContext = new AlterTableStatementContext(sqlStatement);
        new ShardingAlterTableStatementValidator().preValidate(shardingRule, sqlStatementContext, Collections.emptyList(), schema);
    }
}
