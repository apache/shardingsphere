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

package org.apache.shardingsphere.sharding.route.engine.validator.dml;

import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.dml.UpdateStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.exception.syntax.DMLWithMultipleShardingTablesException;
import org.apache.shardingsphere.sharding.exception.syntax.UnsupportedUpdatingShardingValueException;
import org.apache.shardingsphere.sharding.route.engine.validator.dml.impl.ShardingUpdateStatementValidator;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.rule.TableRule;
import org.apache.shardingsphere.sharding.spi.ShardingAlgorithm;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.AssignmentSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.ColumnAssignmentSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.SetAssignmentSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.JoinTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.UpdateStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLUpdateStatement;
import org.apache.shardingsphere.test.util.PropertiesBuilder;
import org.apache.shardingsphere.test.util.PropertiesBuilder.Property;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShardingUpdateStatementValidatorTest {
    
    @Mock
    private ShardingRule shardingRule;
    
    @Mock
    private ShardingSphereDatabase database;
    
    @Mock
    private ShardingSphereSchema schema;
    
    @Test
    void assertPreValidateWhenUpdateSingleTable() {
        UpdateStatement updateStatement = createUpdateStatement();
        updateStatement.setTable(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("user"))));
        SQLStatementContext sqlStatementContext = new UpdateStatementContext(updateStatement);
        Collection<String> tableNames = sqlStatementContext.getTablesContext().getTableNames();
        when(shardingRule.isAllShardingTables(tableNames)).thenReturn(true);
        when(shardingRule.containsShardingTable(tableNames)).thenReturn(true);
        when(schema.containsTable("user")).thenReturn(true);
        when(database.getSchema(any())).thenReturn(schema);
        when(database.getName()).thenReturn("sharding_db");
        assertDoesNotThrow(() -> new ShardingUpdateStatementValidator().preValidate(
                shardingRule, sqlStatementContext, Collections.emptyList(), database, mock(ConfigurationProperties.class)));
    }
    
    @Test
    void assertPreValidateWhenUpdateMultipleTables() {
        UpdateStatement updateStatement = createUpdateStatement();
        JoinTableSegment joinTableSegment = new JoinTableSegment();
        joinTableSegment.setLeft(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("user"))));
        joinTableSegment.setRight(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("order"))));
        updateStatement.setTable(joinTableSegment);
        SQLStatementContext sqlStatementContext = new UpdateStatementContext(updateStatement);
        Collection<String> tableNames = sqlStatementContext.getTablesContext().getTableNames();
        when(shardingRule.isAllShardingTables(tableNames)).thenReturn(false);
        when(shardingRule.containsShardingTable(tableNames)).thenReturn(true);
        when(schema.containsTable("user")).thenReturn(true);
        when(schema.containsTable("order")).thenReturn(true);
        when(database.getSchema(any())).thenReturn(schema);
        when(database.getName()).thenReturn("sharding_db");
        assertThrows(DMLWithMultipleShardingTablesException.class, () -> new ShardingUpdateStatementValidator().preValidate(
                shardingRule, sqlStatementContext, Collections.emptyList(), database, mock(ConfigurationProperties.class)));
    }
    
    @Test
    void assertPostValidateWhenNotUpdateShardingColumn() {
        UpdateStatementContext sqlStatementContext = new UpdateStatementContext(createUpdateStatement());
        assertDoesNotThrow(() -> new ShardingUpdateStatementValidator().postValidate(shardingRule,
                sqlStatementContext, new HintValueContext(), Collections.emptyList(), database, mock(ConfigurationProperties.class), mock(RouteContext.class)));
    }
    
    @Test
    void assertPostValidateWhenUpdateShardingColumnWithSameRouteContext() {
        mockShardingRuleForUpdateShardingColumn();
        assertDoesNotThrow(() -> new ShardingUpdateStatementValidator().postValidate(shardingRule, new UpdateStatementContext(createUpdateStatement()),
                new HintValueContext(), Collections.emptyList(), database, mock(ConfigurationProperties.class), createSingleRouteContext()));
    }
    
    @Test
    void assertPostValidateWhenTableNameIsBroadcastTable() {
        mockShardingRuleForUpdateShardingColumn();
        assertDoesNotThrow(() -> new ShardingUpdateStatementValidator().postValidate(shardingRule, new UpdateStatementContext(createUpdateStatement()),
                new HintValueContext(), Collections.emptyList(), database, mock(ConfigurationProperties.class), createSingleRouteContext()));
    }
    
    @Test
    void assertPostValidateWhenUpdateShardingColumnWithDifferentRouteContext() {
        mockShardingRuleForUpdateShardingColumn();
        assertThrows(UnsupportedUpdatingShardingValueException.class,
                () -> new ShardingUpdateStatementValidator().postValidate(shardingRule, new UpdateStatementContext(createUpdateStatement()), new HintValueContext(),
                        Collections.emptyList(), database, mock(ConfigurationProperties.class), createFullRouteContext()));
    }
    
    private void mockShardingRuleForUpdateShardingColumn() {
        TableRule tableRule = mock(TableRule.class);
        when(tableRule.getActualDataSourceNames()).thenReturn(Arrays.asList("ds_0", "ds_1"));
        when(tableRule.getActualTableNames("ds_1")).thenReturn(Collections.singleton("user"));
        when(shardingRule.findShardingColumn("id", "user")).thenReturn(Optional.of("id"));
        when(shardingRule.getTableRule("user")).thenReturn(tableRule);
        StandardShardingStrategyConfiguration databaseStrategyConfig = mock(StandardShardingStrategyConfiguration.class);
        when(databaseStrategyConfig.getShardingColumn()).thenReturn("id");
        when(databaseStrategyConfig.getShardingAlgorithmName()).thenReturn("database_inline");
        when(shardingRule.getDatabaseShardingStrategyConfiguration(tableRule)).thenReturn(databaseStrategyConfig);
        when(shardingRule.getShardingAlgorithms()).thenReturn(createShardingAlgorithmMap());
    }
    
    private Map<String, ShardingAlgorithm> createShardingAlgorithmMap() {
        return Collections.singletonMap("database_inline", TypedSPILoader.getService(ShardingAlgorithm.class, "INLINE", PropertiesBuilder.build(new Property("algorithm-expression", "ds_${id % 2}"))));
    }
    
    private RouteContext createSingleRouteContext() {
        RouteContext result = new RouteContext();
        result.getRouteUnits().add(new RouteUnit(new RouteMapper("ds_1", "ds_1"), Collections.singleton(new RouteMapper("user", "user"))));
        return result;
    }
    
    private RouteContext createFullRouteContext() {
        RouteContext result = new RouteContext();
        result.getRouteUnits().add(new RouteUnit(new RouteMapper("ds_0", "ds_0"), Collections.singleton(new RouteMapper("user", "user"))));
        result.getRouteUnits().add(new RouteUnit(new RouteMapper("ds_1", "ds_1"), Collections.singleton(new RouteMapper("user", "user"))));
        return result;
    }
    
    private UpdateStatement createUpdateStatement() {
        UpdateStatement result = new MySQLUpdateStatement();
        result.setTable(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("user"))));
        List<ColumnSegment> columns = new LinkedList<>();
        columns.add(new ColumnSegment(0, 0, new IdentifierValue("id")));
        AssignmentSegment assignment = new ColumnAssignmentSegment(0, 0, columns, new LiteralExpressionSegment(0, 0, 1));
        result.setSetAssignment(new SetAssignmentSegment(0, 0, Collections.singleton(assignment)));
        return result;
    }
}
