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

package org.apache.shardingsphere.proxy.backend.handler.distsql.ral.queryable;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.shardingsphere.dbdiscovery.api.config.DatabaseDiscoveryRuleConfiguration;
import org.apache.shardingsphere.dbdiscovery.api.config.rule.DatabaseDiscoveryDataSourceRuleConfiguration;
import org.apache.shardingsphere.dbdiscovery.api.config.rule.DatabaseDiscoveryHeartBeatConfiguration;
import org.apache.shardingsphere.dbdiscovery.yaml.config.YamlDatabaseDiscoveryRuleConfiguration;
import org.apache.shardingsphere.dbdiscovery.yaml.swapper.YamlDatabaseDiscoveryRuleConfigurationSwapper;
import org.apache.shardingsphere.distsql.parser.statement.ral.queryable.ConvertYamlConfigurationStatement;
import org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptColumnRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.encrypt.yaml.config.YamlEncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.yaml.swapper.YamlEncryptRuleConfigurationSwapper;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.datasource.props.DataSourceProperties;
import org.apache.shardingsphere.infra.datasource.props.DataSourcePropertiesCreator;
import org.apache.shardingsphere.infra.datasource.props.custom.CustomDataSourceProperties;
import org.apache.shardingsphere.infra.datasource.props.synonym.PoolPropertySynonyms;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.mask.api.config.MaskRuleConfiguration;
import org.apache.shardingsphere.mask.api.config.rule.MaskColumnRuleConfiguration;
import org.apache.shardingsphere.mask.api.config.rule.MaskTableRuleConfiguration;
import org.apache.shardingsphere.mask.yaml.config.YamlMaskRuleConfiguration;
import org.apache.shardingsphere.mask.yaml.swapper.YamlMaskRuleConfigurationSwapper;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.config.yaml.YamlProxyDataSourceConfiguration;
import org.apache.shardingsphere.proxy.backend.config.yaml.YamlProxyDatabaseConfiguration;
import org.apache.shardingsphere.proxy.backend.config.yaml.swapper.YamlProxyDataSourceConfigurationSwapper;
import org.apache.shardingsphere.proxy.backend.exception.FileIOException;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.QueryableRALBackendHandler;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.common.constant.DistSQLScriptConstants;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.strategy.DynamicReadwriteSplittingStrategyConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.strategy.StaticReadwriteSplittingStrategyConfiguration;
import org.apache.shardingsphere.readwritesplitting.yaml.config.YamlReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.yaml.swapper.YamlReadwriteSplittingRuleConfigurationSwapper;
import org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.api.config.datasource.ShadowDataSourceConfiguration;
import org.apache.shardingsphere.shadow.api.config.table.ShadowTableConfiguration;
import org.apache.shardingsphere.shadow.yaml.config.YamlShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.yaml.swapper.YamlShadowRuleConfigurationSwapper;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableReferenceRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.audit.ShardingAuditStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.keygen.KeyGenerateStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ComplexShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.YamlShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.yaml.swapper.YamlShardingRuleConfigurationSwapper;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.TreeMap;

/**
 * Convert YAML configuration handler.
 */
public final class ConvertYamlConfigurationHandler extends QueryableRALBackendHandler<ConvertYamlConfigurationStatement> {
    
    private final YamlProxyDataSourceConfigurationSwapper dataSourceConfigSwapper = new YamlProxyDataSourceConfigurationSwapper();
    
    @Override
    protected Collection<String> getColumnNames() {
        return Collections.singleton("distsql");
    }
    
    @Override
    protected Collection<LocalDataQueryResultRow> getRows(final ContextManager contextManager) {
        File file = new File(getSqlStatement().getFilePath());
        YamlProxyDatabaseConfiguration yamlConfig;
        try {
            yamlConfig = YamlEngine.unmarshal(file, YamlProxyDatabaseConfiguration.class);
        } catch (final IOException ex) {
            throw new FileIOException(ex);
        }
        Preconditions.checkNotNull(yamlConfig, "Invalid yaml file `%s`", file.getName());
        Preconditions.checkNotNull(yamlConfig.getDatabaseName(), "`databaseName` in file `%s` is required.", file.getName());
        return Collections.singleton(new LocalDataQueryResultRow(generateDistSQL(yamlConfig)));
    }
    
