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
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThat;

public final class DefaultKeyGeneratorTest {
    
    private static final long DEFAULT_SEQUENCE_BITS = 12L;
    
    private static final int DEFAULT_KEY_AMOUNT = 10;
    
    @Test
    @SneakyThrows
    public void assertGenerateKeyWithMultipleThreads() {
        int threadNumber = Runtime.getRuntime().availableProcessors() << 1;
        ExecutorService executor = Executors.newFixedThreadPool(threadNumber);
        int taskNumber = threadNumber << 2;
        final DefaultKeyGenerator keyGenerator = new DefaultKeyGenerator();
        keyGenerator.setProperties(new Properties());
        Set<Comparable<?>> actual = new HashSet<>();
        for (int i = 0; i < taskNumber; i++) {
            actual.add(executor.submit(new Callable<Comparable<?>>() {
                
                @Override
                public Comparable<?> call() {
                    return keyGenerator.generateKey();
                }
            }).get());
        }
        assertThat(actual.size(), is(taskNumber));
    }
    
    @Test
    public void assertGenerateKeyWithSingleThread() {
        DefaultKeyGenerator keyGenerator = new DefaultKeyGenerator();
        keyGenerator.setProperties(new Properties());
        DefaultKeyGenerator.setTimeService(new FixedTimeService(1));
        List<Comparable<?>> expected = Arrays.<Comparable<?>>asList(1L, 4194304L, 4194305L, 8388609L, 8388610L, 12582912L, 12582913L, 16777217L, 16777218L, 20971520L);
        List<Comparable<?>> actual = new ArrayList<>();
        for (int i = 0; i < DEFAULT_KEY_AMOUNT; i++) {
            actual.add(keyGenerator.generateKey());
        }
        assertThat(actual, is(expected));
    }
    
    @Test
    @SneakyThrows
    public void assertGenerateKeyWithClockCallBack() {
        DefaultKeyGenerator keyGenerator = new DefaultKeyGenerator();
        TimeService timeService = new FixedTimeService(1);
        DefaultKeyGenerator.setTimeService(timeService);
        keyGenerator.setProperties(new Properties());
        setLastMilliseconds(keyGenerator, timeService.getCurrentMillis() + 2);
        List<Comparable<?>> expected = Arrays.<Comparable<?>>asList(4194305L, 8388608L, 8388609L, 12582913L, 12582914L, 16777216L, 16777217L, 20971521L, 20971522L, 25165824L);
        List<Comparable<?>> actual = new ArrayList<>();
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
        keyGenerator.setProperties(new Properties());
        Properties properties = new Properties();
        properties.setProperty("max.tolerate.time.difference.milliseconds", String.valueOf(0));
        keyGenerator.setProperties(properties);
        setLastMilliseconds(keyGenerator, timeService.getCurrentMillis() + 2);
        List<Comparable<?>> actual = new ArrayList<>();
        for (int i = 0; i < DEFAULT_KEY_AMOUNT; i++) {
            actual.add(keyGenerator.generateKey());
        }
        assertNotEquals(actual.size(), 10);
    }
    
    @Test
    public void assertGenerateKeyBeyondMaxSequencePerMilliSecond() {
        final DefaultKeyGenerator keyGenerator = new DefaultKeyGenerator();
        TimeService timeService = new FixedTimeService(2);
        DefaultKeyGenerator.setTimeService(timeService);
        keyGenerator.setProperties(new Properties());
        setLastMilliseconds(keyGenerator, timeService.getCurrentMillis());
        setSequence(keyGenerator, (1 << DEFAULT_SEQUENCE_BITS) - 1);
        List<Comparable<?>> expected = Arrays.<Comparable<?>>asList(4194304L, 4194305L, 4194306L, 8388609L, 8388610L, 8388611L, 12582912L, 12582913L, 12582914L, 16777217L);
        List<Comparable<?>> actual = new ArrayList<>();
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
        DefaultKeyGenerator keyGenerator = new DefaultKeyGenerator();
        Properties properties = new Properties();
        properties.setProperty("worker.id", String.valueOf(-1L));
        keyGenerator.setProperties(properties);
        keyGenerator.generateKey();
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertSetWorkerIdFailureWhenTooMuch() {
        DefaultKeyGenerator keyGenerator = new DefaultKeyGenerator();
        Properties properties = new Properties();
        properties.setProperty("worker.id", String.valueOf(-Long.MAX_VALUE));
        keyGenerator.setProperties(properties);
        keyGenerator.generateKey();
    }
    
    @Test
    @SneakyThrows
    public void assertSetWorkerIdSuccess() {
        DefaultKeyGenerator keyGenerator = new DefaultKeyGenerator();
        Properties properties = new Properties();
        properties.setProperty("worker.id", String.valueOf(1L));
        keyGenerator.setProperties(properties);
        Field props = keyGenerator.getClass().getDeclaredField("properties");
        props.setAccessible(true);
        assertThat(((Properties) props.get(keyGenerator)).get("worker.id"), is((Object) "1"));
    }
    
    @Test
    @SneakyThrows
    public void assertSetMaxTolerateTimeDifferenceMilliseconds() {
        DefaultKeyGenerator keyGenerator = new DefaultKeyGenerator();
        Properties properties = new Properties();
        properties.setProperty("max.tolerate.time.difference.milliseconds", String.valueOf(1));
        keyGenerator.setProperties(properties);
        Field props = keyGenerator.getClass().getDeclaredField("properties");
        props.setAccessible(true);
        assertThat(((Properties) props.get(keyGenerator)).get("max.tolerate.time.difference.milliseconds"), is((Object) "1"));
    }
}
