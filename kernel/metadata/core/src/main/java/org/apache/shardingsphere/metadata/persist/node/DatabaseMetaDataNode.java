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

package org.apache.shardingsphere.metadata.persist.node;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Database meta data node.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DatabaseMetaDataNode {
    
    private static final String ROOT_NODE = "metadata";
    
    private static final String SCHEMAS_NODE = "schemas";
    
    private static final String DATA_SOURCE_NODE = "data_sources";
    
    private static final String DATA_SOURCE_NODES_NODE = "nodes";
    
    private static final String DATA_SOURCE_UNITS_NODE = "units";
    
    private static final String RULE_NODE = "rules";
    
    private static final String TABLES_NODE = "tables";
    
    private static final String VIEWS_NODE = "views";
    
    private static final String ACTIVE_VERSION = "active_version";
    
    private static final String VERSIONS = "versions";
    
    /**
     * Get meta data data source nodes path.
     *
     * @param databaseName database name
     * @param version data source version
     * @return data source path
     */
    public static String getMetaDataDataSourceUnitsPath(final String databaseName, final String version) {
        return String.join("/", getFullMetaDataPath(databaseName, VERSIONS), version, DATA_SOURCE_NODE, DATA_SOURCE_UNITS_NODE);
    }
    
    /**
     * Get meta data data source units path.
     *
     * @param databaseName database name
     * @param version data source version
     * @return data source path
     */
    public static String getMetaDataDataSourceNodesPath(final String databaseName, final String version) {
        return String.join("/", getFullMetaDataPath(databaseName, VERSIONS), version, DATA_SOURCE_NODE, DATA_SOURCE_NODES_NODE);
    }
    
    /**
     * Get meta data node path.
     *
     * @return meta data node path
     */
    public static String getMetaDataNodePath() {
        return String.join("/", "", ROOT_NODE);
    }
    
    /**
     * Get database name path.
     *
     * @param databaseName database name
     * @return database name path
     */
    public static String getDatabaseNamePath(final String databaseName) {
        return String.join("/", getMetaDataNodePath(), databaseName);
    }
    
    /**
     * Get rule path.
     *
     * @param databaseName database name
     * @param version rule version
     * @return rule path
     */
    public static String getRulePath(final String databaseName, final String version) {
        return String.join("/", getFullMetaDataPath(databaseName, VERSIONS), version, RULE_NODE);
    }
    
    /**
     * Get meta data tables path.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @return tables path
     */
    public static String getMetaDataTablesPath(final String databaseName, final String schemaName) {
        return String.join("/", getMetaDataSchemaPath(databaseName, schemaName), TABLES_NODE);
    }
    
    /**
     * Get meta data views path.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @return views path
     */
    public static String getMetaDataViewsPath(final String databaseName, final String schemaName) {
        return String.join("/", getMetaDataSchemaPath(databaseName, schemaName), VIEWS_NODE);
    }
    
    /**
     * Get schema path.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @return tables path
     */
    public static String getMetaDataSchemaPath(final String databaseName, final String schemaName) {
        return String.join("/", getMetaDataSchemasPath(databaseName), schemaName);
    }
    
    /**
     * Get meta data schemas path.
     *
     * @param databaseName database name
     * @return schemas path
     */
    public static String getMetaDataSchemasPath(final String databaseName) {
        return String.join("/", getDatabaseNamePath(databaseName), SCHEMAS_NODE);
    }
    
    /**
     * Get table meta data path.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param table table name
     * @return table meta data path
     */
    public static String getTableMetaDataPath(final String databaseName, final String schemaName, final String table) {
        return String.join("/", getMetaDataTablesPath(databaseName, schemaName), table);
    }
    
    /**
     * Get view meta data path.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param view view name
     * @return view meta data path
     */
    public static String getViewMetaDataPath(final String databaseName, final String schemaName, final String view) {
        return String.join("/", getMetaDataViewsPath(databaseName, schemaName), view);
    }
    
    private static String getFullMetaDataPath(final String databaseName, final String node) {
        return String.join("/", "", ROOT_NODE, databaseName, node);
    }
    
    /**
     * Get database name.
     *
     * @param configNodeFullPath config node full path
     * @return database name
     */
    public static Optional<String> getDatabaseName(final String configNodeFullPath) {
        Pattern pattern = Pattern.compile(getMetaDataNodePath() + "/([\\w\\-]+)$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(configNodeFullPath);
        return matcher.find() ? Optional.of(matcher.group(1)) : Optional.empty();
    }
    
    /**
     * Get schema name.
     *
     * @param configNodeFullPath config node full path
     * @return schema name
     */
    public static Optional<String> getSchemaName(final String configNodeFullPath) {
        Pattern pattern = Pattern.compile(getMetaDataNodePath() + "/([\\w\\-]+)/schemas/([\\w\\-]+)$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(configNodeFullPath);
        return matcher.find() ? Optional.of(matcher.group(2)) : Optional.empty();
    }
    
    /**
     * Get database name by database path.
     *
     * @param databasePath database path
     * @return database name
     */
    public static Optional<String> getDatabaseNameByDatabasePath(final String databasePath) {
        Pattern pattern = Pattern.compile(getMetaDataNodePath() + "/([\\w\\-]+)?", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(databasePath);
        return matcher.find() ? Optional.of(matcher.group(1)) : Optional.empty();
    }
    
    /**
     * Get schema name.
     *
     * @param schemaPath schema path
     * @return schema name
     */
    public static Optional<String> getSchemaNameBySchemaPath(final String schemaPath) {
        Pattern pattern = Pattern.compile(getMetaDataNodePath() + "/([\\w\\-]+)/schemas/([\\w\\-]+)?", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(schemaPath);
        return matcher.find() ? Optional.of(matcher.group(2)) : Optional.empty();
    }
    
    /**
     * Get table meta data path.
     *
     * @param tableMetaDataPath table meta data path
     * @return table name
     */
    public static Optional<String> getTableName(final String tableMetaDataPath) {
        Pattern pattern = Pattern.compile(getMetaDataNodePath() + "/([\\w\\-]+)/([\\w\\-]+)/([\\w\\-]+)/tables" + "/([\\w\\-]+)$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(tableMetaDataPath);
        return matcher.find() ? Optional.of(matcher.group(4)) : Optional.empty();
    }
    
    /**
     * Get view meta data path.
     *
     * @param viewMetaDataPath view meta data path
     * @return view name
     */
    public static Optional<String> getViewName(final String viewMetaDataPath) {
        Pattern pattern = Pattern.compile(getMetaDataNodePath() + "/([\\w\\-]+)/([\\w\\-]+)/([\\w\\-]+)/views" + "/([\\w\\-]+)$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(viewMetaDataPath);
        return matcher.find() ? Optional.of(matcher.group(4)) : Optional.empty();
    }
    
    /**
     * Get active version path.
     * 
     * @param databaseName database name
     * @return active version path
     */
    public static String getActiveVersionPath(final String databaseName) {
        return getFullMetaDataPath(databaseName, ACTIVE_VERSION);
    }
    
    /**
     * Get database version path.
     * 
     * @param databaseName database name
     * @param version version
     * @return database version path
     */
    public static String getDatabaseVersionPath(final String databaseName, final String version) {
        return String.join("/", getFullMetaDataPath(databaseName, VERSIONS), version);
    }
    
    /**
     * Get version by data source units path.
     * 
     * @param dataSourceNodeFullPath data sources unit node full path
     * @return version
     */
    public static Optional<String> getVersionByDataSourceUnitsPath(final String dataSourceNodeFullPath) {
        Pattern pattern = Pattern.compile(getMetaDataNodePath() + "/([\\w\\-]+)/versions/([\\w\\-]+)/data_sources/units", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(dataSourceNodeFullPath);
        return matcher.find() ? Optional.of(matcher.group(2)) : Optional.empty();
    }
    
    /**
     * Get version by data source nodes path.
     *
     * @param dataSourceNodeFullPath data sources node full path
     * @return version
     */
    public static Optional<String> getVersionByDataSourceNodesPath(final String dataSourceNodeFullPath) {
        Pattern pattern = Pattern.compile(getMetaDataNodePath() + "/([\\w\\-]+)/versions/([\\w\\-]+)/data_sources/nodes", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(dataSourceNodeFullPath);
        return matcher.find() ? Optional.of(matcher.group(2)) : Optional.empty();
    }
    
    /**
     * Get version by rules path.
     *
     * @param rulesNodeFullPath rules node full path
     * @return version
     */
    public static Optional<String> getVersionByRulesPath(final String rulesNodeFullPath) {
        Pattern pattern = Pattern.compile(getMetaDataNodePath() + "/([\\w\\-]+)/versions/([\\w\\-]+)/rules", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(rulesNodeFullPath);
        return matcher.find() ? Optional.of(matcher.group(2)) : Optional.empty();
    }
}
