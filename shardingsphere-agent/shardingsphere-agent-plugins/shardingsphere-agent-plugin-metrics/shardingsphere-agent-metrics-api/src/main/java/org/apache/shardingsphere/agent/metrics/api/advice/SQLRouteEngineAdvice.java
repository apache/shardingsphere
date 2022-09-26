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

package org.apache.shardingsphere.agent.metrics.api.advice;

import org.apache.shardingsphere.agent.api.advice.AdviceTargetObject;
import org.apache.shardingsphere.agent.api.advice.InstanceMethodAroundAdvice;
import org.apache.shardingsphere.agent.api.result.MethodInvocationResult;
import org.apache.shardingsphere.agent.metrics.api.MetricsPool;
import org.apache.shardingsphere.agent.metrics.api.MetricsWrapper;
import org.apache.shardingsphere.agent.metrics.api.constant.MetricIds;
import org.apache.shardingsphere.infra.binder.QueryContext;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.UpdateStatement;

import java.lang.reflect.Method;
import java.util.Collection;

/**
 * SQL route engine advice.
 */
public final class SQLRouteEngineAdvice implements InstanceMethodAroundAdvice {
    
    static {
        MetricsPool.create(MetricIds.ROUTE_SQL_INSERT);
        MetricsPool.create(MetricIds.ROUTE_SQL_DELETE);
        MetricsPool.create(MetricIds.ROUTE_SQL_UPDATE);
        MetricsPool.create(MetricIds.ROUTE_SQL_SELECT);
        MetricsPool.create(MetricIds.ROUTE_DATASOURCE);
        MetricsPool.create(MetricIds.ROUTE_TABLE);
    }
    
    @Override
    public void beforeMethod(final AdviceTargetObject target, final Method method, final Object[] args, final MethodInvocationResult result) {
        QueryContext queryContext = (QueryContext) args[1];
        SQLStatement sqlStatement = queryContext.getSqlStatementContext().getSqlStatement();
        if (sqlStatement instanceof InsertStatement) {
            MetricsPool.get(MetricIds.ROUTE_SQL_INSERT).ifPresent(MetricsWrapper::inc);
        } else if (sqlStatement instanceof DeleteStatement) {
            MetricsPool.get(MetricIds.ROUTE_SQL_DELETE).ifPresent(MetricsWrapper::inc);
        } else if (sqlStatement instanceof UpdateStatement) {
            MetricsPool.get(MetricIds.ROUTE_SQL_UPDATE).ifPresent(MetricsWrapper::inc);
        } else if (sqlStatement instanceof SelectStatement) {
            MetricsPool.get(MetricIds.ROUTE_SQL_SELECT).ifPresent(MetricsWrapper::inc);
        }
    }
    
    @Override
    public void afterMethod(final AdviceTargetObject target, final Method method, final Object[] args, final MethodInvocationResult result) {
        RouteContext routeContext = (RouteContext) result.getResult();
        if (null != routeContext) {
            Collection<RouteUnit> routeUnits = routeContext.getRouteUnits();
            routeUnits.forEach(each -> {
                RouteMapper dataSourceMapper = each.getDataSourceMapper();
                MetricsPool.get(MetricIds.ROUTE_DATASOURCE).ifPresent(optional -> optional.inc(dataSourceMapper.getActualName()));
                each.getTableMappers().forEach(table -> MetricsPool.get(MetricIds.ROUTE_TABLE).ifPresent(optional -> optional.inc(table.getActualName())));
            });
        }
    }
}
