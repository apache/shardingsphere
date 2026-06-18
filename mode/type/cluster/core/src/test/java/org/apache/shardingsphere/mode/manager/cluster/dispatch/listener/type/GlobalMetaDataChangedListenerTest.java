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

package org.apache.shardingsphere.mode.manager.cluster.dispatch.listener.type;

import org.apache.shardingsphere.infra.spi.type.ordered.cache.OrderedServicesCache;
import org.apache.shardingsphere.mode.event.DataChangedEvent;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.cluster.dispatch.handler.global.GlobalDataChangedEventHandler;
import org.apache.shardingsphere.mode.manager.cluster.dispatch.handler.global.config.GlobalConfigurationChangedHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalMetaDataChangedListenerTest {
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ContextManager contextManager;
    
    @AfterEach
    void cleanCache() {
        OrderedServicesCache.clearCache();
    }
    
    @Test
    void assertOnChangeWhenTypeNotSubscribed() {
        OrderedServicesCache.cacheServices(GlobalDataChangedEventHandler.class, Collections.singletonList("type"), Collections.singletonMap("key", "value"));
        GlobalDataChangedEventHandler handler = mock(GlobalDataChangedEventHandler.class);
        when(handler.getSubscribedTypes()).thenReturn(Collections.singleton(DataChangedEvent.Type.ADDED));
        new GlobalMetaDataChangedListener(contextManager, handler).onChange(new DataChangedEvent("version_path", "1", DataChangedEvent.Type.UPDATED));
        assertTrue(OrderedServicesCache.findCachedServices(GlobalDataChangedEventHandler.class, Collections.singletonList("type")).isPresent());
        verify(handler, never()).handle(any(), any());
    }
    
    @Test
    void assertOnChangeWhenActiveVersionNotMatched() {
        OrderedServicesCache.cacheServices(GlobalDataChangedEventHandler.class, Collections.singletonList("type"), Collections.singletonMap("key", "value"));
        when(contextManager.getPersistServiceFacade().getRepository().query("version_path")).thenReturn("2");
        GlobalConfigurationChangedHandler handler = mock(GlobalConfigurationChangedHandler.class);
        when(handler.getSubscribedTypes()).thenReturn(Collections.singleton(DataChangedEvent.Type.UPDATED));
        new GlobalMetaDataChangedListener(contextManager, handler).onChange(new DataChangedEvent("version_path", "1", DataChangedEvent.Type.UPDATED));
        assertTrue(OrderedServicesCache.findCachedServices(GlobalDataChangedEventHandler.class, Collections.singletonList("type")).isPresent());
        verify(handler, never()).handle(any(), any());
    }
    
    @Test
    void assertOnChangeWithMatchedActiveVersion() {
        OrderedServicesCache.cacheServices(GlobalDataChangedEventHandler.class, Collections.singletonList("type"), Collections.singletonMap("key", "value"));
        when(contextManager.getPersistServiceFacade().getRepository().query("version_path")).thenReturn("1");
        GlobalConfigurationChangedHandler handler = mock(GlobalConfigurationChangedHandler.class);
        when(handler.getSubscribedTypes()).thenReturn(Collections.singleton(DataChangedEvent.Type.UPDATED));
        DataChangedEvent event = new DataChangedEvent("version_path", "1", DataChangedEvent.Type.UPDATED);
        new GlobalMetaDataChangedListener(contextManager, handler).onChange(event);
        assertFalse(OrderedServicesCache.findCachedServices(GlobalDataChangedEventHandler.class, Collections.singletonList("type")).isPresent());
        verify(handler).handle(contextManager, event);
    }
    
    @Test
    void assertOnChangeWithNonConfigurationHandler() {
        OrderedServicesCache.cacheServices(GlobalDataChangedEventHandler.class, Collections.singletonList("type"), Collections.singletonMap("key", "value"));
        GlobalDataChangedEventHandler handler = mock(GlobalDataChangedEventHandler.class);
        when(handler.getSubscribedTypes()).thenReturn(Collections.singleton(DataChangedEvent.Type.UPDATED));
        DataChangedEvent event = new DataChangedEvent("version_path", "1", DataChangedEvent.Type.UPDATED);
        new GlobalMetaDataChangedListener(contextManager, handler).onChange(event);
        assertFalse(OrderedServicesCache.findCachedServices(GlobalDataChangedEventHandler.class, Collections.singletonList("type")).isPresent());
        verify(handler).handle(contextManager, event);
    }
}
