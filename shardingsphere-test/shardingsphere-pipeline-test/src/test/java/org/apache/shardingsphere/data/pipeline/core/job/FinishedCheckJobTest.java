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
import org.apache.shardingsphere.data.pipeline.api.PipelineJobAPI;
import org.apache.shardingsphere.data.pipeline.api.PipelineJobAPIFactory;
import org.apache.shardingsphere.data.pipeline.api.pojo.JobInfo;
import org.apache.shardingsphere.data.pipeline.core.fixture.EmbedTestingServer;
import org.apache.shardingsphere.data.pipeline.core.util.ReflectionUtil;
import org.apache.shardingsphere.data.pipeline.core.util.ResourceUtil;
import org.apache.shardingsphere.data.pipeline.core.util.RuleAlteredContextUtil;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class FinishedCheckJobTest {
    
    private static FinishedCheckJob finishedCheckJob;
    
    @Mock
    private PipelineJobAPI pipelineJobAPI;
    
    @BeforeClass
    public static void beforeClass() {
        EmbedTestingServer.start();
        RuleAlteredContextUtil.mockModeConfig();
        finishedCheckJob = new FinishedCheckJob();
    }
    
    @Before
    @SneakyThrows(ReflectiveOperationException.class)
    public void setUp() {
        ReflectionUtil.setFieldValue(finishedCheckJob, "pipelineJobAPI", pipelineJobAPI);
    }
    
    @Test
    public void assertExecuteAllDisabledJob() {
        Optional<String> jobId = PipelineJobAPIFactory.getPipelineJobAPI().start(ResourceUtil.mockJobConfig());
        assertTrue(jobId.isPresent());
        List<JobInfo> jobInfos = PipelineJobAPIFactory.getPipelineJobAPI().list();
        jobInfos.forEach(each -> each.setActive(false));
        when(pipelineJobAPI.list()).thenReturn(jobInfos);
        finishedCheckJob.execute(null);
    }
    
    @Test
    public void assertExecuteActiveJob() {
        Optional<String> jobId = PipelineJobAPIFactory.getPipelineJobAPI().start(ResourceUtil.mockJobConfig());
        assertTrue(jobId.isPresent());
        List<JobInfo> jobInfos = PipelineJobAPIFactory.getPipelineJobAPI().list();
        jobInfos.forEach(each -> each.setActive(true));
        when(pipelineJobAPI.list()).thenReturn(jobInfos);
        finishedCheckJob.execute(null);
    }
}
