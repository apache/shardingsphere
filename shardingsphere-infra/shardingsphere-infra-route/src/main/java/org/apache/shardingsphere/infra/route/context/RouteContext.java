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

package org.apache.shardingsphere.infra.route.context;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.TreeMap;

/**
 * Route context.
 */
@RequiredArgsConstructor
@Getter
public final class RouteContext {

    private final Map<Integer, RouteStageContext> routeStageContexts = new TreeMap<>();

    private final RouteStageContext originRouteStageContext;

    /**
     * Whether there is no RouteStageContext.
     *
     * @return has RouteStageContext or not
     */
    public boolean isRouteStageContextEmpty() {
        return routeStageContexts.isEmpty();
    }

    /**
     * Get original RouteStageContext.
     *
     * @return RouteStageContext
     */
    public RouteStageContext getOriginRouteStageContext() {
        return originRouteStageContext;
    }

    /**
     * Add route stage context.
     *
     * @param order stage order
     * @param routeStageContext route stage context
     */
    public void addRouteStageContext(final int order, final RouteStageContext routeStageContext) {
        getRouteStageContexts().put(order, routeStageContext);
    }

    /**
     * Get first route stage context.
     *
     * @return RouteStageContext first route stage context
     */
    public RouteStageContext firstRouteStageContext() {
        if (isRouteStageContextEmpty()) {
            return originRouteStageContext;
        }
        return ((TreeMap<Integer, RouteStageContext>) getRouteStageContexts()).firstEntry().getValue();
    }

    /**
     * Get last route stage context.
     *
     * @return RouteStageContext last route stage context
     */
    public RouteStageContext lastRouteStageContext() {
        if (isRouteStageContextEmpty()) {
            return originRouteStageContext;
        }
        return ((TreeMap<Integer, RouteStageContext>) getRouteStageContexts()).lastEntry().getValue();
    }
}
