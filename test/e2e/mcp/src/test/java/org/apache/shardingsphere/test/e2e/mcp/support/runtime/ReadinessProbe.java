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

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.function.LongSupplier;

/**
 * Readiness retry probe.
 */
public final class ReadinessProbe {
    
    private final long timeoutMillis;
    
    private final long initialIntervalMillis;
    
    private final long maxIntervalMillis;
    
    private final LongSupplier currentTimeMillis;
    
    private final Sleeper sleeper;
    
    public ReadinessProbe(final long timeoutMillis, final long initialIntervalMillis, final long maxIntervalMillis) {
        this(timeoutMillis, initialIntervalMillis, maxIntervalMillis, System::currentTimeMillis, Thread::sleep);
    }
    
    ReadinessProbe(final long timeoutMillis, final long initialIntervalMillis, final long maxIntervalMillis, final LongSupplier currentTimeMillis, final Sleeper sleeper) {
        checkArguments(timeoutMillis, initialIntervalMillis, maxIntervalMillis);
        this.timeoutMillis = timeoutMillis;
        this.initialIntervalMillis = initialIntervalMillis;
        this.maxIntervalMillis = maxIntervalMillis;
        this.currentTimeMillis = currentTimeMillis;
        this.sleeper = sleeper;
    }
    
    private void checkArguments(final long timeoutMillis, final long initialIntervalMillis, final long maxIntervalMillis) {
        if (0L >= timeoutMillis) {
            throw new IllegalArgumentException("Readiness timeout must be positive.");
        }
        if (0L >= initialIntervalMillis) {
            throw new IllegalArgumentException("Readiness initial interval must be positive.");
        }
        if (initialIntervalMillis > maxIntervalMillis) {
            throw new IllegalArgumentException("Readiness maximum interval must not be less than the initial interval.");
        }
    }
    
    /**
     * Wait until a readiness check succeeds.
     *
     * @param readinessCheck readiness check
     * @param failureFactory failure factory
     * @param <T> ready value type
     * @param <E> failure exception type
     * @return ready value
     * @throws E failure exception
     * @throws InterruptedException interrupted exception
     */
    public <T, E extends Exception> T waitUntilReady(final ReadinessCheck<T> readinessCheck, final FailureFactory<E> failureFactory) throws E, InterruptedException {
        long startTimeMillis = currentTimeMillis.getAsLong();
        long deadlineMillis = startTimeMillis + timeoutMillis;
        long intervalMillis = initialIntervalMillis;
        int attemptCount = 0;
        Exception lastFailure = null;
        while (currentTimeMillis.getAsLong() < deadlineMillis) {
            attemptCount++;
            ReadinessResult<T> readinessResult = readinessCheck.check();
            if (readinessResult.isReady()) {
                return readinessResult.getValue();
            }
            if (null != readinessResult.getFailure()) {
                lastFailure = readinessResult.getFailure();
            }
            if (readinessResult.isFailed()) {
                throw failureFactory.create(lastFailure, attemptCount, getElapsedMillis(startTimeMillis));
            }
            intervalMillis = sleepBeforeRetry(deadlineMillis, intervalMillis);
        }
        throw failureFactory.create(lastFailure, attemptCount, getElapsedMillis(startTimeMillis));
    }
    
    private long sleepBeforeRetry(final long deadlineMillis, final long intervalMillis) throws InterruptedException {
        long remainingMillis = deadlineMillis - currentTimeMillis.getAsLong();
        if (0L >= remainingMillis) {
            return intervalMillis;
        }
        try {
            sleeper.sleep(Math.min(intervalMillis, remainingMillis));
            return Math.min(maxIntervalMillis, intervalMillis * 2L);
        } catch (final InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw ex;
        }
    }
    
    private long getElapsedMillis(final long startTimeMillis) {
        return currentTimeMillis.getAsLong() - startTimeMillis;
    }
    
    /**
     * Readiness check.
     *
     * @param <T> ready value type
     */
    @FunctionalInterface
    public interface ReadinessCheck<T> {
        
        /**
         * Check readiness.
         *
         * @return readiness result
         * @throws InterruptedException interrupted exception
         */
        ReadinessResult<T> check() throws InterruptedException;
    }
    
    /**
     * Failure factory.
     *
     * @param <E> failure exception type
     */
    @FunctionalInterface
    public interface FailureFactory<E extends Exception> {
        
        /**
         * Create failure.
         *
         * @param cause last readiness failure
         * @param attemptCount attempt count
         * @param elapsedMillis elapsed milliseconds
         * @return failure exception
         */
        E create(Exception cause, int attemptCount, long elapsedMillis);
    }
    
    @FunctionalInterface
    interface Sleeper {
        
        void sleep(long millis) throws InterruptedException;
    }
    
    /**
     * Readiness result.
     *
     * @param <T> ready value type
     */
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    @Getter(AccessLevel.PRIVATE)
    public static final class ReadinessResult<T> {
        
        private final T value;
        
        private final Exception failure;
        
        private final boolean ready;
        
        private final boolean failed;
        
        /**
         * Create ready result.
         *
         * @param value ready value
         * @param <T> ready value type
         * @return ready result
         */
        public static <T> ReadinessResult<T> ready(final T value) {
            return new ReadinessResult<>(value, null, true, false);
        }
        
        /**
         * Create retry result.
         *
         * @param failure retryable failure
         * @param <T> ready value type
         * @return retry result
         */
        public static <T> ReadinessResult<T> retry(final Exception failure) {
            return new ReadinessResult<>(null, failure, false, false);
        }
        
        /**
         * Create failed result.
         *
         * @param failure non-retryable failure
         * @param <T> ready value type
         * @return failed result
         */
        public static <T> ReadinessResult<T> failed(final Exception failure) {
            return new ReadinessResult<>(null, failure, false, true);
        }
    }
}
