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
import org.apache.shardingsphere.data.pipeline.core.api.PipelineAPIFactory;
import org.apache.shardingsphere.data.pipeline.core.job.AbstractPipelineJob;
import org.apache.shardingsphere.data.pipeline.core.job.PipelineJobCenter;
import org.apache.shardingsphere.data.pipeline.core.metadata.node.PipelineMetaDataNode;
import org.apache.shardingsphere.data.pipeline.core.metadata.node.config.processor.ChangedJobConfigurationProcessor;
import org.apache.shardingsphere.data.pipeline.core.util.PipelineDistributedBarrier;
import org.apache.shardingsphere.data.pipeline.spi.job.JobType;
import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.lite.api.bootstrap.impl.OneOffJobBootstrap;
import org.apache.shardingsphere.mode.repository.cluster.listener.DataChangedEvent.Type;

/**
 * Abstract changed job configuration processor.
 */
@Slf4j
public abstract class AbstractChangedJobConfigurationProcessor implements ChangedJobConfigurationProcessor {
    
    @Override
    public void process(final Type eventType, final JobConfiguration jobConfig) {
        boolean disabled = jobConfig.isDisabled();
        if (disabled) {
            onDisabled(jobConfig);
        }
        boolean deleted = Type.DELETED == eventType;
        if (deleted) {
            onDeleted(jobConfig);
        }
        String jobId = jobConfig.getJobName();
        if (disabled || deleted) {
            PipelineJobCenter.stop(jobId);
            return;
        }
        switch (eventType) {
            case ADDED:
            case UPDATED:
                if (PipelineJobCenter.isJobExisting(jobId)) {
                    log.info("{} added to executing jobs failed since it already exists", jobId);
                } else {
                    executeJob(jobConfig);
                }
                break;
            default:
                break;
        }
    }
    
    protected void onDisabled(final JobConfiguration jobConfig) {
        String jobId = jobConfig.getJobName();
        for (Integer each : PipelineJobCenter.getShardingItems(jobId)) {
            PipelineDistributedBarrier.getInstance().persistEphemeralChildrenNode(PipelineMetaDataNode.getJobBarrierDisablePath(jobId), each);
        }
    }
    
    protected abstract void onDeleted(JobConfiguration jobConfig);
    
    protected void executeJob(final JobConfiguration jobConfig) {
        AbstractPipelineJob job = buildPipelineJob();
        PipelineJobCenter.addJob(jobConfig.getJobName(), job);
        OneOffJobBootstrap oneOffJobBootstrap = new OneOffJobBootstrap(PipelineAPIFactory.getRegistryCenter(), job, jobConfig);
        job.setJobBootstrap(oneOffJobBootstrap);
        oneOffJobBootstrap.execute();
    }
    
    protected abstract AbstractPipelineJob buildPipelineJob();
    
    protected abstract JobType getJobType();
    
    @Override
    public String getType() {
        return getJobType().getTypeName();
    }
}
