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
        properties.setProperty("serverList", "127.0.0.1:2181");
        properties.setProperty("serviceId", "testService1");
        properties.setProperty("maxTimeDifference", "5000");
        properties.setProperty("registryCenterType", "ForthTestRegistryCenter");
        leafSnowflakeKeyGenerator.setProperties(properties);
        FieldUtil.setStaticFinalField(leafSnowflakeKeyGenerator, "timeService", new FixedTimeService(1));
        List<Comparable<?>> expected = Arrays.<Comparable<?>>asList(4198401L, 4198402L, 8392704L, 8392705L, 12587009L, 12587010L, 16781312L, 16781313L, 20975617L, 20975618L);
        List<Comparable<?>> actual = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            actual.add(leafSnowflakeKeyGenerator.generateKey());
        }
        assertThat(actual, is(expected));
    }
    
    @Test
    public void assertGenerateKeyWithFixedWorkId() {
        Properties properties = new Properties();
        properties.setProperty("serverList", "127.0.0.1:2181");
        properties.setProperty("serviceId", "testService1");
        properties.setProperty("maxTimeDifference", "5000");
        properties.setProperty("registryCenterType", "ForthTestRegistryCenter");
        leafSnowflakeKeyGenerator.setProperties(properties);
        FieldUtil.setStaticFinalField(leafSnowflakeKeyGenerator, "timeService", new FixedTimeService(1));
        List<Comparable<?>> expected = Collections.<Comparable<?>>singletonList(4198401L);
        List<Comparable<?>> actual = new ArrayList<>();
        actual.add(leafSnowflakeKeyGenerator.generateKey());
        assertThat(actual, is(expected));
    }
    
    @Test(expected = IllegalStateException.class)
    public void generateKeyFailureWithTimeRollback() {
        Properties properties = new Properties();
        properties.setProperty("serverList", "127.0.0.1:2181");
        properties.setProperty("serviceId", "specialService");
        properties.setProperty("maxTimeDifference", "5000");
        properties.setProperty("registryCenterType", "FifthTestRegistryCenter");
        leafSnowflakeKeyGenerator.setProperties(properties);
        FieldUtil.setStaticFinalField(leafSnowflakeKeyGenerator, "timeService", new PreviousTimeService(15000));
        leafSnowflakeKeyGenerator.generateKey();
    }
    
    @Test
    public void generateKeySuccessWithTimeRollback() {
        Properties properties = new Properties();
        properties.setProperty("serverList", "127.0.0.1:2181");
        properties.setProperty("serviceId", "specialService");
        properties.setProperty("maxTimeDifference", "5000");
        properties.setProperty("registryCenterType", "FifthTestRegistryCenter");
        leafSnowflakeKeyGenerator.setProperties(properties);
        FieldUtil.setStaticFinalField(leafSnowflakeKeyGenerator, "timeService", new PreviousTimeService(3000));
        leafSnowflakeKeyGenerator.generateKey();
    }
    
    @Test
    public void assertGenerateKeyWithMultipleThreads() throws Exception {
        int threadNumber = Runtime.getRuntime().availableProcessors() << 1;
        ExecutorService executor = Executors.newFixedThreadPool(threadNumber);
        Properties properties = new Properties();
        properties.setProperty("serverList", "127.0.0.1:2181");
        properties.setProperty("serviceId", "testService1");
        properties.setProperty("maxTimeDifference", "5000");
        properties.setProperty("registryCenterType", "ForthTestRegistryCenter");
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
    public void assertGenerateKeyWithDigest() throws Exception {
        int threadNumber = Runtime.getRuntime().availableProcessors() << 1;
        ExecutorService executor = Executors.newFixedThreadPool(threadNumber);
        Properties properties = new Properties();
        properties.setProperty("serverList", "127.0.0.1:2181");
        properties.setProperty("serviceId", "testService1");
        properties.setProperty("maxTimeDifference", "5000");
        properties.setProperty("registryCenterType", "ForthTestRegistryCenter");
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
        properties.setProperty("serverList", "127.0.0.1:2181");
        properties.setProperty("serviceId", "testService1");
        properties.setProperty("registryCenterType", "ForthTestRegistryCenter");
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
        properties.setProperty("serviceId", "testService1");
        properties.setProperty("maxTimeDifference", "5000");
        properties.setProperty("registryCenterType", "ForthTestRegistryCenter");
        leafSnowflakeKeyGenerator.setProperties(properties);
        leafSnowflakeKeyGenerator.generateKey();
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertSetServerListFailureWhenArgumentEmpty() {
        Properties properties = new Properties();
        properties.setProperty("serverList", "");
        properties.setProperty("serviceId", "testService1");
        properties.setProperty("maxTimeDifference", "5000");
        properties.setProperty("registryCenterType", "ForthTestRegistryCenter");
        leafSnowflakeKeyGenerator.setProperties(properties);
        leafSnowflakeKeyGenerator.generateKey();
    }

    @Test(expected = IllegalArgumentException.class)
    public void assertSetServiceIdFailureWhenNull() {
        Properties properties = new Properties();
        properties.setProperty("serverList", "127.0.0.1:2181");
        properties.setProperty("maxTimeDifference", "5000");
        properties.setProperty("registryCenterType", "ForthTestRegistryCenter");
        leafSnowflakeKeyGenerator.setProperties(properties);
        leafSnowflakeKeyGenerator.generateKey();
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertSetServiceIdFailureWhenArgumentEmpty() {
        Properties properties = new Properties();
        properties.setProperty("serverList", "127.0.0.1:2181");
        properties.setProperty("serviceId", "");
        properties.setProperty("maxTimeDifference", "5000");
        properties.setProperty("registryCenterType", "ForthTestRegistryCenter");
        leafSnowflakeKeyGenerator.setProperties(properties);
        leafSnowflakeKeyGenerator.generateKey();
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertSetServiceIdFailureWithSlantingBar() {
        Properties properties = new Properties();
        properties.setProperty("serverList", "127.0.0.1:2181");
        properties.setProperty("serviceId", "/testService1");
        properties.setProperty("maxTimeDifference", "5000");
        properties.setProperty("registryCenterType", "ForthTestRegistryCenter");
        leafSnowflakeKeyGenerator.setProperties(properties);
        leafSnowflakeKeyGenerator.generateKey();
    }

    @Test(expected = IllegalArgumentException.class)
    public void assertSetMaxTimeDifferenceFailureWhenNegative() {
        Properties properties = new Properties();
        properties.setProperty("serverList", "127.0.0.1:2181");
        properties.setProperty("serviceId", "testService1");
        properties.setProperty("maxTimeDifference", "-5");
        properties.setProperty("registryCenterType", "ForthTestRegistryCenter");
        leafSnowflakeKeyGenerator.setProperties(properties);
        leafSnowflakeKeyGenerator.generateKey();
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertSetMaxTimeDifferenceFailureWhenArgumentEmpty() {
        Properties properties = new Properties();
        properties.setProperty("serverList", "127.0.0.1:2181");
        properties.setProperty("serviceId", "testService1");
        properties.setProperty("maxTimeDifference", "");
        properties.setProperty("registryCenterType", "ForthTestRegistryCenter");
        leafSnowflakeKeyGenerator.setProperties(properties);
        leafSnowflakeKeyGenerator.generateKey();
    }
    
    @Test
    public void assertSetMaxTimeDifferenceSuccessWhenNull() {
        Properties properties = new Properties();
        properties.setProperty("serverList", "127.0.0.1:2181");
        properties.setProperty("serviceId", "testService1");
        properties.setProperty("registryCenterType", "ForthTestRegistryCenter");
        leafSnowflakeKeyGenerator.setProperties(properties);
        leafSnowflakeKeyGenerator.generateKey();
    }
    
    @Test(expected = Exception.class)
    public void assertSetRegistryCenterTypeFailureWhenArgumentEmpty() {
        Properties properties = new Properties();
        properties.setProperty("serverList", "127.0.0.1:2181");
        properties.setProperty("serviceId", "testService1");
        properties.setProperty("maxTimeDifference", "5000");
        properties.setProperty("registryCenterType", "FakeTestRegistryCenter");
        leafSnowflakeKeyGenerator.setProperties(properties);
        leafSnowflakeKeyGenerator.generateKey();
    }
}
