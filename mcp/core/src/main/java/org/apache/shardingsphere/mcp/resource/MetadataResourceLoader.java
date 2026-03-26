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

import org.apache.shardingsphere.mcp.capability.DatabaseCapabilityAssembler;
import org.apache.shardingsphere.mcp.capability.DatabaseCapabilityView;
import org.apache.shardingsphere.mcp.capability.SupportedObjectType;
import org.apache.shardingsphere.mcp.protocol.ErrorCode;

import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Load normalized metadata resources for the MCP public object model.
 */
public final class MetadataResourceLoader {
    
    private final DatabaseCapabilityAssembler capabilityAssembler;
    
    /**
     * Construct a metadata resource loader with a caller-provided capability assembler.
     *
     * @param capabilityAssembler capability assembler
     */
    public MetadataResourceLoader(final DatabaseCapabilityAssembler capabilityAssembler) {
        this.capabilityAssembler = capabilityAssembler;
    }
    
    /**
     * Load one metadata resource view from the supplied catalog.
     *
     * @param metadataCatalog metadata catalog
     * @param resourceRequest resource request
     * @return loaded metadata resource result
     */
    public ResourceLoadResult load(final MetadataCatalog metadataCatalog, final ResourceRequest resourceRequest) {
        MetadataCatalog actualMetadataCatalog = Objects.requireNonNull(metadataCatalog, "metadataCatalog cannot be null");
        ResourceRequest actualResourceRequest = Objects.requireNonNull(resourceRequest, "resourceRequest cannot be null");
        if (MetadataObjectType.DATABASE == actualResourceRequest.getObjectType()) {
            return ResourceLoadResult.success(filterDatabases(actualMetadataCatalog, actualResourceRequest));
        }
        String databaseType = actualMetadataCatalog.findDatabaseType(actualResourceRequest.getDatabase())
                .orElseThrow(() -> new IllegalStateException("Database does not exist."));
        if (MetadataObjectType.INDEX == actualResourceRequest.getObjectType() && !supportsObjectType(actualResourceRequest.getDatabase(), databaseType, SupportedObjectType.INDEX)) {
            return ResourceLoadResult.error(ErrorCode.UNSUPPORTED, "Index resources are not supported for the current database.");
        }
        return ResourceLoadResult.success(filterMetadataObjects(actualMetadataCatalog, actualResourceRequest, databaseType));
    }
    
    private List<MetadataObject> filterDatabases(final MetadataCatalog metadataCatalog, final ResourceRequest resourceRequest) {
        List<MetadataObject> result = new LinkedList<>();
        for (String each : metadataCatalog.getDatabaseTypes().keySet()) {
            if (resourceRequest.getObjectName().isEmpty() || each.equals(resourceRequest.getObjectName())) {
                result.add(new MetadataObject(each, "", MetadataObjectType.DATABASE, each, "", ""));
            }
        }
        return sortMetadataObjects(result);
    }
    
    private List<MetadataObject> filterMetadataObjects(final MetadataCatalog metadataCatalog, final ResourceRequest resourceRequest, final String databaseType) {
        List<MetadataObject> result = new LinkedList<>();
        for (MetadataObject each : metadataCatalog.getMetadataObjects()) {
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
            if (isVisiblePublicObject(resourceRequest.getDatabase(), databaseType, each)) {
                result.add(each);
            }
        }
        return sortMetadataObjects(result);
    }
    
    private boolean isVisiblePublicObject(final String database, final String databaseType, final MetadataObject metadataObject) {
        Optional<SupportedObjectType> supportedObjectType = toSupportedObjectType(metadataObject.getObjectType());
        return supportedObjectType.isPresent() && supportsObjectType(database, databaseType, supportedObjectType.get());
    }
    
    private boolean supportsObjectType(final String database, final String databaseType, final SupportedObjectType supportedObjectType) {
        Optional<DatabaseCapabilityView> databaseCapability = capabilityAssembler.assembleDatabaseCapability(database, databaseType);
        return databaseCapability.isPresent() && databaseCapability.get().getSupportedObjectTypes().contains(supportedObjectType);
    }
    
    private Optional<SupportedObjectType> toSupportedObjectType(final MetadataObjectType metadataObjectType) {
        switch (metadataObjectType) {
            case DATABASE:
                return Optional.of(SupportedObjectType.DATABASE);
            case SCHEMA:
                return Optional.of(SupportedObjectType.SCHEMA);
            case TABLE:
                return Optional.of(SupportedObjectType.TABLE);
            case VIEW:
                return Optional.of(SupportedObjectType.VIEW);
            case COLUMN:
                return Optional.of(SupportedObjectType.COLUMN);
            case INDEX:
                return Optional.of(SupportedObjectType.INDEX);
            default:
                return Optional.empty();
        }
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
