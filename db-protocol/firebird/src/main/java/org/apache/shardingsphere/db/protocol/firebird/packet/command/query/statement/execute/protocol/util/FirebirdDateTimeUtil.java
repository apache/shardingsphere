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

package org.apache.shardingsphere.db.protocol.firebird.packet.command.query.statement.execute.protocol.util;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Getter
@Setter
public final class FirebirdDateTimeUtil {
    
    public int year;
    public int month;
    public int day;
    public int hour;
    public int minute;
    public int second;
    public int fractions;
    
    static final int NANOSECONDS_PER_FRACTION = 100 * 1000;
    static final int FRACTIONS_PER_MILLISECOND = 10;
    static final int FRACTIONS_PER_SECOND = 1000 * FRACTIONS_PER_MILLISECOND;
    static final int FRACTIONS_PER_MINUTE = 60 * FRACTIONS_PER_SECOND;
    static final int FRACTIONS_PER_HOUR = 60 * FRACTIONS_PER_MINUTE;
    
    public FirebirdDateTimeUtil(LocalDateTime localDateTime) {
        year = localDateTime.getYear();
        month = localDateTime.getMonthValue();
        day = localDateTime.getDayOfMonth();
        hour = localDateTime.getHour();
        minute = localDateTime.getMinute();
        second = localDateTime.getSecond();
        fractions = (localDateTime.getNano() / NANOSECONDS_PER_FRACTION) % FRACTIONS_PER_SECOND;
    }
    
    public FirebirdDateTimeUtil() {
        year = 0;
        month = 1;
        day = 1;
        hour = 0;
        minute = 0;
        second = 0;
        fractions = 0;
    }
    
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
    
    private int convertDate(int c, int ya, int cpMonth) {
        return (146097 * c) / 4 +
                (1461 * ya) / 4 +
                (153 * cpMonth + 2) / 5 +
                day + 1721119 - 2400001;
    }
    
    public FirebirdDateTimeUtil setDate(int encodedDate) {
        int sql_date = encodedDate - 1721119 + 2400001;
        int century = (4 * sql_date - 1) / 146097;
        sql_date = 4 * sql_date - 1 - 146097 * century;
        day = sql_date / 4;
        sql_date = (4 * day + 3) / 1461;
        day = 4 * day + 3 - 1461 * sql_date;
        day = (day + 4) / 4;
        month = (5 * day - 3) / 153;
        day = 5 * day - 3 - 153 * month;
        day = (day + 5) / 5;
        year = 100 * century + sql_date;
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
    
    public FirebirdDateTimeUtil setTime(int encodedTime) {
        int fractionsInDay = encodedTime;
        hour = fractionsInDay / FRACTIONS_PER_HOUR;
        fractionsInDay -= hour * FRACTIONS_PER_HOUR;
        minute = fractionsInDay / FRACTIONS_PER_MINUTE;
        fractionsInDay -= minute * FRACTIONS_PER_MINUTE;
        second = fractionsInDay / FRACTIONS_PER_SECOND;
        fractions = fractionsInDay - second * FRACTIONS_PER_SECOND;
        return this;
    }
    
    public Timestamp asTimestamp() {
        return Timestamp.valueOf(LocalDateTime.of(year, month, day, hour, minute, second, fractions));
    }
    
    public static Timestamp getDate(int encodedDate) {
        return new FirebirdDateTimeUtil().setDate(encodedDate).asTimestamp();
    }
    
    public static int getEncodedDate(LocalDateTime localDateTime) {
        return new FirebirdDateTimeUtil(localDateTime).getEncodedDate();
    }
    
    public static Timestamp getTime(int encodedTime) {
        return new FirebirdDateTimeUtil().setTime(encodedTime).asTimestamp();
    }
    
    public static int getEncodedTime(LocalDateTime localDateTime) {
        return new FirebirdDateTimeUtil(localDateTime).getEncodedDate();
    }
    
    public static Timestamp getDateTime(int encodedDate, int encodedTime) {
        return new FirebirdDateTimeUtil().setDate(encodedDate).setTime(encodedTime).asTimestamp();
    }
}
