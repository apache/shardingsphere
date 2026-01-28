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

package org.apache.shardingsphere.distsql.handler.ral.updatable.refresh;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.distsql.statement.type.ral.updatable.RefreshTableMetaDataStatement;
import org.apache.shardingsphere.infra.exception.kernel.metadata.SchemaNotFoundException;
import org.apache.shardingsphere.infra.exception.kernel.metadata.TableNotFoundException;
import org.apache.shardingsphere.infra.exception.kernel.metadata.resource.storageunit.EmptyStorageUnitException;
import org.apache.shardingsphere.infra.exception.kernel.metadata.resource.storageunit.MissingRequiredStorageUnitsException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RefreshTableMetaDataExecutorTest {
    
    private final RefreshTableMetaDataExecutor executor = new RefreshTableMetaDataExecutor();
    
    @Test
    void assertExecuteWithSchemaAndStorageUnit() {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        ShardingSphereDatabase database = mockDatabase();
        executor.setDatabase(database);
        executor.executeUpdate(new RefreshTableMetaDataStatement("foo_table", "foo_ds", "foo_schema"), contextManager);
        verify(contextManager).reloadTable(database, "foo_schema", "foo_ds", "foo_table");
    }
    
    @Test
    void assertExecuteWithMissingSchema() {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        ShardingSphereDatabase database = mockDatabase();
        when(database.containsSchema("missing")).thenReturn(false);
        executor.setDatabase(database);
        assertThrows(SchemaNotFoundException.class, () -> executor.executeUpdate(new RefreshTableMetaDataStatement("foo_table", "foo_ds", "missing"), contextManager));
    }
    
    @Test
    void assertExecuteWithMissingTable() {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        ShardingSphereDatabase database = mockDatabase();
        when(database.getSchema("foo_schema").containsTable("missing")).thenReturn(false);
        executor.setDatabase(database);
        assertThrows(TableNotFoundException.class, () -> executor.executeUpdate(new RefreshTableMetaDataStatement("missing", null, "foo_schema"), contextManager));
    }
    
    @Test
    void assertExecuteWithMissingStorageUnit() {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        ShardingSphereDatabase database = mockDatabase();
        executor.setDatabase(database);
        assertThrows(MissingRequiredStorageUnitsException.class, () -> executor.executeUpdate(new RefreshTableMetaDataStatement("foo_table", "missing", "foo_schema"), contextManager));
    }
    
    @Test
    void assertExecuteWithEmptyStorageUnits() {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        ResourceMetaData resourceMetaData = new ResourceMetaData(Collections.emptyMap(), Collections.emptyMap());
        when(database.getResourceMetaData()).thenReturn(resourceMetaData);
        when(database.getName()).thenReturn("foo_db");
        when(database.getProtocolType()).thenReturn(mock(DatabaseType.class));
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        executor.setDatabase(database);
        assertThrows(EmptyStorageUnitException.class, () -> executor.executeUpdate(new RefreshTableMetaDataStatement("foo_table", null, "foo_schema"), contextManager));
    }
    
    private ShardingSphereDatabase mockDatabase() {
        return mockDatabase(new HashMap<>());
    }
    
    private ShardingSphereDatabase mockDatabase(final Map<String, StorageUnit> storageUnits) {
        Map<String, StorageUnit> mutableStorageUnits = new HashMap<>(storageUnits);
        if (mutableStorageUnits.isEmpty()) {
            StorageUnit storageUnit = mock(StorageUnit.class, RETURNS_DEEP_STUBS);
            mutableStorageUnits.put("foo_ds", storageUnit);
        }
        ResourceMetaData resourceMetaData = new ResourceMetaData(Collections.emptyMap(), mutableStorageUnits);
        ShardingSphereSchema schema = mock(ShardingSphereSchema.class, RETURNS_DEEP_STUBS);
        when(schema.containsTable("foo_table")).thenReturn(true);
        when(schema.getTable("foo_table")).thenReturn(mock(ShardingSphereTable.class, RETURNS_DEEP_STUBS));
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getResourceMetaData()).thenReturn(resourceMetaData);
        when(database.containsSchema("foo_schema")).thenReturn(true);
        when(database.getSchema("foo_schema")).thenReturn(schema);
        when(database.getName()).thenReturn("foo_db");
        when(database.getProtocolType()).thenReturn(mock(DatabaseType.class));
        when(database.getRuleMetaData()).thenReturn(mock(RuleMetaData.class, RETURNS_DEEP_STUBS));
        return database;
    }
}