    private String generateDistSQL(final YamlProxyDatabaseConfiguration yamlConfig) {
        StringBuilder result = new StringBuilder();
        appendResourceDistSQL(yamlConfig, result);
        swapToRuleConfigs(yamlConfig).values().forEach(each -> {
            if (each instanceof ShardingRuleConfiguration) {
                appendShardingDistSQL((ShardingRuleConfiguration) each, result);
            } else if (each instanceof ReadwriteSplittingRuleConfiguration) {
                appendReadWriteSplittingDistSQL((ReadwriteSplittingRuleConfiguration) each, result);
            } else if (each instanceof DatabaseDiscoveryRuleConfiguration) {
                appendDatabaseDiscoveryDistSQL((DatabaseDiscoveryRuleConfiguration) each, result);
            } else if (each instanceof EncryptRuleConfiguration) {
                appendEncryptDistSQL((EncryptRuleConfiguration) each, result);
            } else if (each instanceof ShadowRuleConfiguration) {
                appendShadowDistSQL((ShadowRuleConfiguration) each, result);
            } else if (each instanceof MaskRuleConfiguration) {
                appendMaskDistSQL((MaskRuleConfiguration) each, result);
            }
        });
        return result.toString();
    }
    
    private Map<Integer, RuleConfiguration> swapToRuleConfigs(final YamlProxyDatabaseConfiguration yamlConfig) {
        Map<Integer, RuleConfiguration> result = new TreeMap<>(Comparator.reverseOrder());
        yamlConfig.getRules().forEach(each -> {
            if (each instanceof YamlShardingRuleConfiguration) {
                YamlShardingRuleConfigurationSwapper swapper = new YamlShardingRuleConfigurationSwapper();
                result.put(swapper.getOrder(), swapper.swapToObject((YamlShardingRuleConfiguration) each));
            } else if (each instanceof YamlReadwriteSplittingRuleConfiguration) {
                YamlReadwriteSplittingRuleConfigurationSwapper swapper = new YamlReadwriteSplittingRuleConfigurationSwapper();
                result.put(swapper.getOrder(), swapper.swapToObject((YamlReadwriteSplittingRuleConfiguration) each));
            } else if (each instanceof YamlDatabaseDiscoveryRuleConfiguration) {
                YamlDatabaseDiscoveryRuleConfigurationSwapper swapper = new YamlDatabaseDiscoveryRuleConfigurationSwapper();
                result.put(swapper.getOrder(), swapper.swapToObject((YamlDatabaseDiscoveryRuleConfiguration) each));
            } else if (each instanceof YamlEncryptRuleConfiguration) {
                YamlEncryptRuleConfigurationSwapper swapper = new YamlEncryptRuleConfigurationSwapper();
                result.put(swapper.getOrder(), swapper.swapToObject((YamlEncryptRuleConfiguration) each));
            } else if (each instanceof YamlShadowRuleConfiguration) {
                YamlShadowRuleConfigurationSwapper swapper = new YamlShadowRuleConfigurationSwapper();
                result.put(swapper.getOrder(), swapper.swapToObject((YamlShadowRuleConfiguration) each));
            } else if (each instanceof YamlMaskRuleConfiguration) {
                YamlMaskRuleConfigurationSwapper swapper = new YamlMaskRuleConfigurationSwapper();
                result.put(swapper.getOrder(), swapper.swapToObject((YamlMaskRuleConfiguration) each));
            }
        });
        return result;
    }
    
    private void appendResourceDistSQL(final YamlProxyDatabaseConfiguration yamlConfig, final StringBuilder result) {
        appendDatabase(yamlConfig.getDatabaseName(), result);
        appendResources(yamlConfig.getDataSources(), result);
    }
    
    private void appendDatabase(final String databaseName, final StringBuilder result) {
        result.append(String.format(DistSQLScriptConstants.CREATE_DATABASE, databaseName)).append(System.lineSeparator());
        result.append(String.format(DistSQLScriptConstants.USE_DATABASE, databaseName)).append(System.lineSeparator()).append(System.lineSeparator());
    }
    
    private void appendResources(final Map<String, YamlProxyDataSourceConfiguration> dataSources, final StringBuilder result) {
        if (dataSources.isEmpty()) {
            return;
        }
        result.append(DistSQLScriptConstants.REGISTER_STORAGE_UNIT);
        Iterator<Entry<String, YamlProxyDataSourceConfiguration>> iterator = dataSources.entrySet().iterator();
        while (iterator.hasNext()) {
            Entry<String, YamlProxyDataSourceConfiguration> entry = iterator.next();
            DataSourceProperties dataSourceProps = DataSourcePropertiesCreator.create(HikariDataSource.class.getName(), dataSourceConfigSwapper.swap(entry.getValue()));
            appendResource(entry.getKey(), dataSourceProps, result);
            if (iterator.hasNext()) {
                result.append(DistSQLScriptConstants.COMMA);
            }
        }
        result.append(DistSQLScriptConstants.SEMI).append(System.lineSeparator()).append(System.lineSeparator());
    }
    
