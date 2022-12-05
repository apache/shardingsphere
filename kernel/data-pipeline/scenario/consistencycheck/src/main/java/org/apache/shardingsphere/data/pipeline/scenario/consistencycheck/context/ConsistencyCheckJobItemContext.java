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

package org.apache.shardingsphere.data.pipeline.scenario.consistencycheck.context;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.data.pipeline.api.config.job.ConsistencyCheckJobConfiguration;
import org.apache.shardingsphere.data.pipeline.api.context.PipelineJobItemContext;
import org.apache.shardingsphere.data.pipeline.api.context.PipelineProcessContext;
import org.apache.shardingsphere.data.pipeline.api.job.JobStatus;
import org.apache.shardingsphere.data.pipeline.api.job.progress.ConsistencyCheckJobItemProgress;
import org.apache.shardingsphere.data.pipeline.core.check.consistency.ConsistencyCheckJobItemProgressContext;

import java.util.Optional;

/**
 * Consistency check job item context.
 */
@Getter
@Setter
public final class ConsistencyCheckJobItemContext implements PipelineJobItemContext {
    
    private final String jobId;
    
    private final int shardingItem;
    
    private String dataSourceName;
    
    private volatile boolean stopping;
    
    private volatile JobStatus status;
    
    private final ConsistencyCheckJobConfiguration jobConfig;
    
    private final ConsistencyCheckJobItemProgressContext progressContext;
    
    public ConsistencyCheckJobItemContext(final ConsistencyCheckJobConfiguration jobConfig, final int shardingItem, final JobStatus status, final ConsistencyCheckJobItemProgress jobItemProgress) {
        this.jobConfig = jobConfig;
        jobId = jobConfig.getJobId();
        this.shardingItem = shardingItem;
        this.status = status;
        progressContext = new ConsistencyCheckJobItemProgressContext(jobId, shardingItem);
        if (null != jobItemProgress) {
            progressContext.getCheckedRecordsCount().set(Optional.ofNullable(jobItemProgress.getCheckedRecordsCount()).orElse(0L));
            Optional.ofNullable(jobItemProgress.getTableCheckPositions()).ifPresent(progressContext.getTableCheckPositions()::putAll);
        }
    }
    
    @Override
    public PipelineProcessContext getJobProcessContext() {
        throw new UnsupportedOperationException();
    }
}
