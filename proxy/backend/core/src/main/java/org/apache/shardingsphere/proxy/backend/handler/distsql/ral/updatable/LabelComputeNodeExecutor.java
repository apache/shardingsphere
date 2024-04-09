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

import org.apache.shardingsphere.distsql.handler.required.DistSQLExecutorClusterModeRequired;
import org.apache.shardingsphere.distsql.handler.engine.update.DistSQLUpdateExecutor;
import org.apache.shardingsphere.distsql.statement.ral.updatable.LabelComputeNodeStatement;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstance;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.event.LabelsChangedEvent;

import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Optional;

/**
 * Label compute node executor.
 */
@DistSQLExecutorClusterModeRequired
public final class LabelComputeNodeExecutor implements DistSQLUpdateExecutor<LabelComputeNodeStatement> {
    
    @Override
    public void executeUpdate(final LabelComputeNodeStatement sqlStatement, final ContextManager contextManager) throws SQLException {
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
    public Class<LabelComputeNodeStatement> getType() {
        return LabelComputeNodeStatement.class;
    }
}
