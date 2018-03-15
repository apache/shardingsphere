/*
 * Copyright 1999-2015 dangdang.com.
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

package io.shardingjdbc.proxy.packet.command;

/**
 * Command packet factory.
 *
 * @author zhangliang
 */
public final class CommandPacketFactory {
    
    /**
     * Get command Packet.
     * 
     * @param commandPacketTypeValue command packet type value
     * @return Command packet
     */
    public static CommandPacket getCommandPacket(final int commandPacketTypeValue) {
        CommandPacketType type = CommandPacketType.valueOf(commandPacketTypeValue);
        switch (type) {
            case COM_QUIT:
                return new ComQuitPacket();
            case COM_INIT_DB:
                return new ComInitDbPacket();
            case COM_FIELD_LIST:
                return new ComFieldListPacket();
            case COM_QUERY:
                return new ComQueryPacket();
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
            case COM_PING:
            case COM_TIME:
            case COM_DELAYED_INSERT:
            case COM_CHANGE_USER:
            case COM_BINLOG_DUMP:
            case COM_TABLE_DUMP:
            case COM_CONNECT_OUT:
            case COM_REGISTER_SLAVE:
            case COM_STMT_PREPARE:
            case COM_STMT_EXECUTE:
            case COM_STMT_SEND_LONG_DATA:
            case COM_STMT_CLOSE:
            case COM_STMT_RESET:
            case COM_SET_OPTION:
            case COM_STMT_FETCH:
            case COM_DAEMON:
            case COM_BINLOG_DUMP_GTID:
            case COM_RESET_CONNECTION:
                return new UnsupportedCommandPacket(type);
            default:
                return new UnsupportedCommandPacket(type);
        }
    }
}
