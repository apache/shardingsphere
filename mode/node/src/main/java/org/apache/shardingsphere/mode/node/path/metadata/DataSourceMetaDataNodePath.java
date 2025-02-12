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
import org.apache.shardingsphere.mode.node.path.NodePathPattern;
import org.apache.shardingsphere.mode.node.path.version.VersionNodePathGenerator;
import org.apache.shardingsphere.mode.node.path.version.VersionNodePathParser;

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
    
    private static final VersionNodePathParser STORAGE_UNITS_PARSER =
            new VersionNodePathParser(String.join("/", getStorageUnitsPath(NodePathPattern.GROUPED_IDENTIFIER), NodePathPattern.GROUPED_IDENTIFIER));
    
    private static final VersionNodePathParser STORAGE_NODES_PARSER =
            new VersionNodePathParser(String.join("/", getStorageNodesPath(NodePathPattern.GROUPED_IDENTIFIER), NodePathPattern.GROUPED_IDENTIFIER));
    
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
     * Get storage unit version node path generator.
     *
     * @param databaseName database name
     * @param storageUnitName storage unit name
     * @return storage unit version node path generator
     */
    public static VersionNodePathGenerator getStorageUnitVersionNodePathGenerator(final String databaseName, final String storageUnitName) {
        return new VersionNodePathGenerator(String.join("/", getStorageUnitsPath(databaseName), storageUnitName));
    }
    
    /**
     * Get storage node version node path generator.
     *
     * @param databaseName database name
     * @param storageNodeName storage node name
     * @return storage node version node path generator
     */
    public static VersionNodePathGenerator getStorageNodeVersionNodePathGenerator(final String databaseName, final String storageNodeName) {
        return new VersionNodePathGenerator(String.join("/", getStorageNodesPath(databaseName), storageNodeName));
    }
    
    /**
     * Get storage unit version unit path parser.
     *
     * @return storage unit version node path parser
     */
    public static VersionNodePathParser getStorageUnitVersionNodePathParser() {
        return STORAGE_UNITS_PARSER;
    }
    
    /**
     * Get storage node version node path parser.
     *
     * @return storage node version node path parser
     */
    public static VersionNodePathParser getStorageNodeVersionNodePathParser() {
        return STORAGE_NODES_PARSER;
    }
    
    /**
     * Find storage unit name by storage unit path.
     *
     * @param path path
     * @return found storage unit name
     */
    public static Optional<String> findStorageUnitNameByStorageUnitPath(final String path) {
        Pattern pattern = Pattern.compile(getStorageUnitPath(NodePathPattern.GROUPED_IDENTIFIER, NodePathPattern.GROUPED_IDENTIFIER) + "$", Pattern.CASE_INSENSITIVE);
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
        Pattern pattern = Pattern.compile(getStorageNodePath(NodePathPattern.GROUPED_IDENTIFIER, NodePathPattern.GROUPED_IDENTIFIER) + "$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(path);
        return matcher.find() ? Optional.of(matcher.group(2)) : Optional.empty();
    }
    
    /**
     * Is data source root path.
     *
     * @param path path
     * @return is data source root path or not
     */
    public static boolean isDataSourceRootPath(final String path) {
        return Pattern.compile(getDataSourceRootPath(NodePathPattern.GROUPED_IDENTIFIER) + "?", Pattern.CASE_INSENSITIVE).matcher(path).find();
    }
}
