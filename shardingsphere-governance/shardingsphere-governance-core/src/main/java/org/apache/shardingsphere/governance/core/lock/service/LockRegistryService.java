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

package org.apache.shardingsphere.governance.core.lock.service;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import org.apache.shardingsphere.governance.core.GovernanceInstance;
import org.apache.shardingsphere.governance.core.registry.state.node.StatesNode;
import org.apache.shardingsphere.governance.repository.spi.RegistryCenterRepository;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

/**
 * Lock registry service.
 */
public final class LockRegistryService {
    
    private static final int CHECK_ACK_MAXIMUM = 5;
    
    private static final int CHECK_ACK_INTERVAL_SECONDS = 1;
    
    private final String instanceId;
    
    private final RegistryCenterRepository repository;
    
    public LockRegistryService(final RegistryCenterRepository repository) {
        instanceId = GovernanceInstance.getInstance().getId();
        this.repository = repository;
        initLockNode();
    }
    
    private void initLockNode() {
        repository.persist(LockNode.getLockRootNodePath(), "");
        repository.persist(LockNode.getLockedAckRootNodePah(), "");
    }
    
    /**
     * Try to get lock.
     *
     * @param lockName lock name
     * @param timeoutMilliseconds the maximum time in milliseconds to acquire lock
     * @return true if get the lock, false if not
     */
    public boolean tryLock(final String lockName, final long timeoutMilliseconds) {
        return repository.tryLock(LockNode.getLockNodePath(lockName), timeoutMilliseconds, TimeUnit.MILLISECONDS);
    }
    
    /**
     * Release lock.
     * 
     * @param lockName lock name
     */
    public void releaseLock(final String lockName) {
        repository.releaseLock(LockNode.getLockNodePath(lockName));
    }
    
    /**
     * Ack lock.
     * 
     * @param lockName lock name
     */
    public void ackLock(final String lockName) {
        repository.persistEphemeral(LockNode.getLockedAckNodePath(Joiner.on("-").join(instanceId, lockName)), LockAck.LOCKED.name());
    }
    
    /**
     * Ack unlock.
     * 
     * @param lockName lock name
     */
    public void ackUnlock(final String lockName) {
        repository.persistEphemeral(LockNode.getLockedAckNodePath(Joiner.on("-").join(instanceId, lockName)), LockAck.UNLOCKED.name());
    }
    
    /**
     * Delete lock ack.
     * 
     * @param lockName lock name
     */
    public void deleteLockAck(final String lockName) {
        repository.delete(LockNode.getLockedAckNodePath(Joiner.on("-").join(instanceId, lockName)));
    }
    
    /**
     * Check lock ack.
     * 
     * @param lockName lock name
     * @return true if all instances ack lock, false if not
     */
    public boolean checkLockAck(final String lockName) {
        boolean result = checkAck(lockName, LockAck.LOCKED.name());
        if (!result) {
            releaseLock(lockName);
        }
        return result;
    }
    
    /**
     * Check unlock ack.
     * 
     * @param lockName lock name
     * @return true if all instances ack unlock, false if not
     */
    public boolean checkUnlockAck(final String lockName) {
        return checkAck(lockName, LockAck.UNLOCKED.name());
    }
    
    private boolean checkAck(final String lockName, final String ackValue) {
        Collection<String> instanceIds = repository.getChildrenKeys(StatesNode.getProxyNodesPath());
        for (int i = 0; i < CHECK_ACK_MAXIMUM; i++) {
            if (check(instanceIds, lockName, ackValue)) {
                return true;
            }
            try {
                Thread.sleep(CHECK_ACK_INTERVAL_SECONDS * 1000L);
                // CHECKSTYLE:OFF
            } catch (final InterruptedException ex) {
                // CHECKSTYLE:ON
            }
        }
        return false;
    }
    
    private boolean check(final Collection<String> instanceIds, final String lockName, final String ackValue) {
        return instanceIds.stream().allMatch(each -> ackValue.equalsIgnoreCase(loadLockAck(each, lockName)));
    }
    
    private String loadLockAck(final String instanceId, final String lockName) {
        return Strings.nullToEmpty(repository.get(LockNode.getLockedAckNodePath(Joiner.on("-").join(instanceId, lockName))));
    }
}
