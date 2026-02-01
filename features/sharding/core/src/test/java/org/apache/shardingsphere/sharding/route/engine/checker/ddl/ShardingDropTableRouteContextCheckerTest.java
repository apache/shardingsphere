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

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.binder.context.statement.type.CommonSQLStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sharding.exception.connection.ShardingDDLRouteException;
import org.apache.shardingsphere.sharding.exception.metadata.InUsedTablesException;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.rule.ShardingTable;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.table.DropTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ShardingDropTableRouteContextCheckerTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @Mock
    private ShardingRule shardingRule;
    
    @Mock
    private RouteContext routeContext;
    
    @Mock
    private QueryContext queryContext;
    
    @BeforeEach
    void init() {
        Map<String, ShardingTable> shardingTables = new LinkedHashMap<>(2, 1F);
        shardingTables.put("t_order_item", createShardingTable("t_order_item"));
        shardingTables.put("t_order", createShardingTable("t_order"));
        when(shardingRule.getShardingTables()).thenReturn(shardingTables);
    }
    
    @Test
    void assertCheckWhenDropTableInUsed() {
        DropTableStatement sqlStatement = new DropTableStatement(databaseType);
        SimpleTableSegment table = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order_item")));
        sqlStatement.getTables().add(table);
        sqlStatement.buildAttributes();
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getName()).thenReturn("db_schema");
        when(database.getSchema("db_schema").containsTable("t_order_item")).thenReturn(true);
        Collection<RouteUnit> routeUnits = new LinkedList<>();
        RouteMapper dataSourceMapper = new RouteMapper("db", "db1");
        Collection<RouteMapper> tableMapper = new LinkedList<>();
        tableMapper.add(new RouteMapper("t_order_item", "t_order_item_1"));
        tableMapper.add(new RouteMapper("t_order_item", "t_order_item_2"));
        RouteUnit routeUnit = new RouteUnit(dataSourceMapper, tableMapper);
        routeUnits.add(routeUnit);
        RouteContext routeContext = mock(RouteContext.class);
        when(routeContext.getRouteUnits()).thenReturn(routeUnits);
        when(queryContext.getSqlStatementContext()).thenReturn(new CommonSQLStatementContext(sqlStatement));
        assertThrows(InUsedTablesException.class, () -> new ShardingDropTableRouteContextChecker().check(shardingRule, queryContext, database, mock(ConfigurationProperties.class), routeContext));
    }
    
    private ShardingTable createShardingTable(final String tableName) {
        ShardingTable result = mock(ShardingTable.class);
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
    
    @AfterEach
    void clean() {
        shardingRule = mock(ShardingRule.class);
    }
    
    @Test
    void assertCheckWithSameRouteResultShardingTableForPostgreSQL() {
        DropTableStatement sqlStatement = new DropTableStatement(databaseType);
        SimpleTableSegment table = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order")));
        sqlStatement.getTables().add(table);
        sqlStatement.buildAttributes();
        when(shardingRule.isShardingTable("t_order")).thenReturn(true);
        when(shardingRule.getShardingTable("t_order")).thenReturn(new ShardingTable(Arrays.asList("ds_0", "ds_1"), "t_order"));
        Collection<RouteUnit> routeUnits = new LinkedList<>();
        routeUnits.add(new RouteUnit(new RouteMapper("ds_0", "ds_0"), Collections.singleton(new RouteMapper("t_order", "t_order_0"))));
        routeUnits.add(new RouteUnit(new RouteMapper("ds_1", "ds_1"), Collections.singleton(new RouteMapper("t_order", "t_order_0"))));
        when(routeContext.getRouteUnits()).thenReturn(routeUnits);
        when(queryContext.getSqlStatementContext()).thenReturn(new CommonSQLStatementContext(sqlStatement));
        assertDoesNotThrow(() -> new ShardingDropTableRouteContextChecker().check(shardingRule, queryContext, mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS),
                mock(ConfigurationProperties.class), routeContext));
    }
    
    @Test
    void assertCheckWithDifferentRouteResultShardingTableForPostgreSQL() {
        DropTableStatement sqlStatement = new DropTableStatement(databaseType);
        SimpleTableSegment table = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order")));
        sqlStatement.getTables().add(table);
        sqlStatement.buildAttributes();
        when(shardingRule.isShardingTable("t_order")).thenReturn(true);
        when(shardingRule.getShardingTable("t_order")).thenReturn(new ShardingTable(Arrays.asList("ds_0", "ds_1"), "t_order"));
        Collection<RouteUnit> routeUnits = new LinkedList<>();
        routeUnits.add(new RouteUnit(new RouteMapper("ds_0", "ds_0"), Collections.singleton(new RouteMapper("t_order", "t_order_0"))));
        when(routeContext.getRouteUnits()).thenReturn(routeUnits);
        when(queryContext.getSqlStatementContext()).thenReturn(new CommonSQLStatementContext(sqlStatement));
        assertThrows(ShardingDDLRouteException.class, () -> new ShardingDropTableRouteContextChecker().check(shardingRule, queryContext, mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS),
                mock(ConfigurationProperties.class), routeContext));
    }
    
    @Test
    void assertCheckWithSameRouteResultBroadcastTableForPostgreSQL() {
        DropTableStatement sqlStatement = new DropTableStatement(databaseType);
        SimpleTableSegment table = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_config")));
        sqlStatement.getTables().add(table);
        sqlStatement.buildAttributes();
        when(shardingRule.getShardingTable("t_config")).thenReturn(new ShardingTable(Arrays.asList("ds_0", "ds_1"), "t_config"));
        Collection<RouteUnit> routeUnits = new LinkedList<>();
        routeUnits.add(new RouteUnit(new RouteMapper("ds_0", "ds_0"), Collections.singleton(new RouteMapper("t_config", "t_config"))));
        routeUnits.add(new RouteUnit(new RouteMapper("ds_1", "ds_1"), Collections.singleton(new RouteMapper("t_config", "t_config"))));
        when(routeContext.getRouteUnits()).thenReturn(routeUnits);
        when(queryContext.getSqlStatementContext()).thenReturn(new CommonSQLStatementContext(sqlStatement));
        assertDoesNotThrow(() -> new ShardingDropTableRouteContextChecker().check(shardingRule, queryContext, mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS),
                mock(ConfigurationProperties.class), routeContext));
    }
}
