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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ShardingSphere data node.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShardingSphereDataNode {
    
    private static final String ROOT_NODE = "sys_data";
    
    private static final String SCHEMAS_NODE = "schemas";
    
    private static final String TABLES_NODE = "tables";
    
    /**
     * Get ShardingSphere data node path.
     *
     * @return meta data node path
     */
    public static String getShardingSphereDataNodePath() {
        return String.join("/", "", ROOT_NODE);
    }
    
    /**
     * Get database name path.
     *
     * @param databaseName database name
     * @return database name path
     */
    public static String getDatabaseNamePath(final String databaseName) {
        return String.join("/", getShardingSphereDataNodePath(), databaseName);
    }
    
    /**
     * Get meta data tables path.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @return tables path
     */
    public static String getTablesPath(final String databaseName, final String schemaName) {
        return String.join("/", getSchemaDataPath(databaseName, schemaName), TABLES_NODE);
    }
    
    /**
     * Get schema path.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @return tables path
     */
    public static String getSchemaDataPath(final String databaseName, final String schemaName) {
        return String.join("/", getSchemasPath(databaseName), schemaName);
    }
    
    /**
     * Get meta data schemas path.
     *
     * @param databaseName database name
     * @return schemas path
     */
    public static String getSchemasPath(final String databaseName) {
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
    public static String getTablePath(final String databaseName, final String schemaName, final String table) {
        return String.join("/", getTablesPath(databaseName, schemaName), table);
    }
    
    /**
     * Get database name.
     *
     * @param configNodeFullPath config node full path
     * @return database name
     */
    public static Optional<String> getDatabaseName(final String configNodeFullPath) {
        Pattern pattern = Pattern.compile(getShardingSphereDataNodePath() + "/([\\w\\-]+)$", Pattern.CASE_INSENSITIVE);
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
        Pattern pattern = Pattern.compile(getShardingSphereDataNodePath() + "/([\\w\\-]+)/schemas/([\\w\\-]+)$", Pattern.CASE_INSENSITIVE);
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
        Pattern pattern = Pattern.compile(getShardingSphereDataNodePath() + "/([\\w\\-]+)?", Pattern.CASE_INSENSITIVE);
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
        Pattern pattern = Pattern.compile(getShardingSphereDataNodePath() + "/([\\w\\-]+)/schemas/([\\w\\-]+)?", Pattern.CASE_INSENSITIVE);
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
        Pattern pattern = Pattern.compile(getShardingSphereDataNodePath() + "/([\\w\\-]+)/([\\w\\-]+)/([\\w\\-]+)/tables" + "/([\\w\\-]+)$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(tableMetaDataPath);
        return matcher.find() ? Optional.of(matcher.group(4)) : Optional.empty();
    }
}
