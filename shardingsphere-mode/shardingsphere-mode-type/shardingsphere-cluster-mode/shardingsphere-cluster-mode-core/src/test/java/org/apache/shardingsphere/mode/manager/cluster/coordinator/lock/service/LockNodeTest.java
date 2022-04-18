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
    public void assertGetGlobalSchemaLocksNodePath() {
        assertThat(LockNode.getGlobalSchemaLocksNodePath(), is("/lock/global/schema/locks"));
    }
    
    @Test
    public void assertGetGlobalSchemaLockedAckNodePath() {
        assertThat(LockNode.getGlobalSchemaLockedAckNodePath(), is("/lock/global/schema/ack"));
    }
    
    @Test
    public void assertGenerateStandardLockName() {
        assertThat(LockNode.generateStandardLockName("lockName"), is("/lock/standard/locks/lockName"));
    }
    
    @Test
    public void assertGenerateGlobalSchemaLocksName() {
        assertThat(LockNode.generateGlobalSchemaLocksName("schema", "127.0.0.1@3307"), is("/lock/global/schema/locks/schema-127.0.0.1@3307"));
    }
    
    @Test
    public void assertGenerateGlobalSchemaAckLockName() {
        assertThat(LockNode.generateGlobalSchemaAckLockName("schema", "127.0.0.1@3307"), is("/lock/global/schema/ack/schema-127.0.0.1@3307"));
    }
    
    @Test
    public void assertGenerateGlobalSchemaLockReleasedNodePath() {
        assertThat(LockNode.generateGlobalSchemaLockReleasedNodePath("schema", "127.0.0.1@3307"), is("/lock/global/schema/locks/schema-127.0.0.1@3307/leases"));
    }
    
    @Test
    public void assertParseGlobalSchemaLocksNodePath() {
        String nodePath = "/lock/global/schema/locks/schema-127.0.0.1@3307";
        Optional<String> globalSchemaLocksNodePath = LockNode.parseGlobalSchemaLocksNodePath(nodePath);
        assertTrue(globalSchemaLocksNodePath.isPresent());
        assertThat(globalSchemaLocksNodePath.get(), is("schema-127.0.0.1@3307"));
    }
    
    @Test
    public void assertParseGlobalSchemaLockedAckNodePath() {
        String nodePath = "/lock/global/schema/ack/schema-127.0.0.1@3307";
        Optional<String> globalSchemaLockedAckNodePath = LockNode.parseGlobalSchemaLockedAckNodePath(nodePath);
        assertTrue(globalSchemaLockedAckNodePath.isPresent());
        assertThat(globalSchemaLockedAckNodePath.get(), is("schema-127.0.0.1@3307"));
    }
}
