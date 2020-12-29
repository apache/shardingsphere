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
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.governance.core.registry.schema.GovernanceSchema;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Registry center node.
 */
@RequiredArgsConstructor
public final class RegistryCenterNode {
    
    private static final String ROOT = "states";
    
    private static final String PROXY_NODES_NAME = "proxynodes";
    
    private static final String DATA_NODES_NAME = "datanodes";
    
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
     * Get schema path.
     * 
     * @param schemaName schema name
     * @return schema path
     */
    public String getSchemaPath(final String schemaName) {
        return Joiner.on("/").join("", ROOT, DATA_NODES_NAME, schemaName);
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
     * Get governance schema.
     *
     * @param dataSourceNodeFullPath data source node full path
     * @return governance schema
     */
    public Optional<GovernanceSchema> getGovernanceSchema(final String dataSourceNodeFullPath) {
        Pattern pattern = Pattern.compile(getDataNodesPath() + "/" + "(\\w+)/(\\w+)$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(dataSourceNodeFullPath);
        return matcher.find() ? Optional.of(new GovernanceSchema(matcher.group(1), matcher.group(2))) : Optional.empty();
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
}
