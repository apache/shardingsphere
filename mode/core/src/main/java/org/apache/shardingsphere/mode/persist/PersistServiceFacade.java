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

package org.apache.shardingsphere.mode.persist;

import lombok.Getter;
import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstance;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.metadata.manager.MetaDataContextManager;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistFacade;
import org.apache.shardingsphere.mode.persist.service.MetaDataManagerPersistService;
import org.apache.shardingsphere.mode.persist.service.PersistServiceBuilder;
import org.apache.shardingsphere.mode.persist.service.ProcessPersistService;
import org.apache.shardingsphere.mode.spi.repository.PersistRepository;
import org.apache.shardingsphere.mode.state.cluster.ClusterStatePersistService;
import org.apache.shardingsphere.mode.state.node.ComputeNodePersistService;
import org.apache.shardingsphere.mode.state.node.QualifiedDataSourceStatePersistService;

/**
 * Persist service facade.
 */
@Getter
public final class PersistServiceFacade {
    
    private final PersistRepository repository;
    
    private final MetaDataPersistFacade metaDataPersistFacade;
    
    private final ComputeNodePersistService computeNodePersistService;
    
    private final ClusterStatePersistService clusterStatePersistService;
    
    private final MetaDataManagerPersistService metaDataManagerPersistService;
    
    private final ProcessPersistService processPersistService;
    
    private final QualifiedDataSourceStatePersistService qualifiedDataSourceStatePersistService;
    
    public PersistServiceFacade(final PersistRepository repository, final ModeConfiguration modeConfig, final MetaDataContextManager metaDataContextManager) {
        this.repository = repository;
        metaDataPersistFacade = new MetaDataPersistFacade(repository);
        computeNodePersistService = new ComputeNodePersistService(repository);
        clusterStatePersistService = new ClusterStatePersistService(repository);
        qualifiedDataSourceStatePersistService = new QualifiedDataSourceStatePersistService(repository);
        PersistServiceBuilder persistServiceBuilder = TypedSPILoader.getService(PersistServiceBuilder.class, modeConfig.getType());
        metaDataManagerPersistService = persistServiceBuilder.buildMetaDataManagerPersistService(repository, metaDataContextManager);
        processPersistService = persistServiceBuilder.buildProcessPersistService(repository);
    }
    
    /**
     * Close persist service facade.
     *
     * @param computeNodeInstance compute node instance
     */
    public void close(final ComputeNodeInstance computeNodeInstance) {
        computeNodePersistService.offline(computeNodeInstance);
        repository.close();
    }
}
