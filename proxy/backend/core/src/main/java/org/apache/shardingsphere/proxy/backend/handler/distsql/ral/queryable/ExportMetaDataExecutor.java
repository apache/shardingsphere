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

import org.apache.shardingsphere.distsql.handler.ral.query.DatabaseRequiredQueryableRALExecutor;
import org.apache.shardingsphere.distsql.parser.statement.ral.queryable.ExportMetaDataStatement;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.database.metadata.DataSourceMetaData;
import org.apache.shardingsphere.infra.datasource.props.DataSourceProperties;
import org.apache.shardingsphere.infra.datasource.props.DataSourcePropertiesCreator;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.util.spi.type.ordered.OrderedSPILoader;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.YamlRuleConfigurationSwapper;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.distsql.export.ExportedClusterInfo;
import org.apache.shardingsphere.proxy.backend.distsql.export.ExportedMetaData;
import org.apache.shardingsphere.proxy.backend.distsql.export.ExportedStorageNode;
import org.apache.shardingsphere.proxy.backend.util.ExportUtils;
import org.apache.shardingsphere.proxy.backend.util.JsonUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

/**
 * Export metadata executor.
 */
public final class ExportMetaDataExecutor implements DatabaseRequiredQueryableRALExecutor<ExportMetaDataStatement> {
    
    @Override
    public Collection<String> getColumnNames() {
        return Arrays.asList("id", "create_time", "cluster_info");
    }
    
    @Override
    public Collection<LocalDataQueryResultRow> getRows(final ShardingSphereDatabase database, final ExportMetaDataStatement sqlStatement) {
        String exportedData = generateExportData(database);
        if (sqlStatement.getFilePath().isPresent()) {
            String filePath = sqlStatement.getFilePath().get();
            ExportUtils.exportToFile(filePath, exportedData);
            return Collections.singleton(new LocalDataQueryResultRow(ProxyContext.getInstance().getContextManager().getInstanceContext().getInstance().getCurrentInstanceId(), LocalDateTime.now(),
                    String.format("Successfully exported to：'%s'", filePath)));
        }
        return Collections.singleton(
                new LocalDataQueryResultRow(ProxyContext.getInstance().getContextManager().getInstanceContext().getInstance().getCurrentInstanceId(), LocalDateTime.now(), exportedData));
    }
    
    private String generateExportData(final ShardingSphereDatabase database) {
        ProxyContext proxyContext = ProxyContext.getInstance();
        ShardingSphereMetaData metaData = proxyContext.getContextManager().getMetaDataContexts().getMetaData();
        ExportedClusterInfo exportedClusterInfo = new ExportedClusterInfo();
        exportedClusterInfo.setStorageNodes(generateExportStorageNodeData(database));
        ExportedMetaData exportedMetaData = new ExportedMetaData();
        exportedMetaData.setDatabases(getDatabases(proxyContext));
        exportedMetaData.setProps(generatePropsData(metaData.getProps().getProps()));
        exportedMetaData.setRules(generateRulesData(metaData.getGlobalRuleMetaData().getConfigurations()));
        exportedClusterInfo.setMetaData(exportedMetaData);
        return JsonUtils.toJsonString(exportedClusterInfo);
    }
    
    private Map<String, String> getDatabases(final ProxyContext proxyContext) {
        Map<String, String> result = new LinkedHashMap<>();
        proxyContext.getAllDatabaseNames().forEach(each -> {
            ShardingSphereDatabase database = proxyContext.getDatabase(each);
            if (database.getResourceMetaData().getAllInstanceDataSourceNames().isEmpty()) {
                return;
            }
            result.put(each, ExportUtils.generateExportDatabaseData(database));
        });
        return result;
    }
    
    private Collection<ExportedStorageNode> generateExportStorageNodeData(final ShardingSphereDatabase database) {
        Map<String, ExportedStorageNode> storageNodes = new LinkedHashMap<>();
        database.getResourceMetaData().getDataSources().forEach((key, value) -> {
            DataSourceMetaData dataSourceMetaData = database.getResourceMetaData().getDataSourceMetaData(key);
            String databaseInstanceIp = dataSourceMetaData.getHostname() + ":" + dataSourceMetaData.getPort();
            if (storageNodes.containsKey(databaseInstanceIp)) {
                return;
            }
            ExportedStorageNode exportedStorageNode = new ExportedStorageNode();
            exportedStorageNode.setIp(dataSourceMetaData.getHostname());
            exportedStorageNode.setPort(String.valueOf(dataSourceMetaData.getPort()));
            exportedStorageNode.setDatabase(dataSourceMetaData.getCatalog());
            DataSourceProperties dataSourceProps = DataSourcePropertiesCreator.create(value);
            Map<String, Object> standardProperties = dataSourceProps.getConnectionPropertySynonyms().getStandardProperties();
            exportedStorageNode.setUsername(String.valueOf(standardProperties.get("username")));
            exportedStorageNode.setPassword(String.valueOf(standardProperties.get("password")));
            storageNodes.put(databaseInstanceIp, exportedStorageNode);
        });
        return storageNodes.values();
    }
    
    private String generatePropsData(final Properties props) {
        if (props.isEmpty()) {
            return "";
        }
        StringBuilder result = new StringBuilder();
        result.append("props:").append(System.lineSeparator());
        props.forEach((key, value) -> result.append("  ").append(key).append(": ").append(value).append(System.lineSeparator()));
        return result.toString();
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    private String generateRulesData(final Collection<RuleConfiguration> rules) {
        if (rules.isEmpty()) {
            return "";
        }
        StringBuilder result = new StringBuilder();
        result.append("rules:").append(System.lineSeparator());
        for (Entry<RuleConfiguration, YamlRuleConfigurationSwapper> entry : OrderedSPILoader.getServices(YamlRuleConfigurationSwapper.class, rules).entrySet()) {
            result.append(YamlEngine.marshal(Collections.singletonList(entry.getValue().swapToYamlConfiguration(entry.getKey()))));
        }
        return result.toString();
    }
    
    @Override
    public String getType() {
        return ExportMetaDataStatement.class.getName();
    }
}
