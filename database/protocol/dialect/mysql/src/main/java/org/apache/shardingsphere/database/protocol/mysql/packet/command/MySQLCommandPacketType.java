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

package org.apache.shardingsphere.database.protocol.mysql.packet.command;

import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.protocol.packet.command.CommandPacketType;

import java.util.HashMap;
import java.util.Map;

/**
 * Command packet type for MySQL.
 */
@RequiredArgsConstructor
@Getter
public enum MySQLCommandPacketType implements CommandPacketType {
    
    /**
     * COM_SLEEP.
     */
    COM_SLEEP(0x00),
    
    /**
     * COM_QUIT.
     *
     * @see <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_com_quit.html">COM_QUIT</a>
     */
    COM_QUIT(0x01),
    
    /**
     * COM_INIT_DB.
     *
     * @see <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_com_init_db.html">COM_INIT_DB</a>
     */
    COM_INIT_DB(0x02),
    
    /**
     * COM_QUERY.
     *
     * @see <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_com_query.html">COM_QUERY</a>
     */
    COM_QUERY(0x03),
    
    /**
     * COM_FIELD_LIST.
     *
     * @see <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_com_field_list.html">COM_FIELD_LIST</a>
     */
    COM_FIELD_LIST(0x04),
    
    /**
     * COM_CREATE_DB.
     */
    COM_CREATE_DB(0x05),
    
    /**
     * COM_DROP_DB.
     */
    COM_DROP_DB(0x06),
    
    /**
     * COM_REFRESH.
     *
     * @see <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_com_refresh.html">COM_REFRESH</a>
     */
    COM_REFRESH(0x07),
    
    /**
     * COM_SHUTDOWN.
     */
    COM_SHUTDOWN(0x08),
    
    /**
     * COM_STATISTICS.
     *
     * @see <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_com_statistics.html">COM_STATISTICS</a>
     */
    COM_STATISTICS(0x09),
    
    /**
     * COM_PROCESS_INFO.
     *
     * @see <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_com_process_info.html">COM_PROCESS_INFO</a>
     */
    COM_PROCESS_INFO(0x0a),
    
    /**
     * COM_CONNECT.
     */
    COM_CONNECT(0x0b),
    
    /**
     * COM_PROCESS_KILL.
     *
     * @see <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_com_process_kill.html">COM_PROCESS_KILL</a>
     */
    COM_PROCESS_KILL(0x0c),
    
    /**
     * COM_DEBUG.
     *
     * @see <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_com_debug.html">COM_DEBUG</a>
     */
    COM_DEBUG(0x0d),
    
    /**
     * COM_PING.
     *
     * @see <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_com_ping.html">COM_PING</a>
     */
    COM_PING(0x0e),
    
    /**
     * COM_TIME.
     */
    COM_TIME(0x0f),
    
    /**
     * COM_DELAYED_INSERT.
     */
    COM_DELAYED_INSERT(0x10),
    
    /**
     * COM_CHANGE_USER.
     *
     * @see <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_com_change_user.html">COM_CHANGE_USER</a>
     */
    COM_CHANGE_USER(0x11),
    
    /**
     * COM_BINLOG_DUMP.
     *
     * @see <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_com_binlog_dump.html">COM_BINLOG_DUMP</a>
     */
    COM_BINLOG_DUMP(0x12),
    
    /**
     * COM_TABLE_DUMP.
     */
    COM_TABLE_DUMP(0x13),
    
    /**
     * COM_CONNECT_OUT.
     */
    COM_CONNECT_OUT(0x14),
    
    /**
     * COM_REGISTER_SLAVE.
     */
    COM_REGISTER_SLAVE(0x15),
    
    /**
     * COM_STMT_PREPARE.
     *
     * @see <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_com_stmt_prepare.html">COM_STMT_PREPARE</a>
     */
    COM_STMT_PREPARE(0x16),
    
    /**
     * COM_STMT_EXECUTE.
     *
     * @see <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_com_stmt_execute.html">COM_STMT_EXECUTE</a>
     */
    COM_STMT_EXECUTE(0x17),
    
    /**
     * COM_STMT_SEND_LONG_DATA.
     *
     * @see <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_com_stmt_send_long_data.html">COM_STMT_SEND_LONG_DATA</a>
     */
    COM_STMT_SEND_LONG_DATA(0x18),
    
    /**
     * COM_STMT_CLOSE.
     *
     * @see <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_com_stmt_close.html">COM_STMT_CLOSE</a>
     */
    COM_STMT_CLOSE(0x19),
    
    /**
     * COM_STMT_RESET.
     *
     * @see <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_com_stmt_reset.html">COM_STMT_RESET</a>
     */
    COM_STMT_RESET(0x1a),
    
    /**
     * COM_SET_OPTION.
     *
     * @see <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_com_set_option.html">COM_SET_OPTION</a>
     */
    COM_SET_OPTION(0x1b),
    
    /**
     * COM_STMT_FETCH.
     *
     * @see <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_com_stmt_fetch.html">COM_STMT_FETCH</a>
     */
    COM_STMT_FETCH(0x1c),
    
    /**
     * COM_DAEMON.
     */
    COM_DAEMON(0x1d),
    
    /**
     * COM_BINLOG_DUMP_GTID.
     */
    COM_BINLOG_DUMP_GTID(0x1e),
    
    /**
     * COM_RESET_CONNECTION.
     *
     * @see <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_com_reset_connection.html">COM_RESET_CONNECTION</a>
     */
    COM_RESET_CONNECTION(0x1f);
    
    private static final Map<Integer, MySQLCommandPacketType> MYSQL_COMMAND_PACKET_TYPE_CACHE = new HashMap<>();
    
    private final int value;
    
    static {
        for (MySQLCommandPacketType each : values()) {
            MYSQL_COMMAND_PACKET_TYPE_CACHE.put(each.value, each);
        }
    }
    
    /**
     * Value of integer.
     *
     * @param value integer value
     * @return command packet type enum
     */
    public static MySQLCommandPacketType valueOf(final int value) {
        MySQLCommandPacketType result = MYSQL_COMMAND_PACKET_TYPE_CACHE.get(value);
        Preconditions.checkNotNull(result, "Cannot find '%s' in command packet type", value);
        return result;
    }
}