    private void appendResource(final String resourceName, final DataSourceProperties dataSourceProps, final StringBuilder result) {
        Map<String, Object> connectionProps = dataSourceProps.getConnectionPropertySynonyms().getStandardProperties();
        String url = (String) connectionProps.get(DistSQLScriptConstants.KEY_URL);
        String username = (String) connectionProps.get(DistSQLScriptConstants.KEY_USERNAME);
        String password = (String) connectionProps.get(DistSQLScriptConstants.KEY_PASSWORD);
        String props = getResourceProperties(dataSourceProps.getPoolPropertySynonyms(), dataSourceProps.getCustomDataSourceProperties());
        if (Strings.isNullOrEmpty(password)) {
            result.append(String.format(DistSQLScriptConstants.RESOURCE_DEFINITION_WITHOUT_PASSWORD, resourceName, url, username, props));
        } else {
            result.append(String.format(DistSQLScriptConstants.RESOURCE_DEFINITION, resourceName, url, username, password, props));
        }
    }
    
    private String getResourceProperties(final PoolPropertySynonyms poolPropertySynonyms, final CustomDataSourceProperties customDataSourceProps) {
        StringBuilder result = new StringBuilder();
        appendProperties(poolPropertySynonyms.getStandardProperties(), result);
        if (!customDataSourceProps.getProperties().isEmpty()) {
            result.append(DistSQLScriptConstants.COMMA);
            appendProperties(customDataSourceProps.getProperties(), result);
        }
        return result.toString();
    }
    
    private void appendProperties(final Map<String, Object> props, final StringBuilder result) {
        Iterator<Entry<String, Object>> iterator = props.entrySet().iterator();
        while (iterator.hasNext()) {
            Entry<String, Object> entry = iterator.next();
            if (null == entry.getValue()) {
                continue;
            }
            result.append(String.format(DistSQLScriptConstants.PROPERTY, entry.getKey(), entry.getValue()));
            if (iterator.hasNext()) {
                result.append(DistSQLScriptConstants.COMMA).append(" ");
            }
        }
    }
    
    private void appendShardingDistSQL(final ShardingRuleConfiguration ruleConfig, final StringBuilder result) {
        appendShardingTableRules(ruleConfig, result);
        appendShardingBindingTableRules(ruleConfig, result);
        appendShardingBroadcastTableRules(ruleConfig, result);
        // TODO append defaultStrategy
    }
    
    private void appendShardingTableRules(final ShardingRuleConfiguration ruleConfig, final StringBuilder result) {
        if (ruleConfig.getTables().isEmpty() && ruleConfig.getAutoTables().isEmpty()) {
            return;
        }
        String tableRules = getTableRules(ruleConfig);
        String autoTableRules = getAutoTableRules(ruleConfig);
        result.append(DistSQLScriptConstants.CREATE_SHARDING_TABLE).append(tableRules);
        if (!Strings.isNullOrEmpty(tableRules) && !Strings.isNullOrEmpty(autoTableRules)) {
            result.append(DistSQLScriptConstants.COMMA).append(System.lineSeparator());
        }
        result.append(autoTableRules).append(DistSQLScriptConstants.SEMI).append(System.lineSeparator()).append(System.lineSeparator());
    }
    
    private String getAutoTableRules(final ShardingRuleConfiguration ruleConfig) {
        StringBuilder result = new StringBuilder();
        if (!ruleConfig.getAutoTables().isEmpty()) {
            Iterator<ShardingAutoTableRuleConfiguration> iterator = ruleConfig.getAutoTables().iterator();
            while (iterator.hasNext()) {
                ShardingAutoTableRuleConfiguration autoTableRuleConfig = iterator.next();
                result.append(String.format(DistSQLScriptConstants.SHARDING_AUTO_TABLE, autoTableRuleConfig.getLogicTable(), autoTableRuleConfig.getActualDataSources(),
                        appendAutoTableStrategy(autoTableRuleConfig, ruleConfig)));
                if (iterator.hasNext()) {
                    result.append(DistSQLScriptConstants.COMMA);
                }
            }
        }
        return result.toString();
    }
    
    private String getTableRules(final ShardingRuleConfiguration ruleConfig) {
        StringBuilder result = new StringBuilder();
        if (!ruleConfig.getTables().isEmpty()) {
            Iterator<ShardingTableRuleConfiguration> iterator = ruleConfig.getTables().iterator();
            while (iterator.hasNext()) {
                ShardingTableRuleConfiguration tableRuleConfig = iterator.next();
                result.append(String.format(DistSQLScriptConstants.SHARDING_TABLE, tableRuleConfig.getLogicTable(), tableRuleConfig.getActualDataNodes(),
                        appendTableStrategy(tableRuleConfig, ruleConfig)));
                if (iterator.hasNext()) {
                    result.append(DistSQLScriptConstants.COMMA);
                }
            }
        }
        return result.toString();
    }
    
