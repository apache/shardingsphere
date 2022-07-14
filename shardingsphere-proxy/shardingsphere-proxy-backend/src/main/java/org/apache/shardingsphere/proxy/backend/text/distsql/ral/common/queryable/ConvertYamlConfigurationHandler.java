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
    
    static String whiteSpace = " ";
    
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
        stringBuilder.append(String.format("CREATE DATABASE %s;", databaseName));
    }
    
    private void appendAddResourceDistSQL(final Map<String, YamlProxyDataSourceConfiguration> databaseResource, final StringBuilder stringBuilder) {
        if (databaseResource.isEmpty()) {
            return;
        }
        if (null == stringBuilder) {
            stringBuilder.append("ADD RESOURCES");
        } else {
            stringBuilder.append(String.format(System.lineSeparator() + System.lineSeparator() + "ADD RESOURCES"));
        }
        for (Map.Entry<String, YamlProxyDataSourceConfiguration> entry : databaseResource.entrySet()) {
            stringBuilder.append(String.format("%s%s (" + System.lineSeparator(), whiteSpace, entry.getKey()));
            if (null != entry.getValue().getUrl()) {
                stringBuilder.append(String.format("%4sURL=%s," + System.lineSeparator(), whiteSpace, entry.getValue().getUrl()));
            } else {
                stringBuilder.append(String.format("%4sHOST=%s," + System.lineSeparator(), whiteSpace, entry.getValue().getHost()));
                stringBuilder.append(String.format("%4sPORT=%s," + System.lineSeparator(), whiteSpace, entry.getValue().getPort()));
                stringBuilder.append(String.format("%4sDB=%s," + System.lineSeparator(), whiteSpace, entry.getValue().getDb()));
            }
            stringBuilder.append(String.format("%4sUSER=%s," + System.lineSeparator(), whiteSpace, entry.getValue().getUsername()));
            stringBuilder.append(String.format("%4sPASSWORD=%s" + System.lineSeparator(), whiteSpace, entry.getValue().getPassword()));
            stringBuilder.append(String.format("%4sPROPERTIES(", whiteSpace));
            if (null != entry.getValue().getProperties()) {
                entry.getValue().getProperties().forEach((key, value) -> stringBuilder.append(String.format("\"%s\"=%s,", key, value)));
                stringBuilder.deleteCharAt(stringBuilder.length() - 1);
            } else {
                if (null != entry.getValue().getConnectionTimeoutMilliseconds())
                    stringBuilder.append(String.format("\"connectionTimeoutMilliseconds\"=%s,", entry.getValue().getConnectionTimeoutMilliseconds()));
                if (null != entry.getValue().getIdleTimeoutMilliseconds())
                    stringBuilder.append(String.format("\"idleTimeoutMilliseconds\"=%s,", entry.getValue().getIdleTimeoutMilliseconds()));
                if (null != entry.getValue().getMaxLifetimeMilliseconds())
                    stringBuilder.append(String.format("\"maxLifetimeMilliseconds\"=%s,", entry.getValue().getMaxLifetimeMilliseconds()));
                if (null != entry.getValue().getMaxPoolSize())
                    stringBuilder.append(String.format("\"maxPoolSize\"=%s,", entry.getValue().getMaxPoolSize()));
                if (null != entry.getValue().getMinPoolSize())
                    stringBuilder.append(String.format("\"minPoolSize\"=%s", entry.getValue().getMinPoolSize()));
            }
            stringBuilder.append(")" + System.lineSeparator() + "),");
        }
        stringBuilder.deleteCharAt(stringBuilder.length() - 1).append(";");
    }
}
