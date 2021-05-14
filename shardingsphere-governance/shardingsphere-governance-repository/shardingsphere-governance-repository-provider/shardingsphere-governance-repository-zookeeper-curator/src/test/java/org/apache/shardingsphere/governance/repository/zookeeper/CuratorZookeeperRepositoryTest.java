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

package org.apache.shardingsphere.governance.repository.zookeeper;

import com.google.common.util.concurrent.SettableFuture;
import lombok.SneakyThrows;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory.Builder;
import org.apache.curator.framework.api.ACLProvider;
import org.apache.curator.framework.api.AddWatchBuilder;
import org.apache.curator.framework.api.AddWatchBuilder2;
import org.apache.curator.framework.api.BackgroundCallback;
import org.apache.curator.framework.api.BackgroundVersionable;
import org.apache.curator.framework.api.CreateBuilder;
import org.apache.curator.framework.api.DeleteBuilder;
import org.apache.curator.framework.api.ExistsBuilder;
import org.apache.curator.framework.api.GetChildrenBuilder;
import org.apache.curator.framework.api.Pathable;
import org.apache.curator.framework.api.ProtectACLCreateModeStatPathAndBytesable;
import org.apache.curator.framework.api.SetDataBuilder;
import org.apache.curator.framework.api.WatchableBase;
import org.apache.curator.framework.api.WatchesBuilder;
import org.apache.curator.framework.listen.Listenable;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;
import org.apache.curator.framework.recipes.locks.InterProcessLock;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.shardingsphere.governance.repository.api.config.RegistryCenterConfiguration;
import org.apache.shardingsphere.governance.repository.api.listener.DataChangedEvent;
import org.apache.shardingsphere.governance.repository.api.listener.DataChangedEvent.Type;
import org.apache.shardingsphere.governance.repository.zookeeper.props.ZookeeperPropertyKey;
import org.apache.zookeeper.AddWatchMode;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.Watcher;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class CuratorZookeeperRepositoryTest {
    
    private static final CuratorZookeeperRepository REPOSITORY = new CuratorZookeeperRepository();
    
    private static final String SERVER_LISTS = "127.0.0.1:2181";
    
    @Mock
    private Map<String, CuratorCache> caches;
    
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
    
    @Mock
    private Listenable<ConnectionStateListener> listenerListenable;
    
    @Mock
    private WatchesBuilder watchesBuilder;
    
    @Before
    @SneakyThrows
    public void init() {
        mockClient();
        mockField();
        mockBuilder();
        RegistryCenterConfiguration config = new RegistryCenterConfiguration(REPOSITORY.getType(), SERVER_LISTS, new Properties());
        REPOSITORY.init("governance", config);
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
        when(client.getConnectionStateListenable()).thenReturn(listenerListenable);
        when(client.watchers()).thenReturn(watchesBuilder);
        AddWatchBuilder addWatchBuilder = mock(AddWatchBuilder.class);
        when(watchesBuilder.add()).thenReturn(addWatchBuilder);
        AddWatchBuilder2 addWatchBuilder2 = mock(AddWatchBuilder2.class);
        when(addWatchBuilder.withMode(any(AddWatchMode.class))).thenReturn(addWatchBuilder2);
        WatchableBase<Pathable<Void>> watchableBase = mock(WatchableBase.class);
        when(addWatchBuilder2.inBackground(any(BackgroundCallback.class))).thenReturn(watchableBase);
        when(watchableBase.usingWatcher(any(Watcher.class))).thenReturn(mock(Pathable.class));
    }
    
    @SneakyThrows
    private void mockField() {
        Field locksFiled = CuratorZookeeperRepository.class.getDeclaredField("locks");
        locksFiled.setAccessible(true);
        Map<String, InterProcessLock> locks = new HashMap<>();
        locks.put("/locks/glock", interProcessLock);
        locksFiled.set(REPOSITORY, locks);
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
        when(existsBuilder.forPath(eq("/test"))).thenReturn(null);
        when(protect.withMode(eq(CreateMode.PERSISTENT))).thenReturn(protect);
        REPOSITORY.persist("/test", "value1");
        verify(protect).forPath(eq("/test"), eq("value1".getBytes(StandardCharsets.UTF_8)));
    }
    
    @Test
    @SneakyThrows
    public void assertUpdate() {
        when(existsBuilder.forPath(eq("/test"))).thenReturn(new Stat());
        REPOSITORY.persist("/test", "value2");
        verify(setDataBuilder).forPath(eq("/test"), eq("value2".getBytes(StandardCharsets.UTF_8)));
    }
    
    @Test
    @SneakyThrows
    public void assertPersistEphemeralNotExist() {
        when(existsBuilder.forPath(eq("/test/ephemeral"))).thenReturn(null);
        when(protect.withMode(eq(CreateMode.EPHEMERAL))).thenReturn(protect);
        REPOSITORY.persistEphemeral("/test/ephemeral", "value3");
        verify(protect).forPath(eq("/test/ephemeral"), eq("value3".getBytes(StandardCharsets.UTF_8)));
    }
    
    @Test
    @SneakyThrows
    public void assertPersistEphemeralExist() {
        when(existsBuilder.forPath(eq("/test/ephemeral"))).thenReturn(new Stat());
        when(protect.withMode(eq(CreateMode.EPHEMERAL))).thenReturn(protect);
        REPOSITORY.persistEphemeral("/test/ephemeral", "value4");
        verify(backgroundVersionable).forPath(eq("/test/ephemeral"));
        verify(protect).forPath(eq("/test/ephemeral"), eq("value4".getBytes(StandardCharsets.UTF_8)));
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
        mockCache();
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
        mockCache();
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
        mockCache();
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
    
    private void mockCache() throws NoSuchFieldException, IllegalAccessException {
        Field cachesFiled = CuratorZookeeperRepository.class.getDeclaredField("caches");
        cachesFiled.setAccessible(true);
        cachesFiled.set(REPOSITORY, caches);
        when(caches.get(anyString())).thenReturn(curatorCache);
        when(curatorCache.listenable()).thenReturn(listenable);
    }
    
    private VoidAnswer1 getListenerAnswer(final CuratorCacheListener.Type type, final ChildData oldData, final ChildData data) {
        return (VoidAnswer1<CuratorCacheListener>) listener -> listener.event(type, oldData, data);
    }
    
    @Test
    public void assertBuildCuratorClientWithCustomConfig() {
        Properties props = new Properties();
        props.setProperty(ZookeeperPropertyKey.RETRY_INTERVAL_MILLISECONDS.getKey(), "1000");
        props.setProperty(ZookeeperPropertyKey.MAX_RETRIES.getKey(), "1");
        props.setProperty(ZookeeperPropertyKey.TIME_TO_LIVE_SECONDS.getKey(), "1000");
        props.setProperty(ZookeeperPropertyKey.OPERATION_TIMEOUT_MILLISECONDS.getKey(), "2000");
        RegistryCenterConfiguration config = new RegistryCenterConfiguration(REPOSITORY.getType(), SERVER_LISTS, new Properties());
        REPOSITORY.setProps(props);
        REPOSITORY.init("governance", config);
        assertThat(REPOSITORY.getProps().getProperty(ZookeeperPropertyKey.RETRY_INTERVAL_MILLISECONDS.getKey()), is("1000"));
        assertThat(REPOSITORY.getProps().getProperty(ZookeeperPropertyKey.MAX_RETRIES.getKey()), is("1"));
        assertThat(REPOSITORY.getProps().getProperty(ZookeeperPropertyKey.TIME_TO_LIVE_SECONDS.getKey()), is("1000"));
        assertThat(REPOSITORY.getProps().getProperty(ZookeeperPropertyKey.OPERATION_TIMEOUT_MILLISECONDS.getKey()), is("2000"));
    }
    
    @Test
    public void assertBuildCuratorClientWithTimeToLiveSecondsEqualsZero() {
        Properties props = new Properties();
        props.setProperty(ZookeeperPropertyKey.TIME_TO_LIVE_SECONDS.getKey(), "0");
        RegistryCenterConfiguration config = new RegistryCenterConfiguration(REPOSITORY.getType(), SERVER_LISTS, new Properties());
        REPOSITORY.setProps(props);
        REPOSITORY.init("governance", config);
        assertThat(REPOSITORY.getProps().getProperty(ZookeeperPropertyKey.TIME_TO_LIVE_SECONDS.getKey()), is("0"));
    }
    
    @Test
    public void assertBuildCuratorClientWithOperationTimeoutMillisecondsEqualsZero() {
        Properties props = new Properties();
        props.setProperty(ZookeeperPropertyKey.OPERATION_TIMEOUT_MILLISECONDS.getKey(), "0");
        RegistryCenterConfiguration config = new RegistryCenterConfiguration(REPOSITORY.getType(), SERVER_LISTS, new Properties());
        REPOSITORY.setProps(props);
        REPOSITORY.init("governance", config);
        assertThat(REPOSITORY.getProps().getProperty(ZookeeperPropertyKey.OPERATION_TIMEOUT_MILLISECONDS.getKey()), is("0"));
    }
    
    @Test
    public void assertBuildCuratorClientWithDigest() {
        Properties props = new Properties();
        props.setProperty(ZookeeperPropertyKey.DIGEST.getKey(), "any");
        RegistryCenterConfiguration config = new RegistryCenterConfiguration(REPOSITORY.getType(), SERVER_LISTS, new Properties());
        REPOSITORY.setProps(props);
        REPOSITORY.init("governance", config);
        assertThat(REPOSITORY.getProps().getProperty(ZookeeperPropertyKey.DIGEST.getKey()), is("any"));
        verify(builder).aclProvider(any(ACLProvider.class));
    }
    
    @Test
    @SneakyThrows
    public void assertDeleteNotExistKey() {
        when(existsBuilder.forPath(eq("/test/children/1"))).thenReturn(null);
        REPOSITORY.delete("/test/children/1");
        verify(client, times(0)).delete();
    }
    
    @Test
    @SneakyThrows
    public void assertDeleteExistKey() {
        when(existsBuilder.forPath(eq("/test/children/1"))).thenReturn(new Stat());
        when(deleteBuilder.deletingChildrenIfNeeded()).thenReturn(backgroundVersionable);
        REPOSITORY.delete("/test/children/1");
        verify(backgroundVersionable).forPath("/test/children/1");
    }
    
    @Test
    @SneakyThrows
    public void assertTryLock() {
        when(interProcessLock.acquire(eq(5L), eq(TimeUnit.SECONDS))).thenReturn(true);
        assertThat(REPOSITORY.tryLock("/locks/glock", 5, TimeUnit.SECONDS), is(true));
    }
    
    @Test
    @SneakyThrows
    public void assertTryLockFailed() {
        when(interProcessLock.acquire(eq(5L), eq(TimeUnit.SECONDS))).thenReturn(false);
        assertThat(REPOSITORY.tryLock("/locks/glock", 5, TimeUnit.SECONDS), is(false));
    }
}