    private String appendAutoTableStrategy(final ShardingAutoTableRuleConfiguration autoTableRuleConfig, final ShardingRuleConfiguration ruleConfig) {
        StringBuilder result = new StringBuilder();
        StandardShardingStrategyConfiguration strategyConfig = (StandardShardingStrategyConfiguration) autoTableRuleConfig.getShardingStrategy();
        String shardingColumn = !Strings.isNullOrEmpty(strategyConfig.getShardingColumn()) ? strategyConfig.getShardingColumn() : ruleConfig.getDefaultShardingColumn();
        result.append(String.format(DistSQLScriptConstants.AUTO_TABLE_STRATEGY, shardingColumn, getAlgorithmType(ruleConfig.getShardingAlgorithms().get(strategyConfig.getShardingAlgorithmName()))));
        appendKeyGenerateStrategy(ruleConfig.getKeyGenerators(), autoTableRuleConfig.getKeyGenerateStrategy(), result);
        appendAuditStrategy(ruleConfig.getAuditors(), null != autoTableRuleConfig.getAuditStrategy() ? autoTableRuleConfig.getAuditStrategy() : ruleConfig.getDefaultAuditStrategy(), result);
        return result.toString();
    }
    
    private void appendAuditStrategy(final Map<String, AlgorithmConfiguration> auditors, final ShardingAuditStrategyConfiguration auditStrategy, final StringBuilder result) {
        if (null != auditStrategy) {
            result.append(DistSQLScriptConstants.COMMA).append(System.lineSeparator());
            result.append(String.format(DistSQLScriptConstants.AUDIT_STRATEGY, getAlgorithmTypes(auditors, auditStrategy.getAuditorNames()), auditStrategy.isAllowHintDisable()));
        }
    }
    
    private String getAlgorithmTypes(final Map<String, AlgorithmConfiguration> auditors, final Collection<String> auditorNames) {
        StringBuilder result = new StringBuilder();
        if (!auditorNames.isEmpty()) {
            Iterator<String> iterator = auditorNames.iterator();
            while (iterator.hasNext()) {
                result.append(getAlgorithmType(auditors.get(iterator.next())));
                if (iterator.hasNext()) {
                    result.append(DistSQLScriptConstants.COMMA);
                }
            }
        }
        return result.toString();
    }
    
    private String appendTableStrategy(final ShardingTableRuleConfiguration tableRuleConfig, final ShardingRuleConfiguration ruleConfig) {
        StringBuilder result = new StringBuilder();
        appendStrategy(tableRuleConfig.getDatabaseShardingStrategy(), DistSQLScriptConstants.DATABASE_STRATEGY, result, ruleConfig.getShardingAlgorithms());
        appendStrategy(tableRuleConfig.getTableShardingStrategy(), DistSQLScriptConstants.TABLE_STRATEGY, result, ruleConfig.getShardingAlgorithms());
        appendKeyGenerateStrategy(ruleConfig.getKeyGenerators(), tableRuleConfig.getKeyGenerateStrategy(), result);
        appendAuditStrategy(ruleConfig.getAuditors(), null != tableRuleConfig.getAuditStrategy() ? tableRuleConfig.getAuditStrategy() : ruleConfig.getDefaultAuditStrategy(), result);
        return result.toString();
    }
    
    private void appendKeyGenerateStrategy(final Map<String, AlgorithmConfiguration> keyGenerators, final KeyGenerateStrategyConfiguration keyGenerateStrategyConfig, final StringBuilder result) {
        if (null == keyGenerateStrategyConfig) {
            return;
        }
        result.append(DistSQLScriptConstants.COMMA).append(System.lineSeparator());
        String algorithmDefinition = getAlgorithmType(keyGenerators.get(keyGenerateStrategyConfig.getKeyGeneratorName()));
        result.append(String.format(DistSQLScriptConstants.KEY_GENERATOR_STRATEGY, keyGenerateStrategyConfig.getColumn(), algorithmDefinition));
    }
    
