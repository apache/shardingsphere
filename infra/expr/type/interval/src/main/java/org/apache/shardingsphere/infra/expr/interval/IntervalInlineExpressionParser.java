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

package org.apache.shardingsphere.infra.expr.interval;

import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.expr.spi.InlineExpressionParser;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.Year;
import java.time.YearMonth;
import java.time.chrono.Chronology;
import java.time.chrono.IsoChronology;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAccessor;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

/**
 * Interval inline expression parser.
 */
public class IntervalInlineExpressionParser implements InlineExpressionParser {
    
    /**
     * Abbreviation for prefix.
     */
    private static final String PREFIX_KEY = "P";
    
    /**
     * Abbreviation for suffix pattern.
     */
    private static final String SUFFIX_PATTERN_KEY = "SP";
    
    /**
     * Abbreviation for datetime interval amount.
     */
    private static final String INTERVAL_AMOUNT_KEY = "DIA";
    
    /**
     * Abbreviation for datetime interval unit.
     */
    private static final String INTERVAL_UNIT_KEY = "DIU";
    
    /**
     * Abbreviation for datetime lower.
     */
    private static final String DATE_TIME_LOWER_KEY = "DL";
    
    /**
     * Abbreviation for datetime upper.
     */
    private static final String DATE_TIME_UPPER_KEY = "DU";
    
    /**
     * Abbreviation for chronology.
     */
    private static final String CHRONOLOGY_KEY = "C";
    
    private TemporalAccessor startTime;
    
    private TemporalAccessor endTime;
    
    private String prefix;
    
    private DateTimeFormatter dateTimeFormatterForSuffixPattern;
    
    private int stepAmount;
    
    private ChronoUnit stepUnit;
    
    @Override
    public void init(final Properties props) {
        String inlineExpression = props.getProperty(INLINE_EXPRESSION_KEY);
        Map<String, String> propsMap = Arrays.stream(inlineExpression.split(";")).collect(Collectors.toMap(key -> key.split("=")[0], value -> value.split("=")[1]));
        prefix = getPrefix(propsMap);
        dateTimeFormatterForSuffixPattern = getSuffixPattern(propsMap);
        startTime = getDateTimeLower(propsMap);
        endTime = getDateTimeUpper(propsMap);
        stepAmount = getStepAmount(propsMap);
        stepUnit = getStepUnit(propsMap);
    }
    
    private String getPrefix(final Map<String, String> props) {
        ShardingSpherePreconditions.checkContainsKey(props, PREFIX_KEY, () -> new RuntimeException(String.format("%s can not be null.", PREFIX_KEY)));
        return props.get(PREFIX_KEY);
    }
    
    private TemporalAccessor getDateTimeLower(final Map<String, String> props) {
        ShardingSpherePreconditions.checkContainsKey(props, DATE_TIME_LOWER_KEY, () -> new RuntimeException(String.format("%s can not be null.", DATE_TIME_LOWER_KEY)));
        return getDateTime(props.get(DATE_TIME_LOWER_KEY));
    }
    
    private TemporalAccessor getDateTimeUpper(final Map<String, String> props) {
        ShardingSpherePreconditions.checkContainsKey(props, DATE_TIME_UPPER_KEY, () -> new RuntimeException(String.format("%s can not be null.", DATE_TIME_UPPER_KEY)));
        return getDateTime(props.get(DATE_TIME_UPPER_KEY));
    }
    
    private TemporalAccessor getDateTime(final String dateTimeValue) {
        return dateTimeFormatterForSuffixPattern.parse(dateTimeValue);
    }
    
    private DateTimeFormatter getSuffixPattern(final Map<String, String> props) {
        String suffix = props.get(SUFFIX_PATTERN_KEY);
        ShardingSpherePreconditions.checkNotEmpty(suffix, () -> new RuntimeException(String.format("%s can not be null or empty.", SUFFIX_PATTERN_KEY)));
        Chronology chronology = getChronology(props);
        return DateTimeFormatter.ofPattern(suffix).withChronology(chronology);
    }
    
