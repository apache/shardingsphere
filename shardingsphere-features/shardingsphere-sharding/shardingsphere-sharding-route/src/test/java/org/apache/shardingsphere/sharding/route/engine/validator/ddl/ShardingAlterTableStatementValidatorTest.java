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
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.sharding.route.engine.validator.ddl.impl.ShardingAlterTableStatementValidator;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
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
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class ShardingAlterTableStatementValidatorTest {
    
    @Mock
    private ShardingRule shardingRule;
    
    @Mock
    private ShardingSphereSchema schema;
    
    @Mock
    private RouteContext routeContext;
    
    @Test
    public void assertPreValidateAlterTableWithSameDatasourceSingleTablesForPostgreSQL() {
        PostgreSQLAlterTableStatement sqlStatement = new PostgreSQLAlterTableStatement();
        sqlStatement.setTable(new SimpleTableSegment(0, 0, new IdentifierValue("t_order")));
        ConstraintDefinitionSegment definitionSegment = new ConstraintDefinitionSegment(0, 0);
        definitionSegment.setReferencedTable(new SimpleTableSegment(0, 0, new IdentifierValue("t_order_item")));
        sqlStatement.getAddConstraintDefinitions().add(definitionSegment);
        SQLStatementContext<AlterTableStatement> sqlStatementContext = new AlterTableStatementContext(sqlStatement);
        when(shardingRule.tableRuleExists(Arrays.asList("t_order", "t_order_item"))).thenReturn(false);
        when(shardingRule.isSingleTablesInSameDataSource(Arrays.asList("t_order", "t_order_item"))).thenReturn(true);
        new ShardingAlterTableStatementValidator().preValidate(shardingRule, sqlStatementContext, Collections.emptyList(), schema);
    }
    
    @Test(expected = ShardingSphereException.class)
    public void assertPreValidateAlterTableWithDifferentDatasourceSingleTablesForPostgreSQL() {
        PostgreSQLAlterTableStatement sqlStatement = new PostgreSQLAlterTableStatement();
        sqlStatement.setTable(new SimpleTableSegment(0, 0, new IdentifierValue("t_order")));
        ConstraintDefinitionSegment definitionSegment = new ConstraintDefinitionSegment(0, 0);
        definitionSegment.setReferencedTable(new SimpleTableSegment(0, 0, new IdentifierValue("t_order_item")));
        sqlStatement.getAddConstraintDefinitions().add(definitionSegment);
        SQLStatementContext<AlterTableStatement> sqlStatementContext = new AlterTableStatementContext(sqlStatement);
        when(shardingRule.tableRuleExists(Arrays.asList("t_order", "t_order_item"))).thenReturn(false);
        when(shardingRule.isSingleTablesInSameDataSource(Arrays.asList("t_order", "t_order_item"))).thenReturn(false);
        new ShardingAlterTableStatementValidator().preValidate(shardingRule, sqlStatementContext, Collections.emptyList(), schema);
    }
    
    @Test(expected = ShardingSphereException.class)
    public void assertPostValidateAlterTableWithEmptyRouteResultForPostgreSQL() {
        PostgreSQLAlterTableStatement sqlStatement = new PostgreSQLAlterTableStatement();
        sqlStatement.setTable(new SimpleTableSegment(0, 0, new IdentifierValue("t_order")));
        when(routeContext.getRouteUnits()).thenReturn(Collections.emptyList());
        new ShardingAlterTableStatementValidator().postValidate(sqlStatement, routeContext);
    }
    
    @Test
    public void assertPostValidateAlterTableWithSingleDataNodeForPostgreSQL() {
        PostgreSQLAlterTableStatement sqlStatement = new PostgreSQLAlterTableStatement();
        sqlStatement.setTable(new SimpleTableSegment(0, 0, new IdentifierValue("t_order")));
        when(routeContext.getActualDataSourceNames()).thenReturn(Collections.singletonList("ds_0"));
        Collection<RouteUnit> routeUnits = new LinkedList<>();
        routeUnits.add(new RouteUnit(new RouteMapper("ds_0", "ds_0"),
                Arrays.asList(new RouteMapper("t_order", "t_order_0"), new RouteMapper("t_order_item", "t_order_item_0"))));
        when(routeContext.getRouteUnits()).thenReturn(routeUnits);
        new ShardingAlterTableStatementValidator().postValidate(sqlStatement, routeContext);
    }
    
    @Test(expected = ShardingSphereException.class)
    public void assertPostValidateAlterTableWithMultiDataNodeForPostgreSQL() {
        PostgreSQLAlterTableStatement sqlStatement = new PostgreSQLAlterTableStatement();
        sqlStatement.setTable(new SimpleTableSegment(0, 0, new IdentifierValue("t_order")));
        when(routeContext.getActualDataSourceNames()).thenReturn(Collections.singletonList("ds_0"));
        Collection<RouteUnit> routeUnits = new LinkedList<>();
        routeUnits.add(new RouteUnit(new RouteMapper("ds_0", "ds_0"),
                Arrays.asList(new RouteMapper("t_order", "t_order_0"), new RouteMapper("t_order_item", "t_order_item_0"))));
        routeUnits.add(new RouteUnit(new RouteMapper("ds_0", "ds_0"),
                Arrays.asList(new RouteMapper("t_order", "t_order_0"), new RouteMapper("t_order_item", "t_order_item_1"))));
        routeUnits.add(new RouteUnit(new RouteMapper("ds_0", "ds_0"),
                Arrays.asList(new RouteMapper("t_order", "t_order_1"), new RouteMapper("t_order_item", "t_order_item_0"))));
        routeUnits.add(new RouteUnit(new RouteMapper("ds_0", "ds_0"),
                Arrays.asList(new RouteMapper("t_order", "t_order_1"), new RouteMapper("t_order_item", "t_order_item_1"))));
        when(routeContext.getRouteUnits()).thenReturn(routeUnits);
        new ShardingAlterTableStatementValidator().postValidate(sqlStatement, routeContext);
    }
}
