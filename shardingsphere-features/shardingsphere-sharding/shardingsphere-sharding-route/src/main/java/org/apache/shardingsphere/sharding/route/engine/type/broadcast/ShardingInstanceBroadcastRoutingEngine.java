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
import org.apache.shardingsphere.infra.metadata.datasource.DataSourceMetaDatas;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteResult;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.sharding.route.engine.type.ShardingRouteEngine;
import org.apache.shardingsphere.sharding.rule.ShardingRule;

import java.util.Collections;

/**
 * Sharding broadcast routing engine for database instance.
 */
@RequiredArgsConstructor
public final class ShardingInstanceBroadcastRoutingEngine implements ShardingRouteEngine {
    
    private final DataSourceMetaDatas dataSourceMetaDatas;
    
    @Override
    public RouteResult route(final RouteContext routeContext, final ShardingRule shardingRule) {
        RouteResult result = new RouteResult();
        for (String each : shardingRule.getDataSourceNames()) {
            if (dataSourceMetaDatas.getAllInstanceDataSourceNames().contains(each)) {
                result.getRouteUnits().add(new RouteUnit(new RouteMapper(each, each), Collections.emptyList()));
            }
        }
        routeContext.getRouteResult().getOriginalDataNodes().addAll(result.getOriginalDataNodes());
        routeContext.getRouteResult().getRouteUnits().addAll(result.getRouteUnits());
        return result;
    }
}
