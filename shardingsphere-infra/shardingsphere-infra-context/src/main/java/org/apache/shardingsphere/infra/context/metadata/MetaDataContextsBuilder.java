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

package org.apache.shardingsphere.infra.context.metadata;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.infra.auth.Authentication;
import org.apache.shardingsphere.infra.config.DatabaseAccessConfiguration;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.properties.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.context.metadata.impl.StandardMetaDataContexts;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.executor.kernel.ExecutorKernel;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.resource.CachedDatabaseMetaData;
import org.apache.shardingsphere.infra.metadata.resource.DataSourcesMetaData;
import org.apache.shardingsphere.infra.metadata.resource.ShardingSphereResource;
import org.apache.shardingsphere.infra.metadata.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.metadata.schema.builder.SchemaBuilder;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.schema.builder.SchemaBuilderMaterials;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.builder.ShardingSphereRulesBuilder;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Properties;

/**
 * Meta data contexts builder.
 */
@Slf4j
public final class MetaDataContextsBuilder {
    
    private final DatabaseType databaseType;
    
    private final Map<String, Map<String, DataSource>> dataSources;
    
    private final Map<String, Collection<RuleConfiguration>> ruleConfigs;
    
    private final Authentication authentication;
    
    private final ConfigurationProperties props;
    
    private final ExecutorKernel executorKernel;
    
    public MetaDataContextsBuilder(final DatabaseType databaseType, final Map<String, Map<String, DataSource>> dataSources,
                                   final Map<String, Collection<RuleConfiguration>> ruleConfigs, final Properties props) {
        this(databaseType, dataSources, ruleConfigs, new Authentication(), props);
    }
    
    public MetaDataContextsBuilder(final DatabaseType databaseType, final Map<String, Map<String, DataSource>> dataSources,
                                   final Map<String, Collection<RuleConfiguration>> ruleConfigs, final Authentication authentication, final Properties props) {
        this.databaseType = databaseType;
        this.dataSources = dataSources;
        this.ruleConfigs = ruleConfigs;
        this.authentication = authentication;
        this.props = new ConfigurationProperties(null == props ? new Properties() : props);
        executorKernel = new ExecutorKernel(this.props.<Integer>getValue(ConfigurationPropertyKey.EXECUTOR_SIZE));
    }
    
    /**
     * Build meta data contexts.
     * 
     * @exception SQLException SQL exception
     * @return meta data contexts
     */
    public MetaDataContexts build() throws SQLException {
        Map<String, ShardingSphereMetaData> mataDataMap = new LinkedHashMap<>(ruleConfigs.size(), 1);
        for (String each : ruleConfigs.keySet()) {
            mataDataMap.put(each, buildMetaData(each));
        }
        return new StandardMetaDataContexts(mataDataMap, executorKernel, authentication, props, databaseType);
    }
    
    private ShardingSphereMetaData buildMetaData(final String schemaName) throws SQLException {
        Map<String, DataSource> dataSourceMap = dataSources.get(schemaName);
        Collection<RuleConfiguration> ruleConfigs = this.ruleConfigs.get(schemaName);
        Collection<ShardingSphereRule> rules = ShardingSphereRulesBuilder.build(ruleConfigs, databaseType, dataSourceMap);
        ShardingSphereRuleMetaData ruleMetaData = new ShardingSphereRuleMetaData(ruleConfigs, rules);
        return new ShardingSphereMetaData(schemaName, buildResource(dataSourceMap), ruleMetaData, buildSchema(schemaName, dataSourceMap, rules));
    }
    
    private ShardingSphereResource buildResource(final Map<String, DataSource> dataSourceMap) throws SQLException {
        DataSourcesMetaData dataSourceMetas = new DataSourcesMetaData(databaseType, getDatabaseAccessConfigurationMap(dataSourceMap));
        CachedDatabaseMetaData cachedDatabaseMetaData = createCachedDatabaseMetaData(dataSourceMap).orElse(null);
        return new ShardingSphereResource(dataSourceMap, dataSourceMetas, cachedDatabaseMetaData);
    }
    
    private Map<String, DatabaseAccessConfiguration> getDatabaseAccessConfigurationMap(final Map<String, DataSource> dataSourceMap) throws SQLException {
        Map<String, DatabaseAccessConfiguration> result = new LinkedHashMap<>(dataSourceMap.size(), 1);
        for (Entry<String, DataSource> entry : dataSourceMap.entrySet()) {
            DataSource dataSource = entry.getValue();
            try (Connection connection = dataSource.getConnection()) {
                DatabaseMetaData metaData = connection.getMetaData();
                result.put(entry.getKey(), new DatabaseAccessConfiguration(metaData.getURL(), metaData.getUserName()));
            }
        }
        return result;
    }
    
    private Optional<CachedDatabaseMetaData> createCachedDatabaseMetaData(final Map<String, DataSource> dataSources) throws SQLException {
        if (dataSources.isEmpty()) {
            return Optional.empty();
        }
        try (Connection connection = dataSources.values().iterator().next().getConnection()) {
            return Optional.of(new CachedDatabaseMetaData(connection.getMetaData()));
        }
    }
    
    private ShardingSphereSchema buildSchema(final String schemaName, final Map<String, DataSource> dataSourceMap, final Collection<ShardingSphereRule> rules) throws SQLException {
        long start = System.currentTimeMillis();
        ShardingSphereSchema result = SchemaBuilder.build(new SchemaBuilderMaterials(databaseType, dataSourceMap, rules, props));
        log.info("Load meta data for schema {} finished, cost {} milliseconds.", schemaName, System.currentTimeMillis() - start);
        return result;
    }
}
