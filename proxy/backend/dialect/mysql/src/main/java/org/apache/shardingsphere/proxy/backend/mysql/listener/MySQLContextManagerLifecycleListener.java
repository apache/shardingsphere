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

package org.apache.shardingsphere.proxy.backend.mysql.listener;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.datanode.DataNodes;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.metadata.database.schema.builder.GenericSchemaBuilder;
import org.apache.shardingsphere.infra.metadata.database.schema.builder.GenericSchemaBuilderMaterial;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.listener.ContextManagerLifecycleListener;

import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * MySQL original temporal type initialization listener.
 */
@Slf4j
public final class MySQLContextManagerLifecycleListener implements ContextManagerLifecycleListener {
    
    @Override
    public void onInitialized(final ContextManager contextManager) {
        for (ShardingSphereDatabase each : contextManager.getMetaDataContexts().getMetaData().getAllDatabases()) {
            if ("MySQL".equals(each.getProtocolType().getType())) {
                initializeDatabase(contextManager, each);
            }
        }
    }
    
    private void initializeDatabase(final ContextManager contextManager, final ShardingSphereDatabase database) {
        DataNodes dataNodes = new DataNodes(database.getRuleMetaData().getRules());
        boolean checkTableMetadata = contextManager.getMetaDataContexts().getMetaData().getProps().getValue(ConfigurationPropertyKey.CHECK_TABLE_METADATA_ENABLED);
        for (ShardingSphereSchema each : database.getAllSchemas()) {
            initializeSchema(contextManager, database, each, dataNodes, checkTableMetadata);
        }
    }
    
    private void initializeSchema(final ContextManager contextManager, final ShardingSphereDatabase database, final ShardingSphereSchema schema,
                                  final DataNodes dataNodes, final boolean checkTableMetadata) {
        Collection<ShardingSphereTable> tables = findUnknownTables(database, schema, dataNodes, checkTableMetadata);
        if (tables.isEmpty()) {
            return;
        }
        GenericSchemaBuilderMaterial material = new GenericSchemaBuilderMaterial(database.getResourceMetaData().getStorageUnits(), database.getRuleMetaData().getRules(),
                contextManager.getMetaDataContexts().getMetaData().getProps(), schema.getName(), database.getIdentifierContext(), database.getAllSchemas());
        Map<String, ShardingSphereSchema> loadedSchemas;
        try {
            loadedSchemas = GenericSchemaBuilder.build(tables.stream().map(ShardingSphereTable::getName).collect(Collectors.toList()), database.getProtocolType(), material);
        } catch (final SQLException ex) {
            log.warn("Load original temporal types for database: {} schema: {} failed", database.getName(), schema.getName(), ex);
            return;
        }
        ShardingSphereSchema loadedSchema = loadedSchemas.get(schema.getName());
        if (null == loadedSchema) {
            return;
        }
        Collection<ShardingSphereTable> initializedTables = mergeTables(tables, loadedSchema);
        if (!initializedTables.isEmpty()) {
            contextManager.getPersistServiceFacade().getMetaDataFacade().getDatabaseMetaDataFacade().getTable().persist(database.getName(), schema.getName(), initializedTables);
            initializedTables.forEach(each -> contextManager.getMetaDataContextManager().getDatabaseMetaDataManager().alterTable(database.getName(), schema.getName(), each));
        }
    }
    
    private Collection<ShardingSphereTable> findUnknownTables(final ShardingSphereDatabase database, final ShardingSphereSchema schema, final DataNodes dataNodes, final boolean checkTableMetadata) {
        Collection<ShardingSphereTable> result = new ArrayList<>();
        for (ShardingSphereTable each : schema.getAllTables()) {
            if (containsUnknownDatetime(each) && isMySQLTable(database, schema.getName(), dataNodes.getDataNodes(each.getName()), checkTableMetadata)) {
                result.add(each);
            }
        }
        return result;
    }
    
    private boolean containsUnknownDatetime(final ShardingSphereTable table) {
        for (ShardingSphereColumn each : table.getAllColumns()) {
            if (Types.TIMESTAMP == each.getDataType() && null == each.getDatetime()) {
                return true;
            }
        }
        return false;
    }
    
    private boolean isMySQLTable(final ShardingSphereDatabase database, final String schemaName, final Collection<DataNode> dataNodes, final boolean checkTableMetadata) {
        boolean matchingSchemaFound = false;
        for (DataNode each : dataNodes) {
            boolean matchingSchema = null == each.getSchemaName() || schemaName.equalsIgnoreCase(each.getSchemaName());
            if (!matchingSchema && !checkTableMetadata) {
                continue;
            }
            int delimiterIndex = each.getDataSourceName().indexOf('.');
            String dataSourceName = -1 == delimiterIndex ? each.getDataSourceName() : each.getDataSourceName().substring(0, delimiterIndex);
            StorageUnit storageUnit = database.getResourceMetaData().getStorageUnits().get(each.getDataSourceName());
            StorageUnit connectionStorageUnit = database.getResourceMetaData().getStorageUnits().get(dataSourceName);
            if (null == storageUnit || !"MySQL".equals(storageUnit.getStorageType().getType())
                    || null == connectionStorageUnit || !"MySQL".equals(connectionStorageUnit.getStorageType().getType())) {
                return false;
            }
            matchingSchemaFound |= matchingSchema;
        }
        return matchingSchemaFound;
    }
    
    private Collection<ShardingSphereTable> mergeTables(final Collection<ShardingSphereTable> tables, final ShardingSphereSchema loadedSchema) {
        Collection<ShardingSphereTable> result = new ArrayList<>(tables.size());
        for (ShardingSphereTable each : tables) {
            ShardingSphereTable loadedTable = loadedSchema.getTable(each.getName());
            if (null != loadedTable) {
                ShardingSphereTable initializedTable = mergeTable(each, loadedTable);
                if (each != initializedTable) {
                    result.add(initializedTable);
                }
            }
        }
        return result;
    }
    
    private ShardingSphereTable mergeTable(final ShardingSphereTable table, final ShardingSphereTable loadedTable) {
        Collection<ShardingSphereColumn> columns = new ArrayList<>(table.getAllColumns().size());
        boolean changed = false;
        for (ShardingSphereColumn each : table.getAllColumns()) {
            ShardingSphereColumn loadedColumn = loadedTable.getColumn(each.getName());
            if (Types.TIMESTAMP == each.getDataType() && null == each.getDatetime() && null != loadedColumn && Types.TIMESTAMP == loadedColumn.getDataType()) {
                columns.add(new ShardingSphereColumn(each.getName(), each.getDataType(), each.isPrimaryKey(), each.isGenerated(), each.isCaseSensitive(), each.isVisible(), each.isUnsigned(),
                        each.isNullable(), loadedColumn.getDatetime()));
                changed = true;
            } else {
                columns.add(each);
            }
        }
        return changed ? new ShardingSphereTable(table.getName(), columns, table.getAllIndexes(), table.getAllConstraints(), table.getType()) : table;
    }
    
    @Override
    public void onDestroyed(final ContextManager contextManager) {
    }
}
