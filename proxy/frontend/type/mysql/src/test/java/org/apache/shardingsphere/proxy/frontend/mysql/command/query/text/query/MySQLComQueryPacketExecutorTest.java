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
import org.apache.shardingsphere.db.protocol.mysql.packet.command.query.text.MySQLTextResultSetRowPacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.command.query.text.query.MySQLComQueryPacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.generic.MySQLOKPacket;
import org.apache.shardingsphere.db.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.database.mysql.MySQLDatabaseType;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.logging.rule.LoggingRule;
import org.apache.shardingsphere.logging.rule.builder.DefaultLoggingRuleConfigurationBuilder;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.parser.rule.builder.DefaultSQLParserRuleConfigurationBuilder;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.handler.ProxyBackendHandler;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryHeader;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.command.executor.ResponseType;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sqltranslator.rule.SQLTranslatorRule;
import org.apache.shardingsphere.sqltranslator.rule.builder.DefaultSQLTranslatorRuleConfigurationBuilder;
import org.apache.shardingsphere.test.mock.AutoMockExtension;
import org.apache.shardingsphere.test.mock.StaticMockSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.internal.configuration.plugins.Plugins;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.plugins.MemberAccessor;
import org.mockito.quality.Strictness;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ProxyContext.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MySQLComQueryPacketExecutorTest {
    
    @Mock
    private ProxyBackendHandler proxyBackendHandler;
    
    @Mock
    private MySQLComQueryPacket packet;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ConnectionSession connectionSession;
    
    @BeforeEach
    void setUp() {
        when(packet.getSQL()).thenReturn("");
        when(connectionSession.getAttributeMap().attr(MySQLConstants.MYSQL_CHARACTER_SET_ATTRIBUTE_KEY).get()).thenReturn(MySQLCharacterSet.UTF8MB4_GENERAL_CI);
    }
    
    @Test
    void assertIsQueryResponse() throws SQLException, NoSuchFieldException, IllegalAccessException {
        MySQLComQueryPacketExecutor mysqlComQueryPacketExecutor = new MySQLComQueryPacketExecutor(packet, connectionSession);
        MemberAccessor accessor = Plugins.getMemberAccessor();
        accessor.set(MySQLComQueryPacketExecutor.class.getDeclaredField("proxyBackendHandler"), mysqlComQueryPacketExecutor, proxyBackendHandler);
        QueryHeader queryHeader = mock(QueryHeader.class);
        when(queryHeader.getColumnTypeName()).thenReturn("VARCHAR");
        when(proxyBackendHandler.execute()).thenReturn(new QueryResponseHeader(Collections.singletonList(queryHeader)));
        mysqlComQueryPacketExecutor.execute();
        assertThat(mysqlComQueryPacketExecutor.getResponseType(), is(ResponseType.QUERY));
    }
    
    @Test
    void assertIsUpdateResponse() throws SQLException, NoSuchFieldException, IllegalAccessException {
        MySQLComQueryPacketExecutor mysqlComQueryPacketExecutor = new MySQLComQueryPacketExecutor(packet, connectionSession);
        MemberAccessor accessor = Plugins.getMemberAccessor();
        accessor.set(MySQLComQueryPacketExecutor.class.getDeclaredField("proxyBackendHandler"), mysqlComQueryPacketExecutor, proxyBackendHandler);
        when(proxyBackendHandler.execute()).thenReturn(new UpdateResponseHeader(mock(SQLStatement.class)));
        mysqlComQueryPacketExecutor.execute();
        assertThat(mysqlComQueryPacketExecutor.getResponseType(), is(ResponseType.UPDATE));
    }
    
    @Test
    void assertExecuteMultiUpdateStatements() throws SQLException, NoSuchFieldException, IllegalAccessException {
        when(connectionSession.getAttributeMap().hasAttr(MySQLConstants.MYSQL_OPTION_MULTI_STATEMENTS)).thenReturn(true);
        when(connectionSession.getAttributeMap().attr(MySQLConstants.MYSQL_OPTION_MULTI_STATEMENTS).get()).thenReturn(0);
        when(connectionSession.getDatabaseName()).thenReturn("foo_db");
        when(packet.getSQL()).thenReturn("update t set v=v+1 where id=1;update t set v=v+1 where id=2;update t set v=v+1 where id=3");
        ContextManager contextManager = mock(ContextManager.class);
        MetaDataContexts metaDataContexts = mockMetaDataContexts();
        when(contextManager.getMetaDataContexts()).thenReturn(metaDataContexts);
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        MySQLComQueryPacketExecutor actual = new MySQLComQueryPacketExecutor(packet, connectionSession);
        MemberAccessor accessor = Plugins.getMemberAccessor();
        accessor.set(MySQLComQueryPacketExecutor.class.getDeclaredField("proxyBackendHandler"), actual, proxyBackendHandler);
        when(proxyBackendHandler.execute()).thenReturn(new UpdateResponseHeader(mock(SQLStatement.class)));
        Collection<DatabasePacket> actualPackets = actual.execute();
        assertThat(actualPackets.size(), is(1));
        assertThat(actualPackets.iterator().next(), instanceOf(MySQLOKPacket.class));
    }
    
    private MetaDataContexts mockMetaDataContexts() {
        MetaDataContexts result = mock(MetaDataContexts.class, RETURNS_DEEP_STUBS);
        when(result.getMetaData().getDatabase("foo_db").getResourceMetaData().getStorageTypes()).thenReturn(Collections.singletonMap("foo_ds", new MySQLDatabaseType()));
        when(result.getMetaData().getDatabase("foo_db").getProtocolType()).thenReturn(new MySQLDatabaseType());
        ShardingSphereRuleMetaData globalRuleMetaData = new ShardingSphereRuleMetaData(
                Arrays.asList(new SQLParserRule(new DefaultSQLParserRuleConfigurationBuilder().build()), new SQLTranslatorRule(new DefaultSQLTranslatorRuleConfigurationBuilder().build()),
                        new LoggingRule(new DefaultLoggingRuleConfigurationBuilder().build())));
        when(result.getMetaData().getGlobalRuleMetaData()).thenReturn(globalRuleMetaData);
        when(result.getMetaData().getProps().<Integer>getValue(ConfigurationPropertyKey.KERNEL_EXECUTOR_SIZE)).thenReturn(1);
        when(result.getMetaData().getProps().<Boolean>getValue(ConfigurationPropertyKey.SQL_SHOW)).thenReturn(false);
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getRuleMetaData()).thenReturn(new ShardingSphereRuleMetaData(Collections.emptyList()));
        when(result.getMetaData().getDatabase("foo_db")).thenReturn(database);
        return result;
        
    }
    
    @Test
    void assertNext() throws SQLException, NoSuchFieldException, IllegalAccessException {
        MySQLComQueryPacketExecutor actual = new MySQLComQueryPacketExecutor(packet, connectionSession);
        MemberAccessor accessor = Plugins.getMemberAccessor();
        accessor.set(MySQLComQueryPacketExecutor.class.getDeclaredField("proxyBackendHandler"), actual, proxyBackendHandler);
        when(proxyBackendHandler.next()).thenReturn(true, false);
        assertTrue(actual.next());
        assertFalse(actual.next());
    }
    
    @Test
    void assertGetQueryRowPacket() throws SQLException {
        assertThat(new MySQLComQueryPacketExecutor(packet, connectionSession).getQueryRowPacket(), instanceOf(MySQLTextResultSetRowPacket.class));
    }
    
    @Test
    void assertClose() throws SQLException, NoSuchFieldException, IllegalAccessException {
        MySQLComQueryPacketExecutor actual = new MySQLComQueryPacketExecutor(packet, connectionSession);
        MemberAccessor accessor = Plugins.getMemberAccessor();
        accessor.set(MySQLComQueryPacketExecutor.class.getDeclaredField("proxyBackendHandler"), actual, proxyBackendHandler);
        actual.close();
        verify(proxyBackendHandler).close();
    }
}
