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

import org.apache.shardingsphere.infra.binder.context.statement.ddl.DropIndexStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.sharding.exception.connection.ShardingDDLRouteException;
import org.apache.shardingsphere.sharding.exception.metadata.IndexNotExistedException;
import org.apache.shardingsphere.sharding.route.engine.validator.ddl.impl.ShardingDropIndexStatementValidator;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.rule.TableRule;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.index.IndexNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLDropIndexStatement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShardingDropIndexStatementValidatorTest {
    
    @Mock
    private ShardingRule shardingRule;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereDatabase database;
    
    @Mock
    private RouteContext routeContext;
    
    @Test
    void assertPreValidateDropIndexWhenIndexExistForPostgreSQL() {
        PostgreSQLDropIndexStatement sqlStatement = new PostgreSQLDropIndexStatement(false);
        sqlStatement.getIndexes().add(new IndexSegment(0, 0, new IndexNameSegment(0, 0, new IdentifierValue("t_order_index"))));
        sqlStatement.getIndexes().add(new IndexSegment(0, 0, new IndexNameSegment(0, 0, new IdentifierValue("t_order_index_new"))));
        ShardingSphereTable table = mock(ShardingSphereTable.class);
        when(database.getSchema("public").getAllTableNames()).thenReturn(Collections.singletonList("t_order"));
        when(database.getSchema("public").getTable("t_order")).thenReturn(table);
        when(table.containsIndex("t_order_index")).thenReturn(true);
        when(table.containsIndex("t_order_index_new")).thenReturn(true);
        assertDoesNotThrow(() -> new ShardingDropIndexStatementValidator().preValidate(
                shardingRule, new DropIndexStatementContext(sqlStatement), Collections.emptyList(), database, mock(ConfigurationProperties.class)));
    }
    
    @Test
    void assertPreValidateDropIndexWhenIndexNotExistForPostgreSQL() {
        PostgreSQLDropIndexStatement sqlStatement = new PostgreSQLDropIndexStatement(false);
        sqlStatement.getIndexes().add(new IndexSegment(0, 0, new IndexNameSegment(0, 0, new IdentifierValue("t_order_index"))));
        sqlStatement.getIndexes().add(new IndexSegment(0, 0, new IndexNameSegment(0, 0, new IdentifierValue("t_order_index_new"))));
        ShardingSphereTable table = mock(ShardingSphereTable.class);
        when(database.getSchema("public").getAllTableNames()).thenReturn(Collections.singletonList("t_order"));
        when(database.getSchema("public").getTable("t_order")).thenReturn(table);
        when(table.containsIndex("t_order_index")).thenReturn(false);
        assertThrows(IndexNotExistedException.class,
                () -> new ShardingDropIndexStatementValidator().preValidate(
                        shardingRule, new DropIndexStatementContext(sqlStatement), Collections.emptyList(), database, mock(ConfigurationProperties.class)));
    }
    
    @Test
    void assertPostValidateDropIndexWithSameRouteResultShardingTableIndexForPostgreSQL() {
        PostgreSQLDropIndexStatement sqlStatement = new PostgreSQLDropIndexStatement(false);
        sqlStatement.getIndexes().add(new IndexSegment(0, 0, new IndexNameSegment(0, 0, new IdentifierValue("t_order_index"))));
        sqlStatement.getIndexes().add(new IndexSegment(0, 0, new IndexNameSegment(0, 0, new IdentifierValue("t_order_index_new"))));
        ShardingSphereTable table = mock(ShardingSphereTable.class);
        when(database.getSchema("public").getAllTableNames()).thenReturn(Collections.singletonList("t_order"));
        when(database.getSchema("public").getTable("t_order")).thenReturn(table);
        when(table.containsIndex("t_order_index")).thenReturn(true);
        when(shardingRule.isShardingTable("t_order")).thenReturn(true);
        when(shardingRule.getTableRule("t_order")).thenReturn(new TableRule(Arrays.asList("ds_0", "ds_1"), "t_order"));
        Collection<RouteUnit> routeUnits = new LinkedList<>();
        routeUnits.add(new RouteUnit(new RouteMapper("ds_0", "ds_0"), Collections.singletonList(new RouteMapper("t_order", "t_order_0"))));
        routeUnits.add(new RouteUnit(new RouteMapper("ds_1", "ds_1"), Collections.singletonList(new RouteMapper("t_order", "t_order_0"))));
        when(routeContext.getRouteUnits()).thenReturn(routeUnits);
        assertDoesNotThrow(() -> new ShardingDropIndexStatementValidator().postValidate(
                shardingRule, new DropIndexStatementContext(sqlStatement), new HintValueContext(), Collections.emptyList(), database, mock(ConfigurationProperties.class), routeContext));
    }
    
    @Test
    void assertPostValidateDropIndexWithDifferentRouteResultShardingTableIndexForPostgreSQL() {
        PostgreSQLDropIndexStatement sqlStatement = new PostgreSQLDropIndexStatement(false);
        sqlStatement.getIndexes().add(new IndexSegment(0, 0, new IndexNameSegment(0, 0, new IdentifierValue("t_order_index"))));
        sqlStatement.getIndexes().add(new IndexSegment(0, 0, new IndexNameSegment(0, 0, new IdentifierValue("t_order_index_new"))));
        ShardingSphereTable table = mock(ShardingSphereTable.class);
        when(database.getSchema("public").getAllTableNames()).thenReturn(Collections.singletonList("t_order"));
        when(database.getSchema("public").getTable("t_order")).thenReturn(table);
        when(table.containsIndex("t_order_index")).thenReturn(true);
        when(shardingRule.isShardingTable("t_order")).thenReturn(true);
        when(shardingRule.getTableRule("t_order")).thenReturn(new TableRule(Arrays.asList("ds_0", "ds_1"), "t_order"));
        Collection<RouteUnit> routeUnits = new LinkedList<>();
        routeUnits.add(new RouteUnit(new RouteMapper("ds_0", "ds_0"), Collections.singletonList(new RouteMapper("t_order", "t_order_0"))));
        when(routeContext.getRouteUnits()).thenReturn(routeUnits);
        assertThrows(ShardingDDLRouteException.class, () -> new ShardingDropIndexStatementValidator().postValidate(shardingRule, new DropIndexStatementContext(sqlStatement), new HintValueContext(),
                Collections.emptyList(), database, mock(ConfigurationProperties.class), routeContext));
    }
}
