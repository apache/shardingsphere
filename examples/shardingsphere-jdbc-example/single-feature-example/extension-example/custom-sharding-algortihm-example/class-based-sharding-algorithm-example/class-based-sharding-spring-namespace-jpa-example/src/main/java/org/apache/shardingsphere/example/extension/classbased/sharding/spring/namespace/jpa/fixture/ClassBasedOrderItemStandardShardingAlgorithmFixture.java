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

package org.apache.shardingsphere.example.extension.classbased.sharding.spring.namespace.jpa.fixture;

import com.google.common.base.Preconditions;
import com.google.common.primitives.Ints;
import lombok.Getter;
import org.apache.shardingsphere.sharding.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.RangeShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.StandardShardingAlgorithm;

import java.util.Collection;
import java.util.Properties;

public final class ClassBasedOrderItemStandardShardingAlgorithmFixture implements StandardShardingAlgorithm<Long> {
    
    private static final String SHARDING_COUNT = "sharding-count";
    
    @Getter
    private Properties props;
    
    private Integer shardingCount;
    
    @Override
    public void init(final Properties props) {
        this.props = props;
        Preconditions.checkArgument(props.containsKey(SHARDING_COUNT), "%s can not be null", SHARDING_COUNT);
        shardingCount = Ints.tryParse(props.getProperty(SHARDING_COUNT));
        Preconditions.checkArgument(null != shardingCount, "%s is invalid", SHARDING_COUNT);
    }
    
    @Override
    public String doSharding(final Collection<String> availableTargetNames, final PreciseShardingValue<Long> shardingValue) {
        for (String each : availableTargetNames) {
            if (each.endsWith(String.valueOf(shardingValue.getValue() % shardingCount))) {
                return each;
            }
        }
        return null;
    }
    
    @Override
    public Collection<String> doSharding(final Collection<String> availableTargetNames, final RangeShardingValue<Long> shardingValue) {
        return availableTargetNames;
    }
}
