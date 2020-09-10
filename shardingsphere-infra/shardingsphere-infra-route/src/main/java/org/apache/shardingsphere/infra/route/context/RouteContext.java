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
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.sql.parser.binder.statement.SQLStatementContext;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Route context.
 */
@RequiredArgsConstructor
@Getter
public final class RouteContext {
    
    private final SQLStatementContext<?> sqlStatementContext;
    
    private final List<Object> parameters;
    
    private final RouteResult routeResult;
    
    private final Map<Class<? extends ShardingSphereRule>, RouteStageContext> routeStageContexts = new LinkedHashMap<>();
    
    public RouteContext(final RouteContext parent, final RouteResult routeResult, final RouteStageContext nextRouteStageContext, final Class<? extends ShardingSphereRule> ruleType) {
        this(parent.getSqlStatementContext(), parent.getParameters(), routeResult);
        addBeforeRouteStageContexts(parent.getRouteStageContexts());
        addNextRouteStageContext(ruleType, nextRouteStageContext);
    }
    
    /**
     * Add before route stage context.
     *
     * @param beforeRouteStageContexts before route stage contexts
     */
    public void addBeforeRouteStageContexts(final Map<Class<? extends ShardingSphereRule>, RouteStageContext> beforeRouteStageContexts) {
        getRouteStageContexts().putAll(beforeRouteStageContexts);
    }
    
    /**
     * Add next route stage context.
     *
     * @param ruleType rule type
     * @param nextRouteStageContext next route stage contexts
     */
    public void addNextRouteStageContext(final Class<? extends ShardingSphereRule> ruleType, final RouteStageContext nextRouteStageContext) {
        getRouteStageContexts().put(ruleType, nextRouteStageContext);
    }
    
    /**
     * Get route stage context by rule type.
     *
     * @param ruleType rule type
     * @return route stage context
     */
    public RouteStageContext getRouteStageContext(final Class<? extends ShardingSphereRule> ruleType) {
        return getRouteStageContexts().get(ruleType);
    }
}
