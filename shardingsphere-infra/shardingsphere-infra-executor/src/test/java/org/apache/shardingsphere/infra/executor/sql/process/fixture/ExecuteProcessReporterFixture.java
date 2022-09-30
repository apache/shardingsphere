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

package org.apache.shardingsphere.infra.executor.sql.process.fixture;

import org.apache.shardingsphere.infra.binder.QueryContext;
import org.apache.shardingsphere.infra.util.eventbus.EventBusContext;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroupContext;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.SQLExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.process.model.ExecuteProcessConstants;
import org.apache.shardingsphere.infra.executor.sql.process.spi.ExecuteProcessReporter;

import java.util.LinkedList;

public final class ExecuteProcessReporterFixture implements ExecuteProcessReporter {
    
    public static final LinkedList<String> ACTIONS = new LinkedList<>();
    
    @Override
    public void report(final ExecutionGroupContext<? extends SQLExecutionUnit> executionGroupContext) {
    }
    
    @Override
    public void report(final QueryContext queryContext, final ExecutionGroupContext<? extends SQLExecutionUnit> executionGroupContext, final ExecuteProcessConstants constants,
                       final EventBusContext eventBusContext) {
        ACTIONS.add("Report the summary of this task");
    }
    
    @Override
    public void report(final String executionID, final SQLExecutionUnit executionUnit, final ExecuteProcessConstants constants, final EventBusContext eventBusContext) {
        ACTIONS.add("Report a unit of this task");
    }
    
    @Override
    public void report(final String executionID, final ExecuteProcessConstants constants, final EventBusContext eventBusContext) {
        ACTIONS.add("Report this task on completion");
    }
    
    @Override
    public void reportClean(final String executionID) {
    }
    
    @Override
    public void reportRemove(final String executionID) {
    }
}
