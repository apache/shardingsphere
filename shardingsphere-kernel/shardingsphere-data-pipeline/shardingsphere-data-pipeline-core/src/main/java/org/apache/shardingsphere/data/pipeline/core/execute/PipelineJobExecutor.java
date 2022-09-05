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

package org.apache.shardingsphere.data.pipeline.core.execute;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.api.executor.AbstractLifecycleExecutor;
import org.apache.shardingsphere.data.pipeline.api.job.JobType;
import org.apache.shardingsphere.data.pipeline.core.api.PipelineAPIFactory;
import org.apache.shardingsphere.data.pipeline.core.constant.DataPipelineConstants;
import org.apache.shardingsphere.data.pipeline.core.job.PipelineJobCenter;
import org.apache.shardingsphere.data.pipeline.core.job.PipelineJobIdUtils;
import org.apache.shardingsphere.data.pipeline.core.metadata.node.PipelineMetaDataNode;
import org.apache.shardingsphere.data.pipeline.core.util.PipelineDistributedBarrier;
import org.apache.shardingsphere.data.pipeline.scenario.migration.MigrationJob;
import org.apache.shardingsphere.data.pipeline.spi.process.JobConfigEventProcess;
import org.apache.shardingsphere.data.pipeline.spi.process.JobConfigEventProcessFactory;
import org.apache.shardingsphere.elasticjob.infra.pojo.JobConfigurationPOJO;
import org.apache.shardingsphere.elasticjob.lite.api.bootstrap.impl.OneOffJobBootstrap;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.mode.repository.cluster.listener.DataChangedEvent;
import org.apache.shardingsphere.mode.repository.cluster.listener.DataChangedEvent.Type;

import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Pipeline job executor.
 */
@Slf4j
public final class PipelineJobExecutor extends AbstractLifecycleExecutor {
    
    private final ExecutorService executor = Executors.newFixedThreadPool(20);
    
    @Override
    protected void doStart() {
        PipelineAPIFactory.getGovernanceRepositoryAPI().watch(DataPipelineConstants.DATA_PIPELINE_ROOT, event -> {
            if (PipelineMetaDataNode.BARRIER_PATTERN.matcher(event.getKey()).matches() && event.getType() == Type.ADDED) {
                PipelineDistributedBarrier.getInstance().checkChildrenNodeCount(event);
            }
            getJobConfigPOJO(event).ifPresent(optional -> processEvent(event, optional));
        });
    }
    
    private Optional<JobConfigurationPOJO> getJobConfigPOJO(final DataChangedEvent event) {
        try {
            if (PipelineMetaDataNode.CONFIG_PATTERN.matcher(event.getKey()).matches()) {
                log.info("{} job config: {}", event.getType(), event.getKey());
                return Optional.of(YamlEngine.unmarshal(event.getValue(), JobConfigurationPOJO.class, true));
            }
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            log.error("analyze job config pojo failed.", ex);
        }
        return Optional.empty();
    }
    
    private void processEvent(final DataChangedEvent event, final JobConfigurationPOJO jobConfigPOJO) {
        boolean isDeleted = DataChangedEvent.Type.DELETED == event.getType();
        boolean isDisabled = jobConfigPOJO.isDisabled();
        String jobId = jobConfigPOJO.getJobName();
        JobType jobType = PipelineJobIdUtils.parseJobType(jobId);
        if (isDeleted || isDisabled) {
            log.info("jobId={}, deleted={}, disabled={}", jobId, isDeleted, isDisabled);
            JobConfigEventProcess process = JobConfigEventProcessFactory.getInstance(jobType);
            if (isDeleted) {
                process.cleanup(jobConfigPOJO.getJobParameter());
            } else if (process.isJobSuccessful(jobConfigPOJO.getJobParameter())) {
                log.info("isJobSuccessful=true");
                process.cleanup(jobConfigPOJO.getJobParameter());
            }
            PipelineJobCenter.stop(jobId);
            return;
        }
        switch (event.getType()) {
            case ADDED:
            case UPDATED:
                if (PipelineJobCenter.isJobExisting(jobConfigPOJO.getJobName())) {
                    log.info("{} added to executing jobs failed since it already exists", jobConfigPOJO.getJobName());
                } else {
                    log.info("{} executing jobs", jobConfigPOJO.getJobName());
                    executor.execute(() -> execute(jobConfigPOJO));
                }
                break;
            default:
                break;
        }
    }
    
    private void execute(final JobConfigurationPOJO jobConfigPOJO) {
        MigrationJob job = new MigrationJob();
        PipelineJobCenter.addJob(jobConfigPOJO.getJobName(), job);
        OneOffJobBootstrap oneOffJobBootstrap = new OneOffJobBootstrap(PipelineAPIFactory.getRegistryCenter(), job, jobConfigPOJO.toJobConfiguration());
        oneOffJobBootstrap.execute();
        job.setOneOffJobBootstrap(oneOffJobBootstrap);
    }
    
    @Override
    protected void doStop() {
        executor.shutdown();
        executor.shutdownNow();
    }
}
