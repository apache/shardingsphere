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
import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 * MySQL DATETIME2 binlog protocol value.
 * Stored as 4-byte value The number of decimals for the fractional part is stored in the table metadata as a one byte value.
 * The number of bytes that follow the 5 byte datetime value can be calculated with the following formula: (decimals + 1) / 2
 *
 * @see <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/field__types_8h.html">field type</a>
 */
public final class MySQLDatetime2BinlogProtocolValue implements MySQLBinlogProtocolValue {
    
    @Override
    public Serializable read(final MySQLBinlogColumnDef columnDef, final MySQLPacketPayload payload) {
        long datetime = readDatetimeV2FromPayload(payload);
        return 0 == datetime ? MySQLTimeValueUtils.DATETIME_OF_ZERO : readDatetime(columnDef, datetime, payload);
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
        if (0 == datetimeWithoutSign) {
            return MySQLTimeValueUtils.DATETIME_OF_ZERO;
        }
        long date = datetimeWithoutSign >> 17;
        long yearAndMonth = date >> 5;
        int year = (int) (yearAndMonth / 13);
        int month = (int) (yearAndMonth % 13);
        int day = (int) (date % (1 << 5));
        long time = datetimeWithoutSign % (1 << 17);
        int hour = (int) (time >> 12);
        int minute = (int) ((time >> 6) % (1 << 6));
        int second = (int) (time % (1 << 6));
        MySQLFractionalSeconds fractionalSeconds = new MySQLFractionalSeconds(columnDef.getColumnMeta(), payload);
        return Timestamp.valueOf(LocalDateTime.of(year, month, day, hour, minute, second, fractionalSeconds.getNanos()));
    }
}
