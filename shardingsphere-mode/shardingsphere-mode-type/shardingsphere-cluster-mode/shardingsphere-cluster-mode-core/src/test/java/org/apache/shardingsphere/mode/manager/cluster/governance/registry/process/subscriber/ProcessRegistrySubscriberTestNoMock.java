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

package org.apache.shardingsphere.mode.manager.cluster.governance.registry.process.subscriber;

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
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.apache.shardingsphere.mode.manager.cluster.governance.registry.process.ProcessListClusterPersistRepositoryFixture;
import org.apache.shardingsphere.mode.manager.cluster.governance.registry.process.event.ExecuteProcessReportEvent;
import org.apache.shardingsphere.mode.manager.cluster.governance.registry.process.event.ExecuteProcessSummaryReportEvent;
import org.apache.shardingsphere.mode.manager.cluster.governance.registry.process.event.ExecuteProcessUnitReportEvent;
import org.apache.shardingsphere.mode.manager.cluster.governance.registry.process.node.ProcessNode;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public final class ProcessRegistrySubscriberTestNoMock {
    
    private final ClusterPersistRepository repository = new ProcessListClusterPersistRepositoryFixture();
    
    private final ProcessRegistrySubscriber subscriber = new ProcessRegistrySubscriber(repository);
    
    private final ExecuteProcessContext executeProcessContext;
    
    private final ExecutionUnit executionUnit;
    
    public ProcessRegistrySubscriberTestNoMock() {
        ExecutionUnit executionUnit = new ExecutionUnit("ds_0", new SQLUnit("sql1_0", Collections.emptyList()));
        this.executionUnit = executionUnit;
        executeProcessContext = createExecuteProcessContext(executionUnit);
    }
    
    private ExecuteProcessContext createExecuteProcessContext(final ExecutionUnit executionUnit) {
        Collection<ExecutionGroup<JDBCExecutionUnit>> inputGroups = new ArrayList<>();
        inputGroups.add(new ExecutionGroup<>(Collections.singletonList(new JDBCExecutionUnit(executionUnit, ConnectionMode.MEMORY_STRICTLY, null))));
        ExecutionGroupContext<JDBCExecutionUnit> executionGroupContext = new ExecutionGroupContext<>(inputGroups);
        executionGroupContext.setSchemaName("sharding_db");
        executionGroupContext.setGrantee(new Grantee("sharding", "127.0.0.1"));
        return new ExecuteProcessContext("sql1", executionGroupContext, ExecuteProcessConstants.EXECUTE_STATUS_START);
    }
    
    @Test
    public void assertWholeProcessCompleted() {
        assertReportExecuteProcessSummary();
        ExecuteProcessConstants processConstants = ExecuteProcessConstants.EXECUTE_STATUS_DONE;
        assertReportExecuteProcessUnit(processConstants);
        assertReportExecuteProcess(processConstants);
    }
    
    @Test
    public void assertWholeProcessUncompleted() {
        assertReportExecuteProcessSummary();
        ExecuteProcessConstants processConstants = ExecuteProcessConstants.EXECUTE_STATUS_START;
        assertReportExecuteProcessUnit(processConstants);
        assertReportExecuteProcess(processConstants);
    }
    
    private void assertReportExecuteProcessSummary() {
        subscriber.reportExecuteProcessSummary(new ExecuteProcessSummaryReportEvent(executeProcessContext));
        String executionID = executeProcessContext.getExecutionID();
        String executeProcessText = repository.get(ProcessNode.getExecutionPath(executionID));
        assertNotNull(executeProcessText);
        YamlExecuteProcessContext yamlExecuteProcessContext = YamlEngine.unmarshal(executeProcessText, YamlExecuteProcessContext.class);
        assertThat(yamlExecuteProcessContext.getExecutionID(), is(executionID));
        assertNotNull(yamlExecuteProcessContext.getStartTimeMillis());
        assertThat(yamlExecuteProcessContext.getStartTimeMillis(), is(executeProcessContext.getStartTimeMillis()));
        assertThat(yamlExecuteProcessContext.getSchemaName(), is("sharding_db"));
        assertThat(yamlExecuteProcessContext.getUsername(), is("sharding"));
        assertThat(yamlExecuteProcessContext.getHostname(), is("127.0.0.1"));
        assertThat(yamlExecuteProcessContext.getSql(), is("sql1"));
        Collection<YamlExecuteProcessUnit> unitStatuses = yamlExecuteProcessContext.getUnitStatuses();
        assertThat(unitStatuses.size(), is(1));
        YamlExecuteProcessUnit yamlExecuteProcessUnit = unitStatuses.iterator().next();
        assertThat(yamlExecuteProcessUnit.getStatus(), is(ExecuteProcessConstants.EXECUTE_STATUS_START));
    }
    
    private void assertReportExecuteProcessUnit(final ExecuteProcessConstants processConstants) {
        String executionID = executeProcessContext.getExecutionID();
        ExecuteProcessUnitReportEvent event = new ExecuteProcessUnitReportEvent(executionID, new ExecuteProcessUnit(executionUnit, processConstants));
        subscriber.reportExecuteProcessUnit(event);
        String executeProcessText = repository.get(ProcessNode.getExecutionPath(executionID));
        assertNotNull(executeProcessText);
        YamlExecuteProcessContext yamlExecuteProcessContext = YamlEngine.unmarshal(executeProcessText, YamlExecuteProcessContext.class);
        assertThat(yamlExecuteProcessContext.getExecutionID(), is(executionID));
        YamlExecuteProcessUnit yamlExecuteProcessUnit = yamlExecuteProcessContext.getUnitStatuses().iterator().next();
        assertThat(yamlExecuteProcessUnit.getStatus(), is(processConstants));
    }
    
    private void assertReportExecuteProcess(final ExecuteProcessConstants processConstants) {
        String executionID = executeProcessContext.getExecutionID();
        ExecuteProcessReportEvent event = new ExecuteProcessReportEvent(executionID);
        subscriber.reportExecuteProcess(event);
        String executeProcessText = repository.get(ProcessNode.getExecutionPath(executionID));
        if (ExecuteProcessConstants.EXECUTE_STATUS_DONE == processConstants) {
            assertNull(executeProcessText);
        } else {
            assertNotNull(executeProcessText);
            YamlExecuteProcessContext yamlExecuteProcessContext = YamlEngine.unmarshal(executeProcessText, YamlExecuteProcessContext.class);
            assertThat(yamlExecuteProcessContext.getExecutionID(), is(executionID));
            YamlExecuteProcessUnit yamlExecuteProcessUnit = yamlExecuteProcessContext.getUnitStatuses().iterator().next();
            assertThat(yamlExecuteProcessUnit.getStatus(), is(processConstants));
        }
    }
}
