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

package org.apache.shardingsphere.governance.core.lock.strategy;

import com.google.common.eventbus.Subscribe;
import org.apache.shardingsphere.governance.core.event.model.lock.GlobalLockAddedEvent;
import org.apache.shardingsphere.governance.core.registry.RegistryCenter;
import org.apache.shardingsphere.governance.core.registry.RegistryCenterNodeStatus;
import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;
import org.apache.shardingsphere.infra.lock.LockStrategy;
import org.apache.shardingsphere.infra.lock.LockStrategyType;
import org.apache.shardingsphere.infra.state.StateContext;
import org.apache.shardingsphere.infra.state.StateEvent;
import org.apache.shardingsphere.infra.state.StateType;

import java.util.concurrent.TimeUnit;

/**
 * Governance lock strategy.
 */
public final class GovernanceLockStrategy implements LockStrategy {
    
    private RegistryCenter registryCenter;
    
    /**
     * Init governance lock strategy.
     * 
     * @param registryCenter registry center
     */
    public void init(final RegistryCenter registryCenter) {
        this.registryCenter = registryCenter;
        ShardingSphereEventBus.getInstance().register(this);
    }
    
    @Override
    public boolean tryLock(final long timeout, final TimeUnit timeUnit) {
        return registryCenter.tryGlobalLock(timeout, timeUnit);
    }
    
    @Override
    public void releaseLock() {
        registryCenter.releaseGlobalLock();
    }
    
    @Override
    public boolean checkLock() {
        return registryCenter.checkLock();
    }
    
    @Override
    public String getType() {
        return LockStrategyType.GOVERNANCE.name();
    }
    
    /**
     * Switch state.
     *
     * @param event state event
     */
    @Subscribe
    public void switchState(final StateEvent event) {
        StateContext.switchState(event);
    }
    
    /**
     * Lock instance after global lock added.
     *
     * @param event global lock added event
     */
    @Subscribe
    public void doLock(final GlobalLockAddedEvent event) {
        StateContext.switchState(new StateEvent(StateType.LOCK, true));
        registryCenter.persistInstanceData(RegistryCenterNodeStatus.LOCKED.toString());
    }
}
