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

package org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.updatable;

import org.apache.shardingsphere.distsql.parser.statement.ral.common.updatable.UnlabelInstanceStatement;
import org.apache.shardingsphere.infra.distsql.exception.DistSQLException;
import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstance;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.event.LabelsChangedEvent;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.mode.repository.standalone.StandalonePersistRepository;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.UpdatableRALBackendHandler;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Optional;

/**
 * Unlabel instance handler.
 */
public final class UnlabelInstanceHandler extends UpdatableRALBackendHandler<UnlabelInstanceStatement, UnlabelInstanceHandler> {
    
    @Override
    protected void update(final ContextManager contextManager, final UnlabelInstanceStatement sqlStatement) throws DistSQLException {
        MetaDataPersistService persistService = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getPersistService().orElse(null);
        if (null == persistService || null == persistService.getRepository() || persistService.getRepository() instanceof StandalonePersistRepository) {
            throw new UnsupportedOperationException("Labels can only be removed in cluster mode");
        }
        String instanceId = sqlStatement.getInstanceId();
        Optional<ComputeNodeInstance> computeNodeInstance = contextManager.getInstanceContext().getComputeNodeInstanceById(instanceId);
        if (computeNodeInstance.isPresent()) {
            Collection<String> labels = new LinkedHashSet<>(computeNodeInstance.get().getLabels());
            if (sqlStatement.getLabels().isEmpty()) {
                ShardingSphereEventBus.getInstance().post(new LabelsChangedEvent(instanceId, Collections.EMPTY_LIST));
            } else {
                labels.removeAll(sqlStatement.getLabels());
                ShardingSphereEventBus.getInstance().post(new LabelsChangedEvent(instanceId, new LinkedList<>(labels)));
            }
        }
    }
}
