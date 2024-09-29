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

import org.apache.shardingsphere.infra.config.props.temporary.TemporaryConfigurationPropertyKey;
import org.apache.shardingsphere.infra.instance.metadata.InstanceType;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereView;
import org.apache.shardingsphere.mode.event.dispatch.metadata.schema.SchemaAddedEvent;
import org.apache.shardingsphere.mode.event.dispatch.metadata.schema.SchemaDeletedEvent;
import org.apache.shardingsphere.mode.event.dispatch.metadata.schema.table.CreateOrAlterTableEvent;
import org.apache.shardingsphere.mode.event.dispatch.metadata.schema.table.DropTableEvent;
import org.apache.shardingsphere.mode.event.dispatch.metadata.schema.view.CreateOrAlterViewEvent;
import org.apache.shardingsphere.mode.event.dispatch.metadata.schema.view.DropViewEvent;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MetaDataChangedSubscriberTest {
    
    private MetaDataChangedSubscriber subscriber;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ContextManager contextManager;
    
    @BeforeEach
    void setUp() {
        when(contextManager.getPersistServiceFacade().getRepository()).thenReturn(mock(ClusterPersistRepository.class));
        when(contextManager.getMetaDataContexts().getMetaData().getTemporaryProps().getValue(TemporaryConfigurationPropertyKey.PROXY_META_DATA_COLLECTOR_ENABLED)).thenReturn(false);
        when(contextManager.getComputeNodeInstanceContext().getModeConfiguration().isCluster()).thenReturn(true);
        subscriber = new MetaDataChangedSubscriber(contextManager);
    }
    
    @Test
    void assertRenewWithSchemaAddedEvent() {
        when(contextManager.getComputeNodeInstanceContext().getInstance().getMetaData().getType()).thenReturn(InstanceType.PROXY);
        subscriber.renew(new SchemaAddedEvent("foo_db", "foo_schema"));
        verify(contextManager.getMetaDataContextManager().getSchemaMetaDataManager()).addSchema("foo_db", "foo_schema");
    }
    
    @Test
    void assertRenewWithSchemaDeletedEvent() {
        when(contextManager.getComputeNodeInstanceContext().getInstance().getMetaData().getType()).thenReturn(InstanceType.PROXY);
        subscriber.renew(new SchemaDeletedEvent("foo_db", "foo_schema"));
        verify(contextManager.getMetaDataContextManager().getSchemaMetaDataManager()).dropSchema("foo_db", "foo_schema");
    }
    
    @Test
    void assertRenewWithCreateOrAlterTableEvent() {
        when(contextManager.getPersistServiceFacade().getMetaDataPersistService().getMetaDataVersionPersistService().getActiveVersionByFullPath("key")).thenReturn("value");
        ShardingSphereTable table = mock(ShardingSphereTable.class);
        when(contextManager.getPersistServiceFacade().getMetaDataPersistService().getDatabaseMetaDataFacade().getTable().load("foo_db", "foo_schema", "foo_tbl"))
                .thenReturn(table);
        subscriber.renew(new CreateOrAlterTableEvent("foo_db", "foo_schema", "foo_tbl", "key", "value"));
        verify(contextManager.getMetaDataContextManager().getSchemaMetaDataManager()).alterSchema("foo_db", "foo_schema", table, null);
    }
    
    @Test
    void assertRenewWithDropTableEvent() {
        subscriber.renew(new DropTableEvent("foo_db", "foo_schema", "foo_tbl"));
        verify(contextManager.getMetaDataContextManager().getSchemaMetaDataManager()).alterSchema("foo_db", "foo_schema", "foo_tbl", null);
    }
    
    @Test
    void assertRenewWithCreateOrAlterViewEvent() {
        when(contextManager.getPersistServiceFacade().getMetaDataPersistService().getMetaDataVersionPersistService().getActiveVersionByFullPath("key")).thenReturn("value");
        ShardingSphereView view = mock(ShardingSphereView.class);
        when(contextManager.getPersistServiceFacade().getMetaDataPersistService().getDatabaseMetaDataFacade().getView().load("foo_db", "foo_schema", "foo_view"))
                .thenReturn(view);
        subscriber.renew(new CreateOrAlterViewEvent("foo_db", "foo_schema", "foo_view", "key", "value"));
        verify(contextManager.getMetaDataContextManager().getSchemaMetaDataManager()).alterSchema("foo_db", "foo_schema", null, view);
    }
    
    @Test
    void assertRenewWithDropViewEvent() {
        subscriber.renew(new DropViewEvent("foo_db", "foo_schema", "foo_view", "key", "value"));
        verify(contextManager.getMetaDataContextManager().getSchemaMetaDataManager()).alterSchema("foo_db", "foo_schema", null, "foo_view");
    }
}
