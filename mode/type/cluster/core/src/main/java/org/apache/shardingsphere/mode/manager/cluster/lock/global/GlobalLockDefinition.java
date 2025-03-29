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

package org.apache.shardingsphere.mode.manager.cluster.lock.global;

import org.apache.shardingsphere.mode.lock.LockDefinition;
import org.apache.shardingsphere.mode.node.path.engine.generator.NodePathGenerator;
import org.apache.shardingsphere.mode.node.path.type.lock.GlobalLockNodePath;

/**
 * Global lock definition.
 */
public final class GlobalLockDefinition implements LockDefinition {
    
    private final GlobalLockNodePath nodePath;
    
    public GlobalLockDefinition(final GlobalLock globalLock) {
        nodePath = new GlobalLockNodePath(globalLock.getName());
    }
    
    @Override
    public String getLockKey() {
        return NodePathGenerator.toPath(nodePath);
    }
}
