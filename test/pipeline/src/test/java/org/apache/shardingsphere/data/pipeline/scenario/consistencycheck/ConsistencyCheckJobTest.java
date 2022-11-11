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

import org.apache.shardingsphere.data.pipeline.api.config.job.ConsistencyCheckJobConfiguration;
import org.apache.shardingsphere.data.pipeline.api.job.JobStatus;
import org.apache.shardingsphere.data.pipeline.core.api.PipelineAPIFactory;
import org.apache.shardingsphere.data.pipeline.core.job.progress.yaml.YamlConsistencyCheckJobItemProgress;
import org.apache.shardingsphere.data.pipeline.core.util.PipelineContextUtil;
import org.apache.shardingsphere.data.pipeline.core.util.ReflectionUtil;
import org.apache.shardingsphere.data.pipeline.yaml.job.YamlConsistencyCheckJobConfiguration;
import org.apache.shardingsphere.data.pipeline.yaml.job.YamlConsistencyCheckJobConfigurationSwapper;
import org.apache.shardingsphere.elasticjob.api.ShardingContext;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.junit.BeforeClass;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public final class ConsistencyCheckJobTest {
    
    @BeforeClass
    public static void beforeClass() {
        PipelineContextUtil.mockModeConfigAndContextManager();
    }
    
    @Test
    public void assertBuildPipelineJobItemContext() throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        YamlConsistencyCheckJobItemProgress jobItemProgress = new YamlConsistencyCheckJobItemProgress();
        jobItemProgress.setStatus(JobStatus.RUNNING.name());
        Map<String, Object> expectTableCheckPosition = new HashMap<>();
        expectTableCheckPosition.put("t_order", 100);
        jobItemProgress.setTableCheckPositions(expectTableCheckPosition);
        String checkJobId = "j0201001";
        PipelineAPIFactory.getGovernanceRepositoryAPI().persistJobItemProgress(checkJobId, 0, YamlEngine.marshal(jobItemProgress));
        ConsistencyCheckJobConfiguration jobConfig = new ConsistencyCheckJobConfiguration(checkJobId, "", null, null);
        YamlConsistencyCheckJobConfiguration yamlJobConfig = new YamlConsistencyCheckJobConfigurationSwapper().swapToYamlConfiguration(jobConfig);
        ShardingContext shardingContext = new ShardingContext(checkJobId, "", 1, YamlEngine.marshal(yamlJobConfig), 0, "");
        ConsistencyCheckJob consistencyCheckJob = new ConsistencyCheckJob();
        ReflectionUtil.invokeMethodInParentClass(consistencyCheckJob, "setJobId", new Class[]{String.class}, new Object[]{checkJobId});
        ConsistencyCheckJobItemContext actualItemContext = consistencyCheckJob.buildPipelineJobItemContext(shardingContext);
        assertThat(actualItemContext.getTableCheckPositions(), is(expectTableCheckPosition));
    }
}
