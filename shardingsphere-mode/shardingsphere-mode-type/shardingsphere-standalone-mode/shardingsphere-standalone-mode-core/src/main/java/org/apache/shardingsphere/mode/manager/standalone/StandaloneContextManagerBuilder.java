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
import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
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
import org.apache.shardingsphere.mode.manager.standalone.lock.StandaloneLockContext;
import org.apache.shardingsphere.mode.manager.standalone.workerid.generator.StandaloneWorkerIdGenerator;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
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
        ModeConfiguration modeConfig = null == parameter.getModeConfig() ? new ModeConfiguration("Standalone", null, true) : parameter.getModeConfig();
        MetaDataPersistService persistService = new MetaDataPersistService(StandalonePersistRepositoryFactory.getInstance(modeConfig.getRepository()));
        persistConfigurations(persistService, parameter, modeConfig);
        InstanceContext instanceContext = buildInstanceContext(parameter, modeConfig);
        return new ContextManager(buildMetaDataContexts(persistService, parameter, instanceContext), instanceContext);
    }
    
    private void persistConfigurations(final MetaDataPersistService persistService, final ContextManagerBuilderParameter parameter, final ModeConfiguration modeConfig) {
        if (!parameter.isEmpty()) {
            persistService.persistConfigurations(parameter.getDatabaseConfigs(), parameter.getGlobalRuleConfigs(), parameter.getProps(), modeConfig.isOverwrite());
        }
    }
    
    private InstanceContext buildInstanceContext(final ContextManagerBuilderParameter parameter, final ModeConfiguration modeConfig) {
        return new InstanceContext(new ComputeNodeInstance(parameter.getInstanceMetaData()), new StandaloneWorkerIdGenerator(), modeConfig, new StandaloneLockContext(),
                new EventBusContext());
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
    
    @Override
    public String getType() {
        return "Standalone";
    }
    
    @Override
    public boolean isDefault() {
        return true;
    }
}
