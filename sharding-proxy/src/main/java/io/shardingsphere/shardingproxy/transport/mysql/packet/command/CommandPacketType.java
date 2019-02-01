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

package io.shardingsphere.shardingproxy.transport.mysql.packet.command;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Command packet type.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
@Getter
public enum CommandPacketType {
    
    /**
     * COM_SLEEP.
     * 
     * @see <a href="https://dev.mysql.com/doc/internals/en/com-sleep.html#packet-COM_SLEEP">COM_SLEEP</a>
     */
    COM_SLEEP(0x00),
    
    /**
     * COM_QUIT.
     * 
     * @see <a href="https://dev.mysql.com/doc/internals/en/com-sleep.html#packet-COM_QUIT">COM_QUIT</a>
     */
    COM_QUIT(0x01),
    
    /**
     * COM_INIT_DB.
     * 
     * @see <a href="https://dev.mysql.com/doc/internals/en/com-sleep.html#packet-COM_INIT_DB">COM_INIT_DB</a>
     */
    COM_INIT_DB(0x02),
    
    /**
     * COM_QUERY.
     * 
     * @see <a href="https://dev.mysql.com/doc/internals/en/com-sleep.html#packet-COM_QUERY">COM_QUERY</a>
     */
    COM_QUERY(0x03),
    
    /**
     * COM_FIELD_LIST.
     * 
     * @see <a href="https://dev.mysql.com/doc/internals/en/com-sleep.html#packet-COM_FIELD_LIST">COM_FIELD_LIST</a>
     */
    COM_FIELD_LIST(0x04),
    
    /**
     * COM_CREATE_DB.
     * 
     * @see <a href="https://dev.mysql.com/doc/internals/en/com-sleep.html#packet-COM_CREATE_DB">COM_CREATE_DB</a>
     */
    COM_CREATE_DB(0x05),
    
    /**
     * COM_DROP_DB.
     * 
     * @see <a href="https://dev.mysql.com/doc/internals/en/com-sleep.html#packet-COM_DROP_DB">COM_DROP_DB</a>
     */
    COM_DROP_DB(0x06),
    
    /**
     * COM_REFRESH.
     * 
     * @see <a href="https://dev.mysql.com/doc/internals/en/com-sleep.html#packet-COM_REFRESH">COM_REFRESH</a>
     */
    COM_REFRESH(0x07),
    
    /**
     * COM_SHUTDOWN.
     * 
     * @see <a href="https://dev.mysql.com/doc/internals/en/com-sleep.html#packet-COM_SHUTDOWN">COM_SHUTDOWN</a>
     */
    COM_SHUTDOWN(0x08),
    
    /**
     * COM_STATISTICS.
     * 
     * @see <a href="https://dev.mysql.com/doc/internals/en/com-sleep.html#packet-COM_STATISTICS">COM_STATISTICS</a>
     */
    COM_STATISTICS(0x09),
    
    /**
     * COM_PROCESS_INFO.
     * 
     * @see <a href="https://dev.mysql.com/doc/internals/en/com-sleep.html#packet-COM_PROCESS_INFO">COM_PROCESS_INFO</a>
     */
    COM_PROCESS_INFO(0x0a),
    
    /**
     * COM_CONNECT.
     * 
     * @see <a href="https://dev.mysql.com/doc/internals/en/com-sleep.html#packet-COM_CONNECT">COM_CONNECT</a>
     */
    COM_CONNECT(0x0b),
    
    /**
     * COM_PROCESS_KILL.
     * 
     * @see <a href="https://dev.mysql.com/doc/internals/en/com-sleep.html#packet-COM_PROCESS_KILL">COM_PROCESS_KILL</a>
     */
    COM_PROCESS_KILL(0x0c),
    
    /**
     * COM_DEBUG.
     * 
     * @see <a href="https://dev.mysql.com/doc/internals/en/com-sleep.html#packet-COM_DEBUG">COM_DEBUG</a>
     */
    COM_DEBUG(0x0d),
    
    /**
     * COM_PING.
     * 
     * @see <a href="https://dev.mysql.com/doc/internals/en/com-sleep.html#packet-COM_PING">COM_PING</a>
     */
    COM_PING(0x0e),
    
    /**
     * COM_TIME.
     * 
     * @see <a href="https://dev.mysql.com/doc/internals/en/com-sleep.html#packet-COM_TIME">COM_TIME</a>
     */
    COM_TIME(0x0f),
    
    /**
     * COM_DELAYED_INSERT.
     * 
     * @see <a href="https://dev.mysql.com/doc/internals/en/com-sleep.html#packet-COM_DELAYED_INSERT">COM_DELAYED_INSERT</a>
     */
    COM_DELAYED_INSERT(0x10),
    
