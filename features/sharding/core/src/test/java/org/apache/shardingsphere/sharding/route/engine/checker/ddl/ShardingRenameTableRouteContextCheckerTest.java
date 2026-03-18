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

package org.apache.shardingsphere.sharding.route.engine.checker.ddl;

import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.CommonSQLStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.sharding.exception.connection.ShardingDDLRouteException;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.rule.ShardingTable;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.table.RenameTableDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.SQLStatementAttributes;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.type.TableSQLStatementAttribute;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.table.RenameTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShardingRenameTableRouteContextCheckerTest {
    
    @Mock
    private ShardingRule shardingRule;
    
    @Mock
    private QueryContext queryContext;
    
    @Test
    void assertCheckDifferentRouteUnitsAndDataNodesSize() {
        RouteContext routeContext = new RouteContext();
        routeContext.getRouteUnits().add(mock(RouteUnit.class));
        ShardingTable shardingTable = mock(ShardingTable.class);
        when(shardingTable.getActualDataNodes()).thenReturn(Arrays.asList(mock(DataNode.class), mock(DataNode.class)));
        when(shardingRule.getShardingTable("foo_tbl")).thenReturn(shardingTable);
        when(shardingRule.isShardingTable("foo_tbl")).thenReturn(true);
        SQLStatementContext sqlStatementContext = createRenameTableStatementContext();
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class);
        ConfigurationProperties props = mock(ConfigurationProperties.class);
        when(queryContext.getSqlStatementContext()).thenReturn(sqlStatementContext);
        assertThrows(ShardingDDLRouteException.class, () -> new ShardingRenameTableRouteContextChecker().check(shardingRule, queryContext, database, props, routeContext));
    }
    
    @Test
    void assertCheckNormalCase() {
        RouteContext routeContext = new RouteContext();
        routeContext.getRouteUnits().add(mock(RouteUnit.class));
        ShardingTable shardingTable = mock(ShardingTable.class);
        when(shardingTable.getActualDataNodes()).thenReturn(Collections.singletonList(mock(DataNode.class)));
        when(shardingRule.getShardingTable("foo_tbl")).thenReturn(shardingTable);
        when(shardingRule.isShardingTable("foo_tbl")).thenReturn(true);
        SQLStatementContext sqlStatementContext = createRenameTableStatementContext();
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class);
        ConfigurationProperties props = mock(ConfigurationProperties.class);
        when(queryContext.getSqlStatementContext()).thenReturn(sqlStatementContext);
        assertDoesNotThrow(() -> new ShardingRenameTableRouteContextChecker().check(shardingRule, queryContext, database, props, routeContext));
    }
    
    private SQLStatementContext createRenameTableStatementContext() {
        RenameTableStatement sqlStatement = mock(RenameTableStatement.class);
        RenameTableDefinitionSegment renameTableDefinitionSegment = new RenameTableDefinitionSegment(0, 0);
        SimpleTableSegment table = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("foo_tbl")));
        renameTableDefinitionSegment.setTable(table);
        SimpleTableSegment renameTable = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("bar_tbl")));
        renameTableDefinitionSegment.setRenameTable(renameTable);
        when(sqlStatement.getRenameTables()).thenReturn(Collections.singleton(renameTableDefinitionSegment));
        when(sqlStatement.getAttributes()).thenReturn(new SQLStatementAttributes(new TableSQLStatementAttribute(Arrays.asList(table, renameTable))));
        return new CommonSQLStatementContext(sqlStatement);
    }
}
