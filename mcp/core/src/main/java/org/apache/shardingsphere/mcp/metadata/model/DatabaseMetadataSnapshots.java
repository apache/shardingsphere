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

package org.apache.shardingsphere.mcp.metadata.model;

import lombok.Getter;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

/**
 * Database metadata snapshots.
 */
@Getter
public final class DatabaseMetadataSnapshots {
    
    private final Map<String, DatabaseMetadataSnapshot> databaseSnapshots;
    
    private final Map<String, String> databaseTypes;
    
    private final List<MetadataObject> metadataObjects;
    
    public DatabaseMetadataSnapshots(final Map<String, DatabaseMetadataSnapshot> databaseSnapshots) {
        this.databaseSnapshots = databaseSnapshots;
        databaseTypes = new LinkedHashMap<>(databaseSnapshots.size(), 1F);
        metadataObjects = new LinkedList<>();
        for (Entry<String, DatabaseMetadataSnapshot> entry : databaseSnapshots.entrySet()) {
            databaseTypes.put(entry.getKey(), entry.getValue().getDatabaseType());
            metadataObjects.addAll(entry.getValue().getMetadataObjects());
        }
    }
    
    /**
     * Find database type.
     *
     * @param databaseName database name
     * @return found database type
     */
    public Optional<String> findDatabaseType(final String databaseName) {
        return Optional.ofNullable(databaseTypes.get(databaseName));
    }
    
    /**
     * Find database metadata snapshot.
     *
     * @param databaseName database name
     * @return found database metadata snapshot
     */
    public Optional<DatabaseMetadataSnapshot> findSnapshot(final String databaseName) {
        return Optional.ofNullable(databaseSnapshots.get(databaseName));
    }
    
    /**
     * Replace database metadata snapshot.
     *
     * @param databaseName database name
     * @param databaseSnapshot database metadata snapshot
     */
    public void replaceSnapshot(final String databaseName, final DatabaseMetadataSnapshot databaseSnapshot) {
        databaseSnapshots.put(databaseName, databaseSnapshot);
        databaseTypes.put(databaseName, databaseSnapshot.getDatabaseType());
        metadataObjects.clear();
        for (Entry<String, DatabaseMetadataSnapshot> entry : databaseSnapshots.entrySet()) {
            metadataObjects.addAll(entry.getValue().getMetadataObjects());
        }
    }
}
