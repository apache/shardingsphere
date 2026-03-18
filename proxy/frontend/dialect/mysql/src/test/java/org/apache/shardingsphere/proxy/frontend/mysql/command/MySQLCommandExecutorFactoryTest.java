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

package org.apache.shardingsphere.proxy.frontend.mysql.command;

import org.apache.shardingsphere.database.protocol.mysql.constant.MySQLCharacterSets;
import org.apache.shardingsphere.database.protocol.mysql.constant.MySQLConstants;
import org.apache.shardingsphere.database.protocol.mysql.packet.command.MySQLCommandPacketType;
import org.apache.shardingsphere.database.protocol.mysql.packet.command.admin.MySQLComSetOptionPacket;
import org.apache.shardingsphere.database.protocol.mysql.packet.command.admin.initdb.MySQLComInitDbPacket;
import org.apache.shardingsphere.database.protocol.mysql.packet.command.query.binary.MySQLComStmtSendLongDataPacket;
import org.apache.shardingsphere.database.protocol.mysql.packet.command.query.binary.close.MySQLComStmtClosePacket;
import org.apache.shardingsphere.database.protocol.mysql.packet.command.query.binary.execute.MySQLComStmtExecutePacket;
import org.apache.shardingsphere.database.protocol.mysql.packet.command.query.binary.prepare.MySQLComStmtPreparePacket;
import org.apache.shardingsphere.database.protocol.mysql.packet.command.query.binary.reset.MySQLComStmtResetPacket;
import org.apache.shardingsphere.database.protocol.mysql.packet.command.query.text.fieldlist.MySQLComFieldListPacket;
import org.apache.shardingsphere.database.protocol.mysql.packet.command.query.text.query.MySQLComQueryPacket;
import org.apache.shardingsphere.database.protocol.packet.command.CommandPacket;
import org.apache.shardingsphere.proxy.backend.connector.ProxyDatabaseConnectionManager;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.mysql.command.admin.MySQLComResetConnectionExecutor;
import org.apache.shardingsphere.proxy.frontend.mysql.command.admin.MySQLComSetOptionExecutor;
import org.apache.shardingsphere.proxy.frontend.mysql.command.admin.initdb.MySQLComInitDbExecutor;
import org.apache.shardingsphere.proxy.frontend.mysql.command.admin.ping.MySQLComPingExecutor;
import org.apache.shardingsphere.proxy.frontend.mysql.command.admin.quit.MySQLComQuitExecutor;
import org.apache.shardingsphere.proxy.frontend.mysql.command.generic.MySQLUnsupportedCommandExecutor;
import org.apache.shardingsphere.proxy.frontend.mysql.command.query.binary.MySQLComStmtSendLongDataExecutor;
import org.apache.shardingsphere.proxy.frontend.mysql.command.query.binary.close.MySQLComStmtCloseExecutor;
import org.apache.shardingsphere.proxy.frontend.mysql.command.query.binary.execute.MySQLComStmtExecuteExecutor;
import org.apache.shardingsphere.proxy.frontend.mysql.command.query.binary.prepare.MySQLComStmtPrepareExecutor;
import org.apache.shardingsphere.proxy.frontend.mysql.command.query.binary.reset.MySQLComStmtResetExecutor;
import org.apache.shardingsphere.proxy.frontend.mysql.command.query.text.fieldlist.MySQLComFieldListPacketExecutor;
import org.apache.shardingsphere.proxy.frontend.mysql.command.query.text.query.MySQLComQueryPacketExecutor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.sql.SQLException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MySQLCommandExecutorFactoryTest {
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ConnectionSession connectionSession;
    
    @Mock
    private ProxyDatabaseConnectionManager databaseConnectionManager;
    
    @BeforeEach
    void setUp() {
        when(connectionSession.getAttributeMap().attr(MySQLConstants.CHARACTER_SET_ATTRIBUTE_KEY).get()).thenReturn(MySQLCharacterSets.UTF8MB4_GENERAL_CI);
        when(connectionSession.getDatabaseConnectionManager()).thenReturn(databaseConnectionManager);
    }
    
    @Test
    void assertNewInstanceWithComQuit() throws SQLException {
        assertThat(MySQLCommandExecutorFactory.newInstance(MySQLCommandPacketType.COM_QUIT, mock(CommandPacket.class), connectionSession), isA(MySQLComQuitExecutor.class));
    }
    
    @Test
    void assertNewInstanceWithComInitDb() throws SQLException {
        assertThat(MySQLCommandExecutorFactory.newInstance(MySQLCommandPacketType.COM_INIT_DB, mock(MySQLComInitDbPacket.class), connectionSession), isA(MySQLComInitDbExecutor.class));
    }
    
    @Test
    void assertNewInstanceWithComFieldList() throws SQLException {
        MySQLComFieldListPacket packet = mock(MySQLComFieldListPacket.class);
        assertThat(MySQLCommandExecutorFactory.newInstance(MySQLCommandPacketType.COM_FIELD_LIST, packet, connectionSession), isA(MySQLComFieldListPacketExecutor.class));
    }
    
    @Test
    void assertNewInstanceWithComQuery() throws SQLException {
        MySQLComQueryPacket packet = mock(MySQLComQueryPacket.class);
        when(packet.getSQL()).thenReturn("");
        assertThat(MySQLCommandExecutorFactory.newInstance(MySQLCommandPacketType.COM_QUERY, packet, connectionSession), isA(MySQLComQueryPacketExecutor.class));
    }
    
    @Test
    void assertNewInstanceWithComPing() throws SQLException {
        assertThat(MySQLCommandExecutorFactory.newInstance(MySQLCommandPacketType.COM_PING, mock(CommandPacket.class), connectionSession), isA(MySQLComPingExecutor.class));
    }
    
    @Test
    void assertNewInstanceWithComStmtPrepare() throws SQLException {
        assertThat(MySQLCommandExecutorFactory.newInstance(
                MySQLCommandPacketType.COM_STMT_PREPARE, mock(MySQLComStmtPreparePacket.class), connectionSession), isA(MySQLComStmtPrepareExecutor.class));
    }
    
    @Test
    void assertNewInstanceWithComStmtExecute() throws SQLException {
        assertThat(MySQLCommandExecutorFactory.newInstance(MySQLCommandPacketType.COM_STMT_EXECUTE, mock(MySQLComStmtExecutePacket.class), connectionSession),
                isA(MySQLComStmtExecuteExecutor.class));
    }
    
    @Test
    void assertNewInstanceWithComStmtSendLongData() throws SQLException {
        assertThat(MySQLCommandExecutorFactory.newInstance(MySQLCommandPacketType.COM_STMT_SEND_LONG_DATA, mock(MySQLComStmtSendLongDataPacket.class), connectionSession),
                isA(MySQLComStmtSendLongDataExecutor.class));
    }
    
    @Test
    void assertNewInstanceWithComStmtReset() throws SQLException {
        assertThat(MySQLCommandExecutorFactory.newInstance(MySQLCommandPacketType.COM_STMT_RESET, mock(MySQLComStmtResetPacket.class), connectionSession), isA(MySQLComStmtResetExecutor.class));
    }
    
    @Test
    void assertNewInstanceWithComStmtClose() throws SQLException {
        assertThat(MySQLCommandExecutorFactory.newInstance(MySQLCommandPacketType.COM_STMT_CLOSE, mock(MySQLComStmtClosePacket.class), connectionSession), isA(MySQLComStmtCloseExecutor.class));
    }
    
    @Test
    void assertNewInstanceWithComSetOption() throws SQLException {
        assertThat(MySQLCommandExecutorFactory.newInstance(MySQLCommandPacketType.COM_SET_OPTION, mock(MySQLComSetOptionPacket.class), connectionSession), isA(MySQLComSetOptionExecutor.class));
    }
    
    @Test
    void assertNewInstanceWithComResetConnection() throws SQLException {
        assertThat(MySQLCommandExecutorFactory.newInstance(MySQLCommandPacketType.COM_RESET_CONNECTION, mock(MySQLComSetOptionPacket.class), connectionSession),
                isA(MySQLComResetConnectionExecutor.class));
    }
    
    @Test
    void assertNewInstanceWithUnsupportedCommand() throws SQLException {
        assertThat(MySQLCommandExecutorFactory.newInstance(MySQLCommandPacketType.COM_REFRESH, mock(CommandPacket.class), connectionSession), isA(MySQLUnsupportedCommandExecutor.class));
    }
}
