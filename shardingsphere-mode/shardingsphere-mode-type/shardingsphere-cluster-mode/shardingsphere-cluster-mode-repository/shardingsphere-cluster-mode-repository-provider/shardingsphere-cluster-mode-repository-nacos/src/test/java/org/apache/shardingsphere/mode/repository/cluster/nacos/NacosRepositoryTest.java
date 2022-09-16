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

package org.apache.shardingsphere.mode.repository.cluster.nacos;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.PreservedMetadataKeys;
import com.alibaba.nacos.api.naming.listener.Event;
import com.alibaba.nacos.api.naming.listener.EventListener;
import com.alibaba.nacos.api.naming.listener.NamingEvent;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.common.utils.StringUtils;
import com.google.common.util.concurrent.SettableFuture;
import lombok.SneakyThrows;
import org.apache.shardingsphere.mode.persist.PersistRepository;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepositoryException;
import org.apache.shardingsphere.mode.repository.cluster.listener.DataChangedEvent;
import org.apache.shardingsphere.mode.repository.cluster.nacos.entity.RegisterMetadata;
import org.apache.shardingsphere.mode.repository.cluster.nacos.props.NacosProperties;
import org.apache.shardingsphere.mode.repository.cluster.nacos.props.NacosPropertyKey;
import org.apache.shardingsphere.mode.repository.cluster.nacos.utils.MetadataUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.AdditionalAnswers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.internal.configuration.plugins.Plugins;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.plugins.MemberAccessor;
import org.mockito.stubbing.VoidAnswer2;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class NacosRepositoryTest {
    
    private static final NacosRepository REPOSITORY = new NacosRepository();
    
    @Mock
    private NamingService client;
    
    @Before
    @SneakyThrows(Exception.class)
    public void initClient() {
        MemberAccessor accessor = Plugins.getMemberAccessor();
        accessor.set(REPOSITORY.getClass().getDeclaredField("nacosProps"), REPOSITORY, new NacosProperties(new Properties()));
        accessor.set(REPOSITORY.getClass().getDeclaredField("client"), REPOSITORY, client);
        accessor.set(REPOSITORY.getClass().getDeclaredField("isDuplicated"), REPOSITORY, new AtomicBoolean(false));
        RegisterMetadata.PERSISTENT.setPort(new AtomicInteger(Integer.MIN_VALUE));
        RegisterMetadata.EPHEMERAL.setPort(new AtomicInteger(Integer.MIN_VALUE));

    }
    
    @Test
    @SneakyThrows
    public void assertGetLatestKey() {
        int total = 2;
        String key = "/test/children/keys/persistent/1";
        List<Instance> instances = new LinkedList<>();
        for (int count = 1; count <= total; count++) {
            Instance instance = new Instance();
            final Map<String, String> metadataMap = new HashMap<>(2, 1);
            metadataMap.put(key, "value" + count);
            metadataMap.put(MetadataUtil.UTC_ZONE_OFFSET.toString(), String.valueOf(count));
            instance.setMetadata(metadataMap);
            instances.add(instance);
        }
        when(client.getAllInstances(RegisterMetadata.PERSISTENT.name(), false)).thenReturn(instances);
        String value = REPOSITORY.get(key);
        verify(client).getAllInstances(RegisterMetadata.PERSISTENT.name(), false);
        assertThat(value, is("value2"));
    }
    
    @Test
    @SneakyThrows
    public void assertGetChildrenKeys() {
        Instance instance = new Instance();
        String key = "/test/children/keys/persistent/0";
        instance.setMetadata(Collections.singletonMap(key, "value0"));
        when(client.getAllInstances(RegisterMetadata.PERSISTENT.name(), false)).thenReturn(Collections.singletonList(instance));
        instance = new Instance();
        key = "/test/children/keys/ephemeral/0";
        instance.setMetadata(Collections.singletonMap(key, "value0"));
        when(client.getAllInstances(RegisterMetadata.EPHEMERAL.name(), false)).thenReturn(Collections.singletonList(instance));
        List<String> childrenKeys = REPOSITORY.getChildrenKeys("/test/children/keys");
        assertThat(childrenKeys.size(), is(2));
        assertThat(childrenKeys.get(0), is("persistent"));
        assertThat(childrenKeys.get(1), is("ephemeral"));
    }
    
    @Test
    @SneakyThrows
    public void assertPersistNotExistKey() {
        String key = "/test/children/keys/persistent/1";
        doAnswer(AdditionalAnswers.answerVoid(getRegisterInstanceAnswer())).when(client).registerInstance(anyString(), any(Instance.class));
        REPOSITORY.persist(key, "value4");
        ArgumentCaptor<Instance> instanceArgumentCaptor = ArgumentCaptor.forClass(Instance.class);
        ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
        verify(client, times(5)).registerInstance(stringArgumentCaptor.capture(), instanceArgumentCaptor.capture());
        Instance registerInstance = instanceArgumentCaptor.getValue();
        String registerType = stringArgumentCaptor.getValue();
        assertThat(registerType, is(RegisterMetadata.PERSISTENT.name()));
        assertThat(registerInstance.isEphemeral(), is(false));
        assertThat(MetadataUtil.getValue(registerInstance), is("value4"));
    }
    
    @Test
    @SneakyThrows
    public void assertPersistExistKey() {
        String ip = "127.0.0.1";
        Instance instance = new Instance();
        instance.setIp(ip);
        instance.setEphemeral(false);
        String key = "/test/children/keys/persistent/0";
        Map<String, String> metadataMap = new HashMap<>(1, 1);
        metadataMap.put(key, "value0");
        instance.setMetadata(metadataMap);
        List<Instance> instances = new LinkedList<>();
        buildParentPath(key, instances);
        instances.add(instance);
        when(client.getAllInstances(RegisterMetadata.PERSISTENT.name(), false)).thenReturn(instances);
        doAnswer(AdditionalAnswers.answerVoid(getRegisterInstanceAnswer())).when(client).registerInstance(anyString(), any(Instance.class));
        REPOSITORY.persist(key, "value4");
        ArgumentCaptor<Instance> instanceArgumentCaptor = ArgumentCaptor.forClass(Instance.class);
        ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).registerInstance(stringArgumentCaptor.capture(), instanceArgumentCaptor.capture());
        Instance registerInstance = instanceArgumentCaptor.getValue();
        String registerType = stringArgumentCaptor.getValue();
        assertThat(registerType, is(RegisterMetadata.PERSISTENT.name()));
        assertThat(registerInstance.getIp(), is(ip));
        assertThat(registerInstance.isEphemeral(), is(false));
        assertThat(MetadataUtil.getValue(registerInstance), is("value4"));
    }
    
    @Test
    @SneakyThrows
    public void assertPersistEphemeralExistKey() {
        final String key = "/test/children/keys/ephemeral/1";
        final Instance instance = new Instance();
        instance.setEphemeral(true);
        Map<String, String> metadataMap = new HashMap<>(4, 1);
        metadataMap.put(PreservedMetadataKeys.HEART_BEAT_INTERVAL, String.valueOf(2000));
        metadataMap.put(PreservedMetadataKeys.HEART_BEAT_TIMEOUT, String.valueOf(4000));
        metadataMap.put(PreservedMetadataKeys.IP_DELETE_TIMEOUT, String.valueOf(6000));
        metadataMap.put(key, "value0");
        instance.setMetadata(metadataMap);
        List<Instance> instances = new LinkedList<>();
        buildParentPath(key, instances);
        when(client.getAllInstances(RegisterMetadata.PERSISTENT.name(), false)).thenReturn(instances);
        instances = new LinkedList<>();
        instances.add(instance);
        when(client.getAllInstances(RegisterMetadata.EPHEMERAL.name(), false)).thenReturn(instances);
        doAnswer(AdditionalAnswers.answerVoid(getDeregisterInstanceAnswer())).when(client).deregisterInstance(anyString(), any(Instance.class));
        doAnswer(AdditionalAnswers.answerVoid(getRegisterInstanceAnswer())).when(client).registerInstance(anyString(), any(Instance.class));
        REPOSITORY.persistEphemeral(key, "value4");
        ArgumentCaptor<Instance> instanceArgumentCaptor = ArgumentCaptor.forClass(Instance.class);
        ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).deregisterInstance(anyString(), any(Instance.class));
        verify(client).registerInstance(stringArgumentCaptor.capture(), instanceArgumentCaptor.capture());
        Instance registerInstance = instanceArgumentCaptor.getValue();
        String registerType = stringArgumentCaptor.getValue();
        assertThat(registerType, is(RegisterMetadata.EPHEMERAL.name()));
        assertThat(registerInstance.isEphemeral(), is(true));
        assertThat(MetadataUtil.getValue(registerInstance), is("value4"));
        Map<String, String> metadata = registerInstance.getMetadata();
        long timeToLiveSeconds = Long.parseLong(NacosPropertyKey.TIME_TO_LIVE_SECONDS.getDefaultValue());
        assertThat(metadata.get(PreservedMetadataKeys.HEART_BEAT_INTERVAL), is(String.valueOf(timeToLiveSeconds * 1000 / 3)));
        assertThat(metadata.get(PreservedMetadataKeys.HEART_BEAT_TIMEOUT), is(String.valueOf(timeToLiveSeconds * 1000 * 2 / 3)));
        assertThat(metadata.get(PreservedMetadataKeys.IP_DELETE_TIMEOUT), is(String.valueOf(timeToLiveSeconds * 1000)));
    }
    
    private void buildParentPath(final String key, final List<Instance> instances) {
        StringBuilder parentPath = new StringBuilder();
        final String[] partPath = key.split(PersistRepository.PATH_SEPARATOR);
        for (int index = 1; index < partPath.length - 1; index++) {
            parentPath.append(PersistRepository.PATH_SEPARATOR);
            parentPath.append(partPath[index]);
            String path = parentPath.toString();
            Instance instance = new Instance();
            instance.setEphemeral(false);
            instance.setMetadata(Collections.singletonMap(path, ""));
            instances.add(instance);
        }
    }
    
    @Test
    @SneakyThrows
    public void assertPersistEphemeralNotExistKey() {
        String key = "/test/children/keys/ephemeral/0";
        doAnswer(AdditionalAnswers.answerVoid(getRegisterInstanceAnswer())).when(client).registerInstance(anyString(), any(Instance.class));
        REPOSITORY.persistEphemeral(key, "value0");
        ArgumentCaptor<Instance> instanceArgumentCaptor = ArgumentCaptor.forClass(Instance.class);
        ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
        verify(client, times(5)).registerInstance(stringArgumentCaptor.capture(), instanceArgumentCaptor.capture());
        Instance registerInstance = instanceArgumentCaptor.getValue();
        String registerType = stringArgumentCaptor.getValue();
        assertThat(registerType, is(RegisterMetadata.EPHEMERAL.name()));
        assertThat(registerInstance.isEphemeral(), is(true));
        assertThat(MetadataUtil.getValue(registerInstance), is("value0"));
        Map<String, String> metadata = registerInstance.getMetadata();
        long timeToLiveSeconds = Long.parseLong(NacosPropertyKey.TIME_TO_LIVE_SECONDS.getDefaultValue());
        assertThat(metadata.get(PreservedMetadataKeys.HEART_BEAT_INTERVAL), is(String.valueOf(timeToLiveSeconds * 1000 / 3)));
        assertThat(metadata.get(PreservedMetadataKeys.HEART_BEAT_TIMEOUT), is(String.valueOf(timeToLiveSeconds * 1000 * 2 / 3)));
        assertThat(metadata.get(PreservedMetadataKeys.IP_DELETE_TIMEOUT), is(String.valueOf(timeToLiveSeconds * 1000)));
    }
    
    @Test
    @SneakyThrows
    public void assertDeleteExistKey() {
        int total = 3;
        List<Instance> instances = new LinkedList<>();
        for (int count = 1; count <= total; count++) {
            String key = "/test/children/keys/ephemeral/" + count;
            Instance instance = new Instance();
            instance.setEphemeral(true);
            instance.setMetadata(Collections.singletonMap(key, "value" + count));
            instances.add(instance);
        }
        when(client.getAllInstances(RegisterMetadata.EPHEMERAL.name(), false)).thenReturn(instances);
        instances = new LinkedList<>();
        String key = "/test/children/keys/persistent/0";
        Instance instance = new Instance();
        instance.setEphemeral(false);
        instance.setMetadata(Collections.singletonMap(key, "value0"));
        instances.add(instance);
        when(client.getAllInstances(RegisterMetadata.PERSISTENT.name(), false)).thenReturn(instances);
        doAnswer(AdditionalAnswers.answerVoid(getDeregisterInstanceAnswer())).when(client).deregisterInstance(anyString(), any(Instance.class));
        REPOSITORY.delete("/test/children/keys");
        verify(client, times(4)).deregisterInstance(anyString(), any(Instance.class));
    }
    
    @Test
    @SneakyThrows
    public void assertDeleteNotExistKey() {
        REPOSITORY.delete("/test/children/keys/persistent/1");
        verify(client, times(0)).deregisterInstance(anyString(), any(Instance.class));
    }
    
    @Test
    @SneakyThrows
    public void assertWatchAdded() {
        RegisterMetadata.PERSISTENT.setListener(null);
        String key = "key/key";
        String value = "value2";
        Instance instance = new Instance();
        instance.setMetadata(Collections.singletonMap(key, value));
        Event event = new NamingEvent(RegisterMetadata.EPHEMERAL.name(), Collections.singletonList(instance));
        doAnswer(AdditionalAnswers.answerVoid(getListenerAnswer(null, event))).when(client).subscribe(anyString(), any(EventListener.class));
        SettableFuture<DataChangedEvent> settableFuture = SettableFuture.create();
        REPOSITORY.watch(key, settableFuture::set);
        DataChangedEvent dataChangedEvent = settableFuture.get();
        assertThat(dataChangedEvent.getType(), is(DataChangedEvent.Type.ADDED));
        assertThat(dataChangedEvent.getKey(), is(key));
        assertThat(dataChangedEvent.getValue(), is(value));
    }
    
    @Test
    @SneakyThrows
    public void assertWatchUpdate() {
        RegisterMetadata.PERSISTENT.setListener(null);
        String key = "key/key";
        long epochMilliseconds = MetadataUtil.getTimestamp();
        Instance preInstance = new Instance();
        Map<String, String> metadataMap = new HashMap<>();
        metadataMap.put(key, "value1");
        metadataMap.put(MetadataUtil.UTC_ZONE_OFFSET.toString(), String.valueOf(epochMilliseconds));
        preInstance.setMetadata(metadataMap);
        final Instance instance = new Instance();
        metadataMap = new HashMap<>();
        metadataMap.put(key, "value2");
        metadataMap.put(MetadataUtil.UTC_ZONE_OFFSET.toString(), String.valueOf(epochMilliseconds + 1));
        instance.setMetadata(metadataMap);
        Event event = new NamingEvent(RegisterMetadata.EPHEMERAL.name(), Collections.singletonList(instance));
        doAnswer(AdditionalAnswers.answerVoid(getListenerAnswer(preInstance, event))).when(client).subscribe(anyString(), any(EventListener.class));
        SettableFuture<DataChangedEvent> settableFuture = SettableFuture.create();
        REPOSITORY.watch(key, settableFuture::set);
        DataChangedEvent dataChangedEvent = settableFuture.get();
        assertThat(dataChangedEvent.getType(), is(DataChangedEvent.Type.UPDATED));
        assertThat(dataChangedEvent.getKey(), is(key));
        assertThat(dataChangedEvent.getValue(), is("value2"));
    }
    
    @Test
    @SneakyThrows
    public void assertWatchDelete() {
        RegisterMetadata.PERSISTENT.setListener(null);
        String key = "key/key";
        Instance preInstance = new Instance();
        preInstance.setMetadata(Collections.singletonMap(key, "value1"));
        Event event = new NamingEvent(RegisterMetadata.PERSISTENT.name(), Collections.emptyList());
        doAnswer(AdditionalAnswers.answerVoid(getListenerAnswer(preInstance, event))).when(client).subscribe(anyString(), any(EventListener.class));
        SettableFuture<DataChangedEvent> settableFuture = SettableFuture.create();
        REPOSITORY.watch(key, settableFuture::set);
        DataChangedEvent dataChangedEvent = settableFuture.get();
        assertThat(dataChangedEvent.getType(), is(DataChangedEvent.Type.DELETED));
        assertThat(dataChangedEvent.getKey(), is(key));
        assertThat(dataChangedEvent.getValue(), is("value1"));
    }
    
    @Test
    @SneakyThrows
    public void assertClose() {
        REPOSITORY.close();
        verify(client).shutDown();
    }
    
    @Test(expected = NacosException.class)
    @SneakyThrows
    public void assertPersistNotAvailable() {
        try {
            REPOSITORY.persist("/test/children/keys/persistent/1", "value4");
        } catch (ClusterPersistRepositoryException cause) {
            throw cause.getCause();
        }
    }
    
    @Test(expected = IllegalStateException.class)
    @SneakyThrows
    public void assertDuplicateClusterIp() {
        Instance instance = new Instance();
        instance.setIp(NacosPropertyKey.CLUSTER_IP.getDefaultValue());
        Map<String, String> metadataMap = new HashMap<>();
        metadataMap.put(MetadataUtil.UUID_NAME, UUID.randomUUID().toString());
        instance.setMetadata(metadataMap);
        List<Instance> instances = new LinkedList<>();
        instances.add(instance);
        NamingEvent event = new NamingEvent(NacosPropertyKey.CLUSTER_IP.name(), instances);
        doAnswer(AdditionalAnswers.answerVoid((VoidAnswer2<String, EventListener>) (serviceName, listener) -> listener.onEvent(event)))
                .when(client).subscribe(anyString(), any(EventListener.class));
        MemberAccessor accessor = Plugins.getMemberAccessor();
        accessor.set(REPOSITORY.getClass().getDeclaredField("isDuplicated"), REPOSITORY, null);
        accessor.invoke(REPOSITORY.getClass().getDeclaredMethod("initRegisterMetadata"), REPOSITORY);
        instance = new Instance();
        instance.setIp(NacosPropertyKey.CLUSTER_IP.getDefaultValue());
        metadataMap = new HashMap<>();
        metadataMap.put(MetadataUtil.UUID_NAME, UUID.randomUUID().toString());
        instance.setMetadata(metadataMap);
        instances = new LinkedList<>();
        instances.add(instance);
        try {
            REPOSITORY.persist("/key", "value");
        } catch (ClusterPersistRepositoryException cause) {
            throw cause.getCause();
        }
    }
    
    @Test(expected = IllegalStateException.class)
    @SneakyThrows
    public void assertExceededMaximum() {
        RegisterMetadata.EPHEMERAL.setPort(new AtomicInteger(Integer.MAX_VALUE));
        try {
            REPOSITORY.persistEphemeral("/key2", "value");
        } catch (ClusterPersistRepositoryException cause) {
            throw cause.getCause();
        }
    }
    
    private VoidAnswer2<String, EventListener> getListenerAnswer(final Instance preInstance, final Event event) {
        return (serviceName, listener) -> {
            MemberAccessor accessor = Plugins.getMemberAccessor();
            if (Objects.nonNull(preInstance)) {
                Map<String, Instance> preInstances = new HashMap<>();
                preInstances.put(MetadataUtil.getKey(preInstance), preInstance);
                accessor.set(listener.getClass().getDeclaredField("preInstances"), listener, preInstances);
            }
            listener.onEvent(event);
        };
    }
    
    private VoidAnswer2<String, Instance> getRegisterInstanceAnswer() {
        return (serviceName, instance) -> {
            List<Instance> instances = client.getAllInstances(serviceName, false);
            instances.removeIf(each -> StringUtils.equals(each.getIp(), instance.getIp()) && each.getPort() == instance.getPort());
            instances.add(instance);
            when(client.getAllInstances(serviceName, false)).thenReturn(instances);
        };
    }
    
    private VoidAnswer2<String, Instance> getDeregisterInstanceAnswer() {
        return (serviceName, instance) -> {
            List<Instance> instances = client.getAllInstances(serviceName, false);
            instances.remove(instance);
            when(client.getAllInstances(serviceName, false)).thenReturn(instances);
        };
    }
    
}
