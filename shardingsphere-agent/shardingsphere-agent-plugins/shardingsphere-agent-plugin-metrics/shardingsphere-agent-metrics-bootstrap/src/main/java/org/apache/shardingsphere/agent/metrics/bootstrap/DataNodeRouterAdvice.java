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
 *
 */

package org.apache.shardingsphere.agent.metrics.bootstrap;

import java.lang.reflect.Method;
import java.util.Collection;
import org.apache.shardingsphere.agent.core.plugin.advice.MethodAroundAdvice;
import org.apache.shardingsphere.agent.core.plugin.advice.MethodInvocationResult;
import org.apache.shardingsphere.agent.core.plugin.advice.TargetObject;
import org.apache.shardingsphere.agent.metrics.api.reporter.MetricsReporter;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.UpdateStatement;

/**
 * Data node router advice.
 */
public final class DataNodeRouterAdvice implements MethodAroundAdvice {
    
    private static final String SELECT = "sql_select_total";
    
    private static final String UPDATE = "sql_update_total";
    
    private static final String DELETE = "sql_delete_total";
    
    private static final String INSERT = "sql_insert_total";
    
    private static final String ROUTE_DATASOURCE = "route_datasource";
    
    private static final String ROUTE_TABLE = "route_table";
    
    static {
        MetricsReporter.registerCounter(SELECT, "the shardingsphere proxy executor select sql total");
        MetricsReporter.registerCounter(UPDATE, "the shardingsphere proxy executor update sql total");
        MetricsReporter.registerCounter(DELETE, "the shardingsphere proxy executor delete sql total");
        MetricsReporter.registerCounter(INSERT, "the shardingsphere proxy executor insert sql total");
        MetricsReporter.registerCounter(ROUTE_DATASOURCE, new String[] {"name"}, "the shardingsphere proxy route datasource");
        MetricsReporter.registerCounter(ROUTE_TABLE, new String[] {"name"}, "the shardingsphere proxy route table");
    }
    
    @Override
    public void beforeMethod(final TargetObject target, final Method method, final Object[] args, final MethodInvocationResult result) {
        SQLStatement sqlStatement = (SQLStatement) args[0];
        if (sqlStatement instanceof InsertStatement) {
            MetricsReporter.counterIncrement(SELECT);
        } else if (sqlStatement instanceof DeleteStatement) {
            MetricsReporter.counterIncrement(DELETE);
        } else if (sqlStatement instanceof UpdateStatement) {
            MetricsReporter.counterIncrement(UPDATE);
        } else if (sqlStatement instanceof SelectStatement) {
            MetricsReporter.counterIncrement(INSERT);
        }
    }

    @Override
    public void afterMethod(final TargetObject target, final Method method, final Object[] args, final MethodInvocationResult result) {
        RouteContext routeContext = (RouteContext) result.getResult();
        if (null != routeContext) {
            Collection<RouteUnit> routeUnits = routeContext.getRouteUnits();
            for (RouteUnit each : routeUnits) {
                RouteMapper dataSourceMapper = each.getDataSourceMapper();
                MetricsReporter.counterIncrement(ROUTE_DATASOURCE, new String[] {dataSourceMapper.getActualName()});
                for (RouteMapper table : each.getTableMappers()) {
                    MetricsReporter.counterIncrement(ROUTE_TABLE, new String[] {table.getActualName()});
                }
            }
        }
    }
}
