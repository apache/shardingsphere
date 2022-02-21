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

import org.apache.shardingsphere.infra.binder.statement.ddl.DropIndexStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.schema.model.IndexMetaData;
import org.apache.shardingsphere.infra.metadata.schema.model.TableMetaData;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.sharding.route.engine.validator.ddl.impl.ShardingDropIndexStatementValidator;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.rule.TableRule;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLDropIndexStatement;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class ShardingDropIndexStatementValidatorTest {
    
    @Mock
    private ShardingRule shardingRule;
    
    @Mock
    private ShardingSphereSchema schema;
    
    @Mock
    private RouteContext routeContext;
    
    @Test
    public void assertPreValidateDropIndexWhenIndexExistForPostgreSQL() {
        PostgreSQLDropIndexStatement sqlStatement = new PostgreSQLDropIndexStatement();
        sqlStatement.getIndexes().add(new IndexSegment(0, 0, new IdentifierValue("t_order_index")));
        sqlStatement.getIndexes().add(new IndexSegment(0, 0, new IdentifierValue("t_order_index_new")));
        TableMetaData tableMetaData = mock(TableMetaData.class);
        Map<String, IndexMetaData> indexes = mock(HashMap.class);
        when(tableMetaData.getIndexes()).thenReturn(indexes);
        when(schema.getAllTableNames()).thenReturn(Collections.singletonList("t_order"));
        when(schema.get("t_order")).thenReturn(tableMetaData);
        when(indexes.containsKey("t_order_index")).thenReturn(true);
        when(indexes.containsKey("t_order_index_new")).thenReturn(true);
        new ShardingDropIndexStatementValidator().preValidate(shardingRule, new DropIndexStatementContext(sqlStatement), Collections.emptyList(), schema);
    }
    
    @Test(expected = ShardingSphereException.class)
    public void assertPreValidateDropIndexWhenIndexNotExistForPostgreSQL() {
        PostgreSQLDropIndexStatement sqlStatement = new PostgreSQLDropIndexStatement();
        sqlStatement.getIndexes().add(new IndexSegment(0, 0, new IdentifierValue("t_order_index")));
        sqlStatement.getIndexes().add(new IndexSegment(0, 0, new IdentifierValue("t_order_index_new")));
        TableMetaData tableMetaData = mock(TableMetaData.class);
        Map<String, IndexMetaData> indexes = mock(HashMap.class);
        when(tableMetaData.getIndexes()).thenReturn(indexes);
        when(schema.getAllTableNames()).thenReturn(Collections.singletonList("t_order"));
        when(schema.get("t_order")).thenReturn(tableMetaData);
        when(indexes.containsKey("t_order_index")).thenReturn(false);
        new ShardingDropIndexStatementValidator().preValidate(shardingRule, new DropIndexStatementContext(sqlStatement), Collections.emptyList(), schema);
    }
    
    @Test
    public void assertPostValidateDropIndexWithSameRouteResultShardingTableIndexForPostgreSQL() {
        PostgreSQLDropIndexStatement sqlStatement = new PostgreSQLDropIndexStatement();
        sqlStatement.getIndexes().add(new IndexSegment(0, 0, new IdentifierValue("t_order_index")));
        sqlStatement.getIndexes().add(new IndexSegment(0, 0, new IdentifierValue("t_order_index_new")));
        TableMetaData tableMetaData = mock(TableMetaData.class);
        Map<String, IndexMetaData> indexes = mock(HashMap.class);
        when(tableMetaData.getIndexes()).thenReturn(indexes);
        when(schema.getAllTableNames()).thenReturn(Collections.singletonList("t_order"));
        when(schema.get("t_order")).thenReturn(tableMetaData);
        when(indexes.containsKey("t_order_index")).thenReturn(true);
        when(shardingRule.isShardingTable("t_order")).thenReturn(true);
        when(shardingRule.getTableRule("t_order")).thenReturn(new TableRule(Arrays.asList("ds_0", "ds_1"), "t_order"));
        Collection<RouteUnit> routeUnits = new LinkedList<>();
        routeUnits.add(new RouteUnit(new RouteMapper("ds_0", "ds_0"), Collections.singletonList(new RouteMapper("t_order", "t_order_0"))));
        routeUnits.add(new RouteUnit(new RouteMapper("ds_1", "ds_1"), Collections.singletonList(new RouteMapper("t_order", "t_order_0"))));
        when(routeContext.getRouteUnits()).thenReturn(routeUnits);
        new ShardingDropIndexStatementValidator().postValidate(shardingRule, new DropIndexStatementContext(sqlStatement), 
                Collections.emptyList(), schema, mock(ConfigurationProperties.class), routeContext);
    }
    
    @Test(expected = ShardingSphereException.class)
    public void assertPostValidateDropIndexWithDifferentRouteResultShardingTableIndexForPostgreSQL() {
        PostgreSQLDropIndexStatement sqlStatement = new PostgreSQLDropIndexStatement();
        sqlStatement.getIndexes().add(new IndexSegment(0, 0, new IdentifierValue("t_order_index")));
        sqlStatement.getIndexes().add(new IndexSegment(0, 0, new IdentifierValue("t_order_index_new")));
        TableMetaData tableMetaData = mock(TableMetaData.class);
        Map<String, IndexMetaData> indexes = mock(HashMap.class);
        when(tableMetaData.getIndexes()).thenReturn(indexes);
        when(schema.getAllTableNames()).thenReturn(Collections.singletonList("t_order"));
        when(schema.get("t_order")).thenReturn(tableMetaData);
        when(indexes.containsKey("t_order_index")).thenReturn(true);
        when(shardingRule.isShardingTable("t_order")).thenReturn(true);
        when(shardingRule.getTableRule("t_order")).thenReturn(new TableRule(Arrays.asList("ds_0", "ds_1"), "t_order"));
        Collection<RouteUnit> routeUnits = new LinkedList<>();
        routeUnits.add(new RouteUnit(new RouteMapper("ds_0", "ds_0"), Collections.singletonList(new RouteMapper("t_order", "t_order_0"))));
        when(routeContext.getRouteUnits()).thenReturn(routeUnits);
        new ShardingDropIndexStatementValidator().postValidate(shardingRule, new DropIndexStatementContext(sqlStatement), 
                Collections.emptyList(), schema, mock(ConfigurationProperties.class), routeContext);
    }
    
    @Test
    public void assertPostValidateDropIndexWithSameRouteResultBroadcastTableIndexForPostgreSQL() {
        PostgreSQLDropIndexStatement sqlStatement = new PostgreSQLDropIndexStatement();
        sqlStatement.getIndexes().add(new IndexSegment(0, 0, new IdentifierValue("t_config_index")));
        sqlStatement.getIndexes().add(new IndexSegment(0, 0, new IdentifierValue("t_config_index_new")));
        TableMetaData tableMetaData = mock(TableMetaData.class);
        Map<String, IndexMetaData> indexes = mock(HashMap.class);
        when(tableMetaData.getIndexes()).thenReturn(indexes);
        when(schema.getAllTableNames()).thenReturn(Collections.singletonList("t_config"));
        when(schema.get("t_config")).thenReturn(tableMetaData);
        when(indexes.containsKey("t_config_index")).thenReturn(true);
        when(shardingRule.isBroadcastTable("t_config")).thenReturn(true);
        when(shardingRule.getTableRule("t_config")).thenReturn(new TableRule(Arrays.asList("ds_0", "ds_1"), "t_config"));
        Collection<RouteUnit> routeUnits = new LinkedList<>();
        routeUnits.add(new RouteUnit(new RouteMapper("ds_0", "ds_0"), Collections.singletonList(new RouteMapper("t_config", "t_config"))));
        routeUnits.add(new RouteUnit(new RouteMapper("ds_1", "ds_1"), Collections.singletonList(new RouteMapper("t_config", "t_config"))));
        when(routeContext.getRouteUnits()).thenReturn(routeUnits);
        new ShardingDropIndexStatementValidator().postValidate(shardingRule, new DropIndexStatementContext(sqlStatement), 
                Collections.emptyList(), schema, mock(ConfigurationProperties.class), routeContext);
    }
    
    @Test(expected = ShardingSphereException.class)
    public void assertPostValidateDropIndexWithDifferentRouteResultBroadcastTableIndexForPostgreSQL() {
        PostgreSQLDropIndexStatement sqlStatement = new PostgreSQLDropIndexStatement();
        sqlStatement.getIndexes().add(new IndexSegment(0, 0, new IdentifierValue("t_config_index")));
        sqlStatement.getIndexes().add(new IndexSegment(0, 0, new IdentifierValue("t_config_index_new")));
        TableMetaData tableMetaData = mock(TableMetaData.class);
        Map<String, IndexMetaData> indexes = mock(HashMap.class);
        when(tableMetaData.getIndexes()).thenReturn(indexes);
        when(schema.getAllTableNames()).thenReturn(Collections.singletonList("t_config"));
        when(schema.get("t_config")).thenReturn(tableMetaData);
        when(indexes.containsKey("t_config_index")).thenReturn(true);
        when(shardingRule.isBroadcastTable("t_config")).thenReturn(true);
        when(shardingRule.getTableRule("t_config")).thenReturn(new TableRule(Arrays.asList("ds_0", "ds_1"), "t_config"));
        Collection<RouteUnit> routeUnits = new LinkedList<>();
        routeUnits.add(new RouteUnit(new RouteMapper("ds_0", "ds_0"), Collections.singletonList(new RouteMapper("t_config", "t_config"))));
        when(routeContext.getRouteUnits()).thenReturn(routeUnits);
        new ShardingDropIndexStatementValidator().postValidate(shardingRule, new DropIndexStatementContext(sqlStatement), 
                Collections.emptyList(), schema, mock(ConfigurationProperties.class), routeContext);
    }
}
