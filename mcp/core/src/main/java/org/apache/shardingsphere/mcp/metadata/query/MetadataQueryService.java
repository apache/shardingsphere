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

package org.apache.shardingsphere.mcp.metadata.query;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.mcp.capability.database.MCPDatabaseCapability;
import org.apache.shardingsphere.mcp.capability.database.MCPDatabaseCapabilityProvider;
import org.apache.shardingsphere.mcp.metadata.model.ColumnMetadata;
import org.apache.shardingsphere.mcp.metadata.model.DatabaseMetadata;
import org.apache.shardingsphere.mcp.metadata.model.DatabaseMetadataSnapshot;
import org.apache.shardingsphere.mcp.metadata.model.DatabaseMetadataSnapshots;
import org.apache.shardingsphere.mcp.metadata.model.IndexMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MetadataObjectType;
import org.apache.shardingsphere.mcp.metadata.model.SchemaMetadata;
import org.apache.shardingsphere.mcp.metadata.model.TableMetadata;
import org.apache.shardingsphere.mcp.metadata.model.ViewMetadata;
import org.apache.shardingsphere.mcp.protocol.exception.MCPUnsupportedException;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

/**
 * Metadata query service.
 */
@RequiredArgsConstructor
public final class MetadataQueryService {
    
    private final DatabaseMetadataSnapshots databaseMetadataSnapshots;
    
    /**
     * Query databases.
     *
     * @return database metadata
     */
    public List<DatabaseMetadata> queryDatabases() {
        List<DatabaseMetadata> result = new LinkedList<>();
        for (Entry<String, DatabaseMetadataSnapshot> entry : databaseMetadataSnapshots.getDatabaseSnapshots().entrySet()) {
            result.add(createDatabaseSummary(entry.getKey(), entry.getValue()));
        }
        result.sort(Comparator.comparing(DatabaseMetadata::getDatabase));
        return result;
    }
    
    /**
     * Query database.
     *
     * @param databaseName database name
     * @return database metadata
     */
    public Optional<DatabaseMetadata> queryDatabase(final String databaseName) {
        return databaseMetadataSnapshots.findSnapshot(databaseName).map(optional -> createDatabaseDetail(databaseName, optional));
    }
    
    /**
     * Query schemas.
     *
     * @param databaseName database name
     * @return schema metadata
     */
    public List<SchemaMetadata> querySchemas(final String databaseName) {
        if (!isSupportedMetadataObjectType(databaseName, MetadataObjectType.SCHEMA)) {
            return Collections.emptyList();
        }
        return databaseMetadataSnapshots.findSnapshot(databaseName).map(optional -> createSchemaSummaries(optional.getSchemas())).orElse(Collections.emptyList());
    }
    
    /**
     * Query schema.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @return schema metadata
     */
    public Optional<SchemaMetadata> querySchema(final String databaseName, final String schemaName) {
        if (!isSupportedMetadataObjectType(databaseName, MetadataObjectType.SCHEMA)) {
            return Optional.empty();
        }
        return findSchema(databaseName, schemaName).map(this::createSchemaDetail);
    }
    
    /**
     * Query tables.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @return table metadata
     */
    public List<TableMetadata> queryTables(final String databaseName, final String schemaName) {
        if (!isSupportedMetadataObjectType(databaseName, MetadataObjectType.TABLE)) {
            return Collections.emptyList();
        }
        return findSchema(databaseName, schemaName).map(optional -> createTableSummaries(optional.getTables())).orElse(Collections.emptyList());
    }
    
    /**
     * Query table.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param tableName table name
     * @return table metadata
     */
    public Optional<TableMetadata> queryTable(final String databaseName, final String schemaName, final String tableName) {
        if (!isSupportedMetadataObjectType(databaseName, MetadataObjectType.TABLE)) {
            return Optional.empty();
        }
        return findTable(databaseName, schemaName, tableName).map(this::createTableDetail);
    }
    
    /**
     * Query views.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @return view metadata
     */
    public List<ViewMetadata> queryViews(final String databaseName, final String schemaName) {
        if (!isSupportedMetadataObjectType(databaseName, MetadataObjectType.VIEW)) {
            return Collections.emptyList();
        }
        return findSchema(databaseName, schemaName).map(optional -> createViewSummaries(optional.getViews())).orElse(Collections.emptyList());
    }
    
    /**
     * Query view.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param viewName view name
     * @return view metadata
     */
    public Optional<ViewMetadata> queryView(final String databaseName, final String schemaName, final String viewName) {
        if (!isSupportedMetadataObjectType(databaseName, MetadataObjectType.VIEW)) {
            return Optional.empty();
        }
        return findView(databaseName, schemaName, viewName).map(this::createViewDetail);
    }
    
