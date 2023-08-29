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

package org.apache.shardingsphere.data.pipeline.common.ratelimit;

import org.apache.shardingsphere.data.pipeline.api.job.JobOperationType;
import org.apache.shardingsphere.data.pipeline.core.exception.job.ratelimit.JobRateLimitAlgorithmInitializationException;
import org.apache.shardingsphere.data.pipeline.spi.ratelimit.JobRateLimitAlgorithm;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.test.util.PropertiesBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TPSJobRateLimitAlgorithmTest {
    
    private TPSJobRateLimitAlgorithm tpsJobRateLimitAlgorithm;
    
    @BeforeEach
    void setup() {
        tpsJobRateLimitAlgorithm = (TPSJobRateLimitAlgorithm) TypedSPILoader.getService(JobRateLimitAlgorithm.class, "TPS");
    }
    
    @Test
    void assertInit() {
        Properties props = PropertiesBuilder.build(new PropertiesBuilder.Property("tps", "1"));
        assertThat(TypedSPILoader.getService(JobRateLimitAlgorithm.class, "TPS", props), instanceOf(TPSJobRateLimitAlgorithm.class));
    }
    
    @Test
    void assertJobRateLimitWithWrongArgumentForTPS() {
        Properties props = PropertiesBuilder.build(new PropertiesBuilder.Property("tps", "0"));
        assertThrows(JobRateLimitAlgorithmInitializationException.class, () -> TypedSPILoader.getService(JobRateLimitAlgorithm.class, "TPS", props));
    }
    
    @Test
    void assertIntercept() {
        assertDoesNotThrow(() -> tpsJobRateLimitAlgorithm.intercept(JobOperationType.UPDATE, 1));
    }
}
