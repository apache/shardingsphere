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

package org.apache.shardingsphere.integration.test;

import lombok.SneakyThrows;
import org.apache.shardingsphere.integration.util.EmbedTestingServer;
import org.apache.shardingsphere.orchestration.internal.keygen.LeafSnowflakeKeyGenerator;
import org.apache.shardingsphere.orchestration.internal.keygen.TimeService;
import org.apache.shardingsphere.orchestration.internal.keygen.fixture.FixedTimeService;
import org.apache.shardingsphere.orchestration.internal.registry.RegistryCenterServiceLoader;
import org.apache.shardingsphere.orchestration.reg.api.RegistryCenter;
import org.apache.shardingsphere.orchestration.reg.api.RegistryCenterConfiguration;
import org.junit.BeforeClass;
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

import static org.apache.shardingsphere.orchestration.util.FieldUtil.setStaticFinalField;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@FixMethodOrder(value = MethodSorters.NAME_ASCENDING)
public final class LeafSnowflakeKeyGeneratorIT {

    private LeafSnowflakeKeyGenerator leafSnowflakeKeyGenerator = new LeafSnowflakeKeyGenerator();

    @BeforeClass
    public static void init() {
        EmbedTestingServer.start();
    }

    @Test
    @SneakyThrows
    public void assertGenerateKeyWithSingleThread() {
        Properties properties = new Properties();
        properties.setProperty("serverList", "127.0.0.1:3181");
        properties.setProperty("serviceId", "testService1");
        properties.setProperty("maxTimeDifference", "5000");
        properties.setProperty("registryCenterType", "zookeeper");
        leafSnowflakeKeyGenerator.setProperties(properties);
        setStaticFinalField(leafSnowflakeKeyGenerator,"timeService",new FixedTimeService(1));
        List<Comparable<?>> expected = Arrays.<Comparable<?>>asList(4198401L, 4198402L, 8392704L, 8392705L, 12587009L, 12587010L, 16781312L, 16781313L, 20975617L, 20975618L);
        List<Comparable<?>> actual = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            actual.add(leafSnowflakeKeyGenerator.generateKey());
        }
        assertThat(actual, is(expected));
    }

    @Test
    @SneakyThrows
    public void assertGenerateKeyWithFixedWorkId() {
        Properties properties = new Properties();
        properties.setProperty("serverList", "127.0.0.1:3181");
        properties.setProperty("serviceId", "testService1");
        properties.setProperty("maxTimeDifference", "5000");
        properties.setProperty("registryCenterType", "zookeeper");
        leafSnowflakeKeyGenerator.setProperties(properties);
        setStaticFinalField(leafSnowflakeKeyGenerator,"timeService",new FixedTimeService(1));
        List<Comparable<?>> expected = Arrays.<Comparable<?>>asList(4198401L);
        List<Comparable<?>> actual = new ArrayList<>();
        actual.add(leafSnowflakeKeyGenerator.generateKey());
        assertThat(actual, is(expected));
    }

    @Test
    @SneakyThrows
    public void assertGenerateKeyWithMultipleThreads() {
        int threadNumber = Runtime.getRuntime().availableProcessors() << 1;
        ExecutorService executor = Executors.newFixedThreadPool(threadNumber);
        Properties properties = new Properties();
        properties.setProperty("serverList", "127.0.0.1:3181");
        properties.setProperty("serviceId", "testService1");
        properties.setProperty("maxTimeDifference", "5000");
        properties.setProperty("registryCenterType", "zookeeper");
        leafSnowflakeKeyGenerator.setProperties(properties);
        setStaticFinalField(leafSnowflakeKeyGenerator,"timeService",new FixedTimeService(1));
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
    @SneakyThrows
    public void assertGenerateKeyWithDigest() {
        int threadNumber = Runtime.getRuntime().availableProcessors() << 1;
        ExecutorService executor = Executors.newFixedThreadPool(threadNumber);
        Properties properties = new Properties();
        properties.setProperty("serverList", "127.0.0.1:3181");
        properties.setProperty("serviceId", "testService1");
        properties.setProperty("maxTimeDifference", "5000");
        properties.setProperty("registryCenterType", "zookeeper");
        leafSnowflakeKeyGenerator.setProperties(properties);
        setStaticFinalField(leafSnowflakeKeyGenerator,"timeService",new FixedTimeService(1));
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
    @SneakyThrows
    public void assertGenerateKeyWithDefaultMaxTimeDifference() {
        int threadNumber = Runtime.getRuntime().availableProcessors() << 1;
        ExecutorService executor = Executors.newFixedThreadPool(threadNumber);
        Properties properties = new Properties();
        properties.setProperty("serverList", "127.0.0.1:3181");
        properties.setProperty("serviceId", "testService1");
        properties.setProperty("registryCenterType", "zookeeper");
        leafSnowflakeKeyGenerator.setProperties(properties);
        setStaticFinalField(leafSnowflakeKeyGenerator,"timeService",new FixedTimeService(1));
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
    @SneakyThrows
    public void generateKeySuccessWithTimeRollback() {
        Properties properties = new Properties();
        properties.setProperty("serverList", "127.0.0.1:3181");
        properties.setProperty("serviceId", "specialService");
        properties.setProperty("maxTimeDifference", "5000");
        properties.setProperty("registryCenterType", "zookeeper");
        leafSnowflakeKeyGenerator.setProperties(properties);
        RegistryCenterConfiguration leafConfiguration = new RegistryCenterConfiguration("zookeeper", properties);
        leafConfiguration.setNamespace("leaf_snowflake");
        leafConfiguration.setServerLists("127.0.0.1:3181");
        RegistryCenter registryCenter = new RegistryCenterServiceLoader().load(leafConfiguration);
        TimeService timeService = new FixedTimeService(1);
        registryCenter.persist("/leaf_snowflake/specialService/time",String.valueOf(timeService.getCurrentMillis()+3000));
        setStaticFinalField(leafSnowflakeKeyGenerator,"timeService",timeService);
        leafSnowflakeKeyGenerator.generateKey();
        registryCenter.persist("/leaf_snowflake/specialService/time",String.valueOf(timeService.getCurrentMillis()));
    }

    @Test(expected = IllegalStateException.class)
    @SneakyThrows
    public void generateKeyFailureWithTimeRollback() {
        Properties properties = new Properties();
        properties.setProperty("serverList", "127.0.0.1:3181");
        properties.setProperty("serviceId", "specialService");
        properties.setProperty("maxTimeDifference", "5000");
        properties.setProperty("registryCenterType", "zookeeper");
        leafSnowflakeKeyGenerator.setProperties(properties);
        RegistryCenterConfiguration leafConfiguration = new RegistryCenterConfiguration("zookeeper", properties);
        leafConfiguration.setNamespace("leaf_snowflake");
        leafConfiguration.setServerLists("127.0.0.1:3181");
        RegistryCenter registryCenter = new RegistryCenterServiceLoader().load(leafConfiguration);
        TimeService timeService = new FixedTimeService(1);
        registryCenter.persist("/leaf_snowflake/specialService/time",String.valueOf(timeService.getCurrentMillis()+15000));
        setStaticFinalField(leafSnowflakeKeyGenerator,"timeService",timeService);
        leafSnowflakeKeyGenerator.generateKey();
        registryCenter.persist("/leaf_snowflake/specialService/time",String.valueOf(timeService.getCurrentMillis()));
    }

}
