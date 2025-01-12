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
 * ShardingSphere data node path.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShardingSphereDataNodePath {
    
    private static final String ROOT_NODE = "/statistics";
    
    private static final String DATABASES_NODE = "databases";
    
    private static final String SCHEMAS_NODE = "schemas";
    
    private static final String TABLES_NODE = "tables";
    
    private static final String JOB_NODE = "job";
    
    private static final String IDENTIFIER_PATTERN = "([\\w\\-]+)";
    
    private static final String UNIQUE_KEY_PATTERN = "(\\w+)";
    
    /**
     * Get database root path.
     *
     * @return database root path
     */
    public static String getDatabasesRootPath() {
        return String.join("/", ROOT_NODE, DATABASES_NODE);
    }
    
    /**
     * Get database path.
     *
     * @param databaseName database name
     * @return database path
     */
    public static String getDatabasePath(final String databaseName) {
        return String.join("/", getDatabasesRootPath(), databaseName);
    }
    
    /**
     * Get schema root path.
     *
     * @param databaseName database name
     * @return schema root path
     */
    public static String getSchemaRootPath(final String databaseName) {
        return String.join("/", getDatabasePath(databaseName), SCHEMAS_NODE);
    }
    
    /**
     * Get schema path.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @return schema path
     */
    public static String getSchemaPath(final String databaseName, final String schemaName) {
        return String.join("/", getSchemaRootPath(databaseName), schemaName);
    }
    
    /**
     * Get table root path.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @return table root path
     */
    public static String getTableRootPath(final String databaseName, final String schemaName) {
        return String.join("/", getSchemaPath(databaseName, schemaName), TABLES_NODE);
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
        return String.join("/", getTableRootPath(databaseName, schemaName), tableName);
    }
    
    /**
     * Get table row path.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param tableName table name
     * @param uniqueKey unique key
     * @return table row path
     */
    public static String getTableRowPath(final String databaseName, final String schemaName, final String tableName, final String uniqueKey) {
        return String.join("/", getTablePath(databaseName, schemaName, tableName), uniqueKey);
    }
    
    /**
     * Find database name.
     *
     * @param path path
     * @param containsChildPath whether contains child path
     * @return found database name
     */
    public static Optional<String> findDatabaseName(final String path, final boolean containsChildPath) {
        String endPattern = containsChildPath ? "?" : "$";
        Pattern pattern = Pattern.compile(getDatabasePath(IDENTIFIER_PATTERN) + endPattern, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(path);
        return matcher.find() ? Optional.of(matcher.group(1)) : Optional.empty();
    }
    
    /**
     * Find schema name.
     *
     * @param path path
     * @param containsChildPath whether contains child path
     * @return found schema name
     */
    public static Optional<String> findSchemaName(final String path, final boolean containsChildPath) {
        String endPattern = containsChildPath ? "?" : "$";
        Pattern pattern = Pattern.compile(getSchemaPath(IDENTIFIER_PATTERN, IDENTIFIER_PATTERN) + endPattern, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(path);
        return matcher.find() ? Optional.of(matcher.group(2)) : Optional.empty();
    }
    
    /**
     * Find table name.
     *
     * @param path path
     * @param containsChildPath whether contains child path
     * @return found table name
     */
    public static Optional<String> findTableName(final String path, final boolean containsChildPath) {
        String endPattern = containsChildPath ? "?" : "$";
        Pattern pattern = Pattern.compile(getTablePath(IDENTIFIER_PATTERN, IDENTIFIER_PATTERN, IDENTIFIER_PATTERN) + endPattern, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(path);
        return matcher.find() ? Optional.of(matcher.group(3)) : Optional.empty();
    }
    
    /**
     * Find row unique key.
     *
     * @param path path
     * @return found row unique key
     */
    public static Optional<String> findRowUniqueKey(final String path) {
        Pattern pattern = Pattern.compile(getTableRowPath(IDENTIFIER_PATTERN, IDENTIFIER_PATTERN, IDENTIFIER_PATTERN, UNIQUE_KEY_PATTERN) + "$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(path);
        return matcher.find() ? Optional.of(matcher.group(4)) : Optional.empty();
    }
    
    /**
     * Get job path.
     *
     * @return job path
     */
    public static String getJobPath() {
        return String.join("/", ROOT_NODE, JOB_NODE);
    }
}
