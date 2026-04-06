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
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.Optional;

/**
 * Database metadata snapshots.
 */
@RequiredArgsConstructor
@Getter
public final class DatabaseMetadataSnapshots {
    
    private final Map<String, MCPDatabaseMetadata> databaseMetadataMap;
    
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
        return findMetadata(databaseName);
    }
    
    /**
     * Find database metadata.
     *
     * @param databaseName database name
     * @return found database metadata
     */
    public Optional<MCPDatabaseMetadata> findMetadata(final String databaseName) {
        return Optional.ofNullable(databaseMetadataMap.get(databaseName));
    }
    
    /**
     * Replace database metadata.
     *
     * @param databaseName database name
     * @param databaseMetadata database metadata
     */
    public void replaceMetadata(final String databaseName, final MCPDatabaseMetadata databaseMetadata) {
        databaseMetadataMap.put(databaseName, databaseMetadata);
    }
}
