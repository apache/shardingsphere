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

package org.apache.shardingsphere.agent.plugin.metrics.core.advice;

import org.apache.shardingsphere.agent.api.advice.TargetAdviceObject;
import org.apache.shardingsphere.agent.api.advice.type.InstanceMethodAdvice;
import org.apache.shardingsphere.agent.plugin.metrics.core.MetricsPool;
import org.apache.shardingsphere.agent.plugin.metrics.core.MetricsWrapper;
import org.apache.shardingsphere.agent.plugin.metrics.core.constant.MetricIds;
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

/**
 * Route count advice.
 */
public final class RouteCountAdvice implements InstanceMethodAdvice {
    
    static {
        MetricsPool.create(MetricIds.ROUTED_INSERT_SQL);
        MetricsPool.create(MetricIds.ROUTED_DELETE_SQL);
        MetricsPool.create(MetricIds.ROUTED_UPDATE_SQL);
        MetricsPool.create(MetricIds.ROUTED_SELECT_SQL);
        MetricsPool.create(MetricIds.ROUTED_DATA_SOURCES);
        MetricsPool.create(MetricIds.ROUTED_TABLES);
    }
    
    @Override
    public void beforeMethod(final TargetAdviceObject target, final Method method, final Object[] args) {
        QueryContext queryContext = (QueryContext) args[1];
        SQLStatement sqlStatement = queryContext.getSqlStatementContext().getSqlStatement();
        if (sqlStatement instanceof InsertStatement) {
            MetricsPool.get(MetricIds.ROUTED_INSERT_SQL).ifPresent(MetricsWrapper::inc);
        } else if (sqlStatement instanceof DeleteStatement) {
            MetricsPool.get(MetricIds.ROUTED_DELETE_SQL).ifPresent(MetricsWrapper::inc);
        } else if (sqlStatement instanceof UpdateStatement) {
            MetricsPool.get(MetricIds.ROUTED_UPDATE_SQL).ifPresent(MetricsWrapper::inc);
        } else if (sqlStatement instanceof SelectStatement) {
            MetricsPool.get(MetricIds.ROUTED_SELECT_SQL).ifPresent(MetricsWrapper::inc);
        }
    }
    
    @Override
    public void afterMethod(final TargetAdviceObject target, final Method method, final Object[] args, final Object result) {
        if (null == result) {
            return;
        }
        for (RouteUnit each : ((RouteContext) result).getRouteUnits()) {
            RouteMapper dataSourceMapper = each.getDataSourceMapper();
            MetricsPool.get(MetricIds.ROUTED_DATA_SOURCES).ifPresent(optional -> optional.inc(dataSourceMapper.getActualName()));
            each.getTableMappers().forEach(table -> MetricsPool.get(MetricIds.ROUTED_TABLES).ifPresent(optional -> optional.inc(table.getActualName())));
        }
    }
}
