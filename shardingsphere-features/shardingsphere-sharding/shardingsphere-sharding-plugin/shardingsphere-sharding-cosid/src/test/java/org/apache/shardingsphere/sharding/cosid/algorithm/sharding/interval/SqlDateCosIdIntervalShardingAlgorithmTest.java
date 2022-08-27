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
import java.util.Collection;
import java.sql.Date;
import org.apache.shardingsphere.sharding.cosid.algorithm.sharding.interval.fixture.IntervalShardingAlgorithmDataFixture;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

public final class SqlDateCosIdIntervalShardingAlgorithmTest {
    
    static Iterable<Object[]> preciseArgsProviderAsDate() {
        return IntervalShardingAlgorithmDataFixture.preciseArgsProvider(ldt -> new Date(ldt.toInstant(IntervalShardingAlgorithmDataFixture.ZONE_OFFSET_SHANGHAI).toEpochMilli()));
    }
    
    static Iterable<Object[]> rangeArgsProviderAsDate() {
        return IntervalShardingAlgorithmDataFixture.rangeArgsProvider(ldt -> new Date(ldt.toInstant(IntervalShardingAlgorithmDataFixture.ZONE_OFFSET_SHANGHAI).toEpochMilli()));
    }
    
    @RunWith(Parameterized.class)
    public static final class PreciseShardingValueDoShardingTest extends AbstractPreciseShardingValueParameterizedTest<Date> {
        
        public PreciseShardingValueDoShardingTest(final Date input, final String expected) {
            super(input, expected);
        }
        
        @Parameterized.Parameters(name = "{index}: doSharding({0})={1}")
        public static Iterable<Object[]> argsProvider() {
            return preciseArgsProviderAsDate();
        }
        
        @Test
        public void assertDoSharding() {
            doSharding();
        }
    }
    
    @RunWith(Parameterized.class)
    public static final class RangeShardingValueDoShardingTest extends AbstractRangeShardingValueParameterizedTest<Date> {
        
        public RangeShardingValueDoShardingTest(final Range<Date> input, final Collection<String> expected) {
            super(input, expected);
        }
        
        @Parameterized.Parameters(name = "{index}: doSharding({0})={1}")
        public static Iterable<Object[]> argsProvider() {
            return rangeArgsProviderAsDate();
        }
        
        @Test
        public void assertDoSharding() {
            doSharding();
        }
    }
}
