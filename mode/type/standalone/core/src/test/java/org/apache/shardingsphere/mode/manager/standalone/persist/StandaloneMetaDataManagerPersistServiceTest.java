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

package org.apache.shardingsphere.mode.manager.standalone.persist;

import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.pojo.AlterSchemaPOJO;
import org.apache.shardingsphere.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.metadata.persist.service.database.DatabaseMetaDataPersistService;
import org.apache.shardingsphere.mode.metadata.MetaDataContextManager;
import org.apache.shardingsphere.mode.spi.PersistRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Properties;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StandaloneMetaDataManagerPersistServiceTest {
    
    private StandaloneMetaDataManagerPersistService metaDataManagerPersistService;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private MetaDataContextManager metaDataContextManager;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private MetaDataPersistService metaDataPersistService;
    
    @BeforeEach
    @SneakyThrows(ReflectiveOperationException.class)
    void setUp() {
        metaDataManagerPersistService = new StandaloneMetaDataManagerPersistService(mock(PersistRepository.class), metaDataContextManager);
        Field field = metaDataManagerPersistService.getClass().getDeclaredField("metaDataPersistService");
        field.setAccessible(true);
        field.set(metaDataManagerPersistService, metaDataPersistService);
        field.setAccessible(false);
    }
    
    @Test
    void assertCreateDatabase() {
        metaDataManagerPersistService.createDatabase("foo_db");
        verify(metaDataContextManager.getSchemaMetaDataManager()).addDatabase("foo_db");
        verify(metaDataPersistService.getDatabaseMetaDataService()).addDatabase("foo_db");
    }
    
    @Test
    void assertDropDatabase() {
        metaDataManagerPersistService.dropDatabase("foo_db");
        verify(metaDataContextManager.getSchemaMetaDataManager()).dropDatabase("foo_db");
        verify(metaDataPersistService.getDatabaseMetaDataService()).dropDatabase("foo_db");
    }
    
    @Test
    void assertCreateSchema() {
        metaDataManagerPersistService.createSchema("foo_db", "foo_schema");
        verify(metaDataPersistService.getDatabaseMetaDataService()).addSchema("foo_db", "foo_schema");
    }
    
    @Test
    void assertAlterSchemaWithEmptyAlteredSchema() {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(
                Collections.singletonMap("foo_db", database), mock(ResourceMetaData.class), mock(RuleMetaData.class), new ConfigurationProperties(new Properties()));
        when(metaDataContextManager.getMetaDataContexts().get().getMetaData()).thenReturn(metaData);
        DatabaseMetaDataPersistService databaseMetaDataService = mock(DatabaseMetaDataPersistService.class, RETURNS_DEEP_STUBS);
        when(metaDataPersistService.getDatabaseMetaDataService()).thenReturn(databaseMetaDataService);
        metaDataManagerPersistService.alterSchema(new AlterSchemaPOJO("foo_db", "foo_schema", "bar_schema", Collections.singleton("foo_ds")));
        verify(databaseMetaDataService, times(0)).addSchema("foo_db", "bar_schema");
        verify(databaseMetaDataService.getTableMetaDataPersistService()).persist("foo_db", "bar_schema", new HashMap<>());
        verify(databaseMetaDataService.getViewMetaDataPersistService()).persist("foo_db", "bar_schema", new HashMap<>());
        verify(databaseMetaDataService).dropSchema("foo_db", "foo_schema");
    }
    
    @Test
    void assertAlterSchemaWithNotEmptyAlteredSchema() {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        ShardingSphereSchema schema = mock(ShardingSphereSchema.class);
        when(schema.isEmpty()).thenReturn(true);
        when(database.getSchema("foo_schema")).thenReturn(schema);
        when(database.getSchema("bar_schema")).thenReturn(schema);
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(
                Collections.singletonMap("foo_db", database), mock(ResourceMetaData.class), mock(RuleMetaData.class), new ConfigurationProperties(new Properties()));
        when(metaDataContextManager.getMetaDataContexts().get().getMetaData()).thenReturn(metaData);
        DatabaseMetaDataPersistService databaseMetaDataService = mock(DatabaseMetaDataPersistService.class, RETURNS_DEEP_STUBS);
        when(metaDataPersistService.getDatabaseMetaDataService()).thenReturn(databaseMetaDataService);
        metaDataManagerPersistService.alterSchema(new AlterSchemaPOJO("foo_db", "foo_schema", "bar_schema", Collections.singleton("foo_ds")));
        verify(databaseMetaDataService).addSchema("foo_db", "bar_schema");
        verify(databaseMetaDataService.getTableMetaDataPersistService()).persist("foo_db", "bar_schema", new HashMap<>());
        verify(databaseMetaDataService.getViewMetaDataPersistService()).persist("foo_db", "bar_schema", new HashMap<>());
        verify(databaseMetaDataService).dropSchema("foo_db", "foo_schema");
    }
}
