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

package org.apache.shardingsphere.proxy.frontend.postgresql.command.query.extended;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.PostgreSQLBinaryColumnType;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.connector.ProxyDatabaseConnectionManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.sql.parser.engine.api.CacheOption;
import org.apache.shardingsphere.infra.parser.ShardingSphereSQLParserEngine;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sqltranslator.rule.SQLTranslatorRule;
import org.apache.shardingsphere.sqltranslator.rule.builder.DefaultSQLTranslatorRuleConfigurationBuilder;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collections;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ProxyContext.class)
class PostgreSQLPreparedStatementMetadataLoaderTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "PostgreSQL");
    
    private final ShardingSphereSQLParserEngine sqlParserEngine = new ShardingSphereSQLParserEngine(databaseType, new CacheOption(2000, 65535L), new CacheOption(128, 1024L));
    
    @Mock
    private ConnectionSession connectionSession;
    
    @Test
    void assertLoad() throws SQLException {
        PreparedStatement expected = prepareJDBCBackendConnection(null);
        assertThat(PostgreSQLPreparedStatementMetadataLoader.load(connectionSession, createPreparedStatement()), is(expected));
    }
    
    @Test
    void assertLoadWithSQLException() throws SQLException {
        PostgreSQLServerPreparedStatement preparedStatement = createPreparedStatement();
        SQLException expected = new SQLException("expected");
        prepareJDBCBackendConnection(expected);
        assertThat(assertThrows(SQLException.class, () -> PostgreSQLPreparedStatementMetadataLoader.load(connectionSession, preparedStatement)), is(expected));
    }
    
    private PostgreSQLServerPreparedStatement createPreparedStatement() {
        SQLStatement sqlStatement = sqlParserEngine.parse("SELECT id FROM foo_tbl WHERE id=?", false);
        SQLStatementContext sqlStatementContext = mock(SelectStatementContext.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(sqlStatement);
        when(connectionSession.getCurrentDatabaseName()).thenReturn("postgres");
        when(connectionSession.getUsedDatabaseName()).thenReturn("postgres");
        when(connectionSession.getConnectionContext()).thenReturn(mock(ConnectionContext.class));
        ContextManager contextManager = mockContextManager();
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        return new PostgreSQLServerPreparedStatement("SELECT id FROM foo_tbl WHERE id=?", sqlStatementContext, new HintValueContext(), Collections.singletonList(PostgreSQLBinaryColumnType.UNSPECIFIED), Collections.singletonList(0));
    }
    
    private ContextManager mockContextManager() {
        ContextManager result = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(result.getMetaDataContexts().getMetaData().getProps()).thenReturn(new ConfigurationProperties(new Properties()));
        RuleMetaData globalRuleMetaData = new RuleMetaData(Collections.singleton(new SQLTranslatorRule(new DefaultSQLTranslatorRuleConfigurationBuilder().build())));
        when(result.getMetaDataContexts().getMetaData().getGlobalRuleMetaData()).thenReturn(globalRuleMetaData);
        when(result.getMetaDataContexts().getMetaData().containsDatabase("postgres")).thenReturn(true);
        when(result.getMetaDataContexts().getMetaData().getDatabase("postgres").getProtocolType()).thenReturn(databaseType);
        StorageUnit storageUnit = mock(StorageUnit.class, RETURNS_DEEP_STUBS);
        when(storageUnit.getStorageType()).thenReturn(databaseType);
        when(result.getMetaDataContexts().getMetaData().getDatabase("postgres").getResourceMetaData().getStorageUnits()).thenReturn(Collections.singletonMap("ds_0", storageUnit));
        ShardingSphereSchema schema = mock(ShardingSphereSchema.class);
        when(result.getMetaDataContexts().getMetaData().getDatabase("postgres").containsSchema("public")).thenReturn(true);
        when(result.getMetaDataContexts().getMetaData().getDatabase("postgres").getSchema("public")).thenReturn(schema);
        ShardingSphereTable table = new ShardingSphereTable("foo_tbl", Collections.singletonList(
                new ShardingSphereColumn("id", Types.INTEGER, true, false, false, true, false, false)), Collections.emptyList(), Collections.emptyList());
        when(schema.containsTable("foo_tbl")).thenReturn(true);
        when(schema.getTable("foo_tbl")).thenReturn(table);
        ShardingSphereDatabase database = result.getMetaDataContexts().getMetaData().getDatabase("postgres");
        when(result.getDatabase("postgres")).thenReturn(database);
        return result;
    }
    
    private PreparedStatement prepareJDBCBackendConnection(final SQLException ex) throws SQLException {
        PreparedStatement result = mock(PreparedStatement.class, RETURNS_DEEP_STUBS);
        Connection connection = mock(Connection.class, RETURNS_DEEP_STUBS);
        if (null == ex) {
            when(connection.prepareStatement(anyString())).thenReturn(result);
        } else {
            when(connection.prepareStatement(anyString())).thenThrow(ex);
        }
        ProxyDatabaseConnectionManager databaseConnectionManager = mock(ProxyDatabaseConnectionManager.class);
        when(databaseConnectionManager.getConnections(any(), nullable(String.class), anyInt(), anyInt(), any())).thenReturn(Collections.singletonList(connection));
        when(connectionSession.getDatabaseConnectionManager()).thenReturn(databaseConnectionManager);
        return result;
    }
}
