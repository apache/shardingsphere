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
import org.apache.shardingsphere.agent.plugin.metrics.core.collector.MetricsCollectorRegistry;
import org.apache.shardingsphere.agent.plugin.metrics.core.collector.type.CounterMetricsCollector;
import org.apache.shardingsphere.agent.plugin.metrics.core.config.MetricCollectorType;
import org.apache.shardingsphere.agent.plugin.metrics.core.config.MetricConfiguration;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.UpdateStatement;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Optional;

/**
 * SQL route count advice.
 */
public final class SQLRouteCountAdvice implements InstanceMethodAdvice {
    
    private final MetricConfiguration config = new MetricConfiguration("routed_sql_total",
            MetricCollectorType.COUNTER, "Total count of routed SQL", Collections.singletonList("type"), Collections.emptyMap());
    
    @Override
    public void beforeMethod(final TargetAdviceObject target, final Method method, final Object[] args, final String pluginType) {
        QueryContext queryContext = (QueryContext) args[1];
        SQLStatement sqlStatement = queryContext.getSqlStatementContext().getSqlStatement();
        getSQLType(sqlStatement).ifPresent(optional -> MetricsCollectorRegistry.<CounterMetricsCollector>get(config, pluginType).inc(optional));
    }
    
    private Optional<String> getSQLType(final SQLStatement sqlStatement) {
        if (sqlStatement instanceof InsertStatement) {
            return Optional.of("INSERT");
        }
        if (sqlStatement instanceof UpdateStatement) {
            return Optional.of("UPDATE");
        }
        if (sqlStatement instanceof DeleteStatement) {
            return Optional.of("DELETE");
        }
        if (sqlStatement instanceof SelectStatement) {
            return Optional.of("SELECT");
        }
        return Optional.empty();
    }
}