    private void appendStrategy(final ShardingStrategyConfiguration strategyConfig, final String strategyType,
                                final StringBuilder result, final Map<String, AlgorithmConfiguration> shardingAlgorithms) {
        if (null == strategyConfig) {
            return;
        }
        result.append(DistSQLScriptConstants.COMMA).append(System.lineSeparator());
        String type = strategyConfig.getType().toLowerCase();
        String algorithmDefinition = getAlgorithmType(shardingAlgorithms.get(strategyConfig.getShardingAlgorithmName()));
        switch (type) {
            case DistSQLScriptConstants.STANDARD:
                StandardShardingStrategyConfiguration standardShardingStrategyConfig = (StandardShardingStrategyConfiguration) strategyConfig;
                result.append(String.format(DistSQLScriptConstants.SHARDING_STRATEGY_STANDARD, strategyType, type, standardShardingStrategyConfig.getShardingColumn(), algorithmDefinition));
                break;
            case DistSQLScriptConstants.COMPLEX:
                ComplexShardingStrategyConfiguration complexShardingStrategyConfig = (ComplexShardingStrategyConfiguration) strategyConfig;
                result.append(String.format(DistSQLScriptConstants.SHARDING_STRATEGY_COMPLEX, strategyType, type, complexShardingStrategyConfig.getShardingColumns(), algorithmDefinition));
                break;
            case DistSQLScriptConstants.HINT:
                result.append(String.format(DistSQLScriptConstants.SHARDING_STRATEGY_HINT, type, algorithmDefinition));
                break;
            default:
                break;
        }
    }
    
    private void appendShardingBindingTableRules(final ShardingRuleConfiguration ruleConfig, final StringBuilder result) {
        if (ruleConfig.getBindingTableGroups().isEmpty()) {
            return;
        }
        result.append(DistSQLScriptConstants.SHARDING_BINDING_TABLE_RULES);
        Iterator<ShardingTableReferenceRuleConfiguration> iterator = ruleConfig.getBindingTableGroups().iterator();
        while (iterator.hasNext()) {
            ShardingTableReferenceRuleConfiguration referenceRuleConfig = iterator.next();
            result.append(String.format(DistSQLScriptConstants.BINDING_TABLES, referenceRuleConfig.getName(), referenceRuleConfig.getReference()));
            if (iterator.hasNext()) {
                result.append(DistSQLScriptConstants.COMMA);
            }
        }
        result.append(DistSQLScriptConstants.SEMI).append(System.lineSeparator()).append(System.lineSeparator());
    }
    
    private void appendShardingBroadcastTableRules(final ShardingRuleConfiguration ruleConfig, final StringBuilder result) {
        if (ruleConfig.getBroadcastTables().isEmpty()) {
            return;
        }
        result.append(String.format(DistSQLScriptConstants.BROADCAST_TABLE_RULE, String.join(",", ruleConfig.getBroadcastTables())));
        result.append(DistSQLScriptConstants.SEMI).append(System.lineSeparator()).append(System.lineSeparator());
    }
    
    private void appendReadWriteSplittingDistSQL(final ReadwriteSplittingRuleConfiguration ruleConfig, final StringBuilder result) {
        if (ruleConfig.getDataSources().isEmpty()) {
            return;
        }
        result.append(DistSQLScriptConstants.CREATE_READWRITE_SPLITTING_RULE);
        Iterator<ReadwriteSplittingDataSourceRuleConfiguration> iterator = ruleConfig.getDataSources().iterator();
        while (iterator.hasNext()) {
            ReadwriteSplittingDataSourceRuleConfiguration dataSourceRuleConfig = iterator.next();
            appendStaticReadWriteSplittingRule(dataSourceRuleConfig.getStaticStrategy(), ruleConfig.getLoadBalancers(), dataSourceRuleConfig, result);
            appendDynamicReadWriteSplittingRule(dataSourceRuleConfig.getDynamicStrategy(), ruleConfig.getLoadBalancers(), dataSourceRuleConfig, result);
            if (iterator.hasNext()) {
                result.append(DistSQLScriptConstants.COMMA);
            }
        }
        result.append(DistSQLScriptConstants.SEMI).append(System.lineSeparator()).append(System.lineSeparator());
    }
    
    private void appendDynamicReadWriteSplittingRule(final DynamicReadwriteSplittingStrategyConfiguration dynamicConfig, final Map<String, AlgorithmConfiguration> loadBalancers,
                                                     final ReadwriteSplittingDataSourceRuleConfiguration dataSourceRuleConfig, final StringBuilder result) {
        if (null == dynamicConfig) {
            return;
        }
        String loadBalancerType = getLoadBalancerType(loadBalancers.get(dataSourceRuleConfig.getLoadBalancerName()));
        boolean allowWriteDataSourceQuery = Strings.isNullOrEmpty(dynamicConfig.getWriteDataSourceQueryEnabled()) ? Boolean.TRUE : Boolean.parseBoolean(dynamicConfig.getWriteDataSourceQueryEnabled());
        result.append(String.format(DistSQLScriptConstants.READWRITE_SPLITTING_FOR_DYNAMIC,
                dataSourceRuleConfig.getName(), dynamicConfig.getAutoAwareDataSourceName(), allowWriteDataSourceQuery, loadBalancerType));
    }
    
