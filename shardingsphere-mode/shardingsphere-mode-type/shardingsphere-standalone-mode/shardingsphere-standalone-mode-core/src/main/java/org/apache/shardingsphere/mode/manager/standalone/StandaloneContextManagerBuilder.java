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

package org.apache.shardingsphere.mode.manager.standalone;

import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.schema.SchemaConfiguration;
import org.apache.shardingsphere.infra.config.schema.impl.DataSourceProvidedSchemaConfiguration;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeFactory;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.instance.definition.InstanceType;
import org.apache.shardingsphere.infra.rule.identifier.type.InstanceAwareRule;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.ContextManagerBuilder;
import org.apache.shardingsphere.mode.manager.ContextManagerBuilderParameter;
import org.apache.shardingsphere.mode.manager.standalone.lock.StandaloneLockContext;
import org.apache.shardingsphere.mode.manager.standalone.workerid.generator.StandaloneWorkerIdGenerator;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.MetaDataContextsBuilder;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.mode.repository.standalone.StandalonePersistRepositoryFactory;
import org.apache.shardingsphere.transaction.context.TransactionContexts;
import org.apache.shardingsphere.transaction.context.TransactionContextsBuilder;
import org.apache.shardingsphere.transaction.rule.TransactionRule;
import org.apache.shardingsphere.transaction.spi.TransactionConfigurationFileGenerator;
import org.apache.shardingsphere.transaction.spi.TransactionConfigurationFileGeneratorFactory;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

/**
 * Standalone context manager builder.
 */
public final class StandaloneContextManagerBuilder implements ContextManagerBuilder {
    
    @Override
    public ContextManager build(final ContextManagerBuilderParameter parameter) throws SQLException {
        MetaDataPersistService metaDataPersistService = new MetaDataPersistService(StandalonePersistRepositoryFactory.newInstance(parameter.getModeConfig().getRepository()));
        persistConfigurations(metaDataPersistService, parameter);
        MetaDataContexts metaDataContexts = createMetaDataContexts(metaDataPersistService, parameter);
        return createContextManager(metaDataPersistService, parameter, metaDataContexts);
    }
    
    private void persistConfigurations(final MetaDataPersistService metaDataPersistService, final ContextManagerBuilderParameter parameter) {
        if (!parameter.isEmpty()) {
            metaDataPersistService.persistConfigurations(parameter.getSchemaConfigs(), parameter.getGlobalRuleConfigs(), parameter.getProps(), parameter.getModeConfig().isOverwrite());
        }
    }
    
    private MetaDataContexts createMetaDataContexts(final MetaDataPersistService metaDataPersistService, final ContextManagerBuilderParameter parameter) throws SQLException {
        Collection<RuleConfiguration> globalRuleConfigs = metaDataPersistService.getGlobalRuleService().load();
        Properties props = metaDataPersistService.getPropsService().load();
        MetaDataContextsBuilder builder = new MetaDataContextsBuilder(globalRuleConfigs, props);
        Collection<String> schemaNames = InstanceType.JDBC == parameter.getInstanceDefinition().getInstanceType()
                ? parameter.getSchemaConfigs().keySet() : metaDataPersistService.getSchemaMetaDataService().loadAllNames();
        DatabaseType databaseType = DatabaseTypeFactory.getDatabaseType(parameter.getSchemaConfigs(), new ConfigurationProperties(parameter.getProps()));
        for (String each : schemaNames) {
            if (databaseType.getSystemSchemas().contains(each)) {
                continue;
            }
            builder.addSchema(each, databaseType, createSchemaConfiguration(each, metaDataPersistService, parameter), props);
        }
        builder.addSystemSchemas(databaseType);
        return builder.build(metaDataPersistService);
    }
    
    private SchemaConfiguration createSchemaConfiguration(final String schemaName, final MetaDataPersistService metaDataPersistService,
                                                          final ContextManagerBuilderParameter parameter) {
        Map<String, DataSource> dataSources = metaDataPersistService.getEffectiveDataSources(schemaName, parameter.getSchemaConfigs());
        Collection<RuleConfiguration> schemaRuleConfigs = metaDataPersistService.getSchemaRuleService().load(schemaName);
        return new DataSourceProvidedSchemaConfiguration(dataSources, schemaRuleConfigs);
    }
    
    private ContextManager createContextManager(final MetaDataPersistService metaDataPersistService, final ContextManagerBuilderParameter parameter, final MetaDataContexts metaDataContexts) {
        ContextManager result = new ContextManager();
        InstanceContext instanceContext = new InstanceContext(metaDataPersistService.getComputeNodePersistService().loadComputeNodeInstance(parameter.getInstanceDefinition()),
                new StandaloneWorkerIdGenerator(), parameter.getModeConfig(), new StandaloneLockContext());
        generateTransactionConfigurationFile(instanceContext, metaDataContexts);
        TransactionContexts transactionContexts = new TransactionContextsBuilder(metaDataContexts.getMetaDataMap(), metaDataContexts.getGlobalRuleMetaData().getRules()).build();
        result.init(metaDataContexts, transactionContexts, instanceContext);
        setInstanceContext(result);
        return result;
    }
    
    private void generateTransactionConfigurationFile(final InstanceContext instanceContext, final MetaDataContexts metaDataContexts) {
        Optional<TransactionRule> transactionRule =
                metaDataContexts.getGlobalRuleMetaData().getRules().stream().filter(each -> each instanceof TransactionRule).map(each -> (TransactionRule) each).findFirst();
        if (transactionRule.isPresent()) {
            Optional<TransactionConfigurationFileGenerator> fileGenerator = TransactionConfigurationFileGeneratorFactory.newInstance(transactionRule.get().getProviderType());
            fileGenerator.ifPresent(optional -> optional.generateFile(transactionRule.get().getProps(), instanceContext));
        }
    }
    
    private void setInstanceContext(final ContextManager contextManager) {
        contextManager.getMetaDataContexts().getMetaDataMap()
            .forEach((key, value) -> value.getRuleMetaData().getRules().stream().filter(each -> each instanceof InstanceAwareRule)
            .forEach(each -> ((InstanceAwareRule) each).setInstanceContext(contextManager.getInstanceContext())));
    }
    
    @Override
    public String getType() {
        return "Standalone";
    }
}
