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
import org.apache.shardingsphere.sharding.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.RangeShardingValue;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.LinkedList;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public final class IntervalShardingAlgorithmTest {
    
    public static final DataNodeInfo DATA_NODE_INFO = new DataNodeInfo("t_order_", 6, '0');
    
    private final Collection<String> availableTablesForQuarterDataSources = new LinkedList<>();
    
    private final Collection<String> availableTablesForMonthDataSources = new LinkedList<>();
    
    private final Collection<String> availableTablesForDayDataSources = new LinkedList<>();
    
    private IntervalShardingAlgorithm shardingAlgorithmByQuarter;
    
    private IntervalShardingAlgorithm shardingAlgorithmByMonth;
    
    private IntervalShardingAlgorithm shardingAlgorithmByDay;
    
    @Before
    public void setup() {
        initShardStrategyByMonth();
        initShardStrategyByQuarter();
        initShardingStrategyByDay();
    }
    
    private void initShardStrategyByQuarter() {
        shardingAlgorithmByQuarter = new IntervalShardingAlgorithm();
        shardingAlgorithmByQuarter.getProps().setProperty("datetime-pattern", "yyyy-MM-dd HH:mm:ss");
        shardingAlgorithmByQuarter.getProps().setProperty("datetime-lower", "2016-01-01 00:00:00");
        shardingAlgorithmByQuarter.getProps().setProperty("datetime-upper", "2021-12-31 00:00:00");
        shardingAlgorithmByQuarter.getProps().setProperty("sharding-suffix-pattern", "yyyyQQ");
        shardingAlgorithmByQuarter.getProps().setProperty("datetime-interval-amount", "3");
        shardingAlgorithmByQuarter.getProps().setProperty("datetime-interval-unit", "Months");
        shardingAlgorithmByQuarter.init();
        for (int i = 2016; i <= 2020; i++) {
            for (int j = 1; j <= 4; j++) {
                availableTablesForQuarterDataSources.add(String.format("t_order_%04d%02d", i, j));
            }
        }
    }
    
    private void initShardStrategyByMonth() {
        shardingAlgorithmByMonth = new IntervalShardingAlgorithm();
        shardingAlgorithmByMonth.getProps().setProperty("datetime-pattern", "yyyy-MM-dd HH:mm:ss");
        shardingAlgorithmByMonth.getProps().setProperty("datetime-lower", "2016-01-01 00:00:00");
        shardingAlgorithmByMonth.getProps().setProperty("datetime-upper", "2021-12-31 00:00:00");
        shardingAlgorithmByMonth.getProps().setProperty("sharding-suffix-pattern", "yyyyMM");
        shardingAlgorithmByMonth.getProps().setProperty("datetime-interval-amount", "1");
        shardingAlgorithmByMonth.getProps().setProperty("datetime-interval-unit", "Months");
        shardingAlgorithmByMonth.init();
        for (int i = 2016; i <= 2020; i++) {
            for (int j = 1; j <= 12; j++) {
                availableTablesForMonthDataSources.add(String.format("t_order_%04d%02d", i, j));
            }
        }
    }
    
    private void initShardingStrategyByDay() {
        shardingAlgorithmByDay = new IntervalShardingAlgorithm();
        shardingAlgorithmByDay.getProps().setProperty("datetime-pattern", "yyyy-MM-dd HH:mm:ss");
        shardingAlgorithmByDay.getProps().setProperty("datetime-lower", "2021-06-01 00:00:00");
        shardingAlgorithmByDay.getProps().setProperty("datetime-upper", "2021-07-31 00:00:00");
        shardingAlgorithmByDay.getProps().setProperty("sharding-suffix-pattern", "yyyyMMdd");
        int stepAmount = 2;
        shardingAlgorithmByDay.getProps().setProperty("datetime-interval-amount", Integer.toString(stepAmount));
        shardingAlgorithmByDay.init();
        for (int j = 6; j <= 7; j++) {
            for (int i = 1; j == 6 ? i <= 30 : i <= 31; i = i + stepAmount) {
                availableTablesForDayDataSources.add(String.format("t_order_%04d%02d%02d", 2021, j, i));
            }
        }
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
}
