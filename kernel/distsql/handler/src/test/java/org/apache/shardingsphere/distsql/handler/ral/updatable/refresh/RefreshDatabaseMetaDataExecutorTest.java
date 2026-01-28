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

import org.apache.shardingsphere.distsql.statement.type.ral.updatable.RefreshDatabaseMetaDataStatement;
import org.apache.shardingsphere.infra.metadata.database.schema.util.SystemSchemaUtils;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import java.util.Arrays;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RefreshDatabaseMetaDataExecutorTest {
    
    private final RefreshDatabaseMetaDataExecutor executor = new RefreshDatabaseMetaDataExecutor();
    
    @Test
    void assertExecuteWithDatabaseName() {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(contextManager.getDatabase("foo_db")).thenReturn(database);
        try (MockedStatic<SystemSchemaUtils> mockedStatic = mockStatic(SystemSchemaUtils.class)) {
            mockedStatic.when(() -> SystemSchemaUtils.isSystemSchema(database)).thenReturn(false);
            executor.executeUpdate(new RefreshDatabaseMetaDataStatement("foo_db", false), contextManager);
        }
        verify(contextManager).reloadDatabase(database);
    }
    
    @Test
    void assertExecuteWithDatabases() {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        ShardingSphereDatabase systemDatabase = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getName()).thenReturn("foo_db");
        when(systemDatabase.getName()).thenReturn("information_schema");
        when(contextManager.getMetaDataContexts().getMetaData().getAllDatabases()).thenReturn(Arrays.asList(database, systemDatabase));
        try (MockedStatic<SystemSchemaUtils> mockedStatic = mockStatic(SystemSchemaUtils.class)) {
            mockedStatic.when(() -> SystemSchemaUtils.isSystemSchema(systemDatabase)).thenReturn(true);
            mockedStatic.when(() -> SystemSchemaUtils.isSystemSchema(database)).thenReturn(false);
            executor.executeUpdate(new RefreshDatabaseMetaDataStatement(null, false), contextManager);
        }
        verify(contextManager).reloadDatabase(database);
    }
}
