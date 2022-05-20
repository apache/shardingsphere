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

package org.apache.shardingsphere.readwritesplitting.algorithm.loadbalance;

import lombok.Getter;
import org.apache.shardingsphere.readwritesplitting.spi.ReplicaLoadBalanceAlgorithm;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Select-to-one-slave-round-robin replica load-balance algorithm.
 */
public final class SelectToOneSlaveRoundRobinReplicaLoadBalanceAlgorithm implements ReplicaLoadBalanceAlgorithm {
    
    private static final ThreadLocal<String> SLAVE_ROUTE_HOLDER = new ThreadLocal<>();
    
    private final AtomicInteger count = new AtomicInteger(0);
    
    @Getter
    private Properties props;
    
    @Override
    public void init(final Properties props) {
        this.props = props;
    }
    
    @Override
    public String getDataSource(final String name, final String writeDataSourceName, final List<String> readDataSourceNames) {
        if (null == SLAVE_ROUTE_HOLDER.get()) {
            SLAVE_ROUTE_HOLDER.set(readDataSourceNames.get(Math.abs(count.getAndIncrement()) % readDataSourceNames.size()));
        }
        return SLAVE_ROUTE_HOLDER.get();
    }
    
    @Override
    public String getType() {
        return "SELECT_TO_ONE_SLAVE_ROUND_ROBIN";
    }
    
    @Override
    public boolean isDefault() {
        return true;
    }
}
