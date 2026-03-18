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

import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.DialectDatabaseMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.schema.DialectSchemaOption;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
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
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RefreshTableMetaDataExecutorTest {
    
    private final DatabaseType databaseType = mock(DatabaseType.class);
    
    private final RefreshTableMetaDataExecutor executor = (RefreshTableMetaDataExecutor) TypedSPILoader.getService(DistSQLUpdateExecutor.class, RefreshTableMetaDataStatement.class);
    
    @Test
    void assertExecuteUpdateWithReloadTableWithStorageUnit() {
        ShardingSphereDatabase database = mockDatabase(true);
        ShardingSphereSchema schema = mock(ShardingSphereSchema.class);
        when(schema.containsTable("t_order")).thenReturn(true);
        when(database.getSchema("logic_schema")).thenReturn(schema);
        ContextManager contextManager = mock(ContextManager.class);
        executor.setDatabase(database);
        executor.executeUpdate(new RefreshTableMetaDataStatement("t_order", "ds_0", "logic_schema"), contextManager);
        verify(contextManager).reloadTable(database, "logic_schema", "ds_0", "t_order");
    }
    
    @Test
    void assertExecuteUpdateWithReloadSchemaWithStorageUnit() {
        RefreshTableMetaDataStatement sqlStatement = new RefreshTableMetaDataStatement(null, "ds_0", "logic_schema");
        ShardingSphereDatabase database = mockDatabase(true);
        ContextManager contextManager = mock(ContextManager.class);
        executor.setDatabase(database);
        executor.executeUpdate(sqlStatement, contextManager);
        verify(contextManager).reloadSchema(database, "logic_schema", "ds_0");
    }
    
    @Test
    void assertExecuteUpdateWithReloadTableWithoutStorageUnit() {
        RefreshTableMetaDataStatement sqlStatement = new RefreshTableMetaDataStatement("t_order", null, null);
        ShardingSphereDatabase database = mockDatabase(true);
        when(database.getProtocolType()).thenReturn(databaseType);
        DialectDatabaseMetaData dialectDatabaseMetaData = mockDialectDatabaseMetaData();
        ShardingSphereSchema schema = mock(ShardingSphereSchema.class);
        when(schema.containsTable("t_order")).thenReturn(true);
        when(database.getSchema("public")).thenReturn(schema);
        ContextManager contextManager = mock(ContextManager.class);
        executor.setDatabase(database);
        try (MockedStatic<DatabaseTypedSPILoader> mockedStatic = mockStatic(DatabaseTypedSPILoader.class)) {
            mockedStatic.when(() -> DatabaseTypedSPILoader.getService(DialectDatabaseMetaData.class, databaseType)).thenReturn(dialectDatabaseMetaData);
            executor.executeUpdate(sqlStatement, contextManager);
        }
        verify(contextManager).reloadTable(database, "public", "t_order");
    }
    
    @Test
    void assertExecuteUpdateWithReloadDatabase() {
        RefreshTableMetaDataStatement sqlStatement = new RefreshTableMetaDataStatement(null, null, null);
        ShardingSphereDatabase database = mockDatabase(true);
        when(database.getProtocolType()).thenReturn(databaseType);
        DialectDatabaseMetaData dialectDatabaseMetaData = mockDialectDatabaseMetaData();
        ContextManager contextManager = mock(ContextManager.class);
        executor.setDatabase(database);
        try (MockedStatic<DatabaseTypedSPILoader> mockedStatic = mockStatic(DatabaseTypedSPILoader.class)) {
            mockedStatic.when(() -> DatabaseTypedSPILoader.getService(DialectDatabaseMetaData.class, databaseType)).thenReturn(dialectDatabaseMetaData);
            executor.executeUpdate(sqlStatement, contextManager);
        }
        verify(contextManager).reloadDatabase(database);
    }
    
    @Test
    void assertExecuteUpdateWithEmptyStorageUnits() {
        RefreshTableMetaDataStatement sqlStatement = new RefreshTableMetaDataStatement(null, null, null);
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class);
        when(database.getName()).thenReturn("logic_db");
        when(database.getProtocolType()).thenReturn(databaseType);
        ResourceMetaData resourceMetaData = mock(ResourceMetaData.class);
        when(database.getResourceMetaData()).thenReturn(resourceMetaData);
        DialectDatabaseMetaData dialectDatabaseMetaData = mockDialectDatabaseMetaData();
        executor.setDatabase(database);
        try (MockedStatic<DatabaseTypedSPILoader> mockedStatic = mockStatic(DatabaseTypedSPILoader.class)) {
            mockedStatic.when(() -> DatabaseTypedSPILoader.getService(DialectDatabaseMetaData.class, databaseType)).thenReturn(dialectDatabaseMetaData);
            assertThrows(EmptyStorageUnitException.class, () -> executor.executeUpdate(sqlStatement, mock(ContextManager.class)));
        }
    }
    
    @Test
    void assertExecuteUpdateWithMissingStorageUnit() {
        RefreshTableMetaDataStatement sqlStatement = new RefreshTableMetaDataStatement(null, "miss_ds", null);
        ShardingSphereDatabase database = mockDatabase(true);
        when(database.getProtocolType()).thenReturn(databaseType);
        DialectDatabaseMetaData dialectDatabaseMetaData = mockDialectDatabaseMetaData();
        executor.setDatabase(database);
        try (MockedStatic<DatabaseTypedSPILoader> mockedStatic = mockStatic(DatabaseTypedSPILoader.class)) {
            mockedStatic.when(() -> DatabaseTypedSPILoader.getService(DialectDatabaseMetaData.class, databaseType)).thenReturn(dialectDatabaseMetaData);
            assertThrows(MissingRequiredStorageUnitsException.class, () -> executor.executeUpdate(sqlStatement, mock(ContextManager.class)));
        }
    }
    
    @Test
    void assertExecuteUpdateWhenSchemaMissing() {
        RefreshTableMetaDataStatement sqlStatement = new RefreshTableMetaDataStatement(null, null, "absent_schema");
        ShardingSphereDatabase database = mockDatabase(false);
        executor.setDatabase(database);
        assertThrows(SchemaNotFoundException.class, () -> executor.executeUpdate(sqlStatement, mock(ContextManager.class)));
    }
    
    @Test
    void assertExecuteUpdateWhenTableMissing() {
        ShardingSphereDatabase database = mockDatabase(true);
        ShardingSphereSchema schema = mock(ShardingSphereSchema.class);
        when(schema.containsTable("missing_table")).thenReturn(false);
        when(database.getSchema("logic_schema")).thenReturn(schema);
        executor.setDatabase(database);
        assertThrows(TableNotFoundException.class, () -> executor.executeUpdate(new RefreshTableMetaDataStatement("missing_table", null, "logic_schema"), mock(ContextManager.class)));
    }
    
    private ShardingSphereDatabase mockDatabase(final boolean schemaExists) {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(result.getName()).thenReturn("logic_db");
        when(result.getResourceMetaData().getStorageUnits()).thenReturn(Collections.singletonMap("ds_0", mock(StorageUnit.class)));
        when(result.containsSchema(anyString())).thenReturn(schemaExists);
        return result;
    }
    
    private DialectDatabaseMetaData mockDialectDatabaseMetaData() {
        DialectDatabaseMetaData result = mock(DialectDatabaseMetaData.class);
        DialectSchemaOption schemaOption = mock(DialectSchemaOption.class);
        when(schemaOption.getDefaultSchema()).thenReturn(Optional.of("public"));
        when(result.getSchemaOption()).thenReturn(schemaOption);
        return result;
    }
}
