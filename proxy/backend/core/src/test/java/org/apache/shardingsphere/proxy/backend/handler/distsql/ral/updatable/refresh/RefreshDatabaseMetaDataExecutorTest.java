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
import org.apache.shardingsphere.distsql.statement.type.ral.updatable.RefreshDatabaseMetaDataStatement;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.util.SystemSchemaUtils;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.Arrays;
import java.util.Collection;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RefreshDatabaseMetaDataExecutorTest {
    
    private final RefreshDatabaseMetaDataExecutor executor = (RefreshDatabaseMetaDataExecutor) TypedSPILoader.getService(DistSQLUpdateExecutor.class, RefreshDatabaseMetaDataStatement.class);
    
    @Test
    void assertRefreshSpecifiedDatabase() {
        ContextManager contextManager = mock(ContextManager.class);
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class);
        when(contextManager.getDatabase("db0")).thenReturn(database);
        try (MockedStatic<SystemSchemaUtils> mockedStatic = mockStatic(SystemSchemaUtils.class)) {
            mockedStatic.when(() -> SystemSchemaUtils.isSystemSchema(database)).thenReturn(false);
            executor.executeUpdate(new RefreshDatabaseMetaDataStatement("db0", false), contextManager);
            verify(contextManager).reloadDatabase(database);
        }
    }
    
    @Test
    void assertRefreshAllDatabasesExceptSystem() {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        ShardingSphereDatabase systemDatabase = mock(ShardingSphereDatabase.class);
        ShardingSphereDatabase normalDatabase = mock(ShardingSphereDatabase.class);
        Collection<ShardingSphereDatabase> databases = Arrays.asList(systemDatabase, normalDatabase);
        when(contextManager.getMetaDataContexts().getMetaData().getAllDatabases()).thenReturn(databases);
        try (MockedStatic<SystemSchemaUtils> mockedStatic = mockStatic(SystemSchemaUtils.class)) {
            mockedStatic.when(() -> SystemSchemaUtils.isSystemSchema(systemDatabase)).thenReturn(true);
            executor.executeUpdate(new RefreshDatabaseMetaDataStatement(null, false), contextManager);
            verify(contextManager).reloadDatabase(normalDatabase);
        }
    }
}
