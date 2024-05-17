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

import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstance;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.instance.InstanceContextAware;
import org.apache.shardingsphere.infra.instance.metadata.jdbc.JDBCInstanceMetaData;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.state.cluster.ClusterState;
import org.apache.shardingsphere.infra.util.eventbus.EventBusContext;
import org.apache.shardingsphere.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.mode.lock.GlobalLockContext;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.ContextManagerAware;
import org.apache.shardingsphere.mode.manager.ContextManagerBuilder;
import org.apache.shardingsphere.mode.manager.ContextManagerBuilderParameter;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.GlobalLockPersistService;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.GovernanceWatcherFactory;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.metadata.subscriber.ShardingSphereSchemaDataRegistrySubscriber;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.process.subscriber.ClusterProcessSubscriber;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.cluster.subscriber.ClusterStatusSubscriber;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.service.ComputeNodeStatusService;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.subscriber.ComputeNodeStatusSubscriber;
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
import org.apache.shardingsphere.mode.state.StateService;
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
        ClusterPersistRepository repository = getClusterPersistRepository((ClusterPersistRepositoryConfiguration) param.getModeConfiguration().getRepository());
        InstanceContext instanceContext = buildInstanceContext(repository, param, eventBusContext);
        if (repository instanceof InstanceContextAware) {
            ((InstanceContextAware) repository).setInstanceContext(instanceContext);
        }
        MetaDataPersistService metaDataPersistService = new MetaDataPersistService(repository);
        MetaDataContexts metaDataContexts = MetaDataContextsFactory.create(metaDataPersistService, param, instanceContext, new QualifiedDataSourceStatusService(repository).loadStatus());
        ContextManager result = new ContextManager(metaDataContexts, instanceContext);
        setContextManagerAware(result);
        createSubscribers(eventBusContext, repository);
        registerOnline(eventBusContext, instanceContext, repository, param, result);
        setClusterState(result);
        return result;
    }
    
    private ClusterPersistRepository getClusterPersistRepository(final ClusterPersistRepositoryConfiguration config) {
        ShardingSpherePreconditions.checkNotNull(config, MissingRequiredClusterRepositoryConfigurationException::new);
        ClusterPersistRepository result = TypedSPILoader.getService(ClusterPersistRepository.class, config.getType(), config.getProps());
        result.init(config);
        return result;
    }
    
    private InstanceContext buildInstanceContext(final ClusterPersistRepository repository, final ContextManagerBuilderParameter param, final EventBusContext eventBusContext) {
        return new InstanceContext(new ComputeNodeInstance(param.getInstanceMetaData()), new ClusterWorkerIdGenerator(repository, param.getInstanceMetaData()), param.getModeConfiguration(),
                new ClusterModeContextManager(), new GlobalLockContext(new GlobalLockPersistService(initDistributedLockHolder(repository))), eventBusContext);
    }
    
    private DistributedLockHolder initDistributedLockHolder(final ClusterPersistRepository repository) {
        DistributedLockHolder distributedLockHolder = repository.getDistributedLockHolder();
        return null == distributedLockHolder ? new DistributedLockHolder("default", repository, new DefaultLockTypedProperties(new Properties())) : distributedLockHolder;
    }
    
    private void setContextManagerAware(final ContextManager contextManager) {
        ((ContextManagerAware) contextManager.getInstanceContext().getModeContextManager()).setContextManager(contextManager);
    }
    
    // TODO remove the method, only keep ZooKeeper's events, remove all decouple events
    private void createSubscribers(final EventBusContext eventBusContext, final ClusterPersistRepository repository) {
        eventBusContext.register(new ComputeNodeStatusSubscriber(repository));
        eventBusContext.register(new ClusterStatusSubscriber(repository));
        eventBusContext.register(new QualifiedDataSourceStatusSubscriber(repository));
        eventBusContext.register(new ClusterProcessSubscriber(repository, eventBusContext));
        eventBusContext.register(new ShardingSphereSchemaDataRegistrySubscriber(repository));
    }
    
    private void registerOnline(final EventBusContext eventBusContext, final InstanceContext instanceContext,
                                final ClusterPersistRepository repository, final ContextManagerBuilderParameter param, final ContextManager contextManager) {
        ComputeNodeStatusService computeNodeStatusService = new ComputeNodeStatusService(repository);
        computeNodeStatusService.registerOnline(instanceContext.getInstance().getMetaData());
        computeNodeStatusService.persistInstanceLabels(instanceContext.getInstance().getCurrentInstanceId(), instanceContext.getInstance().getLabels());
        computeNodeStatusService.persistInstanceState(instanceContext.getInstance().getCurrentInstanceId(), instanceContext.getInstance().getState());
        new GovernanceWatcherFactory(repository,
                eventBusContext, param.getInstanceMetaData() instanceof JDBCInstanceMetaData ? param.getDatabaseConfigs().keySet() : Collections.emptyList()).watchListeners();
        contextManager.getInstanceContext().getInstance().setLabels(param.getLabels());
        contextManager.getInstanceContext().getAllClusterInstances().addAll(new ComputeNodeStatusService(repository).loadAllComputeNodeInstances());
        new ClusterEventSubscriberRegistry(contextManager, repository).register();
    }
    
    private void setClusterState(final ContextManager contextManager) {
        StateService stateService = contextManager.getStateContext().getStateService();
        Optional<ClusterState> clusterState = stateService.load();
        if (clusterState.isPresent()) {
            contextManager.updateClusterState(clusterState.get());
        } else {
            stateService.persist(ClusterState.OK);
        }
    }
    
    @Override
    public String getType() {
        return "Cluster";
    }
}
