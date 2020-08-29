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

import org.apache.shardingsphere.infra.route.context.RouteStageContext;
import org.apache.shardingsphere.shadow.constant.ShadowOrder;
import org.apache.shardingsphere.shadow.rule.ShadowRule;
import org.apache.shardingsphere.shadow.route.engine.impl.PreparedShadowDataSourceRouter;
import org.apache.shardingsphere.shadow.route.engine.impl.SimpleShadowDataSourceRouter;
import org.apache.shardingsphere.sql.parser.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.sql.parser.sql.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.DMLStatement;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteResult;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.infra.route.decorator.RouteDecorator;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Route decorator for shadow.
 */
public final class ShadowRouteDecorator implements RouteDecorator<ShadowRule> {
    
    @Override
    public RouteContext decorate(final RouteContext routeContext, final ShardingSphereMetaData metaData, final ShadowRule shadowRule, final ConfigurationProperties props) {
        RouteStageContext preRouteStageContext = routeContext.lastRouteStageContext();
        return preRouteStageContext.getRouteResult().getRouteUnits().isEmpty() ? getRouteContext(routeContext, preRouteStageContext, shadowRule)
                : getRouteContextWithRouteResult(routeContext, preRouteStageContext, shadowRule);
    }
    
    private RouteContext getRouteContext(final RouteContext routeContext, final RouteStageContext preRouteStageContext, final ShadowRule shadowRule) {
        SQLStatementContext sqlStatementContext = preRouteStageContext.getSqlStatementContext();
        SQLStatement sqlStatement = sqlStatementContext.getSqlStatement();
        RouteResult routeResult = new RouteResult();
        List<Object> parameters = preRouteStageContext.getParameters();
        if (!(sqlStatement instanceof DMLStatement)) {
            shadowRule.getShadowMappings().forEach((k, v) -> {
                routeResult.getRouteUnits().add(new RouteUnit(new RouteMapper(k, k), Collections.emptyList()));
                routeResult.getRouteUnits().add(new RouteUnit(new RouteMapper(v, v), Collections.emptyList()));
            });
            routeContext.addRouteStageContext(getOrder(), new ShadowRouteStageContext(preRouteStageContext.getCurrentSchemaName(), sqlStatementContext, parameters, routeResult));
            return routeContext;
        }
        if (isShadowSQL(preRouteStageContext, shadowRule)) {
            shadowRule.getShadowMappings().values().forEach(each -> routeResult.getRouteUnits().add(new RouteUnit(new RouteMapper(each, each), Collections.emptyList())));
        } else {
            shadowRule.getShadowMappings().keySet().forEach(each -> routeResult.getRouteUnits().add(new RouteUnit(new RouteMapper(each, each), Collections.emptyList())));
        }
        routeContext.addRouteStageContext(getOrder(), new ShadowRouteStageContext(preRouteStageContext.getCurrentSchemaName(), sqlStatementContext, parameters, routeResult));
        return routeContext;
    }
    
    private RouteContext getRouteContextWithRouteResult(final RouteContext routeContext, final RouteStageContext preRouteStageContext, final ShadowRule shadowRule) {
        SQLStatement sqlStatement = preRouteStageContext.getSqlStatementContext().getSqlStatement();
        Collection<RouteUnit> toBeAdded = new LinkedList<>();
        if (!(sqlStatement instanceof DMLStatement)) {
            for (RouteUnit each : preRouteStageContext.getRouteResult().getRouteUnits()) {
                String shadowDataSourceName = shadowRule.getShadowMappings().get(each.getDataSourceMapper().getActualName());
                toBeAdded.add(new RouteUnit(new RouteMapper(each.getDataSourceMapper().getLogicName(), shadowDataSourceName), each.getTableMappers()));
            }
            preRouteStageContext.getRouteResult().getRouteUnits().addAll(toBeAdded);
            routeContext.addRouteStageContext(getOrder(),
                    new ShadowRouteStageContext(preRouteStageContext.getCurrentSchemaName(), preRouteStageContext.getSqlStatementContext(),
                            preRouteStageContext.getParameters(), preRouteStageContext.getRouteResult()));
            return routeContext;
        }
        Collection<RouteUnit> toBeRemoved = new LinkedList<>();
        if (isShadowSQL(preRouteStageContext, shadowRule)) {
            for (RouteUnit each : preRouteStageContext.getRouteResult().getRouteUnits()) {
                toBeRemoved.add(each);
                String shadowDataSourceName = shadowRule.getShadowMappings().get(each.getDataSourceMapper().getActualName());
                toBeAdded.add(new RouteUnit(new RouteMapper(each.getDataSourceMapper().getLogicName(), shadowDataSourceName), each.getTableMappers()));
            }
        }
        preRouteStageContext.getRouteResult().getRouteUnits().removeAll(toBeRemoved);
        preRouteStageContext.getRouteResult().getRouteUnits().addAll(toBeAdded);
        routeContext.addRouteStageContext(getOrder(),
                new ShadowRouteStageContext(preRouteStageContext.getCurrentSchemaName(), preRouteStageContext.getSqlStatementContext(),
                        preRouteStageContext.getParameters(), preRouteStageContext.getRouteResult()));
        return routeContext;
    }
    
    private boolean isShadowSQL(final RouteStageContext preRouteStageContext, final ShadowRule shadowRule) {
        List<Object> parameters = preRouteStageContext.getParameters();
        SQLStatementContext sqlStatementContext = preRouteStageContext.getSqlStatementContext();
        ShadowDataSourceRouter shadowDataSourceRouter = parameters.isEmpty() ? new SimpleShadowDataSourceRouter(shadowRule, sqlStatementContext)
                : new PreparedShadowDataSourceRouter(shadowRule, sqlStatementContext, parameters);
        return shadowDataSourceRouter.isShadowSQL();
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
