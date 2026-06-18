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

package org.apache.shardingsphere.database.protocol.mysql.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * MySQL binlog event type.
 *
 * @see <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/group__group__cs__binglog__event__header__flags.html">Binlog Event Flag</a>
 * @see <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/binlog__event_8h_source.html">binlog_event.h</a>
 */
@RequiredArgsConstructor
@Getter
public enum MySQLBinlogEventType {
    
    UNKNOWN_EVENT(0),
    
    START_EVENT_V3(1),
    
    QUERY_EVENT(2),
    
    STOP_EVENT(3),
    
    ROTATE_EVENT(4),
    
    INTVAR_EVENT(5),
    
    LOAD_EVENT(6),
    
    SLAVE_EVENT(7),
    
    CREATE_FILE_EVENT(8),
    
    APPEND_BLOCK_EVENT(9),
    
    EXEC_LOAD_EVENT(10),
    
    DELETE_FILE_EVENT(11),
    
    NEW_LOAD_EVENT(12),
    
    RAND_EVENT(13),
    
    USER_VAR_EVENT(14),
    
    FORMAT_DESCRIPTION_EVENT(15),
    
    XID_EVENT(16),
    
    BEGIN_LOAD_QUERY_EVENT(17),
    
    EXECUTE_LOAD_QUERY_EVENT(18),
    
    TABLE_MAP_EVENT(19),
    
    WRITE_ROWS_EVENT_V0(20),
    
    UPDATE_ROWS_EVENT_V0(21),
    
    DELETE_ROWS_EVENT_V0(22),
    
    WRITE_ROWS_EVENT_V1(23),
    
    UPDATE_ROWS_EVENT_V1(24),
    
    DELETE_ROWS_EVENT_V1(25),
    
    INCIDENT_EVENT(26),
    
    HEARTBEAT_LOG_EVENT(27),
    
    IGNORABLE_LOG_EVENT(28),
    
    ROWS_QUERY_LOG_EVENT(29),
    
    WRITE_ROWS_EVENT_V2(30),
    
    UPDATE_ROWS_EVENT_V2(31),
    
    DELETE_ROWS_EVENT_V2(32),
    
    GTID_LOG_EVENT(33),
    
    ANONYMOUS_GTID_LOG_EVENT(34),
    
    PREVIOUS_GTIDS_LOG_EVENT(35),
    
    TRANSACTION_CONTEXT_EVENT(36),
    
    VIEW_CHANGE_EVENT(37),
    
    XA_PREPARE_LOG_EVENT(38),
    
    PARTIAL_UPDATE_ROWS_EVENT(39),
    
    TRANSACTION_PAYLOAD_EVENT(40),
    
    HEARTBEAT_LOG_EVENT_V2(41);
    
    private static final Map<Integer, MySQLBinlogEventType> VALUE_AND_EVENT_TYPE_MAP = new HashMap<>(values().length, 1F);
    
    private final int value;
    
    static {
        for (MySQLBinlogEventType each : values()) {
            VALUE_AND_EVENT_TYPE_MAP.put(each.value, each);
        }
    }
    
    /**
     * Get {@code MySQLBinlogEventType} by value.
     *
     * @param value value
     * @return MySQL binlog event type
     */
    public static Optional<MySQLBinlogEventType> valueOf(final int value) {
        return Optional.ofNullable(VALUE_AND_EVENT_TYPE_MAP.get(value));
    }
}
