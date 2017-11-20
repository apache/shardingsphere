/*
 * Copyright 1999-2015 dangdang.com.
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

package io.shardingjdbc.orchestration.internal.state;

/**
 * State node.
 *
 * @author caohao
 */
public class StateNode {
    
    private static final String ROOT = "state";
    
    private static final String INSTANCES_NODE_PATH = ROOT + "/instances";
    
    private static final String DATA_SOURCES_NODE_PATH = ROOT + "/datasources";
    
    private final String name;
    
    public StateNode(final String name) {
        this.name = name;
    }
    
    /**
     * Get data source node full path.
     *
     * @return data source node full path
     */
    public String getDataSourcesNodeFullPath() {
        return String.format("/%s/%s", name, DATA_SOURCES_NODE_PATH);
    }
    
    
    /**
     * Get instance node full path.
     *
     * @param instanceId instance id
     * @return instance node full path
     */
    public String getInstancesNodeFullPath(final String instanceId) {
        return String.format("/%s/%s/%s", name, INSTANCES_NODE_PATH, instanceId);
    }
}
