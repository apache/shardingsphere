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
import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.datanode.DataNodeInfo;
import org.apache.shardingsphere.sharding.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.RangeShardingValue;
import org.apache.shardingsphere.sharding.factory.ShardingAlgorithmFactory;
import org.junit.Before;
import org.junit.Test;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public final class IntervalShardingAlgorithmTest {
    
    public static final DataNodeInfo DATA_NODE_INFO = new DataNodeInfo("t_order_", 6, '0');
    
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
    
    @Before
    public void setup() {
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
        shardingAlgorithmByQuarter = (IntervalShardingAlgorithm) ShardingAlgorithmFactory.newInstance(new AlgorithmConfiguration("INTERVAL", createQuarterProperties()));
        for (int i = 2016; i <= 2020; i++) {
            for (int j = 1; j <= 4; j++) {
                availableTablesForQuarterDataSources.add(String.format("t_order_%04d%02d", i, j));
            }
        }
    }
    
    private Properties createQuarterProperties() {
        Properties result = new Properties();
        result.setProperty("datetime-pattern", "yyyy-MM-dd HH:mm:ss");
        result.setProperty("datetime-lower", "2016-01-01 00:00:00");
        result.setProperty("datetime-upper", "2021-12-31 00:00:00");
        result.setProperty("sharding-suffix-pattern", "yyyyQQ");
        result.setProperty("datetime-interval-amount", "3");
        result.setProperty("datetime-interval-unit", "Months");
        return result;
    }
    
    private void initShardStrategyByMonth() {
        shardingAlgorithmByMonth = (IntervalShardingAlgorithm) ShardingAlgorithmFactory.newInstance(new AlgorithmConfiguration("INTERVAL", createMonthProperties()));
        for (int i = 2016; i <= 2020; i++) {
            for (int j = 1; j <= 12; j++) {
                availableTablesForMonthDataSources.add(String.format("t_order_%04d%02d", i, j));
            }
        }
    }
    
    private Properties createMonthProperties() {
        Properties result = new Properties();
        result.setProperty("datetime-pattern", "yyyy-MM-dd HH:mm:ss");
        result.setProperty("datetime-lower", "2016-01-01 00:00:00");
        result.setProperty("datetime-upper", "2021-12-31 00:00:00");
        result.setProperty("sharding-suffix-pattern", "yyyyMM");
        result.setProperty("datetime-interval-amount", "1");
        result.setProperty("datetime-interval-unit", "Months");
        return result;
    }
    
    private void initShardingStrategyByDay() {
        int stepAmount = 2;
        shardingAlgorithmByDay = (IntervalShardingAlgorithm) ShardingAlgorithmFactory.newInstance(
                new AlgorithmConfiguration("INTERVAL", createDayProperties(stepAmount)));
        for (int j = 6; j <= 7; j++) {
            for (int i = 1; j == 6 ? i <= 30 : i <= 31; i = i + stepAmount) {
                availableTablesForDayDataSources.add(String.format("t_order_%04d%02d%02d", 2021, j, i));
            }
        }
    }
    
    private Properties createDayProperties(final int stepAmount) {
        Properties result = new Properties();
        result.setProperty("datetime-pattern", "yyyy-MM-dd HH:mm:ss");
        result.setProperty("datetime-lower", "2021-06-01 00:00:00");
        result.setProperty("datetime-upper", "2021-07-31 00:00:00");
        result.setProperty("sharding-suffix-pattern", "yyyyMMdd");
        result.setProperty("datetime-interval-amount", Integer.toString(stepAmount));
        return result;
    }
    
    private void initShardStrategyByDayWithMillisecond() {
        int stepAmount = 2;
        shardingAlgorithmByDayWithMillisecond = (IntervalShardingAlgorithm) ShardingAlgorithmFactory.newInstance(
                new AlgorithmConfiguration("INTERVAL", createDayWithMillisecondProperties(stepAmount)));
        for (int j = 6; j <= 7; j++) {
            for (int i = 1; j == 6 ? i <= 30 : i <= 31; i = i + stepAmount) {
                availableTablesForDayWithMillisecondDataSources.add(String.format("t_order_%04d%02d%02d", 2021, j, i));
            }
        }
    }
    
    private Properties createDayWithMillisecondProperties(final int stepAmount) {
        Properties result = new Properties();
        result.setProperty("datetime-pattern", "yyyy-MM-dd HH:mm:ss.SSS");
        result.setProperty("datetime-lower", "2021-06-01 00:00:00.000");
        result.setProperty("datetime-upper", "2021-07-31 00:00:00.000");
        result.setProperty("sharding-suffix-pattern", "yyyyMMdd");
        result.setProperty("datetime-interval-amount", Integer.toString(stepAmount));
        result.setProperty("datetime-interval-unit", "DAYS");
        return result;
    }
    
    private void initShardingStrategyByJDBCDate() {
        int stepAmount = 2;
        shardingAlgorithmByJDBCDate = (IntervalShardingAlgorithm) ShardingAlgorithmFactory.newInstance(
                new AlgorithmConfiguration("INTERVAL", createJDBCDateProperties(stepAmount)));
        for (int j = 6; j <= 7; j++) {
            for (int i = 1; j == 6 ? i <= 30 : i <= 31; i = i + stepAmount) {
                availableTablesForJDBCDateDataSources.add(String.format("t_order_%04d%02d%02d", 2021, j, i));
            }
        }
    }
    
    private Properties createJDBCDateProperties(final int stepAmount) {
        Properties result = new Properties();
        result.setProperty("datetime-pattern", "yyyy-MM-dd");
        result.setProperty("datetime-lower", "2021-06-01");
        result.setProperty("datetime-upper", "2021-07-31");
        result.setProperty("sharding-suffix-pattern", "yyyyMMdd");
        result.setProperty("datetime-interval-amount", Integer.toString(stepAmount));
        return result;
    }
    
    private void initShardingStrategyByJDBCTime() {
        int stepAmount = 2;
        shardingAlgorithmByJDBCTime = (IntervalShardingAlgorithm) ShardingAlgorithmFactory.newInstance(
                new AlgorithmConfiguration("INTERVAL", createJDBCTimeProperties(stepAmount)));
        for (int i = 2; i < 13; i++) {
            availableTablesForJDBCTimeDataSources.add(String.format("t_order_%02d%02d", i, 0));
        }
    }
    
    private Properties createJDBCTimeProperties(final int stepAmount) {
        Properties result = new Properties();
        result.setProperty("datetime-pattern", "HH:mm:ss.SSS");
        result.setProperty("datetime-lower", "02:00:00.000");
        result.setProperty("datetime-upper", "13:00:00.000");
        result.setProperty("sharding-suffix-pattern", "HHmm");
        result.setProperty("datetime-interval-amount", Integer.toString(stepAmount));
        result.setProperty("datetime-interval-unit", "Hours");
        return result;
    }
    
    private void initShardingStrategyByYear() {
        int stepAmount = 2;
        shardingAlgorithmByYear = (IntervalShardingAlgorithm) ShardingAlgorithmFactory.newInstance(
                new AlgorithmConfiguration("INTERVAL", createYearProperties(stepAmount)));
        for (int i = 2000; i < 2023; i++) {
            availableTablesForYearDataSources.add(String.format("t_order_%04d", i));
        }
    }
    
    private Properties createYearProperties(final int stepAmount) {
        Properties result = new Properties();
        result.setProperty("datetime-pattern", "yyyy");
        result.setProperty("datetime-lower", "2000");
        result.setProperty("datetime-upper", "2022");
        result.setProperty("sharding-suffix-pattern", "yyyy");
        result.setProperty("datetime-interval-amount", Integer.toString(stepAmount));
        result.setProperty("datetime-interval-unit", "Years");
        return result;
    }
    
    private void initShardingStrategyByYearMonth() {
        int stepAmount = 2;
        shardingAlgorithmByYearMonth = (IntervalShardingAlgorithm) ShardingAlgorithmFactory.newInstance(
                new AlgorithmConfiguration("INTERVAL", createYearMonthProperties(stepAmount)));
        for (int i = 2016; i <= 2021; i++) {
            for (int j = 1; j <= 12; j++) {
                availableTablesForYearMonthDataSources.add(String.format("t_order_%04d%02d", i, j));
            }
        }
    }
    
    private Properties createYearMonthProperties(final int stepAmount) {
        Properties result = new Properties();
        result.setProperty("datetime-pattern", "yyyy-MM");
        result.setProperty("datetime-lower", "2016-01");
        result.setProperty("datetime-upper", "2021-12");
        result.setProperty("sharding-suffix-pattern", "yyyyMM");
        result.setProperty("datetime-interval-amount", Integer.toString(stepAmount));
        result.setProperty("datetime-interval-unit", "Years");
        return result;
    }
    
    private void initShardingStrategyByMonthInJSR310() {
        int stepAmount = 2;
        shardingAlgorithmByMonthInJSR310 = (IntervalShardingAlgorithm) ShardingAlgorithmFactory.newInstance(
                new AlgorithmConfiguration("INTERVAL", createMonthInJSR310Properties(stepAmount)));
        for (int i = 2; i < 13; i++) {
            availableTablesForMonthInJSR310DataSources.add(String.format("t_order_%02d", i));
        }
    }
    
    private Properties createMonthInJSR310Properties(final int stepAmount) {
        Properties result = new Properties();
        result.setProperty("datetime-pattern", "MM");
        result.setProperty("datetime-lower", "02");
        result.setProperty("datetime-upper", "12");
        result.setProperty("sharding-suffix-pattern", "MM");
        result.setProperty("datetime-interval-amount", Integer.toString(stepAmount));
        result.setProperty("datetime-interval-unit", "Months");
        return result;
    }
    
    @Test
    public void assertPreciseDoShardingByQuarter() {
        assertThat(shardingAlgorithmByQuarter.doSharding(availableTablesForQuarterDataSources,
                new PreciseShardingValue<>("t_order", "create_time", DATA_NODE_INFO, "2020-01-01 00:00:01")), is("t_order_202001"));
        assertThat(shardingAlgorithmByQuarter.doSharding(availableTablesForQuarterDataSources,
                new PreciseShardingValue<>("t_order", "create_time", DATA_NODE_INFO, "2020-02-01 00:00:01")), is("t_order_202001"));
    }
    
    @Test
    public void assertRangeDoShardingByQuarter() {
        Collection<String> actual = shardingAlgorithmByQuarter.doSharding(availableTablesForQuarterDataSources,
                new RangeShardingValue<>("t_order", "create_time", DATA_NODE_INFO, Range.closed("2019-10-15 10:59:08", "2020-04-08 10:59:08")));
        assertThat(actual.size(), is(3));
    }
    
    @Test
    public void assertPreciseDoShardingByMonth() {
        assertThat(shardingAlgorithmByMonth.doSharding(availableTablesForMonthDataSources,
                new PreciseShardingValue<>("t_order", "create_time", DATA_NODE_INFO, "2020-01-01 00:00:01")), is("t_order_202001"));
        assertNull(shardingAlgorithmByMonth.doSharding(availableTablesForMonthDataSources,
                new PreciseShardingValue<>("t_order", "create_time", DATA_NODE_INFO, "2030-01-01 00:00:01")));
    }
    
    @Test
    public void assertRangeDoShardingByMonth() {
        Collection<String> actual = shardingAlgorithmByMonth.doSharding(availableTablesForMonthDataSources,
                new RangeShardingValue<>("t_order", "create_time", DATA_NODE_INFO, Range.closed("2019-10-15 10:59:08", "2020-04-08 10:59:08")));
        assertThat(actual.size(), is(7));
    }
    
    @Test
    public void assertLowerHalfRangeDoSharding() {
        Collection<String> actual = shardingAlgorithmByQuarter.doSharding(availableTablesForQuarterDataSources,
                new RangeShardingValue<>("t_order", "create_time", DATA_NODE_INFO, Range.atLeast("2018-10-15 10:59:08")));
        assertThat(actual.size(), is(9));
    }
    
    @Test
    public void assertUpperHalfRangeDoSharding() {
        Collection<String> actual = shardingAlgorithmByQuarter.doSharding(availableTablesForQuarterDataSources,
                new RangeShardingValue<>("t_order", "create_time", DATA_NODE_INFO, Range.atMost("2019-09-01 00:00:00")));
        assertThat(actual.size(), is(15));
    }
    
    @Test
    public void assertLowerHalfRangeDoShardingByDay() {
        Collection<String> actual = shardingAlgorithmByDay.doSharding(availableTablesForDayDataSources,
                new RangeShardingValue<>("t_order", "create_time", DATA_NODE_INFO, Range.atLeast("2021-01-01 00:00:00")));
        assertThat(actual.size(), is(31));
    }
    
    @Test
    public void assertUpperHalfRangeDoShardingByDay() {
        Collection<String> actual = shardingAlgorithmByDay.doSharding(availableTablesForDayDataSources,
                new RangeShardingValue<>("t_order", "create_time", DATA_NODE_INFO, Range.atMost("2021-07-31 01:00:00")));
        assertThat(actual.size(), is(31));
    }
    
    @Test
    public void assertPreciseDoShardingByDay() {
        assertThat(shardingAlgorithmByDay.doSharding(availableTablesForDayDataSources,
                new PreciseShardingValue<>("t_order", "create_time", DATA_NODE_INFO, "2021-07-01 00:00:01")), is("t_order_20210701"));
        assertThat(shardingAlgorithmByDay.doSharding(availableTablesForDayDataSources,
                new PreciseShardingValue<>("t_order", "create_time", DATA_NODE_INFO, "2021-07-02 00:00:01")), is("t_order_20210701"));
    }
    
    @Test
    public void assertRangeDoShardingByDay() {
        Collection<String> actual = shardingAlgorithmByDay.doSharding(availableTablesForDayDataSources,
                new RangeShardingValue<>("t_order", "create_time", DATA_NODE_INFO, Range.closed("2021-06-15 00:00:00", "2021-07-31 01:00:00")));
        assertThat(actual.size(), is(24));
    }
    
    @Test
    public void assertFormat() {
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
    @SneakyThrows(ParseException.class)
    public void assertTimestampInJDBCTypeWithZeroMillisecond() {
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
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        Collection<String> actualAsDate = shardingAlgorithmByDayWithMillisecond.doSharding(availableTablesForDayWithMillisecondDataSources,
                new RangeShardingValue<>("t_order", "create_time", DATA_NODE_INFO,
                        Range.closed(simpleDateFormat.parse("2021-06-15 02:25:27.000"), simpleDateFormat.parse("2021-07-31 02:25:27.000"))));
        assertThat(actualAsDate.size(), is(24));
    }
    
    @Test
    public void assertDateInJDBCType() {
        Collection<String> actualAsLocalDate = shardingAlgorithmByJDBCDate.doSharding(availableTablesForJDBCDateDataSources,
                new RangeShardingValue<>("t_order", "create_time", DATA_NODE_INFO,
                        Range.closed(LocalDate.of(2021, 6, 15), LocalDate.of(2021, 7, 31))));
        assertThat(actualAsLocalDate.size(), is(24));
    }
    
    @Test
    public void assertTimeInJDBCType() {
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
    public void assertIntegerInJDBCType() {
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
    public void assertDateInSqlDate() {
        Collection<String> actualAsLocalDate = shardingAlgorithmByJDBCDate.doSharding(availableTablesForJDBCDateDataSources,
                new RangeShardingValue<>("t_order", "create_time", DATA_NODE_INFO,
                        Range.closed(new Date(2021 - 1900, 6 - 1, 15), new Date(2021 - 1900, 7 - 1, 31))));
        assertThat(actualAsLocalDate.size(), is(24));
    }
}
