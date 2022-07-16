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

import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.database.DatabaseConfiguration;
import org.apache.shardingsphere.infra.config.database.impl.DataSourceProvidedDatabaseConfiguration;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.eventbus.EventBusContext;
import org.apache.shardingsphere.infra.federation.optimizer.context.OptimizerContextFactory;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstance;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.instance.metadata.jdbc.JDBCInstanceMetaData;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabasesFactory;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.rule.builder.global.GlobalRulesBuilder;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.ContextManagerBuilder;
import org.apache.shardingsphere.mode.manager.ContextManagerBuilderParameter;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.ClusterContextManagerCoordinator;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.RegistryCenter;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.DistributedLockContext;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.workerid.generator.ClusterWorkerIdGenerator;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepositoryConfiguration;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepositoryFactory;
import org.apache.shardingsphere.schedule.core.ScheduleContextFactory;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

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
        RegistryCenter registryCenter = new RegistryCenter(repository, new EventBusContext());
        InstanceContext instanceContext = buildInstanceContext(registryCenter, parameter);
        registryCenter.getRepository().watchSessionConnection(instanceContext);
        MetaDataContexts metaDataContexts = buildMetaDataContexts(persistService, parameter, instanceContext);
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
                parameter.getModeConfiguration(), new DistributedLockContext(registryCenter.getRepository()), registryCenter.getEventBusContext());
    }
    
    private MetaDataContexts buildMetaDataContexts(final MetaDataPersistService persistService,
                                                   final ContextManagerBuilderParameter parameter, final InstanceContext instanceContext) throws SQLException {
        Collection<String> databaseNames = parameter.getInstanceMetaData() instanceof JDBCInstanceMetaData
                ? parameter.getDatabaseConfigs().keySet()
                : persistService.getDatabaseMetaDataService().loadAllDatabaseNames();
        Map<String, DatabaseConfiguration> databaseConfigMap = buildDatabaseConfigMap(databaseNames, persistService, parameter);
        Collection<RuleConfiguration> globalRuleConfigs = persistService.getGlobalRuleService().load();
        ConfigurationProperties props = new ConfigurationProperties(persistService.getPropsService().load());
        Map<String, ShardingSphereDatabase> databases = ShardingSphereDatabasesFactory.create(databaseConfigMap, props, instanceContext);
        ShardingSphereRuleMetaData globalMetaData = new ShardingSphereRuleMetaData(GlobalRulesBuilder.buildRules(globalRuleConfigs, databases, instanceContext));
        return new MetaDataContexts(persistService, new ShardingSphereMetaData(databases, globalMetaData, props), OptimizerContextFactory.create(databases, globalMetaData));
    }
    
    private Map<String, DatabaseConfiguration> buildDatabaseConfigMap(final Collection<String> databaseNames,
                                                                      final MetaDataPersistService persistService, final ContextManagerBuilderParameter parameter) {
        Map<String, DatabaseConfiguration> result = new HashMap<>(databaseNames.size(), 1);
        databaseNames.forEach(each -> result.put(each, buildDatabaseConfiguration(each, persistService, parameter)));
        return result;
    }
    
    private DatabaseConfiguration buildDatabaseConfiguration(final String databaseName, final MetaDataPersistService persistService, final ContextManagerBuilderParameter parameter) {
        Map<String, DataSource> dataSources = persistService.getEffectiveDataSources(databaseName, parameter.getDatabaseConfigs());
        Collection<RuleConfiguration> databaseRuleConfigs = persistService.getDatabaseRulePersistService().load(databaseName);
        return new DataSourceProvidedDatabaseConfiguration(dataSources, databaseRuleConfigs);
    }
    
    private void persistMetaData(final MetaDataContexts metaDataContexts) {
        metaDataContexts.getMetaData().getDatabases().forEach((databaseName, schemas) -> schemas.getSchemas()
                .forEach((schemaName, tables) -> metaDataContexts.getPersistService().getDatabaseMetaDataService().persistMetaData(databaseName, schemaName, tables)));
    }
    
    private void registerOnline(final MetaDataPersistService persistService, final RegistryCenter registryCenter,
                                final ContextManagerBuilderParameter parameter, final ContextManager contextManager) {
        contextManager.getInstanceContext().getInstance().setLabels(parameter.getLabels());
        contextManager.getInstanceContext().getComputeNodeInstances().addAll(registryCenter.getComputeNodeStatusService().loadAllComputeNodeInstances());
        new ClusterContextManagerCoordinator(persistService, registryCenter, contextManager);
        registryCenter.onlineInstance(contextManager.getInstanceContext().getInstance());
    }
    
    @Override
    public String getType() {
        return "Cluster";
    }
}
