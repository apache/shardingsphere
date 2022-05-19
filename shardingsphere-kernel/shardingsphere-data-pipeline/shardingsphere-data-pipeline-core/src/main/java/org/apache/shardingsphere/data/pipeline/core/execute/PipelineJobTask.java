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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.api.config.rulealtered.RuleAlteredJobConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.rulealtered.yaml.RuleAlteredJobConfigurationSwapper;
import org.apache.shardingsphere.data.pipeline.api.executor.AbstractLifecycleExecutor;
import org.apache.shardingsphere.data.pipeline.core.api.PipelineAPIFactory;
import org.apache.shardingsphere.data.pipeline.core.lock.PipelineSimpleLock;
import org.apache.shardingsphere.data.pipeline.scenario.rulealtered.RuleAlteredJob;
import org.apache.shardingsphere.data.pipeline.scenario.rulealtered.RuleAlteredJobSchedulerCenter;
import org.apache.shardingsphere.elasticjob.infra.pojo.JobConfigurationPOJO;
import org.apache.shardingsphere.elasticjob.lite.api.bootstrap.impl.OneOffJobBootstrap;

/**
 * Pipeline job task. execute pipeline job.
 */
@RequiredArgsConstructor
@Slf4j
public final class PipelineJobTask extends AbstractLifecycleExecutor {
    
    private final JobConfigurationPOJO jobConfigPOJO;
    
    @Override
    protected void doStart() {
        log.info("pipeline job task start jobId:{}", jobConfigPOJO.getJobName());
        RuleAlteredJobConfiguration jobConfig = RuleAlteredJobConfigurationSwapper.swapToObject(jobConfigPOJO.getJobParameter());
        String databaseName = jobConfig.getDatabaseName();
        if (PipelineSimpleLock.getInstance().tryLock(databaseName, 1000)) {
            execute(jobConfigPOJO);
        } else {
            log.info("tryLock failed, databaseName={}", databaseName);
        }
    }
    
    @Override
    protected void doStop() {
    }
    
    private void execute(final JobConfigurationPOJO jobConfigPOJO) {
        if (!RuleAlteredJobSchedulerCenter.existJob(jobConfigPOJO.getJobName())) {
            log.info("{} added to executing jobs success", jobConfigPOJO.getJobName());
            new OneOffJobBootstrap(PipelineAPIFactory.getRegistryCenter(), new RuleAlteredJob(), jobConfigPOJO.toJobConfiguration()).execute();
        } else {
            log.info("{} added to executing jobs failed since it already exists", jobConfigPOJO.getJobName());
        }
    }
}
