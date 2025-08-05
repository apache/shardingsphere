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

package org.apache.shardingsphere.proxy.backend.handler.distsql.ral.queryable.yaml;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.apache.shardingsphere.distsql.handler.engine.query.DistSQLQueryExecutor;
import org.apache.shardingsphere.distsql.handler.engine.query.ral.convert.DistSQLScriptConstants;
import org.apache.shardingsphere.distsql.handler.engine.query.ral.convert.RuleConfigurationToDistSQLConverter;
import org.apache.shardingsphere.distsql.statement.type.ral.queryable.convert.ConvertYamlConfigurationStatement;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.datasource.pool.config.DataSourceConfiguration;
import org.apache.shardingsphere.infra.datasource.pool.props.creator.DataSourcePoolPropertiesCreator;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.custom.CustomDataSourcePoolProperties;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.synonym.PoolPropertySynonyms;
import org.apache.shardingsphere.infra.exception.generic.FileIOException;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.spi.type.ordered.OrderedSPILoader;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.yaml.config.pojo.rule.YamlRuleConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.YamlRuleConfigurationSwapper;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.config.yaml.YamlProxyDataSourceConfiguration;
import org.apache.shardingsphere.proxy.backend.config.yaml.YamlProxyDatabaseConfiguration;
import org.apache.shardingsphere.proxy.backend.config.yaml.swapper.YamlProxyDataSourceConfigurationSwapper;

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
public final class ConvertYamlConfigurationExecutor implements DistSQLQueryExecutor<ConvertYamlConfigurationStatement> {
    
    private final YamlProxyDataSourceConfigurationSwapper dataSourceConfigSwapper = new YamlProxyDataSourceConfigurationSwapper();
    
    @Override
    public Collection<String> getColumnNames(final ConvertYamlConfigurationStatement statement) {
        return Collections.singleton("dist_sql");
    }
    
    @Override
    public Collection<LocalDataQueryResultRow> getRows(final ConvertYamlConfigurationStatement sqlStatement, final ContextManager contextManager) {
        File file = new File(sqlStatement.getFilePath());
        YamlProxyDatabaseConfiguration yamlConfig;
        try {
            yamlConfig = YamlEngine.unmarshal(file, YamlProxyDatabaseConfiguration.class);
        } catch (final IOException ignore) {
            throw new FileIOException(file);
        }
        Preconditions.checkNotNull(yamlConfig, "Invalid yaml file `%s`", file.getName());
        Preconditions.checkNotNull(yamlConfig.getDatabaseName(), "`databaseName` in file `%s` is required.", file.getName());
        return Collections.singleton(new LocalDataQueryResultRow(convertYamlConfigurationToDistSQL(yamlConfig)));
    }
    
    @SuppressWarnings("unchecked")
    private String convertYamlConfigurationToDistSQL(final YamlProxyDatabaseConfiguration yamlConfig) {
        StringBuilder result = new StringBuilder();
        result.append(convertDatabase(yamlConfig.getDatabaseName()));
        result.append(System.lineSeparator()).append(System.lineSeparator());
        if (null == yamlConfig.getDataSources() || yamlConfig.getDataSources().isEmpty()) {
            return result.toString();
        }
        result.append(convertDataSources(yamlConfig.getDataSources()));
        result.append(System.lineSeparator()).append(System.lineSeparator());
        if (null == yamlConfig.getRules() || yamlConfig.getRules().isEmpty()) {
            return result.toString();
        }
        for (RuleConfiguration each : swapToRuleConfigs(yamlConfig).values()) {
            result.append(TypedSPILoader.getService(RuleConfigurationToDistSQLConverter.class, each.getClass()).convert(each));
            result.append(System.lineSeparator()).append(System.lineSeparator());
        }
        return result.toString();
    }
    
    private String convertDatabase(final String databaseName) {
        return String.format(DistSQLScriptConstants.CREATE_DATABASE, databaseName) + System.lineSeparator() + String.format(DistSQLScriptConstants.USE_DATABASE, databaseName);
    }
    
    private String convertDataSources(final Map<String, YamlProxyDataSourceConfiguration> dataSources) {
        StringBuilder result = new StringBuilder(DistSQLScriptConstants.REGISTER_STORAGE_UNIT);
        Iterator<Entry<String, YamlProxyDataSourceConfiguration>> iterator = dataSources.entrySet().iterator();
        while (iterator.hasNext()) {
            Entry<String, YamlProxyDataSourceConfiguration> entry = iterator.next();
            DataSourceConfiguration dataSourceConfig = dataSourceConfigSwapper.swap(entry.getValue());
            DataSourcePoolProperties props = DataSourcePoolPropertiesCreator.create(dataSourceConfig);
            result.append(convertDataSource(entry.getKey(), props));
            if (iterator.hasNext()) {
                result.append(",");
            }
        }
        result.append(";");
        return result.toString();
    }
    
    private String convertDataSource(final String resourceName, final DataSourcePoolProperties dataSourcePoolProps) {
        Map<String, Object> connectionProps = dataSourcePoolProps.getConnectionPropertySynonyms().getStandardProperties();
        String url = (String) connectionProps.get(DistSQLScriptConstants.KEY_URL);
        String username = (String) connectionProps.get(DistSQLScriptConstants.KEY_USERNAME);
        String password = (String) connectionProps.get(DistSQLScriptConstants.KEY_PASSWORD);
        String props = getDataSourcePoolProps(dataSourcePoolProps.getPoolPropertySynonyms(), dataSourcePoolProps.getCustomProperties());
        if (Strings.isNullOrEmpty(password)) {
            return String.format(DistSQLScriptConstants.STORAGE_UNIT_DEFINITION_WITHOUT_PASSWORD, resourceName, url, username, props);
        }
        return String.format(DistSQLScriptConstants.STORAGE_UNIT_DEFINITION, resourceName, url, username, password, props);
    }
    
    private String getDataSourcePoolProps(final PoolPropertySynonyms poolPropertySynonyms, final CustomDataSourcePoolProperties customDataSourcePoolProps) {
        StringBuilder result = new StringBuilder();
        result.append(getDataSourcePoolProps(poolPropertySynonyms.getStandardProperties()));
        if (!customDataSourcePoolProps.getProperties().isEmpty()) {
            result.append(",");
            result.append(getDataSourcePoolProps(customDataSourcePoolProps.getProperties()));
        }
        return result.toString();
    }
    
    private String getDataSourcePoolProps(final Map<String, Object> props) {
        StringBuilder result = new StringBuilder();
        Iterator<Entry<String, Object>> iterator = props.entrySet().iterator();
        while (iterator.hasNext()) {
            Entry<String, Object> entry = iterator.next();
            if (null == entry.getValue()) {
                continue;
            }
            result.append(String.format(DistSQLScriptConstants.PROPERTY, entry.getKey(), entry.getValue()));
            if (iterator.hasNext()) {
                result.append(",");
            }
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
    
    @Override
    public Class<ConvertYamlConfigurationStatement> getType() {
        return ConvertYamlConfigurationStatement.class;
    }
}
