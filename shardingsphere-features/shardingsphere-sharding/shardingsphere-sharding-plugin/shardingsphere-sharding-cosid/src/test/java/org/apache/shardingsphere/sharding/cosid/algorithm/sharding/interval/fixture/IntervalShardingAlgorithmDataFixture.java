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

package org.apache.shardingsphere.sharding.cosid.algorithm.sharding.interval.fixture;

import com.google.common.collect.Range;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Properties;
import java.util.function.Function;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import me.ahoo.cosid.sharding.ExactCollection;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.sharding.cosid.algorithm.Arguments;
import org.apache.shardingsphere.sharding.cosid.algorithm.CosIdAlgorithmConstants;
import org.apache.shardingsphere.sharding.cosid.algorithm.sharding.interval.CosIdIntervalShardingAlgorithm;
import org.apache.shardingsphere.sharding.factory.ShardingAlgorithmFactory;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class IntervalShardingAlgorithmDataFixture {
    
    public static final ZoneOffset ZONE_OFFSET_SHANGHAI;
    
    public static final LocalDateTime LOWER_DATE_TIME;
    
    public static final LocalDateTime UPPER_DATE_TIME;
    
    public static final String LOGIC_NAME;
    
    public static final String LOGIC_NAME_PREFIX;
    
    public static final String COLUMN_NAME;
    
    public static final String SUFFIX_FORMATTER_STRING;
    
    public static final ExactCollection<String> ALL_NODES;
    
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
    
    /**
     * Precise parameter provider.
     *
     * @param datetimeConvert datetimeConvert
     * @return unit test parameter collection
     */
    public static Iterable<Object[]> preciseArgsProvider(final Function<LocalDateTime, ? extends Comparable<?>> datetimeConvert) {
        return Arguments.ofArrayElement(
                Arguments.of(datetimeConvert.apply(LOWER_DATE_TIME), "table_202101"),
                Arguments.of(datetimeConvert.apply(LocalDateTime.of(2021, 2, 14, 22, 0)), "table_202102"),
                Arguments.of(datetimeConvert.apply(LocalDateTime.of(2021, 10, 1, 0, 0)), "table_202110"),
                Arguments.of(datetimeConvert.apply(UPPER_DATE_TIME), "table_202201"));
    }
    
    /**
     * Range parameter provider.
     *
     * @param datetimeConvert datetimeConvert
     * @return unit test parameter collection
     */
    public static Iterable<Object[]> rangeArgsProvider(final Function<LocalDateTime, ? extends Comparable<?>> datetimeConvert) {
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
    
    /**
     * Create CosIdIntervalShardingAlgorithm.
     *
     * @return CosIdIntervalShardingAlgorithm
     */
    public static CosIdIntervalShardingAlgorithm createShardingAlgorithm() {
        return (CosIdIntervalShardingAlgorithm) ShardingAlgorithmFactory.newInstance(new AlgorithmConfiguration("COSID_INTERVAL", createProperties()));
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
}
