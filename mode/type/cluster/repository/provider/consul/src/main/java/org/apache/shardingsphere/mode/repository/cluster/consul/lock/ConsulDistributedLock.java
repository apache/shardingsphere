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
import com.ecwid.consul.v1.session.model.Session.Behavior;
import com.google.common.base.Strings;
import com.google.common.reflect.TypeToken;
import org.apache.shardingsphere.mode.repository.cluster.consul.ShardingSphereConsulClient;
import org.apache.shardingsphere.mode.repository.cluster.consul.ShardingSphereQueryParams;
import org.apache.shardingsphere.mode.repository.cluster.consul.props.ConsulProperties;
import org.apache.shardingsphere.mode.repository.cluster.consul.props.ConsulPropertyKey;
import org.apache.shardingsphere.mode.repository.cluster.lock.DistributedLock;

import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Consul distributed lock.
 */
public final class ConsulDistributedLock implements DistributedLock {
    
    private static final String LOCK_PATH_PATTERN = "lock/%s";
    
    private static final String LOCK_VALUE = "LOCKED";
    
    private static final String UNLOCK_VALUE = "UNLOCKED";
    
    private static final ScheduledThreadPoolExecutor SESSION_FLUSH_EXECUTOR = new ScheduledThreadPoolExecutor(2);
    
    private final String lockPath;
    
    private final ConsulClient client;
    
    private final String timeToLiveSeconds;
    
    private final ThreadLocal<String> lockSessionMap;
    
    public ConsulDistributedLock(final String lockKey, final ConsulClient client, final ConsulProperties props) {
        lockPath = String.format(LOCK_PATH_PATTERN, lockKey);
        this.client = client;
        timeToLiveSeconds = props.getValue(ConsulPropertyKey.TIME_TO_LIVE_SECONDS);
        lockSessionMap = new ThreadLocal<>();
    }
    
    @Override
    public boolean tryLock(final long timeoutMillis) {
        if (!Strings.isNullOrEmpty(lockSessionMap.get())) {
            return true;
        }
        try {
            PutParams putParams = new PutParams();
            long remainingMillis = timeoutMillis;
            while (true) {
                String sessionId = createSessionId();
                putParams.setAcquireSession(sessionId);
                Response<Boolean> response = client.setKVValue(lockPath, LOCK_VALUE, putParams);
                if (response.getValue()) {
                    lockSessionMap.set(sessionId);
                    SESSION_FLUSH_EXECUTOR.scheduleAtFixedRate(() -> client.renewSession(sessionId, QueryParams.DEFAULT), 5L, 10L, TimeUnit.SECONDS);
                    return true;
                }
                client.sessionDestroy(sessionId, null);
                long waitingMillis = waitUntilRelease(response.getConsulIndex(), remainingMillis);
                if (waitingMillis >= remainingMillis) {
                    return false;
                }
                remainingMillis -= waitingMillis;
            }
            // CHECKSTYLE:OFF
        } catch (final Exception ignored) {
            // CHECKSTYLE:ON
            return false;
        }
    }
    
    private String createSessionId() {
        NewSession session = new NewSession();
        session.setName(lockPath);
        session.setTtl(timeToLiveSeconds);
        session.setBehavior(Behavior.RELEASE);
        return client.sessionCreate(session, null).getValue();
    }
    
    private long waitUntilRelease(final long valueIndex, final long timeoutMillis) {
        long currentIndex = valueIndex < 0 ? 0 : valueIndex;
        long spentMillis = 0L;
        long timeoutTime = System.currentTimeMillis() + timeoutMillis;
        long remainingMillis = timeoutMillis;
        while (true) {
            long startTime = System.currentTimeMillis();
            if (startTime >= timeoutTime) {
                return timeoutMillis;
            }
            Response<GetValue> response = getResponse(
                    ((ShardingSphereConsulClient) client).getRawClient().makeGetRequest("/v1/kv/" + lockPath, null, new ShardingSphereQueryParams(remainingMillis, currentIndex)));
            spentMillis += System.currentTimeMillis() - startTime;
            remainingMillis -= spentMillis;
            Long index = response.getConsulIndex();
            if (null != index && index >= currentIndex) {
                if (0 != currentIndex && (null == response.getValue() || null == response.getValue().getValue() || lockPath.equals(response.getValue().getKey()))) {
                    return spentMillis;
                }
                currentIndex = index;
                continue;
            }
            if (null != index) {
                currentIndex = 0;
            }
        }
    }
    
    private Response<GetValue> getResponse(final RawResponse rawResponse) {
        if (200 == rawResponse.getStatusCode()) {
            List<GetValue> value = GsonFactory.getGson().fromJson(rawResponse.getContent(), new TypeToken<List<GetValue>>() {
                
                private static final long serialVersionUID = -5065504617907914417L;
                
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
            client.setKVValue(lockPath, UNLOCK_VALUE, putParams);
            client.sessionDestroy(sessionId, null);
            // CHECKSTYLE:OFF
        } catch (final Exception ignored) {
            // CHECKSTYLE:ON
        } finally {
            lockSessionMap.remove();
        }
    }
}
