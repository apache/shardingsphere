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

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

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
import static org.junit.Assert.assertThat;

@FixMethodOrder(value = MethodSorters.NAME_ASCENDING)
public final class LeafSegmentKeyGeneratorTest {
    
    private final LeafSegmentKeyGenerator leafSegmentKeyGenerator = new LeafSegmentKeyGenerator();
    
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
    public void assertGenerateKeyWithSingleThread() {
        Properties properties = new Properties();
        properties.setProperty("server.list", "127.0.0.1:2181");
        properties.setProperty("leaf.segment.id.initial.value", "100001");
        properties.setProperty("leaf.segment.step", "5");
        properties.setProperty("leaf.key", "test_table_1");
        properties.setProperty("registry.center.type", "ThirdTestRegistryCenter");
        leafSegmentKeyGenerator.setProperties(properties);
        List<Comparable<?>> expected = Arrays.<Comparable<?>>asList(100001L, 100002L, 100003L, 100004L, 100005L, 100006L, 100007L, 100008L, 100009L, 100010L);
        List<Comparable<?>> actual = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            actual.add(leafSegmentKeyGenerator.generateKey());
        }
        assertThat(actual, is(expected));
    }
    
    @Test
    public void assertGenerateKeyWithFirstSpecialStep() {
        Properties properties = new Properties();
        properties.setProperty("server.list", "127.0.0.1:2181");
        properties.setProperty("leaf.segment.id.initial.value", "100001");
        properties.setProperty("leaf.segment.step", "3");
        properties.setProperty("leaf.key", "test_table_6");
        properties.setProperty("registry.center.type", "ThirdTestRegistryCenter");
        leafSegmentKeyGenerator.setProperties(properties);
        List<Comparable<?>> expected = Arrays.<Comparable<?>>asList(100001L, 100002L, 100003L, 100004L, 100005L, 100006L, 100007L, 100008L, 100009L, 100010L);
        List<Comparable<?>> actual = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            actual.add(leafSegmentKeyGenerator.generateKey());
        }
        assertThat(actual, is(expected));
    }
    
    @Test
    public void assertGenerateKeyWithSecondSpecialStep() {
        Properties properties = new Properties();
        properties.setProperty("server.list", "127.0.0.1:2181");
        properties.setProperty("leaf.segment.id.initial.value", "100001");
        properties.setProperty("leaf.segment.step", "7");
        properties.setProperty("leaf.key", "test_table_7");
        properties.setProperty("registry.center.type", "ThirdTestRegistryCenter");
        leafSegmentKeyGenerator.setProperties(properties);
        List<Comparable<?>> expected = Arrays.<Comparable<?>>asList(100001L, 100002L, 100003L, 100004L, 100005L, 100006L, 100007L, 100008L, 100009L, 100010L);
        List<Comparable<?>> actual = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            actual.add(leafSegmentKeyGenerator.generateKey());
        }
        assertThat(actual, is(expected));
    }
    
    @Test
    public void assertGenerateKeyWithMultipleThreads() throws Exception {
        int threadNumber = 2;
        ExecutorService executor = Executors.newFixedThreadPool(threadNumber);
        Properties properties = new Properties();
        properties.setProperty("server.list", "127.0.0.1:2181");
        properties.setProperty("leaf.segment.id.initial.value", "100001");
        properties.setProperty("leaf.segment.step", "3");
        properties.setProperty("leaf.key", "test_table_2");
        properties.setProperty("registry.center.type", "ThirdTestRegistryCenter");
        leafSegmentKeyGenerator.setProperties(properties);
        Set<Comparable<?>> actual = new HashSet<>();
        int taskNumber = threadNumber * 2;
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
    public void assertGenerateKeyWithDigest() throws Exception {
        int threadNumber = 2;
        ExecutorService executor = Executors.newFixedThreadPool(threadNumber);
        Properties properties = new Properties();
        properties.setProperty("server.list", "127.0.0.1:2181");
        properties.setProperty("leaf.segment.id.initial.value", "100001");
        properties.setProperty("leaf.segment.step", "3");
        properties.setProperty("registry.center.digest", "name:123456");
        properties.setProperty("leaf.key", "test_table_3");
        properties.setProperty("registry.center.type", "ThirdTestRegistryCenter");
        leafSegmentKeyGenerator.setProperties(properties);
        Set<Comparable<?>> actual = new HashSet<>();
        int taskNumber = threadNumber * 2;
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
    public void assertGenerateKeyWithDefaultStep() throws Exception {
        int threadNumber = 2;
        ExecutorService executor = Executors.newFixedThreadPool(threadNumber);
        Properties properties = new Properties();
        properties.setProperty("server.list", "127.0.0.1:2181");
        properties.setProperty("leaf.segment.id.initial.value", "100001");
        properties.setProperty("leaf.key", "test_table_4");
        properties.setProperty("registry.center.type", "ThirdTestRegistryCenter");
        leafSegmentKeyGenerator.setProperties(properties);
        Set<Comparable<?>> actual = new HashSet<>();
        int taskNumber = threadNumber * 2;
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
    public void assertGenerateKeyWithDefaultInitialValue() throws Exception {
        int threadNumber = 2;
        ExecutorService executor = Executors.newFixedThreadPool(threadNumber);
        Properties properties = new Properties();
        properties.setProperty("server.list", "127.0.0.1:2181");
        properties.setProperty("leaf.segment.step", "3");
        properties.setProperty("leaf.key", "test_table_5");
        properties.setProperty("registry.center.type", "ThirdTestRegistryCenter");
        leafSegmentKeyGenerator.setProperties(properties);
        int taskNumber = threadNumber * 2;
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
        properties.setProperty("server.list", "127.0.0.1:2181");
        properties.setProperty("leaf.segment.step", String.valueOf(-1L));
        properties.setProperty("leaf.segment.id.initial.value", "100001");
        properties.setProperty("leaf.key", "test_table_9");
        properties.setProperty("registry.center.type", "ThirdTestRegistryCenter");
        leafSegmentKeyGenerator.setProperties(properties);
        leafSegmentKeyGenerator.generateKey();
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertSetStepFailureWhenZero() {
        Properties properties = new Properties();
        properties.setProperty("server.list", "127.0.0.1:2181");
        properties.setProperty("leaf.segment.step", String.valueOf(0L));
        properties.setProperty("leaf.segment.id.initial.value", "100001");
        properties.setProperty("leaf.key", "test_table_10");
        properties.setProperty("registry.center.type", "ThirdTestRegistryCenter");
        leafSegmentKeyGenerator.setProperties(properties);
        leafSegmentKeyGenerator.generateKey();
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertSetStepFailureWhenTooMuch() {
        Properties properties = new Properties();
        properties.setProperty("server.list", "127.0.0.1:2181");
        properties.setProperty("leaf.segment.step", String.valueOf(Long.MAX_VALUE));
        properties.setProperty("leaf.segment.id.initial.value", "100001");
        properties.setProperty("leaf.key", "test_table_11");
        properties.setProperty("registry.center.type", "ThirdTestRegistryCenter");
        leafSegmentKeyGenerator.setProperties(properties);
        leafSegmentKeyGenerator.generateKey();
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertSetInitialValueFailureWhenNegative() {
        Properties properties = new Properties();
        properties.setProperty("server.list", "127.0.0.1:2181");
        properties.setProperty("leaf.segment.step", "3");
        properties.setProperty("leaf.segment.id.initial.value", String.valueOf(-1L));
        properties.setProperty("leaf.key", "test_table_12");
        properties.setProperty("registry.center.type", "ThirdTestRegistryCenter");
        leafSegmentKeyGenerator.setProperties(properties);
        leafSegmentKeyGenerator.generateKey();
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertSetInitialValueFailureWhenTooMuch() {
        Properties properties = new Properties();
        properties.setProperty("server.list", "127.0.0.1:2181");
        properties.setProperty("leaf.segment.step", "3");
        properties.setProperty("leaf.segment.id.initial.value", String.valueOf(Long.MAX_VALUE));
        properties.setProperty("leaf.key", "test_table_13");
        properties.setProperty("registry.center.type", "ThirdTestRegistryCenter");
        leafSegmentKeyGenerator.setProperties(properties);
        leafSegmentKeyGenerator.generateKey();
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertSetServerListFailureWhenNull() {
        Properties properties = new Properties();
        properties.setProperty("leaf.segment.step", "3");
        properties.setProperty("leaf.segment.id.initial.value", "100001");
        properties.setProperty("leaf.key", "test_table_14");
        properties.setProperty("registry.center.type", "ThirdTestRegistryCenter");
        leafSegmentKeyGenerator.setProperties(properties);
        leafSegmentKeyGenerator.generateKey();
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertSetServerListFailureWhenArgumentEmpty() {
        Properties properties = new Properties();
        properties.setProperty("server.list", "");
        properties.setProperty("leaf.segment.step", "3");
        properties.setProperty("leaf.segment.id.initial.value", "100001");
        properties.setProperty("leaf.key", "test_table_15");
        properties.setProperty("registry.center.type", "ThirdTestRegistryCenter");
        leafSegmentKeyGenerator.setProperties(properties);
        leafSegmentKeyGenerator.generateKey();
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertSetLeafKeyFailureWhenArgumentIllegal() {
        Properties properties = new Properties();
        properties.setProperty("server.list", "127.0.0.1:2181");
        properties.setProperty("leaf.segment.step", "3");
        properties.setProperty("leaf.segment.id.initial.value", "100001");
        properties.setProperty("leaf.key", "/test_table_16");
        properties.setProperty("registry.center.type", "ThirdTestRegistryCenter");
        leafSegmentKeyGenerator.setProperties(properties);
        leafSegmentKeyGenerator.generateKey();
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertSetLeafKeyFailureWhenArgumentEmpty() {
        Properties properties = new Properties();
        properties.setProperty("server.list", "127.0.0.1:2181");
        properties.setProperty("leaf.segment.step", "3");
        properties.setProperty("leaf.segment.id.initial.value", "100001");
        properties.setProperty("leaf.key", "");
        properties.setProperty("registry.center.type", "ThirdTestRegistryCenter");
        leafSegmentKeyGenerator.setProperties(properties);
        leafSegmentKeyGenerator.generateKey();
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertSetLeafKeyFailureWhenNull() {
        Properties properties = new Properties();
        properties.setProperty("server.list", "127.0.0.1:2181");
        properties.setProperty("leaf.segment.step", "3");
        properties.setProperty("leaf.segment.id.initial.value", "100001");
        properties.setProperty("registry.center.type", "ThirdTestRegistryCenter");
        leafSegmentKeyGenerator.setProperties(properties);
        leafSegmentKeyGenerator.generateKey();
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertSetRegistryCenterTypeFailureWhenWrongType() {
        Properties properties = new Properties();
        properties.setProperty("server.list", "127.0.0.1:2181");
        properties.setProperty("leaf.segment.step", "3");
        properties.setProperty("leaf.segment.id.initial.value", "100001");
        properties.setProperty("leaf.key", "/test_table_17");
        properties.setProperty("registry.center.type", "ThirdTestRegistryCenter");
        leafSegmentKeyGenerator.setProperties(properties);
        leafSegmentKeyGenerator.generateKey();
    }
}
