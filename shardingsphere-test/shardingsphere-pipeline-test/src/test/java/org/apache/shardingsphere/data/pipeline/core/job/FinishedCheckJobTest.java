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

package org.apache.shardingsphere.data.pipeline.core.job;

import lombok.SneakyThrows;
import org.apache.shardingsphere.data.pipeline.api.pojo.JobInfo;
import org.apache.shardingsphere.data.pipeline.core.util.JobConfigurationBuilder;
import org.apache.shardingsphere.data.pipeline.core.util.PipelineContextUtil;
import org.apache.shardingsphere.data.pipeline.core.util.ReflectionUtil;
import org.apache.shardingsphere.data.pipeline.scenario.migration.MigrationJobAPI;
import org.apache.shardingsphere.data.pipeline.scenario.migration.MigrationJobAPIFactory;
import org.apache.shardingsphere.data.pipeline.scenario.migration.MigrationProcessContext;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class FinishedCheckJobTest {
    
    private static FinishedCheckJob finishedCheckJob;
    
    @Mock
    private MigrationJobAPI jobAPI;
    
    @Mock
    private MigrationProcessContext processContext;
    
    @BeforeClass
    public static void beforeClass() {
        PipelineContextUtil.mockModeConfigAndContextManager();
        finishedCheckJob = new FinishedCheckJob();
    }
    
    @Before
    @SneakyThrows(ReflectiveOperationException.class)
    public void setUp() {
        when(jobAPI.buildPipelineProcessContext(any())).thenReturn(processContext);
        ReflectionUtil.setFieldValue(finishedCheckJob, "jobAPI", jobAPI);
    }
    
    @Test
    public void assertExecuteAllDisabledJob() {
        Optional<String> jobId = MigrationJobAPIFactory.getInstance().start(JobConfigurationBuilder.createJobConfiguration());
        assertTrue(jobId.isPresent());
        List<JobInfo> jobInfos = MigrationJobAPIFactory.getInstance().list();
        jobInfos.forEach(each -> each.setActive(false));
        when(jobAPI.list()).thenReturn(jobInfos);
        finishedCheckJob.execute(null);
    }
    
    @Test
    public void assertExecuteActiveJob() {
        Optional<String> jobId = MigrationJobAPIFactory.getInstance().start(JobConfigurationBuilder.createJobConfiguration());
        assertTrue(jobId.isPresent());
        List<JobInfo> jobInfos = MigrationJobAPIFactory.getInstance().list();
        jobInfos.forEach(each -> each.setActive(true));
        when(jobAPI.list()).thenReturn(jobInfos);
        finishedCheckJob.execute(null);
    }
}
