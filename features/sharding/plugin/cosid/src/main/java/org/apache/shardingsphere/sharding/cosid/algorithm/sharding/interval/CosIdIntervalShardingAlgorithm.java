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

package org.apache.shardingsphere.sharding.cosid.algorithm.sharding.interval;

import me.ahoo.cosid.util.LocalDateTimeConvert;
import org.apache.shardingsphere.sharding.cosid.algorithm.CosIdAlgorithmConstants;
import org.apache.shardingsphere.sharding.exception.ShardingPluginException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Properties;

/**
 * Interval range sharding algorithm with CosId.
 */
public final class CosIdIntervalShardingAlgorithm extends AbstractCosIdIntervalShardingAlgorithm<Comparable<?>> {
    
    private static final String DATE_TIME_PATTERN_KEY = "datetime-pattern";
    
    private static final String TIMESTAMP_SECOND_UNIT = "SECOND";
    
    private static final String TIMESTAMP_UNIT_KEY = "ts-unit";
    
    private boolean isSecondTs;
    
    private DateTimeFormatter dateTimeFormatter;
    
    @Override
    public void init(final Properties props) {
        super.init(props);
        isSecondTs = getIsSecondTs(props);
        dateTimeFormatter = getDateTimeFormatter(props);
    }
    
    private boolean getIsSecondTs(final Properties props) {
        return props.containsKey(TIMESTAMP_UNIT_KEY) && TIMESTAMP_SECOND_UNIT.equalsIgnoreCase(props.getProperty(TIMESTAMP_UNIT_KEY));
    }
    
    private DateTimeFormatter getDateTimeFormatter(final Properties props) {
        return DateTimeFormatter.ofPattern(props.getProperty(DATE_TIME_PATTERN_KEY, DEFAULT_DATE_TIME_PATTERN));
    }
    
    @SuppressWarnings("checkstyle:CyclomaticComplexity")
    @Override
    protected LocalDateTime convertShardingValue(final Comparable<?> shardingValue) {
        if (shardingValue instanceof LocalDateTime) {
            return (LocalDateTime) shardingValue;
        }
        if (shardingValue instanceof ZonedDateTime) {
            return ((ZonedDateTime) shardingValue).toLocalDateTime();
        }
        if (shardingValue instanceof OffsetDateTime) {
            return ((OffsetDateTime) shardingValue).toLocalDateTime();
        }
        if (shardingValue instanceof Instant) {
            return LocalDateTimeConvert.fromInstant((Instant) shardingValue, getZoneId());
        }
        if (shardingValue instanceof LocalDate) {
            return LocalDateTime.of((LocalDate) shardingValue, LocalTime.MIN);
        }
        if (shardingValue instanceof Date) {
            return LocalDateTimeConvert.fromDate((Date) shardingValue, getZoneId());
        }
        if (shardingValue instanceof YearMonth) {
            YearMonth yearMonth = (YearMonth) shardingValue;
            return LocalDateTime.of(yearMonth.getYear(), yearMonth.getMonthValue(), 1, 0, 0);
        }
        if (shardingValue instanceof Year) {
            return LocalDateTime.of(((Year) shardingValue).getValue(), 1, 1, 0, 0);
        }
        if (shardingValue instanceof Long) {
            return isSecondTs ? LocalDateTimeConvert.fromTimestampSecond((Long) shardingValue, getZoneId()) : LocalDateTimeConvert.fromTimestamp((Long) shardingValue, getZoneId());
        }
        if (shardingValue instanceof String) {
            return LocalDateTimeConvert.fromString((String) shardingValue, dateTimeFormatter);
        }
        throw new ShardingPluginException("Unsupported sharding value type `%s`.", shardingValue);
    }
    
    @Override
    public String getType() {
        return CosIdAlgorithmConstants.TYPE_PREFIX + "INTERVAL";
    }
}
