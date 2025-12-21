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
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.util.eventbus.EventBusContext;
import org.apache.shardingsphere.infra.yaml.config.pojo.mode.YamlModeConfiguration;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.builder.ContextManagerBuilder;
import org.apache.shardingsphere.mode.manager.builder.ContextManagerBuilderParameter;
import org.apache.shardingsphere.proxy.backend.config.YamlProxyConfiguration;
import org.apache.shardingsphere.proxy.backend.config.yaml.YamlProxyDatabaseConfiguration;
import org.apache.shardingsphere.proxy.backend.config.yaml.YamlProxyServerConfiguration;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.version.ShardingSphereProxyVersion;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

import java.sql.SQLException;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BootstrapInitializerTest {
    
    @Test
    void assertInitWithoutModeConfiguration() throws SQLException {
        InstanceMetaDataBuilder instanceMetaDataBuilder = mock(InstanceMetaDataBuilder.class);
        InstanceMetaData instanceMetaData = mock(InstanceMetaData.class);
        when(instanceMetaDataBuilder.build(3307, "")).thenReturn(instanceMetaData);
        ContextManagerBuilder contextManagerBuilder = mock(ContextManagerBuilder.class);
        ContextManager contextManager = mock(ContextManager.class);
        ArgumentCaptor<ContextManagerBuilderParameter> paramCaptor = ArgumentCaptor.forClass(ContextManagerBuilderParameter.class);
        when(contextManagerBuilder.build(any(ContextManagerBuilderParameter.class), any(EventBusContext.class))).thenReturn(contextManager);
        try (
                MockedStatic<TypedSPILoader> typedSPILoader = mockStatic(TypedSPILoader.class);
                MockedStatic<ProxyContext> proxyContext = mockStatic(ProxyContext.class);
                MockedStatic<ShardingSphereProxyVersion> proxyVersion = mockStatic(ShardingSphereProxyVersion.class)) {
            typedSPILoader.when(() -> TypedSPILoader.getService(InstanceMetaDataBuilder.class, "Proxy")).thenReturn(instanceMetaDataBuilder);
            typedSPILoader.when(() -> TypedSPILoader.getService(ContextManagerBuilder.class, null)).thenReturn(contextManagerBuilder);
            new BootstrapInitializer().init(createYamlProxyConfiguration(null), 3307);
            typedSPILoader.verify(() -> TypedSPILoader.getService(InstanceMetaDataBuilder.class, "Proxy"));
            typedSPILoader.verify(() -> TypedSPILoader.getService(ContextManagerBuilder.class, null));
            verify(instanceMetaDataBuilder).build(3307, "");
            verify(contextManagerBuilder).build(paramCaptor.capture(), any(EventBusContext.class));
            proxyContext.verify(() -> ProxyContext.init(contextManager));
            proxyVersion.verify(() -> ShardingSphereProxyVersion.setVersion(contextManager));
            ContextManagerBuilderParameter actualParameter = paramCaptor.getValue();
            ModeConfiguration actualModeConfiguration = actualParameter.getModeConfiguration();
            String actualModeType = actualModeConfiguration.getType();
            String expectedModeType = "Standalone";
            assertThat(actualModeType, is(expectedModeType));
            int expectedDatabaseConfigCount = 1;
            int actualDatabaseConfigCount = actualParameter.getDatabaseConfigs().size();
            assertThat(actualDatabaseConfigCount, is(expectedDatabaseConfigCount));
            assertTrue(actualParameter.getGlobalDataSources().isEmpty());
            assertTrue(actualParameter.getGlobalRuleConfigs().isEmpty());
            assertTrue(actualParameter.getProps().isEmpty());
            assertTrue(actualParameter.getLabels().isEmpty());
            InstanceMetaData actualInstanceMetaData = actualParameter.getInstanceMetaData();
            assertThat(actualInstanceMetaData, is(instanceMetaData));
        }
    }
    
    @Test
    void assertInitWithModeConfiguration() throws SQLException {
        YamlModeConfiguration yamlModeConfig = new YamlModeConfiguration();
        yamlModeConfig.setType("Cluster");
        YamlProxyConfiguration yamlConfig = createYamlProxyConfiguration(yamlModeConfig);
        InstanceMetaDataBuilder instanceMetaDataBuilder = mock(InstanceMetaDataBuilder.class);
        InstanceMetaData instanceMetaData = mock(InstanceMetaData.class);
        when(instanceMetaDataBuilder.build(3307, "")).thenReturn(instanceMetaData);
        ContextManagerBuilder contextManagerBuilder = mock(ContextManagerBuilder.class);
        ContextManager contextManager = mock(ContextManager.class);
        ArgumentCaptor<ContextManagerBuilderParameter> paramCaptor = ArgumentCaptor.forClass(ContextManagerBuilderParameter.class);
        when(contextManagerBuilder.build(any(ContextManagerBuilderParameter.class), any(EventBusContext.class))).thenReturn(contextManager);
        try (
                MockedStatic<TypedSPILoader> typedSPILoader = mockStatic(TypedSPILoader.class);
                MockedStatic<ProxyContext> proxyContext = mockStatic(ProxyContext.class);
                MockedStatic<ShardingSphereProxyVersion> proxyVersion = mockStatic(ShardingSphereProxyVersion.class)) {
            typedSPILoader.when(() -> TypedSPILoader.getService(InstanceMetaDataBuilder.class, "Proxy")).thenReturn(instanceMetaDataBuilder);
            typedSPILoader.when(() -> TypedSPILoader.getService(ContextManagerBuilder.class, "Cluster")).thenReturn(contextManagerBuilder);
            new BootstrapInitializer().init(yamlConfig, 3307);
            typedSPILoader.verify(() -> TypedSPILoader.getService(InstanceMetaDataBuilder.class, "Proxy"));
            typedSPILoader.verify(() -> TypedSPILoader.getService(ContextManagerBuilder.class, "Cluster"));
            verify(instanceMetaDataBuilder).build(3307, "");
            verify(contextManagerBuilder).build(paramCaptor.capture(), any(EventBusContext.class));
            proxyContext.verify(() -> ProxyContext.init(contextManager));
            proxyVersion.verify(() -> ShardingSphereProxyVersion.setVersion(contextManager));
            ContextManagerBuilderParameter actualParameter = paramCaptor.getValue();
            ModeConfiguration actualModeConfiguration = actualParameter.getModeConfiguration();
            String actualModeType = actualModeConfiguration.getType();
            assertThat(actualModeType, is("Cluster"));
            int expectedDatabaseConfigCount = 1;
            int actualDatabaseConfigCount = actualParameter.getDatabaseConfigs().size();
            assertThat(actualDatabaseConfigCount, is(expectedDatabaseConfigCount));
            assertTrue(actualParameter.getGlobalDataSources().isEmpty());
            assertTrue(actualParameter.getGlobalRuleConfigs().isEmpty());
            assertTrue(actualParameter.getProps().isEmpty());
            assertTrue(actualParameter.getLabels().isEmpty());
            InstanceMetaData actualInstanceMetaData = actualParameter.getInstanceMetaData();
            assertThat(actualInstanceMetaData, is(instanceMetaData));
        }
    }
    
    private YamlProxyConfiguration createYamlProxyConfiguration(final YamlModeConfiguration modeConfig) {
        YamlProxyServerConfiguration serverConfig = new YamlProxyServerConfiguration();
        serverConfig.setMode(modeConfig);
        YamlProxyDatabaseConfiguration databaseConfig = new YamlProxyDatabaseConfiguration();
        databaseConfig.setDatabaseName("logic_db");
        return new YamlProxyConfiguration(serverConfig, Collections.singletonMap("logic_db", databaseConfig));
    }
}
