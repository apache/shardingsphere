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

package org.apache.shardingsphere.sharding.route.engine.type.unconfigured;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteResult;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.sharding.route.engine.type.ShardingRouteEngine;
import org.apache.shardingsphere.sharding.rule.ShardingRule;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Sharding unconfigured tables engine.
 */
@RequiredArgsConstructor
public final class ShardingUnconfiguredTablesRoutingEngine implements ShardingRouteEngine {
    
    private final Collection<String> logicTables;
    
    private final Map<String, Collection<String>> unconfiguredSchemaMetaDataMap;
    
    @Override
    public void route(final RouteContext routeContext, final ShardingRule shardingRule) {
        Optional<String> dataSourceName = findDataSourceName();
        if (!dataSourceName.isPresent()) {
            throw new ShardingSphereException("Can not route tables for `%s`, please make sure the tables are in same schema.", logicTables);
        }
        RouteResult result = new RouteResult();
        List<RouteMapper> routingTables = logicTables.stream().map(table -> new RouteMapper(table, table)).collect(Collectors.toList());
        result.getRouteUnits().add(new RouteUnit(new RouteMapper(dataSourceName.get(), dataSourceName.get()), routingTables));
        routeContext.getOriginalDataNodes().addAll(result.getOriginalDataNodes());
        routeContext.getRouteUnits().addAll(result.getRouteUnits());
    }
    
    private Optional<String> findDataSourceName() {
        for (Entry<String, Collection<String>> entry : unconfiguredSchemaMetaDataMap.entrySet()) {
            if (entry.getValue().containsAll(logicTables)) {
                return Optional.of(entry.getKey());
            }
        }
        return Optional.empty();
    }
}
