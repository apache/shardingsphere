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
import com.google.common.base.Strings;
import com.google.common.reflect.TypeToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    
    @RequiredArgsConstructor
    private static class ConsulInternalLock implements InternalLock {
        
        private final ConsulClient consulClient;
        
        private final String lockName;
        
        private final ConsulProperties consulProperties;
        
        private final ThreadLocal<String> lockSessionMap = new ThreadLocal<>();
        
        @Override
        public boolean tryLock(final long timeoutMillis) {
            if (!Strings.isNullOrEmpty(lockSessionMap.get())) {
                return true;
            }
            try {
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
                        SESSION_FLUSH_EXECUTOR.scheduleAtFixedRate(() -> consulClient.renewSession(sessionId, QueryParams.DEFAULT), 5L, 10L, TimeUnit.SECONDS);
                        return true;
                    }
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
                // CHECKSTYLE:OFF
            } catch (final Exception ex) {
                // CHECKSTYLE:ON
                log.error("EtcdRepository tryLock error, lockName:{}", lockName, ex);
                return false;
            }
        }
        
        private String createSession(final String lockName) {
            NewSession session = new NewSession();
            session.setName(lockName);
            // lock was released by force while session is invalid
            session.setBehavior(Session.Behavior.RELEASE);
            session.setTtl(consulProperties.getValue(ConsulPropertyKey.TIME_TO_LIVE_SECONDS));
            return consulClient.sessionCreate(session, null).getValue();
        }
        
        private long doWaitRelease(final String key, final long valueIndex, final long waitTime) {
            long currentIndex = valueIndex;
            if (currentIndex < 0) {
                currentIndex = 0;
            }
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
                RawResponse rawResponse = ((ShardingSphereConsulClient) consulClient).getRawClient().makeGetRequest("/v1/kv/" + key, null, new ShardingSphereQueryParams(blockWaitTime, currentIndex));
                Response<GetValue> response = warpRawResponse(rawResponse);
                Long index = response.getConsulIndex();
                waitCostTime += System.currentTimeMillis() - startWaitTime;
                blockWaitTime -= waitCostTime;
                if (null != index && index >= currentIndex) {
                    if (currentIndex == 0) {
                        currentIndex = index;
                        continue;
                    }
                    currentIndex = index;
                    GetValue getValue = response.getValue();
                    if (null == getValue || null == getValue.getValue()) {
                        return waitCostTime;
                    }
                    if (!key.equals(getValue.getKey())) {
                        continue;
                    }
                    return waitCostTime;
                }
                if (null != index) {
                    currentIndex = 0;
                }
            }
            return -1;
        }
        
        private Response<GetValue> warpRawResponse(final RawResponse rawResponse) {
            if (200 == rawResponse.getStatusCode()) {
                List<GetValue> value = GsonFactory.getGson().fromJson(rawResponse.getContent(), new TypeToken<List<GetValue>>() {
                }.getType());
                if (value.isEmpty()) {
                    return new Response<>(null, rawResponse);
                }
                if (1 == value.size()) {
                    return new Response<>(value.get(0), rawResponse);
                }
                throw new ConsulException("Strange response (list size=" + value.size() + ")");
            }
            if (404 == rawResponse.getStatusCode()) {
                return new Response<>(null, rawResponse);
            }
            throw new OperationException(rawResponse);
        }
        
        @Override
        public void unlock() {
            try {
                PutParams putParams = new PutParams();
                String sessionId = lockSessionMap.get();
                putParams.setReleaseSession(sessionId);
                String lockPath = CONSUL_ROOT_PATH + CONSUL_PATH_SEPARATOR + lockName;
                consulClient.setKVValue(lockPath, DEFAULT_CONSUL_UNLOCK_VALUE, putParams);
                consulClient.sessionDestroy(sessionId, null);
                // CHECKSTYLE:OFF
            } catch (final Exception ex) {
                // CHECKSTYLE:ON
                log.error("EtcdRepository unlock error, lockName: {}", lockName, ex);
            } finally {
                lockSessionMap.remove();
            }
        }
    }
}
