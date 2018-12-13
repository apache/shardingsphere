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

package io.shardingsphere.proxy;

import io.shardingsphere.api.algorithm.sharding.PreciseShardingValue;
import io.shardingsphere.api.algorithm.sharding.standard.PreciseShardingAlgorithm;
import io.shardingsphere.core.exception.ShardingException;

import java.util.Collection;

public class DBAlgorithm implements PreciseShardingAlgorithm<String> {
    
    @Override
    public String doSharding(final Collection<String> availableTargetNames, final PreciseShardingValue<String> shardingValue) {
        for (String each : availableTargetNames) {
//            String tableNameNum = shardingValue.getValue().substring(12, 16);
//            Integer dbNameNum = Integer.parseInt(tableNameNum)/200+1;
//            if (each.endsWith(String.valueOf(dbNameNum))) {
//                return each;
//            }
            if ("5".equals(shardingValue.getValue())) {
                return "5";
            }
        }
        throw new ShardingException("no!");
//        throw new UnsupportedOperationException();
    }
}
