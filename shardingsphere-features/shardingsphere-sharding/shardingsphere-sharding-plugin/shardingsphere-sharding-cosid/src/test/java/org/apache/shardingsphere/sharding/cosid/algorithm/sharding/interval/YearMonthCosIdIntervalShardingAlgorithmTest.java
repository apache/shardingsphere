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

package org.apache.shardingsphere.sharding.cosid.algorithm.sharding.interval;

import com.google.common.collect.Range;
import java.time.YearMonth;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import me.ahoo.cosid.sharding.ExactCollection;
import org.apache.shardingsphere.sharding.cosid.algorithm.sharding.interval.fixture.IntervalShardingAlgorithmDataFixture;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

public final class YearMonthCosIdIntervalShardingAlgorithmTest {
    
    static Iterable<Object[]> preciseArgsProviderAsYearMonth() {
        return IntervalShardingAlgorithmDataFixture.preciseArgsProvider(ldt -> YearMonth.of(ldt.getYear(), ldt.getMonth()));
    }
    
    static Iterable<Object[]> rangeArgsProviderAsYearMonth() {
        return IntervalShardingAlgorithmDataFixture.rangeArgsProvider(ldt -> YearMonth.of(ldt.getYear(), ldt.getMonth()));
    }
    
    @RunWith(Parameterized.class)
    public static final class PreciseShardingValueDoShardingTest extends AbstractPreciseShardingValueParameterizedTest<YearMonth> {
        
        public PreciseShardingValueDoShardingTest(final YearMonth input, final String expected) {
            super(input, expected);
        }
        
        @Parameterized.Parameters(name = "{index}: doSharding({0})={1}")
        public static Iterable<Object[]> argsProvider() {
            return preciseArgsProviderAsYearMonth();
        }
        
        @Test
        public void assertDoSharding() {
            doSharding();
        }
    }
    
    @RunWith(Parameterized.class)
    public static final class RangeShardingValueDoShardingTest extends AbstractRangeShardingValueParameterizedTest<YearMonth> {
        
        public RangeShardingValueDoShardingTest(final Range<YearMonth> input, final Collection<String> expected) {
            super(input, expected);
        }
        
        @Parameterized.Parameters(name = "{index}: doSharding({0})={1}")
        public static Iterable<Object[]> argsProvider() {
            List<Object[]> args = new LinkedList<>();
            Iterable<Object[]> iterable = rangeArgsProviderAsYearMonth();
            iterable.forEach(each -> {
                Range<YearMonth> shardingValue = (Range<YearMonth>) each[0];
                ExactCollection<String> expect = (ExactCollection<String>) each[1];
                if (shardingValue.equals(Range.lessThan(YearMonth.of(2021, 5)))) {
                    expect = new ExactCollection<>("table_202101", "table_202102", "table_202103", "table_202104");
                }
                args.add(new Object[]{shardingValue, expect});
            });
            return args;
        }
        
        @Test
        public void assertDoSharding() {
            doSharding();
        }
    }
}
