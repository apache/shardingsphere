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

package org.apache.shardingsphere.example.extension.spibased.sharding.spring.boot.mybatis.fixture;

import org.apache.shardingsphere.sharding.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.RangeShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.StandardShardingAlgorithm;

import java.util.Collection;

public final class SPIBasedDatasourceStandardShardingAlgorithmFixture implements StandardShardingAlgorithm<Integer> {
    
    @Override
    public String doSharding(final Collection<String> dataSourceNames, final PreciseShardingValue<Integer> shardingValue) {
        for (String each : dataSourceNames) {
            if (each.endsWith(shardingSuffix(shardingValue.getValue()))) {
                return each;
            }
        }
        return null;
    }
    
    private String shardingSuffix(final Integer shardingValue) {
        return "-" + (shardingValue % 2);
    }
    
    @Override
    public Collection<String> doSharding(final Collection<String> availableTargetNames, final RangeShardingValue<Integer> shardingValue) {
        return availableTargetNames;
    }
    
    @Override
    public void init() {
    }
    
    @Override
    public String getType() {
        return "DATASOURCE_SPI_BASED";
    }
}
