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

import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.infra.route.lifecycle.DecorateSQLRouter;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.shadow.constant.ShadowOrder;
import org.apache.shardingsphere.shadow.route.retriever.ShadowDataSourceMappingsRetrieverFactory;
import org.apache.shardingsphere.shadow.rule.ShadowRule;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;

/**
 * Shadow SQL router.
 */
@HighFrequencyInvocation
public final class ShadowSQLRouter implements DecorateSQLRouter<ShadowRule> {
    
    @Override
    public void decorateRouteContext(final RouteContext routeContext, final QueryContext queryContext, final ShardingSphereDatabase database,
                                     final ShadowRule rule, final Collection<String> tableNames, final ConfigurationProperties props) {
        Collection<RouteUnit> toBeRemovedRouteUnit = new LinkedList<>();
        Collection<RouteUnit> toBeAddedRouteUnit = new LinkedList<>();
        Map<String, String> shadowDataSourceMappings = ShadowDataSourceMappingsRetrieverFactory.newInstance(queryContext).retrieve(rule);
        for (RouteUnit each : routeContext.getRouteUnits()) {
            String logicName = each.getDataSourceMapper().getLogicName();
            String actualName = each.getDataSourceMapper().getActualName();
            Optional<String> productionDataSourceName = rule.findProductionDataSourceName(actualName);
            if (productionDataSourceName.isPresent()) {
                String shadowDataSourceName = shadowDataSourceMappings.get(productionDataSourceName.get());
                toBeRemovedRouteUnit.add(each);
                String dataSourceName = null == shadowDataSourceName ? productionDataSourceName.get() : shadowDataSourceName;
                toBeAddedRouteUnit.add(new RouteUnit(new RouteMapper(logicName, dataSourceName), each.getTableMappers()));
            }
        }
        routeContext.getRouteUnits().removeAll(toBeRemovedRouteUnit);
        routeContext.getRouteUnits().addAll(toBeAddedRouteUnit);
    }
    
    @Override
    public Type getType() {
        return Type.DATA_SOURCE;
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
