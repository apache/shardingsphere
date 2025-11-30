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

import org.apache.shardingsphere.data.pipeline.core.job.engine.PipelineJobRunnerManager;
import org.apache.shardingsphere.data.pipeline.core.job.executor.DistributedPipelineJobExecutor;
import org.apache.shardingsphere.elasticjob.api.ShardingContext;
import org.junit.jupiter.api.Test;
import org.mockito.internal.configuration.plugins.Plugins;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ConsistencyCheckJobTest {
    
    @Test
    void assertGetJobRunnerManager() throws ReflectiveOperationException {
        ConsistencyCheckJob job = new ConsistencyCheckJob();
        DistributedPipelineJobExecutor executor = mock(DistributedPipelineJobExecutor.class);
        PipelineJobRunnerManager jobRunnerManager = mock(PipelineJobRunnerManager.class);
        when(executor.getJobRunnerManager()).thenReturn(jobRunnerManager);
        setExecutor(executor, job);
        assertThat(jobRunnerManager, is(job.getJobRunnerManager()));
    }
    
    @Test
    void assertExecute() throws ReflectiveOperationException {
        ConsistencyCheckJob job = new ConsistencyCheckJob();
        DistributedPipelineJobExecutor executor = mock(DistributedPipelineJobExecutor.class);
        setExecutor(executor, job);
        ShardingContext shardingContext = mock(ShardingContext.class);
        job.execute(shardingContext);
        verify(executor).execute(shardingContext);
    }
    
    private void setExecutor(final DistributedPipelineJobExecutor executor, final ConsistencyCheckJob job) throws ReflectiveOperationException {
        Plugins.getMemberAccessor().set(ConsistencyCheckJob.class.getDeclaredField("executor"), job, executor);
    }
}
