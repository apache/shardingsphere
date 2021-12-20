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
    
    /**
     * Get meta data data source path.
     *
     * @param schemaName schema name
     * @return data source path
     */
    public static String getMetaDataDataSourcePath(final String schemaName) {
        return getFullMetaDataPath(schemaName, DATA_SOURCE_NODE);
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
     * @return rule path
     */
    public static String getRulePath(final String schemaName) {
        return getFullMetaDataPath(schemaName, RULE_NODE);
    }
    
    /**
     * Get meta data tables path.
     *
     * @param schemaName schema name
     * @return tables path
     */
    public static String getMetaDataTablesPath(final String schemaName) {
        return getFullMetaDataPath(schemaName, TABLES_NODE);
    }
    
    /**
     * Get table meta data path.
     * 
     * @param schemaName schema name
     * @param table table name
     * @return table meta data path
     */
    public static String getTableMetaDataPath(final String schemaName, final String table) {
        return String.join("/", getMetaDataTablesPath(schemaName), table);
    }
    
    private static String getFullMetaDataPath(final String schemaName, final String node) {
        return String.join("/", "", ROOT_NODE, schemaName, node);
    }
    
    /**
     * Get schema name.
     *
     * @param configurationNodeFullPath configuration node full path
     * @return schema name
     */
    public static String getSchemaName(final String configurationNodeFullPath) {
        Pattern pattern = Pattern.compile(getMetaDataNodePath() + "/([\\w\\-]+)" + "(/datasources|/rules|/tables)?", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(configurationNodeFullPath);
        return matcher.find() ? matcher.group(1) : "";
    }
    
    /**
     * Get schema name by schema path.
     *
     * @param schemaPath schema path
     * @return schema name
     */
    public static String getSchemaNameBySchemaPath(final String schemaPath) {
        Pattern pattern = Pattern.compile(getMetaDataNodePath() + "/([\\w\\-]+)$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(schemaPath);
        return matcher.find() ? matcher.group(1) : "";
    }
    
    /**
     * Get table meta data path.
     * 
     * @param schemaName schema name
     * @param tableMetaDataPath table meta data path
     * @return table name
     */
    public static String getTableName(final String schemaName, final String tableMetaDataPath) {
        Pattern pattern = Pattern.compile(getMetaDataTablesPath(schemaName) + "/([\\w\\-]+)$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(tableMetaDataPath);
        return matcher.find() ? matcher.group(1) : "";
    }
}
