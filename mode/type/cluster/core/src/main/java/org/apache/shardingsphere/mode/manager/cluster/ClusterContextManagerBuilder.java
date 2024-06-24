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

package org.apache.shardingsphere.mode.manager.cluster;

import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstance;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstanceContext;
import org.apache.shardingsphere.infra.instance.metadata.InstanceMetaData;
import org.apache.shardingsphere.infra.instance.metadata.jdbc.JDBCInstanceMetaData;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.util.eventbus.EventBusContext;
import org.apache.shardingsphere.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.metadata.persist.node.DatabaseMetaDataNode;
import org.apache.shardingsphere.mode.lock.GlobalLockContext;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.ContextManagerBuilder;
import org.apache.shardingsphere.mode.manager.ContextManagerBuilderParameter;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.listener.MetaDataChangedListener;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.listener.MetaDataWatchListenerManager;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.GlobalLockPersistService;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.workerid.generator.ClusterWorkerIdGenerator;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.subscriber.ClusterDeliverEventSubscriberRegistry;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.subscriber.ClusterDispatchEventSubscriberRegistry;
import org.apache.shardingsphere.mode.manager.cluster.exception.MissingRequiredClusterRepositoryConfigurationException;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.MetaDataContextsFactory;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepositoryConfiguration;

import java.sql.SQLException;
import java.util.Collection;

/**
 * Cluster context manager builder.
 */
public final class ClusterContextManagerBuilder implements ContextManagerBuilder {
    
    @Override
    public ContextManager build(final ContextManagerBuilderParameter param, final EventBusContext eventBusContext) throws SQLException {
        ModeConfiguration modeConfig = param.getModeConfiguration();
        ClusterPersistRepositoryConfiguration config = (ClusterPersistRepositoryConfiguration) modeConfig.getRepository();
        ComputeNodeInstanceContext computeNodeInstanceContext = buildComputeNodeInstanceContext(modeConfig, param.getInstanceMetaData(), eventBusContext, param.getLabels());
        ClusterPersistRepository repository = getClusterPersistRepository(config);
        repository.init(config, computeNodeInstanceContext);
        computeNodeInstanceContext.init(new ClusterWorkerIdGenerator(repository, param.getInstanceMetaData().getId()), new GlobalLockContext(new GlobalLockPersistService(repository)));
        MetaDataPersistService metaDataPersistService = new MetaDataPersistService(repository);
        MetaDataContexts metaDataContexts = MetaDataContextsFactory.create(metaDataPersistService, param, computeNodeInstanceContext);
        ContextManager result = new ContextManager(metaDataContexts, computeNodeInstanceContext, repository);
        registerOnline(eventBusContext, computeNodeInstanceContext, repository, param, result);
        return result;
    }
    
    private ClusterPersistRepository getClusterPersistRepository(final ClusterPersistRepositoryConfiguration config) {
        ShardingSpherePreconditions.checkNotNull(config, MissingRequiredClusterRepositoryConfigurationException::new);
        return TypedSPILoader.getService(ClusterPersistRepository.class, config.getType(), config.getProps());
    }
    
    private ComputeNodeInstanceContext buildComputeNodeInstanceContext(final ModeConfiguration modeConfig, final InstanceMetaData instanceMetaData,
                                                                       final EventBusContext eventBusContext, final Collection<String> labels) {
        return new ComputeNodeInstanceContext(new ComputeNodeInstance(instanceMetaData, labels), modeConfig, eventBusContext);
    }
    
    private void registerOnline(final EventBusContext eventBusContext, final ComputeNodeInstanceContext computeNodeInstanceContext,
                                final ClusterPersistRepository repository, final ContextManagerBuilderParameter param, final ContextManager contextManager) {
        contextManager.getPersistServiceFacade().getComputeNodePersistService().registerOnline(computeNodeInstanceContext.getInstance());
        watchDatabaseMetaDataListener(param, contextManager.getPersistServiceFacade().getMetaDataPersistService(), eventBusContext);
        contextManager.getComputeNodeInstanceContext().getAllClusterInstances().addAll(contextManager.getPersistServiceFacade().getComputeNodePersistService().loadAllComputeNodeInstances());
        new ClusterDeliverEventSubscriberRegistry(contextManager, repository).register();
        new ClusterDispatchEventSubscriberRegistry(contextManager, repository).register();
    }
    
    private void watchDatabaseMetaDataListener(final ContextManagerBuilderParameter param, final MetaDataPersistService metaDataPersistService, final EventBusContext eventBusContext) {
        getDatabaseNames(param, metaDataPersistService).forEach(each -> new MetaDataWatchListenerManager((ClusterPersistRepository) metaDataPersistService.getRepository())
                .addListener(DatabaseMetaDataNode.getDatabaseNamePath(each), new MetaDataChangedListener(eventBusContext)));
    }
    
    private Collection<String> getDatabaseNames(final ContextManagerBuilderParameter param, final MetaDataPersistService metaDataPersistService) {
        return param.getInstanceMetaData() instanceof JDBCInstanceMetaData ? param.getDatabaseConfigs().keySet() : metaDataPersistService.getDatabaseMetaDataService().loadAllDatabaseNames();
    }
    
    @Override
    public String getType() {
        return "Cluster";
    }
}
