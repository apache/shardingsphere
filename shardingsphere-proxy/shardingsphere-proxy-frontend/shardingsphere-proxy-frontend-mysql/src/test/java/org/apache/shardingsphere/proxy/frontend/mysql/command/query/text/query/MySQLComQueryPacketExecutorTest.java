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

import org.apache.shardingsphere.db.protocol.mysql.constant.MySQLCharacterSet;
import org.apache.shardingsphere.db.protocol.mysql.constant.MySQLConstants;
import org.apache.shardingsphere.db.protocol.mysql.packet.MySQLPacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.command.query.text.MySQLTextResultSetRowPacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.command.query.text.query.MySQLComQueryPacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.generic.MySQLOKPacket;
import org.apache.shardingsphere.db.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.parser.rule.builder.DefaultSQLParserRuleConfigurationBuilder;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.text.TextProtocolBackendHandler;
import org.apache.shardingsphere.proxy.frontend.command.executor.ResponseType;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.internal.util.reflection.FieldSetter;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class MySQLComQueryPacketExecutorTest {
    
    @Mock
    private TextProtocolBackendHandler textProtocolBackendHandler;
    
    @Mock
    private MySQLComQueryPacket packet;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ConnectionSession connectionSession;
    
    @Before
    public void setUp() {
        when(packet.getSql()).thenReturn("");
        when(connectionSession.getAttributeMap().attr(MySQLConstants.MYSQL_CHARACTER_SET_ATTRIBUTE_KEY).get()).thenReturn(MySQLCharacterSet.UTF8MB4_GENERAL_CI);
    }
    
    @Test
    public void assertIsQueryResponse() throws SQLException, NoSuchFieldException {
        MySQLComQueryPacketExecutor mysqlComQueryPacketExecutor = new MySQLComQueryPacketExecutor(packet, connectionSession);
        FieldSetter.setField(mysqlComQueryPacketExecutor, MySQLComQueryPacketExecutor.class.getDeclaredField("textProtocolBackendHandler"), textProtocolBackendHandler);
        when(textProtocolBackendHandler.execute()).thenReturn(new QueryResponseHeader(Collections.singletonList(mock(QueryHeader.class))));
        mysqlComQueryPacketExecutor.execute();
        assertThat(mysqlComQueryPacketExecutor.getResponseType(), is(ResponseType.QUERY));
    }
    
    @Test
    public void assertIsUpdateResponse() throws SQLException, NoSuchFieldException {
        MySQLComQueryPacketExecutor mysqlComQueryPacketExecutor = new MySQLComQueryPacketExecutor(packet, connectionSession);
        FieldSetter.setField(mysqlComQueryPacketExecutor, MySQLComQueryPacketExecutor.class.getDeclaredField("textProtocolBackendHandler"), textProtocolBackendHandler);
        when(textProtocolBackendHandler.execute()).thenReturn(new UpdateResponseHeader(mock(SQLStatement.class)));
        mysqlComQueryPacketExecutor.execute();
        assertThat(mysqlComQueryPacketExecutor.getResponseType(), is(ResponseType.UPDATE));
    }
    
    @Test
    public void assertExecuteMultiUpdateStatements() throws SQLException, NoSuchFieldException {
        when(connectionSession.getAttributeMap().hasAttr(MySQLConstants.MYSQL_OPTION_MULTI_STATEMENTS)).thenReturn(true);
        when(connectionSession.getAttributeMap().attr(MySQLConstants.MYSQL_OPTION_MULTI_STATEMENTS).get()).thenReturn(0);
        when(connectionSession.getSchemaName()).thenReturn("schema_name");
        when(packet.getSql()).thenReturn("update t set v=v+1 where id=1;update t set v=v+1 where id=2;update t set v=v+1 where id=3");
        try (MockedStatic<ProxyContext> mockedStatic = mockStatic(ProxyContext.class)) {
            ProxyContext mockedProxyContext = mock(ProxyContext.class, RETURNS_DEEP_STUBS);
            mockedStatic.when(ProxyContext::getInstance).thenReturn(mockedProxyContext);
            when(ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData("schema_name").getResource().getDatabaseType()).thenReturn(new MySQLDatabaseType());
            when(ProxyContext.getInstance().getContextManager().getMetaDataContexts().getGlobalRuleMetaData().findSingleRule(SQLParserRule.class))
                    .thenReturn(Optional.of(new SQLParserRule(new DefaultSQLParserRuleConfigurationBuilder().build())));
            when(ProxyContext.getInstance().getContextManager().getMetaDataContexts().getProps().<Integer>getValue(ConfigurationPropertyKey.KERNEL_EXECUTOR_SIZE)).thenReturn(1);
            when(ProxyContext.getInstance().getContextManager().getMetaDataContexts().getProps().<Boolean>getValue(ConfigurationPropertyKey.SQL_SHOW)).thenReturn(false);
            MySQLComQueryPacketExecutor actual = new MySQLComQueryPacketExecutor(packet, connectionSession);
            FieldSetter.setField(actual, MySQLComQueryPacketExecutor.class.getDeclaredField("textProtocolBackendHandler"), textProtocolBackendHandler);
            when(textProtocolBackendHandler.execute()).thenReturn(new UpdateResponseHeader(mock(SQLStatement.class)));
            Collection<DatabasePacket<?>> actualPackets = actual.execute();
            assertThat(actualPackets.size(), is(1));
            assertTrue(actualPackets.iterator().next() instanceof MySQLOKPacket);
        }
    }
    
    @Test
    public void assertNext() throws SQLException, NoSuchFieldException {
        MySQLComQueryPacketExecutor actual = new MySQLComQueryPacketExecutor(packet, connectionSession);
        FieldSetter.setField(actual, MySQLComQueryPacketExecutor.class.getDeclaredField("textProtocolBackendHandler"), textProtocolBackendHandler);
        when(textProtocolBackendHandler.next()).thenReturn(true, false);
        assertTrue(actual.next());
        assertFalse(actual.next());
    }
    
    @Test
    public void assertGetQueryRowPacket() throws SQLException {
        MySQLPacket actual = new MySQLComQueryPacketExecutor(packet, connectionSession).getQueryRowPacket();
        assertTrue(actual instanceof MySQLTextResultSetRowPacket);
    }
    
    @Test
    public void assertClose() throws SQLException, NoSuchFieldException {
        MySQLComQueryPacketExecutor actual = new MySQLComQueryPacketExecutor(packet, connectionSession);
        FieldSetter.setField(actual, MySQLComQueryPacketExecutor.class.getDeclaredField("textProtocolBackendHandler"), textProtocolBackendHandler);
        actual.close();
        verify(textProtocolBackendHandler).close();
    }
}
