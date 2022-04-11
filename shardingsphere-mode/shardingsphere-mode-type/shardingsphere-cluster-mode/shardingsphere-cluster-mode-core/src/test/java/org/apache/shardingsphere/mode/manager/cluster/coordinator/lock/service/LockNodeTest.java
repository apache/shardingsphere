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
    public void assertGetLockNodePath() {
        assertThat(LockNode.getLockNodePath("test"), is("/lock/locks/test"));
    }
    
    @Test
    public void assertGetLockName() {
        assertThat(LockNode.getLockName("/lock/locks/sharding_db.test/_c_c2d-lock-00000").orElse(null), is("sharding_db.test"));
    }
    
    @Test
    public void assertGetLockedKey() {
        Optional<String> lockName = LockNode.getLockedName("key/lock/global/locks/schema-127.0.0.1@3307");
        assertTrue(lockName.isPresent());
        assertThat(lockName.get(), is("schema-127.0.0.1@3307"));
    }
    
    @Test
    public void assertGetAckLockedKey() {
        Optional<String> lockName = LockNode.getAckLockedName("/lock/global/ack/schema-127.0.0.1@3308");
        assertTrue(lockName.isPresent());
        assertThat(lockName.get(), is("schema-127.0.0.1@3308"));
    }
    
    @Test
    public void assertGetGlobalLocksNodePath() {
        assertThat(LockNode.getGlobalLocksNodePath(), is("/lock/global/locks"));
    }
    
    @Test
    public void assertGenerateSchemaAckLockName() {
        assertThat(LockNode.generateSchemaAckLockName("schema", "lockedInstanceId"), is("/lock/global/ack/schema-lockedInstanceId"));
    }
    
    @Test
    public void assertGetGlobalAckNodePath() {
        assertThat(LockNode.getGlobalAckNodePath(), is("/lock/global/ack"));
    }
    
    @Test
    public void assertGenerateSchemaLockName() {
        assertThat(LockNode.generateSchemaLockName("schema", "instanceId"), is("/lock/global/locks/schema-instanceId"));
    }
}
