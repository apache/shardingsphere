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

package org.apache.shardingsphere.core.strategy.algorithm.sharding;

import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import org.apache.shardingsphere.api.config.sharding.strategy.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.core.strategy.route.standard.StandardShardingStrategy;
import org.apache.shardingsphere.core.strategy.route.value.ListRouteValue;
import org.apache.shardingsphere.core.strategy.route.value.RangeRouteValue;
import org.apache.shardingsphere.core.strategy.route.value.RouteValue;
import org.apache.shardingsphere.underlying.common.config.properties.ConfigurationProperties;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * datetime sharding algorithm test.
 */
public class CustomDateTimeShardingAlgorithmTest {

    private final List<String> availableTables = new ArrayList<>();

    private StandardShardingStrategy shardingStrategy;

    @Before
    public void setup() {
        CustomDateTimeShardingAlgorithm shardingAlgorithm = new CustomDateTimeShardingAlgorithm();
        shardingAlgorithm.getProperties().setProperty("datetime.format", "yyyy-MM-dd HH:mm:ss");
        shardingAlgorithm.getProperties().setProperty("table.suffix.format", "yyyyQQ");
        shardingAlgorithm.getProperties().setProperty("datetime.lower", "2010-01-01 00:00:00.000");
        shardingAlgorithm.getProperties().setProperty("datetime.step.unit", "Months");
        shardingAlgorithm.getProperties().setProperty("datetime.step.amount", "3");
        StandardShardingStrategyConfiguration shardingStrategyConfig = new StandardShardingStrategyConfiguration("create_time", shardingAlgorithm);
        this.shardingStrategy = new StandardShardingStrategy(shardingStrategyConfig);

        for (int i = 2016; i < 2021; i++) {
            for (int j = 1; j <= 4; j++) {
                availableTables.add(String.format("t_order_%04d%02d", i, j));
            }
        }
    }

    @Test
    public void assertPreciseDoSharding() {
        List<RouteValue> shardingValues = Lists.newArrayList(new ListRouteValue<>("create_time", "t_order",
                Lists.newArrayList("2020-01-01 00:00:01", "2020-01-01 00:00:02", "2020-04-15 10:59:08")));
        Collection<String> actual = shardingStrategy.doSharding(availableTables, shardingValues, new ConfigurationProperties(new Properties()));
        assertThat(actual.size(), is(2));
        assertTrue(actual.contains("t_order_202001"));
        assertTrue(actual.contains("t_order_202002"));
    }

    @Test
    public void assertRangeDoSharding() {
        Range<String> rangeValue = Range.closed("2019-10-15 10:59:08", "2020-04-08 10:59:08");
        List<RouteValue> shardingValues = Lists.newArrayList(new RangeRouteValue<>("create_time", "t_order", rangeValue));
        Collection<String> actual = shardingStrategy.doSharding(availableTables, shardingValues, new ConfigurationProperties(new Properties()));
        assertThat(actual.size(), is(3));
    }

    @Test
    public void testFormat() {
        String inputFormat = "yyyy-MM-dd HH:mm:ss.SSS";
        String tableFormat = "yyyyQQ";
        String value = "2020-10-11 00:00:00.000000";
        LocalDateTime localDateTime = LocalDateTime.parse(value.substring(0, inputFormat.length()), DateTimeFormatter.ofPattern(inputFormat));
        String tableName = localDateTime.format(DateTimeFormatter.ofPattern(tableFormat));
        assertEquals("202004", tableName);
    }
}
