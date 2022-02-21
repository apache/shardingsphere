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

import org.apache.shardingsphere.infra.metadata.schema.event.SchemaAlteredEvent;
import org.apache.shardingsphere.infra.metadata.schema.model.TableMetaData;
import org.apache.shardingsphere.mode.metadata.persist.service.SchemaMetaDataPersistService;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Field;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public final class SchemaMetaDataRegistrySubscriberTest {
    
    @Mock
    private SchemaMetaDataPersistService persistService;
    
    private SchemaMetaDataRegistrySubscriber schemaMetaDataRegistrySubscriber;
    
    @Before
    public void setUp() throws ReflectiveOperationException {
        schemaMetaDataRegistrySubscriber = new SchemaMetaDataRegistrySubscriber(mock(ClusterPersistRepository.class));
        Field field = schemaMetaDataRegistrySubscriber.getClass().getDeclaredField("persistService");
        field.setAccessible(true);
        field.set(schemaMetaDataRegistrySubscriber, persistService);
    }
    
    @Test
    public void assertUpdateWithMetaDataAlteredEvent() {
        SchemaAlteredEvent event = new SchemaAlteredEvent("foo_db");
        TableMetaData tableMetaData = new TableMetaData();
        event.getAlteredTables().add(tableMetaData);
        event.getDroppedTables().add("foo_table");
        schemaMetaDataRegistrySubscriber.update(event);
        verify(persistService).persist("foo_db", tableMetaData);
        verify(persistService).delete("foo_db", "foo_table");
    }
}
