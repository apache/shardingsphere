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

package org.apache.shardingsphere.scaling.core.job;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.elasticjob.api.ShardingContext;
import org.apache.shardingsphere.elasticjob.simple.job.SimpleJob;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.apache.shardingsphere.scaling.core.api.GovernanceRepositoryAPI;
import org.apache.shardingsphere.scaling.core.api.ScalingAPIFactory;
import org.apache.shardingsphere.scaling.core.config.JobConfiguration;
import org.apache.shardingsphere.scaling.core.job.preparer.ScalingJobPreparer;
import org.apache.shardingsphere.scaling.core.job.schedule.JobSchedulerCenter;

/**
 * Scaling job.
 */
@Slf4j
public final class ScalingJob implements SimpleJob {
    
    private final GovernanceRepositoryAPI governanceRepositoryAPI = ScalingAPIFactory.getGovernanceRepositoryAPI();
    
    private final ScalingJobPreparer jobPreparer = new ScalingJobPreparer();
    
    @Override
    public void execute(final ShardingContext shardingContext) {
        log.info("Execute scaling job {}-{}", shardingContext.getJobName(), shardingContext.getShardingItem());
        JobConfiguration jobConfig = YamlEngine.unmarshal(shardingContext.getJobParameter(), JobConfiguration.class);
        jobConfig.getHandleConfig().setShardingItem(shardingContext.getShardingItem());
        JobContext jobContext = new JobContext(jobConfig);
        jobContext.setInitProgress(governanceRepositoryAPI.getJobProgress(jobContext.getJobId(), jobContext.getShardingItem()));
        jobPreparer.prepare(jobContext);
        governanceRepositoryAPI.persistJobProgress(jobContext);
        JobSchedulerCenter.start(jobContext);
    }
}
