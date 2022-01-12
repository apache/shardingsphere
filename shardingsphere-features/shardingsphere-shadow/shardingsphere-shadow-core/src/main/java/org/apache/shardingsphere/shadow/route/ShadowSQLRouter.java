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

package org.apache.shardingsphere.shadow.route;

import org.apache.shardingsphere.infra.binder.LogicSQL;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.route.SQLRouter;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.shadow.constant.ShadowOrder;
import org.apache.shardingsphere.shadow.route.engine.ShadowRouteEngineFactory;
import org.apache.shardingsphere.shadow.rule.ShadowRule;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;

/**
 * Shadow SQL router.
 */
public final class ShadowSQLRouter implements SQLRouter<ShadowRule> {
    
    @Override
    public RouteContext createRouteContext(final LogicSQL logicSQL, final ShardingSphereMetaData metaData, final ShadowRule rule, final ConfigurationProperties props) {
        // TODO
        return new RouteContext();
    }
    
    @Override
    public void decorateRouteContext(final RouteContext routeContext,
                                     final LogicSQL logicSQL, final ShardingSphereMetaData metaData, final ShadowRule rule, final ConfigurationProperties props) {
        doDecorate(routeContext, rule);
        doShadowDecorate(routeContext, logicSQL, rule);
    }
    
    private void doDecorate(final RouteContext routeContext, final ShadowRule shadowRule) {
        Collection<RouteUnit> routeUnits = routeContext.getRouteUnits();
        Collection<RouteUnit> toBeRemoved = new LinkedList<>();
        Collection<RouteUnit> toBeAdded = new LinkedList<>();
        for (RouteUnit each : routeUnits) {
            String logicName = each.getDataSourceMapper().getLogicName();
            String actualName = each.getDataSourceMapper().getActualName();
            Optional<String> sourceDataSourceName = shadowRule.getSourceDataSourceName(actualName);
            if (sourceDataSourceName.isPresent()) {
                toBeRemoved.add(each);
                toBeAdded.add(new RouteUnit(new RouteMapper(logicName, sourceDataSourceName.get()), each.getTableMappers()));
            }
        }
        routeUnits.removeAll(toBeRemoved);
        routeUnits.addAll(toBeAdded);
    }
    
    private void doShadowDecorate(final RouteContext routeContext, final LogicSQL logicSQL, final ShadowRule shadowRule) {
        ShadowRouteEngineFactory.newInstance(logicSQL).route(routeContext, shadowRule);
    }
    
    @Override
    public int getOrder() {
        return ShadowOrder.ORDER;
    }
    
    @Override
    public Class<ShadowRule> getTypeClass() {
        return ShadowRule.class;
    }
}
