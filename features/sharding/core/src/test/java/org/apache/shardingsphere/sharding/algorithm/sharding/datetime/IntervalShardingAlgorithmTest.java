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

import com.google.common.collect.Range;
import org.apache.shardingsphere.infra.datanode.DataNodeInfo;
import org.apache.shardingsphere.infra.exception.generic.UnsupportedSQLOperationException;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.util.datetime.DateTimeFormatterFactory;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder.Property;
import org.apache.shardingsphere.sharding.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.RangeShardingValue;
import org.apache.shardingsphere.sharding.exception.data.InvalidDatetimeFormatException;
import org.apache.shardingsphere.sharding.spi.ShardingAlgorithm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.UnsupportedTemporalTypeException;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class IntervalShardingAlgorithmTest {
    
    private static final DataNodeInfo DATA_NODE_INFO = new DataNodeInfo("t_order_", 6, '0');
    
    private final Collection<String> availableTablesForQuarterDataSources = new LinkedList<>();
    
    private final Collection<String> availableTablesForMonthDataSources = new LinkedList<>();
    
    private final Collection<String> availableTablesForDayDataSources = new LinkedList<>();
    
    private IntervalShardingAlgorithm shardingAlgorithmByQuarter;
    
    private IntervalShardingAlgorithm shardingAlgorithmByMonth;
    
    private IntervalShardingAlgorithm shardingAlgorithmByDay;
    
    @BeforeEach
    void setup() {
        initShardStrategyByMonth();
        initShardStrategyByQuarter();
        initShardingStrategyByDay();
    }
    
    private void initShardStrategyByQuarter() {
        shardingAlgorithmByQuarter = createAlgorithm("yyyy-MM-dd HH:mm:ss", "2016-01-01 00:00:00",
                "2021-12-31 00:00:00", "yyyyQQ", 3, "Months");
        for (int i = 2016; i <= 2020; i++) {
            for (int j = 1; j <= 4; j++) {
                availableTablesForQuarterDataSources.add(String.format("t_order_%04d%02d", i, j));
            }
        }
    }
    
    private void initShardStrategyByMonth() {
        shardingAlgorithmByMonth = createAlgorithm("yyyy-MM-dd HH:mm:ss", "2016-01-01 00:00:00",
                "2021-12-31 00:00:00", "yyyyMM", 1, "Months");
        for (int i = 2016; i <= 2020; i++) {
            for (int j = 1; j <= 12; j++) {
                availableTablesForMonthDataSources.add(String.format("t_order_%04d%02d", i, j));
            }
        }
    }
    
    private void initShardingStrategyByDay() {
        int stepAmount = 2;
        shardingAlgorithmByDay = createAlgorithm("yyyy-MM-dd HH:mm:ss", "2021-06-01 00:00:00",
                "2021-07-31 00:00:00", "yyyyMMdd", stepAmount, null);
        for (int j = 6; j <= 7; j++) {
            for (int i = 1; j == 6 ? i <= 30 : i <= 31; i = i + stepAmount) {
                availableTablesForDayDataSources.add(String.format("t_order_%04d%02d%02d", 2021, j, i));
            }
        }
    }
    
    @Test
    void assertInitFailedWithInvalidDatetimeFormat() {
        assertThrows(InvalidDatetimeFormatException.class,
                () -> TypedSPILoader.getService(ShardingAlgorithm.class, "INTERVAL", PropertiesBuilder.build(new Property("datetime-pattern", "yyyy"), new Property("datetime-lower", "invalid"))));
    }
    
    @Test
    void assertInitFailedWithInvalidStepUnit() {
        assertThrows(UnsupportedSQLOperationException.class, () -> TypedSPILoader.getService(ShardingAlgorithm.class, "INTERVAL", PropertiesBuilder.build(
                new Property("datetime-pattern", "yy"), new Property("datetime-lower", "16"), new Property("sharding-suffix-pattern", "yy"), new Property("datetime-interval-unit", "invalid"))));
    }
    
    @Test
    void assertPreciseDoShardingByQuarter() {
        assertThat(shardingAlgorithmByQuarter.doSharding(availableTablesForQuarterDataSources,
                new PreciseShardingValue<>("t_order", "create_time", DATA_NODE_INFO, "2020-01-01 00:00:01")), is("t_order_202001"));
        assertThat(shardingAlgorithmByQuarter.doSharding(availableTablesForQuarterDataSources,
                new PreciseShardingValue<>("t_order", "create_time", DATA_NODE_INFO, "2020-02-01 00:00:01")), is("t_order_202001"));
    }
    
    @Test
    void assertRangeDoShardingByQuarter() {
        Collection<String> actual = shardingAlgorithmByQuarter.doSharding(availableTablesForQuarterDataSources, createShardingValue("2019-10-15 10:59:08", "2020-04-08 10:59:08"));
        assertThat(actual.size(), is(3));
    }
    
    @Test
    void assertPreciseDoShardingByMonth() {
        assertThat(shardingAlgorithmByMonth.doSharding(availableTablesForMonthDataSources,
                new PreciseShardingValue<>("t_order", "create_time", DATA_NODE_INFO, "2020-01-01 00:00:01")), is("t_order_202001"));
        assertNull(shardingAlgorithmByMonth.doSharding(availableTablesForMonthDataSources,
                new PreciseShardingValue<>("t_order", "create_time", DATA_NODE_INFO, "2030-01-01 00:00:01")));
    }
    
    @Test
    void assertLowerHalfRangeDoSharding() {
        Collection<String> actual = shardingAlgorithmByQuarter.doSharding(availableTablesForQuarterDataSources,
                new RangeShardingValue<>("t_order", "create_time", DATA_NODE_INFO, Range.atLeast("2018-10-15 10:59:08")));
        assertThat(actual.size(), is(9));
    }
    
    @Test
    void assertUpperHalfRangeDoSharding() {
        Collection<String> actual = shardingAlgorithmByQuarter.doSharding(availableTablesForQuarterDataSources,
                new RangeShardingValue<>("t_order", "create_time", DATA_NODE_INFO, Range.atMost("2019-09-01 00:00:00")));
        assertThat(actual.size(), is(15));
    }
    
    @Test
    void assertLowerHalfRangeDoShardingByDay() {
        Collection<String> actual = shardingAlgorithmByDay.doSharding(availableTablesForDayDataSources,
                new RangeShardingValue<>("t_order", "create_time", DATA_NODE_INFO, Range.atLeast("2021-01-01 00:00:00")));
        assertThat(actual.size(), is(31));
    }
    
    @Test
    void assertUpperHalfRangeDoShardingByDay() {
        Collection<String> actual = shardingAlgorithmByDay.doSharding(availableTablesForDayDataSources,
                new RangeShardingValue<>("t_order", "create_time", DATA_NODE_INFO, Range.atMost("2021-07-31 01:00:00")));
        assertThat(actual.size(), is(31));
    }
    
    @Test
    void assertPreciseDoShardingByDay() {
        assertThat(shardingAlgorithmByDay.doSharding(availableTablesForDayDataSources,
                new PreciseShardingValue<>("t_order", "create_time", DATA_NODE_INFO, "2021-07-01 00:00:01")), is("t_order_20210701"));
        assertThat(shardingAlgorithmByDay.doSharding(availableTablesForDayDataSources,
                new PreciseShardingValue<>("t_order", "create_time", DATA_NODE_INFO, "2021-07-02 00:00:01")), is("t_order_20210701"));
    }
    
    @Test
    void assertFormat() {
        String inputFormat = "yyyy-MM-dd HH:mm:ss.SSS";
        String tableFormatByQuarter = "yyyyQQ";
        String tableFormatByMonth = "yyyyMM";
        String value = "2020-10-11 00:00:00.000000";
        LocalDateTime localDateTime = LocalDateTime.parse(value.substring(0, inputFormat.length()), DateTimeFormatter.ofPattern(inputFormat));
        String tableNameShardedByQuarter = localDateTime.format(DateTimeFormatter.ofPattern(tableFormatByQuarter));
        String tableNameShardedByMonth = localDateTime.format(DateTimeFormatter.ofPattern(tableFormatByMonth));
        assertThat(tableNameShardedByQuarter, is("202004"));
        assertThat(tableNameShardedByMonth, is("202010"));
    }
    
    @Test
    void assertRangeDoShardingByDays() {
        final int expectSize = 24;
        final int stepAmount = 2;
        IntervalShardingAlgorithm algorithm = createAlgorithm("yyyy-MM-dd HH:mm:ss.SSS", "2021-06-01 00:00:00.000",
                "2021-07-31 00:00:00.000", "yyyyMMdd", stepAmount, "DAYS");
        Collection<String> availableTargetNames = new LinkedList<>();
        for (int j = 6; j <= 7; j++) {
            for (int i = 1; j == 6 ? i <= 30 : i <= 31; i = i + stepAmount) {
                availableTargetNames.add(String.format("t_order_%04d%02d%02d", 2021, j, i));
            }
        }
        final LocalDateTime lower = LocalDateTime.of(2021, 6, 15, 2, 25, 27, 0);
        final LocalDateTime upper = LocalDateTime.of(2021, 7, 31, 2, 25, 27, 0);
        final RangeShardingValue<Comparable<?>> shardingValueAsLocalDateTime = createShardingValue(lower, upper);
        assertThat(algorithm.doSharding(availableTargetNames, shardingValueAsLocalDateTime).size(), is(expectSize));
        final RangeShardingValue<Comparable<?>> shardingValueAsInstant = createShardingValue(
                lower.atZone(ZoneId.systemDefault()).toInstant(),
                upper.atZone(ZoneId.systemDefault()).toInstant());
        assertThat(algorithm.doSharding(availableTargetNames, shardingValueAsInstant).size(), is(expectSize));
        final RangeShardingValue<Comparable<?>> shardingValueAsTimestamp = createShardingValue(Timestamp.valueOf(lower), Timestamp.valueOf(upper));
        assertThat(algorithm.doSharding(availableTargetNames, shardingValueAsTimestamp).size(), is(expectSize));
        final RangeShardingValue<Comparable<?>> shardingValueAsOffsetDateTime = createShardingValue(
                OffsetDateTime.of(lower, OffsetDateTime.now().getOffset()),
                OffsetDateTime.of(upper, OffsetDateTime.now().getOffset()));
        assertThat(algorithm.doSharding(availableTargetNames, shardingValueAsOffsetDateTime).size(), is(expectSize));
        final RangeShardingValue<Comparable<?>> shardingValueAsZonedDateTime = createShardingValue(
                ZonedDateTime.of(lower, ZoneId.systemDefault()),
                ZonedDateTime.of(upper, ZoneId.systemDefault()));
        assertThat(algorithm.doSharding(availableTargetNames, shardingValueAsZonedDateTime).size(), is(expectSize));
        final RangeShardingValue<Comparable<?>> shardingValueAsUtilDate = createShardingValue(
                Date.from(lower.atZone(ZoneId.systemDefault()).toInstant()),
                Date.from(upper.atZone(ZoneId.systemDefault()).toInstant()));
        assertThat(algorithm.doSharding(availableTargetNames, shardingValueAsUtilDate).size(), is(expectSize));
        final RangeShardingValue<Comparable<?>> shardingValueAsSqlDate = createShardingValue(
                new java.sql.Date(lower.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()),
                new java.sql.Date(upper.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()));
        assertThrows(UnsupportedTemporalTypeException.class, () -> algorithm.doSharding(availableTargetNames, shardingValueAsSqlDate),
                "SQL Date values do not have a time component.");
        assertThat(createAlgorithm("yyyy-MM-dd", "2021-06-01",
                "2021-07-31", "yyyyMMdd", stepAmount, null)
                .doSharding(availableTargetNames, shardingValueAsSqlDate).size(),
                is(expectSize));
        final RangeShardingValue<Comparable<?>> shardingValueAsString = createShardingValue(
                DateTimeFormatterFactory.getDatetimeFormatter().format(lower),
                DateTimeFormatterFactory.getDatetimeFormatter().format(upper));
        assertThat(algorithm.doSharding(availableTargetNames, shardingValueAsString).size(), is(expectSize));
        assertThat(shardingAlgorithmByDay.doSharding(availableTablesForDayDataSources, shardingValueAsString).size(), is(expectSize));
    }
    
    @Test
    void assertRangeDoShardingByDaysInLocalDate() {
        int stepAmount = 2;
        Collection<String> availableTargetNames = new LinkedList<>();
        for (int j = 6; j <= 7; j++) {
            for (int i = 1; j == 6 ? i <= 30 : i <= 31; i = i + stepAmount) {
                availableTargetNames.add(String.format("t_order_%04d%02d%02d", 2021, j, i));
            }
        }
        Collection<String> actualAsLocalDate = createAlgorithm("yyyy-MM-dd", "2021-06-01",
                "2021-07-31", "yyyyMMdd", stepAmount, null)
                .doSharding(availableTargetNames,
                        createShardingValue(LocalDate.of(2021, 6, 15), LocalDate.of(2021, 7, 31)));
        assertThat(actualAsLocalDate.size(), is(24));
    }
    
    @Test
    void assertRangeDoShardingByHours() {
        int stepAmount = 2;
        IntervalShardingAlgorithm algorithm = createAlgorithm("HH:mm:ss.SSS", "02:00:00.000",
                "13:00:00.000", "HHmm", stepAmount, "Hours");
        Collection<String> availableTablesForJDBCTimeDataSources = new LinkedList<>();
        for (int i = 2; i < 13; i++) {
            availableTablesForJDBCTimeDataSources.add(String.format("t_order_%02d%02d", i, 0));
        }
        Collection<String> actualAsLocalTime = algorithm.doSharding(availableTablesForJDBCTimeDataSources,
                createShardingValue(LocalTime.of(2, 25, 27), LocalTime.of(12, 25, 27)));
        assertThat(actualAsLocalTime.size(), is(6));
        Collection<String> actualAsOffsetTime = algorithm.doSharding(availableTablesForJDBCTimeDataSources,
                createShardingValue(OffsetTime.of(2, 25, 27, 0, OffsetDateTime.now().getOffset()),
                        OffsetTime.of(12, 25, 27, 0, OffsetDateTime.now().getOffset())));
        assertThat(actualAsOffsetTime.size(), is(6));
    }
    
    @Test
    void assertRangeDoShardingByYears() {
        Collection<String> availableTargetNames = new LinkedList<>();
        for (int i = 2000; i < 2023; i++) {
            availableTargetNames.add(String.format("t_order_%04d", i));
        }
        Collection<String> actual = createAlgorithm("yyyy", "2000",
                "2022", "yyyy", 2, "Years")
                .doSharding(availableTargetNames, createShardingValue(Year.of(2001), Year.of(2013)));
        assertThat(actual.size(), is(7));
    }
    
    @Test
    void assertRangeDoShardingByYearsInYearMonth() {
        Collection<String> availableTargetNames = new LinkedList<>();
        for (int i = 2016; i <= 2021; i++) {
            for (int j = 1; j <= 12; j++) {
                availableTargetNames.add(String.format("t_order_%04d%02d", i, j));
            }
        }
        Collection<String> actualAsYearMonth = createAlgorithm("yyyy-MM", "2016-01",
                "2021-12", "yyyyMM", 2, "Years")
                .doSharding(availableTargetNames,
                        createShardingValue(YearMonth.of(2016, 1), YearMonth.of(2020, 1)));
        assertThat(actualAsYearMonth.size(), is(3));
    }
    
    @Test
    void assertRangeDoShardingByMonths() {
        IntervalShardingAlgorithm algorithm = createAlgorithm("MM", "02",
                "12", "MM", 2, "Months");
        Collection<String> availableTargetNames = new LinkedList<>();
        for (int i = 2; i < 13; i++) {
            availableTargetNames.add(String.format("t_order_%02d", i));
        }
        Collection<String> actual = algorithm.doSharding(availableTargetNames, createShardingValue(Month.of(4), Month.of(10)));
        assertThat(actual.size(), is(4));
        Collection<String> actualAsMonthString = algorithm.doSharding(availableTargetNames, createShardingValue("04", "10"));
        assertThat(actualAsMonthString.size(), is(4));
        Collection<String> actualAsString = shardingAlgorithmByMonth.doSharding(availableTablesForMonthDataSources,
                createShardingValue("2019-10-15 10:59:08", "2020-04-08 10:59:08"));
        assertThat(actualAsString.size(), is(7));
    }
    
    private IntervalShardingAlgorithm createAlgorithm(final String datetimePattern, final String datetimeLower,
                                                      final String datetimeUpper, final String shardingSuffixPattern,
                                                      final Integer datetimeIntervalAmount, final String datetimeIntervalUnit) {
        Properties props = PropertiesBuilder.build(
                new Property("datetime-pattern", datetimePattern),
                new Property("datetime-lower", datetimeLower),
                new Property("datetime-upper", datetimeUpper),
                new Property("sharding-suffix-pattern", shardingSuffixPattern),
                new Property("datetime-interval-amount", Integer.toString(datetimeIntervalAmount)));
        if (null != datetimeIntervalUnit) {
            props.setProperty("datetime-interval-unit", datetimeIntervalUnit);
        }
        return (IntervalShardingAlgorithm) TypedSPILoader.getService(ShardingAlgorithm.class, "INTERVAL", props);
    }
    
    private RangeShardingValue<Comparable<?>> createShardingValue(final Comparable<?> lower, final Comparable<?> upper) {
        return new RangeShardingValue<>("t_order", "create_time", DATA_NODE_INFO, Range.closed(lower, upper));
    }
}
