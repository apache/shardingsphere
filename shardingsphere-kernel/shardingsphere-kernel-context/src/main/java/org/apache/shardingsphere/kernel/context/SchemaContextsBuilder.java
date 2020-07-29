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

package org.apache.shardingsphere.kernel.context;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.infra.auth.Authentication;
import org.apache.shardingsphere.infra.config.DatabaseAccessConfiguration;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.properties.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.DatabaseTypes;
import org.apache.shardingsphere.infra.executor.kernel.ExecutorKernel;
import org.apache.shardingsphere.infra.log.ConfigurationLogger;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.datasource.DataSourceMetas;
import org.apache.shardingsphere.infra.metadata.schema.RuleSchemaMetaData;
import org.apache.shardingsphere.infra.metadata.schema.RuleSchemaMetaDataLoader;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.ShardingSphereRulesBuilder;
import org.apache.shardingsphere.kernel.context.runtime.CachedDatabaseMetaData;
import org.apache.shardingsphere.kernel.context.runtime.RuntimeContext;
import org.apache.shardingsphere.kernel.context.schema.DataSourceParameter;
import org.apache.shardingsphere.kernel.context.schema.ShardingSphereSchema;
import org.apache.shardingsphere.sql.parser.SQLParserEngineFactory;
import org.apache.shardingsphere.transaction.ShardingTransactionManagerEngine;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

/**
 * Schema contexts builder.
 */
@Slf4j(topic = "ShardingSphere-schemaContexts")
public final class SchemaContextsBuilder {
    
    private final DatabaseType databaseType;
    
    private final Map<String, Map<String, DataSource>> dataSources;
    
    private final Map<String, Map<String, DataSourceParameter>> dataSourceParameters = new LinkedHashMap<>();
    
    private final Map<String, Collection<RuleConfiguration>> configurations;
    
    private final ConfigurationProperties props;
    
    private final ExecutorKernel executorKernel;
    
    private final Authentication authentication;
    
    public SchemaContextsBuilder(final Map<String, Map<String, DataSource>> dataSources,
                                 final DatabaseType databaseType, final Map<String, Collection<RuleConfiguration>> configurations, final Properties props) {
        this.dataSources = dataSources;
        this.databaseType = databaseType;
        this.configurations = configurations;
        this.props = new ConfigurationProperties(null == props ? new Properties() : props);
        executorKernel = new ExecutorKernel(this.props.<Integer>getValue(ConfigurationPropertyKey.EXECUTOR_SIZE));
        authentication = new Authentication();
        log(configurations, props);
    }
    
    public SchemaContextsBuilder(final Map<String, Map<String, DataSource>> dataSources, final Map<String, Map<String, DataSourceParameter>> dataSourceParameters, final Authentication authentication, 
                                 final DatabaseType databaseType, final Map<String, Collection<RuleConfiguration>> configurations, final Properties props) {
        this.dataSources = dataSources;
        this.databaseType = databaseType;
        this.configurations = configurations;
        this.props = new ConfigurationProperties(null == props ? new Properties() : props);
        executorKernel = new ExecutorKernel(this.props.<Integer>getValue(ConfigurationPropertyKey.EXECUTOR_SIZE));
        this.dataSourceParameters.putAll(dataSourceParameters);
        this.authentication = authentication;
        log(configurations, props);
    }
    
    /**
     *  Build.
     * 
     * @exception SQLException sql exception
     * @return SchemaContexts
     */
    public SchemaContexts build() throws SQLException {
        Map<String, SchemaContext> schemaContexts = new LinkedHashMap<>();
        for (String each : configurations.keySet()) {
            schemaContexts.put(each, createSchemaContext(each));
        }
        return new SchemaContexts(schemaContexts, props, authentication);
    }
    
    private SchemaContext createSchemaContext(final String schemaName) throws SQLException {
        Map<String, DataSource> dataSources = this.dataSources.get(schemaName);
        RuntimeContext runtimeContext = new RuntimeContext(createCachedDatabaseMetaData(dataSources),
                executorKernel, SQLParserEngineFactory.getSQLParserEngine(DatabaseTypes.getTrunkDatabaseTypeName(databaseType)), createShardingTransactionManagerEngine(dataSources));
        return new SchemaContext(schemaName, createShardingSphereSchema(schemaName), runtimeContext);
    }
    
    private ShardingTransactionManagerEngine createShardingTransactionManagerEngine(final Map<String, DataSource> dataSources) {
        ShardingTransactionManagerEngine result = new ShardingTransactionManagerEngine();
        result.init(databaseType, dataSources);
        return result;
    }
    
    private ShardingSphereSchema createShardingSphereSchema(final String schemaName) throws SQLException {
        Map<String, DataSource> dataSources = this.dataSources.get(schemaName);
        Collection<RuleConfiguration> configurations = this.configurations.get(schemaName);
        Collection<ShardingSphereRule> rules = ShardingSphereRulesBuilder.build(configurations, dataSources.keySet());
        if (dataSourceParameters.isEmpty()) {
            return new ShardingSphereSchema(databaseType, configurations, rules, dataSources, createMetaData(dataSources, rules));
        }
        return new ShardingSphereSchema(databaseType, configurations, rules, dataSources, dataSourceParameters.get(schemaName), createMetaData(dataSources, rules));
    }
    
    private ShardingSphereMetaData createMetaData(final Map<String, DataSource> dataSourceMap, final Collection<ShardingSphereRule> rules) throws SQLException {
        long start = System.currentTimeMillis();
        DataSourceMetas dataSourceMetas = new DataSourceMetas(databaseType, getDatabaseAccessConfigurationMap(dataSourceMap));
        RuleSchemaMetaData ruleSchemaMetaData = new RuleSchemaMetaDataLoader(rules).load(databaseType, dataSourceMap, props, executorKernel.getExecutorService().getExecutorService());
        ShardingSphereMetaData result = new ShardingSphereMetaData(dataSourceMetas, ruleSchemaMetaData);
        log.info("Meta data load finished, cost {} milliseconds.", System.currentTimeMillis() - start);
        return result;
    }
    
    private Map<String, DatabaseAccessConfiguration> getDatabaseAccessConfigurationMap(final Map<String, DataSource> dataSourceMap) throws SQLException {
        Map<String, DatabaseAccessConfiguration> result = new LinkedHashMap<>(dataSourceMap.size(), 1);
        for (Entry<String, DataSource> entry : dataSourceMap.entrySet()) {
            DataSource dataSource = entry.getValue();
            try (Connection connection = dataSource.getConnection()) {
                DatabaseMetaData metaData = connection.getMetaData();
                result.put(entry.getKey(), new DatabaseAccessConfiguration(metaData.getURL(), metaData.getUserName(), null));
            }
        }
        return result;
    }
    
    private CachedDatabaseMetaData createCachedDatabaseMetaData(final Map<String, DataSource> dataSourceMap) throws SQLException {
        try (Connection connection = dataSourceMap.values().iterator().next().getConnection()) {
            return new CachedDatabaseMetaData(connection.getMetaData());
        }
    }
    
    private void log(final Map<String, Collection<RuleConfiguration>> configurations, final Properties props) {
        configurations.values().forEach(ConfigurationLogger::log);
        ConfigurationLogger.log(props);
    }
}