    private int getStepAmount(final Map<String, String> props) {
        ShardingSpherePreconditions.checkContainsKey(props, INTERVAL_AMOUNT_KEY, () -> new RuntimeException(String.format("%s can not be null.", INTERVAL_AMOUNT_KEY)));
        return Integer.parseInt(props.get(INTERVAL_AMOUNT_KEY));
    }
    
    private ChronoUnit getStepUnit(final Map<String, String> props) {
        ShardingSpherePreconditions.checkContainsKey(props, INTERVAL_UNIT_KEY, () -> new RuntimeException(String.format("%s can not be null.", INTERVAL_UNIT_KEY)));
        String stepUnit = props.get(INTERVAL_UNIT_KEY);
        return Arrays.stream(ChronoUnit.values())
                .filter(chronoUnit -> chronoUnit.toString().equals(stepUnit))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(String.format("Cannot find step unit for specified %s property: `%s`", INTERVAL_UNIT_KEY, stepUnit)));
    }
    
    private Chronology getChronology(final Map<String, String> props) {
        if (props.containsKey(CHRONOLOGY_KEY)) {
            String chronology = props.get(CHRONOLOGY_KEY);
            return Chronology.getAvailableChronologies()
                    .stream()
                    .filter(chronologyInstance -> chronologyInstance.getId().equals(chronology))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException(String.format("Cannot find chronology for specified %s property: `%s`", CHRONOLOGY_KEY, chronology)));
        }
        return IsoChronology.INSTANCE;
    }
    
    @Override
    public List<String> splitAndEvaluate() {
        TemporalAccessor calculateTime = startTime;
        if (!calculateTime.isSupported(ChronoField.NANO_OF_DAY)) {
            if (calculateTime.isSupported(ChronoField.EPOCH_DAY)) {
                return convertStringFromTemporal(startTime.query(LocalDate::from), endTime.query(LocalDate::from));
            }
            if (calculateTime.isSupported(ChronoField.YEAR) && calculateTime.isSupported(ChronoField.MONTH_OF_YEAR)) {
                return convertStringFromTemporal(startTime.query(YearMonth::from), endTime.query(YearMonth::from));
            }
            if (calculateTime.isSupported(ChronoField.YEAR)) {
                return convertStringFromTemporal(startTime.query(Year::from), endTime.query(Year::from));
            }
            if (calculateTime.isSupported(ChronoField.MONTH_OF_YEAR)) {
                return convertStringFromMonth();
            }
        }
        if (!calculateTime.isSupported(ChronoField.EPOCH_DAY)) {
            return convertStringFromTemporal(startTime.query(LocalTime::from), endTime.query(LocalTime::from));
        }
        return convertStringFromTemporal(startTime.query(LocalDateTime::from), endTime.query(LocalDateTime::from));
    }
    
    private List<String> convertStringFromMonth() {
        Month startTimeAsMonth = startTime.query(Month::from);
        Month endTimeAsMonth = endTime.query(Month::from);
        return LongStream.iterate(0L, x -> x + stepAmount)
                .limit(((endTimeAsMonth.getValue() - startTimeAsMonth.getValue()) / stepAmount) + 1L)
                .parallel()
                .boxed()
                .map(startTimeAsMonth::plus)
                .map(dateTimeFormatterForSuffixPattern::format)
                .map(suffix -> prefix + suffix)
                .collect(Collectors.toList());
    }
    
    private List<String> convertStringFromTemporal(final Temporal startTimeAsTemporal, final Temporal endTimeAsTemporal) {
        return LongStream.iterate(0L, x -> x + stepAmount)
                .limit(stepUnit.between(startTimeAsTemporal, endTimeAsTemporal) / stepAmount + 1L)
                .parallel()
                .boxed()
                .map(arithmeticSequence -> startTimeAsTemporal.plus(arithmeticSequence, stepUnit))
                .map(dateTimeFormatterForSuffixPattern::format)
                .map(suffix -> prefix + suffix)
                .collect(Collectors.toList());
    }
    
    @Override
    public String getType() {
        return "INTERVAL";
    }
}
