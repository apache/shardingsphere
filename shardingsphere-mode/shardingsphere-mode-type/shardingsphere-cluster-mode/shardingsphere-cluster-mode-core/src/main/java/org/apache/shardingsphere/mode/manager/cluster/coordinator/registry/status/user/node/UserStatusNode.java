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

package org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.user.node;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.StatusNode;

/**
 * User status node.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UserStatusNode {
    
    private static final String PRIVILEGE_NODES = "privilege_nodes";
    
    /**
     * Get privilege node path.
     *
     * @return privilege node path
     */
    public static String getPrivilegeNodePath() {
        return String.join("/", "", StatusNode.ROOT_NODE, PRIVILEGE_NODES);
    }
}
