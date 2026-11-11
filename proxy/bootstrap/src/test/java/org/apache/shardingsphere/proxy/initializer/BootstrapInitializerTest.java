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

import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.config.database.impl.DataSourceGeneratedDatabaseConfiguration;
import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.infra.config.props.temporary.TemporaryConfigurationPropertyKey;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.datasource.pool.config.ConnectionConfiguration;
import org.apache.shardingsphere.infra.datasource.pool.config.DataSourceConfiguration;
import org.apache.shardingsphere.infra.datasource.pool.config.PoolConfiguration;
import org.apache.shardingsphere.infra.datasource.pool.creator.DataSourcePoolCreator;
import org.apache.shardingsphere.infra.instance.metadata.InstanceMetaData;
import org.apache.shardingsphere.infra.instance.metadata.InstanceMetaDataBuilder;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.util.eventbus.EventBusContext;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder.Property;
import org.apache.shardingsphere.mode.manager.builder.ContextManagerBuilder;
import org.apache.shardingsphere.mode.manager.builder.ContextManagerBuilderParameter;
import org.apache.shardingsphere.proxy.backend.config.ProxyConfigurationLoadResult;
import org.apache.shardingsphere.proxy.backend.config.ProxyDatabaseConfiguration;
import org.apache.shardingsphere.proxy.backend.context.BackendExecutorContext;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.version.ShardingSphereProxyVersion;
import org.apache.shardingsphere.test.infra.fixture.jdbc.MockedDataSource;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.internal.configuration.plugins.Plugins;
import org.mockito.plugins.MemberAccessor;

