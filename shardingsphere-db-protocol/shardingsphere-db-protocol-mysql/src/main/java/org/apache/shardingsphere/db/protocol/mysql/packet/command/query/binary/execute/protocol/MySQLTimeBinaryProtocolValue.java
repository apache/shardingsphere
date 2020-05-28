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

package org.apache.shardingsphere.db.protocol.mysql.packet.command.query.binary.execute.protocol;

import org.apache.shardingsphere.db.protocol.mysql.payload.MySQLPacketPayload;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;

/**
 * Binary protocol value for time for MySQL.
 */
public final class MySQLTimeBinaryProtocolValue implements MySQLBinaryProtocolValue {
    
    @Override
    public Object read(final MySQLPacketPayload payload) {
        int length = payload.readInt1();
        payload.readInt1();
        payload.readInt4();
        switch (length) {
            case 0:
                return new Timestamp(0L);
            case 8:
                return getTimestamp(payload);
            case 12:
                Timestamp result = getTimestamp(payload);
                result.setNanos(payload.readInt4());
                return result;
            default:
                throw new IllegalArgumentException(String.format("Wrong length '%d' of MYSQL_TYPE_DATE", length));
        }
    }
    
    private Timestamp getTimestamp(final MySQLPacketPayload payload) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(0, Calendar.JANUARY, 0, payload.readInt1(), payload.readInt1(), payload.readInt1());
        Timestamp result = new Timestamp(calendar.getTimeInMillis());
        result.setNanos(0);
        return result;
    }
    
    @Override
    public void write(final MySQLPacketPayload payload, final Object value) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(((Time) value).getTime());
        int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
        int minutes = calendar.get(Calendar.MINUTE);
        int seconds = calendar.get(Calendar.SECOND);
        int nanos = new Timestamp(calendar.getTimeInMillis()).getNanos();
        boolean isTimeAbsent = 0 == hourOfDay && 0 == minutes && 0 == seconds;
        boolean isNanosAbsent = 0 == nanos;
        if (isTimeAbsent && isNanosAbsent) {
            payload.writeInt1(0);
            return;
        }
        if (isNanosAbsent) {
            payload.writeInt1(8);
            writeTime(payload, hourOfDay, minutes, seconds);
            return;
        }
        payload.writeInt1(12);
        writeTime(payload, hourOfDay, minutes, seconds);
        writeNanos(payload, nanos);
    }
    
    private void writeTime(final MySQLPacketPayload payload, final int hourOfDay, final int minutes, final int seconds) {
        payload.writeInt1(0);
        payload.writeInt4(0);
        payload.writeInt1(hourOfDay);
        payload.writeInt1(minutes);
        payload.writeInt1(seconds);
    }
    
    private void writeNanos(final MySQLPacketPayload payload, final int nanos) {
        payload.writeInt4(nanos);
    }
}
