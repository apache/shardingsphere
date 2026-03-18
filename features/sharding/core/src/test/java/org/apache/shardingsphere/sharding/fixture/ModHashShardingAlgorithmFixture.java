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

package org.apache.shardingsphere.sharding.fixture;

import org.apache.shardingsphere.sharding.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.RangeShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.StandardShardingAlgorithm;

import java.util.Collection;
import java.util.HashSet;
import java.util.Properties;

public final class ModHashShardingAlgorithmFixture implements StandardShardingAlgorithm<Comparable<?>> {
    
    private int shardingCount = 8;
    
    @Override
    public void init(final Properties props) {
        shardingCount = Integer.parseInt(props.getProperty("sharding-count", "8"));
    }
    
    @Override
    public String doSharding(final Collection<String> availableTargetNames, final PreciseShardingValue<Comparable<?>> shardingValue) {
        int hashCode = shardingValue.getValue().toString().hashCode();
        int index = Math.abs(hashCode) % shardingCount;
        int i = 0;
        for (String each : availableTargetNames) {
            if (i == index) {
                return each;
            }
            i++;
        }
        return null;
    }
    
    @Override
    public Collection<String> doSharding(final Collection<String> availableTargetNames, final RangeShardingValue<Comparable<?>> shardingValue) {
        Collection<String> result = new HashSet<>(availableTargetNames.size(), 1F);
        result.addAll(availableTargetNames);
        return result;
    }
    
    @Override
    public String getType() {
        return "MOD.HASH.FIXTURE";
    }
}
