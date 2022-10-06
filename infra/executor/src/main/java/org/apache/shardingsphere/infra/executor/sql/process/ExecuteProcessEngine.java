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
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.binder.QueryContext;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroupContext;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutorDataMap;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.SQLExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.process.model.ExecuteProcessConstants;
import org.apache.shardingsphere.infra.executor.sql.process.spi.ExecuteProcessReporter;
import org.apache.shardingsphere.infra.executor.sql.process.spi.ExecuteProcessReporterFactory;
import org.apache.shardingsphere.infra.metadata.user.Grantee;
import org.apache.shardingsphere.infra.util.eventbus.EventBusContext;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DDLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.DMLStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.MySQLStatement;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Execute process engine.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ExecuteProcessEngine {
    
    /**
     * Initialize connection.
     *
     * @param grantee grantee
     * @param databaseName database name
     * @param eventBusContext event bus context
     * @return execution id
     */
    public static String initializeConnection(final Grantee grantee, final String databaseName, final EventBusContext eventBusContext) {
        ExecutionGroupContext<SQLExecutionUnit> executionGroupContext = new ExecutionGroupContext<>(Collections.emptyList());
        executionGroupContext.setExecutionID(new UUID(ThreadLocalRandom.current().nextLong(), ThreadLocalRandom.current().nextLong()).toString().replace("-", ""));
        executionGroupContext.setGrantee(grantee);
        executionGroupContext.setDatabaseName(databaseName);
        Optional<ExecuteProcessReporter> reporter = ExecuteProcessReporterFactory.getInstance();
        reporter.ifPresent(executeProcessReporter -> executeProcessReporter.report(executionGroupContext));
        return executionGroupContext.getExecutionID();
    }
    
    /**
     * Finish connection.
     *
     * @param executionID execution id
     */
    public static void finishConnection(final String executionID) {
        Optional<ExecuteProcessReporter> reporter = ExecuteProcessReporterFactory.getInstance();
        reporter.ifPresent(executeProcessReporter -> executeProcessReporter.reportRemove(executionID));
    }
    
    /**
     * Initialize execution.
     *
     * @param queryContext query context
     * @param executionGroupContext execution group context
     * @param eventBusContext event bus context             
     */
    public static void initializeExecution(final QueryContext queryContext, final ExecutionGroupContext<? extends SQLExecutionUnit> executionGroupContext, final EventBusContext eventBusContext) {
        Optional<ExecuteProcessReporter> reporter = ExecuteProcessReporterFactory.getInstance();
        if (Strings.isNullOrEmpty(executionGroupContext.getExecutionID())) {
            executionGroupContext.setExecutionID(new UUID(ThreadLocalRandom.current().nextLong(), ThreadLocalRandom.current().nextLong()).toString().replace("-", ""));
        }
        if (reporter.isPresent() && isMySQLDDLOrDMLStatement(queryContext.getSqlStatementContext().getSqlStatement())) {
            ExecutorDataMap.getValue().put(ExecuteProcessConstants.EXECUTE_ID.name(), executionGroupContext.getExecutionID());
            reporter.get().report(queryContext, executionGroupContext, ExecuteProcessConstants.EXECUTE_STATUS_START, eventBusContext);
        }
    }
    
    /**
     * Finish execution.
     *
     * @param executionID execution ID
     * @param executionUnit execution unit
     * @param eventBusContext event bus context                      
     */
    public static void finishExecution(final String executionID, final SQLExecutionUnit executionUnit, final EventBusContext eventBusContext) {
        Optional<ExecuteProcessReporter> reporter = ExecuteProcessReporterFactory.getInstance();
        if (reporter.isPresent() && ExecutorDataMap.getValue().containsKey(ExecuteProcessConstants.EXECUTE_ID.name())) {
            reporter.get().report(executionID, executionUnit, ExecuteProcessConstants.EXECUTE_STATUS_DONE, eventBusContext);
        }
    }
    
    /**
     * Finish execution.
     *
     * @param executionID execution ID
     * @param eventBusContext event bus context                    
     */
    public static void finishExecution(final String executionID, final EventBusContext eventBusContext) {
        Optional<ExecuteProcessReporter> reporter = ExecuteProcessReporterFactory.getInstance();
        if (reporter.isPresent() && ExecutorDataMap.getValue().containsKey(ExecuteProcessConstants.EXECUTE_ID.name())) {
            reporter.get().report(executionID, ExecuteProcessConstants.EXECUTE_STATUS_DONE, eventBusContext);
        }
    }
    
    /**
     * Clean execution.
     */
    public static void cleanExecution() {
        Optional<ExecuteProcessReporter> reporter = ExecuteProcessReporterFactory.getInstance();
        if (reporter.isPresent() && ExecutorDataMap.getValue().containsKey(ExecuteProcessConstants.EXECUTE_ID.name())) {
            reporter.get().reportClean(ExecutorDataMap.getValue().get(ExecuteProcessConstants.EXECUTE_ID.name()).toString());
        }
        ExecutorDataMap.getValue().remove(ExecuteProcessConstants.EXECUTE_ID.name());
    }
    
    private static boolean isMySQLDDLOrDMLStatement(final SQLStatement sqlStatement) {
        return sqlStatement instanceof MySQLStatement && (sqlStatement instanceof DDLStatement || sqlStatement instanceof DMLStatement);
    }
}
