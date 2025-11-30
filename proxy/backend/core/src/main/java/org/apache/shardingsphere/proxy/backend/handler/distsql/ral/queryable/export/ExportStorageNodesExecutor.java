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

package org.apache.shardingsphere.proxy.backend.handler.distsql.ral.queryable.export;

import org.apache.shardingsphere.database.connector.core.jdbcurl.parser.ConnectionProperties;
import org.apache.shardingsphere.distsql.handler.engine.query.DistSQLQueryExecutor;
import org.apache.shardingsphere.distsql.statement.type.ral.queryable.export.ExportStorageNodesStatement;
import org.apache.shardingsphere.infra.datasource.pool.props.creator.DataSourcePoolPropertiesCreator;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.util.json.JsonUtils;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.distsql.export.ExportedStorageNode;
import org.apache.shardingsphere.proxy.backend.distsql.export.ExportedStorageNodes;
import org.apache.shardingsphere.proxy.backend.util.ExportUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Export storage nodes executor.
 */
public final class ExportStorageNodesExecutor implements DistSQLQueryExecutor<ExportStorageNodesStatement> {
    
    @Override
    public Collection<String> getColumnNames(final ExportStorageNodesStatement sqlStatement) {
        return Arrays.asList("id", "create_time", "storage_nodes");
    }
    
    @Override
    public Collection<LocalDataQueryResultRow> getRows(final ExportStorageNodesStatement sqlStatement, final ContextManager contextManager) {
        checkSQLStatement(contextManager.getMetaDataContexts().getMetaData(), sqlStatement);
        String exportedData = generateExportData(contextManager.getMetaDataContexts().getMetaData(), sqlStatement);
        if (sqlStatement.getFilePath().isPresent()) {
            String filePath = sqlStatement.getFilePath().get();
            ExportUtils.exportToFile(filePath, exportedData);
            return Collections.singleton(new LocalDataQueryResultRow(contextManager.getComputeNodeInstanceContext().getInstance().getMetaData().getId(), LocalDateTime.now(),
                    String.format("Successfully exported to：'%s'", filePath)));
        }
        return Collections.singleton(
                new LocalDataQueryResultRow(contextManager.getComputeNodeInstanceContext().getInstance().getMetaData().getId(), LocalDateTime.now(), exportedData));
    }
    
    private void checkSQLStatement(final ShardingSphereMetaData metaData, final ExportStorageNodesStatement sqlStatement) {
        ShardingSpherePreconditions.checkState(null == sqlStatement.getDatabaseName() || null != metaData.getDatabase(sqlStatement.getDatabaseName()),
                () -> new IllegalArgumentException(String.format("database %s is not existed", sqlStatement.getDatabaseName())));
    }
    
    private String generateExportData(final ShardingSphereMetaData metaData, final ExportStorageNodesStatement sqlStatement) {
        ExportedStorageNodes storageNodes = new ExportedStorageNodes();
        storageNodes.setStorageNodes(null == sqlStatement.getDatabaseName()
                ? getAllStorageNodes(metaData)
                : generateDatabaseExportStorageNodesData(metaData.getDatabase(sqlStatement.getDatabaseName())));
        return JsonUtils.toJsonString(storageNodes);
    }
    
    private Map<String, Collection<ExportedStorageNode>> getAllStorageNodes(final ShardingSphereMetaData metaData) {
        Map<String, Collection<ExportedStorageNode>> storageNodes = new LinkedHashMap<>();
        for (ShardingSphereDatabase each : metaData.getAllDatabases()) {
            if (each.getResourceMetaData().getAllInstanceDataSourceNames().isEmpty()) {
                continue;
            }
            storageNodes.putAll(generateDatabaseExportStorageNodesData(each));
        }
        return storageNodes;
    }
    
    private Map<String, Collection<ExportedStorageNode>> generateDatabaseExportStorageNodesData(final ShardingSphereDatabase database) {
        Map<String, ExportedStorageNode> storageNodes = new LinkedHashMap<>(database.getResourceMetaData().getStorageUnits().size(), 1F);
        for (Entry<String, StorageUnit> entry : database.getResourceMetaData().getStorageUnits().entrySet()) {
            ConnectionProperties connectionProps = database.getResourceMetaData().getStorageUnits().get(entry.getKey()).getConnectionProperties();
            String databaseInstanceIp = getDatabaseInstanceIp(connectionProps);
            if (storageNodes.containsKey(databaseInstanceIp)) {
                continue;
            }
            Map<String, Object> standardProps = DataSourcePoolPropertiesCreator.create(entry.getValue().getDataSource()).getConnectionPropertySynonyms().getStandardProperties();
            ExportedStorageNode exportedStorageNode = new ExportedStorageNode(connectionProps.getHostname(), String.valueOf(connectionProps.getPort()),
                    String.valueOf(standardProps.get("username")), String.valueOf(standardProps.get("password")), connectionProps.getCatalog());
            storageNodes.put(databaseInstanceIp, exportedStorageNode);
        }
        return Collections.singletonMap(database.getName(), storageNodes.values());
    }
    
    private String getDatabaseInstanceIp(final ConnectionProperties connectionProps) {
        return String.format("%s:%s", connectionProps.getHostname(), connectionProps.getPort());
    }
    
    @Override
    public Class<ExportStorageNodesStatement> getType() {
        return ExportStorageNodesStatement.class;
    }
}
