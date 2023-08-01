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
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sharding.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.RangeShardingValue;
import org.apache.shardingsphere.sharding.spi.ShardingAlgorithm;
import org.apache.shardingsphere.test.util.PropertiesBuilder;
import org.apache.shardingsphere.test.util.PropertiesBuilder.Property;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Date;
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
import java.util.Collection;
import java.util.LinkedList;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;

class IntervalShardingAlgorithmTest {
    
    private static final DataNodeInfo DATA_NODE_INFO = new DataNodeInfo("t_order_", 6, '0');
    
    private final Collection<String> availableTablesForQuarterDataSources = new LinkedList<>();
    
    private final Collection<String> availableTablesForMonthDataSources = new LinkedList<>();
    
    private final Collection<String> availableTablesForDayDataSources = new LinkedList<>();
    
    private final Collection<String> availableTablesForJDBCDateDataSources = new LinkedList<>();
    
    private final Collection<String> availableTablesForJDBCTimeDataSources = new LinkedList<>();
    
    private final Collection<String> availableTablesForYearDataSources = new LinkedList<>();
    
    private final Collection<String> availableTablesForYearMonthDataSources = new LinkedList<>();
    
    private final Collection<String> availableTablesForMonthInJSR310DataSources = new LinkedList<>();
    
    private final Collection<String> availableTablesForDayWithMillisecondDataSources = new LinkedList<>();
    
    private IntervalShardingAlgorithm shardingAlgorithmByQuarter;
    
    private IntervalShardingAlgorithm shardingAlgorithmByMonth;
    
    private IntervalShardingAlgorithm shardingAlgorithmByDay;
    
    private IntervalShardingAlgorithm shardingAlgorithmByJDBCDate;
    
    private IntervalShardingAlgorithm shardingAlgorithmByJDBCTime;
    
    private IntervalShardingAlgorithm shardingAlgorithmByDayWithMillisecond;
    
    private IntervalShardingAlgorithm shardingAlgorithmByYear;
    
    private IntervalShardingAlgorithm shardingAlgorithmByYearMonth;
    
    private IntervalShardingAlgorithm shardingAlgorithmByMonthInJSR310;
    
    @BeforeEach
    void setup() {
        initShardStrategyByMonth();
        initShardStrategyByQuarter();
        initShardingStrategyByDay();
        initShardStrategyByDayWithMillisecond();
        initShardingStrategyByJDBCDate();
        initShardingStrategyByJDBCTime();
        initShardingStrategyByYear();
        initShardingStrategyByYearMonth();
        initShardingStrategyByMonthInJSR310();
    }
    
    private void initShardStrategyByQuarter() {
        Properties props = PropertiesBuilder.build(
                new Property("datetime-pattern", "yyyy-MM-dd HH:mm:ss"),
                new Property("datetime-lower", "2016-01-01 00:00:00"),
                new Property("datetime-upper", "2021-12-31 00:00:00"),
                new Property("sharding-suffix-pattern", "yyyyQQ"),
                new Property("datetime-interval-amount", "3"),
                new Property("datetime-interval-unit", "Months"));
        shardingAlgorithmByQuarter = (IntervalShardingAlgorithm) TypedSPILoader.getService(ShardingAlgorithm.class, "INTERVAL", props);
        for (int i = 2016; i <= 2020; i++) {
            for (int j = 1; j <= 4; j++) {
                availableTablesForQuarterDataSources.add(String.format("t_order_%04d%02d", i, j));
            }
        }
    }
    
    private void initShardStrategyByMonth() {
        Properties props = PropertiesBuilder.build(
                new Property("datetime-pattern", "yyyy-MM-dd HH:mm:ss"),
                new Property("datetime-lower", "2016-01-01 00:00:00"),
                new Property("datetime-upper", "2021-12-31 00:00:00"),
                new Property("sharding-suffix-pattern", "yyyyMM"),
                new Property("datetime-interval-amount", "1"),
                new Property("datetime-interval-unit", "Months"));
        shardingAlgorithmByMonth = (IntervalShardingAlgorithm) TypedSPILoader.getService(ShardingAlgorithm.class, "INTERVAL", props);
        for (int i = 2016; i <= 2020; i++) {
            for (int j = 1; j <= 12; j++) {
                availableTablesForMonthDataSources.add(String.format("t_order_%04d%02d", i, j));
            }
        }
    }
    
