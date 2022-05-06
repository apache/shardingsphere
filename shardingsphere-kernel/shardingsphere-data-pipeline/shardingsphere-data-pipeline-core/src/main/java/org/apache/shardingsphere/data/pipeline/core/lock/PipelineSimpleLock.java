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

package org.apache.shardingsphere.data.pipeline.core.lock;

import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.core.constant.DataPipelineConstants;
import org.apache.shardingsphere.data.pipeline.core.context.PipelineContext;
import org.apache.shardingsphere.infra.lock.LockContext;

import java.util.Map;

/**
 * Pipeline simple lock.
 */
@Slf4j
// TODO extract interface and factory
public final class PipelineSimpleLock {
    
    private static volatile PipelineSimpleLock instance;
    
    private final LockContext lockContext;
    
    private final Map<String, Boolean> lockNameLockedMap;
    
    private PipelineSimpleLock() {
        lockNameLockedMap = Maps.newConcurrentMap();
        lockContext = PipelineContext.getContextManager().getInstanceContext().getLockContext();
    }
    
    /**
     * Get instance.
     *
     * @return instance
     */
    public static PipelineSimpleLock getInstance() {
        if (null == instance) {
            synchronized (PipelineSimpleLock.class) {
                if (null == instance) {
                    instance = new PipelineSimpleLock();
                }
            }
        }
        return instance;
    }
    
    /**
     * Try to lock.
     *
     * @param lockName lock name
     * @param timeoutMills the maximum time in milliseconds to acquire lock
     * @return true if lock got, else false
     */
    public boolean tryLock(final String lockName, final long timeoutMills) {
        boolean result = lockContext.getOrCreateGlobalLock(lockName).tryLock(decorateLockName(lockName), timeoutMills);
        if (result) {
            lockNameLockedMap.put(lockName, true);
        }
        log.info("tryLock, lockName={}, timeoutMills={}, result={}", lockName, timeoutMills, result);
        return result;
    }
    
    /**
     * Release lock.
     *
     * @param lockName lock name
     */
    public void releaseLock(final String lockName) {
        log.info("releaseLock, lockName={}", lockName);
        if (lockNameLockedMap.getOrDefault(lockName, false)) {
            lockNameLockedMap.remove(lockName);
            lockContext.getOrCreateGlobalLock(lockName).releaseLock(lockName);
        }
    }
    
    private String decorateLockName(final String lockName) {
        return DataPipelineConstants.DATA_PIPELINE_NODE_NAME + "-" + lockName;
    }
}
