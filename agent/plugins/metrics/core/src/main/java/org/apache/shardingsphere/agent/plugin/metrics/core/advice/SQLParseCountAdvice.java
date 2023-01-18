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

/**
 * SQL parse count advice.
 */
public final class SQLParseCountAdvice implements InstanceMethodAdvice {
    
    private static final String PARSED_SQL_METRIC_KEY = "parsed_sql_total";
    
    @Override
    public void afterMethod(final TargetAdviceObject target, final Method method, final Object[] args, final Object result, final String pluginType) {
        String sqlType = getSQLType((SQLStatement) result);
        if (null == sqlType) {
            return;
        }
        MetricsCollectorRegistry.<CounterMetricsCollector>get(PARSED_SQL_METRIC_KEY).inc(sqlType);
    }
    
    private String getSQLType(final SQLStatement sqlStatement) {
        String result = null;
        if (sqlStatement instanceof InsertStatement) {
            result = "INSERT";
        } else if (sqlStatement instanceof UpdateStatement) {
            result = "UPDATE";
        } else if (sqlStatement instanceof DeleteStatement) {
            result = "DELETE";
        } else if (sqlStatement instanceof SelectStatement) {
            result = "SELECT";
        } else if (sqlStatement instanceof DDLStatement) {
            result = "DDL";
        } else if (sqlStatement instanceof DCLStatement) {
            result = "DCL";
        } else if (sqlStatement instanceof DALStatement) {
            result = "DAL";
        } else if (sqlStatement instanceof TCLStatement) {
            result = "TCL";
        } else if (sqlStatement instanceof RQLStatement) {
            result = "RQL";
        } else if (sqlStatement instanceof RDLStatement) {
            result = "RDL";
        } else if (sqlStatement instanceof RALStatement) {
            result = "RAL";
        } else if (sqlStatement instanceof RULStatement) {
            result = "RUL";
        }
        return result;
    }
}
