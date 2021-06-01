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

package org.apache.shardingsphere.governance.context.process;

import org.apache.shardingsphere.governance.core.registry.process.event.ExecuteProcessReportEvent;
import org.apache.shardingsphere.governance.core.registry.process.event.ExecuteProcessSummaryReportEvent;
import org.apache.shardingsphere.governance.core.registry.process.event.ExecuteProcessUnitReportEvent;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroupContext;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.SQLExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.process.model.ExecuteProcessConstants;
import org.apache.shardingsphere.infra.executor.sql.process.model.ExecuteProcessContext;
import org.apache.shardingsphere.infra.executor.sql.process.model.ExecuteProcessUnit;
import org.apache.shardingsphere.infra.executor.sql.process.spi.ExecuteProcessReporter;

/**
 * Governance execute process reporter.
 */
public final class GovernanceExecuteProcessReporter implements ExecuteProcessReporter {
    
    @Override
    public void report(final SQLStatementContext<?> context, final ExecutionGroupContext<? extends SQLExecutionUnit> executionGroupContext, final ExecuteProcessConstants constants) {
        ExecuteProcessContext executeProcessContext = new ExecuteProcessContext(executionGroupContext, constants);
        ShardingSphereEventBus.getInstance().post(new ExecuteProcessSummaryReportEvent(executeProcessContext));
    }
    
    @Override
    public void report(final String executionID, final SQLExecutionUnit executionUnit, final ExecuteProcessConstants constants) {
        ExecuteProcessUnit executeProcessUnit = new ExecuteProcessUnit(executionUnit.getExecutionUnit(), constants);
        ShardingSphereEventBus.getInstance().post(new ExecuteProcessUnitReportEvent(executionID, executeProcessUnit));
    }
    
    @Override
    public void report(final String executionID, final ExecuteProcessConstants constants) {
        ShardingSphereEventBus.getInstance().post(new ExecuteProcessReportEvent(executionID));
    }
}
