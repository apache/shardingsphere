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
import org.apache.shardingsphere.agent.plugin.core.util.SQLStatementUtil;
import org.apache.shardingsphere.agent.plugin.metrics.core.collector.MetricsCollectorRegistry;
import org.apache.shardingsphere.agent.plugin.metrics.core.collector.type.CounterMetricsCollector;
import org.apache.shardingsphere.agent.plugin.metrics.core.config.MetricCollectorType;
import org.apache.shardingsphere.agent.plugin.metrics.core.config.MetricConfiguration;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Optional;

/**
 * SQL parse count advice.
 */
public final class SQLParseCountAdvice implements InstanceMethodAdvice {
    
    private final MetricConfiguration config = new MetricConfiguration("parsed_sql_total",
            MetricCollectorType.COUNTER, "Total count of parsed SQL", Collections.singletonList("type"), Collections.emptyMap());
    
    @Override
    public void afterMethod(final TargetAdviceObject target, final Method method, final Object[] args, final Object result, final String pluginType) {
        getSQLType((SQLStatement) result).ifPresent(optional -> MetricsCollectorRegistry.<CounterMetricsCollector>get(config, pluginType).inc(optional));
    }
    
    private Optional<String> getSQLType(final SQLStatement sqlStatement) {
        return Optional.of(SQLStatementUtil.getType(sqlStatement).name());
    }
}
