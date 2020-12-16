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
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.governance.core.event.model.lock.GlobalLockAddedEvent;
import org.apache.shardingsphere.governance.core.lock.node.LockNode;
import org.apache.shardingsphere.governance.core.registry.RegistryCenter;
import org.apache.shardingsphere.governance.core.registry.RegistryCenterNodeStatus;
import org.apache.shardingsphere.governance.core.state.GovernedState;
import org.apache.shardingsphere.governance.repository.api.RegistryRepository;
import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Lock center.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class LockCenter {
    
    private static final int CHECK_RETRY_MAXIMUM = 5;
    
    private static final int CHECK_RETRY_INTERVAL_SECONDS = 3;
    
    private static final LockCenter INSTANCE = new LockCenter();
    
    private RegistryRepository registryRepository;
    
    private RegistryCenter registryCenter;
    
    private final LockNode lockNode = new LockNode();
    
    private final GovernedState governedState = new GovernedState();
    
    /**
     * Get lock center instance.
     * 
     * @return lock center instance
     */
    public static LockCenter getInstance() {
        return INSTANCE;
    }
    
    /**
     * Initialize lock center.
     * 
     * @param registryRepository registry repository
     * @param registryCenter registry center
     */
    public void init(final RegistryRepository registryRepository, final RegistryCenter registryCenter) {
        this.registryRepository = registryRepository;
        this.registryCenter = registryCenter;
        this.registryRepository.initLock(lockNode.getGlobalLockNodePath());
        ShardingSphereEventBus.getInstance().register(this);
    }
    
    /**
     * Lock instance after global lock added.
     *
     * @param event global lock added event
     */
    @Subscribe
    public synchronized void lock(final GlobalLockAddedEvent event) {
        if (Optional.of(event).isPresent()) {
            registryCenter.persistInstanceData(governedState.addState(RegistryCenterNodeStatus.LOCKED).toString());
        }
    }
    
    /**
     * Unlock instance.
     */
    public void unlock() {
        if (governedState.getState().toString().equalsIgnoreCase(RegistryCenterNodeStatus.LOCKED.toString())) {
            registryCenter.persistInstanceData(governedState.recoverState().toString());    
        }
    }
    
    /**
     * Try to get global lock.
     * 
     * @param timeout the maximum time in milliseconds to acquire lock
     * @return true if get the lock, false if not
     */
    public boolean tryGlobalLock(final Long timeout) {
        return registryRepository.tryLock(timeout, TimeUnit.MILLISECONDS);
    }
    
    /**
     * Release global lock.
     */
    public void releaseGlobalLock() {
        registryRepository.releaseLock();
        registryRepository.delete(lockNode.getGlobalLockNodePath());
    }
    
    /**
     * Check lock state.
     * 
     * @return true if all instances were locked, else false
     */
    public boolean checkLock() {
        Collection<String> instanceIds = registryCenter.loadAllInstances();
        if (instanceIds.isEmpty()) {
            return true;
        }
        return checkOrRetry(instanceIds);
    }
    
    private boolean checkOrRetry(final Collection<String> instanceIds) {
        for (int i = 0; i < CHECK_RETRY_MAXIMUM; i++) {
            if (check(instanceIds)) {
                return true;
            }
            try {
                Thread.sleep(CHECK_RETRY_INTERVAL_SECONDS * 1000L);
                // CHECKSTYLE:OFF
            } catch (final InterruptedException ex) {
                // CHECKSTYLE:ON
            }
        }
        return false;
    }
    
    private boolean check(final Collection<String> instanceIds) {
        for (String instanceId : instanceIds) {
            if (!RegistryCenterNodeStatus.LOCKED.toString()
                    .equalsIgnoreCase(registryCenter.loadInstanceData(instanceId))) {
                return false;
            }
        }
        return true;
    }
}
