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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.elasticjob.api.ShardingContext;
import org.apache.shardingsphere.elasticjob.simple.job.SimpleJob;
import org.apache.shardingsphere.scaling.core.api.JobSchedulerCenter;
import org.apache.shardingsphere.scaling.core.api.RegistryRepositoryAPI;
import org.apache.shardingsphere.scaling.core.api.ScalingAPIFactory;
import org.apache.shardingsphere.scaling.core.config.JobConfiguration;
import org.apache.shardingsphere.scaling.core.job.preparer.ScalingJobPreparer;

/**
 * Scaling job.
 */
@Slf4j
public final class ScalingJob implements SimpleJob {
    
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().serializeNulls().create();
    
    private final RegistryRepositoryAPI registryRepositoryAPI = ScalingAPIFactory.getRegistryRepositoryAPI();
    
    private final ScalingJobPreparer jobPreparer = new ScalingJobPreparer();
    
    @Override
    public void execute(final ShardingContext shardingContext) {
        log.info("Execute scaling job {}-{}", shardingContext.getJobName(), shardingContext.getShardingItem());
        JobConfiguration jobConfig = GSON.fromJson(shardingContext.getJobParameter(), JobConfiguration.class);
        jobConfig.getHandleConfig().setShardingItem(shardingContext.getShardingItem());
        JobContext jobContext = new JobContext(jobConfig);
        jobContext.setInitProgress(registryRepositoryAPI.getJobProgress(jobContext.getJobId(), jobContext.getShardingItem()));
        jobPreparer.prepare(jobContext);
        JobSchedulerCenter.start(jobContext);
    }
}
