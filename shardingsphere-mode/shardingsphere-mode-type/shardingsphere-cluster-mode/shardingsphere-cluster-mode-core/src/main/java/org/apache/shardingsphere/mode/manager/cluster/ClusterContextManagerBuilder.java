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
import com.google.common.base.Splitter;
import org.apache.shardingsphere.infra.config.database.DatabaseConfiguration;
import org.apache.shardingsphere.infra.datasource.state.DataSourceState;
import org.apache.shardingsphere.infra.datasource.state.DataSourceStateManager;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstance;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.instance.InstanceContextAware;
import org.apache.shardingsphere.infra.util.eventbus.EventBusContext;
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
import org.apache.shardingsphere.mode.metadata.storage.StorageNodeDataSource;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepositoryConfiguration;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepositoryFactory;
import org.apache.shardingsphere.schedule.core.ScheduleContextFactory;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Cluster context manager builder.
 */
public final class ClusterContextManagerBuilder implements ContextManagerBuilder {
    
    @Override
    public ContextManager build(final ContextManagerBuilderParameter parameter) throws SQLException {
        ClusterPersistRepository repository = ClusterPersistRepositoryFactory.getInstance((ClusterPersistRepositoryConfiguration) parameter.getModeConfiguration().getRepository());
        MetaDataPersistService persistService = new MetaDataPersistService(repository);
        persistConfigurations(persistService, parameter);
        RegistryCenter registryCenter = new RegistryCenter(repository, new EventBusContext(), parameter.getInstanceMetaData(), parameter.getDatabaseConfigs());
        InstanceContext instanceContext = buildInstanceContext(registryCenter, parameter);
        ClusterPersistRepository persistRepository = registryCenter.getRepository();
        if (persistRepository instanceof InstanceContextAware) {
            ((InstanceContextAware) persistRepository).setInstanceContext(instanceContext);
        }
        checkDataSourceStates(parameter.getDatabaseConfigs(), registryCenter, parameter.isForce());
        MetaDataContexts metaDataContexts = MetaDataContextsFactory.create(persistService, parameter, instanceContext);
        persistMetaData(metaDataContexts);
        ContextManager result = new ContextManager(metaDataContexts, instanceContext);
        registerOnline(persistService, registryCenter, parameter, result);
        return result;
    }
    
    private void checkDataSourceStates(final Map<String, DatabaseConfiguration> databaseConfigs, final RegistryCenter registryCenter, final boolean force) {
        Map<String, StorageNodeDataSource> storageNodes = registryCenter.getStorageNodeStatusService().loadStorageNodes();
        Map<String, DataSourceState> storageDataSourceStates = getStorageDataSourceStates(storageNodes);
        databaseConfigs.forEach((key, value) -> {
            if (null != value.getDataSources()) {
                DataSourceStateManager.getInstance().initStates(key, value.getDataSources(), storageDataSourceStates, force);
            }
        });
    }
    
    private Map<String, DataSourceState> getStorageDataSourceStates(final Map<String, StorageNodeDataSource> storageDataSourceStates) {
        Map<String, DataSourceState> result = new HashMap<>(storageDataSourceStates.size(), 1);
        storageDataSourceStates.forEach((key, value) -> {
            List<String> values = Splitter.on(".").splitToList(key);
            Preconditions.checkArgument(3 == values.size(), "Illegal data source of storage node.");
            String databaseName = values.get(0);
            String dataSourceName = values.get(2);
            result.put(databaseName + "." + dataSourceName, DataSourceState.getDataSourceState(value.getStatus()));
        });
        return result;
    }
    
    private void persistConfigurations(final MetaDataPersistService persistService, final ContextManagerBuilderParameter parameter) {
        if (!parameter.isEmpty()) {
            persistService.persistConfigurations(parameter.getDatabaseConfigs(), parameter.getGlobalRuleConfigs(), parameter.getProps(), parameter.getModeConfiguration().isOverwrite());
        }
    }
    
    private InstanceContext buildInstanceContext(final RegistryCenter registryCenter, final ContextManagerBuilderParameter parameter) {
        return new InstanceContext(new ComputeNodeInstance(parameter.getInstanceMetaData()), new ClusterWorkerIdGenerator(registryCenter, parameter.getInstanceMetaData()),
                parameter.getModeConfiguration(), new ShardingSphereLockContext(registryCenter.getLockPersistService()),
                registryCenter.getEventBusContext(), ScheduleContextFactory.newInstance(parameter.getModeConfiguration()));
    }
    
    private void persistMetaData(final MetaDataContexts metaDataContexts) {
        metaDataContexts.getMetaData().getDatabases().values().forEach(each -> each.getSchemas()
                .forEach((schemaName, schema) -> metaDataContexts.getPersistService().getDatabaseMetaDataService().persist(each.getName(), schemaName, schema)));
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
