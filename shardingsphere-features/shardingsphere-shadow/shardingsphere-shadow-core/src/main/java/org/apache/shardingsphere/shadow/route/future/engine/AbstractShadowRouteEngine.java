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

package org.apache.shardingsphere.shadow.route.future.engine;

import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.shadow.route.future.engine.determiner.ShadowTableDeterminer;
import org.apache.shardingsphere.shadow.rule.ShadowRule;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Abstract shadow route engine.
 */
public abstract class AbstractShadowRouteEngine implements ShadowRouteEngine {
    
    private final Map<String, ShadowTableDeterminer> shadowTableDeterminers = new LinkedHashMap<>();
    
    protected Map<String, ShadowTableDeterminer> getShadowTableDeterminers() {
        return shadowTableDeterminers;
    }
    
    /**
     * Get shadow table determiner by table name.
     *
     * @param tableName table name
     * @return shadow table determiner
     */
    protected Optional<ShadowTableDeterminer> getShadowTableDeterminer(final String tableName) {
        ShadowTableDeterminer shadowTableDeterminer = shadowTableDeterminers.get(tableName);
        return Objects.isNull(shadowTableDeterminer) ? Optional.empty() : Optional.of(shadowTableDeterminer);
    }
    
    /**
     * Do shadow decorate in DML statement.
     *
     * @param routeContext route context
     * @param shadowRule shadow rule
     */
    protected void shadowDMLStatementRouteDecorate(final RouteContext routeContext, final ShadowRule shadowRule) {
        Collection<RouteUnit> toBeAdded = new LinkedList<>();
        routeContext.getRouteUnits().forEach(each -> toBeAdded.add(createActualShadowRouteUnit(each, shadowRule)));
        routeContext.getRouteUnits().clear();
        routeContext.getRouteUnits().addAll(toBeAdded);
    }
    
    private RouteUnit createActualShadowRouteUnit(final RouteUnit routeUnit, final ShadowRule shadowRule) {
        return new RouteUnit(new RouteMapper(routeUnit.getDataSourceMapper().getLogicName(), shadowRule.getShadowDataSourceMappings().get(routeUnit.getDataSourceMapper().getActualName())),
                routeUnit.getTableMappers());
    }
}
