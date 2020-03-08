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

import com.google.common.util.concurrent.SettableFuture;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.shardingsphere.orchestration.center.configuration.InstanceConfiguration;

import org.apache.shardingsphere.orchestration.center.listener.DataChangedEvent;
import org.apache.shardingsphere.orchestration.center.util.EmbedTestingServer;
import org.junit.BeforeClass;
import org.junit.Test;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public final class CuratorZookeeperInstanceTest {
    
    private static CuratorZookeeperInstance curatorZookeeperInstance = new CuratorZookeeperInstance();
    
    private static CuratorFramework client;
    
    private static final String SERVER_LISTS = "127.0.0.1:3181";
    
    @BeforeClass
    public static void init() {
        EmbedTestingServer.start();
        InstanceConfiguration configuration = new InstanceConfiguration(curatorZookeeperInstance.getType(), new Properties());
        configuration.setServerLists(SERVER_LISTS);
        curatorZookeeperInstance.init(configuration);
        client = CuratorFrameworkFactory.newClient(SERVER_LISTS, new ExponentialBackoffRetry(1000, 3));
        client.start();
    }
    
    @Test
    public void assertPersist() {
        curatorZookeeperInstance.persist("/test", "value1");
        assertThat(curatorZookeeperInstance.get("/test"), is("value1"));
    }
    
    @Test
    public void assertUpdate() {
        curatorZookeeperInstance.persist("/test", "value2");
        assertThat(curatorZookeeperInstance.get("/test"), is("value2"));
    }
    
    @Test
    public void assertPersistEphemeral() {
        curatorZookeeperInstance.persistEphemeral("/test/ephemeral", "value3");
        assertThat(curatorZookeeperInstance.get("/test/ephemeral"), is("value3"));
    }
    
    @Test
    public void assertGetChildrenKeys() {
        curatorZookeeperInstance.persist("/test/children/1", "value11");
        curatorZookeeperInstance.persist("/test/children/2", "value12");
        curatorZookeeperInstance.persist("/test/children/3", "value13");
        List<String> childrenKeys = curatorZookeeperInstance.getChildrenKeys("/test/children");
        assertThat(childrenKeys.size(), is(3));
    }
    
    @Test
    public void assertWatchUpdatedChangedType() throws Exception {
        curatorZookeeperInstance.persist("/test/children/1", "value1");
        final SettableFuture<DataChangedEvent> future = SettableFuture.create();
        curatorZookeeperInstance.watch("/test/children", future::set);
        curatorZookeeperInstance.persist("/test/children/1", "value2");
        DataChangedEvent dataChangedEvent = future.get(5, TimeUnit.SECONDS);
        assertNotNull(dataChangedEvent);
        assertThat(dataChangedEvent.getChangedType(), is(DataChangedEvent.ChangedType.UPDATED));
        assertThat(dataChangedEvent.getKey(), is("/test/children/1"));
        assertThat(dataChangedEvent.getValue(), is("value2"));
        assertThat(curatorZookeeperInstance.get("/test/children/1"), is("value2"));
    }
    
    @Test
    public void assertWatchDeletedChangedType() throws Exception {
        curatorZookeeperInstance.persist("/test/children/5", "value5");
        SettableFuture<DataChangedEvent> future = SettableFuture.create();
        curatorZookeeperInstance.watch("/test/children/5", future::set);
        client.delete().forPath("/test/children/5");
        DataChangedEvent dataChangedEvent = future.get(5, TimeUnit.SECONDS);
        assertNotNull(dataChangedEvent);
        assertThat(dataChangedEvent.getChangedType(), is(DataChangedEvent.ChangedType.DELETED));
        assertThat(dataChangedEvent.getKey(), is("/test/children/5"));
        assertThat(dataChangedEvent.getValue(), is("value5"));
    }
    
    @Test
    public void assertWatchAddedChangedType() throws InterruptedException {
        curatorZookeeperInstance.persist("/test/children/4", "value4");
        AtomicReference<DataChangedEvent> actualDataChangedEvent = new AtomicReference<>();
        curatorZookeeperInstance.watch("/test/children", actualDataChangedEvent::set);
        Thread.sleep(2000L);
        assertNull(actualDataChangedEvent.get());
    }
    
    @Test
    public void assertGetWithNonExistentKey() {
        assertNull(curatorZookeeperInstance.get("/test/nonExistentKey"));
    }
    
    @Test
    public void assertBuildCuratorClientWithCustomConfig() {
        final CuratorZookeeperInstance customCuratorZookeeperInstance = new CuratorZookeeperInstance();
        Properties properties = new Properties();
        properties.setProperty(ZookeeperPropertiesEnum.RETRY_INTERVAL_MILLISECONDS.getKey(), "1000");
        properties.setProperty(ZookeeperPropertiesEnum.MAX_RETRIES.getKey(), "1");
        properties.setProperty(ZookeeperPropertiesEnum.TIME_TO_LIVE_SECONDS.getKey(), "100");
        properties.setProperty(ZookeeperPropertiesEnum.OPERATION_TIMEOUT_MILLISECONDS.getKey(), "1000");
        EmbedTestingServer.start();
        InstanceConfiguration configuration = new InstanceConfiguration(customCuratorZookeeperInstance.getType(), new Properties());
        configuration.setServerLists(SERVER_LISTS);
        customCuratorZookeeperInstance.setProperties(properties);
        customCuratorZookeeperInstance.init(configuration);
        assertThat(customCuratorZookeeperInstance.getProperties().getProperty(ZookeeperPropertiesEnum.RETRY_INTERVAL_MILLISECONDS.getKey()), is("1000"));
        assertThat(customCuratorZookeeperInstance.getProperties().getProperty(ZookeeperPropertiesEnum.MAX_RETRIES.getKey()), is("1"));
        assertThat(customCuratorZookeeperInstance.getProperties().getProperty(ZookeeperPropertiesEnum.TIME_TO_LIVE_SECONDS.getKey()), is("100"));
        assertThat(customCuratorZookeeperInstance.getProperties().getProperty(ZookeeperPropertiesEnum.OPERATION_TIMEOUT_MILLISECONDS.getKey()), is("1000"));
        customCuratorZookeeperInstance.persist("/test/children/1", "value1");
        assertThat(customCuratorZookeeperInstance.get("/test/children/1"), is("value1"));
    }
    
    @Test
    public void assertBuildCuratorClientWithTimeToLiveSecondsEqualsZero() {
        final CuratorZookeeperInstance customCuratorZookeeperInstance = new CuratorZookeeperInstance();
        Properties properties = new Properties();
        properties.setProperty(ZookeeperPropertiesEnum.TIME_TO_LIVE_SECONDS.getKey(), "0");
        EmbedTestingServer.start();
        InstanceConfiguration configuration = new InstanceConfiguration(customCuratorZookeeperInstance.getType(), new Properties());
        configuration.setServerLists(SERVER_LISTS);
        customCuratorZookeeperInstance.setProperties(properties);
        customCuratorZookeeperInstance.init(configuration);
        assertThat(customCuratorZookeeperInstance.getProperties().getProperty(ZookeeperPropertiesEnum.TIME_TO_LIVE_SECONDS.getKey()), is("0"));
        customCuratorZookeeperInstance.persist("/test/children/1", "value1");
        assertThat(customCuratorZookeeperInstance.get("/test/children/1"), is("value1"));
    }
    
    @Test
    public void assertBuildCuratorClientWithOperationTimeoutMillisecondsEqualsZero() {
        final CuratorZookeeperInstance customCuratorZookeeperInstance = new CuratorZookeeperInstance();
        Properties properties = new Properties();
        properties.setProperty(ZookeeperPropertiesEnum.OPERATION_TIMEOUT_MILLISECONDS.getKey(), "0");
        EmbedTestingServer.start();
        InstanceConfiguration configuration = new InstanceConfiguration(customCuratorZookeeperInstance.getType(), new Properties());
        configuration.setServerLists(SERVER_LISTS);
        customCuratorZookeeperInstance.setProperties(properties);
        customCuratorZookeeperInstance.init(configuration);
        assertThat(customCuratorZookeeperInstance.getProperties().getProperty(ZookeeperPropertiesEnum.OPERATION_TIMEOUT_MILLISECONDS.getKey()), is("0"));
        customCuratorZookeeperInstance.persist("/test/children/1", "value1");
        assertThat(customCuratorZookeeperInstance.get("/test/children/1"), is("value1"));
    }
    
    @Test
    public void assertBuildCuratorClientWithDigest() {
        final CuratorZookeeperInstance customCuratorZookeeperInstance = new CuratorZookeeperInstance();
        Properties properties = new Properties();
        properties.setProperty(ZookeeperPropertiesEnum.DIGEST.getKey(), "any");
        EmbedTestingServer.start();
        InstanceConfiguration configuration = new InstanceConfiguration(customCuratorZookeeperInstance.getType(), new Properties());
        configuration.setServerLists(SERVER_LISTS);
        customCuratorZookeeperInstance.setProperties(properties);
        customCuratorZookeeperInstance.init(configuration);
        assertThat(customCuratorZookeeperInstance.getProperties().getProperty(ZookeeperPropertiesEnum.DIGEST.getKey()), is("any"));
        customCuratorZookeeperInstance.persist("/test/children/1", "value1");
        assertThat(customCuratorZookeeperInstance.get("/test/children/1"), is("value1"));
    }
}
