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

package org.apache.shardingsphere.sharding.cache.route.cache;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteStageContext;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Value of sharding route cache.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShardingRouteCacheValue {
    
    private final boolean cacheable;
    
    private final RouteContext cachedRouteContext;
    
    public ShardingRouteCacheValue(final RouteContext routeContext) {
        this(null != routeContext, routeContext);
    }
    
    /**
     * Get cached route context.
     *
     * @return optional cached route context
     */
    public Optional<RouteContext> getCachedRouteContext() {
        return cacheable ? Optional.of(deepCopyRouteContext()) : Optional.empty();
    }
    
    private RouteContext deepCopyRouteContext() {
        RouteContext result = new RouteContext();
        result.getOriginalDataNodes().addAll(deepCopyOriginalDataNodes());
        result.getRouteUnits().addAll(deepCopyRouteUnits());
        result.getRouteStageContexts().putAll(deepCopyRouteStageContext());
        return result;
    }
    
    private Collection<Collection<DataNode>> deepCopyOriginalDataNodes() {
        Collection<Collection<DataNode>> result = new ArrayList<>(cachedRouteContext.getOriginalDataNodes().size());
        for (Collection<DataNode> eachDataNodes : cachedRouteContext.getOriginalDataNodes()) {
            result.add(eachDataNodes.stream().map(each -> new DataNode(each.getDataSourceName(), each.getSchemaName(), each.getTableName())).collect(Collectors.toList()));
        }
        return result;
    }
    
    private Collection<RouteUnit> deepCopyRouteUnits() {
        Collection<RouteUnit> result = new ArrayList<>(cachedRouteContext.getRouteUnits().size());
        for (RouteUnit each : cachedRouteContext.getRouteUnits()) {
            result.add(new RouteUnit(each.getDataSourceMapper(), new ArrayList<>(each.getTableMappers())));
        }
        return result;
    }
    
    private Map<Class<? extends ShardingSphereRule>, ? extends RouteStageContext> deepCopyRouteStageContext() {
        // TODO Implements deep copy for route stage contexts
        return cachedRouteContext.getRouteStageContexts();
    }
}
