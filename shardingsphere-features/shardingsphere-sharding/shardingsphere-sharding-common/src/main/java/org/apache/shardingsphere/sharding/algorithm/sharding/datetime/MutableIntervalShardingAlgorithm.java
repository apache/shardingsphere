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

package org.apache.shardingsphere.sharding.algorithm.sharding.datetime;

import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.infra.config.exception.ShardingSphereConfigurationException;
import org.apache.shardingsphere.sharding.algorithm.sharding.ShardingAlgorithmException;
import org.apache.shardingsphere.sharding.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.RangeShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.StandardShardingAlgorithm;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.IsoFields;
import java.time.temporal.TemporalField;
import java.util.Collection;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Mutable interval sharding algorithm that adapt various shard method by define properties below.
 *
 * <p>properties defined here:
 *
 * <p>datetime.pattern: the datetime format used by applications, must can be transformed to {@link LocalDateTime},
 * used by {@link LocalDateTime#parse(CharSequence, DateTimeFormatter)}.
 *
 * <p>table.suffix.pattern: suffix for sharded tables, used by {@link LocalDateTime#format(DateTimeFormatter)},
 * examples:
 * suffix=yyyyQQ means shard by {@link IsoFields#QUARTER_OF_YEAR};
 * suffix=yyyyMM means shard by {@link ChronoUnit#MONTHS};
 * suffix=yyyyMMdd means shard by {@link ChronoField#DAY_OF_YEAR}.
 *
 * <p>detail explain for each char in datetime.pattern and table.suffix.pattern can refer {@link TemporalField}.
 *
 * <p>datetime.lower and datetime.upper: if app query with only half bound, lower and upper helps to build other half bound,
 * datetime.lower must be specified and datetime.upper has a default value to {@link LocalDateTime#now}
 * (default value of datetime.upper could only be used when query sql needn't get result that time larger than query time).
 *
 * <p>datetime.step.unit and datetime.step.amount used for calculate tables for range shard, datetime.step.unit is name of
 * {@link ChronoUnit}, default unit is Days and amount is 1, amount + unit should not be larger than but close to your shard range.
 *
 * <p>examples: when shard by {@link IsoFields#QUARTER_OF_YEAR}, datetime.step.unit = Months and datetime.step.amount = 3 is a better choice.
 */
public final class MutableIntervalShardingAlgorithm implements StandardShardingAlgorithm<Comparable<?>> {
    
    private static final String DATE_TIME_PATTERN_KEY = "datetime.pattern";
    
    private static final String DATE_TIME_LOWER_KEY = "datetime.lower";
    
    private static final String DATE_TIME_UPPER_KEY = "datetime.upper";
    
    private static final String TABLE_SUFFIX_FORMAT_KEY = "table.suffix.pattern";
    
    private static final String STEP_AMOUNT_KEY = "datetime.step.amount";
    
    private static final String STEP_UNIT_KEY = "datetime.step.unit";
    
    @Getter
    @Setter
    private Properties props = new Properties();
    
    private DateTimeFormatter dateTimeFormatter;
    
    private int dateTimePatternLength;
    
    private LocalDateTime dateTimeLower;
    
    private LocalDateTime dateTimeUpper;
    
    private DateTimeFormatter tableSuffixPattern;
    
    private int stepAmount;
    
    private ChronoUnit stepUnit;
    
    @Override
    public void init() {
        String dateTimePattern = getDateTimePattern();
        dateTimeFormatter = DateTimeFormatter.ofPattern(dateTimePattern);
        dateTimePatternLength = dateTimePattern.length();
        dateTimeLower = getDateTimeLower(dateTimePattern);
        dateTimeUpper = getDateTimeUpper(dateTimePattern);
        tableSuffixPattern = getTableSuffixPattern();
        stepAmount = Integer.parseInt(props.getOrDefault(STEP_AMOUNT_KEY, 1).toString());
        stepUnit = props.containsKey(STEP_UNIT_KEY) ? getStepUnit(props.getProperty(STEP_UNIT_KEY)) : ChronoUnit.DAYS;
    }
    
    private String getDateTimePattern() {
        Preconditions.checkArgument(props.containsKey(DATE_TIME_PATTERN_KEY), "% can not be null.", DATE_TIME_PATTERN_KEY);
        return props.getProperty(DATE_TIME_PATTERN_KEY);
    }
    
    private LocalDateTime getDateTimeLower(final String dateTimePattern) {
        Preconditions.checkArgument(props.containsKey(DATE_TIME_LOWER_KEY), "% can not be null.", DATE_TIME_LOWER_KEY);
        return getDateTime(DATE_TIME_LOWER_KEY, props.getProperty(DATE_TIME_LOWER_KEY), dateTimePattern);
    }
    
    private LocalDateTime getDateTimeUpper(final String dateTimePattern) {
        return props.containsKey(DATE_TIME_UPPER_KEY) ? getDateTime(DATE_TIME_UPPER_KEY, props.getProperty(DATE_TIME_UPPER_KEY), dateTimePattern) : LocalDateTime.now();
    }
    
    private LocalDateTime getDateTime(final String dateTimeKey, final String dateTimeValue, final String dateTimePattern) {
        try {
            return LocalDateTime.parse(dateTimeValue, dateTimeFormatter);
        } catch (final DateTimeParseException ex) {
            throw new ShardingSphereConfigurationException("Invalid %s, datetime pattern should be `%s`, value is `%s`", dateTimeKey, dateTimePattern, dateTimeValue);
        }
    }
    
    private DateTimeFormatter getTableSuffixPattern() {
        Preconditions.checkArgument(props.containsKey(TABLE_SUFFIX_FORMAT_KEY), "% can not be null.", TABLE_SUFFIX_FORMAT_KEY);
        return DateTimeFormatter.ofPattern(props.getProperty(TABLE_SUFFIX_FORMAT_KEY));
    }
    
    private ChronoUnit getStepUnit(final String stepUnit) {
        for (ChronoUnit each : ChronoUnit.values()) {
            if (each.toString().equalsIgnoreCase(stepUnit)) {
                return each;
            }
        }
        throw new UnsupportedOperationException(String.format("Cannot find step unit for specified %s property: `%s`", STEP_UNIT_KEY, stepUnit));
    }
    
    @Override
    public String doSharding(final Collection<String> availableTargetNames, final PreciseShardingValue<Comparable<?>> shardingValue) {
        return availableTargetNames.stream()
                .filter(each -> each.endsWith(parseDateTime(shardingValue.getValue().toString()).format(tableSuffixPattern)))
                .findFirst().orElseThrow(() -> new ShardingAlgorithmException(String.format("failed to shard value %s, and availableTables %s", shardingValue, availableTargetNames)));
    }
    
    @Override
    public Collection<String> doSharding(final Collection<String> availableTargetNames, final RangeShardingValue<Comparable<?>> shardingValue) {
        boolean hasStartTime = shardingValue.getValueRange().hasLowerBound();
        boolean hasEndTime = shardingValue.getValueRange().hasUpperBound();
        if (!hasStartTime && !hasEndTime) {
            return availableTargetNames;
        }
        LocalDateTime startTime = hasStartTime ? parseDateTime(shardingValue.getValueRange().lowerEndpoint().toString()) : dateTimeLower;
        LocalDateTime endTime = hasEndTime ? parseDateTime(shardingValue.getValueRange().upperEndpoint().toString()) : dateTimeUpper;
        LocalDateTime calculateTime = startTime;
        Set<String> result = new HashSet<>();
        while (!calculateTime.isAfter(endTime)) {
            result.addAll(getMatchedTables(calculateTime, availableTargetNames));
            calculateTime = calculateTime.plus(stepAmount, stepUnit);
        }
        result.addAll(getMatchedTables(endTime, availableTargetNames));
        return result;
    }
    
    private LocalDateTime parseDateTime(final String value) {
        return LocalDateTime.parse(value.substring(0, dateTimePatternLength), dateTimeFormatter);
    }
    
    private Collection<String> getMatchedTables(final LocalDateTime dateTime, final Collection<String> availableTargetNames) {
        String tableSuffix = dateTime.format(tableSuffixPattern);
        return availableTargetNames.parallelStream().filter(each -> each.endsWith(tableSuffix)).collect(Collectors.toSet());
    }
    
    @Override
    public String getType() {
        return "MUTABLE_INTERVAL";
    }
}
