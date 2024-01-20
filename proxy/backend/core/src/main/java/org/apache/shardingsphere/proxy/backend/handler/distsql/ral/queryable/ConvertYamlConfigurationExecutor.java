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
import org.apache.shardingsphere.distsql.handler.type.ral.constant.DistSQLScriptConstants;
import org.apache.shardingsphere.distsql.handler.type.ral.query.ConvertRuleConfigurationProvider;
import org.apache.shardingsphere.distsql.handler.type.ral.query.QueryableRALExecutor;
import org.apache.shardingsphere.distsql.statement.ral.queryable.ConvertYamlConfigurationStatement;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.datasource.pool.config.DataSourceConfiguration;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.datasource.pool.props.creator.DataSourcePoolPropertiesCreator;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.custom.CustomDataSourcePoolProperties;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.synonym.PoolPropertySynonyms;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.spi.type.ordered.OrderedSPILoader;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.yaml.config.pojo.rule.YamlRuleConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.YamlRuleConfigurationSwapper;
import org.apache.shardingsphere.proxy.backend.config.yaml.YamlProxyDataSourceConfiguration;
import org.apache.shardingsphere.proxy.backend.config.yaml.YamlProxyDatabaseConfiguration;
import org.apache.shardingsphere.proxy.backend.config.yaml.swapper.YamlProxyDataSourceConfigurationSwapper;
import org.apache.shardingsphere.proxy.backend.exception.FileIOException;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 * Convert YAML configuration executor.
 */
public final class ConvertYamlConfigurationExecutor implements QueryableRALExecutor<ConvertYamlConfigurationStatement> {
    
    private final YamlProxyDataSourceConfigurationSwapper dataSourceConfigSwapper = new YamlProxyDataSourceConfigurationSwapper();
    
    @Override
    public Collection<String> getColumnNames() {
        return Collections.singleton("dist_sql");
    }
    
    @Override
    public Collection<LocalDataQueryResultRow> getRows(final ConvertYamlConfigurationStatement sqlStatement, final ShardingSphereMetaData metaData) {
        File file = new File(sqlStatement.getFilePath());
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
        for (RuleConfiguration each : swapToRuleConfigs(yamlConfig).values()) {
            Class<? extends RuleConfiguration> type = each.getClass();
            ConvertRuleConfigurationProvider convertRuleConfigProvider = TypedSPILoader.getService(ConvertRuleConfigurationProvider.class, type);
            result.append(convertRuleConfigProvider.convert(each));
        }
        return result.toString();
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    private Map<Integer, RuleConfiguration> swapToRuleConfigs(final YamlProxyDatabaseConfiguration yamlConfig) {
        Map<Integer, RuleConfiguration> result = new TreeMap<>(Comparator.reverseOrder());
        for (YamlRuleConfiguration each : yamlConfig.getRules()) {
            YamlRuleConfigurationSwapper swapper = OrderedSPILoader.getServicesByClass(YamlRuleConfigurationSwapper.class, Collections.singleton(each.getRuleConfigurationType()))
                    .get(each.getRuleConfigurationType());
            result.put(swapper.getOrder(), (RuleConfiguration) swapper.swapToObject(each));
        }
        return result;
    }
    
    private void appendResourceDistSQL(final YamlProxyDatabaseConfiguration yamlConfig, final StringBuilder stringBuilder) {
        appendDatabase(yamlConfig.getDatabaseName(), stringBuilder);
        appendResources(yamlConfig.getDataSources(), stringBuilder);
    }
    
    private void appendDatabase(final String databaseName, final StringBuilder stringBuilder) {
        stringBuilder.append(String.format(DistSQLScriptConstants.CREATE_DATABASE, databaseName)).append(System.lineSeparator())
                .append(String.format(DistSQLScriptConstants.USE_DATABASE, databaseName)).append(System.lineSeparator()).append(System.lineSeparator());
    }
    
    private void appendResources(final Map<String, YamlProxyDataSourceConfiguration> dataSources, final StringBuilder stringBuilder) {
        if (dataSources.isEmpty()) {
            return;
        }
        stringBuilder.append(DistSQLScriptConstants.REGISTER_STORAGE_UNIT);
        Iterator<Entry<String, YamlProxyDataSourceConfiguration>> iterator = dataSources.entrySet().iterator();
        while (iterator.hasNext()) {
            Entry<String, YamlProxyDataSourceConfiguration> entry = iterator.next();
            DataSourceConfiguration dataSourceConfig = dataSourceConfigSwapper.swap(entry.getValue());
            DataSourcePoolProperties props = DataSourcePoolPropertiesCreator.create(dataSourceConfig);
            appendResource(entry.getKey(), props, stringBuilder);
            if (iterator.hasNext()) {
                stringBuilder.append(DistSQLScriptConstants.COMMA);
            }
        }
        stringBuilder.append(DistSQLScriptConstants.SEMI).append(System.lineSeparator()).append(System.lineSeparator());
    }
    
    private void appendResource(final String resourceName, final DataSourcePoolProperties dataSourcePoolProps, final StringBuilder stringBuilder) {
        Map<String, Object> connectionProps = dataSourcePoolProps.getConnectionPropertySynonyms().getStandardProperties();
        String url = (String) connectionProps.get(DistSQLScriptConstants.KEY_URL);
        String username = (String) connectionProps.get(DistSQLScriptConstants.KEY_USERNAME);
        String password = (String) connectionProps.get(DistSQLScriptConstants.KEY_PASSWORD);
        String props = getResourceProperties(dataSourcePoolProps.getPoolPropertySynonyms(), dataSourcePoolProps.getCustomProperties());
        if (Strings.isNullOrEmpty(password)) {
            stringBuilder.append(String.format(DistSQLScriptConstants.RESOURCE_DEFINITION_WITHOUT_PASSWORD, resourceName, url, username, props));
        } else {
            stringBuilder.append(String.format(DistSQLScriptConstants.RESOURCE_DEFINITION, resourceName, url, username, password, props));
        }
    }
    
    private String getResourceProperties(final PoolPropertySynonyms poolPropertySynonyms, final CustomDataSourcePoolProperties customDataSourcePoolProps) {
        StringBuilder result = new StringBuilder();
        appendProperties(poolPropertySynonyms.getStandardProperties(), result);
        if (!customDataSourcePoolProps.getProperties().isEmpty()) {
            result.append(DistSQLScriptConstants.COMMA);
            appendProperties(customDataSourcePoolProps.getProperties(), result);
        }
        return result.toString();
    }
    
    private void appendProperties(final Map<String, Object> props, final StringBuilder stringBuilder) {
        Iterator<Entry<String, Object>> iterator = props.entrySet().iterator();
        while (iterator.hasNext()) {
            Entry<String, Object> entry = iterator.next();
            if (null == entry.getValue()) {
                continue;
            }
            stringBuilder.append(String.format(DistSQLScriptConstants.PROPERTY, entry.getKey(), entry.getValue()));
            if (iterator.hasNext()) {
                stringBuilder.append(DistSQLScriptConstants.COMMA).append(' ');
            }
        }
    }
    
    @Override
    public Class<ConvertYamlConfigurationStatement> getType() {
        return ConvertYamlConfigurationStatement.class;
    }
}
