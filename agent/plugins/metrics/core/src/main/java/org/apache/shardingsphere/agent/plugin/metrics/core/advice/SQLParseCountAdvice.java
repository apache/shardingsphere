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
 * SQL parse count advice.
 */
public final class SQLParseCountAdvice implements InstanceMethodAdvice {
    
    static {
        MetricsPool.create(MetricIds.PARSED_INSERT_SQL);
        MetricsPool.create(MetricIds.PARSED_DELETE_SQL);
        MetricsPool.create(MetricIds.PARSED_UPDATE_SQL);
        MetricsPool.create(MetricIds.PARSED_SELECT_SQL);
        MetricsPool.create(MetricIds.PARSED_DDL);
        MetricsPool.create(MetricIds.PARSED_DCL);
        MetricsPool.create(MetricIds.PARSED_DAL);
        MetricsPool.create(MetricIds.PARSED_TCL);
        MetricsPool.create(MetricIds.PARSED_RQL);
        MetricsPool.create(MetricIds.PARSED_RDL);
        MetricsPool.create(MetricIds.PARSED_RAL);
    }
    
    @Override
    public void afterMethod(final TargetAdviceObject target, final Method method, final Object[] args, final Object result) {
        SQLStatement sqlStatement = (SQLStatement) result;
        countSQL(sqlStatement);
        countDistSQL(sqlStatement);
    }
    
    private void countSQL(final SQLStatement sqlStatement) {
        if (sqlStatement instanceof InsertStatement) {
            MetricsPool.get(MetricIds.PARSED_INSERT_SQL).ifPresent(MetricsWrapper::inc);
        } else if (sqlStatement instanceof DeleteStatement) {
            MetricsPool.get(MetricIds.PARSED_DELETE_SQL).ifPresent(MetricsWrapper::inc);
        } else if (sqlStatement instanceof UpdateStatement) {
            MetricsPool.get(MetricIds.PARSED_UPDATE_SQL).ifPresent(MetricsWrapper::inc);
        } else if (sqlStatement instanceof SelectStatement) {
            MetricsPool.get(MetricIds.PARSED_SELECT_SQL).ifPresent(MetricsWrapper::inc);
        } else if (sqlStatement instanceof DDLStatement) {
            MetricsPool.get(MetricIds.PARSED_DDL).ifPresent(MetricsWrapper::inc);
        } else if (sqlStatement instanceof DCLStatement) {
            MetricsPool.get(MetricIds.PARSED_DCL).ifPresent(MetricsWrapper::inc);
        } else if (sqlStatement instanceof DALStatement) {
            MetricsPool.get(MetricIds.PARSED_DAL).ifPresent(MetricsWrapper::inc);
        } else if (sqlStatement instanceof TCLStatement) {
            MetricsPool.get(MetricIds.PARSED_TCL).ifPresent(MetricsWrapper::inc);
        }
    }
    
    private void countDistSQL(final SQLStatement sqlStatement) {
        if (sqlStatement instanceof RQLStatement) {
            MetricsPool.get(MetricIds.PARSED_RQL).ifPresent(MetricsWrapper::inc);
        } else if (sqlStatement instanceof RDLStatement) {
            MetricsPool.get(MetricIds.PARSED_RDL).ifPresent(MetricsWrapper::inc);
        } else if (sqlStatement instanceof RALStatement) {
            MetricsPool.get(MetricIds.PARSED_RAL).ifPresent(MetricsWrapper::inc);
        }
    }
}
