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

package org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.extended.bind.protocol;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Utils for PostgreSQL timestamp.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PostgreSQLTextTimestampUtils {
    
    private static final DateTimeFormatter POSTGRESQL_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(
            "[yyyy-MM-dd][yyyy_MM_dd][yyyyMMdd][yyyy-M-d][MM/dd/yy][yyMMdd]"
                    + "['T'][ ]"
                    + "[HH:mm:ss][HHmmss][HH:mm][HHmm]"
                    + "[.SSSSSS][.SSSSS][.SSSS][.SSS][.SS][.S]"
                    + "[ ]"
                    + "[XXXXX][XXXX][XXX][XX][X]"
    );
    
    /**
     * Parse timestamp in PostgreSQL text format.
     *
     * @param value text value to be parsed
     * @return timestamp
     */
    public static Timestamp parse(final String value) {
        try {
            return Timestamp.valueOf(LocalDateTime.from(POSTGRESQL_DATE_TIME_FORMATTER.parse(value)));
        } catch (final DateTimeParseException ignored) {
            return Timestamp.valueOf(value);
        }
    }
}
