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
 * TODO Rename DatabaseMetaDataNode when metadata structure adjustment completed. #25485
 * New database meta data node.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class NewDatabaseMetaDataNode {
    
    private static final String ROOT_NODE = "metadata";
    
    private static final String DATA_SOURCES_NODE = "data_sources";
    
    private static final String RULE_NODE = "rules";
    
    private static final String SCHEMAS_NODE = "schemas";
    
    private static final String TABLES_NODE = "tables";
    
    private static final String VIEWS_NODE = "views";
    
    private static final String ACTIVE_VERSION = "active_version";
    
    private static final String VERSIONS = "versions";
    
    /**
     * Get data Sources node.
     *
     * @param databaseName database name
     * @return data sources node
     */
    public static String getDataSourcesNode(final String databaseName) {
        return String.join("/", getMetaDataNodeNode(), databaseName, DATA_SOURCES_NODE);
    }
    
    /**
     * Get data Source node.
     *
     * @param databaseName database name
     * @param dataSourceName data source name
     * @param version version
     * @return data source node
     */
    public static String getDataSourceNode(final String databaseName, final String dataSourceName, final String version) {
        return String.join("/", getDataSourceVersionsNode(databaseName, dataSourceName), version);
    }
    
    /**
     * Get data Source active version node.
     *
     * @param databaseName database name
     * @param dataSourceName data source name
     * @return data source active version node
     */
    public static String getDataSourceActiveVersionNode(final String databaseName, final String dataSourceName) {
        return String.join("/", getDataSourcesNode(databaseName), dataSourceName, ACTIVE_VERSION);
    }
    
    /**
     * Get data source versions node.
     *
     * @param databaseName database name
     * @param dataSourceName data source name
     * @return data source versions node
     */
    public static String getDataSourceVersionsNode(final String databaseName, final String dataSourceName) {
        return String.join("/", getDataSourcesNode(databaseName), dataSourceName, VERSIONS);
    }
    
    /**
     * Get database rule active version node.
     *
     * @param databaseName database name
     * @param ruleName rule name
     * @param key key
     * @return database rule active version node
     */
    public static String getDatabaseRuleActiveVersionNode(final String databaseName, final String ruleName, final String key) {
        return String.join("/", getDatabaseRuleNode(databaseName, ruleName), key, ACTIVE_VERSION);
    }
    
    /**
     * Get database rule versions node.
     *
     * @param databaseName database name
     * @param ruleName rule name
     * @param key key
     * @return database rule versions node
     */
    public static String getDatabaseRuleVersionsNode(final String databaseName, final String ruleName, final String key) {
        return String.join("/", getDatabaseRuleNode(databaseName, ruleName), key, VERSIONS);
    }
    
    /**
     * Get database rule version node.
     *
     * @param databaseName database name
     * @param ruleName rule name
     * @param key key
     * @param version version
     * @return database rule next version
     */
    public static String getDatabaseRuleVersionNode(final String databaseName, final String ruleName, final String key, final String version) {
        return String.join("/", getDatabaseRuleNode(databaseName, ruleName), key, VERSIONS, version);
    }
    
    /**
     * Get database rule node.
     *
     * @param databaseName database name
     * @param ruleName rule name
     * @param key key
     * @return database rule node without version
     */
    public static String getDatabaseRuleNode(final String databaseName, final String ruleName, final String key) {
        return String.join("/", getDatabaseRuleNode(databaseName, ruleName), key);
    }
    
    private static String getDatabaseRuleNode(final String databaseName, final String ruleName) {
        return String.join("/", getRulesNode(databaseName), ruleName);
    }
    
    /**
     * Get database rules node.
     *
     * @param databaseName database name
     * @return database rules node
     */
    public static String getRulesNode(final String databaseName) {
        return String.join("/", getMetaDataNodeNode(), databaseName, RULE_NODE);
    }
    
    /**
     * Get table active version node.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param tableName table name
     * @return tables active version node
     */
    public static String getTableActiveVersionNode(final String databaseName, final String schemaName, final String tableName) {
        return String.join("/", getTableNode(databaseName, schemaName, tableName), ACTIVE_VERSION);
    }
    
    /**
     * Get table versions node.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param tableName table name
     * @return tables versions node
     */
    public static String getTableVersionsNode(final String databaseName, final String schemaName, final String tableName) {
        return String.join("/", getTableNode(databaseName, schemaName, tableName), VERSIONS);
    }
    
    /**
     * Get table version node.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param tableName table name
     * @param version version
     * @return table version node
     */
    public static String getTableVersionNode(final String databaseName, final String schemaName, final String tableName, final String version) {
        return String.join("/", getTableVersionsNode(databaseName, schemaName, tableName), version);
    }
    
    /**
     * Get table node.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param tableName table name
     * @return table node
     */
    public static String getTableNode(final String databaseName, final String schemaName, final String tableName) {
        return String.join("/", getMetaDataNodeNode(), databaseName, SCHEMAS_NODE, schemaName, TABLES_NODE, tableName);
    }
    
    /**
     * Get view name active version node.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param viewName view name
     * @return view active version node
     */
    public static String getViewActiveVersionNode(final String databaseName, final String schemaName, final String viewName) {
        return String.join("/", getViewNode(databaseName, schemaName, viewName), ACTIVE_VERSION);
    }
    
    /**
     * Get view versions node.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param viewName view name
     * @return view versions node
     */
    public static String getViewVersionsNode(final String databaseName, final String schemaName, final String viewName) {
        return String.join("/", getViewNode(databaseName, schemaName, viewName), VERSIONS);
    }
    
    /**
     * Get view version node.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param viewName view name
     * @param version version
     * @return view version node
     */
    public static String getViewVersionNode(final String databaseName, final String schemaName, final String viewName, final String version) {
        return String.join("/", getViewVersionsNode(databaseName, schemaName, viewName), version);
    }
    
    /**
     * Get view node.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param viewName view name
     * @return tables path
     */
    public static String getViewNode(final String databaseName, final String schemaName, final String viewName) {
        return String.join("/", getMetaDataNodeNode(), databaseName, SCHEMAS_NODE, schemaName, VIEWS_NODE, viewName);
    }
    
    /**
     * Is data sources node.
     *
     * @param path path
     * @return true or false
     */
    public static boolean isDataSourcesNode(final String path) {
        Pattern pattern = Pattern.compile(getMetaDataNodeNode() + "/([\\w\\-]+)/" + DATA_SOURCES_NODE + "/?", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(path);
        return matcher.find();
    }
    
    /**
     * Get data source name by data source node.
     *
     * @param path path
     * @return data source name
     */
    public static Optional<String> getDataSourceNameByDataSourceNode(final String path) {
        Pattern pattern = Pattern.compile(getMetaDataNodeNode() + "/([\\w\\-]+)/" + DATA_SOURCES_NODE + "/([\\w\\-]+)?", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(path);
        return matcher.find() ? Optional.of(matcher.group(2)) : Optional.empty();
    }
    
    /**
     * Get version by data source node.
     *
     * @param path path
     * @return data source version
     */
    public static Optional<String> getVersionByDataSourceNode(final String path) {
        Pattern pattern = Pattern.compile(getMetaDataNodeNode() + "/([\\w\\-]+)/" + DATA_SOURCES_NODE + "/([\\w\\-]+)/versions/([\\w\\-]+)$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(path);
        return matcher.find() ? Optional.of(matcher.group(3)) : Optional.empty();
    }
    
    /**
     * Get database name.
     *
     * @param path path
     * @return database name
     */
    public static Optional<String> getDatabaseName(final String path) {
        Pattern pattern = Pattern.compile(getMetaDataNodeNode() + "/([\\w\\-]+)$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(path);
        return matcher.find() ? Optional.of(matcher.group(1)) : Optional.empty();
    }
    
    /**
     * Get database name by schema path.
     *
     * @param schemaPath database path
     * @return database name
     */
    public static Optional<String> getDatabaseNameBySchemaNode(final String schemaPath) {
        Pattern pattern = Pattern.compile(getMetaDataNodeNode() + "/([\\w\\-]+)?", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(schemaPath);
        return matcher.find() ? Optional.of(matcher.group(1)) : Optional.empty();
    }
    
    /**
     * Get schema name.
     *
     * @param path path
     * @return schema name
     */
    public static Optional<String> getSchemaName(final String path) {
        Pattern pattern = Pattern.compile(getMetaDataNodeNode() + "/([\\w\\-]+)/schemas/([\\w\\-]+)$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(path);
        return matcher.find() ? Optional.of(matcher.group(2)) : Optional.empty();
    }
    
    /**
     * Get schema name by table path.
     *
     * @param tablePath table path
     * @return schema name
     */
    public static Optional<String> getSchemaNameByTableNode(final String tablePath) {
        Pattern pattern = Pattern.compile(getMetaDataNodeNode() + "/([\\w\\-]+)/schemas/([\\w\\-]+)?", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(tablePath);
        return matcher.find() ? Optional.of(matcher.group(2)) : Optional.empty();
    }
    
    /**
     * Get table name.
     *
     * @param path path
     * @return table name
     */
    public static Optional<String> getTableName(final String path) {
        Pattern pattern = Pattern.compile(getMetaDataNodeNode() + "/([\\w\\-]+)/schemas/([\\w\\-]+)/tables" + "/([\\w\\-]+)?", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(path);
        return matcher.find() ? Optional.of(matcher.group(3)) : Optional.empty();
    }
    
    /**
     * Get table name version.
     *
     * @param path path
     * @return table name version
     */
    public static Optional<String> getTableNameVersion(final String path) {
        Pattern pattern = Pattern.compile(getMetaDataNodeNode() + "/([\\w\\-]+)/schemas/([\\w\\-]+)/tables" + "/([\\w\\-]+)/versions/([\\w\\-]+)$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(path);
        return matcher.find() ? Optional.of(matcher.group(4)) : Optional.empty();
    }
    
    /**
     * Get view name.
     *
     * @param path path
     * @return view name
     */
    public static Optional<String> getViewName(final String path) {
        Pattern pattern = Pattern.compile(getMetaDataNodeNode() + "/([\\w\\-]+)/schemas/([\\w\\-]+)/views" + "/([\\w\\-]+)?", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(path);
        return matcher.find() ? Optional.of(matcher.group(3)) : Optional.empty();
    }
    
    /**
     * Get view name version.
     *
     * @param path path
     * @return view name version
     */
    public static Optional<String> getViewNameVersion(final String path) {
        Pattern pattern = Pattern.compile(getMetaDataNodeNode() + "/([\\w\\-]+)/schemas/([\\w\\-]+)/views" + "/([\\w\\-]+)/versions/([\\w\\-]+)$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(path);
        return matcher.find() ? Optional.of(matcher.group(4)) : Optional.empty();
    }
    
    /**
     * Decorate active version.
     *
     * @param rulePath rule path
     * @return group name
     */
    public static String decorateActiveVersion(final String rulePath) {
        return rulePath.substring(0, rulePath.indexOf(VERSIONS)) + ACTIVE_VERSION;
    }
    
    private static String getMetaDataNodeNode() {
        return String.join("/", "", ROOT_NODE);
    }
}
