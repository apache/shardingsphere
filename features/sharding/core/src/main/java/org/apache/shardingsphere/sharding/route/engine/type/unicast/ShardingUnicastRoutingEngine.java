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

package org.apache.shardingsphere.sharding.route.engine.type.unicast;

import com.google.common.collect.Sets;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.ddl.AlterViewStatementContext;
import org.apache.shardingsphere.infra.binder.statement.ddl.CreateViewStatementContext;
import org.apache.shardingsphere.infra.binder.statement.ddl.DropViewStatementContext;
import org.apache.shardingsphere.infra.binder.type.CursorAvailable;
import org.apache.shardingsphere.infra.context.ConnectionContext;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.sharding.exception.syntax.DataSourceIntersectionNotFoundException;
import org.apache.shardingsphere.sharding.route.engine.type.ShardingRouteEngine;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.rule.TableRule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * Sharding unicast routing engine.
 */
@RequiredArgsConstructor
public final class ShardingUnicastRoutingEngine implements ShardingRouteEngine {
    
    private final SQLStatementContext<?> sqlStatementContext;
    
    private final Collection<String> logicTables;
    
    private final ConnectionContext connectionContext;
    
    @Override
    public RouteContext route(final ShardingRule shardingRule) {
        RouteContext result = new RouteContext();
        String dataSourceName = sqlStatementContext instanceof CursorAvailable || isViewStatementContext(sqlStatementContext)
                ? shardingRule.getDataSourceNames().iterator().next()
                : getRandomDataSourceName(shardingRule.getDataSourceNames());
        RouteMapper dataSourceMapper = new RouteMapper(dataSourceName, dataSourceName);
        if (shardingRule.isAllBroadcastTables(logicTables)) {
            List<RouteMapper> tableMappers = new ArrayList<>(logicTables.size());
            for (String each : logicTables) {
                tableMappers.add(new RouteMapper(each, each));
            }
            result.getRouteUnits().add(new RouteUnit(dataSourceMapper, tableMappers));
        } else if (logicTables.isEmpty()) {
            result.getRouteUnits().add(new RouteUnit(dataSourceMapper, Collections.emptyList()));
        } else if (1 == logicTables.size()) {
            String logicTableName = logicTables.iterator().next();
            if (!shardingRule.findTableRule(logicTableName).isPresent()) {
                result.getRouteUnits().add(new RouteUnit(dataSourceMapper, Collections.emptyList()));
                return result;
            }
            DataNode dataNode = shardingRule.getDataNode(logicTableName);
            result.getRouteUnits().add(new RouteUnit(new RouteMapper(dataNode.getDataSourceName(), dataNode.getDataSourceName()),
                    Collections.singletonList(new RouteMapper(logicTableName, dataNode.getTableName()))));
        } else {
            routeWithMultipleTables(result, shardingRule);
        }
        return result;
    }
    
    private boolean isViewStatementContext(final SQLStatementContext<?> sqlStatementContext) {
        return sqlStatementContext instanceof CreateViewStatementContext || sqlStatementContext instanceof AlterViewStatementContext || sqlStatementContext instanceof DropViewStatementContext;
    }
    
    private void routeWithMultipleTables(final RouteContext routeContext, final ShardingRule shardingRule) {
        List<RouteMapper> tableMappers = new ArrayList<>(logicTables.size());
        Set<String> availableDataSourceNames = Collections.emptySet();
        boolean first = true;
        for (String each : logicTables) {
            TableRule tableRule = shardingRule.getTableRule(each);
            DataNode dataNode = tableRule.getActualDataNodes().get(0);
            tableMappers.add(new RouteMapper(each, dataNode.getTableName()));
            Set<String> currentDataSourceNames = tableRule.getActualDataNodes().stream().map(DataNode::getDataSourceName).collect(
                    Collectors.toCollection(() -> new HashSet<>(tableRule.getActualDataSourceNames().size())));
            if (first) {
                availableDataSourceNames = currentDataSourceNames;
                first = false;
            } else {
                availableDataSourceNames = Sets.intersection(availableDataSourceNames, currentDataSourceNames);
            }
        }
        if (availableDataSourceNames.isEmpty()) {
            throw new DataSourceIntersectionNotFoundException(logicTables);
        }
        String dataSourceName = getRandomDataSourceName(availableDataSourceNames);
        routeContext.getRouteUnits().add(new RouteUnit(new RouteMapper(dataSourceName, dataSourceName), tableMappers));
    }
    
    private String getRandomDataSourceName(final Collection<String> dataSourceNames) {
        Collection<String> preferredDataSourceNames = connectionContext.getPreferredDataSourceNames();
        List<String> availableDataSourceNames = new ArrayList<>(!preferredDataSourceNames.isEmpty() ? preferredDataSourceNames : dataSourceNames);
        return availableDataSourceNames.get(ThreadLocalRandom.current().nextInt(availableDataSourceNames.size()));
    }
}
