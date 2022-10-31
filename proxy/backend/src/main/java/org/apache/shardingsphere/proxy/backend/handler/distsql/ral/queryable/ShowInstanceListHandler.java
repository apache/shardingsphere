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

import org.apache.shardingsphere.distsql.parser.statement.ral.queryable.ShowInstanceListStatement;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstance;
import org.apache.shardingsphere.infra.instance.metadata.InstanceMetaData;
import org.apache.shardingsphere.infra.instance.metadata.InstanceType;
import org.apache.shardingsphere.infra.instance.metadata.proxy.ProxyInstanceMetaData;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.QueryableRALBackendHandler;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Show instance list handler.
 */
public final class ShowInstanceListHandler extends QueryableRALBackendHandler<ShowInstanceListStatement> {
    
    private static final String ID = "instance_id";
    
    private static final String HOST = "host";
    
    private static final String PORT = "port";
    
    private static final String STATUS = "status";
    
    private static final String MODE_TYPE = "mode_type";
    
    private static final String WORKER_ID = "worker_id";
    
    private static final String LABELS = "labels";
    
    @Override
    protected Collection<String> getColumnNames() {
        return Arrays.asList(ID, HOST, PORT, STATUS, MODE_TYPE, WORKER_ID, LABELS);
    }
    
    @Override
    protected Collection<LocalDataQueryResultRow> getRows(final ContextManager contextManager) {
        String modeType = contextManager.getInstanceContext().getModeConfiguration().getType();
        if ("Standalone".equalsIgnoreCase(modeType)) {
            return Collections.singletonList(buildRow(contextManager.getInstanceContext().getInstance(), modeType));
        }
        Collection<ComputeNodeInstance> instances = contextManager.getInstanceContext().getAllClusterInstances().stream()
                .filter(each -> InstanceType.PROXY == each.getMetaData().getType()).collect(Collectors.toList());
        return instances.isEmpty() ? Collections.emptyList() : instances.stream().filter(Objects::nonNull).map(each -> buildRow(each, modeType)).collect(Collectors.toList());
    }
    
    private LocalDataQueryResultRow buildRow(final ComputeNodeInstance instance, final String modeType) {
        String labels = String.join(",", instance.getLabels());
        InstanceMetaData instanceMetaData = instance.getMetaData();
        return new LocalDataQueryResultRow(instanceMetaData.getId(), instanceMetaData.getIp(),
                instanceMetaData instanceof ProxyInstanceMetaData ? ((ProxyInstanceMetaData) instanceMetaData).getPort() : -1,
                instance.getState().getCurrentState().name(), modeType, instance.getWorkerId(), labels);
    }
}
