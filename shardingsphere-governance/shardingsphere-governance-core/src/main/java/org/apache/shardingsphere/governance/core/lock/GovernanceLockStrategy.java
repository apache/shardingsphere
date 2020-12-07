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

import org.apache.shardingsphere.governance.core.facade.GovernanceFacade;
import org.apache.shardingsphere.governance.core.registry.RegistryCenterNodeStatus;
import org.apache.shardingsphere.infra.lock.LockStrategy;

import java.util.Collection;

/**
 * Governance lock strategy.
 */
public final class GovernanceLockStrategy implements LockStrategy {
    
    private static final int CHECK_RETRY_MAXIMUM = 5;
    
    private static final int CHECK_RETRY_INTERVAL_SECONDS = 3;
    
    private final GovernanceFacade governanceFacade;
    
    public GovernanceLockStrategy(final GovernanceFacade governanceFacade) {
        this.governanceFacade = governanceFacade;
    }
    
    @Override
    public boolean tryLock() {
        return governanceFacade.getLockCenter().tryGlobalLock();
    }
    
    @Override
    public void releaseLock() {
        governanceFacade.getLockCenter().releaseGlobalLock();
    }
    
    @Override
    public boolean checkLock() {
        Collection<String> instanceIds = governanceFacade.getRegistryCenter().loadAllInstances();
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
                    .equalsIgnoreCase(governanceFacade.getRegistryCenter().loadInstanceData(instanceId))) {
                return false;
            }
        }
        return true;
    }
}
