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
import org.apache.shardingsphere.infra.config.database.impl.DataSourceGeneratedDatabaseConfiguration;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.datasource.pool.config.DataSourceConfiguration;
import org.apache.shardingsphere.infra.datasource.pool.destroyer.DataSourcePoolDestroyer;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstanceContext;
import org.apache.shardingsphere.infra.instance.metadata.jdbc.JDBCInstanceMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabasesFactory;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.mode.manager.builder.ContextManagerBuilderParameter;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.factory.init.MetaDataContextsInitFactory;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistFacade;
import org.apache.shardingsphere.mode.metadata.persist.config.global.PropertiesPersistService;
import org.apache.shardingsphere.mode.metadata.persist.version.VersionPersistService;
import org.apache.shardingsphere.mode.spi.repository.PersistRepository;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Register center meta data contexts init factory.
 */
public final class RegisterCenterMetaDataContextsInitFactory extends MetaDataContextsInitFactory {
    
    private final MetaDataPersistFacade persistFacade;
    
    private final ComputeNodeInstanceContext instanceContext;
    
    private final boolean persistSchemasEnabled;
    
    public RegisterCenterMetaDataContextsInitFactory(final PersistRepository repository, final ComputeNodeInstanceContext instanceContext) {
        persistSchemasEnabled = new ConfigurationProperties(new PropertiesPersistService(repository, new VersionPersistService(repository)).load())
                .getValue(ConfigurationPropertyKey.PERSIST_SCHEMAS_TO_REPOSITORY_ENABLED);
        persistFacade = new MetaDataPersistFacade(repository, persistSchemasEnabled);
        this.instanceContext = instanceContext;
    }
    
    @Override
    public MetaDataContexts create(final ContextManagerBuilderParameter param) throws SQLException {
        Map<String, DatabaseConfiguration> effectiveDatabaseConfigs = createEffectiveDatabaseConfigurations(getDatabaseNames(param.getDatabaseConfigs()), param.getDatabaseConfigs());
        // TODO load global data sources from persist service
        Map<String, DataSource> globalDataSources = param.getGlobalDataSources();
        ConfigurationProperties props = new ConfigurationProperties(persistFacade.getPropsService().load());
        Map<String, Collection<ShardingSphereSchema>> schemas = loadSchemas(effectiveDatabaseConfigs.keySet());
        Collection<ShardingSphereDatabase> databases;
        if (persistSchemasEnabled) {
            // TODO merge schemas with local
            databases = ShardingSphereDatabasesFactory.create(effectiveDatabaseConfigs, schemas, props, instanceContext);
        } else {
            databases = ShardingSphereDatabasesFactory.create(effectiveDatabaseConfigs, props, instanceContext);
            databases.stream().filter(database -> schemas.containsKey(database.getName()))
                    .forEach(database -> schemas.get(database.getName()).stream()
                            .filter(schema -> database.containsSchema(schema.getName()))
                            .forEach(schema -> {
                                ShardingSphereSchema targetSchema = database.getSchema(schema.getName());
                                schema.getAllViews().forEach(targetSchema::putView);
                            }));
        }
        return create(persistFacade.getGlobalRuleService().load(), globalDataSources, databases, props, persistFacade);
    }
    
    private Collection<String> getDatabaseNames(final Map<String, DatabaseConfiguration> databaseConfigs) {
        return instanceContext.getInstance().getMetaData() instanceof JDBCInstanceMetaData
                ? databaseConfigs.keySet()
                : persistFacade.getDatabaseMetaDataFacade().getDatabase().loadAllDatabaseNames();
    }
    
    private Map<String, DatabaseConfiguration> createEffectiveDatabaseConfigurations(final Collection<String> databaseNames, final Map<String, DatabaseConfiguration> databaseConfigs) {
        return databaseNames.stream().collect(Collectors.toMap(each -> each, each -> createEffectiveDatabaseConfiguration(each, databaseConfigs)));
    }
    
    private DatabaseConfiguration createEffectiveDatabaseConfiguration(final String databaseName, final Map<String, DatabaseConfiguration> databaseConfigs) {
        closeGeneratedDataSources(databaseName, databaseConfigs);
        Map<String, DataSourceConfiguration> dataSources = persistFacade.loadDataSourceConfigurations(databaseName);
        Collection<RuleConfiguration> databaseRuleConfigs = persistFacade.getDatabaseRuleService().load(databaseName);
        return new DataSourceGeneratedDatabaseConfiguration(dataSources, databaseRuleConfigs);
    }
    
    private void closeGeneratedDataSources(final String databaseName, final Map<String, ? extends DatabaseConfiguration> databaseConfigs) {
        if (databaseConfigs.containsKey(databaseName) && !databaseConfigs.get(databaseName).getStorageUnits().isEmpty()) {
            databaseConfigs.get(databaseName).getDataSources().values().forEach(each -> new DataSourcePoolDestroyer(each).asyncDestroy());
        }
    }
    
    private Map<String, Collection<ShardingSphereSchema>> loadSchemas(final Collection<String> databaseNames) {
        Map<String, Collection<ShardingSphereSchema>> result = new HashMap<>(databaseNames.size());
        for (String dbName : databaseNames) {
            Collection<ShardingSphereSchema> schemas = persistFacade.getDatabaseMetaDataFacade().getSchema().load(dbName);
            if (null != schemas) {
                result.put(dbName, schemas);
            }
        }
        return result;
    }
}
