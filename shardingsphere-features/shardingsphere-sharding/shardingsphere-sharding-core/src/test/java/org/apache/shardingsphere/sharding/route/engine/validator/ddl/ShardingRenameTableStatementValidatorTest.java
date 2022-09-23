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
import org.apache.shardingsphere.infra.binder.statement.ddl.RenameTableStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.sharding.exception.connection.ShardingDDLRouteException;
import org.apache.shardingsphere.sharding.exception.syntax.UnsupportedShardingOperationException;
import org.apache.shardingsphere.sharding.route.engine.validator.ddl.impl.ShardingRenameTableStatementValidator;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.rule.TableRule;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.table.RenameTableDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.RenameTableStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.ddl.MySQLRenameTableStatement;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class ShardingRenameTableStatementValidatorTest {
    
    @Mock
    private ShardingRule shardingRule;
    
    @Test(expected = UnsupportedShardingOperationException.class)
    public void assertPreValidateShardingTable() {
        SQLStatementContext<RenameTableStatement> sqlStatementContext = createRenameTableStatementContext("t_order", "t_user_order");
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class);
        when(shardingRule.tableRuleExists(argThat(tableNames -> tableNames.contains("t_order") || tableNames.contains("t_user_order")))).thenReturn(true);
        new ShardingRenameTableStatementValidator().preValidate(shardingRule, sqlStatementContext, Collections.emptyList(), database, mock(ConfigurationProperties.class));
    }
    
    @Test(expected = UnsupportedShardingOperationException.class)
    public void assertPreValidateBroadcastTable() {
        SQLStatementContext<RenameTableStatement> sqlStatementContext = createRenameTableStatementContext("t_order", "t_user_order");
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class);
        when(shardingRule.isBroadcastTable(eq("t_order"))).thenReturn(true);
        new ShardingRenameTableStatementValidator().preValidate(shardingRule, sqlStatementContext, Collections.emptyList(), database, mock(ConfigurationProperties.class));
    }
    
    @Test
    public void assertPreValidateNormalCase() {
        SQLStatementContext<RenameTableStatement> sqlStatementContext = createRenameTableStatementContext("t_not_sharding_table", "t_not_sharding_table_new");
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class);
        new ShardingRenameTableStatementValidator().preValidate(shardingRule, sqlStatementContext, Collections.emptyList(), database, mock(ConfigurationProperties.class));
    }
    
    @Test(expected = ShardingDDLRouteException.class)
    public void assertPostValidateDifferentRouteUnitsAndDataNodesSize() {
        RouteContext routeContext = new RouteContext();
        routeContext.getRouteUnits().add(mock(RouteUnit.class));
        TableRule tableRule = mock(TableRule.class);
        when(tableRule.getActualDataNodes()).thenReturn(Arrays.asList(mock(DataNode.class), mock(DataNode.class)));
        when(shardingRule.getTableRule("t_order")).thenReturn(tableRule);
        when(shardingRule.isShardingTable("t_order")).thenReturn(true);
        SQLStatementContext<RenameTableStatement> sqlStatementContext = createRenameTableStatementContext("t_order", "t_user_order");
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class);
        ConfigurationProperties props = mock(ConfigurationProperties.class);
        new ShardingRenameTableStatementValidator().postValidate(shardingRule, sqlStatementContext, Collections.emptyList(), database, props, routeContext);
    }
    
    @Test
    public void assertPostValidateNormalCase() {
        RouteContext routeContext = new RouteContext();
        routeContext.getRouteUnits().add(mock(RouteUnit.class));
        TableRule tableRule = mock(TableRule.class);
        when(tableRule.getActualDataNodes()).thenReturn(Collections.singletonList(mock(DataNode.class)));
        when(shardingRule.getTableRule("t_order")).thenReturn(tableRule);
        when(shardingRule.isShardingTable("t_order")).thenReturn(true);
        SQLStatementContext<RenameTableStatement> sqlStatementContext = createRenameTableStatementContext("t_order", "t_user_order");
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class);
        ConfigurationProperties props = mock(ConfigurationProperties.class);
        new ShardingRenameTableStatementValidator().postValidate(shardingRule, sqlStatementContext, Collections.emptyList(), database, props, routeContext);
    }
    
    private SQLStatementContext<RenameTableStatement> createRenameTableStatementContext(final String originTableName, final String newTableName) {
        MySQLRenameTableStatement sqlStatement = new MySQLRenameTableStatement();
        RenameTableDefinitionSegment renameTableDefinitionSegment = new RenameTableDefinitionSegment(0, 0);
        renameTableDefinitionSegment.setTable(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue(originTableName))));
        renameTableDefinitionSegment.setRenameTable(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue(newTableName))));
        sqlStatement.getRenameTables().add(renameTableDefinitionSegment);
        return new RenameTableStatementContext(sqlStatement);
    }
}
