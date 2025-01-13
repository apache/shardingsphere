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
 * Table meta data node path.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TableMetaDataNodePath {
    
    private static final String ROOT_NODE = "/metadata";
    
    private static final String SCHEMAS_NODE = "schemas";
    
    private static final String TABLES_NODE = "tables";
    
    private static final String VERSIONS_NODE = "versions";
    
    private static final String ACTIVE_VERSION_NODE = "active_version";
    
    private static final String TABLES_PATTERN = "/([\\w\\-]+)/schemas/([\\w\\-]+)/tables";
    
    private static final String ACTIVE_VERSION_SUFFIX = "/([\\w\\-]+)/active_version";
    
    private static final String TABLE_SUFFIX = "/([\\w\\-]+)$";
    
    /**
     * Get meta data tables path.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @return tables path
     */
    public static String getMetaDataTablesPath(final String databaseName, final String schemaName) {
        return String.join("/", ROOT_NODE, databaseName, SCHEMAS_NODE, schemaName, TABLES_NODE);
    }
    
    /**
     * Get table active version path.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param tableName table name
     * @return tables active version path
     */
    public static String getTableActiveVersionPath(final String databaseName, final String schemaName, final String tableName) {
        return String.join("/", ROOT_NODE, databaseName, SCHEMAS_NODE, schemaName, TABLES_NODE, tableName, ACTIVE_VERSION_NODE);
    }
    
    /**
     * Get table versions path.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param tableName table name
     * @return tables versions path
     */
    public static String getTableVersionsPath(final String databaseName, final String schemaName, final String tableName) {
        return String.join("/", ROOT_NODE, databaseName, SCHEMAS_NODE, schemaName, TABLES_NODE, tableName, VERSIONS_NODE);
    }
    
    /**
     * Get table version path.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param tableName table name
     * @param version version
     * @return table version path
     */
    public static String getTableVersionPath(final String databaseName, final String schemaName, final String tableName, final String version) {
        return String.join("/", getTableVersionsPath(databaseName, schemaName, tableName), version);
    }
    
    /**
     * Get table path.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param tableName table name
     * @return table path
     */
    public static String getTablePath(final String databaseName, final String schemaName, final String tableName) {
        return String.join("/", ROOT_NODE, databaseName, SCHEMAS_NODE, schemaName, TABLES_NODE, tableName);
    }
    
    /**
     * Get table name by active version path.
     *
     * @param path path
     * @return table name
     */
    public static Optional<String> getTableNameByActiveVersionPath(final String path) {
        Pattern pattern = Pattern.compile(ROOT_NODE + TABLES_PATTERN + ACTIVE_VERSION_SUFFIX, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(path);
        return matcher.find() ? Optional.of(matcher.group(3)) : Optional.empty();
    }
    
    /**
     * Find table name.
     *
     * @param path path
     * @return found table name
     */
    public static Optional<String> findTableName(final String path) {
        Pattern pattern = Pattern.compile(ROOT_NODE + TABLES_PATTERN + "/([\\w\\-]+)$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(path);
        return matcher.find() ? Optional.of(matcher.group(3)) : Optional.empty();
    }
    
    /**
     * Is table active version path.
     *
     * @param path path
     * @return true or false
     */
    public static boolean isTableActiveVersionPath(final String path) {
        return Pattern.compile(ROOT_NODE + TABLES_PATTERN + ACTIVE_VERSION_SUFFIX, Pattern.CASE_INSENSITIVE).matcher(path).find();
    }
    
    /**
     * Is table path.
     *
     * @param path path
     * @return true or false
     */
    public static boolean isTablePath(final String path) {
        return Pattern.compile(ROOT_NODE + TABLES_PATTERN + TABLE_SUFFIX, Pattern.CASE_INSENSITIVE).matcher(path).find();
    }
}