    /**
     * Query table columns.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param tableName table name
     * @return column metadata
     */
    public List<ColumnMetadata> queryTableColumns(final String databaseName, final String schemaName, final String tableName) {
        if (!isSupportedMetadataObjectType(databaseName, MetadataObjectType.COLUMN)) {
            return Collections.emptyList();
        }
        return findTable(databaseName, schemaName, tableName).map(optional -> sortColumns(optional.getColumns())).orElse(Collections.emptyList());
    }
    
    /**
     * Query table column.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param tableName table name
     * @param columnName column name
     * @return column metadata
     */
    public Optional<ColumnMetadata> queryTableColumn(final String databaseName, final String schemaName, final String tableName, final String columnName) {
        if (!isSupportedMetadataObjectType(databaseName, MetadataObjectType.COLUMN)) {
            return Optional.empty();
        }
        return findColumn(queryTableColumns(databaseName, schemaName, tableName), columnName);
    }
    
    /**
     * Query view columns.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param viewName view name
     * @return column metadata
     */
    public List<ColumnMetadata> queryViewColumns(final String databaseName, final String schemaName, final String viewName) {
        if (!isSupportedMetadataObjectType(databaseName, MetadataObjectType.COLUMN)) {
            return Collections.emptyList();
        }
        return findView(databaseName, schemaName, viewName).map(optional -> sortColumns(optional.getColumns())).orElse(Collections.emptyList());
    }
    
    /**
     * Query view column.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param viewName view name
     * @param columnName column name
     * @return column metadata
     */
    public Optional<ColumnMetadata> queryViewColumn(final String databaseName, final String schemaName, final String viewName, final String columnName) {
        if (!isSupportedMetadataObjectType(databaseName, MetadataObjectType.COLUMN)) {
            return Optional.empty();
        }
        return findColumn(queryViewColumns(databaseName, schemaName, viewName), columnName);
    }
    
    /**
     * Query indexes.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param tableName table name
     * @return index metadata
     */
    public List<IndexMetadata> queryIndexes(final String databaseName, final String schemaName, final String tableName) {
        assertIndexSupported(databaseName);
        return findTable(databaseName, schemaName, tableName).map(optional -> sortIndexes(optional.getIndexes())).orElse(Collections.emptyList());
    }
    
    /**
     * Query index.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param tableName table name
     * @param indexName index name
     * @return index metadata
     */
    public Optional<IndexMetadata> queryIndex(final String databaseName, final String schemaName, final String tableName, final String indexName) {
        assertIndexSupported(databaseName);
        return findIndex(queryIndexes(databaseName, schemaName, tableName), indexName);
    }
    
    /**
     * Judge whether the metadata object type is supported for the database.
     *
     * @param databaseName database name
     * @param objectType metadata object type
     * @return whether supported or not
     */
    public boolean isSupportedMetadataObjectType(final String databaseName, final MetadataObjectType objectType) {
        return getSupportedMetadataObjectTypes(databaseName).contains(objectType);
    }
    
    private Optional<SchemaMetadata> findSchema(final String databaseName, final String schemaName) {
        Optional<DatabaseMetadataSnapshot> databaseSnapshot = databaseMetadataSnapshots.findSnapshot(databaseName);
        if (databaseSnapshot.isEmpty()) {
            return Optional.empty();
        }
        for (SchemaMetadata each : databaseSnapshot.get().getSchemas()) {
            if (schemaName.equals(each.getSchema())) {
                return Optional.of(each);
            }
        }
        return Optional.empty();
    }
    
    private Optional<TableMetadata> findTable(final String databaseName, final String schemaName, final String tableName) {
        Optional<SchemaMetadata> schemaMetadata = findSchema(databaseName, schemaName);
        if (schemaMetadata.isEmpty()) {
            return Optional.empty();
        }
        for (TableMetadata each : schemaMetadata.get().getTables()) {
            if (tableName.equals(each.getTable())) {
                return Optional.of(each);
            }
        }
        return Optional.empty();
    }
    
    private Optional<ViewMetadata> findView(final String databaseName, final String schemaName, final String viewName) {
        Optional<SchemaMetadata> schemaMetadata = findSchema(databaseName, schemaName);
        if (schemaMetadata.isEmpty()) {
            return Optional.empty();
        }
        for (ViewMetadata each : schemaMetadata.get().getViews()) {
            if (viewName.equals(each.getView())) {
                return Optional.of(each);
            }
        }
        return Optional.empty();
    }
    
    private Optional<ColumnMetadata> findColumn(final Collection<ColumnMetadata> columns, final String columnName) {
        for (ColumnMetadata each : columns) {
            if (columnName.equals(each.getColumn())) {
                return Optional.of(each);
            }
        }
        return Optional.empty();
    }
    
