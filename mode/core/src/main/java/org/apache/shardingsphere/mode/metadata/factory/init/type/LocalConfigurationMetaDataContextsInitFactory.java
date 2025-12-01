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

package org.apache.shardingsphere.mode.metadata.factory.init.type;

import org.apache.shardingsphere.infra.config.database.DatabaseConfiguration;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstanceContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabasesFactory;
import org.apache.shardingsphere.infra.metadata.statistics.DatabaseStatistics;
import org.apache.shardingsphere.infra.metadata.statistics.SchemaStatistics;
import org.apache.shardingsphere.mode.manager.builder.ContextManagerBuilderParameter;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.factory.init.MetaDataContextsInitFactory;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistFacade;
import org.apache.shardingsphere.mode.spi.repository.PersistRepository;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Local configuration meta data contexts init factory.
 */
public final class LocalConfigurationMetaDataContextsInitFactory extends MetaDataContextsInitFactory {
    
    private final MetaDataPersistFacade persistFacade;
    
    private final ComputeNodeInstanceContext instanceContext;
    
    private final boolean persistSchemasEnabled;
    
    public LocalConfigurationMetaDataContextsInitFactory(final PersistRepository repository, final ComputeNodeInstanceContext instanceContext, final Properties props) {
        persistSchemasEnabled = new ConfigurationProperties(props).getValue(ConfigurationPropertyKey.PERSIST_SCHEMAS_TO_REPOSITORY_ENABLED);
        persistFacade = new MetaDataPersistFacade(repository, persistSchemasEnabled);
        this.instanceContext = instanceContext;
    }
    
    @Override
    public MetaDataContexts create(final ContextManagerBuilderParameter param) throws SQLException {
        ConfigurationProperties props = new ConfigurationProperties(param.getProps());
        Collection<ShardingSphereDatabase> databases = ShardingSphereDatabasesFactory.create(param.getDatabaseConfigs(), props, instanceContext);
        MetaDataContexts result = create(param.getGlobalRuleConfigs(), param.getGlobalDataSources(), databases, props, persistFacade);
        persistDatabaseConfigurations(result, param);
        persistMetaData(result);
        return result;
    }
    
    private void persistDatabaseConfigurations(final MetaDataContexts metadataContexts, final ContextManagerBuilderParameter param) {
        Collection<RuleConfiguration> globalRuleConfigs = metadataContexts.getMetaData().getGlobalRuleMetaData().getConfigurations();
        persistFacade.persistGlobalRuleConfiguration(globalRuleConfigs, param.getProps());
        for (Entry<String, ? extends DatabaseConfiguration> entry : param.getDatabaseConfigs().entrySet()) {
            ShardingSphereDatabase database = metadataContexts.getMetaData().getDatabase(entry.getKey());
            Map<String, DataSource> dataSources = database.getResourceMetaData().getStorageUnits().entrySet().stream()
                    .collect(Collectors.toMap(Entry::getKey, each -> each.getValue().getDataSource(), (oldValue, currentValue) -> oldValue, LinkedHashMap::new));
            persistFacade.persistConfigurations(entry.getKey(), entry.getValue(), dataSources, database.getRuleMetaData().getRules());
        }
    }
    
    private void persistMetaData(final MetaDataContexts metaDataContexts) {
        metaDataContexts.getMetaData().getAllDatabases().forEach(each -> each.getAllSchemas().forEach(schema -> {
            if (schema.isEmpty()) {
                persistFacade.getDatabaseMetaDataFacade().getSchema().add(each.getName(), schema.getName());
            }
            if (persistSchemasEnabled) {
                persistFacade.getDatabaseMetaDataFacade().getTable().persist(each.getName(), schema.getName(), schema.getAllTables());
            }
        }));
        for (Entry<String, DatabaseStatistics> databaseStatisticsEntry : metaDataContexts.getStatistics().getDatabaseStatisticsMap().entrySet()) {
            for (Entry<String, SchemaStatistics> schemaStatisticsEntry : databaseStatisticsEntry.getValue().getSchemaStatisticsMap().entrySet()) {
                persistFacade.getStatisticsService().persist(
                        metaDataContexts.getMetaData().getDatabase(databaseStatisticsEntry.getKey()), schemaStatisticsEntry.getKey(), schemaStatisticsEntry.getValue());
            }
        }
    }
}
