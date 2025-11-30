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

package org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.execute.protocol.util;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 * Firebird date and time utility class.
 */
@Getter
@Setter
public final class FirebirdDateTimeUtils {
    
    static final int NANOSECONDS_PER_FRACTION = 100 * 1000;
    
    static final int FRACTIONS_PER_MILLISECOND = 10;
    
    static final int FRACTIONS_PER_SECOND = 1000 * FRACTIONS_PER_MILLISECOND;
    
    static final int FRACTIONS_PER_MINUTE = 60 * FRACTIONS_PER_SECOND;
    
    static final int FRACTIONS_PER_HOUR = 60 * FRACTIONS_PER_MINUTE;
    
    private int year;
    
    private int month;
    
    private int day;
    
    private int hour;
    
    private int minute;
    
    private int second;
    
    private int fractions;
    
    public FirebirdDateTimeUtils(final LocalDateTime localDateTime) {
        year = localDateTime.getYear();
        month = localDateTime.getMonthValue();
        day = localDateTime.getDayOfMonth();
        hour = localDateTime.getHour();
        minute = localDateTime.getMinute();
        second = localDateTime.getSecond();
        fractions = (localDateTime.getNano() / NANOSECONDS_PER_FRACTION) % FRACTIONS_PER_SECOND;
    }
    
    public FirebirdDateTimeUtils() {
        year = 0;
        month = 1;
        day = 1;
        hour = 0;
        minute = 0;
        second = 0;
        fractions = 0;
    }
    
    /**
     * Get encoded date value.
     *
     * @return encoded date as integer
     */
    public int getEncodedDate() {
        int cpMonth = month;
        int cpYear = year;
        if (cpMonth > 2) {
            cpMonth -= 3;
        } else {
            cpMonth += 9;
            cpYear -= 1;
        }
        int c = cpYear / 100;
        int ya = cpYear - 100 * c;
        return convertDate(c, ya, cpMonth);
    }
    
    /**
     * Encode {@link LocalDateTime} to an integer date representation.
     *
     * @param localDateTime the local date-time to encode
     * @return encoded date as integer
     */
    public static int getEncodedDate(final LocalDateTime localDateTime) {
        return new FirebirdDateTimeUtils(localDateTime).getEncodedDate();
    }
    
    private int convertDate(final int c, final int ya, final int cpMonth) {
        return (146097 * c) / 4
                + (1461 * ya) / 4
                + (153 * cpMonth + 2) / 5
                + day + 1721119 - 2400001;
    }
    
    /**
     * Decode encoded date and set internal year, month, and day fields.
     *
     * @param encodedDate encoded date as integer
     * @return this instance with updated date fields
     */
    public FirebirdDateTimeUtils setDate(final int encodedDate) {
        int sqlDate = encodedDate - 1721119 + 2400001;
        int century = (4 * sqlDate - 1) / 146097;
        sqlDate = 4 * sqlDate - 1 - 146097 * century;
        day = sqlDate / 4;
        sqlDate = (4 * day + 3) / 1461;
        day = 4 * day + 3 - 1461 * sqlDate;
        day = (day + 5) / 5;
        month = (5 * day - 3) / 153;
        day = 5 * day - 3 - 153 * month;
        day = (day + 5) / 5;
        year = 100 * century + sqlDate;
        if (month < 10) {
            month += 3;
        } else {
            month -= 9;
            year += 1;
        }
        return this;
    }
    
    public int getEncodedTime() {
        return hour * FRACTIONS_PER_HOUR
                + minute * FRACTIONS_PER_MINUTE
                + second * FRACTIONS_PER_SECOND
                + fractions;
    }
    
    /**
     * Decode encoded time and set internal hour, minute, second, and fraction fields.
     *
     * @param encodedTime encoded time as integer
     * @return this instance with updated time fields
     */
    public FirebirdDateTimeUtils setTime(final int encodedTime) {
        int fractionsInDay = encodedTime;
        hour = fractionsInDay / FRACTIONS_PER_HOUR;
        fractionsInDay -= hour * FRACTIONS_PER_HOUR;
        minute = fractionsInDay / FRACTIONS_PER_MINUTE;
        fractionsInDay -= minute * FRACTIONS_PER_MINUTE;
        second = fractionsInDay / FRACTIONS_PER_SECOND;
        fractions = fractionsInDay - second * FRACTIONS_PER_SECOND;
        return this;
    }
    
    /**
     * Convert the internal date-time fields to a {@link Timestamp}.
     *
     * @return timestamp representation of the current date-time fields
     */
    public Timestamp asTimestamp() {
        return Timestamp.valueOf(LocalDateTime.of(year, month, day, hour, minute, second, fractions));
    }
    
    /**
     * Convert encoded date value to {@link Timestamp}.
     *
     * @param encodedDate encoded date as integer
     * @return timestamp representation of the encoded date
     */
    public static Timestamp getDate(final int encodedDate) {
        return new FirebirdDateTimeUtils().setDate(encodedDate).asTimestamp();
    }
    
    /**
     * Convert encoded time value to {@link Timestamp}.
     *
     * @param encodedTime encoded time as integer
     * @return timestamp representation of the encoded time
     */
    public static Timestamp getTime(final int encodedTime) {
        return new FirebirdDateTimeUtils().setTime(encodedTime).asTimestamp();
    }
    
    /**
     * Convert encoded date and time values to {@link Timestamp}.
     *
     * @param encodedDate encoded date as integer
     * @param encodedTime encoded time as integer
     * @return timestamp representation of the encoded date and time
     */
    public static Timestamp getDateTime(final int encodedDate, final int encodedTime) {
        return new FirebirdDateTimeUtils().setDate(encodedDate).setTime(encodedTime).asTimestamp();
    }
    
    /**
     * Convert encoded date and time values with offset to {@link Timestamp}.
     *
     * @param encodedDate encoded date as integer
     * @param encodedTime encoded time as integer
     * @param offset offset value in minutes or seconds (pending implementation details)
     * @return timestamp representation of the encoded date and time with offset
     */
    public static Timestamp getDateTimeWithOffset(final int encodedDate, final int encodedTime, final int offset) {
        // TODO add time zone support
        return new FirebirdDateTimeUtils().setDate(encodedDate).setTime(encodedTime).asTimestamp();
    }
}
