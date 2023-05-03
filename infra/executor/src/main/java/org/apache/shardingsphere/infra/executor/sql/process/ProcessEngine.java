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
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroupReportContext;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.SQLExecutionUnit;
import org.apache.shardingsphere.infra.metadata.user.Grantee;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DDLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.DMLStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.MySQLStatement;

import java.util.Collections;

/**
 * Process engine.
 */
public final class ProcessEngine {
    
    /**
     * Connect.
     *
     * @param grantee grantee
     * @param databaseName database name
     * @return process ID
     */
    public String connect(final Grantee grantee, final String databaseName) {
        ExecutionGroupContext<? extends SQLExecutionUnit> executionGroupContext = new ExecutionGroupContext<>(Collections.emptyList(), new ExecutionGroupReportContext(databaseName, grantee));
        Process process = new Process(executionGroupContext);
        ProcessRegistry.getInstance().putProcess(process.getId(), process);
        return executionGroupContext.getReportContext().getProcessID();
    }
    
    /**
     * Disconnect.
     *
     * @param processID process ID
     */
    public void disconnect(final String processID) {
        ProcessRegistry.getInstance().removeProcess(processID);
        
    }
    
    /**
     * Execute SQL.
     *
     * @param executionGroupContext execution group context
     * @param queryContext query context
     */
    public void executeSQL(final ExecutionGroupContext<? extends SQLExecutionUnit> executionGroupContext, final QueryContext queryContext) {
        if (isMySQLDDLOrDMLStatement(queryContext.getSqlStatementContext().getSqlStatement())) {
            ProcessIDContext.set(executionGroupContext.getReportContext().getProcessID());
            Process process = new Process(queryContext.getSql(), executionGroupContext);
            ProcessRegistry.getInstance().putProcess(process.getId(), process);
        }
    }
    
    /**
     * Complete SQL unit execution.
     */
    public void completeSQLUnitExecution() {
        if (ProcessIDContext.isEmpty()) {
            return;
        }
        ProcessRegistry.getInstance().getProcess(ProcessIDContext.get()).completeExecutionUnit();
    }
    
    /**
     * Complete SQL execution.
     */
    public void completeSQLExecution() {
        if (ProcessIDContext.isEmpty()) {
            return;
        }
        Process process = ProcessRegistry.getInstance().getProcess(ProcessIDContext.get());
        if (null == process) {
            return;
        }
        ExecutionGroupContext<? extends SQLExecutionUnit> executionGroupContext = new ExecutionGroupContext<>(
                Collections.emptyList(), new ExecutionGroupReportContext(process.getDatabaseName(), new Grantee(process.getUsername(), process.getHostname())));
        ProcessRegistry.getInstance().putProcess(ProcessIDContext.get(), new Process(executionGroupContext));
        ProcessIDContext.remove();
    }
    
    private boolean isMySQLDDLOrDMLStatement(final SQLStatement sqlStatement) {
        return sqlStatement instanceof MySQLStatement && (sqlStatement instanceof DDLStatement || sqlStatement instanceof DMLStatement);
    }
}
