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

package org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.show.executor;

import org.apache.shardingsphere.infra.instance.ComputeNodeInstance;
import org.apache.shardingsphere.infra.instance.definition.InstanceId;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.ComputeNodeStatus;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.mode.repository.standalone.StandalonePersistRepository;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.response.header.query.impl.QueryHeader;
import org.apache.shardingsphere.sharding.merge.dal.common.MultipleLocalDataMergedResult;

import java.sql.Types;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Show instance executor.
 */
public final class ShowInstanceExecutor extends AbstractShowExecutor {
    
    private static final String DELIMITER = "@";
    
    private static final String ID = "instance_id";
    
    private static final String HOST = "host";
    
    private static final String PORT = "port";
    
    private static final String STATUS = "status";
    
    private static final String DISABLED = "disabled";
    
    private static final String LABELS = "labels";
    
    private static final String ENABLED = "enabled";
    
    @Override
    protected List<QueryHeader> createQueryHeaders() {
        return Arrays.asList(
                new QueryHeader("", "", ID, ID, Types.VARCHAR, "VARCHAR", 64, 0, false, false, false, false),
                new QueryHeader("", "", HOST, HOST, Types.VARCHAR, "VARCHAR", 64, 0, false, false, false, false),
                new QueryHeader("", "", PORT, PORT, Types.VARCHAR, "VARCHAR", 64, 0, false, false, false, false),
                new QueryHeader("", "", STATUS, STATUS, Types.VARCHAR, "VARCHAR", 64, 0, false, false, false, false),
                new QueryHeader("", "", LABELS, LABELS, Types.VARCHAR, "VARCHAR", 64, 0, false, false, false, false)
        );
    }
    
    @Override
    protected MergedResult createMergedResult() {
        MetaDataPersistService persistService = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaDataPersistService().orElse(null);
        if (null == persistService || null == persistService.getRepository() || persistService.getRepository() instanceof StandalonePersistRepository) {
            return new MultipleLocalDataMergedResult(buildInstanceRows());
        }
        return new MultipleLocalDataMergedResult(buildInstanceRows(persistService));
    }
    
    private Collection<List<Object>> buildInstanceRows() {
        List<List<Object>> result = new LinkedList<>();
        InstanceId instanceId = ProxyContext.getInstance().getContextManager().getInstanceContext().getInstance().getInstanceDefinition().getInstanceId();
        result.add(buildRow(instanceId.getId(), ENABLED, Collections.emptyList()));
        return result;
    }
    
    private Collection<List<Object>> buildInstanceRows(final MetaDataPersistService persistService) {
        Collection<ComputeNodeInstance> instances = persistService.getComputeNodePersistService().loadAllComputeNodeInstances();
        if (!instances.isEmpty()) {
            return instances.stream().filter(Objects::nonNull)
                    .map(each -> buildRow(each.getInstanceDefinition().getInstanceId().getId(), getStatus(each.getStatus()), each.getLabels()))
                    .collect(Collectors.toCollection(LinkedList::new));
        }
        return Collections.emptyList();
    }
    
    private List<Object> buildRow(final String instanceId, final String status, final Collection<String> instanceLabels) {
        String[] splitInstanceId = instanceId.split(DELIMITER);
        String host = splitInstanceId[0];
        String port = splitInstanceId.length < 2 ? "" : splitInstanceId[1];
        String labels = null == instanceLabels ? "" : String.join(",", instanceLabels);
        return new LinkedList<>(Arrays.asList(instanceId, host, port, status, labels));
    }
    
    private String getStatus(final Collection<String> computeNodeStatus) {
        return computeNodeStatus.isEmpty() || !computeNodeStatus.contains(ComputeNodeStatus.CIRCUIT_BREAK.name()) ? ENABLED : DISABLED;
    }
}
