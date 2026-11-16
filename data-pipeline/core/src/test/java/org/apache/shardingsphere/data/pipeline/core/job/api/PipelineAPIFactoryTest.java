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

package org.apache.shardingsphere.data.pipeline.core.job.api;

import lombok.SneakyThrows;
import org.apache.shardingsphere.data.pipeline.core.context.PipelineContextKey;
import org.apache.shardingsphere.data.pipeline.core.context.PipelineContextManager;
import org.apache.shardingsphere.data.pipeline.core.metadata.node.PipelineMetaDataNode;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.infra.instance.metadata.InstanceType;
import org.apache.shardingsphere.infra.spi.exception.ServiceProviderNotFoundException;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepositoryConfiguration;
import org.apache.shardingsphere.schedule.spi.CoordinatorRegistryCenterProvider;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.internal.configuration.plugins.Plugins;

import java.util.Map;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PipelineAPIFactoryTest {
    
    @Test
    void assertGetRegistryCenterViaProvider() {
        PipelineContextKey contextKey = new PipelineContextKey("foo_db", InstanceType.JDBC);
        CoordinatorRegistryCenter registryCenter = mock(CoordinatorRegistryCenter.class);
        CoordinatorRegistryCenterProvider provider = mock(CoordinatorRegistryCenterProvider.class);
        try (
                MockedStatic<PipelineContextManager> contextManager = mockStatic(PipelineContextManager.class);
                MockedStatic<TypedSPILoader> serviceLoader = mockStatic(TypedSPILoader.class)) {
            ClusterPersistRepositoryConfiguration repositoryConfig = mockContext(contextKey, contextManager);
            serviceLoader.when(() -> TypedSPILoader.getService(CoordinatorRegistryCenterProvider.class, repositoryConfig.getType())).thenReturn(provider);
            when(provider.create(repositoryConfig, PipelineMetaDataNode.getElasticJobNamespace())).thenReturn(registryCenter);
            assertThat(PipelineAPIFactory.getRegistryCenter(contextKey), sameInstance(registryCenter));
            assertThat(PipelineAPIFactory.getRegistryCenter(contextKey), sameInstance(registryCenter));
            verify(provider).create(repositoryConfig, PipelineMetaDataNode.getElasticJobNamespace());
        } finally {
            PipelineAPIFactory.close(contextKey);
        }
        verify(registryCenter).close();
    }
    
    @Test
    void assertClose() {
        PipelineContextKey contextKey = new PipelineContextKey("bar_db", InstanceType.JDBC);
        CoordinatorRegistryCenter registryCenter = mock(CoordinatorRegistryCenter.class);
        CoordinatorRegistryCenterProvider provider = mock(CoordinatorRegistryCenterProvider.class);
        try (
                MockedStatic<PipelineContextManager> contextManager = mockStatic(PipelineContextManager.class);
                MockedStatic<TypedSPILoader> serviceLoader = mockStatic(TypedSPILoader.class)) {
            ClusterPersistRepositoryConfiguration repositoryConfig = mockContext(contextKey, contextManager);
            serviceLoader.when(() -> TypedSPILoader.getService(CoordinatorRegistryCenterProvider.class, repositoryConfig.getType())).thenReturn(provider);
            when(provider.create(repositoryConfig, PipelineMetaDataNode.getElasticJobNamespace())).thenReturn(registryCenter);
            PipelineAPIFactory.getJobStatisticsAPI(contextKey);
            PipelineAPIFactory.getJobConfigurationAPI(contextKey);
            PipelineAPIFactory.getJobOperateAPI(contextKey);
            PipelineAPIFactory.getShardingStatisticsAPI(contextKey);
            PipelineAPIFactory.getPipelineGovernanceFacade(contextKey);
            assertTrue(getMap(PipelineAPIFactory.class, "GOVERNANCE_FACADE_MAP").containsKey(contextKey));
            assertTrue(getMap(getHolderClass("ElasticJobAPIHolder"), "INSTANCE_MAP").containsKey(contextKey));
            assertTrue(getMap(getHolderClass("RegistryCenterHolder"), "INSTANCE_MAP").containsKey(contextKey));
            PipelineAPIFactory.close(contextKey);
            assertFalse(getMap(PipelineAPIFactory.class, "GOVERNANCE_FACADE_MAP").containsKey(contextKey));
            assertFalse(getMap(getHolderClass("ElasticJobAPIHolder"), "INSTANCE_MAP").containsKey(contextKey));
            assertFalse(getMap(getHolderClass("RegistryCenterHolder"), "INSTANCE_MAP").containsKey(contextKey));
            PipelineAPIFactory.close(contextKey);
        }
        verify(registryCenter, times(1)).close();
    }
    
    @Test
    void assertCloseWithoutCreatingResources() {
        PipelineContextKey contextKey = new PipelineContextKey("empty_db", InstanceType.JDBC);
        try (MockedStatic<TypedSPILoader> serviceLoader = mockStatic(TypedSPILoader.class)) {
            PipelineAPIFactory.close(contextKey);
            serviceLoader.verifyNoInteractions();
        }
        assertFalse(getMap(PipelineAPIFactory.class, "GOVERNANCE_FACADE_MAP").containsKey(contextKey));
        assertFalse(getMap(getHolderClass("ElasticJobAPIHolder"), "INSTANCE_MAP").containsKey(contextKey));
        assertFalse(getMap(getHolderClass("RegistryCenterHolder"), "INSTANCE_MAP").containsKey(contextKey));
    }
    
    @Test
    void assertGetRegistryCenterWithUnsupportedProvider() {
        PipelineContextKey contextKey = new PipelineContextKey("unsupported_db", InstanceType.JDBC);
        try (MockedStatic<PipelineContextManager> contextManager = mockStatic(PipelineContextManager.class)) {
            ClusterPersistRepositoryConfiguration repositoryConfig = new ClusterPersistRepositoryConfiguration("NOT_REGISTERED", "namespace", "serverLists", new Properties());
            ContextManager contextManagerInstance = mock(ContextManager.class, RETURNS_DEEP_STUBS);
            contextManager.when(() -> PipelineContextManager.getContext(contextKey)).thenReturn(contextManagerInstance);
            whenModeConfiguration(contextManagerInstance, repositoryConfig);
            ServiceProviderNotFoundException actual = assertThrows(ServiceProviderNotFoundException.class, () -> PipelineAPIFactory.getRegistryCenter(contextKey));
            assertThat(actual.getMessage(), containsString("NOT_REGISTERED"));
        } finally {
            PipelineAPIFactory.close(contextKey);
        }
    }
    
    private ClusterPersistRepositoryConfiguration mockContext(final PipelineContextKey contextKey, final MockedStatic<PipelineContextManager> pipelineContextManager) {
        ClusterPersistRepositoryConfiguration result = new ClusterPersistRepositoryConfiguration("FIXTURE", "namespace", "serverLists", new Properties());
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        pipelineContextManager.when(() -> PipelineContextManager.getContext(contextKey)).thenReturn(contextManager);
        whenModeConfiguration(contextManager, result);
        whenRepository(contextManager);
        return result;
    }
    
    private void whenModeConfiguration(final ContextManager contextManager, final ClusterPersistRepositoryConfiguration repositoryConfig) {
        when(contextManager.getComputeNodeInstanceContext().getModeConfiguration()).thenReturn(new ModeConfiguration("Cluster", repositoryConfig));
    }
    
    private void whenRepository(final ContextManager contextManager) {
        when(contextManager.getPersistServiceFacade().getRepository()).thenReturn(mock(ClusterPersistRepository.class));
    }
    
    @SneakyThrows(ClassNotFoundException.class)
    private Class<?> getHolderClass(final String simpleName) {
        return Class.forName(PipelineAPIFactory.class.getName() + "$" + simpleName);
    }
    
    @SuppressWarnings("unchecked")
    @SneakyThrows(ReflectiveOperationException.class)
    private Map<PipelineContextKey, ?> getMap(final Class<?> owner, final String fieldName) {
        return (Map<PipelineContextKey, ?>) Plugins.getMemberAccessor().get(owner.getDeclaredField(fieldName), owner);
    }
}
