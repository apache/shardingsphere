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

import org.apache.shardingsphere.distsql.parser.statement.ral.queryable.ConvertYamlConfigurationStatement;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.config.yaml.YamlProxyDataSourceConfiguration;
import org.apache.shardingsphere.proxy.backend.config.yaml.YamlProxyDatabaseConfiguration;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.QueryableRALBackendHandler;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * Convert database configuration handler.
 */
public class ConvertYamlConfigurationHandler extends QueryableRALBackendHandler<ConvertYamlConfigurationStatement> {
    
    private static final String CREATE_DATABASE = "CREATE DATABASE %s;";
    
    private static final String ADD_RESOURCE = "ADD RESOURCE";
    
    private static final String RESOURCES = " %s ("
                                            + System.lineSeparator()
                                            + "\tURL=\"%s\","
                                            + System.lineSeparator()
                                            + "\tUSER=%s,"
                                            + System.lineSeparator()
                                            + "\tPASSWORD=%s,"
                                            + System.lineSeparator()
                                            + "\tPROPERTIES(%s%s%s%s%s)"
                                            + System.lineSeparator()
                                            + "),";
    
    @Override
    protected Collection<String> getColumnNames() {
        return Collections.singleton("converted DistSQL");
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
    
    private void appendAddResourceDistSQL(final Map<String, YamlProxyDataSourceConfiguration> databaseResource, final StringBuilder stringBuilder) {
        if (databaseResource.isEmpty()) {
            return;
        }
        if (null == stringBuilder) {
            stringBuilder.append(ADD_RESOURCE);
        } else {
            stringBuilder.append(String.format(System.lineSeparator()
                                               + System.lineSeparator()
                                               + ADD_RESOURCE));
        }
        stringBuilder.append(ADD_RESOURCE);
        for (Map.Entry<String, YamlProxyDataSourceConfiguration> entry : databaseResource.entrySet()) {
            String resourceName = entry.getKey();
            String url = entry.getValue().getUrl();
            String username = entry.getValue().getUsername();
            String password = entry.getValue().getPassword();
            String connectionTimeoutMilliseconds = null == entry.getValue().getConnectionTimeoutMilliseconds() ? String.format("")
                    : String.format("\"connectionTimeoutMilliseconds\"=%s,", entry.getValue().getConnectionTimeoutMilliseconds().toString());
            String idleTimeoutMilliseconds = null == entry.getValue().getIdleTimeoutMilliseconds() ? String.format("")
                    : String.format("\"idleTimeoutMilliseconds\"=%s,", entry.getValue().getIdleTimeoutMilliseconds().toString());
            String maxLifetimeMilliseconds = null == entry.getValue().getMaxLifetimeMilliseconds() ? String.format("")
                    : String.format("\"getMaxLifetimeMilliseconds\"=%s,", entry.getValue().getMaxLifetimeMilliseconds().toString());
            String maxPoolSize = null == entry.getValue().getMaxPoolSize() ? String.format("") : String.format("\"maxPoolSize\"=%s,", entry.getValue().getMaxPoolSize().toString());
            String minPoolSize = null == entry.getValue().getMinPoolSize() ? String.format("") : String.format("\"minPoolSize\"=%s", entry.getValue().getMinPoolSize().toString());
            stringBuilder.append(String.format(RESOURCES, resourceName, url, username, password, connectionTimeoutMilliseconds,
                    idleTimeoutMilliseconds, maxLifetimeMilliseconds, maxPoolSize, minPoolSize));
        }
        stringBuilder.deleteCharAt(stringBuilder.length() - 1).append(";");
    }
}
