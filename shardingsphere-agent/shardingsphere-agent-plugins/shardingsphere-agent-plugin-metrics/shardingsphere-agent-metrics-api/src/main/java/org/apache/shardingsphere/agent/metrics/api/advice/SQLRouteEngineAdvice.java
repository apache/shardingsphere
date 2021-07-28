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
import org.apache.shardingsphere.agent.metrics.api.reporter.MetricsReporter;
import org.apache.shardingsphere.distsql.parser.statement.ral.RALStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.RDLStatement;
import org.apache.shardingsphere.distsql.parser.statement.rql.RQLStatement;
import org.apache.shardingsphere.infra.binder.LogicSQL;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
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
import java.util.Collection;

/**
 * SQL route engine advice.
 */
public final class SQLRouteEngineAdvice implements InstanceMethodAroundAdvice {
    
    private static final String SELECT = "sql_dml_select_total";
    
    private static final String UPDATE = "sql_dml_update_total";
    
    private static final String DELETE = "sql_dml_delete_total";
    
    private static final String INSERT = "sql_dml_insert_total";
    
    private static final String SQL_DDL = "sql_ddl_total";
    
    private static final String SQL_DCL = "sql_dcl_total";
    
    private static final String SQL_DAL = "sql_dal_total";
    
    private static final String SQL_TCL = "sql_tcl_total";
    
    private static final String DIST_SQL_RQL = "dist_sql_rql_total";
    
    private static final String DIST_SQL_RDL = "dist_sql_rdl_total";
    
    private static final String DIST_SQL_RAL = "dist_sql_ral_total";
    
    private static final String ROUTE_DATASOURCE = "route_datasource";
    
    private static final String ROUTE_TABLE = "route_table";
    
    static {
        MetricsReporter.registerCounter(INSERT, "the shardingsphere proxy executor insert sql total");
        MetricsReporter.registerCounter(DELETE, "the shardingsphere proxy executor delete sql total");
        MetricsReporter.registerCounter(UPDATE, "the shardingsphere proxy executor update sql total");
        MetricsReporter.registerCounter(SELECT, "the shardingsphere proxy executor select sql total");
        MetricsReporter.registerCounter(SQL_DDL, "the shardingsphere proxy executor ddl sql total");
        MetricsReporter.registerCounter(SQL_DCL, "the shardingsphere proxy executor dcl sql total");
        MetricsReporter.registerCounter(SQL_DAL, "the shardingsphere proxy executor dal sql total");
        MetricsReporter.registerCounter(SQL_TCL, "the shardingsphere proxy executor tcl sql total");
        MetricsReporter.registerCounter(DIST_SQL_RQL, "the shardingsphere proxy executor rql dist sql total");
        MetricsReporter.registerCounter(DIST_SQL_RDL, "the shardingsphere proxy executor rdl dist total");
        MetricsReporter.registerCounter(DIST_SQL_RAL, "the shardingsphere proxy executor ral dist sql total");
        MetricsReporter.registerCounter(ROUTE_DATASOURCE, new String[] {"name"}, "the shardingsphere proxy route datasource");
        MetricsReporter.registerCounter(ROUTE_TABLE, new String[] {"name"}, "the shardingsphere proxy route table");
    }
    
    @Override
    public void beforeMethod(final AdviceTargetObject target, final Method method, final Object[] args, final MethodInvocationResult result) {
        LogicSQL logicSQL = (LogicSQL) args[0];
        SQLStatement sqlStatement = logicSQL.getSqlStatementContext().getSqlStatement();
        if (sqlStatement instanceof InsertStatement) {
            MetricsReporter.counterIncrement(INSERT);
        } else if (sqlStatement instanceof DeleteStatement) {
            MetricsReporter.counterIncrement(DELETE);
        } else if (sqlStatement instanceof UpdateStatement) {
            MetricsReporter.counterIncrement(UPDATE);
        } else if (sqlStatement instanceof SelectStatement) {
            MetricsReporter.counterIncrement(SELECT);
        } else if (sqlStatement instanceof DDLStatement) {
            MetricsReporter.counterIncrement(SQL_DDL);
        } else if (sqlStatement instanceof DCLStatement) {
            MetricsReporter.counterIncrement(SQL_DCL);
        } else if (sqlStatement instanceof DALStatement) {
            MetricsReporter.counterIncrement(SQL_DAL);
        } else if (sqlStatement instanceof TCLStatement) {
            MetricsReporter.counterIncrement(SQL_TCL);
        } else if (sqlStatement instanceof RQLStatement) {
            MetricsReporter.counterIncrement(DIST_SQL_RQL);
        } else if (sqlStatement instanceof RDLStatement) {
            MetricsReporter.counterIncrement(DIST_SQL_RDL);
        } else if (sqlStatement instanceof RALStatement) {
            MetricsReporter.counterIncrement(DIST_SQL_RAL);
        }
    }

    @Override
    public void afterMethod(final AdviceTargetObject target, final Method method, final Object[] args, final MethodInvocationResult result) {
        RouteContext routeContext = (RouteContext) result.getResult();
        if (null != routeContext) {
            Collection<RouteUnit> routeUnits = routeContext.getRouteUnits();
            routeUnits.forEach(each -> {
                RouteMapper dataSourceMapper = each.getDataSourceMapper();
                MetricsReporter.counterIncrement(ROUTE_DATASOURCE, new String[]{dataSourceMapper.getActualName()});
                each.getTableMappers().forEach(table -> MetricsReporter.counterIncrement(ROUTE_TABLE, new String[]{table.getActualName()}));
            });
        }
    }
}
