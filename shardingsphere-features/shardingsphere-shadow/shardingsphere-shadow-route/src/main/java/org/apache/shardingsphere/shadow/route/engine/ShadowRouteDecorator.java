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

import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.infra.route.RouteDecorator;
import org.apache.shardingsphere.shadow.constant.ShadowOrder;
import org.apache.shardingsphere.shadow.route.engine.judge.ShadowDataSourceJudgeEngine;
import org.apache.shardingsphere.shadow.route.engine.judge.impl.PreparedShadowDataSourceJudgeEngine;
import org.apache.shardingsphere.shadow.route.engine.judge.impl.SimpleShadowDataSourceJudgeEngine;
import org.apache.shardingsphere.shadow.rule.ShadowRule;
import org.apache.shardingsphere.sql.parser.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.DMLStatement;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Route decorator for shadow.
 */
public final class ShadowRouteDecorator implements RouteDecorator<ShadowRule> {
    
    @Override
    public void decorate(final RouteContext routeContext, final SQLStatementContext<?> sqlStatementContext, final List<Object> parameters,
                         final ShardingSphereMetaData metaData, final ShadowRule shadowRule, final ConfigurationProperties props) {
        if (routeContext.getRouteUnits().isEmpty()) {
            decorateRouteContext(routeContext, sqlStatementContext, parameters, shadowRule);
            return;
        }
        decorateRouteContextWithRouteUnits(routeContext, sqlStatementContext, parameters, shadowRule);
    }
    
    private void decorateRouteContext(final RouteContext routeContext, final SQLStatementContext<?> sqlStatementContext, final List<Object> parameters, final ShadowRule shadowRule) {
        if (!(sqlStatementContext.getSqlStatement() instanceof DMLStatement)) {
            shadowRule.getShadowMappings().forEach((key, value) -> {
                routeContext.getRouteUnits().add(new RouteUnit(new RouteMapper(key, key), Collections.emptyList()));
                routeContext.getRouteUnits().add(new RouteUnit(new RouteMapper(value, value), Collections.emptyList()));
            });
            return;
        }
        if (isShadow(sqlStatementContext, parameters, shadowRule)) {
            shadowRule.getShadowMappings().values().forEach(each -> routeContext.getRouteUnits().add(new RouteUnit(new RouteMapper(each, each), Collections.emptyList())));
        } else {
            shadowRule.getShadowMappings().keySet().forEach(each -> routeContext.getRouteUnits().add(new RouteUnit(new RouteMapper(each, each), Collections.emptyList())));
        }
    }
    
    private void decorateRouteContextWithRouteUnits(final RouteContext routeContext, final SQLStatementContext<?> sqlStatementContext, final List<Object> parameters, final ShadowRule shadowRule) {
        Collection<RouteUnit> toBeAdded = new LinkedList<>();
        if (!(sqlStatementContext.getSqlStatement() instanceof DMLStatement)) {
            for (RouteUnit each : routeContext.getRouteUnits()) {
                String shadowDataSourceName = shadowRule.getShadowMappings().get(each.getDataSourceMapper().getActualName());
                toBeAdded.add(new RouteUnit(new RouteMapper(each.getDataSourceMapper().getLogicName(), shadowDataSourceName), each.getTableMappers()));
            }
            routeContext.getRouteUnits().addAll(toBeAdded);
            return;
        }
        Collection<RouteUnit> toBeRemoved = new LinkedList<>();
        if (isShadow(sqlStatementContext, parameters, shadowRule)) {
            for (RouteUnit each : routeContext.getRouteUnits()) {
                toBeRemoved.add(each);
                String shadowDataSourceName = shadowRule.getShadowMappings().get(each.getDataSourceMapper().getActualName());
                toBeAdded.add(new RouteUnit(new RouteMapper(each.getDataSourceMapper().getLogicName(), shadowDataSourceName), each.getTableMappers()));
            }
        }
        routeContext.getRouteUnits().removeAll(toBeRemoved);
        routeContext.getRouteUnits().addAll(toBeAdded);
    }
    
    private boolean isShadow(final SQLStatementContext<?> sqlStatementContext, final List<Object> parameters, final ShadowRule shadowRule) {
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
