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
    
    private static final byte[] EVENT_TYPE_HEADER_LENGTHS_BY_BINLOG_VERSION_4 = new byte[35];
    
    private final int value;
    
    static {
        EVENT_TYPE_HEADER_LENGTHS_BY_BINLOG_VERSION_4[START_EVENT_V3.getValue() - 1] = 0x38;
        EVENT_TYPE_HEADER_LENGTHS_BY_BINLOG_VERSION_4[QUERY_EVENT.getValue() - 1] = 0x0d;
        EVENT_TYPE_HEADER_LENGTHS_BY_BINLOG_VERSION_4[STOP_EVENT.getValue() - 1] = 0x00;
        EVENT_TYPE_HEADER_LENGTHS_BY_BINLOG_VERSION_4[ROTATE_EVENT.getValue() - 1] = 0x08;
        EVENT_TYPE_HEADER_LENGTHS_BY_BINLOG_VERSION_4[INTVAR_EVENT.getValue() - 1] = 0x00;
        EVENT_TYPE_HEADER_LENGTHS_BY_BINLOG_VERSION_4[LOAD_EVENT.getValue() - 1] = 0x12;
        EVENT_TYPE_HEADER_LENGTHS_BY_BINLOG_VERSION_4[SLAVE_EVENT.getValue() - 1] = 0x00;
        EVENT_TYPE_HEADER_LENGTHS_BY_BINLOG_VERSION_4[CREATE_FILE_EVENT.getValue() - 1] = 0x04;
        EVENT_TYPE_HEADER_LENGTHS_BY_BINLOG_VERSION_4[APPEND_BLOCK_EVENT.getValue() - 1] = 0x04;
        EVENT_TYPE_HEADER_LENGTHS_BY_BINLOG_VERSION_4[EXEC_LOAD_EVENT.getValue() - 1] = 0x04;
        EVENT_TYPE_HEADER_LENGTHS_BY_BINLOG_VERSION_4[DELETE_FILE_EVENT.getValue() - 1] = 0x04;
        EVENT_TYPE_HEADER_LENGTHS_BY_BINLOG_VERSION_4[NEW_LOAD_EVENT.getValue() - 1] = 0x12;
        EVENT_TYPE_HEADER_LENGTHS_BY_BINLOG_VERSION_4[RAND_EVENT.getValue() - 1] = 0x00;
        EVENT_TYPE_HEADER_LENGTHS_BY_BINLOG_VERSION_4[USER_VAR_EVENT.getValue() - 1] = 0x00;
        EVENT_TYPE_HEADER_LENGTHS_BY_BINLOG_VERSION_4[FORMAT_DESCRIPTION_EVENT.getValue() - 1] = 0x54;
        EVENT_TYPE_HEADER_LENGTHS_BY_BINLOG_VERSION_4[XID_EVENT.getValue() - 1] = 0x00;
        EVENT_TYPE_HEADER_LENGTHS_BY_BINLOG_VERSION_4[BEGIN_LOAD_QUERY_EVENT.getValue() - 1] = 0x04;
        EVENT_TYPE_HEADER_LENGTHS_BY_BINLOG_VERSION_4[EXECUTE_LOAD_QUERY_EVENT.getValue() - 1] = 0x1a;
        EVENT_TYPE_HEADER_LENGTHS_BY_BINLOG_VERSION_4[TABLE_MAP_EVENT.getValue() - 1] = 0x08;
        EVENT_TYPE_HEADER_LENGTHS_BY_BINLOG_VERSION_4[WRITE_ROWS_EVENTv0.getValue() - 1] = 0x00;
        EVENT_TYPE_HEADER_LENGTHS_BY_BINLOG_VERSION_4[UPDATE_ROWS_EVENTv0.getValue() - 1] = 0x00;
        EVENT_TYPE_HEADER_LENGTHS_BY_BINLOG_VERSION_4[DELETE_ROWS_EVENTv0.getValue() - 1] = 0x00;
        EVENT_TYPE_HEADER_LENGTHS_BY_BINLOG_VERSION_4[WRITE_ROWS_EVENTv1.getValue() - 1] = 0x08;
        EVENT_TYPE_HEADER_LENGTHS_BY_BINLOG_VERSION_4[UPDATE_ROWS_EVENTv1.getValue() - 1] = 0x08;
        EVENT_TYPE_HEADER_LENGTHS_BY_BINLOG_VERSION_4[DELETE_ROWS_EVENTv1.getValue() - 1] = 0x08;
        EVENT_TYPE_HEADER_LENGTHS_BY_BINLOG_VERSION_4[INCIDENT_EVENT.getValue() - 1] = 0x02;
        EVENT_TYPE_HEADER_LENGTHS_BY_BINLOG_VERSION_4[HEARTBEAT_EVENT.getValue() - 1] = 0x00;
        EVENT_TYPE_HEADER_LENGTHS_BY_BINLOG_VERSION_4[IGNORABLE_EVENT.getValue() - 1] = 0x00;
        EVENT_TYPE_HEADER_LENGTHS_BY_BINLOG_VERSION_4[ROWS_QUERY_EVENT.getValue() - 1] = 0x00;
        EVENT_TYPE_HEADER_LENGTHS_BY_BINLOG_VERSION_4[WRITE_ROWS_EVENTv2.getValue() - 1] = 0x0a;
        EVENT_TYPE_HEADER_LENGTHS_BY_BINLOG_VERSION_4[UPDATE_ROWS_EVENTv2.getValue() - 1] = 0x0a;
        EVENT_TYPE_HEADER_LENGTHS_BY_BINLOG_VERSION_4[DELETE_ROWS_EVENTv2.getValue() - 1] = 0x0a;
        EVENT_TYPE_HEADER_LENGTHS_BY_BINLOG_VERSION_4[GTID_EVENT.getValue() - 1] = 0x19;
        EVENT_TYPE_HEADER_LENGTHS_BY_BINLOG_VERSION_4[ANONYMOUS_GTID_EVENT.getValue() - 1] = 0x19;
        EVENT_TYPE_HEADER_LENGTHS_BY_BINLOG_VERSION_4[PREVIOUS_GTIDS_EVENT.getValue() - 1] = 0x00;
    }
    
    /**
     * Get event type header lengths by binlog version 4.
     *
     * @return event type header lengths
     */
    public static byte[] getEventTypeHeaderLengthsByBinlogVersion4() {
        return EVENT_TYPE_HEADER_LENGTHS_BY_BINLOG_VERSION_4;
    }
    
    /**
     * Get event type header length.
     *
     * @param mySQLBinlogEventType binlog event type
     * @return event type header length
     */
    public static int getEventTypeHeaderLength(final MySQLBinlogEventType mySQLBinlogEventType) {
        return EVENT_TYPE_HEADER_LENGTHS_BY_BINLOG_VERSION_4[mySQLBinlogEventType.getValue() - 1];
    }
}
