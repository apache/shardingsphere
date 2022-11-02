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

package org.apache.shardingsphere.shadow.route.engine;

import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.shadow.rule.ShadowRule;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;

/**
 * Shadow route engine.
 */
public interface ShadowRouteEngine {
    
    /**
     * Route.
     *
     * @param routeContext route context
     * @param shadowRule shadow rule
     */
    void route(RouteContext routeContext, ShadowRule shadowRule);
    
    /**
     * Decorate route context.
     *
     * @param routeContext route context to be decorated
     * @param shadowRule shadow rule
     * @param shadowDataSourceMappings shadow data source mappings
     */
    default void decorateRouteContext(final RouteContext routeContext, final ShadowRule shadowRule, final Map<String, String> shadowDataSourceMappings) {
        Collection<RouteUnit> toBeRemovedRouteUnit = new LinkedList<>();
        Collection<RouteUnit> toBeAddedRouteUnit = new LinkedList<>();
        for (RouteUnit each : routeContext.getRouteUnits()) {
            String logicName = each.getDataSourceMapper().getLogicName();
            String actualName = each.getDataSourceMapper().getActualName();
            Optional<String> sourceDataSourceName = shadowRule.getSourceDataSourceName(actualName);
            if (sourceDataSourceName.isPresent()) {
                String shadowDataSourceName = shadowDataSourceMappings.get(sourceDataSourceName.get());
                toBeRemovedRouteUnit.add(each);
                toBeAddedRouteUnit.add(null == shadowDataSourceName
                        ? new RouteUnit(new RouteMapper(logicName, sourceDataSourceName.get()), each.getTableMappers())
                        : new RouteUnit(new RouteMapper(logicName, shadowDataSourceName), each.getTableMappers()));
            }
        }
        routeContext.getRouteUnits().removeAll(toBeRemovedRouteUnit);
        routeContext.getRouteUnits().addAll(toBeAddedRouteUnit);
    }
}