import javax.sql.DataSource;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings({BackendExecutorContext.class, ProxyContext.class, ShardingSphereProxyVersion.class})
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
    void assertInitWithoutModeConfiguration() throws SQLException {
        InstanceMetaDataBuilder instanceMetaDataBuilder = mock(InstanceMetaDataBuilder.class);
        InstanceMetaData instanceMetaData = mock(InstanceMetaData.class);
        registerSingletonService(InstanceMetaDataBuilder.class, instanceMetaDataBuilder);
        ContextManagerBuilder contextManagerBuilder = mock(ContextManagerBuilder.class);
        when(instanceMetaDataBuilder.getType()).thenReturn("Proxy");
        when(instanceMetaDataBuilder.build(3307, "")).thenReturn(instanceMetaData);
        when(contextManagerBuilder.isDefault()).thenReturn(true);
        registerSingletonService(ContextManagerBuilder.class, contextManagerBuilder);
        new BootstrapInitializer().init(createProxyConfigurationLoadResult(null), 3307);
        ArgumentCaptor<ContextManagerBuilderParameter> paramCaptor = ArgumentCaptor.forClass(ContextManagerBuilderParameter.class);
        verify(instanceMetaDataBuilder).build(3307, "");
        verify(contextManagerBuilder).build(paramCaptor.capture(), any(EventBusContext.class));
        verify(BackendExecutorContext.getInstance()).init();
        ContextManagerBuilderParameter actualParameter = paramCaptor.getValue();
        ModeConfiguration actualModeConfig = actualParameter.getModeConfiguration();
        assertThat(actualModeConfig.getType(), is("Standalone"));
        assertThat(actualParameter.getDatabaseConfigs().size(), is(1));
        assertTrue(actualParameter.getGlobalDataSources().isEmpty());
        assertTrue(actualParameter.getGlobalRuleConfigs().isEmpty());
        assertTrue(actualParameter.getProps().isEmpty());
        assertThat(actualParameter.getInstanceMetaData(), is(instanceMetaData));
    }
    
    @Test
    void assertInitWithModeConfiguration() throws SQLException {
        ModeConfiguration modeConfig = new ModeConfiguration("Cluster", null);
        InstanceMetaDataBuilder instanceMetaDataBuilder = mock(InstanceMetaDataBuilder.class);
        InstanceMetaData instanceMetaData = mock(InstanceMetaData.class);
        registerSingletonService(InstanceMetaDataBuilder.class, instanceMetaDataBuilder);
        ContextManagerBuilder contextManagerBuilder = mock(ContextManagerBuilder.class);
        when(instanceMetaDataBuilder.getType()).thenReturn("Proxy");
        when(instanceMetaDataBuilder.build(3307, "")).thenReturn(instanceMetaData);
        when(contextManagerBuilder.getType()).thenReturn("Cluster");
        registerSingletonService(ContextManagerBuilder.class, contextManagerBuilder);
        ProxyConfigurationLoadResult loadResult = createProxyConfigurationLoadResult(modeConfig);
        new BootstrapInitializer().init(loadResult, 3307);
        ArgumentCaptor<ContextManagerBuilderParameter> paramCaptor = ArgumentCaptor.forClass(ContextManagerBuilderParameter.class);
        verify(instanceMetaDataBuilder).build(3307, "");
        verify(contextManagerBuilder).build(paramCaptor.capture(), any(EventBusContext.class));
        verify(BackendExecutorContext.getInstance()).init();
        ContextManagerBuilderParameter actualParameter = paramCaptor.getValue();
        assertThat(actualParameter.getModeConfiguration().getType(), is("Cluster"));
        assertThat(actualParameter.getDatabaseConfigs().size(), is(1));
        assertTrue(actualParameter.getGlobalDataSources().isEmpty());
        assertTrue(actualParameter.getGlobalRuleConfigs().isEmpty());
        assertTrue(actualParameter.getProps().isEmpty());
        assertThat(actualParameter.getInstanceMetaData(), is(instanceMetaData));
    }
    
    @Test
    void assertInitWithRepeatedBootstrap() throws SQLException {
        InstanceMetaDataBuilder instanceMetaDataBuilder = mock(InstanceMetaDataBuilder.class);
        InstanceMetaData instanceMetaData = mock(InstanceMetaData.class);
        registerSingletonService(InstanceMetaDataBuilder.class, instanceMetaDataBuilder);
        ContextManagerBuilder contextManagerBuilder = mock(ContextManagerBuilder.class);
        when(instanceMetaDataBuilder.getType()).thenReturn("Proxy");
        when(instanceMetaDataBuilder.build(3307, "")).thenReturn(instanceMetaData);
        when(contextManagerBuilder.isDefault()).thenReturn(true);
        registerSingletonService(ContextManagerBuilder.class, contextManagerBuilder);
        BootstrapInitializer bootstrapInitializer = new BootstrapInitializer();
        bootstrapInitializer.init(createProxyConfigurationLoadResult(null), 3307);
        BackendExecutorContext.getInstance().shutdown();
        bootstrapInitializer.init(createProxyConfigurationLoadResult(null), 3307);
        verify(BackendExecutorContext.getInstance(), times(2)).init();
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("dataSourceCreationArguments")
    void assertInitWithDataSources(final String name, final Properties props, final boolean expectedInstanceConnectionEnabled) throws SQLException {
        InstanceMetaDataBuilder instanceMetaDataBuilder = mock(InstanceMetaDataBuilder.class);
        registerSingletonService(InstanceMetaDataBuilder.class, instanceMetaDataBuilder);
        ContextManagerBuilder contextManagerBuilder = mock(ContextManagerBuilder.class);
        when(instanceMetaDataBuilder.getType()).thenReturn("Proxy");
        when(contextManagerBuilder.isDefault()).thenReturn(true);
        registerSingletonService(ContextManagerBuilder.class, contextManagerBuilder);
        Map<String, DataSourceConfiguration> databaseDataSources = Collections.singletonMap("foo_ds", mock(DataSourceConfiguration.class));
        Collection<RuleConfiguration> databaseRules = Collections.singleton(mock(RuleConfiguration.class));
        Collection<RuleConfiguration> globalRules = Collections.singleton(mock(RuleConfiguration.class));
        Map<String, DataSource> globalDataSources = Collections.singletonMap("global_ds", new MockedDataSource());
        DataSourceConfiguration globalDataSourceConfig = new DataSourceConfiguration(
                new ConnectionConfiguration(MockedDataSource.class.getName(), null, "jdbc:mock://127.0.0.1/global", "root", ""),
                new PoolConfiguration(30000L, 60000L, 1800000L, 5, 1, false, new Properties()));
        ProxyConfigurationLoadResult loadResult = new ProxyConfigurationLoadResult(null, Collections.singletonMap("global_ds", globalDataSourceConfig), globalRules, props,
                Collections.singletonMap("foo_db", new ProxyDatabaseConfiguration("foo_db", databaseDataSources, databaseRules)));
        try (
                MockedStatic<DataSourcePoolCreator> mockedPoolCreator = mockStatic(DataSourcePoolCreator.class);
                MockedConstruction<DataSourceGeneratedDatabaseConfiguration> mockedDatabaseConfigs = mockConstruction(DataSourceGeneratedDatabaseConfiguration.class, (mock, context) -> {
                    assertThat(context.arguments().get(0), sameInstance(databaseDataSources));
                    assertThat(context.arguments().get(1), sameInstance(databaseRules));
                    assertThat(context.arguments().get(2), is(expectedInstanceConnectionEnabled));
                })) {
            mockedPoolCreator.when(() -> DataSourcePoolCreator.create(anyMap(), eq(true))).thenReturn(globalDataSources);
            new BootstrapInitializer().init(loadResult, 3307);
            ArgumentCaptor<ContextManagerBuilderParameter> paramCaptor = ArgumentCaptor.forClass(ContextManagerBuilderParameter.class);
            verify(contextManagerBuilder).build(paramCaptor.capture(), any(EventBusContext.class));
            ContextManagerBuilderParameter actual = paramCaptor.getValue();
            assertThat(actual.getDatabaseConfigs().get("foo_db"), sameInstance(mockedDatabaseConfigs.constructed().get(0)));
            assertThat(actual.getGlobalDataSources(), sameInstance(globalDataSources));
            assertThat(actual.getGlobalRuleConfigs(), sameInstance(globalRules));
            assertThat(actual.getProps(), sameInstance(props));
            mockedPoolCreator.verify(() -> DataSourcePoolCreator.create(argThat(propsMap -> propsMap.containsKey("global_ds")), eq(true)));
        }
    }
    
    private static Stream<Arguments> dataSourceCreationArguments() {
        return Stream.of(
                Arguments.of("default instance connection", new Properties(), false),
                Arguments.of("disabled instance connection", PropertiesBuilder.build(new Property(TemporaryConfigurationPropertyKey.INSTANCE_CONNECTION_ENABLED.getKey(), false)), false),
                Arguments.of("enabled instance connection", PropertiesBuilder.build(new Property(TemporaryConfigurationPropertyKey.INSTANCE_CONNECTION_ENABLED.getKey(), true)), true));
    }
    
    private ProxyConfigurationLoadResult createProxyConfigurationLoadResult(final ModeConfiguration modeConfig) {
        return new ProxyConfigurationLoadResult(modeConfig, Collections.emptyMap(), Collections.emptyList(), new Properties(),
                Collections.singletonMap("logic_db", new ProxyDatabaseConfiguration("logic_db", Collections.emptyMap(), Collections.emptyList())));
    }
    
    @SuppressWarnings("unchecked")
    @SneakyThrows(ReflectiveOperationException.class)
    private <T> void registerSingletonService(final Class<T> serviceClass, final T serviceInstance) {
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
