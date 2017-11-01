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

import io.shardingjdbc.orchestration.internal.util.IpUtils;

import java.lang.management.ManagementFactory;

/**
 * Instance state node.
 * 
 * @author caohao
 */
public final class InstanceStateNode {
    
    public static final String ROOT = "state";
    
    public static final String INSTANCES_NODE_PATH = ROOT + "/instances";
    
    private static final String DELIMITER = "@-@";
    
    private final String name;
    
    private final String instanceId;
    
    public InstanceStateNode(final String name) {
        this.name = name;
        instanceId = IpUtils.getIp() + DELIMITER + ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
    }
    
    /**
     * Get node full path.
     *
     * @return node full path
     */
    public String getFullPath() {
        return String.format("/%s/%s/%s", name, INSTANCES_NODE_PATH, instanceId);
    }
}
