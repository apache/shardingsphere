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

package org.apache.shardingsphere.shardingscaling.mysql.binlog.codec;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * Date and time value decoder.
 *
 * <p>
 *     https://dev.mysql.com/doc/refman/5.7/en/date-and-time-types.html
 * </p>
 */
public final class DateAndTimeValueDecoder {
    
    public static final String ZERO_OF_TIME = "00:00:00";
    
    public static final String ZERO_OF_DATE = "0000-00-00";
    
    public static final String YEAR_OF_ZERO = "0000";
    
    public static final String DATETIME_OF_ZERO = "0000-00-00 00:00:00";
    
    public static final ThreadLocal<SimpleDateFormat> TIMESTAMP_FORMAT = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            sdf.setTimeZone(TimeZone.getTimeZone("GMT+0:00"));
            return sdf;
        }
    };
    
    /**
     * Decode time.
     *
     * @param meta value
     * @param in   buffer
     * @return time string value
     */
    public static Serializable decodeTime(final int meta, final ByteBuf in) {
        int datetime = DataTypesCodec.readInt3LE(in);
        if (0 == datetime) {
            return ZERO_OF_TIME;
        }
        int hour = datetime / 10000;
        int minuteSecond = Math.abs(datetime) % 10000;
        int minute = minuteSecond / 100;
        int second = minuteSecond % 100;
        return String.format("%02d:%02d:%02d", hour, minute, second);
    }
    
    /**
     * Decode time2.
     *
     * @param meta value
     * @param in   buffer
     * @return time string value
     */
    public static Serializable decodeTime2(final int meta, final ByteBuf in) {
        TimeValue timeValue = decodeTime2Value(meta, in);
        if (0 == timeValue.time) {
            return ZERO_OF_TIME;
        }
        int hour = (timeValue.time >> 12) % (1 << 10);
        int minute = (timeValue.time >> 6) % (1 << 6);
        int second = timeValue.time % (1 << 6);
        return (timeValue.nonNegative ? "" : "-") + String.format("%02d:%02d:%02d%s", hour, minute, second,
                0 < meta ? "." + alignMillisecond(meta, timeValue.fraction) : "");
    }
    
    private static TimeValue decodeTime2Value(final int meta, final ByteBuf in) {
        // first bit for sign (1 = non negative, 0 = negative)
        // reverse first bit
        int signedTime = DataTypesCodec.readUnsignedInt3BE(in) - 0x800000;
        int fraction = 0;
        byte offset = 0;
        switch (meta) {
            case 0:
                break;
            case 1:
            case 2:
                fraction = DataTypesCodec.readUnsignedInt1(in);
                offset = 8;
                break;
            case 3:
            case 4:
                fraction = DataTypesCodec.readUnsignedInt2BE(in);
                offset = 16;
                break;
            case 5:
            case 6:
                fraction = DataTypesCodec.readUnsignedInt3BE(in);
                offset = 24;
                break;
            default:
                throw new UnsupportedOperationException();
        }
        signedTime = signedTime << 8;
        if (0 < offset && 0 > signedTime && 0 < fraction) {
            signedTime++;
            fraction -= 0x1 << offset;
        }
        return new TimeValue(signedTime > 0, Math.abs(signedTime) >> 8, Math.abs(fraction));
    }
    
    /**
     * Decode date.
     *
     * @param meta value
     * @param in   buffer
     * @return date string value
     */
    public static Serializable decodeDate(final int meta, final ByteBuf in) {
        int date = DataTypesCodec.readUnsignedInt3LE(in);
        if (0 == date) {
            return ZERO_OF_DATE;
        }
        return String.format("%d-%02d-%02d",
                date / 16 / 32,
                date / 32 % 16,
                date % 32);
    }
    
    /**
     * Decode year.
     *
     * @param meta value
     * @param in   buffer
     * @return year int value
     */
    public static Serializable decodeYear(final int meta, final ByteBuf in) {
        short value = DataTypesCodec.readUnsignedInt1(in);
        if (0 == value) {
            return YEAR_OF_ZERO;
        }
        return Integer.toString(value + 1900);
    }
    
    /**
     * Decode timestamp.
     *
     * @param meta value
     * @param in   buffer
     * @return datetime string value
     */
    public static Serializable decodeTimestamp(final int meta, final ByteBuf in) {
        long second = DataTypesCodec.readUnsignedInt4LE(in);
        if (0 == second) {
            return DATETIME_OF_ZERO;
        }
        return TIMESTAMP_FORMAT.get().format(new Timestamp(second * 1000));
    }
    
    /**
     * Decode timestamp2.
     *
     * @param meta value
     * @param in   buffer
     * @return datetime string value
     */
    public static Serializable decodeTimestamp2(final int meta, final ByteBuf in) {
        long second = DataTypesCodec.readUnsignedInt4BE(in);
        if (0 == second) {
            return DATETIME_OF_ZERO;
        }
        String secondStr = TIMESTAMP_FORMAT.get().format(new Timestamp(second * 1000));
        if (0 < meta) {
            secondStr += "." + alignMillisecond(meta, readMillisecond(meta, in));
        }
        return secondStr;
    }
    
    /**
     * Decode datetime.
     *
     * @param meta value
     * @param in   buffer
     * @return datetime string value
     */
    public static Serializable decodeDateTime(final int meta, final ByteBuf in) {
        final long datetime = DataTypesCodec.readInt8LE(in);
        if (0 == datetime) {
            return DATETIME_OF_ZERO;
        }
        final int d = (int) (datetime / 1000000);
        final int t = (int) (datetime % 1000000);
        final int year = d / 10000;
        final int month = (d % 10000) / 100;
        final int day = d % 100;
        final int hour = t / 10000;
        final int minute = (t % 10000) / 100;
        final int second = t % 100;
        return String.format("%04d-%02d-%02d %02d:%02d:%02d", year, month, day, hour, minute, second);
    }
    
    /**
     * Decode datetime2.
     *
     * @param meta value
     * @param in   buffer
     * @return datetime string value
     */
    public static Serializable decodeDatetime2(final int meta, final ByteBuf in) {
        long datetime = DataTypesCodec.readUnsignedInt5BE(in);
        datetime = datetime - 0x8000000000L;
        if (0 == datetime) {
            return DATETIME_OF_ZERO;
        }
        long ymd = datetime >> 17;
        long ym = ymd >> 5;
        long hms = datetime % (1 << 17);
        return String.format("%d-%02d-%02d %02d:%02d:%02d%s",
                ym / 13,
                ym % 13,
                ymd % (1 << 5),
                hms >> 12,
                (hms >> 6) % (1 << 6),
                hms % (1 << 6),
                0 < meta ? "." + alignMillisecond(meta, readMillisecond(meta, in)) : "");
    }
    
    private static int readMillisecond(final int meta, final ByteBuf in) {
        switch (meta) {
            case 1:
            case 2:
                return DataTypesCodec.readUnsignedInt1(in);
            case 3:
            case 4:
                return DataTypesCodec.readUnsignedInt2BE(in);
            case 5:
            case 6:
                return DataTypesCodec.readUnsignedInt3BE(in);
            default:
                throw new UnsupportedOperationException();
        }
    }
    
    private static String alignMillisecond(final int meta, final int fraction) {
        int value = 0;
        switch (meta) {
            case 1:
            case 2:
                value = fraction * 10000;
                break;
            case 3:
            case 4:
                value = fraction * 100;
                break;
            case 5:
            case 6:
                value = fraction;
                break;
            default:
                throw new UnsupportedOperationException();
        }
        return alignMillisecond0(meta, value);
    }
    
    private static String alignMillisecond0(final int meta, final int fraction) {
        StringBuilder result = new StringBuilder(6);
        String str = Integer.toString(fraction);
        int append = 6 - str.length();
        for (int i = 0; i < append; i++) {
            result.append("0");
        }
        result.append(str);
        return result.substring(0, meta);
    }
    
    @AllArgsConstructor
    @Getter
    private static class TimeValue {
        
        private boolean nonNegative;
        
        private int time;
        
        private int fraction;
    }
}
