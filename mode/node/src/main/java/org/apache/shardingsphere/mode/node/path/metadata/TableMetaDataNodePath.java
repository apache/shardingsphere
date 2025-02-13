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
 * Table meta data node path.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TableMetaDataNodePath {
    
    private static final String TABLES_NODE = "tables";
    
    private static final VersionNodePathParser PARSER =
            new VersionNodePathParser(getTablePath(NodePathPattern.GROUPED_IDENTIFIER, NodePathPattern.GROUPED_IDENTIFIER, NodePathPattern.GROUPED_IDENTIFIER));
    
    /**
     * Get table root path.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @return table root path
     */
    public static String getTableRootPath(final String databaseName, final String schemaName) {
        return String.join("/", DatabaseMetaDataNodePath.getSchemaPath(databaseName, schemaName), TABLES_NODE);
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
     * Get table version node path generator.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param tableName table name
     * @return table version node path generator
     */
    public static VersionNodePathGenerator getVersionNodePathGenerator(final String databaseName, final String schemaName, final String tableName) {
        return new VersionNodePathGenerator(getTablePath(databaseName, schemaName, tableName));
    }
    
    /**
     * Find table name.
     *
     * @param path path
     * @return found table name
     */
    public static Optional<String> findTableName(final String path) {
        Pattern pattern = Pattern.compile(getTablePath(NodePathPattern.GROUPED_IDENTIFIER, NodePathPattern.GROUPED_IDENTIFIER, NodePathPattern.GROUPED_IDENTIFIER) + "$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(path);
        return matcher.find() ? Optional.of(matcher.group(3)) : Optional.empty();
    }
    
    /**
     * Is table path.
     *
     * @param path path
     * @return true or false
     */
    public static boolean isTablePath(final String path) {
        return findTableName(path).isPresent();
    }
    
    /**
     * Get table version pattern node path parser.
     *
     * @return table version node path parser
     */
    public static VersionNodePathParser getVersionNodePathParser() {
        return PARSER;
    }
}
