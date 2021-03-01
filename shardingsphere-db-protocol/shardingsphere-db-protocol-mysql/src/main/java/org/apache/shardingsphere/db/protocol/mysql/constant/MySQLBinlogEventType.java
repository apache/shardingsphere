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

package org.apache.shardingsphere.db.protocol.mysql.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * MySQL binlog event type.
 *
 * @see <a href="https://dev.mysql.com/doc/internals/en/binlog-event-type.html">Binlog Event Type</a>
 */
@RequiredArgsConstructor
@Getter
public enum MySQLBinlogEventType {
    
    UNKNOWN_EVENT(0x00),
    
    START_EVENT_V3(0x01),
    
    QUERY_EVENT(0x02),
    
    STOP_EVENT(0x03),
    
    ROTATE_EVENT(0x04),
    
    INTVAR_EVENT(0x05),
    
    LOAD_EVENT(0x06),
    
    SLAVE_EVENT(0x07),
    
    CREATE_FILE_EVENT(0x08),
    
    APPEND_BLOCK_EVENT(0x09),
    
    EXEC_LOAD_EVENT(0x0a),
    
    DELETE_FILE_EVENT(0x0b),
    
    NEW_LOAD_EVENT(0x0c),
    
    RAND_EVENT(0x0d),
    
    USER_VAR_EVENT(0x0e),
    
    FORMAT_DESCRIPTION_EVENT(0x0f),
    
    XID_EVENT(0x10),
    
    BEGIN_LOAD_QUERY_EVENT(0x11),
    
    EXECUTE_LOAD_QUERY_EVENT(0x12),
    
    TABLE_MAP_EVENT(0x13),
    
    WRITE_ROWS_EVENTv0(0x14),
    
    UPDATE_ROWS_EVENTv0(0x15),
    
    DELETE_ROWS_EVENTv0(0x16),
    
    WRITE_ROWS_EVENTv1(0x17),
    
    UPDATE_ROWS_EVENTv1(0x18),
    
    DELETE_ROWS_EVENTv1(0x19),
    
    INCIDENT_EVENT(0x1a),
    
    HEARTBEAT_EVENT(0x1b),
    
    IGNORABLE_EVENT(0x1c),
    
    ROWS_QUERY_EVENT(0x1d),
    
    WRITE_ROWS_EVENTv2(0x1e),
    
    UPDATE_ROWS_EVENTv2(0x1f),
    
    DELETE_ROWS_EVENTv2(0x20),
    
    GTID_EVENT(0x21),
    
    ANONYMOUS_GTID_EVENT(0x22),
    
    PREVIOUS_GTIDS_EVENT(0x23);
    
    private static final Map<Integer, MySQLBinlogEventType> VALUE_AND_EVENT_TYPE_MAP = new HashMap<>(values().length, 1);
    
    private final int value;
    
    static {
        for (MySQLBinlogEventType each : values()) {
            VALUE_AND_EVENT_TYPE_MAP.put(each.value, each);
        }
    }
    
    /**
     * Value of {@code MySQLBinlogEventType}.
     *
     * @param value value
     * @return MySQL binlog event type
     */
    public static MySQLBinlogEventType valueOf(final int value) {
        if (VALUE_AND_EVENT_TYPE_MAP.containsKey(value)) {
            return VALUE_AND_EVENT_TYPE_MAP.get(value);
        }
        throw new IllegalArgumentException(String.format("Cannot find value '%s' in binlog event type", value));
    }
}
