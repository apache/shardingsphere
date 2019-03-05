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

package org.apache.shardingsphere.shardingproxy.frontend.mysql.executor;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.shardingproxy.frontend.api.CommandExecutor;
import org.apache.shardingsphere.shardingproxy.frontend.mysql.executor.admin.initdb.MySQLComInitDbExecutor;
import org.apache.shardingsphere.shardingproxy.frontend.mysql.executor.admin.ping.MySQLComPingExecutor;
import org.apache.shardingsphere.shardingproxy.frontend.mysql.executor.admin.quit.MySQLComQuitExecutor;
import org.apache.shardingsphere.shardingproxy.frontend.mysql.executor.generic.MySQLUnsupportedCommandExecutor;
import org.apache.shardingsphere.shardingproxy.frontend.mysql.executor.query.binary.close.MySQLComStmtCloseExecutor;
import org.apache.shardingsphere.shardingproxy.frontend.mysql.executor.query.binary.execute.MySQLQueryComStmtExecuteExecutor;
import org.apache.shardingsphere.shardingproxy.frontend.mysql.executor.query.binary.prepare.MySQLComStmtPrepareExecutor;
import org.apache.shardingsphere.shardingproxy.frontend.mysql.executor.query.text.fieldlist.MySQLComFieldListPacketExecutor;
import org.apache.shardingsphere.shardingproxy.frontend.mysql.executor.query.text.query.MySQLComQueryPacketExecutor;
import org.apache.shardingsphere.shardingproxy.transport.api.packet.CommandPacket;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.MySQLPacket;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.MySQLCommandPacketType;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.admin.initdb.MySQLComInitDbPacket;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.query.binary.close.MySQLComStmtClosePacket;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.query.binary.execute.MySQLQueryComStmtExecutePacket;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.query.binary.prepare.MySQLComStmtPreparePacket;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.query.text.fieldlist.MySQLComFieldListPacket;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.query.text.query.MySQLComQueryPacket;

/**
 * Command executor factory for MySQL.
 *
 * @author zhangliang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MySQLCommandExecutorFactory {
    
    /**
     * Create new instance of packet executor.
     *
     * @param commandPacketType command packet type for MySQL
     * @param commandPacket command packet for MySQL
     * @param backendConnection backend connection
     * @return command executor
     */
    public static CommandExecutor<MySQLPacket> newInstance(final MySQLCommandPacketType commandPacketType, final CommandPacket commandPacket, final BackendConnection backendConnection) {
        switch (commandPacketType) {
            case COM_QUIT:
                return new MySQLComQuitExecutor();
            case COM_INIT_DB:
                return new MySQLComInitDbExecutor((MySQLComInitDbPacket) commandPacket, backendConnection);
            case COM_FIELD_LIST:
                return new MySQLComFieldListPacketExecutor((MySQLComFieldListPacket) commandPacket, backendConnection);
            case COM_QUERY:
                return new MySQLComQueryPacketExecutor((MySQLComQueryPacket) commandPacket, backendConnection);
            case COM_STMT_PREPARE:
                return new MySQLComStmtPrepareExecutor((MySQLComStmtPreparePacket) commandPacket, backendConnection);
            case COM_STMT_EXECUTE:
                return new MySQLQueryComStmtExecuteExecutor((MySQLQueryComStmtExecutePacket) commandPacket, backendConnection);
            case COM_STMT_CLOSE:
                return new MySQLComStmtCloseExecutor((MySQLComStmtClosePacket) commandPacket);
            case COM_PING:
                return new MySQLComPingExecutor();
            default:
                return new MySQLUnsupportedCommandExecutor(commandPacketType);
        }
    }
}
