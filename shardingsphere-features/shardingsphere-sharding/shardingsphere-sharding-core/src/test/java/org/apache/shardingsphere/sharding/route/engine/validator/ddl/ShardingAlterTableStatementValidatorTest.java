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
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.sharding.exception.connection.ShardingDDLRouteException;
import org.apache.shardingsphere.sharding.exception.syntax.UnsupportedShardingOperationException;
import org.apache.shardingsphere.sharding.route.engine.validator.ddl.impl.ShardingAlterTableStatementValidator;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.rule.TableRule;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.AlterTableStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLAlterTableStatement;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class ShardingAlterTableStatementValidatorTest {
    
    @Mock
    private ShardingRule shardingRule;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereDatabase database;
    
    @Mock
    private RouteContext routeContext;
    
    @Test(expected = UnsupportedShardingOperationException.class)
    public void assertPreValidateAlterTableWithRenameTableWithShardingTableForPostgreSQL() {
        PostgreSQLAlterTableStatement sqlStatement = new PostgreSQLAlterTableStatement();
        sqlStatement.setTable(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order"))));
        sqlStatement.setRenameTable(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order_new"))));
        SQLStatementContext<AlterTableStatement> sqlStatementContext = new AlterTableStatementContext(sqlStatement);
        when(shardingRule.tableRuleExists(Arrays.asList("t_order", "t_order_new"))).thenReturn(true);
        new ShardingAlterTableStatementValidator().preValidate(shardingRule, sqlStatementContext, Collections.emptyList(), database, mock(ConfigurationProperties.class));
    }
    
    @Test(expected = UnsupportedShardingOperationException.class)
    public void assertPreValidateAlterTableWithRenameTableWithBroadcastTableForPostgreSQL() {
        PostgreSQLAlterTableStatement sqlStatement = new PostgreSQLAlterTableStatement();
        sqlStatement.setTable(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order"))));
        sqlStatement.setRenameTable(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order_new"))));
        SQLStatementContext<AlterTableStatement> sqlStatementContext = new AlterTableStatementContext(sqlStatement);
        when(shardingRule.tableRuleExists(Arrays.asList("t_order", "t_order_new"))).thenReturn(false);
        when(shardingRule.isBroadcastTable("t_order")).thenReturn(true);
        new ShardingAlterTableStatementValidator().preValidate(shardingRule, sqlStatementContext, Collections.emptyList(), database, mock(ConfigurationProperties.class));
    }
    
    @Test
    public void assertPostValidateAlterTableWithSameRouteResultShardingTableForPostgreSQL() {
        PostgreSQLAlterTableStatement sqlStatement = new PostgreSQLAlterTableStatement();
        sqlStatement.setTable(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order"))));
        when(shardingRule.isShardingTable("t_order")).thenReturn(true);
        when(shardingRule.getTableRule("t_order")).thenReturn(new TableRule(Arrays.asList("ds_0", "ds_1"), "t_order"));
        Collection<RouteUnit> routeUnits = new LinkedList<>();
        routeUnits.add(new RouteUnit(new RouteMapper("ds_0", "ds_0"), Collections.singletonList(new RouteMapper("t_order", "t_order_0"))));
        routeUnits.add(new RouteUnit(new RouteMapper("ds_1", "ds_1"), Collections.singletonList(new RouteMapper("t_order", "t_order_0"))));
        when(routeContext.getRouteUnits()).thenReturn(routeUnits);
        new ShardingAlterTableStatementValidator().postValidate(shardingRule, new AlterTableStatementContext(sqlStatement),
                Collections.emptyList(), database, mock(ConfigurationProperties.class), routeContext);
    }
    
    @Test(expected = ShardingDDLRouteException.class)
    public void assertPostValidateAlterTableWithDifferentRouteResultShardingTableForPostgreSQL() {
        PostgreSQLAlterTableStatement sqlStatement = new PostgreSQLAlterTableStatement();
        sqlStatement.setTable(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order"))));
        when(shardingRule.isShardingTable("t_order")).thenReturn(true);
        when(shardingRule.getTableRule("t_order")).thenReturn(new TableRule(Arrays.asList("ds_0", "ds_1"), "t_order"));
        Collection<RouteUnit> routeUnits = new LinkedList<>();
        routeUnits.add(new RouteUnit(new RouteMapper("ds_0", "ds_0"), Collections.singletonList(new RouteMapper("t_order", "t_order_0"))));
        when(routeContext.getRouteUnits()).thenReturn(routeUnits);
        new ShardingAlterTableStatementValidator().postValidate(shardingRule, new AlterTableStatementContext(sqlStatement),
                Collections.emptyList(), database, mock(ConfigurationProperties.class), routeContext);
    }
    
    @Test
    public void assertPostValidateAlterTableWithSameRouteResultBroadcastTableForPostgreSQL() {
        PostgreSQLAlterTableStatement sqlStatement = new PostgreSQLAlterTableStatement();
        sqlStatement.setTable(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_config"))));
        when(shardingRule.isBroadcastTable("t_config")).thenReturn(true);
        when(shardingRule.getTableRule("t_config")).thenReturn(new TableRule(Arrays.asList("ds_0", "ds_1"), "t_config"));
        Collection<RouteUnit> routeUnits = new LinkedList<>();
        routeUnits.add(new RouteUnit(new RouteMapper("ds_0", "ds_0"), Collections.singletonList(new RouteMapper("t_config", "t_config"))));
        routeUnits.add(new RouteUnit(new RouteMapper("ds_1", "ds_1"), Collections.singletonList(new RouteMapper("t_config", "t_config"))));
        when(routeContext.getRouteUnits()).thenReturn(routeUnits);
        new ShardingAlterTableStatementValidator().postValidate(shardingRule, new AlterTableStatementContext(sqlStatement),
                Collections.emptyList(), database, mock(ConfigurationProperties.class), routeContext);
    }
    
    @Test(expected = ShardingDDLRouteException.class)
    public void assertPostValidateAlterTableWithDifferentRouteResultBroadcastTableForPostgreSQL() {
        PostgreSQLAlterTableStatement sqlStatement = new PostgreSQLAlterTableStatement();
        sqlStatement.setTable(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_config"))));
        when(shardingRule.isBroadcastTable("t_config")).thenReturn(true);
        when(shardingRule.getTableRule("t_config")).thenReturn(new TableRule(Arrays.asList("ds_0", "ds_1"), "t_config"));
        Collection<RouteUnit> routeUnits = new LinkedList<>();
        routeUnits.add(new RouteUnit(new RouteMapper("ds_0", "ds_0"), Collections.singletonList(new RouteMapper("t_config", "t_config"))));
        when(routeContext.getRouteUnits()).thenReturn(routeUnits);
        new ShardingAlterTableStatementValidator().postValidate(shardingRule, new AlterTableStatementContext(sqlStatement),
                Collections.emptyList(), database, mock(ConfigurationProperties.class), routeContext);
    }
}
