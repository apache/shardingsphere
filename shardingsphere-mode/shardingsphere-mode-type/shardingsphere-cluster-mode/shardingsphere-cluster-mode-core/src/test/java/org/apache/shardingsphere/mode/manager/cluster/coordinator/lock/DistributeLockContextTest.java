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
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.global.general.ShardingSphereGeneralLock;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public final class DistributeLockContextTest {
    
    @Test
    public void assertTryLockWriteDatabase() {
        ComputeNodeInstance currentInstance = new ComputeNodeInstance(new InstanceDefinition(InstanceType.PROXY, "127.0.0.1@3307"));
        DistributeLockContext distributeLockContext = new DistributeLockContext(mock(ClusterPersistRepository.class), currentInstance);
        InstanceContext instanceContext = new InstanceContext(currentInstance, mock(WorkerIdGenerator.class), mock(ModeConfiguration.class), distributeLockContext);
        instanceContext.initLockContext();
        ShardingSphereLock databaseLock = distributeLockContext.getGlobalLock("database");
        assertNotNull(databaseLock);
    }
    
    @Test
    public void assertReleaseLockWriteDatabase() {
        ComputeNodeInstance currentInstance = new ComputeNodeInstance(new InstanceDefinition(InstanceType.PROXY, "127.0.0.1@3307"));
        DistributeLockContext distributeLockContext = new DistributeLockContext(mock(ClusterPersistRepository.class), currentInstance);
        InstanceContext instanceContext = new InstanceContext(currentInstance, mock(WorkerIdGenerator.class), mock(ModeConfiguration.class), distributeLockContext);
        instanceContext.initLockContext();
        distributeLockContext.releaseLockWriteDatabase("database");
    }
    
    @Test
    public void assertIsLockedDatabase() {
        ComputeNodeInstance currentInstance = new ComputeNodeInstance(new InstanceDefinition(InstanceType.PROXY, "127.0.0.1@3307"));
        DistributeLockContext distributeLockContext = new DistributeLockContext(mock(ClusterPersistRepository.class), currentInstance);
        InstanceContext instanceContext = new InstanceContext(currentInstance, mock(WorkerIdGenerator.class), mock(ModeConfiguration.class), distributeLockContext);
        instanceContext.initLockContext();
        distributeLockContext.isLockedDatabase("database");
    }
    
    @Test
    public void assertGetOrCreateGlobalLock() {
        DistributeLockContext distributeLockContext = new DistributeLockContext(mock(ClusterPersistRepository.class), mock(ComputeNodeInstance.class));
        ComputeNodeInstance currentInstance = new ComputeNodeInstance(new InstanceDefinition(InstanceType.PROXY, "127.0.0.1@3307"));
        InstanceContext instanceContext = new InstanceContext(currentInstance, mock(WorkerIdGenerator.class), mock(ModeConfiguration.class), distributeLockContext);
        instanceContext.initLockContext();
        ShardingSphereLock databaseLock = distributeLockContext.getGlobalLock("database");
        assertNotNull(databaseLock);
    }
    
    @Test
    public void assertGetGlobalLock() {
        DistributeLockContext distributeLockContext = new DistributeLockContext(mock(ClusterPersistRepository.class), mock(ComputeNodeInstance.class));
        ComputeNodeInstance currentInstance = new ComputeNodeInstance(new InstanceDefinition(InstanceType.PROXY, "127.0.0.1@3307"));
        InstanceContext instanceContext = new InstanceContext(currentInstance, mock(WorkerIdGenerator.class), mock(ModeConfiguration.class), distributeLockContext);
        instanceContext.initLockContext();
        distributeLockContext.getGlobalLock("database");
        ShardingSphereLock databaseLock = distributeLockContext.getGlobalLock("database");
        assertTrue(databaseLock instanceof ShardingSphereGeneralLock);
    }
}