    private void appendStaticReadWriteSplittingRule(final StaticReadwriteSplittingStrategyConfiguration staticConfig, final Map<String, AlgorithmConfiguration> loadBalancers,
                                                    final ReadwriteSplittingDataSourceRuleConfiguration dataSourceRuleConfig, final StringBuilder result) {
        if (null == staticConfig) {
            return;
        }
        String readDataSourceNames = getReadDataSourceNames(staticConfig.getReadDataSourceNames());
        String loadBalancerType = getLoadBalancerType(loadBalancers.get(dataSourceRuleConfig.getLoadBalancerName()));
        result.append(String.format(DistSQLScriptConstants.READWRITE_SPLITTING_FOR_STATIC,
                dataSourceRuleConfig.getName(), staticConfig.getWriteDataSourceName(), readDataSourceNames, loadBalancerType));
    }
    
    private String getLoadBalancerType(final AlgorithmConfiguration algorithmConfig) {
        StringBuilder result = new StringBuilder();
        String loadBalancerType = getAlgorithmType(algorithmConfig);
        if (!Strings.isNullOrEmpty(loadBalancerType)) {
            result.append(DistSQLScriptConstants.COMMA).append(System.lineSeparator()).append(loadBalancerType);
        }
        return result.toString();
    }
    
    private String getReadDataSourceNames(final Collection<String> readDataSourceNames) {
        StringBuilder result = new StringBuilder();
        Iterator<String> iterator = readDataSourceNames.iterator();
        while (iterator.hasNext()) {
            result.append(String.format(DistSQLScriptConstants.READ_RESOURCE, iterator.next()));
            if (iterator.hasNext()) {
                result.append(DistSQLScriptConstants.COMMA);
            }
        }
        return result.toString();
    }
    
    private void appendDatabaseDiscoveryDistSQL(final DatabaseDiscoveryRuleConfiguration ruleConfig, final StringBuilder result) {
        if (ruleConfig.getDataSources().isEmpty()) {
            return;
        }
        result.append(DistSQLScriptConstants.CREATE_DB_DISCOVERY);
        Iterator<DatabaseDiscoveryDataSourceRuleConfiguration> iterator = ruleConfig.getDataSources().iterator();
        while (iterator.hasNext()) {
            DatabaseDiscoveryDataSourceRuleConfiguration dataSourceRuleConfig = iterator.next();
            String groupName = dataSourceRuleConfig.getGroupName();
            String dataSourceNames = String.join(",", dataSourceRuleConfig.getDataSourceNames());
            String databaseType = getAlgorithmType(ruleConfig.getDiscoveryTypes().get(dataSourceRuleConfig.getDiscoveryTypeName()));
            String databaseHeartbeatProperty = getDatabaseDiscoveryHeartbeat(ruleConfig.getDiscoveryHeartbeats().get(dataSourceRuleConfig.getDiscoveryHeartbeatName()));
            result.append(String.format(DistSQLScriptConstants.DB_DISCOVERY, groupName, dataSourceNames, databaseType, databaseHeartbeatProperty));
            if (iterator.hasNext()) {
                result.append(DistSQLScriptConstants.COMMA);
            }
        }
        result.append(DistSQLScriptConstants.SEMI).append(System.lineSeparator()).append(System.lineSeparator());
    }
    
    private String getDatabaseDiscoveryHeartbeat(final DatabaseDiscoveryHeartBeatConfiguration heartBeatConfig) {
        StringBuilder result = new StringBuilder();
        if (null != heartBeatConfig) {
            result.append(getAlgorithmProperties(heartBeatConfig.getProps()));
        }
        return result.toString();
    }
    
    private void appendEncryptDistSQL(final EncryptRuleConfiguration ruleConfig, final StringBuilder result) {
        if (ruleConfig.getTables().isEmpty()) {
            return;
        }
        result.append(DistSQLScriptConstants.CREATE_ENCRYPT);
        Iterator<EncryptTableRuleConfiguration> iterator = ruleConfig.getTables().iterator();
        while (iterator.hasNext()) {
            EncryptTableRuleConfiguration tableRuleConfig = iterator.next();
            boolean queryWithCipher = null != tableRuleConfig.getQueryWithCipherColumn() ? tableRuleConfig.getQueryWithCipherColumn() : true;
            result.append(String.format(DistSQLScriptConstants.ENCRYPT, tableRuleConfig.getName(), getEncryptColumns(tableRuleConfig.getColumns(), ruleConfig.getEncryptors()), queryWithCipher));
            if (iterator.hasNext()) {
                result.append(DistSQLScriptConstants.COMMA).append(System.lineSeparator());
            }
        }
        result.append(DistSQLScriptConstants.SEMI).append(System.lineSeparator()).append(System.lineSeparator());
    }
    
