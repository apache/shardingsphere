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

package org.apache.shardingsphere.keygen.snowflake.algorithm;

import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstance;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.instance.InstanceContextAware;
import org.apache.shardingsphere.infra.instance.metadata.InstanceMetaData;
import org.apache.shardingsphere.infra.instance.mode.ModeContextManager;
import org.apache.shardingsphere.infra.lock.LockContext;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.util.eventbus.EventBusContext;
import org.apache.shardingsphere.keygen.core.algorithm.KeyGenerateAlgorithm;
import org.apache.shardingsphere.keygen.core.context.KeyGenerateContext;
import org.apache.shardingsphere.keygen.core.exception.algorithm.KeyGenerateAlgorithmInitializationException;
import org.apache.shardingsphere.keygen.snowflake.exception.SnowflakeClockMoveBackException;
import org.apache.shardingsphere.keygen.snowflake.fixture.FixedTimeService;
import org.apache.shardingsphere.keygen.snowflake.fixture.WorkerIdGeneratorFixture;
import org.apache.shardingsphere.test.util.PropertiesBuilder;
import org.apache.shardingsphere.test.util.PropertiesBuilder.Property;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.mockito.internal.configuration.plugins.Plugins;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SnowflakeKeyGenerateAlgorithmTest {
    
    private static final long DEFAULT_SEQUENCE_BITS = 12L;
    
    private static final int DEFAULT_KEY_AMOUNT = 10;
    
    private static final InstanceContext INSTANCE;
    
    static {
        InstanceContext instanceContext = mock(InstanceContext.class);
        when(instanceContext.getWorkerId()).thenReturn(0);
        INSTANCE = instanceContext;
    }
    
    @Test
    void assertGenerateKeyWithMultipleThreads() throws ExecutionException, InterruptedException {
        int threadNumber = Runtime.getRuntime().availableProcessors() * 2;
        ExecutorService executor = Executors.newFixedThreadPool(threadNumber);
        int taskNumber = threadNumber * 4;
        KeyGenerateAlgorithm algorithm = TypedSPILoader.getService(KeyGenerateAlgorithm.class, "SNOWFLAKE");
        if (algorithm instanceof InstanceContextAware) {
            ((InstanceContextAware) algorithm).setInstanceContext(INSTANCE);
        }
        Set<Comparable<?>> actual = new HashSet<>(taskNumber, 1F);
        for (int i = 0; i < taskNumber; i++) {
            actual.addAll(executor.submit(() -> algorithm.generateKeys(mock(KeyGenerateContext.class), 1)).get());
        }
        assertThat(actual.size(), is(taskNumber));
    }
    
    @Test
    void assertGenerateKeyWithSingleThread() {
        SnowflakeKeyGenerateAlgorithm.setTimeService(new FixedTimeService(1));
        KeyGenerateAlgorithm algorithm = TypedSPILoader.getService(KeyGenerateAlgorithm.class, "SNOWFLAKE");
        if (algorithm instanceof InstanceContextAware) {
            ((InstanceContextAware) algorithm).setInstanceContext(INSTANCE);
        }
        List<Comparable<?>> expected = Arrays.asList(0L, 4194305L, 4194306L, 8388608L, 8388609L, 12582913L, 12582914L, 16777216L, 16777217L, 20971521L);
        List<Comparable<?>> actual = new ArrayList<>(DEFAULT_KEY_AMOUNT);
        actual.addAll(algorithm.generateKeys(mock(KeyGenerateContext.class), DEFAULT_KEY_AMOUNT));
        assertThat(actual, is(expected));
    }
    
    @Test
    void assertLastDigitalOfGenerateKeySameMillisecond() {
        SnowflakeKeyGenerateAlgorithm.setTimeService(new FixedTimeService(5));
        KeyGenerateAlgorithm algorithm = TypedSPILoader.getService(KeyGenerateAlgorithm.class, "SNOWFLAKE", PropertiesBuilder.build(new Property("max-vibration-offset", "3")));
        if (algorithm instanceof InstanceContextAware) {
            ((InstanceContextAware) algorithm).setInstanceContext(INSTANCE);
        }
        Collection<? extends Comparable<?>> actual = algorithm.generateKeys(mock(KeyGenerateContext.class), 5);
        assertThat(actual.size(), is(5));
        Iterator<? extends Comparable<?>> iterator = actual.iterator();
        assertThat(iterator.next(), is(0L));
        assertThat(iterator.next(), is(1L));
        assertThat(iterator.next(), is(2L));
        assertThat(iterator.next(), is(3L));
        assertThat(iterator.next(), is(4L));
    }
    
    @Test
    void assertLastDigitalOfGenerateKeyDifferentMillisecond() {
        SnowflakeKeyGenerateAlgorithm.setTimeService(new TimeService());
        KeyGenerateAlgorithm algorithm = TypedSPILoader.getService(KeyGenerateAlgorithm.class, "SNOWFLAKE", PropertiesBuilder.build(new Property("max-vibration-offset", "3")));
        if (algorithm instanceof InstanceContextAware) {
            ((InstanceContextAware) algorithm).setInstanceContext(INSTANCE);
        }
        String actualGenerateKey0 = Long.toBinaryString(Long.parseLong(algorithm.generateKeys(mock(KeyGenerateContext.class), 1).iterator().next().toString()));
        assertThat(Integer.parseInt(actualGenerateKey0.substring(actualGenerateKey0.length() - 3), 2), is(0));
        Awaitility.await().pollDelay(2L, TimeUnit.MILLISECONDS).until(() -> true);
        String actualGenerateKey1 = Long.toBinaryString(Long.parseLong(algorithm.generateKeys(mock(KeyGenerateContext.class), 1).iterator().next().toString()));
        assertThat(Integer.parseInt(actualGenerateKey1.substring(actualGenerateKey1.length() - 3), 2), is(1));
        Awaitility.await().pollDelay(2L, TimeUnit.MILLISECONDS).until(() -> true);
        String actualGenerateKey2 = Long.toBinaryString(Long.parseLong(algorithm.generateKeys(mock(KeyGenerateContext.class), 1).iterator().next().toString()));
        assertThat(Integer.parseInt(actualGenerateKey2.substring(actualGenerateKey2.length() - 3), 2), is(2));
        Awaitility.await().pollDelay(2L, TimeUnit.MILLISECONDS).until(() -> true);
        String actualGenerateKey3 = Long.toBinaryString(Long.parseLong(algorithm.generateKeys(mock(KeyGenerateContext.class), 1).iterator().next().toString()));
        assertThat(Integer.parseInt(actualGenerateKey3.substring(actualGenerateKey3.length() - 3), 2), is(3));
        Awaitility.await().pollDelay(2L, TimeUnit.MILLISECONDS).until(() -> true);
        String actualGenerateKey4 = Long.toBinaryString(Long.parseLong(algorithm.generateKeys(mock(KeyGenerateContext.class), 1).iterator().next().toString()));
        assertThat(Integer.parseInt(actualGenerateKey4.substring(actualGenerateKey4.length() - 3), 2), is(0));
    }
    
    @Test
    void assertGenerateKeyWithClockCallBack() {
        TimeService timeService = new FixedTimeService(1);
        SnowflakeKeyGenerateAlgorithm.setTimeService(timeService);
        KeyGenerateAlgorithm algorithm = TypedSPILoader.getService(KeyGenerateAlgorithm.class, "SNOWFLAKE");
        if (algorithm instanceof InstanceContextAware) {
            ((InstanceContextAware) algorithm).setInstanceContext(INSTANCE);
        }
        setLastMillis(algorithm, timeService.getCurrentMillis() + 2);
        List<Comparable<?>> expected = Arrays.asList(4194304L, 8388609L, 8388610L, 12582912L, 12582913L, 16777217L, 16777218L, 20971520L, 20971521L, 25165825L);
        List<Comparable<?>> actual = new ArrayList<>(DEFAULT_KEY_AMOUNT);
        actual.addAll(algorithm.generateKeys(mock(KeyGenerateContext.class), DEFAULT_KEY_AMOUNT));
        assertThat(actual, is(expected));
    }
    
    @Test
    void assertGenerateKeyWithClockCallBackBeyondTolerateTime() {
        TimeService timeService = new FixedTimeService(1);
        SnowflakeKeyGenerateAlgorithm.setTimeService(timeService);
        KeyGenerateAlgorithm algorithm = TypedSPILoader.getService(KeyGenerateAlgorithm.class, "SNOWFLAKE", PropertiesBuilder.build(new Property("max-tolerate-time-difference-milliseconds", "0")));
        if (algorithm instanceof InstanceContextAware) {
            ((InstanceContextAware) algorithm).setInstanceContext(INSTANCE);
        }
        setLastMillis(algorithm, timeService.getCurrentMillis() + 2);
        assertThrows(SnowflakeClockMoveBackException.class, () -> batchGenerate(algorithm));
    }
    
    private void batchGenerate(final KeyGenerateAlgorithm algorithm) {
        algorithm.generateKeys(mock(KeyGenerateContext.class), DEFAULT_KEY_AMOUNT);
    }
    
    @Test
    void assertGenerateKeyBeyondMaxSequencePerMilliSecond() {
        TimeService timeService = new FixedTimeService(2);
        SnowflakeKeyGenerateAlgorithm.setTimeService(timeService);
        KeyGenerateAlgorithm algorithm = TypedSPILoader.getService(KeyGenerateAlgorithm.class, "SNOWFLAKE");
        if (algorithm instanceof InstanceContextAware) {
            ((InstanceContextAware) algorithm).setInstanceContext(INSTANCE);
        }
        setLastMillis(algorithm, timeService.getCurrentMillis());
        setSequence(algorithm, (1 << DEFAULT_SEQUENCE_BITS) - 1L);
        List<Comparable<?>> expected = Arrays.asList(4194304L, 4194305L, 4194306L, 8388608L, 8388609L, 8388610L, 12582913L, 12582914L, 12582915L, 16777216L);
        List<Comparable<?>> actual = new ArrayList<>(DEFAULT_KEY_AMOUNT);
        actual.addAll(algorithm.generateKeys(mock(KeyGenerateContext.class), DEFAULT_KEY_AMOUNT));
        assertThat(actual, is(expected));
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private void setLastMillis(final KeyGenerateAlgorithm algorithm, final Number value) {
        Plugins.getMemberAccessor().set(SnowflakeKeyGenerateAlgorithm.class.getDeclaredField("lastMillis"), algorithm, new AtomicLong(value.longValue()));
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private void setSequence(final KeyGenerateAlgorithm algorithm, final Number value) {
        Plugins.getMemberAccessor().set(SnowflakeKeyGenerateAlgorithm.class.getDeclaredField("sequence"), algorithm, new AtomicLong(value.longValue()));
    }
    
    @Test
    void assertSetWorkerIdFailureWhenNegative() {
        SnowflakeKeyGenerateAlgorithm algorithm = (SnowflakeKeyGenerateAlgorithm) TypedSPILoader.getService(KeyGenerateAlgorithm.class, "SNOWFLAKE");
        InstanceContext instanceContext = new InstanceContext(new ComputeNodeInstance(mock(InstanceMetaData.class)), new WorkerIdGeneratorFixture(-1),
                new ModeConfiguration("Standalone", null), mock(ModeContextManager.class), mock(LockContext.class), new EventBusContext());
        assertThrows(IllegalArgumentException.class, () -> algorithm.setInstanceContext(instanceContext));
    }
    
    @Test
    void assertSetMaxVibrationOffsetFailureWhenNegative() {
        assertThrows(KeyGenerateAlgorithmInitializationException.class,
                () -> TypedSPILoader.getService(KeyGenerateAlgorithm.class, "SNOWFLAKE", PropertiesBuilder.build(new Property("max-vibration-offset", "-1")))
                        .generateKeys(mock(KeyGenerateContext.class), 1));
    }
    
    @Test
    void assertSetWorkerIdFailureWhenOutOfRange() {
        SnowflakeKeyGenerateAlgorithm algorithm = (SnowflakeKeyGenerateAlgorithm) TypedSPILoader.getService(KeyGenerateAlgorithm.class, "SNOWFLAKE");
        InstanceContext instanceContext = new InstanceContext(new ComputeNodeInstance(mock(InstanceMetaData.class)), new WorkerIdGeneratorFixture(Integer.MIN_VALUE),
                new ModeConfiguration("Standalone", null), mock(ModeContextManager.class), mock(LockContext.class), new EventBusContext());
        assertThrows(IllegalArgumentException.class, () -> algorithm.setInstanceContext(instanceContext));
    }
    
    @Test
    void assertSetMaxVibrationOffsetFailureWhenOutOfRange() {
        assertThrows(KeyGenerateAlgorithmInitializationException.class,
                () -> TypedSPILoader.getService(KeyGenerateAlgorithm.class, "SNOWFLAKE", PropertiesBuilder.build(new Property("max-vibration-offset", "4096")))
                        .generateKeys(mock(KeyGenerateContext.class), 1));
    }
    
    @Test
    void assertSetMaxTolerateTimeDifferenceMilliseconds() throws ReflectiveOperationException {
        KeyGenerateAlgorithm algorithm = TypedSPILoader.getService(KeyGenerateAlgorithm.class, "SNOWFLAKE", PropertiesBuilder.build(new Property("max-tolerate-time-difference-milliseconds", "1")));
        assertThat(((Properties) Plugins.getMemberAccessor().get(algorithm.getClass().getDeclaredField("props"), algorithm)).getProperty("max-tolerate-time-difference-milliseconds"), is("1"));
    }
    
    @Test
    void assertMaxTolerateTimeDifferenceMillisecondsWhenNegative() {
        assertThrows(KeyGenerateAlgorithmInitializationException.class,
                () -> TypedSPILoader.getService(KeyGenerateAlgorithm.class, "SNOWFLAKE", PropertiesBuilder.build(new Property("max-tolerate-time-difference-milliseconds", "-1")))
                        .generateKeys(mock(KeyGenerateContext.class), 1));
    }
}
