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

package org.apache.shardingsphere.mode.manager.cluster.persist.facade;

import lombok.AccessLevel;
import lombok.Getter;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstance;
import org.apache.shardingsphere.mode.manager.cluster.persist.service.ClusterComputeNodePersistService;
import org.apache.shardingsphere.mode.manager.cluster.persist.service.ClusterMetaDataManagerPersistService;
import org.apache.shardingsphere.mode.manager.cluster.persist.service.ClusterProcessPersistService;
import org.apache.shardingsphere.mode.metadata.manager.MetaDataContextManager;
import org.apache.shardingsphere.mode.persist.mode.ModePersistServiceFacade;
import org.apache.shardingsphere.mode.persist.service.MetaDataManagerPersistService;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;

/**
 * Cluster persist service facade.
 */
@Getter
public final class ClusterPersistServiceFacade implements ModePersistServiceFacade {
    
    private final MetaDataManagerPersistService metaDataManagerService;
    
    private final ClusterComputeNodePersistService computeNodeService;
    
    private final ClusterProcessPersistService processService;
    
    @Getter(AccessLevel.NONE)
    private final ComputeNodeInstance computeNodeInstance;
    
    public ClusterPersistServiceFacade(final MetaDataContextManager metaDataContextManager, final ClusterPersistRepository repository) {
        metaDataManagerService = new ClusterMetaDataManagerPersistService(metaDataContextManager, repository);
        computeNodeService = new ClusterComputeNodePersistService(repository);
        processService = new ClusterProcessPersistService(repository);
        computeNodeInstance = metaDataContextManager.getComputeNodeInstanceContext().getInstance();
    }
    
    @Override
    public void close() {
        computeNodeService.offline(computeNodeInstance);
    }
}
