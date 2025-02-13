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

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Database meta data node path.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DatabaseMetaDataNodePath {
    
    private static final String ROOT_NODE = "/metadata";
    
    private static final String SCHEMAS_NODE = "schemas";
    
    /**
     * Get meta data root path.
     *
     * @return meta data root path
     */
    public static String getRootPath() {
        return ROOT_NODE;
    }
    
    /**
     * Get database path.
     *
     * @param databaseName database name
     * @return database path
     */
    public static String getDatabasePath(final String databaseName) {
        return String.join("/", getRootPath(), databaseName);
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
     * Find database name.
     *
     * @param path path
     * @param containsChildPath whether contains child path
     * @return found database name
     */
    public static Optional<String> findDatabaseName(final String path, final boolean containsChildPath) {
        String endPattern = containsChildPath ? "?" : "$";
        Pattern pattern = Pattern.compile(getDatabasePath(NodePathPattern.GROUPED_IDENTIFIER) + endPattern, Pattern.CASE_INSENSITIVE);
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
        Pattern pattern = Pattern.compile(getSchemaPath(NodePathPattern.GROUPED_IDENTIFIER, NodePathPattern.GROUPED_IDENTIFIER) + endPattern, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(path);
        return matcher.find() ? Optional.of(matcher.group(2)) : Optional.empty();
    }
}
