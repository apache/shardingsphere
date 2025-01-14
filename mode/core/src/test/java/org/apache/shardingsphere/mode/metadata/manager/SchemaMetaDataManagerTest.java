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

package org.apache.shardingsphere.mode.metadata.manager;

import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereView;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.spi.repository.PersistRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.sql.Types;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SchemaMetaDataManagerTest {
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private MetaDataContexts metaDataContexts;
    
    private SchemaMetaDataManager schemaMetaDataManager;
    
    @BeforeEach
    void setUp() {
        ShardingSphereDatabase database = mockDatabase();
        when(metaDataContexts.getMetaData().containsDatabase("foo_db")).thenReturn(true);
        when(metaDataContexts.getMetaData().getDatabase("foo_db")).thenReturn(database);
        when(metaDataContexts.getMetaData().getAllDatabases()).thenReturn(Collections.singleton(database));
        schemaMetaDataManager = new SchemaMetaDataManager(metaDataContexts, mock(PersistRepository.class));
    }
    
    private ShardingSphereDatabase mockDatabase() {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(result.getName()).thenReturn("foo_db");
        when(result.getProtocolType()).thenReturn(TypedSPILoader.getService(DatabaseType.class, "FIXTURE"));
        when(result.containsSchema("foo_schema")).thenReturn(true);
        return result;
    }
    
    @Test
    void assertAddNotExistedDatabase() {
        schemaMetaDataManager.addDatabase("new_db");
        verify(metaDataContexts.getMetaData()).addDatabase(eq("new_db"), any(DatabaseType.class), any(ConfigurationProperties.class));
    }
    
    @Test
    void assertAddExistedDatabase() {
        when(metaDataContexts.getMetaData().containsDatabase("foo_db")).thenReturn(true);
        schemaMetaDataManager.addDatabase("foo_db");
        verify(metaDataContexts.getMetaData(), times(0)).addDatabase(eq("foo_db"), any(DatabaseType.class), any(ConfigurationProperties.class));
    }
    
    @Test
    void assertDropExistedDatabase() {
        when(metaDataContexts.getMetaData().getDatabase("foo_db").getName()).thenReturn("foo_db");
        when(metaDataContexts.getMetaData().containsDatabase("foo_db")).thenReturn(true);
        schemaMetaDataManager.dropDatabase("foo_db");
        verify(metaDataContexts.getMetaData()).dropDatabase("foo_db");
    }
    
    @Test
    void assertDropNotExistedDatabase() {
        schemaMetaDataManager.dropDatabase("not_existed_db");
        verify(metaDataContexts.getMetaData(), times(0)).dropDatabase("not_existed_db");
    }
    
    @Test
    void assertAddNotExistedSchema() {
        schemaMetaDataManager.addSchema("foo_db", "bar_schema");
        verify(metaDataContexts.getMetaData().getDatabase("foo_db")).addSchema(any(ShardingSphereSchema.class));
    }
    
    @Test
    void assertAddExistedSchema() {
        when(metaDataContexts.getMetaData().getDatabase("foo_db").containsSchema("foo_schema")).thenReturn(true);
        schemaMetaDataManager.addSchema("foo_db", "foo_schema");
        verify(metaDataContexts.getMetaData().getDatabase("foo_db"), times(0)).addSchema(any(ShardingSphereSchema.class));
    }
    
    @Test
    void assertDropNotExistedSchema() {
        schemaMetaDataManager.dropSchema("foo_db", "bar_schema");
        verify(metaDataContexts.getMetaData().getDatabase("foo_db"), times(0)).dropSchema(anyString());
    }
    
    @Test
    void assertDropExistedSchema() {
        schemaMetaDataManager.dropSchema("foo_db", "foo_schema");
        verify(metaDataContexts.getMetaData().getDatabase("foo_db")).dropSchema("foo_schema");
    }
    
    @Test
    void assertAlterNotExistedSchema() {
        ShardingSphereSchema toBeAlteredSchema = createToBeAlteredSchema();
        when(metaDataContexts.getMetaData().getDatabase("foo_db").getAllSchemas()).thenReturn(Collections.singleton(toBeAlteredSchema));
        when(metaDataContexts.getMetaData().getDatabase("foo_db").getSchema("foo_schema")).thenReturn(toBeAlteredSchema);
        schemaMetaDataManager.alterSchema("foo_db", "bar_schema", null, (ShardingSphereView) null);
        verify(metaDataContexts.getMetaData().getDatabase("foo_db"), times(0)).getSchema(any());
    }
    
    @Test
    void assertAlterSchemaForNothingAltered() {
        ShardingSphereSchema toBeAlteredSchema = createToBeAlteredSchema();
        when(metaDataContexts.getMetaData().getDatabase("foo_db").getAllSchemas()).thenReturn(Collections.singleton(toBeAlteredSchema));
        when(metaDataContexts.getMetaData().getDatabase("foo_db").getSchema("foo_schema")).thenReturn(toBeAlteredSchema);
        schemaMetaDataManager.alterSchema("foo_db", "foo_schema", null, (ShardingSphereView) null);
        verify(metaDataContexts.getMetaData().getDatabase("foo_db"), times(0)).getSchema(any());
    }
    
    @Test
    void assertAlterSchemaForTableAltered() {
        ShardingSphereSchema toBeAlteredSchema = createToBeAlteredSchema();
        when(metaDataContexts.getMetaData().getDatabase("foo_db").getAllSchemas()).thenReturn(Collections.singleton(toBeAlteredSchema));
        when(metaDataContexts.getMetaData().getDatabase("foo_db").getSchema("foo_schema")).thenReturn(toBeAlteredSchema);
        ShardingSphereColumn toBeChangedColumn = new ShardingSphereColumn("foo_col", Types.VARCHAR, false, false, false, true, false, false);
        ShardingSphereTable toBeChangedTable = new ShardingSphereTable("foo_tbl", Collections.singleton(toBeChangedColumn), Collections.emptyList(), Collections.emptyList());
        schemaMetaDataManager.alterSchema("foo_db", "foo_schema", toBeChangedTable, null);
        ShardingSphereTable table = metaDataContexts.getMetaData().getDatabase("foo_db").getSchema("foo_schema").getTable("foo_tbl");
        assertThat(table.getAllColumns().size(), is(1));
        assertTrue(table.containsColumn("foo_col"));
    }
    
    @Test
    void assertAlterSchemaForViewAltered() {
        ShardingSphereSchema toBeAlteredSchema = createToBeAlteredSchema();
        when(metaDataContexts.getMetaData().getDatabase("foo_db").getAllSchemas()).thenReturn(Collections.singleton(toBeAlteredSchema));
        when(metaDataContexts.getMetaData().getDatabase("foo_db").getSchema("foo_schema")).thenReturn(toBeAlteredSchema);
        ShardingSphereView toBeChangedView = new ShardingSphereView("foo_view", "select `foo_view`.`foo_view`.`id` AS `id` from `foo_view`.`foo_view`");
        schemaMetaDataManager.alterSchema("foo_db", "foo_schema", null, toBeChangedView);
        ShardingSphereView view = metaDataContexts.getMetaData().getDatabase("foo_db").getSchema("foo_schema").getView("foo_view");
        assertThat(view.getName(), is("foo_view"));
        assertThat(view.getViewDefinition(), is("select `foo_view`.`foo_view`.`id` AS `id` from `foo_view`.`foo_view`"));
    }
    
    @Test
    void assertAlterNotExistedSchemaForTableDropped() {
        ShardingSphereSchema toBeAlteredSchema = createToBeAlteredSchema();
        when(metaDataContexts.getMetaData().getDatabase("foo_db").getAllSchemas()).thenReturn(Collections.singleton(toBeAlteredSchema));
        when(metaDataContexts.getMetaData().getDatabase("foo_db").getSchema("foo_schema")).thenReturn(toBeAlteredSchema);
        schemaMetaDataManager.alterSchema("foo_db", "bar_schema", "", "");
        verify(metaDataContexts.getMetaData().getDatabase("foo_db"), times(0)).getSchema(any());
    }
    
    @Test
    void assertAlterSchemaForNothingTableDropped() {
        ShardingSphereSchema toBeAlteredSchema = createToBeAlteredSchema();
        when(metaDataContexts.getMetaData().getDatabase("foo_db").getAllSchemas()).thenReturn(Collections.singleton(toBeAlteredSchema));
        when(metaDataContexts.getMetaData().getDatabase("foo_db").getSchema("foo_schema")).thenReturn(toBeAlteredSchema);
        schemaMetaDataManager.alterSchema("foo_db", "foo_schema", "", "");
        verify(metaDataContexts.getMetaData().getGlobalRuleMetaData(), times(0)).getRules();
    }
    
    @Test
    void assertAlterSchemaForTableDropped() {
        when(metaDataContexts.getMetaData().getDatabase("foo_db").getAllSchemas()).thenReturn(Collections.singleton(createToBeAlteredSchema()));
        schemaMetaDataManager.alterSchema("foo_db", "foo_schema", "foo_tbl", null);
        assertFalse(metaDataContexts.getMetaData().getDatabase("foo_db").getSchema("foo_schema").containsTable("foo_tbl"));
    }
    
    @Test
    void assertAlterSchemaForViewDropped() {
        when(metaDataContexts.getMetaData().getDatabase("foo_db").getAllSchemas()).thenReturn(Collections.singleton(createToBeAlteredSchema()));
        schemaMetaDataManager.alterSchema("foo_db", "foo_schema", "foo_view", null);
        assertFalse(metaDataContexts.getMetaData().getDatabase("foo_db").getSchema("foo_schema").containsView("foo_view"));
    }
    
    private ShardingSphereSchema createToBeAlteredSchema() {
        ShardingSphereTable beforeChangedTable = new ShardingSphereTable("foo_tbl", Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        ShardingSphereView beforeChangedView = new ShardingSphereView("foo_view", "");
        return new ShardingSphereSchema("foo_schema", Collections.singleton(beforeChangedTable), Collections.singleton(beforeChangedView));
    }
}
