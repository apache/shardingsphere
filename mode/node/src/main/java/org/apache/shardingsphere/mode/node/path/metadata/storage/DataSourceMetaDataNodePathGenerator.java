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
import org.apache.shardingsphere.mode.node.path.metadata.DatabaseNodePathGenerator;
import org.apache.shardingsphere.mode.node.path.version.VersionNodePathGenerator;

/**
 * Data source meta data node path generator.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DataSourceMetaDataNodePathGenerator {
    
    private static final String DATA_SOURCES_NODE = "data_sources";
    
    private static final String NODES_NODE = "nodes";
    
    private static final String UNITS_NODE = "units";
    
    /**
     * Get data source root path.
     *
     * @param databaseName database name
     * @return data source root path
     */
    public static String getDataSourceRootPath(final String databaseName) {
        return String.join("/", DatabaseNodePathGenerator.getRootPath(), databaseName, DATA_SOURCES_NODE);
    }
    
    /**
     * Get storage units path.
     *
     * @param databaseName database name
     * @return storage units path
     */
    public static String getStorageUnitsPath(final String databaseName) {
        return String.join("/", getDataSourceRootPath(databaseName), UNITS_NODE);
    }
    
    /**
     * Get storage nodes path.
     *
     * @param databaseName database name
     * @return storage nodes path
     */
    public static String getStorageNodesPath(final String databaseName) {
        return String.join("/", getDataSourceRootPath(databaseName), NODES_NODE);
    }
    
    /**
     * Get storage unit path.
     *
     * @param databaseName database name
     * @param storageUnitName storage unit name
     * @return storage unit path
     */
    public static String getStorageUnitPath(final String databaseName, final String storageUnitName) {
        return String.join("/", getStorageUnitsPath(databaseName), storageUnitName);
    }
    
    /**
     * Get storage node path.
     *
     * @param databaseName database name
     * @param storageNodeName storage node name
     * @return storage node path
     */
    public static String getStorageNodePath(final String databaseName, final String storageNodeName) {
        return String.join("/", getStorageNodesPath(databaseName), storageNodeName);
    }
    
    /**
     * Get storage unit version node path generator.
     *
     * @param databaseName database name
     * @param storageUnitName storage unit name
     * @return storage unit version node path generator
     */
    public static VersionNodePathGenerator getStorageUnitVersion(final String databaseName, final String storageUnitName) {
        return new VersionNodePathGenerator(String.join("/", getStorageUnitsPath(databaseName), storageUnitName));
    }
    
    /**
     * Get storage node version node path generator.
     *
     * @param databaseName database name
     * @param storageNodeName storage node name
     * @return storage node version node path generator
     */
    public static VersionNodePathGenerator getStorageNodeVersion(final String databaseName, final String storageNodeName) {
        return new VersionNodePathGenerator(String.join("/", getStorageNodesPath(databaseName), storageNodeName));
    }
}
