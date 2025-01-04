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

package org.apache.shardingsphere.mode.manager.cluster.dispatch.listener;

import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.cluster.dispatch.listener.type.DatabaseMetaDataChangedListener;
import org.apache.shardingsphere.mode.manager.cluster.dispatch.listener.type.GlobalMetaDataChangedListener;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DataChangedEventListenerRegistryTest {
    
    @Test
    void assertRegister() {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        ClusterPersistRepository repository = mock(ClusterPersistRepository.class);
        when(contextManager.getPersistServiceFacade().getRepository()).thenReturn(repository);
        DataChangedEventListenerRegistry registry = new DataChangedEventListenerRegistry(contextManager, Collections.singleton("foo_db"));
        registry.register();
        verify(repository).watch(eq("/metadata/foo_db"), any(DatabaseMetaDataChangedListener.class));
        verify(repository, atLeastOnce()).watch(anyString(), any(GlobalMetaDataChangedListener.class));
    }
}
