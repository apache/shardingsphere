/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.proxy.transport.mysql.packet.command.query.binary.execute.protocol;

import io.shardingsphere.proxy.transport.mysql.packet.MySQLPacketPayload;

import java.sql.Timestamp;
import java.util.Calendar;

/**
 * Binary protocol value for date.
 * 
 * @author zhangyonglun
 * @author zhangliang
 */
public final class DateBinaryProtocolValue implements BinaryProtocolValue {
    
    @Override
    public Object read(final MySQLPacketPayload payload) {
        int length = payload.readInt1();
        switch (length) {
            case 0:
                return new Timestamp(0L);
            case 4:
                return getTimestampForDate(payload);
            case 7:
                return getTimestampForDatetime(payload);
            case 11:
                Timestamp result = getTimestampForDatetime(payload);
                result.setNanos(payload.readInt4());
                return result;
            default:
                throw new IllegalArgumentException(String.format("Wrong length '%d' of MYSQL_TYPE_TIME", length));
        }
    }
    
    private Timestamp getTimestampForDate(final MySQLPacketPayload payload) {
        Calendar result = Calendar.getInstance();
        result.set(payload.readInt2(), payload.readInt1() - 1, payload.readInt1());
        return new Timestamp(result.getTimeInMillis());
    }
    
    private Timestamp getTimestampForDatetime(final MySQLPacketPayload payload) {
        Calendar result = Calendar.getInstance();
        result.set(payload.readInt2(), payload.readInt1() - 1, payload.readInt1(), payload.readInt1(), payload.readInt1(), payload.readInt1());
        return new Timestamp(result.getTimeInMillis());
    }
    
    @Override
    public void write(final MySQLPacketPayload payload, final Object value) {
        // TODO :yonglun confirm here is cannot set YEAR == 0, it at least 1970 here
        Timestamp timestamp = (Timestamp) value;
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp.getTime());
        int year = calendar.get(Calendar.YEAR);
        // TODO :yonglun confirm here is month + 1, and isDateAbsent adjust is 0 == month, is it never == 0?
        int month = calendar.get(Calendar.MONTH) + 1;
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
        int minutes = calendar.get(Calendar.MINUTE);
        int seconds = calendar.get(Calendar.SECOND);
        int nanos = timestamp.getNanos();
        boolean isDateAbsent = 0 == year && 0 == month && 0 == dayOfMonth;
        boolean isTimeAbsent = 0 == hourOfDay && 0 == minutes && 0 == seconds;
        boolean isNanosAbsent = 0 == nanos;
        if (isDateAbsent && isTimeAbsent && isNanosAbsent) {
            payload.writeInt1(0);
            return;
        }
        if (isTimeAbsent && isNanosAbsent) {
            payload.writeInt1(4);
            writeDate(payload, year, month, dayOfMonth);
            return;
        }
        if (isNanosAbsent) {
            payload.writeInt1(7);
            writeDate(payload, year, month, dayOfMonth);
            writeTime(payload, hourOfDay, minutes, seconds);
            return;
        }
        payload.writeInt1(11);
        writeDate(payload, year, month, dayOfMonth);
        writeTime(payload, hourOfDay, minutes, seconds);
        writeNanos(payload, nanos);
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
        payload.writeInt4(nanos);
    }
}
