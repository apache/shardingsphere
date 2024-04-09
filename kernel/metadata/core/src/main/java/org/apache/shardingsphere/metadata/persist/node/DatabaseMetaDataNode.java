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
    
    private static final String TABLES_NODE = "tables";
    
    private static final String ACTIVE_VERSION = "active_version";
    
    private static final String VERSIONS = "versions";
    
    /**
     * Get database name path.
     *
     * @param databaseName database name
     * @return database name path
     */
    public static String getDatabaseNamePath(final String databaseName) {
        return String.join("/", getMetaDataNode(), databaseName);
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
     * Get database name.
     *
     * @param path path
     * @return database name
     */
    public static Optional<String> getDatabaseName(final String path) {
        Pattern pattern = Pattern.compile(getMetaDataNode() + "/([\\w\\-]+)$", Pattern.CASE_INSENSITIVE);
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
        Pattern pattern = Pattern.compile(getMetaDataNode() + "/([\\w\\-]+)?", Pattern.CASE_INSENSITIVE);
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
        Pattern pattern = Pattern.compile(getMetaDataNode() + "/([\\w\\-]+)/schemas/([\\w\\-]+)$", Pattern.CASE_INSENSITIVE);
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
        Pattern pattern = Pattern.compile(getMetaDataNode() + "/([\\w\\-]+)/schemas/([\\w\\-]+)?", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(tablePath);
        return matcher.find() ? Optional.of(matcher.group(2)) : Optional.empty();
    }
    
    /**
     * Get version node by active version path.
     *
     * @param rulePath rule path
     * @param activeVersion active version
     * @return active version node
     */
    public static String getVersionNodeByActiveVersionPath(final String rulePath, final String activeVersion) {
        return rulePath.replace(ACTIVE_VERSION, VERSIONS) + "/" + activeVersion;
    }
    
    /**
     * Get meta data node.
     *
     * @return meta data node
     */
    public static String getMetaDataNode() {
        return String.join("/", "", ROOT_NODE);
    }
}
