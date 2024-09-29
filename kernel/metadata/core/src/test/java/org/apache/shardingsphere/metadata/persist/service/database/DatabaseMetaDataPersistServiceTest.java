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

package org.apache.shardingsphere.metadata.persist.service.database;

import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereView;
import org.apache.shardingsphere.metadata.persist.service.schema.TableMetaDataPersistService;
import org.apache.shardingsphere.metadata.persist.service.schema.ViewMetaDataPersistService;
import org.apache.shardingsphere.metadata.persist.service.version.MetaDataVersionPersistService;
import org.apache.shardingsphere.mode.spi.PersistRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.internal.configuration.plugins.Plugins;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DatabaseMetaDataPersistServiceTest {
    
    private DatabaseMetaDataPersistService persistService;
    
    @Mock
    private PersistRepository repository;
    
    @Mock
    private TableMetaDataPersistService tableMetaDataPersistService;
    
    @Mock
    private ViewMetaDataPersistService viewMetaDataPersistService;
    
    @BeforeEach
    void setUp() throws ReflectiveOperationException {
        persistService = new DatabaseMetaDataPersistService(repository, mock(MetaDataVersionPersistService.class));
        Plugins.getMemberAccessor().set(DatabaseMetaDataPersistService.class.getDeclaredField("tableMetaDataPersistService"), persistService, tableMetaDataPersistService);
        Plugins.getMemberAccessor().set(DatabaseMetaDataPersistService.class.getDeclaredField("viewMetaDataPersistService"), persistService, viewMetaDataPersistService);
    }
    
    @Test
    void assertAddDatabase() {
        persistService.addDatabase("foo_db");
        verify(repository).persist("/metadata/foo_db", "");
    }
    
    @Test
    void assertDropDatabase() {
        persistService.dropDatabase("foo_db");
        verify(repository).delete("/metadata/foo_db");
    }
    
    @Test
    void assertLoadAllDatabaseNames() {
        when(repository.getChildrenKeys("/metadata")).thenReturn(Collections.singletonList("foo"));
        assertThat(persistService.loadAllDatabaseNames(), is(Collections.singletonList("foo")));
    }
    
    @Test
    void assertAddSchema() {
        persistService.addSchema("foo_db", "foo_schema");
        verify(repository).persist("/metadata/foo_db/schemas/foo_schema/tables", "");
    }
    
    @Test
    void assertDropSchema() {
        persistService.dropSchema("foo_db", "foo_schema");
        verify(repository).delete("/metadata/foo_db/schemas/foo_schema");
    }
    
    @Test
    void assertAlterSchemaByRefreshWithoutTablesAndViews() {
        persistService.alterSchemaByRefresh("foo_db", new ShardingSphereSchema("foo_schema"));
        verify(repository).persist("/metadata/foo_db/schemas/foo_schema/tables", "");
        verify(tableMetaDataPersistService).persist("foo_db", "foo_schema", Collections.emptyMap());
    }
    
    @Test
    void assertAlterSchemaByRefreshWithTables() {
        Map<String, ShardingSphereTable> tables = Collections.singletonMap("foo_tbl", mock(ShardingSphereTable.class));
        persistService.alterSchemaByRefresh("foo_db", new ShardingSphereSchema("foo_schema", tables, Collections.emptyMap()));
        verify(repository, times(0)).persist("/metadata/foo_db/schemas/foo_schema/tables", "");
        verify(tableMetaDataPersistService).persist("foo_db", "foo_schema", tables);
    }
    
    @Test
    void assertAlterSchemaByRefreshWithViews() {
        persistService.alterSchemaByRefresh("foo_db", new ShardingSphereSchema("foo_schema", Collections.emptyMap(), Collections.singletonMap("foo_view", mock(ShardingSphereView.class))));
        verify(repository, times(0)).persist("/metadata/foo_db/schemas/foo_schema/tables", "");
        verify(tableMetaDataPersistService).persist("foo_db", "foo_schema", Collections.emptyMap());
    }
    
    @Test
    void assertLoadSchemas() {
        when(repository.getChildrenKeys("/metadata/foo_db/schemas")).thenReturn(Collections.singletonList("foo_schema"));
        ShardingSphereTable table = mock(ShardingSphereTable.class);
        when(tableMetaDataPersistService.load("foo_db", "foo_schema")).thenReturn(Collections.singletonMap("foo_tbl", table));
        ShardingSphereView view = mock(ShardingSphereView.class);
        when(viewMetaDataPersistService.load("foo_db", "foo_schema")).thenReturn(Collections.singletonMap("foo_view", view));
        Map<String, ShardingSphereSchema> actual = persistService.loadSchemas("foo_db");
        assertThat(actual.size(), is(1));
        assertThat(actual.get("foo_schema").getTable("foo_tbl"), is(table));
        assertThat(actual.get("foo_schema").getView("foo_view"), is(view));
    }
    
    @Test
    void assertDropTables() {
        persistService.dropTables("foo_db", "foo_schema", Collections.singletonMap("foo_tbl", mock(ShardingSphereTable.class)));
        verify(tableMetaDataPersistService).delete("foo_db", "foo_schema", "foo_tbl");
    }
    
    @Test
    void assertAlterSchemaByAlterConfigurationByRefresh() {
        persistService.alterSchemaByRuleAltered("foo_db", new ShardingSphereSchema("foo_schema"));
        verify(repository).persist("/metadata/foo_db/schemas/foo_schema/tables", "");
        verify(tableMetaDataPersistService).persist("foo_db", "foo_schema", Collections.emptyMap());
    }
    
    @Test
    void assertAlterSchemaByAlterConfigurationWithNotEmptyTablesByRefresh() {
        Map<String, ShardingSphereTable> tables = Collections.singletonMap("foo_tbl", mock(ShardingSphereTable.class));
        persistService.alterSchemaByRuleAltered("foo_db", new ShardingSphereSchema("foo_schema", tables, Collections.emptyMap()));
        verify(repository, times(0)).persist("/metadata/foo_db/schemas/foo_schema/tables", "");
        verify(tableMetaDataPersistService).persist("foo_db", "foo_schema", tables);
    }
    
    @Test
    void assertAlterSchemaByAlterConfigurationWithNotEmptyViewsByRefresh() {
        persistService.alterSchemaByRuleAltered("foo_db", new ShardingSphereSchema("foo_schema", Collections.emptyMap(), Collections.singletonMap("foo_view", mock(ShardingSphereView.class))));
        verify(repository, times(0)).persist("/metadata/foo_db/schemas/foo_schema/tables", "");
        verify(tableMetaDataPersistService).persist("foo_db", "foo_schema", Collections.emptyMap());
    }
    
    @Test
    void assertAlterSchemaByRefreshByDropConfiguration() {
        persistService.alterSchemaByRuleDropped("foo_db", "foo_schema", mock(ShardingSphereSchema.class));
        verify(tableMetaDataPersistService).persist("foo_db", "foo_schema", Collections.emptyMap());
    }
}
