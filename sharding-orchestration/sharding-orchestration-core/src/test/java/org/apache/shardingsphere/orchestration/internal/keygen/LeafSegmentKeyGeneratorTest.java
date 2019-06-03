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

import lombok.SneakyThrows;
import org.apache.curator.test.TestingServer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class LeafSegmentKeyGeneratorTest {

    private LeafSegmentKeyGenerator leafSegmentKeyGenerator = new LeafSegmentKeyGenerator();

    private static TestingServer server;

    @BeforeClass
    @SneakyThrows
    public static void startServer(){
        server = new TestingServer(2181,true);
    }

    @Test
    public void assertGetProperties() {
        assertThat(leafSegmentKeyGenerator.getProperties().entrySet().size(), is(0));
    }

    @Test
    public void assertSetProperties() {
        Properties properties = new Properties();
        properties.setProperty("key1", "value1");
        leafSegmentKeyGenerator.setProperties(properties);
        assertThat(leafSegmentKeyGenerator.getProperties().get("key1"), is((Object) "value1"));
    }

    @Test
    public void assertGenerateKeyWithSingleThread(){
        Properties properties = new Properties();
        properties.setProperty("serverList","127.0.0.1:2181");
        properties.setProperty("initialValue","100001");
        properties.setProperty("step","3");
        properties.setProperty("digest","");
        properties.setProperty("leaf.key","test_table_1");
        leafSegmentKeyGenerator.setProperties(properties);
        List<Comparable<?>> expected = Arrays.<Comparable<?>>asList(100001L,100002L,100003L,100004L,100005L,100006L,100007L,100008L,100009L,100010L);
        List<Comparable<?>> actual = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            actual.add(leafSegmentKeyGenerator.generateKey());
        }
        assertThat(actual, is(expected));
    }

    @Test
    @SneakyThrows
    public void assertGenerateKeyWithMultipleThreads() {
        int threadNumber = Runtime.getRuntime().availableProcessors() << 1;
        ExecutorService executor = Executors.newFixedThreadPool(threadNumber);
        int taskNumber = threadNumber << 2;
        Properties properties = new Properties();
        properties.setProperty("serverList","127.0.0.1:2181");
        properties.setProperty("initialValue","100001");
        properties.setProperty("step","3");
        properties.setProperty("digest","");
        properties.setProperty("leaf.key","test_table_2");
        leafSegmentKeyGenerator.setProperties(properties);
        Set<Comparable<?>> actual = new HashSet<>();
        for (int i = 0; i < taskNumber; i++) {
            actual.add(executor.submit(new Callable<Comparable<?>>() {
                @Override
                public Comparable<?> call() {
                    return leafSegmentKeyGenerator.generateKey();
                }
            }).get());
        }
        assertThat(actual.size(), is(taskNumber));
    }

    @Test
    @SneakyThrows
    public void assertGenerateKeyWithDigest() {
        int threadNumber = Runtime.getRuntime().availableProcessors() << 1;
        ExecutorService executor = Executors.newFixedThreadPool(threadNumber);
        int taskNumber = threadNumber << 2;
        Properties properties = new Properties();
        properties.setProperty("serverList","127.0.0.1:2181");
        properties.setProperty("initialValue","100001");
        properties.setProperty("step","3");
        properties.setProperty("digest","user1:1231");
        properties.setProperty("leaf.key","test_table_3");
        leafSegmentKeyGenerator.setProperties(properties);
        Set<Comparable<?>> actual = new HashSet<>();
        for (int i = 0; i < taskNumber; i++) {
            actual.add(executor.submit(new Callable<Comparable<?>>() {
                @Override
                public Comparable<?> call() {
                    return leafSegmentKeyGenerator.generateKey();
                }
            }).get());
        }
        assertThat(actual.size(), is(taskNumber));
    }

    @Test(expected = Exception.class)
    @SneakyThrows
    public void assertGenerateKeyWithWrongDigest() {
        int threadNumber = Runtime.getRuntime().availableProcessors() << 1;
        ExecutorService executor = Executors.newFixedThreadPool(threadNumber);
        int taskNumber = threadNumber << 2;
        Properties propertiesBefore = new Properties();
        propertiesBefore.setProperty("serverList","127.0.0.1:2181");
        propertiesBefore.setProperty("initialValue","100001");
        propertiesBefore.setProperty("step","3");
        propertiesBefore.setProperty("digest","user2:1231");
        propertiesBefore.setProperty("leaf.key","test_table_3");
        leafSegmentKeyGenerator.setProperties(propertiesBefore);
        Set<Comparable<?>> actualBefore = new HashSet<>();
        for (int i = 0; i < taskNumber; i++) {
            actualBefore.add(executor.submit(new Callable<Comparable<?>>() {
                @Override
                public Comparable<?> call() {
                    return leafSegmentKeyGenerator.generateKey();
                }
            }).get());
        }
        Properties propertiesAfter = new Properties();
        propertiesAfter.setProperty("serverList","127.0.0.1:2181");
        propertiesAfter.setProperty("initialValue","100001");
        propertiesAfter.setProperty("step","3");
        propertiesAfter.setProperty("digest","user2:98");
        propertiesAfter.setProperty("leaf.key","test_table_4");
        leafSegmentKeyGenerator.setProperties(propertiesAfter);
        Set<Comparable<?>> actualAfter = new HashSet<>();
        for (int i = 0; i < taskNumber; i++) {
            actualAfter.add(executor.submit(new Callable<Comparable<?>>() {
                @Override
                public Comparable<?> call() {
                    return leafSegmentKeyGenerator.generateKey();
                }
            }).get());
        }
        assertThat(actualAfter.size(), is(taskNumber));
    }

    @Test
    @SneakyThrows
    public void assertGenerateKeyWithDefaultStep() {
        int threadNumber = Runtime.getRuntime().availableProcessors() << 1;
        ExecutorService executor = Executors.newFixedThreadPool(threadNumber);
        int taskNumber = threadNumber << 2;
        Properties properties = new Properties();
        properties.setProperty("serverList","127.0.0.1:2181");
        properties.setProperty("initialValue","100001");
        properties.setProperty("digest","");
        properties.setProperty("leaf.key","test_table_5");
        leafSegmentKeyGenerator.setProperties(properties);
        Set<Comparable<?>> actual = new HashSet<>();
        for (int i = 0; i < taskNumber; i++) {
            actual.add(executor.submit(new Callable<Comparable<?>>() {
                @Override
                public Comparable<?> call() {
                    return leafSegmentKeyGenerator.generateKey();
                }
            }).get());
        }
        assertThat(actual.size(), is(taskNumber));
    }

    @Test
    @SneakyThrows
    public void assertGenerateKeyWithDefaultInitialValue() {
        int threadNumber = Runtime.getRuntime().availableProcessors() << 1;
        ExecutorService executor = Executors.newFixedThreadPool(threadNumber);
        int taskNumber = threadNumber << 2;
        Properties properties = new Properties();
        properties.setProperty("serverList","127.0.0.1:2181");
        properties.setProperty("step","3");
        properties.setProperty("digest","");
        properties.setProperty("leaf.key","test_table_6");
        leafSegmentKeyGenerator.setProperties(properties);
        Set<Comparable<?>> actual = new HashSet<>();
        for (int i = 0; i < taskNumber; i++) {
            actual.add(executor.submit(new Callable<Comparable<?>>() {
                @Override
                public Comparable<?> call() {
                    return leafSegmentKeyGenerator.generateKey();
                }
            }).get());
        }
        assertThat(actual.size(), is(taskNumber));
    }

    @Test(expected = IllegalArgumentException.class)
    public void assertSetStepFailureWhenNegative() {
        Properties properties = new Properties();
        properties.setProperty("serverList","127.0.0.1:2181");
        properties.setProperty("step", String.valueOf(-1L));
        properties.setProperty("initialValue","100001");
        properties.setProperty("digest","");
        properties.setProperty("leaf.key","test_table_7");
        leafSegmentKeyGenerator.setProperties(properties);
        leafSegmentKeyGenerator.generateKey();
    }

    @Test(expected = IllegalArgumentException.class)
    public void assertSetStepFailureWhenZero() {
        Properties properties = new Properties();
        properties.setProperty("serverList","127.0.0.1:2181");
        properties.setProperty("step", String.valueOf(0L));
        properties.setProperty("initialValue","100001");
        properties.setProperty("digest","");
        properties.setProperty("leaf.key","test_table_8");
        leafSegmentKeyGenerator.setProperties(properties);
        leafSegmentKeyGenerator.generateKey();
    }

    @Test(expected = IllegalArgumentException.class)
    public void assertSetStepFailureWhenTooMuch() {
        Properties properties = new Properties();
        properties.setProperty("serverList","127.0.0.1:2181");
        properties.setProperty("step", String.valueOf(Long.MAX_VALUE));
        properties.setProperty("initialValue","100001");
        properties.setProperty("digest","");
        properties.setProperty("leaf.key","test_table_9");
        leafSegmentKeyGenerator.setProperties(properties);
        leafSegmentKeyGenerator.generateKey();
    }

    @Test(expected = IllegalArgumentException.class)
    public void assertSetInitialValueFailureWhenNegative() {
        Properties properties = new Properties();
        properties.setProperty("serverList","127.0.0.1:2181");
        properties.setProperty("step","3");
        properties.setProperty("initialValue", String.valueOf(-1L));
        properties.setProperty("digest","");
        properties.setProperty("leaf.key","test_table_10");
        leafSegmentKeyGenerator.setProperties(properties);
        leafSegmentKeyGenerator.generateKey();
    }

    @Test(expected = IllegalArgumentException.class)
    public void assertSetInitialValueFailureWhenTooMuch() {
        Properties properties = new Properties();
        properties.setProperty("serverList","127.0.0.1:2181");
        properties.setProperty("step","3");
        properties.setProperty("initialValue", String.valueOf(Long.MAX_VALUE));
        properties.setProperty("digest","");
        properties.setProperty("leaf.key","test_table_11");
        leafSegmentKeyGenerator.setProperties(properties);
        leafSegmentKeyGenerator.generateKey();
    }

    @Test(expected = IllegalArgumentException.class)
    public void assertSetServerListFailureWhenNull() {
        Properties properties = new Properties();
        properties.setProperty("step","3");
        properties.setProperty("initialValue", "100001");
        properties.setProperty("digest","");
        properties.setProperty("leaf.key","test_table_12");
        leafSegmentKeyGenerator.setProperties(properties);
        leafSegmentKeyGenerator.generateKey();
    }

    @Test(expected = IllegalArgumentException.class)
    public void assertSetServerListFailureWhenArgumentEmpty() {
        Properties properties = new Properties();
        properties.setProperty("serverList","");
        properties.setProperty("step","3");
        properties.setProperty("initialValue", "100001");
        properties.setProperty("digest","");
        properties.setProperty("leaf.key","test_table_13");
        leafSegmentKeyGenerator.setProperties(properties);
        leafSegmentKeyGenerator.generateKey();
    }

    @Test(expected = IllegalArgumentException.class)
    public void assertSetLeafKeyFailureWhenArgumentIllegal() {
        Properties properties = new Properties();
        properties.setProperty("serverList","127.0.0.1:2181");
        properties.setProperty("step","3");
        properties.setProperty("initialValue", "100001");
        properties.setProperty("digest","");
        properties.setProperty("leaf.key","/test_table_14");
        leafSegmentKeyGenerator.setProperties(properties);
        leafSegmentKeyGenerator.generateKey();
    }

    @Test(expected = IllegalArgumentException.class)
    public void assertSetLeafKeyFailureWhenArgumentEmpty() {
        Properties properties = new Properties();
        properties.setProperty("serverList","127.0.0.1:2181");
        properties.setProperty("step","3");
        properties.setProperty("initialValue", "100001");
        properties.setProperty("digest","");
        properties.setProperty("leaf.key","");
        leafSegmentKeyGenerator.setProperties(properties);
        leafSegmentKeyGenerator.generateKey();
    }

    @Test(expected = IllegalArgumentException.class)
    public void assertSetLeafKeyFailureWhenNull() {
        Properties properties = new Properties();
        properties.setProperty("serverList","127.0.0.1:2181");
        properties.setProperty("step","3");
        properties.setProperty("initialValue", "100001");
        properties.setProperty("digest","");
        leafSegmentKeyGenerator.setProperties(properties);
        leafSegmentKeyGenerator.generateKey();
    }

    @AfterClass
    @SneakyThrows
    public static void closeServer(){
        server.close();
    }

}
