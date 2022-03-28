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

import com.google.common.eventbus.Subscribe;
import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstance;
import org.apache.shardingsphere.infra.instance.definition.InstanceDefinition;
import org.apache.shardingsphere.infra.instance.definition.InstanceType;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.process.ProcessHolder;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.process.event.ExecuteProcessReportEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.process.event.ExecuteProcessSummaryReportEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.process.event.ExecuteProcessUnitReportEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.process.event.ShowProcessListRequestEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.process.event.ShowProcessListResponseEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.process.node.ProcessNode;
import org.apache.shardingsphere.mode.metadata.persist.node.ComputeNode;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;
import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;
import org.apache.shardingsphere.infra.executor.sql.process.model.ExecuteProcessConstants;
import org.apache.shardingsphere.infra.executor.sql.process.model.ExecuteProcessContext;
import org.apache.shardingsphere.infra.executor.sql.process.model.ExecuteProcessUnit;
import org.apache.shardingsphere.infra.executor.sql.process.model.yaml.YamlExecuteProcessContext;
import org.apache.shardingsphere.infra.executor.sql.process.model.yaml.YamlExecuteProcessUnit;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Process registry subscriber.
 */
public final class ProcessRegistrySubscriber {
    
    private final ClusterPersistRepository repository;
    
    public ProcessRegistrySubscriber(final ClusterPersistRepository repository) {
        this.repository = repository;
        ShardingSphereEventBus.getInstance().register(this);
    }
    
    /**
     * Load show process list data.
     *
     * @param event get children request event.
     */
    @SneakyThrows
    @Subscribe
    public void loadShowProcessListData(final ShowProcessListRequestEvent event) {
        Collection<String> onlineComputeNodes = repository.getChildrenKeys(ComputeNode.getOnlineNodePath(InstanceType.JDBC));
        onlineComputeNodes.forEach(each -> repository.persist(ComputeNode.getOnlineProcessTriggerNodePath(each, InstanceType.JDBC), "ON"));
        Collection<String> proxyOnlineComputeNodes = repository.getChildrenKeys(ComputeNode.getOnlineNodePath(InstanceType.PROXY));
        proxyOnlineComputeNodes.forEach(each -> repository.persist(ComputeNode.getOnlineProcessTriggerNodePath(each, InstanceType.PROXY), "ON"));
        // loop until trigger is deleted
        out : while(true) {
            Thread.sleep(10);
            for(String each : onlineComputeNodes) {
                String value = repository.get(ComputeNode.getOnlineInstanceNodePath(each, InstanceType.JDBC));
                if ("process_trigger".equalsIgnoreCase(value)) {
                    continue out;
                }
            }
            for(String each : proxyOnlineComputeNodes) {
                String value = repository.get(ComputeNode.getOnlineInstanceNodePath(each, InstanceType.PROXY));
                if ("process_trigger".equalsIgnoreCase(value)) {
                    continue out;
                }
            }
           break;
        }
        List<String> childrenKeys = repository.getChildrenKeys(ProcessNode.getExecutionNodesPath());
        Collection<String> processListData = childrenKeys.stream().map(key -> repository.get(ProcessNode.getExecutionPath(key))).collect(Collectors.toList());
        ShardingSphereEventBus.getInstance().post(new ShowProcessListResponseEvent(processListData));
        repository.delete(ProcessNode.getExecutionNodesPath());
    }
    
    /**
     * Report execute process summary.
     *
     * @param event execute process summary report event.
     */
    @Subscribe
    public void reportExecuteProcessSummary(final ExecuteProcessSummaryReportEvent event) {
        ExecuteProcessContext executeProcessContext = event.getExecuteProcessContext();
        ProcessHolder.getInstance().put(executeProcessContext.getExecutionID(), new YamlExecuteProcessContext(executeProcessContext));
    }
    
    /**
     * Report execute process unit.
     *
     * @param event execute process unit report event.
     */
    @Subscribe
    public void reportExecuteProcessUnit(final ExecuteProcessUnitReportEvent event) {
        String executionID = event.getExecutionID();
        YamlExecuteProcessContext yamlExecuteProcessContext = ProcessHolder.getInstance().get(executionID);
        ExecuteProcessUnit executeProcessUnit = event.getExecuteProcessUnit();
        for (YamlExecuteProcessUnit unit : yamlExecuteProcessContext.getUnitStatuses()) {
            if (unit.getUnitID().equals(executeProcessUnit.getUnitID())) {
                unit.setStatus(executeProcessUnit.getStatus());
            }
        }
    }
    
    /**
     * Report execute process.
     *
     * @param event execute process report event.
     */
    @Subscribe
    public void reportExecuteProcess(final ExecuteProcessReportEvent event) {
        YamlExecuteProcessContext yamlExecuteProcessContext = ProcessHolder.getInstance().get(event.getExecutionID());
        for (YamlExecuteProcessUnit unit : yamlExecuteProcessContext.getUnitStatuses()) {
            if (unit.getStatus() != ExecuteProcessConstants.EXECUTE_STATUS_DONE) {
                return;
            }
        }
        ProcessHolder.getInstance().remove(event.getExecutionID());
    }
}
