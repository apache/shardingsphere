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

import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;

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
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DistSQLUpdateProxyBackendHandlerTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ContextManager contextManager;
    
    @Test
    void assertEmptyStorageUnit() {
        ResourceMetaData resourceMetaData = mock(ResourceMetaData.class);
        RuleMetaData ruleMetaData = mock(RuleMetaData.class);
        ShardingSphereDatabase database = new ShardingSphereDatabase("foo_db", databaseType, resourceMetaData, ruleMetaData,
                Collections.emptyList(), new ConfigurationProperties(new Properties()));
        when(contextManager.getDatabase("foo_db")).thenReturn(database);
        RefreshTableMetaDataStatement sqlStatement = new RefreshTableMetaDataStatement();
        DistSQLUpdateProxyBackendHandler backendHandler = new DistSQLUpdateProxyBackendHandler(sqlStatement, mockQueryContext(), mockConnectionSession(), contextManager);
        assertThrows(EmptyStorageUnitException.class, backendHandler::execute);
    }
    
    @Test
    void assertMissingRequiredStorageUnit() {
        ResourceMetaData resourceMetaData = mock(ResourceMetaData.class);
        StorageUnit storageUnit = mock(StorageUnit.class);
        when(storageUnit.getStorageType()).thenReturn(databaseType);
        when(resourceMetaData.getStorageUnits()).thenReturn(Collections.singletonMap("ds_0", storageUnit));
        RuleMetaData ruleMetaData = mock(RuleMetaData.class);
        ShardingSphereDatabase database = new ShardingSphereDatabase("foo_db", databaseType, resourceMetaData, ruleMetaData,
                Collections.emptyList(), new ConfigurationProperties(new Properties()));
        when(contextManager.getDatabase("foo_db")).thenReturn(database);
        RefreshTableMetaDataStatement sqlStatement = new RefreshTableMetaDataStatement("t_order", "ds_1", null);
        DistSQLUpdateProxyBackendHandler backendHandler = new DistSQLUpdateProxyBackendHandler(sqlStatement, mockQueryContext(), mockConnectionSession(), contextManager);
        assertThrows(MissingRequiredStorageUnitsException.class, backendHandler::execute);
    }
    
    @Test
    void assertSchemaNotFound() {
        ResourceMetaData resourceMetaData = mock(ResourceMetaData.class);
        StorageUnit storageUnit = mock(StorageUnit.class);
        when(storageUnit.getStorageType()).thenReturn(databaseType);
        when(resourceMetaData.getStorageUnits()).thenReturn(Collections.singletonMap("ds_0", storageUnit));
        RuleMetaData ruleMetaData = mock(RuleMetaData.class);
        ShardingSphereDatabase database = new ShardingSphereDatabase("foo_db", databaseType, resourceMetaData, ruleMetaData,
                Collections.emptyList(), new ConfigurationProperties(new Properties()));
        when(contextManager.getDatabase("foo_db")).thenReturn(database);
        RefreshTableMetaDataStatement sqlStatement = new RefreshTableMetaDataStatement("t_order", "ds_0", "bar_db");
        DistSQLUpdateProxyBackendHandler backendHandler = new DistSQLUpdateProxyBackendHandler(sqlStatement, mockQueryContext(), mockConnectionSession(), contextManager);
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
        RefreshTableMetaDataStatement sqlStatement = new RefreshTableMetaDataStatement("t_order", "ds_0", "foo_db");
        DistSQLUpdateProxyBackendHandler backendHandler = new DistSQLUpdateProxyBackendHandler(sqlStatement, mockQueryContext(), mockConnectionSession(), contextManager);
        assertThrows(TableNotFoundException.class, backendHandler::execute);
    }
    
    @Test
    void assertUpdate() throws SQLException {
        ResourceMetaData resourceMetaData = mock(ResourceMetaData.class);
        when(resourceMetaData.getStorageUnits()).thenReturn(Collections.singletonMap("ds_0", mock(StorageUnit.class)));
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class);
        when(database.containsSchema(nullable(String.class))).thenReturn(true);
        when(database.getProtocolType()).thenReturn(databaseType);
        when(database.getResourceMetaData()).thenReturn(resourceMetaData);
        when(contextManager.getDatabase("foo_db")).thenReturn(database);
        RefreshTableMetaDataStatement sqlStatement = new RefreshTableMetaDataStatement();
        assertThat(new DistSQLUpdateProxyBackendHandler(sqlStatement, mockQueryContext(), mockConnectionSession(), contextManager).execute(), isA(UpdateResponseHeader.class));
    }
    
    private QueryContext mockQueryContext() {
        return mock(QueryContext.class, RETURNS_DEEP_STUBS);
    }
    
    private ConnectionSession mockConnectionSession() {
        ConnectionSession result = mock(ConnectionSession.class, RETURNS_DEEP_STUBS);
        when(result.getUsedDatabaseName()).thenReturn("foo_db");
        when(result.getDatabaseConnectionManager().getConnectionSize()).thenReturn(1);
        return result;
    }
}
