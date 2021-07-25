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

package org.apache.shardingsphere.governance.core.registry.metadata.subscriber;

import org.apache.shardingsphere.governance.core.registry.metadata.event.DatabaseDroppedSQLNotificationEvent;
import org.apache.shardingsphere.governance.repository.spi.RegistryCenterRepository;
import org.apache.shardingsphere.infra.config.persist.service.SchemaMetaDataPersistService;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.schema.refresher.event.SchemaAlteredEvent;
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
        schemaMetaDataRegistrySubscriber = new SchemaMetaDataRegistrySubscriber(mock(RegistryCenterRepository.class));
        Field field = schemaMetaDataRegistrySubscriber.getClass().getDeclaredField("persistService");
        field.setAccessible(true);
        field.set(schemaMetaDataRegistrySubscriber, persistService);
    }
    
    @Test
    public void assertUpdateWithMetaDataAlteredEvent() {
        SchemaAlteredEvent event = new SchemaAlteredEvent("foo_db", mock(ShardingSphereSchema.class));
        schemaMetaDataRegistrySubscriber.update(event);
        verify(persistService).persist("foo_db", event.getSchema());
    }
    
    @Test
    public void assertUpdateWithDatabaseDroppedSQLNotificationEvent() {
        DatabaseDroppedSQLNotificationEvent event = new DatabaseDroppedSQLNotificationEvent("foo_db");
        schemaMetaDataRegistrySubscriber.update(event);
        verify(persistService).delete("foo_db");
    }
}
