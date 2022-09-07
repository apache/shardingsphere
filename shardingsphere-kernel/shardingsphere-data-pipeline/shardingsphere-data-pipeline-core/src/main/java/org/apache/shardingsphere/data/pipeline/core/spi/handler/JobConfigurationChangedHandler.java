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

package org.apache.shardingsphere.data.pipeline.core.spi.handler;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.api.config.job.MigrationJobConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.job.yaml.YamlMigrationJobConfigurationSwapper;
import org.apache.shardingsphere.data.pipeline.core.api.PipelineAPIFactory;
import org.apache.shardingsphere.data.pipeline.core.context.PipelineContext;
import org.apache.shardingsphere.data.pipeline.core.job.PipelineJobCenter;
import org.apache.shardingsphere.data.pipeline.core.metadata.node.PipelineMetaDataNode;
import org.apache.shardingsphere.data.pipeline.scenario.migration.MigrationJob;
import org.apache.shardingsphere.data.pipeline.scenario.migration.MigrationJobPreparer;
import org.apache.shardingsphere.elasticjob.infra.pojo.JobConfigurationPOJO;
import org.apache.shardingsphere.elasticjob.lite.api.bootstrap.impl.OneOffJobBootstrap;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.mode.repository.cluster.listener.DataChangedEvent;

import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

/**
 * Migration pipeline meta data handler.
 */
@Slf4j
public final class JobConfigurationChangedHandler implements PipelineMetaDataChangedHandler {
    
    @Override
    public Pattern getKeyPattern() {
        return PipelineMetaDataNode.CONFIG_PATTERN;
    }
    
    @Override
    public void handle(final DataChangedEvent event) {
        log.info("{} job config: {}", event.getType(), event.getKey());
        JobConfigurationPOJO jobConfigPOJO;
        try {
            jobConfigPOJO = YamlEngine.unmarshal(event.getValue(), JobConfigurationPOJO.class, true);
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            log.error("analyze job config pojo failed.", ex);
            return;
        }
        String jobId = jobConfigPOJO.getJobName();
        if (jobConfigPOJO.isDisabled()) {
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
                    CompletableFuture.runAsync(() -> execute(jobConfigPOJO), PipelineContext.getEventListenerExecutor());
                }
                break;
            case DELETED:
                log.info("deleted jobId={}", jobId);
                MigrationJobConfiguration jobConfig = YamlMigrationJobConfigurationSwapper.swapToObject(jobConfigPOJO.getJobParameter());
                new MigrationJobPreparer().cleanup(jobConfig);
                PipelineJobCenter.stop(jobConfigPOJO.getJobName());
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
}
