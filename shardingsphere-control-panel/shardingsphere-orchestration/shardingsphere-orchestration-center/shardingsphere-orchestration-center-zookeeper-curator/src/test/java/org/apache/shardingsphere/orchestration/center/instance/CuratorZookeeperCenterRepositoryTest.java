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

package org.apache.shardingsphere.orchestration.center.instance;

import org.apache.curator.framework.CuratorFramework;
import org.apache.shardingsphere.orchestration.center.config.CenterConfiguration;
import org.apache.shardingsphere.orchestration.center.exception.OrchestrationException;
import org.apache.shardingsphere.orchestration.center.listener.DataChangedEvent;
import org.apache.shardingsphere.orchestration.center.util.EmbedTestingServer;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public final class CuratorZookeeperCenterRepositoryTest {
    
    private static final CuratorZookeeperCenterRepository REPOSITORY = new CuratorZookeeperCenterRepository();
    
    private static String serverLists;
    
    @BeforeClass
    public static void init() {
        EmbedTestingServer.start();
        serverLists = EmbedTestingServer.getTestingServerConnectionString();
        CenterConfiguration configuration = new CenterConfiguration(REPOSITORY.getType(), new Properties());
        configuration.setServerLists(serverLists);
        REPOSITORY.init(configuration);
    }
    
    @Test
    public void assertPersist() {
        REPOSITORY.persist("/test", "value1");
        assertThat(REPOSITORY.get("/test"), is("value1"));
    }
    
    @Test
    public void assertUpdate() {
        REPOSITORY.persist("/test", "value2");
        assertThat(REPOSITORY.get("/test"), is("value2"));
    }
    
    @Test
    public void assertPersistEphemeral() {
        REPOSITORY.persistEphemeral("/test/ephemeral", "value3");
        assertThat(REPOSITORY.get("/test/ephemeral"), is("value3"));
    }
    
    @Test
    public void assertGetChildrenKeys() {
        REPOSITORY.persist("/test/children/keys/1", "value11");
        REPOSITORY.persist("/test/children/keys/2", "value12");
        REPOSITORY.persist("/test/children/keys/3", "value13");
        List<String> childrenKeys = REPOSITORY.getChildrenKeys("/test/children/keys");
        assertThat(childrenKeys.size(), is(3));
    }
    
    @Test
    public void assertWatchUpdatedChangedType() throws Exception {
        REPOSITORY.persist("/test/children_updated/1", "value1");
        AtomicReference<DataChangedEvent> dataChangedEventActual = new AtomicReference<>();
        REPOSITORY.watch("/test/children_updated", dataChangedEventActual::set);
        REPOSITORY.persist("/test/children_updated/1", "value2");
        Thread.sleep(50L);
        DataChangedEvent dataChangedEvent = dataChangedEventActual.get();
        assertNotNull(dataChangedEvent);
        assertThat(dataChangedEvent.getChangedType(), is(DataChangedEvent.ChangedType.UPDATED));
        assertThat(dataChangedEvent.getKey(), is("/test/children_updated/1"));
        assertThat(dataChangedEvent.getValue(), is("value2"));
        assertThat(REPOSITORY.get("/test/children_updated/1"), is("value2"));
    }
    
    @Test
    public void assertWatchDeletedChangedType() throws Exception {
        REPOSITORY.persist("/test/children_deleted/5", "value5");
        Field field = CuratorZookeeperCenterRepository.class.getDeclaredField("client");
        field.setAccessible(true);
        CuratorFramework client = (CuratorFramework) field.get(REPOSITORY);
        AtomicReference<DataChangedEvent> dataChangedEventActual = new AtomicReference<>();
        REPOSITORY.watch("/test/children_deleted/5", dataChangedEventActual::set);
        client.delete().forPath("/test/children_deleted/5");
        Thread.sleep(50L);
        DataChangedEvent dataChangedEvent = dataChangedEventActual.get();
        assertNotNull(dataChangedEvent);
        assertThat(dataChangedEvent.getChangedType(), is(DataChangedEvent.ChangedType.DELETED));
        assertThat(dataChangedEvent.getKey(), is("/test/children_deleted/5"));
        assertThat(dataChangedEvent.getValue(), is("value5"));
    }
    
    @Test
    public void assertWatchAddedChangedType() throws InterruptedException {
        REPOSITORY.persist("/test/children_added/4", "value4");
        AtomicReference<DataChangedEvent> actualDataChangedEvent = new AtomicReference<>();
        REPOSITORY.watch("/test/children_added", actualDataChangedEvent::set);
        Thread.sleep(50L);
        DataChangedEvent event = actualDataChangedEvent.get();
        assertNotNull(event);
        assertThat(event.getChangedType(), is(DataChangedEvent.ChangedType.ADDED));
        assertThat(event.getKey(), is("/test/children_added/4"));
        assertThat(event.getValue(), is("value4"));
    }
    
    @Test
    public void assertGetWithNonExistentKey() {
        assertNull(REPOSITORY.get("/test/nonExistentKey"));
    }
    
    @Test
    public void assertBuildCuratorClientWithCustomConfig() {
        final CuratorZookeeperCenterRepository customCenterRepository = new CuratorZookeeperCenterRepository();
        Properties props = new Properties();
        props.setProperty(ZookeeperPropertyKey.RETRY_INTERVAL_MILLISECONDS.getKey(), "1000");
        props.setProperty(ZookeeperPropertyKey.MAX_RETRIES.getKey(), "1");
        props.setProperty(ZookeeperPropertyKey.TIME_TO_LIVE_SECONDS.getKey(), "1000");
        props.setProperty(ZookeeperPropertyKey.OPERATION_TIMEOUT_MILLISECONDS.getKey(), "2000");
        CenterConfiguration configuration = new CenterConfiguration(customCenterRepository.getType(), new Properties());
        configuration.setServerLists(serverLists);
        customCenterRepository.setProps(props);
        customCenterRepository.init(configuration);
        assertThat(customCenterRepository.getProps().getProperty(ZookeeperPropertyKey.RETRY_INTERVAL_MILLISECONDS.getKey()), is("1000"));
        assertThat(customCenterRepository.getProps().getProperty(ZookeeperPropertyKey.MAX_RETRIES.getKey()), is("1"));
        assertThat(customCenterRepository.getProps().getProperty(ZookeeperPropertyKey.TIME_TO_LIVE_SECONDS.getKey()), is("1000"));
        assertThat(customCenterRepository.getProps().getProperty(ZookeeperPropertyKey.OPERATION_TIMEOUT_MILLISECONDS.getKey()), is("2000"));
        customCenterRepository.persist("/test/children/build/1", "value1");
        assertThat(customCenterRepository.get("/test/children/build/1"), is("value1"));
    }
    
    @Test
    public void assertBuildCuratorClientWithTimeToLiveSecondsEqualsZero() {
        final CuratorZookeeperCenterRepository customCenterRepository = new CuratorZookeeperCenterRepository();
        Properties props = new Properties();
        props.setProperty(ZookeeperPropertyKey.TIME_TO_LIVE_SECONDS.getKey(), "0");
        CenterConfiguration configuration = new CenterConfiguration(customCenterRepository.getType(), new Properties());
        configuration.setServerLists(serverLists);
        customCenterRepository.setProps(props);
        customCenterRepository.init(configuration);
        assertThat(customCenterRepository.getProps().getProperty(ZookeeperPropertyKey.TIME_TO_LIVE_SECONDS.getKey()), is("0"));
        customCenterRepository.persist("/test/children/build/2", "value1");
        assertThat(customCenterRepository.get("/test/children/build/2"), is("value1"));
    }
    
    @Test
    public void assertBuildCuratorClientWithOperationTimeoutMillisecondsEqualsZero() {
        final CuratorZookeeperCenterRepository customCenterRepository = new CuratorZookeeperCenterRepository();
        Properties props = new Properties();
        props.setProperty(ZookeeperPropertyKey.OPERATION_TIMEOUT_MILLISECONDS.getKey(), "0");
        CenterConfiguration configuration = new CenterConfiguration(customCenterRepository.getType(), new Properties());
        configuration.setServerLists(serverLists);
        customCenterRepository.setProps(props);
        customCenterRepository.init(configuration);
        assertThat(customCenterRepository.getProps().getProperty(ZookeeperPropertyKey.OPERATION_TIMEOUT_MILLISECONDS.getKey()), is("0"));
        customCenterRepository.persist("/test/children/build/3", "value1");
        assertThat(customCenterRepository.get("/test/children/build/3"), is("value1"));
    }
    
    @Test
    public void assertBuildCuratorClientWithDigest() {
        final CuratorZookeeperCenterRepository customCenterRepository = new CuratorZookeeperCenterRepository();
        Properties props = new Properties();
        props.setProperty(ZookeeperPropertyKey.DIGEST.getKey(), "any");
        CenterConfiguration configuration = new CenterConfiguration(customCenterRepository.getType(), new Properties());
        configuration.setServerLists(serverLists);
        customCenterRepository.setProps(props);
        customCenterRepository.init(configuration);
        assertThat(customCenterRepository.getProps().getProperty(ZookeeperPropertyKey.DIGEST.getKey()), is("any"));
        customCenterRepository.persist("/test/children/build/4", "value1");
        assertThat(customCenterRepository.get("/test/children/build/4"), is("value1"));
    }
    
    @Test
    public void assertDelete() {
        REPOSITORY.persist("/test/children/1", "value1");
        REPOSITORY.persist("/test/children/2", "value2");
        assertThat(REPOSITORY.get("/test/children/1"), is("value1"));
        assertThat(REPOSITORY.get("/test/children/2"), is("value2"));
        REPOSITORY.delete("/test/children");
        assertNull(REPOSITORY.get("/test/children/1"));
        assertNull(REPOSITORY.get("/test/children/2"));
    }
    
    @Test
    public void assertZKCloseAndException() {
        final CuratorZookeeperCenterRepository customCenterRepository = new CuratorZookeeperCenterRepository();
        CenterConfiguration configuration = new CenterConfiguration(customCenterRepository.getType(), new Properties());
        Properties props = new Properties();
        props.setProperty(ZookeeperPropertyKey.DIGEST.getKey(), "digest");
        configuration.setServerLists(serverLists);
        customCenterRepository.setProps(props);
        customCenterRepository.init(configuration);
        customCenterRepository.close();
        
        try {
            customCenterRepository.get("/test/children/1");
            fail("must be failed after close.");
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            Assert.assertTrue(ex instanceof OrchestrationException);
        }
        
        try {
            customCenterRepository.persist("/test/children/01", "value1");
            fail("must be failed after close.");
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            Assert.assertTrue(ex instanceof OrchestrationException);
        }
        
        try {
            customCenterRepository.delete("/test/children/02");
            fail("must be failed after close.");
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            Assert.assertTrue(ex instanceof OrchestrationException);
        }
        
        try {
            customCenterRepository.persistEphemeral("/test/children/03", "value1");
            fail("must be failed after close.");
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            Assert.assertTrue(ex instanceof OrchestrationException);
        }
        
        try {
            customCenterRepository.getChildrenKeys("/test/children");
            fail("must be failed after close.");
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            Assert.assertTrue(ex instanceof OrchestrationException);
        }
    }
}
