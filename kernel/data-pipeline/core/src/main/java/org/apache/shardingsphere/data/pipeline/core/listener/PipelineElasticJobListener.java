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

package org.apache.shardingsphere.data.pipeline.core.listener;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.elasticjob.infra.listener.ElasticJobListener;
import org.apache.shardingsphere.elasticjob.infra.listener.ShardingContexts;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Pipeline elastic job listener.
 */
@Slf4j
public final class PipelineElasticJobListener implements ElasticJobListener {
    
    // TODO ElasticJobListenerFactory.createListener return new class instance, it's the reason why static variables
    private static final Map<String, Long> RUNNING_JOBS = new ConcurrentHashMap<>();
    
    @Override
    public void beforeJobExecuted(final ShardingContexts shardingContexts) {
        if (RUNNING_JOBS.containsKey(shardingContexts.getJobName())) {
            log.warn("{} already exists", shardingContexts.getJobName());
        }
        RUNNING_JOBS.put(shardingContexts.getJobName(), System.currentTimeMillis());
    }
    
    @Override
    public void afterJobExecuted(final ShardingContexts shardingContexts) {
        log.info("After {} job execute ", shardingContexts.getJobName());
        RUNNING_JOBS.remove(shardingContexts.getJobName());
    }
    
    /**
     * Is job running.
     *
     * @param jobId job id
     * @return true if job is running otherwise false
     */
    public boolean isJobRunning(final String jobId) {
        return RUNNING_JOBS.containsKey(jobId);
    }
    
    @Override
    public String getType() {
        return PipelineElasticJobListener.class.getName();
    }
}
