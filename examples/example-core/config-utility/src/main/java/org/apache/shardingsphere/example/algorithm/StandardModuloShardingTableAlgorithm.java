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

package org.apache.shardingsphere.example.algorithm;

import com.google.common.collect.Range;
import org.apache.shardingsphere.sharding.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.RangeShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.StandardShardingAlgorithm;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

public final class StandardModuloShardingTableAlgorithm implements StandardShardingAlgorithm<Long> {
    
    @Override
    public void init() {
    }
    
    @Override
    public String doSharding(final Collection<String> tableNames, final PreciseShardingValue<Long> shardingValue) {
        for (String each : tableNames) {
            if (each.endsWith(String.valueOf(shardingValue.getValue() % 2))) {
                return each;
            }
        }
        throw new UnsupportedOperationException("");
    }
    
    @Override
    public Collection<String> doSharding(final Collection<String> tableNames, final RangeShardingValue<Long> shardingValue) {
        Set<String> result = new LinkedHashSet<>();
        if (Range.closed(200000000000000000L, 400000000000000000L).encloses(shardingValue.getValueRange())) {
            for (String each : tableNames) {
                if (each.endsWith("0")) {
                    result.add(each);
                }
            }
        } else {
            throw new UnsupportedOperationException("");
        }
        return result;
    }
    
    @Override
    public String getType() {
        return "STANDARD_TEST_TBL";
    }
}
