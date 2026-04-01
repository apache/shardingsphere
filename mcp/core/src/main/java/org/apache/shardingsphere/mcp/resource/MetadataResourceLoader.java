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
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Load normalized metadata resources for the MCP public object model.
 */
public final class MetadataResourceLoader {
    
    /**
     * Load one metadata resource view from the supplied catalog.
     *
     * @param databaseMetadataSnapshots database metadata snapshots
     * @param resourceRequest resource request
     * @return loaded metadata resource result
     */
    public ResourceLoadResult load(final DatabaseMetadataSnapshots databaseMetadataSnapshots, final ResourceRequest resourceRequest) {
        if (MetadataObjectType.DATABASE == resourceRequest.getObjectType()) {
            return ResourceLoadResult.success(filterDatabases(databaseMetadataSnapshots, resourceRequest));
        }
        Set<MetadataObjectType> supportedMetadataObjectTypes = getSupportedMetadataObjectTypes(databaseMetadataSnapshots, resourceRequest.getDatabase());
        if (MetadataObjectType.INDEX == resourceRequest.getObjectType() && !supportsObjectType(supportedMetadataObjectTypes, MetadataObjectType.INDEX)) {
            return ResourceLoadResult.error(MCPErrorCode.UNSUPPORTED, "Index resources are not supported for the current database.");
        }
        return ResourceLoadResult.success(filterMetadataObjects(databaseMetadataSnapshots, resourceRequest, supportedMetadataObjectTypes));
    }
    
    private List<MetadataObject> filterDatabases(final DatabaseMetadataSnapshots databaseMetadataSnapshots, final ResourceRequest resourceRequest) {
        List<MetadataObject> result = new LinkedList<>();
        for (String each : databaseMetadataSnapshots.getDatabaseTypes().keySet()) {
            if (resourceRequest.getObjectName().isEmpty() || each.equals(resourceRequest.getObjectName())) {
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
    
    private List<MetadataObject> filterMetadataObjects(final DatabaseMetadataSnapshots databaseMetadataSnapshots, final ResourceRequest resourceRequest,
                                                       final Set<MetadataObjectType> supportedMetadataObjectTypes) {
        List<MetadataObject> result = new LinkedList<>();
        for (MetadataObject each : databaseMetadataSnapshots.getMetadataObjects()) {
            if (!resourceRequest.getDatabase().equals(each.getDatabase())) {
                continue;
            }
            if (!resourceRequest.getSchema().isEmpty() && !resourceRequest.getSchema().equals(each.getSchema())) {
                continue;
            }
            if (resourceRequest.getObjectType() != each.getObjectType()) {
                continue;
            }
            if (!resourceRequest.getObjectName().isEmpty() && !resourceRequest.getObjectName().equals(each.getName())) {
                continue;
            }
            if (!resourceRequest.getParentObjectType().isEmpty() && !resourceRequest.getParentObjectType().equals(each.getParentObjectType())) {
                continue;
            }
            if (!resourceRequest.getParentObjectName().isEmpty() && !resourceRequest.getParentObjectName().equals(each.getParentObjectName())) {
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
        List<MetadataObject> result = new LinkedList<>(metadataObjects);
        result.sort(Comparator.comparing(MetadataObject::getDatabase)
                .thenComparing(MetadataObject::getSchema)
                .thenComparing(each -> each.getObjectType().name())
                .thenComparing(MetadataObject::getParentObjectName)
                .thenComparing(MetadataObject::getName));
        return result;
    }
}
