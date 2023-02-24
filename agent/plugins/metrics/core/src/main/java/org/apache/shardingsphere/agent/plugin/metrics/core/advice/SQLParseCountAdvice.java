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
import org.apache.shardingsphere.distsql.parser.statement.ral.RALStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.RDLStatement;
import org.apache.shardingsphere.distsql.parser.statement.rql.RQLStatement;
import org.apache.shardingsphere.distsql.parser.statement.rul.RULStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dal.DALStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dcl.DCLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DDLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.UpdateStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.tcl.TCLStatement;

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
        if (sqlStatement instanceof DDLStatement) {
            return Optional.of("DDL");
        }
        if (sqlStatement instanceof DCLStatement) {
            return Optional.of("DCL");
        }
        if (sqlStatement instanceof DALStatement) {
            return Optional.of("DAL");
        }
        if (sqlStatement instanceof TCLStatement) {
            return Optional.of("TCL");
        }
        if (sqlStatement instanceof RQLStatement) {
            return Optional.of("RQL");
        }
        if (sqlStatement instanceof RDLStatement) {
            return Optional.of("RDL");
        }
        if (sqlStatement instanceof RALStatement) {
            return Optional.of("RAL");
        }
        if (sqlStatement instanceof RULStatement) {
            return Optional.of("RUL");
        }
        return Optional.empty();
    }
}
