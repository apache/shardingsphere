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

package org.apache.shardingsphere.db.protocol.mysql.packet.binlog.row.column.value.time;

import org.apache.shardingsphere.db.protocol.mysql.packet.binlog.row.column.MySQLBinlogColumnDef;
import org.apache.shardingsphere.db.protocol.mysql.packet.binlog.row.column.value.MySQLBinlogProtocolValue;
import org.apache.shardingsphere.db.protocol.mysql.payload.MySQLPacketPayload;

import java.io.Serializable;

/**
 * MySQL DATETIME2 binlog protocol value.
 *
 * @see <a href="https://dev.mysql.com/doc/internals/en/date-and-time-data-type-representation.html">Date and Time Data Type Representation</a>
 */
public final class MySQLDatetime2BinlogProtocolValue implements MySQLBinlogProtocolValue {
    
    @Override
    public Serializable read(final MySQLBinlogColumnDef columnDef, final MySQLPacketPayload payload) {
        long datetime = readDatetimeV2FromPayload(payload);
        return 0 == datetime ? MySQLTimeValueUtil.DATETIME_OF_ZERO : readDatetime(columnDef, datetime, payload);
    }
    
    private long readDatetimeV2FromPayload(final MySQLPacketPayload payload) {
        long result = 0;
        for (int i = 4; i >= 0; i--) {
            result |= (long) payload.readInt1() << (8 * i);
        }
        return result;
    }
    
    private Serializable readDatetime(final MySQLBinlogColumnDef columnDef, final long datetime, final MySQLPacketPayload payload) {
        long datetimeWithoutSign = datetime & (0x8000000000L - 1);
        return String.format("%s %s%s", readDate(datetimeWithoutSign >> 17), readTime(datetimeWithoutSign % (1 << 17)), new MySQLFractionalSeconds(columnDef.getColumnMeta(), payload));
    }
    
    private String readDate(final long date) {
        long yearAndMonth = date >> 5;
        return String.format("%d-%02d-%02d", yearAndMonth / 13, yearAndMonth % 13, date % (1 << 5));
    }
    
    private String readTime(final long time) {
        return String.format("%02d:%02d:%02d", time >> 12, (time >> 6) % (1 << 6), time % (1 << 6));
    }
}
