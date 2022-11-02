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
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.datanode.DataNodeInfo;
import org.apache.shardingsphere.sharding.api.sharding.standard.RangeShardingValue;
import org.apache.shardingsphere.sharding.cosid.algorithm.sharding.interval.fixture.IntervalShardingAlgorithmDataFixture;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import org.junit.Before;

@RequiredArgsConstructor
public abstract class AbstractRangeShardingValueParameterizedTest<T extends Comparable<?>> {
    
    private final Range<T> input;
    
    private final Collection<String> expected;
    
    private CosIdIntervalShardingAlgorithm algorithm;
    
    @Before
    public void init() {
        algorithm = IntervalShardingAlgorithmDataFixture.createShardingAlgorithm();
    }
    
    /**
     * Do sharding test.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    protected void doSharding() {
        RangeShardingValue shardingValue = new RangeShardingValue<>(
                IntervalShardingAlgorithmDataFixture.LOGIC_NAME,
                IntervalShardingAlgorithmDataFixture.COLUMN_NAME,
                new DataNodeInfo(IntervalShardingAlgorithmDataFixture.LOGIC_NAME_PREFIX, 6, '0'),
                input);
        Collection<String> actual = algorithm.doSharding(IntervalShardingAlgorithmDataFixture.ALL_NODES, shardingValue);
        assertThat(actual, is(expected));
    }
}
