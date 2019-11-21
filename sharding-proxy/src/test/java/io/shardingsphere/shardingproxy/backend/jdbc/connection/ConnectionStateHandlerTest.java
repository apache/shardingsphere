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

import lombok.SneakyThrows;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertTrue;

public class ConnectionStateHandlerTest {
    
    private ResourceSynchronizer resourceSynchronizer = new ResourceSynchronizer();
    
    private ConnectionStateHandler connectionStateHandler = new ConnectionStateHandler(resourceSynchronizer);
    
    @Test
    public void assertWaitUntilConnectionReleaseForNoneTransaction() throws InterruptedException {
        final AtomicBoolean flag = new AtomicBoolean(true);
        Thread waitThread = new Thread(new Runnable() {
            @Override
            @SneakyThrows
            public void run() {
                connectionStateHandler.getAndSetStatus(ConnectionStatus.RUNNING);
                connectionStateHandler.waitUntilConnectionReleasedIfNecessary();
                if (ConnectionStatus.RUNNING != connectionStateHandler.getStatus()) {
                    flag.getAndSet(false);
                }
            }
        });
        Thread notifyThread = new Thread(new Runnable() {
            @Override
            @SneakyThrows
            public void run() {
                Thread.sleep(2000);
                connectionStateHandler.doNotifyIfNecessary();
            }
        });
        waitThread.start();
        notifyThread.start();
        waitThread.join();
        notifyThread.join();
        assertTrue(flag.get());
    }
    
    @Test
    public void assertWaitUntilConnectionReleaseForTransaction() throws InterruptedException {
        final AtomicBoolean flag = new AtomicBoolean(true);
        Thread waitThread = new Thread(new Runnable() {
            @Override
            @SneakyThrows
            public void run() {
                connectionStateHandler.getAndSetStatus(ConnectionStatus.TERMINATED);
                connectionStateHandler.waitUntilConnectionReleasedIfNecessary();
                if (ConnectionStatus.RUNNING != connectionStateHandler.getStatus()) {
                    flag.getAndSet(false);
                }
            }
        });
        Thread notifyThread = new Thread(new Runnable() {
            @Override
            @SneakyThrows
            public void run() {
                Thread.sleep(2000);
                connectionStateHandler.doNotifyIfNecessary();
            }
        });
        waitThread.start();
        notifyThread.start();
        waitThread.join();
        notifyThread.join();
        assertTrue(flag.get());
    }
}
