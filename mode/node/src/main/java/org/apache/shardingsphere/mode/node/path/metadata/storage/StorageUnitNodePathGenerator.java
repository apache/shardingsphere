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

package org.apache.shardingsphere.mode.node.path.metadata.storage;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.mode.node.path.version.VersionNodePathGenerator;

/**
 * Storage unit node path generator.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class StorageUnitNodePathGenerator {
    
    private static final String UNITS_NODE = "units";
    
    /**
     * Get storage unit root path.
     *
     * @param databaseName database name
     * @return storage unit root path
     */
    public static String getRootPath(final String databaseName) {
        return String.join("/", DataSourceNodePathGenerator.getRootPath(databaseName), UNITS_NODE);
    }
    
    /**
     * Get storage unit path.
     *
     * @param databaseName database name
     * @param storageUnitName storage unit name
     * @return storage unit path
     */
    public static String getStorageUnitPath(final String databaseName, final String storageUnitName) {
        return String.join("/", getRootPath(databaseName), storageUnitName);
    }
    
    /**
     * Get storage unit version node path generator.
     *
     * @param databaseName database name
     * @param storageUnitName storage unit name
     * @return storage unit version node path generator
     */
    public static VersionNodePathGenerator getVersion(final String databaseName, final String storageUnitName) {
        return new VersionNodePathGenerator(String.join("/", getRootPath(databaseName), storageUnitName));
    }
}
