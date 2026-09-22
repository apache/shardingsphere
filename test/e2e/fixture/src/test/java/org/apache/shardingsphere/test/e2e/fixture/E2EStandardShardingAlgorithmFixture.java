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

package org.apache.shardingsphere.test.e2e.fixture;

import org.apache.shardingsphere.sharding.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.RangeShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.StandardShardingAlgorithm;

import java.math.BigInteger;
import java.util.Collection;
import java.util.LinkedHashSet;

public final class E2EStandardShardingAlgorithmFixture implements StandardShardingAlgorithm<Comparable<?>> {
    
    @Override
    public String doSharding(final Collection<String> availableTargetNames, final PreciseShardingValue<Comparable<?>> shardingValue) {
        long value = ((Number) shardingValue.getValue()).longValue();
        for (String each : availableTargetNames) {
            if (each.endsWith(String.valueOf(value % 10L))) {
                return each;
            }
        }
        throw new UnsupportedOperationException("");
    }
    
    @Override
    public Collection<String> doSharding(final Collection<String> availableTargetNames, final RangeShardingValue<Comparable<?>> shardingValue) {
        Collection<String> result = new LinkedHashSet<>(availableTargetNames.size(), 1F);
        long minValue = shardingValue.getValueRange().hasLowerBound() ? ((Number) shardingValue.getValueRange().lowerEndpoint()).longValue() : Integer.MIN_VALUE;
        long maxValue = shardingValue.getValueRange().hasUpperBound() ? ((Number) shardingValue.getValueRange().upperEndpoint()).longValue() : Integer.MAX_VALUE;
        long range = BigInteger.valueOf(maxValue).subtract(BigInteger.valueOf(minValue)).longValue();
        int begin = (int) (Math.abs(minValue) % 10L);
        if (range > 9L) {
            return availableTargetNames;
        }
        for (int i = begin; i <= range; i += 1) {
            for (String each : availableTargetNames) {
                if (each.endsWith(String.valueOf(i))) {
                    result.add(each);
                }
            }
        }
        return result;
    }
    
    @Override
    public String getType() {
        return "E2E.SHARDING.STANDARD.FIXTURE";
    }
}
