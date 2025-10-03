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
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstance;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstanceContext;
import org.apache.shardingsphere.infra.instance.metadata.jdbc.JDBCInstanceMetaData;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.util.eventbus.EventBusContext;
import org.apache.shardingsphere.mode.deliver.DeliverEventSubscriber;
import org.apache.shardingsphere.mode.deliver.DeliverEventSubscriberRegistry;
import org.apache.shardingsphere.mode.exclusive.ExclusiveOperatorEngine;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.builder.ContextManagerBuilder;
import org.apache.shardingsphere.mode.manager.builder.ContextManagerBuilderParameter;
import org.apache.shardingsphere.mode.manager.cluster.dispatch.listener.DataChangedEventListenerRegistry;
import org.apache.shardingsphere.mode.manager.cluster.exception.MissingRequiredClusterRepositoryConfigurationException;
import org.apache.shardingsphere.mode.manager.cluster.exclusive.ClusterExclusiveOperatorContext;
import org.apache.shardingsphere.mode.manager.cluster.persist.facade.ClusterPersistServiceFacade;
import org.apache.shardingsphere.mode.manager.cluster.workerid.ClusterWorkerIdGenerator;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.factory.MetaDataContextsFactory;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistFacade;
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
        computeNodeInstanceContext.init(new ClusterWorkerIdGenerator(repository, param.getInstanceMetaData().getId()));
        ExclusiveOperatorEngine exclusiveOperatorEngine = new ExclusiveOperatorEngine(new ClusterExclusiveOperatorContext(repository));
        MetaDataContexts metaDataContexts = new MetaDataContextsFactory(new MetaDataPersistFacade(repository), computeNodeInstanceContext).create(param);
        ContextManager result = new ContextManager(metaDataContexts, computeNodeInstanceContext, exclusiveOperatorEngine, repository);
        registerOnline(computeNodeInstanceContext, param, result);
        new DeliverEventSubscriberRegistry(result.getComputeNodeInstanceContext().getEventBusContext()).register(createDeliverEventSubscribers(repository));
        return result;
    }
    
    private ClusterPersistRepository getClusterPersistRepository(final ClusterPersistRepositoryConfiguration config, final ComputeNodeInstanceContext computeNodeInstanceContext) {
        ShardingSpherePreconditions.checkNotNull(config, MissingRequiredClusterRepositoryConfigurationException::new);
        ClusterPersistRepository result = TypedSPILoader.getService(ClusterPersistRepository.class, config.getType(), config.getProps());
        result.init(config, computeNodeInstanceContext);
        return result;
    }
    
    private void registerOnline(final ComputeNodeInstanceContext computeNodeInstanceContext, final ContextManagerBuilderParameter param, final ContextManager contextManager) {
        ClusterPersistServiceFacade clusterPersistServiceFacade = (ClusterPersistServiceFacade) contextManager.getPersistServiceFacade().getModeFacade();
        clusterPersistServiceFacade.getComputeNodeService().registerOnline(computeNodeInstanceContext.getInstance());
        contextManager.getComputeNodeInstanceContext().getClusterInstanceRegistry().getAllClusterInstances()
                .addAll(clusterPersistServiceFacade.getComputeNodeService().loadAllInstances());
        new DataChangedEventListenerRegistry(contextManager, getDatabaseNames(param, contextManager.getPersistServiceFacade().getMetaDataFacade())).register();
    }
    
    private Collection<String> getDatabaseNames(final ContextManagerBuilderParameter param, final MetaDataPersistFacade metaDataPersistFacade) {
        return param.getInstanceMetaData() instanceof JDBCInstanceMetaData
                ? param.getDatabaseConfigs().keySet()
                : metaDataPersistFacade.getDatabaseMetaDataFacade().getDatabase().loadAllDatabaseNames();
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
