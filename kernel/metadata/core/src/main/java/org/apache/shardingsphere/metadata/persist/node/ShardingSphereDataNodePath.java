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
 * ShardingSphere data node path.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShardingSphereDataNodePath {
    
    private static final String ROOT_NODE = "/statistics";
    
    private static final String DATABASES_NODE = "databases";
    
    private static final String SCHEMAS_NODE = "schemas";
    
    private static final String TABLES_NODE = "tables";
    
    private static final String JOB_NODE = "job";
    
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
    public static String getSchemaDataPath(final String databaseName, final String schemaName) {
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
        return String.join("/", getSchemaDataPath(databaseName, schemaName), TABLES_NODE);
    }
    
    /**
     * Get table path.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param table table name
     * @return table path
     */
    public static String getTablePath(final String databaseName, final String schemaName, final String table) {
        return String.join("/", getTableRootPath(databaseName, schemaName), table);
    }
    
    /**
     * Get table row path.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param table table name
     * @param uniqueKey unique key
     * @return table row path
     */
    public static String getTableRowPath(final String databaseName, final String schemaName, final String table, final String uniqueKey) {
        return String.join("/", getTablePath(databaseName, schemaName, table), uniqueKey);
    }
    
    /**
     * Find database name.
     *
     * @param configNodeFullPath config node full path
     * @return found database name
     */
    public static Optional<String> findDatabaseName(final String configNodeFullPath) {
        Pattern pattern = Pattern.compile(getDatabasesRootPath() + "/([\\w\\-]+)$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(configNodeFullPath);
        return matcher.find() ? Optional.of(matcher.group(1)) : Optional.empty();
    }
    
    /**
     * Find schema name.
     *
     * @param configNodeFullPath config node full path
     * @return found schema name
     */
    public static Optional<String> findSchemaName(final String configNodeFullPath) {
        Pattern pattern = Pattern.compile(getDatabasesRootPath() + "/([\\w\\-]+)/schemas/([\\w\\-]+)$", Pattern.CASE_INSENSITIVE);
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
        Pattern pattern = Pattern.compile(getDatabasesRootPath() + "/([\\w\\-]+)?", Pattern.CASE_INSENSITIVE);
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
        Pattern pattern = Pattern.compile(getDatabasesRootPath() + "/([\\w\\-]+)/schemas/([\\w\\-]+)?", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(schemaPath);
        return matcher.find() ? Optional.of(matcher.group(2)) : Optional.empty();
    }
    
    /**
     * Get table data path.
     *
     * @param tableMetaDataPath table data path
     * @return table name
     */
    public static Optional<String> getTableName(final String tableMetaDataPath) {
        Pattern pattern = Pattern.compile(getDatabasesRootPath() + "/([\\w\\-]+)/schemas/([\\w\\-]+)/tables" + "/([\\w\\-]+)$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(tableMetaDataPath);
        return matcher.find() ? Optional.of(matcher.group(3)) : Optional.empty();
    }
    
    /**
     * Get table name by row path.
     *
     * @param rowPath row data path
     * @return table name
     */
    public static Optional<String> getTableNameByRowPath(final String rowPath) {
        Pattern pattern = Pattern.compile(getDatabasesRootPath() + "/([\\w\\-]+)/schemas/([\\w\\-]+)/tables" + "/([\\w\\-]+)?", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(rowPath);
        return matcher.find() ? Optional.of(matcher.group(3)) : Optional.empty();
    }
    
    /**
     * Get row unique key.
     *
     * @param rowPath row data path
     * @return row unique key
     */
    public static Optional<String> getRowUniqueKey(final String rowPath) {
        Pattern pattern = Pattern.compile(getDatabasesRootPath() + "/([\\w\\-]+)/schemas/([\\w\\-]+)/tables" + "/([\\w\\-]+)" + "/(\\w+)$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(rowPath);
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
