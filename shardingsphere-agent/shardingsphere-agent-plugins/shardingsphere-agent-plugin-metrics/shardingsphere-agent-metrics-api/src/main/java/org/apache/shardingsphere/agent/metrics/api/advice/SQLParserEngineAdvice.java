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
import org.apache.shardingsphere.distsql.parser.statement.ral.RALStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.RDLStatement;
import org.apache.shardingsphere.distsql.parser.statement.rql.RQLStatement;
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
 * SQL parse engine advice.
 */
public final class SQLParserEngineAdvice implements InstanceMethodAroundAdvice {
    
    static {
        MetricsPool.create(MetricIds.PARSE_SQL_INSERT);
        MetricsPool.create(MetricIds.PARSE_SQL_DELETE);
        MetricsPool.create(MetricIds.PARSE_SQL_UPDATE);
        MetricsPool.create(MetricIds.PARSE_SQL_SELECT);
        MetricsPool.create(MetricIds.PARSE_SQL_DDL);
        MetricsPool.create(MetricIds.PARSE_SQL_DCL);
        MetricsPool.create(MetricIds.PARSE_SQL_DAL);
        MetricsPool.create(MetricIds.PARSE_SQL_TCL);
        MetricsPool.create(MetricIds.PARSE_DIST_SQL_RQL);
        MetricsPool.create(MetricIds.PARSE_DIST_SQL_RDL);
        MetricsPool.create(MetricIds.PARSE_DIST_SQL_RAL);
    }
    
    @Override
    public void afterMethod(final AdviceTargetObject target, final Method method, final Object[] args, final MethodInvocationResult result) {
        SQLStatement sqlStatement = (SQLStatement) result.getResult();
        countSQL(sqlStatement);
        countDistSQL(sqlStatement);
    }
    
    private void countSQL(final SQLStatement sqlStatement) {
        if (sqlStatement instanceof InsertStatement) {
            MetricsPool.get(MetricIds.PARSE_SQL_INSERT).ifPresent(MetricsWrapper::inc);
        } else if (sqlStatement instanceof DeleteStatement) {
            MetricsPool.get(MetricIds.PARSE_SQL_DELETE).ifPresent(MetricsWrapper::inc);
        } else if (sqlStatement instanceof UpdateStatement) {
            MetricsPool.get(MetricIds.PARSE_SQL_UPDATE).ifPresent(MetricsWrapper::inc);
        } else if (sqlStatement instanceof SelectStatement) {
            MetricsPool.get(MetricIds.PARSE_SQL_SELECT).ifPresent(MetricsWrapper::inc);
        } else if (sqlStatement instanceof DDLStatement) {
            MetricsPool.get(MetricIds.PARSE_SQL_DDL).ifPresent(MetricsWrapper::inc);
        } else if (sqlStatement instanceof DCLStatement) {
            MetricsPool.get(MetricIds.PARSE_SQL_DCL).ifPresent(MetricsWrapper::inc);
        } else if (sqlStatement instanceof DALStatement) {
            MetricsPool.get(MetricIds.PARSE_SQL_DAL).ifPresent(MetricsWrapper::inc);
        } else if (sqlStatement instanceof TCLStatement) {
            MetricsPool.get(MetricIds.PARSE_SQL_TCL).ifPresent(MetricsWrapper::inc);
        }
    }
    
    private void countDistSQL(final SQLStatement sqlStatement) {
        if (sqlStatement instanceof RQLStatement) {
            MetricsPool.get(MetricIds.PARSE_DIST_SQL_RQL).ifPresent(MetricsWrapper::inc);
        } else if (sqlStatement instanceof RDLStatement) {
            MetricsPool.get(MetricIds.PARSE_DIST_SQL_RDL).ifPresent(MetricsWrapper::inc);
        } else if (sqlStatement instanceof RALStatement) {
            MetricsPool.get(MetricIds.PARSE_DIST_SQL_RAL).ifPresent(MetricsWrapper::inc);
        }
    }
}
