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

import java.util.Map;
import java.util.Optional;

/**
 * Database metadata snapshots.
 */
@Getter
public final class DatabaseMetadataSnapshots {
    
    private final Map<String, DatabaseMetadataSnapshot> databaseSnapshots;
    
    public DatabaseMetadataSnapshots(final Map<String, DatabaseMetadataSnapshot> databaseSnapshots) {
        this.databaseSnapshots = databaseSnapshots;
    }
    
    /**
     * Find database type.
     *
     * @param databaseName database name
     * @return found database type
     */
    public Optional<String> findDatabaseType(final String databaseName) {
        return findDatabaseMetadata(databaseName).map(MCPDatabaseMetadata::getDatabaseType);
    }
    
    /**
     * Find database metadata.
     *
     * @param databaseName database name
     * @return found database metadata
     */
    public Optional<MCPDatabaseMetadata> findDatabaseMetadata(final String databaseName) {
        return findSnapshot(databaseName).map(DatabaseMetadataSnapshot::getDatabaseMetadata);
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
    }
}
