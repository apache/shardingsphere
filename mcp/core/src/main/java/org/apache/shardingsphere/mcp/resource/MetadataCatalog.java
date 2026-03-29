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

import lombok.Getter;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Immutable in-memory metadata catalog for discovery tests and adapters.
 */
public final class MetadataCatalog {
    
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
        snapshot = new Snapshot(Collections.unmodifiableMap(new LinkedHashMap<>(databaseTypes)),
                Collections.unmodifiableList(new LinkedList<>(metadataObjects)),
                Collections.unmodifiableMap(new LinkedHashMap<>(runtimeDatabaseDescriptors)));
    }
    
    /**
     * Replace the runtime metadata snapshot for one logical database.
     *
     * @param databaseName logical database name
     * @param databaseType database type
     * @param metadataObjects metadata objects for the logical database
     * @param runtimeDatabaseDescriptor runtime database descriptor
     */
    public void replaceDatabaseSnapshot(final String databaseName, final String databaseType, final Collection<MetadataObject> metadataObjects,
                                        final RuntimeDatabaseDescriptor runtimeDatabaseDescriptor) {
        Map<String, String> databaseTypes = new LinkedHashMap<>(snapshot.getDatabaseTypes());
        databaseTypes.put(databaseName, databaseType);
        LinkedList<MetadataObject> actualMetadataObjects = new LinkedList<>();
        for (MetadataObject each : snapshot.getMetadataObjects()) {
            if (!databaseName.equals(each.getDatabase())) {
                actualMetadataObjects.add(each);
            }
        }
        for (MetadataObject each : metadataObjects) {
            if (databaseName.equals(each.getDatabase())) {
                actualMetadataObjects.add(each);
            }
        }
        Map<String, RuntimeDatabaseDescriptor> runtimeDatabaseDescriptors = new LinkedHashMap<>(snapshot.getRuntimeDatabaseDescriptors());
        runtimeDatabaseDescriptors.put(databaseName, runtimeDatabaseDescriptor);
        replaceSnapshot(databaseTypes, actualMetadataObjects, runtimeDatabaseDescriptors);
    }
    
    /**
     * Find one database type.
     *
     * @param databaseName logical database name
     * @return database type when present
     */
    public Optional<String> findDatabaseType(final String databaseName) {
        return Optional.ofNullable(snapshot.getDatabaseTypes().get(databaseName));
    }
    
    /**
     * Find one runtime database descriptor.
     *
     * @param databaseName logical database name
     * @return runtime database descriptor when present
     */
    public Optional<RuntimeDatabaseDescriptor> findRuntimeDatabaseDescriptor(final String databaseName) {
        return Optional.ofNullable(snapshot.getRuntimeDatabaseDescriptors().get(databaseName));
    }
    
    @Getter
    private static final class Snapshot {
        
        private final Map<String, String> databaseTypes;
        
        private final List<MetadataObject> metadataObjects;
        
        private final Map<String, RuntimeDatabaseDescriptor> runtimeDatabaseDescriptors;
        
        private Snapshot(final Map<String, String> databaseTypes, final List<MetadataObject> metadataObjects,
                         final Map<String, RuntimeDatabaseDescriptor> runtimeDatabaseDescriptors) {
            this.databaseTypes = databaseTypes;
            this.metadataObjects = metadataObjects;
            this.runtimeDatabaseDescriptors = runtimeDatabaseDescriptors;
        }
    }
}
