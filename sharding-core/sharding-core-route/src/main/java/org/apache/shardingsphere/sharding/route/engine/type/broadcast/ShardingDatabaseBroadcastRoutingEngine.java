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

import org.apache.shardingsphere.sharding.route.engine.type.ShardingRouteEngine;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteResult;
import org.apache.shardingsphere.infra.route.context.RouteUnit;

import java.util.Collections;

/**
 * Sharding broadcast routing engine for databases.
 */
public final class ShardingDatabaseBroadcastRoutingEngine implements ShardingRouteEngine {
    
    @Override
    public RouteResult route(final ShardingRule shardingRule) {
        RouteResult result = new RouteResult();
        for (String each : shardingRule.getDataSourceNames()) {
            result.getRouteUnits().add(new RouteUnit(new RouteMapper(each, each), Collections.emptyList()));
        }
        return result;
    }
}