    /**
     * COM_CHANGE_USER.
     * 
     * @see <a href="https://dev.mysql.com/doc/internals/en/com-sleep.html#packet-COM_CHANGE_USER">COM_CHANGE_USER</a>
     */
    COM_CHANGE_USER(0x11),
    
    /**
     * COM_BINLOG_DUMP.
     * 
     * @see <a href="https://dev.mysql.com/doc/internals/en/com-sleep.html#packet-COM_BINLOG_DUMP">COM_BINLOG_DUMP</a>
     */
    COM_BINLOG_DUMP(0x12),
    
    /**
     * COM_TABLE_DUMP.
     * 
     * @see <a href="https://dev.mysql.com/doc/internals/en/com-sleep.html#packet-COM_TABLE_DUMP">COM_TABLE_DUMP</a>
     */
    COM_TABLE_DUMP(0x13),
    
    /**
     * COM_CONNECT_OUT.
     * 
     * @see <a href="https://dev.mysql.com/doc/internals/en/com-sleep.html#packet-COM_CONNECT_OUT">COM_CONNECT_OUT</a>
     */
    COM_CONNECT_OUT(0x14),
    
    /**
     * COM_REGISTER_SLAVE.
     * 
     * @see <a href="https://dev.mysql.com/doc/internals/en/com-sleep.html#packet-COM_REGISTER_SLAVE">COM_REGISTER_SLAVE</a>
     */
    COM_REGISTER_SLAVE(0x15),
    
    /**
     * COM_STMT_PREPARE.
     * 
     * @see <a href="https://dev.mysql.com/doc/internals/en/com-sleep.html#packet-COM_STMT_PREPARE">COM_STMT_PREPARE</a>
     */
    COM_STMT_PREPARE(0x16),
    
    /**
     * COM_STMT_EXECUTE.
     * 
     * @see <a href="https://dev.mysql.com/doc/internals/en/com-sleep.html#packet-COM_STMT_EXECUTE">COM_STMT_EXECUTE</a>
     */
    COM_STMT_EXECUTE(0x17),
    
    /**
     * COM_STMT_SEND_LONG_DATA.
     * 
     * @see <a href="https://dev.mysql.com/doc/internals/en/com-sleep.html#packet-COM_STMT_SEND_LONG_DATA">COM_STMT_SEND_LONG_DATA</a>
     */
    COM_STMT_SEND_LONG_DATA(0x18),
    
    /**
     * COM_STMT_CLOSE.
     * 
     * @see <a href="https://dev.mysql.com/doc/internals/en/com-sleep.html#packet-COM_STMT_CLOSE">COM_STMT_CLOSE</a>
     */
    COM_STMT_CLOSE(0x19),
    
    /**
     * COM_STMT_RESET.
     * 
     * @see <a href="https://dev.mysql.com/doc/internals/en/com-sleep.html#packet-COM_STMT_RESET">COM_STMT_RESET</a>
     */
    COM_STMT_RESET(0x1a),
    
    /**
     * COM_SET_OPTION.
     * 
     * @see <a href="https://dev.mysql.com/doc/internals/en/com-sleep.html#packet-COM_SET_OPTION">COM_SET_OPTION</a>
     */
    COM_SET_OPTION(0x1b),
    
    /**
     * COM_STMT_FETCH.
     * 
     * @see <a href="https://dev.mysql.com/doc/internals/en/com-sleep.html#packet-COM_STMT_FETCH">COM_STMT_FETCH</a>
     */
    COM_STMT_FETCH(0x1c),
    
    /**
     * COM_DAEMON.
     * 
     * @see <a href="https://dev.mysql.com/doc/internals/en/com-sleep.html#packet-COM_DAEMON">COM_DAEMON</a>
     */
    COM_DAEMON(0x1d),
    
    /**
     * COM_BINLOG_DUMP_GTID.
     * 
     * @see <a href="https://dev.mysql.com/doc/internals/en/com-sleep.html#packet-COM_BINLOG_DUMP_GTID">COM_BINLOG_DUMP_GTID</a>
     */
    COM_BINLOG_DUMP_GTID(0x1e),
    
    /**
     * COM_RESET_CONNECTION.
     * 
     * @see <a href="https://dev.mysql.com/doc/internals/en/com-sleep.html#packet-COM_RESET_CONNECTION">COM_RESET_CONNECTION</a>
     */
    COM_RESET_CONNECTION(0x1f);
    
    private final int value;
    
    /**
     * Value of integer.
     * 
     * @param value integer value
     * @return command packet type enum
     */
    public static CommandPacketType valueOf(final int value) {
        for (CommandPacketType each : CommandPacketType.values()) {
            if (value == each.value) {
                return each;
            }
        }
        throw new IllegalArgumentException(String.format("Cannot find '%s' in command packet type", value));
    }
}
