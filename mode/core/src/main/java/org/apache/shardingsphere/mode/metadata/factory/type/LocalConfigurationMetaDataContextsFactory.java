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

package org.apache.shardingsphere.mode.metadata.factory.type;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.config.database.DatabaseConfiguration;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstanceContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabasesFactory;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereDatabaseData;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereSchemaData;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereStatistics;
import org.apache.shardingsphere.infra.metadata.statistics.builder.ShardingSphereStatisticsFactory;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.builder.global.GlobalRulesBuilder;
import org.apache.shardingsphere.mode.manager.ContextManagerBuilderParameter;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Local configuration meta data contexts factory.
 */
@RequiredArgsConstructor
public final class LocalConfigurationMetaDataContextsFactory {
    
    private final MetaDataPersistService persistService;
    
    private final ComputeNodeInstanceContext instanceContext;
    
    /**
     * Create meta data contexts.
     *
     * @param param context manager builder parameter
     * @return meta data contexts
     * @throws SQLException SQL exception
     */
    public MetaDataContexts create(final ContextManagerBuilderParameter param) throws SQLException {
        ConfigurationProperties props = new ConfigurationProperties(param.getProps());
        Collection<ShardingSphereDatabase> databases = ShardingSphereDatabasesFactory.create(param.getDatabaseConfigs(), props, instanceContext);
        MetaDataContexts result = createMetaDataContexts(param.getGlobalRuleConfigs(), param.getGlobalDataSources(), databases, props);
        persistDatabaseConfigurations(result, param);
        persistMetaData(result);
        return result;
    }
    
    private MetaDataContexts createMetaDataContexts(final Collection<RuleConfiguration> globalRuleConfigs, final Map<String, DataSource> globalDataSources,
                                                    final Collection<ShardingSphereDatabase> databases, final ConfigurationProperties props) {
        Collection<ShardingSphereRule> globalRules = GlobalRulesBuilder.buildRules(globalRuleConfigs, databases, props);
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(databases, new ResourceMetaData(globalDataSources), new RuleMetaData(globalRules), props);
        ShardingSphereStatistics statistics = ShardingSphereStatisticsFactory.create(metaData, persistService.getShardingSphereDataPersistService().load(metaData));
        return new MetaDataContexts(metaData, statistics);
    }
    
    private void persistDatabaseConfigurations(final MetaDataContexts metadataContexts, final ContextManagerBuilderParameter param) {
        Collection<RuleConfiguration> globalRuleConfigs = metadataContexts.getMetaData().getGlobalRuleMetaData().getConfigurations();
        persistService.persistGlobalRuleConfiguration(globalRuleConfigs, param.getProps());
        for (Entry<String, ? extends DatabaseConfiguration> entry : param.getDatabaseConfigs().entrySet()) {
            ShardingSphereDatabase database = metadataContexts.getMetaData().getDatabase(entry.getKey());
            Map<String, DataSource> dataSources = database.getResourceMetaData().getStorageUnits().entrySet().stream()
                    .collect(Collectors.toMap(Entry::getKey, each -> each.getValue().getDataSource(), (oldValue, currentValue) -> oldValue, LinkedHashMap::new));
            persistService.persistConfigurations(entry.getKey(), entry.getValue(), dataSources, database.getRuleMetaData().getRules());
        }
    }
    
    private void persistMetaData(final MetaDataContexts metaDataContexts) {
        metaDataContexts.getMetaData().getAllDatabases().forEach(each -> each.getAllSchemas().forEach(schema -> {
            if (schema.isEmpty()) {
                persistService.getDatabaseMetaDataFacade().getSchema().add(each.getName(), schema.getName());
            }
            persistService.getDatabaseMetaDataFacade().getTable().persist(each.getName(), schema.getName(), schema.getAllTables());
        }));
        for (Entry<String, ShardingSphereDatabaseData> databaseDataEntry : metaDataContexts.getStatistics().getDatabaseData().entrySet()) {
            for (Entry<String, ShardingSphereSchemaData> schemaDataEntry : databaseDataEntry.getValue().getSchemaData().entrySet()) {
                persistService.getShardingSphereDataPersistService().persist(
                        metaDataContexts.getMetaData().getDatabase(databaseDataEntry.getKey()), schemaDataEntry.getKey(), schemaDataEntry.getValue());
            }
        }
    }
}
