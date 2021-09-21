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

package org.apache.shardingsphere.sharding.route.engine.type.federated;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.sharding.route.engine.condition.ShardingConditions;
import org.apache.shardingsphere.sharding.route.engine.type.ShardingRouteEngine;
import org.apache.shardingsphere.sharding.route.engine.type.standard.ShardingStandardRoutingEngine;
import org.apache.shardingsphere.sharding.route.engine.type.unicast.ShardingUnicastRoutingEngine;
import org.apache.shardingsphere.sharding.rule.ShardingRule;

import java.util.Collection;
import java.util.Collections;

/**
 * Sharding federated routing engine.
 */
@RequiredArgsConstructor
public final class ShardingFederatedRoutingEngine implements ShardingRouteEngine {
    
    private final Collection<String> logicTables;
    
    private final ShardingConditions shardingConditions;
    
    private final ConfigurationProperties properties;
    
    @Override
    public RouteContext route(final ShardingRule shardingRule) {
        RouteContext result = new RouteContext();
        for (String each : logicTables) {
            RouteContext newRouteContext;
            if (shardingRule.isShardingTable(each)) {
                newRouteContext = new ShardingStandardRoutingEngine(each, shardingConditions, properties).route(shardingRule);
            } else {
                newRouteContext = new ShardingUnicastRoutingEngine(Collections.singletonList(each)).route(shardingRule);
            }
            fillRouteContext(result, newRouteContext);
        }
        result.setFederated(true);
        return result;
    }
    
    private void fillRouteContext(final RouteContext routeContext, final RouteContext newRouteContext) {
        for (RouteUnit each : newRouteContext.getRouteUnits()) {
            routeContext.putRouteUnit(each.getDataSourceMapper(), each.getTableMappers());
        }
    }
}
