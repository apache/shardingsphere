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

package org.apache.shardingsphere.proxy.frontend.mysql.command.query.text.fieldlist;

import io.netty.util.DefaultAttributeMap;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.protocol.mysql.constant.MySQLCharacterSets;
import org.apache.shardingsphere.database.protocol.mysql.constant.MySQLConstants;
import org.apache.shardingsphere.database.protocol.mysql.packet.command.query.MySQLColumnDefinition41Packet;
import org.apache.shardingsphere.database.protocol.mysql.packet.command.query.text.fieldlist.MySQLComFieldListPacket;
import org.apache.shardingsphere.database.protocol.mysql.packet.generic.MySQLEofPacket;
import org.apache.shardingsphere.database.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.engine.SQLBindEngine;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.parser.SQLParserEngine;
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.proxy.backend.connector.DatabaseProxyConnector;
import org.apache.shardingsphere.proxy.backend.connector.DatabaseProxyConnectorFactory;
import org.apache.shardingsphere.proxy.backend.connector.ProxyDatabaseConnectionManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.response.data.QueryResponseCell;
import org.apache.shardingsphere.proxy.backend.response.data.QueryResponseRow;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.session.transaction.TransactionStatus;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;

import java.sql.SQLException;
import java.sql.Types;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings({ProxyContext.class, DatabaseProxyConnectorFactory.class})
class MySQLComFieldListPacketExecutorTest {
    
    private static final String TABLE_NAME = "foo_table";
    
    private static final String DATABASE_NAME = "foo_db";
    
    @Mock
    private MySQLComFieldListPacket packet;
    
    @Mock
    private ConnectionSession connectionSession;
    
    @Mock
    private ProxyDatabaseConnectionManager databaseConnectionManager;
    
    @Mock
    private SQLStatement sqlStatement;
    
    @BeforeEach
    void setUp() {
        DefaultAttributeMap attributeMap = new DefaultAttributeMap();
        attributeMap.attr(MySQLConstants.CHARACTER_SET_ATTRIBUTE_KEY).set(MySQLCharacterSets.UTF8MB4_GENERAL_CI);
        when(packet.getTable()).thenReturn(TABLE_NAME);
        when(connectionSession.getCurrentDatabaseName()).thenReturn(DATABASE_NAME);
        when(connectionSession.getAttributeMap()).thenReturn(attributeMap);
        when(connectionSession.getDatabaseConnectionManager()).thenReturn(databaseConnectionManager);
        when(connectionSession.getConnectionContext()).thenReturn(new ConnectionContext(Collections::emptyList));
        when(connectionSession.getTransactionStatus()).thenReturn(new TransactionStatus());
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        RuleMetaData globalRuleMetaData = mock(RuleMetaData.class);
        when(contextManager.getMetaDataContexts().getMetaData().getGlobalRuleMetaData()).thenReturn(globalRuleMetaData);
        SQLParserRule sqlParserRule = mock(SQLParserRule.class);
        when(globalRuleMetaData.getSingleRule(SQLParserRule.class)).thenReturn(sqlParserRule);
        SQLParserEngine sqlParserEngine = mock(SQLParserEngine.class);
        when(sqlParserRule.getSQLParserEngine(any(DatabaseType.class))).thenReturn(sqlParserEngine);
        when(sqlParserEngine.parse(anyString(), eq(false))).thenReturn(sqlStatement);
        ProxyContext proxyContext = mock(ProxyContext.class);
        when(proxyContext.getContextManager()).thenReturn(contextManager);
        when(ProxyContext.getInstance()).thenReturn(proxyContext);
    }
    
    @Test
    void assertExecute() throws SQLException {
        try (
                MockedConstruction<SQLBindEngine> mockedBindEngine = mockConstruction(SQLBindEngine.class,
                        (mock, context) -> when(mock.bind(sqlStatement)).thenReturn(mock(SQLStatementContext.class)));
                MockedConstruction<QueryContext> ignoredQueryContext = mockConstruction(QueryContext.class)) {
            QueryResponseCell columnCell = new QueryResponseCell(Types.VARCHAR, "foo_column");
            DatabaseProxyConnector databaseProxyConnector = mock(DatabaseProxyConnector.class);
            when(databaseProxyConnector.next()).thenReturn(true, false);
            when(databaseProxyConnector.getRowData()).thenReturn(new QueryResponseRow(Collections.singletonList(columnCell)));
            when(DatabaseProxyConnectorFactory.newInstance(any(QueryContext.class), eq(databaseConnectionManager), eq(false))).thenReturn(databaseProxyConnector);
            MySQLComFieldListPacketExecutor executor = new MySQLComFieldListPacketExecutor(packet, connectionSession);
            executor.close();
            Collection<DatabasePacket> actual = executor.execute();
            executor.close();
            assertThat(actual.size(), is(2));
            LinkedList<DatabasePacket> actualPackets = new LinkedList<>(actual);
            assertThat(actualPackets.getFirst(), isA(MySQLColumnDefinition41Packet.class));
            assertThat(actualPackets.getLast(), isA(MySQLEofPacket.class));
            assertThat(mockedBindEngine.constructed().size(), is(1));
        }
    }
}
