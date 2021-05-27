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

package org.apache.shardingsphere.governance.core.registry.service.config.node;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Registry center node.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SchemaRuleNode {
    
    private static final String EXECUTION_NODES_NAME = "executionnodes";
    
    private static final String METADATA_NODE = "metadata";
    
    private static final String DATA_SOURCES_NODE = "dataSources";
    
    private static final String RULES_NODE = "rules";
    
    private static final String SCHEMA_NODE = "schema";
    
    private static final String USERS_NODE = "users";
    
    private static final String COMMA_SEPARATOR = ",";
    
    private static final String PATH_SEPARATOR = "/";
    
    private static final String CACHE_NODE = "cache";
    
    /**
     * Get metadata data source path.
     *
     * @param schemaName schema name
     * @return data source path
     */
    public static String getMetadataDataSourcePath(final String schemaName) {
        return getFullMetadataPath(schemaName, DATA_SOURCES_NODE);
    }
    
    /**
     * Get metadata node path.
     *
     * @return metadata node path
     */
    public static String getMetadataNodePath() {
        return Joiner.on(PATH_SEPARATOR).join("", METADATA_NODE);
    }
    
    /**
     * Get schema name path.
     *
     * @param schemaName schema name
     * @return schema name path
     */
    public static String getSchemaNamePath(final String schemaName) {
        return Joiner.on(PATH_SEPARATOR).join("", METADATA_NODE, schemaName);
    }
    
    /**
     * Get rule path.
     *
     * @param schemaName schema name
     * @return rule path
     */
    public static String getRulePath(final String schemaName) {
        return getFullMetadataPath(schemaName, RULES_NODE);
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

    /**
     * Get users path.
     *
     * @return users path
     */
    public static String getUsersNode() {
        return getFullPath(USERS_NODE);
    }
    
    private static String getFullMetadataPath(final String schemaName, final String node) {
        return Joiner.on(PATH_SEPARATOR).join("", METADATA_NODE, schemaName, node);
    }
    
    private static String getFullPath(final String node) {
        return Joiner.on(PATH_SEPARATOR).join("", node);
    }
    
    /**
     * Get schema name.
     *
     * @param configurationNodeFullPath configuration node full path
     * @return schema name
     */
    public static String getSchemaName(final String configurationNodeFullPath) {
        Pattern pattern = Pattern.compile(getMetadataNodePath() + "/(\\w+)" + "(/datasource|/rule)?", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(configurationNodeFullPath);
        return matcher.find() ? matcher.group(1) : "";
    }
    
    /**
     * Split schema name.
     *
     * @param schemaNames schema names
     * @return schema names
     */
    public static Collection<String> splitSchemaName(final String schemaNames) {
        return Strings.isNullOrEmpty(schemaNames) ? Collections.emptyList() : Splitter.on(COMMA_SEPARATOR).splitToList(schemaNames);
    }
    
    /**
     * Get all schema config paths.
     *
     * @param schemaNames schema names
     * @return config paths list
     */
    public static Collection<String> getAllSchemaConfigPaths(final Collection<String> schemaNames) {
        Collection<String> result = new ArrayList<>(Collections.singleton(getMetadataNodePath()));
        for (String schemaName : schemaNames) {
            result.add(getRulePath(schemaName));
            result.add(getMetadataDataSourcePath(schemaName));
            result.add(getMetadataSchemaPath(schemaName));
        }
        return result;
    }
    
    /**
     * Get cache path.
     *
     * @param path path
     * @return cache path
     */
    public static String getCachePath(final String path) {
        return Joiner.on(PATH_SEPARATOR).join(path, CACHE_NODE);
    }
    
    /**
     * Get all metadata schema paths.
     *
     * @param schemaNames schema names
     * @return list of schema path
     */
    public static Collection<String> getAllMetadataSchemaPaths(final Collection<String> schemaNames) {
        return schemaNames.stream().map(SchemaRuleNode::getMetadataSchemaPath).collect(Collectors.toList());
    }
    
    /**
     * Get all rule paths.
     *
     * @param schemaNames schema names
     * @return list of rule path
     */
    public static Collection<String> getAllRulePaths(final Collection<String> schemaNames) {
        return schemaNames.stream().map(SchemaRuleNode::getRulePath).collect(Collectors.toList());
    }
    
    /**
     * Get all data source paths.
     *
     * @param schemaNames schema names
     * @return list of data source path
     */
    public static Collection<String> getAllDataSourcePaths(final Collection<String> schemaNames) {
        return schemaNames.stream().map(SchemaRuleNode::getMetadataDataSourcePath).collect(Collectors.toList());
    }
    
    /**
     * Get execution nodes path.
     *
     * @return execution nodes path
     */
    public static String getExecutionNodesPath() {
        return Joiner.on("/").join("", EXECUTION_NODES_NAME);
    }
    
    /**
     * Get execution path.
     *
     * @param executionId execution id
     * @return execution path
     */
    public static String getExecutionPath(final String executionId) {
        return Joiner.on("/").join("", EXECUTION_NODES_NAME, executionId);
    }
}
