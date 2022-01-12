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

import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.UpdateStatementContext;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.DefaultSchema;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.sharding.algorithm.sharding.inline.InlineShardingAlgorithm;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class ShardingUpdateStatementValidatorTest {
    
    @Mock
    private ShardingRule shardingRule;
    
    @Test
    public void assertPreValidateWhenUpdateSingleTable() {
        UpdateStatement updateStatement = createUpdateStatement();
        updateStatement.setTableSegment(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("user"))));
        SQLStatementContext<UpdateStatement> sqlStatementContext = new UpdateStatementContext(updateStatement, DefaultSchema.LOGIC_NAME);
        Collection<String> tableNames = sqlStatementContext.getTablesContext().getTableNames();
        when(shardingRule.isAllShardingTables(tableNames)).thenReturn(true);
        when(shardingRule.tableRuleExists(tableNames)).thenReturn(true);
        new ShardingUpdateStatementValidator().preValidate(shardingRule, sqlStatementContext, Collections.emptyList(), mock(ShardingSphereSchema.class));
    }
    
    @Test(expected = ShardingSphereException.class)
    public void assertPreValidateWhenUpdateMultipleTables() {
        UpdateStatement updateStatement = createUpdateStatement();
        JoinTableSegment joinTableSegment = new JoinTableSegment();
        joinTableSegment.setLeft(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("user"))));
        joinTableSegment.setRight(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("order"))));
        updateStatement.setTableSegment(joinTableSegment);
        SQLStatementContext<UpdateStatement> sqlStatementContext = new UpdateStatementContext(updateStatement, DefaultSchema.LOGIC_NAME);
        Collection<String> tableNames = sqlStatementContext.getTablesContext().getTableNames();
        when(shardingRule.isAllShardingTables(tableNames)).thenReturn(false);
        when(shardingRule.tableRuleExists(tableNames)).thenReturn(true);
        new ShardingUpdateStatementValidator().preValidate(shardingRule, sqlStatementContext, Collections.emptyList(), mock(ShardingSphereSchema.class));
    }
    
    @Test
    public void assertPostValidateWhenNotUpdateShardingColumn() {
        UpdateStatementContext sqlStatementContext = new UpdateStatementContext(createUpdateStatement(), DefaultSchema.LOGIC_NAME);
        new ShardingUpdateStatementValidator().postValidate(shardingRule, sqlStatementContext, Collections.emptyList(), 
                mock(ShardingSphereSchema.class), mock(ConfigurationProperties.class), mock(RouteContext.class));
    }
    
    @Test
    public void assertPostValidateWhenUpdateShardingColumnWithSameRouteContext() {
        mockShardingRuleForUpdateShardingColumn();
        new ShardingUpdateStatementValidator().postValidate(shardingRule, new UpdateStatementContext(createUpdateStatement(), DefaultSchema.LOGIC_NAME),
                Collections.emptyList(), mock(ShardingSphereSchema.class), mock(ConfigurationProperties.class), createSingleRouteContext());
    }
    
    @Test(expected = ShardingSphereException.class)
    public void assertPostValidateWhenUpdateShardingColumnWithDifferentRouteContext() {
        mockShardingRuleForUpdateShardingColumn();
        new ShardingUpdateStatementValidator().postValidate(shardingRule, new UpdateStatementContext(createUpdateStatement(), DefaultSchema.LOGIC_NAME), 
                Collections.emptyList(), mock(ShardingSphereSchema.class), mock(ConfigurationProperties.class), createFullRouteContext());
    }
    
    private void mockShardingRuleForUpdateShardingColumn() {
        TableRule tableRule = mock(TableRule.class);
        when(tableRule.getActualDatasourceNames()).thenReturn(Arrays.asList("ds_0", "ds_1"));
        when(tableRule.getActualTableNames("ds_1")).thenReturn(Collections.singletonList("user"));
        when(shardingRule.findShardingColumn("id", "user")).thenReturn(Optional.of("id"));
        when(shardingRule.getTableRule("user")).thenReturn(tableRule);
        StandardShardingStrategyConfiguration databaseStrategyConfiguration = mock(StandardShardingStrategyConfiguration.class);
        when(databaseStrategyConfiguration.getShardingColumn()).thenReturn("id");
        when(databaseStrategyConfiguration.getShardingAlgorithmName()).thenReturn("database_inline");
        when(shardingRule.getDatabaseShardingStrategyConfiguration(tableRule)).thenReturn(databaseStrategyConfiguration);
        when(shardingRule.getShardingAlgorithms()).thenReturn(createShardingAlgorithmMap());
    }
    
    private Map<String, ShardingAlgorithm> createShardingAlgorithmMap() {
        ShardingAlgorithm shardingAlgorithm = new InlineShardingAlgorithm();
        Properties props = new Properties();
        props.put("algorithm-expression", "ds_${id % 2}");
        shardingAlgorithm.setProps(props);
        shardingAlgorithm.init();
        Map<String, ShardingAlgorithm> result = new HashMap<>();
        result.put("database_inline", shardingAlgorithm);
        return result;
    }
    
    private RouteContext createSingleRouteContext() {
        RouteContext result = new RouteContext();
        result.getRouteUnits().add(new RouteUnit(new RouteMapper("ds_1", "ds_1"), Collections.singletonList(new RouteMapper("user", "user"))));
        return result;
    }
    
    private RouteContext createFullRouteContext() {
        RouteContext result = new RouteContext();
        result.getRouteUnits().add(new RouteUnit(new RouteMapper("ds_0", "ds_0"), Collections.singletonList(new RouteMapper("user", "user"))));
        result.getRouteUnits().add(new RouteUnit(new RouteMapper("ds_1", "ds_1"), Collections.singletonList(new RouteMapper("user", "user"))));
        return result;
    }
    
    private UpdateStatement createUpdateStatement() {
        UpdateStatement result = new MySQLUpdateStatement();
        result.setTableSegment(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("user"))));
        List<ColumnSegment> columns = new LinkedList<>();
        columns.add(new ColumnSegment(0, 0, new IdentifierValue("id")));
        AssignmentSegment assignment = new ColumnAssignmentSegment(0, 0, columns, new LiteralExpressionSegment(0, 0, 1));
        result.setSetAssignment(
                new SetAssignmentSegment(0, 0, Collections.singletonList(assignment)));
        return result;
    }
}
