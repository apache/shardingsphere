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

package org.apache.shardingsphere.orchestration.internal.keygen;

import org.apache.shardingsphere.orchestration.internal.keygen.fixture.FixedTimeService;
import org.apache.shardingsphere.orchestration.internal.keygen.fixture.PreviousTimeService;
import org.apache.shardingsphere.orchestration.util.FieldUtil;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class LeafSnowflakeKeyGeneratorTest {
    
    private final LeafSnowflakeKeyGenerator leafSnowflakeKeyGenerator = new LeafSnowflakeKeyGenerator();
    
    @Test
    public void assertGetProperties() {
        assertThat(leafSnowflakeKeyGenerator.getProperties().entrySet().size(), is(0));
    }
    
    @Test
    public void assertSetProperties() {
        Properties properties = new Properties();
        properties.setProperty("key1", "value1");
        leafSnowflakeKeyGenerator.setProperties(properties);
        assertThat(leafSnowflakeKeyGenerator.getProperties().get("key1"), is((Object) "value1"));
    }
    
    @Test
    public void assertGenerateKeyWithSingleThread() {
        Properties properties = new Properties();
        properties.setProperty("server.list", "127.0.0.1:2181");
        properties.setProperty("service.id", "testService1");
        properties.setProperty("max.tolerate.time.difference.milliseconds", "5000");
        properties.setProperty("registry.center.type", "ForthTestRegistryCenter");
        leafSnowflakeKeyGenerator.setProperties(properties);
        FieldUtil.setStaticFinalField(leafSnowflakeKeyGenerator, "timeService", new FixedTimeService(1));
        List<Comparable<?>> expected = Arrays.<Comparable<?>>asList(4198400L, 4198401L, 8392705L, 8392706L, 12587008L, 12587009L, 16781313L, 16781314L, 20975616L, 20975617L);
        List<Comparable<?>> actual = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            actual.add(leafSnowflakeKeyGenerator.generateKey());
        }
        assertThat(actual, is(expected));
    }

    @Test
    public void assertLastDigitalOfGenerateKeySameMillisecond() {
        Properties properties = new Properties();
        properties.setProperty("server.list", "127.0.0.1:2181");
        properties.setProperty("service.id", "testService1");
        properties.setProperty("registry.center.digest", "name:123456");
        properties.setProperty("max.tolerate.time.difference.milliseconds", "5000");
        properties.setProperty("registry.center.type", "ForthTestRegistryCenter");
        properties.setProperty("max.vibration.offset", String.valueOf(3));
        FieldUtil.setStaticFinalField(leafSnowflakeKeyGenerator, "timeService", new FixedTimeService(6));
        leafSnowflakeKeyGenerator.setProperties(properties);
        String actualGenerateKeyBinaryString0 = Long.toBinaryString(Long.parseLong(leafSnowflakeKeyGenerator.generateKey().toString()));
        assertThat(Integer.parseInt(actualGenerateKeyBinaryString0.substring(actualGenerateKeyBinaryString0.length() - 3), 2), is(0));
        String actualGenerateKeyBinaryString1 = Long.toBinaryString(Long.parseLong(leafSnowflakeKeyGenerator.generateKey().toString()));
        assertThat(Integer.parseInt(actualGenerateKeyBinaryString1.substring(actualGenerateKeyBinaryString1.length() - 3), 2), is(1));
        String actualGenerateKeyBinaryString2 = Long.toBinaryString(Long.parseLong(leafSnowflakeKeyGenerator.generateKey().toString()));
        assertThat(Integer.parseInt(actualGenerateKeyBinaryString2.substring(actualGenerateKeyBinaryString2.length() - 3), 2), is(2));
        String actualGenerateKeyBinaryString3 = Long.toBinaryString(Long.parseLong(leafSnowflakeKeyGenerator.generateKey().toString()));
        assertThat(Integer.parseInt(actualGenerateKeyBinaryString3.substring(actualGenerateKeyBinaryString3.length() - 3), 2), is(3));
        String actualGenerateKeyBinaryString4 = Long.toBinaryString(Long.parseLong(leafSnowflakeKeyGenerator.generateKey().toString()));
        assertThat(Integer.parseInt(actualGenerateKeyBinaryString4.substring(actualGenerateKeyBinaryString4.length() - 3), 2), is(4));
    }

    @Test
    public void assertLastDigitalOfGenerateKeyDifferentMillisecond() throws InterruptedException {
        Properties properties = new Properties();
        properties.setProperty("server.list", "127.0.0.1:2181");
        properties.setProperty("service.id", "testService1");
        properties.setProperty("registry.center.digest", "name:123456");
        properties.setProperty("max.tolerate.time.difference.milliseconds", "5000");
        properties.setProperty("registry.center.type", "ForthTestRegistryCenter");
        properties.setProperty("max.vibration.offset", String.valueOf(3));
        FieldUtil.setStaticFinalField(leafSnowflakeKeyGenerator, "timeService", new TimeService());
        leafSnowflakeKeyGenerator.setProperties(properties);
        String actualGenerateKeyBinaryString0 = Long.toBinaryString(Long.parseLong(leafSnowflakeKeyGenerator.generateKey().toString()));
        assertThat(Integer.parseInt(actualGenerateKeyBinaryString0.substring(actualGenerateKeyBinaryString0.length() - 3), 2), is(0));
        Thread.sleep(2L);
        String actualGenerateKeyBinaryString1 = Long.toBinaryString(Long.parseLong(leafSnowflakeKeyGenerator.generateKey().toString()));
        assertThat(Integer.parseInt(actualGenerateKeyBinaryString1.substring(actualGenerateKeyBinaryString1.length() - 3), 2), is(1));
        Thread.sleep(2L);
        String actualGenerateKeyBinaryString2 = Long.toBinaryString(Long.parseLong(leafSnowflakeKeyGenerator.generateKey().toString()));
        assertThat(Integer.parseInt(actualGenerateKeyBinaryString2.substring(actualGenerateKeyBinaryString2.length() - 3), 2), is(2));
        Thread.sleep(2L);
        String actualGenerateKeyBinaryString3 = Long.toBinaryString(Long.parseLong(leafSnowflakeKeyGenerator.generateKey().toString()));
        assertThat(Integer.parseInt(actualGenerateKeyBinaryString3.substring(actualGenerateKeyBinaryString3.length() - 3), 2), is(3));
        Thread.sleep(2L);
        String actualGenerateKeyBinaryString4 = Long.toBinaryString(Long.parseLong(leafSnowflakeKeyGenerator.generateKey().toString()));
        assertThat(Integer.parseInt(actualGenerateKeyBinaryString4.substring(actualGenerateKeyBinaryString4.length() - 3), 2), is(0));
    }
    
    @Test
    public void assertGenerateKeyWithDigest() {
        Properties properties = new Properties();
        properties.setProperty("server.list", "127.0.0.1:2181");
        properties.setProperty("service.id", "testService1");
        properties.setProperty("registry.center.digest", "name:123456");
        properties.setProperty("max.tolerate.time.difference.milliseconds", "5000");
        properties.setProperty("registry.center.type", "ForthTestRegistryCenter");
        leafSnowflakeKeyGenerator.setProperties(properties);
        FieldUtil.setStaticFinalField(leafSnowflakeKeyGenerator, "timeService", new FixedTimeService(1));
        List<Comparable<?>> expected = Arrays.<Comparable<?>>asList(4198400L, 4198401L, 8392705L, 8392706L, 12587008L, 12587009L, 16781313L, 16781314L, 20975616L, 20975617L);
        List<Comparable<?>> actual = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            actual.add(leafSnowflakeKeyGenerator.generateKey());
        }
        assertThat(actual, is(expected));
    }
    
    @Test
    public void assertGenerateKeyWithFixedWorkId() {
        Properties properties = new Properties();
        properties.setProperty("server.list", "127.0.0.1:2181");
        properties.setProperty("service.id", "testService1");
        properties.setProperty("max.tolerate.time.difference.milliseconds", "5000");
        properties.setProperty("registry.center.type", "ForthTestRegistryCenter");
        leafSnowflakeKeyGenerator.setProperties(properties);
        FieldUtil.setStaticFinalField(leafSnowflakeKeyGenerator, "timeService", new FixedTimeService(1));
        List<Comparable<?>> expected = Collections.<Comparable<?>>singletonList(4198400L);
        List<Comparable<?>> actual = new ArrayList<>();
        actual.add(leafSnowflakeKeyGenerator.generateKey());
        assertThat(actual, is(expected));
    }
    
    @Test(expected = IllegalStateException.class)
    public void generateKeyFailureWithTimeRollback() {
        Properties properties = new Properties();
        properties.setProperty("server.list", "127.0.0.1:2181");
        properties.setProperty("service.id", "specialService");
        properties.setProperty("max.tolerate.time.difference.milliseconds", "5000");
        properties.setProperty("registry.center.type", "FifthTestRegistryCenter");
        leafSnowflakeKeyGenerator.setProperties(properties);
        FieldUtil.setStaticFinalField(leafSnowflakeKeyGenerator, "timeService", new PreviousTimeService(15000));
        leafSnowflakeKeyGenerator.generateKey();
    }
    
    @Test
    public void generateKeySuccessWithTimeRollback() {
        Properties properties = new Properties();
        properties.setProperty("server.list", "127.0.0.1:2181");
        properties.setProperty("service.id", "specialService");
        properties.setProperty("max.tolerate.time.difference.milliseconds", "5000");
        properties.setProperty("registry.center.type", "FifthTestRegistryCenter");
        leafSnowflakeKeyGenerator.setProperties(properties);
        FieldUtil.setStaticFinalField(leafSnowflakeKeyGenerator, "timeService", new PreviousTimeService(3000));
        leafSnowflakeKeyGenerator.generateKey();
    }
    
    @Test
    public void assertGenerateKeyWithMultipleThreads() throws Exception {
        int threadNumber = Runtime.getRuntime().availableProcessors() << 1;
        ExecutorService executor = Executors.newFixedThreadPool(threadNumber);
        Properties properties = new Properties();
        properties.setProperty("server.list", "127.0.0.1:2181");
        properties.setProperty("service.id", "testService1");
        properties.setProperty("max.tolerate.time.difference.milliseconds", "5000");
        properties.setProperty("registry.center.type", "ForthTestRegistryCenter");
        leafSnowflakeKeyGenerator.setProperties(properties);
        Set<Comparable<?>> actual = new HashSet<>();
        int taskNumber = threadNumber << 2;
        for (int i = 0; i < taskNumber; i++) {
            actual.add(executor.submit(new Callable<Comparable<?>>() {

                @Override
                public Comparable<?> call() {
                    return leafSnowflakeKeyGenerator.generateKey();
                }
            }).get());
        }
        assertThat(actual.size(), is(taskNumber));
    }
    
    @Test
    public void assertGenerateKeyWithDefaultMaxTimeDifference() throws Exception {
        int threadNumber = Runtime.getRuntime().availableProcessors() << 1;
        ExecutorService executor = Executors.newFixedThreadPool(threadNumber);
        Properties properties = new Properties();
        properties.setProperty("server.list", "127.0.0.1:2181");
        properties.setProperty("service.id", "testService1");
        properties.setProperty("registry.center.type", "ForthTestRegistryCenter");
        leafSnowflakeKeyGenerator.setProperties(properties);
        Set<Comparable<?>> actual = new HashSet<>();
        int taskNumber = threadNumber << 2;
        for (int i = 0; i < taskNumber; i++) {
            actual.add(executor.submit(new Callable<Comparable<?>>() {

                @Override
                public Comparable<?> call() {
                    return leafSnowflakeKeyGenerator.generateKey();
                }
            }).get());
        }
        assertThat(actual.size(), is(taskNumber));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertSetServerListFailureWhenNull() {
        Properties properties = new Properties();
        properties.setProperty("service.id", "testService1");
        properties.setProperty("max.tolerate.time.difference.milliseconds", "5000");
        properties.setProperty("registry.center.type", "ForthTestRegistryCenter");
        leafSnowflakeKeyGenerator.setProperties(properties);
        leafSnowflakeKeyGenerator.generateKey();
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertSetServerListFailureWhenArgumentEmpty() {
        Properties properties = new Properties();
        properties.setProperty("server.list", "");
        properties.setProperty("service.id", "testService1");
        properties.setProperty("max.tolerate.time.difference.milliseconds", "5000");
        properties.setProperty("registry.center.type", "ForthTestRegistryCenter");
        leafSnowflakeKeyGenerator.setProperties(properties);
        leafSnowflakeKeyGenerator.generateKey();
    }

    @Test(expected = IllegalArgumentException.class)
    public void assertSetServiceIdFailureWhenNull() {
        Properties properties = new Properties();
        properties.setProperty("server.list", "127.0.0.1:2181");
        properties.setProperty("max.tolerate.time.difference.milliseconds", "5000");
        properties.setProperty("registry.center.type", "ForthTestRegistryCenter");
        leafSnowflakeKeyGenerator.setProperties(properties);
        leafSnowflakeKeyGenerator.generateKey();
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertSetServiceIdFailureWhenArgumentEmpty() {
        Properties properties = new Properties();
        properties.setProperty("server.list", "127.0.0.1:2181");
        properties.setProperty("service.id", "");
        properties.setProperty("max.tolerate.time.difference.milliseconds", "5000");
        properties.setProperty("registry.center.type", "ForthTestRegistryCenter");
        leafSnowflakeKeyGenerator.setProperties(properties);
        leafSnowflakeKeyGenerator.generateKey();
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertSetServiceIdFailureWithSlantingBar() {
        Properties properties = new Properties();
        properties.setProperty("server.list", "127.0.0.1:2181");
        properties.setProperty("service.id", "/testService1");
        properties.setProperty("max.tolerate.time.difference.milliseconds", "5000");
        properties.setProperty("registry.center.type", "ForthTestRegistryCenter");
        leafSnowflakeKeyGenerator.setProperties(properties);
        leafSnowflakeKeyGenerator.generateKey();
    }

    @Test(expected = IllegalArgumentException.class)
    public void assertSetMaxTimeDifferenceFailureWhenNegative() {
        Properties properties = new Properties();
        properties.setProperty("server.list", "127.0.0.1:2181");
        properties.setProperty("service.id", "testService1");
        properties.setProperty("max.tolerate.time.difference.milliseconds", "-5");
        properties.setProperty("registry.center.type", "ForthTestRegistryCenter");
        leafSnowflakeKeyGenerator.setProperties(properties);
        leafSnowflakeKeyGenerator.generateKey();
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertSetMaxTimeDifferenceFailureWhenArgumentEmpty() {
        Properties properties = new Properties();
        properties.setProperty("server.list", "127.0.0.1:2181");
        properties.setProperty("service.id", "testService1");
        properties.setProperty("max.tolerate.time.difference.milliseconds", "");
        properties.setProperty("registry.center.type", "ForthTestRegistryCenter");
        leafSnowflakeKeyGenerator.setProperties(properties);
        leafSnowflakeKeyGenerator.generateKey();
    }
    
    @Test
    public void assertSetMaxTimeDifferenceSuccessWhenNull() {
        Properties properties = new Properties();
        properties.setProperty("server.list", "127.0.0.1:2181");
        properties.setProperty("service.id", "testService1");
        properties.setProperty("registry.center.type", "ForthTestRegistryCenter");
        leafSnowflakeKeyGenerator.setProperties(properties);
        leafSnowflakeKeyGenerator.generateKey();
    }
    
    @Test(expected = Exception.class)
    public void assertSetRegistryCenterTypeFailureWhenArgumentWrong() {
        Properties properties = new Properties();
        properties.setProperty("server.list", "127.0.0.1:2181");
        properties.setProperty("service.id", "testService1");
        properties.setProperty("max.tolerate.time.difference.milliseconds", "5000");
        properties.setProperty("registry.center.type", "FakeTestRegistryCenter");
        leafSnowflakeKeyGenerator.setProperties(properties);
        leafSnowflakeKeyGenerator.generateKey();
    }

    @Test(expected = IllegalArgumentException.class)
    public void assertSetMaxVibrationOffsetFailureWhenNegative() {
        Properties properties = new Properties();
        properties.setProperty("max.vibration.offset", String.valueOf(-1));
        leafSnowflakeKeyGenerator.setProperties(properties);
        leafSnowflakeKeyGenerator.generateKey();
    }

    @Test(expected = IllegalArgumentException.class)
    public void assertSetMaxVibrationOffsetFailureWhenOutOfRange() {
        Properties properties = new Properties();
        properties.setProperty("max.vibration.offset", String.valueOf(4096));
        leafSnowflakeKeyGenerator.setProperties(properties);
        leafSnowflakeKeyGenerator.generateKey();
    }
}
