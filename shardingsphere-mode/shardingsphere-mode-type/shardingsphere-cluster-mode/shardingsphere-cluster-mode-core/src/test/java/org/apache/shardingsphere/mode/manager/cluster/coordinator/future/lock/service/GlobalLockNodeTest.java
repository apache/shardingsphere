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

package org.apache.shardingsphere.mode.manager.cluster.coordinator.future.lock.service;

import org.junit.Test;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class GlobalLockNodeTest {
    
    @Test
    public void assertGetLockedKey() {
        Optional<String> lockName = GlobalLockNode.getLockedKey("key/lock/global/locks/schema-127.0.0.1@3307");
        assertTrue(lockName.isPresent());
        assertThat(lockName.get(), is("schema-127.0.0.1@3307"));
    }
    
    @Test
    public void assertGetAckLockedKey() {
        Optional<String> lockName = GlobalLockNode.getAckLockedKey("/lock/global/ack/schema-127.0.0.1@3308");
        assertTrue(lockName.isPresent());
        assertThat(lockName.get(), is("schema-127.0.0.1@3308"));
    }

    @Test
    public void assertGetGlobalAckNodePath() {
        assertThat(GlobalLockNode.getGlobalAckNodePath(), is("/lock/global/ack"));
    }

    @Test
    public void assertGenerateSchemaLockName() {
        assertThat(GlobalLockNode.generateSchemaLockName("schema", "instanceId"), is("/lock/global/locks/schema-instanceId"));
    }
}
