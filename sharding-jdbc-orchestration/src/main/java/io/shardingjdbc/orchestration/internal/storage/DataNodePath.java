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

package io.shardingjdbc.orchestration.internal.storage;

import lombok.RequiredArgsConstructor;

/**
 * Data node path.
 * 
 * @author caohao
 */
@RequiredArgsConstructor
public final class DataNodePath {
    
    private static final String CONFIG_NODE = "config";
    
    private static final String INSTANCES_NODE = "instances";
    
    private final String name;
    
    /**
     * Get node full path.
     *
     * @param node node name
     * @return node full path
     */
    public String getFullPath(final String node) {
        return String.format("/%s/%s", name, node);
    }
    
    /**
     * Get config node path.
     *
     * @return instance node root path
     */
    public String getConfigNodePath() {
        return String.format("/%s/%s", name, CONFIG_NODE);
    }
    
    /**
    /**
     * Get instance node root path.
     *
     * @return instance node root path
     */
    public String getInstancesNodePath() {
        return String.format("/%s/%s", name, INSTANCES_NODE);
    }
    
    /**
     * Get instance node path by instance id.
     *
     * @param instanceId instance id
     * @return instance node path
     */
    public String getInstanceNodePath(final String instanceId) {
        return String.format("%s/%s", getInstancesNodePath(), instanceId);
    }
}
