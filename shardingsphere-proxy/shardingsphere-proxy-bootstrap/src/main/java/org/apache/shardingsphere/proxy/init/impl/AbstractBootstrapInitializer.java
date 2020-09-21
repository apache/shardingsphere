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

package org.apache.shardingsphere.proxy.init.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.db.protocol.mysql.constant.MySQLServerInfo;
import org.apache.shardingsphere.infra.config.properties.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.context.schema.SchemaContext;
import org.apache.shardingsphere.infra.context.schema.SchemaContexts;
import org.apache.shardingsphere.infra.context.schema.SchemaContextsBuilder;
import org.apache.shardingsphere.infra.config.datasource.DataSourceParameter;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.DatabaseTypes;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.datasource.factory.JDBCRawBackendDataSourceFactory;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.recognizer.JDBCDriverURLRecognizerEngine;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.config.ProxyConfiguration;
import org.apache.shardingsphere.proxy.config.YamlProxyConfiguration;
import org.apache.shardingsphere.proxy.db.DatabaseServerInfo;
import org.apache.shardingsphere.proxy.frontend.ShardingSphereProxy;
import org.apache.shardingsphere.proxy.init.BootstrapInitializer;
import org.apache.shardingsphere.tracing.opentracing.OpenTracingTracer;
import org.apache.shardingsphere.transaction.ShardingTransactionManagerEngine;
import org.apache.shardingsphere.transaction.context.TransactionContexts;
import org.apache.shardingsphere.transaction.context.impl.StandardTransactionContexts;

import javax.sql.DataSource;
import java.sql.SQLException;
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
    
    private ShardingSphereProxy shardingSphereProxy = new ShardingSphereProxy();
    
    @Override
    public final void init(final YamlProxyConfiguration yamlConfig, final int port) throws SQLException {
        ProxyConfiguration proxyConfig = getProxyConfiguration(yamlConfig);
        SchemaContexts schemaContexts = decorateSchemaContexts(createSchemaContexts(proxyConfig));
        TransactionContexts transactionContexts = decorateTransactionContexts(createTransactionContexts(schemaContexts));
        ProxyContext.getInstance().init(schemaContexts, transactionContexts);
        initOpenTracing();
        setDatabaseServerInfo();
        shardingSphereProxy.start(port);
    }
    
    private SchemaContexts createSchemaContexts(final ProxyConfiguration proxyConfig) throws SQLException {
        DatabaseType databaseType = containsDataSources(proxyConfig.getSchemaDataSources()) ? getDatabaseType(proxyConfig.getSchemaDataSources()) : new MySQLDatabaseType();
        Map<String, Map<String, DataSource>> dataSourcesMap = createDataSourcesMap(proxyConfig.getSchemaDataSources());
        SchemaContextsBuilder schemaContextsBuilder = new SchemaContextsBuilder(databaseType, dataSourcesMap, proxyConfig.getSchemaRules(), proxyConfig.getAuthentication(), proxyConfig.getProps());
        return schemaContextsBuilder.build();
    }
    
    private boolean containsDataSources(final Map<String, Map<String, DataSourceParameter>> schemaDataSources) {
        return !schemaDataSources.isEmpty() && !schemaDataSources.values().iterator().next().isEmpty();
    }
    
    private static DatabaseType getDatabaseType(final Map<String, Map<String, DataSourceParameter>> schemaDataSources) {
        String databaseTypeName = JDBCDriverURLRecognizerEngine.getJDBCDriverURLRecognizer(schemaDataSources.values().iterator().next().values().iterator().next().getUrl()).getDatabaseType();
        return DatabaseTypes.getActualDatabaseType(databaseTypeName);
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
    
    private TransactionContexts createTransactionContexts(final SchemaContexts schemaContexts) {
        Map<String, ShardingTransactionManagerEngine> transactionManagerEngines = new HashMap<>(schemaContexts.getSchemaContextMap().size(), 1);
        for (Entry<String, SchemaContext> entry : schemaContexts.getSchemaContextMap().entrySet()) {
            ShardingTransactionManagerEngine engine = new ShardingTransactionManagerEngine();
            engine.init(schemaContexts.getDatabaseType(), entry.getValue().getSchema().getDataSources());
            transactionManagerEngines.put(entry.getKey(), engine);
        }
        return new StandardTransactionContexts(transactionManagerEngines);
    }
    
    private void initOpenTracing() {
        if (ProxyContext.getInstance().getSchemaContexts().getProps().<Boolean>getValue(ConfigurationPropertyKey.PROXY_OPENTRACING_ENABLED)) {
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
    
    protected abstract SchemaContexts decorateSchemaContexts(SchemaContexts schemaContexts);
    
    protected abstract TransactionContexts decorateTransactionContexts(TransactionContexts transactionContexts);
}
