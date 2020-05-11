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

package org.apache.shardingsphere.core.strategy.algorithm.keygen;

import lombok.SneakyThrows;
import org.apache.shardingsphere.core.strategy.algorithm.keygen.fixture.FixedTimeService;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThat;

public final class SnowflakeKeyGenerateAlgorithmTest {
    
    private static final long DEFAULT_SEQUENCE_BITS = 12L;
    
    private static final int DEFAULT_KEY_AMOUNT = 10;
    
    @Test
    public void assertGenerateKeyWithMultipleThreads() throws ExecutionException, InterruptedException {
        int threadNumber = Runtime.getRuntime().availableProcessors() << 1;
        ExecutorService executor = Executors.newFixedThreadPool(threadNumber);
        int taskNumber = threadNumber << 2;
        SnowflakeKeyGenerateAlgorithm keyGenerateAlgorithm = new SnowflakeKeyGenerateAlgorithm();
        keyGenerateAlgorithm.setProperties(new Properties());
        Set<Comparable<?>> actual = new HashSet<>();
        for (int i = 0; i < taskNumber; i++) {
            actual.add(executor.submit((Callable<Comparable<?>>) keyGenerateAlgorithm::generateKey).get());
        }
        assertThat(actual.size(), is(taskNumber));
    }
    
    @Test
    public void assertGenerateKeyWithSingleThread() {
        SnowflakeKeyGenerateAlgorithm keyGenerateAlgorithm = new SnowflakeKeyGenerateAlgorithm();
        keyGenerateAlgorithm.setProperties(new Properties());
        SnowflakeKeyGenerateAlgorithm.setTimeService(new FixedTimeService(1));
        List<Comparable<?>> expected = Arrays.asList(0L, 4194305L, 4194306L, 8388608L, 8388609L, 12582913L, 12582914L, 16777216L, 16777217L, 20971521L);
        List<Comparable<?>> actual = new ArrayList<>();
        for (int i = 0; i < DEFAULT_KEY_AMOUNT; i++) {
            actual.add(keyGenerateAlgorithm.generateKey());
        }
        assertThat(actual, is(expected));
    }
    
    @Test
    public void assertLastDigitalOfGenerateKeySameMillisecond() {
        SnowflakeKeyGenerateAlgorithm keyGenerateAlgorithm = new SnowflakeKeyGenerateAlgorithm();
        Properties properties = new Properties();
        SnowflakeKeyGenerateAlgorithm.setTimeService(new FixedTimeService(5));
        properties.setProperty("max.vibration.offset", String.valueOf(3));
        keyGenerateAlgorithm.setProperties(properties);
        assertThat(keyGenerateAlgorithm.generateKey(), is((Comparable) 0L));
        assertThat(keyGenerateAlgorithm.generateKey(), is((Comparable) 1L));
        assertThat(keyGenerateAlgorithm.generateKey(), is((Comparable) 2L));
        assertThat(keyGenerateAlgorithm.generateKey(), is((Comparable) 3L));
        assertThat(keyGenerateAlgorithm.generateKey(), is((Comparable) 4L));
    }
    
