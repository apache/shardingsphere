/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.shardingproxy.backend.jdbc.connection;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Connection state handler
 *
 * @author zhaojun
 */
public class ConnectionStateHandler {
    
    private volatile AtomicReference<ConnectionStatus> status = new AtomicReference<>(ConnectionStatus.INIT);
    
    private final Object lock = new Object();
    
    /**
     * Change connection status using compare and set.
     *
     * @param expect expect status
     * @param update new update status
     * @return boolean set succeed or failed
     */
    public boolean compareAndSetStatus(final ConnectionStatus expect, final ConnectionStatus update) {
        return status.compareAndSet(expect, update);
    }
    
    /**
     * Change connection status using get and set.
     *
     * @param update new update status
     */
    public void getAndSetStatus(final ConnectionStatus update) {
        status.getAndSet(update);
    }
    
    /**
     * Get current connection status.
     *
     * @return connection status
     */
    public ConnectionStatus getStatus() {
        return status.get();
    }
    
    public void changeRunningStatusIfNecessary() {
        if (ConnectionStatus.INIT == status.get() || ConnectionStatus.TERMINATED == status.get()) {
            status.getAndSet(ConnectionStatus.RUNNING);
        }
    }
    
    public boolean isInTransaction() {
        return ConnectionStatus.TRANSACTION == status.get();
    }
    
    
    public void doNotifyIfNecessary() {
        if (status.compareAndSet(ConnectionStatus.RUNNING, ConnectionStatus.RELEASE)) {
            synchronized (lock) {
                lock.notifyAll();
            }
        }
    }
    
    public void waitUntilConnectionReleasedIfNecessary() throws InterruptedException {
        if (ConnectionStatus.TRANSACTION != status.get() && ConnectionStatus.INIT != status.get() && ConnectionStatus.TERMINATED != status.get()) {
            while (!compareAndSetStatus(ConnectionStatus.RELEASE, ConnectionStatus.RUNNING)) {
                synchronized (lock) {
                    lock.wait(1000);
                }
            }
        }
    }
}
