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

import com.google.common.base.Preconditions;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.shardingsphere.dbdiscovery.api.config.DatabaseDiscoveryRuleConfiguration;
import org.apache.shardingsphere.dbdiscovery.rule.DatabaseDiscoveryRule;
import org.apache.shardingsphere.dbdiscovery.yaml.config.YamlDatabaseDiscoveryRuleConfiguration;
import org.apache.shardingsphere.dbdiscovery.yaml.swapper.YamlDatabaseDiscoveryRuleConfigurationSwapper;
import org.apache.shardingsphere.distsql.handler.exception.DistSQLException;
import org.apache.shardingsphere.distsql.handler.exception.storageunit.InvalidStorageUnitsException;
import org.apache.shardingsphere.distsql.handler.validate.DataSourcePropertiesValidateHandler;
import org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.encrypt.yaml.config.YamlEncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.yaml.swapper.YamlEncryptRuleConfigurationSwapper;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeEngine;
import org.apache.shardingsphere.infra.datasource.pool.creator.DataSourcePoolCreator;
import org.apache.shardingsphere.infra.datasource.props.DataSourceProperties;
import org.apache.shardingsphere.infra.datasource.props.DataSourcePropertiesCreator;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.yaml.config.pojo.rule.YamlRuleConfiguration;
import org.apache.shardingsphere.mask.api.config.MaskRuleConfiguration;
import org.apache.shardingsphere.mask.rule.MaskRule;
import org.apache.shardingsphere.mask.yaml.config.YamlMaskRuleConfiguration;
import org.apache.shardingsphere.mask.yaml.swapper.YamlMaskRuleConfigurationSwapper;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.proxy.backend.config.yaml.YamlProxyDataSourceConfiguration;
import org.apache.shardingsphere.proxy.backend.config.yaml.YamlProxyDatabaseConfiguration;
import org.apache.shardingsphere.proxy.backend.config.yaml.swapper.YamlProxyDataSourceConfigurationSwapper;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.common.checker.DatabaseDiscoveryRuleConfigurationImportChecker;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.common.checker.EncryptRuleConfigurationImportChecker;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.common.checker.MaskRuleConfigurationImportChecker;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.common.checker.ReadwriteSplittingRuleConfigurationImportChecker;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.common.checker.ShadowRuleConfigurationImportChecker;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.common.checker.ShardingRuleConfigurationImportChecker;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.rule.ReadwriteSplittingRule;
import org.apache.shardingsphere.readwritesplitting.yaml.config.YamlReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.yaml.swapper.YamlReadwriteSplittingRuleConfigurationSwapper;
import org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.rule.ShadowRule;
import org.apache.shardingsphere.shadow.yaml.config.YamlShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.yaml.swapper.YamlShadowRuleConfigurationSwapper;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.yaml.config.YamlShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.yaml.swapper.YamlShardingRuleConfigurationSwapper;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Yaml database configuration import executor.
 */
public final class YamlDatabaseConfigurationImportExecutor {
    
    private final ShardingRuleConfigurationImportChecker shardingRuleConfigImportChecker = new ShardingRuleConfigurationImportChecker();
    
    private final ReadwriteSplittingRuleConfigurationImportChecker readwriteSplittingRuleConfigImportChecker = new ReadwriteSplittingRuleConfigurationImportChecker();
    
    private final DatabaseDiscoveryRuleConfigurationImportChecker databaseDiscoveryRuleConfigImportChecker = new DatabaseDiscoveryRuleConfigurationImportChecker();
    
    private final EncryptRuleConfigurationImportChecker encryptRuleConfigImportChecker = new EncryptRuleConfigurationImportChecker();
    
    private final ShadowRuleConfigurationImportChecker shadowRuleConfigImportChecker = new ShadowRuleConfigurationImportChecker();
    
    private final MaskRuleConfigurationImportChecker maskRuleConfigImportChecker = new MaskRuleConfigurationImportChecker();
    
    private final YamlProxyDataSourceConfigurationSwapper dataSourceConfigSwapper = new YamlProxyDataSourceConfigurationSwapper();
    
    private final DataSourcePropertiesValidateHandler validateHandler = new DataSourcePropertiesValidateHandler();
    
    /**
     * Import proxy database from yaml configuration.
     *
     * @param yamlConfig yaml proxy database configuration
     */
    public void importDatabaseConfiguration(final YamlProxyDatabaseConfiguration yamlConfig) {
        String databaseName = yamlConfig.getDatabaseName();
        checkDatabase(databaseName);
        checkDataSource(yamlConfig.getDataSources());
        addDatabase(databaseName);
        addResources(databaseName, yamlConfig.getDataSources());
        try {
            addRules(databaseName, yamlConfig.getRules());
        } catch (final DistSQLException ex) {
            dropDatabase(databaseName);
            throw ex;
        }
    }
    
    private void checkDatabase(final String databaseName) {
        Preconditions.checkNotNull(databaseName, "Property `databaseName` in imported config is required");
        if (ProxyContext.getInstance().databaseExists(databaseName)) {
            Preconditions.checkState(ProxyContext.getInstance().getDatabase(databaseName).getResourceMetaData().getDataSources().isEmpty(), "Database `%s` exists and is not empty", databaseName);
        }
    }
    
    private void checkDataSource(final Map<String, YamlProxyDataSourceConfiguration> dataSources) {
        Preconditions.checkState(!dataSources.isEmpty(), "Data source configurations in imported config is required");
    }
    
