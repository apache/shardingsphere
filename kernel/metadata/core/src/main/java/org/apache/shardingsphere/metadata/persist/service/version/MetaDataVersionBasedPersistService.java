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

package org.apache.shardingsphere.metadata.persist.service.version;

import java.util.Optional;

/**
 * TODO replace the old implementation after meta data refactor completed
 * Meta data version based registry service.
 */
public interface MetaDataVersionBasedPersistService {
    
    /**
     * Get active version.
     *
     * @param databaseName database name
     * @return active database version
     */
    Optional<String> getActiveVersion(String databaseName);
    
    /**
     * Judge whether active version.
     *
     * @param databaseName database name
     * @param version version
     * @return is active version or not
     */
    boolean isActiveVersion(String databaseName, String version);
    
    /**
     * Create new schema version.
     *
     * @param databaseName database name
     * @return new version
     */
    Optional<String> createNewVersion(String databaseName);
    
    /**
     * Persist active database version.
     *
     * @param databaseName database name
     * @param version version
     */
    void persistActiveVersion(String databaseName, String version);
    
    /**
     * Delete database version.
     *
     * @param databaseName database name
     * @param version version
     */
    void deleteVersion(String databaseName, String version);
}
