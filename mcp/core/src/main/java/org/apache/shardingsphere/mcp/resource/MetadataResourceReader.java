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

package org.apache.shardingsphere.mcp.resource;

import org.apache.shardingsphere.mcp.capability.DatabaseCapability;
import org.apache.shardingsphere.mcp.capability.MCPCapabilityBuilder;
import org.apache.shardingsphere.mcp.metadata.model.DatabaseMetadataSnapshots;
import org.apache.shardingsphere.mcp.metadata.model.MetadataObject;
import org.apache.shardingsphere.mcp.metadata.model.MetadataObjectType;
import org.apache.shardingsphere.mcp.protocol.MCPErrorCode;

import java.util.Collection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * Read normalized metadata resources for the MCP public object model.
 */
public final class MetadataResourceReader {
    
    /**
     * Read one metadata resource view from the supplied catalog.
     *
     * @param databaseMetadataSnapshots database metadata snapshots
     * @param metadataResourceQuery metadata resource query
     * @return metadata resource result
     */
    public MetadataResourceResult read(final DatabaseMetadataSnapshots databaseMetadataSnapshots, final MetadataResourceQuery metadataResourceQuery) {
        if (MetadataObjectType.DATABASE == metadataResourceQuery.getObjectType()) {
            return MetadataResourceResult.success(filterDatabases(databaseMetadataSnapshots, metadataResourceQuery));
        }
        Set<MetadataObjectType> supportedMetadataObjectTypes = getSupportedMetadataObjectTypes(databaseMetadataSnapshots, metadataResourceQuery.getDatabase());
        if (MetadataObjectType.INDEX == metadataResourceQuery.getObjectType() && !supportsObjectType(supportedMetadataObjectTypes, MetadataObjectType.INDEX)) {
            return MetadataResourceResult.error(MCPErrorCode.UNSUPPORTED, "Index resources are not supported for the current database.");
        }
        return MetadataResourceResult.success(filterMetadataObjects(databaseMetadataSnapshots, metadataResourceQuery, supportedMetadataObjectTypes));
    }
    
    private List<MetadataObject> filterDatabases(final DatabaseMetadataSnapshots databaseMetadataSnapshots, final MetadataResourceQuery metadataResourceQuery) {
        List<MetadataObject> result = new ArrayList<>(databaseMetadataSnapshots.getDatabaseTypes().size());
        for (String each : databaseMetadataSnapshots.getDatabaseTypes().keySet()) {
            if (metadataResourceQuery.getObjectName().isEmpty() || each.equals(metadataResourceQuery.getObjectName())) {
                result.add(new MetadataObject(each, "", MetadataObjectType.DATABASE, each, "", ""));
            }
        }
        return sortMetadataObjects(result);
    }
    
    private Set<MetadataObjectType> getSupportedMetadataObjectTypes(final DatabaseMetadataSnapshots databaseMetadataSnapshots, final String databaseName) {
        databaseMetadataSnapshots.findDatabaseType(databaseName).orElseThrow(() -> new IllegalStateException("Database does not exist."));
        return new MCPCapabilityBuilder(databaseMetadataSnapshots).buildDatabaseCapability(databaseName)
                .map(DatabaseCapability::getSupportedMetadataObjectTypes).orElseGet(Collections::emptySet);
    }
    
    private List<MetadataObject> filterMetadataObjects(final DatabaseMetadataSnapshots databaseMetadataSnapshots, final MetadataResourceQuery metadataResourceQuery,
                                                       final Set<MetadataObjectType> supportedMetadataObjectTypes) {
        List<MetadataObject> result = new ArrayList<>();
        for (MetadataObject each : databaseMetadataSnapshots.getMetadataObjects()) {
            if (!metadataResourceQuery.getDatabase().equals(each.getDatabase())) {
                continue;
            }
            if (!metadataResourceQuery.getSchema().isEmpty() && !metadataResourceQuery.getSchema().equals(each.getSchema())) {
                continue;
            }
            if (metadataResourceQuery.getObjectType() != each.getObjectType()) {
                continue;
            }
            if (!metadataResourceQuery.getObjectName().isEmpty() && !metadataResourceQuery.getObjectName().equals(each.getName())) {
                continue;
            }
            if (!metadataResourceQuery.getParentObjectType().isEmpty() && !metadataResourceQuery.getParentObjectType().equals(each.getParentObjectType())) {
                continue;
            }
            if (!metadataResourceQuery.getParentObjectName().isEmpty() && !metadataResourceQuery.getParentObjectName().equals(each.getParentObjectName())) {
                continue;
            }
            if (supportsObjectType(supportedMetadataObjectTypes, each.getObjectType())) {
                result.add(each);
            }
        }
        return sortMetadataObjects(result);
    }
    
    private boolean supportsObjectType(final Set<MetadataObjectType> supportedMetadataObjectTypes, final MetadataObjectType metadataObjectType) {
        return supportedMetadataObjectTypes.contains(metadataObjectType);
    }
    
    private List<MetadataObject> sortMetadataObjects(final Collection<MetadataObject> metadataObjects) {
        List<MetadataObject> result = new ArrayList<>(metadataObjects);
        result.sort(Comparator.comparing(MetadataObject::getDatabase)
                .thenComparing(MetadataObject::getSchema)
                .thenComparing(each -> each.getObjectType().name())
                .thenComparing(MetadataObject::getParentObjectName)
                .thenComparing(MetadataObject::getName));
        return result;
    }
}
