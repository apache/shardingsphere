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

package org.apache.shardingsphere.mode.manager.cluster.process;

import org.apache.shardingsphere.infra.binder.LogicSQL;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroupContext;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.SQLExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.process.model.ExecuteProcessConstants;
import org.apache.shardingsphere.infra.executor.sql.process.model.ExecuteProcessContext;
import org.junit.Test;

import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class GovernanceExecuteProcessReporterTest {
    
    @Test
    public void assertReport() {
        GovernanceExecuteProcessReporterSubscriberFixture subscriber = new GovernanceExecuteProcessReporterSubscriberFixture();
        LogicSQL logicSQL = new LogicSQL(null, null, null);
        ExecutionGroupContext<? extends SQLExecutionUnit> executionGroupContext = mockExecutionGroupContext();
        ExecuteProcessContext executeProcessContext = new ExecuteProcessContext(logicSQL.getSql(), executionGroupContext, ExecuteProcessConstants.EXECUTE_ID);
        GovernanceExecuteProcessReporter reporter = new GovernanceExecuteProcessReporter();
        reporter.report(logicSQL, executionGroupContext, ExecuteProcessConstants.EXECUTE_ID);
        assertThat(subscriber.getValue(), is(executeProcessContext.getExecutionID()));
    }
    
    @SuppressWarnings("unchecked")
    private ExecutionGroupContext<? extends SQLExecutionUnit> mockExecutionGroupContext() {
        ExecutionGroupContext<? extends SQLExecutionUnit> result = mock(ExecutionGroupContext.class);
        when(result.getExecutionID()).thenReturn(UUID.randomUUID().toString());
        return result;
    }
}