    private Optional<IndexMetadata> findIndex(final Collection<IndexMetadata> indexes, final String indexName) {
        for (IndexMetadata each : indexes) {
            if (indexName.equals(each.getIndex())) {
                return Optional.of(each);
            }
        }
        return Optional.empty();
    }
    
    private DatabaseMetadata createDatabaseSummary(final String databaseName, final DatabaseMetadataSnapshot databaseSnapshot) {
        return new DatabaseMetadata(databaseName, databaseSnapshot.getDatabaseType(), databaseSnapshot.getDatabaseVersion(), Collections.emptyList());
    }
    
    private DatabaseMetadata createDatabaseDetail(final String databaseName, final DatabaseMetadataSnapshot databaseSnapshot) {
        return new DatabaseMetadata(databaseName, databaseSnapshot.getDatabaseType(), databaseSnapshot.getDatabaseVersion(), createSchemaDetails(databaseSnapshot.getSchemas()));
    }
    
    private List<SchemaMetadata> createSchemaSummaries(final Collection<SchemaMetadata> schemas) {
        List<SchemaMetadata> result = new LinkedList<>();
        for (SchemaMetadata each : schemas) {
            result.add(new SchemaMetadata(each.getDatabase(), each.getSchema(), Collections.emptyList(), Collections.emptyList()));
        }
        result.sort((left, right) -> left.getSchema().compareTo(right.getSchema()));
        return result;
    }
    
    private List<SchemaMetadata> createSchemaDetails(final Collection<SchemaMetadata> schemas) {
        List<SchemaMetadata> result = new LinkedList<>();
        for (SchemaMetadata each : schemas) {
            result.add(createSchemaDetail(each));
        }
        result.sort((left, right) -> left.getSchema().compareTo(right.getSchema()));
        return result;
    }
    
    private SchemaMetadata createSchemaDetail(final SchemaMetadata schemaMetadata) {
        return new SchemaMetadata(schemaMetadata.getDatabase(), schemaMetadata.getSchema(), createTableSummaries(schemaMetadata.getTables()), createViewSummaries(schemaMetadata.getViews()));
    }
    
    private List<TableMetadata> createTableSummaries(final Collection<TableMetadata> tables) {
        List<TableMetadata> result = new LinkedList<>();
        for (TableMetadata each : tables) {
            result.add(new TableMetadata(each.getDatabase(), each.getSchema(), each.getTable(), Collections.emptyList(), Collections.emptyList()));
        }
        result.sort((left, right) -> left.getTable().compareTo(right.getTable()));
        return result;
    }
    
    private TableMetadata createTableDetail(final TableMetadata tableMetadata) {
        return new TableMetadata(tableMetadata.getDatabase(), tableMetadata.getSchema(), tableMetadata.getTable(),
                sortColumns(tableMetadata.getColumns()), sortIndexes(tableMetadata.getIndexes()));
    }
    
    private List<ViewMetadata> createViewSummaries(final Collection<ViewMetadata> views) {
        List<ViewMetadata> result = new LinkedList<>();
        for (ViewMetadata each : views) {
            result.add(new ViewMetadata(each.getDatabase(), each.getSchema(), each.getView(), Collections.emptyList()));
        }
        result.sort((left, right) -> left.getView().compareTo(right.getView()));
        return result;
    }
    
    private ViewMetadata createViewDetail(final ViewMetadata viewMetadata) {
        return new ViewMetadata(viewMetadata.getDatabase(), viewMetadata.getSchema(), viewMetadata.getView(), sortColumns(viewMetadata.getColumns()));
    }
    
    private List<ColumnMetadata> sortColumns(final Collection<ColumnMetadata> columns) {
        List<ColumnMetadata> result = new LinkedList<>(columns);
        result.sort((left, right) -> left.getColumn().compareTo(right.getColumn()));
        return result;
    }
    
    private List<IndexMetadata> sortIndexes(final Collection<IndexMetadata> indexes) {
        List<IndexMetadata> result = new LinkedList<>(indexes);
        result.sort((left, right) -> left.getIndex().compareTo(right.getIndex()));
        return result;
    }
    
    private void assertIndexSupported(final String databaseName) {
        if (!isSupportedMetadataObjectType(databaseName, MetadataObjectType.INDEX)) {
            throw new MCPUnsupportedException("Index resources are not supported for the current database.");
        }
    }
    
    private Set<MetadataObjectType> getSupportedMetadataObjectTypes(final String databaseName) {
        Optional<MCPDatabaseCapability> databaseCapability = new MCPDatabaseCapabilityProvider(databaseMetadataSnapshots).provide(databaseName);
        return databaseCapability.isPresent() ? databaseCapability.get().getSupportedMetadataObjectTypes() : Collections.emptySet();
    }
}
