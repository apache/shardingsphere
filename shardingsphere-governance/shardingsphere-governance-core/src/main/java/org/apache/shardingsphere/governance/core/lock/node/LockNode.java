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

package org.apache.shardingsphere.governance.core.lock.node;

import com.google.common.base.Joiner;

/**
 * Lock node.
 */
public final class LockNode {
    
    private static final String LOCK_NODE_ROOT = "lock";
    
    private static final String LOCKED_RESOURCES_NODE = "locked_resources";
    
    /**
     * Get lock root node path.
     * 
     * @return lock root node path
     */
    public String getLockRootNodePath() {
        return Joiner.on("/").join("", LOCK_NODE_ROOT);
    }
    
    /**
     * Get lock node path.
     * 
     * @param lockName lock name
     * @return lock node path
     */
    public String getLockNodePath(final String lockName) {
        return Joiner.on("/").join("", LOCK_NODE_ROOT, lockName);
    }
    
    /**
     * Get locked resources node path.
     *
     * @return locked resources node path
     */
    public String getLockedResourcesNodePath() {
        return Joiner.on("/").join("", LOCK_NODE_ROOT, LOCKED_RESOURCES_NODE);
    }
}
