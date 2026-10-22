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

package org.apache.shardingsphere.mode.manager.standalone.exclusive;

import org.apache.shardingsphere.mode.exclusive.ExclusiveLockHandle;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StandaloneExclusiveOperatorContextTest {
    
    @Test
    void assertStartReturnsHandleWhenKeyAbsent() {
        Optional<ExclusiveLockHandle> actual = new StandaloneExclusiveOperatorContext().start("operation-key", 50L);
        assertTrue(actual.isPresent());
    }
    
    @Test
    void assertStartReturnsEmptyWhenKeyExists() {
        StandaloneExclusiveOperatorContext context = new StandaloneExclusiveOperatorContext();
        ExclusiveLockHandle firstHandle = context.start("duplicated-key", 0L).orElseThrow(AssertionError::new);
        assertFalse(context.start("duplicated-key", 0L).isPresent());
        firstHandle.close();
    }
    
    @Test
    void assertStartWithConcurrentSameKey() throws InterruptedException, ExecutionException, TimeoutException {
        StandaloneExclusiveOperatorContext context = new StandaloneExclusiveOperatorContext();
        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch attemptsFinished = new CountDownLatch(2);
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        Collection<Future<Boolean>> actualResults = new ArrayList<>(2);
        try {
            for (int i = 0; i < 2; i++) {
                actualResults.add(executorService.submit(() -> {
                    assertTrue(startGate.await(5L, TimeUnit.SECONDS));
                    Optional<ExclusiveLockHandle> actualLockHandle = context.start("foo_operation", 0L);
                    try {
                        attemptsFinished.countDown();
                        assertTrue(attemptsFinished.await(5L, TimeUnit.SECONDS));
                        return actualLockHandle.isPresent();
                    } finally {
                        actualLockHandle.ifPresent(ExclusiveLockHandle::close);
                    }
                }));
            }
            startGate.countDown();
            int actualAcquiredCount = 0;
            for (Future<Boolean> each : actualResults) {
                if (each.get(5L, TimeUnit.SECONDS)) {
                    actualAcquiredCount++;
                }
            }
            assertThat(actualAcquiredCount, is(1));
        } finally {
            startGate.countDown();
            executorService.shutdownNow();
        }
    }
    
    @Test
    void assertCloseReleasesOperationKey() {
        StandaloneExclusiveOperatorContext context = new StandaloneExclusiveOperatorContext();
        ExclusiveLockHandle firstHandle = context.start("stopped-key", 0L).orElseThrow(AssertionError::new);
        firstHandle.close();
        Optional<ExclusiveLockHandle> actual = context.start("stopped-key", 0L);
        assertTrue(actual.isPresent());
        assertThat(actual.orElseThrow(AssertionError::new), not(firstHandle));
    }
}
