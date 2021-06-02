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

package org.apache.shardingsphere.governance.core.registry.state.node;

import com.google.common.base.Joiner;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.governance.core.schema.GovernanceSchema;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * States node.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class StatesNode {
    
    private static final String ROOT_NODE = "states";
    
    private static final String PROXY_NODE = "proxynodes";
    
    private static final String DATA_NODE = "datanodes";
    
    private static final String PRIMARY_NODE = "primarynodes";
    
    private static final String PRIVILEGE_NODE = "privilegenode";
    
    /**
     * Get proxy node path.
     *
     * @param instanceId instance id
     * @return proxy node path
     */
    public static String getProxyNodePath(final String instanceId) {
        return Joiner.on("/").join("", ROOT_NODE, PROXY_NODE, instanceId);
    }
    
    /**
     * Get data nodes path.
     *
     * @return data nodes path
     */
    public static String getDataNodesPath() {
        return Joiner.on("/").join("", ROOT_NODE, DATA_NODE);
    }
    
    /**
     * Get primary nodes path.
     *
     * @return primary nodes path
     */
    public static String getPrimaryNodesPath() {
        return Joiner.on("/").join("", ROOT_NODE, PRIMARY_NODE);
    }
    
    /**
     * Get schema path.
     * 
     * @param schemaName schema name
     * @return schema path
     */
    public static String getSchemaPath(final String schemaName) {
        return Joiner.on("/").join("", ROOT_NODE, DATA_NODE, schemaName);
    }
    
    /**
     * Get primary nodes schema path.
     *
     * @param schemaName schema name
     * @return schema path
     */
    public static String getPrimaryNodesSchemaPath(final String schemaName) {
        return Joiner.on("/").join("", ROOT_NODE, PRIMARY_NODE, schemaName);
    }
    
    /**
     * Get data source path.
     * 
     * @param schemaName schema name
     * @param dataSourceName data source name
     * @return data source path
     */
    public static String getDataSourcePath(final String schemaName, final String dataSourceName) {
        return Joiner.on("/").join("", ROOT_NODE, DATA_NODE, schemaName, dataSourceName);
    }
    
    /**
     * Get primary data source path.
     *
     * @param schemaName schema name
     * @param groupName group name
     * @return data source path
     */
    public static String getPrimaryDataSourcePath(final String schemaName, final String groupName) {
        return Joiner.on("/").join("", ROOT_NODE, PRIMARY_NODE, schemaName, groupName);
    }
    
    /**
     * Get governance schema.
     *
     * @param dataSourceNodeFullPath data source node full path
     * @return governance schema
     */
    public static Optional<GovernanceSchema> getGovernanceSchema(final String dataSourceNodeFullPath) {
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
    public static Optional<GovernanceSchema> getPrimaryNodesGovernanceSchema(final String dataSourceNodeFullPath) {
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
    public static boolean isPrimaryDataSourcePath(final String dataSourceNodeFullPath) {
        Pattern pattern = Pattern.compile(getPrimaryNodesPath() + "/" + "(\\w+)/(\\w+)$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(dataSourceNodeFullPath);
        return matcher.find();
    }
    
    /**
     * Get all schema paths.
     * 
     * @param schemaNames schema names
     * @return schema paths
     */
    public static Collection<String> getAllSchemaPaths(final Collection<String> schemaNames) {
        Collection<String> result = new ArrayList<>(schemaNames.size());
        for (String each : schemaNames) {
            result.add(getSchemaPath(each));
            result.add(getPrimaryNodesSchemaPath(each));
        }
        return result;
    }
    
    /**
     * Get proxy nodes path.
     *
     * @return proxy nodes path
     */
    public static String getProxyNodesPath() {
        return Joiner.on("/").join("", ROOT_NODE, PROXY_NODE);
    }
    
    /**
     * Get privilege node path.
     *
     * @return privilege node path
     */
    public static String getPrivilegeNodePath() {
        return Joiner.on("/").join("", ROOT_NODE, PRIVILEGE_NODE);
    }
}
