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

package org.apache.shardingsphere.sharding.route.engine.type.broadcast;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.type.IndexAvailable;
import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.QualifiedTable;
import org.apache.shardingsphere.infra.metadata.database.schema.util.IndexMetaDataUtils;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.sharding.route.engine.type.ShardingRouteEngine;
import org.apache.shardingsphere.sharding.route.engine.type.complex.ShardingCartesianRoutingEngine;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.rule.TableRule;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.index.IndexSegment;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.stream.Collectors;

/**
 * Sharding broadcast routing engine for tables.
 */
@RequiredArgsConstructor
public final class ShardingTableBroadcastRoutingEngine implements ShardingRouteEngine {
    
    private final ShardingSphereDatabase database;
    
    private final SQLStatementContext sqlStatementContext;
    
    private final Collection<String> shardingRuleTableNames;
    
    @Override
    public RouteContext route(final ShardingRule shardingRule) {
        RouteContext result = new RouteContext();
        Collection<String> logicTableNames = getLogicTableNames();
        if (logicTableNames.isEmpty()) {
            result.getRouteUnits().addAll(getBroadcastTableRouteUnits(shardingRule, ""));
            return result;
        }
        if (logicTableNames.size() > 1 && shardingRule.isAllBindingTables(logicTableNames)) {
            result.getRouteUnits().addAll(getBindingTableRouteUnits(shardingRule, logicTableNames));
        } else {
            Collection<RouteContext> routeContexts = getRouteContexts(shardingRule, logicTableNames);
            RouteContext routeContext = new ShardingCartesianRoutingEngine(routeContexts).route(shardingRule);
            result.getOriginalDataNodes().addAll(routeContext.getOriginalDataNodes());
            result.getRouteUnits().addAll(routeContext.getRouteUnits());
        }
        return result;
    }
    
    private Collection<RouteContext> getRouteContexts(final ShardingRule shardingRule, final Collection<String> logicTableNames) {
        Collection<RouteContext> result = new LinkedList<>();
        for (String each : logicTableNames) {
            RouteContext routeContext = new RouteContext();
            if (shardingRule.findTableRule(each).isPresent()) {
                routeContext.getRouteUnits().addAll(getAllRouteUnits(shardingRule, each));
            }
            if (!routeContext.getRouteUnits().isEmpty()) {
                result.add(routeContext);
            }
        }
        return result;
    }
    
    private Collection<RouteUnit> getBindingTableRouteUnits(final ShardingRule shardingRule, final Collection<String> tableNames) {
        String primaryTableName = tableNames.iterator().next();
        Collection<RouteUnit> result = new LinkedList<>();
        TableRule tableRule = shardingRule.getTableRule(primaryTableName);
        for (DataNode each : tableRule.getActualDataNodes()) {
            result.add(new RouteUnit(new RouteMapper(each.getDataSourceName(), each.getDataSourceName()), getBindingTableMappers(shardingRule, each, primaryTableName, tableNames)));
        }
        return result;
    }
    
    private Collection<RouteMapper> getBindingTableMappers(final ShardingRule shardingRule, final DataNode dataNode, final String primaryTableName, final Collection<String> tableNames) {
        Collection<RouteMapper> result = new LinkedList<>();
        result.add(new RouteMapper(primaryTableName, dataNode.getTableName()));
        result.addAll(shardingRule.getLogicAndActualTablesFromBindingTable(dataNode.getDataSourceName(), primaryTableName, dataNode.getTableName(), tableNames)
                .entrySet().stream().map(each -> new RouteMapper(each.getKey(), each.getValue())).collect(Collectors.toList()));
        return result;
    }
    
    private Collection<String> getLogicTableNames() {
        if (!shardingRuleTableNames.isEmpty()) {
            return shardingRuleTableNames;
        }
        return sqlStatementContext instanceof IndexAvailable
                ? getTableNames(database, sqlStatementContext.getDatabaseType(), ((IndexAvailable) sqlStatementContext).getIndexes())
                : Collections.emptyList();
    }
    
    private Collection<String> getTableNames(final ShardingSphereDatabase database, final DatabaseType databaseType, final Collection<IndexSegment> indexes) {
        Collection<String> result = new LinkedList<>();
        for (QualifiedTable each : IndexMetaDataUtils.getTableNames(database, databaseType, indexes)) {
            result.add(each.getTableName());
        }
        return result;
    }
    
    private Collection<RouteUnit> getBroadcastTableRouteUnits(final ShardingRule shardingRule, final String broadcastTableName) {
        Collection<RouteUnit> result = new LinkedList<>();
        for (String each : shardingRule.getDataSourceNames()) {
            result.add(new RouteUnit(new RouteMapper(each, each), Collections.singletonList(new RouteMapper(broadcastTableName, broadcastTableName))));
        }
        return result;
    }
    
    private Collection<RouteUnit> getAllRouteUnits(final ShardingRule shardingRule, final String logicTableName) {
        Collection<RouteUnit> result = new LinkedList<>();
        TableRule tableRule = shardingRule.getTableRule(logicTableName);
        for (DataNode each : tableRule.getActualDataNodes()) {
            result.add(new RouteUnit(new RouteMapper(each.getDataSourceName(), each.getDataSourceName()), Collections.singletonList(new RouteMapper(logicTableName, each.getTableName()))));
        }
        return result;
    }
}
