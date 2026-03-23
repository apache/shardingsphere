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

import lombok.AccessLevel;
import lombok.Getter;
import org.apache.shardingsphere.mcp.capability.DatabaseCapabilityAssembler;
import org.apache.shardingsphere.mcp.capability.DatabaseCapabilityAssembler.DatabaseCapabilityView;
import org.apache.shardingsphere.mcp.capability.DatabaseCapabilityRegistry.SupportedObjectType;
import org.apache.shardingsphere.mcp.protocol.ExecuteQueryResponse.ErrorCode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Load normalized metadata resources for the MCP public object model.
 */
public final class MetadataResourceLoader {
    
    private final DatabaseCapabilityAssembler capabilityAssembler;
    
    /**
     * Construct a metadata resource loader with the default capability assembler.
     */
    public MetadataResourceLoader() {
        this(new DatabaseCapabilityAssembler());
    }
    
    /**
     * Construct a metadata resource loader with a caller-provided capability assembler.
     *
     * @param capabilityAssembler capability assembler
     */
    public MetadataResourceLoader(final DatabaseCapabilityAssembler capabilityAssembler) {
        this.capabilityAssembler = Objects.requireNonNull(capabilityAssembler, "capabilityAssembler cannot be null");
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
        return Collections.unmodifiableList(result);
    }
    
    /**
     * Request contract for one metadata resource load.
     */
    @Getter
    public static final class ResourceRequest {
        
        private final String database;
        
        private final String schema;
        
        private final MetadataObjectType objectType;
        
        private final String objectName;
        
        private final String parentObjectType;
        
        private final String parentObjectName;
        
        /**
         * Construct a resource request.
         *
         * @param database logical database name or empty string
         * @param schema schema name or empty string
         * @param objectType target object type
         * @param objectName target object name or empty string
         * @param parentObjectType parent object type name or empty string
         * @param parentObjectName parent object name or empty string
         */
        public ResourceRequest(final String database, final String schema, final MetadataObjectType objectType,
                               final String objectName, final String parentObjectType, final String parentObjectName) {
            this.database = Objects.requireNonNull(database, "database cannot be null");
            this.schema = Objects.requireNonNull(schema, "schema cannot be null");
            this.objectType = Objects.requireNonNull(objectType, "objectType cannot be null");
            this.objectName = Objects.requireNonNull(objectName, "objectName cannot be null");
            this.parentObjectType = Objects.requireNonNull(parentObjectType, "parentObjectType cannot be null");
            this.parentObjectName = Objects.requireNonNull(parentObjectName, "parentObjectName cannot be null");
        }
    }
    
    /**
     * Loaded resource result for one request.
     */
    @Getter
    public static final class ResourceLoadResult {
        
        private final List<MetadataObject> metadataObjects;
        
        @Getter(AccessLevel.NONE)
        private final boolean errorCodePresent;
        
        @Getter(AccessLevel.NONE)
        private final ErrorCode errorCode;
        
        private final String message;
        
        private ResourceLoadResult(final Collection<MetadataObject> metadataObjects, final boolean errorCodePresent, final ErrorCode errorCode, final String message) {
            this.metadataObjects = Collections.unmodifiableList(new ArrayList<>(Objects.requireNonNull(metadataObjects, "metadataObjects cannot be null")));
            this.errorCodePresent = errorCodePresent;
            this.errorCode = Objects.requireNonNull(errorCode, "errorCode cannot be null");
            this.message = Objects.requireNonNull(message, "message cannot be null");
        }
        
        /**
         * Determine whether the load finished successfully.
         *
         * @return {@code true} when no error is attached
         */
        public boolean isSuccessful() {
            return !errorCodePresent;
        }
        
        /**
         * Create a successful resource load result.
         *
         * @param metadataObjects loaded metadata objects
         * @return successful resource load result
         */
        public static ResourceLoadResult success(final Collection<MetadataObject> metadataObjects) {
            return new ResourceLoadResult(metadataObjects, false, ErrorCode.INVALID_REQUEST, "");
        }
        
