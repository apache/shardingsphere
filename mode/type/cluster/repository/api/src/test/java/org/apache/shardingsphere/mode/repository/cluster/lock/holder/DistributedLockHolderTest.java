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

package org.apache.shardingsphere.mode.repository.cluster.lock.holder;

import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;
import org.apache.shardingsphere.mode.repository.cluster.lock.DistributedLock;
import org.apache.shardingsphere.mode.repository.cluster.lock.impl.DefaultDistributedLock;
import org.apache.shardingsphere.mode.repository.cluster.lock.impl.props.DefaultLockTypedProperties;
import org.junit.Before;
import org.junit.Test;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

public final class DistributedLockHolderTest {
    
    private DistributedLockHolder defaultDistributedLockHolder;
    
    @Before
    public void setUp() {
        defaultDistributedLockHolder = new DistributedLockHolder("default", mock(ClusterPersistRepository.class), new DefaultLockTypedProperties(new Properties()));
    }
    
    @Test
    public void assertGetDistributedLock() {
        DistributedLock distributedLock = defaultDistributedLockHolder.getDistributedLock("lock/key");
        assertThat(distributedLock, instanceOf(DefaultDistributedLock.class));
    }
}
