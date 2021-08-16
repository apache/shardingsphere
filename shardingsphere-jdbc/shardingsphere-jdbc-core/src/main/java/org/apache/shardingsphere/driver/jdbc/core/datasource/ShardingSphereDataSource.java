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
import org.apache.shardingsphere.driver.jdbc.core.connection.ShardingSphereConnection;
import org.apache.shardingsphere.driver.jdbc.unsupported.AbstractUnsupportedOperationDataSource;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.properties.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.context.manager.ContextManager;
import org.apache.shardingsphere.infra.context.manager.impl.MemoryContextManager;
import org.apache.shardingsphere.infra.context.manager.impl.StandaloneContextManager;
import org.apache.shardingsphere.infra.context.metadata.MetaDataContexts;
import org.apache.shardingsphere.infra.context.metadata.MetaDataContextsBuilder;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.mode.ShardingSphereMode;
import org.apache.shardingsphere.infra.mode.builder.ModeBuilderEngine;
import org.apache.shardingsphere.infra.mode.config.ModeConfiguration;
import org.apache.shardingsphere.infra.mode.impl.memory.MemoryMode;
import org.apache.shardingsphere.infra.mode.repository.PersistRepository;
import org.apache.shardingsphere.infra.persist.DistMetaDataPersistService;
import org.apache.shardingsphere.transaction.ShardingTransactionManagerEngine;
import org.apache.shardingsphere.transaction.context.TransactionContexts;
import org.apache.shardingsphere.transaction.core.TransactionTypeHolder;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

/**
 * ShardingSphere data source.
 */
@Getter
public final class ShardingSphereDataSource extends AbstractUnsupportedOperationDataSource implements AutoCloseable {
    
    private final String schemaName;
    
    private final ShardingSphereMode mode;
    
    private final ContextManager contextManager;
    
    public ShardingSphereDataSource(final String schemaName, final ModeConfiguration modeConfig, final Map<String, DataSource> dataSourceMap,
                                    final Collection<RuleConfiguration> ruleConfigs, final Properties props) throws SQLException {
        this.schemaName = schemaName;
        mode = ModeBuilderEngine.build(modeConfig);
        Optional<PersistRepository> persistRepository = mode.getPersistRepository();
        DistMetaDataPersistService persistService = persistRepository.map(DistMetaDataPersistService::new).orElse(null);
        MetaDataContexts metaDataContexts = new MetaDataContextsBuilder(
                Collections.singletonMap(schemaName, dataSourceMap), Collections.singletonMap(schemaName, ruleConfigs), props).build(persistService);
        String xaTransactionMangerType = metaDataContexts.getProps().getValue(ConfigurationPropertyKey.XA_TRANSACTION_MANAGER_TYPE);
        TransactionContexts transactionContexts = createTransactionContexts(metaDataContexts.getMetaData(schemaName).getResource().getDatabaseType(), dataSourceMap, xaTransactionMangerType);
        contextManager = mode instanceof MemoryMode ? new MemoryContextManager() : new StandaloneContextManager();
        contextManager.init(metaDataContexts, transactionContexts);
    }
    
    private TransactionContexts createTransactionContexts(final DatabaseType databaseType, final Map<String, DataSource> dataSourceMap, final String xaTransactionMangerType) {
        ShardingTransactionManagerEngine engine = new ShardingTransactionManagerEngine();
        engine.init(databaseType, dataSourceMap, xaTransactionMangerType);
        return new TransactionContexts(Collections.singletonMap(schemaName, engine));
    }
    
    @Override
    public ShardingSphereConnection getConnection() {
        return new ShardingSphereConnection(schemaName, getDataSourceMap(), contextManager, TransactionTypeHolder.get());
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
        return contextManager.getMetaDataContexts().getMetaData(schemaName).getResource().getDataSources();
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
        contextManager.getMetaDataContexts().close();
        mode.close();
    }
    
    private void close(final DataSource dataSource) throws Exception {
        if (dataSource instanceof AutoCloseable) {
            ((AutoCloseable) dataSource).close();
        }
    }
}
