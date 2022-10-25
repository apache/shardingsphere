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

package org.apache.shardingsphere.mode.repository.cluster.consul.lock;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.QueryParams;
import com.ecwid.consul.v1.session.model.NewSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.mode.repository.cluster.consul.props.ConsulProperties;
import org.apache.shardingsphere.mode.repository.cluster.lock.InternalLock;
import org.apache.shardingsphere.mode.repository.cluster.lock.InternalLockProvider;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Consul internal lock holder.
 */
@RequiredArgsConstructor
@Slf4j
public class ConsulInternalLockProvider implements InternalLockProvider {
    
    private static final ScheduledThreadPoolExecutor SESSION_FLUSH_EXECUTOR = new ScheduledThreadPoolExecutor(2);
    
    private final Map<String, ConsulInternalLock> locks = new ConcurrentHashMap<>();
    
    private final ConsulClient consulClient;
    
    private final ConsulProperties consulProps;
    
    @Override
    public InternalLock getInternalLock(final String lockKey) {
        return getInternalReentrantMutexLock(lockKey);
    }
    
    /**
     * Get internal mutex lock.
     *
     * @param lockName lock name
     * @return internal mutex lock
     */
    public InternalLock getInternalMutexLock(final String lockName) {
        return getInternalReentrantMutexLock(lockName);
    }
    
    /**
     * Get internal reentrant mutex lock.
     *
     * @param lockName lock name
     * @return internal reentrant mutex lock
     */
    public InternalLock getInternalReentrantMutexLock(final String lockName) {
        ConsulInternalLock result = locks.get(lockName);
        if (null == result) {
            result = createLock(lockName);
            locks.put(lockName, result);
        }
        return result;
    }
    
    private ConsulInternalLock createLock(final String lockName) {
        try {
            NewSession session = new NewSession();
            session.setName(lockName);
            return new ConsulInternalLock(consulClient, lockName, consulProps);
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            log.error("ConsulRepository tryLock error, lockName:{}", lockName, ex);
        }
        return null;
    }
    
    /**
     * Flush session by update TTL.
     * 
     * @param consulClient consul client
     * @param sessionId session id
     */
    public void generatorFlushSessionTtlTask(final ConsulClient consulClient, final String sessionId) {
        SESSION_FLUSH_EXECUTOR.scheduleAtFixedRate(() -> consulClient.renewSession(sessionId, QueryParams.DEFAULT), 5L, 10L, TimeUnit.SECONDS);
    }
}