    private void initShardingStrategyByDay() {
        int stepAmount = 2;
        Properties props = PropertiesBuilder.build(
                new Property("datetime-pattern", "yyyy-MM-dd HH:mm:ss"),
                new Property("datetime-lower", "2021-06-01 00:00:00"),
                new Property("datetime-upper", "2021-07-31 00:00:00"),
                new Property("sharding-suffix-pattern", "yyyyMMdd"),
                new Property("datetime-interval-amount", Integer.toString(stepAmount)));
        shardingAlgorithmByDay = (IntervalShardingAlgorithm) TypedSPILoader.getService(ShardingAlgorithm.class, "INTERVAL", props);
        for (int j = 6; j <= 7; j++) {
            for (int i = 1; j == 6 ? i <= 30 : i <= 31; i = i + stepAmount) {
                availableTablesForDayDataSources.add(String.format("t_order_%04d%02d%02d", 2021, j, i));
            }
        }
    }
    
    private void initShardStrategyByDayWithMillisecond() {
        int stepAmount = 2;
        Properties props = PropertiesBuilder.build(
                new Property("datetime-pattern", "yyyy-MM-dd HH:mm:ss.SSS"),
                new Property("datetime-lower", "2021-06-01 00:00:00.000"),
                new Property("datetime-upper", "2021-07-31 00:00:00.000"),
                new Property("sharding-suffix-pattern", "yyyyMMdd"),
                new Property("datetime-interval-amount", Integer.toString(stepAmount)),
                new Property("datetime-interval-unit", "DAYS"));
        shardingAlgorithmByDayWithMillisecond = (IntervalShardingAlgorithm) TypedSPILoader.getService(ShardingAlgorithm.class, "INTERVAL", props);
        for (int j = 6; j <= 7; j++) {
            for (int i = 1; j == 6 ? i <= 30 : i <= 31; i = i + stepAmount) {
                availableTablesForDayWithMillisecondDataSources.add(String.format("t_order_%04d%02d%02d", 2021, j, i));
            }
        }
    }
    
    private void initShardingStrategyByJDBCDate() {
        int stepAmount = 2;
        Properties props = PropertiesBuilder.build(
                new Property("datetime-pattern", "yyyy-MM-dd"),
                new Property("datetime-lower", "2021-06-01"),
                new Property("datetime-upper", "2021-07-31"),
                new Property("sharding-suffix-pattern", "yyyyMMdd"),
                new Property("datetime-interval-amount", Integer.toString(stepAmount)));
        shardingAlgorithmByJDBCDate = (IntervalShardingAlgorithm) TypedSPILoader.getService(ShardingAlgorithm.class, "INTERVAL", props);
        for (int j = 6; j <= 7; j++) {
            for (int i = 1; j == 6 ? i <= 30 : i <= 31; i = i + stepAmount) {
                availableTablesForJDBCDateDataSources.add(String.format("t_order_%04d%02d%02d", 2021, j, i));
            }
        }
    }
    
    private void initShardingStrategyByJDBCTime() {
        int stepAmount = 2;
        Properties props = PropertiesBuilder.build(
                new Property("datetime-pattern", "HH:mm:ss.SSS"),
                new Property("datetime-lower", "02:00:00.000"),
                new Property("datetime-upper", "13:00:00.000"),
                new Property("sharding-suffix-pattern", "HHmm"),
                new Property("datetime-interval-amount", Integer.toString(stepAmount)),
                new Property("datetime-interval-unit", "Hours"));
        shardingAlgorithmByJDBCTime = (IntervalShardingAlgorithm) TypedSPILoader.getService(ShardingAlgorithm.class, "INTERVAL", props);
        for (int i = 2; i < 13; i++) {
            availableTablesForJDBCTimeDataSources.add(String.format("t_order_%02d%02d", i, 0));
        }
    }
    
    private void initShardingStrategyByYear() {
        int stepAmount = 2;
        Properties props = PropertiesBuilder.build(
                new Property("datetime-pattern", "yyyy"),
                new Property("datetime-lower", "2000"),
                new Property("datetime-upper", "2022"),
                new Property("sharding-suffix-pattern", "yyyy"),
                new Property("datetime-interval-amount", Integer.toString(stepAmount)),
                new Property("datetime-interval-unit", "Years"));
        shardingAlgorithmByYear = (IntervalShardingAlgorithm) TypedSPILoader.getService(ShardingAlgorithm.class, "INTERVAL", props);
        for (int i = 2000; i < 2023; i++) {
            availableTablesForYearDataSources.add(String.format("t_order_%04d", i));
        }
    }
    
