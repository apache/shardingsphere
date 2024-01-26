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

import org.apache.shardingsphere.data.pipeline.core.context.TransmissionProcessContext;
import org.apache.shardingsphere.data.pipeline.core.job.AbstractSeparablePipelineJob;
import org.apache.shardingsphere.data.pipeline.core.job.JobStatus;
import org.apache.shardingsphere.data.pipeline.core.job.progress.ConsistencyCheckJobItemProgress;
import org.apache.shardingsphere.data.pipeline.core.task.runner.PipelineTasksRunner;
import org.apache.shardingsphere.data.pipeline.scenario.consistencycheck.config.ConsistencyCheckJobConfiguration;
import org.apache.shardingsphere.data.pipeline.scenario.consistencycheck.context.ConsistencyCheckJobItemContext;
import org.apache.shardingsphere.data.pipeline.scenario.consistencycheck.task.ConsistencyCheckTasksRunner;

/**
 * Consistency check job.
 */
public final class ConsistencyCheckJob extends AbstractSeparablePipelineJob<ConsistencyCheckJobConfiguration, ConsistencyCheckJobItemContext, ConsistencyCheckJobItemProgress> {
    
    public ConsistencyCheckJob(final String jobId) {
        super(jobId, false);
    }
    
    @Override
    public ConsistencyCheckJobItemContext buildJobItemContext(final ConsistencyCheckJobConfiguration jobConfig,
                                                              final int shardingItem, final ConsistencyCheckJobItemProgress jobItemProgress, final TransmissionProcessContext jobProcessContext) {
        return new ConsistencyCheckJobItemContext(jobConfig, shardingItem, JobStatus.RUNNING, jobItemProgress);
    }
    
    @Override
    protected PipelineTasksRunner buildTasksRunner(final ConsistencyCheckJobItemContext jobItemContext) {
        return new ConsistencyCheckTasksRunner(jobItemContext);
    }
    
    @Override
    protected void doPrepare(final ConsistencyCheckJobItemContext jobItemContext) {
    }
}
