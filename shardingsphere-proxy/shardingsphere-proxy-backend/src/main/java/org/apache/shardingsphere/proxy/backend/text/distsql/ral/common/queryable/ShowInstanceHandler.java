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

import org.apache.shardingsphere.distsql.parser.statement.ral.common.queryable.ShowInstanceStatement;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstance;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.mode.repository.standalone.StandalonePersistRepository;
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
    
    private static final String DISABLED = "disabled";
    
    private static final String ENABLED = "enabled";
    
    @Override
    protected Collection<String> getColumnNames() {
        return Arrays.asList(ID, HOST, PORT, STATUS, MODE_TYPE, LABELS, XA_RECOVERY_NODES);
    }
    
    @Override
    protected Collection<List<Object>> getRows(final ContextManager contextManager) {
        MetaDataPersistService persistService = contextManager.getMetaDataContexts().getMetaDataPersistService().orElse(null);
        if (null == persistService || null == persistService.getRepository() || persistService.getRepository() instanceof StandalonePersistRepository) {
            return buildInstanceRows(contextManager.getInstanceContext());
        }
        return buildInstanceRows(contextManager.getInstanceContext(), persistService);
    }
    
    private Collection<List<Object>> buildInstanceRows(final InstanceContext instanceContext) {
        List<List<Object>> result = new LinkedList<>();
        result.add(buildRow(instanceContext.getInstance(), instanceContext.getModeConfiguration().getType()));
        return result;
    }
    
    private Collection<List<Object>> buildInstanceRows(final InstanceContext instanceContext, final MetaDataPersistService persistService) {
        Collection<ComputeNodeInstance> instances = persistService.getComputeNodePersistService().loadAllComputeNodeInstances();
        if (!instances.isEmpty()) {
            return instances.stream().filter(Objects::nonNull)
                    .map(each -> buildRow(each, instanceContext.getModeConfiguration().getType()))
                    .collect(Collectors.toCollection(LinkedList::new));
        }
        return Collections.emptyList();
    }
    
    private List<Object> buildRow(final ComputeNodeInstance instance, final String modeType) {
        return buildRow(instance.getInstanceDefinition().getInstanceId().getId(), instance.getState().getCurrentState().name(), modeType, instance.getLabels(), instance.getXaRecoveryId());
    }
    
    private List<Object> buildRow(final String instanceId, final String status, final String modeType, final Collection<String> instanceLabels, final String xaRecoveryId) {
        String[] splitInstanceId = instanceId.split(DELIMITER);
        String host = splitInstanceId[0];
        String port = splitInstanceId.length < 2 ? "" : splitInstanceId[1];
        String labels = null == instanceLabels ? "" : String.join(",", instanceLabels);
        return new LinkedList<>(Arrays.asList(instanceId, host, port, status, modeType, labels, xaRecoveryId));
    }
}
