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

package org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.manager.state;

import org.apache.shardingsphere.infra.lock.LockNameDefinition;
import org.apache.shardingsphere.spi.annotation.SingletonSPI;
import org.apache.shardingsphere.spi.type.required.RequiredSPI;

/**
 * Lock state context.
 */
@SingletonSPI
public interface LockStateContext extends RequiredSPI {
    
    /**
     * Register lock state.
     *
     * @param lockName lock name definition
     */
    void register(LockNameDefinition lockName);
    
    /**
     * Un-register lock state.
     *
     * @param lockName lock name definition
     */
    void unregister(LockNameDefinition lockName);
    
    /**
     * Is locked.
     *
     * @param lockName lock name definition
     * @return is locked or not
     */
    boolean isLocked(LockNameDefinition lockName);
}