    private String getEncryptColumns(final Collection<EncryptColumnRuleConfiguration> ruleConfigs, final Map<String, AlgorithmConfiguration> encryptors) {
        StringBuilder result = new StringBuilder();
        Iterator<EncryptColumnRuleConfiguration> iterator = ruleConfigs.iterator();
        while (iterator.hasNext()) {
            EncryptColumnRuleConfiguration columnRuleConfig = iterator.next();
            result.append(String.format(DistSQLScriptConstants.ENCRYPT_COLUMN, columnRuleConfig.getLogicColumn(), getColumns(columnRuleConfig), getEncryptAlgorithms(columnRuleConfig, encryptors)));
            if (iterator.hasNext()) {
                result.append(DistSQLScriptConstants.COMMA).append(System.lineSeparator());
            }
        }
        return result.toString();
    }
    
    private String getColumns(final EncryptColumnRuleConfiguration ruleConfig) {
        StringBuilder result = new StringBuilder();
        String plainColumnName = ruleConfig.getPlainColumn();
        String cipherColumnName = ruleConfig.getCipherColumn();
        if (null != plainColumnName) {
            result.append(String.format(DistSQLScriptConstants.PLAIN, plainColumnName));
        }
        if (null != cipherColumnName) {
            if (null != plainColumnName) {
                result.append(DistSQLScriptConstants.COMMA).append(" ");
            }
            result.append(String.format(DistSQLScriptConstants.CIPHER, cipherColumnName));
        }
        if (null != ruleConfig.getAssistedQueryColumn()) {
            result.append(DistSQLScriptConstants.COMMA).append(" ").append(String.format(DistSQLScriptConstants.ASSISTED_QUERY_COLUMN, ruleConfig.getAssistedQueryColumn()));
        }
        if (null != ruleConfig.getLikeQueryColumn()) {
            result.append(DistSQLScriptConstants.COMMA).append(" ").append(String.format(DistSQLScriptConstants.LIKE_QUERY_COLUMN, ruleConfig.getLikeQueryColumn()));
        }
        return result.toString();
    }
    
    private String getEncryptAlgorithms(final EncryptColumnRuleConfiguration ruleConfig, final Map<String, AlgorithmConfiguration> encryptors) {
        StringBuilder result = new StringBuilder();
        String cipherEncryptorName = ruleConfig.getEncryptorName();
        String assistedQueryEncryptorName = ruleConfig.getAssistedQueryEncryptorName();
        String likeQueryEncryptorName = ruleConfig.getLikeQueryEncryptorName();
        if (null != cipherEncryptorName) {
            result.append(String.format(DistSQLScriptConstants.ENCRYPT_ALGORITHM, getAlgorithmType(encryptors.get(cipherEncryptorName))));
        }
        if (null != assistedQueryEncryptorName) {
            result.append(DistSQLScriptConstants.COMMA).append(" ")
                    .append(String.format(DistSQLScriptConstants.ASSISTED_QUERY_ALGORITHM, getAlgorithmType(encryptors.get(assistedQueryEncryptorName))));
        }
        if (null != likeQueryEncryptorName) {
            result.append(DistSQLScriptConstants.COMMA).append(" ")
                    .append(String.format(DistSQLScriptConstants.LIKE_QUERY_ALGORITHM, getAlgorithmType(encryptors.get(likeQueryEncryptorName))));
        }
        return result.toString();
    }
    
    private void appendShadowDistSQL(final ShadowRuleConfiguration ruleConfig, final StringBuilder result) {
        if (ruleConfig.getDataSources().isEmpty()) {
            return;
        }
        result.append(DistSQLScriptConstants.CREATE_SHADOW);
        Iterator<ShadowDataSourceConfiguration> iterator = ruleConfig.getDataSources().iterator();
        while (iterator.hasNext()) {
            ShadowDataSourceConfiguration dataSourceConfig = iterator.next();
            String shadowRuleName = dataSourceConfig.getName();
            String shadowTables = getShadowTables(shadowRuleName, ruleConfig.getTables(), ruleConfig.getShadowAlgorithms());
            result.append(String.format(DistSQLScriptConstants.SHADOW, shadowRuleName, dataSourceConfig.getProductionDataSourceName(), dataSourceConfig.getShadowDataSourceName(), shadowTables));
            if (iterator.hasNext()) {
                result.append(DistSQLScriptConstants.COMMA);
            }
        }
        result.append(DistSQLScriptConstants.SEMI).append(System.lineSeparator()).append(System.lineSeparator());
    }
    
