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
import org.apache.shardingsphere.infra.executor.sql.process.model.ExecuteProcessReportContext;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.process.event.ExecuteProcessReportEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.process.event.ExecuteProcessSummaryReportEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.process.event.ExecuteProcessUnitReportEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.process.event.ShowProcessListRequestEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.process.event.ShowProcessListResponseEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.process.executor.ProcessThreadExecutorGroup;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.process.node.ProcessNode;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;
import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;
import org.apache.shardingsphere.infra.executor.sql.process.model.ExecuteProcessConstants;
import org.apache.shardingsphere.infra.executor.sql.process.model.ExecuteProcessUnit;
import org.apache.shardingsphere.infra.executor.sql.process.model.yaml.YamlExecuteProcessContext;
import org.apache.shardingsphere.infra.executor.sql.process.model.yaml.YamlExecuteProcessUnit;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;

import java.util.Collection;
import java.util.List;
import java.util.Map;
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
    @Subscribe
    public void loadShowProcessListData(final ShowProcessListRequestEvent event) {
        List<String> childrenKeys = repository.getChildrenKeys(ProcessNode.getExecutionNodesPath());
        Collection<String> processListData = childrenKeys.stream().map(key -> repository.get(ProcessNode.getExecutionPath(key))).collect(Collectors.toList());
        ShardingSphereEventBus.getInstance().post(new ShowProcessListResponseEvent(processListData));
    }
    
    /**
     * Report execute process summary.
     *
     * @param event execute process summary report event.
     */
    @Subscribe
    public void reportExecuteProcessSummary(final ExecuteProcessSummaryReportEvent event) {
        String executionID = event.getExecutionID();
        final int showProcessListAsyncThreadNumber;
        final String processContext;
        synchronized (executionID) {
            Map<String, Object> dataMap = event.getDataMap();
            ExecuteProcessReportContext reportContext = (ExecuteProcessReportContext) dataMap.get(ExecuteProcessConstants.EXECUTE_ID.name());
            if (null == reportContext) {
                return;
            }
            YamlExecuteProcessContext yamlExecuteProcessContext = reportContext.getYamlExecuteProcessContext();
            processContext = YamlEngine.marshal(yamlExecuteProcessContext);
            showProcessListAsyncThreadNumber = reportContext.getShowProcessListAsyncThreadNumber();
        }
        ProcessThreadExecutorGroup.getInstance().get(executionID, showProcessListAsyncThreadNumber)
                .submit(() -> repository.persist(ProcessNode.getExecutionPath(executionID), processContext));
    }
    
    /**
     * Report execute process unit.
     *
     * @param event execute process unit report event.
     */
    @Subscribe
    public void reportExecuteProcessUnit(final ExecuteProcessUnitReportEvent event) {
        String executionID = event.getExecutionID();
        final int showProcessListAsyncThreadNumber;
        final String processContext;
        synchronized (executionID) {
            Map<String, Object> dataMap = event.getDataMap();
            ExecuteProcessReportContext reportContext = (ExecuteProcessReportContext) dataMap.get(ExecuteProcessConstants.EXECUTE_ID.name());
            if (null == reportContext) {
                return;
            }
            YamlExecuteProcessContext yamlExecuteProcessContext = reportContext.getYamlExecuteProcessContext();
            ExecuteProcessUnit executeProcessUnit = event.getExecuteProcessUnit();
            for (YamlExecuteProcessUnit unit : yamlExecuteProcessContext.getUnitStatuses()) {
                if (unit.getUnitID().equals(executeProcessUnit.getUnitID())) {
                    unit.setStatus(executeProcessUnit.getStatus());
                }
            }
            processContext = YamlEngine.marshal(yamlExecuteProcessContext);
            showProcessListAsyncThreadNumber = reportContext.getShowProcessListAsyncThreadNumber();
        }
        ProcessThreadExecutorGroup.getInstance().get(executionID, showProcessListAsyncThreadNumber)
                .submit(() -> repository.persist(ProcessNode.getExecutionPath(executionID), processContext));
    }
    
    /**
     * Report execute process.
     *
     * @param event execute process report event.
     */
    @Subscribe
    public void reportExecuteProcess(final ExecuteProcessReportEvent event) {
        String executionID = event.getExecutionID();
        final int showProcessListAsyncThreadNumber;
        synchronized (executionID) {
            Map<String, Object> dataMap = event.getDataMap();
            ExecuteProcessReportContext reportContext = (ExecuteProcessReportContext) dataMap.get(ExecuteProcessConstants.EXECUTE_ID.name());
            YamlExecuteProcessContext yamlExecuteProcessContext = reportContext.getYamlExecuteProcessContext();
            for (YamlExecuteProcessUnit unit : yamlExecuteProcessContext.getUnitStatuses()) {
                if (unit.getStatus() != ExecuteProcessConstants.EXECUTE_STATUS_DONE) {
                    return;
                }
            }
            showProcessListAsyncThreadNumber = reportContext.getShowProcessListAsyncThreadNumber();
        }
        ProcessThreadExecutorGroup.getInstance().get(executionID, showProcessListAsyncThreadNumber)
                .submit(() -> repository.delete(ProcessNode.getExecutionPath(executionID)));
    }
}
