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

package org.apache.shardingsphere.sharding.route.engine.type.defaultdb;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.sharding.route.engine.type.ShardingRouteEngine;
import org.apache.shardingsphere.underlying.route.context.RouteResult;
import org.apache.shardingsphere.underlying.route.context.TableUnit;
import org.apache.shardingsphere.underlying.route.context.RouteUnit;
import org.apache.shardingsphere.core.rule.ShardingRule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Sharding default datasource engine.
 * 
 * @author maxiaoguang
 */
@RequiredArgsConstructor
public final class ShardingDefaultDatabaseRoutingEngine implements ShardingRouteEngine {
    
    private final Collection<String> logicTables;
    
    @Override
    public RouteResult route(final ShardingRule shardingRule) {
        RouteResult result = new RouteResult();
        List<TableUnit> routingTables = new ArrayList<>(logicTables.size());
        for (String each : logicTables) {
            routingTables.add(new TableUnit(each, each));
        }
        RouteUnit routeUnit = new RouteUnit(shardingRule.getShardingDataSourceNames().getDefaultDataSourceName());
        routeUnit.getTableUnits().addAll(routingTables);
        result.getRouteUnits().add(routeUnit);
        return result;
    }
}
