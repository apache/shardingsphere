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
import org.apache.shardingsphere.infra.config.database.DatabaseConfiguration;
import org.apache.shardingsphere.infra.config.database.impl.DataSourceProvidedDatabaseConfiguration;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstance;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.instance.definition.InstanceType;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.ContextManagerBuilder;
import org.apache.shardingsphere.mode.manager.ContextManagerBuilderParameter;
import org.apache.shardingsphere.mode.manager.standalone.lock.StandaloneLockContext;
import org.apache.shardingsphere.mode.manager.standalone.workerid.generator.StandaloneWorkerIdGenerator;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.MetaDataContextsBuilder;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.mode.repository.standalone.StandalonePersistRepositoryFactory;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Standalone context manager builder.
 */
public final class StandaloneContextManagerBuilder implements ContextManagerBuilder {
    
    @Override
    public ContextManager build(final ContextManagerBuilderParameter parameter) throws SQLException {
        MetaDataPersistService metaDataPersistService = new MetaDataPersistService(StandalonePersistRepositoryFactory.getInstance(parameter.getModeConfig().getRepository()));
        persistConfigurations(metaDataPersistService, parameter);
        MetaDataContexts metaDataContexts = createMetaDataContextsBuilder(metaDataPersistService, parameter).build(metaDataPersistService);
        return createContextManager(parameter, metaDataContexts);
    }
    
    private void persistConfigurations(final MetaDataPersistService metaDataPersistService, final ContextManagerBuilderParameter parameter) {
        if (!parameter.isEmpty()) {
            metaDataPersistService.persistConfigurations(parameter.getDatabaseConfigs(), parameter.getGlobalRuleConfigs(), parameter.getProps(), parameter.getModeConfig().isOverwrite());
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
    
    private ContextManager createContextManager(final ContextManagerBuilderParameter parameter, final MetaDataContexts metaDataContexts) {
        InstanceContext instanceContext = new InstanceContext(new ComputeNodeInstance(parameter.getInstanceDefinition()), new StandaloneWorkerIdGenerator(), parameter.getModeConfig(),
                new StandaloneLockContext());
        ContextManager result = new ContextManager(metaDataContexts, instanceContext);
        return result;
    }
    
    @Override
    public String getType() {
        return "Standalone";
    }
}
