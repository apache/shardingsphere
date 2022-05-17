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
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.database.DatabaseConfiguration;
import org.apache.shardingsphere.infra.config.database.impl.DataSourceProvidedDatabaseConfiguration;
import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstance;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.instance.definition.InstanceDefinition;
import org.apache.shardingsphere.infra.instance.definition.InstanceType;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.ContextManagerBuilder;
import org.apache.shardingsphere.mode.manager.ContextManagerBuilderParameter;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.ClusterContextManagerCoordinator;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.RegistryCenter;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.DistributeLockContext;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.workerid.generator.ClusterWorkerIdGenerator;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.MetaDataContextsBuilder;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepositoryConfiguration;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepositoryFactory;
import org.apache.shardingsphere.schedule.core.api.ModeScheduleContextFactory;
import org.apache.shardingsphere.transaction.config.TransactionRuleConfiguration;
import org.apache.shardingsphere.transaction.context.TransactionContexts;
import org.apache.shardingsphere.transaction.context.TransactionContextsBuilder;
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
        ModeScheduleContextFactory.getInstance().init(parameter.getInstanceDefinition().getInstanceId().getId(), parameter.getModeConfig());
        ClusterPersistRepository repository = ClusterPersistRepositoryFactory.getInstance((ClusterPersistRepositoryConfiguration) parameter.getModeConfig().getRepository());
        MetaDataPersistService metaDataPersistService = new MetaDataPersistService(repository);
        persistConfigurations(metaDataPersistService, parameter);
        RegistryCenter registryCenter = new RegistryCenter(repository);
        MetaDataContextsBuilder builder = createMetaDataContextsBuilder(metaDataPersistService, parameter);
        MetaDataContexts metaDataContexts = builder.build(metaDataPersistService);
        persistMetaData(metaDataContexts);
        Properties transactionProps = getTransactionProperties(metaDataContexts);
        persistTransactionConfiguration(parameter, metaDataPersistService, transactionProps);
        ContextManager result = createContextManager(repository, metaDataPersistService, parameter.getInstanceDefinition(), metaDataContexts, transactionProps, parameter.getModeConfig());
        registerOnline(metaDataPersistService, parameter.getInstanceDefinition(), result, registryCenter);
        return result;
    }
    
    private void persistConfigurations(final MetaDataPersistService metaDataPersistService, final ContextManagerBuilderParameter parameter) {
        boolean isOverwrite = parameter.getModeConfig().isOverwrite();
        if (!parameter.isEmpty()) {
            metaDataPersistService.persistConfigurations(parameter.getDatabaseConfigs(), parameter.getGlobalRuleConfigs(), parameter.getProps(), isOverwrite);
        }
        metaDataPersistService.persistInstanceLabels(parameter.getInstanceDefinition().getInstanceId().getId(), parameter.getLabels(), isOverwrite);
    }
    
    private MetaDataContextsBuilder createMetaDataContextsBuilder(final MetaDataPersistService metaDataPersistService, final ContextManagerBuilderParameter parameter) throws SQLException {
        Collection<String> databaseNames = InstanceType.JDBC == parameter.getInstanceDefinition().getInstanceType()
                ? parameter.getDatabaseConfigs().keySet()
                : metaDataPersistService.getSchemaMetaDataService().loadAllDatabaseNames();
        Collection<RuleConfiguration> globalRuleConfigs = metaDataPersistService.getGlobalRuleService().load();
        ConfigurationProperties props = new ConfigurationProperties(metaDataPersistService.getPropsService().load());
        Map<String, DatabaseConfiguration> databaseConfigMap = getDatabaseConfigMap(databaseNames, metaDataPersistService, parameter);
        return new MetaDataContextsBuilder(databaseConfigMap, globalRuleConfigs, props);
    }
    
    private Properties getTransactionProperties(final MetaDataContexts metaDataContexts) {
        Optional<String> databaseName = metaDataContexts.getAllDatabaseNames().stream().findFirst();
        Optional<TransactionRule> transactionRule =
                metaDataContexts.getGlobalRuleMetaData().getRules().stream().filter(each -> each instanceof TransactionRule).map(each -> (TransactionRule) each).findFirst();
        Optional<TransactionConfigurationFileGenerator> fileGenerator = transactionRule.isPresent()
                ? TransactionConfigurationFileGeneratorFactory.findInstance(transactionRule.get().getProviderType())
                : Optional.empty();
        if (!databaseName.isPresent() || !fileGenerator.isPresent()) {
            return transactionRule.isPresent() ? transactionRule.get().getProps() : new Properties();
        }
        ShardingSphereMetaData metaData = metaDataContexts.getMetaData(databaseName.get());
        Properties result = fileGenerator.get().getTransactionProps(transactionRule.get().getProps(),
                new DataSourceProvidedDatabaseConfiguration(metaData.getResource().getDataSources(), metaData.getRuleMetaData().getConfigurations()), getType());
        Optional<TransactionRuleConfiguration> transactionRuleConfig = metaDataContexts.getGlobalRuleMetaData().findSingleRuleConfiguration(TransactionRuleConfiguration.class);
        Preconditions.checkState(transactionRuleConfig.isPresent());
        transactionRuleConfig.get().getProps().clear();
        transactionRuleConfig.get().getProps().putAll(result);
        transactionRule.get().getProps().clear();
        transactionRule.get().getProps().putAll(result);
        return result;
    }
    
    private void persistTransactionConfiguration(final ContextManagerBuilderParameter parameter, final MetaDataPersistService metaDataPersistService, final Properties transactionProps) {
        if (!transactionProps.isEmpty()) {
            metaDataPersistService.persistTransactionRule(transactionProps, true);
        }
        String instanceId = parameter.getInstanceDefinition().getInstanceId().getId();
        if (!metaDataPersistService.getComputeNodePersistService().loadXaRecoveryId(instanceId).isPresent()) {
            metaDataPersistService.getComputeNodePersistService().persistInstanceXaRecoveryId(instanceId, instanceId);
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
        metaDataContexts.getMetaDataMap().forEach((databaseName, schemas) -> schemas.getDatabase().getSchemas().forEach((schemaName, tables) -> {
            if (tables.getTables().isEmpty()) {
                metaDataContexts.getMetaDataPersistService().ifPresent(optional -> optional.getSchemaMetaDataService().persistSchema(databaseName, schemaName));
            } else {
                metaDataContexts.getMetaDataPersistService().ifPresent(optional -> optional.getSchemaMetaDataService().persistTables(databaseName, schemaName, tables));
            }
        }));
    }
    
    private ContextManager createContextManager(final ClusterPersistRepository repository, final MetaDataPersistService metaDataPersistService,
                                                final InstanceDefinition instanceDefinition, final MetaDataContexts metaDataContexts,
                                                final Properties transactionProps, final ModeConfiguration modeConfig) {
        ComputeNodeInstance computeNodeInstance = metaDataPersistService.getComputeNodePersistService().loadComputeNodeInstance(instanceDefinition);
        ClusterWorkerIdGenerator clusterWorkerIdGenerator = new ClusterWorkerIdGenerator(repository, metaDataPersistService, instanceDefinition);
        DistributeLockContext distributeLockContext = new DistributeLockContext(repository);
        InstanceContext instanceContext = new InstanceContext(computeNodeInstance, clusterWorkerIdGenerator, modeConfig, distributeLockContext);
        instanceContext.initLockContext();
        repository.watchSessionConnection(instanceContext);
        generateTransactionConfigurationFile(instanceContext, metaDataContexts, transactionProps);
        TransactionContexts transactionContexts = new TransactionContextsBuilder(metaDataContexts.getMetaDataMap(), metaDataContexts.getGlobalRuleMetaData().getRules()).build();
        ContextManager result = new ContextManager();
        result.init(metaDataContexts, transactionContexts, instanceContext);
        return result;
    }
    
    private void generateTransactionConfigurationFile(final InstanceContext instanceContext, final MetaDataContexts metaDataContexts, final Properties transactionProps) {
        Optional<TransactionRule> transactionRule = metaDataContexts.getGlobalRuleMetaData().getRules()
                .stream().filter(each -> each instanceof TransactionRule).map(each -> (TransactionRule) each).findFirst();
        if (transactionRule.isPresent()) {
            Optional<TransactionConfigurationFileGenerator> fileGenerator = TransactionConfigurationFileGeneratorFactory.findInstance(transactionRule.get().getProviderType());
            fileGenerator.ifPresent(optional -> optional.generateFile(transactionProps, instanceContext));
        }
    }
    
    private void registerOnline(final MetaDataPersistService metaDataPersistService, final InstanceDefinition instanceDefinition, final ContextManager contextManager,
                                final RegistryCenter registryCenter) {
        new ClusterContextManagerCoordinator(metaDataPersistService, contextManager, registryCenter);
        registryCenter.onlineInstance(instanceDefinition);
        contextManager.getInstanceContext().getComputeNodeInstances().addAll(metaDataPersistService.getComputeNodePersistService().loadAllComputeNodeInstances());
    }
    
    @Override
    public String getType() {
        return "Cluster";
    }
}
