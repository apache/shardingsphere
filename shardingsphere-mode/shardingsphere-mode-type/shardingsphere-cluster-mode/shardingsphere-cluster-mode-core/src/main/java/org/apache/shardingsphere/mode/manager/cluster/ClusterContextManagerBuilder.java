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
import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstance;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.instance.definition.InstanceDefinition;
import org.apache.shardingsphere.infra.instance.definition.InstanceType;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.ContextManagerBuilder;
import org.apache.shardingsphere.mode.manager.ContextManagerBuilderParameter;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.ClusterContextManagerCoordinator;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.RegistryCenter;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.DistributedLockContext;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.workerid.generator.ClusterWorkerIdGenerator;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.MetaDataContextsBuilder;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepositoryConfiguration;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepositoryFactory;
import org.apache.shardingsphere.schedule.core.api.ModeScheduleContextFactory;
import org.apache.shardingsphere.transaction.rule.TransactionRule;
import org.apache.shardingsphere.transaction.spi.TransactionConfigurationFileGenerator;
import org.apache.shardingsphere.transaction.spi.TransactionConfigurationFileGeneratorFactory;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

/**
 * Cluster context manager builder.
 */
public final class ClusterContextManagerBuilder implements ContextManagerBuilder {
    
    @Override
    public ContextManager build(final ContextManagerBuilderParameter parameter) throws SQLException {
        ModeScheduleContextFactory.getInstance().init(parameter.getInstanceDefinition().getInstanceId(), parameter.getModeConfig());
        ClusterPersistRepository repository = ClusterPersistRepositoryFactory.getInstance((ClusterPersistRepositoryConfiguration) parameter.getModeConfig().getRepository());
        MetaDataPersistService metaDataPersistService = new MetaDataPersistService(repository);
        persistConfigurations(metaDataPersistService, parameter);
        RegistryCenter registryCenter = new RegistryCenter(repository);
        MetaDataContexts metaDataContexts = createMetaDataContextsBuilder(metaDataPersistService, parameter).build(metaDataPersistService);
        persistMetaData(metaDataContexts);
        Properties transactionProps = getTransactionProperties(metaDataContexts);
        persistTransactionConfiguration(metaDataPersistService, transactionProps);
        ContextManager result = createContextManager(repository, registryCenter, parameter.getInstanceDefinition(), metaDataContexts, transactionProps, parameter.getModeConfig());
        registerOnline(metaDataPersistService, parameter, result, registryCenter);
        return result;
    }
    
    private void persistConfigurations(final MetaDataPersistService metaDataPersistService, final ContextManagerBuilderParameter parameter) {
        boolean isOverwrite = parameter.getModeConfig().isOverwrite();
        if (!parameter.isEmpty()) {
            metaDataPersistService.persistConfigurations(parameter.getDatabaseConfigs(), parameter.getGlobalRuleConfigs(), parameter.getProps(), isOverwrite);
        }
    }
    
    private MetaDataContextsBuilder createMetaDataContextsBuilder(final MetaDataPersistService metaDataPersistService, final ContextManagerBuilderParameter parameter) {
        Collection<String> databaseNames = InstanceType.JDBC == parameter.getInstanceDefinition().getInstanceType()
                ? parameter.getDatabaseConfigs().keySet()
                : metaDataPersistService.getSchemaMetaDataService().loadAllDatabaseNames();
        Map<String, DatabaseConfiguration> databaseConfigMap = getDatabaseConfigMap(databaseNames, metaDataPersistService, parameter);
        Collection<RuleConfiguration> globalRuleConfigs = metaDataPersistService.getGlobalRuleService().load();
        ConfigurationProperties props = new ConfigurationProperties(metaDataPersistService.getPropsService().load());
        return new MetaDataContextsBuilder(databaseConfigMap, globalRuleConfigs, props);
    }
    
    private Properties getTransactionProperties(final MetaDataContexts metaDataContexts) {
        Optional<String> databaseName = metaDataContexts.getMetaData().getDatabases().keySet().stream().findFirst();
        Optional<TransactionRule> transactionRule = metaDataContexts.getMetaData().getGlobalRuleMetaData().findSingleRule(TransactionRule.class);
        Optional<TransactionConfigurationFileGenerator> fileGenerator = transactionRule.isPresent()
                ? TransactionConfigurationFileGeneratorFactory.findInstance(transactionRule.get().getProviderType())
                : Optional.empty();
        if (!databaseName.isPresent() || !fileGenerator.isPresent()) {
            return transactionRule.isPresent() ? transactionRule.get().getProps() : new Properties();
        }
        ShardingSphereDatabase database = metaDataContexts.getMetaData().getDatabases().get(databaseName.get());
        Properties result = fileGenerator.get().getTransactionProps(transactionRule.get().getProps(),
                new DataSourceProvidedDatabaseConfiguration(database.getResource().getDataSources(), database.getRuleMetaData().getConfigurations()), getType());
        transactionRule.get().getProps().clear();
        transactionRule.get().getProps().putAll(result);
        transactionRule.get().getProps().clear();
        transactionRule.get().getProps().putAll(result);
        return result;
    }
    
