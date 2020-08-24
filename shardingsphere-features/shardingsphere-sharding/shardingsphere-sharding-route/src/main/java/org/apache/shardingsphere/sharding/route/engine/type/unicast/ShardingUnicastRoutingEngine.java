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
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.rule.TableRule;
import org.apache.shardingsphere.sharding.route.engine.type.ShardingRouteEngine;
import org.apache.shardingsphere.infra.config.exception.ShardingSphereConfigurationException;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteResult;
import org.apache.shardingsphere.infra.route.context.RouteUnit;

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
    public RouteResult route(final ShardingRule shardingRule) {
        RouteResult result = new RouteResult();
        String dataSourceName = getRandomDataSourceName(shardingRule.getDataSourceNames());
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
            List<RouteMapper> tableMappers = new ArrayList<>(logicTables.size());
            Set<String> availableDatasourceNames = null;
            boolean first = true;
            for (String each : logicTables) {
                TableRule tableRule = shardingRule.getTableRule(each);
                DataNode dataNode = tableRule.getActualDataNodes().get(0);
                tableMappers.add(new RouteMapper(each, dataNode.getTableName()));
                Set<String> currentDataSourceNames = new HashSet<>(tableRule.getActualDatasourceNames().size());
                for (DataNode eachDataNode : tableRule.getActualDataNodes()) {
                    currentDataSourceNames.add(eachDataNode.getDataSourceName());
                }
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
            dataSourceName = getRandomDataSourceName(availableDatasourceNames);
            result.getRouteUnits().add(new RouteUnit(new RouteMapper(dataSourceName, dataSourceName), tableMappers));
        }
        return result;
    }
    
    private String getRandomDataSourceName(final Collection<String> dataSourceNames) {
        return Lists.newArrayList(dataSourceNames).get(ThreadLocalRandom.current().nextInt(dataSourceNames.size()));
    }
}
