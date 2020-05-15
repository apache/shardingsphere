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

import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import lombok.SneakyThrows;
import org.apache.shardingsphere.orchestration.center.ConfigCenterRepository;
import org.apache.shardingsphere.orchestration.center.listener.DataChangedEvent.ChangedType;
import org.apache.shardingsphere.orchestration.center.listener.DataChangedEventListener;
import org.apache.shardingsphere.orchestration.center.config.CenterConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.mockito.AdditionalAnswers;
import org.mockito.stubbing.VoidAnswer3;

import java.lang.reflect.Field;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public final class NacosCenterRepositoryTest {
    
    private static final ConfigCenterRepository REPOSITORY = new NacosCenterRepository();
    
    private final ConfigService configService = mock(ConfigService.class);
    
    private final String group = "SHARDING_SPHERE_DEFAULT_GROUP";
    
    @Before
    public void init() {
        Properties properties = new Properties();
        properties.setProperty("group", group);
        properties.setProperty("timeout", "3000");
        CenterConfiguration configuration = new CenterConfiguration(REPOSITORY.getType(), properties);
        configuration.setServerLists("127.0.0.1:8848");
        REPOSITORY.init(configuration);
        setConfigService(configService);
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private void setConfigService(final ConfigService configService) {
        Field configServiceField = NacosCenterRepository.class.getDeclaredField("configService");
        configServiceField.setAccessible(true);
        configServiceField.set(REPOSITORY, configService);
    }
    
    @Test
    public void assertPersist() throws NacosException {
        String value = "value";
        REPOSITORY.persist("/sharding/test", value);
        verify(configService).publishConfig("sharding.test", group, value);
    }
    
    @Test
    public void assertGet() throws NacosException {
        String value = "value";
        when(configService.getConfig(eq("sharding.test"), eq(group), anyLong())).thenReturn(value);
        assertThat(REPOSITORY.get("/sharding/test"), is(value));
    }
    
    @Test
    public void assertWatch() throws NacosException {
        final String expectValue = "expectValue";
        final String[] actualValue = {null};
        doAnswer(AdditionalAnswers.answerVoid(getListenerAnswer(expectValue))).when(configService).addListener(anyString(), anyString(), any(Listener.class));
        DataChangedEventListener listener = dataChangedEvent -> actualValue[0] = dataChangedEvent.getValue();
        REPOSITORY.watch("/sharding/test", listener);
        assertThat(actualValue[0], is(expectValue));
    }
    
    @Test
    public void assertGetWithNonExistentKey() {
        assertNull(REPOSITORY.get("/sharding/nonExistentKey"));
    }
    
    @Test
    public void assertGetWhenThrowException() throws NacosException {
        doThrow(NacosException.class).when(configService).getConfig(eq("sharding.test"), eq(group), anyLong());
        assertNull(REPOSITORY.get("/sharding/test"));
    }
    
    @Test
    public void assertUpdate() throws NacosException {
        String updatedValue = "newValue";
        REPOSITORY.persist("/sharding/test", updatedValue);
        verify(configService).publishConfig("sharding.test", group, updatedValue);
    }
    
    @Test
    public void assertWatchUpdatedChangedType() throws NacosException {
        final String expectValue = "expectValue";
        final String[] actualValue = {null};
        final ChangedType[] actualType = {null};
        doAnswer(AdditionalAnswers.answerVoid(getListenerAnswer(expectValue))).when(configService).addListener(anyString(), anyString(), any(Listener.class));
        DataChangedEventListener listener = dataChangedEvent -> {
            actualValue[0] = dataChangedEvent.getValue();
            actualType[0] = dataChangedEvent.getChangedType();
        };
        REPOSITORY.watch("/sharding/test", listener);
        assertThat(actualValue[0], is(expectValue));
        assertThat(actualType[0], is(ChangedType.UPDATED));
    }
    
    @Test
    public void assertWatchDeletedChangedType() throws NacosException {
        final ChangedType[] actualType = {null};
        doAnswer(AdditionalAnswers.answerVoid(getListenerAnswer(null))).when(configService).addListener(anyString(), anyString(), any(Listener.class));
        DataChangedEventListener listener = dataChangedEvent -> actualType[0] = dataChangedEvent.getChangedType();
        REPOSITORY.watch("/sharding/test", listener);
        assertThat(actualType[0], is(ChangedType.UPDATED));
    }
    
    @Test
    public void assertDelete() throws NacosException {
        REPOSITORY.delete("/sharding/test");
        verify(configService).removeConfig("sharding.test", group);
    }
    
    @SneakyThrows
    @Test
    public void assertDeleteWhenThrowException() {
        when(configService.getConfig(eq("sharding.test"), eq(group), anyLong())).thenReturn("value");
        doThrow(NacosException.class).when(configService).removeConfig(eq("sharding.test"), eq(group));
        REPOSITORY.delete("/sharding/test");
        assertNotNull(REPOSITORY.get("/sharding/test"));
    }
    
    @SneakyThrows
    @Test
    public void assertWatchWhenThrowException() {
        final ChangedType[] actualType = {null};
        doThrow(NacosException.class)
                .when(configService)
                .addListener(anyString(), anyString(), any(Listener.class));
        DataChangedEventListener listener = dataChangedEvent -> actualType[0] = dataChangedEvent.getChangedType();
        REPOSITORY.watch("/sharding/test", listener);
        assertNull(actualType[0]);
    }
    
    @Test
    @SneakyThrows
    public void assertPersistWhenThrowException() {
        String value = "value";
        doThrow(NacosException.class).when(configService).publishConfig(eq("sharding.test"), eq(group), eq(value));
        REPOSITORY.persist("/sharding/test", value);
        assertNull(REPOSITORY.get("/sharding/test"));
    }
    
    @Test
    public void assertProperties() {
        Properties properties = new Properties();
        REPOSITORY.setProperties(properties);
        assertThat(REPOSITORY.getProperties(), is(properties));
    }
    
    @Test
    public void assertGetChildrenKeys() {
        assertNull(REPOSITORY.getChildrenKeys("/sharding/test"));
    }
    
    private VoidAnswer3 getListenerAnswer(final String expectValue) {
        return (VoidAnswer3<String, String, Listener>) (dataId, group, listener) -> listener.receiveConfigInfo(expectValue);
    }
}
