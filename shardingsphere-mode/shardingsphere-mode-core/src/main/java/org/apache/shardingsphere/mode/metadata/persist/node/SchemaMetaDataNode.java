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

package org.apache.shardingsphere.mode.metadata.persist.node;

import com.google.common.base.Joiner;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Schema meta data node.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SchemaMetaDataNode {
    
    private static final String ROOT_NODE = "metadata";
    
    private static final String DATA_SOURCE_NODE = "dataSources";
    
    private static final String RULE_NODE = "rules";
    
    private static final String TABLES_NODE = "tables";
    
    private static final String ACTIVE_VERSION = "active_version";
    
    private static final String VERSIONS = "versions";
    
    /**
     * Get meta data data source path.
     *
     * @param schemaName schema name
     * @param version data source version                  
     * @return data source path
     */
    public static String getMetaDataDataSourcePath(final String schemaName, final String version) {
        return Joiner.on("/").join(getFullMetaDataPath(schemaName, VERSIONS), version, DATA_SOURCE_NODE);
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
     * Get schema name path.
     *
     * @param schemaName schema name
     * @return schema name path
     */
    public static String getSchemaNamePath(final String schemaName) {
        return String.join("/", "", ROOT_NODE, schemaName);
    }
    
    /**
     * Get rule path.
     *
     * @param schemaName schema name
     * @param version rule version                  
     * @return rule path
     */
    public static String getRulePath(final String schemaName, final String version) {
        return Joiner.on("/").join(getFullMetaDataPath(schemaName, VERSIONS), version, RULE_NODE);
    }
    
    /**
     * Get meta data tables path.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @return tables path
     */
    public static String getMetaDataTablesPath(final String databaseName, final String schemaName) {
        return String.join("/", getDatabaseNamePath(databaseName), schemaName, TABLES_NODE);
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
    
    private static String getFullMetaDataPath(final String schemaName, final String node) {
        return String.join("/", "", ROOT_NODE, schemaName, schemaName, node);
    }
    
    /**
     * Get schema name.
     *
     * @param configurationNodeFullPath configuration node full path
     * @return schema name
     */
    public static Optional<String> getSchemaName(final String configurationNodeFullPath) {
        Pattern pattern = Pattern.compile(getMetaDataNodePath() + "/([\\w\\-]+)/([\\w\\-]+)" + "(/datasources|/rules|/tables)?", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(configurationNodeFullPath);
        return matcher.find() ? Optional.of(matcher.group(2)) : Optional.empty();
    }
    
    /**
     * Get schema name by schema path.
     *
     * @param schemaPath schema path
     * @return schema name
     */
    public static Optional<String> getDatabaseNameBySchemaPath(final String schemaPath) {
        Pattern pattern = Pattern.compile(getMetaDataNodePath() + "/([\\w\\-]+)$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(schemaPath);
        return matcher.find() ? Optional.of(matcher.group(1)) : Optional.empty();
    }
    
    /**
     * Get table meta data path.
     *
     * @param tableMetaDataPath table meta data path
     * @return table name
     */
    public static Optional<String> getTableName(final String tableMetaDataPath) {
        Pattern pattern = Pattern.compile(getMetaDataNodePath() + "/([\\w\\-]+)/([\\w\\-]+)/tables" + "/([\\w\\-]+)$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(tableMetaDataPath);
        return matcher.find() ? Optional.of(matcher.group(3)) : Optional.empty();
    }
    
    /**
     * Get active version path.
     * 
     * @param schemaName schema name
     * @return active version path
     */
    public static String getActiveVersionPath(final String schemaName) {
        return getFullMetaDataPath(schemaName, ACTIVE_VERSION);
    }
    
    /**
     * Get schema version path.
     * 
     * @param schemaName schema name
     * @param version version
     * @return schema version path
     */
    public static String getSchemaVersionPath(final String schemaName, final String version) {
        return Joiner.on("/").join(getFullMetaDataPath(schemaName, VERSIONS), version);
    }
    
    /**
     * Get version by data sources path.
     * 
     * @param dataSourceNodeFullPath data sources node full path
     * @return version
     */
    public static Optional<String> getVersionByDataSourcesPath(final String dataSourceNodeFullPath) {
        Pattern pattern = Pattern.compile(getMetaDataNodePath() + "/([\\w\\-]+)/([\\w\\-]+)" + "/versions/([\\w\\-]+)/dataSources", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(dataSourceNodeFullPath);
        return matcher.find() ? Optional.of(matcher.group(3)) : Optional.empty();
    }
    
    /**
     * Get version by rules path.
     *
     * @param rulesNodeFullPath rules node full path
     * @return version
     */
    public static Optional<String> getVersionByRulesPath(final String rulesNodeFullPath) {
        Pattern pattern = Pattern.compile(getMetaDataNodePath() + "/([\\w\\-]+)/([\\w\\-]+)" + "/versions/([\\w\\-]+)/rules", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(rulesNodeFullPath);
        return matcher.find() ? Optional.of(matcher.group(3)) : Optional.empty();
    }
}
