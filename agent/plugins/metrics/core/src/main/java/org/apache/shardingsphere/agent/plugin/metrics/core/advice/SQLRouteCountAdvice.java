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
import org.apache.shardingsphere.agent.plugin.metrics.core.wrapper.type.CounterMetricsCollector;
import org.apache.shardingsphere.agent.plugin.metrics.core.wrapper.MetricsCollectorRegistry;
import org.apache.shardingsphere.infra.binder.QueryContext;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.UpdateStatement;

import java.lang.reflect.Method;

/**
 * SQL route count advice.
 */
public final class SQLRouteCountAdvice implements InstanceMethodAdvice {
    
    private static final String ROUTED_INSERT_SQL_METRIC_KEY = "routed_insert_sql_total";
    
    private static final String ROUTED_UPDATE_SQL_METRIC_KEY = "routed_update_sql_total";
    
    private static final String ROUTED_DELETE_SQL_METRIC_KEY = "routed_delete_sql_total";
    
    private static final String ROUTED_SELECT_SQL_METRIC_KEY = "routed_select_sql_total";
    
    @Override
    public void beforeMethod(final TargetAdviceObject target, final Method method, final Object[] args) {
        QueryContext queryContext = (QueryContext) args[1];
        SQLStatement sqlStatement = queryContext.getSqlStatementContext().getSqlStatement();
        if (sqlStatement instanceof InsertStatement) {
            MetricsCollectorRegistry.<CounterMetricsCollector>get(ROUTED_INSERT_SQL_METRIC_KEY).inc();
        } else if (sqlStatement instanceof UpdateStatement) {
            MetricsCollectorRegistry.<CounterMetricsCollector>get(ROUTED_UPDATE_SQL_METRIC_KEY).inc();
        } else if (sqlStatement instanceof DeleteStatement) {
            MetricsCollectorRegistry.<CounterMetricsCollector>get(ROUTED_DELETE_SQL_METRIC_KEY).inc();
        } else if (sqlStatement instanceof SelectStatement) {
            MetricsCollectorRegistry.<CounterMetricsCollector>get(ROUTED_SELECT_SQL_METRIC_KEY).inc();
        }
    }
}
