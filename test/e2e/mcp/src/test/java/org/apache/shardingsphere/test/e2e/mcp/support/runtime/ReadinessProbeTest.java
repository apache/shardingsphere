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

package org.apache.shardingsphere.test.e2e.mcp.support.runtime;

import org.apache.shardingsphere.test.e2e.mcp.support.runtime.ReadinessProbe.ReadinessResult;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReadinessProbeTest {
    
    @Test
    void assertWaitUntilReadyAfterRetry() throws InterruptedException {
        AtomicLong currentTimeMillis = new AtomicLong();
        AtomicLong actualSleepMillis = new AtomicLong();
        AtomicInteger actualAttemptCount = new AtomicInteger();
        ReadinessProbe probe = new ReadinessProbe(1000L, 250L, 500L, currentTimeMillis::get, millis -> {
            actualSleepMillis.addAndGet(millis);
            currentTimeMillis.addAndGet(millis);
        });
        String actual = probe.waitUntilReady(() -> 1 == actualAttemptCount.incrementAndGet()
                ? ReadinessResult.retry(new IllegalStateException("not ready"))
                : ReadinessResult.ready("ready"), this::createFailure);
        assertThat(actual, is("ready"));
        assertThat(actualAttemptCount.get(), is(2));
        assertThat(actualSleepMillis.get(), is(250L));
    }
    
    @Test
    void assertWaitUntilReadyTimeout() {
        AtomicLong currentTimeMillis = new AtomicLong();
        AtomicInteger actualAttemptCount = new AtomicInteger();
        ReadinessProbe probe = new ReadinessProbe(500L, 250L, 500L, currentTimeMillis::get, currentTimeMillis::addAndGet);
        IllegalStateException actual = assertThrows(IllegalStateException.class,
                () -> probe.waitUntilReady(() -> ReadinessResult.retry(new IllegalStateException("not ready " + actualAttemptCount.incrementAndGet())), this::createFailure));
        assertThat(actual.getMessage(), is("attempts=2, elapsedMillis=500, cause=not ready 2"));
    }
    
    @Test
    void assertWaitUntilReadyInterrupted() {
        ReadinessProbe probe = new ReadinessProbe(1000L, 250L, 500L, System::currentTimeMillis, ignored -> {
            throw new InterruptedException("interrupted");
        });
        InterruptedException actual = assertThrows(InterruptedException.class,
                () -> probe.waitUntilReady(() -> ReadinessResult.retry(new IllegalStateException("not ready")), this::createFailure));
        assertThat(actual.getMessage(), is("interrupted"));
        assertTrue(Thread.currentThread().isInterrupted());
        Thread.interrupted();
    }
    
    @Test
    void assertWaitUntilReadyStopsForFailure() {
        AtomicInteger actualSleepCount = new AtomicInteger();
        ReadinessProbe probe = new ReadinessProbe(1000L, 250L, 500L, System::currentTimeMillis, ignored -> actualSleepCount.incrementAndGet());
        IllegalStateException actual = assertThrows(IllegalStateException.class,
                () -> probe.waitUntilReady(() -> ReadinessResult.failed(new IllegalStateException("bad request")), this::createFailure));
        assertTrue(actual.getMessage().startsWith("attempts=1, elapsedMillis="));
        assertTrue(actual.getMessage().endsWith(", cause=bad request"));
        assertThat(actualSleepCount.get(), is(0));
    }
    
    private IllegalStateException createFailure(final Exception cause, final int attemptCount, final long elapsedMillis) {
        return new IllegalStateException(String.format("attempts=%d, elapsedMillis=%d, cause=%s", attemptCount, elapsedMillis, cause.getMessage()), cause);
    }
}
