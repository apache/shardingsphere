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

import org.apache.shardingsphere.distsql.handler.ral.update.RALUpdater;
import org.apache.shardingsphere.distsql.parser.statement.ral.updatable.LabelComputeNodeStatement;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstance;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.core.external.sql.type.generic.UnsupportedSQLOperationException;
import org.apache.shardingsphere.metadata.persist.MetaDataBasedPersistService;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.event.LabelsChangedEvent;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;

import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Optional;

/**
 * Label compute node updater.
 */
public final class LabelComputeNodeUpdater implements RALUpdater<LabelComputeNodeStatement> {
    
    @Override
    public void executeUpdate(final String databaseName, final LabelComputeNodeStatement sqlStatement) throws SQLException {
        ContextManager contextManager = ProxyContext.getInstance().getContextManager();
        MetaDataBasedPersistService persistService = contextManager.getMetaDataContexts().getPersistService();
        ShardingSpherePreconditions.checkState(null != persistService && null != persistService.getRepository() && persistService.getRepository() instanceof ClusterPersistRepository,
                () -> new UnsupportedSQLOperationException("Labels can only be added in cluster mode"));
        String instanceId = sqlStatement.getInstanceId();
        Optional<ComputeNodeInstance> computeNodeInstance = contextManager.getInstanceContext().getComputeNodeInstanceById(instanceId);
        if (computeNodeInstance.isPresent()) {
            Collection<String> labels = new LinkedHashSet<>(sqlStatement.getLabels());
            if (!sqlStatement.isOverwrite()) {
                labels.addAll(computeNodeInstance.get().getLabels());
            }
            contextManager.getInstanceContext().getEventBusContext().post(new LabelsChangedEvent(instanceId, new LinkedList<>(labels)));
        }
    }
    
    @Override
    public String getType() {
        return LabelComputeNodeStatement.class.getName();
    }
}
