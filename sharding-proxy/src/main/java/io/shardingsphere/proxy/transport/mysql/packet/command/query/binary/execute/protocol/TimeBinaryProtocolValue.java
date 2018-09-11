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
import java.util.Date;

/**
 * Binary protocol value for time.
 * 
 * @author zhangyonglun
 * @author zhangliang
 */
public final class TimeBinaryProtocolValue implements BinaryProtocolValue {
    
    @Override
    public Object read(final MySQLPacketPayload payload) {
        Timestamp result;
        Calendar calendar = Calendar.getInstance();
        int length = payload.readInt1();
        payload.readInt1();
        payload.readInt4();
        switch (length) {
            case 0:
                return new Timestamp(0);
            case 8:
                calendar.set(0, Calendar.JANUARY, 0, payload.readInt1(), payload.readInt1(), payload.readInt1());
                result = new Timestamp(calendar.getTimeInMillis());
                result.setNanos(0);
                return result;
            case 12:
                calendar.set(0, Calendar.JANUARY, 0, payload.readInt1(), payload.readInt1(), payload.readInt1());
                result = new Timestamp(calendar.getTimeInMillis());
                result.setNanos(payload.readInt4());
                return result;
            default:
                throw new IllegalArgumentException(String.format("Wrong length '%d' of MYSQL_TYPE_DATE", length));
        }
    }
    
    @Override
    public void write(final MySQLPacketPayload payload, final Object value) {
        Date date = (Date) value;
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(date.getTime());
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);
        Timestamp timestamp = new Timestamp(date.getTime());
        int millisecond = timestamp.getNanos();
        boolean isTimeValueAbsent = 0 == hour && 0 == minute && 0 == second;
        boolean isMillisecondValueAbsent = 0 == millisecond;
        if (isTimeValueAbsent && isMillisecondValueAbsent) {
            payload.writeInt1(0);
            return;
        }
        if (isMillisecondValueAbsent) {
            payload.writeInt1(8);
            payload.writeInt1(0);
            payload.writeInt4(0);
            payload.writeInt1(hour);
            payload.writeInt1(minute);
            payload.writeInt1(second);
            return;
        }
        payload.writeInt1(12);
        payload.writeInt1(0);
        payload.writeInt4(0);
        payload.writeInt1(hour);
        payload.writeInt1(minute);
        payload.writeInt1(second);
        payload.writeInt4(millisecond);
    }
}
