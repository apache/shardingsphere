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

package io.shardingsphere.orchestration.internal.config;

import lombok.RequiredArgsConstructor;

/**
 * Data configuration node.
 *
 * @author caohao
 * @author panjuan
 */
@RequiredArgsConstructor
public final class ConfigurationNode {
    
    public static final String ROOT = "config";
    
    public static final String PROXY_NODE = "proxy";
    
    public static final String DATA_SOURCE_NODE_PATH = ROOT + "/%s/datasource";
    
    public static final String RULE_NODE_PATH = ROOT + "/%s/rule";
    
    public static final String CONFIG_MAP_NODE_PATH = ROOT + "/%s/configmap";
    
    public static final String PROPS_NODE_PATH = ROOT + "/%s/props";
    
    public static final String SERVER_NODE_PATH = ROOT + "/%s/server";
    
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
}
