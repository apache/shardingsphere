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

import com.google.common.base.Strings;
import org.apache.shardingsphere.infra.binder.QueryContext;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroupContext;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutorDataMap;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.SQLExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.process.model.ExecuteProcessConstants;
import org.apache.shardingsphere.infra.metadata.user.Grantee;
import org.apache.shardingsphere.infra.util.eventbus.EventBusContext;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DDLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.DMLStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.MySQLStatement;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Execute process engine.
 */
public final class ExecuteProcessEngine {
    
    private final ExecuteProcessReporter reporter = new ExecuteProcessReporter();
    
    /**
     * Initialize connection.
     *
     * @param grantee grantee
     * @param databaseName database name
     * @return execution ID
     */
    public String initializeConnection(final Grantee grantee, final String databaseName) {
        ExecutionGroupContext<SQLExecutionUnit> executionGroupContext = createExecutionGroupContext(grantee, databaseName);
        reporter.report(executionGroupContext);
        return executionGroupContext.getExecutionID();
    }
    
    private ExecutionGroupContext<SQLExecutionUnit> createExecutionGroupContext(final Grantee grantee, final String databaseName) {
        ExecutionGroupContext<SQLExecutionUnit> result = new ExecutionGroupContext<>(Collections.emptyList());
        result.setExecutionID(new UUID(ThreadLocalRandom.current().nextLong(), ThreadLocalRandom.current().nextLong()).toString().replace("-", ""));
        result.setGrantee(grantee);
        result.setDatabaseName(databaseName);
        return result;
    }
    
    /**
     * Finish connection.
     *
     * @param executionID execution ID
     */
    public void finishConnection(final String executionID) {
        reporter.reportRemove(executionID);
    }
    
    /**
     * Initialize execution.
     *
     * @param queryContext query context
     * @param executionGroupContext execution group context
     * @param eventBusContext event bus context
     */
    public void initializeExecution(final QueryContext queryContext, final ExecutionGroupContext<? extends SQLExecutionUnit> executionGroupContext, final EventBusContext eventBusContext) {
        if (Strings.isNullOrEmpty(executionGroupContext.getExecutionID())) {
            executionGroupContext.setExecutionID(new UUID(ThreadLocalRandom.current().nextLong(), ThreadLocalRandom.current().nextLong()).toString().replace("-", ""));
        }
        if (isMySQLDDLOrDMLStatement(queryContext.getSqlStatementContext().getSqlStatement())) {
            ExecutorDataMap.getValue().put(ExecuteProcessConstants.EXECUTE_ID.name(), executionGroupContext.getExecutionID());
            reporter.report(queryContext, executionGroupContext, ExecuteProcessConstants.EXECUTE_STATUS_START);
        }
    }
    
    /**
     * Finish execution.
     *
     * @param executionID execution ID
     * @param executionUnit execution unit
     */
    public void finishExecution(final String executionID, final SQLExecutionUnit executionUnit) {
        reporter.report(executionID, executionUnit, ExecuteProcessConstants.EXECUTE_STATUS_DONE);
    }
    
    /**
     * Finish execution.
     *
     * @param executionID execution ID
     * @param eventBusContext event bus context
     */
    public void finishExecution(final String executionID, final EventBusContext eventBusContext) {
        if (ExecutorDataMap.getValue().containsKey(ExecuteProcessConstants.EXECUTE_ID.name())) {
            reporter.report(executionID, ExecuteProcessConstants.EXECUTE_STATUS_DONE, eventBusContext);
        }
    }
    
    /**
     * Clean execution.
     */
    public void cleanExecution() {
        if (ExecutorDataMap.getValue().containsKey(ExecuteProcessConstants.EXECUTE_ID.name())) {
            reporter.reportClean(ExecutorDataMap.getValue().get(ExecuteProcessConstants.EXECUTE_ID.name()).toString());
        }
        ExecutorDataMap.getValue().remove(ExecuteProcessConstants.EXECUTE_ID.name());
    }
    
    private boolean isMySQLDDLOrDMLStatement(final SQLStatement sqlStatement) {
        return sqlStatement instanceof MySQLStatement && (sqlStatement instanceof DDLStatement || sqlStatement instanceof DMLStatement);
    }
}
