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

package org.apache.shardingsphere.governance.core.registry;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import org.apache.shardingsphere.governance.core.registry.schema.GovernanceSchema;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Registry center node.
 */
public final class RegistryCenterNode {
    
    private static final String ROOT = "states";
    
    private static final String PROXY_NODES_NAME = "proxynodes";
    
    private static final String DATA_NODES_NAME = "datanodes";
    
    private static final String PRIMARY_NODES_NAME = "primarynodes";
    
    private static final String EXECUTION_NODES_NAME = "executionnodes";
    
    private static final String METADATA_NODE = "metadata";
    
    private static final String DATA_SOURCES_NODE = "dataSources";
    
    private static final String RULES_NODE = "rules";
    
    private static final String SCHEMA_NODE = "schema";
    
    private static final String USERS_NODE = "users";

    private static final String GLOBAL_RULE_NODE = "rules";

    private static final String PRIVILEGE_NODE = "privilegenode";
    
    private static final String PROPS_NODE = "props";
    
    private static final String COMMA_SEPARATOR = ",";
    
    private static final String PATH_SEPARATOR = "/";
    
    private static final String CACHE_NODE = "cache";
    
    /**
     * Get proxy node path.
     *
     * @param instanceId instance id
     * @return proxy node path
     */
    public String getProxyNodePath(final String instanceId) {
        return Joiner.on("/").join("", ROOT, PROXY_NODES_NAME, instanceId);
    }
    
    /**
     * Get data nodes path.
     *
     * @return data nodes path
     */
    public String getDataNodesPath() {
        return Joiner.on("/").join("", ROOT, DATA_NODES_NAME);
    }
    
    /**
     * Get primary nodes path.
     *
     * @return primary nodes path
     */
    public String getPrimaryNodesPath() {
        return Joiner.on("/").join("", ROOT, PRIMARY_NODES_NAME);
    }
    
    /**
     * Get schema path.
     * 
     * @param schemaName schema name
     * @return schema path
     */
    public String getSchemaPath(final String schemaName) {
        return Joiner.on("/").join("", ROOT, DATA_NODES_NAME, schemaName);
    }
    
    /**
     * Get primary nodes schema path.
     *
     * @param schemaName schema name
     * @return schema path
     */
    public String getPrimaryNodesSchemaPath(final String schemaName) {
        return Joiner.on("/").join("", ROOT, PRIMARY_NODES_NAME, schemaName);
    }
    
    /**
     * Get data source path.
     * 
     * @param schemaName schema name
     * @param dataSourceName data source name
     * @return data source path
     */
    public String getDataSourcePath(final String schemaName, final String dataSourceName) {
        return Joiner.on("/").join("", ROOT, DATA_NODES_NAME, schemaName, dataSourceName);
    }
    
    /**
     * Get metadata data source path.
     *
     * @param schemaName schema name
     * @return data source path
     */
    public String getMetadataDataSourcePath(final String schemaName) {
        return getFullMetadataPath(schemaName, DATA_SOURCES_NODE);
    }
    
    /**
     * Get primary data source path.
     *
     * @param schemaName schema name
     * @param groupName group name
     * @return data source path
     */
    public String getPrimaryDataSourcePath(final String schemaName, final String groupName) {
        return Joiner.on("/").join("", ROOT, PRIMARY_NODES_NAME, schemaName, groupName);
    }
    