    private void addDatabase(final String databaseName) {
        ContextManager contextManager = ProxyContext.getInstance().getContextManager();
        contextManager.getInstanceContext().getModeContextManager().createDatabase(databaseName);
        DatabaseType protocolType = DatabaseTypeEngine.getProtocolType(Collections.emptyMap(), contextManager.getMetaDataContexts().getMetaData().getProps());
        contextManager.getMetaDataContexts().getMetaData().addDatabase(databaseName, protocolType);
    }
    
    private void addResources(final String databaseName, final Map<String, YamlProxyDataSourceConfiguration> yamlDataSourceMap) {
        Map<String, DataSourceProperties> dataSourcePropsMap = new LinkedHashMap<>(yamlDataSourceMap.size(), 1);
        for (Entry<String, YamlProxyDataSourceConfiguration> entry : yamlDataSourceMap.entrySet()) {
            dataSourcePropsMap.put(entry.getKey(), DataSourcePropertiesCreator.create(HikariDataSource.class.getName(), dataSourceConfigSwapper.swap(entry.getValue())));
        }
        validateHandler.validate(dataSourcePropsMap);
        try {
            ProxyContext.getInstance().getContextManager().getInstanceContext().getModeContextManager().registerStorageUnits(databaseName, dataSourcePropsMap);
        } catch (final SQLException ex) {
            throw new InvalidStorageUnitsException(Collections.singleton(ex.getMessage()));
        }
        Map<String, DataSource> dataSource = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getDatabase(databaseName).getResourceMetaData().getDataSources();
        dataSourcePropsMap.forEach((key, value) -> dataSource.put(key, DataSourcePoolCreator.create(value)));
    }
    
    private void addRules(final String databaseName, final Collection<YamlRuleConfiguration> yamlRuleConfigs) {
        if (yamlRuleConfigs == null || yamlRuleConfigs.isEmpty()) {
            return;
        }
        Collection<RuleConfiguration> ruleConfigs = new LinkedList<>();
        MetaDataContexts metaDataContexts = ProxyContext.getInstance().getContextManager().getMetaDataContexts();
        InstanceContext instanceContext = ProxyContext.getInstance().getContextManager().getInstanceContext();
        ShardingSphereDatabase database = metaDataContexts.getMetaData().getDatabase(databaseName);
        Collection<ShardingSphereRule> rules = database.getRuleMetaData().getRules();
        for (YamlRuleConfiguration each : yamlRuleConfigs) {
            if (each instanceof YamlShardingRuleConfiguration) {
                ShardingRuleConfiguration shardingRuleConfig = new YamlShardingRuleConfigurationSwapper().swapToObject((YamlShardingRuleConfiguration) each);
                shardingRuleConfigImportChecker.check(database, shardingRuleConfig);
                ruleConfigs.add(shardingRuleConfig);
                rules.add(new ShardingRule(shardingRuleConfig, database.getResourceMetaData().getDataSources().keySet(), instanceContext));
            } else if (each instanceof YamlReadwriteSplittingRuleConfiguration) {
                ReadwriteSplittingRuleConfiguration readwriteSplittingRuleConfig = new YamlReadwriteSplittingRuleConfigurationSwapper().swapToObject((YamlReadwriteSplittingRuleConfiguration) each);
                readwriteSplittingRuleConfigImportChecker.check(database, readwriteSplittingRuleConfig);
                ruleConfigs.add(readwriteSplittingRuleConfig);
                rules.add(new ReadwriteSplittingRule(databaseName, readwriteSplittingRuleConfig, rules, instanceContext));
            } else if (each instanceof YamlDatabaseDiscoveryRuleConfiguration) {
                DatabaseDiscoveryRuleConfiguration databaseDiscoveryRuleConfig = new YamlDatabaseDiscoveryRuleConfigurationSwapper().swapToObject((YamlDatabaseDiscoveryRuleConfiguration) each);
                databaseDiscoveryRuleConfigImportChecker.check(database, databaseDiscoveryRuleConfig);
                ruleConfigs.add(databaseDiscoveryRuleConfig);
                rules.add(new DatabaseDiscoveryRule(databaseName, database.getResourceMetaData().getDataSources(), databaseDiscoveryRuleConfig, instanceContext));
            } else if (each instanceof YamlEncryptRuleConfiguration) {
                EncryptRuleConfiguration encryptRuleConfig = new YamlEncryptRuleConfigurationSwapper().swapToObject((YamlEncryptRuleConfiguration) each);
                encryptRuleConfigImportChecker.check(database, encryptRuleConfig);
                ruleConfigs.add(encryptRuleConfig);
                rules.add(new EncryptRule(encryptRuleConfig));
            } else if (each instanceof YamlShadowRuleConfiguration) {
                ShadowRuleConfiguration shadowRuleConfig = new YamlShadowRuleConfigurationSwapper().swapToObject((YamlShadowRuleConfiguration) each);
                shadowRuleConfigImportChecker.check(database, shadowRuleConfig);
                ruleConfigs.add(shadowRuleConfig);
                rules.add(new ShadowRule(shadowRuleConfig));
            } else if (each instanceof YamlMaskRuleConfiguration) {
                MaskRuleConfiguration maskRuleConfig = new YamlMaskRuleConfigurationSwapper().swapToObject((YamlMaskRuleConfiguration) each);
                maskRuleConfigImportChecker.check(database, maskRuleConfig);
                ruleConfigs.add(maskRuleConfig);
                rules.add(new MaskRule(maskRuleConfig));
            }
        }
        metaDataContexts.getPersistService().getDatabaseRulePersistService().persist(metaDataContexts.getMetaData().getActualDatabaseName(databaseName), ruleConfigs);
    }
    
    private void dropDatabase(final String databaseName) {
        ProxyContext.getInstance().getContextManager().getInstanceContext().getModeContextManager().dropDatabase(databaseName);
    }
}
