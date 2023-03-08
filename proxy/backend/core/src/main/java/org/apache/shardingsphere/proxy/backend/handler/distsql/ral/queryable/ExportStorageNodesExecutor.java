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
import org.apache.shardingsphere.distsql.parser.statement.ral.queryable.ExportStorageNodesStatement;
import org.apache.shardingsphere.infra.database.metadata.DataSourceMetaData;
import org.apache.shardingsphere.infra.datasource.props.DataSourceProperties;
import org.apache.shardingsphere.infra.datasource.props.DataSourcePropertiesCreator;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.distsql.export.ExportedStorageNode;
import org.apache.shardingsphere.proxy.backend.distsql.export.ExportedStorageNodes;
import org.apache.shardingsphere.proxy.backend.util.ExportUtils;
import org.apache.shardingsphere.proxy.backend.util.JsonUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Export storage nodes executor.
 */
public final class ExportStorageNodesExecutor implements DatabaseRequiredQueryableRALExecutor<ExportStorageNodesStatement> {
    
    @Override
    public Collection<String> getColumnNames() {
        return Arrays.asList("id", "create_time", "storage_nodes");
    }
    
    @Override
    public Collection<LocalDataQueryResultRow> getRows(final ShardingSphereDatabase database, final ExportStorageNodesStatement sqlStatement) {
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
        ExportedStorageNodes storageNodes = new ExportedStorageNodes();
        storageNodes.setStorageNodes(generateExportStorageNodeData(database));
        return JsonUtils.toJsonString(storageNodes);
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
    
    @Override
    public String getType() {
        return ExportStorageNodesStatement.class.getName();
    }
}
