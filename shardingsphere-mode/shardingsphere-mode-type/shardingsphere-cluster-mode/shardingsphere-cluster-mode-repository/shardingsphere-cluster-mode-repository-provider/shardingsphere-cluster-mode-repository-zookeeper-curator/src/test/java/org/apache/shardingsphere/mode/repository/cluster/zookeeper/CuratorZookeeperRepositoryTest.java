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

import com.google.common.util.concurrent.SettableFuture;
import lombok.SneakyThrows;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory.Builder;
import org.apache.curator.framework.api.ACLProvider;
import org.apache.curator.framework.api.BackgroundVersionable;
import org.apache.curator.framework.api.CreateBuilder;
import org.apache.curator.framework.api.DeleteBuilder;
import org.apache.curator.framework.api.ExistsBuilder;
import org.apache.curator.framework.api.GetChildrenBuilder;
import org.apache.curator.framework.api.ProtectACLCreateModeStatPathAndBytesable;
import org.apache.curator.framework.api.SetDataBuilder;
import org.apache.curator.framework.listen.Listenable;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;
import org.apache.curator.framework.recipes.locks.InterProcessLock;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepositoryConfiguration;
import org.apache.shardingsphere.mode.repository.cluster.listener.DataChangedEvent;
import org.apache.shardingsphere.mode.repository.cluster.listener.DataChangedEvent.Type;
import org.apache.shardingsphere.mode.repository.cluster.zookeeper.props.ZookeeperPropertyKey;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.AdditionalAnswers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.VoidAnswer1;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class CuratorZookeeperRepositoryTest {
    
    private static final CuratorZookeeperRepository REPOSITORY = new CuratorZookeeperRepository();
    
    private static final String SERVER_LISTS = "127.0.0.1:2181";
    
    @Mock
    private CuratorCache curatorCache;
    
    @Mock
    private CuratorFramework client;
    
    @Mock
    private Listenable<CuratorCacheListener> listenable;
    
    @Mock
    private ExistsBuilder existsBuilder;
    
    @Mock
    private CreateBuilder createBuilder;
    
    @Mock
    private SetDataBuilder setDataBuilder;
    
    @Mock
    private DeleteBuilder deleteBuilder;
    
    @Mock
    private GetChildrenBuilder getChildrenBuilder;
    
    @Mock
    private ProtectACLCreateModeStatPathAndBytesable<String> protect;
    
    @Mock
    private BackgroundVersionable backgroundVersionable;
    
    @Mock
    private Builder builder;
    
    @Mock
    private InterProcessLock interProcessLock;
    
    @Before
    @SneakyThrows
    public void init() {
        mockClient();
        mockField();
        mockBuilder();
        ClusterPersistRepositoryConfiguration config = new ClusterPersistRepositoryConfiguration(REPOSITORY.getType(), "governance", SERVER_LISTS, new Properties());
        REPOSITORY.init(config);
    }
    
    @SneakyThrows
    private void mockClient() {
        Field builderFiled = CuratorZookeeperRepository.class.getDeclaredField("builder");
        builderFiled.setAccessible(true);
        builderFiled.set(REPOSITORY, builder);
        when(builder.connectString(anyString())).thenReturn(builder);
        when(builder.retryPolicy(any(RetryPolicy.class))).thenReturn(builder);
        when(builder.namespace(anyString())).thenReturn(builder);
        when(builder.sessionTimeoutMs(anyInt())).thenReturn(builder);
        when(builder.connectionTimeoutMs(anyInt())).thenReturn(builder);
        when(builder.authorization(anyString(), any(byte[].class))).thenReturn(builder);
        when(builder.aclProvider(any(ACLProvider.class))).thenReturn(builder);
        when(builder.build()).thenReturn(client);
        when(client.blockUntilConnected(anyInt(), eq(TimeUnit.MILLISECONDS))).thenReturn(true);
    }
    
    @SneakyThrows
    private void mockField() {
        Field locksFiled = CuratorZookeeperRepository.class.getDeclaredField("locks");
        locksFiled.setAccessible(true);
        locksFiled.set(REPOSITORY, Collections.singletonMap("/locks/glock", interProcessLock));
    }
    
    private void mockBuilder() {
        when(client.checkExists()).thenReturn(existsBuilder);
        when(client.create()).thenReturn(createBuilder);
        when(createBuilder.creatingParentsIfNeeded()).thenReturn(protect);
        when(client.setData()).thenReturn(setDataBuilder);
        when(client.delete()).thenReturn(deleteBuilder);
        when(deleteBuilder.deletingChildrenIfNeeded()).thenReturn(backgroundVersionable);
        when(client.getChildren()).thenReturn(getChildrenBuilder);
    }
    
    @Test
    @SneakyThrows
    public void assertPersist() {
        when(protect.withMode(CreateMode.PERSISTENT)).thenReturn(protect);
        REPOSITORY.persist("/test", "value1");
        verify(protect).forPath("/test", "value1".getBytes(StandardCharsets.UTF_8));
    }
    
    @Test
    @SneakyThrows
    public void assertUpdate() {
        when(existsBuilder.forPath("/test")).thenReturn(new Stat());
        REPOSITORY.persist("/test", "value2");
        verify(setDataBuilder).forPath("/test", "value2".getBytes(StandardCharsets.UTF_8));
    }
    
    @Test
    @SneakyThrows
    public void assertPersistEphemeralNotExist() {
        when(protect.withMode(CreateMode.EPHEMERAL)).thenReturn(protect);
        REPOSITORY.persistEphemeral("/test/ephemeral", "value3");
        verify(protect).forPath("/test/ephemeral", "value3".getBytes(StandardCharsets.UTF_8));
    }
    
    @Test
    @SneakyThrows
    public void assertPersistEphemeralExist() {
        when(existsBuilder.forPath("/test/ephemeral")).thenReturn(new Stat());
        when(protect.withMode(CreateMode.EPHEMERAL)).thenReturn(protect);
        REPOSITORY.persistEphemeral("/test/ephemeral", "value4");
        verify(backgroundVersionable).forPath("/test/ephemeral");
        verify(protect).forPath("/test/ephemeral", "value4".getBytes(StandardCharsets.UTF_8));
    }
    
    @Test
    @SneakyThrows
    public void assertGetChildrenKeys() {
        List<String> keys = Arrays.asList("/test/children/keys/1", "/test/children/keys/2");
        when(getChildrenBuilder.forPath("/test/children/keys")).thenReturn(keys);
        List<String> childrenKeys = REPOSITORY.getChildrenKeys("/test/children/keys");
        assertThat(childrenKeys.size(), is(2));
    }
    
    @Test
    @SneakyThrows
    public void assertWatchUpdatedChangedType() {
        mockCache("/test/children_updated/1");
        ChildData oldData = new ChildData("/test/children_updated/1", null, "value1".getBytes());
        ChildData data = new ChildData("/test/children_updated/1", null, "value2".getBytes());
        doAnswer(AdditionalAnswers.answerVoid(getListenerAnswer(CuratorCacheListener.Type.NODE_CHANGED, oldData, data))).when(listenable).addListener(any(CuratorCacheListener.class));
        SettableFuture<DataChangedEvent> settableFuture = SettableFuture.create();
        REPOSITORY.watch("/test/children_updated/1", settableFuture::set);
        DataChangedEvent dataChangedEvent = settableFuture.get();
        assertNotNull(dataChangedEvent);
        assertThat(dataChangedEvent.getType(), is(Type.UPDATED));
        assertThat(dataChangedEvent.getKey(), is("/test/children_updated/1"));
        assertThat(dataChangedEvent.getValue(), is("value2"));
    }
    
    @Test
    public void assertWatchDeletedChangedType() throws Exception {
        mockCache("/test/children_deleted/5");
        ChildData oldData = new ChildData("/test/children_deleted/5", null, "value5".getBytes());
        ChildData data = new ChildData("/test/children_deleted/5", null, "value5".getBytes());
        doAnswer(AdditionalAnswers.answerVoid(getListenerAnswer(CuratorCacheListener.Type.NODE_DELETED, oldData, data))).when(listenable).addListener(any(CuratorCacheListener.class));
        SettableFuture<DataChangedEvent> settableFuture = SettableFuture.create();
        REPOSITORY.watch("/test/children_deleted/5", settableFuture::set);
        DataChangedEvent dataChangedEvent = settableFuture.get();
        assertNotNull(dataChangedEvent);
        assertThat(dataChangedEvent.getType(), is(Type.DELETED));
        assertThat(dataChangedEvent.getKey(), is("/test/children_deleted/5"));
        assertThat(dataChangedEvent.getValue(), is("value5"));
    }
    
    @Test
    @SneakyThrows
    public void assertWatchAddedChangedType() {
        mockCache("/test/children_added/4");
        ChildData data = new ChildData("/test/children_added/4", null, "value4".getBytes());
        doAnswer(AdditionalAnswers.answerVoid(getListenerAnswer(CuratorCacheListener.Type.NODE_CREATED, null, data))).when(listenable).addListener(any(CuratorCacheListener.class));
        SettableFuture<DataChangedEvent> settableFuture = SettableFuture.create();
        REPOSITORY.watch("/test/children_added/4", settableFuture::set);
        DataChangedEvent dataChangedEvent = settableFuture.get();
        assertNotNull(dataChangedEvent);
        assertThat(dataChangedEvent.getType(), is(Type.ADDED));
        assertThat(dataChangedEvent.getKey(), is("/test/children_added/4"));
        assertThat(dataChangedEvent.getValue(), is("value4"));
    }
    
    private void mockCache(final String key) throws Exception {
        Field cachesFiled = CuratorZookeeperRepository.class.getDeclaredField("caches");
        cachesFiled.setAccessible(true);
        Map<String, CuratorCache> caches = new HashMap<>();
        caches.put(key, curatorCache);
        cachesFiled.set(REPOSITORY, caches);
        when(curatorCache.listenable()).thenReturn(listenable);
    }
    
    private VoidAnswer1<CuratorCacheListener> getListenerAnswer(final CuratorCacheListener.Type type, final ChildData oldData, final ChildData data) {
        return listener -> listener.event(type, oldData, data);
    }
    
    @Test
    public void assertBuildCuratorClientWithCustomConfig() {
        Properties props = new Properties();
        props.setProperty(ZookeeperPropertyKey.RETRY_INTERVAL_MILLISECONDS.getKey(), "1000");
        props.setProperty(ZookeeperPropertyKey.MAX_RETRIES.getKey(), "1");
        props.setProperty(ZookeeperPropertyKey.TIME_TO_LIVE_SECONDS.getKey(), "1000");
        props.setProperty(ZookeeperPropertyKey.OPERATION_TIMEOUT_MILLISECONDS.getKey(), "2000");
        ClusterPersistRepositoryConfiguration config = new ClusterPersistRepositoryConfiguration(REPOSITORY.getType(), "governance", SERVER_LISTS, new Properties());
        REPOSITORY.setProps(props);
        REPOSITORY.init(config);
        assertThat(REPOSITORY.getProps().getProperty(ZookeeperPropertyKey.RETRY_INTERVAL_MILLISECONDS.getKey()), is("1000"));
        assertThat(REPOSITORY.getProps().getProperty(ZookeeperPropertyKey.MAX_RETRIES.getKey()), is("1"));
        assertThat(REPOSITORY.getProps().getProperty(ZookeeperPropertyKey.TIME_TO_LIVE_SECONDS.getKey()), is("1000"));
        assertThat(REPOSITORY.getProps().getProperty(ZookeeperPropertyKey.OPERATION_TIMEOUT_MILLISECONDS.getKey()), is("2000"));
    }
    
    @Test
    public void assertBuildCuratorClientWithTimeToLiveSecondsEqualsZero() {
        Properties props = new Properties();
        props.setProperty(ZookeeperPropertyKey.TIME_TO_LIVE_SECONDS.getKey(), "0");
        ClusterPersistRepositoryConfiguration config = new ClusterPersistRepositoryConfiguration(REPOSITORY.getType(), "governance", SERVER_LISTS, new Properties());
        REPOSITORY.setProps(props);
        REPOSITORY.init(config);
        assertThat(REPOSITORY.getProps().getProperty(ZookeeperPropertyKey.TIME_TO_LIVE_SECONDS.getKey()), is("0"));
    }
    
    @Test
    public void assertBuildCuratorClientWithOperationTimeoutMillisecondsEqualsZero() {
        Properties props = new Properties();
        props.setProperty(ZookeeperPropertyKey.OPERATION_TIMEOUT_MILLISECONDS.getKey(), "0");
        ClusterPersistRepositoryConfiguration config = new ClusterPersistRepositoryConfiguration(REPOSITORY.getType(), "governance", SERVER_LISTS, new Properties());
        REPOSITORY.setProps(props);
        REPOSITORY.init(config);
        assertThat(REPOSITORY.getProps().getProperty(ZookeeperPropertyKey.OPERATION_TIMEOUT_MILLISECONDS.getKey()), is("0"));
    }
    
    @Test
    public void assertBuildCuratorClientWithDigest() {
        Properties props = new Properties();
        props.setProperty(ZookeeperPropertyKey.DIGEST.getKey(), "any");
        ClusterPersistRepositoryConfiguration config = new ClusterPersistRepositoryConfiguration(REPOSITORY.getType(), "governance", SERVER_LISTS, new Properties());
        REPOSITORY.setProps(props);
        REPOSITORY.init(config);
        assertThat(REPOSITORY.getProps().getProperty(ZookeeperPropertyKey.DIGEST.getKey()), is("any"));
        verify(builder).aclProvider(any(ACLProvider.class));
    }
    
    @Test
    @SneakyThrows
    public void assertDeleteNotExistKey() {
        REPOSITORY.delete("/test/children/1");
        verify(client, times(0)).delete();
    }
    
    @Test
    @SneakyThrows
    public void assertDeleteExistKey() {
        when(existsBuilder.forPath("/test/children/1")).thenReturn(new Stat());
        when(deleteBuilder.deletingChildrenIfNeeded()).thenReturn(backgroundVersionable);
        REPOSITORY.delete("/test/children/1");
        verify(backgroundVersionable).forPath("/test/children/1");
    }
    
    @Test
    @SneakyThrows
    public void assertTryLock() {
        when(interProcessLock.acquire(5L, TimeUnit.SECONDS)).thenReturn(true);
        assertTrue(REPOSITORY.tryLock("/locks/glock", 5, TimeUnit.SECONDS));
    }
    
    @Test
    @SneakyThrows
    public void assertTryLockFailed() {
        when(interProcessLock.acquire(5L, TimeUnit.SECONDS)).thenReturn(false);
        assertFalse(REPOSITORY.tryLock("/locks/glock", 5, TimeUnit.SECONDS));
    }
}
