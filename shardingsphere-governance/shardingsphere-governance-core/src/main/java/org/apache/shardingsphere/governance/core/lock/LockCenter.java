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
import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;
import org.apache.shardingsphere.governance.core.event.model.lock.GlobalLockAddedEvent;
import org.apache.shardingsphere.governance.core.lock.node.LockNode;
import org.apache.shardingsphere.governance.core.registry.RegistryCenter;
import org.apache.shardingsphere.governance.core.registry.RegistryCenterNodeStatus;
import org.apache.shardingsphere.governance.core.state.GovernedState;
import org.apache.shardingsphere.governance.repository.api.RegistryRepository;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Lock center.
 */
public final class LockCenter {
    
    private final RegistryRepository registryRepository;
    
    private final RegistryCenter registryCenter;
    
    private final LockNode lockNode;
    
    private final GovernedState governedState;
    
    public LockCenter(final RegistryRepository registryRepository, final RegistryCenter registryCenter) {
        this.registryRepository = registryRepository;
        this.registryCenter = registryCenter;
        this.lockNode = new LockNode();
        this.governedState = new GovernedState();
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
        registryCenter.persistInstanceData(governedState.recoverState().toString());
    }
    
    /**
     * Try to get global lock.
     * 
     * @return true if get the lock, false if not
     */
    public boolean tryGlobalLock() {
        // TODO timeout and retry
        return registryRepository.tryLock(5, TimeUnit.SECONDS);
    }
    
    /**
     * Release global lock.
     */
    public void releaseGlobalLock() {
        registryRepository.releaseLock();
    }
    
    /**
     * Delete global lock.
     */
    public void deleteGlobalLock() {
        registryRepository.delete(lockNode.getGlobalLockNodePath());
    }
}
