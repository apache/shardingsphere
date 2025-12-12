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

package org.apache.shardingsphere.data.pipeline.scenario.consistencycheck;

import org.apache.shardingsphere.data.pipeline.core.job.config.PipelineJobConfiguration;
import org.apache.shardingsphere.data.pipeline.core.job.progress.yaml.swapper.YamlConsistencyCheckJobItemProgressSwapper;
import org.apache.shardingsphere.data.pipeline.core.job.type.PipelineJobOption;
import org.apache.shardingsphere.data.pipeline.core.job.type.PipelineJobType;
import org.apache.shardingsphere.data.pipeline.scenario.consistencycheck.config.yaml.swapper.YamlConsistencyCheckJobConfigurationSwapper;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class ConsistencyCheckJobTypeTest {
    
    @SuppressWarnings("rawtypes")
    private final PipelineJobType jobType = TypedSPILoader.getService(PipelineJobType.class, "CONSISTENCY_CHECK");
    
    @Test
    void assertGetOption() {
        PipelineJobOption actual = jobType.getOption();
        assertThat(actual.getCode(), is("02"));
        assertFalse(actual.isTransmissionJob());
        assertThat(actual.getYamlJobConfigurationSwapper(), isA(YamlConsistencyCheckJobConfigurationSwapper.class));
        assertThat(actual.getYamlJobItemProgressSwapper(), isA(YamlConsistencyCheckJobItemProgressSwapper.class));
        assertThat(actual.getJobClass(), is(ConsistencyCheckJob.class));
        assertTrue(actual.isIgnoreToStartDisabledJobWhenJobItemProgressIsFinished());
        assertNull(actual.getToBeStartDisabledNextJobType());
        assertNull(actual.getToBeStoppedPreviousJobType());
        assertFalse(actual.isForceNoShardingWhenConvertToJobConfigurationPOJO());
    }
    
    @SuppressWarnings("unchecked")
    @Test
    void assertGetJobTarget() {
        assertNull(jobType.getJobTarget(mock(PipelineJobConfiguration.class)));
    }
}