    @Test
    public void assertLastDigitalOfGenerateKeyDifferentMillisecond() throws InterruptedException {
        SnowflakeKeyGenerateAlgorithm keyGenerateAlgorithm = new SnowflakeKeyGenerateAlgorithm();
        Properties properties = new Properties();
        SnowflakeKeyGenerateAlgorithm.setTimeService(new TimeService());
        properties.setProperty("max.vibration.offset", String.valueOf(3));
        keyGenerateAlgorithm.setProperties(properties);
        String actualGenerateKeyBinaryString0 = Long.toBinaryString(Long.parseLong(keyGenerateAlgorithm.generateKey().toString()));
        assertThat(Integer.parseInt(actualGenerateKeyBinaryString0.substring(actualGenerateKeyBinaryString0.length() - 3), 2), is(0));
        Thread.sleep(2L);
        String actualGenerateKeyBinaryString1 = Long.toBinaryString(Long.parseLong(keyGenerateAlgorithm.generateKey().toString()));
        assertThat(Integer.parseInt(actualGenerateKeyBinaryString1.substring(actualGenerateKeyBinaryString1.length() - 3), 2), is(1));
        Thread.sleep(2L);
        String actualGenerateKeyBinaryString2 = Long.toBinaryString(Long.parseLong(keyGenerateAlgorithm.generateKey().toString()));
        assertThat(Integer.parseInt(actualGenerateKeyBinaryString2.substring(actualGenerateKeyBinaryString2.length() - 3), 2), is(2));
        Thread.sleep(2L);
        String actualGenerateKeyBinaryString3 = Long.toBinaryString(Long.parseLong(keyGenerateAlgorithm.generateKey().toString()));
        assertThat(Integer.parseInt(actualGenerateKeyBinaryString3.substring(actualGenerateKeyBinaryString3.length() - 3), 2), is(3));
        Thread.sleep(2L);
        String actualGenerateKeyBinaryString4 = Long.toBinaryString(Long.parseLong(keyGenerateAlgorithm.generateKey().toString()));
        assertThat(Integer.parseInt(actualGenerateKeyBinaryString4.substring(actualGenerateKeyBinaryString4.length() - 3), 2), is(0));
    }
    
    @Test
    public void assertGenerateKeyWithClockCallBack() {
        SnowflakeKeyGenerateAlgorithm keyGenerateAlgorithm = new SnowflakeKeyGenerateAlgorithm();
        TimeService timeService = new FixedTimeService(1);
        SnowflakeKeyGenerateAlgorithm.setTimeService(timeService);
        keyGenerateAlgorithm.setProperties(new Properties());
        setLastMilliseconds(keyGenerateAlgorithm, timeService.getCurrentMillis() + 2);
        List<Comparable<?>> expected = Arrays.asList(4194304L, 8388609L, 8388610L, 12582912L, 12582913L, 16777217L, 16777218L, 20971520L, 20971521L, 25165825L);
        List<Comparable<?>> actual = new ArrayList<>();
        for (int i = 0; i < DEFAULT_KEY_AMOUNT; i++) {
            actual.add(keyGenerateAlgorithm.generateKey());
        }
        assertThat(actual, is(expected));
    }
    
    @Test(expected = IllegalStateException.class)
    public void assertGenerateKeyWithClockCallBackBeyondTolerateTime() {
        SnowflakeKeyGenerateAlgorithm keyGenerateAlgorithm = new SnowflakeKeyGenerateAlgorithm();
        TimeService timeService = new FixedTimeService(1);
        SnowflakeKeyGenerateAlgorithm.setTimeService(timeService);
        keyGenerateAlgorithm.setProperties(new Properties());
        Properties properties = new Properties();
        properties.setProperty("max.tolerate.time.difference.milliseconds", String.valueOf(0));
        keyGenerateAlgorithm.setProperties(properties);
        setLastMilliseconds(keyGenerateAlgorithm, timeService.getCurrentMillis() + 2);
        List<Comparable<?>> actual = new ArrayList<>();
        for (int i = 0; i < DEFAULT_KEY_AMOUNT; i++) {
            actual.add(keyGenerateAlgorithm.generateKey());
        }
        assertNotEquals(actual.size(), 10);
    }
    
    @Test
    public void assertGenerateKeyBeyondMaxSequencePerMilliSecond() {
        final SnowflakeKeyGenerateAlgorithm keyGenerateAlgorithm = new SnowflakeKeyGenerateAlgorithm();
        TimeService timeService = new FixedTimeService(2);
        SnowflakeKeyGenerateAlgorithm.setTimeService(timeService);
        keyGenerateAlgorithm.setProperties(new Properties());
        setLastMilliseconds(keyGenerateAlgorithm, timeService.getCurrentMillis());
        setSequence(keyGenerateAlgorithm, (1 << DEFAULT_SEQUENCE_BITS) - 1);
        List<Comparable<?>> expected = Arrays.asList(4194304L, 4194305L, 4194306L, 8388608L, 8388609L, 8388610L, 12582913L, 12582914L, 12582915L, 16777216L);
        List<Comparable<?>> actual = new ArrayList<>();
        for (int i = 0; i < DEFAULT_KEY_AMOUNT; i++) {
            actual.add(keyGenerateAlgorithm.generateKey());
        }
        assertThat(actual, is(expected));
    }
    
