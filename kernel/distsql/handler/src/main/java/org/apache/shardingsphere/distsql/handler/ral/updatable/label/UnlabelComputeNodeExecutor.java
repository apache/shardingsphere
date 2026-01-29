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

package org.apache.shardingsphere.distsql.handler.ral.updatable.label;

import org.apache.shardingsphere.distsql.handler.engine.update.DistSQLUpdateExecutor;
import org.apache.shardingsphere.distsql.handler.required.DistSQLExecutorClusterModeRequired;
import org.apache.shardingsphere.distsql.statement.type.ral.updatable.UnlabelComputeNodeStatement;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstance;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.cluster.persist.facade.ClusterPersistServiceFacade;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Optional;

/**
 * Unlabel compute node executor.
 */
@DistSQLExecutorClusterModeRequired
public final class UnlabelComputeNodeExecutor implements DistSQLUpdateExecutor<UnlabelComputeNodeStatement> {
    
    @Override
    public void executeUpdate(final UnlabelComputeNodeStatement sqlStatement, final ContextManager contextManager) {
        String instanceId = sqlStatement.getInstanceId();
        Optional<ComputeNodeInstance> computeNodeInstance = contextManager.getComputeNodeInstanceContext().getClusterInstanceRegistry().find(instanceId);
        if (computeNodeInstance.isPresent()) {
            ClusterPersistServiceFacade clusterPersistServiceFacade = (ClusterPersistServiceFacade) contextManager.getPersistServiceFacade().getModeFacade();
            Collection<String> labels = new LinkedHashSet<>(computeNodeInstance.get().getLabels());
            if (sqlStatement.getLabels().isEmpty()) {
                clusterPersistServiceFacade.getComputeNodeService().persistLabels(instanceId, Collections.emptyList());
            } else {
                labels.removeAll(sqlStatement.getLabels());
                clusterPersistServiceFacade.getComputeNodeService().persistLabels(instanceId, new ArrayList<>(labels));
            }
        }
    }
    
    @Override
    public Class<UnlabelComputeNodeStatement> getType() {
        return UnlabelComputeNodeStatement.class;
    }
}
