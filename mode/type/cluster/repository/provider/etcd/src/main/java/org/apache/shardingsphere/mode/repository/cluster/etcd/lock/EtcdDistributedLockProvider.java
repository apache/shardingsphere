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

package org.apache.shardingsphere.mode.repository.cluster.etcd.lock;

import io.etcd.jetcd.Client;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.mode.repository.cluster.etcd.props.EtcdProperties;
import org.apache.shardingsphere.mode.repository.cluster.etcd.props.EtcdPropertyKey;
import org.apache.shardingsphere.mode.repository.cluster.lock.DistributedLock;
import org.apache.shardingsphere.mode.repository.cluster.lock.DistributedLockProvider;

import java.util.HashMap;
import java.util.Map;

/**
 * Etcd distributed lock provider.
 */
@RequiredArgsConstructor
@Slf4j
public final class EtcdDistributedLockProvider implements DistributedLockProvider {
    
    private final Map<String, EtcdDistributedLock> locks = new HashMap<>();
    
    private final Client client;
    
    private final EtcdProperties etcdProps;
    
    @Override
    public synchronized DistributedLock getDistributedLock(final String lockKey) {
        EtcdDistributedLock result = locks.get(lockKey);
        if (null == result) {
            result = createLock(lockKey);
            locks.put(lockKey, result);
        }
        return result;
    }
    
    private EtcdDistributedLock createLock(final String lockKey) {
        try {
            long leaseId = client.getLeaseClient().grant(etcdProps.getValue(EtcdPropertyKey.TIME_TO_LIVE_SECONDS)).get().getID();
            return new EtcdDistributedLock(client.getLockClient(), lockKey, leaseId);
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            log.error("Create etcd internal lock failed, lockName:{}", lockKey, ex);
        }
        return null;
    }
}
