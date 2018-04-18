/*
 * Copyright 1999-2015 dangdang.com.
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

package io.shardingjdbc.proxy.transport.mysql.packet.command.statement.execute;

import io.shardingjdbc.proxy.transport.mysql.constant.ColumnType;
import io.shardingjdbc.proxy.transport.mysql.packet.MySQLPacketPayload;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

/**
 * Binary protocol value.
 * @see <a href="https://dev.mysql.com/doc/internals/en/binary-protocol-value.html">Binary Protocol Value</a>
 *
 * @author zhangyonglun
 */
@NoArgsConstructor(access = AccessLevel.NONE)
public final class BinaryProtocolValueUtility {
    
    private static final BinaryProtocolValueUtility INSTANCE = new BinaryProtocolValueUtility();
    
    /**
     * Get binary protocol value utility instance.
     *
     * @return binary protocol value utility
     */
    public static BinaryProtocolValueUtility getInstance() {
        return INSTANCE;
    }
    
    /**
     * Read binary protocol value.
     *
     * @param columnType column type
     * @param mysqlPacketPayload mysql packet payload
     * @return object value
     */
    public Object readBinaryProtocolValue(final ColumnType columnType, final MySQLPacketPayload mysqlPacketPayload) {
        switch (columnType) {
            case MYSQL_TYPE_STRING:
            case MYSQL_TYPE_VARCHAR:
            case MYSQL_TYPE_VAR_STRING:
            case MYSQL_TYPE_ENUM:
            case MYSQL_TYPE_SET:
            case MYSQL_TYPE_LONG_BLOB:
            case MYSQL_TYPE_MEDIUM_BLOB:
            case MYSQL_TYPE_BLOB:
            case MYSQL_TYPE_TINY_BLOB:
            case MYSQL_TYPE_GEOMETRY:
            case MYSQL_TYPE_BIT:
            case MYSQL_TYPE_DECIMAL:
            case MYSQL_TYPE_NEWDECIMAL:
                return mysqlPacketPayload.readStringLenenc();
            case MYSQL_TYPE_LONGLONG:
                return mysqlPacketPayload.readInt8();
            case MYSQL_TYPE_LONG:
            case MYSQL_TYPE_INT24:
                return mysqlPacketPayload.readInt4();
            case MYSQL_TYPE_SHORT:
            case MYSQL_TYPE_YEAR:
                return mysqlPacketPayload.readInt2();
            case MYSQL_TYPE_TINY:
                return mysqlPacketPayload.readInt1();
            case MYSQL_TYPE_DOUBLE:
                return mysqlPacketPayload.readInt8();
            case MYSQL_TYPE_FLOAT:
                return mysqlPacketPayload.readInt4();
            case MYSQL_TYPE_DATE:
            case MYSQL_TYPE_DATETIME:
            case MYSQL_TYPE_TIMESTAMP:
                return readDate(mysqlPacketPayload);
            case MYSQL_TYPE_TIME:
                return readTime(mysqlPacketPayload);
            default:
                throw new IllegalArgumentException(String.format("Cannot find MYSQL type '%s' in column type when read binary protocol value", columnType));
        }
    }
    
    private Timestamp readDate(final MySQLPacketPayload mysqlPacketPayload) {
        Timestamp timestamp;
        Calendar calendar = Calendar.getInstance();
        int length = mysqlPacketPayload.readInt1();
        switch (length) {
            case 0:
                timestamp = new Timestamp(0);
                break;
            case 4:
                calendar.set(mysqlPacketPayload.readInt2(), mysqlPacketPayload.readInt1() - 1, mysqlPacketPayload.readInt1());
                timestamp = new Timestamp(calendar.getTimeInMillis());
                break;
            case 7:
                calendar.set(mysqlPacketPayload.readInt2(), mysqlPacketPayload.readInt1() - 1, mysqlPacketPayload.readInt1(),
                    mysqlPacketPayload.readInt1(), mysqlPacketPayload.readInt1(), mysqlPacketPayload.readInt1());
                timestamp = new Timestamp(calendar.getTimeInMillis());
                break;
            case 11:
                calendar.set(mysqlPacketPayload.readInt2(), mysqlPacketPayload.readInt1() - 1, mysqlPacketPayload.readInt1(),
                    mysqlPacketPayload.readInt1(), mysqlPacketPayload.readInt1(), mysqlPacketPayload.readInt1());
                timestamp = new Timestamp(calendar.getTimeInMillis());
                timestamp.setNanos(mysqlPacketPayload.readInt4());
                break;
            default:
                throw new IllegalArgumentException(String.format("Wrong length '%d' of MYSQL_TYPE_TIME", length));
        }
        return timestamp;
    }
    
    private Timestamp readTime(final MySQLPacketPayload mysqlPacketPayload) {
        Timestamp timestamp;
        Calendar calendar = Calendar.getInstance();
        int length = mysqlPacketPayload.readInt1();
        mysqlPacketPayload.readInt1();
        mysqlPacketPayload.readInt4();
        switch (length) {
            case 0:
                timestamp = new Timestamp(0);
                break;
            case 8:
                calendar.set(0, 0, 0, mysqlPacketPayload.readInt1(), mysqlPacketPayload.readInt1() - 1, mysqlPacketPayload.readInt1());
                timestamp = new Timestamp(calendar.getTimeInMillis());
                break;
            case 12:
                calendar.set(0, 0, 0, mysqlPacketPayload.readInt1(), mysqlPacketPayload.readInt1() - 1, mysqlPacketPayload.readInt1());
                timestamp = new Timestamp(calendar.getTimeInMillis());
                timestamp.setNanos(mysqlPacketPayload.readInt4());
                break;
            default:
                throw new IllegalArgumentException(String.format("Wrong length '%d' of MYSQL_TYPE_DATE", length));
        }
        return timestamp;
    }
    
