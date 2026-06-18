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

package org.apache.shardingsphere.data.pipeline.scenario.consistencycheck.metadata.processor;

import org.apache.shardingsphere.data.pipeline.core.metadata.node.config.processor.JobConfigurationChangedProcessor;
import org.apache.shardingsphere.data.pipeline.scenario.consistencycheck.ConsistencyCheckJob;
import org.apache.shardingsphere.data.pipeline.scenario.consistencycheck.config.ConsistencyCheckJobConfiguration;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.mockito.Mockito.mock;

class ConsistencyCheckJobConfigurationChangedProcessorTest {
    
    @SuppressWarnings("rawtypes")
    private final JobConfigurationChangedProcessor processor = TypedSPILoader.getService(JobConfigurationChangedProcessor.class, "CONSISTENCY_CHECK");
    
    @SuppressWarnings("unchecked")
    @Test
    void assertCreateJobAndGetType() {
        assertThat(processor.createJob(mock(ConsistencyCheckJobConfiguration.class)), isA(ConsistencyCheckJob.class));
    }
}
