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

package org.apache.shardingsphere.sharding.algorithm.keygen;

import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.instance.InstanceContextAware;
import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.infra.schedule.ScheduleContext;
import org.apache.shardingsphere.infra.util.eventbus.EventBusContext;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstance;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.instance.metadata.InstanceMetaData;
import org.apache.shardingsphere.infra.lock.LockContext;
import org.apache.shardingsphere.sharding.algorithm.keygen.fixture.FixedTimeService;
import org.apache.shardingsphere.sharding.algorithm.keygen.fixture.WorkerIdGeneratorFixture;
import org.apache.shardingsphere.sharding.factory.KeyGenerateAlgorithmFactory;
import org.apache.shardingsphere.sharding.spi.KeyGenerateAlgorithm;
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
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class SnowflakeKeyGenerateAlgorithmTest {
    
    private static final long DEFAULT_SEQUENCE_BITS = 12L;
    
    private static final int DEFAULT_KEY_AMOUNT = 10;
    
    private static final InstanceContext INSTANCE;
    
    static {
        InstanceContext instanceContext = mock(InstanceContext.class);
        when(instanceContext.getWorkerId()).thenReturn(0L);
        INSTANCE = instanceContext;
    }
    
    @Test
    public void assertGenerateKeyWithMultipleThreads() throws ExecutionException, InterruptedException {
        int threadNumber = Runtime.getRuntime().availableProcessors() * 2;
        ExecutorService executor = Executors.newFixedThreadPool(threadNumber);
        int taskNumber = threadNumber * 4;
        KeyGenerateAlgorithm algorithm = KeyGenerateAlgorithmFactory.newInstance(new AlgorithmConfiguration("SNOWFLAKE", new Properties()));
        if (algorithm instanceof InstanceContextAware) {
            ((InstanceContextAware) algorithm).setInstanceContext(INSTANCE);
        }
        Set<Comparable<?>> actual = new HashSet<>(taskNumber, 1);
        for (int i = 0; i < taskNumber; i++) {
            actual.add(executor.submit((Callable<Comparable<?>>) algorithm::generateKey).get());
        }
        assertThat(actual.size(), is(taskNumber));
    }
    
    @Test
    public void assertGenerateKeyWithSingleThread() {
        SnowflakeKeyGenerateAlgorithm.setTimeService(new FixedTimeService(1));
        KeyGenerateAlgorithm algorithm = KeyGenerateAlgorithmFactory.newInstance(new AlgorithmConfiguration("SNOWFLAKE", new Properties()));
        if (algorithm instanceof InstanceContextAware) {
            ((InstanceContextAware) algorithm).setInstanceContext(INSTANCE);
        }
        List<Comparable<?>> expected = Arrays.asList(0L, 4194305L, 4194306L, 8388608L, 8388609L, 12582913L, 12582914L, 16777216L, 16777217L, 20971521L);
        List<Comparable<?>> actual = new ArrayList<>(DEFAULT_KEY_AMOUNT);
        for (int i = 0; i < DEFAULT_KEY_AMOUNT; i++) {
            actual.add(algorithm.generateKey());
        }
        assertThat(actual, is(expected));
    }
    
    @Test
    public void assertLastDigitalOfGenerateKeySameMillisecond() {
        SnowflakeKeyGenerateAlgorithm.setTimeService(new FixedTimeService(5));
        Properties props = new Properties();
        props.setProperty("max-vibration-offset", "3");
        KeyGenerateAlgorithm algorithm = KeyGenerateAlgorithmFactory.newInstance(new AlgorithmConfiguration("SNOWFLAKE", props));
        if (algorithm instanceof InstanceContextAware) {
            ((InstanceContextAware) algorithm).setInstanceContext(INSTANCE);
        }
        assertThat(algorithm.generateKey(), is(0L));
        assertThat(algorithm.generateKey(), is(1L));
        assertThat(algorithm.generateKey(), is(2L));
        assertThat(algorithm.generateKey(), is(3L));
        assertThat(algorithm.generateKey(), is(4L));
    }
    
    @Test
    public void assertLastDigitalOfGenerateKeyDifferentMillisecond() throws InterruptedException {
        Properties props = new Properties();
        SnowflakeKeyGenerateAlgorithm.setTimeService(new TimeService());
        props.setProperty("max-vibration-offset", String.valueOf(3));
        KeyGenerateAlgorithm algorithm = KeyGenerateAlgorithmFactory.newInstance(new AlgorithmConfiguration("SNOWFLAKE", props));
        if (algorithm instanceof InstanceContextAware) {
            ((InstanceContextAware) algorithm).setInstanceContext(INSTANCE);
        }
        String actualGenerateKey0 = Long.toBinaryString(Long.parseLong(algorithm.generateKey().toString()));
        assertThat(Integer.parseInt(actualGenerateKey0.substring(actualGenerateKey0.length() - 3), 2), is(0));
        Thread.sleep(2L);
        String actualGenerateKey1 = Long.toBinaryString(Long.parseLong(algorithm.generateKey().toString()));
        assertThat(Integer.parseInt(actualGenerateKey1.substring(actualGenerateKey1.length() - 3), 2), is(1));
        Thread.sleep(2L);
        String actualGenerateKey2 = Long.toBinaryString(Long.parseLong(algorithm.generateKey().toString()));
        assertThat(Integer.parseInt(actualGenerateKey2.substring(actualGenerateKey2.length() - 3), 2), is(2));
        Thread.sleep(2L);
        String actualGenerateKey3 = Long.toBinaryString(Long.parseLong(algorithm.generateKey().toString()));
        assertThat(Integer.parseInt(actualGenerateKey3.substring(actualGenerateKey3.length() - 3), 2), is(3));
        Thread.sleep(2L);
        String actualGenerateKey4 = Long.toBinaryString(Long.parseLong(algorithm.generateKey().toString()));
        assertThat(Integer.parseInt(actualGenerateKey4.substring(actualGenerateKey4.length() - 3), 2), is(0));
    }
    
    @Test
    public void assertGenerateKeyWithClockCallBack() {
        TimeService timeService = new FixedTimeService(1);
        SnowflakeKeyGenerateAlgorithm.setTimeService(timeService);
        KeyGenerateAlgorithm algorithm = KeyGenerateAlgorithmFactory.newInstance(new AlgorithmConfiguration("SNOWFLAKE", new Properties()));
        if (algorithm instanceof InstanceContextAware) {
            ((InstanceContextAware) algorithm).setInstanceContext(INSTANCE);
        }
        setLastMilliseconds(algorithm, timeService.getCurrentMillis() + 2);
        List<Comparable<?>> expected = Arrays.asList(4194304L, 8388609L, 8388610L, 12582912L, 12582913L, 16777217L, 16777218L, 20971520L, 20971521L, 25165825L);
        List<Comparable<?>> actual = new ArrayList<>(DEFAULT_KEY_AMOUNT);
        for (int i = 0; i < DEFAULT_KEY_AMOUNT; i++) {
            actual.add(algorithm.generateKey());
        }
        assertThat(actual, is(expected));
    }
    
    @Test(expected = IllegalStateException.class)
    public void assertGenerateKeyWithClockCallBackBeyondTolerateTime() {
        TimeService timeService = new FixedTimeService(1);
        SnowflakeKeyGenerateAlgorithm.setTimeService(timeService);
        Properties props = new Properties();
        props.setProperty("max-tolerate-time-difference-milliseconds", String.valueOf(0));
        KeyGenerateAlgorithm algorithm = KeyGenerateAlgorithmFactory.newInstance(new AlgorithmConfiguration("SNOWFLAKE", props));
        if (algorithm instanceof InstanceContextAware) {
            ((InstanceContextAware) algorithm).setInstanceContext(INSTANCE);
        }
        setLastMilliseconds(algorithm, timeService.getCurrentMillis() + 2);
        List<Comparable<?>> actual = new ArrayList<>(DEFAULT_KEY_AMOUNT);
        for (int i = 0; i < DEFAULT_KEY_AMOUNT; i++) {
            actual.add(algorithm.generateKey());
        }
        assertThat(actual.size(), not(10));
    }
    
    @Test
    public void assertGenerateKeyBeyondMaxSequencePerMilliSecond() {
        TimeService timeService = new FixedTimeService(2);
        SnowflakeKeyGenerateAlgorithm.setTimeService(timeService);
        KeyGenerateAlgorithm algorithm = KeyGenerateAlgorithmFactory.newInstance(new AlgorithmConfiguration("SNOWFLAKE", new Properties()));
        if (algorithm instanceof InstanceContextAware) {
            ((InstanceContextAware) algorithm).setInstanceContext(INSTANCE);
        }
        setLastMilliseconds(algorithm, timeService.getCurrentMillis());
        setSequence(algorithm, (1 << DEFAULT_SEQUENCE_BITS) - 1);
        List<Comparable<?>> expected = Arrays.asList(4194304L, 4194305L, 4194306L, 8388608L, 8388609L, 8388610L, 12582913L, 12582914L, 12582915L, 16777216L);
        List<Comparable<?>> actual = new ArrayList<>(DEFAULT_KEY_AMOUNT);
        for (int i = 0; i < DEFAULT_KEY_AMOUNT; i++) {
            actual.add(algorithm.generateKey());
        }
        assertThat(actual, is(expected));
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private void setLastMilliseconds(final KeyGenerateAlgorithm algorithm, final Number value) {
        Field lastMilliseconds = SnowflakeKeyGenerateAlgorithm.class.getDeclaredField("lastMilliseconds");
        lastMilliseconds.setAccessible(true);
        lastMilliseconds.set(algorithm, value);
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private void setSequence(final KeyGenerateAlgorithm algorithm, final Number value) {
        Field sequence = SnowflakeKeyGenerateAlgorithm.class.getDeclaredField("sequence");
        sequence.setAccessible(true);
        sequence.set(algorithm, value);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertSetWorkerIdFailureWhenNegative() {
        SnowflakeKeyGenerateAlgorithm algorithm = (SnowflakeKeyGenerateAlgorithm) KeyGenerateAlgorithmFactory.newInstance(new AlgorithmConfiguration("SNOWFLAKE", new Properties()));
        InstanceContext instanceContext = new InstanceContext(new ComputeNodeInstance(mock(InstanceMetaData.class)), new WorkerIdGeneratorFixture(-1L),
                new ModeConfiguration("Standalone", null, false), mock(LockContext.class), new EventBusContext(), mock(ScheduleContext.class));
        algorithm.setInstanceContext(instanceContext);
        algorithm.generateKey();
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertSetMaxVibrationOffsetFailureWhenNegative() {
        Properties props = new Properties();
        props.setProperty("max-vibration-offset", String.valueOf(-1));
        KeyGenerateAlgorithmFactory.newInstance(new AlgorithmConfiguration("SNOWFLAKE", props)).generateKey();
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertSetWorkerIdFailureWhenOutOfRange() {
        SnowflakeKeyGenerateAlgorithm algorithm = (SnowflakeKeyGenerateAlgorithm) KeyGenerateAlgorithmFactory.newInstance(new AlgorithmConfiguration("SNOWFLAKE", new Properties()));
        InstanceContext instanceContext = new InstanceContext(new ComputeNodeInstance(mock(InstanceMetaData.class)), new WorkerIdGeneratorFixture(Long.MIN_VALUE),
                new ModeConfiguration("Standalone", null, false), mock(LockContext.class), new EventBusContext(), mock(ScheduleContext.class));
        algorithm.setInstanceContext(instanceContext);
        algorithm.generateKey();
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertSetMaxVibrationOffsetFailureWhenOutOfRange() {
        Properties props = new Properties();
        props.setProperty("max-vibration-offset", String.valueOf(4096));
        KeyGenerateAlgorithmFactory.newInstance(new AlgorithmConfiguration("SNOWFLAKE", props)).generateKey();
    }
    
    @Test
    public void assertSetMaxTolerateTimeDifferenceMilliseconds() throws NoSuchFieldException, IllegalAccessException {
        Properties props = new Properties();
        props.setProperty("max-tolerate-time-difference-milliseconds", String.valueOf(1));
        KeyGenerateAlgorithm algorithm = KeyGenerateAlgorithmFactory.newInstance(new AlgorithmConfiguration("SNOWFLAKE", props));
        Field field = algorithm.getClass().getDeclaredField("props");
        field.setAccessible(true);
        assertThat(((Properties) field.get(algorithm)).getProperty("max-tolerate-time-difference-milliseconds"), is("1"));
    }
}
