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
import org.apache.shardingsphere.infra.util.spi.ShardingSphereServiceLoader;

import java.util.Collections;

/**
 * Process report.
 */
public final class ProcessReporter {
    
    /**
     * Report connect.
     *
     * @param grantee grantee
     * @param databaseName databaseName
     * @return execution ID
     */
    public String reportConnect(final Grantee grantee, final String databaseName) {
        ExecutionGroupContext<? extends SQLExecutionUnit> executionGroupContext = new ExecutionGroupContext<>(Collections.emptyList(), new ExecutionGroupReportContext(databaseName, grantee));
        ProcessContext processContext = new ProcessContext(executionGroupContext);
        ShowProcessListManager.getInstance().putProcessContext(processContext.getProcessID(), processContext);
        return executionGroupContext.getReportContext().getExecutionID();
    }
    
    /**
     * Report execute.
     *
     * @param queryContext query context
     * @param executionGroupContext execution group context
     */
    public void reportExecute(final QueryContext queryContext, final ExecutionGroupContext<? extends SQLExecutionUnit> executionGroupContext) {
        ProcessContext processContext = new ProcessContext(queryContext.getSql(), executionGroupContext);
        ShowProcessListManager.getInstance().putProcessContext(processContext.getProcessID(), processContext);
        ShowProcessListManager.getInstance().putProcessStatement(processContext.getProcessID(), processContext.getProcessStatements());
    }
    
    /**
     * Report complete execution unit.
     *
     * @param executionID execution ID
     * @param completedExecutionUnitCount completed execution unit count
     */
    public void reportComplete(final String executionID, final int completedExecutionUnitCount) {
        ShowProcessListManager.getInstance().getProcessContext(executionID).completeExecutionUnits(completedExecutionUnitCount);
    }
    
    /**
     * Reset report.
     *
     * @param executionID execution ID
     */
    public void reset(final String executionID) {
        ShowProcessListManager.getInstance().removeProcessStatement(executionID);
        ProcessContext context = ShowProcessListManager.getInstance().getProcessContext(executionID);
        if (null == context) {
            return;
        }
        for (ProcessReporterCleaner each : ShardingSphereServiceLoader.getServiceInstances(ProcessReporterCleaner.class)) {
            each.reset(context);
        }
    }
    
    /**
     * Remove process context.
     *
     * @param executionID execution ID
     */
    public void remove(final String executionID) {
        ShowProcessListManager.getInstance().removeProcessStatement(executionID);
        ShowProcessListManager.getInstance().removeProcessContext(executionID);
    }
}
