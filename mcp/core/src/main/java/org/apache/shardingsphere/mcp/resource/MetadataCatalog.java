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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;

/**
 * Immutable in-memory metadata catalog for discovery tests and adapters.
 */
public final class MetadataCatalog {
    
    private volatile Snapshot snapshot;
    
    /**
     * Construct a metadata catalog with per-database snapshots.
     *
     * @param databaseSnapshots per-database snapshots
     */
    public MetadataCatalog(final Map<String, DatabaseMetadataSnapshot> databaseSnapshots) {
        replaceSnapshot(databaseSnapshots);
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
     * Replace the metadata snapshot.
     *
     * @param databaseSnapshots per-database snapshots
     */
    public void replaceSnapshot(final Map<String, DatabaseMetadataSnapshot> databaseSnapshots) {
        snapshot = createSnapshot(databaseSnapshots);
    }
    
    /**
     * Replace the metadata snapshot for one logical database.
     *
     * @param databaseName logical database name
     * @param databaseSnapshot database metadata snapshot
     */
    public void replaceDatabaseSnapshot(final String databaseName, final DatabaseMetadataSnapshot databaseSnapshot) {
        Map<String, DatabaseMetadataSnapshot> databaseSnapshots = new LinkedHashMap<>(snapshot.getDatabaseSnapshots());
        databaseSnapshots.put(databaseName, databaseSnapshot);
        replaceSnapshot(databaseSnapshots);
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
     * Find one database metadata snapshot.
     *
     * @param databaseName logical database name
     * @return database metadata snapshot when present
     */
    public Optional<DatabaseMetadataSnapshot> findDatabaseSnapshot(final String databaseName) {
        return Optional.ofNullable(snapshot.getDatabaseSnapshots().get(databaseName));
    }
    
    private Snapshot createSnapshot(final Map<String, DatabaseMetadataSnapshot> databaseSnapshots) {
        Map<String, DatabaseMetadataSnapshot> actualDatabaseSnapshots = new LinkedHashMap<>(databaseSnapshots.size(), 1F);
        Map<String, String> databaseTypes = new LinkedHashMap<>(databaseSnapshots.size(), 1F);
        List<MetadataObject> metadataObjects = new LinkedList<>();
        for (Entry<String, DatabaseMetadataSnapshot> entry : databaseSnapshots.entrySet()) {
            DatabaseMetadataSnapshot databaseSnapshot = copySnapshot(entry.getValue());
            actualDatabaseSnapshots.put(entry.getKey(), databaseSnapshot);
            if (!databaseSnapshot.getDatabaseType().isEmpty()) {
                databaseTypes.put(entry.getKey(), databaseSnapshot.getDatabaseType());
            }
            metadataObjects.addAll(databaseSnapshot.getMetadataObjects());
        }
        return new Snapshot(Collections.unmodifiableMap(actualDatabaseSnapshots), Collections.unmodifiableMap(databaseTypes),
                Collections.unmodifiableList(new LinkedList<>(metadataObjects)));
    }
    
    private DatabaseMetadataSnapshot copySnapshot(final DatabaseMetadataSnapshot databaseSnapshot) {
        return new DatabaseMetadataSnapshot(databaseSnapshot.getDatabaseType(), databaseSnapshot.getMetadataObjects(), databaseSnapshot.getDatabaseVersion(), databaseSnapshot.getDefaultSchema());
    }
    
    @Getter
    private static final class Snapshot {
        
        private final Map<String, DatabaseMetadataSnapshot> databaseSnapshots;
        
        private final Map<String, String> databaseTypes;
        
        private final List<MetadataObject> metadataObjects;
        
        private Snapshot(final Map<String, DatabaseMetadataSnapshot> databaseSnapshots,
                         final Map<String, String> databaseTypes, final List<MetadataObject> metadataObjects) {
            this.databaseSnapshots = databaseSnapshots;
            this.databaseTypes = databaseTypes;
            this.metadataObjects = metadataObjects;
        }
    }
}
