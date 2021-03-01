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

import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ConnectionStatusTest {
    
    private int numberOfThreads = 10;
    
    private ExecutorService service;
    
    private ConnectionStatus connectionStatus;
    
    private CountDownLatch latch;
    
    private Field usingField;
    
    @Before
    public void setup() throws NoSuchFieldException {
        connectionStatus = new ConnectionStatus();
        service = Executors.newFixedThreadPool(numberOfThreads);
        latch = new CountDownLatch(numberOfThreads);
        usingField = ConnectionStatus.class.getDeclaredField("isUsing");
        usingField.setAccessible(true);
    }
    
    @Test
    public void assertSwitchToUsing() throws InterruptedException, IllegalAccessException {
        AtomicInteger counter = new AtomicInteger();
        for (int i = 0; i < numberOfThreads; i++) {
            service.submit(() -> {
                connectionStatus.switchToUsing();
                counter.incrementAndGet();
                latch.countDown();
            });
        }
        latch.await();
        assertThat(numberOfThreads, is(counter.get()));
        assertThat(usingField.getBoolean(connectionStatus), is(true));
    }
    
    @Test
    public void assertSwitchToReleased() throws InterruptedException, IllegalAccessException {
        AtomicInteger counter = new AtomicInteger();
        for (int i = 0; i < numberOfThreads; i++) {
            service.submit(() -> {
                connectionStatus.switchToUsing();
                counter.incrementAndGet();
                connectionStatus.switchToReleased();
                counter.decrementAndGet();
                latch.countDown();
            });
        }
        latch.await();
        assertThat(counter.get(), is(0));
        assertThat(usingField.getBoolean(connectionStatus), is(false));
    }
    
    @Test
    public void assertWaitUntilConnectionRelease() throws InterruptedException, IllegalAccessException {
        AtomicInteger counter = new AtomicInteger();
        for (int i = 0; i < numberOfThreads; i++) {
            service.submit(() -> {
                connectionStatus.switchToUsing();
                counter.incrementAndGet();
                connectionStatus.waitUntilConnectionRelease();
                counter.decrementAndGet();
                latch.countDown();
            });
        }
        latch.await(200, TimeUnit.MILLISECONDS);
        assertThat(counter.get(), is(10));
        assertThat(usingField.getBoolean(connectionStatus), is(true));
        connectionStatus.switchToReleased();
        assertThat(usingField.getBoolean(connectionStatus), is(false));
        latch.await(300, TimeUnit.MILLISECONDS);
        assertThat(counter.get(), is(0));
    }
}
