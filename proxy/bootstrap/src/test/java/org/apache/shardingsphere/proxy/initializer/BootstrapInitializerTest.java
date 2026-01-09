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

package org.apache.shardingsphere.proxy.initializer;

import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.infra.instance.metadata.InstanceMetaData;
import org.apache.shardingsphere.infra.instance.metadata.InstanceMetaDataBuilder;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.util.eventbus.EventBusContext;
import org.apache.shardingsphere.infra.yaml.config.pojo.mode.YamlModeConfiguration;
import org.apache.shardingsphere.mode.manager.builder.ContextManagerBuilder;
import org.apache.shardingsphere.mode.manager.builder.ContextManagerBuilderParameter;
import org.apache.shardingsphere.proxy.backend.config.YamlProxyConfiguration;
import org.apache.shardingsphere.proxy.backend.config.yaml.YamlProxyDatabaseConfiguration;
import org.apache.shardingsphere.proxy.backend.config.yaml.YamlProxyServerConfiguration;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.version.ShardingSphereProxyVersion;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.internal.configuration.plugins.Plugins;
import org.mockito.plugins.MemberAccessor;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings({ProxyContext.class, ShardingSphereProxyVersion.class})
class BootstrapInitializerTest {
    
    private final Map<Class<?>, Object> originalRegisteredServices = new HashMap<>(2, 1F);
    
    @AfterEach
    void restoreRegisteredServices() throws ReflectiveOperationException {
        Field servicesField = ShardingSphereServiceLoader.class.getDeclaredField("REGISTERED_SERVICES");
        @SuppressWarnings("unchecked")
        Map<Class<?>, Object> registeredServices = (Map<Class<?>, Object>) Plugins.getMemberAccessor().get(servicesField, null);
        for (Entry<Class<?>, Object> entry : originalRegisteredServices.entrySet()) {
            if (null == entry.getValue()) {
                registeredServices.remove(entry.getKey());
            } else {
                registeredServices.put(entry.getKey(), entry.getValue());
            }
        }
        originalRegisteredServices.clear();
    }
    
    @Test
    void assertInitWithoutModeConfiguration() throws SQLException, ReflectiveOperationException {
        InstanceMetaDataBuilder instanceMetaDataBuilder = mock(InstanceMetaDataBuilder.class);
        InstanceMetaData instanceMetaData = mock(InstanceMetaData.class);
        registerSingletonService(InstanceMetaDataBuilder.class, instanceMetaDataBuilder);
        ContextManagerBuilder contextManagerBuilder = mock(ContextManagerBuilder.class);
        when(instanceMetaDataBuilder.getType()).thenReturn("Proxy");
        when(instanceMetaDataBuilder.build(3307, "")).thenReturn(instanceMetaData);
        when(contextManagerBuilder.isDefault()).thenReturn(true);
        registerSingletonService(ContextManagerBuilder.class, contextManagerBuilder);
        new BootstrapInitializer().init(createYamlProxyConfiguration(null), 3307);
        ArgumentCaptor<ContextManagerBuilderParameter> paramCaptor = ArgumentCaptor.forClass(ContextManagerBuilderParameter.class);
        verify(instanceMetaDataBuilder).build(3307, "");
        verify(contextManagerBuilder).build(paramCaptor.capture(), any(EventBusContext.class));
        ContextManagerBuilderParameter actualParameter = paramCaptor.getValue();
        ModeConfiguration actualModeConfig = actualParameter.getModeConfiguration();
        assertThat(actualModeConfig.getType(), is("Standalone"));
        assertThat(actualParameter.getDatabaseConfigs().size(), is(1));
        assertTrue(actualParameter.getGlobalDataSources().isEmpty());
        assertTrue(actualParameter.getGlobalRuleConfigs().isEmpty());
        assertTrue(actualParameter.getProps().isEmpty());
        assertTrue(actualParameter.getLabels().isEmpty());
        assertThat(actualParameter.getInstanceMetaData(), is(instanceMetaData));
    }
    
