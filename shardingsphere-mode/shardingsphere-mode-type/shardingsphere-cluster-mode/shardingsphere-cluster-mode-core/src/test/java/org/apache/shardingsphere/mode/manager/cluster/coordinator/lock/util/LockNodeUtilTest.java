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

package org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.util;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class LockNodeUtilTest {
    
    @Test
    public void assertGenerateLockLeasesNodePath() {
        String lockName = "/lock/distributed/locks/sharding_db";
        assertThat(LockNodeUtil.generateLockLeasesNodePath(lockName), is("/lock/distributed/locks/sharding_db/leases"));
    }
    
    @Test
    public void assertGenerateLockSequenceNodePath() {
        String lockName = "/lock/distributed/locks/sharding_db";
        assertThat(LockNodeUtil.generateLockSequenceNodePath(lockName), is("/lock/distributed/locks/sharding_db/sequence"));
    }
    
    @Test
    public void assertParseAckLockName() {
        String[] lockName = LockNodeUtil.parseAckLockName("sharding_db#@#127.0.0.1@3307");
        assertThat(lockName.length, is(2));
        assertThat(lockName[0], is("sharding_db"));
        assertThat(lockName[1], is("127.0.0.1@3307"));
    }
}
