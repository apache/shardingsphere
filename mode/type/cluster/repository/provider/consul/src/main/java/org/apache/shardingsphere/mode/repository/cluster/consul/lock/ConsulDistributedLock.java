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

import com.orbitz.consul.Consul;
import com.orbitz.consul.model.session.ImmutableSession;
import com.orbitz.consul.model.session.Session;
import com.orbitz.consul.model.session.SessionCreatedResponse;
import org.apache.shardingsphere.mode.repository.cluster.lock.DistributedLock;

import java.util.UUID;

/**
 * Consul distributed lock.
 */
public final class ConsulDistributedLock implements DistributedLock {
    
    private final String lockKey;
    
    private final Consul consulClient;
    
    private String sessionId;
    
    public ConsulDistributedLock(final String lockKey, final Consul consulClient) {
        this.lockKey = lockKey;
        this.consulClient = consulClient;
    }
    
    @Override
    public boolean tryLock(final long timeoutMillis) {
        String sessionValue = "session_" + UUID.randomUUID();
        Session session = ImmutableSession.builder().name(sessionValue).ttl(String.format("%ss", timeoutMillis / 1000)).build();
        SessionCreatedResponse response = consulClient.sessionClient().createSession(session);
        sessionId = response.getId();
        return consulClient.keyValueClient().acquireLock(lockKey, sessionValue, sessionId);
    }
    
    @Override
    public void unlock() {
        consulClient.keyValueClient().releaseLock(lockKey, sessionId);
        consulClient.sessionClient().destroySession(sessionId);
    }
}
