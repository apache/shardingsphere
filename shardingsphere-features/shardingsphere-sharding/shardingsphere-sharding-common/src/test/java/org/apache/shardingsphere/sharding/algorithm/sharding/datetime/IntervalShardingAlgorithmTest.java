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
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.sharding.strategy.standard.StandardShardingStrategy;
import org.apache.shardingsphere.sharding.strategy.value.ListRouteValue;
import org.apache.shardingsphere.sharding.strategy.value.RangeRouteValue;
import org.apache.shardingsphere.sharding.strategy.value.RouteValue;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class IntervalShardingAlgorithmTest {
    
    private final Collection<String> availableTablesForMonthStrategy = new LinkedList<>();
    
    private final Collection<String> availableTablesForQuarterStrategy = new LinkedList<>();
    
    private StandardShardingStrategy shardingStrategyByMonth;
    
    private StandardShardingStrategy shardingStrategyByQuarter;
    
    @Before
    public void setup() {
        initShardStrategyByMonth();
        initShardStrategyByQuarter();
    }
    
    private void initShardStrategyByQuarter() {
        IntervalShardingAlgorithm shardingAlgorithm = new IntervalShardingAlgorithm();
        shardingAlgorithm.getProps().setProperty("datetime.pattern", "yyyy-MM-dd HH:mm:ss");
        shardingAlgorithm.getProps().setProperty("datetime.lower", "2016-01-01 00:00:00");
        shardingAlgorithm.getProps().setProperty("datetime.upper", "2021-12-31 00:00:00");
        shardingAlgorithm.getProps().setProperty("sharding.suffix.pattern", "yyyyQQ");
        shardingAlgorithm.getProps().setProperty("datetime.interval.amount", "3");
        shardingAlgorithm.getProps().setProperty("datetime.interval.unit", "Months");
        shardingAlgorithm.init();
        shardingStrategyByQuarter = new StandardShardingStrategy("create_time", shardingAlgorithm);
        for (int i = 2016; i <= 2020; i++) {
            for (int j = 1; j <= 4; j++) {
                availableTablesForQuarterStrategy.add(String.format("t_order_%04d%02d", i, j));
            }
        }
    }
    
    private void initShardStrategyByMonth() {
        IntervalShardingAlgorithm shardingAlgorithm = new IntervalShardingAlgorithm();
        shardingAlgorithm.getProps().setProperty("datetime.pattern", "yyyy-MM-dd HH:mm:ss");
        shardingAlgorithm.getProps().setProperty("datetime.lower", "2016-01-01 00:00:00");
        shardingAlgorithm.getProps().setProperty("datetime.upper", "2021-12-31 00:00:00");
        shardingAlgorithm.getProps().setProperty("sharding.suffix.pattern", "yyyyMM");
        shardingAlgorithm.getProps().setProperty("datetime.interval.amount", "1");
        shardingAlgorithm.getProps().setProperty("datetime.interval.unit", "Months");
        shardingAlgorithm.init();
        shardingStrategyByMonth = new StandardShardingStrategy("create_time", shardingAlgorithm);
        for (int i = 2016; i <= 2020; i++) {
            for (int j = 1; j <= 12; j++) {
                availableTablesForMonthStrategy.add(String.format("t_order_%04d%02d", i, j));
            }
        }
    }
    
    @Test
    public void assertPreciseDoShardingByQuarter() {
        List<RouteValue> shardingValues = Collections.singletonList(new ListRouteValue<>("create_time", "t_order", Arrays.asList("2020-01-01 00:00:01", "2020-01-01 00:00:02", "2020-04-15 10:59:08")));
        Collection<String> actual = shardingStrategyByQuarter.doSharding(availableTablesForQuarterStrategy, shardingValues, new ConfigurationProperties(new Properties()));
        assertThat(actual.size(), is(2));
        assertTrue(actual.contains("t_order_202001"));
        assertTrue(actual.contains("t_order_202002"));
    }
    
    @Test
    public void assertRangeDoShardingByQuarter() {
        Range<String> rangeValue = Range.closed("2019-10-15 10:59:08", "2020-04-08 10:59:08");
        List<RouteValue> shardingValues = Collections.singletonList(new RangeRouteValue<>("create_time", "t_order", rangeValue));
        Collection<String> actual = shardingStrategyByQuarter.doSharding(availableTablesForQuarterStrategy, shardingValues, new ConfigurationProperties(new Properties()));
        assertThat(actual.size(), is(3));
    }
    
    @Test
    public void assertPreciseDoShardingByMonth() {
        List<RouteValue> shardingValues = Collections.singletonList(new ListRouteValue<>("create_time", "t_order", Arrays.asList("2020-01-01 00:00:01", "2020-01-01 00:00:02", "2020-04-15 10:59:08")));
        Collection<String> actual = shardingStrategyByMonth.doSharding(availableTablesForMonthStrategy, shardingValues, new ConfigurationProperties(new Properties()));
        assertThat(actual.size(), is(2));
        assertTrue(actual.contains("t_order_202001"));
        assertTrue(actual.contains("t_order_202004"));
    }
    
    @Test
    public void assertRangeDoShardingByMonth() {
        Range<String> rangeValue = Range.closed("2019-10-15 10:59:08", "2020-04-08 10:59:08");
        List<RouteValue> shardingValues = Collections.singletonList(new RangeRouteValue<>("create_time", "t_order", rangeValue));
        Collection<String> actual = shardingStrategyByMonth.doSharding(availableTablesForMonthStrategy, shardingValues, new ConfigurationProperties(new Properties()));
        assertThat(actual.size(), is(7));
    }
    
    @Test
    public void assertLowerHalfRangeDoSharding() {
        Range<String> rangeValue = Range.atLeast("2018-10-15 10:59:08");
        List<RouteValue> shardingValues = Collections.singletonList(new RangeRouteValue<>("create_time", "t_order", rangeValue));
        Collection<String> actual = shardingStrategyByQuarter.doSharding(availableTablesForQuarterStrategy, shardingValues, new ConfigurationProperties(new Properties()));
        assertThat(actual.size(), is(9));
    }
    
    @Test
    public void assertUpperHalfRangeDoSharding() {
        Range<String> rangeValue = Range.atMost("2019-09-01 00:00:00");
        List<RouteValue> shardingValues = Collections.singletonList(new RangeRouteValue<>("create_time", "t_order", rangeValue));
        Collection<String> actual = shardingStrategyByQuarter.doSharding(availableTablesForQuarterStrategy, shardingValues, new ConfigurationProperties(new Properties()));
        assertThat(actual.size(), is(15));
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
}
