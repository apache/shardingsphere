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

import lombok.Getter;
import org.apache.shardingsphere.driver.governance.internal.state.DriverStateContext;
import org.apache.shardingsphere.driver.jdbc.unsupported.AbstractUnsupportedOperationDataSource;
import org.apache.shardingsphere.governance.context.metadata.GovernanceMetaDataContexts;
import org.apache.shardingsphere.governance.core.registry.RegistryCenter;
import org.apache.shardingsphere.governance.core.rule.GovernanceRule;
import org.apache.shardingsphere.governance.repository.api.config.GovernanceConfiguration;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.datasource.DataSourceConfiguration;
import org.apache.shardingsphere.infra.config.datasource.DataSourceConverter;
import org.apache.shardingsphere.infra.config.persist.DistMetaDataPersistService;
import org.apache.shardingsphere.infra.config.properties.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.config.scope.GlobalRuleConfiguration;
import org.apache.shardingsphere.infra.config.scope.SchemaRuleConfiguration;
import org.apache.shardingsphere.infra.context.metadata.MetaDataContexts;
import org.apache.shardingsphere.infra.context.metadata.MetaDataContextsBuilder;
import org.apache.shardingsphere.infra.context.metadata.impl.StandardMetaDataContexts;
import org.apache.shardingsphere.infra.database.DefaultSchema;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.transaction.ShardingTransactionManagerEngine;
import org.apache.shardingsphere.transaction.context.TransactionContexts;
import org.apache.shardingsphere.transaction.context.impl.StandardTransactionContexts;
import org.apache.shardingsphere.transaction.core.TransactionTypeHolder;

import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Governance ShardingSphere data source.
 */
public final class GovernanceShardingSphereDataSource extends AbstractUnsupportedOperationDataSource implements AutoCloseable {
    
    private final GovernanceRule governanceRule;
    
    @Getter
    private final MetaDataContexts metaDataContexts;
    
    @Getter
    private final TransactionContexts transactionContexts;
    
    public GovernanceShardingSphereDataSource(final GovernanceConfiguration governanceConfig) throws SQLException {
        // TODO new GovernanceRule from SPI
        governanceRule = new GovernanceRule(governanceConfig);
        DistMetaDataPersistService persistService = new DistMetaDataPersistService(governanceRule.getRegistryCenter().getRepository());
        metaDataContexts = new GovernanceMetaDataContexts(createMetaDataContexts(persistService), persistService, governanceRule.getRegistryCenter());
        String xaTransactionMangerType = metaDataContexts.getProps().getValue(ConfigurationPropertyKey.XA_TRANSACTION_MANAGER_TYPE);
        transactionContexts = createTransactionContexts(metaDataContexts.getDefaultMetaData().getResource().getDatabaseType(),
                metaDataContexts.getDefaultMetaData().getResource().getDataSources(), xaTransactionMangerType);
    }
    
    public GovernanceShardingSphereDataSource(final Map<String, DataSource> dataSourceMap, final Collection<RuleConfiguration> ruleConfigs, 
                                              final Properties props, final GovernanceConfiguration governanceConfig) throws SQLException {
        // TODO new GovernanceRule from SPI
        governanceRule = new GovernanceRule(governanceConfig);
        DistMetaDataPersistService persistService = new DistMetaDataPersistService(governanceRule.getRegistryCenter().getRepository());
        metaDataContexts = new GovernanceMetaDataContexts(createMetaDataContexts(persistService, dataSourceMap, ruleConfigs, props), persistService, governanceRule.getRegistryCenter());
        String xaTransactionMangerType = metaDataContexts.getProps().getValue(ConfigurationPropertyKey.XA_TRANSACTION_MANAGER_TYPE);
        transactionContexts = createTransactionContexts(metaDataContexts.getDefaultMetaData().getResource().getDatabaseType(),
                metaDataContexts.getDefaultMetaData().getResource().getDataSources(), xaTransactionMangerType);
        uploadLocalConfiguration(persistService, governanceRule.getRegistryCenter(), ruleConfigs, governanceConfig.isOverwrite());
    }
    
