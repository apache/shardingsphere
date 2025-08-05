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

package org.apache.shardingsphere.proxy.backend.handler.distsql.ral.queryable.computenode;

import org.apache.shardingsphere.distsql.handler.engine.query.DistSQLQueryExecutor;
import org.apache.shardingsphere.distsql.statement.type.ral.queryable.show.ShowComputeNodeInfoStatement;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstance;
import org.apache.shardingsphere.infra.instance.metadata.InstanceMetaData;
import org.apache.shardingsphere.infra.instance.metadata.proxy.ProxyInstanceMetaData;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.mode.manager.ContextManager;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * Show compute node info executor.
 */
public final class ShowComputeNodeInfoExecutor implements DistSQLQueryExecutor<ShowComputeNodeInfoStatement> {
    
    @Override
    public Collection<String> getColumnNames(final ShowComputeNodeInfoStatement sqlStatement) {
        return Arrays.asList("instance_id", "host", "port", "status", "mode_type", "worker_id", "labels", "version");
    }
    
    @Override
    public Collection<LocalDataQueryResultRow> getRows(final ShowComputeNodeInfoStatement sqlStatement, final ContextManager contextManager) {
        ComputeNodeInstance instance = contextManager.getComputeNodeInstanceContext().getInstance();
        InstanceMetaData instanceMetaData = instance.getMetaData();
        String modeType = contextManager.getComputeNodeInstanceContext().getModeConfiguration().getType();
        return Collections.singletonList(new LocalDataQueryResultRow(instanceMetaData.getId(), instanceMetaData.getIp(),
                instanceMetaData instanceof ProxyInstanceMetaData ? ((ProxyInstanceMetaData) instanceMetaData).getPort() : -1,
                instance.getState().getCurrentState(), modeType, instance.getWorkerId(), String.join(",", instance.getLabels()),
                instanceMetaData.getVersion()));
    }
    
    @Override
    public Class<ShowComputeNodeInfoStatement> getType() {
        return ShowComputeNodeInfoStatement.class;
    }
}
