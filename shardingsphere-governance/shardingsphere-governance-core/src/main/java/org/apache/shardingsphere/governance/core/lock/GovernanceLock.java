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
import org.apache.shardingsphere.governance.core.event.model.lock.GlobalLockAddedEvent;
import org.apache.shardingsphere.governance.core.event.model.lock.GlobalLockReleasedEvent;
import org.apache.shardingsphere.governance.core.registry.RegistryCenter;
import org.apache.shardingsphere.governance.core.registry.RegistryCenterNodeStatus;
import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;
import org.apache.shardingsphere.infra.lock.AbstractShardingSphereLock;
import org.apache.shardingsphere.infra.state.StateEvent;
import org.apache.shardingsphere.infra.state.StateType;

/**
 * Governance lock.
 */
public final class GovernanceLock extends AbstractShardingSphereLock {
    
    private final RegistryCenter registryCenter;
    
    public GovernanceLock(final RegistryCenter registryCenter) {
        this.registryCenter = registryCenter;
        ShardingSphereEventBus.getInstance().register(this);
    }
    
    @Override
    public boolean tryGlobalLock(final long timeoutMilliseconds) {
        return registryCenter.tryGlobalLock(timeoutMilliseconds);
    }
    
    @Override
    public void releaseGlobalLock() {
        registryCenter.releaseGlobalLock();
    }
    
    /**
     * Lock instance after global lock added.
     *
     * @param event global lock added event
     */
    @Subscribe
    public void doLock(final GlobalLockAddedEvent event) {
        ShardingSphereEventBus.getInstance().post(new StateEvent(StateType.LOCK, true));
        registryCenter.persistInstanceData(RegistryCenterNodeStatus.LOCKED.toString());
    }
    
    /**
     * Unlock instance.
     *
     * @param event global lock released event
     */
    @Subscribe
    public void unlock(final GlobalLockReleasedEvent event) {
        ShardingSphereEventBus.getInstance().post(new StateEvent(StateType.LOCK, false));
        registryCenter.persistInstanceData("");
        signalAll();
    }
}
