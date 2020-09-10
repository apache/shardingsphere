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

import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertTrue;

public final class ConnectionStateHandlerTest {
    
    private final ResourceLock resourceLock = new ResourceLock();
    
    private final ConnectionStateHandler connectionStateHandler = new ConnectionStateHandler(resourceLock);
    
    @Test
    public void assertWaitUntilConnectionReleaseForNoneTransaction() throws InterruptedException {
        AtomicBoolean flag = new AtomicBoolean(true);
        Thread waitThread = new Thread(() -> {
            connectionStateHandler.setStatus(ConnectionStatus.RUNNING);
            connectionStateHandler.waitUntilConnectionReleasedIfNecessary();
            if (ConnectionStatus.RUNNING != connectionStateHandler.getStatus()) {
                flag.getAndSet(false);
            }
        });
        Thread notifyThread = new Thread(() -> {
            try {
                Thread.sleep(2000);
            } catch (final InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
            connectionStateHandler.doNotifyIfNecessary();
        });
        waitThread.start();
        notifyThread.start();
        waitThread.join();
        notifyThread.join();
        assertTrue(flag.get());
    }
    
    @Test
    public void assertWaitUntilConnectionReleaseForTransaction() throws InterruptedException {
        AtomicBoolean flag = new AtomicBoolean(true);
        Thread waitThread = new Thread(() -> {
            connectionStateHandler.setStatus(ConnectionStatus.TERMINATED);
            connectionStateHandler.waitUntilConnectionReleasedIfNecessary();
            if (ConnectionStatus.RUNNING != connectionStateHandler.getStatus()) {
                flag.getAndSet(false);
            }
        });
        Thread notifyThread = new Thread(() -> {
            try {
                Thread.sleep(2000);
            } catch (final InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
            connectionStateHandler.doNotifyIfNecessary();
        });
        waitThread.start();
        notifyThread.start();
        waitThread.join();
        notifyThread.join();
        assertTrue(flag.get());
    }
}
