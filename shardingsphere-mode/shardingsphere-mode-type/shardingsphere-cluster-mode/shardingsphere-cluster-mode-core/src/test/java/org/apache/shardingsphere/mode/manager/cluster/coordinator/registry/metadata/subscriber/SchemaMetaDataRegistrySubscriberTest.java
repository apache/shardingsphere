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

package org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.metadata.subscriber;

import org.apache.shardingsphere.infra.util.eventbus.EventBusContext;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.database.schema.event.AddSchemaEvent;
import org.apache.shardingsphere.infra.metadata.database.schema.event.AlterSchemaEvent;
import org.apache.shardingsphere.infra.metadata.database.schema.event.DropSchemaEvent;
import org.apache.shardingsphere.infra.metadata.database.schema.event.SchemaAlteredEvent;
import org.apache.shardingsphere.mode.metadata.persist.service.DatabaseMetaDataPersistService;
import org.apache.shardingsphere.mode.metadata.persist.service.schema.TableMetaDataPersistService;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Field;
import java.util.Collections;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyMap;

@RunWith(MockitoJUnitRunner.class)
public final class SchemaMetaDataRegistrySubscriberTest {
    
    @Mock
    private DatabaseMetaDataPersistService persistService;
    
    private SchemaMetaDataRegistrySubscriber schemaMetaDataRegistrySubscriber;
    
    @Before
    public void setUp() throws ReflectiveOperationException {
        schemaMetaDataRegistrySubscriber = new SchemaMetaDataRegistrySubscriber(mock(ClusterPersistRepository.class), new EventBusContext());
        Field field = schemaMetaDataRegistrySubscriber.getClass().getDeclaredField("persistService");
        field.setAccessible(true);
        field.set(schemaMetaDataRegistrySubscriber, persistService);
    }
    
    @Test
    public void assertUpdateWithMetaDataAlteredEvent() {
        SchemaAlteredEvent event = new SchemaAlteredEvent("foo_db", "foo_schema");
        ShardingSphereTable table = new ShardingSphereTable();
        event.getAlteredTables().add(table);
        event.getDroppedTables().add("foo_table");
        when(persistService.getTableMetaDataPersistService()).thenReturn(mock(TableMetaDataPersistService.class));
        schemaMetaDataRegistrySubscriber.update(event);
        TableMetaDataPersistService tableMetaDataPersistService = persistService.getTableMetaDataPersistService();
        verify(tableMetaDataPersistService).persist(anyString(), anyString(), anyMap());
        verify(tableMetaDataPersistService).delete("foo_db", "foo_schema", "foo_table");
    }
    
    @Test
    public void assertAddSchemaEvent() {
        AddSchemaEvent event = new AddSchemaEvent("foo_db", "foo_schema");
        schemaMetaDataRegistrySubscriber.addSchema(event);
        verify(persistService).addSchema("foo_db", "foo_schema");
    }
    
    @Test
    public void assertDropSchemaEvent() {
        DropSchemaEvent event = new DropSchemaEvent("foo_db", Collections.singleton("foo_schema"));
        schemaMetaDataRegistrySubscriber.dropSchema(event);
        verify(persistService).dropSchema("foo_db", "foo_schema");
    }
    
    @Test
    public void assertAlterSchemaEventWhenContainsTable() {
        ShardingSphereSchema schema = new ShardingSphereSchema(Collections.singletonMap("t_order", new ShardingSphereTable()), Collections.emptyMap());
        AlterSchemaEvent event = new AlterSchemaEvent("foo_db", "foo_schema", "new_foo_schema", schema);
        schemaMetaDataRegistrySubscriber.alterSchema(event);
        verify(persistService).compareAndPersist("foo_db", "new_foo_schema", schema);
        verify(persistService).dropSchema("foo_db", "foo_schema");
    }
    
    @Test
    public void assertAlterSchemaEventWhenNotContainsTable() {
        ShardingSphereSchema schema = new ShardingSphereSchema();
        AlterSchemaEvent event = new AlterSchemaEvent("foo_db", "foo_schema", "new_foo_schema", schema);
        schemaMetaDataRegistrySubscriber.alterSchema(event);
        verify(persistService).compareAndPersist("foo_db", "new_foo_schema", schema);
        verify(persistService).dropSchema("foo_db", "foo_schema");
    }
}
