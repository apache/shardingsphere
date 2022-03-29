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

package org.apache.shardingsphere.mode.metadata.persist.service;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.mode.metadata.persist.node.SchemaMetaDataNode;
import org.apache.shardingsphere.mode.persist.PersistRepository;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Schema version persist service.
 */
@RequiredArgsConstructor
public final class SchemaVersionPersistService {
    
    private final PersistRepository repository;
    
    /**
     * Get schema active version.
     * 
     * @param schemaName schema name
     * @return active version
     */
    public Optional<String> getSchemaActiveVersion(final String schemaName) {
        return Optional.ofNullable(repository.get(SchemaMetaDataNode.getActiveVersionPath(schemaName)));
    }
    
    /**
     * Verify the version is the active version.
     * 
     * @param schemaName schema name
     * @param version version
     * @return true if the version is active version, false if not
     */
    public boolean isActiveVersion(final String schemaName, final String version) {
        Optional<String> actualVersion = getSchemaActiveVersion(schemaName);
        return actualVersion.isPresent() && actualVersion.get().equals(version);
    }
    
    /**
     * Create new schema version.
     * 
     * @param schemaName schema name
     * @return new version
     */
    public Optional<String> createNewVersion(final String schemaName) {
        Optional<String> activeVersion = getSchemaActiveVersion(schemaName);
        if (activeVersion.isPresent()) {
            String newVersion = String.valueOf(new AtomicLong(Long.valueOf(activeVersion.get())).incrementAndGet());
            repository.persist(SchemaMetaDataNode.getRulePath(schemaName, newVersion), repository.get(SchemaMetaDataNode.getRulePath(schemaName, activeVersion.get())));
            repository.persist(SchemaMetaDataNode.getMetaDataDataSourcePath(schemaName, newVersion), repository.get(SchemaMetaDataNode.getMetaDataDataSourcePath(schemaName, activeVersion.get())));
            return Optional.of(newVersion);
        }
        return Optional.empty();
    }
    
    /**
     * Persist active schema version.
     * 
     * @param schemaName schema name
     * @param version version
     */
    public void persistActiveVersion(final String schemaName, final String version) {
        Optional<String> activeVersion = getSchemaActiveVersion(schemaName);
        if (activeVersion.isPresent() && !activeVersion.get().equals(version)) {
            repository.persist(SchemaMetaDataNode.getActiveVersionPath(schemaName), version);
        }
    }
    
    /**
     * Delete schema version.
     * 
     * @param schemaName schema name
     * @param version version
     */
    public void deleteVersion(final String schemaName, final String version) {
        repository.delete(SchemaMetaDataNode.getSchemaVersionPath(schemaName, version));
    }
}
