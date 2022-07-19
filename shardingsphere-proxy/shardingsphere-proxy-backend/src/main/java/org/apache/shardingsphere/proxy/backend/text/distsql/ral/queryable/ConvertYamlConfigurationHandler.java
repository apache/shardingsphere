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

package org.apache.shardingsphere.proxy.backend.text.distsql.ral.queryable;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.shardingsphere.distsql.parser.statement.ral.queryable.ConvertYamlConfigurationStatement;
import org.apache.shardingsphere.infra.datasource.props.DataSourceProperties;
import org.apache.shardingsphere.infra.datasource.props.DataSourcePropertiesCreator;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.config.yaml.YamlProxyDataSourceConfiguration;
import org.apache.shardingsphere.proxy.backend.config.yaml.YamlProxyDatabaseConfiguration;
import org.apache.shardingsphere.proxy.backend.config.yaml.swapper.YamlProxyDataSourceConfigurationSwapper;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.QueryableRALBackendHandler;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Convert database configuration handler.
 */
public class ConvertYamlConfigurationHandler extends QueryableRALBackendHandler<ConvertYamlConfigurationStatement> {

    private static final String CREATE_DATABASE = "CREATE DATABASE %s;";
    
    private static final String ADD_RESOURCE = "ADD RESOURCE";
    
    private static final String RESOURCES = " %s (" + System.lineSeparator()
                                            + "%s"
                                            + "    PROPERTIES(%s)" + System.lineSeparator()
                                            + "),";

    private final YamlProxyDataSourceConfigurationSwapper dataSourceConfigSwapper = new YamlProxyDataSourceConfigurationSwapper();

    @Override
    protected Collection<String> getColumnNames() {
        return Collections.singleton("converted_distsql");
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
        String convertedDistSQL = generateConvertedDistSQL(yamlConfig);
        return Collections.singleton(new LocalDataQueryResultRow(convertedDistSQL));
    }
    
    private String generateConvertedDistSQL(final YamlProxyDatabaseConfiguration yamlConfig) {
        StringBuilder convetedDistSQL = new StringBuilder();
        appendCreateDatabaseDistSQL(yamlConfig.getDatabaseName(), convetedDistSQL);
        appendAddResourceDistSQL(yamlConfig.getDataSources(), convetedDistSQL);
        return convetedDistSQL.toString();
    }
    
    private void appendCreateDatabaseDistSQL(final String databaseName, final StringBuilder stringBuilder) {
        if (databaseName.isEmpty()) {
            return;
        }
        stringBuilder.append(String.format(CREATE_DATABASE, databaseName));
    }
    
    private void appendAddResourceDistSQL(final Map<String, YamlProxyDataSourceConfiguration> yamlDataSourceMap, final StringBuilder stringBuilder) {
        if (yamlDataSourceMap.isEmpty()) {
            return;
        }
        if (null == stringBuilder) {
            stringBuilder.append(ADD_RESOURCE);
        } else {
            stringBuilder.append(String.format(System.lineSeparator() + System.lineSeparator() + ADD_RESOURCE));
        }
        Map<String, DataSourceProperties> dataSourcePropsMap = new LinkedHashMap<>(yamlDataSourceMap.size(), 1);
        for (Map.Entry<String, YamlProxyDataSourceConfiguration> entry : yamlDataSourceMap.entrySet()) {
            dataSourcePropsMap.put(entry.getKey(), DataSourcePropertiesCreator.create(HikariDataSource.class.getName(), dataSourceConfigSwapper.swap(entry.getValue())));
        }
        stringBuilder.append(ADD_RESOURCE);
        dataSourcePropsMap.forEach((key, value) -> addResources(key, value, stringBuilder));
        stringBuilder.deleteCharAt(stringBuilder.length() - 1).append(";");
    }

    private void addResources(final String resourceName, final DataSourceProperties properties, final StringBuilder stringBuilder) {
        String connectProperties = new String();
        String poolProperties = new String();
        for (Map.Entry<String, Object> entry : properties.getConnectionPropertySynonyms().getStandardProperties().entrySet()) {
            connectProperties = connectProperties.concat(String.format("    %s=%s," + System.lineSeparator(), entry.getKey(), entry.getValue()));
        }
        for (Map.Entry<String, Object> entry : properties.getPoolPropertySynonyms().getStandardProperties().entrySet()) {
            if (null != entry.getValue()) {
                poolProperties = poolProperties.concat(String.format("\"%s\"=%s, ", entry.getKey(), entry.getValue()));
            }
        }
        for (Map.Entry<String, Object> entry : properties.getCustomDataSourceProperties().getProperties().entrySet()) {
            if (entry.getValue().equals(false) || entry.getValue().equals(true)) {
                poolProperties = poolProperties.concat(String.format("\"%s\"=%s, ", entry.getKey(), entry.getValue()));
            } else {
                poolProperties = poolProperties.concat(String.format("\"%s\"=\"%s\", ", entry.getKey(), entry.getValue()));
            }
        }
        poolProperties = poolProperties.substring(0, poolProperties.length() - 2);
        stringBuilder.append(String.format(RESOURCES, resourceName, connectProperties, poolProperties));
    }
}
