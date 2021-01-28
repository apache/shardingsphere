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

package org.apache.shardingsphere.scaling.core.api;

import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.infra.executor.kernel.thread.ExecutorThreadFactoryBuilder;
import org.apache.shardingsphere.scaling.core.job.ScalingJob;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Job scheduler center.
 */
@Slf4j
public final class JobSchedulerCenter {
    
    private static final Map<String, ScalingJob> SCALING_JOB_MAP = Maps.newConcurrentMap();
    
    private static final ScheduledExecutorService JOB_PERSIST_EXECUTOR = Executors.newSingleThreadScheduledExecutor(ExecutorThreadFactoryBuilder.build("scaling-job-persist-%d"));
    
    private static final RegistryRepositoryAPI REGISTRY_REPOSITORY_API = new RegistryRepositoryAPIImpl();
    
    static {
        JOB_PERSIST_EXECUTOR.scheduleWithFixedDelay(new PersistJobContextRunnable(), 1, 1, TimeUnit.MINUTES);
    }
    
    /**
     * Add job.
     *
     * @param scalingJob scheduler job
     */
    public static void addJob(final ScalingJob scalingJob) {
        SCALING_JOB_MAP.put(String.format("%d-%d", scalingJob.getJobId(), scalingJob.getShardingItem()), scalingJob);
    }
    
    /**
     * Remove job.
     *
     * @param scalingJob scheduler job
     */
    public static void removeJob(final ScalingJob scalingJob) {
        SCALING_JOB_MAP.remove(String.format("%d-%d", scalingJob.getJobId(), scalingJob.getShardingItem()));
    }
    
    private static final class PersistJobContextRunnable implements Runnable {
        
        @Override
        public void run() {
            for (Map.Entry<String, ScalingJob> entry : SCALING_JOB_MAP.entrySet()) {
                try {
                    REGISTRY_REPOSITORY_API.persistJobPosition(entry.getValue());
                    // CHECKSTYLE:OFF
                } catch (final Exception ex) {
                    // CHECKSTYLE:ON
                    log.error("persist job {} context failed.", entry.getKey(), ex);
                }
            }
        }
    }
}
