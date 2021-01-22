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

package org.apache.shardingsphere.sharding.route.engine.type.single;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.sharding.route.engine.type.ShardingRouteEngine;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.rule.TableRule;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

/**
 * Sharding single routing engine.
 */
@RequiredArgsConstructor
public final class ShardingSingleRoutingEngine implements ShardingRouteEngine {
    
    private final Collection<String> logicTables;
    
    @Override
    public void route(final RouteContext routeContext, final ShardingRule shardingRule) {
        for (String each : logicTables) {
            if (shardingRule.getSingleTableRules().containsKey(each)) {
                String datasource = shardingRule.getSingleTableRules().get(each).getDataSourceName();
                RouteUnit unit = new RouteUnit(new RouteMapper(datasource, datasource), Collections.singletonList(new RouteMapper(each, each)));
                routeContext.getRouteUnits().add(unit);
            } else {
                routeContext.getRouteUnits().addAll(getAllRouteUnits(shardingRule, each));
            }
        }
        routeContext.setToCalcite(true);
    }
    
    private Collection<RouteUnit> getAllRouteUnits(final ShardingRule shardingRule, final String logicTableName) {
        Collection<RouteUnit> result = new LinkedList<>();
        TableRule tableRule = shardingRule.getTableRule(logicTableName);
        for (DataNode each : tableRule.getActualDataNodes()) {
            RouteUnit routeUnit = new RouteUnit(new RouteMapper(each.getDataSourceName(), each.getDataSourceName()), Collections.singletonList(new RouteMapper(logicTableName, each.getTableName())));
            result.add(routeUnit);
        }
        return result;
    }
}
