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

import org.apache.shardingsphere.distsql.parser.statement.ral.queryable.ShowInstanceInfoStatement;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstance;
import org.apache.shardingsphere.infra.instance.metadata.InstanceMetaData;
import org.apache.shardingsphere.infra.instance.metadata.proxy.ProxyInstanceMetaData;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.QueryableRALBackendHandler;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * Show instance info handler.
 */
public final class ShowInstanceInfoHandler extends QueryableRALBackendHandler<ShowInstanceInfoStatement> {
    
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
        ComputeNodeInstance instance = contextManager.getInstanceContext().getInstance();
        InstanceMetaData instanceMetaData = instance.getMetaData();
        String modeType = contextManager.getInstanceContext().getModeConfiguration().getType();
        return Collections.singletonList(new LocalDataQueryResultRow(instanceMetaData.getId(), instanceMetaData.getIp(),
                instanceMetaData instanceof ProxyInstanceMetaData ? ((ProxyInstanceMetaData) instanceMetaData).getPort() : -1,
                instance.getState().getCurrentState().name(), modeType, instance.getWorkerId(), String.join(",", instance.getLabels())));
    }
}
