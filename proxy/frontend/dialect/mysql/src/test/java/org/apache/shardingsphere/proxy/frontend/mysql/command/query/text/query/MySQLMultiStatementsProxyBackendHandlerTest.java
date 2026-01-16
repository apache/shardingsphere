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

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.ConnectionMode;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.jdbc.StatementOption;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.parser.rule.builder.DefaultSQLParserRuleConfigurationBuilder;
import org.apache.shardingsphere.proxy.backend.connector.ProxyDatabaseConnectionManager;
import org.apache.shardingsphere.proxy.backend.connector.jdbc.statement.JDBCBackendStatement;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.MultiStatementsUpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.UpdateStatement;
import org.apache.shardingsphere.sqltranslator.rule.SQLTranslatorRule;
import org.apache.shardingsphere.sqltranslator.rule.builder.DefaultSQLTranslatorRuleConfigurationBuilder;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ProxyContext.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MySQLMultiStatementsProxyBackendHandlerTest {
    
    @Test
    void assertExecute() throws SQLException {
        String sql = "UPDATE t SET v=v+1 WHERE id=1;UPDATE t SET v=v+1 WHERE id=2;UPDATE t SET v=v+1 WHERE id=3";
        ConnectionSession connectionSession = mockConnectionSession();
        UpdateStatement expectedStatement = mock(UpdateStatement.class);
        ContextManager contextManager = mockContextManager();
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        ResponseHeader actual = new MySQLMultiStatementsProxyBackendHandler(connectionSession, expectedStatement, sql).execute();
        assertThat(actual, isA(MultiStatementsUpdateResponseHeader.class));
        MultiStatementsUpdateResponseHeader actualHeader = (MultiStatementsUpdateResponseHeader) actual;
        assertThat(actualHeader.getUpdateResponseHeaders().size(), is(3));
        Iterator<UpdateResponseHeader> iterator = actualHeader.getUpdateResponseHeaders().iterator();
        UpdateResponseHeader responseHeader = iterator.next();
        assertThat(responseHeader.getUpdateCount(), is(1L));
        assertThat(responseHeader.getLastInsertId(), is(0L));
        assertThat(responseHeader.getSqlStatement(), is(expectedStatement));
        responseHeader = iterator.next();
        assertThat(responseHeader.getUpdateCount(), is(1L));
        assertThat(responseHeader.getLastInsertId(), is(0L));
        assertThat(responseHeader.getSqlStatement(), is(expectedStatement));
        responseHeader = iterator.next();
        assertThat(responseHeader.getUpdateCount(), is(1L));
        assertThat(responseHeader.getLastInsertId(), is(0L));
        assertThat(responseHeader.getSqlStatement(), is(expectedStatement));
    }
    
    @Test
    void assertExecuteWithSpecifiedDatabaseName() throws SQLException {
        String sql = "UPDATE foo_db.t SET v=v+1 WHERE id=1;UPDATE foo_db.t SET v=v+1 WHERE id=2;UPDATE foo_db.t SET v=v+1 WHERE id=3";
        ConnectionSession connectionSession = mockConnectionSession();
        UpdateStatement expectedStatement = mock(UpdateStatement.class);
        ContextManager contextManager = mockContextManager();
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        ResponseHeader actual = new MySQLMultiStatementsProxyBackendHandler(connectionSession, expectedStatement, sql).execute();
        assertThat(actual, isA(MultiStatementsUpdateResponseHeader.class));
        MultiStatementsUpdateResponseHeader actualHeader = (MultiStatementsUpdateResponseHeader) actual;
        assertThat(actualHeader.getUpdateResponseHeaders().size(), is(3));
        Iterator<UpdateResponseHeader> iterator = actualHeader.getUpdateResponseHeaders().iterator();
        UpdateResponseHeader responseHeader = iterator.next();
        assertThat(responseHeader.getUpdateCount(), is(1L));
        assertThat(responseHeader.getLastInsertId(), is(0L));
        assertThat(responseHeader.getSqlStatement(), is(expectedStatement));
        responseHeader = iterator.next();
        assertThat(responseHeader.getUpdateCount(), is(1L));
        assertThat(responseHeader.getLastInsertId(), is(0L));
        assertThat(responseHeader.getSqlStatement(), is(expectedStatement));
        responseHeader = iterator.next();
        assertThat(responseHeader.getUpdateCount(), is(1L));
        assertThat(responseHeader.getLastInsertId(), is(0L));
        assertThat(responseHeader.getSqlStatement(), is(expectedStatement));
    }
    
    @Test
    void assertExecuteWithMultiInsertOnDuplicateKey() throws SQLException {
        String sql = "INSERT INTO t (id, v) VALUES(1,1) ON DUPLICATE KEY UPDATE v=2;"
                + "INSERT INTO t (id, v) VALUES(2,1) ON DUPLICATE KEY UPDATE v=3;INSERT INTO t (id, v) VALUES(2,1) ON DUPLICATE KEY UPDATE v=3";
        ConnectionSession connectionSession = mockConnectionSession();
        UpdateStatement expectedStatement = mock(UpdateStatement.class);
        ContextManager contextManager = mockContextManager();
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        ResponseHeader actual = new MySQLMultiStatementsProxyBackendHandler(connectionSession, expectedStatement, sql).execute();
        assertThat(actual, isA(MultiStatementsUpdateResponseHeader.class));
        MultiStatementsUpdateResponseHeader actualHeader = (MultiStatementsUpdateResponseHeader) actual;
        assertThat(actualHeader.getUpdateResponseHeaders().size(), is(3));
        Iterator<UpdateResponseHeader> iterator = actualHeader.getUpdateResponseHeaders().iterator();
        UpdateResponseHeader responseHeader = iterator.next();
        assertThat(responseHeader.getUpdateCount(), is(1L));
        assertThat(responseHeader.getLastInsertId(), is(0L));
        assertThat(responseHeader.getSqlStatement(), is(expectedStatement));
        responseHeader = iterator.next();
        assertThat(responseHeader.getUpdateCount(), is(1L));
        assertThat(responseHeader.getLastInsertId(), is(0L));
        assertThat(responseHeader.getSqlStatement(), is(expectedStatement));
        responseHeader = iterator.next();
        assertThat(responseHeader.getUpdateCount(), is(1L));
        assertThat(responseHeader.getLastInsertId(), is(0L));
        assertThat(responseHeader.getSqlStatement(), is(expectedStatement));
    }
    
    private ConnectionSession mockConnectionSession() throws SQLException {
        ConnectionSession result = mock(ConnectionSession.class, RETURNS_DEEP_STUBS);
        when(result.getCurrentDatabaseName()).thenReturn("foo_db");
        when(result.getUsedDatabaseName()).thenReturn("foo_db");
        Connection connection = mock(Connection.class, RETURNS_DEEP_STUBS);
        when(connection.getMetaData().getURL()).thenReturn("jdbc:mysql://127.0.0.1/db");
        Statement statement = mock(Statement.class);
        when(statement.getConnection()).thenReturn(connection);
        when(statement.executeBatch()).thenReturn(new int[]{1, 1, 1});
        ProxyDatabaseConnectionManager databaseConnectionManager = mock(ProxyDatabaseConnectionManager.class);
        when(databaseConnectionManager.getConnections(any(), nullable(String.class), anyInt(), anyInt(), any(ConnectionMode.class))).thenReturn(Collections.singletonList(connection));
        when(result.getDatabaseConnectionManager()).thenReturn(databaseConnectionManager);
        JDBCBackendStatement backendStatement = mock(JDBCBackendStatement.class);
        when(backendStatement.createStorageResource(eq(connection), any(ConnectionMode.class), any(StatementOption.class), nullable(DatabaseType.class))).thenReturn(statement);
        when(result.getStatementManager()).thenReturn(backendStatement);
        return result;
    }
    
    private ContextManager mockContextManager() {
        ContextManager result = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        ResourceMetaData resourceMetaData = mock(ResourceMetaData.class, RETURNS_DEEP_STUBS);
        when(result.getMetaDataContexts().getMetaData().getDatabase("foo_db").getResourceMetaData()).thenReturn(resourceMetaData);
        when(resourceMetaData.getAllInstanceDataSourceNames()).thenReturn(Collections.singletonList("foo_ds"));
        StorageUnit storageUnit = mock(StorageUnit.class, RETURNS_DEEP_STUBS);
        when(storageUnit.getStorageType()).thenReturn(TypedSPILoader.getService(DatabaseType.class, "FIXTURE"));
        when(resourceMetaData.getStorageUnits()).thenReturn(Collections.singletonMap("foo_ds", storageUnit));
        when(result.getMetaDataContexts().getMetaData().getDatabase("foo_db").getProtocolType()).thenReturn(TypedSPILoader.getService(DatabaseType.class, "MySQL"));
        when(result.getMetaDataContexts().getMetaData().getDatabase("foo_db").getRuleMetaData())
                .thenReturn(new RuleMetaData(Collections.emptyList()));
        RuleMetaData globalRuleMetaData = new RuleMetaData(
                Arrays.asList(new SQLParserRule(new DefaultSQLParserRuleConfigurationBuilder().build()), new SQLTranslatorRule(new DefaultSQLTranslatorRuleConfigurationBuilder().build())));
        when(result.getMetaDataContexts().getMetaData().getGlobalRuleMetaData()).thenReturn(globalRuleMetaData);
        when(result.getMetaDataContexts().getMetaData().getProps().<Integer>getValue(ConfigurationPropertyKey.KERNEL_EXECUTOR_SIZE)).thenReturn(1);
        when(result.getMetaDataContexts().getMetaData().getProps().<Boolean>getValue(ConfigurationPropertyKey.SQL_SHOW)).thenReturn(false);
        when(result.getMetaDataContexts().getMetaData().getProps().<Integer>getValue(ConfigurationPropertyKey.MAX_CONNECTIONS_SIZE_PER_QUERY)).thenReturn(1);
        ShardingSphereTable table = new ShardingSphereTable("t", Arrays.asList(new ShardingSphereColumn("id", Types.BIGINT, true, false, "bigint", false, false, true, false),
                new ShardingSphereColumn("v", Types.INTEGER, false, false, "int", false, false, true, false)), Collections.emptyList(), Collections.emptyList());
        when(result.getMetaDataContexts().getMetaData().getDatabase("foo_db").getSchema("foo_db").getTable("t")).thenReturn(table);
        when(result.getMetaDataContexts().getMetaData().containsDatabase("foo_db")).thenReturn(true);
        when(result.getMetaDataContexts().getMetaData().getDatabase("foo_db").containsSchema("foo_db")).thenReturn(true);
        when(result.getMetaDataContexts().getMetaData().getDatabase("foo_db").getSchema("foo_db").containsTable("t")).thenReturn(true);
        when(result.getMetaDataContexts().getMetaData().getProps()).thenReturn(new ConfigurationProperties(new Properties()));
        return result;
    }
}
