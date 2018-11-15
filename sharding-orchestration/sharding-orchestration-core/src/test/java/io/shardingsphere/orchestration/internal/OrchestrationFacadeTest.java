/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.orchestration.internal;

import io.shardingsphere.orchestration.config.OrchestrationConfiguration;
import io.shardingsphere.orchestration.internal.config.listener.DataSourceOrchestrationListener;
import io.shardingsphere.orchestration.internal.config.listener.RuleOrchestrationListener;
import io.shardingsphere.orchestration.internal.listener.OrchestrationListenerManager;
import io.shardingsphere.orchestration.reg.api.RegistryCenter;
import io.shardingsphere.orchestration.reg.api.RegistryCenterConfiguration;
import io.shardingsphere.orchestration.reg.listener.EventListener;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public final class OrchestrationFacadeTest {
    
    private OrchestrationFacade orchestrationFacade;
    
    @Mock
    private RegistryCenter regCenter;
    
    @Before
    public void setUp() throws ReflectiveOperationException {
        orchestrationFacade = new OrchestrationFacade(getOrchestrationConfiguration(), Arrays.asList("sharding_db", "masterslave_db"));
        setRegistry(orchestrationFacade);
        setRegistry(orchestrationFacade.getConfigService());
        setRegistry(orchestrationFacade.getClass().getDeclaredField("instanceStateService"), orchestrationFacade);
        setRegistry(orchestrationFacade.getClass().getDeclaredField("dataSourceService"), orchestrationFacade);
        setRegCenterForOrchestrationListenerManager();
    }
    
    private void setRegCenterForOrchestrationListenerManager() throws ReflectiveOperationException {
        Field file = orchestrationFacade.getClass().getDeclaredField("listenerManager");
        file.setAccessible(true);
        OrchestrationListenerManager listenerManager = (OrchestrationListenerManager) file.get(orchestrationFacade);
        setRegistry(listenerManager.getClass().getDeclaredField("propertiesListenerManager"), listenerManager);
        setRegistry(listenerManager.getClass().getDeclaredField("authenticationListenerManager"), listenerManager);
        setRegistry(listenerManager.getClass().getDeclaredField("configMapListenerManager"), listenerManager);
        setRegistry(listenerManager.getClass().getDeclaredField("instanceStateListenerManager"), listenerManager);
        setRegistry(listenerManager.getClass().getDeclaredField("dataSourceStateListenerManager"), listenerManager);
        Field childField = listenerManager.getClass().getDeclaredField("ruleListenerManagers");
        childField.setAccessible(true);
        Collection<RuleOrchestrationListener> ruleListenerManagers = (Collection<RuleOrchestrationListener>) childField.get(listenerManager);
        for (RuleOrchestrationListener each : ruleListenerManagers) {
            setRegistry(each);
        }
        Collection<DataSourceOrchestrationListener> dataSourceListenerManagers = (Collection<DataSourceOrchestrationListener>) listenerManager.getClass().getDeclaredField("dataSourceListenerManagers").get(listenerManager);
        for (DataSourceOrchestrationListener each : dataSourceListenerManagers) {
            setRegistry(each);
        }
    }
    
    private void setRegistry(final Object target) throws ReflectiveOperationException {
        Field field = target.getClass().getDeclaredField("regCenter");
        field.setAccessible(true);
        field.set(target, regCenter);
    }
    
    private void setRegistry(final Field field, final Object target) throws ReflectiveOperationException {
        field.setAccessible(true);
        setRegistry(field.get(target));
    }
    
    private OrchestrationConfiguration getOrchestrationConfiguration() {
        return new OrchestrationConfiguration("test", new RegistryCenterConfiguration(), true);
    }
    
    @Test
    public void assertInitWithParameters() {
    }
    
    @Test
    public void assertInitWithoutParameters() {
        orchestrationFacade.init();
        verify(regCenter).persistEphemeral(anyString(), anyString());
        verify(regCenter).persist("/test/state/datasources", "");
        verify(regCenter).watch(eq("/test/config/authentication"), any(EventListener.class));
        verify(regCenter).watch(eq("/test/config/configmap"), any(EventListener.class));
        verify(regCenter).watch(eq("/test/config/schema/sharding_db/datasource"), any(EventListener.class));
        verify(regCenter).watch(eq("/test/config/schema/masterslave_db/datasource"), any(EventListener.class));
        verify(regCenter).watch(eq("/test/config/props"), any(EventListener.class));
        verify(regCenter).watch(eq("/test/config/schema/sharding_db/rule"), any(EventListener.class));
        verify(regCenter).watch(eq("/test/config/schema/masterslave_db/rule"), any(EventListener.class));
    }
    
    @Test
    public void testClose() {
    }
}
