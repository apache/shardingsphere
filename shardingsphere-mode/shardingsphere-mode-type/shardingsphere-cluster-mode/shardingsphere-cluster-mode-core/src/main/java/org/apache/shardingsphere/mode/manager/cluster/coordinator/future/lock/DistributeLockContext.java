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

package org.apache.shardingsphere.mode.manager.cluster.coordinator.future.lock;

import com.google.common.eventbus.Subscribe;
import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.lock.ShardingSphereGlobalLock;
import org.apache.shardingsphere.infra.lock.ShardingSphereLock;
import org.apache.shardingsphere.mode.lock.LockContext;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.future.lock.event.AckLockedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.future.lock.event.AckLockReleasedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.future.lock.event.LockedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.future.lock.event.LockReleasedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.future.lock.service.GlobalLockRegistryService;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.future.lock.util.LockNodeUtil;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Distribute lock context.
 */
public final class DistributeLockContext implements LockContext {
    
    private final Map<String, ShardingSphereGlobalLock> globalLocks = new ConcurrentHashMap<>();
    
    private final InstanceContext instanceContext;
    
    private final GlobalLockRegistryService globalLockService;
    
    public DistributeLockContext(final InstanceContext instanceContext, final GlobalLockRegistryService globalLockService) {
        this.instanceContext = instanceContext;
        this.globalLockService = globalLockService;
        ShardingSphereEventBus.getInstance().register(this);
    }
    
    @Override
    public synchronized Optional<ShardingSphereLock> getSchemaLock(final String schemaName) {
        ShardingSphereGlobalLock result = globalLocks.get(schemaName);
        if (null == result) {
            result = crateGlobalLock(instanceContext.getInstance().getInstanceDefinition().getInstanceId().getId());
            globalLocks.put(schemaName, result);
        }
        return Optional.of(result);
    }
    
    private ShardingSphereGlobalLock crateGlobalLock(final String ownerInstanceId) {
        return new ShardingSphereDistributeGlobalLock(instanceContext, ownerInstanceId, globalLockService);
    }
    
    @Override
    public synchronized boolean isLockedSchema(final String schemaName) {
        return getGlobalLock(schemaName).map(shardingSphereGlobalLock -> shardingSphereGlobalLock.isLocked(schemaName)).orElse(false);
    }
    
    /**
     * Synchronize global lock.
     */
    public void synchronizeGlobalLock() {
        Collection<String> allGlobalLock = globalLockService.synchronizeAllGlobalLock();
        if (allGlobalLock.isEmpty()) {
            globalLockService.initGlobalLockRoot();
            return;
        }
        ShardingSphereGlobalLock lock;
        for (String each : allGlobalLock) {
            String[] schemaInstanceId = LockNodeUtil.parseLockName(each);
            lock = crateGlobalLock(schemaInstanceId[1]);
            lock.ackLock(schemaInstanceId[0], getCurrentInstanceId());
            globalLocks.put(schemaInstanceId[0], lock);
        }
    }
    
    private boolean isSameInstanceId(final String instanceId) {
        return getCurrentInstanceId().equals(instanceId);
    }
    
    private String getCurrentInstanceId() {
        return instanceContext.getInstance().getInstanceDefinition().getInstanceId().getId();
    }
    
    /**
     * Locked event.
     *
     * @param event locked event
     */
    @Subscribe
    public synchronized void renew(final LockedEvent event) {
        String schema = event.getSchema();
        String ownerInstanceId = event.getOwnerInstanceId();
        if (isSameInstanceId(ownerInstanceId)) {
            return;
        }
        Optional<ShardingSphereGlobalLock> globalLock = getGlobalLock(schema);
        ShardingSphereGlobalLock lock;
        if (globalLock.isPresent()) {
            lock = globalLock.get();
        } else {
            lock = crateGlobalLock(ownerInstanceId);
            globalLocks.put(schema, lock);
        }
        lock.ackLock(schema, getCurrentInstanceId());
    }
    
    /**
     * Lock released event.
     *
     * @param event lock released event
     */
    @Subscribe
    public synchronized void renew(final LockReleasedEvent event) {
        String schema = event.getSchema();
        String ownerInstanceId = event.getOwnerInstanceId();
        if (isSameInstanceId(ownerInstanceId)) {
            globalLocks.remove(schema);
            return;
        }
        getGlobalLock(schema).ifPresent(shardingSphereGlobalLock -> {
            shardingSphereGlobalLock.releaseAckLock(schema, getCurrentInstanceId());
            globalLocks.remove(schema);
        });
    }
    
    /**
     * Ack locked event.
     *
     * @param event ack locked event
     */
    @Subscribe
    public synchronized void renew(final AckLockedEvent event) {
        String schema = event.getSchema();
        String lockedInstanceId = event.getLockedInstanceId();
        getGlobalLock(schema).ifPresent(shardingSphereGlobalLock -> shardingSphereGlobalLock.addLockedInstance(lockedInstanceId));
    }
    
    /**
     * Ack lock released event.
     *
     * @param event ack lock released event.
     */
    @Subscribe
    public synchronized void renew(final AckLockReleasedEvent event) {
        String schema = event.getSchema();
        String lockedInstanceId = event.getLockedInstanceId();
        if (isSameInstanceId(lockedInstanceId)) {
            globalLocks.remove(schema);
            return;
        }
        getGlobalLock(schema).ifPresent(shardingSphereGlobalLock -> shardingSphereGlobalLock.addLockedInstance(lockedInstanceId));
    }
    
    private Optional<ShardingSphereGlobalLock> getGlobalLock(final String schemaName) {
        return Optional.ofNullable(globalLocks.get(schemaName));
    }
}
