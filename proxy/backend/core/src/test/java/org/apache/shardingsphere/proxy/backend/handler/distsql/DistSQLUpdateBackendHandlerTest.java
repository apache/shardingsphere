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

package org.apache.shardingsphere.proxy.backend.handler.distsql;

import org.apache.shardingsphere.distsql.statement.ral.updatable.RefreshTableMetaDataStatement;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
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
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.test.mock.AutoMockExtension;
import org.apache.shardingsphere.test.mock.StaticMockSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;

import java.sql.SQLException;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ProxyContext.class)
class DistSQLUpdateBackendHandlerTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ContextManager contextManager;
    
    @BeforeEach
    void setUp() {
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
    }
    
    @Test
    void assertEmptyStorageUnit() {
        when(contextManager.getDatabase("foo_db")).thenReturn(new ShardingSphereDatabase("foo_db", databaseType, mock(), mock(), Collections.emptyList()));
        DistSQLUpdateBackendHandler backendHandler = new DistSQLUpdateBackendHandler(new RefreshTableMetaDataStatement(), mockConnectionSession("foo_db"));
        assertThrows(EmptyStorageUnitException.class, backendHandler::execute);
    }
    
    @Test
    void assertMissingRequiredStorageUnit() {
        ResourceMetaData resourceMetaData = mock(ResourceMetaData.class);
        when(resourceMetaData.getStorageUnits()).thenReturn(Collections.singletonMap("ds_0", mock(StorageUnit.class)));
        when(contextManager.getDatabase("foo_db")).thenReturn(new ShardingSphereDatabase("foo_db", databaseType, resourceMetaData, mock(), Collections.emptyList()));
        DistSQLUpdateBackendHandler backendHandler = new DistSQLUpdateBackendHandler(new RefreshTableMetaDataStatement("t_order", "ds_1", null), mockConnectionSession("foo_db"));
        assertThrows(MissingRequiredStorageUnitsException.class, backendHandler::execute);
    }
    
    @Test
    void assertSchemaNotFound() {
        ResourceMetaData resourceMetaData = mock(ResourceMetaData.class);
        when(resourceMetaData.getStorageUnits()).thenReturn(Collections.singletonMap("ds_0", mock(StorageUnit.class)));
        when(contextManager.getDatabase("foo_db")).thenReturn(new ShardingSphereDatabase("foo_db", databaseType, resourceMetaData, mock(), Collections.emptyList()));
        DistSQLUpdateBackendHandler backendHandler = new DistSQLUpdateBackendHandler(new RefreshTableMetaDataStatement("t_order", "ds_0", "bar_db"), mockConnectionSession("foo_db"));
        assertThrows(SchemaNotFoundException.class, backendHandler::execute);
    }
    
    @Test
    void assertTableNotFound() {
        ResourceMetaData resourceMetaData = mock(ResourceMetaData.class);
        when(resourceMetaData.getStorageUnits()).thenReturn(Collections.singletonMap("ds_0", mock(StorageUnit.class)));
        ShardingSphereSchema schema = mock(ShardingSphereSchema.class);
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class);
        when(database.containsSchema("foo_db")).thenReturn(true);
        when(database.getSchema("foo_db")).thenReturn(schema);
        when(database.getResourceMetaData()).thenReturn(resourceMetaData);
        when(contextManager.getDatabase("foo_db")).thenReturn(database);
        DistSQLUpdateBackendHandler backendHandler = new DistSQLUpdateBackendHandler(new RefreshTableMetaDataStatement("t_order", "ds_0", "foo_db"), mockConnectionSession("foo_db"));
        assertThrows(TableNotFoundException.class, backendHandler::execute);
    }
    
    @Test
    void assertUpdate() throws SQLException {
        ResourceMetaData resourceMetaData = mock(ResourceMetaData.class);
        when(resourceMetaData.getStorageUnits()).thenReturn(Collections.singletonMap("ds_0", mock(StorageUnit.class)));
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class);
        when(database.containsSchema(any())).thenReturn(true);
        when(database.getProtocolType()).thenReturn(databaseType);
        when(database.getResourceMetaData()).thenReturn(resourceMetaData);
        when(contextManager.getDatabase("foo_db")).thenReturn(database);
        ResponseHeader actual = new DistSQLUpdateBackendHandler(new RefreshTableMetaDataStatement(), mockConnectionSession("foo_db")).execute();
        assertThat(actual, instanceOf(UpdateResponseHeader.class));
    }
    
    private ConnectionSession mockConnectionSession(final String databaseName) {
        ConnectionSession result = mock(ConnectionSession.class);
        when(result.getUsedDatabaseName()).thenReturn(databaseName);
        return result;
    }
}