    @Test
    void assertInitWithModeConfiguration() throws SQLException, ReflectiveOperationException {
        YamlModeConfiguration yamlModeConfig = new YamlModeConfiguration();
        yamlModeConfig.setType("Cluster");
        InstanceMetaDataBuilder instanceMetaDataBuilder = mock(InstanceMetaDataBuilder.class);
        InstanceMetaData instanceMetaData = mock(InstanceMetaData.class);
        registerSingletonService(InstanceMetaDataBuilder.class, instanceMetaDataBuilder);
        ContextManagerBuilder contextManagerBuilder = mock(ContextManagerBuilder.class);
        when(instanceMetaDataBuilder.getType()).thenReturn("Proxy");
        when(instanceMetaDataBuilder.build(3307, "")).thenReturn(instanceMetaData);
        when(contextManagerBuilder.getType()).thenReturn("Cluster");
        registerSingletonService(ContextManagerBuilder.class, contextManagerBuilder);
        YamlProxyConfiguration yamlConfig = createYamlProxyConfiguration(yamlModeConfig);
        new BootstrapInitializer().init(yamlConfig, 3307);
        ArgumentCaptor<ContextManagerBuilderParameter> paramCaptor = ArgumentCaptor.forClass(ContextManagerBuilderParameter.class);
        verify(instanceMetaDataBuilder).build(3307, "");
        verify(contextManagerBuilder).build(paramCaptor.capture(), any(EventBusContext.class));
        ContextManagerBuilderParameter actualParameter = paramCaptor.getValue();
        assertThat(actualParameter.getModeConfiguration().getType(), is("Cluster"));
        assertThat(actualParameter.getDatabaseConfigs().size(), is(1));
        assertTrue(actualParameter.getGlobalDataSources().isEmpty());
        assertTrue(actualParameter.getGlobalRuleConfigs().isEmpty());
        assertTrue(actualParameter.getProps().isEmpty());
        assertTrue(actualParameter.getLabels().isEmpty());
        assertThat(actualParameter.getInstanceMetaData(), is(instanceMetaData));
    }
    
    private YamlProxyConfiguration createYamlProxyConfiguration(final YamlModeConfiguration modeConfig) {
        YamlProxyServerConfiguration serverConfig = new YamlProxyServerConfiguration();
        serverConfig.setMode(modeConfig);
        YamlProxyDatabaseConfiguration databaseConfig = new YamlProxyDatabaseConfiguration();
        databaseConfig.setDatabaseName("logic_db");
        return new YamlProxyConfiguration(serverConfig, Collections.singletonMap("logic_db", databaseConfig));
    }
    
    @SuppressWarnings("unchecked")
    private <T> void registerSingletonService(final Class<T> serviceClass, final T serviceInstance) throws ReflectiveOperationException {
        MemberAccessor accessor = Plugins.getMemberAccessor();
        Field servicesField = ShardingSphereServiceLoader.class.getDeclaredField("REGISTERED_SERVICES");
        Map<Class<?>, Object> registeredServices = (Map<Class<?>, Object>) accessor.get(servicesField, null);
        if (!originalRegisteredServices.containsKey(serviceClass)) {
            originalRegisteredServices.put(serviceClass, registeredServices.get(serviceClass));
        }
        Class<?> registeredServiceClass = Class.forName("org.apache.shardingsphere.infra.spi.RegisteredShardingSphereSPI");
        Constructor<?> constructor = registeredServiceClass.getDeclaredConstructor(Class.class);
        constructor.setAccessible(true);
        Object registeredService = constructor.newInstance(serviceClass);
        Field services = registeredServiceClass.getDeclaredField("services");
        services.setAccessible(true);
        services.set(registeredService, Collections.singletonList(serviceInstance));
        registeredServices.put(serviceClass, registeredService);
    }
}