    private void initShardingStrategyByYearMonth() {
        int stepAmount = 2;
        Properties props = PropertiesBuilder.build(
                new Property("datetime-pattern", "yyyy-MM"),
                new Property("datetime-lower", "2016-01"),
                new Property("datetime-upper", "2021-12"),
                new Property("sharding-suffix-pattern", "yyyyMM"),
                new Property("datetime-interval-amount", Integer.toString(stepAmount)),
                new Property("datetime-interval-unit", "Years"));
        shardingAlgorithmByYearMonth = (IntervalShardingAlgorithm) TypedSPILoader.getService(ShardingAlgorithm.class, "INTERVAL", props);
        for (int i = 2016; i <= 2021; i++) {
            for (int j = 1; j <= 12; j++) {
                availableTablesForYearMonthDataSources.add(String.format("t_order_%04d%02d", i, j));
            }
        }
    }
    
    private void initShardingStrategyByMonthInJSR310() {
        int stepAmount = 2;
        Properties props = PropertiesBuilder.build(
                new Property("datetime-pattern", "MM"),
                new Property("datetime-lower", "02"),
                new Property("datetime-upper", "12"),
                new Property("sharding-suffix-pattern", "MM"),
                new Property("datetime-interval-amount", Integer.toString(stepAmount)),
                new Property("datetime-interval-unit", "Months"));
        shardingAlgorithmByMonthInJSR310 = (IntervalShardingAlgorithm) TypedSPILoader.getService(ShardingAlgorithm.class, "INTERVAL", props);
        for (int i = 2; i < 13; i++) {
            availableTablesForMonthInJSR310DataSources.add(String.format("t_order_%02d", i));
        }
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
        Collection<String> actual = shardingAlgorithmByQuarter.doSharding(availableTablesForQuarterDataSources,
                new RangeShardingValue<>("t_order", "create_time", DATA_NODE_INFO, Range.closed("2019-10-15 10:59:08", "2020-04-08 10:59:08")));
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
    void assertRangeDoShardingByMonth() {
        Collection<String> actual = shardingAlgorithmByMonth.doSharding(availableTablesForMonthDataSources,
                new RangeShardingValue<>("t_order", "create_time", DATA_NODE_INFO, Range.closed("2019-10-15 10:59:08", "2020-04-08 10:59:08")));
        assertThat(actual.size(), is(7));
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
    void assertRangeDoShardingByDay() {
        Collection<String> actual = shardingAlgorithmByDay.doSharding(availableTablesForDayDataSources,
                new RangeShardingValue<>("t_order", "create_time", DATA_NODE_INFO, Range.closed("2021-06-15 00:00:00", "2021-07-31 01:00:00")));
        assertThat(actual.size(), is(24));
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
    void assertTimestampInJDBCTypeWithZeroMillisecond() {
        Collection<String> actualAsLocalDateTime = shardingAlgorithmByDayWithMillisecond.doSharding(availableTablesForDayWithMillisecondDataSources,
                new RangeShardingValue<>("t_order", "create_time", DATA_NODE_INFO,
                        Range.closed(LocalDateTime.of(2021, 6, 15, 2, 25, 27), LocalDateTime.of(2021, 7, 31, 2, 25, 27))));
        assertThat(actualAsLocalDateTime.size(), is(24));
        Collection<String> actualAsInstant = shardingAlgorithmByDayWithMillisecond.doSharding(availableTablesForDayWithMillisecondDataSources,
                new RangeShardingValue<>("t_order", "create_time", DATA_NODE_INFO,
                        Range.closed(
                                LocalDateTime.of(2021, 6, 15, 2, 25, 27).atZone(ZoneId.systemDefault()).toInstant(),
                                LocalDateTime.of(2021, 7, 31, 2, 25, 27).atZone(ZoneId.systemDefault()).toInstant())));
        assertThat(actualAsInstant.size(), is(24));
        Collection<String> actualAsTimestamp = shardingAlgorithmByDayWithMillisecond.doSharding(availableTablesForDayWithMillisecondDataSources,
                new RangeShardingValue<>("t_order", "create_time", DATA_NODE_INFO,
                        Range.closed(
                                Timestamp.valueOf(LocalDateTime.of(2021, 6, 15, 2, 25, 27)),
                                Timestamp.valueOf(LocalDateTime.of(2021, 7, 31, 2, 25, 27)))));
        assertThat(actualAsTimestamp.size(), is(24));
        Collection<String> actualAsOffsetDateTime = shardingAlgorithmByDayWithMillisecond.doSharding(availableTablesForDayWithMillisecondDataSources,
                new RangeShardingValue<>("t_order", "create_time", DATA_NODE_INFO,
                        Range.closed(
                                OffsetDateTime.of(2021, 6, 15, 2, 25, 27, 0, OffsetDateTime.now().getOffset()),
                                OffsetDateTime.of(2021, 7, 31, 2, 25, 27, 0, OffsetDateTime.now().getOffset()))));
        assertThat(actualAsOffsetDateTime.size(), is(24));
        Collection<String> actualAsZonedDateTime = shardingAlgorithmByDayWithMillisecond.doSharding(availableTablesForDayWithMillisecondDataSources,
                new RangeShardingValue<>("t_order", "create_time", DATA_NODE_INFO,
                        Range.closed(
                                ZonedDateTime.of(2021, 6, 15, 2, 25, 27, 0, ZoneId.systemDefault()),
                                ZonedDateTime.of(2021, 7, 31, 2, 25, 27, 0, ZoneId.systemDefault()))));
        assertThat(actualAsZonedDateTime.size(), is(24));
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        Collection<String> actualAsDate = shardingAlgorithmByDayWithMillisecond.doSharding(availableTablesForDayWithMillisecondDataSources,
                new RangeShardingValue<>("t_order", "create_time", DATA_NODE_INFO,
                        Range.closed(Date.from(LocalDate.from(dateTimeFormatter.parse("2021-06-15 02:25:27.000")).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()),
                                Date.from(LocalDate.from(dateTimeFormatter.parse("2021-07-31 02:25:27.000")).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()))));
        assertThat(actualAsDate.size(), is(24));
    }
    
    @Test
    void assertDateInJDBCType() {
        Collection<String> actualAsLocalDate = shardingAlgorithmByJDBCDate.doSharding(availableTablesForJDBCDateDataSources,
                new RangeShardingValue<>("t_order", "create_time", DATA_NODE_INFO,
                        Range.closed(LocalDate.of(2021, 6, 15), LocalDate.of(2021, 7, 31))));
        assertThat(actualAsLocalDate.size(), is(24));
    }
    
    @Test
    void assertTimeInJDBCType() {
        Collection<String> actualAsLocalTime = shardingAlgorithmByJDBCTime.doSharding(availableTablesForJDBCTimeDataSources,
                new RangeShardingValue<>("t_order", "create_time", DATA_NODE_INFO,
                        Range.closed(LocalTime.of(2, 25, 27), LocalTime.of(12, 25, 27))));
        assertThat(actualAsLocalTime.size(), is(6));
        Collection<String> actualAsOffsetTime = shardingAlgorithmByJDBCTime.doSharding(availableTablesForJDBCTimeDataSources,
                new RangeShardingValue<>("t_order", "create_time", DATA_NODE_INFO,
                        Range.closed(OffsetTime.of(2, 25, 27, 0, OffsetDateTime.now().getOffset()),
                                OffsetTime.of(12, 25, 27, 0, OffsetDateTime.now().getOffset()))));
        assertThat(actualAsOffsetTime.size(), is(6));
    }
    
    @Test
    void assertIntegerInJDBCType() {
        Collection<String> actualAsYear = shardingAlgorithmByYear.doSharding(availableTablesForYearDataSources,
                new RangeShardingValue<>("t_order", "create_time", DATA_NODE_INFO, Range.closed(Year.of(2001), Year.of(2013))));
        assertThat(actualAsYear.size(), is(7));
        Collection<String> actualAsYearMonth = shardingAlgorithmByYearMonth.doSharding(availableTablesForYearMonthDataSources,
                new RangeShardingValue<>("t_order", "create_time", DATA_NODE_INFO, Range.closed(YearMonth.of(2016, 1), YearMonth.of(2020, 1))));
        assertThat(actualAsYearMonth.size(), is(3));
        Collection<String> actualAsMonth = shardingAlgorithmByMonthInJSR310.doSharding(availableTablesForMonthInJSR310DataSources,
                new RangeShardingValue<>("t_order", "create_time", DATA_NODE_INFO, Range.closed(Month.of(4), Month.of(10))));
        assertThat(actualAsMonth.size(), is(4));
        Collection<String> actualAsMonthString = shardingAlgorithmByMonthInJSR310.doSharding(availableTablesForMonthInJSR310DataSources,
                new RangeShardingValue<>("t_order", "create_time", DATA_NODE_INFO, Range.closed("04", "10")));
        assertThat(actualAsMonthString.size(), is(4));
    }
    
    @Test
    void assertDateInSqlDate() {
        Collection<String> actualAsLocalDate = shardingAlgorithmByJDBCDate.doSharding(availableTablesForJDBCDateDataSources,
                new RangeShardingValue<>("t_order", "create_time", DATA_NODE_INFO,
                        Range.closed(new Date(LocalDate.of(2021, 6, 15).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()),
                                new Date(LocalDate.of(2021, 7, 31).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()))));
        assertThat(actualAsLocalDate.size(), is(24));
    }
}
