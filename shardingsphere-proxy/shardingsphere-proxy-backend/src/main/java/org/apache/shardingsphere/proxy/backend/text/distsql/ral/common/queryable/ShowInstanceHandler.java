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

package org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.queryable;

import com.google.common.base.Joiner;
import org.apache.shardingsphere.distsql.parser.statement.ral.common.queryable.ShowInstanceStatement;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstance;
import org.apache.shardingsphere.infra.instance.definition.InstanceDefinition;
import org.apache.shardingsphere.infra.instance.definition.InstanceType;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.QueryableRALBackendHandler;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Show instance handler.
 */
public final class ShowInstanceHandler extends QueryableRALBackendHandler<ShowInstanceStatement, ShowInstanceHandler> {
    
    private static final String DELIMITER = "@";
    
    private static final String ID = "instance_id";
    
    private static final String HOST = "host";
    
    private static final String PORT = "port";
    
    private static final String STATUS = "status";
    
    private static final String MODE_TYPE = "mode_type";
    
    private static final String LABELS = "labels";
    
    private static final String XA_RECOVERY_NODES = "xa_recovery_nodes";
    
    @Override
    protected Collection<String> getColumnNames() {
        return Arrays.asList(ID, HOST, PORT, STATUS, MODE_TYPE, LABELS, XA_RECOVERY_NODES);
    }
    
    @Override
    protected Collection<List<Object>> getRows(final ContextManager contextManager) {
        return buildInstanceRows(contextManager);
    }
    
    private Collection<List<Object>> buildInstanceRows(final ContextManager contextManager) {
        String modeType = contextManager.getInstanceContext().getModeConfiguration().getType();
        if ("Memory".equalsIgnoreCase(modeType) || "Standalone".equalsIgnoreCase(modeType)) {
            return Collections.singletonList(buildRow(contextManager.getInstanceContext().getInstance(), modeType));
        }
        Collection<ComputeNodeInstance> instances = contextManager.getInstanceContext().getComputeNodeInstances().stream()
                .filter(each -> InstanceType.PROXY.equals(each.getInstanceDefinition().getInstanceType())).collect(Collectors.toList());
        return instances.isEmpty() ? Collections.emptyList()
                : instances.stream().filter(Objects::nonNull).map(each -> buildRow(each, modeType)).collect(Collectors.toList());
    }
    
    private List<Object> buildRow(final ComputeNodeInstance instance, final String modeType) {
        return buildRow(instance.getInstanceDefinition(), instance.getState().getCurrentState().name(), modeType, instance.getLabels(), Joiner.on(",").join(instance.getXaRecoveryIds()));
    }
    
    private List<Object> buildRow(final InstanceDefinition instanceDefinition, final String status, final String modeType, final Collection<String> instanceLabels, final String xaRecoveryId) {
        String host = instanceDefinition.getIp();
        String port = instanceDefinition.getUniqueSign();
        String labels = null == instanceLabels ? "" : String.join(",", instanceLabels);
        return new LinkedList<>(Arrays.asList(instanceDefinition.getInstanceId(), host, port, status, modeType, labels, null == xaRecoveryId ? "" : xaRecoveryId));
    }
}
