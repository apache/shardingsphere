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

package org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.bind.protocol.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * Binary timestamp utility class of PostgreSQL.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PostgreSQLBinaryTimestampUtils {
    
    private static final Long POSTGRESQL_SECONDS_OFFSET = 946684800L;
    
    private static final Long JULIAN_GREGORIAN_CALENDAR_CUTOFF_POINT = -13165977600L;
    
    /**
     * Convert Timestamp to PostgreSQL time.
     *
     * @param timestamp timestamp
     * @return PostgreSQL time
     */
    public static long toPostgreSQLTime(final Timestamp timestamp) {
        long millis = timestamp.getTime() - (timestamp.getNanos() / 1000000L) + (TimeZone.getDefault().getRawOffset());
        long nanos = timestamp.getNanos() / 1000L;
        long pgSeconds = convertJavaEpochToPgEpoch(millis / 1000L);
        if (nanos >= 1000000L) {
            nanos -= 1000000L;
            pgSeconds++;
        }
        return pgSeconds * 1000000L + nanos;
    }
    
    /**
     * Refer to <a href="https://github.com/pgjdbc/pgjdbc/blob/e5e36bd3e8ac87ae554ac5cd1ac664fcd0010073/pgjdbc/src/main/java/org/postgresql/jdbc/TimestampUtils.java#L1453-L1475">
     * org.postgresql.jdbc.TimestampUtils</a>.
     *
     * @param seconds seconds
     * @return epoch of PostgreSQL
     */
    private static long convertJavaEpochToPgEpoch(final long seconds) {
        long result = seconds - POSTGRESQL_SECONDS_OFFSET;
        if (result >= JULIAN_GREGORIAN_CALENDAR_CUTOFF_POINT) {
            return result;
        }
        result = convertToJulianSeconds(result);
        if (result < -15773356800L) {
            int years = (int) ((result + 15773356800L) / -3155823050L);
            years++;
            years -= years / 4;
            result += years * 86400L;
        }
        return result;
    }
    
    private static long convertToJulianSeconds(final long seconds) {
        return seconds - TimeUnit.DAYS.toSeconds(10L);
    }
}
