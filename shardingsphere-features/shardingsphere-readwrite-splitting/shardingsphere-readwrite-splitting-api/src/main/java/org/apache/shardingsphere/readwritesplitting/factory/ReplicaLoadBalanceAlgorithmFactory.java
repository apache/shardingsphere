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

package org.apache.shardingsphere.readwritesplitting.factory;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmFactory;
import org.apache.shardingsphere.readwritesplitting.spi.ReadQueryLoadBalanceAlgorithm;
import org.apache.shardingsphere.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.spi.type.required.RequiredSPIRegistry;
import org.apache.shardingsphere.spi.type.typed.TypedSPIRegistry;

/**
 * Replica load-balance algorithm factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ReplicaLoadBalanceAlgorithmFactory {
    
    static {
        ShardingSphereServiceLoader.register(ReadQueryLoadBalanceAlgorithm.class);
    }
    
    /**
     * Create new instance of replica load-balance algorithm.
     *
     * @return created instance
     */
    public static ReadQueryLoadBalanceAlgorithm newInstance() {
        return RequiredSPIRegistry.getRegisteredService(ReadQueryLoadBalanceAlgorithm.class);
    }
    
    /**
     * Create new instance of replica load-balance algorithm.
     * 
     * @param replicaLoadBalanceAlgorithmConfig replica load-balance algorithm configuration
     * @return created instance
     */
    public static ReadQueryLoadBalanceAlgorithm newInstance(final ShardingSphereAlgorithmConfiguration replicaLoadBalanceAlgorithmConfig) {
        return ShardingSphereAlgorithmFactory.createAlgorithm(replicaLoadBalanceAlgorithmConfig, ReadQueryLoadBalanceAlgorithm.class);
    }
    
    /**
     * Judge whether contains load-balance algorithm.
     *
     * @param replicaLoadBalanceAlgorithmType replica load-balance algorithm type
     * @return contains replica load-balance algorithm or not
     */
    public static boolean contains(final String replicaLoadBalanceAlgorithmType) {
        return TypedSPIRegistry.findRegisteredService(ReadQueryLoadBalanceAlgorithm.class, replicaLoadBalanceAlgorithmType).isPresent();
    }
}
