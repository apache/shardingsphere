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

package org.apache.shardingsphere.data.pipeline.core.metadata.node.config.processor.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.core.job.PipelineJob;
import org.apache.shardingsphere.data.pipeline.core.job.PipelineJobRegistry;
import org.apache.shardingsphere.data.pipeline.core.job.api.PipelineAPIFactory;
import org.apache.shardingsphere.data.pipeline.core.job.id.PipelineJobIdUtils;
import org.apache.shardingsphere.data.pipeline.core.job.type.PipelineJobType;
import org.apache.shardingsphere.data.pipeline.core.metadata.node.PipelineMetaDataNode;
import org.apache.shardingsphere.data.pipeline.core.metadata.node.config.processor.JobConfigurationChangedProcessor;
import org.apache.shardingsphere.data.pipeline.core.util.PipelineDistributedBarrier;
import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.lite.api.bootstrap.impl.OneOffJobBootstrap;
import org.apache.shardingsphere.mode.event.DataChangedEvent.Type;

/**
 * Abstract job configuration changed processor.
 */
@Slf4j
public abstract class AbstractJobConfigurationChangedProcessor implements JobConfigurationChangedProcessor {
    
    @Override
    public final void process(final Type eventType, final JobConfiguration jobConfig) {
        String jobId = jobConfig.getJobName();
        if (jobConfig.isDisabled()) {
            PipelineJobRegistry.stop(jobId);
            onDisabled(jobId);
            return;
        }
        switch (eventType) {
            case ADDED:
            case UPDATED:
                if (PipelineJobRegistry.isExisting(jobId)) {
                    log.info("{} added to executing jobs failed since it already exists", jobId);
                } else {
                    executeJob(jobConfig);
                }
                break;
            case DELETED:
                PipelineJobRegistry.stop(jobId);
                onDeleted(jobConfig);
                break;
            default:
                break;
        }
    }
    
    protected abstract void onDeleted(JobConfiguration jobConfig);
    
    private void onDisabled(final String jobId) {
        PipelineDistributedBarrier distributedBarrier = PipelineDistributedBarrier.getInstance(PipelineJobIdUtils.parseContextKey(jobId));
        for (Integer each : PipelineJobRegistry.getShardingItems(jobId)) {
            distributedBarrier.persistEphemeralChildrenNode(PipelineMetaDataNode.getJobBarrierDisablePath(jobId), each);
        }
    }
    
    protected void executeJob(final JobConfiguration jobConfig) {
        PipelineJob job = buildJob();
        String jobId = jobConfig.getJobName();
        PipelineJobRegistry.add(jobId, job);
        OneOffJobBootstrap oneOffJobBootstrap = new OneOffJobBootstrap(PipelineAPIFactory.getRegistryCenter(PipelineJobIdUtils.parseContextKey(jobId)), job, jobConfig);
        job.getJobRunnerManager().setJobBootstrap(oneOffJobBootstrap);
        oneOffJobBootstrap.execute();
    }
    
    protected abstract PipelineJob buildJob();
    
    protected abstract PipelineJobType getJobType();
    
    @Override
    public String getType() {
        return getJobType().getType();
    }
}
