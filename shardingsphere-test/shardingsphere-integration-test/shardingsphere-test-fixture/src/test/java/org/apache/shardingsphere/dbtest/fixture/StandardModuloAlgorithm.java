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

package org.apache.shardingsphere.dbtest.fixture;

import org.apache.shardingsphere.sharding.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.RangeShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.StandardShardingAlgorithm;

import java.math.BigInteger;
import java.util.Collection;
import java.util.LinkedHashSet;

public final class StandardModuloAlgorithm implements StandardShardingAlgorithm<Integer> {
    
    @Override
    public void init() {
    }
    
    @Override
    public String doSharding(final Collection<String> availableTargetNames, final PreciseShardingValue<Integer> shardingValue) {
        for (String each : availableTargetNames) {
            if (each.endsWith(String.valueOf(shardingValue.getValue() % 10))) {
                return each;
            }
        }
        throw new UnsupportedOperationException("");
    }
    
    @Override
    public Collection<String> doSharding(final Collection<String> availableTargetNames, final RangeShardingValue<Integer> shardingValue) {
        Collection<String> result = new LinkedHashSet<>(availableTargetNames.size());
        int minValue = shardingValue.getValueRange().hasLowerBound() ? shardingValue.getValueRange().lowerEndpoint() : Integer.MIN_VALUE;
        int maxValue = shardingValue.getValueRange().hasUpperBound() ? shardingValue.getValueRange().upperEndpoint() : Integer.MAX_VALUE;
        long range = BigInteger.valueOf(maxValue).subtract(BigInteger.valueOf(minValue)).longValue();
        int begin = Math.abs(minValue) % 10;
        if (range > 9) {
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
        return "STANDARD_TEST";
    }
}
