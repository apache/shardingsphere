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

package org.apache.shardingsphere.driver.governance.internal.datasource;

import com.google.common.base.Preconditions;
import lombok.Getter;
import org.apache.shardingsphere.driver.governance.internal.state.DriverStateContext;
import org.apache.shardingsphere.driver.jdbc.unsupported.AbstractUnsupportedOperationDataSource;
import org.apache.shardingsphere.governance.context.ClusterContextManager;
import org.apache.shardingsphere.governance.core.registry.RegistryCenter;
import org.apache.shardingsphere.governance.repository.spi.RegistryCenterRepository;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.datasource.DataSourceConfiguration;
import org.apache.shardingsphere.infra.config.datasource.DataSourceConverter;
import org.apache.shardingsphere.infra.config.properties.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.config.scope.GlobalRuleConfiguration;
import org.apache.shardingsphere.infra.config.scope.SchemaRuleConfiguration;
import org.apache.shardingsphere.infra.context.manager.ContextManager;
import org.apache.shardingsphere.infra.context.metadata.MetaDataContextsBuilder;
import org.apache.shardingsphere.infra.context.metadata.MetaDataContexts;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.mode.ShardingSphereMode;
import org.apache.shardingsphere.infra.mode.builder.ModeBuilderEngine;
import org.apache.shardingsphere.infra.mode.config.ModeConfiguration;
import org.apache.shardingsphere.infra.mode.repository.PersistRepository;
import org.apache.shardingsphere.infra.persist.DistMetaDataPersistService;
import org.apache.shardingsphere.transaction.ShardingTransactionManagerEngine;
import org.apache.shardingsphere.transaction.context.TransactionContexts;
import org.apache.shardingsphere.transaction.core.TransactionTypeHolder;

import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Governance ShardingSphere data source.
 */
public final class GovernanceShardingSphereDataSource extends AbstractUnsupportedOperationDataSource implements AutoCloseable {
    
    private final String schemaName;
    
    private final ShardingSphereMode mode;
    
    @Getter
    private final ContextManager contextManager;
    
    public GovernanceShardingSphereDataSource(final String schemaName, final ModeConfiguration modeConfig) throws SQLException {
        this.schemaName = schemaName;
        mode = ModeBuilderEngine.build(modeConfig);
        Optional<PersistRepository> persistRepository = mode.getPersistRepository();
        Preconditions.checkState(persistRepository.isPresent());
        DistMetaDataPersistService persistService = new DistMetaDataPersistService(persistRepository.get());
        RegistryCenter registryCenter = new RegistryCenter((RegistryCenterRepository) persistRepository.get());
        MetaDataContexts metaDataContexts = createMetaDataContexts(persistService);
        String xaTransactionMangerType = metaDataContexts.getProps().getValue(ConfigurationPropertyKey.XA_TRANSACTION_MANAGER_TYPE);
        TransactionContexts transactionContexts = createTransactionContexts(
                metaDataContexts.getMetaData(schemaName).getResource().getDatabaseType(), metaDataContexts.getMetaData(schemaName).getResource().getDataSources(), xaTransactionMangerType);
        contextManager = new ClusterContextManager(persistService, registryCenter);
        contextManager.init(metaDataContexts, transactionContexts);
    }
    
    public GovernanceShardingSphereDataSource(final String schemaName, final ModeConfiguration modeConfig, final Map<String, DataSource> dataSourceMap, 
                                              final Collection<RuleConfiguration> ruleConfigs, final Properties props) throws SQLException {
        this.schemaName = schemaName;
        mode = ModeBuilderEngine.build(modeConfig);
        Optional<PersistRepository> persistRepository = mode.getPersistRepository();
        Preconditions.checkState(persistRepository.isPresent());
        DistMetaDataPersistService persistService = new DistMetaDataPersistService(persistRepository.get());
        RegistryCenter registryCenter = new RegistryCenter((RegistryCenterRepository) persistRepository.get());
        MetaDataContexts metaDataContexts = createMetaDataContexts(persistService, dataSourceMap, ruleConfigs, props);
        String xaTransactionMangerType = metaDataContexts.getProps().getValue(ConfigurationPropertyKey.XA_TRANSACTION_MANAGER_TYPE);
        TransactionContexts transactionContexts = createTransactionContexts(
                metaDataContexts.getMetaData(schemaName).getResource().getDatabaseType(), metaDataContexts.getMetaData(schemaName).getResource().getDataSources(), xaTransactionMangerType);
        contextManager = new ClusterContextManager(persistService, registryCenter);
        contextManager.init(metaDataContexts, transactionContexts);
        uploadLocalConfiguration(persistService, registryCenter, ruleConfigs, modeConfig.isOverwrite());
    }
    