        /**
         * Create an error resource load result.
         *
         * @param errorCode unified error code
         * @param message error message
         * @return failed resource load result
         */
        public static ResourceLoadResult error(final ErrorCode errorCode, final String message) {
            return new ResourceLoadResult(Collections.emptyList(), true, Objects.requireNonNull(errorCode, "errorCode cannot be null"), message);
        }
        
        /**
         * Get the error code when one exists.
         *
         * @return optional error code
         */
        public Optional<ErrorCode> getErrorCode() {
            return errorCodePresent ? Optional.of(errorCode) : Optional.empty();
        }
    }
    
    /**
     * Immutable in-memory metadata catalog for discovery tests and adapters.
     */
    public static final class MetadataCatalog {
        
        private volatile Snapshot snapshot;
        
        /**
         * Construct a metadata catalog.
         *
         * @param databaseTypes database-to-type mapping
         * @param metadataObjects metadata objects
         */
        public MetadataCatalog(final Map<String, String> databaseTypes, final Collection<MetadataObject> metadataObjects) {
            this(databaseTypes, metadataObjects, Collections.emptyMap());
        }
        
        /**
         * Construct a metadata catalog with runtime descriptors.
         *
         * @param databaseTypes database-to-type mapping
         * @param metadataObjects metadata objects
         * @param runtimeDatabaseDescriptors runtime database descriptors
         */
        public MetadataCatalog(final Map<String, String> databaseTypes, final Collection<MetadataObject> metadataObjects,
                               final Map<String, RuntimeDatabaseDescriptor> runtimeDatabaseDescriptors) {
            replaceSnapshot(databaseTypes, metadataObjects, runtimeDatabaseDescriptors);
        }
        
        /**
         * Get the current database-to-type mapping.
         *
         * @return database-to-type mapping
         */
        public Map<String, String> getDatabaseTypes() {
            return snapshot.getDatabaseTypes();
        }
        
        /**
         * Get the current metadata objects.
         *
         * @return metadata objects
         */
        public List<MetadataObject> getMetadataObjects() {
            return snapshot.getMetadataObjects();
        }
        
        /**
         * Get the current runtime database descriptors.
         *
         * @return runtime database descriptors
         */
        public Map<String, RuntimeDatabaseDescriptor> getRuntimeDatabaseDescriptors() {
            return snapshot.getRuntimeDatabaseDescriptors();
        }
        
        /**
         * Replace the runtime metadata snapshot.
         *
         * @param databaseTypes database-to-type mapping
         * @param metadataObjects metadata objects
         * @param runtimeDatabaseDescriptors runtime database descriptors
         */
        public void replaceSnapshot(final Map<String, String> databaseTypes, final Collection<MetadataObject> metadataObjects,
                                    final Map<String, RuntimeDatabaseDescriptor> runtimeDatabaseDescriptors) {
            snapshot = new Snapshot(databaseTypes, metadataObjects, runtimeDatabaseDescriptors);
        }
        
        Optional<String> findDatabaseType(final String database) {
            return Optional.ofNullable(snapshot.getDatabaseTypes().get(database));
        }
        
        /**
         * Find one runtime database descriptor.
         *
         * @param database logical database name
         * @return runtime database descriptor when present
         */
        public Optional<RuntimeDatabaseDescriptor> findRuntimeDatabaseDescriptor(final String database) {
            return Optional.ofNullable(snapshot.getRuntimeDatabaseDescriptors().get(database));
        }
        
        @Getter
        private static final class Snapshot {
            
            private final Map<String, String> databaseTypes;
            
            private final List<MetadataObject> metadataObjects;
            
            private final Map<String, RuntimeDatabaseDescriptor> runtimeDatabaseDescriptors;
            
