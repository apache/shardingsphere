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

package org.apache.shardingsphere.data.pipeline.spi.ratelimit;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmFactory;
import org.apache.shardingsphere.infra.util.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPIRegistry;

/**
 * Job rate limit algorithm factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JobRateLimitAlgorithmFactory {
    
    static {
        ShardingSphereServiceLoader.register(JobRateLimitAlgorithm.class);
    }
    
    /**
     * Create new instance of job rate limit algorithm.
     *
     * @param jobRateLimitAlgorithmConfig job rate limit algorithm configuration
     * @return created instance
     */
    public static JobRateLimitAlgorithm newInstance(final AlgorithmConfiguration jobRateLimitAlgorithmConfig) {
        return ShardingSphereAlgorithmFactory.createAlgorithm(jobRateLimitAlgorithmConfig, JobRateLimitAlgorithm.class);
    }
    
    /**
     * Judge whether contains job rate limit algorithm.
     *
     * @param jobRateLimitAlgorithmType job rate limit algorithm type
     * @return contains job rate limit algorithm or not
     */
    public static boolean contains(final String jobRateLimitAlgorithmType) {
        return TypedSPIRegistry.findRegisteredService(JobRateLimitAlgorithm.class, jobRateLimitAlgorithmType).isPresent();
    }
}