    private MetaDataContexts createMetaDataContexts(final DistMetaDataPersistService persistService) throws SQLException {
        Map<String, DataSourceConfiguration> dataSourceConfigs = persistService.getDataSourceService().load(schemaName);
        Collection<RuleConfiguration> ruleConfigs = persistService.getSchemaRuleService().load(schemaName);
        Map<String, DataSource> dataSourceMap = DataSourceConverter.getDataSourceMap(dataSourceConfigs);
        MetaDataContextsBuilder metaDataContextsBuilder = new MetaDataContextsBuilder(Collections.singletonMap(schemaName, dataSourceMap),
                Collections.singletonMap(schemaName, ruleConfigs), persistService.getGlobalRuleService().load(), persistService.getPropsService().load());
        return metaDataContextsBuilder.build(persistService);
    }
    
    private MetaDataContexts createMetaDataContexts(final DistMetaDataPersistService persistService,
                                                    final Map<String, DataSource> dataSourceMap, final Collection<RuleConfiguration> ruleConfigs, final Properties props) throws SQLException {
        MetaDataContextsBuilder metaDataContextsBuilder = new MetaDataContextsBuilder(
                Collections.singletonMap(schemaName, dataSourceMap), Collections.singletonMap(schemaName, ruleConfigs), props);
        return metaDataContextsBuilder.build(persistService);
    }
    
    private TransactionContexts createTransactionContexts(final DatabaseType databaseType, final Map<String, DataSource> dataSourceMap, final String xaTransactionMangerType) {
        ShardingTransactionManagerEngine engine = new ShardingTransactionManagerEngine();
        engine.init(databaseType, dataSourceMap, xaTransactionMangerType);
        Map<String, ShardingTransactionManagerEngine> engines = new HashMap<>(1, 1);
        engines.put(schemaName, engine);
        return new TransactionContexts(engines);
    }
    
    private void uploadLocalConfiguration(final DistMetaDataPersistService persistService, 
                                          final RegistryCenter registryCenter, final Collection<RuleConfiguration> ruleConfigs, final boolean isOverwrite) {
        Map<String, DataSourceConfiguration> dataSourceConfigs = DataSourceConverter.getDataSourceConfigurationMap(
                contextManager.getMetaDataContexts().getMetaData(schemaName).getResource().getDataSources());
        Collection<RuleConfiguration> schemaRuleConfigs = ruleConfigs.stream().filter(each -> each instanceof SchemaRuleConfiguration).collect(Collectors.toList());
        Collection<RuleConfiguration> globalRuleConfigs = ruleConfigs.stream().filter(each -> each instanceof GlobalRuleConfiguration).collect(Collectors.toList());
        persistService.persistConfigurations(Collections.singletonMap(schemaName, dataSourceConfigs),
                Collections.singletonMap(schemaName, schemaRuleConfigs), globalRuleConfigs, contextManager.getMetaDataContexts().getProps().getProps(), isOverwrite);
        registryCenter.onlineInstance(Collections.singletonList(schemaName));
    }
    
    @Override
    public Connection getConnection() {
        return DriverStateContext.getConnection(schemaName, getDataSourceMap(), contextManager, TransactionTypeHolder.get());
    }
    
    @Override
    public Connection getConnection(final String username, final String password) {
        return getConnection();
    }
    
    @Override
    public void close() throws Exception {
        getDataSourceMap().forEach((key, value) -> close(value));
        contextManager.getMetaDataContexts().close();
        mode.close();
    }
    
    private void close(final DataSource dataSource) {
        try {
            Method method = dataSource.getClass().getDeclaredMethod("close");
            method.setAccessible(true);
            method.invoke(dataSource);
        } catch (final ReflectiveOperationException ignored) {
        }
    }
    
    private Map<String, DataSource> getDataSourceMap() {
        return contextManager.getMetaDataContexts().getMetaData(schemaName).getResource().getDataSources();
    }
}
