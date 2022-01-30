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
import org.apache.shardingsphere.infra.config.schema.impl.DataSourceProvidedSchemaConfiguration;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.instance.definition.InstanceType;
import org.apache.shardingsphere.infra.rule.identifier.type.InstanceAwareRule;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.ContextManagerBuilder;
import org.apache.shardingsphere.mode.manager.ContextManagerBuilderParameter;
import org.apache.shardingsphere.mode.manager.standalone.workerid.generator.StandaloneWorkerIdGenerator;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.MetaDataContextsBuilder;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.mode.repository.standalone.StandalonePersistRepositoryFactory;
import org.apache.shardingsphere.transaction.context.TransactionContexts;
import org.apache.shardingsphere.transaction.context.TransactionContextsBuilder;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;

/**
 * Standalone context manager builder.
 */
public final class StandaloneContextManagerBuilder implements ContextManagerBuilder {
    
    @Override
    public ContextManager build(final ContextManagerBuilderParameter parameter) throws SQLException {
        MetaDataPersistService metaDataPersistService = new MetaDataPersistService(StandalonePersistRepositoryFactory.newInstance(parameter.getModeConfig().getRepository()));
        persistConfigurations(metaDataPersistService, parameter);
        return createContextManager(metaDataPersistService, parameter, createMetaDataContexts(metaDataPersistService, parameter));
    }
    
    private MetaDataContexts createMetaDataContexts(final MetaDataPersistService metaDataPersistService, final ContextManagerBuilderParameter parameter) throws SQLException {
        Collection<RuleConfiguration> globalRuleConfigs = metaDataPersistService.getGlobalRuleService().load();
        Properties props = metaDataPersistService.getPropsService().load();
        MetaDataContextsBuilder builder = new MetaDataContextsBuilder(globalRuleConfigs, props);
        Collection<String> schemaNames = InstanceType.JDBC == parameter.getInstanceDefinition().getInstanceType()
                ? parameter.getSchemaConfigs().keySet() : metaDataPersistService.getSchemaMetaDataService().loadAllNames();
        for (String each : schemaNames) {
            Map<String, DataSource> dataSources = metaDataPersistService.getEffectiveDataSources(each, parameter.getSchemaConfigs());
            Collection<RuleConfiguration> schemaRuleConfigs = metaDataPersistService.getSchemaRuleService().load(each);
            builder.addSchema(each, new DataSourceProvidedSchemaConfiguration(dataSources, schemaRuleConfigs), props);
        }
        return builder.build(metaDataPersistService);
    }
    
    private void persistConfigurations(final MetaDataPersistService metaDataPersistService, final ContextManagerBuilderParameter parameter) {
        if (!parameter.isEmpty()) {
            metaDataPersistService.persistConfigurations(parameter.getSchemaConfigs(), parameter.getGlobalRuleConfigs(), parameter.getProps(), parameter.getModeConfig().isOverwrite());
        }
    }
    
    private ContextManager createContextManager(final MetaDataPersistService metaDataPersistService, final ContextManagerBuilderParameter parameter, final MetaDataContexts metaDataContexts) {
        ContextManager result = new ContextManager();
        TransactionContexts transactionContexts = new TransactionContextsBuilder(metaDataContexts.getMetaDataMap(), metaDataContexts.getGlobalRuleMetaData().getRules()).build();
        InstanceContext instanceContext = new InstanceContext(
                metaDataPersistService.getComputeNodePersistService().loadComputeNodeInstance(parameter.getInstanceDefinition()), new StandaloneWorkerIdGenerator(), parameter.getModeConfig());
        result.init(metaDataContexts, transactionContexts, instanceContext);
        setInstanceContext(result);
        return result;
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
