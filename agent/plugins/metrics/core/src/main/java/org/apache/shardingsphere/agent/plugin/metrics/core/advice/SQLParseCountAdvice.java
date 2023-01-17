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
import org.apache.shardingsphere.agent.plugin.metrics.core.MetricsWrapperRegistry;
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
    
    private static final String PARSED_INSERT_SQL_METRIC_KEY = "parsed_insert_sql_total";
    
    private static final String PARSED_UPDATE_SQL_METRIC_KEY = "parsed_update_sql_total";
    
    private static final String PARSED_DELETE_SQL_METRIC_KEY = "parsed_delete_sql_total";
    
    private static final String PARSED_SELECT_SQL_METRIC_KEY = "parsed_select_sql_total";
    
    private static final String PARSED_DDL_METRIC_KEY = "parsed_ddl_total";
    
    private static final String PARSED_DCL_METRIC_KEY = "parsed_dcl_total";
    
    private static final String PARSED_DAL_METRIC_KEY = "parsed_dal_total";
    
    private static final String PARSED_TCL_METRIC_KEY = "parsed_tcl_total";
    
    private static final String PARSED_RQL_METRIC_KEY = "parsed_rql_total";
    
    private static final String PARSED_RDL_METRIC_KEY = "parsed_rdl_total";
    
    private static final String PARSED_RAL_METRIC_KEY = "parsed_ral_total";
    
    private static final String PARSED_RUL_METRIC_KEY = "parsed_rul_total";
    
    @Override
    public void afterMethod(final TargetAdviceObject target, final Method method, final Object[] args, final Object result) {
        SQLStatement sqlStatement = (SQLStatement) result;
        countSQL(sqlStatement);
        countDistSQL(sqlStatement);
    }
    
    private void countSQL(final SQLStatement sqlStatement) {
        if (sqlStatement instanceof InsertStatement) {
            MetricsWrapperRegistry.get(PARSED_INSERT_SQL_METRIC_KEY).inc();
        } else if (sqlStatement instanceof UpdateStatement) {
            MetricsWrapperRegistry.get(PARSED_UPDATE_SQL_METRIC_KEY).inc();
        } else if (sqlStatement instanceof DeleteStatement) {
            MetricsWrapperRegistry.get(PARSED_DELETE_SQL_METRIC_KEY).inc();
        } else if (sqlStatement instanceof SelectStatement) {
            MetricsWrapperRegistry.get(PARSED_SELECT_SQL_METRIC_KEY).inc();
        } else if (sqlStatement instanceof DDLStatement) {
            MetricsWrapperRegistry.get(PARSED_DDL_METRIC_KEY).inc();
        } else if (sqlStatement instanceof DCLStatement) {
            MetricsWrapperRegistry.get(PARSED_DCL_METRIC_KEY).inc();
        } else if (sqlStatement instanceof DALStatement) {
            MetricsWrapperRegistry.get(PARSED_DAL_METRIC_KEY).inc();
        } else if (sqlStatement instanceof TCLStatement) {
            MetricsWrapperRegistry.get(PARSED_TCL_METRIC_KEY).inc();
        }
    }
    
    private void countDistSQL(final SQLStatement sqlStatement) {
        if (sqlStatement instanceof RQLStatement) {
            MetricsWrapperRegistry.get(PARSED_RQL_METRIC_KEY).inc();
        } else if (sqlStatement instanceof RDLStatement) {
            MetricsWrapperRegistry.get(PARSED_RDL_METRIC_KEY).inc();
        } else if (sqlStatement instanceof RALStatement) {
            MetricsWrapperRegistry.get(PARSED_RAL_METRIC_KEY).inc();
        } else if (sqlStatement instanceof RULStatement) {
            MetricsWrapperRegistry.get(PARSED_RUL_METRIC_KEY).inc();
        }
    }
}
