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

package org.apache.shardingsphere.sharding.cosid.algorithm.sharding.mod;

import com.google.common.collect.Range;
import lombok.RequiredArgsConstructor;
import me.ahoo.cosid.sharding.ExactCollection;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.datanode.DataNodeInfo;
import org.apache.shardingsphere.sharding.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.RangeShardingValue;
import org.apache.shardingsphere.sharding.cosid.algorithm.Arguments;
import org.apache.shardingsphere.sharding.cosid.algorithm.CosIdAlgorithmConstants;
import org.apache.shardingsphere.sharding.factory.ShardingAlgorithmFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Collection;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public final class CosIdModShardingAlgorithmTest {
    
    static final int DIVISOR = 4;
    
    static final String LOGIC_NAME = "t_mod";
    
    static final String COLUMN_NAME = "id";
    
    static final String LOGIC_NAME_PREFIX = LOGIC_NAME + "_";
    
    static final ExactCollection<String> ALL_NODES = new ExactCollection<>("t_mod_0", "t_mod_1", "t_mod_2", "t_mod_3");
    
    @SuppressWarnings("unchecked")
    static CosIdModShardingAlgorithm<Long> createShardingAlgorithm() {
        return (CosIdModShardingAlgorithm<Long>) ShardingAlgorithmFactory.newInstance(new AlgorithmConfiguration("COSID_MOD", createProperties()));
    }
    
    private static Properties createProperties() {
        Properties result = new Properties();
        result.setProperty(CosIdAlgorithmConstants.LOGIC_NAME_PREFIX_KEY, LOGIC_NAME_PREFIX);
        result.put("mod", DIVISOR);
        return result;
    }
    
    @RunWith(Parameterized.class)
    @RequiredArgsConstructor
    public static class PreciseValueDoShardingTest {
        
        private final long id;
        
        private CosIdModShardingAlgorithm<Long> algorithm;
        
        @Before
        public void init() {
            algorithm = createShardingAlgorithm();
        }
        
        @Parameters
        public static Number[] argsProvider() {
            return Arguments.of(1L, 2L, 3L, 4L, 5L, 6L);
        }
        
        @Test
        public void assertDoSharding() {
            PreciseShardingValue<Long> shardingValue = new PreciseShardingValue<>(LOGIC_NAME, COLUMN_NAME, new DataNodeInfo(LOGIC_NAME_PREFIX, 1, '0'), id);
            String actual = algorithm.doSharding(ALL_NODES, shardingValue);
            String expected = LOGIC_NAME_PREFIX + (id % DIVISOR);
            assertThat(actual, is(expected));
        }
    }
    
    @RunWith(Parameterized.class)
    @RequiredArgsConstructor
    public static class RangeValueDoShardingTest {
        
        private final Range<Long> rangeValue;
        
        private final Collection<String> expected;
        
        private CosIdModShardingAlgorithm<Long> algorithm;
        
        @Before
        public void init() {
            algorithm = createShardingAlgorithm();
        }
        
        @Parameters
        public static Iterable<Object[]> argsProvider() {
            return Arguments.ofArrayElement(Arguments.of(Range.all(), ALL_NODES),
                    Arguments.of(Range.closed(1L, 3L), new ExactCollection<>("t_mod_1", "t_mod_2", "t_mod_3")),
                    Arguments.of(Range.closed(0L, 3L), ALL_NODES), Arguments.of(Range.closed(0L, 4L), ALL_NODES),
                    Arguments.of(Range.closed(0L, 1L), new ExactCollection<>("t_mod_0", "t_mod_1")),
                    Arguments.of(Range.closed(3L, 4L), new ExactCollection<>("t_mod_0", "t_mod_3")),
                    Arguments.of(Range.closedOpen(1L, 3L), new ExactCollection<>("t_mod_1", "t_mod_2")),
                    Arguments.of(Range.closedOpen(0L, 3L), new ExactCollection<>("t_mod_0", "t_mod_1", "t_mod_2")),
                    Arguments.of(Range.closedOpen(0L, 4L), ALL_NODES),
                    Arguments.of(Range.closedOpen(0L, 1L), new ExactCollection<>("t_mod_0")),
                    Arguments.of(Range.closedOpen(3L, 4L), new ExactCollection<>("t_mod_3")),
                    Arguments.of(Range.openClosed(1L, 3L), new ExactCollection<>("t_mod_2", "t_mod_3")),
                    Arguments.of(Range.openClosed(0L, 3L), new ExactCollection<>("t_mod_1", "t_mod_2", "t_mod_3")),
                    Arguments.of(Range.openClosed(0L, 4L), ALL_NODES),
                    Arguments.of(Range.openClosed(0L, 1L), new ExactCollection<>("t_mod_1")),
                    Arguments.of(Range.openClosed(3L, 4L), new ExactCollection<>("t_mod_0")),
                    Arguments.of(Range.open(1L, 3L), new ExactCollection<>("t_mod_2")),
                    Arguments.of(Range.open(0L, 3L), new ExactCollection<>("t_mod_1", "t_mod_2")),
                    Arguments.of(Range.open(0L, 4L), new ExactCollection<>("t_mod_1", "t_mod_2", "t_mod_3")),
                    Arguments.of(Range.open(0L, 1L), ExactCollection.empty()),
                    Arguments.of(Range.open(3L, 4L), ExactCollection.empty()),
                    Arguments.of(Range.greaterThan(0L), ALL_NODES),
                    Arguments.of(Range.greaterThan(1L), ALL_NODES),
                    Arguments.of(Range.greaterThan(2L), ALL_NODES),
                    Arguments.of(Range.greaterThan(3L), ALL_NODES),
                    Arguments.of(Range.greaterThan(4L), ALL_NODES),
                    Arguments.of(Range.greaterThan(5L), ALL_NODES),
                    Arguments.of(Range.atLeast(0L), ALL_NODES),
                    Arguments.of(Range.atLeast(1L), ALL_NODES),
                    Arguments.of(Range.atLeast(2L), ALL_NODES),
                    Arguments.of(Range.atLeast(3L), ALL_NODES),
                    Arguments.of(Range.atLeast(4L), ALL_NODES),
                    Arguments.of(Range.atLeast(5L), ALL_NODES),
                    Arguments.of(Range.lessThan(0L), ExactCollection.empty()),
                    Arguments.of(Range.lessThan(1L), new ExactCollection<>("t_mod_0")),
                    Arguments.of(Range.lessThan(2L), new ExactCollection<>("t_mod_0", "t_mod_1")),
                    Arguments.of(Range.lessThan(3L), new ExactCollection<>("t_mod_0", "t_mod_1", "t_mod_2")),
                    Arguments.of(Range.lessThan(4L), ALL_NODES), Arguments.of(Range.lessThan(5L), ALL_NODES),
                    Arguments.of(Range.atMost(0L), new ExactCollection<>("t_mod_0")),
                    Arguments.of(Range.atMost(1L), new ExactCollection<>("t_mod_0", "t_mod_1")),
                    Arguments.of(Range.atMost(2L), new ExactCollection<>("t_mod_0", "t_mod_1", "t_mod_2")),
                    Arguments.of(Range.atMost(3L), ALL_NODES), Arguments.of(Range.atMost(4L), ALL_NODES),
                    Arguments.of(Range.atMost(5L), ALL_NODES));
        }
        
        @Test
        public void assertDoSharding() {
            RangeShardingValue<Long> shardingValue = new RangeShardingValue<>(LOGIC_NAME, COLUMN_NAME, new DataNodeInfo(LOGIC_NAME_PREFIX, 1, '0'), rangeValue);
            Collection<String> actual = algorithm.doSharding(ALL_NODES, shardingValue);
            assertThat(actual, is(expected));
        }
    }
}
