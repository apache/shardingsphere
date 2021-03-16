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
import org.apache.shardingsphere.governance.core.event.model.lock.LockNotificationEvent;
import org.apache.shardingsphere.governance.core.event.model.lock.LockReleasedEvent;
import org.apache.shardingsphere.governance.core.registry.RegistryCenter;
import org.apache.shardingsphere.governance.core.registry.RegistryCenterNodeStatus;
import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;
import org.apache.shardingsphere.infra.lock.AbstractShardingSphereLock;

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
    public boolean tryLock(final String schemaName, final String tableName, final long timeoutMilliseconds) {
        return registryCenter.tryLock(schemaName, tableName, timeoutMilliseconds);
    }
    
    @Override
    public void releaseLock(final String schemaName, final String tableName) {
        registryCenter.releaseLock(schemaName, tableName);
    }
    
    /**
     * Lock instance.
     *
     * @param event lock notification event
     */
    @Subscribe
    public void doLock(final LockNotificationEvent event) {
        registryCenter.persistInstanceData(RegistryCenterNodeStatus.LOCKED.toString());
    }
    
    /**
     * Unlock instance.
     *
     * @param event lock released event
     */
    @Subscribe
    public void unlock(final LockReleasedEvent event) {
        registryCenter.persistInstanceData("");
    }
}