    /**
     * Get governance schema.
     *
     * @param dataSourceNodeFullPath data source node full path
     * @return governance schema
     */
    public Optional<GovernanceSchema> getGovernanceSchema(final String dataSourceNodeFullPath) {
        Pattern pattern = Pattern.compile(getDataNodesPath() + "/" + "(\\w+)/(\\S+)$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(dataSourceNodeFullPath);
        return matcher.find() ? Optional.of(new GovernanceSchema(matcher.group(1), matcher.group(2))) : Optional.empty();
    }
    
    /**
     * Get primary nodes governance schema.
     *
     * @param dataSourceNodeFullPath data source node full path
     * @return primary nodes governance schema
     */
    public Optional<GovernanceSchema> getPrimaryNodesGovernanceSchema(final String dataSourceNodeFullPath) {
        Pattern pattern = Pattern.compile(getPrimaryNodesPath() + "/" + "(\\w+)/(\\w+)$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(dataSourceNodeFullPath);
        return matcher.find() ? Optional.of(new GovernanceSchema(matcher.group(1), matcher.group(2))) : Optional.empty();
    }
    
    /**
     * Is primary data source path.
     *
     * @param dataSourceNodeFullPath data source node full path
     * @return is primary data source path
     */
    public boolean isPrimaryDataSourcePath(final String dataSourceNodeFullPath) {
        Pattern pattern = Pattern.compile(getPrimaryNodesPath() + "/" + "(\\w+)/(\\w+)$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(dataSourceNodeFullPath);
        return matcher.find();
    }
    
    /**
     * Get all schema path.
     * 
     * @param schemaNames collection of schema name
     * @return collection of schema path
     */
    public Collection<String> getAllSchemaPaths(final Collection<String> schemaNames) {
        Collection<String> result = new ArrayList<>(schemaNames.size());
        for (String schemaName : schemaNames) {
            result.add(getSchemaPath(schemaName));
            result.add(getPrimaryNodesSchemaPath(schemaName));
        }
        return result;
    }
    
    /**
     * Get proxy nodes path.
     *
     * @return proxy nodes path
     */
    public String getProxyNodesPath() {
        return Joiner.on("/").join("", ROOT, PROXY_NODES_NAME);
    }
    
    /**
     * Get metadata node path.
     *
     * @return metadata node path
     */
    public String getMetadataNodePath() {
        return Joiner.on(PATH_SEPARATOR).join("", METADATA_NODE);
    }
    
    /**
     * Get schema name path.
     *
     * @param schemaName schema name
     * @return schema name path
     */
    public String getSchemaNamePath(final String schemaName) {
        return Joiner.on(PATH_SEPARATOR).join("", METADATA_NODE, schemaName);
    }
    
    /**
     * Get rule path.
     *
     * @param schemaName schema name
     * @return rule path
     */
    public String getRulePath(final String schemaName) {
        return getFullMetadataPath(schemaName, RULES_NODE);
    }
    
    /**
     * Get metadata schema path.
     *
     * @param schemaName schema name
     * @return schema path
     */
    public String getMetadataSchemaPath(final String schemaName) {
        return getFullMetadataPath(schemaName, SCHEMA_NODE);
    }

    /**
     * Get users path.
     *
     * @return users path
     */
    public String getUsersNode() {
        return getFullPath(USERS_NODE);
    }
    
    /**
     * Get global rule node path.
     *
     * @return global rule node path
     */
    public String getGlobalRuleNode() {
        return getFullPath(GLOBAL_RULE_NODE);
    }
    
    /**
     * Get privilege node path.
     *
     * @return privilege node path
     */
    public String getPrivilegeNodePath() {
        return Joiner.on(PATH_SEPARATOR).join("", ROOT, PRIVILEGE_NODE);
    }
    
    /**
     * Get properties path.
     *
     * @return properties path
     */
    public String getPropsPath() {
        return getFullPath(PROPS_NODE);
    }
    
    private String getFullMetadataPath(final String schemaName, final String node) {
        return Joiner.on(PATH_SEPARATOR).join("", METADATA_NODE, schemaName, node);
    }
    
    private String getFullPath(final String node) {
        return Joiner.on(PATH_SEPARATOR).join("", node);
    }
    
    /**
     * Get schema name.
     *
     * @param configurationNodeFullPath configuration node full path
     * @return schema name
     */
    public String getSchemaName(final String configurationNodeFullPath) {
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
    public Collection<String> splitSchemaName(final String schemaNames) {
        return Strings.isNullOrEmpty(schemaNames) ? Collections.emptyList() : Splitter.on(COMMA_SEPARATOR).splitToList(schemaNames);
    }
    
    /**
     * Get all schema config paths.
     *
     * @param schemaNames schema names
     * @return config paths list
     */
    public Collection<String> getAllSchemaConfigPaths(final Collection<String> schemaNames) {
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
    public String getCachePath(final String path) {
        return Joiner.on(PATH_SEPARATOR).join(path, CACHE_NODE);
    }
    
    /**
     * Get all metadata schema paths.
     *
     * @param schemaNames schema names
     * @return list of schema path
     */
    public Collection<String> getAllMetadataSchemaPaths(final Collection<String> schemaNames) {
        return schemaNames.stream().map(this::getMetadataSchemaPath).collect(Collectors.toList());
    }
    
    /**
     * Get all rule paths.
     *
     * @param schemaNames schema names
     * @return list of rule path
     */
    public Collection<String> getAllRulePaths(final Collection<String> schemaNames) {
        return schemaNames.stream().map(this::getRulePath).collect(Collectors.toList());
    }
    
    /**
     * Get all data source paths.
     *
     * @param schemaNames schema names
     * @return list of data source path
     */
    public Collection<String> getAllDataSourcePaths(final Collection<String> schemaNames) {
        return schemaNames.stream().map(this::getMetadataDataSourcePath).collect(Collectors.toList());
    }
    
    /**
     * Get execution nodes path.
     *
     * @return execution nodes path
     */
    public String getExecutionNodesPath() {
        return Joiner.on("/").join("", EXECUTION_NODES_NAME);
    }
    
    /**
     * Get execution path.
     *
     * @param executionId execution id
     * @return execution path
     */
    public String getExecutionPath(final String executionId) {
        return Joiner.on("/").join("", EXECUTION_NODES_NAME, executionId);
    }
}
