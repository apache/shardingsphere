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
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.service.LockRegistryService;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public final class DistributeLockContextTest {
    
    @Test
    public void assertGetOrCreateSchemaLock() {
        DistributeLockContext distributeLockContext = new DistributeLockContext(mock(LockRegistryService.class));
        ComputeNodeInstance currentInstance = new ComputeNodeInstance(new InstanceDefinition(InstanceType.PROXY, "127.0.0.1@3307"));
        InstanceContext instanceContext = new InstanceContext(currentInstance, mock(WorkerIdGenerator.class), mock(ModeConfiguration.class), distributeLockContext);
        instanceContext.initLockContext();
        ShardingSphereLock schemaLock = distributeLockContext.getOrCreateSchemaLock("schema");
        assertNotNull(schemaLock);
    }
    
    @Test
    public void assertGetSchemaLock() {
        DistributeLockContext distributeLockContext = new DistributeLockContext(mock(LockRegistryService.class));
        ComputeNodeInstance currentInstance = new ComputeNodeInstance(new InstanceDefinition(InstanceType.PROXY, "127.0.0.1@3307"));
        InstanceContext instanceContext = new InstanceContext(currentInstance, mock(WorkerIdGenerator.class), mock(ModeConfiguration.class), distributeLockContext);
        instanceContext.initLockContext();
        distributeLockContext.getOrCreateSchemaLock("schema");
        Optional<ShardingSphereLock> schemaLock = distributeLockContext.getSchemaLock("schema");
        assertTrue(schemaLock.isPresent());
        assertTrue(schemaLock.get() instanceof ShardingSphereDistributeGlobalLock);
    }
    
    @Test
    public void assertIsLockedSchema() {
        DistributeLockContext distributeLockContext = new DistributeLockContext(mock(LockRegistryService.class));
        ComputeNodeInstance currentInstance = new ComputeNodeInstance(new InstanceDefinition(InstanceType.PROXY, "127.0.0.1@3307"));
        InstanceContext instanceContext = new InstanceContext(currentInstance, mock(WorkerIdGenerator.class), mock(ModeConfiguration.class), distributeLockContext);
        instanceContext.initLockContext();
        distributeLockContext.getOrCreateSchemaLock("schema");
        assertFalse(distributeLockContext.isLockedSchema("schema"));
    }
}
