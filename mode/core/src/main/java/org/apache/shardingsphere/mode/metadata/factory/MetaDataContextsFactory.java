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

package org.apache.shardingsphere.mode.metadata.factory;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.config.database.DatabaseConfiguration;
import org.apache.shardingsphere.infra.config.database.impl.DataSourceProvidedDatabaseConfiguration;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.database.DatabaseTypeEngine;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstanceContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabaseFactory;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.resource.node.StorageNode;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.statistics.builder.ShardingSphereStatisticsFactory;
import org.apache.shardingsphere.infra.rule.builder.global.GlobalRulesBuilder;
import org.apache.shardingsphere.mode.manager.builder.ContextManagerBuilderParameter;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.factory.init.MetaDataContextsInitFactory;
import org.apache.shardingsphere.mode.metadata.factory.init.type.LocalConfigurationMetaDataContextsInitFactory;
import org.apache.shardingsphere.mode.metadata.factory.init.type.RegisterCenterMetaDataContextsInitFactory;
import org.apache.shardingsphere.mode.metadata.manager.resource.SwitchingResource;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistFacade;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Meta data contexts factory.
 */
@RequiredArgsConstructor
public final class MetaDataContextsFactory {
    
    private final MetaDataPersistFacade persistFacade;
    
    private final ComputeNodeInstanceContext instanceContext;
    
    /**
     * Create meta data contexts.
     *
     * @param param context manager builder parameter
     * @return meta data contexts
     * @throws SQLException SQL exception
     */
    public MetaDataContexts create(final ContextManagerBuilderParameter param) throws SQLException {
        MetaDataContextsInitFactory initFactory = containsRegisteredDatabases()
                ? new RegisterCenterMetaDataContextsInitFactory(persistFacade, instanceContext)
                : new LocalConfigurationMetaDataContextsInitFactory(persistFacade, instanceContext);
        return initFactory.create(param);
    }
    
    private boolean containsRegisteredDatabases() {
        return !persistFacade.getDatabaseMetaDataFacade().getDatabase().loadAllDatabaseNames().isEmpty();
    }
    
    /**
     * Create meta data contexts by switch resource.
     *
     * @param databaseName database name
     * @param isLoadSchemasFromRegisterCenter is load schemas from register center or not
     * @param switchingResource switching resource
     * @param originalMetaDataContexts original meta data contexts
     * @return meta data contexts
     * @throws SQLException SQL exception
     */
    public MetaDataContexts createBySwitchResource(final String databaseName, final boolean isLoadSchemasFromRegisterCenter,
                                                   final SwitchingResource switchingResource, final MetaDataContexts originalMetaDataContexts) throws SQLException {
        ShardingSphereDatabase changedDatabase = createChangedDatabase(databaseName, isLoadSchemasFromRegisterCenter, switchingResource, null, originalMetaDataContexts);
        ConfigurationProperties props = originalMetaDataContexts.getMetaData().getProps();
        ShardingSphereMetaData clonedMetaData = cloneMetaData(originalMetaDataContexts.getMetaData(), changedDatabase);
        RuleMetaData changedGlobalMetaData = new RuleMetaData(
                GlobalRulesBuilder.buildRules(originalMetaDataContexts.getMetaData().getGlobalRuleMetaData().getConfigurations(), clonedMetaData.getAllDatabases(), props));
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(
                clonedMetaData.getAllDatabases(), originalMetaDataContexts.getMetaData().getGlobalResourceMetaData(), changedGlobalMetaData, props);
        return new MetaDataContexts(metaData, ShardingSphereStatisticsFactory.create(metaData, persistFacade.getStatisticsService().load(metaData)));
    }
    
    /**
     * Create meta data contexts by alter rule.
     *
     * @param databaseName database name
     * @param isLoadSchemasFromRegisterCenter is load schemas from register center or not
     * @param ruleConfigs rule configs
     * @param originalMetaDataContexts original meta data contexts
     * @return meta data contexts
     * @throws SQLException SQL exception
     */
    public MetaDataContexts createByAlterRule(final String databaseName, final boolean isLoadSchemasFromRegisterCenter,
                                              final Collection<RuleConfiguration> ruleConfigs, final MetaDataContexts originalMetaDataContexts) throws SQLException {
        ShardingSphereDatabase changedDatabase = createChangedDatabase(databaseName, isLoadSchemasFromRegisterCenter, null, ruleConfigs, originalMetaDataContexts);
        ShardingSphereMetaData clonedMetaData = cloneMetaData(originalMetaDataContexts.getMetaData(), changedDatabase);
        ConfigurationProperties props = originalMetaDataContexts.getMetaData().getProps();
        RuleMetaData changedGlobalMetaData = new RuleMetaData(
                GlobalRulesBuilder.buildRules(originalMetaDataContexts.getMetaData().getGlobalRuleMetaData().getConfigurations(), clonedMetaData.getAllDatabases(), props));
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(
                clonedMetaData.getAllDatabases(), originalMetaDataContexts.getMetaData().getGlobalResourceMetaData(), changedGlobalMetaData, props);
        return new MetaDataContexts(metaData, ShardingSphereStatisticsFactory.create(metaData, persistFacade.getStatisticsService().load(metaData)));
    }
    
