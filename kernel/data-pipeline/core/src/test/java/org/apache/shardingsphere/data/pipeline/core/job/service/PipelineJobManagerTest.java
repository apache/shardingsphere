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

package org.apache.shardingsphere.data.pipeline.core.job.service;

import org.apache.shardingsphere.data.pipeline.core.context.PipelineContextKey;
import org.apache.shardingsphere.data.pipeline.core.exception.job.PipelineJobNotFoundException;
import org.apache.shardingsphere.data.pipeline.core.job.api.PipelineAPIFactory;
import org.apache.shardingsphere.data.pipeline.core.job.config.PipelineJobConfiguration;
import org.apache.shardingsphere.data.pipeline.core.job.config.yaml.swapper.YamlPipelineJobConfigurationSwapper;
import org.apache.shardingsphere.data.pipeline.core.job.id.PipelineJobIdUtils;
import org.apache.shardingsphere.data.pipeline.core.job.type.PipelineJobOption;
import org.apache.shardingsphere.data.pipeline.core.job.type.PipelineJobType;
import org.apache.shardingsphere.data.pipeline.core.pojo.PipelineJobInfo;
import org.apache.shardingsphere.data.pipeline.core.pojo.PipelineJobTarget;
import org.apache.shardingsphere.elasticjob.infra.pojo.JobConfigurationPOJO;
import org.apache.shardingsphere.elasticjob.lite.lifecycle.api.JobStatisticsAPI;
import org.apache.shardingsphere.elasticjob.lite.lifecycle.domain.JobBriefInfo;
import org.apache.shardingsphere.infra.instance.metadata.InstanceType;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class PipelineJobManagerTest {
    
    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void assertGetJobInfosSkipsMissingJobConfiguration() {
        PipelineContextKey contextKey = new PipelineContextKey(InstanceType.PROXY);
        JobStatisticsAPI jobStatisticsAPI = mock(JobStatisticsAPI.class);
        when(jobStatisticsAPI.getAllJobsBriefInfo()).thenReturn(Arrays.asList(createJobBriefInfo("j0101p0000_active"), createJobBriefInfo("j0101p0000_missing")));
        PipelineJobType jobType = mock(PipelineJobType.class);
        PipelineJobType parsedJobType = mock(PipelineJobType.class);
        PipelineJobOption jobOption = mock(PipelineJobOption.class);
        when(jobType.getType()).thenReturn("FOO");
        when(parsedJobType.getType()).thenReturn("FOO");
        when(jobType.getOption()).thenReturn(jobOption);
        when(jobOption.isTransmissionJob()).thenReturn(true);
        YamlPipelineJobConfigurationSwapper swapper = mock(YamlPipelineJobConfigurationSwapper.class);
        when(jobOption.getYamlJobConfigurationSwapper()).thenReturn(swapper);
        PipelineJobConfiguration jobConfig = mock(PipelineJobConfiguration.class);
        doReturn(jobConfig).when(swapper).swapToObject("active_param");
        when(jobType.getJobTarget(jobConfig)).thenReturn(new PipelineJobTarget("foo_db", "foo_tbl"));
        JobConfigurationPOJO activeJobConfig = createJobConfiguration();
        try (
                MockedStatic<PipelineAPIFactory> apiFactory = mockStatic(PipelineAPIFactory.class);
                MockedStatic<PipelineJobIdUtils> jobIdUtils = mockStatic(PipelineJobIdUtils.class)) {
            apiFactory.when(() -> PipelineAPIFactory.getJobStatisticsAPI(contextKey)).thenReturn(jobStatisticsAPI);
            jobIdUtils.when(() -> PipelineJobIdUtils.parseJobType("j0101p0000_active")).thenReturn(parsedJobType);
            jobIdUtils.when(() -> PipelineJobIdUtils.parseJobType("j0101p0000_missing")).thenReturn(parsedJobType);
            jobIdUtils.when(() -> PipelineJobIdUtils.getElasticJobConfigurationPOJO("j0101p0000_active")).thenReturn(activeJobConfig);
            jobIdUtils.when(() -> PipelineJobIdUtils.getElasticJobConfigurationPOJO("j0101p0000_missing")).thenThrow(new PipelineJobNotFoundException("j0101p0000_missing"));
            List<PipelineJobInfo> actual = new PipelineJobManager(jobType).getJobInfos(contextKey);
            assertThat(actual.size(), is(1));
            assertThat(actual.get(0).getMetaData().getJobId(), is("j0101p0000_active"));
            assertThat(actual.get(0).getTarget().getTableName(), is("foo_tbl"));
        }
    }
    
    private JobBriefInfo createJobBriefInfo(final String jobName) {
        JobBriefInfo result = new JobBriefInfo();
        result.setJobName(jobName);
        return result;
    }
    
    private JobConfigurationPOJO createJobConfiguration() {
        JobConfigurationPOJO result = new JobConfigurationPOJO();
        result.setJobName("j0101p0000_active");
        result.setJobParameter("active_param");
        result.setShardingTotalCount(1);
        return result;
    }
}
