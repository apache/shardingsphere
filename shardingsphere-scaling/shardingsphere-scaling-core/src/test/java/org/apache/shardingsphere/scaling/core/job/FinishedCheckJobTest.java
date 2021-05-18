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

package org.apache.shardingsphere.scaling.core.job;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.SneakyThrows;
import org.apache.shardingsphere.governance.repository.api.config.RegistryCenterConfiguration;
import org.apache.shardingsphere.governance.repository.api.config.GovernanceConfiguration;
import org.apache.shardingsphere.scaling.core.api.GovernanceRepositoryAPI;
import org.apache.shardingsphere.scaling.core.api.ScalingAPI;
import org.apache.shardingsphere.scaling.core.common.constant.ScalingConstant;
import org.apache.shardingsphere.scaling.core.config.JobConfiguration;
import org.apache.shardingsphere.scaling.core.config.ScalingContext;
import org.apache.shardingsphere.scaling.core.config.ServerConfiguration;
import org.apache.shardingsphere.scaling.core.config.WorkflowConfiguration;
import org.apache.shardingsphere.scaling.core.fixture.EmbedTestingServer;
import org.apache.shardingsphere.scaling.core.job.check.consistency.DataConsistencyCheckResult;
import org.apache.shardingsphere.scaling.core.util.ReflectionUtil;
import org.apache.shardingsphere.scaling.core.util.ResourceUtil;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Map;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class FinishedCheckJobTest {
    
    private static FinishedCheckJob finishedCheckJob;
    
    @Mock
    private ScalingAPI scalingAPI;
    
    @Mock
    private GovernanceRepositoryAPI governanceRepositoryAPI;
    
    @BeforeClass
    public static void beforeClass() throws Exception {
        EmbedTestingServer.start();
        ReflectionUtil.setFieldValue(ScalingContext.getInstance(), "serverConfig", mockServerConfig());
        finishedCheckJob = new FinishedCheckJob();
    }
    
    @Before
    @SneakyThrows(ReflectiveOperationException.class)
    public void setUp() {
        ReflectionUtil.setFieldValue(finishedCheckJob, "scalingAPI", scalingAPI);
        ReflectionUtil.setFieldValue(finishedCheckJob, "governanceRepositoryAPI", governanceRepositoryAPI);
    }
    
    @Test
    public void assertExecuteWithoutWorkflow() {
        when(governanceRepositoryAPI.getChildrenKeys(ScalingConstant.SCALING_ROOT)).thenReturn(Lists.newArrayList("1"));
        when(scalingAPI.getJobConfig(1L)).thenReturn(new JobConfiguration());
        finishedCheckJob.execute(null);
        verify(scalingAPI, never()).getProgress(1L);
    }
    
    @Test
    public void assertExecuteWithWorkflow() {
        when(governanceRepositoryAPI.getChildrenKeys(ScalingConstant.SCALING_ROOT)).thenReturn(Lists.newArrayList("1"));
        when(scalingAPI.getJobConfig(1L)).thenReturn(mockJobConfigWithWorkflow());
        when(scalingAPI.getProgress(1L)).thenReturn(Maps.newHashMap());
        when(scalingAPI.dataConsistencyCheck(1L)).thenReturn(mockDataConsistencyCheck());
        finishedCheckJob.execute(null);
    }
    
    @AfterClass
    public static void afterClass() throws Exception {
        ReflectionUtil.setFieldValue(ScalingContext.getInstance(), "serverConfig", null);
    }
    
    private Map<String, DataConsistencyCheckResult> mockDataConsistencyCheck() {
        Map<String, DataConsistencyCheckResult> result = Maps.newHashMap();
        DataConsistencyCheckResult checkResult = new DataConsistencyCheckResult(1, 1);
        checkResult.setDataValid(true);
        result.put("t_order", checkResult);
        return result;
    }
    
    private static ServerConfiguration mockServerConfig() {
        ServerConfiguration result = new ServerConfiguration();
        result.setGovernanceConfig(new GovernanceConfiguration("test", new RegistryCenterConfiguration("Zookeeper", EmbedTestingServer.getConnectionString(), null), true));
        return result;
    }
    
    private JobConfiguration mockJobConfigWithWorkflow() {
        JobConfiguration result = ResourceUtil.mockJobConfig();
        result.getHandleConfig().setWorkflowConfig(new WorkflowConfiguration("ds_0", "1"));
        return result;
    }
}
