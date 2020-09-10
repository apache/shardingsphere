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

package org.apache.shardingsphere.proxy.backend.communication.jdbc.connection;

import lombok.RequiredArgsConstructor;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Connection status handler.
 */
@RequiredArgsConstructor
public final class ConnectionStatusHandler {
    
    private final AtomicReference<ConnectionStatus> status = new AtomicReference<>(ConnectionStatus.INIT);
    
    private final ResourceLock resourceLock;
    
    /**
     * Change connection status.
     *
     * @param status status to be updated
     */
    public void changeStatus(final ConnectionStatus status) {
        this.status.set(status);
        if (ConnectionStatus.TERMINATED == status) {
            resourceLock.doNotify();
        }
    }
    
    /**
     * Get current connection status.
     *
     * @return connection status
     */
    public ConnectionStatus getStatus() {
        return status.get();
    }
    
    /**
     * Change connection status to running if necessary.
     */
    public void setRunningStatusIfNecessary() {
        if (ConnectionStatus.TRANSACTION != status.get() && ConnectionStatus.RUNNING != status.get()) {
            status.getAndSet(ConnectionStatus.RUNNING);
        }
    }
    
    /**
     * Judge whether connection is in transaction or not.
     *
     * @return true or false
     */
    public boolean isInTransaction() {
        return ConnectionStatus.TRANSACTION == status.get();
    }
    
    /**
     * Notify connection to finish wait if necessary.
     */
    void doNotifyIfNecessary() {
        if (status.compareAndSet(ConnectionStatus.RUNNING, ConnectionStatus.RELEASE) || status.compareAndSet(ConnectionStatus.TERMINATED, ConnectionStatus.RELEASE)) {
            resourceLock.doNotify();
        }
    }
    
    /**
     * Wait until connection is released if necessary.
     */
    public void waitUntilConnectionReleasedIfNecessary() {
        if (ConnectionStatus.RUNNING == status.get() || ConnectionStatus.TERMINATED == status.get()) {
            while (!status.compareAndSet(ConnectionStatus.RELEASE, ConnectionStatus.RUNNING)) {
                resourceLock.doAwaitUntil();
            }
        }
    }
}
