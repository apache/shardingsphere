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

package org.apache.shardingsphere.governance.core.registry.process.subscriber;

import com.google.common.eventbus.Subscribe;
import java.util.Map;
import org.apache.shardingsphere.governance.core.registry.process.event.ExecuteProcessReportEvent;
import org.apache.shardingsphere.governance.core.registry.process.event.ExecuteProcessSummaryReportEvent;
import org.apache.shardingsphere.governance.core.registry.process.event.ExecuteProcessUnitReportEvent;
import org.apache.shardingsphere.governance.core.registry.process.event.ShowProcessListRequestEvent;
import org.apache.shardingsphere.governance.core.registry.process.event.ShowProcessListResponseEvent;
import org.apache.shardingsphere.governance.core.registry.process.node.ProcessNode;
import org.apache.shardingsphere.governance.repository.spi.RegistryCenterRepository;
import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;
import org.apache.shardingsphere.infra.executor.sql.process.model.ExecuteProcessConstants;
import org.apache.shardingsphere.infra.executor.sql.process.model.ExecuteProcessReportContext;
import org.apache.shardingsphere.infra.executor.sql.process.model.ExecuteProcessUnit;
import org.apache.shardingsphere.infra.executor.sql.process.model.yaml.YamlExecuteProcessContext;
import org.apache.shardingsphere.infra.executor.sql.process.model.yaml.YamlExecuteProcessUnit;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Process registry subscriber.
 */
public final class ProcessRegistrySubscriber {
    
    private final RegistryCenterRepository repository;
    
    public ProcessRegistrySubscriber(final RegistryCenterRepository repository) {
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
        synchronized (executionID) {
            Map<String, Object> dataMap = event.getDataMap();
            ExecuteProcessReportContext reportContext = (ExecuteProcessReportContext) dataMap.get(ExecuteProcessConstants.EXECUTE_ID.name());
            if (null == reportContext) {
                return;
            }
            YamlExecuteProcessContext yamlExecuteProcessContext = reportContext.getYamlExecuteProcessContext();
            if (System.currentTimeMillis() - yamlExecuteProcessContext.getStartTimeMillis() <= reportContext.getShowProcessListNoReportThresholdMillis()) {
                return;
            }
            repository.persist(ProcessNode.getExecutionPath(executionID), YamlEngine.marshal(yamlExecuteProcessContext));
            reportContext.setReportToGovernanceDonePartially(true);
        }
    }
    
    /**
     * Report execute process unit.
     *
     * @param event execute process unit report event.
     */
    @Subscribe
    public void reportExecuteProcessUnit(final ExecuteProcessUnitReportEvent event) {
        String executionID = event.getExecutionID();
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
            if (System.currentTimeMillis() - yamlExecuteProcessContext.getStartTimeMillis() <= reportContext.getShowProcessListNoReportThresholdMillis()
                    && !reportContext.isReportToGovernanceDonePartially()) {
                return;
            }
            String executionPath = ProcessNode.getExecutionPath(executionID);
            repository.persist(executionPath, YamlEngine.marshal(yamlExecuteProcessContext));
            reportContext.setReportToGovernanceDonePartially(true);
        }
    }
    
    /**
     * Report execute process.
     *
     * @param event execute process report event.
     */
    @Subscribe
    public void reportExecuteProcess(final ExecuteProcessReportEvent event) {
        String executionID = event.getExecutionID();
        synchronized (executionID) {
            Map<String, Object> dataMap = event.getDataMap();
            ExecuteProcessReportContext reportContext = (ExecuteProcessReportContext) dataMap.get(ExecuteProcessConstants.EXECUTE_ID.name());
            YamlExecuteProcessContext yamlExecuteProcessContext = reportContext.getYamlExecuteProcessContext();
            for (YamlExecuteProcessUnit unit : yamlExecuteProcessContext.getUnitStatuses()) {
                if (unit.getStatus() != ExecuteProcessConstants.EXECUTE_STATUS_DONE) {
                    return;
                }
            }
            if (!reportContext.isReportToGovernanceDonePartially()) {
                return;
            }
            String executionPath = ProcessNode.getExecutionPath(executionID);
            repository.delete(executionPath);
        }
    }
}
