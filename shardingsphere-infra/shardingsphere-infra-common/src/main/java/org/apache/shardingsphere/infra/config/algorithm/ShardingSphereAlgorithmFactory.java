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

package org.apache.shardingsphere.infra.config.algorithm;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.config.TypedSPIConfiguration;
import org.apache.shardingsphere.infra.spi.type.TypedSPIRegistry;

/**
 * ShardingSphere algorithm factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShardingSphereAlgorithmFactory {
    
    /**
     * Create algorithm.
     * 
     * @param typedSPIConfig typed SPI configuration
     * @param algorithmClass algorithm class
     * @param <T> type of algorithm
     * @return algorithm
     */
    @SuppressWarnings("unchecked")
    public static <T extends ShardingSphereAlgorithm> T createAlgorithm(final TypedSPIConfiguration typedSPIConfig, final Class<? extends ShardingSphereAlgorithm> algorithmClass) {
        T result = (T) TypedSPIRegistry.getRegisteredService(algorithmClass, typedSPIConfig.getType(), typedSPIConfig.getProps());
        if (result instanceof ShardingSphereAlgorithmPostProcessor) {
            ((ShardingSphereAlgorithmPostProcessor) result).init();
        }
        return result;
    }
}
