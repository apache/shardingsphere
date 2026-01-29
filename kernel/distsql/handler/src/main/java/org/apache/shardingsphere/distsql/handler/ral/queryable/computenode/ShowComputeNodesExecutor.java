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

package org.apache.shardingsphere.distsql.handler.ral.queryable.computenode;

import org.apache.shardingsphere.distsql.handler.engine.query.DistSQLQueryExecutor;
import org.apache.shardingsphere.distsql.statement.type.ral.queryable.show.ShowComputeNodesStatement;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstance;
import org.apache.shardingsphere.infra.instance.metadata.InstanceMetaData;
import org.apache.shardingsphere.infra.instance.metadata.proxy.ProxyInstanceMetaData;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.mode.manager.ContextManager;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Show compute nodes executor.
 */
public final class ShowComputeNodesExecutor implements DistSQLQueryExecutor<ShowComputeNodesStatement> {
    
    @Override
    public Collection<String> getColumnNames(final ShowComputeNodesStatement sqlStatement) {
        return Arrays.asList("instance_id", "instance_type", "host", "port", "status", "mode_type", "worker_id", "labels", "version", "database_name");
    }
    
    @Override
    public Collection<LocalDataQueryResultRow> getRows(final ShowComputeNodesStatement sqlStatement, final ContextManager contextManager) {
        String modeType = contextManager.getComputeNodeInstanceContext().getModeConfiguration().getType();
        Collection<ComputeNodeInstance> instances = contextManager.getPersistServiceFacade().getModeFacade().getComputeNodeService().loadAllInstances();
        return instances.stream().map(each -> buildRow(each, modeType)).collect(Collectors.toList());
    }
    
    private LocalDataQueryResultRow buildRow(final ComputeNodeInstance instance, final String modeType) {
        String labels = String.join(",", instance.getLabels());
        InstanceMetaData instanceMetaData = instance.getMetaData();
        return new LocalDataQueryResultRow(instanceMetaData.getId(), instanceMetaData.getType(), instanceMetaData.getIp(),
                instanceMetaData instanceof ProxyInstanceMetaData ? ((ProxyInstanceMetaData) instanceMetaData).getPort() : -1,
                instance.getState().getCurrentState(), modeType, instance.getWorkerId(), labels, instanceMetaData.getVersion(), instanceMetaData.getDatabaseName());
    }
    
    @Override
    public Class<ShowComputeNodesStatement> getType() {
        return ShowComputeNodesStatement.class;
    }
}
