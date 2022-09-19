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
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.kv.model.GetValue;
import com.ecwid.consul.v1.kv.model.PutParams;
import com.ecwid.consul.v1.session.model.NewSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.mode.repository.cluster.consul.props.ConsulProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * Consul internal lock holder.
 */
@RequiredArgsConstructor
@Slf4j
public class ConsulInternalLockHolder {
    
    private static Logger log = LoggerFactory
            .getLogger(ConsulInternalLockHolder.class);
    
    private static final String CONSUL_ROOT_PATH = "sharding/lock";
    
    private static final long DEFAULT_LOCK_WAIT_TIME = 10L;
    
    private final Map<String, ConsulInternalLock> locks = new ConcurrentHashMap<String, ConsulInternalLock>();
    
    private final ConsulClient consulClient;
    
    private final ConsulProperties consulProps;
    
    /**
     * Get internal mutex lock.
     *
     * @param lockName lock name
     * @return internal mutex lock
     */
    public Lock getInternalMutexLock(final String lockName) {
        return getInternalReentrantMutexLock(lockName);
    }
    
    /**
     * Get internal reentrant mutex lock.
     *
     * @param lockName lock name
     * @return internal reentrant mutex lock
     */
    public Lock getInternalReentrantMutexLock(final String lockName) {
        ConsulInternalLock result = locks.get(lockName);
        if (result == null) {
            result = createLock(lockName);
            locks.put(lockName, result);
        }
        return result;
    }
    
    private ConsulInternalLock createLock(final String lockName) {
        try {
            NewSession session = new NewSession();
            session.setName(lockName);
            return new ConsulInternalLock(consulClient, lockName);
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            log.error("ConsulRepository tryLock error, lockName:{}", lockName, ex);
        }
        return null;
    }
    
    /**
     * Consul internal lock.
     */
    private static class ConsulInternalLock implements Lock {
        
        private final ConsulClient consulClient;
        
        private final ThreadLocal<String> lockSessionMap;
        
        private final String lockName;
        
        ConsulInternalLock(final ConsulClient consulClient, final String lockName) {
            this.consulClient = consulClient;
            this.lockName = lockName;
            this.lockSessionMap = new ThreadLocal<String>();
        }
        
        @Override
        public void lock() {
            try {
                PutParams putParams = new PutParams();
                NewSession session = new NewSession();
                session.setName(lockName);
                String sessionId = this.consulClient.sessionCreate(session, null).getValue();
                lockSessionMap.set(sessionId);
                String lockPath = CONSUL_ROOT_PATH + "/" + lockName;
                while (true) {
                    if (log.isDebugEnabled()) {
                        log.debug("Start acquire lock {} with session id {}", lockName, sessionId);
                    }
                    putParams.setAcquireSession(sessionId);
                    Response<Boolean> response = consulClient.setKVValue(lockPath, "lock:" + System.nanoTime(), putParams);
                    if (response.getValue()) {
                        // lock success
                        if (log.isDebugEnabled()) {
                            log.debug("Session id {} get lock {} is success", sessionId, lockName);
                        }
                        return;
                    } else {
                        if (log.isDebugEnabled()) {
                            log.debug("Acquire lock {} failed with session id {},start wait lock by release", lockName, sessionId);
                        }
                        // lock failed,exist race so retry
                        // block query if value is change so return
                        Long lockIndex = response.getConsulIndex();
                        if (lockIndex == null) {
                            lockIndex = 0L;
                        }
                        long waitTime = doWaitRelease(lockPath, lockIndex, DEFAULT_LOCK_WAIT_TIME);
                        if (log.isDebugEnabled()) {
                            log.debug("Wait lock {} time {}ms found lock is by release so to retry lock", lockName, TimeUnit.NANOSECONDS.toMillis(waitTime));
                        }
                    }
                }
                // CHECKSTYLE:OFF
            } catch (final Exception ex) {
                // CHECKSTYLE:ON
                log.error("ConsulRepository tryLock error, lockName:{}", lockName, ex);
                throw new IllegalStateException("Acquire consul lock failed", ex);
            }
        }
        
        @Override
        public boolean tryLock() {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public boolean tryLock(final long time, final TimeUnit timeUnit) {
            try {
                long lockTime = timeUnit.toSeconds(time);
                PutParams putParams = new PutParams();
                NewSession session = new NewSession();
                session.setName(lockName);
                String sessionId = this.consulClient.sessionCreate(session, null).getValue();
                lockSessionMap.set(sessionId);
                String lockPath = CONSUL_ROOT_PATH + "/" + lockName;
                while (true) {
                    putParams.setAcquireSession(sessionId);
                    Response<Boolean> response = consulClient.setKVValue(lockPath, "lock:" + System.nanoTime(), putParams);
                    if (response.getValue()) {
                        // lock success
                        return true;
                    } else {
                        // lock failed,exist race so retry
                        // block query if value is change so return
                        long waitTime = doWaitRelease(lockPath, response.getConsulIndex(), lockTime);
                        if (waitTime < lockTime) {
                            lockTime = lockTime - waitTime;
                            continue;
                        } else {
                            consulClient.sessionDestroy(sessionId, null);
                            return false;
                        }
                    }
                }
                // CHECKSTYLE:OFF
            } catch (final Exception ex) {
                // CHECKSTYLE:ON
                log.error("EtcdRepository tryLock error, lockName:{}", lockName, ex);
                return false;
            }
        }
        
        @Override
        public void unlock() {
            try {
                PutParams putParams = new PutParams();
                String sessionId = lockSessionMap.get();
                putParams.setReleaseSession(sessionId);
                String lockPath = CONSUL_ROOT_PATH + "/" + lockName;
                this.consulClient.setKVValue(lockPath, "unlock:" + System.nanoTime(), putParams).getValue();
                this.consulClient.sessionDestroy(sessionId, null);
                if (log.isDebugEnabled()) {
                    log.debug("Release lock {} with session id {} success", lockName, sessionId);
                }
                // CHECKSTYLE:OFF
            } catch (final Exception ex) {
                // CHECKSTYLE:ON
                log.error("EtcdRepository unlock error, lockName:{}", lockName, ex);
            } finally {
                lockSessionMap.remove();
            }
        }
        
        @Override
        public void lockInterruptibly() {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public Condition newCondition() {
            throw new UnsupportedOperationException();
        }
        
        private long doWaitRelease(final String key, final long valueIndex, final long waitTime) {
            long currentIndex = valueIndex;
            if (currentIndex < 0) {
                currentIndex = 0;
            }
            AtomicBoolean running = new AtomicBoolean(true);
            while (running.get()) {
                long startWaitTime = System.nanoTime();
                Response<GetValue> response = consulClient.getKVValue(key,
                        new QueryParams(waitTime, currentIndex));
                Long index = response.getConsulIndex();
                if (index != null && index > currentIndex) {
                    if (currentIndex == 0) {
                        currentIndex = index;
                        continue;
                    }
                    currentIndex = index;
                    GetValue getValue = response.getValue();
                    if (getValue == null || getValue.getValue() == null) {
                        continue;
                    }
                    if (!key.equals(getValue.getKey())) {
                        continue;
                    }
                    return System.nanoTime() - startWaitTime;
                } else if (index != null && index < currentIndex) {
                    currentIndex = 0;
                }
            }
            return -1;
        }
    }
    
}
