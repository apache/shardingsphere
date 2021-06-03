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

package org.apache.shardingsphere.governance.core.lock;

import com.google.common.eventbus.Subscribe;
import org.apache.shardingsphere.governance.core.lock.service.LockRegistryService;
import org.apache.shardingsphere.governance.core.lock.event.LockNotificationEvent;
import org.apache.shardingsphere.governance.core.lock.event.LockReleasedEvent;
import org.apache.shardingsphere.governance.core.registry.config.event.props.PropertiesChangedEvent;
import org.apache.shardingsphere.governance.repository.spi.RegistryCenterRepository;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.properties.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;
import org.apache.shardingsphere.infra.lock.InnerLockReleasedEvent;
import org.apache.shardingsphere.infra.lock.ShardingSphereLock;

import java.util.ArrayList;
import java.util.Collection;

/**
 * ShardingSphere distribute lock.
 */
public final class ShardingSphereDistributeLock implements ShardingSphereLock {
    
    private final LockRegistryService lockService;
    
    private long lockTimeoutMilliseconds;
    
    private final Collection<String> lockedResources = new ArrayList<>();
    
    public ShardingSphereDistributeLock(final RegistryCenterRepository registryCenterRepository, final long lockTimeoutMilliseconds) {
        lockService = new LockRegistryService(registryCenterRepository);
        this.lockTimeoutMilliseconds = lockTimeoutMilliseconds;
        ShardingSphereEventBus.getInstance().register(this);
    }
    
    @Override
    public boolean tryLock(final String lockName) {
        return lockService.tryLock(lockName, lockTimeoutMilliseconds) && lockService.checkLockAck(lockName);
    }
    
    @Override
    public boolean tryLock(final String lockName, final long timeoutMilliseconds) {
        return lockService.tryLock(lockName, timeoutMilliseconds) && lockService.checkLockAck(lockName);
    }
    
    @Override
    public void releaseLock(final String lockName) {
        lockService.releaseLock(lockName);
        lockService.checkUnlockAck(lockName);
    }
    
    @Override
    public boolean isLocked(final String lockName) {
        return lockedResources.contains(lockName);
    }
    
    @Override
    public boolean isReleased(final String lockName) {
        return lockService.checkUnlockAck(lockName);
    }
    
    @Override
    public long getDefaultTimeOut() {
        return lockTimeoutMilliseconds;
    }
    
    /**
     * Renew lock time out.
     *
     * @param event properties changed event
     */
    @Subscribe
    public void renew(final PropertiesChangedEvent event) {
        ConfigurationProperties props = new ConfigurationProperties(event.getProps());
        lockTimeoutMilliseconds = props.<Long>getValue(ConfigurationPropertyKey.LOCK_WAIT_TIMEOUT_MILLISECONDS);
    }
    
    /**
     * Add locked resource and ack lock.
     * 
     * @param event lock notification event
     */
    @Subscribe
    public void renew(final LockNotificationEvent event) {
        lockedResources.add(event.getLockName());
        lockService.ackLock(event.getLockName());
    }
    
    /**
     * Release lock.
     * 
     * @param event lock released event
     */
    @Subscribe
    public void renew(final LockReleasedEvent event) {
        lockService.deleteLockAck(event.getLockName());
    }
    
    /**
     * Release inner lock.
     * 
     * @param event inner lock released event
     */
    @Subscribe
    public void renew(final InnerLockReleasedEvent event) {
        releaseInnerLock(event.getLockName());
    }
    
    private void releaseInnerLock(final String lockName) {
        if (lockedResources.contains(lockName)) {
            lockedResources.remove(lockName);
            lockService.ackUnlock(lockName);
        }
    }
}
