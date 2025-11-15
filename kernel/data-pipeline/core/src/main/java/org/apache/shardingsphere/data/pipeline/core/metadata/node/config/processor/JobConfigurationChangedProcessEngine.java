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

package org.apache.shardingsphere.data.pipeline.core.metadata.node.config.processor;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.core.job.PipelineJob;
import org.apache.shardingsphere.data.pipeline.core.job.PipelineJobRegistry;
import org.apache.shardingsphere.data.pipeline.core.job.api.PipelineAPIFactory;
import org.apache.shardingsphere.data.pipeline.core.job.config.PipelineJobConfiguration;
import org.apache.shardingsphere.data.pipeline.core.job.id.PipelineJobIdUtils;
import org.apache.shardingsphere.data.pipeline.core.metadata.node.PipelineMetaDataNode;
import org.apache.shardingsphere.data.pipeline.core.util.PipelineDistributedBarrier;
import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.lite.api.bootstrap.impl.OneOffJobBootstrap;
import org.apache.shardingsphere.mode.event.DataChangedEvent.Type;

import java.util.Collection;

/**
 * Job configuration changed process engine.
 */
@Slf4j
public final class JobConfigurationChangedProcessEngine {
    
    /**
     * Process changed job configuration.
     *
     * @param eventType event type
     * @param jobConfig pipeline job configuration
     * @param processor pipeline job configuration changed processor
     * @param <T> type of pipeline job configuration
     */
    @SuppressWarnings("unchecked")
    public <T extends PipelineJobConfiguration> void process(final Type eventType, final JobConfiguration jobConfig, final JobConfigurationChangedProcessor<T> processor) {
        String jobId = jobConfig.getJobName();
        if (jobConfig.isDisabled()) {
            // Get sharding items before stop job, because sharding items will be cleared after stop job.
            Collection<Integer> shardingItems = PipelineJobRegistry.getShardingItems(jobId);
            PipelineJobRegistry.stop(jobId);
            disableJob(jobId, shardingItems);
        }
        switch (eventType) {
            case ADDED:
            case UPDATED:
                if (jobConfig.isDisabled()) {
                    break;
                }
                if (PipelineJobRegistry.isExisting(jobId)) {
                    log.info("{} added to executing jobs failed since it already exists", jobId);
                } else {
                    T pipelineJobConfig = (T) PipelineJobIdUtils.parseJobType(jobConfig.getJobName()).getOption().getYamlJobConfigurationSwapper().swapToObject(jobConfig.getJobParameter());
                    executeJob(jobConfig, pipelineJobConfig, processor);
                }
                break;
            case DELETED:
                PipelineJobRegistry.stop(jobId);
                processor.clean(jobConfig);
                break;
            default:
                break;
        }
    }
    
    private void disableJob(final String jobId, final Collection<Integer> shardingItems) {
        PipelineDistributedBarrier distributedBarrier = PipelineDistributedBarrier.getInstance(PipelineJobIdUtils.parseContextKey(jobId));
        for (Integer each : shardingItems) {
            distributedBarrier.persistEphemeralChildrenNode(PipelineMetaDataNode.getJobBarrierDisablePath(jobId), each);
        }
    }
    
    private <T extends PipelineJobConfiguration> void executeJob(final JobConfiguration jobConfig, final T pipelineJobConfig, final JobConfigurationChangedProcessor<T> processor) {
        PipelineJob job = processor.createJob(pipelineJobConfig);
        String jobId = jobConfig.getJobName();
        PipelineJobRegistry.add(jobId, job);
        OneOffJobBootstrap oneOffJobBootstrap = new OneOffJobBootstrap(PipelineAPIFactory.getRegistryCenter(PipelineJobIdUtils.parseContextKey(jobId)), job, jobConfig);
        job.getJobRunnerManager().setJobBootstrap(oneOffJobBootstrap);
        oneOffJobBootstrap.execute();
    }
}
