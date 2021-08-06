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

package org.apache.shardingsphere.driver.jdbc.core.datasource;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.apache.shardingsphere.driver.jdbc.core.connection.ShardingSphereConnection;
import org.apache.shardingsphere.driver.jdbc.unsupported.AbstractUnsupportedOperationDataSource;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.persist.DistMetaDataPersistService;
import org.apache.shardingsphere.infra.config.persist.repository.DistMetaDataPersistRepository;
import org.apache.shardingsphere.infra.config.persist.repository.DistMetaDataPersistRepositoryFactory;
import org.apache.shardingsphere.infra.config.properties.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.context.metadata.MetaDataContexts;
import org.apache.shardingsphere.infra.context.metadata.MetaDataContextsBuilder;
import org.apache.shardingsphere.infra.database.DefaultSchema;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.rule.persist.DistMetaDataPersistRuleConfiguration;
import org.apache.shardingsphere.transaction.ShardingTransactionManagerEngine;
import org.apache.shardingsphere.transaction.context.TransactionContexts;
import org.apache.shardingsphere.transaction.context.impl.StandardTransactionContexts;
import org.apache.shardingsphere.transaction.core.TransactionTypeHolder;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

/**
 * ShardingSphere data source.
 */
@RequiredArgsConstructor
@Getter
public final class ShardingSphereDataSource extends AbstractUnsupportedOperationDataSource implements AutoCloseable {
    
    private final MetaDataContexts metaDataContexts;
    
    private final TransactionContexts transactionContexts;
    
    private final String schemaName;
    
    public ShardingSphereDataSource(final Map<String, DataSource> dataSourceMap, final Collection<RuleConfiguration> ruleConfigs, final Properties props) throws SQLException {
        schemaName = DefaultSchema.LOGIC_NAME;
        DistMetaDataPersistRepository repository = DistMetaDataPersistRepositoryFactory.newInstance(findDistMetaDataPersistRuleConfiguration(ruleConfigs));
        metaDataContexts = new MetaDataContextsBuilder(Collections.singletonMap(schemaName, dataSourceMap),
                Collections.singletonMap(schemaName, ruleConfigs), props).build(new DistMetaDataPersistService(repository));
        String xaTransactionMangerType = metaDataContexts.getProps().getValue(ConfigurationPropertyKey.XA_TRANSACTION_MANAGER_TYPE);
        transactionContexts = createTransactionContexts(metaDataContexts.getDefaultMetaData().getResource().getDatabaseType(), dataSourceMap, xaTransactionMangerType);
    }
    
    public ShardingSphereDataSource(final Map<String, DataSource> dataSourceMap, final Collection<RuleConfiguration> ruleConfigs, final Properties props, final String schemaName) throws SQLException {
        this.schemaName = getSchemaName(schemaName);
        DistMetaDataPersistRepository repository = DistMetaDataPersistRepositoryFactory.newInstance(findDistMetaDataPersistRuleConfiguration(ruleConfigs));
        metaDataContexts = new MetaDataContextsBuilder(Collections.singletonMap(getSchemaName(schemaName), dataSourceMap),
                Collections.singletonMap(getSchemaName(schemaName), ruleConfigs), props).build(new DistMetaDataPersistService(repository));
        String xaTransactionMangerType = metaDataContexts.getProps().getValue(ConfigurationPropertyKey.XA_TRANSACTION_MANAGER_TYPE);
        transactionContexts = createTransactionContexts(metaDataContexts.getMetaData(getSchemaName(schemaName)).getResource().getDatabaseType(), dataSourceMap, xaTransactionMangerType);
    }
    
    private String getSchemaName(final String schemaName) {
        return StringUtils.isNotEmpty(schemaName) ? schemaName : DefaultSchema.LOGIC_NAME;
    }
    
    private static DistMetaDataPersistRuleConfiguration findDistMetaDataPersistRuleConfiguration(final Collection<RuleConfiguration> ruleConfigs) {
        return ruleConfigs.stream().filter(each -> each instanceof DistMetaDataPersistRuleConfiguration)
                .map(each -> (DistMetaDataPersistRuleConfiguration) each).findFirst().orElse(new DistMetaDataPersistRuleConfiguration("Local", true, new Properties()));
    }
    
    private TransactionContexts createTransactionContexts(final DatabaseType databaseType, final Map<String, DataSource> dataSourceMap, final String xaTransactionMangerType) {
        ShardingTransactionManagerEngine engine = new ShardingTransactionManagerEngine();
        engine.init(databaseType, dataSourceMap, xaTransactionMangerType);
        return new StandardTransactionContexts(Collections.singletonMap(DefaultSchema.LOGIC_NAME, engine));
    }
    
    @Override
    public ShardingSphereConnection getConnection() {
        return new ShardingSphereConnection(getDataSourceMap(), metaDataContexts, transactionContexts, TransactionTypeHolder.get());
    }
    
    @Override
    public ShardingSphereConnection getConnection(final String username, final String password) {
        return getConnection();
    }
    
    /**
     * Get data sources.
     * 
     * @return data sources
     */
    public Map<String, DataSource> getDataSourceMap() {
        return metaDataContexts.getDefaultMetaData().getResource().getDataSources();
    }
    
    @Override
    public void close() throws Exception {
        close(getDataSourceMap().keySet());
    }
    
    /**
     * Close dataSources.
     * 
     * @param dataSourceNames data source names
     * @throws Exception exception
     */
    public void close(final Collection<String> dataSourceNames) throws Exception {
        for (String each : dataSourceNames) {
            close(getDataSourceMap().get(each));
        }
        metaDataContexts.close();
    }
    
    private void close(final DataSource dataSource) throws Exception {
        if (dataSource instanceof AutoCloseable) {
            ((AutoCloseable) dataSource).close();
        }
    }
}
