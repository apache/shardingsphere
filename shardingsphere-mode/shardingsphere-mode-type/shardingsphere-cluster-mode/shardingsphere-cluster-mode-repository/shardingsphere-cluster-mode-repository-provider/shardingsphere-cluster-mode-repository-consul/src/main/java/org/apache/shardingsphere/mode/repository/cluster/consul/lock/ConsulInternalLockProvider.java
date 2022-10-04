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

import com.ecwid.consul.ConsulException;
import com.ecwid.consul.json.GsonFactory;
import com.ecwid.consul.transport.RawResponse;
import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.OperationException;
import com.ecwid.consul.v1.QueryParams;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.kv.model.GetValue;
import com.ecwid.consul.v1.kv.model.PutParams;
import com.ecwid.consul.v1.session.model.NewSession;
import com.ecwid.consul.v1.session.model.Session;
import com.google.gson.reflect.TypeToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.shardingsphere.mode.repository.cluster.consul.ShardingSphereConsulClient;
import org.apache.shardingsphere.mode.repository.cluster.consul.ShardingSphereQueryParams;
import org.apache.shardingsphere.mode.repository.cluster.consul.props.ConsulProperties;
import org.apache.shardingsphere.mode.repository.cluster.consul.props.ConsulPropertyKey;
import org.apache.shardingsphere.mode.repository.cluster.lock.InternalLock;
import org.apache.shardingsphere.mode.repository.cluster.lock.InternalLockProvider;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;

/**
 * Consul internal lock holder.
 */
@RequiredArgsConstructor
@Slf4j
public class ConsulInternalLockProvider implements InternalLockProvider {
    
    private static final String CONSUL_ROOT_PATH = "sharding/lock";
    
    private static final String CONSUL_PATH_SEPARATOR = "/";
    
    private static final String DEFAULT_CONSUL_LOCK_VALUE = "LOCKED";
    
    private static final String DEFAULT_CONSUL_UNLOCK_VALUE = "UNLOCKED";
    
    private static final long DEFAULT_LOCK_WAIT_TIME = 5000L;
    
    private static final ScheduledThreadPoolExecutor SESSION_FLUSH_EXECUTOR = new ScheduledThreadPoolExecutor(2);
    
