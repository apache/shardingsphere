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

package org.apache.shardingsphere.proxy.backend.handler.distsql.ral.updatable;

import org.apache.shardingsphere.distsql.parser.statement.ral.updatable.UnlabelInstanceStatement;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstance;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.util.exception.external.sql.type.generic.UnsupportedSQLOperationException;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.event.LabelsChangedEvent;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.mode.repository.standalone.StandalonePersistRepository;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.UpdatableRALBackendHandler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Optional;

/**
 * Unlabel instance handler.
 */
public final class UnlabelInstanceHandler extends UpdatableRALBackendHandler<UnlabelInstanceStatement> {
    
    @Override
    protected void update(final ContextManager contextManager) {
        MetaDataPersistService persistService = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getPersistService();
        ShardingSpherePreconditions.checkState(null != persistService.getRepository() && !(persistService.getRepository() instanceof StandalonePersistRepository),
                () -> new UnsupportedSQLOperationException("Labels can only be removed in cluster mode"));
        String instanceId = getSqlStatement().getInstanceId();
        Optional<ComputeNodeInstance> computeNodeInstance = contextManager.getInstanceContext().getComputeNodeInstanceById(instanceId);
        if (computeNodeInstance.isPresent()) {
            Collection<String> labels = new LinkedHashSet<>(computeNodeInstance.get().getLabels());
            if (getSqlStatement().getLabels().isEmpty()) {
                contextManager.getInstanceContext().getEventBusContext().post(new LabelsChangedEvent(instanceId, Collections.emptyList()));
            } else {
                labels.removeAll(getSqlStatement().getLabels());
                contextManager.getInstanceContext().getEventBusContext().post(new LabelsChangedEvent(instanceId, new ArrayList<>(labels)));
            }
        }
    }
}
