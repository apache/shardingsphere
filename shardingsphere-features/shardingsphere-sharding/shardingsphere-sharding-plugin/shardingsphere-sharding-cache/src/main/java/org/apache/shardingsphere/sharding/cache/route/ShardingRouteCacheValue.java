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

package org.apache.shardingsphere.sharding.cache.route;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteStageContext;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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
            Collection<DataNode> eachResult = new ArrayList<>(eachDataNodes.size());
            // TODO This could be simplified if all fields of DataNode were immutable
            for (DataNode each : eachDataNodes) {
                DataNode copiedDataNode = new DataNode(each.getDataSourceName(), each.getTableName());
                copiedDataNode.setSchemaName(each.getSchemaName());
                eachResult.add(copiedDataNode);
            }
            result.add(eachResult);
        }
        return result;
    }
    
    private Collection<RouteUnit> deepCopyRouteUnits() {
        Collection<RouteUnit> result = new ArrayList<>(cachedRouteContext.getRouteUnits().size());
        for (RouteUnit eachRouteUnit : cachedRouteContext.getRouteUnits()) {
            result.add(new RouteUnit(eachRouteUnit.getDataSourceMapper(), new ArrayList<>(eachRouteUnit.getTableMappers())));
        }
        return result;
    }
    
    private Map<Class<? extends ShardingSphereRule>, ? extends RouteStageContext> deepCopyRouteStageContext() {
        // TODO Implements deep copy for route stage contexts
        return new HashMap<>(cachedRouteContext.getRouteStageContexts());
    }
}
