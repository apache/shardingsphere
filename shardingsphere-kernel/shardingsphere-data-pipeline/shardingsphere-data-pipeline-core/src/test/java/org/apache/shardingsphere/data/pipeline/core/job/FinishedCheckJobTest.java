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
import org.apache.shardingsphere.data.pipeline.api.config.server.ServerConfiguration;
import org.apache.shardingsphere.data.pipeline.api.pojo.JobInfo;
import org.apache.shardingsphere.data.pipeline.core.fixture.EmbedTestingServer;
import org.apache.shardingsphere.data.pipeline.core.util.ReflectionUtil;
import org.apache.shardingsphere.data.pipeline.scenario.rulealtered.RuleAlteredContext;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepositoryConfiguration;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;
import java.util.Properties;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class FinishedCheckJobTest {
    
    private static FinishedCheckJob finishedCheckJob;
    
    @Mock
    private PipelineJobAPI pipelineJobAPI;
    
    @BeforeClass
    public static void beforeClass() throws Exception {
        EmbedTestingServer.start();
        ReflectionUtil.setFieldValue(RuleAlteredContext.getInstance(), "serverConfig", null);
        RuleAlteredContext.getInstance().init(mockServerConfig());
        finishedCheckJob = new FinishedCheckJob();
    }
    
    @Before
    @SneakyThrows(ReflectiveOperationException.class)
    public void setUp() {
        ReflectionUtil.setFieldValue(finishedCheckJob, "pipelineJobAPI", pipelineJobAPI);
    }
    
    @Test
    public void assertExecuteAllDisabledJob() {
        JobInfo jobInfo = new JobInfo(1L);
        jobInfo.setActive(false);
        List<JobInfo> jobInfos = Collections.singletonList(jobInfo);
        when(pipelineJobAPI.list()).thenReturn(jobInfos);
        finishedCheckJob.execute(null);
    }
    
    @Test
    public void assertExecuteActiveJob() {
        JobInfo jobInfo = new JobInfo(1L);
        jobInfo.setActive(true);
        jobInfo.setJobParameter("handleConfig:\n"
                + "  concurrency: 2\n"
                + "  shardingTables:\n"
                + "  - ds_0.t_order_$->{0..1}\n"
                + "ruleConfig:\n");
        List<JobInfo> jobInfos = Collections.singletonList(jobInfo);
        when(pipelineJobAPI.list()).thenReturn(jobInfos);
        when(pipelineJobAPI.getProgress(1L)).thenReturn(Collections.emptyMap());
        finishedCheckJob.execute(null);
    }
    
    @AfterClass
    public static void afterClass() throws Exception {
        ReflectionUtil.setFieldValue(RuleAlteredContext.getInstance(), "serverConfig", null);
    }
    
    private static ServerConfiguration mockServerConfig() {
        ServerConfiguration result = new ServerConfiguration();
        result.setClusterAutoSwitchAlgorithm(new ShardingSphereAlgorithmConfiguration("Fixture", new Properties()));
        result.setModeConfiguration(new ModeConfiguration("Cluster", new ClusterPersistRepositoryConfiguration("Zookeeper", "test", EmbedTestingServer.getConnectionString(), null), true));
        return result;
    }
}
