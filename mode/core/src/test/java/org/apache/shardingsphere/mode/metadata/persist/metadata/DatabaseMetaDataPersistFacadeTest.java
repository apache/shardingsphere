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

package org.apache.shardingsphere.mode.metadata.persist.metadata;

import org.apache.shardingsphere.infra.datasource.pool.props.creator.DataSourcePoolPropertiesCreator;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.manager.GenericSchemaManager;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.mode.metadata.persist.metadata.service.SchemaMetaDataPersistService;
import org.apache.shardingsphere.mode.persist.service.TableMetaDataPersistService;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.internal.configuration.plugins.Plugins;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings({DataSourcePoolPropertiesCreator.class, GenericSchemaManager.class})
class DatabaseMetaDataPersistFacadeTest {
    
    private DatabaseMetaDataPersistFacade databaseMetaDataFacade;
    
    @Mock
    private SchemaMetaDataPersistService schemaMetaDataService;
    
    @Mock
    private TableMetaDataPersistService tableMetaDataService;
    
    @BeforeEach
    void setUp() throws ReflectiveOperationException {
        databaseMetaDataFacade = new DatabaseMetaDataPersistFacade(mock(), mock(), true);
        setField("schema", schemaMetaDataService);
        setField("table", tableMetaDataService);
    }
    
    private void setField(final String name, final Object value) throws ReflectiveOperationException {
        Plugins.getMemberAccessor().set(DatabaseMetaDataPersistFacade.class.getDeclaredField(name), databaseMetaDataFacade, value);
    }
    
    @Test
    void assertPersistReloadDatabase() {
        ShardingSphereSchema toBeDeletedSchema = new ShardingSphereSchema("to_be_deleted");
        ShardingSphereSchema toBeAddedSchema = new ShardingSphereSchema("to_be_added");
        when(GenericSchemaManager.getToBeAlteredSchemasWithTablesDropped(any(), any())).thenReturn(Collections.singleton(toBeDeletedSchema));
        when(GenericSchemaManager.getToBeAlteredSchemasWithTablesAdded(any(), any())).thenReturn(Collections.singleton(toBeAddedSchema));
        databaseMetaDataFacade.persistReloadDatabase("foo_db", mock(ShardingSphereDatabase.class), mock(ShardingSphereDatabase.class));
        verify(tableMetaDataService).drop(eq("foo_db"), eq("to_be_deleted"), anyCollection());
    }
    
    @Test
    void assertPersistReloadDatabaseByDrop() {
        ShardingSphereSchema toBeDeletedSchema = new ShardingSphereSchema("to_be_deleted");
        ShardingSphereSchema toBeAlterSchema = new ShardingSphereSchema("to_be_altered");
        when(GenericSchemaManager.getToBeAlteredSchemasWithTablesDropped(any(), any())).thenReturn(Collections.singleton(toBeDeletedSchema));
        when(GenericSchemaManager.getToBeAlteredSchemasWithTablesAdded(any(), any())).thenReturn(Collections.singleton(toBeAlterSchema));
        databaseMetaDataFacade.persistReloadDatabase("foo_db", mock(ShardingSphereDatabase.class), mock(ShardingSphereDatabase.class));
        verify(tableMetaDataService).drop(eq("foo_db"), eq("to_be_deleted"), anyCollection());
    }
}
