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

import lombok.RequiredArgsConstructor;
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
    
    private static final long NANOS_PER_SECOND = 1_000_000_000L;
    
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
        LocalDateTime dateTime;
        if (value instanceof LocalDate) {
            dateTime = ((LocalDate) value).atStartOfDay();
        } else {
            dateTime = value instanceof LocalDateTime ? (LocalDateTime) value : new Timestamp(((Date) value).getTime()).toLocalDateTime();
        }
        DateTimeValues values = buildDateTimeValues(dateTime);
        boolean isTimeAbsent = 0 == values.hours && 0 == values.minutes && 0 == values.seconds;
        boolean isNanosAbsent = 0 == values.nanos;
        if (isTimeAbsent && isNanosAbsent) {
            payload.writeInt1(4);
            writeDate(payload, values.year, values.month, values.dayOfMonth);
            return;
        }
        if (isNanosAbsent) {
            payload.writeInt1(7);
            writeDate(payload, values.year, values.month, values.dayOfMonth);
            writeTime(payload, values.hours, values.minutes, values.seconds);
            return;
        }
        payload.writeInt1(11);
        writeDate(payload, values.year, values.month, values.dayOfMonth);
        writeTime(payload, values.hours, values.minutes, values.seconds);
        writeNanos(payload, values.nanos);
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
    
    private DateTimeValues buildDateTimeValues(final LocalDateTime dateTime) {
        int year = dateTime.getYear();
        int month = dateTime.getMonthValue();
        int dayOfMonth = dateTime.getDayOfMonth();
        int hours = dateTime.getHour();
        int minutes = dateTime.getMinute();
        int seconds = dateTime.getSecond();
        int nanos = dateTime.getNano();
        if (nanos >= NANOS_PER_SECOND) {
            long overflowNanos = nanos;
            seconds = (int) (seconds + overflowNanos / NANOS_PER_SECOND);
            nanos = (int) (overflowNanos % NANOS_PER_SECOND);
            if (seconds >= 60) {
                LocalDateTime normalized = dateTime.plusSeconds(seconds - dateTime.getSecond());
                year = normalized.getYear();
                month = normalized.getMonthValue();
                dayOfMonth = normalized.getDayOfMonth();
                hours = normalized.getHour();
                minutes = normalized.getMinute();
                seconds = normalized.getSecond();
            }
        }
        return new DateTimeValues(year, month, dayOfMonth, hours, minutes, seconds, nanos);
    }
    
    @RequiredArgsConstructor
    private static class DateTimeValues {
        
        private final int year;
        
        private final int month;
        
        private final int dayOfMonth;
        
        private final int hours;
        
        private final int minutes;
        
        private final int seconds;
        
        private final int nanos;
    }
}
