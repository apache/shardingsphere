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
import org.apache.shardingsphere.infra.exception.generic.UnknownSQLException;

import java.sql.SQLException;

/**
 * Status flags are a bit-field for MySQL.
 * 
 * @see <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/mysql__com_8h.html#a1d854e841086925be1883e4d7b4e8cad">Server Status Flags</a>
 */
@RequiredArgsConstructor
@Getter
public enum MySQLStatusFlag {
    
    SERVER_STATUS_IN_TRANS(0x0001),
    
    SERVER_STATUS_AUTOCOMMIT(0x0002),
    
    SERVER_MORE_RESULTS_EXISTS(0x0008),
    
    SERVER_STATUS_NO_GOOD_INDEX_USED(0x0010),
    
    SERVER_STATUS_NO_INDEX_USED(0x0020),
    
    SERVER_STATUS_CURSOR_EXISTS(0x0040),
    
    SERVER_STATUS_LAST_ROW_SENT(0x0080),
    
    SERVER_STATUS_DB_DROPPED(0x0100),
    
    SERVER_STATUS_NO_BACKSLASH_ESCAPES(0x0200),
    
    SERVER_STATUS_METADATA_CHANGED(0x0400),
    
    SERVER_QUERY_WAS_SLOW(0x0800),
    
    SERVER_PS_OUT_PARAMS(0x1000),
    
    SERVER_STATUS_IN_TRANS_READONLY(0x2000),
    
    SERVER_SESSION_STATE_CHANGED(0x4000);
    
    private final int value;
    
    /**
     * Value of MySQL status flag.
     *
     * @param value value
     * @return MySQL status flag
     * @throws UnknownSQLException unknown SQL exception
     */
    public static MySQLStatusFlag valueOf(final int value) {
        for (MySQLStatusFlag each : values()) {
            if (each.value == value) {
                return each;
            }
        }
        throw new UnknownSQLException(new SQLException(String.format("Illegal MySQL status flag value %d", value)));
    }
}