    private StandardMetaDataContexts createMetaDataContexts(final DistMetaDataPersistService persistService) throws SQLException {
        Map<String, DataSourceConfiguration> dataSourceConfigs = persistService.getDataSourceService().load(DefaultSchema.LOGIC_NAME);
        Collection<RuleConfiguration> ruleConfigs = persistService.getSchemaRuleService().load(DefaultSchema.LOGIC_NAME);
        Map<String, DataSource> dataSourceMap = DataSourceConverter.getDataSourceMap(dataSourceConfigs);
        MetaDataContextsBuilder metaDataContextsBuilder = new MetaDataContextsBuilder(Collections.singletonMap(DefaultSchema.LOGIC_NAME, dataSourceMap), 
                Collections.singletonMap(DefaultSchema.LOGIC_NAME, ruleConfigs), persistService.getGlobalRuleService().load(), persistService.getPropsService().load());
        return metaDataContextsBuilder.build(persistService);
    }
    
    private StandardMetaDataContexts createMetaDataContexts(final DistMetaDataPersistService persistService, 
                                                            final Map<String, DataSource> dataSourceMap, final Collection<RuleConfiguration> ruleConfigs, final Properties props) throws SQLException {
        MetaDataContextsBuilder metaDataContextsBuilder = new MetaDataContextsBuilder(
                Collections.singletonMap(DefaultSchema.LOGIC_NAME, dataSourceMap), Collections.singletonMap(DefaultSchema.LOGIC_NAME, ruleConfigs), props);
        return metaDataContextsBuilder.build(persistService);
    }
    
    private TransactionContexts createTransactionContexts(final DatabaseType databaseType, final Map<String, DataSource> dataSourceMap, final String xaTransactionMangerType) {
        ShardingTransactionManagerEngine engine = new ShardingTransactionManagerEngine();
        engine.init(databaseType, dataSourceMap, xaTransactionMangerType);
        return new StandardTransactionContexts(Collections.singletonMap(DefaultSchema.LOGIC_NAME, engine));
    }
    
    private void uploadLocalConfiguration(final DistMetaDataPersistService persistService, 
                                          final RegistryCenter registryCenter, final Collection<RuleConfiguration> ruleConfigs, final boolean isOverwrite) {
        Map<String, DataSourceConfiguration> dataSourceConfigs = DataSourceConverter.getDataSourceConfigurationMap(metaDataContexts.getDefaultMetaData().getResource().getDataSources());
        Collection<RuleConfiguration> schemaRuleConfigs = ruleConfigs.stream().filter(each -> each instanceof SchemaRuleConfiguration).collect(Collectors.toList());
        Collection<RuleConfiguration> globalRuleConfigs = ruleConfigs.stream().filter(each -> each instanceof GlobalRuleConfiguration).collect(Collectors.toList());
        persistService.persistConfigurations(Collections.singletonMap(DefaultSchema.LOGIC_NAME, dataSourceConfigs),
                Collections.singletonMap(DefaultSchema.LOGIC_NAME, schemaRuleConfigs), globalRuleConfigs, metaDataContexts.getProps().getProps(), isOverwrite);
        registryCenter.onlineInstance(Collections.singletonList(DefaultSchema.LOGIC_NAME));
    }
    
    @Override
    public Connection getConnection() {
        return DriverStateContext.getConnection(getDataSourceMap(), metaDataContexts, transactionContexts, TransactionTypeHolder.get());
    }
    
    @Override
    public Connection getConnection(final String username, final String password) {
        return getConnection();
    }
    
    @Override
    public void close() throws Exception {
        getDataSourceMap().forEach((key, value) -> close(value));
        metaDataContexts.close();
        governanceRule.getRegistryCenter().close();
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
        return metaDataContexts.getDefaultMetaData().getResource().getDataSources();
    }
}
