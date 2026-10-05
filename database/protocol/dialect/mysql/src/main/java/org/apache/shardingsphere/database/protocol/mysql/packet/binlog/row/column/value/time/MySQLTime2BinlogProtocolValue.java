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
import java.util.Locale;

/**
 * TIME2 type value of MySQL binlog protocol.
 * The 3-byte value is the packed time offset by 0x800000, so it is signed: MySQL TIME ranges from -838:59:59 to 838:59:59.
 * Stored as 3-byte value The number of decimals for the fractional part is stored in the table metadata as a one byte value.
 * The number of bytes that follow the 3 byte time value can be calculated with the following formula: (decimals + 1) / 2
 * A negative value with a non-zero fraction is stored with its whole seconds one lower and its fraction counted up from there, so the two parts are only meaningful together.
 *
 * <p>
 * TIME2 type applied after MySQL 5.6.4.
 * </p>
 *
 * @see <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/field__types_8h.html">field type</a>
 * @see <a href="https://github.com/mysql/mysql-server/blob/mysql-8.0.46/mysys/my_time.cc">my_time_packed_from_binary</a>
 */
public final class MySQLTime2BinlogProtocolValue implements MySQLBinlogProtocolValue {
    
    private static final int INT_OFFSET = 0x800000;
    
    @Override
    public Serializable read(final MySQLBinlogColumnDef columnDef, final MySQLPacketPayload payload) {
        long packedTime = readPackedTime(columnDef.getColumnMeta(), payload);
        if (0L == packedTime) {
            return MySQLTimeValueUtils.ZERO_OF_TIME;
        }
        long magnitude = Math.abs(packedTime);
        int wholeSeconds = (int) (magnitude >> 24);
        int hour = (wholeSeconds >> 12) % (1 << 10);
        int minute = (wholeSeconds >> 6) % (1 << 6);
        int second = wholeSeconds % (1 << 6);
        int micros = (int) (magnitude % (1 << 24));
        return packedTime > 0L && hour < 24
                ? LocalTime.of(hour, minute, second, micros * 1000)
                : formatOutOfLocalTimeRange(packedTime < 0L, hour, minute, second, micros);
    }
    
    /**
     * Read the value as MySQL's signed packed time, the whole seconds shifted left by 24 bits plus the microseconds, the same way my_time_packed_from_binary does.
     * The fraction is read as stored rather than through {@link MySQLFractionalSeconds}, because for a negative value it can be any number the bytes hold.
     *
     * @param fractionalSecondsPrecision fractional seconds precision of the column
     * @param payload payload
     * @return signed packed time
     */
    private long readPackedTime(final int fractionalSecondsPrecision, final MySQLPacketPayload payload) {
        long wholeSeconds = payload.getByteBuf().readUnsignedMedium() - INT_OFFSET;
        switch (fractionalSecondsPrecision) {
            case 1:
            case 2:
                return toPackedTime(wholeSeconds, payload.readInt1(), 0x100, 10000);
            case 3:
            case 4:
                return toPackedTime(wholeSeconds, payload.getByteBuf().readUnsignedShort(), 0x10000, 100);
            case 5:
            case 6:
                return toPackedTime(wholeSeconds, payload.getByteBuf().readUnsignedMedium(), 0x1000000, 1);
            default:
                return wholeSeconds << 24;
        }
    }
    
    /**
     * Combine the stored whole seconds and fraction into a signed packed time.
     * A negative value with a non-zero fraction is stored with its whole seconds one lower and its fraction as fractionCarry minus the real fraction, so both are brought back first.
     *
     * @param wholeSeconds stored whole seconds, already signed
     * @param fraction stored fraction
     * @param fractionCarry value at which the stored fraction carries into the whole seconds
     * @param microsPerFractionUnit microseconds in one unit of the stored fraction
     * @return signed packed time
     */
    private long toPackedTime(final long wholeSeconds, final int fraction, final int fractionCarry, final int microsPerFractionUnit) {
        return wholeSeconds < 0L && 0 != fraction
                ? ((wholeSeconds + 1L) << 24) + (long) (fraction - fractionCarry) * microsPerFractionUnit
                : (wholeSeconds << 24) + (long) fraction * microsPerFractionUnit;
    }
    
    /**
     * Formats a value that {@link LocalTime} cannot hold, which is any negative time and any time from 24:00:00 up to the MySQL maximum of 838:59:59.
     * The text form matches what {@link MySQLTimeBinlogProtocolValue} returns for every value and what {@link MySQLTimeValueUtils#ZERO_OF_TIME} returns from this same method.
     * Digits are always ASCII, whatever the default locale.
     *
     * @param negative whether the time is negative
     * @param hour hour
     * @param minute minute
     * @param second second
     * @param micros microseconds
     * @return time in MySQL text form
     */
    private String formatOutOfLocalTimeRange(final boolean negative, final int hour, final int minute, final int second, final int micros) {
        String result = String.format(Locale.ROOT, "%s%02d:%02d:%02d", negative ? "-" : "", hour, minute, second);
        return 0 == micros ? result : result + String.format(Locale.ROOT, ".%06d", micros);
    }
}
