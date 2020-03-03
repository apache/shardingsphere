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

package org.apache.shardingsphere.shardingscaling.mysql.binlog.packet.binlog;

/**
 * MySQL event types.
 *
 * <p>
 *     https://dev.mysql.com/doc/shorternals/en/binlog-event-type.html
 * </p>
 */
public final class EventTypes {
    
    public static final short UNKNOWN_EVENT = 0;
    
    public static final short START_EVENT_V3 = 1;
    
    public static final short QUERY_EVENT = 2;
    
    public static final short STOP_EVENT = 3;
    
    public static final short ROTATE_EVENT = 4;
    
    public static final short INTVAR_EVENT = 5;
    
    public static final short LOAD_EVENT = 6;
    
    public static final short SLAVE_EVENT = 7;
    
    public static final short CREATE_FILE_EVENT = 8;
    
    public static final short APPEND_BLOCK_EVENT = 9;
    
    public static final short EXEC_LOAD_EVENT = 10;
    
    public static final short DELETE_FILE_EVENT = 11;
    
    public static final short NEW_LOAD_EVENT = 12;
    
    public static final short RAND_EVENT = 13;
    
    public static final short USER_VAR_EVENT = 14;
    
    public static final short FORMAT_DESCRIPTION_EVENT = 15;
    
    public static final short XID_EVENT = 16;
    
    public static final short BEGIN_LOAD_QUERY_EVENT = 17;
    
    public static final short EXECUTE_LOAD_QUERY_EVENT = 18;
    
    public static final short TABLE_MAP_EVENT = 19;
    
    public static final short WRITE_ROWS_EVENT_V0 = 20;
    
    public static final short UPDATE_ROWS_EVENT_V0 = 21;
    
    public static final short DELETE_ROWS_EVENT_V0 = 22;
    
    public static final short WRITE_ROWS_EVENT_V1 = 23;
    
    public static final short UPDATE_ROWS_EVENT_V1 = 24;
    
    public static final short DELETE_ROWS_EVENT_V1 = 25;
    
    public static final short INCIDENT_EVENT = 26;
    
    public static final short HEARTBEAT_EVENT = 27;
    
    public static final short IGNORABLE_EVENT = 28;
    
    public static final short ROWS_QUERY_EVENT = 29;
    
    public static final short WRITE_ROWS_EVENT_V2 = 30;
    
    public static final short UPDATE_ROWS_EVENT_V2 = 31;
    
    public static final short DELETE_ROWS_EVENT_V2 = 32;
    
    public static final short GTID_EVENT = 33;
    
    public static final short ANONYMOUS_GTID_EVENT = 34;
    
    public static final short PREVIOUS_GTIDS_EVENT = 35;
}
