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
import org.apache.shardingsphere.infra.instance.metadata.jdbc.JDBCInstanceMetaData;
import org.apache.shardingsphere.infra.lock.LockContext;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.util.eventbus.EventBusContext;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.mode.deliver.DeliverEventSubscriber;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.ContextManagerBuilder;
import org.apache.shardingsphere.mode.manager.ContextManagerBuilderParameter;
import org.apache.shardingsphere.mode.manager.cluster.dispatch.listener.DataChangedEventListenerRegistry;
import org.apache.shardingsphere.mode.manager.cluster.exception.MissingRequiredClusterRepositoryConfigurationException;
import org.apache.shardingsphere.mode.manager.cluster.lock.ClusterLockContext;
import org.apache.shardingsphere.mode.manager.cluster.persist.service.GlobalLockPersistService;
import org.apache.shardingsphere.mode.deliver.DeliverEventSubscriberRegistry;
import org.apache.shardingsphere.mode.manager.cluster.workerid.ClusterWorkerIdGenerator;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.MetaDataContextsFactory;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepositoryConfiguration;
import org.apache.shardingsphere.mode.spi.repository.PersistRepository;

import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;

/**
 * Cluster context manager builder.
 */
public final class ClusterContextManagerBuilder implements ContextManagerBuilder {
    
    @Override
    public ContextManager build(final ContextManagerBuilderParameter param, final EventBusContext eventBusContext) throws SQLException {
        ModeConfiguration modeConfig = param.getModeConfiguration();
        ClusterPersistRepositoryConfiguration config = (ClusterPersistRepositoryConfiguration) modeConfig.getRepository();
        ComputeNodeInstanceContext computeNodeInstanceContext = new ComputeNodeInstanceContext(new ComputeNodeInstance(param.getInstanceMetaData(), param.getLabels()), modeConfig, eventBusContext);
        ClusterPersistRepository repository = getClusterPersistRepository(config, computeNodeInstanceContext);
        LockContext lockContext = new ClusterLockContext(new GlobalLockPersistService(repository));
        computeNodeInstanceContext.init(new ClusterWorkerIdGenerator(repository, param.getInstanceMetaData().getId()), lockContext);
        MetaDataPersistService metaDataPersistService = new MetaDataPersistService(repository);
        MetaDataContexts metaDataContexts = MetaDataContextsFactory.create(metaDataPersistService, param, computeNodeInstanceContext);
        ContextManager result = new ContextManager(metaDataContexts, computeNodeInstanceContext, repository);
        registerOnline(computeNodeInstanceContext, param, result, repository);
        return result;
    }
    
    private ClusterPersistRepository getClusterPersistRepository(final ClusterPersistRepositoryConfiguration config, final ComputeNodeInstanceContext computeNodeInstanceContext) {
        ShardingSpherePreconditions.checkNotNull(config, MissingRequiredClusterRepositoryConfigurationException::new);
        ClusterPersistRepository result = TypedSPILoader.getService(ClusterPersistRepository.class, config.getType(), config.getProps());
        result.init(config, computeNodeInstanceContext);
        return result;
    }
    
    private void registerOnline(final ComputeNodeInstanceContext computeNodeInstanceContext, final ContextManagerBuilderParameter param,
                                final ContextManager contextManager, final PersistRepository repository) {
        contextManager.getPersistServiceFacade().getComputeNodePersistService().registerOnline(computeNodeInstanceContext.getInstance());
        contextManager.getComputeNodeInstanceContext().getClusterInstanceRegistry().getAllClusterInstances()
                .addAll(contextManager.getPersistServiceFacade().getComputeNodePersistService().loadAllInstances());
        new DataChangedEventListenerRegistry(contextManager, getDatabaseNames(param, contextManager.getPersistServiceFacade().getMetaDataPersistService())).register();
        DeliverEventSubscriberRegistry deliverEventSubscriberRegistry = new DeliverEventSubscriberRegistry(contextManager.getComputeNodeInstanceContext().getEventBusContext());
        deliverEventSubscriberRegistry.register(createDeliverEventSubscribers(repository));
    }
    
    private Collection<String> getDatabaseNames(final ContextManagerBuilderParameter param, final MetaDataPersistService metaDataPersistService) {
        return param.getInstanceMetaData() instanceof JDBCInstanceMetaData
                ? param.getDatabaseConfigs().keySet()
                : metaDataPersistService.getDatabaseMetaDataFacade().getDatabase().loadAllDatabaseNames();
    }
    
    private Collection<DeliverEventSubscriber> createDeliverEventSubscribers(final PersistRepository repository) {
        Collection<DeliverEventSubscriber> result = new LinkedList<>();
        for (DeliverEventSubscriber each : ShardingSphereServiceLoader.getServiceInstances(DeliverEventSubscriber.class)) {
            each.setRepository(repository);
            result.add(each);
        }
        return result;
    }
    
    @Override
    public String getType() {
        return "Cluster";
    }
}
