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

package org.apache.shardingsphere.proxy.frontend.mysql.command.query.text.query;

import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.ConnectionMode;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.jdbc.StatementOption;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.parser.rule.builder.DefaultSQLParserRuleConfigurationBuilder;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.JDBCBackendConnection;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.statement.JDBCBackendStatement;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLUpdateStatement;
import org.apache.shardingsphere.sqltranslator.rule.SQLTranslatorRule;
import org.apache.shardingsphere.sqltranslator.rule.builder.DefaultSQLTranslatorRuleConfigurationBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class MySQLMultiStatementsHandlerTest {
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ConnectionSession connectionSession;
    
    @Mock
    private JDBCBackendConnection backendConnection;
    
    @Mock
    private JDBCBackendStatement backendStatement;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ProxyContext proxyContext;
    
    @Test
    public void assertExecute() throws SQLException {
        final String sql = "update t set v=v+1 where id=1;update t set v=v+1 where id=2;update t set v=v+1 where id=3";
        when(connectionSession.getDatabaseName()).thenReturn("");
        when(connectionSession.getBackendConnection()).thenReturn(backendConnection);
        when(connectionSession.getStatementManager()).thenReturn(backendStatement);
        Connection connection = mock(Connection.class, RETURNS_DEEP_STUBS);
        when(connection.getMetaData().getURL()).thenReturn("jdbc:mysql://127.0.0.1/db");
        when(backendConnection.getConnections(nullable(String.class), anyInt(), any(ConnectionMode.class))).thenReturn(Collections.singletonList(connection));
        Statement statement = mock(Statement.class);
        when(backendStatement.createStorageResource(eq(connection), any(ConnectionMode.class), any(StatementOption.class))).thenReturn(statement);
        when(statement.getConnection()).thenReturn(connection);
        when(statement.executeBatch()).thenReturn(new int[]{1, 1, 1});
        MySQLUpdateStatement expectedStatement = mock(MySQLUpdateStatement.class);
        try (MockedStatic<ProxyContext> mockedStatic = mockStatic(ProxyContext.class)) {
            mockedStatic.when(ProxyContext::getInstance).thenReturn(proxyContext);
            when(ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getDatabase("").getResource().getDatabaseType()).thenReturn(new MySQLDatabaseType());
            when(ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getDatabase("").getProtocolType()).thenReturn(new MySQLDatabaseType());
            ShardingSphereRuleMetaData globalRuleMetaData = mock(ShardingSphereRuleMetaData.class);
            when(ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getGlobalRuleMetaData()).thenReturn(globalRuleMetaData);
            when(globalRuleMetaData.getSingleRule(SQLParserRule.class)).thenReturn(new SQLParserRule(new DefaultSQLParserRuleConfigurationBuilder().build()));
            when(globalRuleMetaData.getSingleRule(SQLTranslatorRule.class)).thenReturn(new SQLTranslatorRule(new DefaultSQLTranslatorRuleConfigurationBuilder().build()));
            when(ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getProps().<Integer>getValue(ConfigurationPropertyKey.KERNEL_EXECUTOR_SIZE)).thenReturn(1);
            when(ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getProps().<Boolean>getValue(ConfigurationPropertyKey.SQL_SHOW)).thenReturn(false);
            when(ProxyContext.getInstance()
                    .getContextManager().getMetaDataContexts().getMetaData().getProps().<Integer>getValue(ConfigurationPropertyKey.MAX_CONNECTIONS_SIZE_PER_QUERY)).thenReturn(1);
            ResponseHeader actual = new MySQLMultiStatementsHandler(connectionSession, expectedStatement, sql).execute();
            assertThat(actual, instanceOf(UpdateResponseHeader.class));
            UpdateResponseHeader actualHeader = (UpdateResponseHeader) actual;
            assertThat(actualHeader.getUpdateCount(), is(3L));
            assertThat(actualHeader.getLastInsertId(), is(0L));
            assertThat(actualHeader.getSqlStatement(), is(expectedStatement));
        }
    }
}
