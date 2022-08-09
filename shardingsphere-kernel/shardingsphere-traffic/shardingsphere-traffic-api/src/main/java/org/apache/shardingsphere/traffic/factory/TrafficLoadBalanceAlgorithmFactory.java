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

package org.apache.shardingsphere.traffic.factory;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmFactory;
import org.apache.shardingsphere.infra.util.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPIRegistry;
import org.apache.shardingsphere.traffic.spi.TrafficLoadBalanceAlgorithm;

/**
 * Traffic load balance algorithm factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TrafficLoadBalanceAlgorithmFactory {
    
    static {
        ShardingSphereServiceLoader.register(TrafficLoadBalanceAlgorithm.class);
    }
    
    /**
     * Create new instance of traffic load balance algorithm.
     * 
     * @param trafficLoadBalanceAlgorithmConfig traffic load balance algorithm configuration
     * @return created instance
     */
    public static TrafficLoadBalanceAlgorithm newInstance(final AlgorithmConfiguration trafficLoadBalanceAlgorithmConfig) {
        return ShardingSphereAlgorithmFactory.createAlgorithm(trafficLoadBalanceAlgorithmConfig, TrafficLoadBalanceAlgorithm.class);
    }
    
    /**
     * Judge whether contains traffic load balance algorithm.
     *
     * @param trafficLoadBalanceAlgorithmType traffic load balance algorithm type
     * @return contains traffic load balance algorithm or not
     */
    public static boolean contains(final String trafficLoadBalanceAlgorithmType) {
        return TypedSPIRegistry.findRegisteredService(TrafficLoadBalanceAlgorithm.class, trafficLoadBalanceAlgorithmType).isPresent();
    }
}
