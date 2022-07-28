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

package org.apache.shardingsphere.proxy.frontend.mysql.command.query.binary.prepare;

import org.apache.shardingsphere.db.protocol.mysql.constant.MySQLCharacterSet;
import org.apache.shardingsphere.db.protocol.mysql.constant.MySQLConstants;
import org.apache.shardingsphere.db.protocol.mysql.packet.command.query.MySQLColumnDefinition41Packet;
import org.apache.shardingsphere.proxy.frontend.mysql.command.query.binary.MySQLStatementIDGenerator;
import org.apache.shardingsphere.db.protocol.mysql.packet.command.query.binary.prepare.MySQLComStmtPrepareOKPacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.command.query.binary.prepare.MySQLComStmtPreparePacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.generic.MySQLEofPacket;
import org.apache.shardingsphere.db.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.UpdateStatementContext;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.parser.config.SQLParserRuleConfiguration;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.session.PreparedStatementRegistry;
import org.apache.shardingsphere.proxy.frontend.exception.UnsupportedPreparedStatementException;
import org.apache.shardingsphere.proxy.frontend.mysql.ProxyContextRestorer;
import org.apache.shardingsphere.proxy.frontend.mysql.command.query.binary.MySQLPreparedStatement;
import org.apache.shardingsphere.sql.parser.api.CacheOption;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLUpdateStatement;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.SQLException;
import java.util.Iterator;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class MySQLComStmtPrepareExecutorTest extends ProxyContextRestorer {
    
    @Mock
    private MySQLComStmtPreparePacket packet;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ConnectionSession connectionSession;
    
    @Before
    public void setup() {
        ProxyContext.init(mock(ContextManager.class, RETURNS_DEEP_STUBS));
        prepareSQLParser();
        when(connectionSession.getPreparedStatementRegistry()).thenReturn(new PreparedStatementRegistry());
        when(connectionSession.getAttributeMap().attr(MySQLConstants.MYSQL_CHARACTER_SET_ATTRIBUTE_KEY).get()).thenReturn(MySQLCharacterSet.UTF8MB4_UNICODE_CI);
    }
    
    private void prepareSQLParser() {
        ContextManager contextManager = ProxyContext.getInstance().getContextManager();
        MetaDataContexts metaDataContexts = contextManager.getMetaDataContexts();
        when(metaDataContexts.getMetaData().getGlobalRuleMetaData()).thenReturn(mock(ShardingSphereRuleMetaData.class));
        CacheOption cacheOption = new CacheOption(1024, 1024);
        when(metaDataContexts.getMetaData().getGlobalRuleMetaData().getSingleRule(SQLParserRule.class))
                .thenReturn(new SQLParserRule(new SQLParserRuleConfiguration(false, cacheOption, cacheOption)));
        when(metaDataContexts.getMetaData().getDatabase(connectionSession.getDatabaseName()).getProtocolType()).thenReturn(new MySQLDatabaseType());
    }
    
    @Test(expected = UnsupportedPreparedStatementException.class)
    public void assertPrepareMultiStatements() throws SQLException {
        when(packet.getSql()).thenReturn("update t set v=v+1 where id=1;update t set v=v+1 where id=2;update t set v=v+1 where id=3");
        when(connectionSession.getAttributeMap().hasAttr(MySQLConstants.MYSQL_OPTION_MULTI_STATEMENTS)).thenReturn(true);
        when(connectionSession.getAttributeMap().attr(MySQLConstants.MYSQL_OPTION_MULTI_STATEMENTS).get()).thenReturn(0);
        new MySQLComStmtPrepareExecutor(packet, connectionSession).execute();
    }
    
    @Test
    public void assertPrepareSelectStatement() throws SQLException {
        String sql = "select name from db.user where id = ?";
        when(packet.getSql()).thenReturn(sql);
        when(connectionSession.getConnectionId()).thenReturn(1);
        MySQLStatementIDGenerator.getInstance().registerConnection(1);
        Iterator<DatabasePacket<?>> actualIterator = new MySQLComStmtPrepareExecutor(packet, connectionSession).execute().iterator();
        assertThat(actualIterator.next(), instanceOf(MySQLComStmtPrepareOKPacket.class));
        assertThat(actualIterator.next(), instanceOf(MySQLColumnDefinition41Packet.class));
        assertThat(actualIterator.next(), instanceOf(MySQLEofPacket.class));
        assertThat(actualIterator.next(), instanceOf(MySQLColumnDefinition41Packet.class));
        assertThat(actualIterator.next(), instanceOf(MySQLEofPacket.class));
        assertFalse(actualIterator.hasNext());
        MySQLPreparedStatement actualPreparedStatement = connectionSession.getPreparedStatementRegistry().getPreparedStatement(1);
        assertThat(actualPreparedStatement.getSql(), is(sql));
        assertThat(actualPreparedStatement.getSqlStatement(), instanceOf(MySQLSelectStatement.class));
        assertThat(actualPreparedStatement.getSqlStatementContext().get(), instanceOf(SelectStatementContext.class));
        MySQLStatementIDGenerator.getInstance().unregisterConnection(1);
    }
    
    @Test
    public void assertPrepareUpdateStatement() throws SQLException {
        String sql = "update t set v = ?";
        when(packet.getSql()).thenReturn(sql);
        when(connectionSession.getConnectionId()).thenReturn(1);
        MySQLStatementIDGenerator.getInstance().registerConnection(1);
        Iterator<DatabasePacket<?>> actualIterator = new MySQLComStmtPrepareExecutor(packet, connectionSession).execute().iterator();
        assertThat(actualIterator.next(), instanceOf(MySQLComStmtPrepareOKPacket.class));
        assertThat(actualIterator.next(), instanceOf(MySQLColumnDefinition41Packet.class));
        assertThat(actualIterator.next(), instanceOf(MySQLEofPacket.class));
        assertFalse(actualIterator.hasNext());
        MySQLPreparedStatement actualPreparedStatement = connectionSession.getPreparedStatementRegistry().getPreparedStatement(1);
        assertThat(actualPreparedStatement.getSql(), is(sql));
        assertThat(actualPreparedStatement.getSqlStatement(), instanceOf(MySQLUpdateStatement.class));
        assertThat(actualPreparedStatement.getSqlStatementContext().get(), instanceOf(UpdateStatementContext.class));
        MySQLStatementIDGenerator.getInstance().unregisterConnection(1);
    }
    
    @Test(expected = UnsupportedPreparedStatementException.class)
    public void assertPrepareNotAllowedStatement() throws SQLException {
        when(packet.getSql()).thenReturn("begin");
        new MySQLComStmtPrepareExecutor(packet, connectionSession).execute();
    }
}
