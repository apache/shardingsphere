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
import org.apache.shardingsphere.infra.instance.ComputeNodeInstanceContextAware;
import org.apache.shardingsphere.infra.instance.metadata.InstanceMetaData;
import org.apache.shardingsphere.infra.instance.metadata.jdbc.JDBCInstanceMetaData;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.state.cluster.ClusterState;
import org.apache.shardingsphere.infra.util.eventbus.EventBusContext;
import org.apache.shardingsphere.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.mode.lock.GlobalLockContext;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.ContextManagerBuilder;
import org.apache.shardingsphere.mode.manager.ContextManagerBuilderParameter;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.GlobalLockPersistService;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.GovernanceWatcherFactory;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.storage.subscriber.QualifiedDataSourceStatusSubscriber;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.workerid.generator.ClusterWorkerIdGenerator;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.subscriber.ClusterEventSubscriberRegistry;
import org.apache.shardingsphere.mode.manager.cluster.exception.MissingRequiredClusterRepositoryConfigurationException;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.MetaDataContextsFactory;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepositoryConfiguration;
import org.apache.shardingsphere.mode.repository.cluster.lock.holder.DistributedLockHolder;
import org.apache.shardingsphere.mode.repository.cluster.lock.impl.props.DefaultLockTypedProperties;
import org.apache.shardingsphere.mode.state.StatePersistService;
import org.apache.shardingsphere.mode.storage.service.QualifiedDataSourceStatusService;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Optional;
import java.util.Properties;

/**
 * Cluster context manager builder.
 */
public final class ClusterContextManagerBuilder implements ContextManagerBuilder {
    
    @Override
    public ContextManager build(final ContextManagerBuilderParameter param, final EventBusContext eventBusContext) throws SQLException {
        ModeConfiguration modeConfig = param.getModeConfiguration();
        ClusterPersistRepository repository = getClusterPersistRepository((ClusterPersistRepositoryConfiguration) modeConfig.getRepository());
        ComputeNodeInstanceContext computeNodeInstanceContext = buildComputeNodeInstanceContext(modeConfig, param.getInstanceMetaData(), repository, eventBusContext);
        if (repository instanceof ComputeNodeInstanceContextAware) {
            ((ComputeNodeInstanceContextAware) repository).setComputeNodeInstanceContext(computeNodeInstanceContext);
        }
        MetaDataPersistService metaDataPersistService = new MetaDataPersistService(repository);
        MetaDataContexts metaDataContexts = MetaDataContextsFactory.create(metaDataPersistService, param, computeNodeInstanceContext, new QualifiedDataSourceStatusService(repository).loadStatus());
        ContextManager result = new ContextManager(metaDataContexts, computeNodeInstanceContext, repository);
        createSubscribers(eventBusContext, repository);
        registerOnline(eventBusContext, computeNodeInstanceContext, repository, param, result);
        setClusterState(result);
        return result;
    }
    
    private ClusterPersistRepository getClusterPersistRepository(final ClusterPersistRepositoryConfiguration config) {
        ShardingSpherePreconditions.checkNotNull(config, MissingRequiredClusterRepositoryConfigurationException::new);
        ClusterPersistRepository result = TypedSPILoader.getService(ClusterPersistRepository.class, config.getType(), config.getProps());
        result.init(config);
        return result;
    }
    
    private ComputeNodeInstanceContext buildComputeNodeInstanceContext(final ModeConfiguration modeConfig,
                                                                       final InstanceMetaData instanceMetaData, final ClusterPersistRepository repository, final EventBusContext eventBusContext) {
        return new ComputeNodeInstanceContext(new ComputeNodeInstance(instanceMetaData),
                new ClusterWorkerIdGenerator(repository, instanceMetaData.getId()), modeConfig,
                new GlobalLockContext(new GlobalLockPersistService(initDistributedLockHolder(repository))), eventBusContext);
    }
    
    private DistributedLockHolder initDistributedLockHolder(final ClusterPersistRepository repository) {
        DistributedLockHolder distributedLockHolder = repository.getDistributedLockHolder();
        return null == distributedLockHolder ? new DistributedLockHolder("default", repository, new DefaultLockTypedProperties(new Properties())) : distributedLockHolder;
    }
    
    // TODO remove the method, only keep ZooKeeper's events, remove all decouple events
    private void createSubscribers(final EventBusContext eventBusContext, final ClusterPersistRepository repository) {
        eventBusContext.register(new QualifiedDataSourceStatusSubscriber(repository));
    }
    
    private void registerOnline(final EventBusContext eventBusContext, final ComputeNodeInstanceContext computeNodeInstanceContext,
                                final ClusterPersistRepository repository, final ContextManagerBuilderParameter param, final ContextManager contextManager) {
        contextManager.getPersistServiceFacade().getComputeNodePersistService().registerOnline(computeNodeInstanceContext.getInstance());
        new GovernanceWatcherFactory(repository,
                eventBusContext, param.getInstanceMetaData() instanceof JDBCInstanceMetaData ? param.getDatabaseConfigs().keySet() : Collections.emptyList()).watchListeners();
        if (null != param.getLabels()) {
            contextManager.getComputeNodeInstanceContext().getInstance().getLabels().addAll(param.getLabels());
        }
        contextManager.getComputeNodeInstanceContext().getAllClusterInstances().addAll(contextManager.getPersistServiceFacade().getComputeNodePersistService().loadAllComputeNodeInstances());
        new ClusterEventSubscriberRegistry(contextManager, repository).register();
    }
    
    private void setClusterState(final ContextManager contextManager) {
        StatePersistService statePersistService = contextManager.getPersistServiceFacade().getStatePersistService();
        Optional<ClusterState> clusterState = statePersistService.loadClusterState();
        if (clusterState.isPresent()) {
            contextManager.getStateContext().switchCurrentClusterState(clusterState.get());
        } else {
            statePersistService.updateClusterState(ClusterState.OK);
        }
    }
    
    @Override
    public String getType() {
        return "Cluster";
    }
}
