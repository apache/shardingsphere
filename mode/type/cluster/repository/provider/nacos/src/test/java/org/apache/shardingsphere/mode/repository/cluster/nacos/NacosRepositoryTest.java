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
import org.apache.shardingsphere.mode.repository.cluster.exception.ClusterPersistRepositoryException;
import org.apache.shardingsphere.mode.event.DataChangedEvent;
import org.apache.shardingsphere.mode.repository.cluster.nacos.entity.ServiceController;
import org.apache.shardingsphere.mode.repository.cluster.nacos.entity.ServiceMetaData;
import org.apache.shardingsphere.mode.repository.cluster.nacos.props.NacosProperties;
import org.apache.shardingsphere.mode.repository.cluster.nacos.props.NacosPropertyKey;
import org.apache.shardingsphere.mode.repository.cluster.nacos.util.NacosMetaDataUtils;
import org.apache.shardingsphere.mode.spi.PersistRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.AdditionalAnswers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.internal.configuration.plugins.Plugins;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.plugins.MemberAccessor;
import org.mockito.stubbing.VoidAnswer2;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NacosRepositoryTest {
    
    private static final NacosRepository REPOSITORY = new NacosRepository();
    
    @Mock
    private NamingService client;
    
    private ServiceController serviceController;
    
    @BeforeEach
    void initClient() throws ReflectiveOperationException {
        MemberAccessor accessor = Plugins.getMemberAccessor();
        accessor.set(REPOSITORY.getClass().getDeclaredField("nacosProps"), REPOSITORY, new NacosProperties(new Properties()));
        accessor.set(REPOSITORY.getClass().getDeclaredField("client"), REPOSITORY, client);
        accessor.invoke(REPOSITORY.getClass().getDeclaredMethod("initServiceMetaData"), REPOSITORY);
        serviceController = (ServiceController) accessor.get(REPOSITORY.getClass().getDeclaredField("serviceController"), REPOSITORY);
    }
    
    @Test
    void assertGetLatestKey() throws NacosException {
        int total = 2;
        String key = "/test/children/keys/persistent/1";
        List<Instance> instances = new LinkedList<>();
        for (int count = 1; count <= total; count++) {
            Instance instance = new Instance();
            Map<String, String> metaDataMap = new HashMap<>(2, 1F);
            metaDataMap.put(key, "value" + count);
            metaDataMap.put(NacosMetaDataUtils.UTC_ZONE_OFFSET.toString(), String.valueOf(count));
            instance.setMetadata(metaDataMap);
            instances.add(instance);
        }
        ServiceMetaData persistentService = serviceController.getPersistentService();
        when(client.getAllInstances(persistentService.getServiceName(), false)).thenReturn(instances);
        String value = REPOSITORY.getDirectly(key);
        assertThat(value, is("value2"));
    }
    
    @Test
    void assertGetChildrenKeys() throws NacosException {
        Instance instance = new Instance();
        String key = "/test/children/keys/persistent/0";
        instance.setMetadata(Collections.singletonMap(key, "value0"));
        ServiceMetaData persistentService = serviceController.getPersistentService();
        when(client.getAllInstances(persistentService.getServiceName(), false)).thenReturn(Collections.singletonList(instance));
        instance = new Instance();
        key = "/test/children/keys/ephemeral/0";
        instance.setMetadata(Collections.singletonMap(key, "value0"));
        ServiceMetaData ephemeralService = serviceController.getEphemeralService();
        when(client.getAllInstances(ephemeralService.getServiceName(), false)).thenReturn(Collections.singletonList(instance));
        List<String> childrenKeys = REPOSITORY.getChildrenKeys("/test/children/keys");
        assertThat(childrenKeys.size(), is(2));
        assertThat(childrenKeys.get(0), is("persistent"));
        assertThat(childrenKeys.get(1), is("ephemeral"));
    }
    
    @Test
    void assertPersistNotExistKey() throws NacosException {
        String key = "/test/children/keys/persistent/1";
        doAnswer(AdditionalAnswers.answerVoid(getRegisterInstanceAnswer())).when(client).registerInstance(anyString(), any(Instance.class));
        REPOSITORY.persist(key, "value4");
        ArgumentCaptor<Instance> instanceArgumentCaptor = ArgumentCaptor.forClass(Instance.class);
        ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
        verify(client, times(5)).registerInstance(stringArgumentCaptor.capture(), instanceArgumentCaptor.capture());
        Instance registerInstance = instanceArgumentCaptor.getValue();
        String registerType = stringArgumentCaptor.getValue();
        ServiceMetaData persistentService = serviceController.getPersistentService();
        assertThat(registerType, is(persistentService.getServiceName()));
        assertThat(registerInstance.isEphemeral(), is(false));
        assertThat(NacosMetaDataUtils.getValue(registerInstance), is("value4"));
    }
    
    @Test
    void assertPersistExistKey() throws NacosException {
        String ip = "127.0.0.1";
        Instance instance = new Instance();
        instance.setIp(ip);
        instance.setEphemeral(false);
        String key = "/test/children/keys/persistent/0";
        instance.setMetadata(new HashMap<>(Collections.singletonMap(key, "value0")));
        List<Instance> instances = new LinkedList<>();
        buildParentPath(key, instances);
        instances.add(instance);
        ServiceMetaData persistentService = serviceController.getPersistentService();
        when(client.getAllInstances(persistentService.getServiceName(), false)).thenReturn(instances);
        doAnswer(AdditionalAnswers.answerVoid(getRegisterInstanceAnswer())).when(client).registerInstance(anyString(), any(Instance.class));
        REPOSITORY.persist(key, "value4");
        ArgumentCaptor<Instance> instanceArgumentCaptor = ArgumentCaptor.forClass(Instance.class);
        ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).registerInstance(stringArgumentCaptor.capture(), instanceArgumentCaptor.capture());
        Instance registerInstance = instanceArgumentCaptor.getValue();
        String registerType = stringArgumentCaptor.getValue();
        assertThat(registerType, is(persistentService.getServiceName()));
        assertThat(registerInstance.getIp(), is(ip));
        assertThat(registerInstance.isEphemeral(), is(false));
        assertThat(NacosMetaDataUtils.getValue(registerInstance), is("value4"));
    }
    
    @Test
    void assertPersistEphemeralExistKey() throws NacosException {
        final String key = "/test/children/keys/ephemeral/1";
        final Instance instance = new Instance();
        instance.setEphemeral(true);
        Map<String, String> metaDataMap = new HashMap<>(4, 1F);
        metaDataMap.put(PreservedMetadataKeys.HEART_BEAT_INTERVAL, String.valueOf(2000));
        metaDataMap.put(PreservedMetadataKeys.HEART_BEAT_TIMEOUT, String.valueOf(4000));
        metaDataMap.put(PreservedMetadataKeys.IP_DELETE_TIMEOUT, String.valueOf(6000));
        metaDataMap.put(key, "value0");
        instance.setMetadata(metaDataMap);
        List<Instance> instances = new LinkedList<>();
        buildParentPath(key, instances);
        ServiceMetaData persistentService = serviceController.getPersistentService();
        when(client.getAllInstances(persistentService.getServiceName(), false)).thenReturn(instances);
        instances = new LinkedList<>();
        instances.add(instance);
        ServiceMetaData ephemeralService = serviceController.getEphemeralService();
        when(client.getAllInstances(ephemeralService.getServiceName(), false)).thenReturn(instances);
        doAnswer(AdditionalAnswers.answerVoid(getDeregisterInstanceAnswer())).when(client).deregisterInstance(anyString(), any(Instance.class));
        doAnswer(AdditionalAnswers.answerVoid(getRegisterInstanceAnswer())).when(client).registerInstance(anyString(), any(Instance.class));
        REPOSITORY.persistEphemeral(key, "value4");
        ArgumentCaptor<Instance> instanceArgumentCaptor = ArgumentCaptor.forClass(Instance.class);
        ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).deregisterInstance(anyString(), any(Instance.class));
        verify(client).registerInstance(stringArgumentCaptor.capture(), instanceArgumentCaptor.capture());
        Instance registerInstance = instanceArgumentCaptor.getValue();
        String registerType = stringArgumentCaptor.getValue();
        assertThat(registerType, is(ephemeralService.getServiceName()));
        assertThat(registerInstance.isEphemeral(), is(true));
        assertThat(NacosMetaDataUtils.getValue(registerInstance), is("value4"));
        Map<String, String> metaData = registerInstance.getMetadata();
        long timeToLiveSeconds = Long.parseLong(NacosPropertyKey.TIME_TO_LIVE_SECONDS.getDefaultValue());
        assertThat(metaData.get(PreservedMetadataKeys.HEART_BEAT_INTERVAL), is(String.valueOf(timeToLiveSeconds * 1000 / 3)));
        assertThat(metaData.get(PreservedMetadataKeys.HEART_BEAT_TIMEOUT), is(String.valueOf(timeToLiveSeconds * 1000 * 2 / 3)));
        assertThat(metaData.get(PreservedMetadataKeys.IP_DELETE_TIMEOUT), is(String.valueOf(timeToLiveSeconds * 1000)));
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
    void assertPersistEphemeralNotExistKey() throws NacosException {
        String key = "/test/children/keys/ephemeral/0";
        doAnswer(AdditionalAnswers.answerVoid(getRegisterInstanceAnswer())).when(client).registerInstance(anyString(), any(Instance.class));
        REPOSITORY.persistEphemeral(key, "value0");
        ArgumentCaptor<Instance> instanceArgumentCaptor = ArgumentCaptor.forClass(Instance.class);
        ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
        verify(client, times(5)).registerInstance(stringArgumentCaptor.capture(), instanceArgumentCaptor.capture());
        Instance registerInstance = instanceArgumentCaptor.getValue();
        String registerType = stringArgumentCaptor.getValue();
        ServiceMetaData ephemeralService = serviceController.getEphemeralService();
        assertThat(registerType, is(ephemeralService.getServiceName()));
        assertThat(registerInstance.isEphemeral(), is(true));
        assertThat(NacosMetaDataUtils.getValue(registerInstance), is("value0"));
        Map<String, String> metaData = registerInstance.getMetadata();
        long timeToLiveSeconds = Long.parseLong(NacosPropertyKey.TIME_TO_LIVE_SECONDS.getDefaultValue());
        assertThat(metaData.get(PreservedMetadataKeys.HEART_BEAT_INTERVAL), is(String.valueOf(timeToLiveSeconds * 1000 / 3)));
        assertThat(metaData.get(PreservedMetadataKeys.HEART_BEAT_TIMEOUT), is(String.valueOf(timeToLiveSeconds * 1000 * 2 / 3)));
        assertThat(metaData.get(PreservedMetadataKeys.IP_DELETE_TIMEOUT), is(String.valueOf(timeToLiveSeconds * 1000)));
    }
    
    @Test
    void assertDeleteExistKey() throws NacosException {
        int total = 3;
        List<Instance> instances = new LinkedList<>();
        for (int count = 1; count <= total; count++) {
            String key = "/test/children/keys/ephemeral/" + count;
            Instance instance = new Instance();
            instance.setEphemeral(true);
            instance.setMetadata(Collections.singletonMap(key, "value" + count));
            instances.add(instance);
        }
        ServiceMetaData ephemeralService = serviceController.getEphemeralService();
        when(client.getAllInstances(ephemeralService.getServiceName(), false)).thenReturn(instances);
        instances = new LinkedList<>();
        String key = "/test/children/keys/persistent/0";
        Instance instance = new Instance();
        instance.setEphemeral(false);
        instance.setMetadata(Collections.singletonMap(key, "value0"));
        instances.add(instance);
        ServiceMetaData persistentService = serviceController.getPersistentService();
        when(client.getAllInstances(persistentService.getServiceName(), false)).thenReturn(instances);
        doAnswer(AdditionalAnswers.answerVoid(getDeregisterInstanceAnswer())).when(client).deregisterInstance(anyString(), any(Instance.class));
        REPOSITORY.delete("/test/children/keys");
        verify(client, times(4)).deregisterInstance(anyString(), any(Instance.class));
    }
    
    @Test
    void assertDeleteNotExistKey() throws NacosException {
        REPOSITORY.delete("/test/children/keys/persistent/1");
        verify(client, times(0)).deregisterInstance(anyString(), any(Instance.class));
    }
    
    @Test
    void assertWatchAdded() throws NacosException, ExecutionException, InterruptedException {
        ServiceMetaData ephemeralService = serviceController.getEphemeralService();
        ephemeralService.setListener(null);
        String key = "key/key";
        String value = "value2";
        Instance instance = new Instance();
        instance.setMetadata(Collections.singletonMap(key, value));
        Event event = new NamingEvent(ephemeralService.getServiceName(), Collections.singletonList(instance));
        doAnswer(AdditionalAnswers.answerVoid(getListenerAnswer(null, event))).when(client).subscribe(anyString(), any(EventListener.class));
        SettableFuture<DataChangedEvent> settableFuture = SettableFuture.create();
        REPOSITORY.watch(key, settableFuture::set);
        DataChangedEvent dataChangedEvent = settableFuture.get();
        assertThat(dataChangedEvent.getType(), is(DataChangedEvent.Type.ADDED));
        assertThat(dataChangedEvent.getKey(), is(key));
        assertThat(dataChangedEvent.getValue(), is(value));
    }
    
    @Test
    void assertWatchUpdate() throws NacosException, ExecutionException, InterruptedException {
        ServiceMetaData persistentService = serviceController.getPersistentService();
        persistentService.setListener(null);
        String key = "key/key";
        long epochMilliseconds = NacosMetaDataUtils.getTimestamp();
        Instance preInstance = new Instance();
        Map<String, String> metaDataMap = new HashMap<>();
        metaDataMap.put(key, "value1");
        metaDataMap.put(NacosMetaDataUtils.UTC_ZONE_OFFSET.toString(), String.valueOf(epochMilliseconds));
        preInstance.setMetadata(metaDataMap);
        final Instance instance = new Instance();
        metaDataMap = new HashMap<>();
        metaDataMap.put(key, "value2");
        metaDataMap.put(NacosMetaDataUtils.UTC_ZONE_OFFSET.toString(), String.valueOf(epochMilliseconds + 1));
        instance.setMetadata(metaDataMap);
        Event event = new NamingEvent(persistentService.getServiceName(), Collections.singletonList(instance));
        doAnswer(AdditionalAnswers.answerVoid(getListenerAnswer(preInstance, event))).when(client).subscribe(anyString(), any(EventListener.class));
        SettableFuture<DataChangedEvent> settableFuture = SettableFuture.create();
        REPOSITORY.watch(key, settableFuture::set);
        DataChangedEvent dataChangedEvent = settableFuture.get();
        assertThat(dataChangedEvent.getType(), is(DataChangedEvent.Type.UPDATED));
        assertThat(dataChangedEvent.getKey(), is(key));
        assertThat(dataChangedEvent.getValue(), is("value2"));
    }
    
    @Test
    void assertWatchDelete() throws NacosException, ExecutionException, InterruptedException {
        ServiceMetaData persistentService = serviceController.getPersistentService();
        persistentService.setListener(null);
        String key = "key/key";
        Instance preInstance = new Instance();
        preInstance.setMetadata(Collections.singletonMap(key, "value1"));
        Event event = new NamingEvent(persistentService.getServiceName(), Collections.emptyList());
        doAnswer(AdditionalAnswers.answerVoid(getListenerAnswer(preInstance, event))).when(client).subscribe(anyString(), any(EventListener.class));
        SettableFuture<DataChangedEvent> settableFuture = SettableFuture.create();
        REPOSITORY.watch(key, settableFuture::set);
        DataChangedEvent dataChangedEvent = settableFuture.get();
        assertThat(dataChangedEvent.getType(), is(DataChangedEvent.Type.DELETED));
        assertThat(dataChangedEvent.getKey(), is(key));
        assertThat(dataChangedEvent.getValue(), is("value1"));
    }
    
    @Test
    void assertClose() throws NacosException {
        REPOSITORY.close();
        verify(client).shutDown();
    }
    
    @Test
    void assertPersistNotAvailable() {
        assertThrows(ClusterPersistRepositoryException.class, () -> REPOSITORY.persist("/test/children/keys/persistent/1", "value4"));
    }
    
    @Test
    void assertExceededMaximum() {
        ServiceMetaData ephemeralService = serviceController.getEphemeralService();
        ephemeralService.setPort(new AtomicInteger(Integer.MAX_VALUE));
        assertThrows(IllegalStateException.class, () -> REPOSITORY.persistEphemeral("/key2", "value"));
    }
    
    private VoidAnswer2<String, EventListener> getListenerAnswer(final Instance preInstance, final Event event) {
        return (serviceName, listener) -> {
            MemberAccessor accessor = Plugins.getMemberAccessor();
            if (null != preInstance) {
                Map<String, Instance> preInstances = new HashMap<>();
                preInstances.put(NacosMetaDataUtils.getKey(preInstance), preInstance);
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
