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

import org.apache.shardingsphere.infra.util.eventbus.EventBusContext;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstance;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.mode.lock.ShardingSphereLockContext;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.ContextManagerBuilder;
import org.apache.shardingsphere.mode.manager.ContextManagerBuilderParameter;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.ClusterContextManagerCoordinator;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.RegistryCenter;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.workerid.generator.ClusterWorkerIdGenerator;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.MetaDataContextsFactory;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepositoryConfiguration;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepositoryFactory;
import org.apache.shardingsphere.schedule.core.ScheduleContextFactory;

import java.sql.SQLException;

/**
 * Cluster context manager builder.
 */
public final class ClusterContextManagerBuilder implements ContextManagerBuilder {
    
    @Override
    public ContextManager build(final ContextManagerBuilderParameter parameter) throws SQLException {
        ScheduleContextFactory.getInstance().init(parameter.getInstanceMetaData().getId(), parameter.getModeConfiguration());
        ClusterPersistRepository repository = ClusterPersistRepositoryFactory.getInstance((ClusterPersistRepositoryConfiguration) parameter.getModeConfiguration().getRepository());
        MetaDataPersistService persistService = new MetaDataPersistService(repository);
        persistConfigurations(persistService, parameter);
        RegistryCenter registryCenter = new RegistryCenter(repository, new EventBusContext(), parameter.getInstanceMetaData(), parameter.getDatabaseConfigs());
        InstanceContext instanceContext = buildInstanceContext(registryCenter, parameter);
        registryCenter.getRepository().watchSessionConnection(instanceContext);
        MetaDataContexts metaDataContexts = MetaDataContextsFactory.create(persistService, parameter, instanceContext);
        persistMetaData(metaDataContexts);
        ContextManager result = new ContextManager(metaDataContexts, instanceContext);
        registerOnline(persistService, registryCenter, parameter, result);
        return result;
    }
    
    private void persistConfigurations(final MetaDataPersistService persistService, final ContextManagerBuilderParameter parameter) {
        if (!parameter.isEmpty()) {
            persistService.persistConfigurations(parameter.getDatabaseConfigs(), parameter.getGlobalRuleConfigs(), parameter.getProps(), parameter.getModeConfiguration().isOverwrite());
        }
    }
    
    private InstanceContext buildInstanceContext(final RegistryCenter registryCenter, final ContextManagerBuilderParameter parameter) {
        return new InstanceContext(new ComputeNodeInstance(parameter.getInstanceMetaData()), new ClusterWorkerIdGenerator(registryCenter, parameter.getInstanceMetaData()),
                parameter.getModeConfiguration(), new ShardingSphereLockContext(registryCenter.getLockPersistService()), registryCenter.getEventBusContext());
    }
    
    private void persistMetaData(final MetaDataContexts metaDataContexts) {
        metaDataContexts.getMetaData().getDatabases().values().forEach(each -> each.getSchemas()
                .forEach((schemaName, schema) -> metaDataContexts.getPersistService().getDatabaseMetaDataService().persistMetaData(each.getName(), schemaName, schema.getTables())));
    }
    
    private void registerOnline(final MetaDataPersistService persistService, final RegistryCenter registryCenter,
                                final ContextManagerBuilderParameter parameter, final ContextManager contextManager) {
        contextManager.getInstanceContext().getInstance().setLabels(parameter.getLabels());
        contextManager.getInstanceContext().getAllClusterInstances().addAll(registryCenter.getComputeNodeStatusService().loadAllComputeNodeInstances());
        new ClusterContextManagerCoordinator(persistService, registryCenter, contextManager);
        registryCenter.onlineInstance(contextManager.getInstanceContext().getInstance());
    }
    
    @Override
    public String getType() {
        return "Cluster";
    }
}
