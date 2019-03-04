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

package org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.shardingproxy.transport.common.packet.CommandPacketExecutor;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.MySQLPacket;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.admin.MySQLOKCommandPacketExecutor;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.admin.MySQLUnsupportedCommandPacketExecutor;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.admin.initdb.MySQLComInitDbPacketExecutor;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.query.binary.close.MySQLComStmtClosePacketExecutor;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.query.binary.execute.MySQLQueryComStmtExecutePacketExecutor;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.query.binary.prepare.MySQLComStmtPreparePacketExecutor;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.query.text.fieldlist.MySQLComFieldListPacketExecutor;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.query.text.query.MySQLComQueryPacketExecutor;

/**
 * Command packet executor factory for MySQL.
 *
 * @author zhangliang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MySQLCommandPacketExecutorFactory {
    
    /**
     * Create new instance of command packet executor.
     *
     * @param commandPacketType command packet type for MySQL
     * @return command packet executor
     */
    public static CommandPacketExecutor<MySQLPacket> newInstance(final MySQLCommandPacketType commandPacketType) {
        switch (commandPacketType) {
            case COM_QUIT:
                return new MySQLOKCommandPacketExecutor();
            case COM_INIT_DB:
                return new MySQLComInitDbPacketExecutor();
            case COM_FIELD_LIST:
                return new MySQLComFieldListPacketExecutor();
            case COM_QUERY:
                return new MySQLComQueryPacketExecutor();
            case COM_STMT_PREPARE:
                return new MySQLComStmtPreparePacketExecutor();
            case COM_STMT_EXECUTE:
                return new MySQLQueryComStmtExecutePacketExecutor();
            case COM_STMT_CLOSE:
                return new MySQLComStmtClosePacketExecutor();
            case COM_PING:
                return new MySQLOKCommandPacketExecutor();
            default:
                return new MySQLUnsupportedCommandPacketExecutor(commandPacketType);
        }
    }
}