    private String getShadowTables(final String shadowRuleName, final Map<String, ShadowTableConfiguration> ruleConfig, final Map<String, AlgorithmConfiguration> algorithmConfigs) {
        StringBuilder result = new StringBuilder();
        Iterator<Entry<String, ShadowTableConfiguration>> iterator = ruleConfig.entrySet().iterator();
        while (iterator.hasNext()) {
            Entry<String, ShadowTableConfiguration> shadowTableConfig = iterator.next();
            if (shadowTableConfig.getValue().getDataSourceNames().contains(shadowRuleName)) {
                String shadowTableTypes = getShadowTableTypes(shadowTableConfig.getValue().getShadowAlgorithmNames(), algorithmConfigs);
                result.append(String.format(DistSQLScriptConstants.SHADOW_TABLE, shadowTableConfig.getKey(), shadowTableTypes));
            }
            if (iterator.hasNext()) {
                result.append(DistSQLScriptConstants.COMMA).append(System.lineSeparator());
            }
        }
        return result.toString();
    }
    
    private String getShadowTableTypes(final Collection<String> shadowAlgorithmNames, final Map<String, AlgorithmConfiguration> algorithmConfigs) {
        StringBuilder result = new StringBuilder();
        Iterator<String> iterator = shadowAlgorithmNames.iterator();
        while (iterator.hasNext()) {
            result.append(getAlgorithmType(algorithmConfigs.get(iterator.next())));
            if (iterator.hasNext()) {
                result.append(DistSQLScriptConstants.COMMA).append(" ");
            }
        }
        return result.toString();
    }
    
    private void appendMaskDistSQL(final MaskRuleConfiguration ruleConfig, final StringBuilder result) {
        if (ruleConfig.getTables().isEmpty()) {
            return;
        }
        result.append(DistSQLScriptConstants.CREATE_MASK);
        Iterator<MaskTableRuleConfiguration> iterator = ruleConfig.getTables().iterator();
        while (iterator.hasNext()) {
            MaskTableRuleConfiguration tableRuleConfig = iterator.next();
            result.append(String.format(DistSQLScriptConstants.MASK, tableRuleConfig.getName(), getMaskColumns(tableRuleConfig.getColumns(), ruleConfig.getMaskAlgorithms())));
            if (iterator.hasNext()) {
                result.append(DistSQLScriptConstants.COMMA).append(System.lineSeparator());
            }
        }
        result.append(DistSQLScriptConstants.SEMI).append(System.lineSeparator()).append(System.lineSeparator());
    }
    
    private String getMaskColumns(final Collection<MaskColumnRuleConfiguration> columnRuleConfig, final Map<String, AlgorithmConfiguration> maskAlgorithms) {
        StringBuilder result = new StringBuilder();
        Iterator<MaskColumnRuleConfiguration> iterator = columnRuleConfig.iterator();
        if (iterator.hasNext()) {
            MaskColumnRuleConfiguration column = iterator.next();
            String columnName = column.getLogicColumn();
            result.append(String.format(DistSQLScriptConstants.MASK_COLUMN, columnName, getMaskAlgorithms(column, maskAlgorithms)));
        }
        return result.toString();
    }
    
    private String getMaskAlgorithms(final MaskColumnRuleConfiguration columnRuleConfig, final Map<String, AlgorithmConfiguration> maskAlgorithms) {
        String algorithmName = columnRuleConfig.getMaskAlgorithm();
        return getAlgorithmType(maskAlgorithms.get(algorithmName));
    }
    
    private String getAlgorithmType(final AlgorithmConfiguration algorithmConfig) {
        StringBuilder result = new StringBuilder();
        if (null == algorithmConfig) {
            return result.toString();
        }
        String type = algorithmConfig.getType().toLowerCase();
        if (algorithmConfig.getProps().isEmpty()) {
            result.append(String.format(DistSQLScriptConstants.ALGORITHM_TYPE_WITHOUT_PROPS, type));
        } else {
            result.append(String.format(DistSQLScriptConstants.ALGORITHM_TYPE, type, getAlgorithmProperties(algorithmConfig.getProps())));
        }
        return result.toString();
    }
    
    private String getAlgorithmProperties(final Properties props) {
        StringBuilder result = new StringBuilder();
        Iterator<String> iterator = new TreeMap(props).keySet().iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            Object value = props.get(key);
            if (null == value) {
                continue;
            }
            result.append(String.format(DistSQLScriptConstants.PROPERTY, key, value));
            if (iterator.hasNext()) {
                result.append(DistSQLScriptConstants.COMMA).append(" ");
            }
        }
        return result.toString();
    }
}
