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

package org.apache.shardingsphere.database.protocol.mysql.packet.binlog.row.column.value.time;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Time value utility class of MySQL.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MySQLTimeValueUtils {
    
    public static final String ZERO_OF_TIME = "00:00:00";
    
    public static final String ZERO_OF_DATE = "0000-00-00";
    
    public static final String YEAR_OF_ZERO = "0000";
    
    public static final String DATETIME_OF_ZERO = "0000-00-00 00:00:00";
    
    /**
     * Judge whether a date has both a month and a day, which is what {@link java.time.LocalDate} requires.
     * MySQL stores a zero month or a zero day whenever NO_ZERO_IN_DATE is not in sql_mode.
     *
     * @param month month
     * @param day day
     * @return whether the date is complete
     */
    public static boolean isCompleteDate(final int month, final int day) {
        return month > 0 && day > 0;
    }
    
    /**
     * Format a date that {@link java.time.LocalDate} cannot hold, in the text form MySQL prints it in.
     *
     * @param year year
     * @param month month
     * @param day day
     * @return date in MySQL text form
     */
    public static String formatIncompleteDate(final int year, final int month, final int day) {
        return String.format("%04d-%02d-%02d", year, month, day);
    }
    
    /**
     * Format a datetime whose date part {@link java.time.LocalDate} cannot hold, in the text form MySQL prints it in.
     *
     * @param year year
     * @param month month
     * @param day day
     * @param hour hour
     * @param minute minute
     * @param second second
     * @param nanos nanoseconds, always a whole number of microseconds
     * @return datetime in MySQL text form
     */
    public static String formatIncompleteDatetime(final int year, final int month, final int day, final int hour, final int minute, final int second, final int nanos) {
        String result = String.format("%04d-%02d-%02d %02d:%02d:%02d", year, month, day, hour, minute, second);
        return 0 == nanos ? result : result + String.format(".%06d", nanos / 1000);
    }
}
