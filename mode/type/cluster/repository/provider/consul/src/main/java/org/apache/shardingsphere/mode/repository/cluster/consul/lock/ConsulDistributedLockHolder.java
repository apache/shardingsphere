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
import com.ecwid.consul.v1.session.model.NewSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.mode.repository.cluster.consul.props.ConsulProperties;
import org.apache.shardingsphere.mode.repository.cluster.lock.DistributedLock;
import org.apache.shardingsphere.mode.repository.cluster.lock.DistributedLockHolder;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Consul distributed lock holder.
 */
@RequiredArgsConstructor
@Slf4j
public class ConsulDistributedLockHolder implements DistributedLockHolder {
    
    private final Map<String, ConsulDistributedLock> locks = new ConcurrentHashMap<>();
    
    private final ConsulClient consulClient;
    
    private final ConsulProperties consulProps;
    
    @Override
    public DistributedLock getDistributedLock(final String lockKey) {
        ConsulDistributedLock result = locks.get(lockKey);
        if (null == result) {
            result = createLock(lockKey);
            locks.put(lockKey, result);
        }
        return result;
    }
    
    private ConsulDistributedLock createLock(final String lockName) {
        try {
            NewSession session = new NewSession();
            session.setName(lockName);
            return new ConsulDistributedLock(consulClient, lockName, consulProps);
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            log.error("ConsulRepository tryLock error, lockName:{}", lockName, ex);
        }
        return null;
    }
    
}
