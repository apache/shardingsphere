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

package org.apache.shardingsphere.sharding.algorithm.sharding.cosid.interval;

import com.google.common.collect.Range;
import lombok.RequiredArgsConstructor;
import me.ahoo.cosid.sharding.ExactCollection;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.datanode.DataNodeInfo;
import org.apache.shardingsphere.sharding.algorithm.constant.CosIdAlgorithmConstants;
import org.apache.shardingsphere.sharding.algorithm.sharding.cosid.Arguments;
import org.apache.shardingsphere.sharding.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.RangeShardingValue;
import org.apache.shardingsphere.sharding.factory.ShardingAlgorithmFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.Date;
import java.util.Properties;
import java.util.function.Function;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class CosIdIntervalShardingAlgorithmTest {
    
    static final ZoneOffset ZONE_OFFSET_SHANGHAI;
    
    static final LocalDateTime LOWER_DATE_TIME;
    
    static final LocalDateTime UPPER_DATE_TIME;
    
    static final String LOGIC_NAME;
    
    static final String LOGIC_NAME_PREFIX;
    
    static final String COLUMN_NAME;
    
    static final String SUFFIX_FORMATTER_STRING;
    
    static final ExactCollection<String> ALL_NODES;
    
    static {
        ZONE_OFFSET_SHANGHAI = ZoneOffset.of("+8");
        LOWER_DATE_TIME = LocalDateTime.of(2021, 1, 1, 0, 0);
        UPPER_DATE_TIME = LOWER_DATE_TIME.plusYears(1);
        LOGIC_NAME = "table";
        LOGIC_NAME_PREFIX = LOGIC_NAME + "_";
        COLUMN_NAME = "create_time";
        SUFFIX_FORMATTER_STRING = "yyyyMM";
        ALL_NODES = new ExactCollection<>("table_202101", "table_202102", "table_202103", "table_202104",
                "table_202105", "table_202106", "table_202107", "table_202108", "table_202109",
                "table_202110", "table_202111", "table_202112", "table_202201");
    }
    
    static CosIdIntervalShardingAlgorithm createShardingAlgorithm() {
        return (CosIdIntervalShardingAlgorithm) ShardingAlgorithmFactory.newInstance(new ShardingSphereAlgorithmConfiguration("COSID_INTERVAL", createProperties()));
    }
    
    private static Properties createProperties() {
        Properties result = new Properties();
        result.setProperty(CosIdIntervalShardingAlgorithm.ZONE_ID_KEY, "Asia/Shanghai");
        result.setProperty(CosIdAlgorithmConstants.LOGIC_NAME_PREFIX_KEY, LOGIC_NAME_PREFIX);
        result.setProperty(CosIdIntervalShardingAlgorithm.DATE_TIME_LOWER_KEY, LOWER_DATE_TIME.format(CosIdIntervalShardingAlgorithm.DEFAULT_DATE_TIME_FORMATTER));
        result.setProperty(CosIdIntervalShardingAlgorithm.DATE_TIME_UPPER_KEY, UPPER_DATE_TIME.format(CosIdIntervalShardingAlgorithm.DEFAULT_DATE_TIME_FORMATTER));
        result.setProperty(CosIdIntervalShardingAlgorithm.SHARDING_SUFFIX_FORMAT_KEY, SUFFIX_FORMATTER_STRING);
        result.setProperty(CosIdIntervalShardingAlgorithm.INTERVAL_UNIT_KEY, "MONTHS");
        result.put(CosIdIntervalShardingAlgorithm.INTERVAL_AMOUNT_KEY, 1);
        return result;
    }
    
    static Iterable<Object[]> preciseArgsProvider(final Function<LocalDateTime, ? extends Comparable<?>> datetimeConvert) {
        return Arguments.ofArrayElement(
                Arguments.of(datetimeConvert.apply(LOWER_DATE_TIME), "table_202101"),
                Arguments.of(datetimeConvert.apply(LocalDateTime.of(2021, 2, 14, 22, 0)), "table_202102"),
                Arguments.of(datetimeConvert.apply(LocalDateTime.of(2021, 10, 1, 0, 0)), "table_202110"),
                Arguments.of(datetimeConvert.apply(UPPER_DATE_TIME), "table_202201"));
    }
    
    static Iterable<Object[]> preciseArgsProviderAsLocalDateTime() {
        return preciseArgsProvider(ldt -> ldt);
    }
    
    static Iterable<Object[]> preciseArgsProviderAsString() {
        return preciseArgsProvider(ldt -> ldt.format(CosIdIntervalShardingAlgorithm.DEFAULT_DATE_TIME_FORMATTER));
    }
    
    static Iterable<Object[]> preciseArgsProviderAsDate() {
        return preciseArgsProvider(ldt -> new Date(ldt.toInstant(ZONE_OFFSET_SHANGHAI).toEpochMilli()));
    }
    
    static Iterable<Object[]> preciseArgsProviderAsTimestamp() {
        return preciseArgsProvider(ldt -> ldt.toInstant(ZONE_OFFSET_SHANGHAI).toEpochMilli());
    }
    
    static Iterable<Object[]> rangeArgsProvider(final Function<LocalDateTime, ? extends Comparable<?>> datetimeConvert) {
        return Arguments.ofArrayElement(
                Arguments.of(Range.all(), ALL_NODES),
                Arguments.of(Range.closed(datetimeConvert.apply(LOWER_DATE_TIME), datetimeConvert.apply(UPPER_DATE_TIME)), ALL_NODES),
                Arguments.of(Range.closed(datetimeConvert.apply(LocalDateTime.of(2021, 1, 1, 0, 0)),
                        datetimeConvert.apply(LocalDateTime.of(2021, 2, 1, 0, 0))),
                        new ExactCollection<>("table_202101", "table_202102")),
                Arguments.of(Range.closed(datetimeConvert.apply(LOWER_DATE_TIME.minusMonths(1)), datetimeConvert.apply(UPPER_DATE_TIME.plusMonths(1))), ALL_NODES),
                Arguments.of(Range.closed(datetimeConvert.apply(LocalDateTime.of(2021, 12, 1, 0, 0)),
                        datetimeConvert.apply(LocalDateTime.of(2022, 2, 1, 0, 0))),
                        new ExactCollection<>("table_202112", "table_202201")),
                Arguments.of(Range.closedOpen(datetimeConvert.apply(LOWER_DATE_TIME), datetimeConvert.apply(UPPER_DATE_TIME)),
                        new ExactCollection<>("table_202101", "table_202102", "table_202103", "table_202104", "table_202105", "table_202106",
                                "table_202107", "table_202108", "table_202109", "table_202110", "table_202111", "table_202112")),
                Arguments.of(Range.openClosed(datetimeConvert.apply(LOWER_DATE_TIME), datetimeConvert.apply(UPPER_DATE_TIME)), ALL_NODES),
                Arguments.of(Range.greaterThan(datetimeConvert.apply(LOWER_DATE_TIME)), ALL_NODES),
                Arguments.of(Range.atLeast(datetimeConvert.apply(LOWER_DATE_TIME)), ALL_NODES),
                Arguments.of(Range.greaterThan(datetimeConvert.apply(UPPER_DATE_TIME)), new ExactCollection<>("table_202201")),
                Arguments.of(Range.atLeast(datetimeConvert.apply(UPPER_DATE_TIME)), new ExactCollection<>("table_202201")),
                Arguments.of(Range.greaterThan(datetimeConvert.apply(LocalDateTime.of(2021, 12, 5, 0, 0))),
                        new ExactCollection<>("table_202112", "table_202201")),
                Arguments.of(Range.atLeast(datetimeConvert.apply(LocalDateTime.of(2021, 12, 5, 0, 0))),
                        new ExactCollection<>("table_202112", "table_202201")),
                Arguments.of(Range.lessThan(datetimeConvert.apply(LOWER_DATE_TIME)), ExactCollection.empty()),
                Arguments.of(Range.atMost(datetimeConvert.apply(LOWER_DATE_TIME)), new ExactCollection<>("table_202101")),
                Arguments.of(Range.lessThan(datetimeConvert.apply(UPPER_DATE_TIME)), new ExactCollection<>("table_202101",
                        "table_202102", "table_202103", "table_202104", "table_202105", "table_202106", "table_202107",
                        "table_202108", "table_202109", "table_202110", "table_202111", "table_202112")),
                Arguments.of(Range.atMost(datetimeConvert.apply(UPPER_DATE_TIME)), ALL_NODES),
                Arguments.of(Range.lessThan(datetimeConvert.apply(LocalDateTime.of(2021, 5, 5, 0, 0))),
                        new ExactCollection<>("table_202101", "table_202102", "table_202103", "table_202104", "table_202105")),
                Arguments.of(Range.atMost(datetimeConvert.apply(LocalDateTime.of(2021, 5, 5, 0, 0))),
                        new ExactCollection<>("table_202101", "table_202102", "table_202103", "table_202104", "table_202105")));
    }
    
    static Iterable<Object[]> rangeArgsProviderAsLocalDateTime() {
        return rangeArgsProvider(ldt -> ldt);
    }
    
    static Iterable<Object[]> rangeArgsProviderAsString() {
        return rangeArgsProvider(ldt -> ldt.format(CosIdIntervalShardingAlgorithm.DEFAULT_DATE_TIME_FORMATTER));
    }
    
    static Iterable<Object[]> rangeArgsProviderAsDate() {
        return rangeArgsProvider(ldt -> new Date(ldt.toInstant(ZONE_OFFSET_SHANGHAI).toEpochMilli()));
    }
    
    static Iterable<Object[]> rangeArgsProviderAsTimestamp() {
        return rangeArgsProvider(ldt -> ldt.toInstant(ZONE_OFFSET_SHANGHAI).toEpochMilli());
    }
    
    @RunWith(Parameterized.class)
    @RequiredArgsConstructor
    public static class LocalDateTimePreciseValueDoShardingTest {
        
        private final LocalDateTime dateTime;
        
        private final String expected;
    
        private CosIdIntervalShardingAlgorithm algorithm;
        
        @Before
        public void init() {
            algorithm = createShardingAlgorithm();
        }
        
        @Parameters
        public static Iterable<Object[]> argsProvider() {
            return preciseArgsProviderAsLocalDateTime();
        }
        
        @Test
        public void assertDoSharding() {
            PreciseShardingValue<Comparable<?>> shardingValue = new PreciseShardingValue<>(LOGIC_NAME, COLUMN_NAME, new DataNodeInfo(LOGIC_NAME_PREFIX, 6, '0'), dateTime);
            String actual = algorithm.doSharding(ALL_NODES, shardingValue);
            assertThat(actual, is(expected));
        }
    }
    
    @RunWith(Parameterized.class)
    @RequiredArgsConstructor
    public static class LocalDateTimeRangeValueDoShardingTest {
        
        private final Range<LocalDateTime> rangeValue;
        
        private final Collection<String> expected;
    
        private CosIdIntervalShardingAlgorithm algorithm;
        
        @Before
        public void init() {
            algorithm = createShardingAlgorithm();
        }
        
        @Parameters
        public static Iterable<Object[]> argsProvider() {
            return rangeArgsProviderAsLocalDateTime();
        }
        
        @Test
        public void assertDoSharding() {
            RangeShardingValue shardingValue = new RangeShardingValue<>(LOGIC_NAME, COLUMN_NAME, new DataNodeInfo(LOGIC_NAME_PREFIX, 6, '0'), rangeValue);
            Collection<String> actual = algorithm.doSharding(ALL_NODES, shardingValue);
            assertThat(actual, is(expected));
        }
    }
    
    @RunWith(Parameterized.class)
    @RequiredArgsConstructor
    public static class StringPreciseValueDoShardingTest {
        
        private final String value;
        
        private final String expected;
    
        private CosIdIntervalShardingAlgorithm algorithm;
        
        @Before
        public void init() {
            algorithm = createShardingAlgorithm();
        }
        
        @Parameters
        public static Iterable<Object[]> argsProvider() {
            return preciseArgsProviderAsString();
        }
        
        @Test
        public void assertDoSharding() {
            PreciseShardingValue shardingValue = new PreciseShardingValue<>(LOGIC_NAME, COLUMN_NAME, new DataNodeInfo(LOGIC_NAME_PREFIX, 6, '0'), value);
            String actual = algorithm.doSharding(ALL_NODES, shardingValue);
            assertThat(actual, is(expected));
        }
    }
    
    @RunWith(Parameterized.class)
    @RequiredArgsConstructor
    public static class StringRangeValueDoShardingTest {
        
        private final Range<String> rangeValue;
        
        private final Collection<String> expected;
    
        private CosIdIntervalShardingAlgorithm algorithm;
        
        @Before
        public void init() {
            algorithm = createShardingAlgorithm();
        }
        
        @Parameters
        public static Iterable<Object[]> argsProvider() {
            return rangeArgsProviderAsString();
        }
        
        @Test
        public void assertDoSharding() {
            RangeShardingValue shardingValue = new RangeShardingValue<>(LOGIC_NAME, COLUMN_NAME, new DataNodeInfo(LOGIC_NAME_PREFIX, 6, '0'), rangeValue);
            Collection<String> actual = algorithm.doSharding(ALL_NODES, shardingValue);
            assertThat(actual, is(expected));
        }
    }
    
    @RunWith(Parameterized.class)
    @RequiredArgsConstructor
    public static class DatePreciseValueDoShardingTest {
        
        private final Date value;
        
        private final String expected;
    
        private CosIdIntervalShardingAlgorithm algorithm;
        
        @Before
        public void init() {
            algorithm = createShardingAlgorithm();
        }
        
        @Parameters
        public static Iterable<Object[]> argsProvider() {
            return preciseArgsProviderAsDate();
        }
        
        @Test
        public void assertDoSharding() {
            PreciseShardingValue shardingValue = new PreciseShardingValue<>(LOGIC_NAME, COLUMN_NAME, new DataNodeInfo(LOGIC_NAME_PREFIX, 6, '0'), value);
            String actual = algorithm.doSharding(ALL_NODES, shardingValue);
            assertThat(actual, is(expected));
        }
    }
    
    @RunWith(Parameterized.class)
    @RequiredArgsConstructor
    public static class DateRangeValueDoShardingTest {
        
        private final Range<Date> rangeValue;
        
        private final Collection<String> expected;
        
        private CosIdIntervalShardingAlgorithm algorithm;
        
        @Before
        public void init() {
            algorithm = createShardingAlgorithm();
        }
        
        @Parameters
        public static Iterable<Object[]> argsProvider() {
            return rangeArgsProviderAsDate();
        }
        
        @Test
        public void assertDoSharding() {
            RangeShardingValue shardingValue = new RangeShardingValue<>(LOGIC_NAME, COLUMN_NAME, new DataNodeInfo(LOGIC_NAME_PREFIX, 6, '0'), rangeValue);
            assertThat(algorithm.doSharding(ALL_NODES, shardingValue), is(expected));
        }
    }
    
    @RunWith(Parameterized.class)
    @RequiredArgsConstructor
    public static class TimestampPreciseValueDoShardingTest {
        
        private final Long value;
        
        private final String expected;
        
        private CosIdIntervalShardingAlgorithm algorithm;
        
        @Before
        public void init() {
            algorithm = createShardingAlgorithm();
        }
        
        @Parameters
        public static Iterable<Object[]> argsProvider() {
            return preciseArgsProviderAsTimestamp();
        }
        
        @Test
        public void assertDoSharding() {
            PreciseShardingValue shardingValue = new PreciseShardingValue<>(LOGIC_NAME, COLUMN_NAME, new DataNodeInfo(LOGIC_NAME_PREFIX, 6, '0'), value);
            assertThat(algorithm.doSharding(ALL_NODES, shardingValue), is(expected));
        }
    }
    
    @RunWith(Parameterized.class)
    @RequiredArgsConstructor
    public static class TimestampRangeValueDoShardingTest {
        
        private final Range<Long> rangeValue;
        
        private final Collection<String> expected;
        
        private CosIdIntervalShardingAlgorithm algorithm;
        
        @Before
        public void init() {
            algorithm = createShardingAlgorithm();
        }
        
        @Parameters
        public static Iterable<Object[]> argsProvider() {
            return rangeArgsProviderAsTimestamp();
        }
        
        @Test
        public void assertDoSharding() {
            RangeShardingValue shardingValue = new RangeShardingValue<>(LOGIC_NAME, COLUMN_NAME, new DataNodeInfo(LOGIC_NAME_PREFIX, 6, '0'), rangeValue);
            Collection<String> actual = algorithm.doSharding(ALL_NODES, shardingValue);
            assertThat(actual, is(expected));
        }
    }
}
