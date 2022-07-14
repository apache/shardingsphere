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

package org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.queryable;

import org.apache.shardingsphere.distsql.parser.statement.ral.common.queryable.ConvertYamlConfigurationStatement;
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
 * Convert yaml configuration handler.
 */
public class ConvertYamlConfigurationHandler extends QueryableRALBackendHandler<ConvertYamlConfigurationStatement> {
    
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
        if (databaseName == null) {
            return;
        }
        stringBuilder.append("CREATE DATABASE").append(" ").append(databaseName).append(";");
    }
    
    private void appendAddResourceDistSQL(final Map<String, YamlProxyDataSourceConfiguration> databaseResource, final StringBuilder stringBuilder) {
        if (databaseResource.isEmpty()) {
            return;
        }
        if (stringBuilder.length() == 0) {
            stringBuilder.append("ADD RESOURCES");
        } else {
            stringBuilder.append(System.lineSeparator() + System.lineSeparator()).append("ADD RESOURCES");
        }
        for (Map.Entry<String, YamlProxyDataSourceConfiguration> entry : databaseResource.entrySet()) {
            stringBuilder.append(" ").append(entry.getKey()).append(" (" + System.lineSeparator());
            if (entry.getValue().getUrl() != null) {
                stringBuilder.append("    URL=").append(entry.getValue().getUrl()).append("," + System.lineSeparator());
            } else {
                stringBuilder.append("    HOST=").append(entry.getValue().getHost()).append("," + System.lineSeparator());
                stringBuilder.append("    PORT=").append(entry.getValue().getPort()).append("," + System.lineSeparator());
                stringBuilder.append("    DB=").append(entry.getValue().getDb()).append("," + System.lineSeparator());
            }
            stringBuilder.append("    USER=").append(entry.getValue().getUsername()).append("," + System.lineSeparator());
            stringBuilder.append("    PASSWORD=").append(entry.getValue().getPassword()).append("" + System.lineSeparator());
            stringBuilder.append("    PROPERTIES(");
            if (entry.getValue().getProperties() != null) {
                entry.getValue().getProperties().forEach((key, value) -> stringBuilder.append("\"" + key + "\"=").append(value).append(","));
                stringBuilder.deleteCharAt(stringBuilder.length() - 1);
            } else {
                if (entry.getValue().getConnectionTimeoutMilliseconds() != null)
                    stringBuilder.append("\"connectionTimeoutMilliseconds\"=").append(entry.getValue().getConnectionTimeoutMilliseconds()).append(",");
                if (entry.getValue().getIdleTimeoutMilliseconds() != null)
                    stringBuilder.append("\"idleTimeoutMilliseconds\"=").append(entry.getValue().getIdleTimeoutMilliseconds()).append(",");
                if (entry.getValue().getMaxLifetimeMilliseconds() != null)
                    stringBuilder.append("\"maxLifetimeMilliseconds\"=").append(entry.getValue().getMaxLifetimeMilliseconds()).append(",");
                if (entry.getValue().getMaxPoolSize() != null)
                    stringBuilder.append("\"maxPoolSize\"=").append(entry.getValue().getMaxPoolSize());
                if (entry.getValue().getMinPoolSize() != null)
                    stringBuilder.append("\"minPoolSize\"=").append(entry.getValue().getMinPoolSize());
            }
            stringBuilder.append(")").append(System.lineSeparator()).append("),");
        }
        stringBuilder.deleteCharAt(stringBuilder.length() - 1).append(";");
    }
}