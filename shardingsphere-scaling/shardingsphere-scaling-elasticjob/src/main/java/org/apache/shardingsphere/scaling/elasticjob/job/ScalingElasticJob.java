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

package org.apache.shardingsphere.scaling.elasticjob.job;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.elasticjob.api.ShardingContext;
import org.apache.shardingsphere.elasticjob.simple.job.SimpleJob;
import org.apache.shardingsphere.scaling.core.api.JobSchedulerCenter;
import org.apache.shardingsphere.scaling.core.config.JobConfiguration;
import org.apache.shardingsphere.scaling.core.job.JobContext;
import org.apache.shardingsphere.scaling.core.service.ScalingJobService;
import org.apache.shardingsphere.scaling.core.service.impl.StandaloneScalingJobService;

/**
 * Scaling elastic job.
 */
@Slf4j
public final class ScalingElasticJob implements SimpleJob {
    
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().serializeNulls().create();
    
    private static final ScalingJobService SCALING_JOB_SERVICE = new StandaloneScalingJobService();
    
    private JobContext jobContext;
    
    @Override
    public void execute(final ShardingContext shardingContext) {
        log.info("execute job: {} - {}/{}", shardingContext.getTaskId(), shardingContext.getShardingItem(), shardingContext.getShardingTotalCount());
        JobConfiguration jobConfig = GSON.fromJson(shardingContext.getJobParameter(), JobConfiguration.class);
        if (jobConfig.getHandleConfig().isRunning()) {
            startJob(jobConfig, shardingContext);
            return;
        }
        stopJob(shardingContext);
    }
    
    private void startJob(final JobConfiguration jobConfig, final ShardingContext shardingContext) {
        log.info("start job: {} - {}", shardingContext.getJobName(), shardingContext.getShardingItem());
        jobConfig.getHandleConfig().setShardingItem(shardingContext.getShardingItem());
        jobConfig.getHandleConfig().setJobId(Long.valueOf(shardingContext.getJobName()));
        jobContext = SCALING_JOB_SERVICE.start(jobConfig).orElse(null);
        JobSchedulerCenter.addJob(jobContext);
    }
    
    private void stopJob(final ShardingContext shardingContext) {
        if (null != jobContext) {
            log.info("stop job: {} - {}", shardingContext.getJobName(), shardingContext.getShardingItem());
            SCALING_JOB_SERVICE.stop(jobContext.getJobId());
            JobSchedulerCenter.removeJob(jobContext);
            jobContext = null;
        }
    }
}
