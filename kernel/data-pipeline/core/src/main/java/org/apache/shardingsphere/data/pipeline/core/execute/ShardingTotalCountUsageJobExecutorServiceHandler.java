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

import org.apache.shardingsphere.data.pipeline.core.job.id.PipelineJobIdUtils;
import org.apache.shardingsphere.elasticjob.infra.concurrent.ElasticJobExecutorService;
import org.apache.shardingsphere.elasticjob.infra.handler.threadpool.JobExecutorServiceHandler;
import org.apache.shardingsphere.elasticjob.infra.pojo.JobConfigurationPOJO;

import java.util.concurrent.ExecutorService;

/**
 * Sharding total count usage job executor service handler.
 */
public final class ShardingTotalCountUsageJobExecutorServiceHandler implements JobExecutorServiceHandler {
    
    public static final String TYPE = "SHARDING_TOTAL_COUNT";
    
    @Override
    public ExecutorService createExecutorService(final String jobName) {
        int poolSize;
        try {
            JobConfigurationPOJO jobConfigPOJO = PipelineJobIdUtils.getElasticJobConfigurationPOJO(jobName);
            poolSize = jobConfigPOJO.getShardingTotalCount();
            // CHECKSTYLE:OFF
        } catch (final RuntimeException ignored) {
            // CHECKSTYLE:ON
            poolSize = Runtime.getRuntime().availableProcessors() * 4;
        }
        return new ElasticJobExecutorService("elasticjob-" + jobName, poolSize).createExecutorService();
    }
    
    @Override
    public String getType() {
        return TYPE;
    }
}
