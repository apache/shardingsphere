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
                return new Timestamp(0);
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
        Timestamp timestamp = (Timestamp) value;
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp.getTime());
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);
        int millisecond = timestamp.getNanos();
        boolean isDateValueAbsent = 0 == year && 0 == month && 0 == day;
        boolean isTimeValueAbsent = 0 == hour && 0 == minute && 0 == second;
        boolean isMillisecondValueAbsent = 0 == millisecond;
        if (isDateValueAbsent && isTimeValueAbsent && isMillisecondValueAbsent) {
            payload.writeInt1(0);
            return;
        }
        if (isTimeValueAbsent && isMillisecondValueAbsent) {
            payload.writeInt1(4);
            payload.writeInt2(year);
            payload.writeInt1(month);
            payload.writeInt1(day);
            return;
        }
        if (isMillisecondValueAbsent) {
            payload.writeInt1(7);
            payload.writeInt2(year);
            payload.writeInt1(month);
            payload.writeInt1(day);
            payload.writeInt1(hour);
            payload.writeInt1(minute);
            payload.writeInt1(second);
            return;
        }
        payload.writeInt1(11);
        payload.writeInt2(year);
        payload.writeInt1(month);
        payload.writeInt1(day);
        payload.writeInt1(hour);
        payload.writeInt1(minute);
        payload.writeInt1(second);
        payload.writeInt4(millisecond);
    }
}
