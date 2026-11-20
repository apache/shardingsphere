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

package org.apache.shardingsphere.mode.manager.cluster.dispatch.handler.database.metadata;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.instance.metadata.InstanceType;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereStatistics;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.event.DataChangedEvent;
import org.apache.shardingsphere.mode.event.DataChangedEvent.Type;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.cluster.persist.service.ClusterMetaDataManagerPersistService;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.manager.MetaDataContextManager;
import org.apache.shardingsphere.mode.metadata.manager.database.DatabaseMetaDataManager;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistFacade;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Types;
import java.util.Collections;
import java.util.Optional;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TableChangedHandlerTest {
    
    private TableChangedHandler handler;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ContextManager contextManager;
    
    @BeforeEach
    void setUp() {
        handler = new TableChangedHandler(contextManager);
    }
    
    @Test
    void assertHandleTableCreated() {
        ShardingSphereTable table = mock(ShardingSphereTable.class);
        when(contextManager.getPersistServiceFacade().getMetaDataFacade().getDatabaseMetaDataFacade().getTable().load("foo_db", "foo_schema", "foo_tbl"))
                .thenReturn(Optional.of(table));
        handler.handle("foo_db", new DataChangedEvent("/metadata/foo_db/schemas/foo_schema/tables/foo_tbl/active_version", "0", Type.ADDED));
        verify(contextManager.getMetaDataContextManager().getDatabaseMetaDataManager()).alterTable("foo_db", "foo_schema", table);
    }
    
    @Test
    void assertHandleTableAltered() {
        ShardingSphereTable table = mock(ShardingSphereTable.class);
        when(contextManager.getPersistServiceFacade().getMetaDataFacade().getDatabaseMetaDataFacade().getTable().load("foo_db", "foo_schema", "foo_tbl"))
                .thenReturn(Optional.of(table));
        handler.handle("foo_db", new DataChangedEvent("/metadata/foo_db/schemas/foo_schema/tables/foo_tbl/active_version", "0", Type.UPDATED));
        verify(contextManager.getMetaDataContextManager().getDatabaseMetaDataManager()).alterTable("foo_db", "foo_schema", table);
    }
    
    @Test
    void assertHandleTableDropped() {
        handler.handle("foo_db", new DataChangedEvent("/metadata/foo_db/schemas/foo_schema/tables/foo_tbl", "", Type.DELETED));
        verify(contextManager.getMetaDataContextManager().getDatabaseMetaDataManager()).dropTable("foo_db", "foo_schema", "foo_tbl");
    }
    
    @Test
    void assertInvalidateBindingsAfterChangedTableIsApplied() {
        ShardingSphereTable previousTable = createTable(Types.INTEGER);
        ShardingSphereDatabase database = createDatabase(previousTable);
        ShardingSphereTable alteredTable = createTable(Types.VARCHAR);
        createPersistService().alterTables(database, "foo_schema", Collections.singleton(alteredTable));
        prepareTableEvent(alteredTable);
        assertThat(database.getSchema("foo_schema").getTable("foo_tbl"), sameInstance(previousTable));
        assertFalse(previousTable.isBindingInvalidated());
        handler.handle("foo_db", new DataChangedEvent("/metadata/foo_db/schemas/foo_schema/tables/foo_tbl/active_version", "1", Type.UPDATED));
        assertThat(database.getSchema("foo_schema").getTable("foo_tbl"), sameInstance(alteredTable));
        assertTrue(previousTable.isBindingInvalidated());
        assertFalse(alteredTable.isBindingInvalidated());
    }
    
    private ShardingSphereTable createTable(final int dataType) {
        ShardingSphereColumn column = new ShardingSphereColumn("foo_col", dataType, false, false, false, true, false, false);
        return new ShardingSphereTable("foo_tbl", Collections.singleton(column), Collections.emptyList(), Collections.emptyList());
    }
    
    private ShardingSphereDatabase createDatabase(final ShardingSphereTable table) {
        DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
        ConfigurationProperties props = new ConfigurationProperties(new Properties());
        ShardingSphereSchema schema = new ShardingSphereSchema("foo_schema", databaseType, Collections.singleton(table), Collections.emptyList());
        ShardingSphereDatabase result = new ShardingSphereDatabase("foo_db", databaseType, new ResourceMetaData(Collections.emptyMap()), new RuleMetaData(Collections.emptyList()),
                Collections.singleton(schema), props);
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(Collections.singleton(result), new ResourceMetaData(Collections.emptyMap()), new RuleMetaData(Collections.emptyList()), props,
                databaseType);
        DatabaseMetaDataManager manager = new DatabaseMetaDataManager(new MetaDataContexts(metaData, new ShardingSphereStatistics()), contextManager.getPersistServiceFacade().getMetaDataFacade());
        when(contextManager.getMetaDataContextManager().getDatabaseMetaDataManager()).thenReturn(manager);
        when(contextManager.getComputeNodeInstanceContext().getInstance().getMetaData().getType()).thenReturn(InstanceType.JDBC);
        return result;
    }
    
    private ClusterMetaDataManagerPersistService createPersistService() {
        MetaDataPersistFacade metaDataPersistFacade = contextManager.getPersistServiceFacade().getMetaDataFacade();
        MetaDataContextManager metaDataContextManager = contextManager.getMetaDataContextManager();
        when(metaDataContextManager.getMetaDataPersistFacade()).thenReturn(metaDataPersistFacade);
        return new ClusterMetaDataManagerPersistService(metaDataContextManager, mock(ClusterPersistRepository.class));
    }
    
    private void prepareTableEvent(final ShardingSphereTable table) {
        verify(contextManager.getPersistServiceFacade().getMetaDataFacade().getDatabaseMetaDataFacade().getTable()).persist("foo_db", "foo_schema", Collections.singleton(table));
        when(contextManager.getPersistServiceFacade().getMetaDataFacade().getDatabaseMetaDataFacade().getTable().load("foo_db", "foo_schema", "foo_tbl")).thenReturn(Optional.of(table));
    }
    
    @Test
    void assertKeepBindingsAfterUnchangedTableIsApplied() {
        ShardingSphereTable previousTable = createTable(Types.INTEGER);
        ShardingSphereDatabase database = createDatabase(previousTable);
        ShardingSphereTable unchangedTable = createTable(Types.INTEGER);
        createPersistService().alterTables(database, "foo_schema", Collections.singleton(unchangedTable));
        prepareTableEvent(unchangedTable);
        assertThat(database.getSchema("foo_schema").getTable("foo_tbl"), sameInstance(previousTable));
        assertFalse(previousTable.isBindingInvalidated());
        handler.handle("foo_db", new DataChangedEvent("/metadata/foo_db/schemas/foo_schema/tables/foo_tbl/active_version", "1", Type.UPDATED));
        assertThat(database.getSchema("foo_schema").getTable("foo_tbl"), sameInstance(unchangedTable));
        assertFalse(previousTable.isBindingInvalidated());
        assertFalse(unchangedTable.isBindingInvalidated());
    }
    
    @Test
    void assertInvalidateBindingsAfterDroppedTableIsApplied() {
        ShardingSphereTable previousTable = createTable(Types.INTEGER);
        ShardingSphereDatabase database = createDatabase(previousTable);
        createPersistService().dropTables(database, "foo_schema", Collections.singleton("foo_tbl"));
        verify(contextManager.getPersistServiceFacade().getMetaDataFacade().getDatabaseMetaDataFacade().getTable()).drop("foo_db", "foo_schema", "foo_tbl");
        assertThat(database.getSchema("foo_schema").getTable("foo_tbl"), sameInstance(previousTable));
        assertFalse(previousTable.isBindingInvalidated());
        handler.handle("foo_db", new DataChangedEvent("/metadata/foo_db/schemas/foo_schema/tables/foo_tbl", "", Type.DELETED));
        assertFalse(database.getSchema("foo_schema").containsTable("foo_tbl"));
        assertTrue(previousTable.isBindingInvalidated());
    }
}
