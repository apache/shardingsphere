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

package org.apache.shardingsphere.proxy.backend.handler.distsql.ral.updatable.refresh;

import org.apache.shardingsphere.distsql.handler.engine.update.DistSQLUpdateExecutor;
import org.apache.shardingsphere.distsql.statement.type.ral.updatable.RefreshTableMetaDataStatement;
import org.apache.shardingsphere.infra.exception.kernel.metadata.SchemaNotFoundException;
import org.apache.shardingsphere.infra.exception.kernel.metadata.TableNotFoundException;
import org.apache.shardingsphere.infra.exception.kernel.metadata.resource.storageunit.EmptyStorageUnitException;
import org.apache.shardingsphere.infra.exception.kernel.metadata.resource.storageunit.MissingRequiredStorageUnitsException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RefreshTableMetaDataExecutorTest {
    
    private final RefreshTableMetaDataExecutor executor = (RefreshTableMetaDataExecutor) TypedSPILoader.getService(DistSQLUpdateExecutor.class, RefreshTableMetaDataStatement.class);
    
    @Test
    void assertExecuteUpdateWithReloadTableWithStorageUnit() {
        ShardingSphereDatabase database = mockDatabase(true);
        ShardingSphereSchema schema = mock(ShardingSphereSchema.class);
        when(schema.getName()).thenReturn("logic_schema");
        when(schema.containsTable(new IdentifierValue("t_order"))).thenReturn(true);
        when(database.getSchema(new IdentifierValue("logic_schema"))).thenReturn(schema);
        ContextManager contextManager = mock(ContextManager.class);
        executor.setDatabase(database);
        executor.executeUpdate(new RefreshTableMetaDataStatement(new IdentifierValue("t_order"), "ds_0", new IdentifierValue("logic_schema")), contextManager);
        verify(contextManager).reloadTable(database, "logic_schema", "ds_0", new IdentifierValue("t_order"));
    }
    
    @Test
    void assertExecuteUpdateWithReloadSchemaWithStorageUnit() {
        ShardingSphereDatabase database = mockDatabase(true);
        ShardingSphereSchema schema = mock(ShardingSphereSchema.class);
        when(schema.getName()).thenReturn("logic_schema");
        when(database.getSchema(new IdentifierValue("logic_schema"))).thenReturn(schema);
        ContextManager contextManager = mock(ContextManager.class);
        executor.setDatabase(database);
        executor.executeUpdate(new RefreshTableMetaDataStatement(null, "ds_0", new IdentifierValue("logic_schema")), contextManager);
        verify(contextManager).reloadSchema(database, "logic_schema", "ds_0");
    }
    
    @Test
    void assertExecuteUpdateWithReloadTableWithoutStorageUnit() {
        ShardingSphereDatabase database = mockDatabase(true);
        when(database.getDefaultSchemaName()).thenReturn("foo_default_schema");
        ShardingSphereSchema schema = mock(ShardingSphereSchema.class);
        when(schema.getName()).thenReturn("foo_default_schema");
        when(schema.containsTable(new IdentifierValue("t_order"))).thenReturn(true);
        when(database.getSchema(new IdentifierValue("foo_default_schema"))).thenReturn(schema);
        ContextManager contextManager = mock(ContextManager.class);
        executor.setDatabase(database);
        executor.executeUpdate(new RefreshTableMetaDataStatement(new IdentifierValue("t_order"), null, null), contextManager);
        verify(contextManager).reloadTable(database, "foo_default_schema", new IdentifierValue("t_order"));
    }
    
    @Test
    void assertExecuteUpdateWithReloadDatabase() {
        ShardingSphereDatabase database = mockDatabase(true);
        when(database.getDefaultSchemaName()).thenReturn("foo_default_schema");
        ContextManager contextManager = mock(ContextManager.class);
        executor.setDatabase(database);
        executor.executeUpdate(new RefreshTableMetaDataStatement(null, null, null), contextManager);
        verify(contextManager).reloadDatabase(database);
    }
    
    @Test
    void assertExecuteUpdateWithEmptyStorageUnits() {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class);
        when(database.getName()).thenReturn("logic_db");
        when(database.getDefaultSchemaName()).thenReturn("foo_default_schema");
        ResourceMetaData resourceMetaData = mock(ResourceMetaData.class);
        when(database.getResourceMetaData()).thenReturn(resourceMetaData);
        executor.setDatabase(database);
        assertThrows(EmptyStorageUnitException.class, () -> executor.executeUpdate(new RefreshTableMetaDataStatement(null, null, null), mock(ContextManager.class)));
    }
    
    @Test
    void assertExecuteUpdateWithMissingStorageUnit() {
        RefreshTableMetaDataStatement sqlStatement = new RefreshTableMetaDataStatement(null, "miss_ds", null);
        ShardingSphereDatabase database = mockDatabase(true);
        when(database.getDefaultSchemaName()).thenReturn("foo_default_schema");
        executor.setDatabase(database);
        assertThrows(MissingRequiredStorageUnitsException.class, () -> executor.executeUpdate(sqlStatement, mock(ContextManager.class)));
    }
    
    @Test
    void assertExecuteUpdateWhenSchemaMissing() {
        RefreshTableMetaDataStatement sqlStatement = new RefreshTableMetaDataStatement(null, null, new IdentifierValue("absent_schema"));
        ShardingSphereDatabase database = mockDatabase(false);
        executor.setDatabase(database);
        assertThrows(SchemaNotFoundException.class, () -> executor.executeUpdate(sqlStatement, mock(ContextManager.class)));
    }
    
    @Test
    void assertExecuteUpdateWhenTableMissing() {
        ShardingSphereDatabase database = mockDatabase(true);
        ShardingSphereSchema schema = mock(ShardingSphereSchema.class);
        when(schema.containsTable(new IdentifierValue("missing_table"))).thenReturn(false);
        when(database.getSchema(new IdentifierValue("logic_schema"))).thenReturn(schema);
        executor.setDatabase(database);
        assertThrows(TableNotFoundException.class,
                () -> executor.executeUpdate(new RefreshTableMetaDataStatement(new IdentifierValue("missing_table"), null, new IdentifierValue("logic_schema")), mock(ContextManager.class)));
    }
    
    private ShardingSphereDatabase mockDatabase(final boolean schemaExists) {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(result.getName()).thenReturn("logic_db");
        when(result.getResourceMetaData().getStorageUnits()).thenReturn(Collections.singletonMap("ds_0", mock(StorageUnit.class)));
        when(result.containsSchema(anyString())).thenReturn(schemaExists);
        when(result.containsSchema(any(IdentifierValue.class))).thenReturn(schemaExists);
        return result;
    }
}
