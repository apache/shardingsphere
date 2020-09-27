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

import org.apache.shardingsphere.infra.route.context.DefaultRouteStageContext;
import org.apache.shardingsphere.shadow.constant.ShadowOrder;
import org.apache.shardingsphere.shadow.route.engine.judge.ShadowDataSourceJudgeEngine;
import org.apache.shardingsphere.shadow.rule.ShadowRule;
import org.apache.shardingsphere.shadow.route.engine.judge.impl.PreparedShadowDataSourceJudgeEngine;
import org.apache.shardingsphere.shadow.route.engine.judge.impl.SimpleShadowDataSourceJudgeEngine;
import org.apache.shardingsphere.sql.parser.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.DMLStatement;
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
        return routeContext.getRouteResult().getRouteUnits().isEmpty() ? getRouteContext(routeContext, shadowRule) : getRouteContextWithRouteResult(routeContext, shadowRule);
    }
    
    private RouteContext getRouteContext(final RouteContext routeContext, final ShadowRule shadowRule) {
        SQLStatementContext<?> sqlStatementContext = routeContext.getSqlStatementContext();
        SQLStatement sqlStatement = sqlStatementContext.getSqlStatement();
        RouteResult routeResult = new RouteResult();
        if (!(sqlStatement instanceof DMLStatement)) {
            shadowRule.getShadowMappings().forEach((key, value) -> {
                routeResult.getRouteUnits().add(new RouteUnit(new RouteMapper(key, key), Collections.emptyList()));
                routeResult.getRouteUnits().add(new RouteUnit(new RouteMapper(value, value), Collections.emptyList()));
            });
            routeContext.addNextRouteStageContext(getTypeClass(), new DefaultRouteStageContext());
            return routeContext;
        }
        if (isShadow(routeContext, shadowRule)) {
            shadowRule.getShadowMappings().values().forEach(each -> routeResult.getRouteUnits().add(new RouteUnit(new RouteMapper(each, each), Collections.emptyList())));
        } else {
            shadowRule.getShadowMappings().keySet().forEach(each -> routeResult.getRouteUnits().add(new RouteUnit(new RouteMapper(each, each), Collections.emptyList())));
        }
        routeContext.addNextRouteStageContext(getTypeClass(), new DefaultRouteStageContext());
        return routeContext;
    }
    
    private RouteContext getRouteContextWithRouteResult(final RouteContext routeContext, final ShadowRule shadowRule) {
        SQLStatement sqlStatement = routeContext.getSqlStatementContext().getSqlStatement();
        Collection<RouteUnit> toBeAdded = new LinkedList<>();
        if (!(sqlStatement instanceof DMLStatement)) {
            for (RouteUnit each : routeContext.getRouteResult().getRouteUnits()) {
                String shadowDataSourceName = shadowRule.getShadowMappings().get(each.getDataSourceMapper().getActualName());
                toBeAdded.add(new RouteUnit(new RouteMapper(each.getDataSourceMapper().getLogicName(), shadowDataSourceName), each.getTableMappers()));
            }
            routeContext.getRouteResult().getRouteUnits().addAll(toBeAdded);
            return routeContext;
        }
        Collection<RouteUnit> toBeRemoved = new LinkedList<>();
        if (isShadow(routeContext, shadowRule)) {
            for (RouteUnit each : routeContext.getRouteResult().getRouteUnits()) {
                toBeRemoved.add(each);
                String shadowDataSourceName = shadowRule.getShadowMappings().get(each.getDataSourceMapper().getActualName());
                toBeAdded.add(new RouteUnit(new RouteMapper(each.getDataSourceMapper().getLogicName(), shadowDataSourceName), each.getTableMappers()));
            }
        }
        routeContext.getRouteResult().getRouteUnits().removeAll(toBeRemoved);
        routeContext.getRouteResult().getRouteUnits().addAll(toBeAdded);
        return routeContext;
    }
    
    private boolean isShadow(final RouteContext routeContext, final ShadowRule shadowRule) {
        List<Object> parameters = routeContext.getParameters();
        SQLStatementContext<?> sqlStatementContext = routeContext.getSqlStatementContext();
        ShadowDataSourceJudgeEngine shadowDataSourceRouter = parameters.isEmpty() ? new SimpleShadowDataSourceJudgeEngine(shadowRule, sqlStatementContext)
                : new PreparedShadowDataSourceJudgeEngine(shadowRule, sqlStatementContext, parameters);
        return shadowDataSourceRouter.isShadow();
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
