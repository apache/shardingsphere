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
import com.zaxxer.hikari.HikariDataSource;
import org.apache.commons.lang3.StringUtils;
import org.apache.shardingsphere.distsql.parser.statement.ral.queryable.ConvertYamlConfigurationStatement;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.datasource.props.DataSourceProperties;
import org.apache.shardingsphere.infra.datasource.props.DataSourcePropertiesCreator;
import org.apache.shardingsphere.infra.datasource.props.custom.CustomDataSourceProperties;
import org.apache.shardingsphere.infra.datasource.props.synonym.PoolPropertySynonyms;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.yaml.config.pojo.rule.YamlRuleConfiguration;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.config.yaml.YamlProxyDataSourceConfiguration;
import org.apache.shardingsphere.proxy.backend.config.yaml.YamlProxyDatabaseConfiguration;
import org.apache.shardingsphere.proxy.backend.config.yaml.swapper.YamlProxyDataSourceConfigurationSwapper;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.QueryableRALBackendHandler;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.common.constant.DistSQLScriptConstants;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.keygen.KeyGenerateStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ComplexShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.YamlShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.yaml.swapper.ShardingRuleConfigurationYamlSwapper;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

/**
 * Convert yaml configuration handler.
 */
public class ConvertYamlConfigurationHandler extends QueryableRALBackendHandler<ConvertYamlConfigurationStatement> {
    
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
            throw new ShardingSphereException(ex);
        }
        Preconditions.checkNotNull(yamlConfig, "Invalid yaml file `%s`", file.getName());
        Preconditions.checkNotNull(yamlConfig.getDatabaseName(), "`databaseName` in file `%s` is required.", file.getName());
        return Collections.singleton(new LocalDataQueryResultRow(generateDistSQL(yamlConfig)));
    }
    
    private String generateDistSQL(final YamlProxyDatabaseConfiguration yamlConfig) {
        StringBuilder result = new StringBuilder();
        appendDatabase(yamlConfig.getDatabaseName(), result);
        appendResources(yamlConfig.getDataSources(), result);
        appendRules(yamlConfig.getRules(), result);
        // TODO append rules by feature SPI
        return result.toString();
    }
    
    private void appendDatabase(final String databaseName, final StringBuilder stringBuilder) {
        stringBuilder.append(String.format(DistSQLScriptConstants.CREATE_DATABASE, databaseName)).append(System.lineSeparator());
        stringBuilder.append(String.format(DistSQLScriptConstants.USE_DATABASE, databaseName)).append(System.lineSeparator());
    }
    
    private void appendResources(final Map<String, YamlProxyDataSourceConfiguration> dataSources, final StringBuilder stringBuilder) {
        if (dataSources.isEmpty()) {
            return;
        }
        stringBuilder.append(DistSQLScriptConstants.ADD_RESOURCE);
        Iterator<Entry<String, YamlProxyDataSourceConfiguration>> iterator = dataSources.entrySet().iterator();
        while (iterator.hasNext()) {
            Entry<String, YamlProxyDataSourceConfiguration> entry = iterator.next();
            DataSourceProperties dataSourceProperties = DataSourcePropertiesCreator.create(HikariDataSource.class.getName(), dataSourceConfigSwapper.swap(entry.getValue()));
            appendResource(entry.getKey(), dataSourceProperties, stringBuilder);
            if (iterator.hasNext()) {
                stringBuilder.append(DistSQLScriptConstants.COMMA).append(System.lineSeparator());
            }
        }
        stringBuilder.append(DistSQLScriptConstants.SEMI).append(System.lineSeparator());
    }
    
    private void appendResource(final String resourceName, final DataSourceProperties dataSourceProperties, final StringBuilder stringBuilder) {
        Map<String, Object> connectionProperties = dataSourceProperties.getConnectionPropertySynonyms().getStandardProperties();
        String url = (String) connectionProperties.get(DistSQLScriptConstants.KEY_URL);
        String username = (String) connectionProperties.get(DistSQLScriptConstants.KEY_USERNAME);
        String password = (String) connectionProperties.get(DistSQLScriptConstants.KEY_PASSWORD);
        String props = getResourceProperties(dataSourceProperties.getPoolPropertySynonyms(), dataSourceProperties.getCustomDataSourceProperties());
        if (StringUtils.isNotEmpty(password)) {
            stringBuilder.append(String.format(DistSQLScriptConstants.RESOURCE_DEFINITION, resourceName, url, username, password, props));
        } else {
            stringBuilder.append(String.format(DistSQLScriptConstants.RESOURCE_DEFINITION_WITHOUT_PASSWORD, resourceName, url, username, props));
        }
    }
    
    private String getResourceProperties(final PoolPropertySynonyms poolPropertySynonyms, final CustomDataSourceProperties customDataSourceProperties) {
        StringBuilder result = new StringBuilder();
        appendProperties(poolPropertySynonyms.getStandardProperties(), result);
        if (!customDataSourceProperties.getProperties().isEmpty()) {
            result.append(DistSQLScriptConstants.COMMA);
            appendProperties(customDataSourceProperties.getProperties(), result);
        }
        return result.toString();
    }
    
    private void appendProperties(final Map<String, Object> properties, final StringBuilder stringBuilder) {
        Iterator<Entry<String, Object>> iterator = properties.entrySet().iterator();
        while (iterator.hasNext()) {
            Entry<String, Object> entry = iterator.next();
            if (null == entry.getValue()) {
                continue;
            }
            stringBuilder.append(String.format(DistSQLScriptConstants.PROPERTY, entry.getKey(), entry.getValue()));
            if (iterator.hasNext()) {
                stringBuilder.append(DistSQLScriptConstants.COMMA);
            }
        }
    }
    
    private void appendRules(final Collection<YamlRuleConfiguration> rules, final StringBuilder stringBuilder) {
        if (rules.isEmpty()) {
            return;
        }
        for (YamlRuleConfiguration rule : rules) {
            ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfigurationYamlSwapper().swapToObject((YamlShardingRuleConfiguration) rule);
            appendShardingAlgorithms(shardingRuleConfig, stringBuilder);
            appendKeyGenerators(shardingRuleConfig, stringBuilder);
            appendShardingTableRules(shardingRuleConfig, stringBuilder);
            // TODO append autoTables
            appendShardingBindingTableRules(shardingRuleConfig, stringBuilder);
        }
    }
    
    private void appendShardingAlgorithms(final ShardingRuleConfiguration shardingRuleConfig, final StringBuilder stringBuilder) {
        stringBuilder.append(DistSQLScriptConstants.CREATE_SHARDING_ALGORITHM);
        Iterator<Entry<String, AlgorithmConfiguration>> iterator = shardingRuleConfig.getShardingAlgorithms().entrySet().iterator();
        while (iterator.hasNext()) {
            Entry<String, AlgorithmConfiguration> entry = iterator.next();
            String shardingAlgorithmName = entry.getKey();
            String algorithmType = entry.getValue().getType().toLowerCase();
            String property = appendShardingAlgorithmProperties(entry.getValue().getProps());
            stringBuilder.append(String.format(DistSQLScriptConstants.SHARDING_ALGORITHM, shardingAlgorithmName, algorithmType, property));
            if (iterator.hasNext()) {
                stringBuilder.append(DistSQLScriptConstants.COMMA);
            }
        }
        stringBuilder.append(DistSQLScriptConstants.SEMI).append(System.lineSeparator());
    }
    
    private String appendShardingAlgorithmProperties(final Properties property) {
        StringBuilder result = new StringBuilder();
        Iterator<Entry<Object, Object>> iterator = property.entrySet().iterator();
        while (iterator.hasNext()) {
            Entry<Object, Object> entry = iterator.next();
            result.append(String.format(DistSQLScriptConstants.PROPERTY, entry.getKey(), entry.getValue()));
            if (iterator.hasNext()) {
                result.append(DistSQLScriptConstants.COMMA);
            }
        }
        return result.toString();
    }
    
    private void appendKeyGenerators(final ShardingRuleConfiguration shardingRuleConfig, final StringBuilder stringBuilder) {
        stringBuilder.append(DistSQLScriptConstants.CREATE_KEY_GENERATOR);
        Iterator<Entry<String, AlgorithmConfiguration>> iterator = shardingRuleConfig.getKeyGenerators().entrySet().iterator();
        while (iterator.hasNext()) {
            Entry<String, AlgorithmConfiguration> entry = iterator.next();
            String generatorName = entry.getKey();
            String type = entry.getValue().getType();
            stringBuilder.append(String.format(DistSQLScriptConstants.KEY_GENERATOR, generatorName, type));
            if (iterator.hasNext()) {
                stringBuilder.append(DistSQLScriptConstants.COMMA);
            }
        }
        stringBuilder.append(DistSQLScriptConstants.SEMI).append(System.lineSeparator());
    }
    
    private void appendShardingTableRules(final ShardingRuleConfiguration shardingRuleConfig, final StringBuilder stringBuilder) {
        stringBuilder.append(DistSQLScriptConstants.CREATE_SHARDING_TABLE);
        Iterator<ShardingTableRuleConfiguration> iterator = shardingRuleConfig.getTables().iterator();
        while (iterator.hasNext()) {
            ShardingTableRuleConfiguration entry = iterator.next();
            String tableName = entry.getLogicTable();
            String dataNodes = entry.getActualDataNodes();
            String strategy = appendTableStrategy(entry);
            stringBuilder.append(String.format(DistSQLScriptConstants.SHARDING_TABLE, tableName, dataNodes, strategy));
            if (iterator.hasNext()) {
                stringBuilder.append(DistSQLScriptConstants.COMMA);
            }
        }
        stringBuilder.append(DistSQLScriptConstants.SEMI).append(System.lineSeparator());
    }
    
    private String appendTableStrategy(final ShardingTableRuleConfiguration shardingTableRuleConfig) {
        StringBuilder result = new StringBuilder();
        if (null != shardingTableRuleConfig.getDatabaseShardingStrategy()) {
            getStrategy(shardingTableRuleConfig.getDatabaseShardingStrategy(), DistSQLScriptConstants.DATABASE_STRATEGY, result);
        }
        if (null != shardingTableRuleConfig.getTableShardingStrategy()) {
            getStrategy(shardingTableRuleConfig.getTableShardingStrategy(), DistSQLScriptConstants.TABLE_STRATEGY, result);
        }
        if (null != shardingTableRuleConfig.getKeyGenerateStrategy()) {
            KeyGenerateStrategyConfiguration keyGenerateStrategyConfig = shardingTableRuleConfig.getKeyGenerateStrategy();
            String column = keyGenerateStrategyConfig.getColumn();
            String keyGenerator = keyGenerateStrategyConfig.getKeyGeneratorName();
            result.append(String.format(DistSQLScriptConstants.KEY_GENERATOR_STRATEGY, column, keyGenerator));
        }
        return result.substring(0, result.length() - 2);
    }
    
    private StringBuilder getStrategy(final ShardingStrategyConfiguration shardingStrategyConfiguration, final String strategyType, final StringBuilder result) {
        String type = shardingStrategyConfiguration.getType().toLowerCase();
        String shardingAlgorithmName = shardingStrategyConfiguration.getShardingAlgorithmName();
        switch (type) {
            case "standard":
                StandardShardingStrategyConfiguration standardShardingStrategyConfig = (StandardShardingStrategyConfiguration) shardingStrategyConfiguration;
                String shardingColumn = standardShardingStrategyConfig.getShardingColumn();
                result.append(String.format(DistSQLScriptConstants.SHARDING_STRATEGY_STANDARD, strategyType, type, shardingColumn, shardingAlgorithmName));
                break;
            case "complex":
                ComplexShardingStrategyConfiguration complexShardingStrategyConfig = (ComplexShardingStrategyConfiguration) shardingStrategyConfiguration;
                String shardingColumns = complexShardingStrategyConfig.getShardingColumns();
                result.append(String.format(DistSQLScriptConstants.SHARDING_STRATEGY_COMPLEX, strategyType, type, shardingColumns, shardingAlgorithmName));
                break;
            case "hint":
                result.append(String.format(DistSQLScriptConstants.SHARDING_STRATEGY_HINT, type, shardingAlgorithmName));
                break;
            default:
                break;
        }
        return result;
    }
    
    private void appendShardingBindingTableRules(final ShardingRuleConfiguration shardingRuleConfig, final StringBuilder stringBuilder) {
        String bindings = getBinding(shardingRuleConfig.getBindingTableGroups().iterator());
        stringBuilder.append(String.format(DistSQLScriptConstants.SHARDING_BINDING_TABLE_RULES, bindings));
    }
    
    private String getBinding(final Iterator<String> iterator) {
        StringBuilder result = new StringBuilder();
        while (iterator.hasNext()) {
            String binding = iterator.next();
            result.append(String.format(DistSQLScriptConstants.BINDING, binding));
            if (iterator.hasNext()) {
                result.append(DistSQLScriptConstants.COMMA);
            }
        }
        result.append(DistSQLScriptConstants.SEMI).append(System.lineSeparator());
        return result.toString();
    }
}