    private final Map<String, ConsulInternalLock> locks = new ConcurrentHashMap<String, ConsulInternalLock>();
    
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
            return new ConsulInternalLock(consulClient, lockName, consulProps);
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            log.error("ConsulRepository tryLock error, lockName:{}", lockName, ex);
        }
        return null;
    }
    
    /**
     * flush session by update ttl.
     * @param consulClient consul client
     * @param sessionId session id
     */
    public static void generatorFlushSessionTtlTask(final ConsulClient consulClient, final String sessionId) {
        SESSION_FLUSH_EXECUTOR.scheduleAtFixedRate(new Runnable() {
            
            @Override
            public void run() {
                consulClient.renewSession(sessionId, QueryParams.DEFAULT);
            }
        }, 5, 10, TimeUnit.SECONDS);
    }
    
    /**
     * Consul internal lock.
     */
    private static class ConsulInternalLock implements InternalLock {
        
        private final ConsulClient consulClient;
        
        private final ConsulProperties consulProperties;
        
        private final ThreadLocal<String> lockSessionMap;
        
        private final String lockName;
        
        ConsulInternalLock(final ConsulClient consulClient, final String lockName, final ConsulProperties consulProperties) {
            this.consulClient = consulClient;
            this.lockName = lockName;
            this.consulProperties = consulProperties;
            this.lockSessionMap = new ThreadLocal<String>();
        }
        
        // @Override
        public void lock() {
            try {
                // support reentrant lock
                if (StringUtils.isNotEmpty(lockSessionMap.get())) {
                    return;
                }
                PutParams putParams = new PutParams();
                String lockPath = CONSUL_ROOT_PATH + CONSUL_PATH_SEPARATOR + lockName;
                while (true) {
                    String sessionId = createSession(lockName);
                    putParams.setAcquireSession(sessionId);
                    Response<Boolean> response = consulClient.setKVValue(lockPath, DEFAULT_CONSUL_LOCK_VALUE, putParams);
                    if (response.getValue()) {
                        // lock success
                        lockSessionMap.set(sessionId);
                        ConsulInternalLockProvider.generatorFlushSessionTtlTask(consulClient, sessionId);
                        if (log.isDebugEnabled()) {
                            log.debug("Session id {} get lock {} is success", sessionId, lockName);
                        }
                        return;
                    } else {
                        // lock failed,exist race so retry
                        // block query if value is change so return
                        consulClient.sessionDestroy(sessionId, null);
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
        public boolean tryLock(final long timeoutMillis) {
            try {
                if (StringUtils.isNotEmpty(lockSessionMap.get())) {
                    return true;
                }
                long lockTime = timeoutMillis;
                PutParams putParams = new PutParams();
                String lockPath = CONSUL_ROOT_PATH + CONSUL_PATH_SEPARATOR + lockName;
                while (true) {
                    String sessionId = createSession(lockPath);
                    putParams.setAcquireSession(sessionId);
                    Response<Boolean> response = consulClient.setKVValue(lockPath, DEFAULT_CONSUL_LOCK_VALUE, putParams);
                    if (response.getValue()) {
                        // lock success
                        lockSessionMap.set(sessionId);
                        ConsulInternalLockProvider.generatorFlushSessionTtlTask(this.consulClient, sessionId);
                        return true;
                    } else {
                        // lock failed,exist race so retry
                        // block query if value is change so return
                        consulClient.sessionDestroy(sessionId, null);
                        long waitTime = doWaitRelease(lockPath, response.getConsulIndex(), lockTime);
                        if (waitTime < lockTime) {
                            lockTime = lockTime - waitTime;
                            continue;
                        }
                        return false;
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
                String lockPath = CONSUL_ROOT_PATH + CONSUL_PATH_SEPARATOR + lockName;
                this.consulClient.setKVValue(lockPath, DEFAULT_CONSUL_UNLOCK_VALUE, putParams).getValue();
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
        
        // @Override
        public void lockInterruptibly() {
            throw new UnsupportedOperationException();
        }
        
        // @Override
        public Condition newCondition() {
            throw new UnsupportedOperationException();
        }
        
        private String createSession(final String lockName) {
            NewSession session = new NewSession();
            session.setName(lockName);
            // lock was released by force while session is invalid
            session.setBehavior(Session.Behavior.RELEASE);
            session.setTtl(consulProperties.getValue(ConsulPropertyKey.TIME_TO_LIVE_IN_SECONDS) + "s");
            return this.consulClient.sessionCreate(session, null).getValue();
        }
        
        private long doWaitRelease(final String key, final long valueIndex, final long waitTime) {
            long currentIndex = valueIndex;
            if (currentIndex < 0) {
                currentIndex = 0;
            }
            ShardingSphereConsulClient shardingSphereConsulClient = (ShardingSphereConsulClient) consulClient;
            AtomicBoolean running = new AtomicBoolean(true);
            long waitCostTime = 0L;
            long now = System.currentTimeMillis();
            long deadlineWaitTime = now + waitTime;
            long blockWaitTime = waitTime;
            while (running.get()) {
                long startWaitTime = System.currentTimeMillis();
                if (startWaitTime >= deadlineWaitTime) {
                    // wait time is reached max
                    return waitTime;
                }
                RawResponse rawResponse = shardingSphereConsulClient.getRawClient().makeGetRequest("/v1/kv/" + key, null, new ShardingSphereQueryParams(blockWaitTime, currentIndex));
                Response<GetValue> response = warpRawResponse(rawResponse);
                Long index = response.getConsulIndex();
                waitCostTime += System.currentTimeMillis() - startWaitTime;
                blockWaitTime -= waitCostTime;
                if (index != null && index >= currentIndex) {
                    if (currentIndex == 0) {
                        currentIndex = index;
                        continue;
                    }
                    currentIndex = index;
                    GetValue getValue = response.getValue();
                    if (getValue == null || getValue.getValue() == null) {
                        return waitCostTime;
                    }
                    if (!key.equals(getValue.getKey())) {
                        continue;
                    }
                    return waitCostTime;
                } else if (index != null && index < currentIndex) {
                    currentIndex = 0;
                }
            }
            return -1;
        }
        
        private Response<GetValue> warpRawResponse(final RawResponse rawResponse) {
            if (rawResponse.getStatusCode() == 200) {
                List<GetValue> value = GsonFactory.getGson().fromJson(rawResponse.getContent(), new TypeToken<List<GetValue>>() {
                }.getType());
                
                if (value.size() == 0) {
                    return new Response<GetValue>(null, rawResponse);
                } else if (value.size() == 1) {
                    return new Response<GetValue>(value.get(0), rawResponse);
                } else {
                    throw new ConsulException("Strange response (list size=" + value.size() + ")");
                }
            } else if (rawResponse.getStatusCode() == 404) {
                return new Response<GetValue>(null, rawResponse);
            } else {
                throw new OperationException(rawResponse);
            }
        }
    }
}
