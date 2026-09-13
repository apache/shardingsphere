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

package org.apache.shardingsphere.database.protocol.mysql.packet.binlog.row.column.value.time;

import org.apache.shardingsphere.database.protocol.mysql.packet.binlog.row.column.MySQLBinlogColumnDef;
import org.apache.shardingsphere.database.protocol.mysql.packet.binlog.row.column.value.MySQLBinlogProtocolValue;
import org.apache.shardingsphere.database.protocol.mysql.payload.MySQLPacketPayload;

import java.io.Serializable;
import java.time.LocalTime;

/**
 * TIME2 type value of MySQL binlog protocol.
 * The 3-byte value is the packed time offset by 0x800000, so it is signed: MySQL TIME ranges from -838:59:59 to 838:59:59.
 * Stored as 3-byte value The number of decimals for the fractional part is stored in the table metadata as a one byte value.
 * The number of bytes that follow the 3 byte time value can be calculated with the following formula: (decimals + 1) / 2
 *
 * <p>
 * TIME2 type applied after MySQL 5.6.4.
 * </p>
 *
 * @see <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/field__types_8h.html">field type</a>
 */
public final class MySQLTime2BinlogProtocolValue implements MySQLBinlogProtocolValue {
    
    private static final int INT_OFFSET = 0x800000;
    
    @Override
    public Serializable read(final MySQLBinlogColumnDef columnDef, final MySQLPacketPayload payload) {
        int packedTime = payload.getByteBuf().readUnsignedMedium() - INT_OFFSET;
        MySQLFractionalSeconds fractionalSeconds = new MySQLFractionalSeconds(columnDef.getColumnMeta(), payload);
        if (0 == packedTime) {
            return MySQLTimeValueUtils.ZERO_OF_TIME;
        }
        int magnitude = Math.abs(packedTime);
        int hour = (magnitude >> 12) % (1 << 10);
        int minute = (magnitude >> 6) % (1 << 6);
        int second = magnitude % (1 << 6);
        return packedTime > 0 && hour < 24
                ? LocalTime.of(hour, minute, second).withNano(fractionalSeconds.getNanos())
                : formatOutOfLocalTimeRange(packedTime < 0, hour, minute, second, fractionalSeconds.getNanos());
    }
    
    /**
     * Formats a value that {@link LocalTime} cannot hold, which is any negative time and any time from 24:00:00 up to the MySQL maximum of 838:59:59.
     * The text form matches what {@link MySQLTimeBinlogProtocolValue} returns for every value and what {@link MySQLTimeValueUtils#ZERO_OF_TIME} returns from this same method.
     *
     * @param negative whether the time is negative
     * @param hour hour
     * @param minute minute
     * @param second second
     * @param nanos nanoseconds, always a whole number of microseconds
     * @return time in MySQL text form
     */
    private String formatOutOfLocalTimeRange(final boolean negative, final int hour, final int minute, final int second, final int nanos) {
        String result = String.format("%s%02d:%02d:%02d", negative ? "-" : "", hour, minute, second);
        return 0 == nanos ? result : result + String.format(".%06d", nanos / 1000);
    }
}
