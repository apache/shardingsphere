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

package org.apache.shardingsphere.mode.node.path.metadata;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Data source meta data node path.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DataSourceMetaDataNodePath {
    
    private static final String ROOT_NODE = "/metadata";
    
    private static final String DATA_SOURCES_NODE = "data_sources";
    
    private static final String NODES_NODE = "nodes";
    
    private static final String UNITS_NODE = "units";
    
    private static final String VERSIONS_NODE = "versions";
    
    private static final String ACTIVE_VERSION_NODE = "active_version";
    
    private static final String IDENTIFIER_PATTERN = "([\\w\\-]+)";
    
    /**
     * Get data source root path.
     *
     * @param databaseName database name
     * @return data source root path
     */
    public static String getDataSourceRootPath(final String databaseName) {
        return String.join("/", ROOT_NODE, databaseName, DATA_SOURCES_NODE);
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
     * Get storage unit version path.
     *
     * @param databaseName database name
     * @param storageUnitName storage unit name
     * @param version version
     * @return storage unit version path
     */
    public static String getStorageUnitVersionPath(final String databaseName, final String storageUnitName, final String version) {
        return String.join("/", getStorageUnitVersionsPath(databaseName, storageUnitName), version);
    }
    
    /**
     * Get storage unit versions path.
     *
     * @param databaseName database name
     * @param dataSourceName storage unit name
     * @return storage unit versions path
     */
    public static String getStorageUnitVersionsPath(final String databaseName, final String dataSourceName) {
        return String.join("/", getStorageUnitsPath(databaseName), dataSourceName, VERSIONS_NODE);
    }
    
    /**
     * Get storage unit active version path.
     *
     * @param databaseName database name
     * @param dataSourceName storage unit name
     * @return storage unit active version path
     */
    public static String getStorageUnitActiveVersionPath(final String databaseName, final String dataSourceName) {
        return String.join("/", getStorageUnitsPath(databaseName), dataSourceName, ACTIVE_VERSION_NODE);
    }
    
    /**
     * Get storage node versions path.
     *
     * @param databaseName database name
     * @param storageNodeName storage node name
     * @return storage node versions path
     */
    public static String getStorageNodeVersionsPath(final String databaseName, final String storageNodeName) {
        return String.join("/", getStorageNodesPath(databaseName), storageNodeName, VERSIONS_NODE);
    }
    
    /**
     * Get storage node version path.
     *
     * @param databaseName database name
     * @param storageNodeName storage node name
     * @param version version
     * @return storage node version path
     */
    public static String getStorageNodeVersionPath(final String databaseName, final String storageNodeName, final String version) {
        return String.join("/", getStorageNodeVersionsPath(databaseName, storageNodeName), version);
    }
    
    /**
     * Get storage node active version path.
     *
     * @param databaseName database name
     * @param storageNodeName storage node name
     * @return storage node active version path
     */
    public static String getStorageNodeActiveVersionPath(final String databaseName, final String storageNodeName) {
        return String.join("/", getStorageNodesPath(databaseName), storageNodeName, ACTIVE_VERSION_NODE);
    }
    
    /**
     * Find storage unit name by active version path.
     *
     * @param path path
     * @return found storage unit name
     */
    public static Optional<String> findStorageUnitNameByActiveVersionPath(final String path) {
        Pattern pattern = Pattern.compile(getStorageUnitActiveVersionPath(IDENTIFIER_PATTERN, IDENTIFIER_PATTERN), Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(path);
        return matcher.find() ? Optional.of(matcher.group(2)) : Optional.empty();
    }
    
    /**
     * Find storage unit name by storage unit path.
     *
     * @param path path
     * @return found storage unit name
     */
    public static Optional<String> findStorageUnitNameByStorageUnitPath(final String path) {
        Pattern pattern = Pattern.compile(getStorageUnitPath(IDENTIFIER_PATTERN, IDENTIFIER_PATTERN) + "$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(path);
        return matcher.find() ? Optional.of(matcher.group(2)) : Optional.empty();
    }
    
    /**
     * Find storage node name by active version path.
     *
     * @param path path
     * @return found storage unit name
     */
    public static Optional<String> findStorageNodeNameByActiveVersionPath(final String path) {
        Pattern pattern = Pattern.compile(getStorageNodeActiveVersionPath(IDENTIFIER_PATTERN, IDENTIFIER_PATTERN), Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(path);
        return matcher.find() ? Optional.of(matcher.group(2)) : Optional.empty();
    }
    
    /**
     * Find storage node name by storage node path.
     *
     * @param path path
     * @return found storage unit name
     */
    public static Optional<String> findStorageNodeNameByStorageNodePath(final String path) {
        Pattern pattern = Pattern.compile(getStorageNodePath(IDENTIFIER_PATTERN, IDENTIFIER_PATTERN) + "$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(path);
        return matcher.find() ? Optional.of(matcher.group(2)) : Optional.empty();
    }
    
    /**
     * Is data source root path.
     *
     * @param path path
     * @return true or false
     */
    public static boolean isDataSourceRootPath(final String path) {
        return Pattern.compile(getDataSourceRootPath(IDENTIFIER_PATTERN) + "?", Pattern.CASE_INSENSITIVE).matcher(path).find();
    }
}
