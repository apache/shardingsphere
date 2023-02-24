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

package org.apache.shardingsphere.data.pipeline.scenario.consistencycheck.metadata.processor;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.core.api.PipelineAPIFactory;
import org.apache.shardingsphere.data.pipeline.core.job.PipelineJobCenter;
import org.apache.shardingsphere.data.pipeline.core.job.type.ConsistencyCheckJobType;
import org.apache.shardingsphere.data.pipeline.core.metadata.node.PipelineMetaDataNode;
import org.apache.shardingsphere.data.pipeline.core.metadata.node.event.handler.PipelineChangedJobConfigurationProcessor;
import org.apache.shardingsphere.data.pipeline.core.util.PipelineDistributedBarrier;
import org.apache.shardingsphere.data.pipeline.scenario.consistencycheck.ConsistencyCheckJob;
import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.lite.api.bootstrap.impl.OneOffJobBootstrap;
import org.apache.shardingsphere.mode.repository.cluster.listener.DataChangedEvent.Type;

/**
 * Consistency check changed job configuration processor.
 */
@Slf4j
public final class ConsistencyCheckChangedJobConfigurationProcessor implements PipelineChangedJobConfigurationProcessor {
    
    @Override
    public void process(final Type eventType, final JobConfiguration jobConfig) {
        String jobId = jobConfig.getJobName();
        boolean disabled = jobConfig.isDisabled();
        if (disabled) {
            for (Integer each : PipelineJobCenter.getShardingItems(jobId)) {
                PipelineDistributedBarrier.getInstance().persistEphemeralChildrenNode(PipelineMetaDataNode.getJobBarrierDisablePath(jobId), each);
            }
        }
        boolean deleted = Type.DELETED == eventType;
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
                    execute(jobConfig);
                }
                break;
            default:
                break;
        }
    }
    
    private void execute(final JobConfiguration jobConfig) {
        ConsistencyCheckJob job = new ConsistencyCheckJob();
        PipelineJobCenter.addJob(jobConfig.getJobName(), job);
        OneOffJobBootstrap oneOffJobBootstrap = new OneOffJobBootstrap(PipelineAPIFactory.getRegistryCenter(), job, jobConfig);
        job.setJobBootstrap(oneOffJobBootstrap);
        oneOffJobBootstrap.execute();
    }
    
    @Override
    public String getType() {
        return new ConsistencyCheckJobType().getTypeName();
    }
}
