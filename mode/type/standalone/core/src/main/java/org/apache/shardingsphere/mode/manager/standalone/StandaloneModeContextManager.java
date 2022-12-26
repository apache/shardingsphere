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

package org.apache.shardingsphere.mode.manager.standalone;

import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.datasource.props.DataSourceProperties;
import org.apache.shardingsphere.infra.instance.mode.ModeContextManager;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rule.identifier.type.ResourceHeldRule;
import org.apache.shardingsphere.infra.util.spi.type.ordered.cache.OrderedServicesCache;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.ContextManagerAware;
import org.apache.shardingsphere.mode.manager.switcher.ResourceSwitchManager;
import org.apache.shardingsphere.mode.manager.switcher.SwitchingResource;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Standalone mode context manager.
 */
public final class StandaloneModeContextManager implements ModeContextManager, ContextManagerAware {
    
    private ContextManager contextManager;
    
    private volatile MetaDataContexts metaDataContexts;
    
    @Override
    public void createDatabase(final String databaseName) {
        contextManager.addDatabase(databaseName);
        contextManager.getMetaDataContexts().getPersistService().getDatabaseMetaDataService().addDatabase(databaseName);
        clearServiceCache();
    }
    
    @Override
    public void dropDatabase(final String databaseName) {
        contextManager.dropDatabase(databaseName);
        contextManager.getMetaDataContexts().getPersistService().getDatabaseMetaDataService().dropDatabase(databaseName);
        clearServiceCache();
    }
    
    @Override
    public void registerStorageUnits(final String databaseName, final Map<String, DataSourceProperties> toBeRegisterStorageUnitProps) throws SQLException {
        SwitchingResource switchingResource = new ResourceSwitchManager().create(metaDataContexts.getMetaData().getDatabase(databaseName).getResourceMetaData(), toBeRegisterStorageUnitProps);
        metaDataContexts.getMetaData().getDatabases().putAll(contextManager.createChangedDatabases(databaseName, switchingResource, null));
        metaDataContexts.getMetaData().getGlobalRuleMetaData().findRules(ResourceHeldRule.class).forEach(each -> each.addResource(metaDataContexts.getMetaData().getDatabase(databaseName)));
        metaDataContexts.getMetaData().getDatabase(databaseName).getSchemas().forEach((schemaName, schema) -> metaDataContexts.getPersistService().getDatabaseMetaDataService()
                .persist(metaDataContexts.getMetaData().getActualDatabaseName(databaseName), schemaName, schema));
        metaDataContexts.getPersistService().getDataSourceService().append(metaDataContexts.getMetaData().getActualDatabaseName(databaseName), toBeRegisterStorageUnitProps);
        clearServiceCache();
    }
    
    @Override
    public void alterStorageUnits(final String databaseName, final Map<String, DataSourceProperties> toBeUpdatedStorageUnitProps) throws SQLException {
        SwitchingResource switchingResource = new ResourceSwitchManager().create(metaDataContexts.getMetaData().getDatabase(databaseName).getResourceMetaData(), toBeUpdatedStorageUnitProps);
        metaDataContexts.getMetaData().getDatabases().putAll(contextManager.createChangedDatabases(databaseName, switchingResource, null));
        metaDataContexts.getMetaData().getGlobalRuleMetaData().findRules(ResourceHeldRule.class).forEach(each -> each.addResource(metaDataContexts.getMetaData().getDatabase(databaseName)));
        metaDataContexts.getMetaData().getDatabases().putAll(contextManager.newShardingSphereDatabase(metaDataContexts.getMetaData().getDatabase(databaseName)));
        metaDataContexts.getPersistService().getDataSourceService().append(metaDataContexts.getMetaData().getActualDatabaseName(databaseName), toBeUpdatedStorageUnitProps);
        switchingResource.closeStaleDataSources();
        clearServiceCache();
    }
    