    @SneakyThrows
    private void setSequence(final SnowflakeKeyGenerateAlgorithm keyGenerateAlgorithm, final Number value) {
        Field sequence = SnowflakeKeyGenerateAlgorithm.class.getDeclaredField("sequence");
        sequence.setAccessible(true);
        sequence.set(keyGenerateAlgorithm, value);
    }
    
    @SneakyThrows
    private void setLastMilliseconds(final SnowflakeKeyGenerateAlgorithm keyGenerateAlgorithm, final Number value) {
        Field lastMilliseconds = SnowflakeKeyGenerateAlgorithm.class.getDeclaredField("lastMilliseconds");
        lastMilliseconds.setAccessible(true);
        lastMilliseconds.set(keyGenerateAlgorithm, value);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertSetWorkerIdFailureWhenNegative() {
        SnowflakeKeyGenerateAlgorithm keyGenerateAlgorithm = new SnowflakeKeyGenerateAlgorithm();
        Properties properties = new Properties();
        properties.setProperty("worker.id", String.valueOf(-1L));
        keyGenerateAlgorithm.setProperties(properties);
        keyGenerateAlgorithm.generateKey();
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertSetMaxVibrationOffsetFailureWhenNegative() {
        SnowflakeKeyGenerateAlgorithm keyGenerateAlgorithm = new SnowflakeKeyGenerateAlgorithm();
        Properties properties = new Properties();
        properties.setProperty("max.vibration.offset", String.valueOf(-1));
        keyGenerateAlgorithm.setProperties(properties);
        keyGenerateAlgorithm.generateKey();
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertSetWorkerIdFailureWhenOutOfRange() {
        SnowflakeKeyGenerateAlgorithm keyGenerateAlgorithm = new SnowflakeKeyGenerateAlgorithm();
        Properties properties = new Properties();
        properties.setProperty("worker.id", String.valueOf(Long.MIN_VALUE));
        keyGenerateAlgorithm.setProperties(properties);
        keyGenerateAlgorithm.generateKey();
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertSetMaxVibrationOffsetFailureWhenOutOfRange() {
        SnowflakeKeyGenerateAlgorithm keyGenerateAlgorithm = new SnowflakeKeyGenerateAlgorithm();
        Properties properties = new Properties();
        properties.setProperty("max.vibration.offset", String.valueOf(4096));
        keyGenerateAlgorithm.setProperties(properties);
        keyGenerateAlgorithm.generateKey();
    }
    
    @Test
    @SneakyThrows
    public void assertSetWorkerIdSuccess() {
        SnowflakeKeyGenerateAlgorithm keyGenerateAlgorithm = new SnowflakeKeyGenerateAlgorithm();
        Properties properties = new Properties();
        properties.setProperty("worker.id", String.valueOf(1L));
        keyGenerateAlgorithm.setProperties(properties);
        Field props = keyGenerateAlgorithm.getClass().getDeclaredField("properties");
        props.setAccessible(true);
        assertThat(((Properties) props.get(keyGenerateAlgorithm)).get("worker.id"), is("1"));
    }
    
    @Test
    @SneakyThrows
    public void assertSetMaxTolerateTimeDifferenceMilliseconds() {
        SnowflakeKeyGenerateAlgorithm keyGenerateAlgorithm = new SnowflakeKeyGenerateAlgorithm();
        Properties properties = new Properties();
        properties.setProperty("max.tolerate.time.difference.milliseconds", String.valueOf(1));
        keyGenerateAlgorithm.setProperties(properties);
        Field props = keyGenerateAlgorithm.getClass().getDeclaredField("properties");
        props.setAccessible(true);
        assertThat(((Properties) props.get(keyGenerateAlgorithm)).get("max.tolerate.time.difference.milliseconds"), is("1"));
    }
}
