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

import org.apache.shardingsphere.dialect.exception.syntax.database.NoDatabaseSelectedException;
import org.apache.shardingsphere.dialect.exception.syntax.database.UnknownDatabaseException;
import org.apache.shardingsphere.distsql.parser.statement.ral.QueryableRALStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.queryable.ExportDatabaseConfigurationStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.queryable.ShowTableMetaDataStatement;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereIndex;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.util.spi.exception.ServiceProviderNotFoundServerException;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.util.ProxyContextRestorer;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.DatabaseSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class QueryableRALBackendHandlerTest extends ProxyContextRestorer {
    
    @Test(expected = NoDatabaseSelectedException.class)
    public void assertExecuteWithNoDatabase() {
        ConnectionSession connectionSession = mock(ConnectionSession.class);
        QueryableRALBackendHandler handler = new QueryableRALBackendHandler(mock(ExportDatabaseConfigurationStatement.class), connectionSession);
        when(connectionSession.getDatabaseName()).thenReturn(null);
        handler.execute();
    }
    
    @Test(expected = UnknownDatabaseException.class)
    public void assertExecuteWithUnknownDatabase() {
        ShardingSphereMetaData metaData = new ShardingSphereMetaData();
        MetaDataContexts metaDataContexts = new MetaDataContexts(mock(MetaDataPersistService.class), metaData);
        ContextManager contextManager = new ContextManager(metaDataContexts, mock(InstanceContext.class));
        ProxyContext.init(contextManager);
        ConnectionSession connectionSession = mock(ConnectionSession.class);
        QueryableRALBackendHandler handler = new QueryableRALBackendHandler(mock(ExportDatabaseConfigurationStatement.class), connectionSession);
        when(connectionSession.getDatabaseName()).thenReturn("unknown");
        handler.execute();
    }
    
    @Test(expected = ServiceProviderNotFoundServerException.class)
    public void assertExecuteWithAbstractStatement() {
        QueryableRALBackendHandler handler = new QueryableRALBackendHandler(mock(QueryableRALStatement.class), mock(ConnectionSession.class));
        handler.execute();
    }
    
    @Test
    public void assertExecute() {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getSchema("db_name")).thenReturn(new ShardingSphereSchema(createTableMap(), Collections.emptyMap()));
        when(contextManager.getMetaDataContexts().getMetaData().getDatabases()).thenReturn(Collections.singletonMap("db_name", database));
        when(contextManager.getMetaDataContexts().getMetaData().containsDatabase("db_name")).thenReturn(true);
        when(contextManager.getMetaDataContexts().getMetaData().getDatabase("db_name")).thenReturn(database);
        ProxyContext.init(contextManager);
        ConnectionSession connectionSession = mock(ConnectionSession.class, RETURNS_DEEP_STUBS);
        when(connectionSession.getDatabaseName()).thenReturn("db_name");
        QueryableRALBackendHandler handler = new QueryableRALBackendHandler(createSqlStatement(), connectionSession);
        handler.execute();
    }
    
    private Map<String, ShardingSphereTable> createTableMap() {
        Map<String, ShardingSphereTable> result = new HashMap<>();
        Collection<ShardingSphereColumn> columns = Collections.singletonList(new ShardingSphereColumn("order_id", 0, false, false, false, true, false));
        Collection<ShardingSphereIndex> indexes = Collections.singletonList(new ShardingSphereIndex("primary"));
        result.put("t_order", new ShardingSphereTable("t_order", columns, indexes, Collections.emptyList()));
        return result;
    }
    
    private ShowTableMetaDataStatement createSqlStatement() {
        return new ShowTableMetaDataStatement(Collections.singleton("t_order"), new DatabaseSegment(0, 0, new IdentifierValue("db_name")));
    }
}
