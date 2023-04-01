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

package org.apache.shardingsphere.data.pipeline.core.ratelimit;

import org.apache.shardingsphere.data.pipeline.core.exception.job.ratelimit.JobRateLimitAlgorithmInitializationException;
import org.apache.shardingsphere.data.pipeline.spi.ratelimit.JobRateLimitAlgorithm;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.util.spi.type.typed.algorithm.ShardingSphereAlgorithm;
import org.apache.shardingsphere.sharding.algorithm.sharding.mod.HashModShardingAlgorithm;
import org.apache.shardingsphere.sharding.exception.algorithm.sharding.ShardingAlgorithmInitializationException;
import org.apache.shardingsphere.sharding.spi.ShardingAlgorithm;
import org.apache.shardingsphere.test.util.PropertiesBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class QPSJobRateLimitAlgorithmTest {
    
    private QPSJobRateLimitAlgorithm qpsJobRateLimitAlgorithm;
    
    private static final String QPS_KEY = "qps";
    
    @BeforeEach
    void setup() {
        qpsJobRateLimitAlgorithm = (QPSJobRateLimitAlgorithm) TypedSPILoader.getService(JobRateLimitAlgorithm.class, "QPS");
    }
    
    @Test
    void assertJobRateLimitWithWrongArgumentForQPS() {
        Properties props = PropertiesBuilder.build(new PropertiesBuilder.Property(QPS_KEY, "0"));
        assertThrows(JobRateLimitAlgorithmInitializationException.class, () -> TypedSPILoader.getService(JobRateLimitAlgorithm.class, "QPS", props));
    }
}