    private ShardingSphereMetaData cloneMetaData(final ShardingSphereMetaData originalMetaData, final ShardingSphereDatabase changedDatabase) {
        ShardingSphereMetaData result = new ShardingSphereMetaData(
                originalMetaData.getAllDatabases(), originalMetaData.getGlobalResourceMetaData(), originalMetaData.getGlobalRuleMetaData(), originalMetaData.getProps());
        result.putDatabase(changedDatabase);
        return result;
    }
    
    /**
     * Create changed database.
     *
     * @param databaseName database name
     * @param isLoadSchemasFromRegisterCenter is load schemas from register center or not
     * @param switchingResource switching resource
     * @param ruleConfigs rule configurations
     * @param originalMetaDataContext original meta data contexts
     * @return changed database
     * @throws SQLException SQL exception
     */
    public ShardingSphereDatabase createChangedDatabase(final String databaseName, final boolean isLoadSchemasFromRegisterCenter,
                                                        final SwitchingResource switchingResource, final Collection<RuleConfiguration> ruleConfigs,
                                                        final MetaDataContexts originalMetaDataContext) throws SQLException {
        ShardingSphereDatabase database = originalMetaDataContext.getMetaData().getDatabase(databaseName);
        ResourceMetaData effectiveResourceMetaData = getEffectiveResourceMetaData(database, switchingResource);
        Collection<RuleConfiguration> toBeCreatedRuleConfigs = null == ruleConfigs ? database.getRuleMetaData().getConfigurations() : ruleConfigs;
        DatabaseConfiguration toBeCreatedDatabaseConfig = getDatabaseConfiguration(effectiveResourceMetaData, switchingResource, toBeCreatedRuleConfigs);
        return createChangedDatabase(database.getName(), isLoadSchemasFromRegisterCenter, toBeCreatedDatabaseConfig, originalMetaDataContext.getMetaData().getProps());
    }
    
    private ShardingSphereDatabase createChangedDatabase(final String databaseName, final boolean isLoadSchemasFromRegisterCenter, final DatabaseConfiguration databaseConfig,
                                                         final ConfigurationProperties props) throws SQLException {
        DatabaseType protocolType = DatabaseTypeEngine.getProtocolType(databaseConfig, props);
        return isLoadSchemasFromRegisterCenter
                ? ShardingSphereDatabaseFactory.create(databaseName, protocolType, databaseConfig, instanceContext, persistFacade.getDatabaseMetaDataFacade().getSchema().load(databaseName))
                : ShardingSphereDatabaseFactory.create(databaseName, protocolType, databaseConfig, props, instanceContext);
    }
    
    private ResourceMetaData getEffectiveResourceMetaData(final ShardingSphereDatabase database, final SwitchingResource switchingResource) {
        Map<StorageNode, DataSource> storageNodes = getStorageNodes(database.getResourceMetaData().getDataSources(), switchingResource);
        Map<String, StorageUnit> storageUnits = getStorageUnits(database.getResourceMetaData().getStorageUnits(), switchingResource);
        return new ResourceMetaData(storageNodes, storageUnits);
    }
    
    private Map<StorageNode, DataSource> getStorageNodes(final Map<StorageNode, DataSource> currentStorageNodes, final SwitchingResource switchingResource) {
        return currentStorageNodes.entrySet().stream()
                .filter(entry -> null == switchingResource || !switchingResource.getStaleDataSources().containsKey(entry.getKey()))
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue, (a, b) -> b, () -> new LinkedHashMap<>(currentStorageNodes.size(), 1F)));
    }
    
    private Map<String, StorageUnit> getStorageUnits(final Map<String, StorageUnit> currentStorageUnits, final SwitchingResource switchingResource) {
        return currentStorageUnits.entrySet().stream()
                .filter(entry -> null == switchingResource || !switchingResource.getStaleStorageUnitNames().contains(entry.getKey()))
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue, (a, b) -> b, () -> new LinkedHashMap<>(currentStorageUnits.size(), 1F)));
    }
    
    private DatabaseConfiguration getDatabaseConfiguration(final ResourceMetaData currentResourceMetaData,
                                                           final SwitchingResource switchingResource, final Collection<RuleConfiguration> toBeCreatedRuleConfigs) {
        Map<String, DataSourcePoolProperties> propsMap = null == switchingResource ? currentResourceMetaData.getStorageUnits().entrySet().stream()
                .collect(Collectors.toMap(Entry::getKey, entry -> entry.getValue().getDataSourcePoolProperties(), (oldValue, currentValue) -> oldValue, LinkedHashMap::new))
                : switchingResource.getMergedDataSourcePoolPropertiesMap();
        return new DataSourceProvidedDatabaseConfiguration(getMergedStorageNodeDataSources(currentResourceMetaData, switchingResource), toBeCreatedRuleConfigs, propsMap);
    }
    
    private Map<StorageNode, DataSource> getMergedStorageNodeDataSources(final ResourceMetaData currentResourceMetaData, final SwitchingResource switchingResource) {
        Map<StorageNode, DataSource> result = currentResourceMetaData.getDataSources();
        if (null != switchingResource && !switchingResource.getNewDataSources().isEmpty()) {
            result.putAll(switchingResource.getNewDataSources());
        }
        return result;
    }
}
