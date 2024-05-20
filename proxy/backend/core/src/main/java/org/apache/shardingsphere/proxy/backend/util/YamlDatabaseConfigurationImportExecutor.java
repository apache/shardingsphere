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

package org.apache.shardingsphere.proxy.backend.util;

import org.apache.shardingsphere.distsql.handler.validate.DistSQLDataSourcePoolPropertiesValidator;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.config.rule.checker.RuleConfigurationCheckEngine;
import org.apache.shardingsphere.infra.database.DatabaseTypeEngine;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.datasource.pool.config.DataSourceConfiguration;
import org.apache.shardingsphere.infra.datasource.pool.creator.DataSourcePoolCreator;
import org.apache.shardingsphere.infra.datasource.pool.props.creator.DataSourcePoolPropertiesCreator;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.core.external.sql.ShardingSphereSQLException;
import org.apache.shardingsphere.infra.exception.dialect.exception.syntax.database.DatabaseCreateExistsException;
import org.apache.shardingsphere.infra.exception.kernel.metadata.MissingRequiredDatabaseException;
import org.apache.shardingsphere.infra.exception.kernel.metadata.resource.storageunit.StorageUnitsOperateException;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstanceContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.node.StorageNode;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnitNodeMapCreator;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.builder.database.DatabaseRuleBuilder;
import org.apache.shardingsphere.infra.spi.type.ordered.OrderedSPILoader;
import org.apache.shardingsphere.infra.yaml.config.pojo.rule.YamlRuleConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.YamlRuleConfigurationSwapper;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.proxy.backend.config.yaml.YamlProxyDataSourceConfiguration;
import org.apache.shardingsphere.proxy.backend.config.yaml.YamlProxyDatabaseConfiguration;
import org.apache.shardingsphere.proxy.backend.config.yaml.swapper.YamlProxyDataSourceConfigurationSwapper;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Yaml database configuration import executor.
 */
public final class YamlDatabaseConfigurationImportExecutor {
    
    private final YamlProxyDataSourceConfigurationSwapper dataSourceConfigSwapper = new YamlProxyDataSourceConfigurationSwapper();
    
    private final DistSQLDataSourcePoolPropertiesValidator validateHandler = new DistSQLDataSourcePoolPropertiesValidator();
    
    /**
     * Import proxy database from yaml configuration.
     *
     * @param yamlConfig yaml proxy database configuration
     */
    public void importDatabaseConfiguration(final YamlProxyDatabaseConfiguration yamlConfig) {
        String databaseName = yamlConfig.getDatabaseName();
        checkDatabase(databaseName);
        addDatabase(databaseName);
        if (null == yamlConfig.getDataSources() || yamlConfig.getDataSources().isEmpty()) {
            return;
        }
        importDataSources(databaseName, yamlConfig.getDataSources());
        try {
            importRules(databaseName, yamlConfig.getRules());
        } catch (final ShardingSphereSQLException ex) {
            dropDatabase(databaseName);
            throw ex;
        }
    }
    
    private void checkDatabase(final String databaseName) {
        ShardingSpherePreconditions.checkNotEmpty(databaseName, MissingRequiredDatabaseException::new);
        ShardingSpherePreconditions.checkState(!ProxyContext.getInstance().databaseExists(databaseName), () -> new DatabaseCreateExistsException(databaseName));
    }
    
    private void addDatabase(final String databaseName) {
        ContextManager contextManager = ProxyContext.getInstance().getContextManager();
        contextManager.getComputeNodeInstanceContext().getModeContextManager().createDatabase(databaseName);
        DatabaseType protocolType = DatabaseTypeEngine.getProtocolType(Collections.emptyMap(), contextManager.getMetaDataContexts().getMetaData().getProps());
        contextManager.getMetaDataContexts().getMetaData().addDatabase(databaseName, protocolType, contextManager.getMetaDataContexts().getMetaData().getProps());
    }
    
