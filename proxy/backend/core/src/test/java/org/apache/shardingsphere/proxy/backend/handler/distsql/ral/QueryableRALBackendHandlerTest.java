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

import org.apache.shardingsphere.distsql.statement.ral.queryable.QueryableRALStatement;
import org.apache.shardingsphere.distsql.statement.ral.queryable.export.ExportDatabaseConfigurationStatement;
import org.apache.shardingsphere.distsql.statement.ral.queryable.show.ShowTableMetaDataStatement;
import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.exception.dialect.exception.syntax.database.NoDatabaseSelectedException;
import org.apache.shardingsphere.infra.exception.dialect.exception.syntax.database.UnknownDatabaseException;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstanceContext;
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
import org.apache.shardingsphere.mode.metadata.MetaDataContextsFactory;
import org.apache.shardingsphere.mode.spi.PersistRepository;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.handler.distsql.DistSQLQueryBackendHandler;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.DatabaseSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.test.mock.AutoMockExtension;
import org.apache.shardingsphere.test.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Collection;
import java.util.Collections;
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
        assertThrows(NoDatabaseSelectedException.class,
                () -> new DistSQLQueryBackendHandler(mock(ExportDatabaseConfigurationStatement.class), mock(ConnectionSession.class, RETURNS_DEEP_STUBS)).execute());
    }
    
    @Test
    void assertExecuteWithUnknownDatabase() {
        ShardingSphereMetaData metaData = new ShardingSphereMetaData();
        MetaDataContexts metaDataContexts = MetaDataContextsFactory.create(mock(MetaDataPersistService.class), metaData);
        ConnectionSession connectionSession = mock(ConnectionSession.class, RETURNS_DEEP_STUBS);
        when(connectionSession.getDatabaseName()).thenReturn("unknown");
        ComputeNodeInstanceContext computeNodeInstanceContext = mock(ComputeNodeInstanceContext.class);
        when(computeNodeInstanceContext.getModeConfiguration()).thenReturn(mock(ModeConfiguration.class));
        ContextManager contextManager = new ContextManager(metaDataContexts, computeNodeInstanceContext, mock(PersistRepository.class));
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        assertThrows(UnknownDatabaseException.class, () -> new DistSQLQueryBackendHandler(mock(ExportDatabaseConfigurationStatement.class), connectionSession).execute());
    }
    
    @Test
    void assertExecuteWithAbstractStatement() {
        assertThrows(ServiceProviderNotFoundException.class, () -> new DistSQLQueryBackendHandler(mock(QueryableRALStatement.class), mock(ConnectionSession.class, RETURNS_DEEP_STUBS)).execute());
    }
    
    @Test
    void assertExecute() {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getName()).thenReturn("foo_db");
        when(database.getProtocolType()).thenReturn(TypedSPILoader.getService(DatabaseType.class, "FIXTURE"));
        when(database.getSchema("foo_db")).thenReturn(new ShardingSphereSchema(createTableMap(), Collections.emptyMap()));
        when(ProxyContext.getInstance().getContextManager().getDatabase("foo_db")).thenReturn(database);
        assertDoesNotThrow(() -> new DistSQLQueryBackendHandler(createSqlStatement(), mock(ConnectionSession.class, RETURNS_DEEP_STUBS)).execute());
    }
    
    private Map<String, ShardingSphereTable> createTableMap() {
        Collection<ShardingSphereColumn> columns = Collections.singletonList(new ShardingSphereColumn("order_id", 0, false, false, false, true, false, false));
        Collection<ShardingSphereIndex> indexes = Collections.singletonList(new ShardingSphereIndex("primary"));
        return Collections.singletonMap("t_order", new ShardingSphereTable("t_order", columns, indexes, Collections.emptyList()));
    }
    
    private ShowTableMetaDataStatement createSqlStatement() {
        return new ShowTableMetaDataStatement(Collections.singleton("t_order"), new DatabaseSegment(0, 0, new IdentifierValue("foo_db")));
    }
}
