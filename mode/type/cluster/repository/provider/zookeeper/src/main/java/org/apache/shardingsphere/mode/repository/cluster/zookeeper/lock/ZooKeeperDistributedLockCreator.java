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

import org.apache.curator.framework.CuratorFramework;
import org.apache.shardingsphere.mode.repository.cluster.lock.DistributedLock;
import org.apache.shardingsphere.mode.repository.cluster.lock.creator.DistributedLockCreator;
import org.apache.shardingsphere.mode.repository.cluster.zookeeper.props.ZookeeperProperties;

/**
 * ZooKeeper distributed lock creator.
 */
public final class ZooKeeperDistributedLockCreator implements DistributedLockCreator<CuratorFramework, ZookeeperProperties> {
    
    @Override
    public DistributedLock create(final String lockKey, final CuratorFramework client, final ZookeeperProperties props) {
        return new ZookeeperDistributedLock(lockKey, client);
    }
    
    @Override
    public String getType() {
        return "ZooKeeper";
    }
}
