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

import com.zaxxer.hikari.HikariDataSource;
import org.apache.shardingsphere.broadcast.api.config.BroadcastRuleConfiguration;
import org.apache.shardingsphere.broadcast.rule.BroadcastRule;
import org.apache.shardingsphere.broadcast.yaml.config.YamlBroadcastRuleConfiguration;
import org.apache.shardingsphere.broadcast.yaml.swapper.YamlBroadcastRuleConfigurationSwapper;
import org.apache.shardingsphere.distsql.handler.exception.DistSQLException;
import org.apache.shardingsphere.distsql.handler.exception.datasource.MissingRequiredDataSourcesException;
import org.apache.shardingsphere.distsql.handler.exception.storageunit.InvalidStorageUnitsException;
import org.apache.shardingsphere.distsql.handler.validate.DataSourcePropertiesValidateHandler;
import org.apache.shardingsphere.encrypt.api.config.CompatibleEncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.encrypt.yaml.config.YamlCompatibleEncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.yaml.config.YamlEncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.yaml.swapper.YamlCompatibleEncryptRuleConfigurationSwapper;
import org.apache.shardingsphere.encrypt.yaml.swapper.YamlEncryptRuleConfigurationSwapper;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.infra.database.DatabaseTypeEngine;
import org.apache.shardingsphere.infra.datasource.pool.creator.DataSourcePoolCreator;
import org.apache.shardingsphere.infra.datasource.props.DataSourceProperties;
import org.apache.shardingsphere.infra.datasource.props.DataSourcePropertiesCreator;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.util.exception.external.sql.type.generic.UnsupportedSQLOperationException;
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
import org.apache.shardingsphere.single.api.config.SingleRuleConfiguration;
import org.apache.shardingsphere.single.rule.SingleRule;
import org.apache.shardingsphere.single.yaml.config.pojo.YamlSingleRuleConfiguration;
import org.apache.shardingsphere.single.yaml.config.swapper.YamlSingleRuleConfigurationSwapper;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Yaml database configuration import executor.
 */
public final class YamlDatabaseConfigurationImportExecutor {
    
    private final ShardingRuleConfigurationImportChecker shardingRuleConfigImportChecker = new ShardingRuleConfigurationImportChecker();
    
