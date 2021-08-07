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

import org.apache.shardingsphere.infra.config.DatabaseAccessConfiguration;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.persist.DistMetaDataPersistService;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.properties.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.context.metadata.impl.StandardMetaDataContexts;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeRecognizer;
import org.apache.shardingsphere.infra.executor.kernel.ExecutorEngine;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.resource.CachedDatabaseMetaData;
import org.apache.shardingsphere.infra.metadata.resource.DataSourcesMetaData;
import org.apache.shardingsphere.infra.metadata.resource.ShardingSphereResource;
import org.apache.shardingsphere.infra.metadata.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.schema.builder.SchemaBuilder;
import org.apache.shardingsphere.infra.metadata.schema.builder.SchemaBuilderMaterials;
import org.apache.shardingsphere.infra.metadata.schema.model.TableMetaData;
import org.apache.shardingsphere.infra.optimize.context.OptimizeContextFactory;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.builder.ShardingSphereRulesBuilder;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Meta data contexts builder.
 */
public final class MetaDataContextsBuilder {
    
    private final Map<String, Map<String, DataSource>> dataSources;
    
    private final Map<String, Collection<RuleConfiguration>> schemaRuleConfigs;
    
    private final Collection<RuleConfiguration> globalRuleConfigs;
    
    private final ConfigurationProperties props;
    
    private final ExecutorEngine executorEngine;
    
    public MetaDataContextsBuilder(final Map<String, Map<String, DataSource>> dataSources, final Map<String, Collection<RuleConfiguration>> schemaRuleConfigs, final Properties props) {
        this(dataSources, schemaRuleConfigs, new LinkedList<>(), props);
    }
    
    public MetaDataContextsBuilder(final Map<String, Map<String, DataSource>> dataSources,
                                   final Map<String, Collection<RuleConfiguration>> schemaRuleConfigs, final Collection<RuleConfiguration> globalRuleConfigs, final Properties props) {
        this.dataSources = dataSources;
        this.schemaRuleConfigs = schemaRuleConfigs;
        this.globalRuleConfigs = globalRuleConfigs;
        this.props = new ConfigurationProperties(null == props ? new Properties() : props);
        executorEngine = new ExecutorEngine(this.props.<Integer>getValue(ConfigurationPropertyKey.EXECUTOR_SIZE));
    }
    
    /**
     * Build meta data contexts.
     * 
     * @param persistService dist meta data persist service
     * @exception SQLException SQL exception
     * @return meta data contexts
     */
    public StandardMetaDataContexts build(final DistMetaDataPersistService persistService) throws SQLException {
        Map<String, ShardingSphereMetaData> metaDataMap = new HashMap<>(schemaRuleConfigs.size(), 1);
        Map<String, ShardingSphereMetaData> actualMetaDataMap = new HashMap<>(schemaRuleConfigs.size(), 1);
        for (String each : schemaRuleConfigs.keySet()) {
            Map<String, DataSource> dataSourceMap = dataSources.get(each);
            Collection<RuleConfiguration> ruleConfigs = schemaRuleConfigs.get(each);
            DatabaseType databaseType = DatabaseTypeRecognizer.getDatabaseType(dataSourceMap.values());
            Collection<ShardingSphereRule> rules = ShardingSphereRulesBuilder.buildSchemaRules(each, ruleConfigs, databaseType, dataSourceMap);
            Map<TableMetaData, TableMetaData> tableMetaData = SchemaBuilder.build(new SchemaBuilderMaterials(databaseType, dataSourceMap, rules, props));
            ShardingSphereRuleMetaData ruleMetaData = new ShardingSphereRuleMetaData(ruleConfigs, rules);
            ShardingSphereResource resource = buildResource(databaseType, dataSourceMap);
            ShardingSphereSchema actualSchema = new ShardingSphereSchema(tableMetaData.keySet().stream().filter(Objects::nonNull).collect(Collectors.toMap(TableMetaData::getName, v -> v)));
            actualMetaDataMap.put(each, new ShardingSphereMetaData(each, resource, ruleMetaData, actualSchema));
            metaDataMap.put(each, new ShardingSphereMetaData(each, resource, ruleMetaData, buildSchema(tableMetaData)));
        }
        OptimizeContextFactory optimizeContextFactory = new OptimizeContextFactory(actualMetaDataMap);
        return new StandardMetaDataContexts(persistService, metaDataMap, buildGlobalSchemaMetaData(metaDataMap), executorEngine, props, optimizeContextFactory);
    }
    
    private ShardingSphereSchema buildSchema(final Map<TableMetaData, TableMetaData> tableMetaData) {
        Map<String, TableMetaData> tables = new HashMap<>(tableMetaData.size(), 1);
        tables.putAll(tableMetaData.keySet().stream().collect(Collectors.toMap(TableMetaData::getName, v -> v)));
        tables.putAll(tableMetaData.values().stream().collect(Collectors.toMap(TableMetaData::getName, v -> v)));
        return new ShardingSphereSchema(tables);
    }

    private ShardingSphereRuleMetaData buildGlobalSchemaMetaData(final Map<String, ShardingSphereMetaData> mataDataMap) {
        return new ShardingSphereRuleMetaData(globalRuleConfigs, ShardingSphereRulesBuilder.buildGlobalRules(globalRuleConfigs, mataDataMap));
    }
    
    private ShardingSphereResource buildResource(final DatabaseType databaseType, final Map<String, DataSource> dataSourceMap) throws SQLException {
        DataSourcesMetaData dataSourceMetas = new DataSourcesMetaData(databaseType, getDatabaseAccessConfigurationMap(dataSourceMap));
        CachedDatabaseMetaData cachedDatabaseMetaData = createCachedDatabaseMetaData(dataSourceMap).orElse(null);
        return new ShardingSphereResource(dataSourceMap, dataSourceMetas, cachedDatabaseMetaData, databaseType);
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

}
