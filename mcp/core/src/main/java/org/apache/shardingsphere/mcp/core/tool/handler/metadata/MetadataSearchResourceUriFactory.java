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

package org.apache.shardingsphere.mcp.core.tool.handler.metadata;

import org.apache.shardingsphere.mcp.support.protocol.MCPPayloadFieldNames;
import org.apache.shardingsphere.mcp.support.protocol.MCPResourceHintUtils;
import org.apache.shardingsphere.mcp.support.resource.MCPUriPathSegmentUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

final class MetadataSearchResourceUriFactory {
    
    private static final String DATABASES_RESOURCE_URI = "shardingsphere://databases";
    
    MetadataResourceUris create(final String database, final String schema, final String objectType, final String table, final String view, final String name) {
        if ("database".equals(objectType)) {
            return createDatabaseResourceUris(database);
        }
        if ("schema".equals(objectType)) {
            return createSchemaResourceUris(database, schema);
        }
        if ("table".equals(objectType)) {
            return createTableResourceUris(database, schema, table);
        }
        if ("view".equals(objectType)) {
            return createViewResourceUris(database, schema, view);
        }
        if ("column".equals(objectType)) {
            return createColumnResourceUris(database, schema, table, view, name);
        }
        if ("index".equals(objectType)) {
            return createIndexResourceUris(database, schema, table, name);
        }
        return "sequence".equals(objectType)
                ? createSequenceResourceUris(database, schema, name)
                : notSafe("Metadata hit object type is not backed by a descriptor resource pattern.");
    }
    
    private MetadataResourceUris createDatabaseResourceUris(final String database) {
        if (!canUseInUri(database)) {
            return notSafe("Metadata hit does not include a database name safe for resource URI derivation.");
        }
        String databaseUri = createResourceUri(database);
        return derived(databaseUri, DATABASES_RESOURCE_URI, List.of(createResourceUri(database, "capabilities"), createResourceUri(database, "schemas")));
    }
    
    private MetadataResourceUris createSchemaResourceUris(final String database, final String schema) {
        if (!canUseInUri(database, schema)) {
            return notSafe("Metadata hit does not include database and schema names safe for resource URI derivation.");
        }
        String schemaUri = createResourceUri(database, "schemas", schema);
        return derived(schemaUri, createResourceUri(database, "schemas"),
                List.of(createResourceUri(database, "schemas", schema, "tables"), createResourceUri(database, "schemas", schema, "views"),
                        createResourceUri(database, "schemas", schema, "sequences")));
    }
    
    private MetadataResourceUris createTableResourceUris(final String database, final String schema, final String table) {
        if (!canUseInUri(database, schema, table)) {
            return notSafe("Metadata hit does not include database, schema, and table names safe for resource URI derivation.");
        }
        String tableUri = createResourceUri(database, "schemas", schema, "tables", table);
        return derived(tableUri, createResourceUri(database, "schemas", schema, "tables"),
                List.of(createResourceUri(database, "schemas", schema, "tables", table, "columns"), createResourceUri(database, "schemas", schema, "tables", table, "indexes")));
    }
    
    private MetadataResourceUris createViewResourceUris(final String database, final String schema, final String view) {
        if (!canUseInUri(database, schema, view)) {
            return notSafe("Metadata hit does not include database, schema, and view names safe for resource URI derivation.");
        }
        String viewUri = createResourceUri(database, "schemas", schema, "views", view);
        return derived(viewUri, createResourceUri(database, "schemas", schema, "views"), List.of(createResourceUri(database, "schemas", schema, "views", view, "columns")));
    }
    
    private MetadataResourceUris createColumnResourceUris(final String database, final String schema, final String table, final String view, final String column) {
        if (canUseInUri(database, schema, table, column)) {
            return derived(createResourceUri(database, "schemas", schema, "tables", table, "columns", column),
                    createResourceUri(database, "schemas", schema, "tables", table, "columns"), List.of());
        }
        if (canUseInUri(database, schema, view, column)) {
            return derived(createResourceUri(database, "schemas", schema, "views", view, "columns", column),
                    createResourceUri(database, "schemas", schema, "views", view, "columns"), List.of());
        }
        return notSafe("Metadata hit does not include database, schema, parent table or view, and column names safe for resource URI derivation.");
    }
    
    private MetadataResourceUris createIndexResourceUris(final String database, final String schema, final String table, final String index) {
        if (!canUseInUri(database, schema, table, index)) {
            return notSafe("Metadata hit does not include database, schema, table, and index names safe for resource URI derivation.");
        }
        return derived(createResourceUri(database, "schemas", schema, "tables", table, "indexes", index),
                createResourceUri(database, "schemas", schema, "tables", table, "indexes"), List.of());
    }
    
    private MetadataResourceUris createSequenceResourceUris(final String database, final String schema, final String sequence) {
        if (!canUseInUri(database, schema, sequence)) {
            return notSafe("Metadata hit does not include database, schema, and sequence names safe for resource URI derivation.");
        }
        return derived(createResourceUri(database, "schemas", schema, "sequences", sequence), createResourceUri(database, "schemas", schema, "sequences"), List.of());
    }
    
    private MetadataResourceUris derived(final String resourceUri, final String parentResourceUri, final List<String> nextResourceUris) {
        return new MetadataResourceUris(MCPResourceHintUtils.create(resourceUri, resolveResourceKind(resourceUri), "inspect_detail", "Read the matched metadata detail resource.",
                MCPPayloadFieldNames.RESOURCE),
                MCPResourceHintUtils.create(parentResourceUri, resolveResourceKind(parentResourceUri), "inspect_parent", "Read the parent metadata resource.",
                        MCPPayloadFieldNames.PARENT_RESOURCE),
                nextResourceUris.stream().map(each -> MCPResourceHintUtils.create(each, resolveResourceKind(each), "inspect_detail", "Read a child metadata resource.",
                        MCPPayloadFieldNames.NEXT_RESOURCES)).toList(),
                "derived", "");
    }
    
    private MetadataResourceUris notSafe(final String reason) {
        return new MetadataResourceUris(Map.of(), Map.of(), List.of(), "not_safe_to_derive", reason);
    }
    
    private String resolveResourceKind(final String uri) {
        if (uri.contains("/columns")) {
            return "column";
        }
        if (uri.contains("/indexes")) {
            return "index";
        }
        if (uri.contains("/tables")) {
            return "table";
        }
        if (uri.contains("/views")) {
            return "view";
        }
        if (uri.contains("/sequences")) {
            return "sequence";
        }
        return uri.contains("/schemas") ? "schema" : "database";
    }
    
    private boolean canUseInUri(final String... values) {
        for (String each : values) {
            if (!canUsePathSegmentInUri(each)) {
                return false;
            }
        }
        return true;
    }
    
    private boolean canUsePathSegmentInUri(final String value) {
        return null != value && !value.isBlank();
    }
    
    private String createResourceUri(final String... pathSegments) {
        List<String> encodedSegments = new LinkedList<>();
        for (String each : pathSegments) {
            encodedSegments.add(MCPUriPathSegmentUtils.encodePathSegment(each));
        }
        return DATABASES_RESOURCE_URI + "/" + String.join("/", encodedSegments);
    }
    
    record MetadataResourceUris(Map<String, Object> resource, Map<String, Object> parentResource, List<Map<String, Object>> nextResources, String derivationStatus,
                                String derivationReason) {
    }
}
