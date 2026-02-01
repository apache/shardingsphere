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

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.protocol.mysql.constant.MySQLNewParametersBoundFlag;
import org.apache.shardingsphere.database.protocol.mysql.packet.command.MySQLCommandPacketType;
import org.apache.shardingsphere.database.protocol.mysql.packet.command.admin.MySQLComResetConnectionPacket;
import org.apache.shardingsphere.database.protocol.mysql.packet.command.admin.MySQLComSetOptionPacket;
import org.apache.shardingsphere.database.protocol.mysql.packet.command.admin.MySQLUnsupportedCommandPacket;
import org.apache.shardingsphere.database.protocol.mysql.packet.command.admin.initdb.MySQLComInitDbPacket;
import org.apache.shardingsphere.database.protocol.mysql.packet.command.admin.ping.MySQLComPingPacket;
import org.apache.shardingsphere.database.protocol.mysql.packet.command.admin.quit.MySQLComQuitPacket;
import org.apache.shardingsphere.database.protocol.mysql.packet.command.query.binary.MySQLComStmtSendLongDataPacket;
import org.apache.shardingsphere.database.protocol.mysql.packet.command.query.binary.close.MySQLComStmtClosePacket;
import org.apache.shardingsphere.database.protocol.mysql.packet.command.query.binary.execute.MySQLComStmtExecutePacket;
import org.apache.shardingsphere.database.protocol.mysql.packet.command.query.binary.prepare.MySQLComStmtPreparePacket;
import org.apache.shardingsphere.database.protocol.mysql.packet.command.query.binary.reset.MySQLComStmtResetPacket;
import org.apache.shardingsphere.database.protocol.mysql.packet.command.query.text.fieldlist.MySQLComFieldListPacket;
import org.apache.shardingsphere.database.protocol.mysql.packet.command.query.text.query.MySQLComQueryPacket;
import org.apache.shardingsphere.database.protocol.mysql.payload.MySQLPacketPayload;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.CommonSQLStatementContext;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.session.ServerPreparedStatementRegistry;
import org.apache.shardingsphere.proxy.frontend.mysql.command.query.binary.MySQLServerPreparedStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MySQLCommandPacketFactoryTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "MySQL");
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private MySQLPacketPayload payload;
    
    @Mock
    private ConnectionSession connectionSession;
    
    @Test
    void assertNewInstanceWithComQuitPacket() {
        assertThat(MySQLCommandPacketFactory.newInstance(MySQLCommandPacketType.COM_QUIT, payload, connectionSession), isA(MySQLComQuitPacket.class));
    }
    
    @Test
    void assertNewInstanceWithComInitDbPacket() {
        assertThat(MySQLCommandPacketFactory.newInstance(MySQLCommandPacketType.COM_INIT_DB, payload, connectionSession), isA(MySQLComInitDbPacket.class));
    }
    
    @Test
    void assertNewInstanceWithComFieldListPacket() {
        assertThat(MySQLCommandPacketFactory.newInstance(MySQLCommandPacketType.COM_FIELD_LIST, payload, connectionSession), isA(MySQLComFieldListPacket.class));
    }
    
    @Test
    void assertNewInstanceWithComQueryPacket() {
        when(payload.readStringEOF()).thenReturn("SHOW TABLES");
        assertThat(MySQLCommandPacketFactory.newInstance(MySQLCommandPacketType.COM_QUERY, payload, connectionSession), isA(MySQLComQueryPacket.class));
    }
    
    @Test
    void assertNewInstanceWithComStmtPreparePacket() {
        when(payload.readStringEOF()).thenReturn("");
        assertThat(MySQLCommandPacketFactory.newInstance(MySQLCommandPacketType.COM_STMT_PREPARE, payload, connectionSession), isA(MySQLComStmtPreparePacket.class));
    }
    
    @Test
    void assertNewInstanceWithComStmtExecutePacket() {
        when(payload.readInt1()).thenReturn(MySQLNewParametersBoundFlag.PARAMETER_TYPE_EXIST.getValue());
        when(payload.readInt4()).thenReturn(1);
        when(payload.getByteBuf().getIntLE(anyInt())).thenReturn(1);
        ServerPreparedStatementRegistry serverPreparedStatementRegistry = new ServerPreparedStatementRegistry();
        when(connectionSession.getServerPreparedStatementRegistry()).thenReturn(serverPreparedStatementRegistry);
        SelectStatement sqlStatement = new SelectStatement(databaseType);
        sqlStatement.buildAttributes();
        SQLStatementContext sqlStatementContext = new CommonSQLStatementContext(sqlStatement);
        serverPreparedStatementRegistry.addPreparedStatement(1, new MySQLServerPreparedStatement("SELECT 1", sqlStatementContext, new HintValueContext(), Collections.emptyList()));
        assertThat(MySQLCommandPacketFactory.newInstance(MySQLCommandPacketType.COM_STMT_EXECUTE, payload, connectionSession), isA(MySQLComStmtExecutePacket.class));
    }
    
    @Test
    void assertNewInstanceWithComStmtClosePacket() {
        assertThat(MySQLCommandPacketFactory.newInstance(MySQLCommandPacketType.COM_STMT_CLOSE, payload, connectionSession), isA(MySQLComStmtClosePacket.class));
    }
    
    @Test
    void assertNewInstanceWithComPingPacket() {
        assertThat(MySQLCommandPacketFactory.newInstance(MySQLCommandPacketType.COM_PING, payload, connectionSession), isA(MySQLComPingPacket.class));
    }
    
    @Test
    void assertNewInstanceWithComResetConnectionPacket() {
        assertThat(MySQLCommandPacketFactory.newInstance(MySQLCommandPacketType.COM_RESET_CONNECTION, payload, connectionSession), isA(MySQLComResetConnectionPacket.class));
    }
    
    @Test
    void assertNewInstanceWithComSleepPacket() {
        assertThat(MySQLCommandPacketFactory.newInstance(MySQLCommandPacketType.COM_SLEEP, payload, connectionSession), isA(MySQLUnsupportedCommandPacket.class));
    }
    
    @Test
    void assertNewInstanceWithComCreateDbPacket() {
        assertThat(MySQLCommandPacketFactory.newInstance(MySQLCommandPacketType.COM_CREATE_DB, payload, connectionSession), isA(MySQLUnsupportedCommandPacket.class));
    }
    
    @Test
    void assertNewInstanceWithComDropDbPacket() {
        assertThat(MySQLCommandPacketFactory.newInstance(MySQLCommandPacketType.COM_DROP_DB, payload, connectionSession), isA(MySQLUnsupportedCommandPacket.class));
    }
    
    @Test
    void assertNewInstanceWithComRefreshPacket() {
        assertThat(MySQLCommandPacketFactory.newInstance(MySQLCommandPacketType.COM_REFRESH, payload, connectionSession), isA(MySQLUnsupportedCommandPacket.class));
    }
    
    @Test
    void assertNewInstanceWithComShutDownPacket() {
        assertThat(MySQLCommandPacketFactory.newInstance(MySQLCommandPacketType.COM_SHUTDOWN, payload, connectionSession), isA(MySQLUnsupportedCommandPacket.class));
    }
    
    @Test
    void assertNewInstanceWithComStatisticsPacket() {
        assertThat(MySQLCommandPacketFactory.newInstance(MySQLCommandPacketType.COM_STATISTICS, payload, connectionSession), isA(MySQLUnsupportedCommandPacket.class));
    }
    
    @Test
    void assertNewInstanceWithComProcessInfoPacket() {
        assertThat(MySQLCommandPacketFactory.newInstance(MySQLCommandPacketType.COM_PROCESS_INFO, payload, connectionSession), isA(MySQLUnsupportedCommandPacket.class));
    }
    
    @Test
    void assertNewInstanceWithComConnectPacket() {
        assertThat(MySQLCommandPacketFactory.newInstance(MySQLCommandPacketType.COM_CONNECT, payload, connectionSession), isA(MySQLUnsupportedCommandPacket.class));
    }
    
    @Test
    void assertNewInstanceWithComProcessKillPacket() {
        assertThat(MySQLCommandPacketFactory.newInstance(MySQLCommandPacketType.COM_PROCESS_KILL, payload, connectionSession), isA(MySQLUnsupportedCommandPacket.class));
    }
    
    @Test
    void assertNewInstanceWithComDebugPacket() {
        assertThat(MySQLCommandPacketFactory.newInstance(MySQLCommandPacketType.COM_DEBUG, payload, connectionSession), isA(MySQLUnsupportedCommandPacket.class));
    }
    
    @Test
    void assertNewInstanceWithComTimePacket() {
        assertThat(MySQLCommandPacketFactory.newInstance(MySQLCommandPacketType.COM_TIME, payload, connectionSession), isA(MySQLUnsupportedCommandPacket.class));
    }
    
    @Test
    void assertNewInstanceWithComDelayedInsertPacket() {
        assertThat(MySQLCommandPacketFactory.newInstance(MySQLCommandPacketType.COM_DELAYED_INSERT, payload, connectionSession), isA(MySQLUnsupportedCommandPacket.class));
    }
    
    @Test
    void assertNewInstanceWithComChangeUserPacket() {
        assertThat(MySQLCommandPacketFactory.newInstance(MySQLCommandPacketType.COM_CHANGE_USER, payload, connectionSession), isA(MySQLUnsupportedCommandPacket.class));
    }
    
    @Test
    void assertNewInstanceWithComBinlogDumpPacket() {
        assertThat(MySQLCommandPacketFactory.newInstance(MySQLCommandPacketType.COM_BINLOG_DUMP, payload, connectionSession), isA(MySQLUnsupportedCommandPacket.class));
    }
    
    @Test
    void assertNewInstanceWithComTableDumpPacket() {
        assertThat(MySQLCommandPacketFactory.newInstance(MySQLCommandPacketType.COM_TABLE_DUMP, payload, connectionSession), isA(MySQLUnsupportedCommandPacket.class));
    }
    
    @Test
    void assertNewInstanceWithComConnectOutPacket() {
        assertThat(MySQLCommandPacketFactory.newInstance(MySQLCommandPacketType.COM_CONNECT_OUT, payload, connectionSession), isA(MySQLUnsupportedCommandPacket.class));
    }
    
    @Test
    void assertNewInstanceWithComRegisterSlavePacket() {
        assertThat(MySQLCommandPacketFactory.newInstance(MySQLCommandPacketType.COM_REGISTER_SLAVE, payload, connectionSession), isA(MySQLUnsupportedCommandPacket.class));
    }
    
    @Test
    void assertNewInstanceWithComStmtSendLongDataPacket() {
        assertThat(MySQLCommandPacketFactory.newInstance(MySQLCommandPacketType.COM_STMT_SEND_LONG_DATA, payload, connectionSession), isA(MySQLComStmtSendLongDataPacket.class));
    }
    
    @Test
    void assertNewInstanceWithComStmtResetPacket() {
        assertThat(MySQLCommandPacketFactory.newInstance(MySQLCommandPacketType.COM_STMT_RESET, payload, connectionSession), isA(MySQLComStmtResetPacket.class));
    }
    
    @Test
    void assertNewInstanceWithComSetOptionPacket() {
        assertThat(MySQLCommandPacketFactory.newInstance(MySQLCommandPacketType.COM_SET_OPTION, payload, connectionSession), isA(MySQLComSetOptionPacket.class));
    }
    
    @Test
    void assertNewInstanceWithComStmtFetchPacket() {
        assertThat(MySQLCommandPacketFactory.newInstance(MySQLCommandPacketType.COM_STMT_FETCH, payload, connectionSession), isA(MySQLUnsupportedCommandPacket.class));
    }
    
    @Test
    void assertNewInstanceWithComDaemonPacket() {
        assertThat(MySQLCommandPacketFactory.newInstance(MySQLCommandPacketType.COM_DAEMON, payload, connectionSession), isA(MySQLUnsupportedCommandPacket.class));
    }
    
    @Test
    void assertNewInstanceWithComBinlogDumpGTIDPacket() {
        assertThat(MySQLCommandPacketFactory.newInstance(MySQLCommandPacketType.COM_BINLOG_DUMP_GTID, payload, connectionSession), isA(MySQLUnsupportedCommandPacket.class));
    }
}
