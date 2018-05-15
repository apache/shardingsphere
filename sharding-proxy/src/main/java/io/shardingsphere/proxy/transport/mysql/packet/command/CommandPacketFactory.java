/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.proxy.transport.mysql.packet.command;

import io.shardingsphere.proxy.transport.mysql.packet.MySQLPacketPayload;
import io.shardingsphere.proxy.transport.mysql.packet.command.statement.close.ComStmtClosePacket;
import io.shardingsphere.proxy.transport.mysql.packet.command.statement.execute.ComStmtExecutePacket;
import io.shardingsphere.proxy.transport.mysql.packet.command.statement.prepare.ComStmtPreparePacket;
import io.shardingsphere.proxy.transport.mysql.packet.command.text.fieldlist.ComFieldListPacket;
import io.shardingsphere.proxy.transport.mysql.packet.command.text.initdb.ComInitDbPacket;
import io.shardingsphere.proxy.transport.mysql.packet.command.text.ping.ComPingPacket;
import io.shardingsphere.proxy.transport.mysql.packet.command.text.query.ComQueryPacket;
import io.shardingsphere.proxy.transport.mysql.packet.command.text.quit.ComQuitPacket;

/**
 * Command packet factory.
 *
 * @author zhangliang
 * @author wangkai
 */
public final class CommandPacketFactory {
    
    /**
     * Get command Packet.
     *
     * @param sequenceId         sequence ID
     * @param connectionId       MYSQL connection id
     * @param mysqlPacketPayload MySQL packet payload
     * @return Command packet
     */
    public static CommandPacket getCommandPacket(final int sequenceId, final int connectionId, final MySQLPacketPayload mysqlPacketPayload) {
        int commandPacketTypeValue = mysqlPacketPayload.readInt1();
        CommandPacketType type = CommandPacketType.valueOf(commandPacketTypeValue);
        switch (type) {
            case COM_QUIT:
                return new ComQuitPacket(sequenceId);
            case COM_INIT_DB:
                return new ComInitDbPacket(sequenceId, mysqlPacketPayload);
            case COM_FIELD_LIST:
                return new ComFieldListPacket(sequenceId, connectionId, mysqlPacketPayload);
            case COM_QUERY:
                return new ComQueryPacket(sequenceId, connectionId, mysqlPacketPayload);
            case COM_STMT_PREPARE:
                return new ComStmtPreparePacket(sequenceId, mysqlPacketPayload);
            case COM_STMT_EXECUTE:
                return new ComStmtExecutePacket(sequenceId, mysqlPacketPayload);
            case COM_STMT_CLOSE:
                return new ComStmtClosePacket(sequenceId, mysqlPacketPayload);
            case COM_PING:
                return new ComPingPacket(sequenceId);
            case COM_SLEEP:
            case COM_CREATE_DB:
            case COM_DROP_DB:
            case COM_REFRESH:
            case COM_SHUTDOWN:
            case COM_STATISTICS:
            case COM_PROCESS_INFO:
            case COM_CONNECT:
            case COM_PROCESS_KILL:
            case COM_DEBUG:
            case COM_TIME:
            case COM_DELAYED_INSERT:
            case COM_CHANGE_USER:
            case COM_BINLOG_DUMP:
            case COM_TABLE_DUMP:
            case COM_CONNECT_OUT:
            case COM_REGISTER_SLAVE:
            case COM_STMT_SEND_LONG_DATA:
            case COM_STMT_RESET:
            case COM_SET_OPTION:
            case COM_STMT_FETCH:
            case COM_DAEMON:
            case COM_BINLOG_DUMP_GTID:
            case COM_RESET_CONNECTION:
                return new UnsupportedCommandPacket(sequenceId, type);
            default:
                return new UnsupportedCommandPacket(sequenceId, type);
        }
    }
}