    /**
     * Write binary protocol value.
     *
     * @param columnType column type
     * @param mysqlPacketPayload mysql packet pay load
     * @param objectData object data
     */
    public void writeBinaryProtocolValue(final ColumnType columnType, final MySQLPacketPayload mysqlPacketPayload, final Object objectData) {
        switch (columnType) {
            case MYSQL_TYPE_STRING:
            case MYSQL_TYPE_VARCHAR:
            case MYSQL_TYPE_VAR_STRING:
            case MYSQL_TYPE_ENUM:
            case MYSQL_TYPE_SET:
            case MYSQL_TYPE_LONG_BLOB:
            case MYSQL_TYPE_MEDIUM_BLOB:
            case MYSQL_TYPE_BLOB:
            case MYSQL_TYPE_TINY_BLOB:
            case MYSQL_TYPE_GEOMETRY:
            case MYSQL_TYPE_BIT:
            case MYSQL_TYPE_DECIMAL:
            case MYSQL_TYPE_NEWDECIMAL:
                mysqlPacketPayload.writeStringLenenc(objectData.toString());
                break;
            case MYSQL_TYPE_LONGLONG:
                mysqlPacketPayload.writeInt8((Long) objectData);
                break;
            case MYSQL_TYPE_LONG:
            case MYSQL_TYPE_INT24:
                mysqlPacketPayload.writeInt4((Integer) objectData);
                break;
            case MYSQL_TYPE_SHORT:
            case MYSQL_TYPE_YEAR:
                mysqlPacketPayload.writeInt2((Integer) objectData);
                break;
            case MYSQL_TYPE_TINY:
                mysqlPacketPayload.writeInt1((Integer) objectData);
                break;
            case MYSQL_TYPE_DOUBLE:
                mysqlPacketPayload.writeDouble(Double.parseDouble(objectData.toString()));
                break;
            case MYSQL_TYPE_FLOAT:
                mysqlPacketPayload.writeFloat(Float.parseFloat(objectData.toString()));
                break;
            case MYSQL_TYPE_DATE:
            case MYSQL_TYPE_DATETIME:
            case MYSQL_TYPE_TIMESTAMP:
                writeDate((Timestamp) objectData, mysqlPacketPayload);
                break;
            case MYSQL_TYPE_TIME:
                writeTime((Date) objectData, mysqlPacketPayload);
                break;
            default:
                throw new IllegalArgumentException(String.format("Cannot find MYSQL type '%s' in column type when write binary protocol value", columnType));
        }
    }
    
    private void writeDate(final Timestamp timestamp, final MySQLPacketPayload mysqlPacketPayload) {
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
            mysqlPacketPayload.writeInt1(0);
        } else if (isTimeValueAbsent && isMillisecondValueAbsent) {
            mysqlPacketPayload.writeInt1(4);
            mysqlPacketPayload.writeInt2(year);
            mysqlPacketPayload.writeInt1(month);
            mysqlPacketPayload.writeInt1(day);
        } else if (isMillisecondValueAbsent) {
            mysqlPacketPayload.writeInt1(7);
            mysqlPacketPayload.writeInt2(year);
            mysqlPacketPayload.writeInt1(month);
            mysqlPacketPayload.writeInt1(day);
            mysqlPacketPayload.writeInt1(hour);
            mysqlPacketPayload.writeInt1(minute);
            mysqlPacketPayload.writeInt1(second);
        } else {
            mysqlPacketPayload.writeInt1(11);
            mysqlPacketPayload.writeInt2(year);
            mysqlPacketPayload.writeInt1(month);
            mysqlPacketPayload.writeInt1(day);
            mysqlPacketPayload.writeInt1(hour);
            mysqlPacketPayload.writeInt1(minute);
            mysqlPacketPayload.writeInt1(second);
            mysqlPacketPayload.writeInt4(millisecond);
        }
    }
    
    private void writeTime(final Date date, final MySQLPacketPayload mysqlPacketPayload) {
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
            mysqlPacketPayload.writeInt1(0);
        } else if (isMillisecondValueAbsent) {
            mysqlPacketPayload.writeInt1(8);
            mysqlPacketPayload.writeInt1(0);
            mysqlPacketPayload.writeInt4(0);
            mysqlPacketPayload.writeInt1(hour);
            mysqlPacketPayload.writeInt1(minute);
            mysqlPacketPayload.writeInt1(second);
        } else {
            mysqlPacketPayload.writeInt1(12);
            mysqlPacketPayload.writeInt1(0);
            mysqlPacketPayload.writeInt4(0);
            mysqlPacketPayload.writeInt1(hour);
            mysqlPacketPayload.writeInt1(minute);
            mysqlPacketPayload.writeInt1(second);
            mysqlPacketPayload.writeInt4(millisecond);
        }
    }
}
