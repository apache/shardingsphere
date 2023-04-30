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

package org.apache.shardingsphere.infra.executor.sql.process;

import org.apache.shardingsphere.infra.binder.QueryContext;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroupContext;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.SQLExecutionUnit;
import org.apache.shardingsphere.infra.metadata.user.Grantee;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DDLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.DMLStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.MySQLStatement;

/**
 * Process engine.
 */
public final class ProcessEngine {
    
    private final ProcessReporter reporter = new ProcessReporter();
    
    /**
     * Initialize connection.
     *
     * @param grantee grantee
     * @param databaseName database name
     * @return execution ID
     */
    public String initializeConnection(final Grantee grantee, final String databaseName) {
        return reporter.reportConnect(grantee, databaseName);
    }
    
    /**
     * Finish connection.
     *
     * @param executionID execution ID
     */
    public void finishConnection(final String executionID) {
        reporter.remove(executionID);
    }
    
    /**
     * Initialize execution.
     *
     * @param executionGroupContext execution group context
     * @param queryContext query context
     */
    public void initializeExecution(final ExecutionGroupContext<? extends SQLExecutionUnit> executionGroupContext, final QueryContext queryContext) {
        if (isMySQLDDLOrDMLStatement(queryContext.getSqlStatementContext().getSqlStatement())) {
            ExecuteIDContext.set(executionGroupContext.getReportContext().getExecutionID());
            reporter.reportExecute(queryContext, executionGroupContext);
        }
    }
    
    /**
     * Finish execution.
     */
    public void finishExecution() {
        if (ExecuteIDContext.isEmpty()) {
            return;
        }
        reporter.reportComplete(ExecuteIDContext.get());
    }
    
    /**
     * Clean execution.
     */
    public void cleanExecution() {
        if (ExecuteIDContext.isEmpty()) {
            return;
        }
        reporter.reset(ExecuteIDContext.get());
        ExecuteIDContext.remove();
    }
    
    private boolean isMySQLDDLOrDMLStatement(final SQLStatement sqlStatement) {
        return sqlStatement instanceof MySQLStatement && (sqlStatement instanceof DDLStatement || sqlStatement instanceof DMLStatement);
    }
}
