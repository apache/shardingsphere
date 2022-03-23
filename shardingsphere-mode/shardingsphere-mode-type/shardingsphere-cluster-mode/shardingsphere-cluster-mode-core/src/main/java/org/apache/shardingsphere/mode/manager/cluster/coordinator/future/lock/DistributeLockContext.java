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

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.lock.ShardingSphereGlobalLock;
import org.apache.shardingsphere.mode.lock.LockContext;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.future.lock.event.AckLockedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.future.lock.event.AckLockReleasedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.future.lock.event.LockedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.future.lock.event.LockReleasedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.future.lock.service.GlobalLockRegistryService;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Distribute lock context.
 */
@RequiredArgsConstructor
public final class DistributeLockContext implements LockContext {
    
    private static final ReadWriteLock LOCK = new ReentrantReadWriteLock();
    
    private final Map<String, ShardingSphereGlobalLock> globalLocks = new LinkedHashMap<>();
    
    private final InstanceContext instanceContext;
    
    private final GlobalLockRegistryService globalLockService;
    
    @Override
    public Optional<ShardingSphereGlobalLock> createSchemaLock(final String schemaName) {
        LOCK.writeLock().lock();
        try {
            ShardingSphereGlobalLock result = globalLocks.get(schemaName);
            if (null != result) {
                return Optional.empty();
            }
            result = new ShardingSphereDistributeGlobalLock(instanceContext, globalLockService);
            globalLocks.put(schemaName, result);
            return Optional.of(result);
        } finally {
            LOCK.writeLock().unlock();
        }
    }
    
    @Override
    public Optional<ShardingSphereGlobalLock> getSchemaLock(final String schemaName) {
        LOCK.readLock().lock();
        try {
            return Optional.ofNullable(globalLocks.get(schemaName));
        } finally {
            LOCK.readLock().unlock();
        }
    }
    
    @Override
    public boolean isLockedSchema(final String schemaName) {
        LOCK.readLock().lock();
        try {
            return getSchemaLock(schemaName).map(shardingSphereGlobalLock -> shardingSphereGlobalLock.isLocked(schemaName)).orElse(false);
        } finally {
            LOCK.readLock().lock();
        }
    }
    
    /**
     * Synchronize global lock.
     */
    public void synchronizeGlobalLock() {
        // TODO synchronize all global locks from zookeeper path at '/lock/global/locks'
        ackAllGlobalLocks();
    }
    
    private void ackAllGlobalLocks() {
        LOCK.readLock().lock();
        try {
            globalLocks.forEach((key, value) -> value.ackLock(key, getCurrentInstanceId()));
        } finally {
            LOCK.readLock().lock();
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
    public void renew(final LockedEvent event) {
        String schema = event.getSchema();
        String ownerInstanceId = event.getOwnerInstanceId();
        if (isSameInstanceId(ownerInstanceId)) {
            return;
        }
        Optional<ShardingSphereGlobalLock> globalLock = createSchemaLock(schema);
        globalLock.ifPresent(shardingSphereGlobalLock -> shardingSphereGlobalLock.ackLock(schema, getCurrentInstanceId()));
    }
    
    /**
     * Lock released event.
     *
     * @param event lock released event
     */
    public void renew(final LockReleasedEvent event) {
        String schema = event.getSchema();
        String ownerInstanceId = event.getOwnerInstanceId();
        if (isSameInstanceId(ownerInstanceId)) {
            removeGlobalLock(schema);
            return;
        }
        getSchemaLock(schema).ifPresent(shardingSphereGlobalLock -> {
            shardingSphereGlobalLock.releaseAckLock(schema, getCurrentInstanceId());
            removeGlobalLock(schema);
        });
    }
    
    /**
     * Ack locked event.
     *
     * @param event ack locked event
     */
    public void renew(final AckLockedEvent event) {
        String schema = event.getSchema();
        String lockedInstanceId = event.getLockedInstanceId();
        Optional<ShardingSphereGlobalLock> globalLock = getSchemaLock(schema);
        globalLock.ifPresent(shardingSphereGlobalLock -> shardingSphereGlobalLock.addLockedInstance(lockedInstanceId));
    }
    
    /**
     * Ack lock released event.
     *
     * @param event ack lock released event.
     */
    public void renew(final AckLockReleasedEvent event) {
        String schema = event.getSchema();
        String lockedInstanceId = event.getLockedInstanceId();
        if (isSameInstanceId(lockedInstanceId)) {
            removeGlobalLock(schema);
            return;
        }
        Optional<ShardingSphereGlobalLock> globalLock = getSchemaLock(schema);
        globalLock.ifPresent(shardingSphereGlobalLock -> shardingSphereGlobalLock.addLockedInstance(lockedInstanceId));
    }
    
    private void removeGlobalLock(final String schema) {
        LOCK.writeLock().lock();
        try {
            globalLocks.remove(schema);
        } finally {
            LOCK.writeLock().lock();
        }
    }
}
