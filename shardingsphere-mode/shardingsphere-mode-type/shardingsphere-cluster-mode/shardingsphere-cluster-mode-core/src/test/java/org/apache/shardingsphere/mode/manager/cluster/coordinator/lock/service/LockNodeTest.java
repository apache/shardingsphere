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

import org.junit.Test;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class LockNodeTest {
    
    @Test
    public void assertGetStandardLockedNodePath() {
        assertThat(LockNode.getStandardLocksNodePath(), is("/lock/standard/locks"));
    }
    
    @Test
    public void assertGetGlobalDatabaseLocksNodePath() {
        assertThat(LockNode.getGlobalDatabaseLocksNodePath(), is("/lock/global/database/locks"));
    }
    
    @Test
    public void assertGetGlobalDatabaseLockedAckNodePath() {
        assertThat(LockNode.getGlobalDatabaseLockedAckNodePath(), is("/lock/global/database/ack"));
    }
    
    @Test
    public void assertGenerateStandardLockName() {
        assertThat(LockNode.generateStandardLockName("lockName"), is("/lock/standard/locks/lockName"));
    }
    
    @Test
    public void assertGenerateGlobalDatabaseLocksName() {
        assertThat(LockNode.generateGlobalDatabaseLocksName("database"), is("/lock/global/database/locks/database"));
    }
    
    @Test
    public void assertGenerateGlobalDatabaseAckLockName() {
        assertThat(LockNode.generateGlobalDatabaseAckLockName("database", "127.0.0.1@3307"), is("/lock/global/database/ack/database-127.0.0.1@3307"));
    }
    
    @Test
    public void assertGenerateGlobalDatabaseLockReleasedNodePath() {
        assertThat(LockNode.generateGlobalDatabaseLockReleasedNodePath("database"), is("/lock/global/database/locks/database/leases"));
    }
    
    @Test
    public void assertGenerateLockTokenNodePath() {
        assertThat(LockNode.generateLockTokenNodePath(), is("/lock/global/token"));
    }
    
    @Test
    public void assertParseGlobalDatabaseLocksNodePath() {
        String nodePath = "/lock/global/database/locks/database-127.0.0.1@3307/leases/c_l_00000000";
        Optional<String> globalDatabaseLockedAckNodePath = LockNode.parseGlobalDatabaseLocksNodePath(nodePath);
        assertTrue(globalDatabaseLockedAckNodePath.isPresent());
        assertThat(globalDatabaseLockedAckNodePath.get(), is("database-127.0.0.1@3307"));
    }
    
    @Test
    public void assertParseGlobalDatabaseLockedAckNodePath() {
        String nodePath = "/lock/global/database/ack/database-127.0.0.1@3307";
        Optional<String> globalDatabaseLockedAckNodePath = LockNode.parseGlobalDatabaseLockedAckNodePath(nodePath);
        assertTrue(globalDatabaseLockedAckNodePath.isPresent());
        assertThat(globalDatabaseLockedAckNodePath.get(), is("database-127.0.0.1@3307"));
    }
}
