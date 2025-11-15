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

package org.apache.shardingsphere.data.pipeline.cdc;

import org.apache.shardingsphere.data.pipeline.cdc.config.CDCJobConfiguration;
import org.apache.shardingsphere.data.pipeline.cdc.config.yaml.swapper.YamlCDCJobConfigurationSwapper;
import org.apache.shardingsphere.data.pipeline.core.job.progress.yaml.swapper.YamlTransmissionJobItemProgressSwapper;
import org.apache.shardingsphere.data.pipeline.core.job.type.PipelineJobOption;
import org.apache.shardingsphere.data.pipeline.core.job.type.PipelineJobType;
import org.apache.shardingsphere.data.pipeline.core.pojo.PipelineJobTarget;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CDCJobTypeTest {
    
    @SuppressWarnings("rawtypes")
    private final PipelineJobType jobType = TypedSPILoader.getService(PipelineJobType.class, "STREAMING");
    
    @Test
    void assertGetOption() {
        PipelineJobOption actual = jobType.getOption();
        assertThat(actual.getCode(), is("03"));
        assertTrue(actual.isTransmissionJob());
        assertThat(actual.getYamlJobConfigurationSwapper(), isA(YamlCDCJobConfigurationSwapper.class));
        assertThat(actual.getYamlJobItemProgressSwapper(), isA(YamlTransmissionJobItemProgressSwapper.class));
        assertThat(actual.getJobClass(), is(CDCJob.class));
        assertFalse(actual.isIgnoreToStartDisabledJobWhenJobItemProgressIsFinished());
        assertNull(actual.getToBeStartDisabledNextJobType());
        assertNull(actual.getToBeStoppedPreviousJobType());
        assertTrue(actual.isForceNoShardingWhenConvertToJobConfigurationPOJO());
    }
    
    @SuppressWarnings("unchecked")
    @Test
    void assertGetJobTarget() {
        CDCJobConfiguration jobConfig = mock(CDCJobConfiguration.class);
        when(jobConfig.getDatabaseName()).thenReturn("foo_db");
        when(jobConfig.getSchemaTableNames()).thenReturn(Arrays.asList("foo_schema.foo_tbl", "bar_schema.bar_tbl"));
        PipelineJobTarget actual = jobType.getJobTarget(jobConfig);
        assertThat(actual.getDatabaseName(), is("foo_db"));
        assertThat(actual.getTableName(), is("foo_schema.foo_tbl, bar_schema.bar_tbl"));
    }
}
