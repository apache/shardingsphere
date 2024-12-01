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

package org.apache.shardingsphere.metadata.persist.service.metadata.schema;

import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereView;
import org.apache.shardingsphere.metadata.persist.service.metadata.table.TableMetaDataPersistService;
import org.apache.shardingsphere.metadata.persist.service.metadata.table.ViewMetaDataPersistService;
import org.apache.shardingsphere.metadata.persist.service.version.MetaDataVersionPersistService;
import org.apache.shardingsphere.mode.spi.PersistRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.internal.configuration.plugins.Plugins;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SchemaMetaDataPersistServiceTest {
    
    private SchemaMetaDataPersistService persistService;
    
    @Mock
    private PersistRepository repository;
    
    @Mock
    private TableMetaDataPersistService tableMetaDataPersistService;
    
    @Mock
    private ViewMetaDataPersistService viewMetaDataPersistService;
    
    @BeforeEach
    void setUp() throws ReflectiveOperationException {
        persistService = new SchemaMetaDataPersistService(repository, mock(MetaDataVersionPersistService.class));
        Plugins.getMemberAccessor().set(SchemaMetaDataPersistService.class.getDeclaredField("tableMetaDataPersistService"), persistService, tableMetaDataPersistService);
        Plugins.getMemberAccessor().set(SchemaMetaDataPersistService.class.getDeclaredField("viewMetaDataPersistService"), persistService, viewMetaDataPersistService);
    }
    
    @Test
    void assertAdd() {
        persistService.add("foo_db", "foo_schema");
        verify(repository).persist("/metadata/foo_db/schemas/foo_schema/tables", "");
    }
    
    @Test
    void assertDrop() {
        persistService.drop("foo_db", "foo_schema");
        verify(repository).delete("/metadata/foo_db/schemas/foo_schema");
    }
    
    @Test
    void assertAlterByRefreshWithoutTablesAndViews() {
        persistService.alterByRefresh("foo_db", new ShardingSphereSchema("foo_schema"));
        verify(repository).persist("/metadata/foo_db/schemas/foo_schema/tables", "");
        verify(tableMetaDataPersistService).persist("foo_db", "foo_schema", Collections.emptyList());
    }
    
    @Test
    void assertAlterByRefreshWithTables() {
        ShardingSphereTable table = mock(ShardingSphereTable.class);
        when(table.getName()).thenReturn("foo_tbl");
        persistService.alterByRefresh("foo_db", new ShardingSphereSchema("foo_schema", Collections.singleton(table), Collections.emptyList()));
        verify(repository, times(0)).persist("/metadata/foo_db/schemas/foo_schema/tables", "");
        verify(tableMetaDataPersistService).persist("foo_db", "foo_schema", Collections.singletonList(table));
    }
    
    @Test
    void assertAlterByRefreshWithViews() {
        ShardingSphereView view = mock(ShardingSphereView.class);
        when(view.getName()).thenReturn("foo_view");
        persistService.alterByRefresh("foo_db", new ShardingSphereSchema("foo_schema", Collections.emptyList(), Collections.singleton(view)));
        verify(repository, times(0)).persist("/metadata/foo_db/schemas/foo_schema/tables", "");
        verify(tableMetaDataPersistService).persist("foo_db", "foo_schema", Collections.emptyList());
    }
    
    @Test
    void assertLoad() {
        when(repository.getChildrenKeys("/metadata/foo_db/schemas")).thenReturn(Collections.singletonList("foo_schema"));
        ShardingSphereTable table = mock(ShardingSphereTable.class);
        when(table.getName()).thenReturn("foo_tbl");
        when(tableMetaDataPersistService.load("foo_db", "foo_schema")).thenReturn(Collections.singleton(table));
        ShardingSphereView view = mock(ShardingSphereView.class);
        when(view.getName()).thenReturn("foo_view");
        when(viewMetaDataPersistService.load("foo_db", "foo_schema")).thenReturn(Collections.singleton(view));
        Collection<ShardingSphereSchema> actual = persistService.load("foo_db");
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next().getTable("foo_tbl"), is(table));
        assertThat(actual.iterator().next().getView("foo_view"), is(view));
    }
    
    @Test
    void assertAlterSchemaByAlterConfigurationByRefresh() {
        persistService.alterByRuleAltered("foo_db", new ShardingSphereSchema("foo_schema"));
        verify(repository).persist("/metadata/foo_db/schemas/foo_schema/tables", "");
        verify(tableMetaDataPersistService).persist(eq("foo_db"), eq("foo_schema"), anyCollection());
    }
    
    @Test
    void assertAlterSchemaByAlterConfigurationWithNotEmptyTablesByRefresh() {
        ShardingSphereTable table = mock(ShardingSphereTable.class);
        when(table.getName()).thenReturn("foo_tbl");
        persistService.alterByRuleAltered("foo_db", new ShardingSphereSchema("foo_schema", Collections.singletonList(table), Collections.emptyList()));
        verify(repository, times(0)).persist("/metadata/foo_db/schemas/foo_schema/tables", "");
        verify(tableMetaDataPersistService).persist(eq("foo_db"), eq("foo_schema"), anyCollection());
    }
    
    @Test
    void assertAlterSchemaByAlterConfigurationWithNotEmptyViewsByRefresh() {
        ShardingSphereView view = mock(ShardingSphereView.class);
        when(view.getName()).thenReturn("foo_view");
        persistService.alterByRuleAltered("foo_db", new ShardingSphereSchema("foo_schema", Collections.emptyList(), Collections.singleton(view)));
        verify(repository, times(0)).persist("/metadata/foo_db/schemas/foo_schema/tables", "");
        verify(tableMetaDataPersistService).persist(eq("foo_db"), eq("foo_schema"), anyCollection());
    }
    
    @Test
    void assertAlterByRefreshByDropConfiguration() {
        persistService.alterByRuleDropped("foo_db", new ShardingSphereSchema("foo_schema"));
        verify(tableMetaDataPersistService).persist(eq("foo_db"), eq("foo_schema"), anyCollection());
    }
}
