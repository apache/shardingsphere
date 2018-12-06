/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.orchestration.internal.registry.state.node;

import com.google.common.base.Joiner;
import io.shardingsphere.orchestration.internal.registry.state.schema.OrchestrationShardingSchema;
import lombok.RequiredArgsConstructor;

/**
 * State node.
 *
 * @author caohao
 */
@RequiredArgsConstructor
public final class StateNode {
    
    private static final String ROOT = "state";
    
    private static final String INSTANCES_NODE_PATH = "instances";
    
    private static final String DATA_SOURCES_NODE_PATH = "datasources";
    
    private final String name;
    
    /**
     * Get instance node full path.
     *
     * @param instanceId instance id
     * @return instance node full path
     */
    public String getInstancesNodeFullPath(final String instanceId) {
        return Joiner.on("/").join("", name, ROOT, INSTANCES_NODE_PATH, instanceId);
    }
    
    /**
     * Get data source node full root path.
     *
     * @return data source node full root path
     */
    public String getDataSourcesNodeFullRootPath() {
        return Joiner.on("/").join("", name, ROOT, DATA_SOURCES_NODE_PATH);
    }
    
    /**
     * Get data source node full path.
     *
     * @param schemaDataSourceName schema name and data source name
     * @return data source node full path
     */
    public String getDataSourcesNodeFullPath(final String schemaDataSourceName) {
        return Joiner.on("/").join("", name, ROOT, DATA_SOURCES_NODE_PATH, schemaDataSourceName);
    }
    
    /**
     * Get orchestration sharding schema.
     *
     * @param dataSourceNodeFullPath data source node full path
     * @return orchestration sharding schema
     */
    public OrchestrationShardingSchema getOrchestrationShardingSchema(final String dataSourceNodeFullPath) {
        return new OrchestrationShardingSchema(dataSourceNodeFullPath.replace(getDataSourcesNodeFullRootPath() + '/', ""));
    }
}
