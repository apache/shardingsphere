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
 * Data source meta data node.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DataSourceMetaDataNode {
    
    private static final String ROOT_NODE = "metadata";
    
    private static final String DATA_SOURCES_NODE = "data_sources";
    
    private static final String DATA_SOURCE_NODES_NODE = "nodes";
    
    private static final String DATA_SOURCE_UNITS_NODE = "units";
    
    private static final String ACTIVE_VERSION = "active_version";
    
    private static final String VERSIONS = "versions";
    
    private static final String DATABASE_DATA_SOURCES_NODE = "/([\\w\\-]+)/data_sources/";
    
    private static final String ACTIVE_VERSION_SUFFIX = "/([\\w\\-]+)/active_version";
    
    private static final String DATA_SOURCE_SUFFIX = "/([\\w\\-]+)$";
    
    /**
     * Get data source units node.
     *
     * @param databaseName database name
     * @return data sources node
     */
    public static String getDataSourceUnitsNode(final String databaseName) {
        return String.join("/", getMetaDataNode(), databaseName, DATA_SOURCES_NODE, DATA_SOURCE_UNITS_NODE);
    }
    
    /**
     * Get data source nodes node.
     *
     * @param databaseName database name
     * @return data sources node
     */
    public static String getDataSourceNodesNode(final String databaseName) {
        return String.join("/", getMetaDataNode(), databaseName, DATA_SOURCES_NODE, DATA_SOURCE_NODES_NODE);
    }
    
    /**
     * Get data source unit node.
     *
     * @param databaseName database name
     * @param dataSourceName data source name
     * @return data source node
     */
    public static String getDataSourceUnitNode(final String databaseName, final String dataSourceName) {
        return String.join("/", getDataSourceUnitsNode(databaseName), dataSourceName);
    }
    
    /**
     * Get data source node node.
     *
     * @param databaseName database name
     * @param dataSourceName data source name
     * @return data source node
     */
    public static String getDataSourceNodeNode(final String databaseName, final String dataSourceName) {
        return String.join("/", getDataSourceNodesNode(databaseName), dataSourceName);
    }
    
    /**
     * Get data source unit version node.
     *
     * @param databaseName database name
     * @param dataSourceName data source name
     * @param version version
     * @return data source unit version node
     */
    public static String getDataSourceUnitVersionNode(final String databaseName, final String dataSourceName, final String version) {
        return String.join("/", getDataSourceUnitVersionsNode(databaseName, dataSourceName), version);
    }
    
    /**
     * Get data source unit versions node.
     *
     * @param databaseName database name
     * @param dataSourceName data source name
     * @return data source versions node
     */
    public static String getDataSourceUnitVersionsNode(final String databaseName, final String dataSourceName) {
        return String.join("/", getDataSourceUnitsNode(databaseName), dataSourceName, VERSIONS);
    }
    
    /**
     * Get data source unit active version node.
     *
     * @param databaseName database name
     * @param dataSourceName data source name
     * @return data source active version node
     */
    public static String getDataSourceUnitActiveVersionNode(final String databaseName, final String dataSourceName) {
        return String.join("/", getDataSourceUnitsNode(databaseName), dataSourceName, ACTIVE_VERSION);
    }
    
    /**
     * Get data source node versions node.
     *
     * @param databaseName database name
     * @param dataSourceName data source name
     * @return data source versions node
     */
    public static String getDataSourceNodeVersionsNode(final String databaseName, final String dataSourceName) {
        return String.join("/", getDataSourceNodesNode(databaseName), dataSourceName, VERSIONS);
    }
    
    /**
     * Get data source node version node.
     *
     * @param databaseName database name
     * @param dataSourceName data source name
     * @param version version
     * @return data source node version node
     */
    public static String getDataSourceNodeVersionNode(final String databaseName, final String dataSourceName, final String version) {
        return String.join("/", getDataSourceNodeVersionsNode(databaseName, dataSourceName), version);
    }
    
    /**
     * Get data source node active version node.
     *
     * @param databaseName database name
     * @param dataSourceName data source name
     * @return data source active version node
     */
    public static String getDataSourceNodeActiveVersionNode(final String databaseName, final String dataSourceName) {
        return String.join("/", getDataSourceNodesNode(databaseName), dataSourceName, ACTIVE_VERSION);
    }
    
    /**
     * Is data sources node.
     *
     * @param path path
     * @return true or false
     */
    public static boolean isDataSourcesNode(final String path) {
        return Pattern.compile(getMetaDataNode() + DATABASE_DATA_SOURCES_NODE + "?", Pattern.CASE_INSENSITIVE).matcher(path).find();
    }
    
    /**
     * Get data source name by data source unit active version node.
     *
     * @param path path
     * @return data source name
     */
    public static Optional<String> getDataSourceNameByDataSourceUnitActiveVersionNode(final String path) {
        Pattern pattern = Pattern.compile(getMetaDataNode() + DATABASE_DATA_SOURCES_NODE + DATA_SOURCE_UNITS_NODE + ACTIVE_VERSION_SUFFIX, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(path);
        return matcher.find() ? Optional.of(matcher.group(2)) : Optional.empty();
    }
    
    /**
     * Get data source name by data source unit node.
     *
     * @param path path
     * @return data source name
     */
    public static Optional<String> getDataSourceNameByDataSourceUnitNode(final String path) {
        Pattern pattern = Pattern.compile(getMetaDataNode() + DATABASE_DATA_SOURCES_NODE + DATA_SOURCE_UNITS_NODE + DATA_SOURCE_SUFFIX, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(path);
        return matcher.find() ? Optional.of(matcher.group(2)) : Optional.empty();
    }
    
    /**
     * Get data source name by data source node active version node.
     *
     * @param path path
     * @return data source name
     */
    public static Optional<String> getDataSourceNameByDataSourceNodeActiveVersionNode(final String path) {
        Pattern pattern = Pattern.compile(getMetaDataNode() + DATABASE_DATA_SOURCES_NODE + DATA_SOURCE_NODES_NODE + ACTIVE_VERSION_SUFFIX, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(path);
        return matcher.find() ? Optional.of(matcher.group(2)) : Optional.empty();
    }
    
    /**
     * Get data source name by data source node.
     *
     * @param path path
     * @return data source name
     */
    public static Optional<String> getDataSourceNameByDataSourceNodeNode(final String path) {
        Pattern pattern = Pattern.compile(getMetaDataNode() + DATABASE_DATA_SOURCES_NODE + DATA_SOURCE_NODES_NODE + DATA_SOURCE_SUFFIX, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(path);
        return matcher.find() ? Optional.of(matcher.group(2)) : Optional.empty();
    }
    
    /**
     * Is data source unit active version node.
     *
     * @param path path
     * @return true or false
     */
    public static boolean isDataSourceUnitActiveVersionNode(final String path) {
        return Pattern.compile(getMetaDataNode() + DATABASE_DATA_SOURCES_NODE + DATA_SOURCE_UNITS_NODE + ACTIVE_VERSION_SUFFIX, Pattern.CASE_INSENSITIVE).matcher(path).find();
    }
    
    /**
     * Is data source unit node.
     *
     * @param path path
     * @return true or false
     */
    public static boolean isDataSourceUnitNode(final String path) {
        return Pattern.compile(getMetaDataNode() + DATABASE_DATA_SOURCES_NODE + DATA_SOURCE_UNITS_NODE + DATA_SOURCE_SUFFIX, Pattern.CASE_INSENSITIVE).matcher(path).find();
    }
    
    /**
     * Is data source node active version node.
     *
     * @param path path
     * @return true or false
     */
    public static boolean isDataSourceNodeActiveVersionNode(final String path) {
        return Pattern.compile(getMetaDataNode() + DATABASE_DATA_SOURCES_NODE + DATA_SOURCE_NODES_NODE + ACTIVE_VERSION_SUFFIX, Pattern.CASE_INSENSITIVE).matcher(path).find();
    }
    
    /**
     * Is data source node node.
     *
     * @param path path
     * @return true or false
     */
    public static boolean isDataSourceNodeNode(final String path) {
        return Pattern.compile(getMetaDataNode() + DATABASE_DATA_SOURCES_NODE + DATA_SOURCE_NODES_NODE + DATA_SOURCE_SUFFIX, Pattern.CASE_INSENSITIVE).matcher(path).find();
    }
    
    private static String getMetaDataNode() {
        return String.join("/", "", ROOT_NODE);
    }
}
