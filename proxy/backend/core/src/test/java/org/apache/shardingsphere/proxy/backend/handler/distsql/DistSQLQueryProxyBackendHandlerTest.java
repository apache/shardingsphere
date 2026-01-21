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

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.exception.core.exception.syntax.database.NoDatabaseSelectedException;
import org.apache.shardingsphere.database.exception.core.exception.syntax.database.UnknownDatabaseException;
import org.apache.shardingsphere.distsql.handler.engine.query.DistSQLQueryExecutor;
import org.apache.shardingsphere.distsql.statement.DistSQLStatement;
import org.apache.shardingsphere.distsql.statement.type.ral.queryable.QueryableRALStatement;
import org.apache.shardingsphere.distsql.statement.type.ral.queryable.export.ExportDatabaseConfigurationStatement;
import org.apache.shardingsphere.distsql.statement.type.ral.queryable.show.ShowTableMetaDataStatement;
import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstanceContext;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereIndex;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereStatistics;
import org.apache.shardingsphere.infra.spi.exception.ServiceProviderNotFoundException;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.spi.repository.PersistRepository;
import org.apache.shardingsphere.proxy.backend.response.data.QueryResponseRow;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryResponseHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dal.FromDatabaseSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.DatabaseSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.SQLStatementAttributes;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class DistSQLQueryProxyBackendHandlerTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @Test
    void assertExecuteWithNoDatabase() {
        assertThrows(NoDatabaseSelectedException.class, () -> new DistSQLQueryProxyBackendHandler(
                mock(ExportDatabaseConfigurationStatement.class, RETURNS_DEEP_STUBS), mock(), mock(ConnectionSession.class, RETURNS_DEEP_STUBS), mock()).execute());
    }
    
    @Test
    void assertExecuteWithUnknownDatabase() {
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(Collections.emptyList(), new ResourceMetaData(Collections.emptyMap()),
                new RuleMetaData(Collections.emptyList()), new ConfigurationProperties(new Properties()));
        MetaDataContexts metaDataContexts = new MetaDataContexts(metaData, new ShardingSphereStatistics());
        ConnectionSession connectionSession = mock(ConnectionSession.class, RETURNS_DEEP_STUBS);
        when(connectionSession.getUsedDatabaseName()).thenReturn("unknown");
        ComputeNodeInstanceContext computeNodeInstanceContext = mock(ComputeNodeInstanceContext.class);
        when(computeNodeInstanceContext.getModeConfiguration()).thenReturn(mock(ModeConfiguration.class));
        ContextManager contextManager = new ContextManager(metaDataContexts, computeNodeInstanceContext, mock(), mock(PersistRepository.class));
        assertThrows(UnknownDatabaseException.class,
                () -> new DistSQLQueryProxyBackendHandler(mock(ExportDatabaseConfigurationStatement.class, RETURNS_DEEP_STUBS), mock(), connectionSession, contextManager).execute());
    }
    
    @Test
    void assertExecuteWithAbstractStatement() {
        assertThrows(ServiceProviderNotFoundException.class,
                () -> new DistSQLQueryProxyBackendHandler(mock(QueryableRALStatement.class, RETURNS_DEEP_STUBS), mock(), mock(ConnectionSession.class, RETURNS_DEEP_STUBS), mock()).execute());
    }
    
    @Test
    void assertExecute() {
        ShardingSphereDatabase database = new ShardingSphereDatabase(
                "foo_db", databaseType, mock(), mock(), Collections.singleton(new ShardingSphereSchema("foo_db", createTables(), Collections.emptyList())));
        ContextManager contextManager = mock(ContextManager.class);
        when(contextManager.getDatabase("foo_db")).thenReturn(database);
        assertDoesNotThrow(() -> new DistSQLQueryProxyBackendHandler(createSqlStatement(), mock(), mock(ConnectionSession.class, RETURNS_DEEP_STUBS), contextManager).execute());
    }
    
    private Collection<ShardingSphereTable> createTables() {
        Collection<ShardingSphereColumn> columns = Collections.singletonList(new ShardingSphereColumn("order_id", 0, false, false, false, true, false, false));
        Collection<ShardingSphereIndex> indexes = Collections.singletonList(new ShardingSphereIndex("primary", Collections.emptyList(), false));
        return Collections.singleton(new ShardingSphereTable("t_order", columns, indexes, Collections.emptyList()));
    }
    
    private ShowTableMetaDataStatement createSqlStatement() {
        return new ShowTableMetaDataStatement(Collections.singleton("t_order"), new FromDatabaseSegment(0, new DatabaseSegment(0, 0, new IdentifierValue("foo_db"))));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    void assertGetRowData() throws SQLException {
        DistSQLStatement sqlStatement = mock(DistSQLStatement.class);
        when(sqlStatement.getAttributes()).thenReturn(new SQLStatementAttributes());
        ConnectionSession connectionSession = mock(ConnectionSession.class, RETURNS_DEEP_STUBS);
        when(connectionSession.getDatabaseConnectionManager().getConnectionSize()).thenReturn(1);
        DistSQLQueryExecutor<DistSQLStatement> executor = mock(DistSQLQueryExecutor.class);
        when(executor.getColumnNames(sqlStatement)).thenReturn(Collections.singleton("name"));
        ContextManager contextManager = mock(ContextManager.class);
        when(executor.getRows(sqlStatement, contextManager)).thenReturn(Collections.singleton(new LocalDataQueryResultRow("value")));
        try (MockedStatic<TypedSPILoader> typedSPILoader = mockStatic(TypedSPILoader.class)) {
            typedSPILoader.when(() -> TypedSPILoader.getService(DistSQLQueryExecutor.class, sqlStatement.getClass())).thenReturn(executor);
            DistSQLQueryProxyBackendHandler handler = new DistSQLQueryProxyBackendHandler(sqlStatement, mock(), connectionSession, contextManager);
            assertFalse(handler.next());
            QueryResponseHeader responseHeader = (QueryResponseHeader) handler.execute();
            assertThat(responseHeader.getQueryHeaders().size(), is(1));
            assertTrue(handler.next());
            QueryResponseRow row = handler.getRowData();
            assertThat(row.getData(), contains("value"));
            assertFalse(handler.next());
        }
    }
}
