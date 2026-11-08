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

package org.apache.shardingsphere.data.pipeline.core.ratelimit.type;

import org.apache.shardingsphere.data.pipeline.core.constant.PipelineSQLOperationType;
import org.apache.shardingsphere.data.pipeline.core.ratelimit.JobRateLimitAlgorithm;
import org.apache.shardingsphere.infra.algorithm.core.exception.AlgorithmInitializationException;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder.Property;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TPSJobRateLimitAlgorithmTest {
    
    private final JobRateLimitAlgorithm algorithm = TypedSPILoader.getService(JobRateLimitAlgorithm.class, "TPS");
    
    @Test
    void assertInitFailed() {
        Properties props = PropertiesBuilder.build(new Property("tps", "0"));
        assertThrows(AlgorithmInitializationException.class, () -> TypedSPILoader.getService(JobRateLimitAlgorithm.class, "TPS", props));
    }
    
    @Test
    void assertInitSuccess() {
        Properties props = PropertiesBuilder.build(new Property("tps", "1"));
        assertDoesNotThrow(() -> TypedSPILoader.getService(JobRateLimitAlgorithm.class, "TPS", props));
    }
    
    @Test
    void assertIntercept() {
        assertDoesNotThrow(() -> algorithm.intercept(PipelineSQLOperationType.INSERT, null));
        assertDoesNotThrow(() -> algorithm.intercept(PipelineSQLOperationType.UPDATE, 1));
        assertDoesNotThrow(() -> algorithm.intercept(PipelineSQLOperationType.DELETE, 2));
        assertDoesNotThrow(() -> algorithm.intercept(PipelineSQLOperationType.SELECT, null));
    }
}
