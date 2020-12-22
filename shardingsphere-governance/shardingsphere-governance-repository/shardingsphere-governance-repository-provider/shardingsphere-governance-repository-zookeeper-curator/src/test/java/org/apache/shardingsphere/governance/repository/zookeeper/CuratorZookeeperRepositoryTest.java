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
import org.apache.curator.framework.listen.Listenable;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;
import org.apache.shardingsphere.governance.repository.api.config.GovernanceCenterConfiguration;
import org.apache.shardingsphere.governance.repository.api.exception.GovernanceException;
import org.apache.shardingsphere.governance.repository.api.listener.DataChangedEvent;
import org.apache.shardingsphere.governance.repository.api.listener.DataChangedEvent.Type;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.AdditionalAnswers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.VoidAnswer1;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class CuratorZookeeperRepositoryTest {
    
    private static final CuratorZookeeperRepository REPOSITORY = new CuratorZookeeperRepository();
    
    private static String serverLists;
    
    @Mock
    private Map<String, CuratorCache> caches;
    
    @Mock
    private CuratorCache curatorCache;
    
    @Mock
    private Listenable<CuratorCacheListener> listenable;
    
    @BeforeClass
    public static void init() {
        EmbedTestingServer.start();
        serverLists = EmbedTestingServer.getTestingServerConnectionString();
        REPOSITORY.init("governance", new GovernanceCenterConfiguration(REPOSITORY.getType(), serverLists, new Properties()));
        REPOSITORY.initLock("/glock");
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
    public void assertGetWithNonExistentKey() {
        assertNull(REPOSITORY.get("/test/nonExistentKey"));
    }
    
    @Test
    public void assertBuildCuratorClientWithCustomConfig() {
        Properties props = new Properties();
        props.setProperty(ZookeeperPropertyKey.RETRY_INTERVAL_MILLISECONDS.getKey(), "1000");
        props.setProperty(ZookeeperPropertyKey.MAX_RETRIES.getKey(), "1");
        props.setProperty(ZookeeperPropertyKey.TIME_TO_LIVE_SECONDS.getKey(), "1000");
        props.setProperty(ZookeeperPropertyKey.OPERATION_TIMEOUT_MILLISECONDS.getKey(), "2000");
        CuratorZookeeperRepository repository = new CuratorZookeeperRepository();
        GovernanceCenterConfiguration config = new GovernanceCenterConfiguration(repository.getType(), serverLists, new Properties());
        repository.setProps(props);
        repository.init("governance", config);
        assertThat(repository.getProps().getProperty(ZookeeperPropertyKey.RETRY_INTERVAL_MILLISECONDS.getKey()), is("1000"));
        assertThat(repository.getProps().getProperty(ZookeeperPropertyKey.MAX_RETRIES.getKey()), is("1"));
        assertThat(repository.getProps().getProperty(ZookeeperPropertyKey.TIME_TO_LIVE_SECONDS.getKey()), is("1000"));
        assertThat(repository.getProps().getProperty(ZookeeperPropertyKey.OPERATION_TIMEOUT_MILLISECONDS.getKey()), is("2000"));
        repository.persist("/test/children/build/1", "value1");
        assertThat(repository.get("/test/children/build/1"), is("value1"));
    }
    
    @Test
    public void assertBuildCuratorClientWithTimeToLiveSecondsEqualsZero() {
        Properties props = new Properties();
        props.setProperty(ZookeeperPropertyKey.TIME_TO_LIVE_SECONDS.getKey(), "0");
        CuratorZookeeperRepository repository = new CuratorZookeeperRepository();
        GovernanceCenterConfiguration config = new GovernanceCenterConfiguration(repository.getType(), serverLists, new Properties());
        repository.setProps(props);
        repository.init("governance", config);
        assertThat(repository.getProps().getProperty(ZookeeperPropertyKey.TIME_TO_LIVE_SECONDS.getKey()), is("0"));
        repository.persist("/test/children/build/2", "value1");
        assertThat(repository.get("/test/children/build/2"), is("value1"));
    }
    
    @Test
    public void assertBuildCuratorClientWithOperationTimeoutMillisecondsEqualsZero() {
        Properties props = new Properties();
        props.setProperty(ZookeeperPropertyKey.OPERATION_TIMEOUT_MILLISECONDS.getKey(), "0");
        CuratorZookeeperRepository repository = new CuratorZookeeperRepository();
        GovernanceCenterConfiguration config = new GovernanceCenterConfiguration(repository.getType(), serverLists, new Properties());
        repository.setProps(props);
        repository.init("governance", config);
        assertThat(repository.getProps().getProperty(ZookeeperPropertyKey.OPERATION_TIMEOUT_MILLISECONDS.getKey()), is("0"));
        repository.persist("/test/children/build/3", "value1");
        assertThat(repository.get("/test/children/build/3"), is("value1"));
    }
    
    @Test
    public void assertBuildCuratorClientWithDigest() {
        Properties props = new Properties();
        props.setProperty(ZookeeperPropertyKey.DIGEST.getKey(), "any");
        CuratorZookeeperRepository repository = new CuratorZookeeperRepository();
        GovernanceCenterConfiguration config = new GovernanceCenterConfiguration(repository.getType(), serverLists, new Properties());
        repository.setProps(props);
        repository.init("governance", config);
        assertThat(repository.getProps().getProperty(ZookeeperPropertyKey.DIGEST.getKey()), is("any"));
        repository.persist("/test/children/build/4", "value1");
        assertThat(repository.get("/test/children/build/4"), is("value1"));
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
        CuratorZookeeperRepository repository = new CuratorZookeeperRepository();
        GovernanceCenterConfiguration config = new GovernanceCenterConfiguration(repository.getType(), serverLists, new Properties());
        Properties props = new Properties();
        props.setProperty(ZookeeperPropertyKey.DIGEST.getKey(), "digest");
        repository.setProps(props);
        repository.init("governance", config);
        repository.close();
        try {
            repository.get("/test/children/1");
            fail("must be failed after close.");
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            assertTrue(ex instanceof GovernanceException);
        }
        try {
            repository.persist("/test/children/01", "value1");
            fail("must be failed after close.");
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            assertTrue(ex instanceof GovernanceException);
        }
        try {
            repository.delete("/test/children/02");
            fail("must be failed after close.");
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            assertTrue(ex instanceof GovernanceException);
        }
        try {
            repository.persistEphemeral("/test/children/03", "value1");
            fail("must be failed after close.");
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            assertTrue(ex instanceof GovernanceException);
        }
        try {
            repository.getChildrenKeys("/test/children");
            fail("must be failed after close.");
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            assertTrue(ex instanceof GovernanceException);
        }
    }
    
    @Test
    public void assertTryLock() {
        assertThat(REPOSITORY.tryLock(5, TimeUnit.SECONDS), is(true));
        REPOSITORY.releaseLock();
    }
    
    @Test
    @SneakyThrows
    public void assertTryLockFailed() {
        assertThat(REPOSITORY.tryLock(1, TimeUnit.SECONDS), is(true));
        FutureTask<Boolean> task = new FutureTask(() -> REPOSITORY.tryLock(1, TimeUnit.SECONDS));
        new Thread(task).start();
        assertThat(task.get(), is(false));
        REPOSITORY.releaseLock();
    }
}
