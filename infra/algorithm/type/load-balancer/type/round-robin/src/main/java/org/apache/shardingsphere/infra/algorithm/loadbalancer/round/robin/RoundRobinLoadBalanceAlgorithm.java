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

package org.apache.shardingsphere.infra.algorithm.loadbalancer.round.robin;

import org.apache.shardingsphere.infra.algorithm.loadbalancer.spi.LoadBalanceAlgorithm;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Round-robin load balance algorithm.
 */
public final class RoundRobinLoadBalanceAlgorithm implements LoadBalanceAlgorithm {
    
    private final AtomicInteger count = new AtomicInteger(0);
    
    @HighFrequencyInvocation
    @Override
    public String getTargetName(final String groupName, final List<String> availableTargetNames) {
        return availableTargetNames.get(Math.abs(count.getAndIncrement()) % availableTargetNames.size());
    }
    
    @Override
    public String getType() {
        return "ROUND_ROBIN";
    }
    
    @Override
    public boolean isDefault() {
        return true;
    }
}
