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
import org.apache.shardingsphere.sharding.cosid.algorithm.sharding.interval.fixture.IntervalShardingAlgorithmDataFixture;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Collection;

public final class StringCosIdIntervalShardingAlgorithmTest {
    
    static Iterable<Object[]> preciseArgsProviderAsString() {
        return IntervalShardingAlgorithmDataFixture.preciseArgsProvider(ldt -> ldt.format(CosIdIntervalShardingAlgorithm.DEFAULT_DATE_TIME_FORMATTER));
    }
    
    static Iterable<Object[]> rangeArgsProviderAsString() {
        return IntervalShardingAlgorithmDataFixture.rangeArgsProvider(ldt -> ldt.format(CosIdIntervalShardingAlgorithm.DEFAULT_DATE_TIME_FORMATTER));
    }
    
    @RunWith(Parameterized.class)
    public static final class PreciseShardingValueDoShardingTest extends AbstractPreciseShardingValueParameterizedTest<String> {
        
        public PreciseShardingValueDoShardingTest(final String input, final String expected) {
            super(input, expected);
        }
        
        @Parameters(name = "{index}: doSharding({0})={1}")
        public static Iterable<Object[]> argsProvider() {
            return preciseArgsProviderAsString();
        }
        
        @Test
        public void assertDoSharding() {
            doSharding();
        }
    }
    
    @RunWith(Parameterized.class)
    public static final class RangeShardingValueDoShardingTest extends AbstractRangeShardingValueParameterizedTest<String> {
        
        public RangeShardingValueDoShardingTest(final Range<String> input, final Collection<String> expected) {
            super(input, expected);
        }
        
        @Parameters(name = "{index}: doSharding({0})={1}")
        public static Iterable<Object[]> argsProvider() {
            return rangeArgsProviderAsString();
        }
        
        @Test
        public void assertDoSharding() {
            doSharding();
        }
    }
}
