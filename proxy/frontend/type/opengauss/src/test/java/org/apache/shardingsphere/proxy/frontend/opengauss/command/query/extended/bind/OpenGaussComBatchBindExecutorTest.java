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

package org.apache.shardingsphere.proxy.frontend.opengauss.command.query.extended.bind;

import org.apache.shardingsphere.db.protocol.opengauss.packet.command.query.extended.bind.OpenGaussComBatchBindPacket;
import org.apache.shardingsphere.db.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.extended.bind.PostgreSQLBindCompletePacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.generic.PostgreSQLCommandCompletePacket;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.OpenGaussDatabaseType;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.ConnectionMode;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.jdbc.StatementOption;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.parser.ShardingSphereSQLParserEngine;
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.apache.shardingsphere.logging.rule.LoggingRule;
import org.apache.shardingsphere.logging.rule.builder.DefaultLoggingRuleConfigurationBuilder;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.connector.ProxyDatabaseConnectionManager;
import org.apache.shardingsphere.proxy.backend.connector.jdbc.statement.JDBCBackendStatement;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.session.ServerPreparedStatementRegistry;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.query.extended.PostgreSQLServerPreparedStatement;
import org.apache.shardingsphere.sql.parser.api.CacheOption;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sqltranslator.rule.SQLTranslatorRule;
import org.apache.shardingsphere.sqltranslator.rule.builder.DefaultSQLTranslatorRuleConfigurationBuilder;
import org.apache.shardingsphere.test.mock.AutoMockExtension;
import org.apache.shardingsphere.test.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ProxyContext.class)
class OpenGaussComBatchBindExecutorTest {
    
    private final ShardingSphereSQLParserEngine parserEngine = new ShardingSphereSQLParserEngine("openGauss", new CacheOption(2000, 65535L), new CacheOption(128, 1024L), false);
    
    @Test
    void assertExecute() throws SQLException {
        String statement = "S_1";
        String sql = "insert into bmsql (id) values (?)";
        SQLStatement sqlStatement = parserEngine.parse(sql, false);
        SQLStatementContext sqlStatementContext = mock(InsertStatementContext.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(sqlStatement);
        ConnectionSession connectionSession = mockConnectionSession();
        PostgreSQLServerPreparedStatement serverPreparedStatement = new PostgreSQLServerPreparedStatement(sql, sqlStatementContext, new HintValueContext(), Collections.emptyList(),
                Collections.emptyList());
        connectionSession.getServerPreparedStatementRegistry().addPreparedStatement(statement, serverPreparedStatement);
        ContextManager contextManager = mockContextManager();
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        Iterator<DatabasePacket> actualPacketsIterator = new OpenGaussComBatchBindExecutor(mockComBatchBindPacket(), connectionSession).execute().iterator();
        assertThat(actualPacketsIterator.next(), is(PostgreSQLBindCompletePacket.getInstance()));
        assertThat(actualPacketsIterator.next(), instanceOf(PostgreSQLCommandCompletePacket.class));
        assertFalse(actualPacketsIterator.hasNext());
    }
    
    private ConnectionSession mockConnectionSession() throws SQLException {
        ConnectionSession result = mock(ConnectionSession.class);
        when(result.getConnectionContext()).thenReturn(new ConnectionContext());
        when(result.getDatabaseName()).thenReturn("foo_db");
        ProxyDatabaseConnectionManager databaseConnectionManager = mock(ProxyDatabaseConnectionManager.class);
        Connection connection = mock(Connection.class, RETURNS_DEEP_STUBS);
        when(databaseConnectionManager.getConnections(nullable(String.class), anyInt(), anyInt(), any(ConnectionMode.class))).thenReturn(Collections.singletonList(connection));
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        JDBCBackendStatement backendStatement = mock(JDBCBackendStatement.class);
        when(backendStatement.createStorageResource(any(ExecutionUnit.class), any(Connection.class), any(ConnectionMode.class), any(StatementOption.class), nullable(DatabaseType.class)))
                .thenReturn(preparedStatement);
        when(result.getStatementManager()).thenReturn(backendStatement);
        when(result.getDatabaseConnectionManager()).thenReturn(databaseConnectionManager);
        when(result.getServerPreparedStatementRegistry()).thenReturn(new ServerPreparedStatementRegistry());
        return result;
    }
    
    private OpenGaussComBatchBindPacket mockComBatchBindPacket() {
        OpenGaussComBatchBindPacket result = mock(OpenGaussComBatchBindPacket.class);
        when(result.getStatementId()).thenReturn("S_1");
        when(result.readParameterSets(anyList())).thenReturn(Collections.singletonList(Collections.singletonList(0)));
        return result;
    }
    
    private ContextManager mockContextManager() {
        ContextManager result = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(result.getMetaDataContexts().getMetaData().getProps().<Integer>getValue(ConfigurationPropertyKey.KERNEL_EXECUTOR_SIZE)).thenReturn(0);
        when(result.getMetaDataContexts().getMetaData().getProps().<Integer>getValue(ConfigurationPropertyKey.MAX_CONNECTIONS_SIZE_PER_QUERY)).thenReturn(1);
        when(result.getMetaDataContexts().getMetaData().getProps().<Boolean>getValue(ConfigurationPropertyKey.SQL_SHOW)).thenReturn(false);
        when(result.getMetaDataContexts().getMetaData().getGlobalRuleMetaData()).thenReturn(new ShardingSphereRuleMetaData(Arrays.asList(
                new SQLTranslatorRule(new DefaultSQLTranslatorRuleConfigurationBuilder().build()), new LoggingRule(new DefaultLoggingRuleConfigurationBuilder().build()))));
        ShardingSphereDatabase database = mockDatabase();
        when(result.getMetaDataContexts().getMetaData().getDatabase("foo_db")).thenReturn(database);
        return result;
    }
    
    private ShardingSphereDatabase mockDatabase() {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(result.getResourceMetaData().getAllInstanceDataSourceNames()).thenReturn(Collections.singletonList("foo_ds"));
        when(result.getResourceMetaData().getStorageTypes()).thenReturn(Collections.singletonMap("foo_ds", new OpenGaussDatabaseType()));
        when(result.getRuleMetaData()).thenReturn(new ShardingSphereRuleMetaData(Collections.emptyList()));
        return result;
    }
}
