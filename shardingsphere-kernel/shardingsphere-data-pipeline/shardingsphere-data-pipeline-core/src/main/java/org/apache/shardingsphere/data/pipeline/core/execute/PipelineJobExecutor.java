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
import org.apache.shardingsphere.data.pipeline.api.config.rulealtered.JobConfiguration;
import org.apache.shardingsphere.data.pipeline.api.executor.AbstractLifecycleExecutor;
import org.apache.shardingsphere.data.pipeline.core.api.PipelineAPIFactory;
import org.apache.shardingsphere.data.pipeline.core.constant.DataPipelineConstants;
import org.apache.shardingsphere.data.pipeline.core.lock.PipelineSimpleLock;
import org.apache.shardingsphere.data.pipeline.scenario.rulealtered.RuleAlteredJob;
import org.apache.shardingsphere.data.pipeline.scenario.rulealtered.RuleAlteredJobSchedulerCenter;
import org.apache.shardingsphere.elasticjob.infra.pojo.JobConfigurationPOJO;
import org.apache.shardingsphere.elasticjob.lite.api.bootstrap.impl.OneOffJobBootstrap;
import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.config.event.rule.ScalingReleaseSchemaNameLockEvent;
import org.apache.shardingsphere.mode.repository.cluster.listener.DataChangedEvent;

import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Pipeline job executor.
 */
@Slf4j
public final class PipelineJobExecutor extends AbstractLifecycleExecutor {
    
    private static final Pattern CONFIG_PATTERN = Pattern.compile(DataPipelineConstants.DATA_PIPELINE_ROOT + "/(\\d{2}[0-9a-f]+)/config");
    
    @Override
    protected void doStart() {
        watchGovernanceRepositoryConfiguration();
    }
    
    private void watchGovernanceRepositoryConfiguration() {
        PipelineAPIFactory.getGovernanceRepositoryAPI().watch(DataPipelineConstants.DATA_PIPELINE_ROOT, event -> {
            Optional<JobConfigurationPOJO> jobConfigPOJOOptional = getJobConfigPOJO(event);
            if (!jobConfigPOJOOptional.isPresent()) {
                return;
            }
            JobConfigurationPOJO jobConfigPOJO = jobConfigPOJOOptional.get();
            if (DataChangedEvent.Type.DELETED == event.getType() || jobConfigPOJO.isDisabled()) {
                RuleAlteredJobSchedulerCenter.stop(jobConfigPOJO.getJobName());
                JobConfiguration jobConfig = YamlEngine.unmarshal(jobConfigPOJO.getJobParameter(), JobConfiguration.class, true);
                ScalingReleaseSchemaNameLockEvent releaseLockEvent = new ScalingReleaseSchemaNameLockEvent(jobConfig.getWorkflowConfig().getSchemaName());
                ShardingSphereEventBus.getInstance().post(releaseLockEvent);
                return;
            }
            switch (event.getType()) {
                case ADDED:
                case UPDATED:
                    JobConfiguration jobConfig = YamlEngine.unmarshal(jobConfigPOJO.getJobParameter(), JobConfiguration.class, true);
                    String schemaName = jobConfig.getWorkflowConfig().getSchemaName();
                    if (PipelineSimpleLock.getInstance().tryLock(schemaName, 1000)) {
                        execute(jobConfigPOJO);
                    } else {
                        log.info("tryLock failed, schemaName={}", schemaName);
                    }
                    break;
                default:
                    break;
            }
        });
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
    
    private void execute(final JobConfigurationPOJO jobConfigPOJO) {
        if (!RuleAlteredJobSchedulerCenter.existJob(jobConfigPOJO.getJobName())) {
            log.info("{} added to executing jobs success", jobConfigPOJO.getJobName());
            new OneOffJobBootstrap(PipelineAPIFactory.getRegistryCenter(), new RuleAlteredJob(), jobConfigPOJO.toJobConfiguration()).execute();
        } else {
            log.info("{} added to executing jobs failed since it already exists", jobConfigPOJO.getJobName());
        }
    }
    
    @Override
    protected void doStop() {
    }
}
