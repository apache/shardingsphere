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

package org.apache.shardingsphere.sharding.cosid.algorithm.sharding.interval.convertor.impl;

import lombok.RequiredArgsConstructor;
import me.ahoo.cosid.util.LocalDateTimeConvert;
import org.apache.shardingsphere.sharding.cosid.algorithm.sharding.interval.convertor.CosIdLocalDateTimeConvertor;
import org.apache.shardingsphere.sharding.exception.ShardingPluginException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * Standard local date time convertor for CosId.
 */
@RequiredArgsConstructor
public final class StandardCosIdLocalDateTimeConvertor implements CosIdLocalDateTimeConvertor {
    
    private final ZoneId zoneId;
    
    private final boolean isSecondTs;
    
    private final DateTimeFormatter dateTimeFormatter;
    
    @Override
    public LocalDateTime toLocalDateTime(final Comparable<?> value) {
        if (value instanceof LocalDateTime) {
            return (LocalDateTime) value;
        }
        if (value instanceof ZonedDateTime) {
            return ((ZonedDateTime) value).toLocalDateTime();
        }
        if (value instanceof OffsetDateTime) {
            return ((OffsetDateTime) value).toLocalDateTime();
        }
        if (value instanceof Instant) {
            return LocalDateTimeConvert.fromInstant((Instant) value, zoneId);
        }
        if (value instanceof LocalDate) {
            return LocalDateTime.of((LocalDate) value, LocalTime.MIN);
        }
        if (value instanceof Date) {
            return LocalDateTimeConvert.fromDate((Date) value, zoneId);
        }
        if (value instanceof YearMonth) {
            YearMonth yearMonth = (YearMonth) value;
            return LocalDateTime.of(yearMonth.getYear(), yearMonth.getMonthValue(), 1, 0, 0);
        }
        if (value instanceof Year) {
            return LocalDateTime.of(((Year) value).getValue(), 1, 1, 0, 0);
        }
        if (value instanceof Long) {
            return isSecondTs ? LocalDateTimeConvert.fromTimestampSecond((Long) value, zoneId) : LocalDateTimeConvert.fromTimestamp((Long) value, zoneId);
        }
        if (value instanceof String) {
            return LocalDateTimeConvert.fromString((String) value, dateTimeFormatter);
        }
        throw new ShardingPluginException("Unsupported sharding value type `%s`.", value);
    }
}
