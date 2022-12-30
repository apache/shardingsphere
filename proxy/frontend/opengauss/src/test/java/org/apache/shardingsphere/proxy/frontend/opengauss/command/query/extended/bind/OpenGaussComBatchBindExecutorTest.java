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
import org.apache.shardingsphere.infra.context.ConnectionContext;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.OpenGaussDatabaseType;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.ConnectionMode;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.jdbc.StatementOption;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.parser.ShardingSphereSQLParserEngine;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.communication.BackendConnection;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.statement.JDBCBackendStatement;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.session.ServerPreparedStatementRegistry;
import org.apache.shardingsphere.proxy.frontend.opengauss.ProxyContextRestorer;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.query.extended.PostgreSQLServerPreparedStatement;
import org.apache.shardingsphere.sql.parser.api.CacheOption;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sqltranslator.rule.SQLTranslatorRule;
import org.apache.shardingsphere.sqltranslator.rule.builder.DefaultSQLTranslatorRuleConfigurationBuilder;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Iterator;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class OpenGaussComBatchBindExecutorTest extends ProxyContextRestorer {
    
    private static final ShardingSphereSQLParserEngine SQL_PARSER_ENGINE = new ShardingSphereSQLParserEngine("openGauss", new CacheOption(2000, 65535L), new CacheOption(128, 1024L), false);
    
    @Before
    public void setup() {
        ProxyContext.init(mock(ContextManager.class, RETURNS_DEEP_STUBS));
    }
    
    @SuppressWarnings("rawtypes")
    @Test
    public void assertExecute() throws SQLException {
        when(ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getProps().<Integer>getValue(ConfigurationPropertyKey.KERNEL_EXECUTOR_SIZE)).thenReturn(0);
        when(ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getProps().<Integer>getValue(ConfigurationPropertyKey.MAX_CONNECTIONS_SIZE_PER_QUERY)).thenReturn(1);
        when(ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getProps().<Boolean>getValue(ConfigurationPropertyKey.SQL_SHOW)).thenReturn(false);
        ShardingSphereRuleMetaData globalRuleMetaData = mock(ShardingSphereRuleMetaData.class);
        when(ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getGlobalRuleMetaData()).thenReturn(globalRuleMetaData);
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getResourceMetaData().getAllInstanceDataSourceNames()).thenReturn(Collections.singletonList("ds_0"));
        when(database.getResourceMetaData().getStorageTypes()).thenReturn(Collections.singletonMap("ds_0", new OpenGaussDatabaseType()));
        when(ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getDatabase("db")).thenReturn(database);
        when(globalRuleMetaData.getSingleRule(SQLTranslatorRule.class)).thenReturn(new SQLTranslatorRule(new DefaultSQLTranslatorRuleConfigurationBuilder().build()));
        String statement = "S_1";
        OpenGaussComBatchBindPacket packet = mock(OpenGaussComBatchBindPacket.class);
        when(packet.getStatementId()).thenReturn("S_1");
        when(packet.readParameterSets(anyList())).thenReturn(Collections.singletonList(Collections.singletonList(0)));
        ConnectionSession connectionSession = mock(ConnectionSession.class);
        when(connectionSession.getConnectionContext()).thenReturn(new ConnectionContext());
        when(connectionSession.getDatabaseName()).thenReturn("db");
        when(connectionSession.getConnectionId()).thenReturn(1);
        BackendConnection backendConnection = mock(BackendConnection.class);
        Connection connection = mock(Connection.class, RETURNS_DEEP_STUBS);
        when(connection.getMetaData().getURL()).thenReturn("jdbc:opengauss://127.0.0.1/db");
        when(backendConnection.getConnections(nullable(String.class), anyInt(), any(ConnectionMode.class))).thenReturn(Collections.singletonList(connection));
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        when(preparedStatement.getConnection()).thenReturn(connection);
        JDBCBackendStatement backendStatement = mock(JDBCBackendStatement.class);
        when(backendStatement.createStorageResource(any(ExecutionUnit.class), any(Connection.class), any(ConnectionMode.class), any(StatementOption.class), nullable(DatabaseType.class)))
                .thenReturn(preparedStatement);
        when(connectionSession.getStatementManager()).thenReturn(backendStatement);
        when(connectionSession.getBackendConnection()).thenReturn(backendConnection);
        when(connectionSession.getServerPreparedStatementRegistry()).thenReturn(new ServerPreparedStatementRegistry());
        String sql = "insert into bmsql (id) values (?)";
        SQLStatement sqlStatement = SQL_PARSER_ENGINE.parse(sql, false);
        SQLStatementContext sqlStatementContext = mock(InsertStatementContext.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(sqlStatement);
        connectionSession.getServerPreparedStatementRegistry().addPreparedStatement(statement, new PostgreSQLServerPreparedStatement(sql, sqlStatementContext, Collections.emptyList()));
        OpenGaussComBatchBindExecutor executor = new OpenGaussComBatchBindExecutor(packet, connectionSession);
        Iterator<DatabasePacket<?>> actualPacketsIterator = executor.execute().iterator();
        assertThat(actualPacketsIterator.next(), is(PostgreSQLBindCompletePacket.getInstance()));
        assertThat(actualPacketsIterator.next(), instanceOf(PostgreSQLCommandCompletePacket.class));
        assertFalse(actualPacketsIterator.hasNext());
    }
}
