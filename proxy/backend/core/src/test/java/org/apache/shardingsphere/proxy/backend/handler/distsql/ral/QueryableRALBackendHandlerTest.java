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

package org.apache.shardingsphere.proxy.backend.handler.distsql.ral;

import org.apache.shardingsphere.distsql.parser.statement.ral.QueryableRALStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.queryable.ExportDatabaseConfigurationStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.queryable.ShowTableMetaDataStatement;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.exception.dialect.exception.syntax.database.NoDatabaseSelectedException;
import org.apache.shardingsphere.infra.exception.dialect.exception.syntax.database.UnknownDatabaseException;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereIndex;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.spi.exception.ServiceProviderNotFoundException;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.DatabaseSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.test.mock.AutoMockExtension;
import org.apache.shardingsphere.test.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ProxyContext.class)
class QueryableRALBackendHandlerTest {
    
    @Test
    void assertExecuteWithNoDatabase() {
        ConnectionSession connectionSession = mock(ConnectionSession.class);
        when(connectionSession.getDatabaseName()).thenReturn(null);
        assertThrows(NoDatabaseSelectedException.class, () -> new QueryableRALBackendHandler<>(mock(ExportDatabaseConfigurationStatement.class), connectionSession).execute());
    }
    
    @Test
    void assertExecuteWithUnknownDatabase() {
        ShardingSphereMetaData metaData = new ShardingSphereMetaData();
        MetaDataContexts metaDataContexts = new MetaDataContexts(mock(MetaDataPersistService.class), metaData);
        ConnectionSession connectionSession = mock(ConnectionSession.class);
        when(connectionSession.getDatabaseName()).thenReturn("unknown");
        ContextManager contextManager = new ContextManager(metaDataContexts, mock(InstanceContext.class));
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        assertThrows(UnknownDatabaseException.class, () -> new QueryableRALBackendHandler<>(mock(ExportDatabaseConfigurationStatement.class), connectionSession).execute());
    }
    
    @Test
    void assertExecuteWithAbstractStatement() {
        assertThrows(ServiceProviderNotFoundException.class, () -> new QueryableRALBackendHandler<>(mock(QueryableRALStatement.class), mock(ConnectionSession.class)).execute());
    }
    
    @Test
    void assertExecute() {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getName()).thenReturn("foo_db");
        when(database.getProtocolType()).thenReturn(TypedSPILoader.getService(DatabaseType.class, "FIXTURE"));
        when(database.getSchema("foo_db")).thenReturn(new ShardingSphereSchema(createTableMap(), Collections.emptyMap()));
        when(ProxyContext.getInstance().databaseExists("foo_db")).thenReturn(true);
        when(ProxyContext.getInstance().getDatabase("foo_db")).thenReturn(database);
        assertDoesNotThrow(() -> new QueryableRALBackendHandler<>(createSqlStatement(), mock(ConnectionSession.class)).execute());
    }
    
    private Map<String, ShardingSphereTable> createTableMap() {
        Map<String, ShardingSphereTable> result = new HashMap<>();
        Collection<ShardingSphereColumn> columns = Collections.singletonList(new ShardingSphereColumn("order_id", 0, false, false, false, true, false, false));
        Collection<ShardingSphereIndex> indexes = Collections.singletonList(new ShardingSphereIndex("primary"));
        result.put("t_order", new ShardingSphereTable("t_order", columns, indexes, Collections.emptyList()));
        return result;
    }
    
    private ShowTableMetaDataStatement createSqlStatement() {
        return new ShowTableMetaDataStatement(Collections.singleton("t_order"), new DatabaseSegment(0, 0, new IdentifierValue("foo_db")));
    }
}
