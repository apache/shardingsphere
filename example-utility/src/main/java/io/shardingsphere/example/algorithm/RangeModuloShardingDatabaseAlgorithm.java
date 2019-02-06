/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.example.algorithm;


import com.google.common.collect.Range;
import org.apache.shardingsphere.api.algorithm.sharding.standard.RangeShardingAlgorithm;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

public class RangeModuloShardingDatabaseAlgorithm implements RangeShardingAlgorithm<Integer> {
    
    @Override
    public Collection<String> doSharding(final Collection<String> databaseNames, final String logicTableName, final String columnName, final Range<Integer> shardingValueRange) {
        Set<String> result = new LinkedHashSet<>();
        if (Range.closed(1, 5).encloses(shardingValueRange)) {
            for (String each : databaseNames) {
                if (each.endsWith("0")) {
                    result.add(each);
                }
            }
        } else if (Range.closed(6, 10).encloses(shardingValueRange)) {
            for (String each : databaseNames) {
                if (each.endsWith("1")) {
                    result.add(each);
                }
            }
        } else if (Range.closed(1, 10).encloses(shardingValueRange)) {
            result.addAll(databaseNames);
        } else {
            throw new UnsupportedOperationException();
        }
        return result;
    }
}
