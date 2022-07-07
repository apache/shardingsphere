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
import org.apache.shardingsphere.data.pipeline.api.RuleAlteredJobAPIFactory;
import org.apache.shardingsphere.data.pipeline.api.config.rulealtered.RuleAlteredJobConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.rulealtered.yaml.RuleAlteredJobConfigurationSwapper;
import org.apache.shardingsphere.data.pipeline.api.executor.AbstractLifecycleExecutor;
import org.apache.shardingsphere.data.pipeline.core.api.PipelineAPIFactory;
import org.apache.shardingsphere.data.pipeline.core.constant.DataPipelineConstants;
import org.apache.shardingsphere.data.pipeline.scenario.rulealtered.RuleAlteredJob;
import org.apache.shardingsphere.data.pipeline.scenario.rulealtered.RuleAlteredJobCenter;
import org.apache.shardingsphere.data.pipeline.scenario.rulealtered.RuleAlteredJobPreparer;
import org.apache.shardingsphere.data.pipeline.scenario.rulealtered.RuleAlteredJobProgressDetector;
import org.apache.shardingsphere.elasticjob.infra.pojo.JobConfigurationPOJO;
import org.apache.shardingsphere.elasticjob.lite.api.bootstrap.impl.OneOffJobBootstrap;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.apache.shardingsphere.mode.repository.cluster.listener.DataChangedEvent;

import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

/**
 * Pipeline job executor.
 */
@Slf4j
public final class PipelineJobExecutor extends AbstractLifecycleExecutor {
    
    private static final Pattern CONFIG_PATTERN = Pattern.compile(DataPipelineConstants.DATA_PIPELINE_ROOT + "/(\\d{2}[0-9a-f]+)/config");
    
    private final ExecutorService executor = Executors.newFixedThreadPool(20);
    
    @Override
    protected void doStart() {
        PipelineAPIFactory.getGovernanceRepositoryAPI().watch(DataPipelineConstants.DATA_PIPELINE_ROOT, event -> getJobConfigPOJO(event).ifPresent(optional -> processEvent(event, optional)));
    }
    
    private Optional<JobConfigurationPOJO> getJobConfigPOJO(final DataChangedEvent event) {
        try {
            if (CONFIG_PATTERN.matcher(event.getKey()).matches()) {
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
        if (isDeleted || isDisabled) {
            String jobId = jobConfigPOJO.getJobName();
            log.info("jobId={}, deleted={}, disabled={}", jobId, isDeleted, isDisabled);
            // TODO refactor: dispatch to different job types
            RuleAlteredJobConfiguration jobConfig = RuleAlteredJobConfigurationSwapper.swapToObject(jobConfigPOJO.getJobParameter());
            if (isDeleted) {
                new RuleAlteredJobPreparer().cleanup(jobConfig);
            } else if (RuleAlteredJobProgressDetector.isJobSuccessful(jobConfig.getJobShardingCount(), RuleAlteredJobAPIFactory.getInstance().getProgress(jobConfig).values())) {
                log.info("isJobSuccessful=true");
                new RuleAlteredJobPreparer().cleanup(jobConfig);
            }
            RuleAlteredJobCenter.stop(jobId);
            return;
        }
        switch (event.getType()) {
            case ADDED:
            case UPDATED:
                if (RuleAlteredJobCenter.isJobExisting(jobConfigPOJO.getJobName())) {
                    log.info("{} added to executing jobs failed since it already exists", jobConfigPOJO.getJobName());
                } else {
                    executor.execute(() -> execute(jobConfigPOJO));
                }
                break;
            default:
                break;
        }
    }
    
    private void execute(final JobConfigurationPOJO jobConfigPOJO) {
        RuleAlteredJob job = new RuleAlteredJob();
        RuleAlteredJobCenter.addJob(jobConfigPOJO.getJobName(), job);
        new OneOffJobBootstrap(PipelineAPIFactory.getRegistryCenter(), job, jobConfigPOJO.toJobConfiguration()).execute();
    }
    
    @Override
    protected void doStop() {
        executor.shutdown();
        executor.shutdownNow();
    }
}
