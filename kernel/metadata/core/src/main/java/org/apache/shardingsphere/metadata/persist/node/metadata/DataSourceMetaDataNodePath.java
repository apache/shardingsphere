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

package org.apache.shardingsphere.metadata.persist.node.metadata;

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
    
    private static final String DATA_SOURCE_NODES_NODE = "nodes";
    
    private static final String DATA_SOURCE_UNITS_NODE = "units";
    
    private static final String VERSIONS_NODE = "versions";
    
    private static final String ACTIVE_VERSION_NODE = "active_version";
    
    private static final String DATABASE_DATA_SOURCES_NODE = "/([\\w\\-]+)/data_sources/";
    
    private static final String ACTIVE_VERSION_SUFFIX = "/([\\w\\-]+)/active_version";
    
    private static final String DATA_SOURCE_SUFFIX = "/([\\w\\-]+)$";
    
    /**
     * Get data source units path.
     *
     * @param databaseName database name
     * @return data sources path
     */
    public static String getDataSourceUnitsPath(final String databaseName) {
        return String.join("/", ROOT_NODE, databaseName, DATA_SOURCES_NODE, DATA_SOURCE_UNITS_NODE);
    }
    
    /**
     * Get data source nodes path.
     *
     * @param databaseName database name
     * @return data sources path
     */
    public static String getDataSourceNodesPath(final String databaseName) {
        return String.join("/", ROOT_NODE, databaseName, DATA_SOURCES_NODE, DATA_SOURCE_NODES_NODE);
    }
    
    /**
     * Get data source unit path.
     *
     * @param databaseName database name
     * @param dataSourceName data source name
     * @return data source path
     */
    public static String getDataSourceUnitPath(final String databaseName, final String dataSourceName) {
        return String.join("/", getDataSourceUnitsPath(databaseName), dataSourceName);
    }
    
    /**
     * Get data source node path.
     *
     * @param databaseName database name
     * @param dataSourceName data source name
     * @return data source path
     */
    public static String getDataSourceNodePath(final String databaseName, final String dataSourceName) {
        return String.join("/", getDataSourceNodesPath(databaseName), dataSourceName);
    }
    
    /**
     * Get data source unit version path.
     *
     * @param databaseName database name
     * @param dataSourceName data source name
     * @param version version
     * @return data source unit version path
     */
    public static String getDataSourceUnitVersionPath(final String databaseName, final String dataSourceName, final String version) {
        return String.join("/", getDataSourceUnitVersionsPath(databaseName, dataSourceName), version);
    }
    
    /**
     * Get data source unit versions path.
     *
     * @param databaseName database name
     * @param dataSourceName data source name
     * @return data source versions path
     */
    public static String getDataSourceUnitVersionsPath(final String databaseName, final String dataSourceName) {
        return String.join("/", getDataSourceUnitsPath(databaseName), dataSourceName, VERSIONS_NODE);
    }
    
    /**
     * Get data source unit active version path.
     *
     * @param databaseName database name
     * @param dataSourceName data source name
     * @return data source active version path
     */
    public static String getDataSourceUnitActiveVersionPath(final String databaseName, final String dataSourceName) {
        return String.join("/", getDataSourceUnitsPath(databaseName), dataSourceName, ACTIVE_VERSION_NODE);
    }
    
    /**
     * Get data source node versions path.
     *
     * @param databaseName database name
     * @param dataSourceName data source name
     * @return data source versions path
     */
    public static String getDataSourceNodeVersionsPath(final String databaseName, final String dataSourceName) {
        return String.join("/", getDataSourceNodesPath(databaseName), dataSourceName, VERSIONS_NODE);
    }
    
    /**
     * Get data source node version path.
     *
     * @param databaseName database name
     * @param dataSourceName data source name
     * @param version version
     * @return data source node version path
     */
    public static String getDataSourceNodeVersionPath(final String databaseName, final String dataSourceName, final String version) {
        return String.join("/", getDataSourceNodeVersionsPath(databaseName, dataSourceName), version);
    }
    
    /**
     * Get data source node active version path.
     *
     * @param databaseName database name
     * @param dataSourceName data source name
     * @return data source active version path
     */
    public static String getDataSourceNodeActiveVersionPath(final String databaseName, final String dataSourceName) {
        return String.join("/", getDataSourceNodesPath(databaseName), dataSourceName, ACTIVE_VERSION_NODE);
    }
    
    /**
     * Is data sources path.
     *
     * @param path path
     * @return true or false
     */
    public static boolean isDataSourcesPath(final String path) {
        return Pattern.compile(ROOT_NODE + DATABASE_DATA_SOURCES_NODE + "?", Pattern.CASE_INSENSITIVE).matcher(path).find();
    }
    
    /**
     * Find data source name by data source unit active version path.
     *
     * @param path path
     * @return found data source name
     */
    public static Optional<String> findDataSourceNameByDataSourceUnitActiveVersionPath(final String path) {
        Pattern pattern = Pattern.compile(ROOT_NODE + DATABASE_DATA_SOURCES_NODE + DATA_SOURCE_UNITS_NODE + ACTIVE_VERSION_SUFFIX, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(path);
        return matcher.find() ? Optional.of(matcher.group(2)) : Optional.empty();
    }
    
    /**
     * Find data source name by data source unit path.
     *
     * @param path path
     * @return data source name
     */
    public static Optional<String> findDataSourceNameByDataSourceUnitPath(final String path) {
        Pattern pattern = Pattern.compile(ROOT_NODE + DATABASE_DATA_SOURCES_NODE + DATA_SOURCE_UNITS_NODE + DATA_SOURCE_SUFFIX, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(path);
        return matcher.find() ? Optional.of(matcher.group(2)) : Optional.empty();
    }
    
    /**
     * Find data source name by data source node active version path.
     *
     * @param path path
     * @return data source name
     */
    public static Optional<String> findDataSourceNameByDataSourceNodeActiveVersionPath(final String path) {
        Pattern pattern = Pattern.compile(ROOT_NODE + DATABASE_DATA_SOURCES_NODE + DATA_SOURCE_NODES_NODE + ACTIVE_VERSION_SUFFIX, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(path);
        return matcher.find() ? Optional.of(matcher.group(2)) : Optional.empty();
    }
    
    /**
     * Find data source name by data source path.
     *
     * @param path path
     * @return data source name
     */
    public static Optional<String> findDataSourceNameByDataSourceNodePath(final String path) {
        Pattern pattern = Pattern.compile(ROOT_NODE + DATABASE_DATA_SOURCES_NODE + DATA_SOURCE_NODES_NODE + DATA_SOURCE_SUFFIX, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(path);
        return matcher.find() ? Optional.of(matcher.group(2)) : Optional.empty();
    }
    
    /**
     * Is data source unit active version path.
     *
     * @param path path
     * @return true or false
     */
    public static boolean isDataSourceUnitActiveVersionPath(final String path) {
        return Pattern.compile(ROOT_NODE + DATABASE_DATA_SOURCES_NODE + DATA_SOURCE_UNITS_NODE + ACTIVE_VERSION_SUFFIX, Pattern.CASE_INSENSITIVE).matcher(path).find();
    }
    
    /**
     * Is data source unit path.
     *
     * @param path path
     * @return true or false
     */
    public static boolean isDataSourceUnitPath(final String path) {
        return Pattern.compile(ROOT_NODE + DATABASE_DATA_SOURCES_NODE + DATA_SOURCE_UNITS_NODE + DATA_SOURCE_SUFFIX, Pattern.CASE_INSENSITIVE).matcher(path).find();
    }
    
    /**
     * Is data source node active version path.
     *
     * @param path path
     * @return true or false
     */
    public static boolean isDataSourceNodeActiveVersionPath(final String path) {
        return Pattern.compile(ROOT_NODE + DATABASE_DATA_SOURCES_NODE + DATA_SOURCE_NODES_NODE + ACTIVE_VERSION_SUFFIX, Pattern.CASE_INSENSITIVE).matcher(path).find();
    }
    
    /**
     * Is data source node path.
     *
     * @param path path
     * @return true or false
     */
    public static boolean isDataSourceNodePath(final String path) {
        return Pattern.compile(ROOT_NODE + DATABASE_DATA_SOURCES_NODE + DATA_SOURCE_NODES_NODE + DATA_SOURCE_SUFFIX, Pattern.CASE_INSENSITIVE).matcher(path).find();
    }
}
