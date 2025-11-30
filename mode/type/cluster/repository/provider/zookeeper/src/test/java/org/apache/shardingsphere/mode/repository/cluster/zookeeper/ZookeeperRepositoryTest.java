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
import org.apache.shardingsphere.infra.instance.ComputeNodeInstanceContext;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder.Property;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepositoryConfiguration;
import org.apache.shardingsphere.mode.repository.cluster.zookeeper.props.ZookeeperPropertyKey;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.internal.configuration.plugins.Plugins;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ZookeeperRepositoryTest {
    
    private static final ZookeeperRepository REPOSITORY = new ZookeeperRepository();
    
    private static final String SERVER_LISTS = "127.0.0.1:2181";
    
    @Mock
    private CuratorFramework client;
    
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
    
    @BeforeEach
    void init() {
        mockClient();
        mockBuilder();
        ClusterPersistRepositoryConfiguration config = new ClusterPersistRepositoryConfiguration(REPOSITORY.getType(), "governance", SERVER_LISTS, new Properties());
        REPOSITORY.init(config, mock(ComputeNodeInstanceContext.class));
    }
    
    @SneakyThrows({ReflectiveOperationException.class, InterruptedException.class})
    private void mockClient() {
        Plugins.getMemberAccessor().set(ZookeeperRepository.class.getDeclaredField("builder"), REPOSITORY, builder);
        when(builder.connectString(anyString())).thenReturn(builder);
        when(builder.retryPolicy(any(RetryPolicy.class))).thenReturn(builder);
        when(builder.ensembleTracker(anyBoolean())).thenReturn(builder);
        when(builder.namespace(anyString())).thenReturn(builder);
        when(builder.sessionTimeoutMs(anyInt())).thenReturn(builder);
        when(builder.connectionTimeoutMs(anyInt())).thenReturn(builder);
        when(builder.authorization(anyString(), any(byte[].class))).thenReturn(builder);
        when(builder.aclProvider(any(ACLProvider.class))).thenReturn(builder);
        when(builder.build()).thenReturn(client);
        when(client.blockUntilConnected(anyInt(), eq(TimeUnit.MILLISECONDS))).thenReturn(true);
    }
    
    @SuppressWarnings("unchecked")
    private void mockBuilder() {
        when(client.checkExists()).thenReturn(existsBuilder);
        when(client.create()).thenReturn(createBuilder);
        when(createBuilder.creatingParentsIfNeeded()).thenReturn(protect);
        when(client.setData()).thenReturn(setDataBuilder);
        when(client.delete()).thenReturn(deleteBuilder);
        when(deleteBuilder.deletingChildrenIfNeeded()).thenReturn(backgroundVersionable);
        when(client.getChildren()).thenReturn(getChildrenBuilder);
        when(client.getConnectionStateListenable()).thenReturn(mock(Listenable.class));
    }
    
    @Test
    void assertPersist() throws Exception {
        when(protect.withMode(CreateMode.PERSISTENT)).thenReturn(protect);
        REPOSITORY.persist("/test", "value1");
        verify(protect).forPath("/test", "value1".getBytes(StandardCharsets.UTF_8));
    }
    
    @Test
    void assertUpdate() throws Exception {
        when(existsBuilder.forPath("/test")).thenReturn(new Stat());
        REPOSITORY.persist("/test", "value2");
        verify(setDataBuilder).forPath("/test", "value2".getBytes(StandardCharsets.UTF_8));
    }
    
    @Test
    void assertPersistEphemeralNotExist() throws Exception {
        when(protect.withMode(CreateMode.EPHEMERAL)).thenReturn(protect);
        REPOSITORY.persistEphemeral("/test/ephemeral", "value3");
        verify(protect).forPath("/test/ephemeral", "value3".getBytes(StandardCharsets.UTF_8));
    }
    
    @Test
    void assertPersistEphemeralExist() throws Exception {
        when(existsBuilder.forPath("/test/ephemeral")).thenReturn(new Stat());
        when(protect.withMode(CreateMode.EPHEMERAL)).thenReturn(protect);
        REPOSITORY.persistEphemeral("/test/ephemeral", "value4");
        verify(backgroundVersionable).forPath("/test/ephemeral");
        verify(protect).forPath("/test/ephemeral", "value4".getBytes(StandardCharsets.UTF_8));
    }
    
    @Test
    void assertGetChildrenKeys() throws Exception {
        List<String> keys = Arrays.asList("/test/children/keys/1", "/test/children/keys/2");
        when(getChildrenBuilder.forPath("/test/children/keys")).thenReturn(keys);
        List<String> childrenKeys = REPOSITORY.getChildrenKeys("/test/children/keys");
        assertThat(childrenKeys.size(), is(2));
    }
    
    @Test
    void assertBuildCuratorClientWithCustomConfiguration() {
        Properties props = PropertiesBuilder.build(
                new Property(ZookeeperPropertyKey.RETRY_INTERVAL_MILLISECONDS.getKey(), "1000"),
                new Property(ZookeeperPropertyKey.MAX_RETRIES.getKey(), "1"),
                new Property(ZookeeperPropertyKey.TIME_TO_LIVE_SECONDS.getKey(), "1000"),
                new Property(ZookeeperPropertyKey.OPERATION_TIMEOUT_MILLISECONDS.getKey(), "2000"));
        assertDoesNotThrow(() -> REPOSITORY.init(new ClusterPersistRepositoryConfiguration(REPOSITORY.getType(), "governance", SERVER_LISTS, props),
                mock(ComputeNodeInstanceContext.class)));
    }
    
    @Test
    void assertBuildCuratorClientWithTimeToLiveSecondsEqualsZero() {
        assertDoesNotThrow(() -> REPOSITORY.init(new ClusterPersistRepositoryConfiguration(
                REPOSITORY.getType(), "governance", SERVER_LISTS, PropertiesBuilder.build(new Property(ZookeeperPropertyKey.TIME_TO_LIVE_SECONDS.getKey(), "0"))),
                mock(ComputeNodeInstanceContext.class)));
    }
    
    @Test
    void assertBuildCuratorClientWithOperationTimeoutMillisecondsEqualsZero() {
        assertDoesNotThrow(() -> REPOSITORY.init(new ClusterPersistRepositoryConfiguration(
                REPOSITORY.getType(), "governance", SERVER_LISTS, PropertiesBuilder.build(new Property(ZookeeperPropertyKey.OPERATION_TIMEOUT_MILLISECONDS.getKey(), "0"))),
                mock(ComputeNodeInstanceContext.class)));
    }
    
    @Test
    void assertBuildCuratorClientWithDigest() {
        REPOSITORY.init(new ClusterPersistRepositoryConfiguration(REPOSITORY.getType(), "governance", SERVER_LISTS,
                PropertiesBuilder.build(new Property(ZookeeperPropertyKey.DIGEST.getKey(), "any"))), mock(ComputeNodeInstanceContext.class));
        verify(builder).aclProvider(any(ACLProvider.class));
    }
    
    @Test
    void assertDeleteNotExistKey() {
        REPOSITORY.delete("/test/children/1");
        verify(client, never()).delete();
    }
    
    @Test
    void assertDeleteExistKey() throws Exception {
        when(existsBuilder.forPath("/test/children/1")).thenReturn(new Stat());
        when(deleteBuilder.deletingChildrenIfNeeded()).thenReturn(backgroundVersionable);
        REPOSITORY.delete("/test/children/1");
        verify(backgroundVersionable).forPath("/test/children/1");
    }
}
