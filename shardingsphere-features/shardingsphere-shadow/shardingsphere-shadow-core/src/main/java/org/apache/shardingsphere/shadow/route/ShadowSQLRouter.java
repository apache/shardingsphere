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
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.route.SQLRouter;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.shadow.constant.ShadowOrder;
import org.apache.shardingsphere.shadow.route.future.engine.ShadowRouteEngineFactory;
import org.apache.shardingsphere.shadow.route.judge.ShadowDataSourceJudgeEngine;
import org.apache.shardingsphere.shadow.route.judge.impl.PreparedShadowDataSourceJudgeEngine;
import org.apache.shardingsphere.shadow.route.judge.impl.SimpleShadowDataSourceJudgeEngine;
import org.apache.shardingsphere.shadow.rule.ShadowRule;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.DMLStatement;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Shadow SQL router.
 */
public final class ShadowSQLRouter implements SQLRouter<ShadowRule> {
    
    @Override
    public RouteContext createRouteContext(final LogicSQL logicSQL, final ShardingSphereMetaData metaData, final ShadowRule rule, final ConfigurationProperties props) {
        return logicSQL.getSqlStatementContext().getSqlStatement() instanceof DMLStatement ? createRouteContextInDML(logicSQL, rule) : createRouteContextWithoutDML(rule);
    }
    
    private RouteContext createRouteContextWithoutDML(final ShadowRule rule) {
        final RouteContext result = new RouteContext();
        rule.getShadowMappings().forEach((key, value) -> {
            result.getRouteUnits().add(createRouteUnit(key, key));
            result.getRouteUnits().add(createRouteUnit(value, value));
        });
        return result;
    }
    
    private RouteContext createRouteContextInDML(final LogicSQL logicSQL, final ShadowRule rule) {
        final RouteContext result = new RouteContext();
        Map<String, String> shadowMappings = rule.getShadowMappings();
        if (isShadow(logicSQL, rule)) {
            shadowMappings.values().forEach(each -> result.getRouteUnits().add(createRouteUnit(each, each)));
        } else {
            shadowMappings.keySet().forEach(each -> result.getRouteUnits().add(createRouteUnit(each, each)));
        }
        return result;
    }
    
    private boolean isShadow(final LogicSQL logicSQL, final ShadowRule rule) {
        final SQLStatementContext<?> sqlStatementContext = logicSQL.getSqlStatementContext();
        final List<Object> parameters = logicSQL.getParameters();
        ShadowDataSourceJudgeEngine shadowDataSourceRouter = parameters.isEmpty()
                ? new SimpleShadowDataSourceJudgeEngine(rule, sqlStatementContext) : new PreparedShadowDataSourceJudgeEngine(rule, sqlStatementContext, parameters);
        return shadowDataSourceRouter.isShadow();
    }
    
    private RouteUnit createRouteUnit(final String logicName, final String actualName) {
        return new RouteUnit(new RouteMapper(logicName, actualName), Collections.emptyList());
    }
    
    @Override
    public void decorateRouteContext(final RouteContext routeContext,
                                     final LogicSQL logicSQL, final ShardingSphereMetaData metaData, final ShadowRule rule, final ConfigurationProperties props) {
        if (rule.isEnable()) {
            doShadowDecorateFuture(routeContext, logicSQL, metaData, rule, props);
        } else {
            doShadowDecorate(routeContext, logicSQL, rule);
        }
    }
    
    private void doShadowDecorate(final RouteContext routeContext, final LogicSQL logicSQL, final ShadowRule rule) {
        Collection<RouteUnit> toBeAdded = new LinkedList<>();
        if (!(logicSQL.getSqlStatementContext().getSqlStatement() instanceof DMLStatement)) {
            for (RouteUnit each : routeContext.getRouteUnits()) {
                String shadowDataSourceName = rule.getShadowMappings().get(each.getDataSourceMapper().getActualName());
                toBeAdded.add(new RouteUnit(new RouteMapper(each.getDataSourceMapper().getLogicName(), shadowDataSourceName), each.getTableMappers()));
            }
            routeContext.getRouteUnits().addAll(toBeAdded);
            return;
        }
        Collection<RouteUnit> toBeRemoved = new LinkedList<>();
        if (isShadow(logicSQL, rule)) {
            for (RouteUnit each : routeContext.getRouteUnits()) {
                toBeRemoved.add(each);
                String shadowDataSourceName = rule.getShadowMappings().get(each.getDataSourceMapper().getActualName());
                toBeAdded.add(new RouteUnit(new RouteMapper(each.getDataSourceMapper().getLogicName(), shadowDataSourceName), each.getTableMappers()));
            }
        }
        routeContext.getRouteUnits().removeAll(toBeRemoved);
        routeContext.getRouteUnits().addAll(toBeAdded);
    }
    
    private void doShadowDecorateFuture(final RouteContext routeContext, final LogicSQL logicSQL, final ShardingSphereMetaData metaData, final ShadowRule rule, final ConfigurationProperties props) {
        ShadowRouteEngineFactory.newInstance(logicSQL).route(routeContext, logicSQL, metaData, rule, props);
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
