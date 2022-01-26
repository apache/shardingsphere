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

package org.apache.shardingsphere.data.pipeline.scenario.rulealtered;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.api.config.rulealtered.JobConfiguration;
import org.apache.shardingsphere.data.pipeline.api.job.JobStatus;
import org.apache.shardingsphere.data.pipeline.core.api.GovernanceRepositoryAPI;
import org.apache.shardingsphere.data.pipeline.core.api.PipelineAPIFactory;
import org.apache.shardingsphere.elasticjob.api.ShardingContext;
import org.apache.shardingsphere.elasticjob.simple.job.SimpleJob;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;

/**
 * Rule altered job.
 */
@Slf4j
public final class RuleAlteredJob implements SimpleJob {
    
    private final GovernanceRepositoryAPI governanceRepositoryAPI = PipelineAPIFactory.getGovernanceRepositoryAPI();
    
    private final RuleAlteredJobPreparer jobPreparer = new RuleAlteredJobPreparer();
    
    @Override
    public void execute(final ShardingContext shardingContext) {
        log.info("Execute job {}-{}", shardingContext.getJobName(), shardingContext.getShardingItem());
        JobConfiguration jobConfig = YamlEngine.unmarshal(shardingContext.getJobParameter(), JobConfiguration.class, true);
        jobConfig.getHandleConfig().setJobShardingItem(shardingContext.getShardingItem());
        RuleAlteredJobContext jobContext = new RuleAlteredJobContext(jobConfig);
        jobContext.setInitProgress(governanceRepositoryAPI.getJobProgress(jobContext.getJobId(), jobContext.getShardingItem()));
        jobContext.setJobPreparer(jobPreparer);
        try {
            jobPreparer.prepare(jobContext);
            // CHECKSTYLE:OFF
        } catch (final RuntimeException ex) {
            // CHECKSTYLE:ON
            log.error("job prepare failed, {}-{}", shardingContext.getJobName(), shardingContext.getShardingItem());
            jobContext.setStatus(JobStatus.PREPARING_FAILURE);
            governanceRepositoryAPI.persistJobProgress(jobContext);
            throw ex;
        }
        governanceRepositoryAPI.persistJobProgress(jobContext);
        RuleAlteredJobSchedulerCenter.start(jobContext);
    }
}
