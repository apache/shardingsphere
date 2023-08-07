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

package org.apache.shardingsphere.proxy.backend.handler.distsql.ral.queryable;

import org.apache.shardingsphere.distsql.handler.ral.query.InstanceContextRequiredQueryableRALExecutor;
import org.apache.shardingsphere.distsql.parser.statement.ral.queryable.ShowComputeNodesStatement;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstance;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.instance.metadata.InstanceMetaData;
import org.apache.shardingsphere.infra.instance.metadata.InstanceType;
import org.apache.shardingsphere.infra.instance.metadata.proxy.ProxyInstanceMetaData;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Show compute nodes executor.
 */
public final class ShowComputeNodesExecutor implements InstanceContextRequiredQueryableRALExecutor<ShowComputeNodesStatement> {
    
    @Override
    public Collection<String> getColumnNames() {
        return Arrays.asList("instance_id", "host", "port", "status", "mode_type", "worker_id", "labels", "version");
    }
    
    @Override
    public Collection<LocalDataQueryResultRow> getRows(final InstanceContext instanceContext, final ShowComputeNodesStatement sqlStatement) {
        String modeType = instanceContext.getModeConfiguration().getType();
        if ("Standalone".equals(modeType)) {
            return Collections.singleton(buildRow(instanceContext.getInstance(), modeType));
        }
        Collection<ComputeNodeInstance> instances = instanceContext.getAllClusterInstances().stream()
                .filter(each -> InstanceType.PROXY == each.getMetaData().getType()).collect(Collectors.toList());
        return instances.isEmpty() ? Collections.emptyList() : instances.stream().filter(Objects::nonNull).map(each -> buildRow(each, modeType)).collect(Collectors.toList());
    }
    
    private LocalDataQueryResultRow buildRow(final ComputeNodeInstance instance, final String modeType) {
        String labels = String.join(",", instance.getLabels());
        InstanceMetaData instanceMetaData = instance.getMetaData();
        return new LocalDataQueryResultRow(instanceMetaData.getId(), instanceMetaData.getIp(),
                instanceMetaData instanceof ProxyInstanceMetaData ? ((ProxyInstanceMetaData) instanceMetaData).getPort() : -1,
                instance.getState().getCurrentState().name(), modeType, instance.getWorkerId(), labels, instanceMetaData.getVersion());
    }
    
    @Override
    public Class<ShowComputeNodesStatement> getType() {
        return ShowComputeNodesStatement.class;
    }
}
