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
import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.infra.config.schema.SchemaConfiguration;
import org.apache.shardingsphere.infra.config.schema.impl.DataSourceProvidedSchemaConfiguration;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.instance.definition.InstanceDefinition;
import org.apache.shardingsphere.infra.instance.definition.InstanceType;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.ContextManagerBuilder;
import org.apache.shardingsphere.mode.manager.ContextManagerBuilderParameter;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.ClusterContextManagerCoordinator;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.RegistryCenter;
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
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Properties;

/**
 * Cluster context manager builder.
 */
public final class ClusterContextManagerBuilder implements ContextManagerBuilder {
    
    @Override
    public ContextManager build(final ContextManagerBuilderParameter parameter) throws SQLException {
        ModeScheduleContextFactory.getInstance().init(parameter.getInstanceDefinition().getInstanceId().getId(), parameter.getModeConfig());
        ClusterPersistRepository repository = ClusterPersistRepositoryFactory.newInstance((ClusterPersistRepositoryConfiguration) parameter.getModeConfig().getRepository());
        MetaDataPersistService metaDataPersistService = new MetaDataPersistService(repository);
        persistConfigurations(metaDataPersistService, parameter);
        MetaDataContextsBuilder metaDataContextsBuilder = createMetaDataContextsBuilder(metaDataPersistService, parameter);
        persistTransactionConfiguration(metaDataContextsBuilder.getSchemaConfigMap(), metaDataContextsBuilder.getGlobalRuleConfigs(), metaDataPersistService);
        persistMetaData(metaDataPersistService, metaDataContextsBuilder.getSchemaMap());
        MetaDataContexts metaDataContexts = metaDataContextsBuilder.build(metaDataPersistService);
        ContextManager result = createContextManager(repository, metaDataPersistService,
                parameter.getInstanceDefinition(), metaDataContexts, parameter.getModeConfig());
        registerOnline(repository, metaDataPersistService, parameter.getInstanceDefinition(), result);
        return result;
    }
    
    private MetaDataContextsBuilder createMetaDataContextsBuilder(final MetaDataPersistService metaDataPersistService, final ContextManagerBuilderParameter parameter) throws SQLException {
        Collection<String> schemaNames = InstanceType.JDBC == parameter.getInstanceDefinition().getInstanceType()
                ? parameter.getSchemaConfigs().keySet() : metaDataPersistService.getSchemaMetaDataService().loadAllNames();
        Collection<RuleConfiguration> globalRuleConfigs = metaDataPersistService.getGlobalRuleService().load();
        Properties props = metaDataPersistService.getPropsService().load();
        MetaDataContextsBuilder result = new MetaDataContextsBuilder(globalRuleConfigs, props);
        for (String each : schemaNames) {
            Map<String, DataSource> dataSources = metaDataPersistService.getEffectiveDataSources(each, parameter.getSchemaConfigs());
            Collection<RuleConfiguration> schemaRuleConfigs = metaDataPersistService.getSchemaRuleService().load(each);
            result.addSchema(each, new DataSourceProvidedSchemaConfiguration(dataSources, schemaRuleConfigs), props);
        }
        return result;
    }
    
    private void persistConfigurations(final MetaDataPersistService metaDataPersistService, final ContextManagerBuilderParameter parameter) {
        boolean isOverwrite = parameter.getModeConfig().isOverwrite();
        if (!parameter.isEmpty()) {
            metaDataPersistService.persistConfigurations(parameter.getSchemaConfigs(), parameter.getGlobalRuleConfigs(), parameter.getProps(), isOverwrite);
        }
        metaDataPersistService.persistInstanceLabels(parameter.getInstanceDefinition().getInstanceId().getId(), parameter.getLabels(), isOverwrite);
    }
    
    private void persistMetaData(final MetaDataPersistService metaDataPersistService, final Map<String, ShardingSphereSchema> schemaMap) {
        for (Entry<String, ShardingSphereSchema> entry : schemaMap.entrySet()) {
            metaDataPersistService.getSchemaMetaDataService().persist(entry.getKey(), entry.getValue());
        }
    }
    
    private ContextManager createContextManager(final ClusterPersistRepository repository,
                                                final MetaDataPersistService metaDataPersistService, final InstanceDefinition instanceDefinition, final MetaDataContexts metaDataContexts, 
                                                final ModeConfiguration modeConfiguration) {
        InstanceContext instanceContext = new InstanceContext(metaDataPersistService.getComputeNodePersistService().loadComputeNodeInstance(instanceDefinition),
                new ClusterWorkerIdGenerator(repository, metaDataPersistService, instanceDefinition), modeConfiguration);
        generateTransactionConfigurationFile(instanceContext, metaDataContexts);
        TransactionContexts transactionContexts = new TransactionContextsBuilder(metaDataContexts.getMetaDataMap(), metaDataContexts.getGlobalRuleMetaData().getRules()).build();
        ContextManager result = new ContextManager();
        result.init(metaDataContexts, transactionContexts, instanceContext);
        return result;
    }
    
    private void persistTransactionConfiguration(final Map<String, SchemaConfiguration> schemaConfigurationMap, final Collection<RuleConfiguration> globalRuleConfigs,
                                                 final MetaDataPersistService metaDataPersistService) {
        Optional<TransactionRuleConfiguration> transactionRuleConfiguration =
                globalRuleConfigs.stream().filter(each -> each instanceof TransactionRuleConfiguration).map(each -> (TransactionRuleConfiguration) each).findFirst();
        Optional<SchemaConfiguration> schemaConfiguration = schemaConfigurationMap.values().stream().findFirst();
        if (transactionRuleConfiguration.isPresent() && schemaConfiguration.isPresent()) {
            Optional<TransactionConfigurationFileGenerator> fileGenerator = TransactionConfigurationFileGeneratorFactory.newInstance(transactionRuleConfiguration.get().getProviderType());
            if (fileGenerator.isPresent()) {
                Properties props = fileGenerator.get().getTransactionProps(transactionRuleConfiguration.get(), schemaConfiguration.get());
                metaDataPersistService.persistTransactionRule(props, true);
            }
        }
    }
    
    private void generateTransactionConfigurationFile(final InstanceContext instanceContext, final MetaDataContexts metaDataContexts) {
        Optional<TransactionRule> transactionRule =
                metaDataContexts.getGlobalRuleMetaData().getRules().stream().filter(each -> each instanceof TransactionRule).map(each -> (TransactionRule) each).findFirst();
        Optional<ShardingSphereMetaData> shardingSphereMetaData = metaDataContexts.getMetaDataMap().values().stream().findFirst();
        if (transactionRule.isPresent() && shardingSphereMetaData.isPresent()) {
            Optional<TransactionConfigurationFileGenerator> fileGenerator = TransactionConfigurationFileGeneratorFactory.newInstance(transactionRule.get().getProviderType());
            fileGenerator.ifPresent(optional -> optional.generateFile(transactionRule.get(), instanceContext));
        }
    }
    
    private void registerOnline(final ClusterPersistRepository repository, 
                                final MetaDataPersistService metaDataPersistService, final InstanceDefinition instanceDefinition, final ContextManager contextManager) {
        RegistryCenter registryCenter = new RegistryCenter(repository);
        new ClusterContextManagerCoordinator(metaDataPersistService, contextManager, registryCenter);
        registryCenter.onlineInstance(instanceDefinition);
    }
    
    @Override
    public String getType() {
        return "Cluster";
    }
}