    private final ReadwriteSplittingRuleConfigurationImportChecker readwriteSplittingRuleConfigImportChecker = new ReadwriteSplittingRuleConfigurationImportChecker();
    
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
        ShardingSpherePreconditions.checkNotNull(databaseName, () -> new UnsupportedSQLOperationException("Property `databaseName` in imported config is required"));
        if (ProxyContext.getInstance().databaseExists(databaseName)) {
            ShardingSpherePreconditions.checkState(ProxyContext.getInstance().getDatabase(databaseName).getResourceMetaData().getDataSources().isEmpty(),
                    () -> new UnsupportedSQLOperationException(String.format("Database `%s` exists and is not empty，overwrite is not supported", databaseName)));
        }
    }
    
    private void checkDataSource(final Map<String, YamlProxyDataSourceConfiguration> dataSources) {
        ShardingSpherePreconditions.checkState(!dataSources.isEmpty(), () -> new MissingRequiredDataSourcesException("Data source configurations in imported config is required"));
    }
    
    private void addDatabase(final String databaseName) {
        ContextManager contextManager = ProxyContext.getInstance().getContextManager();
        contextManager.getInstanceContext().getModeContextManager().createDatabase(databaseName);
        DatabaseType protocolType = DatabaseTypeEngine.getProtocolType(Collections.emptyMap(), contextManager.getMetaDataContexts().getMetaData().getProps());
        contextManager.getMetaDataContexts().getMetaData().addDatabase(databaseName, protocolType, contextManager.getMetaDataContexts().getMetaData().getProps());
    }
    
    private void addResources(final String databaseName, final Map<String, YamlProxyDataSourceConfiguration> yamlDataSourceMap) {
        Map<String, DataSourceProperties> dataSourcePropsMap = new LinkedHashMap<>(yamlDataSourceMap.size(), 1F);
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
        if (null == yamlRuleConfigs || yamlRuleConfigs.isEmpty()) {
            return;
        }
        Collection<RuleConfiguration> allRuleConfigs = new LinkedList<>();
        MetaDataContexts metaDataContexts = ProxyContext.getInstance().getContextManager().getMetaDataContexts();
        ShardingSphereDatabase database = metaDataContexts.getMetaData().getDatabase(databaseName);
        Map<Integer, Collection<RuleConfiguration>> ruleConfigsMap = new HashMap<>();
        for (YamlRuleConfiguration each : yamlRuleConfigs) {
            if (each instanceof YamlShardingRuleConfiguration) {
                YamlShardingRuleConfigurationSwapper swapper = new YamlShardingRuleConfigurationSwapper();
                ShardingRuleConfiguration shardingRuleConfig = swapper.swapToObject((YamlShardingRuleConfiguration) each);
                ruleConfigsMap.computeIfAbsent(swapper.getOrder(), key -> new LinkedList<>());
                ruleConfigsMap.get(swapper.getOrder()).add(shardingRuleConfig);
            } else if (each instanceof YamlReadwriteSplittingRuleConfiguration) {
                YamlReadwriteSplittingRuleConfigurationSwapper swapper = new YamlReadwriteSplittingRuleConfigurationSwapper();
                ReadwriteSplittingRuleConfiguration readwriteSplittingRuleConfig = swapper.swapToObject((YamlReadwriteSplittingRuleConfiguration) each);
                ruleConfigsMap.computeIfAbsent(swapper.getOrder(), key -> new LinkedList<>());
                ruleConfigsMap.get(swapper.getOrder()).add(readwriteSplittingRuleConfig);
            } else if (each instanceof YamlCompatibleEncryptRuleConfiguration) {
                YamlCompatibleEncryptRuleConfigurationSwapper swapper = new YamlCompatibleEncryptRuleConfigurationSwapper();
                CompatibleEncryptRuleConfiguration encryptRuleConfig = swapper.swapToObject((YamlCompatibleEncryptRuleConfiguration) each);
                ruleConfigsMap.computeIfAbsent(swapper.getOrder(), key -> new LinkedList<>());
                ruleConfigsMap.get(swapper.getOrder()).add(encryptRuleConfig);
            } else if (each instanceof YamlEncryptRuleConfiguration) {
                YamlEncryptRuleConfigurationSwapper swapper = new YamlEncryptRuleConfigurationSwapper();
                EncryptRuleConfiguration encryptRuleConfig = swapper.swapToObject((YamlEncryptRuleConfiguration) each);
                ruleConfigsMap.computeIfAbsent(swapper.getOrder(), key -> new LinkedList<>());
                ruleConfigsMap.get(swapper.getOrder()).add(encryptRuleConfig);
            } else if (each instanceof YamlShadowRuleConfiguration) {
                YamlShadowRuleConfigurationSwapper swapper = new YamlShadowRuleConfigurationSwapper();
                ShadowRuleConfiguration shadowRuleConfig = swapper.swapToObject((YamlShadowRuleConfiguration) each);
                ruleConfigsMap.computeIfAbsent(swapper.getOrder(), key -> new LinkedList<>());
                ruleConfigsMap.get(swapper.getOrder()).add(shadowRuleConfig);
            } else if (each instanceof YamlMaskRuleConfiguration) {
                YamlMaskRuleConfigurationSwapper swapper = new YamlMaskRuleConfigurationSwapper();
                MaskRuleConfiguration maskRuleConfig = swapper.swapToObject((YamlMaskRuleConfiguration) each);
                ruleConfigsMap.computeIfAbsent(swapper.getOrder(), key -> new LinkedList<>());
                ruleConfigsMap.get(swapper.getOrder()).add(maskRuleConfig);
            } else if (each instanceof YamlBroadcastRuleConfiguration) {
                YamlBroadcastRuleConfigurationSwapper swapper = new YamlBroadcastRuleConfigurationSwapper();
                BroadcastRuleConfiguration maskRuleConfig = swapper.swapToObject((YamlBroadcastRuleConfiguration) each);
                ruleConfigsMap.computeIfAbsent(swapper.getOrder(), key -> new LinkedList<>());
                ruleConfigsMap.get(swapper.getOrder()).add(maskRuleConfig);
            } else if (each instanceof YamlSingleRuleConfiguration) {
                YamlSingleRuleConfigurationSwapper swapper = new YamlSingleRuleConfigurationSwapper();
                SingleRuleConfiguration maskRuleConfig = swapper.swapToObject((YamlSingleRuleConfiguration) each);
                ruleConfigsMap.computeIfAbsent(swapper.getOrder(), key -> new LinkedList<>());
                ruleConfigsMap.get(swapper.getOrder()).add(maskRuleConfig);
            }
        }
        ruleConfigsMap.keySet().stream().sorted(Comparator.reverseOrder()).collect(Collectors.toList())
                .forEach(each -> addRules(allRuleConfigs, ruleConfigsMap.get(each), database));
        metaDataContexts.getPersistService().getDatabaseRulePersistService().persist(metaDataContexts.getMetaData().getDatabase(databaseName).getName(), allRuleConfigs);
    }
    
    private void addRules(final Collection<RuleConfiguration> allRuleConfigs, final Collection<RuleConfiguration> ruleConfigs, final ShardingSphereDatabase database) {
        RuleConfiguration ruleConfig = ruleConfigs.stream().findFirst().orElse(null);
        if (null == ruleConfig) {
            return;
        }
        if (ruleConfig instanceof ShardingRuleConfiguration) {
            ruleConfigs.forEach(each -> addShardingRuleConfiguration((ShardingRuleConfiguration) each, allRuleConfigs, database));
        } else if (ruleConfig instanceof ReadwriteSplittingRuleConfiguration) {
            ruleConfigs.forEach(each -> addReadwriteSplittingRuleConfiguration((ReadwriteSplittingRuleConfiguration) each, allRuleConfigs, database));
        } else if (ruleConfig instanceof EncryptRuleConfiguration) {
            ruleConfigs.forEach(each -> addEncryptRuleConfiguration((EncryptRuleConfiguration) each, allRuleConfigs, database));
        } else if (ruleConfig instanceof CompatibleEncryptRuleConfiguration) {
            ruleConfigs.forEach(each -> addEncryptRuleConfiguration(((CompatibleEncryptRuleConfiguration) each).convertToEncryptRuleConfiguration(), allRuleConfigs, database));
        } else if (ruleConfig instanceof ShadowRuleConfiguration) {
            ruleConfigs.forEach(each -> addShadowRuleConfiguration((ShadowRuleConfiguration) each, allRuleConfigs, database));
        } else if (ruleConfig instanceof MaskRuleConfiguration) {
            ruleConfigs.forEach(each -> addMaskRuleConfiguration((MaskRuleConfiguration) each, allRuleConfigs, database));
        } else if (ruleConfig instanceof BroadcastRuleConfiguration) {
            ruleConfigs.forEach(each -> addBroadcastRuleConfiguration((BroadcastRuleConfiguration) each, allRuleConfigs, database));
        } else if (ruleConfig instanceof SingleRuleConfiguration) {
            ruleConfigs.forEach(each -> addSingleRuleConfiguration((SingleRuleConfiguration) each, allRuleConfigs, database));
        }
    }
    
    private void addShardingRuleConfiguration(final ShardingRuleConfiguration shardingRuleConfig, final Collection<RuleConfiguration> allRuleConfigs, final ShardingSphereDatabase database) {
        InstanceContext instanceContext = ProxyContext.getInstance().getContextManager().getInstanceContext();
        shardingRuleConfigImportChecker.check(database, shardingRuleConfig);
        allRuleConfigs.add(shardingRuleConfig);
        database.getRuleMetaData().getRules().add(new ShardingRule(shardingRuleConfig, database.getResourceMetaData().getDataSources().keySet(), instanceContext));
    }
    
    private void addReadwriteSplittingRuleConfiguration(final ReadwriteSplittingRuleConfiguration readwriteSplittingRuleConfig,
                                                        final Collection<RuleConfiguration> allRuleConfigs, final ShardingSphereDatabase database) {
        InstanceContext instanceContext = ProxyContext.getInstance().getContextManager().getInstanceContext();
        Collection<ShardingSphereRule> rules = database.getRuleMetaData().getRules();
        readwriteSplittingRuleConfigImportChecker.check(database, readwriteSplittingRuleConfig);
        allRuleConfigs.add(readwriteSplittingRuleConfig);
        rules.add(new ReadwriteSplittingRule(database.getName(), readwriteSplittingRuleConfig, instanceContext));
    }
    
    private void addEncryptRuleConfiguration(final EncryptRuleConfiguration encryptRuleConfig, final Collection<RuleConfiguration> allRuleConfigs, final ShardingSphereDatabase database) {
        encryptRuleConfigImportChecker.check(database, encryptRuleConfig);
        allRuleConfigs.add(encryptRuleConfig);
        database.getRuleMetaData().getRules().add(new EncryptRule(encryptRuleConfig));
    }
    
    private void addShadowRuleConfiguration(final ShadowRuleConfiguration shadowRuleConfig, final Collection<RuleConfiguration> allRuleConfigs, final ShardingSphereDatabase database) {
        shadowRuleConfigImportChecker.check(database, shadowRuleConfig);
        allRuleConfigs.add(shadowRuleConfig);
        database.getRuleMetaData().getRules().add(new ShadowRule(shadowRuleConfig));
    }
    
    private void addMaskRuleConfiguration(final MaskRuleConfiguration maskRuleConfig, final Collection<RuleConfiguration> allRuleConfigs, final ShardingSphereDatabase database) {
        maskRuleConfigImportChecker.check(database, maskRuleConfig);
        allRuleConfigs.add(maskRuleConfig);
        database.getRuleMetaData().getRules().add(new MaskRule(maskRuleConfig));
    }
    
    private void addBroadcastRuleConfiguration(final BroadcastRuleConfiguration broadcastRuleConfig, final Collection<RuleConfiguration> allRuleConfigs, final ShardingSphereDatabase database) {
        allRuleConfigs.add(broadcastRuleConfig);
        database.getRuleMetaData().getRules().add(new BroadcastRule(broadcastRuleConfig, database.getName(), database.getResourceMetaData().getDataSources()));
    }
    
    private void addSingleRuleConfiguration(final SingleRuleConfiguration broadcastRuleConfig, final Collection<RuleConfiguration> allRuleConfigs, final ShardingSphereDatabase database) {
        allRuleConfigs.add(broadcastRuleConfig);
        database.getRuleMetaData().getRules().add(new SingleRule(broadcastRuleConfig, database.getName(), database.getResourceMetaData().getDataSources(), database.getRuleMetaData().getRules()));
    }
    
    private void dropDatabase(final String databaseName) {
        ProxyContext.getInstance().getContextManager().getInstanceContext().getModeContextManager().dropDatabase(databaseName);
    }
}
