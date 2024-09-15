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

package org.apache.shardingsphere.mode.manager.cluster.event.subscriber.dispatch;

import org.apache.shardingsphere.infra.instance.metadata.InstanceType;
import org.apache.shardingsphere.mode.event.dispatch.assisted.CreateDatabaseListenerAssistedEvent;
import org.apache.shardingsphere.mode.event.dispatch.assisted.DropDatabaseListenerAssistedEvent;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListenerAssistedSubscriberTest {
    
    private ListenerAssistedSubscriber subscriber;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ContextManager contextManager;
    
    @Mock
    private ClusterPersistRepository repository;
    
    @BeforeEach
    void setUp() {
        when(contextManager.getPersistServiceFacade().getRepository()).thenReturn(repository);
        subscriber = new ListenerAssistedSubscriber(contextManager);
    }
    
    @Test
    void assertRenewWithCreateDatabaseListenerAssistedEvent() {
        when(contextManager.getComputeNodeInstanceContext().getModeConfiguration().isCluster()).thenReturn(true);
        when(contextManager.getComputeNodeInstanceContext().getInstance().getMetaData().getType()).thenReturn(InstanceType.JDBC);
        subscriber.renew(new CreateDatabaseListenerAssistedEvent("foo_db"));
        verify(repository).watch(eq("/metadata/foo_db"), any());
        verify(contextManager.getMetaDataContextManager().getSchemaMetaDataManager()).addDatabase("foo_db");
        verify(contextManager.getPersistServiceFacade().getListenerAssistedPersistService()).deleteDatabaseNameListenerAssisted("foo_db");
    }
    
    @Test
    void assertRenewWithDropDatabaseListenerAssistedEvent() {
        when(contextManager.getComputeNodeInstanceContext().getModeConfiguration().isCluster()).thenReturn(true);
        when(contextManager.getComputeNodeInstanceContext().getInstance().getMetaData().getType()).thenReturn(InstanceType.PROXY);
        subscriber.renew(new DropDatabaseListenerAssistedEvent("foo_db"));
        verify(repository).removeDataListener("/metadata/foo_db");
        verify(contextManager.getMetaDataContextManager().getSchemaMetaDataManager()).dropDatabase("foo_db");
        verify(contextManager.getPersistServiceFacade().getListenerAssistedPersistService()).deleteDatabaseNameListenerAssisted("foo_db");
    }
}
