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

package io.shardingsphere.core.keygen;

import io.shardingsphere.core.keygen.fixture.FixedTimeService;
import lombok.SneakyThrows;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThat;

public final class DefaultKeyGeneratorTest {
    
    static final long DEFAULT_SEQUENCE_BITS = 12L;
    
    static final int DEFAULT_KEY_AMOUNT = 10;
    
    @Test
    @SneakyThrows
    public void assertGenerateKeyWithMultipleThreads() {
        int threadNumber = Runtime.getRuntime().availableProcessors() << 1;
        ExecutorService executor = Executors.newFixedThreadPool(threadNumber);
        int taskNumber = threadNumber << 2;
        final DefaultKeyGenerator keyGenerator = new DefaultKeyGenerator();
        Set<Number> actual = new HashSet<>();
        for (int i = 0; i < taskNumber; i++) {
            actual.add(executor.submit(new Callable<Number>() {
                
                @Override
                public Number call() {
                    return keyGenerator.generateKey();
                }
            }).get());
        }
        assertThat(actual.size(), is(taskNumber));
    }
    
    @Test
    public void assertGenerateKeyWithSingleThread() {
        List<Number> expected = Arrays.<Number>asList(1L, 4194304L, 4194305L, 8388609L, 8388610L, 12582912L, 12582913L, 16777217L, 16777218L, 20971520L);
        DefaultKeyGenerator keyGenerator = new DefaultKeyGenerator();
        DefaultKeyGenerator.setTimeService(new FixedTimeService(1));
        List<Number> actual = new ArrayList<>();
        for (int i = 0; i < DEFAULT_KEY_AMOUNT; i++) {
            actual.add(keyGenerator.generateKey());
        }
        assertThat(actual, is(expected));
    }
    
    @Test
    @SneakyThrows
    public void assertGenerateKeyWithClockCallBack() {
        List<Number> expected = Arrays.<Number>asList(4194305L, 8388608L, 8388609L, 12582913L, 12582914L, 16777216L, 16777217L, 20971521L, 20971522L, 25165824L);
        DefaultKeyGenerator keyGenerator = new DefaultKeyGenerator();
        TimeService timeService = new FixedTimeService(1);
        DefaultKeyGenerator.setTimeService(timeService);
        setLastMilliseconds(keyGenerator, timeService.getCurrentMillis() + 2);
        List<Number> actual = new ArrayList<>();
        for (int i = 0; i < DEFAULT_KEY_AMOUNT; i++) {
            actual.add(keyGenerator.generateKey());
        }
        assertThat(actual, is(expected));
    }
    
    @Test(expected = IllegalStateException.class)
    @SneakyThrows
    public void assertGenerateKeyWithClockCallBackBeyondTolerateTime() {
        DefaultKeyGenerator keyGenerator = new DefaultKeyGenerator();
        TimeService timeService = new FixedTimeService(1);
        DefaultKeyGenerator.setTimeService(timeService);
        DefaultKeyGenerator.setMaxTolerateTimeDifferenceMilliseconds(0);
        setLastMilliseconds(keyGenerator, timeService.getCurrentMillis() + 2);
        List<Number> actual = new ArrayList<>();
        for (int i = 0; i < DEFAULT_KEY_AMOUNT; i++) {
            actual.add(keyGenerator.generateKey());
        }
        assertNotEquals(actual.size(), 10);
    }
    
    @Test
    public void assertGenerateKeyBeyondMaxSequencePerMilliSecond() {
        List<Number> expected = Arrays.<Number>asList(4194304L, 4194305L, 4194306L, 8388609L, 8388610L, 8388611L, 12582912L, 12582913L, 12582914L, 16777217L);
        final DefaultKeyGenerator keyGenerator = new DefaultKeyGenerator();
        TimeService timeService = new FixedTimeService(2);
        DefaultKeyGenerator.setTimeService(timeService);
        setLastMilliseconds(keyGenerator, timeService.getCurrentMillis());
        setSequence(keyGenerator, (1 << DEFAULT_SEQUENCE_BITS) - 1);
        List<Number> actual = new ArrayList<>();
        for (int i = 0; i < DEFAULT_KEY_AMOUNT; i++) {
            actual.add(keyGenerator.generateKey());
        }
        assertThat(actual, is(expected));
    }
    
    @SneakyThrows
    private void setSequence(final DefaultKeyGenerator keyGenerator, final Number value) {
        Field sequence = DefaultKeyGenerator.class.getDeclaredField("sequence");
        sequence.setAccessible(true);
        sequence.set(keyGenerator, value);
    }
    
    @SneakyThrows
    private void setLastMilliseconds(final DefaultKeyGenerator keyGenerator, final Number value) {
        Field lastMilliseconds = DefaultKeyGenerator.class.getDeclaredField("lastMilliseconds");
        lastMilliseconds.setAccessible(true);
        lastMilliseconds.set(keyGenerator, value);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertSetWorkerIdFailureWhenNegative() {
        DefaultKeyGenerator.setWorkerId(-1L);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertSetWorkerIdFailureWhenTooMuch() {
        DefaultKeyGenerator.setWorkerId(-Long.MAX_VALUE);
    }
    
    @Test
    @SneakyThrows
    public void assertSetWorkerIdSuccess() {
        DefaultKeyGenerator.setWorkerId(1L);
        Field workerIdField = DefaultKeyGenerator.class.getDeclaredField("workerId");
        workerIdField.setAccessible(true);
        assertThat(workerIdField.getLong(DefaultKeyGenerator.class), is(1L));
        DefaultKeyGenerator.setWorkerId(0L);
    }
    
    @Test
    @SneakyThrows
    public void assertSetMaxTolerateTimeDifferenceMilliseconds() {
        DefaultKeyGenerator.setMaxTolerateTimeDifferenceMilliseconds(1);
        Field maxTolerateTimeDifferenceMillisecondsField = DefaultKeyGenerator.class.getDeclaredField("maxTolerateTimeDifferenceMilliseconds");
        maxTolerateTimeDifferenceMillisecondsField.setAccessible(true);
        assertThat(maxTolerateTimeDifferenceMillisecondsField.getInt(DefaultKeyGenerator.class), is(1));
        DefaultKeyGenerator.setMaxTolerateTimeDifferenceMilliseconds(10);
    }
}
