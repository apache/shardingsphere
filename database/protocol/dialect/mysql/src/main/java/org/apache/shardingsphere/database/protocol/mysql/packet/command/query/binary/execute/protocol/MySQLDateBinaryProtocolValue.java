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

package org.apache.shardingsphere.database.protocol.mysql.packet.command.query.binary.execute.protocol;

import org.apache.shardingsphere.database.protocol.mysql.payload.MySQLPacketPayload;

import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * Binary protocol value for date for MySQL.
 */
public final class MySQLDateBinaryProtocolValue implements MySQLBinaryProtocolValue {
    
    @Override
    public Object read(final MySQLPacketPayload payload, final boolean unsigned) throws SQLException {
        int length = payload.readInt1();
        switch (length) {
            case 0:
                throw new SQLFeatureNotSupportedException("Can not support date format if year, month, day is absent.");
            case 4:
                return getTimestampForDate(payload);
            case 7:
                return getTimestampForDatetime(payload);
            case 11:
                Timestamp result = getTimestampForDatetime(payload);
                result.setNanos(payload.readInt4() * 1000);
                return result;
            default:
                throw new SQLFeatureNotSupportedException(String.format("Wrong length `%d` of MYSQL_TYPE_TIME", length));
        }
    }
    
    private Timestamp getTimestampForDate(final MySQLPacketPayload payload) {
        return Timestamp.valueOf(LocalDate.of(payload.readInt2(), payload.readInt1(), payload.readInt1()).atStartOfDay());
    }
    
    private Timestamp getTimestampForDatetime(final MySQLPacketPayload payload) {
        return Timestamp.valueOf(LocalDateTime.of(payload.readInt2(), payload.readInt1(), payload.readInt1(), payload.readInt1(), payload.readInt1(), payload.readInt1()));
    }
    
    @Override
    public void write(final MySQLPacketPayload payload, final Object value) {
        LocalDateTime dateTime = getLocalDateTime(value);
        int year = dateTime.getYear();
        int month = dateTime.getMonthValue();
        int dayOfMonth = dateTime.getDayOfMonth();
        int hours = dateTime.getHour();
        int minutes = dateTime.getMinute();
        int seconds = dateTime.getSecond();
        int nanos = dateTime.getNano();
        boolean isTimeAbsent = 0 == hours && 0 == minutes && 0 == seconds;
        boolean isNanosAbsent = 0 == nanos;
        if (isTimeAbsent && isNanosAbsent) {
            payload.writeInt1(4);
            writeDate(payload, year, month, dayOfMonth);
            return;
        }
        if (isNanosAbsent) {
            payload.writeInt1(7);
            writeDate(payload, year, month, dayOfMonth);
            writeTime(payload, hours, minutes, seconds);
            return;
        }
        payload.writeInt1(11);
        writeDate(payload, year, month, dayOfMonth);
        writeTime(payload, hours, minutes, seconds);
        writeNanos(payload, nanos);
    }
    
    @SuppressWarnings("UseOfObsoleteDateTimeApi")
    private LocalDateTime getLocalDateTime(final Object value) {
        if (value instanceof LocalDate) {
            return ((LocalDate) value).atStartOfDay();
        }
        if (value instanceof LocalDateTime) {
            return (LocalDateTime) value;
        }
        return new Timestamp(((Date) value).getTime()).toLocalDateTime();
    }
    
    private void writeDate(final MySQLPacketPayload payload, final int year, final int month, final int dayOfMonth) {
        payload.writeInt2(year);
        payload.writeInt1(month);
        payload.writeInt1(dayOfMonth);
    }
    
    private void writeTime(final MySQLPacketPayload payload, final int hourOfDay, final int minutes, final int seconds) {
        payload.writeInt1(hourOfDay);
        payload.writeInt1(minutes);
        payload.writeInt1(seconds);
    }
    
    private void writeNanos(final MySQLPacketPayload payload, final int nanos) {
        payload.writeInt4(nanos / 1000);
    }
}
