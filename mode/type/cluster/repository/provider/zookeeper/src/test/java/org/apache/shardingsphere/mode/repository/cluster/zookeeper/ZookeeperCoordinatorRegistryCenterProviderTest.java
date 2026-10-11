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

package org.apache.shardingsphere.mode.repository.cluster.zookeeper;

import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.apache.shardingsphere.elasticjob.reg.zookeeper.ZookeeperConfiguration;
import org.apache.shardingsphere.elasticjob.reg.zookeeper.ZookeeperRegistryCenter;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepositoryConfiguration;
import org.apache.shardingsphere.schedule.spi.CoordinatorRegistryCenterProvider;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;

class ZookeeperCoordinatorRegistryCenterProviderTest {
    
    @Test
    void assertIsSupportedWithoutCreatingRegistryCenter() {
        try (MockedConstruction<ZookeeperRegistryCenter> construction = mockConstruction(ZookeeperRegistryCenter.class)) {
            assertTrue(TypedSPILoader.findService(CoordinatorRegistryCenterProvider.class, "ZooKeeper").isPresent());
            assertTrue(TypedSPILoader.findService(CoordinatorRegistryCenterProvider.class, "zookeeper").isPresent());
            assertTrue(construction.constructed().isEmpty());
        }
    }
    
    @Test
    void assertCreate() {
        Properties props = new Properties();
        props.setProperty("retryIntervalMilliseconds", "200");
        props.setProperty("maxRetries", "4");
        props.setProperty("timeToLiveSeconds", "5");
        props.setProperty("operationTimeoutMilliseconds", "800");
        props.setProperty("digest", "digest");
        ClusterPersistRepositoryConfiguration repositoryConfig = new ClusterPersistRepositoryConfiguration("ZooKeeper", "namespace", "127.0.0.1:2181", props);
        AtomicReference<ZookeeperConfiguration> configuration = new AtomicReference<>();
        try (
                MockedConstruction<ZookeeperRegistryCenter> construction = mockConstruction(ZookeeperRegistryCenter.class,
                        (mock, context) -> configuration.set((ZookeeperConfiguration) context.arguments().get(0)))) {
            CoordinatorRegistryCenter actual = TypedSPILoader.getService(CoordinatorRegistryCenterProvider.class, repositoryConfig.getType()).create(repositoryConfig, "/jobs");
            assertThat(actual, sameInstance(construction.constructed().get(0)));
            verify(actual).init();
            assertThat(configuration.get().getServerLists(), is("127.0.0.1:2181"));
            assertThat(configuration.get().getNamespace(), is("namespace/jobs"));
            assertThat(configuration.get().getBaseSleepTimeMilliseconds(), is(200));
            assertThat(configuration.get().getMaxRetries(), is(4));
            assertThat(configuration.get().getMaxSleepTimeMilliseconds(), is(800));
            assertThat(configuration.get().getSessionTimeoutMilliseconds(), is(5000));
            assertThat(configuration.get().getConnectionTimeoutMilliseconds(), is(800));
            assertThat(configuration.get().getDigest(), is("digest"));
            actual.close();
            verify(actual).close();
        }
    }
    
    @Test
    void assertCreateWithZeroTimeoutsAndNullNamespaceRelativePath() {
        Properties props = new Properties();
        props.setProperty("timeToLiveSeconds", "0");
        props.setProperty("operationTimeoutMilliseconds", "0");
        ClusterPersistRepositoryConfiguration repositoryConfig = new ClusterPersistRepositoryConfiguration("ZooKeeper", "namespace", "127.0.0.1:2181", props);
        AtomicReference<ZookeeperConfiguration> configuration = new AtomicReference<>();
        try (
                MockedConstruction<ZookeeperRegistryCenter> ignored = mockConstruction(ZookeeperRegistryCenter.class,
                        (mock, context) -> configuration.set((ZookeeperConfiguration) context.arguments().get(0)))) {
            TypedSPILoader.getService(CoordinatorRegistryCenterProvider.class, repositoryConfig.getType()).create(repositoryConfig, null);
            assertThat(configuration.get().getNamespace(), is("namespace"));
            assertThat(configuration.get().getBaseSleepTimeMilliseconds(), is(500));
            assertThat(configuration.get().getMaxRetries(), is(3));
            assertThat(configuration.get().getMaxSleepTimeMilliseconds(), is(1500));
            assertThat(configuration.get().getSessionTimeoutMilliseconds(), is(0));
            assertThat(configuration.get().getConnectionTimeoutMilliseconds(), is(0));
            assertThat(configuration.get().getDigest(), is(""));
        }
    }
    
    @Test
    void assertCloseRegistryCenterWhenInitializationFailed() {
        ClusterPersistRepositoryConfiguration repositoryConfig = new ClusterPersistRepositoryConfiguration("ZooKeeper", "namespace", "127.0.0.1:2181", new Properties());
        RuntimeException expected = new RuntimeException("expected");
        try (
                MockedConstruction<ZookeeperRegistryCenter> construction = mockConstruction(ZookeeperRegistryCenter.class,
                        (mock, context) -> doThrow(expected).when(mock).init())) {
            assertThat(assertThrows(RuntimeException.class,
                    () -> TypedSPILoader.getService(CoordinatorRegistryCenterProvider.class, repositoryConfig.getType()).create(repositoryConfig, null)), sameInstance(expected));
            verify(construction.constructed().get(0)).close();
        }
    }
    
    @Test
    void assertPreserveInitializationFailureWhenCloseFailed() {
        ClusterPersistRepositoryConfiguration repositoryConfig = new ClusterPersistRepositoryConfiguration("ZooKeeper", "namespace", "127.0.0.1:2181", new Properties());
        RuntimeException expected = new RuntimeException("expected");
        RuntimeException closeException = new RuntimeException("close failed");
        try (MockedConstruction<ZookeeperRegistryCenter> ignored = mockConstruction(ZookeeperRegistryCenter.class, (mock, context) -> {
            doThrow(expected).when(mock).init();
            doThrow(closeException).when(mock).close();
        })) {
            RuntimeException actual = assertThrows(RuntimeException.class,
                    () -> TypedSPILoader.getService(CoordinatorRegistryCenterProvider.class, repositoryConfig.getType()).create(repositoryConfig, null));
            assertThat(actual, sameInstance(expected));
            assertThat(actual.getSuppressed(), arrayContaining(closeException));
        }
    }
}
