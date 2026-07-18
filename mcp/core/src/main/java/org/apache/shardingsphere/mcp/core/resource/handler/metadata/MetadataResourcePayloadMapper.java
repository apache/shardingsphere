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

package org.apache.shardingsphere.mcp.core.resource.handler.metadata;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.connector.core.metadata.database.enums.TableType;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereIndex;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSequence;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.mcp.api.capability.resource.MCPUriVariables;
import org.apache.shardingsphere.mcp.support.database.metadata.jdbc.RuntimeDatabaseProfile;
import org.apache.shardingsphere.mcp.support.database.metadata.model.MCPColumnMetadata;
import org.apache.shardingsphere.mcp.support.database.spi.MCPMetadataQueryFacade;
import org.apache.shardingsphere.mcp.support.descriptor.ShardingSphereMCPResourceMetadata;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Metadata resource payload mapper.
 */
@RequiredArgsConstructor
public final class MetadataResourcePayloadMapper {
    
    private final MCPMetadataQueryFacade metadataQueryFacade;
    
    private final MCPUriVariables uriVariables;
    
    private final boolean detail;
    
    /**
     * Map internal metadata objects to MCP resource payload objects.
     *
     * @param metadata resource metadata
     * @param items internal metadata objects
     * @return MCP resource payload objects
     */
    public List<?> map(final ShardingSphereMCPResourceMetadata metadata, final List<?> items) {
        return items.stream().map(each -> mapItem(metadata, each)).toList();
    }
    
    private Object mapItem(final ShardingSphereMCPResourceMetadata metadata, final Object item) {
        if (item instanceof RuntimeDatabaseProfile) {
            return createDatabasePayload((RuntimeDatabaseProfile) item);
        }
        if (item instanceof ShardingSphereSchema) {
            return createSchemaPayload(getDatabase(), (ShardingSphereSchema) item, detail);
        }
        if (item instanceof ShardingSphereTable) {
            ShardingSphereTable table = (ShardingSphereTable) item;
            return "view".equals(metadata.getObjectScope()) ? createViewPayload(getDatabase(), getSchema(), table, detail) : createTablePayload(getDatabase(), getSchema(), table, detail);
        }
        if (item instanceof MCPColumnMetadata) {
            return createColumnPayload((MCPColumnMetadata) item);
        }
        if (item instanceof ShardingSphereIndex) {
            return createIndexPayload((ShardingSphereIndex) item);
        }
        if (item instanceof ShardingSphereSequence) {
            return createSequencePayload((ShardingSphereSequence) item);
        }
        return item;
    }
    
    private Map<String, Object> createDatabasePayload(final RuntimeDatabaseProfile databaseProfile) {
        Map<String, Object> result = new LinkedHashMap<>(4, 1F);
        result.put("database", databaseProfile.getDatabase());
        result.put("databaseType", databaseProfile.getDatabaseType());
        result.put("databaseVersion", databaseProfile.getDatabaseVersion());
        result.put("schemas", detail
                ? metadataQueryFacade.querySchemas(databaseProfile.getDatabase()).stream().map(each -> createSchemaPayload(databaseProfile.getDatabase(), each, true)).toList()
                : List.of());
        return result;
    }
    
    private Map<String, Object> createSchemaPayload(final String database, final ShardingSphereSchema schema, final boolean detail) {
        Map<String, Object> result = new LinkedHashMap<>(5, 1F);
        result.put("database", database);
        result.put("schema", schema.getName());
        result.put("tables", detail
                ? schema.getAllTables().stream().filter(each -> TableType.TABLE == each.getType()).sorted(Comparator.comparing(ShardingSphereTable::getName))
                        .map(each -> createTablePayload(database, schema.getName(), each, false)).toList()
                : List.of());
        result.put("views", detail
                ? schema.getAllTables().stream().filter(each -> TableType.VIEW == each.getType()).sorted(Comparator.comparing(ShardingSphereTable::getName))
                        .map(each -> createViewPayload(database, schema.getName(), each, false)).toList()
                : List.of());
        result.put("sequences", detail
                ? schema.getAllSequences().stream().sorted(Comparator.comparing(ShardingSphereSequence::getName)).map(each -> createSequencePayload(database, schema.getName(), each)).toList()
                : List.of());
        return result;
    }
    
    private Map<String, Object> createTablePayload(final String database, final String schema, final ShardingSphereTable table, final boolean detail) {
        Map<String, Object> result = new LinkedHashMap<>(5, 1F);
        result.put("database", database);
        result.put("schema", schema);
        result.put("table", table.getName());
        result.put("columns", detail
                ? metadataQueryFacade.queryTableColumns(database, schema, table.getName()).stream()
                        .map(each -> createColumnPayload(database, schema, table.getName(), "", each)).toList()
                : List.of());
        result.put("indexes", detail
                ? metadataQueryFacade.queryIndexes(database, schema, table.getName()).stream().map(each -> createIndexPayload(database, schema, table.getName(), each)).toList()
                : List.of());
        return result;
    }
    
    private Map<String, Object> createViewPayload(final String database, final String schema, final ShardingSphereTable view, final boolean detail) {
        Map<String, Object> result = new LinkedHashMap<>(4, 1F);
        result.put("database", database);
        result.put("schema", schema);
        result.put("view", view.getName());
        result.put("columns", detail
                ? metadataQueryFacade.queryViewColumns(database, schema, view.getName()).stream()
                        .map(each -> createColumnPayload(database, schema, "", view.getName(), each)).toList()
                : List.of());
        return result;
    }
    
    private Map<String, Object> createColumnPayload(final MCPColumnMetadata column) {
        return createColumnPayload(getDatabase(), getSchema(), getVariable("table"), getVariable("view"), column);
    }
    
    private Map<String, Object> createColumnPayload(final String database, final String schema, final String table, final String view, final MCPColumnMetadata column) {
        Map<String, Object> result = new LinkedHashMap<>(9, 1F);
        result.put("database", database);
        result.put("schema", schema);
        result.put("table", table);
        result.put("view", view);
        result.put("column", column.getName());
        result.put("ordinalPosition", column.getOrdinalPosition());
        result.put("jdbcType", column.getJdbcType());
        result.put("nativeTypeName", column.getNativeTypeName());
        result.put("nullability", column.getNullability().getValue());
        return result;
    }
    
    private Map<String, Object> createIndexPayload(final ShardingSphereIndex index) {
        return createIndexPayload(getDatabase(), getSchema(), getVariable("table"), index);
    }
    
    private Map<String, Object> createIndexPayload(final String database, final String schema, final String table, final ShardingSphereIndex index) {
        Map<String, Object> result = new LinkedHashMap<>(6, 1F);
        result.put("database", database);
        result.put("schema", schema);
        result.put("table", table);
        result.put("index", index.getName());
        result.put("columns", index.getColumns());
        result.put("unique", index.isUnique());
        return result;
    }
    
    private Map<String, Object> createSequencePayload(final ShardingSphereSequence sequence) {
        return createSequencePayload(getDatabase(), getSchema(), sequence);
    }
    
    private Map<String, Object> createSequencePayload(final String database, final String schema, final ShardingSphereSequence sequence) {
        Map<String, Object> result = new LinkedHashMap<>(3, 1F);
        result.put("database", database);
        result.put("schema", schema);
        result.put("sequence", sequence.getName());
        return result;
    }
    
    private String getDatabase() {
        return getVariable("database");
    }
    
    private String getSchema() {
        return getVariable("schema");
    }
    
    private String getVariable(final String variableName) {
        return uriVariables.containsVariable(variableName) ? uriVariables.getValue(variableName) : "";
    }
}
