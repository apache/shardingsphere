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

package org.apache.shardingsphere.proxy.initializer.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.db.protocol.mysql.constant.MySQLServerInfo;
import org.apache.shardingsphere.infra.config.datasource.DataSourceParameter;
import org.apache.shardingsphere.infra.config.properties.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.context.metadata.MetaDataContexts;
import org.apache.shardingsphere.infra.context.metadata.MetaDataContextsBuilder;
import org.apache.shardingsphere.infra.database.DefaultSchema;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.datasource.factory.JDBCRawBackendDataSourceFactory;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.recognizer.JDBCDriverURLRecognizerEngine;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.config.ProxyConfiguration;
import org.apache.shardingsphere.proxy.config.YamlProxyConfiguration;
import org.apache.shardingsphere.proxy.database.DatabaseServerInfo;
import org.apache.shardingsphere.proxy.frontend.ShardingSphereProxy;
import org.apache.shardingsphere.proxy.initializer.BootstrapInitializer;
import org.apache.shardingsphere.tracing.opentracing.OpenTracingTracer;
import org.apache.shardingsphere.transaction.ShardingTransactionManagerEngine;
import org.apache.shardingsphere.transaction.context.TransactionContexts;
import org.apache.shardingsphere.transaction.context.impl.StandardTransactionContexts;
import org.apache.shardingsphere.transaction.core.XATransactionManagerType;
import org.apache.shardingsphere.transaction.core.XATransactionManagerTypeHolder;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Abstract bootstrap initializer.
 */
@Slf4j
public abstract class AbstractBootstrapInitializer implements BootstrapInitializer {
    
    private final ShardingSphereProxy shardingSphereProxy = new ShardingSphereProxy();
    
    @Override
    public final void init(final YamlProxyConfiguration yamlConfig, final int port) throws SQLException {
        ProxyConfiguration proxyConfig = getProxyConfiguration(yamlConfig);
        MetaDataContexts metaDataContexts = decorateMetaDataContexts(createMetaDataContexts(proxyConfig));
        TransactionContexts transactionContexts = decorateTransactionContexts(createTransactionContexts(metaDataContexts));
        ProxyContext.getInstance().init(metaDataContexts, transactionContexts);
        initOpenTracing();
        setDatabaseServerInfo();
        initLockContext();
        shardingSphereProxy.start(port);
    }
    
    private MetaDataContexts createMetaDataContexts(final ProxyConfiguration proxyConfig) throws SQLException {
        Map<String, DatabaseType> databaseTypes = containsDataSources(proxyConfig.getSchemaDataSources())
                ? getDatabaseTypes(proxyConfig.getSchemaDataSources()) : Collections.singletonMap(DefaultSchema.LOGIC_NAME, new MySQLDatabaseType());
        Map<String, Map<String, DataSource>> dataSourcesMap = createDataSourcesMap(proxyConfig.getSchemaDataSources());
        MetaDataContextsBuilder metaDataContextsBuilder = new MetaDataContextsBuilder(
                databaseTypes, dataSourcesMap, proxyConfig.getSchemaRules(), proxyConfig.getAuthentication(), proxyConfig.getProps());
        return metaDataContextsBuilder.build();
    }
    
    private boolean containsDataSources(final Map<String, Map<String, DataSourceParameter>> schemaDataSources) {
        return !schemaDataSources.isEmpty() && !schemaDataSources.values().iterator().next().isEmpty();
    }
    
    private static Map<String, DatabaseType> getDatabaseTypes(final Map<String, Map<String, DataSourceParameter>> schemaDataSources) {
        Map<String, DatabaseType> result = new HashMap<>(schemaDataSources.size(), 1);
        for (Entry<String, Map<String, DataSourceParameter>> entry : schemaDataSources.entrySet()) {
            String databaseTypeName = JDBCDriverURLRecognizerEngine.getJDBCDriverURLRecognizer(entry.getValue().values().iterator().next().getUrl()).getDatabaseType();
            result.put(entry.getKey(), DatabaseTypeRegistry.getActualDatabaseType(databaseTypeName));
        }
        return result;
    }
    
    private static Map<String, Map<String, DataSource>> createDataSourcesMap(final Map<String, Map<String, DataSourceParameter>> schemaDataSources) {
        return schemaDataSources.entrySet().stream().collect(Collectors.toMap(Entry::getKey, entry -> createDataSources(entry.getValue()), (oldValue, currentValue) -> oldValue, LinkedHashMap::new));
    }
    
    private static Map<String, DataSource> createDataSources(final Map<String, DataSourceParameter> dataSourceParameters) {
        Map<String, DataSource> result = new LinkedHashMap<>(dataSourceParameters.size(), 1);
        for (Entry<String, DataSourceParameter> entry : dataSourceParameters.entrySet()) {
            result.put(entry.getKey(), JDBCRawBackendDataSourceFactory.getInstance().build(entry.getKey(), entry.getValue()));
        }
        return result;
    }
    
    private TransactionContexts createTransactionContexts(final MetaDataContexts metaDataContexts) {
        Map<String, ShardingTransactionManagerEngine> transactionManagerEngines = new HashMap<>(metaDataContexts.getMetaDataMap().size(), 1);
        XATransactionManagerType transactionType = XATransactionManagerType.valueFrom(metaDataContexts.getProps().getValue(ConfigurationPropertyKey.PROXY_XA_TRANSACTION_MANAGER_TYPE));
        XATransactionManagerTypeHolder.set(transactionType);
        for (Entry<String, ShardingSphereMetaData> entry : metaDataContexts.getMetaDataMap().entrySet()) {
            ShardingTransactionManagerEngine engine = new ShardingTransactionManagerEngine();
            engine.init(metaDataContexts.getMetaDataMap().get(entry.getKey()).getResource().getDatabaseType(), entry.getValue().getResource().getDataSources());
            transactionManagerEngines.put(entry.getKey(), engine);
        }
        return new StandardTransactionContexts(transactionManagerEngines);
    }
    
    private void initOpenTracing() {
        if (ProxyContext.getInstance().getMetaDataContexts().getProps().<Boolean>getValue(ConfigurationPropertyKey.PROXY_OPENTRACING_ENABLED)) {
            OpenTracingTracer.init();
        }
    }
    
    private void setDatabaseServerInfo() {
        Optional<DataSource> dataSourceSample = ProxyContext.getInstance().getDataSourceSample();
        if (dataSourceSample.isPresent()) {
            DatabaseServerInfo databaseServerInfo = new DatabaseServerInfo(dataSourceSample.get());
            log.info(databaseServerInfo.toString());
            MySQLServerInfo.setServerVersion(databaseServerInfo.getDatabaseVersion());
        }
    }
    
    protected abstract ProxyConfiguration getProxyConfiguration(YamlProxyConfiguration yamlConfig);
    
    protected abstract MetaDataContexts decorateMetaDataContexts(MetaDataContexts metaDataContexts);
    
    protected abstract TransactionContexts decorateTransactionContexts(TransactionContexts transactionContexts);
    
    protected abstract void initLockContext();
}