    @Override
    public void unregisterStorageUnits(final String databaseName, final Collection<String> toBeDroppedStorageUnitNames) throws SQLException {
        Map<String, DataSourceProperties> dataSourcePropsMap = metaDataContexts.getPersistService().getDataSourceService().load(metaDataContexts.getMetaData().getActualDatabaseName(databaseName));
        Map<String, DataSourceProperties> toBeDeletedDataSourcePropsMap = getToBeDeletedDataSourcePropsMap(dataSourcePropsMap, toBeDroppedStorageUnitNames);
        SwitchingResource switchingResource =
                new ResourceSwitchManager().createByDropResource(metaDataContexts.getMetaData().getDatabase(databaseName).getResourceMetaData(), toBeDeletedDataSourcePropsMap);
        metaDataContexts.getMetaData().getDatabases().putAll(contextManager.renewDatabase(metaDataContexts.getMetaData().getDatabase(databaseName), switchingResource));
        MetaDataContexts reloadMetaDataContexts = contextManager.createMetaDataContexts(databaseName, switchingResource, null);
        contextManager.alterSchemaMetaData(databaseName, reloadMetaDataContexts.getMetaData().getDatabase(databaseName), metaDataContexts.getMetaData().getDatabase(databaseName));
        contextManager.deletedSchemaNames(databaseName, reloadMetaDataContexts.getMetaData().getDatabase(databaseName), metaDataContexts.getMetaData().getDatabase(databaseName));
        metaDataContexts = reloadMetaDataContexts;
        Map<String, DataSourceProperties> toBeReversedDataSourcePropsMap = getToBeReversedDataSourcePropsMap(dataSourcePropsMap, toBeDroppedStorageUnitNames);
        metaDataContexts.getPersistService().getDataSourceService().persist(metaDataContexts.getMetaData().getActualDatabaseName(databaseName), toBeReversedDataSourcePropsMap);
        switchingResource.closeStaleDataSources();
        clearServiceCache();
    }
    
    private Map<String, DataSourceProperties> getToBeDeletedDataSourcePropsMap(final Map<String, DataSourceProperties> dataSourcePropsMap, final Collection<String> toBeDroppedResourceNames) {
        return dataSourcePropsMap.entrySet().stream().filter(entry -> toBeDroppedResourceNames.contains(entry.getKey())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
    
    private Map<String, DataSourceProperties> getToBeReversedDataSourcePropsMap(final Map<String, DataSourceProperties> dataSourcePropsMap, final Collection<String> toBeDroppedResourceNames) {
        return dataSourcePropsMap.entrySet().stream().filter(entry -> !toBeDroppedResourceNames.contains(entry.getKey())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
    
    @Override
    public void alterRuleConfiguration(final String databaseName, final Collection<RuleConfiguration> ruleConfigs) {
        // TODO Verify it
        ShardingSphereDatabase currentDatabase = metaDataContexts.getMetaData().getDatabase(databaseName);
        contextManager.alterRuleConfiguration(databaseName, ruleConfigs);
        ShardingSphereDatabase reloadDatabase = metaDataContexts.getMetaData().getDatabase(databaseName);
        contextManager.alterSchemaMetaData(databaseName, reloadDatabase, currentDatabase);
        metaDataContexts.getPersistService().getDatabaseRulePersistService().persist(metaDataContexts.getMetaData().getActualDatabaseName(databaseName), ruleConfigs);
        clearServiceCache();
    }
    
    @Override
    public void alterGlobalRuleConfiguration(final Collection<RuleConfiguration> globalRuleConfigs) {
        contextManager.alterGlobalRuleConfiguration(globalRuleConfigs);
        metaDataContexts.getPersistService().getGlobalRuleService().persist(metaDataContexts.getMetaData().getGlobalRuleMetaData().getConfigurations());
        clearServiceCache();
    }
    
    @Override
    public void alterProperties(final Properties props) {
        contextManager.alterProperties(props);
        if (null != metaDataContexts.getPersistService().getPropsService()) {
            metaDataContexts.getPersistService().getPropsService().persist(props);
        }
        clearServiceCache();
    }
    
    private void clearServiceCache() {
        OrderedServicesCache.clearCache();
    }
    
    @Override
    public void setContextManagerAware(final ContextManager contextManager) {
        this.contextManager = contextManager;
        this.metaDataContexts = contextManager.getMetaDataContexts();
    }
}
