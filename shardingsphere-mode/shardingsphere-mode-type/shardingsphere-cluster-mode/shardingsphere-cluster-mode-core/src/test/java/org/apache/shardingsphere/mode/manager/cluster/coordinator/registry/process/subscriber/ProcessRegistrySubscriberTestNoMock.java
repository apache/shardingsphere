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

package org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.process.subscriber;

import org.apache.shardingsphere.infra.eventbus.EventBusContext;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroup;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroupContext;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.context.SQLUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.ConnectionMode;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.process.model.ExecuteProcessConstants;
import org.apache.shardingsphere.infra.executor.sql.process.model.ExecuteProcessContext;
import org.apache.shardingsphere.infra.executor.sql.process.model.ExecuteProcessUnit;
import org.apache.shardingsphere.infra.executor.sql.process.model.yaml.YamlExecuteProcessContext;
import org.apache.shardingsphere.infra.executor.sql.process.model.yaml.YamlExecuteProcessUnit;
import org.apache.shardingsphere.infra.metadata.user.Grantee;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.process.ProcessListClusterPersistRepositoryFixture;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.process.ShowProcessListManager;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.process.event.ExecuteProcessReportEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.process.event.ExecuteProcessSummaryReportEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.process.event.ExecuteProcessUnitReportEvent;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public final class ProcessRegistrySubscriberTestNoMock {
    
    private final ClusterPersistRepository repository = new ProcessListClusterPersistRepositoryFixture();
    
    private final ProcessRegistrySubscriber subscriber = new ProcessRegistrySubscriber(repository, new EventBusContext());
    
    private ExecuteProcessContext createExecuteProcessContext() {
        ExecutionUnit executionUnit = createExecuteUnit();
        Collection<ExecutionGroup<JDBCExecutionUnit>> inputGroups = new LinkedList<>();
        inputGroups.add(new ExecutionGroup<>(Collections.singletonList(new JDBCExecutionUnit(executionUnit, ConnectionMode.MEMORY_STRICTLY, null))));
        ExecutionGroupContext<JDBCExecutionUnit> executionGroupContext = new ExecutionGroupContext<>(inputGroups);
        executionGroupContext.setDatabaseName("sharding_db");
        executionGroupContext.setGrantee(new Grantee("sharding", "127.0.0.1"));
        return new ExecuteProcessContext("sql1", executionGroupContext, ExecuteProcessConstants.EXECUTE_STATUS_START);
    }
    
    private ExecutionUnit createExecuteUnit() {
        return new ExecutionUnit("ds_0", new SQLUnit("sql1_0", Collections.emptyList()));
    }
    
    @Test
    public void assertWholeProcessCompleted() {
        ExecuteProcessContext executeProcessContext = createExecuteProcessContext();
        assertReportExecuteProcessSummary(executeProcessContext);
        ExecuteProcessConstants processConstants = ExecuteProcessConstants.EXECUTE_STATUS_DONE;
        assertReportExecuteProcessUnit(processConstants, executeProcessContext.getExecutionID());
        assertReportExecuteProcess(processConstants, executeProcessContext.getExecutionID());
    }
    
    @Test
    public void assertWholeProcessUncompleted() {
        ExecuteProcessContext executeProcessContext = createExecuteProcessContext();
        assertReportExecuteProcessSummary(executeProcessContext);
        ExecuteProcessConstants processConstants = ExecuteProcessConstants.EXECUTE_STATUS_START;
        assertReportExecuteProcessUnit(processConstants, executeProcessContext.getExecutionID());
        assertReportExecuteProcess(processConstants, executeProcessContext.getExecutionID());
    }
    
    private void assertReportExecuteProcessSummary(final ExecuteProcessContext executeProcessContext) {
        subscriber.reportExecuteProcessSummary(new ExecuteProcessSummaryReportEvent(executeProcessContext));
        String executionID = executeProcessContext.getExecutionID();
        YamlExecuteProcessContext yamlExecuteProcessContext = ShowProcessListManager.getInstance().getProcessContext(executionID);
        assertThat(yamlExecuteProcessContext.getExecutionID(), is(executionID));
        assertThat(yamlExecuteProcessContext.getStartTimeMillis(), is(executeProcessContext.getStartTimeMillis()));
        assertThat(yamlExecuteProcessContext.getDatabaseName(), is("sharding_db"));
        assertThat(yamlExecuteProcessContext.getUsername(), is("sharding"));
        assertThat(yamlExecuteProcessContext.getHostname(), is("127.0.0.1"));
        assertThat(yamlExecuteProcessContext.getSql(), is("sql1"));
        Collection<YamlExecuteProcessUnit> unitStatuses = yamlExecuteProcessContext.getUnitStatuses();
        assertThat(unitStatuses.size(), is(1));
        YamlExecuteProcessUnit yamlExecuteProcessUnit = unitStatuses.iterator().next();
        assertThat(yamlExecuteProcessUnit.getStatus(), is(ExecuteProcessConstants.EXECUTE_STATUS_START));
    }
    
    private void assertReportExecuteProcessUnit(final ExecuteProcessConstants processConstants, final String executionID) {
        ExecuteProcessUnitReportEvent event = new ExecuteProcessUnitReportEvent(executionID, new ExecuteProcessUnit(createExecuteUnit(), processConstants));
        subscriber.reportExecuteProcessUnit(event);
        YamlExecuteProcessContext yamlExecuteProcessContext = ShowProcessListManager.getInstance().getProcessContext(executionID);
        assertThat(yamlExecuteProcessContext.getExecutionID(), is(executionID));
        YamlExecuteProcessUnit yamlExecuteProcessUnit = yamlExecuteProcessContext.getUnitStatuses().iterator().next();
        assertThat(yamlExecuteProcessUnit.getStatus(), is(processConstants));
    }
    
    private void assertReportExecuteProcess(final ExecuteProcessConstants processConstants, final String executionID) {
        ExecuteProcessReportEvent event = new ExecuteProcessReportEvent(executionID);
        subscriber.reportExecuteProcess(event);
        YamlExecuteProcessContext yamlExecuteProcessContext = ShowProcessListManager.getInstance().getProcessContext(executionID);
        if (ExecuteProcessConstants.EXECUTE_STATUS_DONE == processConstants) {
            assertNull(yamlExecuteProcessContext);
        } else {
            assertThat(yamlExecuteProcessContext.getExecutionID(), is(executionID));
            YamlExecuteProcessUnit yamlExecuteProcessUnit = yamlExecuteProcessContext.getUnitStatuses().iterator().next();
            assertThat(yamlExecuteProcessUnit.getStatus(), is(processConstants));
        }
    }
}
