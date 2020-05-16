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

package org.apache.shardingsphere.shardingjdbc.jdbc.core.context;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.infra.auth.Authentication;
import org.apache.shardingsphere.infra.config.DatabaseAccessConfiguration;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.properties.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.context.SchemaContext;
import org.apache.shardingsphere.infra.context.SchemaContexts;
import org.apache.shardingsphere.infra.context.ShardingSphereSchema;
import org.apache.shardingsphere.infra.context.runtime.CachedDatabaseMetaData;
import org.apache.shardingsphere.infra.context.runtime.RuntimeContext;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.log.ConfigurationLogger;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.datasource.DataSourceMetas;
import org.apache.shardingsphere.infra.metadata.schema.RuleSchemaMetaData;
import org.apache.shardingsphere.infra.metadata.schema.RuleSchemaMetaDataLoader;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.ShardingSphereRulesBuilder;
import org.apache.shardingsphere.infra.executor.kernel.ExecutorKernel;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

@Slf4j(topic = "ShardingSphere-metadata")
public final class SchemaContextsBuilder {
    
    private final Map<String, DataSource> dataSources;
    
    private final DatabaseType databaseType;
    
    private final Collection<RuleConfiguration> configurations;
    
    private final Collection<ShardingSphereRule> rules;
    
    private final ConfigurationProperties properties;
    
    private final ExecutorKernel executorKernel;
    
    private final CachedDatabaseMetaData cachedDatabaseMetaData;
    
    private final ShardingSphereMetaData metaData;
    
    public SchemaContextsBuilder(final Map<String, DataSource> dataSources,
                                 final DatabaseType databaseType, final Collection<RuleConfiguration> configurations, final Properties props) throws SQLException {
        this.dataSources = dataSources;
        this.databaseType = databaseType;
        this.configurations = configurations;
        rules = ShardingSphereRulesBuilder.build(configurations, dataSources.keySet());
        properties = new ConfigurationProperties(null == props ? new Properties() : props);
        executorKernel = new ExecutorKernel(properties.<Integer>getValue(ConfigurationPropertyKey.EXECUTOR_SIZE));
        cachedDatabaseMetaData = createCachedDatabaseMetaData(dataSources);
        metaData = createMetaData(dataSources, databaseType);
        log(configurations, props);
    }
    
    /**
     *  Build.
     * 
     * @return SchemaContexts
     */
    public SchemaContexts build() {
        return new SchemaContexts(Collections.singleton(createSchemaContext()), properties, new Authentication());
    }
    
    private SchemaContext createSchemaContext() {
        return new SchemaContext(new ShardingSphereSchema(databaseType, configurations, rules, dataSources, metaData), 
                new RuntimeContext(cachedDatabaseMetaData, executorKernel));
    }
    
    private CachedDatabaseMetaData createCachedDatabaseMetaData(final Map<String, DataSource> dataSourceMap) throws SQLException {
        try (Connection connection = dataSourceMap.values().iterator().next().getConnection()) {
            return new CachedDatabaseMetaData(connection.getMetaData());
        }
    }
    
    private ShardingSphereMetaData createMetaData(final Map<String, DataSource> dataSourceMap, final DatabaseType databaseType) throws SQLException {
        long start = System.currentTimeMillis();
        DataSourceMetas dataSourceMetas = new DataSourceMetas(databaseType, getDatabaseAccessConfigurationMap(dataSourceMap));
        RuleSchemaMetaData ruleSchemaMetaData = new RuleSchemaMetaDataLoader(rules).load(databaseType, dataSourceMap, properties, executorKernel.getExecutorService().getExecutorService());
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
    
    private void log(final Collection<RuleConfiguration> configurations, final Properties props) {
        ConfigurationLogger.log(configurations);
        ConfigurationLogger.log(props);
    }
}
