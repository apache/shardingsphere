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

package org.apache.shardingsphere.mode.metadata;

import org.apache.shardingsphere.infra.config.DatabaseAccessConfiguration;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.properties.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeRecognizer;
import org.apache.shardingsphere.infra.executor.kernel.ExecutorEngine;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.resource.CachedDatabaseMetaData;
import org.apache.shardingsphere.infra.metadata.resource.DataSourcesMetaData;
import org.apache.shardingsphere.infra.metadata.resource.ShardingSphereResource;
import org.apache.shardingsphere.infra.metadata.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.metadata.schema.builder.SchemaBuilder;
import org.apache.shardingsphere.infra.metadata.schema.builder.SchemaBuilderMaterials;
import org.apache.shardingsphere.infra.metadata.schema.builder.TableMetaDataBuilder;
import org.apache.shardingsphere.infra.metadata.schema.model.TableMetaData;
import org.apache.shardingsphere.infra.optimize.context.OptimizeContextFactory;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.builder.global.GlobalRulesBuilder;
import org.apache.shardingsphere.infra.rule.builder.schema.SchemaRulesBuilderMaterials;
import org.apache.shardingsphere.infra.rule.builder.schema.SchemaRulesBuilder;
import org.apache.shardingsphere.infra.rule.identifier.type.TableContainedRule;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;

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
     * @param metaDataPersistService persist service
     * @exception SQLException SQL exception
     * @return meta data contexts
     */
    public MetaDataContexts build(final MetaDataPersistService metaDataPersistService) throws SQLException {
        Map<String, ShardingSphereMetaData> metaDataMap = new HashMap<>(schemaRuleConfigs.size(), 1);
        Map<String, ShardingSphereMetaData> actualMetaDataMap = new HashMap<>(schemaRuleConfigs.size(), 1);
        for (String each : schemaRuleConfigs.keySet()) {
            Map<String, DataSource> dataSourceMap = dataSources.get(each);
            Collection<RuleConfiguration> ruleConfigs = schemaRuleConfigs.get(each);
            DatabaseType databaseType = DatabaseTypeRecognizer.getDatabaseType(dataSourceMap.values());
            Collection<ShardingSphereRule> rules = SchemaRulesBuilder.buildRules(new SchemaRulesBuilderMaterials(each, ruleConfigs, databaseType, dataSourceMap, props));
            Collection<TableMetaData> tableMetaDatas = TableMetaDataBuilder.load(getAllTableNames(rules), new SchemaBuilderMaterials(databaseType, dataSourceMap, rules, props)).values();
            ShardingSphereRuleMetaData ruleMetaData = new ShardingSphereRuleMetaData(ruleConfigs, rules);
            ShardingSphereResource resource = buildResource(databaseType, dataSourceMap);
            actualMetaDataMap.put(each, new ShardingSphereMetaData(each, resource, ruleMetaData, SchemaBuilder.buildFederateSchema(tableMetaDatas, rules)));
            metaDataMap.put(each, new ShardingSphereMetaData(each, resource, ruleMetaData, SchemaBuilder.buildKernelSchema(tableMetaDatas, rules)));
        }
        OptimizeContextFactory optimizeContextFactory = new OptimizeContextFactory(actualMetaDataMap);
        return new MetaDataContexts(metaDataPersistService, metaDataMap, buildGlobalSchemaMetaData(metaDataMap), executorEngine, props, optimizeContextFactory);
    }
    
    private Collection<String> getAllTableNames(final Collection<ShardingSphereRule> rules) {
        return rules.stream().filter(rule -> rule instanceof TableContainedRule)
                .flatMap(shardingSphereRule -> ((TableContainedRule) shardingSphereRule).getTables().stream()).collect(Collectors.toSet());
    }
    
    private ShardingSphereRuleMetaData buildGlobalSchemaMetaData(final Map<String, ShardingSphereMetaData> mataDataMap) {
        return new ShardingSphereRuleMetaData(globalRuleConfigs, GlobalRulesBuilder.buildRules(globalRuleConfigs, mataDataMap));
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
