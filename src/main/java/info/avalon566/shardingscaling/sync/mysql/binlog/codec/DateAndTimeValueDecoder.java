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

package info.avalon566.shardingscaling.sync.mysql.binlog.codec;

import com.google.common.base.Strings;
import io.netty.buffer.ByteBuf;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Date and time value decoder.
 * https://dev.mysql.com/doc/refman/5.7/en/date-and-time-types.html
 *
 * @author avalon566
 */
public final class DateAndTimeValueDecoder {

    /**
     * Decode time.
     *
     * @param meta value
     * @param in   buffer
     * @return time string value
     */
    public static Serializable decodeTime(final int meta, final ByteBuf in) {
        String datetime = Long.toString(DataTypesCodec.readUnsignedInt3LE(in));
        if ("0".equals(datetime)) {
            return "00:00:00";
        }
        datetime = Strings.padStart(datetime, 6, '0');
        final String hour = datetime.substring(0, 2);
        final String minute = datetime.substring(2, 4);
        final String second = datetime.substring(4, 6);
        return String.format("%s:%s:%s", hour, minute, second);
    }

    /**
     * Decode time2.
     *
     * @param meta value
     * @param in   buffer
     * @return time string value
     */
    public static Serializable decodeTime2(final int meta, final ByteBuf in) {
        long time = DataTypesCodec.readUnsignedInt3BE(in) - 0x800000L;
        return String.format("%02d:%02d:%02d",
                (time >> 12) % (1 << 10),
                (time >> 6) % (1 << 6),
                time % (1 << 6));
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
            return "0000-00-00";
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
        return DataTypesCodec.readUnsignedInt1(in) + 1900;
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
            return "0000-00-00 00:00:00";
        }
        String secondStr = new Timestamp(second * 1000).toString();
        // remove millsecond data
        return secondStr.substring(0, secondStr.length() - 2);
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
            return "0000-00-00 00:00:00";
        }
        String secondStr = new Timestamp(second * 1000).toString();
        // remove millsecond data
        secondStr = secondStr.substring(0, secondStr.length() - 2);
        if (0 < meta) {
            secondStr += "." + readAndAlignMillisecond(meta, in);
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
        final String datetime = Long.toString(DataTypesCodec.readInt8LE(in));
        if ("0".equals(datetime)) {
            return "0000-00-00 00:00:00";
        }
        final String year = datetime.substring(0, 4);
        final String month = datetime.substring(4, 6);
        final String day = datetime.substring(6, 8);
        final String hour = datetime.substring(8, 10);
        final String minute = datetime.substring(10, 12);
        final String second = datetime.substring(12, 14);
        return String.format("%s-%s-%s %s:%s:%s", year, month, day, hour, minute, second);
    }

    /**
     * Decode datetime2.
     *
     * @param meta value
     * @param in   buffer
     * @return datetime string value
     */
    public static Serializable decodeDatetime2(final int meta, final ByteBuf in) {
        long datetime = DataTypesCodec.readUnsignedInt5BE(in) - 0x8000000000L;
        if (0 == datetime) {
            return "0000-00-00 00:00:00";
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
                0 < meta ? "." + readAndAlignMillisecond(meta, in) : "");
    }

    private static String readAndAlignMillisecond(final int meta, final ByteBuf in) {
        int fraction = 0;
        switch (meta) {
            case 1:
            case 2:
                fraction = DataTypesCodec.readUnsignedInt1(in) * 10000;
                break;
            case 3:
            case 4:
                fraction = DataTypesCodec.readUnsignedInt2BE(in) * 100;
                break;
            case 5:
            case 6:
                fraction = DataTypesCodec.readUnsignedInt3BE(in);
                break;
            default:
                throw new UnsupportedOperationException();
        }
        return alignMillisecond(meta, fraction);
    }

    private static String alignMillisecond(final int meta, final int fraction) {
        StringBuilder result = new StringBuilder(6);
        String str = Integer.toString(fraction);
        int append = 6 - str.length();
        for (int i = 0; i < append; i++) {
            result.append("0");
        }
        result.append(str);
        return result.substring(0, meta);
    }
}
