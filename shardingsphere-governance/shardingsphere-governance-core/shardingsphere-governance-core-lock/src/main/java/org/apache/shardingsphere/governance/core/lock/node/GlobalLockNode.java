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
 * Global lock node.
 */
public final class GlobalLockNode {
    
    private static final String GLOBAL_LOCK_NODE = "glock";
    
    /**
     * Get global lock node path.
     * 
     * @return global lock node path
     */
    public String getGlobalLockNodePath() {
        return Joiner.on("/").join("", GLOBAL_LOCK_NODE);
    }
}
