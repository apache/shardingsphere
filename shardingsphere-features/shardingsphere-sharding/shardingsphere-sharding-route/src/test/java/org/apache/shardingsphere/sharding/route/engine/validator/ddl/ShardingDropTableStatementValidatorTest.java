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

import org.apache.shardingsphere.infra.binder.statement.CommonSQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.sharding.route.engine.validator.ddl.impl.ShardingDropTableStatementValidator;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.rule.TableRule;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DropTableStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.ddl.MySQLDropTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLDropTableStatement;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class ShardingDropTableStatementValidatorTest {
    
    @Mock
    private ShardingRule shardingRule;
    
    @Mock
    private RouteContext routeContext;
    
    @Before
    public void init() {
        Collection<TableRule> tableRules = new LinkedList<>();
        tableRules.add(generateShardingRule("t_order_item"));
        tableRules.add(generateShardingRule("t_order"));
        when(shardingRule.getTableRules()).thenReturn(tableRules);
    }
    
    @Test(expected = ShardingSphereException.class)
    public void assertValidateDropTableForMySQL() {
        MySQLDropTableStatement sqlStatement = new MySQLDropTableStatement();
        sqlStatement.getTables().add(new SimpleTableSegment(0, 0, new IdentifierValue("t_order_item")));
        SQLStatementContext<DropTableStatement> sqlStatementContext = new CommonSQLStatementContext<>(sqlStatement);
        ShardingSphereSchema schema = mock(ShardingSphereSchema.class);
        ShardingDropTableStatementValidator validator = new ShardingDropTableStatementValidator();
        validator.preValidate(shardingRule, sqlStatementContext, Collections.emptyList(), schema);
        Collection<RouteUnit> routeUnits = new LinkedList<>();
        RouteMapper dataSourceMapper = new RouteMapper("db", "db1");
        Collection<RouteMapper> tableMapper = new LinkedList<>();
        tableMapper.add(new RouteMapper("t_order_item", "t_order_item_1"));
        tableMapper.add(new RouteMapper("t_order_item", "t_order_item_2"));
        RouteUnit routeUnit = new RouteUnit(dataSourceMapper, tableMapper);
        routeUnits.add(routeUnit);
        RouteContext routeContext = mock(RouteContext.class);
        when(routeContext.getRouteUnits()).thenReturn(routeUnits);
        validator.postValidate(shardingRule, sqlStatement, routeContext);
    }
    
    private TableRule generateShardingRule(final String tableName) {
        TableRule result = mock(TableRule.class);
        when(result.getLogicTable()).thenReturn(tableName);
        List<DataNode> dataNodes = new LinkedList<>();
        DataNode d1 = mock(DataNode.class);
        when(d1.getTableName()).thenReturn("t_order_item_1");
        dataNodes.add(d1);
        DataNode d2 = mock(DataNode.class);
        when(d2.getTableName()).thenReturn("t_order_item_2");
        dataNodes.add(d2);
        when(result.getActualDataNodes()).thenReturn(dataNodes);
        return result;
    }
    
    @After
    public void clean() {
        shardingRule = mock(ShardingRule.class);
    }
    
    @Test
    public void assertPostValidateDropTableWithSameRouteResultShardingTableForPostgreSQL() {
        PostgreSQLDropTableStatement sqlStatement = new PostgreSQLDropTableStatement();
        sqlStatement.getTables().add(new SimpleTableSegment(0, 0, new IdentifierValue("t_order")));
        when(shardingRule.isShardingTable("t_order")).thenReturn(true);
        when(shardingRule.getTableRule("t_order")).thenReturn(new TableRule(Arrays.asList("ds_0", "ds_1"), "t_order"));
        Collection<RouteUnit> routeUnits = new LinkedList<>();
        routeUnits.add(new RouteUnit(new RouteMapper("ds_0", "ds_0"), Collections.singletonList(new RouteMapper("t_order", "t_order_0"))));
        routeUnits.add(new RouteUnit(new RouteMapper("ds_1", "ds_1"), Collections.singletonList(new RouteMapper("t_order", "t_order_0"))));
        when(routeContext.getRouteUnits()).thenReturn(routeUnits);
        new ShardingDropTableStatementValidator().postValidate(shardingRule, sqlStatement, routeContext);
    }
    
    @Test(expected = ShardingSphereException.class)
    public void assertPostValidateDropTableWithDifferentRouteResultShardingTableForPostgreSQL() {
        PostgreSQLDropTableStatement sqlStatement = new PostgreSQLDropTableStatement();
        sqlStatement.getTables().add(new SimpleTableSegment(0, 0, new IdentifierValue("t_order")));
        when(shardingRule.isShardingTable("t_order")).thenReturn(true);
        when(shardingRule.getTableRule("t_order")).thenReturn(new TableRule(Arrays.asList("ds_0", "ds_1"), "t_order"));
        Collection<RouteUnit> routeUnits = new LinkedList<>();
        routeUnits.add(new RouteUnit(new RouteMapper("ds_0", "ds_0"), Collections.singletonList(new RouteMapper("t_order", "t_order_0"))));
        when(routeContext.getRouteUnits()).thenReturn(routeUnits);
        new ShardingDropTableStatementValidator().postValidate(shardingRule, sqlStatement, routeContext);
    }
    
    @Test
    public void assertPostValidateDropTableWithSameRouteResultBroadcastTableForPostgreSQL() {
        PostgreSQLDropTableStatement sqlStatement = new PostgreSQLDropTableStatement();
        sqlStatement.getTables().add(new SimpleTableSegment(0, 0, new IdentifierValue("t_config")));
        when(shardingRule.isBroadcastTable("t_config")).thenReturn(true);
        when(shardingRule.getTableRule("t_config")).thenReturn(new TableRule(Arrays.asList("ds_0", "ds_1"), "t_config"));
        Collection<RouteUnit> routeUnits = new LinkedList<>();
        routeUnits.add(new RouteUnit(new RouteMapper("ds_0", "ds_0"), Collections.singletonList(new RouteMapper("t_config", "t_config"))));
        routeUnits.add(new RouteUnit(new RouteMapper("ds_1", "ds_1"), Collections.singletonList(new RouteMapper("t_config", "t_config"))));
        when(routeContext.getRouteUnits()).thenReturn(routeUnits);
        new ShardingDropTableStatementValidator().postValidate(shardingRule, sqlStatement, routeContext);
    }
    
    @Test(expected = ShardingSphereException.class)
    public void assertPostValidateDropTableWithDifferentRouteResultBroadcastTableForPostgreSQL() {
        PostgreSQLDropTableStatement sqlStatement = new PostgreSQLDropTableStatement();
        sqlStatement.getTables().add(new SimpleTableSegment(0, 0, new IdentifierValue("t_config")));
        when(shardingRule.isBroadcastTable("t_config")).thenReturn(true);
        when(shardingRule.getTableRule("t_config")).thenReturn(new TableRule(Arrays.asList("ds_0", "ds_1"), "t_config"));
        Collection<RouteUnit> routeUnits = new LinkedList<>();
        routeUnits.add(new RouteUnit(new RouteMapper("ds_0", "ds_0"), Collections.singletonList(new RouteMapper("t_config", "t_config"))));
        when(routeContext.getRouteUnits()).thenReturn(routeUnits);
        new ShardingDropTableStatementValidator().postValidate(shardingRule, sqlStatement, routeContext);
    }
    
    @Test
    public void assertPostValidateDropTableWithSameRouteResultSingleTableForPostgreSQL() {
        PostgreSQLDropTableStatement sqlStatement = new PostgreSQLDropTableStatement();
        sqlStatement.getTables().add(new SimpleTableSegment(0, 0, new IdentifierValue("t_single")));
        Collection<RouteUnit> routeUnits = new LinkedList<>();
        routeUnits.add(new RouteUnit(new RouteMapper("ds_0", "ds_0"), Collections.singletonList(new RouteMapper("t_single", "t_single"))));
        when(routeContext.getRouteUnits()).thenReturn(routeUnits);
        new ShardingDropTableStatementValidator().postValidate(shardingRule, sqlStatement, routeContext);
    }
    
    @Test(expected = ShardingSphereException.class)
    public void assertPostValidateDropTableWithDifferentRouteResultSingleTableForPostgreSQL() {
        PostgreSQLDropTableStatement sqlStatement = new PostgreSQLDropTableStatement();
        sqlStatement.getTables().add(new SimpleTableSegment(0, 0, new IdentifierValue("t_single")));
        Collection<RouteUnit> routeUnits = new LinkedList<>();
        routeUnits.add(new RouteUnit(new RouteMapper("ds_0", "ds_0"), Collections.singletonList(new RouteMapper("t_single", "t_single"))));
        routeUnits.add(new RouteUnit(new RouteMapper("ds_1", "ds_1"), Collections.singletonList(new RouteMapper("t_single", "t_single"))));
        when(routeContext.getRouteUnits()).thenReturn(routeUnits);
        new ShardingDropTableStatementValidator().postValidate(shardingRule, sqlStatement, routeContext);
    }
}
