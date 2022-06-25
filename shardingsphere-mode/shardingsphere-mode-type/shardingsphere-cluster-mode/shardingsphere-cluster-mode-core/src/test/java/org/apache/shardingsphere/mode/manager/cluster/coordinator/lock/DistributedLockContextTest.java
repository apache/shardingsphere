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

package org.apache.shardingsphere.mode.manager.cluster.coordinator.lock;

import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstance;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.instance.definition.InstanceDefinition;
import org.apache.shardingsphere.infra.instance.definition.InstanceType;
import org.apache.shardingsphere.infra.instance.workerid.WorkerIdGenerator;
import org.apache.shardingsphere.infra.lock.ShardingSphereLock;
import org.apache.shardingsphere.mode.manager.lock.definition.LockNameDefinitionFactory;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public final class DistributedLockContextTest {
    
    @Test
    public void assertGetDistributedLock() {
        DistributedLockContext distributedLockContext = new DistributedLockContext(mock(ClusterPersistRepository.class));
        ComputeNodeInstance currentInstance = new ComputeNodeInstance(new InstanceDefinition(InstanceType.PROXY, "127.0.0.1@3307"));
        new InstanceContext(currentInstance, mock(WorkerIdGenerator.class), mock(ModeConfiguration.class), distributedLockContext);
        assertThat(distributedLockContext.getLock(), instanceOf(ShardingSphereLock.class));
    }
    
    @Test
    public void assertTryLock() {
        ComputeNodeInstance currentInstance = new ComputeNodeInstance(new InstanceDefinition(InstanceType.PROXY, "127.0.0.1@3307"));
        DistributedLockContext distributedLockContext = new DistributedLockContext(mock(ClusterPersistRepository.class));
        new InstanceContext(currentInstance, mock(WorkerIdGenerator.class), mock(ModeConfiguration.class), distributedLockContext);
        assertNotNull(distributedLockContext.getLock());
    }
    
    @Test
    public void assertReleaseLock() {
        ComputeNodeInstance currentInstance = new ComputeNodeInstance(new InstanceDefinition(InstanceType.PROXY, "127.0.0.1@3307"));
        DistributedLockContext distributedLockContext = new DistributedLockContext(mock(ClusterPersistRepository.class));
        new InstanceContext(currentInstance, mock(WorkerIdGenerator.class), mock(ModeConfiguration.class), distributedLockContext);
        distributedLockContext.releaseLock(LockNameDefinitionFactory.newDatabaseDefinition("database"));
    }
    
    @Test
    public void assertIsLockedDatabase() {
        ComputeNodeInstance currentInstance = new ComputeNodeInstance(new InstanceDefinition(InstanceType.PROXY, "127.0.0.1@3307"));
        DistributedLockContext distributedLockContext = new DistributedLockContext(mock(ClusterPersistRepository.class));
        new InstanceContext(currentInstance, mock(WorkerIdGenerator.class), mock(ModeConfiguration.class), distributedLockContext);
        assertFalse(distributedLockContext.isLocked(LockNameDefinitionFactory.newDatabaseDefinition("database")));
    }
}
