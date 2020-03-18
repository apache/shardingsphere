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
    
    private static ConfigCenterRepository configCenterRepository = new NacosCenterRepository();
    
    private ConfigService configService = mock(ConfigService.class);
    
    private String group = "SHARDING_SPHERE_DEFAULT_GROUP";
    
    @Before
    public void init() {
        Properties properties = new Properties();
        properties.setProperty("group", group);
        properties.setProperty("timeout", "3000");
        CenterConfiguration configuration = new CenterConfiguration(configCenterRepository.getType(), properties);
        configuration.setServerLists("127.0.0.1:8848");
        configCenterRepository.init(configuration);
        setConfigService(configService);
    }
    
    @SneakyThrows
    private void setConfigService(final ConfigService configService) {
        Field configServiceField = NacosCenterRepository.class.getDeclaredField("configService");
        configServiceField.setAccessible(true);
        configServiceField.set(configCenterRepository, configService);
    }
    
    @Test
    @SneakyThrows
    public void assertPersist() {
        String value = "value";
        configCenterRepository.persist("/sharding/test", value);
        verify(configService).publishConfig("sharding.test", group, value);
    }
    
    @Test
    @SneakyThrows
    public void assertGet() {
        String value = "value";
        when(configService.getConfig(eq("sharding.test"), eq(group), anyLong())).thenReturn(value);
        assertThat(configCenterRepository.get("/sharding/test"), is(value));
    }
    
    @Test
    @SneakyThrows
    public void assertWatch() {
        final String expectValue = "expectValue";
        final String[] actualValue = {null};
        doAnswer(AdditionalAnswers.answerVoid(getListenerAnswer(expectValue)))
            .when(configService)
            .addListener(anyString(), anyString(), any(Listener.class));
        DataChangedEventListener listener = dataChangedEvent -> actualValue[0] = dataChangedEvent.getValue();
        configCenterRepository.watch("/sharding/test", listener);
        assertThat(actualValue[0], is(expectValue));
    }
    
    @Test
    @SneakyThrows
    public void assertGetWithNonExistentKey() {
        assertNull(configCenterRepository.get("/sharding/nonExistentKey"));
    }
    
    @Test
    @SneakyThrows
    public void assertGetWhenThrowException() {
        doThrow(NacosException.class).when(configService).getConfig(eq("sharding.test"), eq(group), anyLong());
        assertNull(configCenterRepository.get("/sharding/test"));
    }
    
    @Test
    @SneakyThrows
    public void assertUpdate() {
        String updatedValue = "newValue";
        configCenterRepository.persist("/sharding/test", updatedValue);
        verify(configService).publishConfig("sharding.test", group, updatedValue);
    }
    
    @Test
    @SneakyThrows
    public void assertWatchUpdatedChangedType() {
        final String expectValue = "expectValue";
        final String[] actualValue = {null};
        final ChangedType[] actualType = {null};
        doAnswer(AdditionalAnswers.answerVoid(getListenerAnswer(expectValue)))
                .when(configService)
                .addListener(anyString(), anyString(), any(Listener.class));
        DataChangedEventListener listener = dataChangedEvent -> {
            actualValue[0] = dataChangedEvent.getValue();
            actualType[0] = dataChangedEvent.getChangedType();
        };
        configCenterRepository.watch("/sharding/test", listener);
        assertThat(actualValue[0], is(expectValue));
        assertThat(actualType[0], is(ChangedType.UPDATED));
    }
    
    @Test
    @SneakyThrows
    public void assertWatchDeletedChangedType() {
        final ChangedType[] actualType = {null};
        doAnswer(AdditionalAnswers.answerVoid(getListenerAnswer(null)))
                .when(configService)
                .addListener(anyString(), anyString(), any(Listener.class));
        DataChangedEventListener listener = dataChangedEvent -> actualType[0] = dataChangedEvent.getChangedType();
        configCenterRepository.watch("/sharding/test", listener);
        assertThat(actualType[0], is(ChangedType.UPDATED));
    }
    
    private VoidAnswer3 getListenerAnswer(final String expectValue) {
        return (VoidAnswer3<String, String, Listener>) (dataId, group, listener) -> listener.receiveConfigInfo(expectValue);
    }
}
