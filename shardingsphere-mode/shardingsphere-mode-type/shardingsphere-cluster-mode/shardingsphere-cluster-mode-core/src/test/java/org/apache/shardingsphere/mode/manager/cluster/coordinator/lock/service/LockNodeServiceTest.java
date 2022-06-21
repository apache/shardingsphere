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

package org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.service;

import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.LockNodeService;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.distributed.node.DistributedLockNodeService;
import org.junit.Test;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class LockNodeServiceTest {
    
    private static final LockNodeService SERVICE = new DistributedLockNodeService();
    
    @Test
    public void assertGetLocksNodePath() {
        String locksNodePath = SERVICE.getLocksNodePath();
        assertThat(locksNodePath, is("/lock/distributed/locks"));
    }
    
    @Test
    public void assertGenerateLocksName() {
        String locksName = SERVICE.generateLocksName("sharding_db");
        assertThat(locksName, is("/lock/distributed/locks/sharding_db"));
    }
    
    @Test
    public void assertGenerateFreezeLockName() {
        String locksName = SERVICE.generateFreezeLockName("sharding_db");
        assertThat(locksName, is("/lock/distributed/locks/sharding_db/freeze"));
    }
    
    @Test
    public void assertGenerateAckLockName() {
        String globalLockedAckNodePath = SERVICE.generateAckLockName("locksName", "127.0.0.1@3307");
        assertThat(globalLockedAckNodePath, is("/lock/distributed/locks/locksName/ack/127.0.0.1@3307"));
    }
    
    @Test
    public void assertParseLocksNodePath() {
        String nodePath = "/lock/distributed/locks/sharding_db/leases/c_l_00000000";
        Optional<String> globalLocksNodePath = SERVICE.parseLocksNodePath(nodePath);
        assertTrue(globalLocksNodePath.isPresent());
        assertThat(globalLocksNodePath.get(), is("sharding_db"));
    }
    
    @Test
    public void assertParseLocksAckNodePath() {
        String nodePath = "/lock/distributed/locks/sharding_db/ack/127.0.0.1@3307";
        Optional<String> locksAckNodePath = SERVICE.parseLocksAckNodePath(nodePath);
        assertTrue(locksAckNodePath.isPresent());
        assertThat(locksAckNodePath.get(), is("sharding_db#@#127.0.0.1@3307"));
    }
}
