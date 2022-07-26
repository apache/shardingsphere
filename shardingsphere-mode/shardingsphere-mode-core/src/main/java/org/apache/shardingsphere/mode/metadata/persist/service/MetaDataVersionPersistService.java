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
import org.apache.shardingsphere.mode.metadata.persist.node.DatabaseMetaDataNode;
import org.apache.shardingsphere.mode.persist.PersistRepository;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Meta data version persist service.
 */
@RequiredArgsConstructor
public final class MetaDataVersionPersistService {
    
    private final PersistRepository repository;
    
    /**
     * Get active version.
     * 
     * @param databaseName database name
     * @return active database version
     */
    public Optional<String> getActiveVersion(final String databaseName) {
        return Optional.ofNullable(repository.get(DatabaseMetaDataNode.getActiveVersionPath(databaseName)));
    }
    
    /**
     * Judge whether active version.
     * 
     * @param databaseName database name
     * @param version version
     * @return is active version or not
     */
    public boolean isActiveVersion(final String databaseName, final String version) {
        Optional<String> actualVersion = getActiveVersion(databaseName);
        return actualVersion.isPresent() && actualVersion.get().equals(version);
    }
    
    /**
     * Create new schema version.
     * 
     * @param databaseName database name
     * @return new version
     */
    public Optional<String> createNewVersion(final String databaseName) {
        Optional<String> activeVersion = getActiveVersion(databaseName);
        if (!activeVersion.isPresent()) {
            return Optional.empty();
        }
        String newVersion = String.valueOf(new AtomicLong(Long.parseLong(activeVersion.get())).incrementAndGet());
        repository.persist(DatabaseMetaDataNode.getRulePath(databaseName, newVersion), repository.get(DatabaseMetaDataNode.getRulePath(databaseName, activeVersion.get())));
        repository.persist(
                DatabaseMetaDataNode.getMetaDataDataSourcePath(databaseName, newVersion), repository.get(DatabaseMetaDataNode.getMetaDataDataSourcePath(databaseName, activeVersion.get())));
        return Optional.of(newVersion);
    }
    
    /**
     * Persist active database version.
     * 
     * @param databaseName database name
     * @param version version
     */
    public void persistActiveVersion(final String databaseName, final String version) {
        Optional<String> activeVersion = getActiveVersion(databaseName);
        if (activeVersion.isPresent() && !activeVersion.get().equals(version)) {
            repository.persist(DatabaseMetaDataNode.getActiveVersionPath(databaseName), version);
        }
    }
    
    /**
     * Delete database version.
     * 
     * @param databaseName database name
     * @param version version
     */
    public void deleteVersion(final String databaseName, final String version) {
        repository.delete(DatabaseMetaDataNode.getDatabaseVersionPath(databaseName, version));
    }
}