            private Snapshot(final Map<String, String> databaseTypes, final Collection<MetadataObject> metadataObjects,
                             final Map<String, RuntimeDatabaseDescriptor> runtimeDatabaseDescriptors) {
                this.databaseTypes = Collections.unmodifiableMap(new LinkedHashMap<>(Objects.requireNonNull(databaseTypes, "databaseTypes cannot be null")));
                this.metadataObjects = Collections.unmodifiableList(new LinkedList<>(Objects.requireNonNull(metadataObjects, "metadataObjects cannot be null")));
                this.runtimeDatabaseDescriptors = Collections.unmodifiableMap(
                        new LinkedHashMap<>(Objects.requireNonNull(runtimeDatabaseDescriptors, "runtimeDatabaseDescriptors cannot be null")));
            }
        }
    }
    
    /**
     * Runtime metadata facts for one logical database.
     */
    @Getter
    public static final class RuntimeDatabaseDescriptor {
        
        private final String database;
        
        private final String databaseType;
        
        private final Set<SupportedObjectType> supportedObjectTypes;
        
        private final String defaultSchema;
        
        private final boolean supportsCrossSchemaSql;
        
        private final boolean supportsExplainAnalyze;
        
        /**
         * Construct one runtime database descriptor.
         *
         * @param database logical database name
         * @param databaseType database type
         * @param supportedObjectTypes supported object types
         * @param defaultSchema default schema
         * @param supportsCrossSchemaSql cross-schema SQL support flag
         * @param supportsExplainAnalyze explain analyze support flag
         */
        public RuntimeDatabaseDescriptor(final String database, final String databaseType, final Collection<SupportedObjectType> supportedObjectTypes,
                                         final String defaultSchema, final boolean supportsCrossSchemaSql, final boolean supportsExplainAnalyze) {
            this.database = Objects.requireNonNull(database, "database cannot be null");
            this.databaseType = Objects.requireNonNull(databaseType, "databaseType cannot be null");
            this.supportedObjectTypes = Collections.unmodifiableSet(new java.util.LinkedHashSet<>(
                    Objects.requireNonNull(supportedObjectTypes, "supportedObjectTypes cannot be null")));
            this.defaultSchema = Objects.requireNonNull(defaultSchema, "defaultSchema cannot be null");
            this.supportsCrossSchemaSql = supportsCrossSchemaSql;
            this.supportsExplainAnalyze = supportsExplainAnalyze;
        }
    }
    
    /**
     * Normalized metadata object for metadata discovery.
     */
    @Getter
    public static final class MetadataObject {
        
        private final String database;
        
        private final String schema;
        
        private final MetadataObjectType objectType;
        
        private final String name;
        
        private final String parentObjectType;
        
        private final String parentObjectName;
        
        /**
         * Construct a normalized metadata object.
         *
         * @param database logical database name
         * @param schema schema name or empty string
         * @param objectType normalized metadata object type
         * @param name object name
         * @param parentObjectType parent object type name or empty string
         * @param parentObjectName parent object name or empty string
         */
        public MetadataObject(final String database, final String schema, final MetadataObjectType objectType,
                              final String name, final String parentObjectType, final String parentObjectName) {
            this.database = Objects.requireNonNull(database, "database cannot be null");
            this.schema = Objects.requireNonNull(schema, "schema cannot be null");
            this.objectType = Objects.requireNonNull(objectType, "objectType cannot be null");
            this.name = Objects.requireNonNull(name, "name cannot be null");
            this.parentObjectType = Objects.requireNonNull(parentObjectType, "parentObjectType cannot be null");
            this.parentObjectName = Objects.requireNonNull(parentObjectName, "parentObjectName cannot be null");
        }
    }
    
    /**
     * Supported normalized and excluded metadata object types used by discovery.
     */
    public enum MetadataObjectType {
        
        DATABASE, SCHEMA, TABLE, VIEW, COLUMN, INDEX, MATERIALIZED_VIEW, SEQUENCE, ROUTINE, TRIGGER, EVENT, SYNONYM, DATABASE_SPECIFIC
    }
}