    private void importDataSources(final String databaseName, final Map<String, YamlProxyDataSourceConfiguration> yamlDataSourceMap) {
        Map<String, DataSourcePoolProperties> propsMap = new LinkedHashMap<>(yamlDataSourceMap.size(), 1F);
        for (Entry<String, YamlProxyDataSourceConfiguration> entry : yamlDataSourceMap.entrySet()) {
            DataSourceConfiguration dataSourceConfig = dataSourceConfigSwapper.swap(entry.getValue());
            propsMap.put(entry.getKey(), DataSourcePoolPropertiesCreator.create(dataSourceConfig));
        }
        validateHandler.validate(propsMap);
        try {
            ProxyContext.getInstance().getContextManager().getComputeNodeInstanceContext().getModeContextManager().registerStorageUnits(databaseName, propsMap);
        } catch (final SQLException ex) {
            throw new StorageUnitsOperateException("import", propsMap.keySet(), ex);
        }
        Map<String, StorageUnit> storageUnits = ProxyContext.getInstance().getContextManager()
                .getMetaDataContexts().getMetaData().getDatabase(databaseName).getResourceMetaData().getStorageUnits();
        Map<String, StorageNode> toBeAddedStorageNode = StorageUnitNodeMapCreator.create(propsMap);
        for (Entry<String, DataSourcePoolProperties> entry : propsMap.entrySet()) {
            storageUnits.put(entry.getKey(), new StorageUnit(toBeAddedStorageNode.get(entry.getKey()), entry.getValue(), DataSourcePoolCreator.create(entry.getValue())));
        }
    }
    
    private void importRules(final String databaseName, final Collection<YamlRuleConfiguration> yamlRuleConfigs) {
        if (null == yamlRuleConfigs || yamlRuleConfigs.isEmpty()) {
            return;
        }
        Collection<RuleConfiguration> ruleConfigs = new LinkedList<>();
        MetaDataContexts metaDataContexts = ProxyContext.getInstance().getContextManager().getMetaDataContexts();
        ShardingSphereDatabase database = metaDataContexts.getMetaData().getDatabase(databaseName);
        swapToRuleConfigs(yamlRuleConfigs).values().forEach(each -> addRule(ruleConfigs, each, database));
        metaDataContexts.getPersistService().getDatabaseRulePersistService().persist(metaDataContexts.getMetaData().getDatabase(databaseName).getName(), ruleConfigs);
    }
    
    private void addRule(final Collection<RuleConfiguration> ruleConfigs, final RuleConfiguration ruleConfig, final ShardingSphereDatabase database) {
        RuleConfigurationCheckEngine.check(ruleConfig, database);
        ruleConfigs.add(ruleConfig);
        database.getRuleMetaData().getRules().add(buildRule(ruleConfig, database));
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    private ShardingSphereRule buildRule(final RuleConfiguration ruleConfig, final ShardingSphereDatabase database) {
        DatabaseRuleBuilder ruleBuilder = OrderedSPILoader.getServices(DatabaseRuleBuilder.class, Collections.singleton(ruleConfig)).get(ruleConfig);
        Map<String, DataSource> dataSources = database.getResourceMetaData().getStorageUnits().entrySet().stream()
                .collect(Collectors.toMap(Entry::getKey, entry -> entry.getValue().getDataSource(), (oldValue, currentValue) -> oldValue, LinkedHashMap::new));
        ComputeNodeInstanceContext computeNodeInstanceContext = ProxyContext.getInstance().getContextManager().getComputeNodeInstanceContext();
        return ruleBuilder.build(ruleConfig, database.getName(), database.getProtocolType(), dataSources, database.getRuleMetaData().getRules(), computeNodeInstanceContext);
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    private Map<Integer, RuleConfiguration> swapToRuleConfigs(final Collection<YamlRuleConfiguration> yamlRuleConfigs) {
        Map<Integer, RuleConfiguration> result = new TreeMap<>(Comparator.reverseOrder());
        for (YamlRuleConfiguration each : yamlRuleConfigs) {
            YamlRuleConfigurationSwapper swapper = OrderedSPILoader.getServicesByClass(YamlRuleConfigurationSwapper.class, Collections.singleton(each.getRuleConfigurationType()))
                    .get(each.getRuleConfigurationType());
            result.put(swapper.getOrder(), (RuleConfiguration) swapper.swapToObject(each));
        }
        return result;
    }
    
    private void dropDatabase(final String databaseName) {
        ProxyContext.getInstance().getContextManager().getComputeNodeInstanceContext().getModeContextManager().dropDatabase(databaseName);
    }
}
