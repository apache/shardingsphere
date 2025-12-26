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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import org.apache.shardingsphere.database.protocol.packet.sql.SQLReceivedPacket;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.command.executor.CommandExecutor;
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

import java.sql.SQLException;

/**
 * Command executor factory for MySQL.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class MySQLCommandExecutorFactory {
    
    /**
     * Create new instance of packet executor.
     *
     * @param commandPacketType command packet type for MySQL
     * @param commandPacket command packet for MySQL
     * @param connectionSession connection session
     * @return created instance
     * @throws SQLException SQL exception
     */
    @SuppressWarnings("DataFlowIssue")
    public static CommandExecutor newInstance(
                                              final MySQLCommandPacketType commandPacketType,
                                              final CommandPacket commandPacket,
                                              final ConnectionSession connectionSession) throws SQLException {
        
        if (commandPacket instanceof SQLReceivedPacket) {
            log.debug("Execute packet type: {}, sql: {}",
                    commandPacketType, ((SQLReceivedPacket) commandPacket).getSQL());
        } else {
            log.debug("Execute packet type: {}", commandPacketType);
        }
        
        switch (commandPacketType) {
            case COM_QUIT:
                return new MySQLComQuitExecutor();
            case COM_INIT_DB:
                return new MySQLComInitDbExecutor((MySQLComInitDbPacket) commandPacket, connectionSession);
            case COM_FIELD_LIST:
                return new MySQLComFieldListPacketExecutor((MySQLComFieldListPacket) commandPacket, connectionSession);
            case COM_QUERY:
                return new MySQLComQueryPacketExecutor((MySQLComQueryPacket) commandPacket, connectionSession);
            case COM_PING:
                return new MySQLComPingExecutor(connectionSession);
            case COM_STMT_PREPARE:
                return new MySQLComStmtPrepareExecutor((MySQLComStmtPreparePacket) commandPacket, connectionSession);
            case COM_STMT_EXECUTE:
                return new MySQLComStmtExecuteExecutor((MySQLComStmtExecutePacket) commandPacket, connectionSession);
            case COM_STMT_SEND_LONG_DATA:
                return new MySQLComStmtSendLongDataExecutor((MySQLComStmtSendLongDataPacket) commandPacket, connectionSession);
            case COM_STMT_RESET:
                return new MySQLComStmtResetExecutor((MySQLComStmtResetPacket) commandPacket, connectionSession);
            case COM_STMT_CLOSE:
                return new MySQLComStmtCloseExecutor((MySQLComStmtClosePacket) commandPacket, connectionSession);
            case COM_SET_OPTION:
                return new MySQLComSetOptionExecutor((MySQLComSetOptionPacket) commandPacket, connectionSession);
            case COM_RESET_CONNECTION:
                return new MySQLComResetConnectionExecutor(connectionSession);
            default:
                return new MySQLUnsupportedCommandExecutor(commandPacketType);
        }
    }
}
