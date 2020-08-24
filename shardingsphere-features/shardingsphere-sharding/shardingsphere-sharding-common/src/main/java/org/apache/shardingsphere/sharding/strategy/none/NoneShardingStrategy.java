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

package org.apache.shardingsphere.sharding.strategy.none;

import lombok.Getter;
import org.apache.shardingsphere.sharding.spi.ShardingAlgorithm;
import org.apache.shardingsphere.sharding.strategy.ShardingStrategy;
import org.apache.shardingsphere.sharding.strategy.value.RouteValue;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;

import java.util.Collection;
import java.util.Collections;

/**
 * None sharding strategy.
 */
@Getter
public final class NoneShardingStrategy implements ShardingStrategy {
    
    private final Collection<String> shardingColumns = Collections.emptyList();

    @Override
    public ShardingAlgorithm getShardingAlgorithm() {
        return null;
    }

    @Override
    public Collection<String> doSharding(final Collection<String> availableTargetNames, final Collection<RouteValue> shardingValues, final ConfigurationProperties props) {
        return availableTargetNames;
    }
}
