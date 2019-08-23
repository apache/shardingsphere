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

package org.apache.shardingsphere.orchestration.zookeeper.curator.integration.test;

import org.apache.shardingsphere.orchestration.internal.keygen.LeafSegmentKeyGenerator;
import org.apache.shardingsphere.orchestration.zookeeper.curator.integration.util.EmbedTestingServer;
import org.junit.BeforeClass;
import org.junit.Test;

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

/**
 * Leaf segment key generator integration test.
 *
 * @author wangguangyuan
 */
public final class LeafSegmentKeyGeneratorIT {
    
    private final LeafSegmentKeyGenerator leafSegmentKeyGenerator = new LeafSegmentKeyGenerator();
    
    @BeforeClass
    public static void init() {
        EmbedTestingServer.start();
    }
    
    @Test
    public void assertGenerateKeyWithSingleThread() {
        Properties properties = new Properties();
        properties.setProperty("serverList", "127.0.0.1:3181");
        properties.setProperty("initialValue", "100001");
        properties.setProperty("step", "3");
        properties.setProperty("digest", "");
        properties.setProperty("leafKey", "test_table_1");
        properties.setProperty("registryCenterType", "zookeeper");
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
        int threadNumber = Runtime.getRuntime().availableProcessors() << 1;
        ExecutorService executor = Executors.newFixedThreadPool(threadNumber);
        Properties properties = new Properties();
        properties.setProperty("serverList", "127.0.0.1:3181");
        properties.setProperty("initialValue", "100001");
        properties.setProperty("step", "3");
        properties.setProperty("digest", "");
        properties.setProperty("leafKey", "test_table_2");
        properties.setProperty("registryCenterType", "zookeeper");
        leafSegmentKeyGenerator.setProperties(properties);
        Set<Comparable<?>> actual = new HashSet<>();
        int taskNumber = threadNumber << 2;
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
        int threadNumber = Runtime.getRuntime().availableProcessors() << 1;
        ExecutorService executor = Executors.newFixedThreadPool(threadNumber);
        Properties properties = new Properties();
        properties.setProperty("serverList", "127.0.0.1:3181");
        properties.setProperty("initialValue", "100001");
        properties.setProperty("step", "3");
        properties.setProperty("digest", "");
        properties.setProperty("leafKey", "test_table_3");
        properties.setProperty("registryCenterType", "zookeeper");
        leafSegmentKeyGenerator.setProperties(properties);
        Set<Comparable<?>> actual = new HashSet<>();
        int taskNumber = threadNumber << 2;
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
        int threadNumber = Runtime.getRuntime().availableProcessors() << 1;
        ExecutorService executor = Executors.newFixedThreadPool(threadNumber);
        Properties properties = new Properties();
        properties.setProperty("serverList", "127.0.0.1:3181");
        properties.setProperty("initialValue", "100001");
        properties.setProperty("digest", "");
        properties.setProperty("leafKey", "test_table_6");
        properties.setProperty("registryCenterType", "zookeeper");
        leafSegmentKeyGenerator.setProperties(properties);
        Set<Comparable<?>> actual = new HashSet<>();
        int taskNumber = threadNumber << 2;
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
        int threadNumber = Runtime.getRuntime().availableProcessors() << 1;
        ExecutorService executor = Executors.newFixedThreadPool(threadNumber);
        Properties properties = new Properties();
        properties.setProperty("serverList", "127.0.0.1:3181");
        properties.setProperty("step", "3");
        properties.setProperty("digest", "");
        properties.setProperty("leafKey", "test_table_7");
        properties.setProperty("registryCenterType", "zookeeper");
        leafSegmentKeyGenerator.setProperties(properties);
        int taskNumber = threadNumber << 2;
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
}
