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

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.config.exception.ShardingSphereConfigurationException;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
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

/**
 * Sharding unicast routing engine.
 */
@RequiredArgsConstructor
public final class ShardingUnicastRoutingEngine implements ShardingRouteEngine {
    
    private final Collection<String> logicTables;
    
    @Override
    public void route(final RouteContext routeContext, final ShardingRule shardingRule) {
        String dataSourceName = getRandomDataSourceName(shardingRule.getDataSourceNames());
        RouteMapper dataSourceMapper = new RouteMapper(dataSourceName, dataSourceName);
        if (shardingRule.isAllBroadcastTables(logicTables)) {
            List<RouteMapper> tableMappers = logicTables.stream().map(each -> new RouteMapper(each, each)).collect(Collectors.toCollection(() -> new ArrayList<>(logicTables.size())));
            routeContext.getRouteUnits().add(new RouteUnit(dataSourceMapper, tableMappers));
        } else if (logicTables.isEmpty()) {
            routeContext.getRouteUnits().add(new RouteUnit(dataSourceMapper, Collections.emptyList()));
        } else if (1 == logicTables.size()) {
            String logicTableName = logicTables.iterator().next();
            if (!shardingRule.findTableRule(logicTableName).isPresent()) {
                routeContext.getRouteUnits().add(new RouteUnit(dataSourceMapper, Collections.emptyList()));
                return;
            }
            DataNode dataNode = shardingRule.getDataNode(logicTableName);
            routeContext.getRouteUnits().add(new RouteUnit(new RouteMapper(dataNode.getDataSourceName(), dataNode.getDataSourceName()),
                    Collections.singletonList(new RouteMapper(logicTableName, dataNode.getTableName()))));
        } else {
            routeWithMultipleTables(routeContext, shardingRule);
        }
    }

    private void routeWithMultipleTables(final RouteContext routeContext, final ShardingRule shardingRule) throws ShardingSphereConfigurationException {
        List<RouteMapper> tableMappers = new ArrayList<>(logicTables.size());
        Set<String> availableDatasourceNames = Collections.emptySet();
        boolean first = true;
        for (String each : logicTables) {
            TableRule tableRule = shardingRule.getTableRule(each);
            DataNode dataNode = tableRule.getActualDataNodes().get(0);
            tableMappers.add(new RouteMapper(each, dataNode.getTableName()));
            Set<String> currentDataSourceNames = tableRule.getActualDataNodes().stream().map(DataNode::getDataSourceName).collect(
                    Collectors.toCollection(() -> new HashSet<>(tableRule.getActualDatasourceNames().size())));
            if (first) {
                availableDatasourceNames = currentDataSourceNames;
                first = false;
            } else {
                availableDatasourceNames = Sets.intersection(availableDatasourceNames, currentDataSourceNames);
            }
        }
        if (availableDatasourceNames.isEmpty()) {
            throw new ShardingSphereConfigurationException("Cannot find actual datasource intersection for logic tables: %s", logicTables);
        }
        String dataSourceName = getRandomDataSourceName(availableDatasourceNames);
        routeContext.getRouteUnits().add(new RouteUnit(new RouteMapper(dataSourceName, dataSourceName), tableMappers));
    }
    
    private String getRandomDataSourceName(final Collection<String> dataSourceNames) {
        return Lists.newArrayList(dataSourceNames).get(ThreadLocalRandom.current().nextInt(dataSourceNames.size()));
    }
}
