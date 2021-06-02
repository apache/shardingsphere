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

package org.apache.shardingsphere.governance.core.registry.config.node;

import com.google.common.base.Joiner;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Schema metadata node.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SchemaMetadataNode {
    
    private static final String ROOT_NODE = "metadata";
    
    private static final String DATA_SOURCE_NODE = "dataSources";
    
    private static final String RULE_NODE = "rules";
    
    private static final String SCHEMA_NODE = "schema";
    
    /**
     * Get metadata data source path.
     *
     * @param schemaName schema name
     * @return data source path
     */
    public static String getMetadataDataSourcePath(final String schemaName) {
        return getFullMetadataPath(schemaName, DATA_SOURCE_NODE);
    }
    
    /**
     * Get metadata node path.
     *
     * @return metadata node path
     */
    public static String getMetadataNodePath() {
        return Joiner.on("/").join("", ROOT_NODE);
    }
    
    /**
     * Get schema name path.
     *
     * @param schemaName schema name
     * @return schema name path
     */
    public static String getSchemaNamePath(final String schemaName) {
        return Joiner.on("/").join("", ROOT_NODE, schemaName);
    }
    
    /**
     * Get rule path.
     *
     * @param schemaName schema name
     * @return rule path
     */
    public static String getRulePath(final String schemaName) {
        return getFullMetadataPath(schemaName, RULE_NODE);
    }
    
    /**
     * Get metadata schema path.
     *
     * @param schemaName schema name
     * @return schema path
     */
    public static String getMetadataSchemaPath(final String schemaName) {
        return getFullMetadataPath(schemaName, SCHEMA_NODE);
    }
    
    private static String getFullMetadataPath(final String schemaName, final String node) {
        return Joiner.on("/").join("", ROOT_NODE, schemaName, node);
    }
    
    /**
     * Get schema name.
     *
     * @param configurationNodeFullPath configuration node full path
     * @return schema name
     */
    public static String getSchemaName(final String configurationNodeFullPath) {
        Pattern pattern = Pattern.compile(getMetadataNodePath() + "/(\\w+)" + "(/datasource|/rule|/schema)?", Pattern.CASE_INSENSITIVE);
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
        Pattern pattern = Pattern.compile(getMetadataNodePath() + "/(\\w+)$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(schemaPath);
        return matcher.find() ? matcher.group(1) : "";
    }
}
