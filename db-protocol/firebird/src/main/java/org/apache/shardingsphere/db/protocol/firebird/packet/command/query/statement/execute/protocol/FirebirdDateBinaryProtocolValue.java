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

package org.apache.shardingsphere.db.protocol.firebird.packet.command.query.statement.execute.protocol;

import org.apache.shardingsphere.db.protocol.firebird.payload.FirebirdPacketPayload;

import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * Binary protocol value for date for Firebird.
 * TODO Test Date type
 */
public final class FirebirdDateBinaryProtocolValue implements FirebirdBinaryProtocolValue {
    
    @Override
    public Object read(final FirebirdPacketPayload payload) throws SQLException {
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
    
    private Timestamp getTimestampForDate(final FirebirdPacketPayload payload) {
        return Timestamp.valueOf(LocalDate.of(payload.readInt2(), payload.readInt1(), payload.readInt1()).atStartOfDay());
    }
    
    private Timestamp getTimestampForDatetime(final FirebirdPacketPayload payload) {
        return Timestamp.valueOf(LocalDateTime.of(payload.readInt2(), payload.readInt1(), payload.readInt1(), payload.readInt1(), payload.readInt1(), payload.readInt1()));
    }
    
    @Override
    public void write(final FirebirdPacketPayload payload, final Object value) {
        LocalDateTime dateTime = value instanceof LocalDateTime ? (LocalDateTime) value : new Timestamp(((Date) value).getTime()).toLocalDateTime();
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
    
    private void writeDate(final FirebirdPacketPayload payload, final int year, final int month, final int dayOfMonth) {
        payload.writeInt2(year);
        payload.writeInt1(month);
        payload.writeInt1(dayOfMonth);
    }
    
    private void writeTime(final FirebirdPacketPayload payload, final int hourOfDay, final int minutes, final int seconds) {
        payload.writeInt1(hourOfDay);
        payload.writeInt1(minutes);
        payload.writeInt1(seconds);
    }
    
    private void writeNanos(final FirebirdPacketPayload payload, final int nanos) {
        payload.writeInt4(nanos);
    }
}
