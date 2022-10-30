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

package org.apache.shardingsphere.mode.repository.cluster.zookeeper.lock;

import lombok.RequiredArgsConstructor;
import org.apache.curator.framework.CuratorFramework;
import org.apache.shardingsphere.mode.repository.cluster.lock.DistributedLock;
import org.apache.shardingsphere.mode.repository.cluster.lock.DistributedLockHolder;

import java.util.HashMap;
import java.util.Map;

/**
 * Curator ZooKeeper distributed lock holder.
 */
@RequiredArgsConstructor
public final class CuratorZookeeperDistributedLockHolder implements DistributedLockHolder {
    
    private final Map<String, CuratorZookeeperDistributedLock> locks = new HashMap<>();
    
    private final CuratorFramework client;
    
    @Override
    public synchronized DistributedLock getDistributedLock(final String lockKey) {
        CuratorZookeeperDistributedLock result = locks.get(lockKey);
        if (null == result) {
            result = new CuratorZookeeperDistributedLock(lockKey, client);
            locks.put(lockKey, result);
        }
        return result;
    }
}