    private void persistTransactionConfiguration(final MetaDataPersistService metaDataPersistService, final Properties transactionProps) {
        if (!transactionProps.isEmpty()) {
            metaDataPersistService.persistTransactionRule(transactionProps, true);
        }
    }
    
    private Map<String, DatabaseConfiguration> getDatabaseConfigMap(final Collection<String> databaseNames, final MetaDataPersistService metaDataPersistService,
                                                                    final ContextManagerBuilderParameter parameter) {
        Map<String, DatabaseConfiguration> result = new HashMap<>(databaseNames.size(), 1);
        databaseNames.forEach(each -> result.put(each, createDatabaseConfiguration(each, metaDataPersistService, parameter)));
        return result;
    }
    
    private DatabaseConfiguration createDatabaseConfiguration(final String databaseName, final MetaDataPersistService metaDataPersistService,
                                                              final ContextManagerBuilderParameter parameter) {
        Map<String, DataSource> dataSources = metaDataPersistService.getEffectiveDataSources(databaseName, parameter.getDatabaseConfigs());
        Collection<RuleConfiguration> databaseRuleConfigs = metaDataPersistService.getDatabaseRulePersistService().load(databaseName);
        return new DataSourceProvidedDatabaseConfiguration(dataSources, databaseRuleConfigs);
    }
    
    private void persistMetaData(final MetaDataContexts metaDataContexts) {
        metaDataContexts.getMetaData().getDatabases().forEach((databaseName, schemas) -> schemas.getSchemas()
                .forEach((schemaName, tables) -> metaDataContexts.getPersistService().ifPresent(optional -> optional.getSchemaMetaDataService().persistMetaData(databaseName, schemaName, tables))));
    }
    
    private ContextManager createContextManager(final ClusterPersistRepository repository, final RegistryCenter registryCenter,
                                                final InstanceDefinition instanceDefinition, final MetaDataContexts metaDataContexts,
                                                final Properties transactionProps, final ModeConfiguration modeConfig) {
        ClusterWorkerIdGenerator clusterWorkerIdGenerator = new ClusterWorkerIdGenerator(repository, registryCenter, instanceDefinition);
        DistributedLockContext distributedLockContext = new DistributedLockContext(repository);
        InstanceContext instanceContext = new InstanceContext(new ComputeNodeInstance(instanceDefinition), clusterWorkerIdGenerator, modeConfig, distributedLockContext);
        repository.watchSessionConnection(instanceContext);
        generateTransactionConfigurationFile(instanceContext, metaDataContexts, transactionProps);
        return new ContextManager(metaDataContexts, instanceContext);
    }
    
    private void generateTransactionConfigurationFile(final InstanceContext instanceContext, final MetaDataContexts metaDataContexts, final Properties transactionProps) {
        Optional<TransactionRule> transactionRule = metaDataContexts.getMetaData().getGlobalRuleMetaData().getRules()
                .stream().filter(each -> each instanceof TransactionRule).map(each -> (TransactionRule) each).findFirst();
        if (transactionRule.isPresent()) {
            Optional<TransactionConfigurationFileGenerator> fileGenerator = TransactionConfigurationFileGeneratorFactory.findInstance(transactionRule.get().getProviderType());
            fileGenerator.ifPresent(optional -> optional.generateFile(transactionProps, instanceContext));
        }
    }
    
    private void registerOnline(final MetaDataPersistService metaDataPersistService, final ContextManagerBuilderParameter parameter, final ContextManager contextManager,
                                final RegistryCenter registryCenter) {
        String instanceId = contextManager.getInstanceContext().getInstance().getCurrentInstanceId();
        contextManager.getInstanceContext().getInstance().getXaRecoveryIds().add(instanceId);
        contextManager.getInstanceContext().getInstance().setLabels(parameter.getLabels());
        contextManager.getInstanceContext().getComputeNodeInstances().addAll(registryCenter.getComputeNodeStatusService().loadAllComputeNodeInstances());
        new ClusterContextManagerCoordinator(metaDataPersistService, contextManager, registryCenter);
        registryCenter.onlineInstance(contextManager.getInstanceContext().getInstance());
    }
    
    @Override
    public String getType() {
        return "Cluster";
    }
}
