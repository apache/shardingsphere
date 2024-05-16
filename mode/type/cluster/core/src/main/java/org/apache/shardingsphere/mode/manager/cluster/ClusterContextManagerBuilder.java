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

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstance;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.instance.InstanceContextAware;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.util.eventbus.EventBusContext;
import org.apache.shardingsphere.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.mode.lock.GlobalLockContext;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.ContextManagerAware;
import org.apache.shardingsphere.mode.manager.ContextManagerBuilder;
import org.apache.shardingsphere.mode.manager.ContextManagerBuilderParameter;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.RegistryCenter;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.metadata.subscriber.ShardingSphereSchemaDataRegistrySubscriber;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.process.subscriber.ClusterProcessSubscriber;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.cluster.subscriber.ClusterStatusSubscriber;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.subscriber.ComputeNodeStatusSubscriber;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.storage.subscriber.QualifiedDataSourceStatusSubscriber;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.workerid.generator.ClusterWorkerIdGenerator;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.subscriber.ClusterEventSubscriberRegistry;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.MetaDataContextsFactory;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepositoryConfiguration;

import java.sql.SQLException;

/**
 * Cluster context manager builder.
 */
public final class ClusterContextManagerBuilder implements ContextManagerBuilder {
    
    @Override
    public ContextManager build(final ContextManagerBuilderParameter param, final EventBusContext eventBusContext) throws SQLException {
        ClusterPersistRepository repository = getClusterPersistRepository((ClusterPersistRepositoryConfiguration) param.getModeConfiguration().getRepository());
        RegistryCenter registryCenter = new RegistryCenter(eventBusContext, repository, param.getInstanceMetaData(), param.getDatabaseConfigs());
        InstanceContext instanceContext = buildInstanceContext(registryCenter, param, eventBusContext);
        if (registryCenter.getRepository() instanceof InstanceContextAware) {
            ((InstanceContextAware) registryCenter.getRepository()).setInstanceContext(instanceContext);
        }
        MetaDataPersistService metaDataPersistService = new MetaDataPersistService(repository);
        MetaDataContexts metaDataContexts = MetaDataContextsFactory.create(metaDataPersistService, param, instanceContext, registryCenter.getQualifiedDataSourceStatusService().loadStatus());
        ContextManager result = new ContextManager(metaDataContexts, instanceContext);
        setContextManagerAware(result);
        createSubscribers(eventBusContext, repository);
        registerOnline(registryCenter, param, result);
        return result;
    }
    
    private ClusterPersistRepository getClusterPersistRepository(final ClusterPersistRepositoryConfiguration config) {
        Preconditions.checkNotNull(config, "Cluster persist repository configuration can not be null.");
        ClusterPersistRepository result = TypedSPILoader.getService(ClusterPersistRepository.class, config.getType(), config.getProps());
        result.init(config);
        return result;
    }
    
    private InstanceContext buildInstanceContext(final RegistryCenter registryCenter, final ContextManagerBuilderParameter param, final EventBusContext eventBusContext) {
        return new InstanceContext(new ComputeNodeInstance(param.getInstanceMetaData()), new ClusterWorkerIdGenerator(registryCenter, param.getInstanceMetaData()),
                param.getModeConfiguration(), new ClusterModeContextManager(), new GlobalLockContext(registryCenter.getGlobalLockPersistService()), eventBusContext);
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
    
    private void registerOnline(final RegistryCenter registryCenter, final ContextManagerBuilderParameter param, final ContextManager contextManager) {
        registryCenter.onlineInstance(contextManager.getInstanceContext().getInstance());
        loadClusterStatus(registryCenter, contextManager);
        contextManager.getInstanceContext().getInstance().setLabels(param.getLabels());
        contextManager.getInstanceContext().getAllClusterInstances().addAll(registryCenter.getComputeNodeStatusService().loadAllComputeNodeInstances());
        new ClusterEventSubscriberRegistry(contextManager, registryCenter).register();
    }
    
    private void loadClusterStatus(final RegistryCenter registryCenter, final ContextManager contextManager) {
        registryCenter.persistClusterState(contextManager);
        contextManager.updateClusterState(registryCenter.getClusterStatusService().loadClusterStatus());
    }
    
    @Override
    public String getType() {
        return "Cluster";
    }
}
