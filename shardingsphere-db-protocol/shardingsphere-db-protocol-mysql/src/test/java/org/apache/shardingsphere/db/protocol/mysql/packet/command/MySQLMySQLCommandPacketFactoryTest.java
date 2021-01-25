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

package org.apache.shardingsphere.db.protocol.mysql.packet.command;

import org.apache.shardingsphere.db.protocol.mysql.constant.MySQLNewParametersBoundFlag;
import org.apache.shardingsphere.db.protocol.mysql.packet.command.admin.MySQLUnsupportedCommandPacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.command.admin.initdb.MySQLComInitDbPacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.command.admin.ping.MySQLComPingPacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.command.admin.quit.MySQLComQuitPacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.command.query.binary.MySQLBinaryStatementRegistry;
import org.apache.shardingsphere.db.protocol.mysql.packet.command.query.binary.close.MySQLComStmtClosePacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.command.query.binary.execute.MySQLComStmtExecutePacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.command.query.binary.prepare.MySQLComStmtPreparePacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.command.query.binary.reset.MySQLComStmtResetPacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.command.query.text.fieldlist.MySQLComFieldListPacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.command.query.text.query.MySQLComQueryPacket;
import org.apache.shardingsphere.db.protocol.mysql.payload.MySQLPacketPayload;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class MySQLMySQLCommandPacketFactoryTest {
    
    @Mock
    private MySQLPacketPayload payload;
    
    @Test
    public void assertNewInstanceWithComQuitPacket() throws SQLException {
        assertThat(MySQLCommandPacketFactory.newInstance(MySQLCommandPacketType.COM_QUIT, payload), instanceOf(MySQLComQuitPacket.class));
    }
    
    @Test
    public void assertNewInstanceWithComInitDbPacket() throws SQLException {
        assertThat(MySQLCommandPacketFactory.newInstance(MySQLCommandPacketType.COM_INIT_DB, payload), instanceOf(MySQLComInitDbPacket.class));
    }
    
    @Test
    public void assertNewInstanceWithComFieldListPacket() throws SQLException {
        assertThat(MySQLCommandPacketFactory.newInstance(MySQLCommandPacketType.COM_FIELD_LIST, payload), instanceOf(MySQLComFieldListPacket.class));
    }
    
    @Test
    public void assertNewInstanceWithComQueryPacket() throws SQLException {
        when(payload.readStringEOF()).thenReturn("SHOW TABLES");
        assertThat(MySQLCommandPacketFactory.newInstance(MySQLCommandPacketType.COM_QUERY, payload), instanceOf(MySQLComQueryPacket.class));
    }
    
    @Test
    public void assertNewInstanceWithComStmtPreparePacket() throws SQLException {
        assertThat(MySQLCommandPacketFactory.newInstance(MySQLCommandPacketType.COM_STMT_PREPARE, payload), instanceOf(MySQLComStmtPreparePacket.class));
    }
    
    @Test
    public void assertNewInstanceWithComStmtExecutePacket() throws SQLException {
        when(payload.readInt1()).thenReturn(MySQLNewParametersBoundFlag.PARAMETER_TYPE_EXIST.getValue());
        when(payload.readInt4()).thenReturn(1);
        MySQLBinaryStatementRegistry.getInstance().register("SELECT * FROM t_order", 1);
        assertThat(MySQLCommandPacketFactory.newInstance(MySQLCommandPacketType.COM_STMT_EXECUTE, payload), instanceOf(MySQLComStmtExecutePacket.class));
    }
    
    @Test
    public void assertNewInstanceWithComStmtClosePacket() throws SQLException {
        assertThat(MySQLCommandPacketFactory.newInstance(MySQLCommandPacketType.COM_STMT_CLOSE, payload), instanceOf(MySQLComStmtClosePacket.class));
    }
    
    @Test
    public void assertNewInstanceWithComPingPacket() throws SQLException {
        assertThat(MySQLCommandPacketFactory.newInstance(MySQLCommandPacketType.COM_PING, payload), instanceOf(MySQLComPingPacket.class));
    }
    
    @Test
    public void assertNewInstanceWithComSleepPacket() throws SQLException {
        assertThat(MySQLCommandPacketFactory.newInstance(MySQLCommandPacketType.COM_SLEEP, payload), instanceOf(MySQLUnsupportedCommandPacket.class));
    }
    
    @Test
    public void assertNewInstanceWithComCreateDbPacket() throws SQLException {
        assertThat(MySQLCommandPacketFactory.newInstance(MySQLCommandPacketType.COM_CREATE_DB, payload), instanceOf(MySQLUnsupportedCommandPacket.class));
    }
    
    @Test
    public void assertNewInstanceWithComDropDbPacket() throws SQLException {
        assertThat(MySQLCommandPacketFactory.newInstance(MySQLCommandPacketType.COM_DROP_DB, payload), instanceOf(MySQLUnsupportedCommandPacket.class));
    }
    
    @Test
    public void assertNewInstanceWithComRefreshPacket() throws SQLException {
        assertThat(MySQLCommandPacketFactory.newInstance(MySQLCommandPacketType.COM_REFRESH, payload), instanceOf(MySQLUnsupportedCommandPacket.class));
    }
    
    @Test
    public void assertNewInstanceWithComShutDownPacket() throws SQLException {
        assertThat(MySQLCommandPacketFactory.newInstance(MySQLCommandPacketType.COM_SHUTDOWN, payload), instanceOf(MySQLUnsupportedCommandPacket.class));
    }
    
    @Test
    public void assertNewInstanceWithComStatisticsPacket() throws SQLException {
        assertThat(MySQLCommandPacketFactory.newInstance(MySQLCommandPacketType.COM_STATISTICS, payload), instanceOf(MySQLUnsupportedCommandPacket.class));
    }
    
    @Test
    public void assertNewInstanceWithComProcessInfoPacket() throws SQLException {
        assertThat(MySQLCommandPacketFactory.newInstance(MySQLCommandPacketType.COM_PROCESS_INFO, payload), instanceOf(MySQLUnsupportedCommandPacket.class));
    }
    
    @Test
    public void assertNewInstanceWithComConnectPacket() throws SQLException {
        assertThat(MySQLCommandPacketFactory.newInstance(MySQLCommandPacketType.COM_CONNECT, payload), instanceOf(MySQLUnsupportedCommandPacket.class));
    }
    
    @Test
    public void assertNewInstanceWithComProcessKillPacket() throws SQLException {
        assertThat(MySQLCommandPacketFactory.newInstance(MySQLCommandPacketType.COM_PROCESS_KILL, payload), instanceOf(MySQLUnsupportedCommandPacket.class));
    }
    
    @Test
    public void assertNewInstanceWithComDebugPacket() throws SQLException {
        assertThat(MySQLCommandPacketFactory.newInstance(MySQLCommandPacketType.COM_DEBUG, payload), instanceOf(MySQLUnsupportedCommandPacket.class));
    }
    
    @Test
    public void assertNewInstanceWithComTimePacket() throws SQLException {
        assertThat(MySQLCommandPacketFactory.newInstance(MySQLCommandPacketType.COM_TIME, payload), instanceOf(MySQLUnsupportedCommandPacket.class));
    }
    
    @Test
    public void assertNewInstanceWithComDelayedInsertPacket() throws SQLException {
        assertThat(MySQLCommandPacketFactory.newInstance(MySQLCommandPacketType.COM_DELAYED_INSERT, payload), instanceOf(MySQLUnsupportedCommandPacket.class));
    }
    
    @Test
    public void assertNewInstanceWithComChangeUserPacket() throws SQLException {
        assertThat(MySQLCommandPacketFactory.newInstance(MySQLCommandPacketType.COM_CHANGE_USER, payload), instanceOf(MySQLUnsupportedCommandPacket.class));
    }
    
    @Test
    public void assertNewInstanceWithComBinlogDumpPacket() throws SQLException {
        assertThat(MySQLCommandPacketFactory.newInstance(MySQLCommandPacketType.COM_BINLOG_DUMP, payload), instanceOf(MySQLUnsupportedCommandPacket.class));
    }
    
    @Test
    public void assertNewInstanceWithComTableDumpPacket() throws SQLException {
        assertThat(MySQLCommandPacketFactory.newInstance(MySQLCommandPacketType.COM_TABLE_DUMP, payload), instanceOf(MySQLUnsupportedCommandPacket.class));
    }
    
    @Test
    public void assertNewInstanceWithComConnectOutPacket() throws SQLException {
        assertThat(MySQLCommandPacketFactory.newInstance(MySQLCommandPacketType.COM_CONNECT_OUT, payload), instanceOf(MySQLUnsupportedCommandPacket.class));
    }
    
    @Test
    public void assertNewInstanceWithComRegisterSlavePacket() throws SQLException {
        assertThat(MySQLCommandPacketFactory.newInstance(MySQLCommandPacketType.COM_REGISTER_SLAVE, payload), instanceOf(MySQLUnsupportedCommandPacket.class));
    }
    
    @Test
    public void assertNewInstanceWithComStmtSendLongDataPacket() throws SQLException {
        assertThat(MySQLCommandPacketFactory.newInstance(MySQLCommandPacketType.COM_STMT_SEND_LONG_DATA, payload), instanceOf(MySQLUnsupportedCommandPacket.class));
    }
    
    @Test
    public void assertNewInstanceWithComStmtResetPacket() throws SQLException {
        assertThat(MySQLCommandPacketFactory.newInstance(MySQLCommandPacketType.COM_STMT_RESET, payload), instanceOf(MySQLComStmtResetPacket.class));
    }
    
    @Test
    public void assertNewInstanceWithComSetOptionPacket() throws SQLException {
        assertThat(MySQLCommandPacketFactory.newInstance(MySQLCommandPacketType.COM_SET_OPTION, payload), instanceOf(MySQLUnsupportedCommandPacket.class));
    }
    
    @Test
    public void assertNewInstanceWithComStmtFetchPacket() throws SQLException {
        assertThat(MySQLCommandPacketFactory.newInstance(MySQLCommandPacketType.COM_STMT_FETCH, payload), instanceOf(MySQLUnsupportedCommandPacket.class));
    }
    
    @Test
    public void assertNewInstanceWithComDaemonPacket() throws SQLException {
        assertThat(MySQLCommandPacketFactory.newInstance(MySQLCommandPacketType.COM_DAEMON, payload), instanceOf(MySQLUnsupportedCommandPacket.class));
    }
    
    @Test
    public void assertNewInstanceWithComBinlogDumpGTIDPacket() throws SQLException {
        assertThat(MySQLCommandPacketFactory.newInstance(MySQLCommandPacketType.COM_BINLOG_DUMP_GTID, payload), instanceOf(MySQLUnsupportedCommandPacket.class));
    }
    
    @Test
    public void assertNewInstanceWithComResetConnectionPacket() throws SQLException {
        assertThat(MySQLCommandPacketFactory.newInstance(MySQLCommandPacketType.COM_RESET_CONNECTION, payload), instanceOf(MySQLUnsupportedCommandPacket.class));
    }
}
